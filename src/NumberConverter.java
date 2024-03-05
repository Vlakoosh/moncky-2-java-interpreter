public class NumberConverter {
    public static String decimalToBinaryString(int decimalNumber, int len) {
        String binaryString = Integer.toBinaryString(decimalNumber);

        //if binary number is too short, add leading zeros
        if (binaryString.length() < len) {
            int lenDif = len - binaryString.length();
            for (int i = 0; i < lenDif; i++) {
                binaryString = "0" + binaryString;
            }
        }
        //return only the needed amount of bits. Cut off the rest
        return binaryString.substring(binaryString.length() - (len));
    }

    public static void main(String[] args) {
        System.out.println(decimalToBinaryString(16, 8));
        System.out.println(decimalToBinaryString(500, 8));
        System.out.println(decimalToBinaryString(2, 4));
    }
}
