package com.guomato.snapshot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Helper {

    public static String generateMD5(Object key) {
        String cacheKey;
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");

            if (key instanceof String) {
                digest.update(((String) key).getBytes());
            } else if (key instanceof File) {
                FileInputStream fis = new FileInputStream((File) key);
                int oneByte;
                while ((oneByte = fis.read()) != -1) {
                    digest.update((byte) oneByte);
                }
            }
            cacheKey = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
