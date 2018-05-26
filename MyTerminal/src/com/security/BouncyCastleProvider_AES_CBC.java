package com.security;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class BouncyCastleProvider_AES_CBC {
	// The default block size
	public static int blockSize = 16;

	Cipher encryptCipher = null;

	// The key
	byte[] key = null;
	// The initialization vector needed by the CBC mode
	byte[] IV = null;

	public BouncyCastleProvider_AES_CBC() {
		// for a 192 key you must install the unrestricted policy files
		// from the JCE/JDK downloads page
		key = "Smart!Cards!18VS".getBytes();
		System.out.println(key);
		// default IV value initialized with 0
		IV = new byte[blockSize];
	}

	public void InitCiphers() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchProviderException,
			NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		// 1. create the cipher using Bouncy Castle Provider
		encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
		// 2. create the key
		SecretKey keyValue = new SecretKeySpec(key, "AES");
		// 3. create the IV
		AlgorithmParameterSpec IVspec = new IvParameterSpec(IV);
		// 4. init the cipher
		encryptCipher.init(Cipher.ENCRYPT_MODE, keyValue, IVspec);

	}

	public byte[] CBCEncrypt(byte[] key, byte[] pin) throws IOException, ShortBufferException,
			IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchProviderException, NoSuchPaddingException, InvalidAlgorithmParameterException {

		InitCiphers();
		byte[] buffer = new byte[blockSize];
		byte[] cipherBlock = new byte[encryptCipher.getOutputSize(buffer.length)];

		encryptCipher.update(pin, 0, pin.length, cipherBlock);

		encryptCipher.doFinal(cipherBlock, 0);
		return cipherBlock;

	}

}
