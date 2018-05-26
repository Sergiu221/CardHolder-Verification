package com.loggin;

import com.sun.javacard.apduio.Apdu;
import com.transformer.Transformer;
import com.utils.ActionCode;

public class Loggin {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_BLUE = "\u001B[34m";

	public static final String ANSI_BOLD = "\u001B[1m";

	private static StringBuilder getAction(ActionCode actionCode) {
		return new StringBuilder("ACTION: " + ANSI_BLUE + ANSI_BOLD + actionCode.getMessage() + ANSI_RESET);
	}

	private static StringBuilder getDetails(String details) {
		return new StringBuilder(" DETAILS: " + ANSI_BLUE + ANSI_BOLD + details + ANSI_RESET);
	}

	private static StringBuilder getSW1SW2(Apdu apdu) {
		byte[] statByte = apdu.getSw1Sw2();
		StringBuilder result = new StringBuilder();
		String message = Transformer.atrToHex(statByte[0]).trim() + Transformer.atrToHex(statByte[1]).trim();

		String decodedMessage = decodeValueFromSW1SW2(message);
		if (!message.equals("900")) {
			result.append((ANSI_BOLD + " SW1:" + ANSI_RED + Transformer.atrToHex(statByte[0]) + " SW2:"
					+ Transformer.atrToHex(statByte[1]))+decodedMessage + ANSI_RESET);
		} else {
			result.append((ANSI_BOLD + " SW1:" + ANSI_BLUE + Transformer.atrToHex(statByte[0]) + " SW2:"
					+ Transformer.atrToHex(statByte[1]) + ANSI_RESET)+decodedMessage);

		}
		return result;
	}

	private static String decodeValueFromSW1SW2(String message) {
		switch (message) {
		case "900":
			return "(Sucessful!)";

		case "630":
			return "(Pine verification fail!)";

		case "631":
			return "(Is necesary verification pin!)";

		case "6312":
			return "(Is necesary verification encrypted pin!)";

		case "6A83":
			return "(Invalid transaction amount!)";

		case "6A84":
			return "(Exceed maximum balance)";

		case "6A85":
			return "(Negative_balance!)";
		default:
			return "(Bad Apdu!)";

		}
	}

	public static void showResponse(ActionCode actionCode, String details, Apdu apdu) {
		StringBuilder result = new StringBuilder();
		result.append(getAction(actionCode));
		result.append(getDetails(details));
		result.append(getSW1SW2(apdu));
		System.out.println(result.toString());

	}

	public static void showMessage(String string) {
		System.out.println(ANSI_GREEN + string + ANSI_RESET);
	}
}
