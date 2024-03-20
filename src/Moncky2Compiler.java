import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Moncky2Compiler {

    private ArrayList<String> compiledBinaryCommands = new ArrayList<String>();
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

        Moncky2Interpreter m2i = new Moncky2Interpreter(codeContent);
        Moncky2Compiler m2c = new Moncky2Compiler();
        m2c.compileCode(m2i, codeContent);
        m2c.setCompiledBinaryCommands(m2i.getCompiledBinaryCommands());
        m2c.parseHexCommands();
    }

    public void setCompiledBinaryCommands(ArrayList<String> compiledBinaryCommands) {
        this.compiledBinaryCommands = compiledBinaryCommands;
    }

    public void parseHexCommands() {

        File myObj = new File("moncky2out/compiledCode.hex");
        if (myObj.delete()) {
            System.out.println("Deleting file: " + myObj.getName() + "...");
        } else {
            System.out.println("No existing file found...");
        }

        try {
            if (myObj.createNewFile()) {
                System.out.println("Generating new file...");
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            FileWriter myWriter = new FileWriter("moncky2out/compiledCode.hex");
            int hexCounter = 0;
            myWriter.write("v2.0 raw\n");
            for (String compiledCommand : compiledBinaryCommands) {
                hexCounter++;
                int commandDecimal = Integer.parseInt(compiledCommand, 2);
                String hexCommand = Integer.toString(commandDecimal, 16);
                myWriter.write(("0000" + hexCommand).substring(hexCommand.length()) + " ");
                if(hexCounter == 8) {
                    myWriter.write("\n");
                    hexCounter = 0;
                }
            }
            myWriter.close();
            System.out.println("Successfully wrote binary commands");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void compileCode(Moncky2Interpreter m2i, String moncky2Code) {
        String[] commands = m2i.getCommands();

        int commandLine = 0;
        while (true) {
            int commandResult = m2i.executeCommand(commands[commandLine]);
            if (commandResult < 0) break;
            commandLine++;
        }
    }
}
