package com.controller;

import java.io.IOException;

import com.loggin.Loggin;
import com.security.BouncyCastleProvider_AES_CBC;
import com.session.Session;
import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadTransportException;
import com.transformer.Transformer;
import com.utils.ActionCode;
import com.utils.Rule;

public class ControllerTerminal implements Terminal {

	private byte[][] matrixRules;
	private byte[] key = "Smart!Cards!18VS".getBytes();
	BouncyCastleProvider_AES_CBC provide = new BouncyCastleProvider_AES_CBC();

	@Override
	public void processTransaction(int amount, ActionCode actionConde, String pin) throws Exception {

		Rule rule = retrieveSeficiRule(amount, matrixRules);
		switch (rule) {
		case Rule_1:
			completeTranzaction(amount, actionConde, rule);
			break;

		case Rule_2:
			verifySimplePin(Transformer.pinToDec(pin));
			completeTranzaction(amount, actionConde, rule);
			break;

		case Rule_3:
			verifyEncryptedPin(Transformer.pinToDec(pin));
			completeTranzaction(amount, actionConde, rule);
			break;

		default:
			System.out.println("This amount can't be associate with any rule");
			break;
		}
	}

	public Rule retrieveSeficiRule(int amount, byte[][] matrixRules) {

		for (int i = 0; i < matrixRules[0].length; i++) {
			if (amount >= matrixRules[i][0] && amount <= matrixRules[i][1]) {
				return Rule.valueof(matrixRules[i][2]);
			}
		}
		return Rule.Rule_Fail;
	}

	private void verifyEncryptedPin(byte[] pin) throws Exception {
		byte[] pin_encrypted = generateEncrytedPin(key, pin);

		Apdu apdu = new Apdu();
		apdu.command = assembleHeadOfApduCommand(ActionCode.VERIFY_ENCRYPTED_PIN);

		apdu.setDataIn(pin_encrypted);
		apdu.setLe(pin.length);
		Session.processApdu(apdu);
		Loggin.showResponse(ActionCode.VERIFY_ENCRYPTED_PIN, "", apdu);
	}

	public void verifySimplePin(byte[] pin) throws IOException, CadTransportException {

		Apdu apdu = new Apdu();
		apdu.command = assembleHeadOfApduCommand(ActionCode.VERIFY_SIMPLE_PIN);

		apdu.setDataIn(pin);
		Session.processApdu(apdu);
		Loggin.showResponse(ActionCode.VERIFY_ENCRYPTED_PIN, "", apdu);
	}

	private byte[] generateEncrytedPin(byte[] key, byte[] pin) throws Exception {

		return provide.CBCEncrypt(key, pin);

	}

	private void completeTranzaction(int amount, ActionCode actionConde, Rule rule)
			throws IOException, CadTransportException {

		Apdu apdu = new Apdu();
		apdu.command = assembleHeadOfApduCommand(actionConde);
		apdu.setDataIn(assembleBodyOfApduCommand(amount, rule));

		Session.processApdu(apdu);
		Loggin.showResponse(actionConde, "amount " + amount, apdu);
	}

	private byte[] assembleBodyOfApduCommand(int amount, Rule rule) {
		byte[] body = new byte[2];
		body[0] = (byte) amount;
		body[1] = rule.getCode();
		return body;
	}

	private byte[] assembleHeadOfApduCommand(ActionCode actionConde) {
		byte[] header = new byte[4];
		header[0] = ActionCode.Wallet_CLA.getCode();
		header[1] = actionConde.getCode();
		return header;
	}

	@Override
	public void processTransaction(int amount, ActionCode actionConde) throws IOException, CadTransportException {
		completeTranzaction(amount, actionConde, Rule.Rule_1);
	}

	@Override
	public void processAction(ActionCode actionConde) throws IOException, CadTransportException {
		completeAction(actionConde);
	}

	private void completeAction(ActionCode actionConde) throws IOException, CadTransportException {
		Apdu apdu = new Apdu();
		apdu.command = assembleHeadOfApduCommand(actionConde);
		apdu.Le = actionConde.getResponseLength();
		Session.processApdu(apdu);
		if (ActionCode.GET_RULES.equals(actionConde)) {
			this.matrixRules = Transformer.toMatrix(apdu.getDataOut());
		}
		if (!ActionCode.GET_BALANCE.equals(actionConde)) {
			Loggin.showResponse(actionConde, "", apdu);
		} else {
			Loggin.showResponse(actionConde, "Balance: " + apdu.getDataOut()[1], apdu);
		}
	}
	
	public byte[][] getMatrixRules() {
		return matrixRules;
	}

	public void setMatrixRules(byte[][] matrixRules) {
		this.matrixRules = matrixRules;
	}
}
