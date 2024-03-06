import java.util.ArrayList;

public class Moncky2Interpreter {

    private short[] register = new short[16];
    private short[] memory = new short[65536];
    public ArrayList<String> compiledBinaryCommands = new ArrayList<String>();

    public Moncky2Interpreter(String moncky2Code) {
        String[] commands = moncky2Code.split("\n");

        int commandLine = 0;
        while (true) {
            int commandResult = executeCommand(commands[commandLine]);
            if (commandResult < 0) break;
            if (commandResult > 0) commandLine = commandResult;
            commandLine++;
        }
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

    public void printCompiledBinaryCommands() {
        for (String command : compiledBinaryCommands) {
            System.out.println(command);
        }
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
            immediateValue = Short.parseShort(commandParts[2]);
            register[registerNumber] = immediateValue;
            //add command to compiler
            compiledBinaryCommands.add("0001" + NumberConverter.decimalToBinaryString(immediateValue, 8) + NumberConverter.decimalToBinaryString(registerNumber, 4));
            return 0;
        }

        if (commandParts[0].equals("ld")) {
            int firstRegisterNumber;
            int secondRegisterNumber;

            //check if register number 1 is 1 or 2 digit (0-15)
            if (commandParts[1].length() == 4)
                firstRegisterNumber = Integer.parseInt(commandParts[1].substring(1, 3));
            else firstRegisterNumber = Integer.parseInt(commandParts[1].substring(1, 2));

            //check if register number 2 is 1 or 2 digit (0-15)
            if (commandParts[2].length() == 5)
                secondRegisterNumber = Integer.parseInt(commandParts[2].substring(2, 4));//(r00)
            else secondRegisterNumber = Integer.parseInt(commandParts[2].substring(2, 3));//(r0)
            //store value in RAM
            register[firstRegisterNumber] = memory[register[secondRegisterNumber]];

            //add the command to compiler
            compiledBinaryCommands.add("10000000" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

            return 0;
        }
        if (commandParts[0].equals("st")) {
            int firstRegisterNumber;
            int secondRegisterNumber;

            //check if register number 1 is 1 or 2 digit (0-15)
            if (commandParts[1].length() == 4)
                firstRegisterNumber = Integer.parseInt(commandParts[1].substring(1, 3));
            else firstRegisterNumber = Integer.parseInt(commandParts[1].substring(1, 2));

            //check if register number 2 is 1 or 2 digit (0-15)
            if (commandParts[2].length() == 5)
                secondRegisterNumber = Integer.parseInt(commandParts[2].substring(2, 4));//(r00)
            else secondRegisterNumber = Integer.parseInt(commandParts[2].substring(2, 3));//(r0)
            //store value in RAM
            memory[register[firstRegisterNumber]] = register[secondRegisterNumber];

            //add the command to compiler
            compiledBinaryCommands.add("10100000" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));


            return 0;
        }
        if (commandParts[0].equals("jp")) {
            int registerNumber;

            //check if register number is 1 or 2 digit (0-15)
            if (commandParts[1].length() == 4) registerNumber = Integer.parseInt(commandParts[1].substring(1, 3));
            else registerNumber = Integer.parseInt(commandParts[1].substring(1, 2));

            compiledBinaryCommands.add("110000000000" + NumberConverter.decimalToBinaryString(registerNumber, 4));
            return register[registerNumber];
        }

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

        if (commandParts[0].equalsIgnoreCase("nop")) {
            //stupid command that does nothing

            //add the command to compiler
            compiledBinaryCommands.add("01000000" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

            return 0;
        }
        if (commandParts[0].equalsIgnoreCase("or")) {
            //set reg1 to 1 if either register is true
            if (register[firstRegisterNumber] == 1 || register[secondRegisterNumber] == 1) register[firstRegisterNumber] = (short) 1;
            else register[firstRegisterNumber] = (short) 0;

            //add the command to compiler
            compiledBinaryCommands.add("01000001" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

            return 0;
        }
        if (commandParts[0].equalsIgnoreCase("and")) {
            //set reg1 to 1 if both registers are true
            if (register[firstRegisterNumber] == 1 && register[secondRegisterNumber] == 1) register[firstRegisterNumber] = (short) 1;
            else register[firstRegisterNumber] = (short) 0;

            //add the command to compiler
            compiledBinaryCommands.add("010000010" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

            return 0;
        }
        if (commandParts[0].equalsIgnoreCase("xor")) {
            //set reg1 to 1 if either register is true, but not both
            if ((register[firstRegisterNumber] == 1 || register[secondRegisterNumber] == 1) && register[firstRegisterNumber] != register[secondRegisterNumber]) register[firstRegisterNumber] = (short) 1;
            else register[firstRegisterNumber] = (short) 0;

            //add the command to compiler
            compiledBinaryCommands.add("01000011" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

            return 0;
        }
        if (commandParts[0].equalsIgnoreCase("add")) {
            //subtract the 2 register values and store them in the first register
            register[firstRegisterNumber] = (short) (register[firstRegisterNumber] + register[secondRegisterNumber]);

            //add the command to compiler
            compiledBinaryCommands.add("01000100" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

            return 0;
        }
        if (commandParts[0].equalsIgnoreCase("sub")) {
            //subtract the 2 register values and store them in the first register
            register[firstRegisterNumber] = (short) (register[firstRegisterNumber] - register[secondRegisterNumber]);

            //add the command to compiler
            compiledBinaryCommands.add("01000101" + NumberConverter.decimalToBinaryString(firstRegisterNumber, 4) + NumberConverter.decimalToBinaryString(secondRegisterNumber, 4));

            return 0;
        }
        if (commandParts[0].equalsIgnoreCase("shl")) {
            return 0;
        }
        if (commandParts[0].equalsIgnoreCase("shr")) {
            return 0;
        }
        if (commandParts[0].equalsIgnoreCase("ashr")) {
            return 0;
        }
        if (commandParts[0].equalsIgnoreCase("not")) {
            return 0;
        }
        if (commandParts[0].equalsIgnoreCase("neg")) {
            return 0;
        }

        if (commandParts[0].startsWith("jp")) {
            return 0;
        }
        return 0;
    }
}

