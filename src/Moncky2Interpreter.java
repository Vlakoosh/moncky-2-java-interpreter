import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Moncky2Interpreter {

    private final short[] register = new short[16];
    private final short[] memory = new short[65536];
    private short ALU = 0;
    private short FLAG_carry = 0;
    private short FLAG_zero = 0;
    private short FLAG_sign = 0;
    private short FLAG_overflow = 0;

    private String[] commands;
    private String[] commandsWithLabels;
    public ArrayList<String> compiledBinaryCommands = new ArrayList<>();


    public Moncky2Interpreter(String code){
        commands = code.split("\n");
    }

    public Moncky2Interpreter(){}

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

    public void interpretCode(String moncky2Code) {
        commandsWithLabels = moncky2Code.split("\n");
        commandsWithLabels = stripEmptyCommands(commandsWithLabels);
        commands = removeLabelsAndComments(commandsWithLabels);
        ALU = 0;
        register[15] = 0;
        while (true) {
            int commandResult = executeCommand(commands[register[15]]);
            if (commandResult < 0) break;
            if (commandResult > 0) register[15] = (short) (commandResult);
            register[15]++;
        }
    }

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

    public ArrayList<String> getCompiledBinaryCommands() {
        return compiledBinaryCommands;
    }

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

    public String[] getCommands() {
        return commands;
    }


    public int executeCommand(String command) {
        String[] commandParts = CommandReader.getCommandParts(command);

        //halt command. Stops execution (halt)
        if (commandParts[0].equalsIgnoreCase("halt")) {
            compiledBinaryCommands.add("0000000000000000");
            return -1;
        }
        //load immediate command (li r, i)
        if (commandParts[0].equals("li")) {
            int registerNumber;
            short immediateValue;

            //check if register number is 1 or 2 digit (0-15)
            if (commandParts[1].length() == 4) registerNumber = Integer.parseInt(commandParts[1].substring(1, 3));
            else registerNumber = Integer.parseInt(commandParts[1].substring(1, 2));

            //save the immediate value from command
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
                    throw new RuntimeException("label not found in code");
                }
            } else if (commandParts[2].startsWith("0x")) {
                immediateValue = (short) NumberConverter.hexStringToDecimal(commandParts[2].substring(2));
            } else {
                immediateValue = Short.parseShort(commandParts[2]);
                if (immediateValue > 255){
                    throw new RuntimeException("number loaded into register r" + registerNumber + " is too great. Consider using a bit shift instead");
                }
            }

            register[registerNumber] = immediateValue;
            //add command to compiler
            compiledBinaryCommands.add("0001" + NumberConverter.decimalToBinaryString(immediateValue, 8) + NumberConverter.decimalToBinaryString(registerNumber, 4));
            return 0;
        }
        if (commandParts[0].equals("ld")) {
            int firstRegisterNumber = getMemoryRegister1(commandParts[1]);
            int secondRegisterNumber = getMemoryRegister2(commandParts[2]);

            //store value in RAM
            register[firstRegisterNumber] = memory[register[secondRegisterNumber]];

            //add the command to compiler
            compiledBinaryCommands.add("10000000" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

            return 0;
        }
        if (commandParts[0].equals("st")) {
            int firstRegisterNumber = getMemoryRegister1(commandParts[1]);
            int secondRegisterNumber = getMemoryRegister2(commandParts[2]);

            //store value in RAM
            memory[register[secondRegisterNumber]] = register[firstRegisterNumber];

            //add the command to compiler
            compiledBinaryCommands.add("10100000" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

            return 0;
        }
        if (commandParts[0].equals("jp")) {
            int registerNumber = getJumpRegisterNumber(commandParts[1]);
            register[15] = register[registerNumber];

            compiledBinaryCommands.add("110000000000" + NumberConverter.decimalToBinaryString(registerNumber, 4));
            return register[registerNumber];
        }
        else if (commandParts[0].startsWith("jp")) {
            //setup for conditional jump
            int registerNumber = getJumpRegisterNumber(commandParts[1]);
            switch (commandParts[0].substring(2)){
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

                for (int i = 0; i < 16; i++){
                    if (reg1.charAt(i) == '1' || reg2.charAt(i) == '1') ALUResult.append("1");
                    else ALUResult.append("0");
                }

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

                for (int i = 0; i < 16; i++){
                    if (reg1.charAt(i) == '1' && reg2.charAt(i) == '1') ALUResult.append("1");
                    else ALUResult.append("0");
                }

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

                for (int i = 0; i < 16; i++){
                    if ((reg1.charAt(i) == '1' || reg2.charAt(i) == '1') && (reg1.charAt(i) != reg2.charAt(i))) ALUResult.append("1");
                    else ALUResult.append("0");
                }

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

                if (ALU == 0){
                    FLAG_zero = 1;
                } else FLAG_zero = 0;
                FLAG_carry = 0;
                FLAG_sign = 0;

                register[firstRegisterNumber] = ALU;

                //add the command to compiler
                compiledBinaryCommands.add("01000100" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            if (commandParts[0].equalsIgnoreCase("sub")) {
                //subtract the 2 register values and store them in the first register
                ALU = (short) (register[firstRegisterNumber] - register[secondRegisterNumber]);
                if (ALU == 0 ) {
                    FLAG_zero = 1;
                }
                else {
                    if (ALU > 0){
                        FLAG_sign = 0;
                    }
                    else {
                        FLAG_sign = 1;
                    }
                    FLAG_zero = 0;
                }
                FLAG_carry = 0;
                FLAG_overflow = 0;

                register[firstRegisterNumber] = ALU;

                //add the command to compiler
                compiledBinaryCommands.add("01000101" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
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

                ALU = (short) NumberConverter.binaryStringToDecimal(reverseBitValue);

                register[firstRegisterNumber] = ALU;

                //add the command to compiler
                compiledBinaryCommands.add("01001001" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
            if (commandParts[0].equalsIgnoreCase("neg")) {

                //2-complement
                String bitValue = NumberConverter.decimalToBinaryString(register[secondRegisterNumber], 16);
                String reverseBitValue = NumberConverter.invertBinary(bitValue);
                ALU = (short) (NumberConverter.binaryStringToDecimal(reverseBitValue) + 1);
                register[firstRegisterNumber] = ALU;

                //add the command to compiler
                compiledBinaryCommands.add("01001010" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

                return 0;
            }
        }//ALU

        return 0;
    }//executeCommand

    public int getJumpRegisterNumber(String commandPart) {
        //check if register number is 1 or 2 digit (0-15)
        return Short.parseShort(commandPart.substring(1));
    }

    public int getMemoryRegister1(String commandPart) {
        //check if register number 1 is 1 or 2 digit (0-15)
        if (commandPart.length() == 4)
            return Integer.parseInt(commandPart.substring(1, 3));
        else return Integer.parseInt(commandPart.substring(1, 2));
    }

    public int getMemoryRegister2(String commandPart) {
        //check if register number 2 is 1 or 2 digit (0-15)
        if (commandPart.length() == 5)
            return Integer.parseInt(commandPart.substring(2, 4));//(r00)
        else return Integer.parseInt(commandPart.substring(2, 3));//(r0)
    }
}

