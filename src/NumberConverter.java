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

    public static String invertBinary(String binaryString) {
        String reverseBitValue = "";
        //reverse the bit value of bitValue
        for (int i = 0; i < binaryString.length(); i++) {
            if (binaryString.charAt(i) == '0') reverseBitValue = reverseBitValue + "1";
            else reverseBitValue = reverseBitValue + "0";
        }
        return reverseBitValue;
    }

    public static int binaryStringToDecimal(String binaryString) {
        return Integer.parseInt(binaryString, 2);
    }

    public static void main(String[] args) {
        System.out.println(decimalToBinaryString(16, 8));
        System.out.println(decimalToBinaryString(500, 8));
        System.out.println(decimalToBinaryString(2, 4));
    }
}
