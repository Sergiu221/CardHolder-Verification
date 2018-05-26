package com.controller;

import java.io.IOException;

import com.sun.javacard.apduio.CadTransportException;
import com.utils.ActionCode;

public interface Terminal {

	void processAction(ActionCode actionConde) throws IOException, CadTransportException;

	void processTransaction(int amount, ActionCode actionConde, String pin) throws Exception;

	void processTransaction(int amount, ActionCode actionConde) throws IOException, CadTransportException;

}
