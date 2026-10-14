package edu.smartcampus.m2;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController @Profile("demo & !mysql") @RequestMapping("/api/auth")
public class DemoAuthController {
    record Login(@NotBlank @Size(max=30) String username,@NotBlank @Size(max=100) String password) {}
    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody Login input,HttpServletRequest request) {
        if(!Set.of("student1","student2").contains(input.username())||!"demo-password".equals(input.password()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid demo credentials");
        var old=request.getSession(false);if(old!=null)old.invalidate();
        var session=request.getSession(true);String csrf=UUID.randomUUID().toString();
        session.setAttribute("studentId",input.username());session.setAttribute("csrfToken",csrf);
        return ApiResponse.ok(Map.of("studentId",input.username(),"csrfToken",csrf,"mode","demo"));
    }
    @GetMapping("/session")
    public ApiResponse<?> session(HttpServletRequest request) {
        var s=request.getSession(false);
        if(s==null||s.getAttribute("studentId")==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in");
        return ApiResponse.ok(Map.of("studentId",s.getAttribute("studentId"),"csrfToken",s.getAttribute("csrfToken"),"mode","demo"));
    }
    @PostMapping("/logout") public ApiResponse<?> logout(HttpServletRequest request) {request.getSession(false).invalidate();return ApiResponse.ok(Map.of());}
}
