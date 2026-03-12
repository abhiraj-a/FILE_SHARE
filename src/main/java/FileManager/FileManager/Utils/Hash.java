package FileManager.FileManager.Utils;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Hash {

    public static String hash(String s){
        try {
            MessageDigest messageDigest= MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
