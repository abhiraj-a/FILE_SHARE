package FileManager.FileManager.Utils;


import lombok.Data;

@Data
public class ClerkWebhookPayload {

    private String type;

    private DataPayload dataPayload;

    @Data
    public static class DataPayload{
        private String id;
        private String object;
    }
}
