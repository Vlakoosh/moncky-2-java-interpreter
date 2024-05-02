import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Moncky2Linter {
    //TODO add:
    // - * extra * count spaces in each line and mark the line as warning if too much whitespace used
    public static void main(String[] args) {
        System.out.println(ANSI_RESET); //set console/terminal text color to white
        Moncky2Linter m2l = new Moncky2Linter();
        m2l.runCheck();
    }

    private final String[] commands;

    /**
     * this default constructor loads commands from input file in moncky2in folder
     */
    public Moncky2Linter() {
        String codeContent;
        try {
            //open the input file
            Path filePath = Path.of("moncky2in/code.txt");
            codeContent = Files.readString(filePath);
        }
        catch (IOException e) {
            //if no file is found, throw an error.
            System.out.println("no code file in moncky2in directory (code.txt)");
            throw new RuntimeException(e);
        }
        commands = codeContent.split("\n");
    }

    /**
     * this constructor loads code from a string block rather than a file. This is good for checking small pieces of code
     * @param codeContent String code block with instructions that need to be checked
     */
    public Moncky2Linter(String codeContent){
        //split code String block into separate lines and store them in an array
        commands = codeContent.split("\n");
    }

    public void runCheck() {
        //go through every line of source code and check it (syntax, values, good practices)
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
        //go through each line of source code (instruction), check if it's a label and compare it to target label
        for (int i = 0; i < commands.length; i++){
            if (commands[i].startsWith(":")){
                if (found){
                    //give a warning if there is a duplicate label
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
            //give an error warning if there is no matching label in code
            System.out.println(lineNumber + ERROR_LABEL + " : label " + label + " not found in code");
        }
    }

    //terminal colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    //warning and error messages
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

        if (codeLine.isBlank()) {
            return;
        }
        System.out.println(lineNumber);

        //split each instruction into words/parts
        String[] codeLineParts = codeLine.split(" ");
        ArrayList<String> code = new ArrayList<>();
        for (String codeLinePart : codeLineParts) {
            if (!codeLinePart.isEmpty()) {
                code.add(codeLinePart);
            }
        }
        codeLineParts = new String[code.size()];
        for (int i = 0; i < codeLineParts.length; i++){
            codeLineParts[i] = code.get(i);
        }

        //ignore empty lines of code
        if (codeLineParts[0].isEmpty()) return;
        //ignore comments
        if (codeLineParts[0].startsWith(";")) { //comment, ignore
            return;
        }
        //ignore labels. They don't need to be checked unless loaded into a register
        if (codeLineParts[0].startsWith(":")) { // label, check if used somewhere else
            return;
        }
        //check the halt instruction.
        if (codeLineParts[0].equals("halt")){ // halt
            //try-catch block to avoid crashes
            try {
                //give warning if arguments are used with the halt instruction
                if (!codeLineParts[1].isEmpty() && !codeLineParts[1].startsWith(";")){
                    System.out.println(lineNumber + ERROR_ARGUMENT + " : unknown argument after halt instruction");
                    return;
                }
            }
            catch (IndexOutOfBoundsException ignored) {}
            return;
        }
        if (codeLineParts[0].equals("li")){ // li rxx, x...
            try {
                //checking for first register syntax
                if (!codeLineParts[1].startsWith("r")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in st/ld instruction. Should be a register followed by a comma \"r??,\"");
                }
                //checking for first register syntax (comma after register number)
                if (!codeLineParts[1].endsWith(",")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in li instruction. Missing a comma after register \"r??,\"");
                }
                //checking if the instruction contains a valid register number (real number and in range)
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
                //not enough arguments for instruction
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing first argument in li instruction (register)");
            } catch (NumberFormatException ignored){
                //invalid numerical value in instruction
                System.out.println(lineNumber + ERROR_VALUE + " : register \"" + codeLineParts[1] + "\" in li instruction does not contain a valid register number");
            }
            //second argument for 'li' instruction
            try {
                if (codeLineParts[2].startsWith("0x")){
                    short number = (short) NumberConverter.hexStringToDecimal(codeLineParts[2].substring(2));
                    //li instructions can only take 8 bits as input. 8 bits have a range from 0 to 255. Anything greater than that will not fit in an 'li' instruction and shouldn't work
                    if (number > 255){
                        System.out.println(lineNumber + ERROR_VALUE + " : value \"" + codeLineParts[2] + "\" in li instruction is too great. " + ANSI_RED + "value is more than 8 bits" + ANSI_RESET);
                    }
                    return;
                }
                if (codeLineParts[2].startsWith("0b")){
                    short number = (short) NumberConverter.binaryStringToDecimal(codeLineParts[2].substring(2));
                    //li instructions can only take 8 bits as input. 8 bits have a range from 0 to 255. Anything greater than that will not fit in an 'li' instruction and shouldn't work
                    if (number > 255){
                        System.out.println(lineNumber + ERROR_VALUE + " : value \"" + codeLineParts[2] + "\" in li instruction is too great. " + ANSI_RED + "value is more than 8 bits" + ANSI_RESET);
                    }
                    return;
                }
                if (codeLineParts[2].startsWith("0o")){
                    short number = (short) NumberConverter.octalStringToDecimal(codeLineParts[2].substring(2));
                    //li instructions can only take 8 bits as input. 8 bits have a range from 0 to 255. Anything greater than that will not fit in an 'li' instruction and shouldn't work
                    if (number > 255){
                        System.out.println(lineNumber + ERROR_VALUE + " : value \"" + codeLineParts[2] + "\" in li instruction is too great. " + ANSI_RED + "value is more than 8 bits" + ANSI_RESET);
                    }
                    return;
                }
                if (codeLineParts[2].startsWith(":")){
                    //check if the loaded label exists in the code
                    checkForLabel(codeLineParts[2], lineNumber);
                    return;
                }

                short number = Short.parseShort(codeLineParts[2]);
                //li instructions can only take 8 bits as input. 8 bits have a range from 0 to 255. Anything greater than that will not fit in an 'li' instruction and shouldn't work
                if (number > 255){
                    System.out.println(lineNumber + ERROR_VALUE + " : value \"" + codeLineParts[2] + "\" in li instruction is too great. " + ANSI_RED + "value is more than 8 bits" + ANSI_RESET);
                }
            } catch (IndexOutOfBoundsException ignored) {
                //not enough arguments for instruction
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing second argument in li instruction (immediate value)");
            } catch (NumberFormatException ignored){
                //invalid numerical value in instruction
                System.out.println(lineNumber + ERROR_VALUE + " : value \"" + codeLineParts[2] + "\" in li instruction does not contain a valid number");
            }
            return;
        }
        if (codeLineParts[0].equals("ld") || codeLineParts[0].equals("st")){ // ld rxx, (rxx) // st rxx, (rxx)
            try {
                //checking for first register syntax
                if (!codeLineParts[1].startsWith("r")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in st/ld instruction. Should be a register followed by a comma \"r??,\"");
                }
                //checking for first register syntax (comma after register number)
                if (!codeLineParts[1].endsWith(",")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in st/ld instruction. Missing a comma after register \"r??,\"");
                }
                //checking if the instruction contains a valid register number (real number and in range)
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
                //not enough arguments for instruction
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing first argument in ld/st instruction (register)");
            } catch (NumberFormatException ignored){
                //invalid numerical value in instruction
                System.out.println(lineNumber + ERROR_VALUE + " : first register \"" + codeLineParts[1] + "\" in st/ld instruction does not contain a valid number");
            }
            try {
                //checking for second register syntax (opening parenthesis)
                if (!codeLineParts[2].startsWith("(")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid second argument \"" + codeLineParts[2] +"\" in st/ld instruction. Register is missing an opening parenthesis \"(r??)\"");
                }
                //checking for second register syntax (closing parenthesis)
                if (!codeLineParts[2].endsWith(")")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid second argument \"" + codeLineParts[2] +"\" in st/ld instruction. Register is missing a closing parenthesis \"(r??)\"");
                }
                //checking if the instruction contains a valid register number (real number and in range)
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
                //not enough arguments for instruction
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing first argument in ld/st instruction (register)");
            } catch (NumberFormatException ignored){
                //invalid numerical value in instruction
                System.out.println(lineNumber + ERROR_VALUE + " : second register \"" + codeLineParts[1] + "\" in st/ld instruction does not contain a valid number");
            }
            return;
        }
        if (codeLineParts[0].equals("jp")){ //jp rxx
            try {
                //checking for register syntax
                if (!codeLineParts[1].startsWith("r")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in jp instruction. Should be a register followed by a comma \"r??,\"");
                }
                //checking if the instruction contains a valid register number (real number and in range)
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
                //not enough arguments for instruction
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing argument in jp instruction (register)");
            } catch (NumberFormatException ignored){
                //invalid numerical value in instruction
                System.out.println(lineNumber + ERROR_VALUE + " : register \"" + codeLineParts[1] + "\" in jp instruction does not contain a valid number");
            }
            return;
        }
        else if (codeLineParts[0].startsWith("jp")){
            //check if the conditional jump uses the correct flag, if not, give an error warning
            if (codeLineParts[0].equals("jps") ||
                    codeLineParts[0].equals("jpns") ||
                    codeLineParts[0].equals("jpz") ||
                    codeLineParts[0].equals("jpnz") ||
                    codeLineParts[0].equals("jpo") ||
                    codeLineParts[0].equals("jpno") ||
                    codeLineParts[0].equals("jpc") ||
                    codeLineParts[0].equals("jpnc")
            ) {
                String i = codeLineParts[0]; // save the instruction name to avoid repeating codeLineParts[0] (readability)
                try {
                    //checking for register syntax
                    if (!codeLineParts[1].startsWith("r")) {
                        System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] + "\" in " + i + " instruction. Should be a register followed by a comma \"r??,\"");
                    }
                    //checking if the instruction contains a valid register number (real number and in range)
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
                    //not enough arguments for instruction
                    System.out.println(lineNumber + ERROR_ARGUMENT + " : missing argument in " + i + " instruction (register)");
                } catch (NumberFormatException ignored) {
                    //invalid numerical value in instruction
                    System.out.println(lineNumber + ERROR_VALUE + " : register \"" + codeLineParts[1] + "\" in " + i + " instruction does not contain a valid number");
                }
            }
            else {
                //any conditional jump that uses a non-default flag will give an error warning
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
                //checking for first register syntax
                if (!codeLineParts[1].startsWith("r")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in " + codeLineParts[0] + " instruction. Should be a register followed by a comma \"r??,\"");
                }
                //checking for first register syntax (comma after register number)
                if (!codeLineParts[1].endsWith(",")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in " + codeLineParts[0] + " instruction. Missing a comma after register \"r??,\"");
                }
                //checking if the instruction contains a valid register number (real number and in range)
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
                //not enough arguments for instruction
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing first argument in " + codeLineParts[0] + " instruction (register)");
            } catch (NumberFormatException ignored){
                //invalid numerical value in instruction
                System.out.println(lineNumber + ERROR_VALUE + " : register \"" + codeLineParts[1] + "\" in " + codeLineParts[0] + " instruction does not contain a valid register number");
            }
            try {
                //checking for second register syntax
                if (!codeLineParts[2].startsWith("r")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid second argument \"" + codeLineParts[2] +"\" in " + codeLineParts[0] + " instruction. Should be a register \"r??\"");
                }
                //checking if the instruction contains a valid register number (real number and in range)
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
                //not enough arguments for instruction
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing second argument in " + codeLineParts[0] + " instruction (register)");
            } catch (NumberFormatException ignored){
                //invalid numerical value in instruction
                System.out.println(lineNumber + ERROR_VALUE + " : second register \"" + codeLineParts[2] + "\" in " + codeLineParts[0] + " instruction does not contain a valid register number");
            }
            return;
        }
        //command not recognized by linter
        System.out.println(lineNumber + ERROR_UNKNOWN + " : unknown command \"" + codeLineParts[0] + "\".");
    }
}
