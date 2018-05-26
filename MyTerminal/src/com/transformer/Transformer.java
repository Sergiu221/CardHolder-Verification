package com.transformer;

public class Transformer {

	public static String atrToHex(byte atCode) {
		char hex[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		String str2 = "";
		int num = atCode & 0xff;
		int rem;
		while (num > 0) {
			rem = num % 16;
			str2 = hex[rem] + str2;
			num = num / 16;
		}
		if (str2 != "") {
			return str2;
		} else {
			return "0";
		}

	}

	public static byte[] pinToDec(String code) {
		byte[] result = new byte[code.length()];
		for (int i = 0; i < code.length(); i++) {
			int number = Character.getNumericValue(code.charAt(i));
			result[i] = (byte) (number);
		}
		return result;
	}

	public static byte[][] toMatrix(byte[] myVector) {
		int size = (int) Math.sqrt(myVector.length);
		byte myMatrix[][] = new byte[size][size];
		System.out.println("Response rules from Applet..");
		System.out.println("Min  " + "Max" + "Code CVM Rule");
		int k = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				myMatrix[i][j] = myVector[k];
				System.out.print(myVector[k] + "   ");
				k++;
			}
			System.out.println();
		}
		return myMatrix;
	}

}
