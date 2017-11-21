package org.xonyne.commons;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

	public static String encodeAsMD5(String text) throws NoSuchAlgorithmException{
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.update(text.getBytes());
		byte[] messageDigestMD5 = messageDigest.digest();
		StringBuilder stringBuilder = new StringBuilder();
		for (byte bytes : messageDigestMD5) {
			stringBuilder.append(String.format("%02x", bytes & 0xff));
		}
		
		return stringBuilder.toString();
	}
}
