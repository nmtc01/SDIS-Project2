import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utility {
    public static byte[] sha1(String string) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] id = string.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        return md.digest(id);
    }

    public static byte[] sha256(String string) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] id = string.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        return md.digest(id);
    }

    public static String shatoString(byte[] sha) {
        StringBuffer shaString = new StringBuffer();

        for (int i = 0; i < sha.length; i++) {
            String hex = Integer.toHexString(0xff & sha[i]);
            if (hex.length() == 1)
                shaString.append('0');
            shaString.append(hex);
        }

        // Convert message digest into bitstring
        return shaString.toString();
    }

}
