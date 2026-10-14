package edu.smartcampus.m2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class RecommendationService {
    private final CourseMapper courses;
    private final LearningMapper learning;
    private final ObjectMapper json;
    private final Recommender engine=new Recommender();
    public RecommendationService(CourseMapper courses,LearningMapper learning,ObjectMapper json) {this.courses=courses;this.learning=learning;this.json=json;}
    @Transactional(readOnly=true)
    public List<Course> catalog() {
        Map<String,Set<String>> prerequisites=new HashMap<>();
        for(var p:learning.prerequisites()) prerequisites.computeIfAbsent(p.courseId(),x->new TreeSet<>()).add(p.prerequisiteId());
        return courses.selectList(null).stream().map(c->new Course(c.getId(),c.getTitle(),c.getDescription(),prerequisites.getOrDefault(c.getId(),Set.of()),c.isActive())).sorted(Comparator.comparing(Course::id)).toList();
    }
    public record Student(String studentId,List<String> completed,LearningMapper.Profile profile) {}
    @Transactional(readOnly=true)
    public Student me(String studentId) {
        var p=learning.profile(studentId); if(p==null) p=new LearningMapper.Profile("","");
        return new Student(studentId,learning.completed(studentId),p);
    }
    public record Result(String algorithm,String query,List<Recommender.Match> items,int catalogSize) {}
    @Transactional
    public Result recommend(String studentId,String interests,String goals,int topK) throws JsonProcessingException {
        var all=catalog(); String query=(interests+" "+goals).trim();
        Result result=new Result("TF-IDF cosine similarity",query,engine.rank(all,query,new HashSet<>(learning.completed(studentId)),topK),all.size());
        learning.saveProfile(studentId,interests,goals);
        learning.addHistory(studentId,query,json.writeValueAsString(result)); return result;
    }
    public record History(long id,String query,String createdAt,JsonNode result) {}
    @Transactional(readOnly=true)
    public List<History> history(String studentId) throws JsonProcessingException {
        List<History> list=new ArrayList<>();
        for(var h:learning.history(studentId)) list.add(new History(h.id(),h.queryText(),h.createdAt().toString(),json.readTree(h.resultsJson())));
        return list;
    }
}
