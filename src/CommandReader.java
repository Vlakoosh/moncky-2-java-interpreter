public class CommandReader {
    public static String[] getCommandParts(String command){
        command = command.trim();
        return command.split(" ");

    }
}
