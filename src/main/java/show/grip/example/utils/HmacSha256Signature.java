package show.grip.example.utils;

import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacSha256Signature {
    private static final String HMAC_SHA_ALGORITHM = "HmacSHA256";

    private static String toHexString(byte[] bytes) {
        try (Formatter formatter = new Formatter();) {

            for (byte b : bytes) {
                formatter.format("%02x", b);
            }

            return formatter.toString();
        }
    }

    public static String digest(String data, String key) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }

    public static String generateFingerprint(String serviceId, long timestamp, String secureKey)  {
        String data = serviceId + ";" + timestamp;

        try {
            return digest(data, secureKey);
        } catch (Exception e) {
            return null;
        }
    }
}
