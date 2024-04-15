public class Moncky2Linter {
    //TODO add:
    // - count spaces in each line and mark the line as warning if too much whitespace used
    //syntax check all commands
    public static void main(String[] args) {
        System.out.println(ANSI_RESET); //set console/terminal text color to white
        String testCommand = "ld r15, (r35";
        Moncky2Linter m2l = new Moncky2Linter();
        m2l.checkCommand(testCommand, 15);
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    private final String ERROR_SYNTAX = String.format(": [%sSYNTAX%s]", ANSI_RED, ANSI_RESET );
    private final String ERROR_VALUE = String.format(": [%sVALUE%s]", ANSI_RED, ANSI_RESET );
    private final String ERROR_ARGUMENT = String.format(": [%sARGUMENT%s]", ANSI_RED, ANSI_RESET );
    private final String WARNING = String.format(": [%sWARNING%s]", ANSI_YELLOW, ANSI_RESET );
    private final String WARNING_MINOR = String.format(": [%sMINOR WARNING%s]", ANSI_YELLOW, ANSI_RESET );

    public void checkCommand(String codeLine, int lineNumber) {
        String[] codeLineParts = codeLine.trim().split(" ");

        if (codeLineParts[0].startsWith(";")) { //comment, ignore
            return;
        }
        if (codeLineParts[0].startsWith(":")) { // label, check if used somewhere else
            return;
        }
        if (codeLineParts[0].equals("halt")){ // halt
            try {
                if (!codeLineParts[1].isEmpty()){
                    System.out.println("unknown argument after 'halt' instruction at code line " + lineNumber + ".");
                }
            }
            catch (IndexOutOfBoundsException ignored) {}
        }
        if (codeLineParts[0].equals("li")){ // li rxx, x...

        }
        if (codeLineParts[0].equals("ld") || codeLineParts[0].equals("st")){ // ld rxx, (rxx) // st rxx, (rxx)
            try {
                if (!codeLineParts[1].startsWith("r")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in st/ld command. Should be a register followed by a comma \"r??,\"");
                }
                if (!codeLineParts[1].endsWith(",")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid argument \"" + codeLineParts[1] +"\" in st/ld command. Missing a comma after register \"r??,\"");
                }
                if (codeLineParts[1].startsWith("r") && codeLineParts[1].endsWith(",")){
                    //check if number of register 1 is valid
                    short number = Short.parseShort(codeLineParts[1].substring(1, codeLineParts[1].length() - 1));
                    if (number > 15){
                        System.out.println(lineNumber + ERROR_VALUE + " : first register number \"" + codeLineParts[1] + "\" in st/ld instruction is too high (0-15)");
                    }
                    if (number < 0){
                        System.out.println(lineNumber + ERROR_VALUE + " : first register number \"" + codeLineParts[1] + "\" in st/ld instruction is too low (0-15)");
                    }
                    if (number == 15 && codeLineParts[0].equals("ld")){
                        System.out.println(lineNumber + WARNING + " : changing register number 15 will change the index of the next executed instruction. " + ANSI_GREEN +"Use 'jp' instead to jump to a command" + ANSI_RESET);
                    }
                }

            } catch (IndexOutOfBoundsException ignored) {
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing first argument in ld/st command (register)");
            } catch (NumberFormatException ignored){
                System.out.println(lineNumber + ERROR_VALUE + " : first register \"" + codeLineParts[1] + "\" in st/ld instruction does not contain a valid number");
            }
            try {
                if (!codeLineParts[2].startsWith("(")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid second argument \"" + codeLineParts[2] +"\" in st/ld command. Register is missing an opening parenthesis \"(r??)\"");
                }
                if (!codeLineParts[2].endsWith(")")){
                    System.out.println(lineNumber + ERROR_SYNTAX + " : invalid second argument \"" + codeLineParts[2] +"\" in st/ld command. Register is missing a closing parenthesis \"(r??)\"");
                }
                if (codeLineParts[2].startsWith("(r") && codeLineParts[2].endsWith(")")){
                    //check if number of register 1 is valid
                    short number = Short.parseShort(codeLineParts[2].substring(2, codeLineParts[2].length() - 1));
                    if (number > 15){
                        System.out.println(lineNumber + ERROR_VALUE + " : second register number \"" + codeLineParts[2] + "\" in st/ld instruction is too high (0-15)");
                    }
                    if (number < 0){
                        System.out.println(lineNumber + ERROR_VALUE + " : second register number \"" + codeLineParts[2] + "\" in st/ld instruction is too low (0-15)");
                    }
                }
            } catch (IndexOutOfBoundsException ignored) {
                System.out.println(lineNumber + ERROR_ARGUMENT + " : missing first argument in ld/st command (register)");
            } catch (NumberFormatException ignored){
                System.out.println(lineNumber + ERROR_VALUE + " : second register \"" + codeLineParts[1] + "\" in st/ld instruction does not contain a valid number");
            }
        }
        if (codeLineParts[0].equals("jp")){ //jp rxx

        }
        if (codeLineParts[0].equals("jp")){ //jpc rxx
            return;
        }
        if (codeLineParts[0].startsWith("jp")){
            return;
        }
        /* //THESE ARE ALL THE SAME just different names
        if (codeLineParts[0].equals("nop")){
            return;
        }
        if (codeLineParts[0].equals("or")){
            return;
        }
        if (codeLineParts[0].equals("and")){
            return;
        }
        if (codeLineParts[0].equals("xor")){
            return;
        }
        if (codeLineParts[0].equals("add")){
            return;
        }
        if (codeLineParts[0].equals("sub")){
            return;
        }
        if (codeLineParts[0].equals("shl")){
            return;
        }
        if (codeLineParts[0].equals("shr")){
            return;
        }
        if (codeLineParts[0].equals("ashr")){
            return;
        }
        if (codeLineParts[0].equals("not")){
            return;
        }
        if (codeLineParts[0].equals("neg")){

        }
        */
    }
}
