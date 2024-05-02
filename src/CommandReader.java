import java.util.ArrayList;

public class CommandReader {
    public static String[] getCommandParts(String command){
        command = command.trim();

        //split each instruction into words/parts
        String[] codeLineParts = command.split(" ");
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

        return codeLineParts;

    }
}
