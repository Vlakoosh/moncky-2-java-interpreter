import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Moncky2Interpreter {

    //a list of registers for the cpu. r0-r15
    private final short[] register = new short[16];
    //"ram" memory storing 65536 16-bit numbers
    private final short[] memory = new short[65536];
    //stores the result of the previous ALU operation
    private short ALU = 0;
    //flags for the 4 ALU outputs. can either be 1 or 0
    private short FLAG_carry = 0;
    private short FLAG_zero = 0;
    private short FLAG_sign = 0;
    private short FLAG_overflow = 0;

    //list of commands/instructions without any blank lines, comments, or labels
    private String[] commands;
    //list of Commands/instructions including all the spaces comments and labels
    //used when checking for position of a label and stores all the raw code
    private String[] commandsWithLabels;
    public ArrayList<String> compiledBinaryCommands = new ArrayList<>();

    /**
     * constructor with code parameter
     * sets the commandsWithLabels and commands attributes
     * @param code
     */
    public Moncky2Interpreter(String code){
        commandsWithLabels = code.split("\n");
        commandsWithLabels = stripEmptyCommands(commandsWithLabels);
        commands = removeLabelsAndComments(commandsWithLabels);
    }

    //default constructor
    public Moncky2Interpreter(){}


    /*
    When you run the interpreter:
    - read raw code text from a file
    - split the raw code into a list of lines of code
    - Create a new interpreter instance
    - run .interpretCode to read the raw commands
    - after all code is simulated, print the CPU and memory information
     */
    public static void main(String[] args) {
        String codeContent;
        try {
            Path filePath = Path.of("moncky2in/code.txt");
            codeContent = Files.readString(filePath);
        }
        catch (IOException e) {
            System.out.println("no code file in moncky2in directory (code.txt)");
            throw new RuntimeException(e);
        }

        Moncky2Interpreter m2i = new Moncky2Interpreter();
        m2i.interpretCode(codeContent);
        m2i.printCPU();

    }

    /**
     * this method goes through every single command and updates the registers and memory accordingly
     * it also resets the ALU variable and register 15
     * @param moncky2Code raw code that needs to be executed/simulated
     */
    public void interpretCode(String moncky2Code) {
        //reset the code attributes
        commandsWithLabels = moncky2Code.split("\n");
        commandsWithLabels = stripEmptyCommands(commandsWithLabels);
        commands = removeLabelsAndComments(commandsWithLabels);
        //reset ALU and register 15 to start code at line 0
        ALU = 0;
        FLAG_overflow = 0;
        FLAG_sign = 0;
        FLAG_zero = 0;
        FLAG_carry = 0;
        register[15] = 0;
        //halt command returns -1, ending the simulation
        //jump commands/instructions return positive numbers, which will update the current code line that's executed
        //all other commands/instructions return 0, which leads to the next command to be read as normal
        while (true) {
            int commandResult = executeCommand(commands[register[15]]);
            if (commandResult < 0) break;
            if (commandResult > 0) register[15] = (short) (commandResult);
            register[15]++; //register 15 stores the current command executed
        }
    }

    /**
     * this method strips code from labels, comments, and empty rows
     * @param rawCommands - list of instructions Strings that includes empty lines, comments, and labels
     * @return - list of instructions Strings without empty lines, comments, and labels
     */
    public static String[] removeLabelsAndComments(String[] rawCommands){
        ArrayList<String> commandsList = new ArrayList<String>();
        for (String command : rawCommands) {
            command = command.strip();
            if (!command.startsWith(":") && !command.startsWith(";") && !command.isEmpty()){
                commandsList.add(command);
            }
        }
        //convert ArrayList back to array
        String[] commands = new String[commandsList.size()];
        for (int i = 0; i < commands.length; i++) {
            commands[i] = commandsList.get(i);
        }
        return commands;
    }

    /**
     * this method strips code from empty rows
     * @param rawCommands - list of instructions Strings including empty lines
     * @return - list of instructions Strings without empty lines
     */
    public static String[] stripEmptyCommands(String[] rawCommands){
        ArrayList<String> commandsList = new ArrayList<String>();
        for (String command : rawCommands) {
            command = command.strip();
            if (!command.isEmpty()){
                commandsList.add(command);
            }
        }
        //convert ArrayList back to array
        String[] commands = new String[commandsList.size()];
        for (int i = 0; i < commands.length; i++) {
            commands[i] = commandsList.get(i);
        }
        return commands;
    }
    //getter for compiledBinaryCommands. Only used in compiler
    public ArrayList<String> getCompiledBinaryCommands() {
        return compiledBinaryCommands;
    }

    //this method prints registers 0-15 and all memory/ram locations not equal to 0
    public void printCPU() {
        for (int i = 0; i < register.length; i++) {
            System.out.println("register " + i + ": " + register[i]);
        }
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != 0) {
                System.out.println("memory at #" + i + ": " + memory[i]);
            }
        }
    }

    //getter for returning stripped command list
    public String[] getCommands() {
        return commands;
    }

    /**
     *
     * @param command executed command/instruction as text
     * @return -1 to terminate code, positive number to jump to another instruction, or 0 for nothing
     */
    public int executeCommand(String command) {
        String[] commandParts = CommandReader.getCommandParts(command);

        //halt command. Stops execution (halt)
        if (commandParts[0].equalsIgnoreCase("halt")) {
            compiledBinaryCommands.add("0000000000000000");
            return -1;
        }
        //load immediate command (li r, i)
        if (commandParts[0].equals("li")) {
            int registerNumber; //number of register in which immediateValue is stored
            short immediateValue; //value stored in the register

            //check if register number is 1 or 2 digit (0-15)
            if (commandParts[1].length() == 4) registerNumber = Integer.parseInt(commandParts[1].substring(1, 3));
            else registerNumber = Integer.parseInt(commandParts[1].substring(1, 2));

            //save the immediate value from command

            //if the value is a label:
            //- find label in code
            //- set the value to the place where the label is found
            if (commandParts[2].charAt(0) == ':'){
                String label = commandParts[2];
                immediateValue = -1;
                int count = 0;
                boolean found = false;
                for (String commandWithLabel : commandsWithLabels) {
                    if (!commandWithLabel.strip().startsWith(":") && !commandWithLabel.strip().startsWith(";")) {
                        count++;
                    }
                    if (commandWithLabel.strip().equals(label)) {
                        immediateValue = (short) (count-1);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    //throw an exception when no label is found in code
                    throw new RuntimeException("label not found in code");
                }
            } else if (commandParts[2].startsWith("0x")) {
                //load hex value
                immediateValue = (short) NumberConverter.hexStringToDecimal(commandParts[2].substring(2));
            } else if (commandParts[2].startsWith("0b")) {
                //load binary value
                immediateValue = (short) NumberConverter.binaryStringToDecimal(commandParts[2].substring(2));
            } else if (commandParts[2].startsWith("0o")) {
                //load octal value
                immediateValue = (short) NumberConverter.octalStringToDecimal(commandParts[2].substring(2));
            } else {
                //load decimal value
                immediateValue = Short.parseShort(commandParts[2]);
                if (immediateValue > 255){
                    throw new RuntimeException("number loaded into register r" + registerNumber + " is too great. Consider using a bit shift instead");
                }
            }

            //load immediateValue into the register
            register[registerNumber] = immediateValue;

            //add command to compiler
            compiledBinaryCommands.add("0001" + NumberConverter.decimalToBinaryString(immediateValue, 8) + NumberConverter.decimalToBinaryString(registerNumber, 4));
            return 0;
        }
        if (commandParts[0].equals("ld")) {
            //get register numbers from command part 2 and 3 "r__, (r__)"
            int firstRegisterNumber = getMemoryRegister1(commandParts[1]);
            int secondRegisterNumber = getMemoryRegister2(commandParts[2]);

            //store value in RAM
            register[firstRegisterNumber] = memory[register[secondRegisterNumber]];

            //add the command to compiler
            compiledBinaryCommands.add("10000000" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

            return 0;
        }
        if (commandParts[0].equals("st")) {
            //get register numbers from command part 2 and 3 "r__, (r__)"
            int firstRegisterNumber = getMemoryRegister1(commandParts[1]);
            int secondRegisterNumber = getMemoryRegister2(commandParts[2]);

            //store value in RAM
            memory[register[secondRegisterNumber]] = register[firstRegisterNumber];

            //add the command to compiler
            compiledBinaryCommands.add("10100000" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

            return 0;
        }
        if (commandParts[0].equals("jp")) {
            //get register number from command part 2 "r__"
            int registerNumber = getJumpRegisterNumber(commandParts[1]);

            //kind of redundant since it's also done in interpretCode method, but: sets the next code line to be read
            //I will leave this just in case it breaks something again. Debugging jumps is a pain
            register[15] = register[registerNumber];

            //add the command to compiler
            compiledBinaryCommands.add("110000000000" + NumberConverter.decimalToBinaryString(registerNumber, 4));

            //return the code line to jump to
            return register[registerNumber];
        }
        else if (commandParts[0].startsWith("jp")) {
            //get register number from command part 2 "r__"
            int registerNumber = getJumpRegisterNumber(commandParts[1]);
            //check which condition needs to be checked and act accordingly
            switch (commandParts[0].substring(2)){
                //carry flag
                case "c":
                    if (FLAG_carry == (short) 1){
                        register[15] = register[registerNumber];
                    }
                    compiledBinaryCommands.add("111100000" + /*flag bits*/ "000" + NumberConverter.decimalToBinaryString(registerNumber, 4));
                    return register[registerNumber];
                case "nc":
                    if (FLAG_carry == (short) 0){
                        register[15] = register[registerNumber];
                    }
                    compiledBinaryCommands.add("111100000" + /*flag bits*/ "001" + NumberConverter.decimalToBinaryString(registerNumber, 4));
                    return register[registerNumber];
                //zero flag
                case "z":
                    if (FLAG_zero == (short) 1){
                        register[15] = register[registerNumber];
                    }
                    compiledBinaryCommands.add("111100000" + /*flag bits*/ "010" + NumberConverter.decimalToBinaryString(registerNumber, 4));
                    return register[registerNumber];
                case "nz":
                    if (FLAG_zero == (short) 0){
                        register[15] = register[registerNumber];
                    }
                    compiledBinaryCommands.add("111100000" + /*flag bits*/ "011" + NumberConverter.decimalToBinaryString(registerNumber, 4));
                    return register[registerNumber];
                //sign flag
                case "s":
                    if (FLAG_sign == (short) 1){
                        register[15] = register[registerNumber];
                    }
                    compiledBinaryCommands.add("111100000" + /*flag bits*/ "100" + NumberConverter.decimalToBinaryString(registerNumber, 4));
                    return register[registerNumber];
                case "ns":
                    if (FLAG_sign == (short) 0){
                        register[15] = register[registerNumber];
                    }
                    compiledBinaryCommands.add("111100000" + /*flag bits*/ "101" + NumberConverter.decimalToBinaryString(registerNumber, 4));
                    return register[registerNumber];
                //overflow flag
                case "o":
                    if (FLAG_overflow == (short) 1){
                        register[15] = register[registerNumber];
                    }
                    compiledBinaryCommands.add("111100000" + /*flag bits*/ "110" + NumberConverter.decimalToBinaryString(registerNumber, 4));
                    return register[registerNumber];
                case "no":
                    if (FLAG_overflow == (short) 0){
                        register[15] = register[registerNumber];
                    }
                    compiledBinaryCommands.add("111100000" + /*flag bits*/ "111" + NumberConverter.decimalToBinaryString(registerNumber, 4));
                    return register[registerNumber];
                //when jp + any other character than previous options
                default:
                    throw new RuntimeException("Invalid flag on conditional jump");
            }

        }

        else if (commandParts.length > 1 && commandParts[0].charAt(0) !=';' && !commandParts[0].startsWith("li")) {
            //SETUP FOR ALU OPERATIONS
            int firstRegisterNumber;
            int secondRegisterNumber;

            //check if register number is 1 or 2 digit (0-15)
            if (commandParts[1].length() == 4)
                firstRegisterNumber = Integer.parseInt(commandParts[1].substring(1, 3));
            else firstRegisterNumber = Integer.parseInt(commandParts[1].substring(1, 2));

            //check if register number is 1 or 2 digit (0-15)
            if (commandParts[2].length() == 3)
                secondRegisterNumber = Integer.parseInt(commandParts[2].substring(1, 3));
            else secondRegisterNumber = Integer.parseInt(commandParts[2].substring(1, 2));

            //ALU OPERATIONS
            if (commandParts[0].equalsIgnoreCase("nop")) {
                //stupid command that does nothing
                register[firstRegisterNumber] = register[secondRegisterNumber];
                //save ALU result
                ALU = 0;
                FLAG_carry = 0;
                FLAG_zero = 0;
                FLAG_sign = 0;
                FLAG_overflow = 0;

                //add the command to compiler
                compiledBinaryCommands.add("01000000" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            if (commandParts[0].equalsIgnoreCase("or")) {
                //or operation on every bit of the number inside the register
                String reg1 = NumberConverter.decimalToBinaryString(register[firstRegisterNumber], 16);
                String reg2 = NumberConverter.decimalToBinaryString(register[secondRegisterNumber], 16);
                StringBuilder ALUResult = new StringBuilder();

                //do the operation on each bit of the register
                for (int i = 0; i < 16; i++){
                    if (reg1.charAt(i) == '1' || reg2.charAt(i) == '1') ALUResult.append("1");
                    else ALUResult.append("0");
                }

                //save result of operation in the correct register
                register[firstRegisterNumber] = (short) NumberConverter.binaryStringToDecimal(ALUResult.toString());

                //save ALU result
                ALU = register[firstRegisterNumber];
                FLAG_carry = 0;
                FLAG_zero = 0;
                FLAG_sign = 0;
                FLAG_overflow = 0;

                //add the command to compiler
                compiledBinaryCommands.add("01000001" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            if (commandParts[0].equalsIgnoreCase("and")) {
                //and operation on every bit of the number inside the register
                String reg1 = NumberConverter.decimalToBinaryString(register[firstRegisterNumber], 16);
                String reg2 = NumberConverter.decimalToBinaryString(register[secondRegisterNumber], 16);
                StringBuilder ALUResult = new StringBuilder();

                //do the operation on each bit of the register
                for (int i = 0; i < 16; i++){
                    if (reg1.charAt(i) == '1' && reg2.charAt(i) == '1') ALUResult.append("1");
                    else ALUResult.append("0");
                }

                //save result of operation in the correct register
                register[firstRegisterNumber] = (short) NumberConverter.binaryStringToDecimal(ALUResult.toString());

                //save ALU result
                ALU = register[firstRegisterNumber];
                FLAG_carry = 0;
                FLAG_sign = 0;
                FLAG_overflow = 0;

                //add the command to compiler
                compiledBinaryCommands.add("010000010" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            if (commandParts[0].equalsIgnoreCase("xor")) {
                //xor operation on every bit of the number inside the register
                String reg1 = NumberConverter.decimalToBinaryString(register[firstRegisterNumber], 16);
                String reg2 = NumberConverter.decimalToBinaryString(register[secondRegisterNumber], 16);
                StringBuilder ALUResult = new StringBuilder();

                //do the operation on each bit of the register
                for (int i = 0; i < 16; i++){
                    if ((reg1.charAt(i) == '1' || reg2.charAt(i) == '1') && (reg1.charAt(i) != reg2.charAt(i))) ALUResult.append("1");
                    else ALUResult.append("0");
                }

                //save result of operation in the correct register
                register[firstRegisterNumber] = (short) NumberConverter.binaryStringToDecimal(ALUResult.toString());

                //save ALU result
                ALU = register[firstRegisterNumber];
                FLAG_carry = 0;
                FLAG_sign = 0;
                FLAG_overflow = 0;

                //add the command to compiler
                compiledBinaryCommands.add("01000011" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            if (commandParts[0].equalsIgnoreCase("add")) {
                //add the 2 register values and store them in the first register
                ALU = (short) (register[firstRegisterNumber] + register[secondRegisterNumber]);
                //set flags
                if ( (register[firstRegisterNumber] + register[secondRegisterNumber]) > Short.MAX_VALUE) { FLAG_overflow = 1; }
                else FLAG_overflow = 0;
                if (ALU == 0) FLAG_zero = 1;
                else FLAG_zero = 0;
                FLAG_carry = 0;
                FLAG_sign = 0;

                //save result of operation in the correct register
                register[firstRegisterNumber] = ALU;

                //add the command to compiler
                compiledBinaryCommands.add("01000100" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            if (commandParts[0].equalsIgnoreCase("sub")) {
                //subtract the 2 register values and store them in the first register
                ALU = (short) (register[firstRegisterNumber] - register[secondRegisterNumber]);

                //set flags
                if (ALU == 0 ) {
                    FLAG_zero = 1;
                }
                else {
                    if (ALU > 0) FLAG_sign = 0;
                    else FLAG_sign = 1;
                    FLAG_zero = 0;
                }
                FLAG_carry = 0;
                FLAG_overflow = 0;

                //save result of operation in the correct register
                register[firstRegisterNumber] = ALU;

                //add the command to compiler
                compiledBinaryCommands.add("01000101" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            //TODO check this weird for loop
            if (commandParts[0].equalsIgnoreCase("shl")) {
                //shift all bits left by a specified number of bits
                //the right-most (low-order) bit is turned into a 0
                //the left-most (high-order) bit is discarded
                for (int i = 0; i < register[secondRegisterNumber]; i++) {
                    String binary = NumberConverter.decimalToBinaryString(register[firstRegisterNumber], 16);
                    binary = binary.substring(1) + "0";
                    ALU = (short) NumberConverter.binaryStringToDecimal(binary);
                    register[firstRegisterNumber] = ALU;
                }

                //add the command to compiler
                compiledBinaryCommands.add("01000110" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            //TODO check this weird for loop
            if (commandParts[0].equalsIgnoreCase("shr")) {
                //shift all bits right by a specified number of bits
                //the right-most (low-order) bit is discarded
                //the left-most (high-order) bit is turned into a 0
                for (int i = 0; i < register[secondRegisterNumber]; i++) {
                    String binary = NumberConverter.decimalToBinaryString(register[firstRegisterNumber], 16);
                    binary = "0" + binary.substring(0, binary.length() - 1);
                    ALU = (short) NumberConverter.binaryStringToDecimal(binary);
                    register[firstRegisterNumber] = ALU;
                }

                //add the command to compiler
                compiledBinaryCommands.add("01000111" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            //TODO check this weird for loop
            if (commandParts[0].equalsIgnoreCase("ashr")) {
                //shift all bits right by a specified number of bits (and keep the sign bit)
                //the right-most (low-order) bit is discarded
                //the left-most (high-order) bit is copied
                for (int i = 0; i < register[secondRegisterNumber]; i++) {
                    String binary = NumberConverter.decimalToBinaryString(register[firstRegisterNumber], 16);
                    binary = binary.charAt(0) + binary.substring(0, binary.length() - 1);
                    ALU = register[firstRegisterNumber] = (short) NumberConverter.binaryStringToDecimal(binary);
                    register[firstRegisterNumber] = ALU;
                }

                //add the command to compiler
                compiledBinaryCommands.add("01001001" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            if (commandParts[0].equalsIgnoreCase("not")) {
                //1-complement
                String bitValue = NumberConverter.decimalToBinaryString(register[secondRegisterNumber], 16);
                String reverseBitValue = NumberConverter.invertBinary(bitValue);

                //save result of operation in the ALU
                ALU = (short) NumberConverter.binaryStringToDecimal(reverseBitValue);

                //save result of operation in the correct register
                register[firstRegisterNumber] = ALU;

                //add the command to compiler
                compiledBinaryCommands.add("01001001" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            if (commandParts[0].equalsIgnoreCase("neg")) {

                //2-complement
                String bitValue = NumberConverter.decimalToBinaryString(register[secondRegisterNumber], 16);
                String reverseBitValue = NumberConverter.invertBinary(bitValue);

                //save result of operation in the ALU
                ALU = (short) (NumberConverter.binaryStringToDecimal(reverseBitValue) + 1);

                //save result of operation in the correct register
                register[firstRegisterNumber] = ALU;

                //add the command to compiler
                compiledBinaryCommands.add("01001010" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
        }//ALU

        return 0;
    }//executeCommand

    /**
     * this method reads the register number from a jump instruction "jp/jpc r__"
     * @param commandPart part of the jump command containing the register "r__"
     * @return number of register stored in the instruction
     */
    public int getJumpRegisterNumber(String commandPart) {
        //check if register number is 1 or 2 digit (0-15)
        return Short.parseShort(commandPart.substring(1));
    }

    /**
     * this method reads the register number from an instruction "r__,"
     * the String need to contain 3-4 characters, start with an 'r' and end with a comma ','
     * @param commandPart part of the instruction containing the register "r__,"
     * @return number of register stored in the instruction
     */
    public int getMemoryRegister1(String commandPart) {
        //check if register number 1 is 1 or 2 digit (0-15)
        if (commandPart.length() == 4)
            return Integer.parseInt(commandPart.substring(1, 3));
        else return Integer.parseInt(commandPart.substring(1, 2));
    }

    /**
     * this method reads the register number from an instruction "(r__)"
     * the String need to contain 3-4 characters, start with "(r" and end with a closing parenthesis ")"
     * @param commandPart part of the instruction containing the register (r__,)
     * @return number of register stored in the instruction
     */
    public int getMemoryRegister2(String commandPart) {
        //check if register number 2 is 1 or 2 digit (0-15)
        if (commandPart.length() == 5)
            return Integer.parseInt(commandPart.substring(2, 4));//(r00)
        else return Integer.parseInt(commandPart.substring(2, 3));//(r0)
    }
}

