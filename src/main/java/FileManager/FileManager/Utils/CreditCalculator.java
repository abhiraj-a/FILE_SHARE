package FileManager.FileManager.Utils;

public class CreditCalculator {

    private static final double BYTES_PER_CREDIT = 1024*1024;
    public static int calculate(long fileSizeBytes){
        return Math.max(1, (int) Math.floor((double) fileSizeBytes / BYTES_PER_CREDIT));
    }
}
