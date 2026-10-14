package edu.smartcampus.m2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ApiGuard extends OncePerRequestFilter {
    private final StudentIdentity identity; private final ObjectMapper json;
    public ApiGuard(StudentIdentity identity,ObjectMapper json){this.identity=identity;this.json=json;}
    protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException {
        res.setHeader("X-Content-Type-Options","nosniff");
        res.setHeader("Content-Security-Policy","default-src 'self'; style-src 'self'; script-src 'self'; frame-ancestors 'none'; base-uri 'self'");
        // Servlet path is decoded, context-relative and excludes matrix parameters,
        // matching the path Spring MVC routes rather than the raw wire URI.
        String path=req.getServletPath();
        if(path.startsWith("/api/")) res.setHeader("Cache-Control","no-store");
        if(path.startsWith("/api/m2/")||path.equals("/api/auth/logout")) {
            String student=identity.resolve(req);
            if(student==null){reject(res,401,"UNAUTHENTICATED","Please sign in");return;}
            req.setAttribute("studentId",student);
            if(!req.getMethod().equals("GET")&&!req.getMethod().equals("HEAD")&&identity instanceof SessionIdentity) {
                var session=req.getSession(false); String expected=(String)session.getAttribute("csrfToken"); String actual=req.getHeader("X-CSRF-Token");
                if(expected==null||actual==null||!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),actual.getBytes(StandardCharsets.UTF_8))) {
                    reject(res,403,"CSRF_REJECTED","Refresh your sign-in session");return;
                }
            }
        }
        chain.doFilter(req,res);
    }
    private void reject(HttpServletResponse res,int status,String code,String message)throws IOException {
        res.setStatus(status);res.setContentType("application/json;charset=UTF-8");json.writeValue(res.getWriter(),ApiResponse.error(code,message));
    }
}
