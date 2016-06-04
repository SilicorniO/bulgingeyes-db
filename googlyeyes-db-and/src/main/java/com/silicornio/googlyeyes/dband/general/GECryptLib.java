package com.silicornio.googlyeyes.dband.general;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class taken from GitHub (free license)
 */
public class GECryptLib {

	/**
	 * Encryption mode enumeration
	 */
	private enum EncryptMode {
		ENCRYPT, DECRYPT;
	}

	// cipher to be used for encryption and decryption
	Cipher _cx;

	// encryption key and initialization vector
	byte[] _key, _iv;

	//key and salt to use
	private String mKey;
	private String mSalt;

	public GECryptLib(String password) throws NoSuchAlgorithmException, NoSuchPaddingException {
		// initialize the cipher with transformation AES/CBC/PKCS5Padding
		_cx = Cipher.getInstance("AES/CBC/PKCS5Padding");
		_key = new byte[32]; //256 bit key space
		_iv = new byte[16]; //128 bit IV

		//generate the password and salt
		mKey = getKey(password);
		mSalt = getSalt(password);
	}

	/**
	 *
	 * @param _inputText
	 *            Text to be encrypted or decrypted
	 * @param _encryptionKey
	 *            Encryption key to used for encryption / decryption
	 * @param _mode
	 *            specify the mode encryption / decryption
	 * @param _initVector
	 * 	      Initialization vector
	 * @return encrypted or decrypted string based on the mode
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private String encryptDecrypt(String _inputText, String _encryptionKey, EncryptMode _mode, String _initVector)
			throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		String _out = "";

		int len = _encryptionKey.getBytes("UTF-8").length; // length of the key	provided

		if (_encryptionKey.getBytes("UTF-8").length > _key.length)
			len = _key.length;

		int ivlen = _initVector.getBytes("UTF-8").length;

		if(_initVector.getBytes("UTF-8").length > _iv.length)
			ivlen = _iv.length;

		System.arraycopy(_encryptionKey.getBytes("UTF-8"), 0, _key, 0, len);
		System.arraycopy(_initVector.getBytes("UTF-8"), 0, _iv, 0, ivlen);

		SecretKeySpec keySpec = new SecretKeySpec(_key, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(_iv);

		// encryption
		if (_mode.equals(EncryptMode.ENCRYPT)) {
			_cx.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);// Initialize this cipher instance
			byte[] results = _cx.doFinal(_inputText.getBytes("UTF-8")); // Finish
			_out = Base64.encodeToString(results, Base64.DEFAULT); // ciphertext

		}else if (_mode.equals(EncryptMode.DECRYPT)) {
			_cx.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);// Initialize this ipher instance

			byte[] decodedValue = Base64.decode(_inputText.getBytes(), Base64.DEFAULT);
			byte[] decryptedVal = _cx.doFinal(decodedValue); // Finish
			_out = new String(decryptedVal);
		}
		return _out; // return encrypted/decrypted string
	}

	/***
	 * This function encrypts the plain text to cipher text using the key
	 * provided. You'll have to use the same key for decryption
	 *
	 * @param _plainText
	 *            Plain text to be encrypted
	 * @param _key
	 *            Encryption Key. You'll have to use the same key for decryption
	 * @param _iv
	 * 	    initialization Vector
	 * @return returns encrypted (cipher) text
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */

	public String encrypt(String _plainText, String _key, String _iv)
			throws InvalidKeyException, UnsupportedEncodingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException,
			BadPaddingException {
		return encryptDecrypt(_plainText, _key, EncryptMode.ENCRYPT, _iv);
	}

	/***
	 * This funtion decrypts the encrypted text to plain text using the key
	 * provided. You'll have to use the same key which you used during
	 * encryprtion
	 *
	 * @param _encryptedText
	 *            Encrypted/Cipher text to be decrypted
	 * @param _key
	 *            Encryption key which you used during encryption
	 * @param _iv
	 * 	    initialization Vector
	 * @return encrypted value
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String decrypt(String _encryptedText, String _key, String _iv)
			throws InvalidKeyException, UnsupportedEncodingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException,
			BadPaddingException {
		return encryptDecrypt(_encryptedText, _key, EncryptMode.DECRYPT, _iv);
	}

	/**
	 * Generate a key with the password received
	 * @param password String password to use
	 * @return String key
     */
	private static String getKey(String password){
		String key = Base64.encodeToString(password.getBytes(), Base64.NO_WRAP);
		while(key.length()<32){
			key += key;
		}
		return key.substring(0,32); //32 bytes = 256
	}

	/**
	 * Generate a salt with the password received
	 * @param password String password to use
	 * @return String salt
     */
	private static String getSalt(String password){
		String key = Base64.encodeToString(password.getBytes(), Base64.NO_WRAP);
		while(key.length()<16){
			key += key;
		}
		return key.substring(0,16); //16 bytes = 128 bit
	}

	/**
	 * Encrypt with default values
	 * @param text String to encrypt
	 * @return encrypted text
	 */
	public String encrypt(String text){

		try {
			return encrypt(text, mKey, mSalt).trim(); // encrypt
		} catch (Exception e) {
			GEL.e("Exception encrypting: " + e.toString());
		}

		return "";
	}

	/**
	 * Decrypt with default values
	 * @param text String to decrypt
	 * @return String decrypted text
	 */
	public String decrypt(String text){

		try {
			return decrypt(text, mKey, mSalt).trim(); // encrypt
		} catch (Exception e) {
			GEL.e("Exception decrypting: " + e.toString());
		}

		return "";
	}

}