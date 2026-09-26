package util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PasswordUtil - PBKDF2 hashing (JDK only, no external libs).
 * Stored format: pbkdf2:ITERATIONS:SALT:HASH  (Base64 salt/hash).
 * Login re-hashes with the same salt and compares in constant time.
 */
public class PasswordUtil {
    static final int ITERATIONS = 65536;
    static final int KEY_LEN = 256;

    public static String hash(String password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return hashWithSalt(password, salt, ITERATIONS);
    }

    static String hashWithSalt(String password, byte[] salt, int iters) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iters, KEY_LEN);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = f.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return "pbkdf2:" + iters + ":" + Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(key);
    }

    public static boolean verify(String password, String stored) {
        try {
            if (password == null || stored == null || !stored.startsWith("pbkdf2:")) return false;
            String[] p = stored.split(":");
            int iters = Integer.parseInt(p[1]);
            byte[] salt = Base64.getDecoder().decode(p[2]);
            byte[] want = Base64.getDecoder().decode(p[3]);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iters, want.length * 8);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] got = f.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return MessageDigest.isEqual(got, want);
        } catch (Exception e) {
            return false;
        }
    }

    // java util.PasswordUtil admin123  -> prints a hash (for seed.sql)
    public static void main(String[] args) throws Exception {
        System.out.println(hash(args.length > 0 ? args[0] : "admin123"));
    }
}
