package edu.smartcampus.m2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Temporary single-user integration bridge, supplied by environment, never a public studentId header. */
@Component @Profile("mysql & !demo")
public class TokenIdentity implements StudentIdentity {
    private final String token,studentId;
    public TokenIdentity(@Value("${M2_SHARED_TOKEN}")String token,@Value("${M2_SHARED_STUDENT_ID}")String studentId) {
        if(token.length()<32||studentId.isBlank()) throw new IllegalArgumentException("Configure a random token of at least 32 characters and a student ID");
        this.token=token;this.studentId=studentId;
    }
    public String resolve(HttpServletRequest request) {
        String value=request.getHeader("Authorization");
        if(value==null||!value.startsWith("Bearer ")) return null;
        return MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),value.substring(7).getBytes(StandardCharsets.UTF_8))?studentId:null;
    }
}
