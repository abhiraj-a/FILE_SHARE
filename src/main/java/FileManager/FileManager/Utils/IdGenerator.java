package FileManager.FileManager.Utils;

import java.security.SecureRandom;

public class IdGenerator {

    private static  final String STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateTransferId(){
        SecureRandom secureRandom =new SecureRandom();
        StringBuilder sb = new StringBuilder("trnf_");
        for (int i =0 ; i < 10 ; i++){
            sb.append(secureRandom.nextInt(STRING.length()));
        }
        return sb.toString();
    }
}
