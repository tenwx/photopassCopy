package com.pictureair.photopass.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
  
/**
 * AES加密解密
 * @author bauer_bao
 *
 */
public class AESKeyHelper {  
  
	/**
	 * aes加密字节数组到文件
	 * @param sourceData 源数据
	 * @param encryptedFilePath 目标文件
	 * @param aesKey 16字节密码
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 */
    public static void encrypt(byte[] sourceData, String encryptedFilePath, String aesKey) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
    	PictureAirLog.out("start encryption------>");
        // Here you read the cleartext.
//        FileInputStream fis = new FileInputStream(sourceFilePath);
        // This stream write the encrypted text. This stream will be wrapped by another stream.
        FileOutputStream fos = new FileOutputStream(encryptedFilePath);

        // Length is 16 byte
        // Careful when taking user input!!! http://stackoverflow.com/a/3452620/1188357
        SecretKeySpec sks = new SecretKeySpec(aesKey.getBytes(), "AES");
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);
        // Wrap the output stream
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        // Write bytes
        cos.write(sourceData);
//        int b;
//        byte[] d = new byte[8];
//        
//        while((b = fis.read(d)) != -1) {
//            cos.write(d, 0, b);
//        }
        // Flush and close streams.
        cos.flush();
        cos.close();
        PictureAirLog.out("finish encryption------>");
//        fis.close();
    }
    
    /**
     * aes解密文件到字节数组
     * @param sourceFilePath
     * @param aesKey
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     */
    public static byte[] decrypt(String sourceFilePath, String aesKey) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
    	PictureAirLog.out("start decryption------>");
        FileInputStream fis = new FileInputStream(sourceFilePath);

        ByteArrayOutputStream outs = new ByteArrayOutputStream(1000);
        SecretKeySpec sks = new SecretKeySpec(aesKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sks);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        int b;
        byte[] d = new byte[8];
        while((b = cis.read(d)) != -1) {
            outs.write(d, 0, b);
        }
        outs.flush();
        outs.close();
        cis.close();
        PictureAirLog.out("finish decryption----->");
        return outs.toByteArray();
    }
    
    /**
     * 加密文件到新的文件
     * @param sourceFilePath
     * @param encryptedFilePath
     * @param aesKey
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     */
    public static void encrypt(String sourceFilePath, String encryptedFilePath, String aesKey) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Here you read the cleartext.
        FileInputStream fis = new FileInputStream(sourceFilePath);
        // This stream write the encrypted text. This stream will be wrapped by another stream.
        FileOutputStream fos = new FileOutputStream(encryptedFilePath);

        // Length is 16 byte
        // Careful when taking user input!!! http://stackoverflow.com/a/3452620/1188357
        SecretKeySpec sks = new SecretKeySpec(aesKey.getBytes(), "AES");
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);
        // Wrap the output stream
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        // Write bytes
        int b;
        byte[] d = new byte[8];
        while((b = fis.read(d)) != -1) {
            cos.write(d, 0, b);
        }
        // Flush and close streams.
        cos.flush();
        cos.close();
        fis.close();
    }
    
    /**
     * aes解密文件到文件
     * @param sourceFilePath
     * @param decryptedFilePath
     * @param aesKey
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     */
    public static void decrypt(String sourceFilePath, String decryptedFilePath, String aesKey) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        FileInputStream fis = new FileInputStream(sourceFilePath);

        FileOutputStream fos = new FileOutputStream(decryptedFilePath);
        SecretKeySpec sks = new SecretKeySpec(aesKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sks);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        int b;
        byte[] d = new byte[8];
        while((b = cis.read(d)) != -1) {
            fos.write(d, 0, b);
        }
        fos.flush();
        fos.close();
        cis.close();
    }
} 