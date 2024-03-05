import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

        Moncky2Interpreter m2i = new Moncky2Interpreter(codeContent);
        m2i.printCompiledBinaryCommands();
    }
}
