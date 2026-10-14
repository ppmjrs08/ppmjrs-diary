package edu.smartcampus.m2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component @Profile("demo & !mysql")
public class SessionIdentity implements StudentIdentity {
    public String resolve(HttpServletRequest request) {
        var session=request.getSession(false);
        return session==null?null:(String)session.getAttribute("studentId");
    }
}
