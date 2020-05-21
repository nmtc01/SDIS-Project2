import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utility {
    public static byte[] sha1(String string) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] id = string.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        return md.digest(id);
    }

    public static String sha256toString(byte[] sha256) {
        StringBuffer sha256String = new StringBuffer();

        for (int i = 0; i < sha256.length; i++) {
            String hex = Integer.toHexString(0xff & sha256[i]);
            if (hex.length() == 1)
                sha256String.append('0');
            sha256String.append(hex);
        }

        // Convert message digest into bitstring
        return sha256String.toString();
    }

}
