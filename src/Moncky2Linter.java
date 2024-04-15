import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Moncky2Linter {
    //TODO add:
    // - * extra * count spaces in each line and mark the line as warning if too much whitespace used
    // - check validity of hex/octal/binary numbers
    public static void main(String[] args) {
        System.out.println(ANSI_RESET); //set console/terminal text color to white
        Moncky2Linter m2l = new Moncky2Linter();
        m2l.runCheck();
    }

    private final String[] commands;

    public Moncky2Linter() {
        String codeContent;
        try {
            Path filePath = Path.of("moncky2in/code.txt");
            codeContent = Files.readString(filePath);
        }
        catch (IOException e) {
            System.out.println("no code file in moncky2in directory (code.txt)");
            throw new RuntimeException(e);
        }
        commands = codeContent.split("\n");
    }

    public Moncky2Linter(String codeContent){
        commands = codeContent.split("\n");
    }

    public void runCheck() {
        for (int i = 0; i < commands.length; i++){
            checkCommand(commands[i], i+1);
        }
    }

    /**
     * this method makes sure that a label exists and does not have duplicates
     * @param label the String value of a label that the method looks for
     */
    public void checkForLabel(String label, int lineNumber) {
        boolean found = false;
        for (int i = 0; i < commands.length; i++){
            if (commands[i].startsWith(":")){
                if (found){
                    if (commands[i].strip().equals(label)) {
                        System.out.println(lineNumber + WARNING_LABEL + " : duplicate label \"" + label + "\" found in the code at line " + (i +1) );
                    }
                }
                if (commands[i].strip().equals(label)) {
                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println(lineNumber + ERROR_LABEL + " : label " + label + " not found in code");
        }
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    private final String ERROR_SYNTAX = String.format(": [%sSYNTAX%s]", ANSI_RED, ANSI_RESET );
    private final String ERROR_VALUE = String.format(": [%sVALUE%s]", ANSI_RED, ANSI_RESET );
    private final String ERROR_ARGUMENT = String.format(": [%sARGUMENT%s]", ANSI_RED, ANSI_RESET );
    private final String ERROR_UNKNOWN = String.format(": [%sUNKNOWN INSTRUCTION%s]", ANSI_RED, ANSI_RESET);
    private final String ERROR_LABEL = String.format(": [%sMISSING LABEL%s]", ANSI_RED, ANSI_RESET);
    private final String WARNING_LABEL = String.format(": [%sDUPLICATE LABEL%s]", ANSI_YELLOW, ANSI_RESET);
    private final String WARNING = String.format(": [%sWARNING%s]", ANSI_YELLOW, ANSI_RESET );
    //private final String WARNING_MINOR = String.format(": [%sMINOR WARNING%s]", ANSI_YELLOW, ANSI_RESET );

    /**
     * this method checks syntax, values, and good practices in assembly code
     * @param codeLine the String value of a line of code
     * @param lineNumber the current instruction index
     */
    public void checkCommand(String codeLine, int lineNumber) {
        String[] codeLineParts = codeLine.trim().split(" ");

        if (codeLineParts[0].isEmpty()) return;
        if (codeLineParts[0].startsWith(";")) { //comment, ignore
            return;
        }
        if (codeLineParts[0].startsWith(":")) { // label, check if used somewhere else
            return;
        }
        if (codeLineParts[0].equals("halt")){ // halt
            try {
                if (!codeLineParts[1].isEmpty() && !codeLineParts[1].startsWith(";")){
                    System.out.println(lineNumber + ERROR_ARGUMENT + " : unknown argument after halt instruction");
                }
            }
            catch (IndexOutOfBoundsException ignored) {}
            return;
        }
        if (codeLineParts[0].equals("li")){ // li rxx, x...
            try {
                if (!codeLineParts[1].startsWith("r")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in st/ld instruction. Should be a register followed by a comma \"r??,\"");
                }
                if (!codeLineParts[1].endsWith(",")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in li instruction. Missing a comma after register \"r??,\"");
                }
                if (codeLineParts[1].startsWith("r") && codeLineParts[1].endsWith(",")){
                    //check if number of register 1 is valid
                    short number = Short.parseShort(codeLineParts[1].substring(1, codeLineParts[1].length()-1));
                    if (number > 15){
                        System.out.println(lineNumber + ERROR_VALUE + " : register number \"" + codeLineParts[1] + "\" in li instruction is too high (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                    if (number < 0){
                        System.out.println(lineNumber + ERROR_VALUE + " : register number \"" + codeLineParts[1] + "\" in li instruction is too low (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                    if (number == 15){
                        System.out.println(lineNumber + WARNING + " : changing register number 15 will change the index of the next executed instruction. " + ANSI_GREEN +"Use 'jp' instead to jump to a instruction" + ANSI_RESET);
                    }
                }

            } catch (IndexOutOfBoundsException ignored) {
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing first argument in li instruction (register)");
            } catch (NumberFormatException ignored){
                System.out.println(lineNumber + ERROR_VALUE + " : register \"" + codeLineParts[1] + "\" in li instruction does not contain a valid register number");
            }
            //second argument for 'li' instruction
            try {
                if (codeLineParts[2].startsWith("0x")){
                    short number = (short) NumberConverter.hexStringToDecimal(codeLineParts[2].substring(2));
                    if (number > 255){
                        System.out.println(lineNumber + ERROR_VALUE + " : value \"" + codeLineParts[2] + "\" in li instruction is too great. " + ANSI_RED + "value is more than 8 bits" + ANSI_RESET);
                    }
                    return;
                }
                if (codeLineParts[2].startsWith("0b")){
                    short number = (short) NumberConverter.binaryStringToDecimal(codeLineParts[2].substring(2));
                    if (number > 255){
                        System.out.println(lineNumber + ERROR_VALUE + " : value \"" + codeLineParts[2] + "\" in li instruction is too great. " + ANSI_RED + "value is more than 8 bits" + ANSI_RESET);
                    }
                    return;
                }
                if (codeLineParts[2].startsWith("0o")){
                    short number = (short) NumberConverter.octalStringToDecimal(codeLineParts[2].substring(2));
                    if (number > 255){
                        System.out.println(lineNumber + ERROR_VALUE + " : value \"" + codeLineParts[2] + "\" in li instruction is too great. " + ANSI_RED + "value is more than 8 bits" + ANSI_RESET);
                    }
                    return;
                }
                if (codeLineParts[2].startsWith(":")){
                    checkForLabel(codeLineParts[2], lineNumber);
                    return; //label found TODO add label search and check
                }

                short number = Short.parseShort(codeLineParts[2]);
                if (number > 255){
                    System.out.println(lineNumber + ERROR_VALUE + " : value \"" + codeLineParts[2] + "\" in li instruction is too great. " + ANSI_RED + "value is more than 8 bits" + ANSI_RESET);
                }
            } catch (IndexOutOfBoundsException ignored) {
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing second argument in li instruction (immediate value)");
            } catch (NumberFormatException ignored){
                System.out.println(lineNumber + ERROR_VALUE + " : value \"" + codeLineParts[2] + "\" in li instruction does not contain a valid number");
            }
            return;
        }
        if (codeLineParts[0].equals("ld") || codeLineParts[0].equals("st")){ // ld rxx, (rxx) // st rxx, (rxx)
            try {
                if (!codeLineParts[1].startsWith("r")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in st/ld instruction. Should be a register followed by a comma \"r??,\"");
                }
                if (!codeLineParts[1].endsWith(",")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in st/ld instruction. Missing a comma after register \"r??,\"");
                }
                if (codeLineParts[1].startsWith("r") && codeLineParts[1].endsWith(",")){
                    //check if number of register 1 is valid
                    short number = Short.parseShort(codeLineParts[1].substring(1, codeLineParts[1].length() - 1));
                    if (number > 15){
                        System.out.println(lineNumber + ERROR_VALUE + " : first register number \"" + codeLineParts[1] + "\" in st/ld instruction is too high (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                    if (number < 0){
                        System.out.println(lineNumber + ERROR_VALUE + " : first register number \"" + codeLineParts[1] + "\" in st/ld instruction is too low (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                    if (number == 15 && codeLineParts[0].equals("ld")){
                        System.out.println(lineNumber + WARNING + " : changing register number 15 will change the index of the next executed instruction. " + ANSI_GREEN +"Use 'jp' instead to jump to a instruction" + ANSI_RESET);
                    }
                }

            } catch (IndexOutOfBoundsException ignored) {
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing first argument in ld/st instruction (register)");
            } catch (NumberFormatException ignored){
                System.out.println(lineNumber + ERROR_VALUE + " : first register \"" + codeLineParts[1] + "\" in st/ld instruction does not contain a valid number");
            }
            try {
                if (!codeLineParts[2].startsWith("(")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid second argument \"" + codeLineParts[2] +"\" in st/ld instruction. Register is missing an opening parenthesis \"(r??)\"");
                }
                if (!codeLineParts[2].endsWith(")")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid second argument \"" + codeLineParts[2] +"\" in st/ld instruction. Register is missing a closing parenthesis \"(r??)\"");
                }
                if (codeLineParts[2].startsWith("(r") && codeLineParts[2].endsWith(")")){
                    //check if number of register 1 is valid
                    short number = Short.parseShort(codeLineParts[2].substring(2, codeLineParts[2].length() - 1));
                    if (number > 15){
                        System.out.println(lineNumber + ERROR_VALUE + " : second register number \"" + codeLineParts[2] + "\" in st/ld instruction is too high (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                    if (number < 0){
                        System.out.println(lineNumber + ERROR_VALUE + " : second register number \"" + codeLineParts[2] + "\" in st/ld instruction is too low (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                }
            } catch (IndexOutOfBoundsException ignored) {
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing first argument in ld/st instruction (register)");
            } catch (NumberFormatException ignored){
                System.out.println(lineNumber + ERROR_VALUE + " : second register \"" + codeLineParts[1] + "\" in st/ld instruction does not contain a valid number");
            }
            return;
        }
        if (codeLineParts[0].equals("jp")){ //jp rxx
            try {
                if (!codeLineParts[1].startsWith("r")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in jp instruction. Should be a register followed by a comma \"r??,\"");
                }
                if (codeLineParts[1].startsWith("r")){
                    //check if number of register 1 is valid
                    short number = Short.parseShort(codeLineParts[1].substring(1));
                    if (number > 15){
                        System.out.println(lineNumber + ERROR_VALUE + " : register number \"" + codeLineParts[1] + "\" in jp instruction is too high (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                    if (number < 0){
                        System.out.println(lineNumber + ERROR_VALUE + " : register number \"" + codeLineParts[1] + "\" in jp instruction is too low (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                    if (number == 15){
                        System.out.println(lineNumber + WARNING + " : using register 15 with jp instruction will jump to current instruction and result in an infinite loop. " + ANSI_RED + "AVOID" + ANSI_RESET);
                    }
                }
            } catch (IndexOutOfBoundsException ignored) {
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing argument in jp instruction (register)");
            } catch (NumberFormatException ignored){
                System.out.println(lineNumber + ERROR_VALUE + " : register \"" + codeLineParts[1] + "\" in jp instruction does not contain a valid number");
            }
            return;
        }
        else if (codeLineParts[0].startsWith("jp")){
            if (codeLineParts[0].equals("jps") ||
                    codeLineParts[0].equals("jpns") ||
                    codeLineParts[0].equals("jpz") ||
                    codeLineParts[0].equals("jpnz") ||
                    codeLineParts[0].equals("jpo") ||
                    codeLineParts[0].equals("jpno") ||
                    codeLineParts[0].equals("jpc") ||
                    codeLineParts[0].equals("jpnc")
            ) {
                String i = codeLineParts[0];
                try {
                    if (!codeLineParts[1].startsWith("r")) {
                        System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] + "\" in " + i + " instruction. Should be a register followed by a comma \"r??,\"");
                    }
                    if (codeLineParts[1].startsWith("r")) {
                        //check if number of register 1 is valid
                        short number = Short.parseShort(codeLineParts[1].substring(1));
                        if (number > 15) {
                            System.out.println(lineNumber + ERROR_VALUE + " : register number \"" + codeLineParts[1] + "\" in " + i + " instruction is too high (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                        }
                        if (number < 0) {
                            System.out.println(lineNumber + ERROR_VALUE + " : register number \"" + codeLineParts[1] + "\" in " + i + " instruction is too low (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                        }
                        if (number == 15) {
                            System.out.println(lineNumber + WARNING + " : using register 15 with jp instruction will jump to current instruction and result in an infinite loop. " + ANSI_RED + "AVOID" + ANSI_RESET);
                        }
                    }

                } catch (IndexOutOfBoundsException ignored) {
                    System.out.println(lineNumber + ERROR_ARGUMENT + " : missing argument in " + i + " instruction (register)");
                } catch (NumberFormatException ignored) {
                    System.out.println(lineNumber + ERROR_VALUE + " : register \"" + codeLineParts[1] + "\" in " + i + " instruction does not contain a valid number");
                }
            }
            else {
                System.out.println(lineNumber + ERROR_UNKNOWN + " : conditional jump instruction \"" + codeLineParts[0] + "\" does not exist");
            }
            return;
        }
        //funny long if statement that covers all the ALU operation instructions as they use the same syntax
        if (codeLineParts[0].equals("nop") ||
                codeLineParts[0].equals("or") ||
                codeLineParts[0].equals("and") ||
                codeLineParts[0].equals("xor") ||
                codeLineParts[0].equals("add") ||
                codeLineParts[0].equals("sub") ||
                codeLineParts[0].equals("shl") ||
                codeLineParts[0].equals("shr") ||
                codeLineParts[0].equals("ashr") ||
                codeLineParts[0].equals("not") ||
                codeLineParts[0].equals("neg")
        ){
            try {
                if (!codeLineParts[1].startsWith("r")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in " + codeLineParts[0] + " instruction. Should be a register followed by a comma \"r??,\"");
                }
                if (!codeLineParts[1].endsWith(",")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in " + codeLineParts[0] + " instruction. Missing a comma after register \"r??,\"");
                }
                if (codeLineParts[1].startsWith("r") && codeLineParts[1].endsWith(",")){
                    //check if number of register 1 is valid
                    short number = Short.parseShort(codeLineParts[1].substring(1, codeLineParts[1].length()-1));
                    if (number > 15){
                        System.out.println(lineNumber + ERROR_VALUE + " : register number \"" + codeLineParts[1] + "\" in " + codeLineParts[0] + " instruction is too high (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                    if (number < 0){
                        System.out.println(lineNumber + ERROR_VALUE + " : register number \"" + codeLineParts[1] + "\" in " + codeLineParts[0] + " instruction is too low (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                    if (number == 15){
                        System.out.println(lineNumber + WARNING + " : changing register number 15 will change the index of the next executed instruction. " + ANSI_GREEN +"Use 'jp' instead to jump to a instruction" + ANSI_RESET);
                    }
                }

            } catch (IndexOutOfBoundsException ignored) {
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing first argument in " + codeLineParts[0] + " instruction (register)");
            } catch (NumberFormatException ignored){
                System.out.println(lineNumber + ERROR_VALUE + " : register \"" + codeLineParts[1] + "\" in " + codeLineParts[0] + " instruction does not contain a valid register number");
            }

            try {
                if (!codeLineParts[2].startsWith("r")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid second argument \"" + codeLineParts[2] +"\" in " + codeLineParts[0] + " instruction. Should be a register \"r??\"");
                }
                if (codeLineParts[2].startsWith("r")){
                    //check if number of register 2 is valid
                    short number = Short.parseShort(codeLineParts[2].substring(1));
                    if (number > 15){
                        System.out.println(lineNumber + ERROR_VALUE + " : register number \"" + codeLineParts[2] + "\" in " + codeLineParts[0] + " instruction is too high (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                    if (number < 0){
                        System.out.println(lineNumber + ERROR_VALUE + " : register number \"" + codeLineParts[2] + "\" in " + codeLineParts[0] + " instruction is too low (0-15). " + ANSI_RED + "No register with number " + number + ANSI_RESET);
                    }
                }

            } catch (IndexOutOfBoundsException ignored) {
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing second argument in " + codeLineParts[0] + " instruction (register)");
            } catch (NumberFormatException ignored){
                System.out.println(lineNumber + ERROR_VALUE + " : second register \"" + codeLineParts[2] + "\" in " + codeLineParts[0] + " instruction does not contain a valid register number");
            }
            return;
        }
        System.out.println(lineNumber + ERROR_UNKNOWN + " : unknown command \"" + codeLineParts[0] + "\".");

    }
}
