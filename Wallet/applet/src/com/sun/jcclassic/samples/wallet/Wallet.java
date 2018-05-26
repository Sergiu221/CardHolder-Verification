/** 
 * Copyright (c) 1998, 2017, Oracle and/or its affiliates. All rights reserved.
 * 
 */

/*
 * @(#)Wallet.java	1.11 06/01/03
 */

package com.sun.jcclassic.samples.wallet;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;
import javacard.security.AESKey;
import javacard.security.Key;
import javacard.security.KeyBuilder;
import javacardx.crypto.Cipher;

public class Wallet extends Applet {

	/* constants declaration */

	// code of CLA byte in the command APDU header
	final static byte Wallet_CLA = (byte) 0x80;

	// codes of INS byte in the command APDU header
	final static byte VERIFY_PLAIN = (byte) 0x20;
	final static byte VERIFY_ENCRIPTED = (byte) 0x21;
	final static byte CREDIT = (byte) 0x30;
	final static byte DEBIT = (byte) 0x40;
	final static byte GET_BALANCE = (byte) 0x50;
	final static byte GET_RULES = (byte) 0x51;

	// CardHolder Verification codes
	final static private byte RULE_1 = (byte) 0x01; // <$50 no PIN
	final static private byte RULE_2 = (byte) 0x02; // $50<>$100, then plaintext PIN  c://Lucru
	final static private byte RULE_3 = (byte) 0x03; // $100< enciphered PIN

	final static byte AMOUNT_LOW = (byte) 50;
	final static byte AMOUNT_HIGH = (byte) 100;

	// maximum balance
	final static short MAX_BALANCE = 0x7FFF;
	// maximum transaction amount
	final static byte MAX_TRANSACTION_AMOUNT = 127;

	// maximum number of incorrect tries before the
	// PIN is blocked
	final static byte PIN_TRY_LIMIT = (byte) 0x03;
	// maximum size PIN
	final static byte MAX_PIN_SIZE = (byte) 0x08;
	// size of current PIN
	final private byte PIN_LENGHT = 5;

	// signal that the PIN verification failed
	final static short SW_VERIFICATION_FAILED = 0x6300;
	// signal the the PIN validation is required
	// for a credit or a debit transaction
	final static short SW_PIN_VERIFICATION_REQUIRED = 0x6301;
	final static short SW_PIN_ENCRIPTED_VERIFICATION_REQUIRED = 0x6302;
	final static short SW_RULE__VERIFICATION_FAIL = 0x6303;
	// signal invalid transaction amount
	// amount > MAX_TRANSACTION_AMOUNT or amount < 0
	final static short SW_INVALID_TRANSACTION_AMOUNT = 0x6A83;

	// signal that the balance exceed the maximum
	final static short SW_EXCEED_MAXIMUM_BALANCE = 0x6A84;
	// signal the the balance becomes negative
	final static short SW_NEGATIVE_BALANCE = 0x6A85;


	/* instance variables declaration */
	OwnerPIN pin;
	short balance;
	boolean pinEncripted = false;
	/* This is my key for decryption */
	byte[] key = new byte[] { 83, 109, 97, 114, 116, 33, 67, 97, 114, 100, 115, 33, 49, 56, 86, 83 };
	byte[] aesICV = new byte[16];

	private Wallet(byte[] bArray, short bOffset, byte bLength) {

		// It is good programming practice to allocate
		// all the memory that an applet needs during
		// its lifetime inside the constructor
		pin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);

		byte iLen = bArray[bOffset]; // aid length
		bOffset = (short) (bOffset + iLen + 1);
		byte cLen = bArray[bOffset]; // info length
		bOffset = (short) (bOffset + cLen + 1);
		byte aLen = bArray[bOffset]; // applet data length

		// The installation parameters contain the PIN
		// initialization value
		pin.update(bArray, (short) (bOffset + 1), aLen);
		register();

	} // end of the constructor

	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// create a Wallet applet instance
		new Wallet(bArray, bOffset, bLength);
	} // end of install method

	@Override
	public boolean select() {

		// The applet declines to be selected
		// if the pin is blocked.
		if (pin.getTriesRemaining() == 0) {
			return false;
		}

		return true;

	}// end of select method

	@Override
	public void deselect() {

		// reset the pin value
		pin.reset();

	}

	@Override
	public void process(APDU apdu) {

		// APDU object carries a byte array (buffer) to
		// transfer incoming and outgoing APDU header
		// and data bytes between card and CAD

		// At this point, only the first header bytes
		// [CLA, INS, P1, P2, P3] are available in
		// the APDU buffer.
		// The interface javacard.framework.ISO7816
		// declares constants to denote the offset of
		// these bytes in the APDU buffer

		byte[] buffer = apdu.getBuffer();
		// check SELECT APDU command

		if (apdu.isISOInterindustryCLA()) {
			if (buffer[ISO7816.OFFSET_INS] == (byte) (0xA4)) {
				return;
			}
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}

		// verify the reset of commands have the
		// correct CLA byte, which specifies the
		// command structure
		if (buffer[ISO7816.OFFSET_CLA] != Wallet_CLA) {
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}

		switch (buffer[ISO7816.OFFSET_INS]) {
		case GET_BALANCE:
			getBalance(apdu);
			return;
		case DEBIT:
			debit(apdu);
			return;
		case CREDIT:
			credit(apdu);
			return;
		case VERIFY_PLAIN:
			verify_plain(apdu);
			return;
		case VERIFY_ENCRIPTED:
			verify_encrypted(apdu);
			return;
		case GET_RULES:
			getCardHolderMethod(apdu);
			return;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}

	} // end of process method

	private void getCardHolderMethod(APDU apdu) {

		byte[] buffer = apdu.getBuffer();

		short le = apdu.setOutgoing();

		if ((le != 10)) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}

		apdu.setOutgoingLength((byte) 9);

		// First rule: low+high+code;
		buffer[0] = 0;
		buffer[1] = AMOUNT_LOW;
		buffer[2] = RULE_1;
		// Second rule: low+high+code;
		buffer[3] = (short) AMOUNT_LOW + 1;
		buffer[4] = AMOUNT_HIGH;
		buffer[5] = RULE_2;

		// Second rule: low+high+code;
		buffer[6] = (short) AMOUNT_HIGH + 1;
		buffer[7] = MAX_TRANSACTION_AMOUNT;
		buffer[8] = RULE_3;
		apdu.sendBytes((short) 0, (byte) 9);

	}

	private void credit(APDU apdu) {

		byte[] buffer = apdu.getBuffer();

		// Lc byte denotes the number of bytes in the
		// data field of the command APDU
		byte numBytes = buffer[ISO7816.OFFSET_LC];

		// indicate that this APDU has incoming data
		// and receive data starting from the offset
		// ISO7816.OFFSET_CDATA following the 5 header
		// bytes.
		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		// it is an error if the number of data bytes
		// read does not match the number in Lc byte
		if ((numBytes != 2) || (byteRead != 2)) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}

		// get the credit amount
		byte creditAmount = buffer[ISO7816.OFFSET_CDATA];

		// check the credit amount
		if ((creditAmount > MAX_TRANSACTION_AMOUNT) || (creditAmount < 0)) {
			ISOException.throwIt(SW_INVALID_TRANSACTION_AMOUNT);
		}

		// check the new balance
		if ((short) (balance + creditAmount) > MAX_BALANCE) {
			ISOException.throwIt(SW_EXCEED_MAXIMUM_BALANCE);
		}

		// credit the amount
		balance = (short) (balance + creditAmount);

		pinEncripted = false;
		pin.reset();

	} // end of deposit method

	private void debit(APDU apdu) {

		byte[] buffer = apdu.getBuffer();

		byte numBytes = (buffer[ISO7816.OFFSET_LC]);

		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		if ((numBytes != 2) || (byteRead != 2)) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}

		// get debit amount
		byte debitAmount = buffer[ISO7816.OFFSET_CDATA];
		// get rule transaction
		byte rule = buffer[ISO7816.OFFSET_CDATA+1];

		checkRule(rule);

		// check debit amount
		if ((debitAmount > MAX_TRANSACTION_AMOUNT) || (debitAmount < 0)) {
			ISOException.throwIt(SW_INVALID_TRANSACTION_AMOUNT);
		}

		// check the new balance
		if ((short) (balance - debitAmount) < (short) 0) {
			ISOException.throwIt(SW_NEGATIVE_BALANCE);
		}

		balance = (short) (balance - debitAmount);

		pinEncripted = false;
		pin.reset();
	} // end of debit method

	private void getBalance(APDU apdu) {

		byte[] buffer = apdu.getBuffer();

		// inform system that the applet has finished
		// processing the command and the system should
		// now prepare to construct a response APDU
		// which contains data field
		short le = apdu.setOutgoing();

		if (le < 2) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}

		// informs the CAD the actual number of bytes
		// returned
		apdu.setOutgoingLength((byte) 2);

		// move the balance data into the APDU buffer
		// starting at the offset 0
		buffer[0] = (byte) (balance >> 8);
		buffer[1] = (byte) (balance & 0xFF);

		// send the 2-byte balance at the offset
		// 0 in the apdu buffer
		apdu.sendBytes((short) 0, (short) 2);

	} // end of getBalance method

	private void verify_plain(APDU apdu) {

		byte[] buffer = apdu.getBuffer();
		// retrieve the PIN data for validation.
		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		// check pin
		// the PIN data is read into the APDU buffer
		// at the offset ISO7816.OFFSET_CDATA
		// the PIN data length = byteRead
		if (pin.check(buffer, ISO7816.OFFSET_CDATA, byteRead) == false) {
			ISOException.throwIt(SW_VERIFICATION_FAILED);
		}

	} // end of validate method

	private void verify_encrypted(APDU apdu) {

		byte[] buffer = apdu.getBuffer();

		byte[] pin_T = new byte[16];
		// 1.Get the key
		Key key = getKey();
		// 2.Get Cipher
		Cipher cipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);

		cipher.init(key, Cipher.MODE_DECRYPT, pin_T, (short) 0, (short) 16);

		cipher.doFinal(buffer, ISO7816.OFFSET_CDATA, (short) 16, buffer, (short) 0);

		apdu.setOutgoingAndSend((short) 0, (short) PIN_LENGHT);

		// the PIN data length = byteRead
		if (pin.check(buffer, (short) 0, PIN_LENGHT) == false) {
			ISOException.throwIt(SW_VERIFICATION_FAILED);
		}

		pinEncripted = true;
	}

	private Key getKey() {

		Key builder = KeyBuilder.buildKey(KeyBuilder.TYPE_AES_TRANSIENT_DESELECT, KeyBuilder.LENGTH_AES_128, false);

		((AESKey) builder).setKey(this.key, (short) 0);

		return builder;
	}

	public void checkRule(byte rule) {

		switch (rule) {

		case RULE_1:

			break;

		case RULE_2:
			if (!pin.isValidated()) {
				ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
			}
			break;

		case RULE_3:
			if (!pinEncripted) {
				ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
			}

			if (!pin.isValidated()) {
				ISOException.throwIt(SW_PIN_ENCRIPTED_VERIFICATION_REQUIRED);
			}
			break;

		default:
			ISOException.throwIt(SW_RULE__VERIFICATION_FAIL);
			break;
		}
	}
} // end of class Wallet
