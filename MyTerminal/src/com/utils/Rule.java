package com.utils;

public enum Rule {
	Rule_1("Rule_1", (byte) 1), Rule_2("Rule_2", (byte) 2), Rule_3("Rule_3", (byte) 3), Rule_Fail("Rule_Fail",
			(byte) 4);

	private byte code;
	private String message;

	private Rule(String message, byte code) {
		this.code = code;
		this.message = message;
	}

	public static Rule valueof(byte i) {

		if (i == (byte) 1)
			return Rule_1;
		if (i == (byte) 2)
			return Rule_2;
		if (i == (byte) 3)
			return Rule_3;

		return Rule_Fail;
	}

	public byte getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

}
