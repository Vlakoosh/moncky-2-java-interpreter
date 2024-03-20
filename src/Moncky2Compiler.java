import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Moncky2Compiler {


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
        Moncky2Compiler m2c = new Moncky2Compiler();
        m2c.compileCode(m2i, codeContent);
        m2i.printCompiledBinaryCommands();
    }

    public void compileCode(Moncky2Interpreter m2i, String moncky2Code) {
        String[] commands = moncky2Code.split("\n");

        int commandLine = 0;
        while (true) {
            int commandResult = m2i.executeCommand(commands[commandLine]);
            if (commandResult < 0) break;
            commandLine++;
        }
    }
}
