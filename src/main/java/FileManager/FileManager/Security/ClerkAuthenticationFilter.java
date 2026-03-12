//package FileManager.FileManager.Security;
//
//import FileManager.FileManager.Utils.ClerkUserPrincipal;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import tools.jackson.databind.JsonNode;
//import tools.jackson.databind.ObjectMapper;
//
//import java.io.IOException;
//import java.security.PublicKey;
//import java.util.Base64;
//import java.util.Collections;
//
//@Component
////@RequiredArgsConstructor
//public class ClerkAuthenticationFilter extends OncePerRequestFilter {
//
//    @Value("${clerk.issuer}")
//    private  String clerkIssuer;
//
//    @Autowired
//    private final ClerkJwksProvider clerkJwksProvider;
//
//    public ClerkAuthenticationFilter(
//            ClerkJwksProvider clerkJwksProvider) {
//        this.clerkJwksProvider = clerkJwksProvider;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//
//        String authHeader = request.getHeader("Authorization");
//
//        if(authHeader==null||!authHeader.startsWith("Bearer ")){
//            response.sendError(HttpServletResponse.SC_FORBIDDEN , "Invalid credentials");
//
//            return;
//        }
//
//
//        try {
//            String token =authHeader.substring(7);
//
//            String[] chunks = token.split("\\.");
//
//            String headerJson =new String(Base64.getUrlDecoder().decode(chunks[0]));
//
//            ObjectMapper mapper =new ObjectMapper();
//
//            JsonNode node = mapper.readTree(headerJson);
//
//            String kid = node.get("kid").asString();
//
//            PublicKey publicKey = clerkJwksProvider.getPublicKey(kid);
//
//         Claims claims = Jwts.parser()
//                 .verifyWith(publicKey)
//                 .requireIssuer(clerkIssuer)
//                 .build().parseSignedClaims(token)
//                 .getPayload();
//
//         String clerkId = claims.getSubject();
//         String email = claims.get("email", String.class);
//            ClerkUserPrincipal principal = new ClerkUserPrincipal(clerkId,email );
//
//            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                    principal , null , Collections.emptyList());
//
//
//            SecurityContextHolder.getContext().setAuthentication(authToken);
//
//            filterChain.doFilter(request,response);
//        }catch (Exception e){
//            response.sendError(HttpServletResponse.SC_FORBIDDEN , "Invalid: " +e.getMessage());
//        }
//    }
//}

package FileManager.FileManager.Security;

import FileManager.FileManager.Repository.UserRepo;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ClerkAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepo userRepo;
    @Value("${clerk.issuer}")
    private String clerkIssuer;

    private final ClerkJwksProvider clerkJwksProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);
                String[] chunks = token.split("\\.");

                String headerJson =
                        new String(Base64.getUrlDecoder().decode(chunks[0]));

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(headerJson);
                String kid = node.get("kid").asText();

                PublicKey publicKey =
                        clerkJwksProvider.getPublicKey(kid);

                Claims claims = Jwts.parser()
                        .verifyWith(publicKey)
                        .requireIssuer(clerkIssuer)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                ClerkUserPrincipal principal =
                        new ClerkUserPrincipal(
                                claims.getSubject(),
                                claims.get("email", String.class),
                                claims.get("name", String.class)
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                Collections.emptyList()
                        );

                userRepo.findByClerkId(principal.getClerkId()).ifPresent(u->{
                    if(u.isDeleted()){
                        try {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN,"Account deleted");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                });

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_FORBIDDEN , "Unauthorized");
            return;
        }

        filterChain.doFilter(request, response);
    }
}