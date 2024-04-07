public class Moncky2Linter {

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
        if (codeLineParts[0].equals("ld")){ // ld rxx, (rxx)

        }
        if (codeLineParts[0].equals("st")){ // st rxx, (rxx)

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
