package FileManager.FileManager.Utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ClerkUserPrincipal {

    private String clerkId;

    private String email;

    private String name;
}
