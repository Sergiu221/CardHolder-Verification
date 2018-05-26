package com.session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.loggin.Loggin;
import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadClientInterface;
import com.sun.javacard.apduio.CadDevice;
import com.sun.javacard.apduio.CadTransportException;

public class Session {

	private static Socket sock;
	private static OutputStream os;
	private static InputStream is;
	private static CadClientInterface cad;

	public static void processApdu(Apdu apdu) throws IOException, CadTransportException {
		cad.exchangeApdu(apdu);
	}

	public static void establishConnection() {

		try {
			sock = new Socket("localhost", 9025);
			os = sock.getOutputStream();
			is = sock.getInputStream();
			// Initialize the instance card acceptance device
			cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T1, is, os);

		} catch (IOException e) {
			System.err.println("Conection Fail!");
		}
	}

	public static void powerUp() throws IOException, CadTransportException {

		cad.powerUp();
		Loggin.showMessage("Card is on!");
	}

	public static void powerDown() throws IOException, CadTransportException {

		cad.powerDown();
		Loggin.showMessage("Card is stop!");
	}

	public static String loadPinFromFileHexa(String file) throws IOException {
		BufferedReader br;
		String everything = "";

		br = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();

		while (line != null) {
			sb.append(line);
			sb.append(System.lineSeparator());
			line = br.readLine();
		}
		everything = sb.toString();
		br.close();
		return everything.trim();
	}

	public static void processCapFile(String file) throws IOException, CadTransportException {
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line = br.readLine();

		while (line != null) {
			if (line.equals("powerup;") || line.isEmpty()) {
				line = br.readLine();
				continue;
			}

			if (line.charAt(0) == '/') {
				line = br.readLine();
				continue;
			}

			String[] splits = line.split(" ");
			byte[] header = new byte[4];

			// 1.HEADER
			for (int i = 0; i < 4; i++) {
				byte b = 0;
				b += Integer.parseInt(String.valueOf(splits[i].charAt(2)), 16) * 16;
				b += Integer.parseInt(String.valueOf(splits[i].charAt(3)), 16);

				header[i] = (byte) b;
			}
			// 2.BODY
			int bodySize = splits.length - 6;
			byte[] body = new byte[bodySize];

			int j = 0;
			for (int i = 5; i < splits.length - 1; i++) {

				byte b = 0;
				b += Integer.parseInt(String.valueOf(splits[i].charAt(2)), 16) * 16;
				b += Integer.parseInt(String.valueOf(splits[i].charAt(3)), 16);

				body[j] = (Byte) b;
				j++;
			}

			Apdu apdu = new Apdu();
			apdu.command = header;
			apdu.setDataIn(body);
			cad.exchangeApdu(apdu);
			line = br.readLine();
		}
		br.close();
	}

	public static void setupEnvironment() throws IOException, CadTransportException {
		install();
		create();
		select();
		Loggin.showMessage("Setup is succesful finish!");
	}

	private static void select() throws IOException, CadTransportException {
		Apdu apdu = new Apdu();
		apdu.command[0] = (byte) 0x00;
		apdu.command[1] = (byte) 0xA4;
		apdu.command[2] = (byte) 0x04;
		apdu.command[3] = (byte) 0x00;
		apdu.setDataIn(new byte[] { (byte) 0xa0, 0x0, 0x0, 0x0, 0x62, 0x3, 0x1, 0xc, 0x6, 0x1 });

		Session.processApdu(apdu);
	}

	private static void create() throws IOException, CadTransportException {
		Apdu apdu = new Apdu();
		apdu.command[0] = (byte) 0x80;
		apdu.command[1] = (byte) 0xB8;
		apdu.command[2] = (byte) 0x00;
		apdu.command[3] = (byte) 0x00;
		apdu.Lc = (byte) 0x14;
		byte[] data = new byte[] { (byte) 0x0a, (byte) 0xa0, 0x0, 0x0, 0x0, 0x62, 0x3, 0x1, 0xc, 0x6, 0x1, 0x08, 0x0,
				0x0, 0x05, 0x01, 0x02, 0x03, 0x04, 0x05 };

		apdu.dataIn = data;
		apdu.setDataIn(apdu.dataIn, apdu.Lc);
		Session.processApdu(apdu);
	}

	private static void install() throws IOException, CadTransportException {
		Apdu apdu = new Apdu();
		byte[] cmnds = { (byte) 0x00, (byte) 0xA4, (byte) 0x04, 0 };
		apdu.command = cmnds;
		apdu.Lc = 9;
		byte[] data = { (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x62, (byte) 0x03, (byte) 0x01,
				(byte) 0x08, (byte) 0x01 };
		apdu.dataIn = data;
		apdu.Le = 0;
		apdu.setDataIn(apdu.dataIn, apdu.Lc);
		Session.processApdu(apdu);
	}
}
