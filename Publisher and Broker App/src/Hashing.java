import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {

    public String hashMD5 (String raw){

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestedBytes = md.digest(raw.getBytes());
            BigInteger bi = new BigInteger(1, digestedBytes);
            String hashed = bi.toString(16);
            while (hashed.length() < 32) {
                hashed = "0" + hashed;
            }
            return hashed;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public int getIntValue (String hash){
        char[] chars = hash.toCharArray();
        int sum = 0;
        for (char c: chars) sum += (int) c;
        return sum;
    }


}
