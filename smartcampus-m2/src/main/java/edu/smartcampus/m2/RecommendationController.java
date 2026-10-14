package edu.smartcampus.m2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/m2")
public class RecommendationController {
    private final RecommendationService service;
    public RecommendationController(RecommendationService service){this.service=service;}
    record Input(@NotBlank @Size(max=500) String interests,@Size(max=300) String goals,@Min(1) @Max(3) Integer topK) {}
    private String id(HttpServletRequest request){return (String)request.getAttribute("studentId");}
    @GetMapping("/me") public ApiResponse<?> me(HttpServletRequest r){return ApiResponse.ok(service.me(id(r)));}
    @GetMapping("/courses") public ApiResponse<?> courses(){return ApiResponse.ok(service.catalog().stream().filter(Course::active).toList());}
    @GetMapping("/history") public ApiResponse<?> history(HttpServletRequest r)throws Exception{return ApiResponse.ok(service.history(id(r)));}
    @PostMapping("/recommendations") public ApiResponse<?> recommendations(@Valid @RequestBody Input input,HttpServletRequest r)throws Exception{
        return ApiResponse.ok(service.recommend(id(r),input.interests().trim(),input.goals()==null?"":input.goals().trim(),input.topK()==null?3:input.topK()));
    }
}
