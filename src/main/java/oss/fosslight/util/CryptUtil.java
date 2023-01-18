/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * 암복호화 유틸 클래스
 */
public class CryptUtil {
	
	public static byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
	/**
	 * AES 256 으로 암호화
	 * 
	 * @param message
	 * @param key
	 * 
	 * @return
	 * 
	 * @throws java.io.UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeySpecException 
	 */
    public static String encryptAES256(String message, String key) throws Exception {
    	if (message == null || message.trim().length() == 0) {
    		return message;
    	}
    	byte[] textBytes = message.getBytes("UTF-8");
		AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
	     SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
	     Cipher cipher = null;
		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
		
		return Base64.encodeBase64String(cipher.doFinal(textBytes));
    }
    
    /**
     * AES 256 으로 복호화
     * 
     * @param encMessage
     * @param key
     * 
     * @return
     * 
     * @throws java.io.UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String decryptAES256(String encMessage, String key) throws Exception {
    	if (encMessage == null || encMessage.trim().length() == 0) {
    		return encMessage;
    	}
    	byte[] textBytes = Base64.decodeBase64(encMessage);
		AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
		SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
		
		return new String(cipher.doFinal(textBytes), "UTF-8");
    }
}

