package show.grip.example.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.spec.AlgorithmParameterSpec;

public class AESCryptor {
    private static final byte[] IV = new byte[16];

    public static String encrypt(
            String source,
            String key
    ) {
        try {
            return new String(Base64.encodeBase64(encrypt(source.getBytes("UTF-8"), key.getBytes())));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static byte[] encrypt(byte[] source, byte[] key) {
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(IV);
        SecretKeySpec newKey = new SecretKeySpec(key, "AES");

        try {
            Cipher cipher = null;
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

            return cipher.doFinal(source);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
