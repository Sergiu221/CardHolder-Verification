package com.utils;

public enum ActionCode {
	Wallet_CLA("Wallet_CLA", (byte) 0x80), 
	VERIFY_SIMPLE_PIN("VERIFY_SIMPLE_PIN", (byte) 0x20), 
	VERIFY_ENCRYPTED_PIN("VERIFY_ENCRYPTED_PIN",(byte) 0x21), 
	CREDIT("CREDIT", (byte) 0x30),
	DEBIT("DEBIT", (byte) 0x40), 
	GET_BALANCE("GET_BALANCE",(byte) 0x50, (byte) 2), 
	GET_RULES("GET_RULES", (byte) 0x51, (byte) 10);

	private String message;
	private byte code;
	private byte responseLength;

	private ActionCode(String message, byte code) {
		this.message = message;
		this.code = code;
		this.responseLength = 0;
	}

	private ActionCode(String message, byte code, byte responseLength) {
		this.message = message;
		this.code = code;
		this.responseLength = responseLength;
	}

	public String getMessage() {
		return message;
	}

	public byte getCode() {
		return code;
	}

	public byte getResponseLength() {
		return responseLength;
	}

}
