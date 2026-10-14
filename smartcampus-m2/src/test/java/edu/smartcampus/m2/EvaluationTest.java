package edu.smartcampus.m2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("demo")
@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:evaluation;MODE=MySQL;DB_CLOSE_DELAY=-1")
class EvaluationTest {
    @Autowired RecommendationService service;
    @Test void comparesTwentySyntheticProfilesWithKeywordBaseline()throws Exception{
        var all=service.catalog();assertEquals(20,all.size());var engine=new Recommender();
        var lines=new String(Objects.requireNonNull(getClass().getResourceAsStream("/evaluation-profiles.tsv")).readAllBytes(),StandardCharsets.UTF_8).lines().toList();
        assertEquals(21,lines.size());List<String> report=new ArrayList<>();report.add("profile,method,returned,relevant,precision_at_returned,completed_violations,prerequisite_violations,ids");
        for(String line:lines.subList(1,lines.size())){
            String[] f=line.split("\t",-1);Set<String> completed=ids(f[2]),relevant=ids(f[3]);
            for(String method:List.of("tfidf","keyword")){
                var items=method.equals("tfidf")?engine.rank(all,f[1],completed,3):engine.baseline(all,f[1],completed,3);
                long hits=items.stream().filter(x->relevant.contains(x.course().id())).count();
                long done=items.stream().filter(x->completed.contains(x.course().id())).count();
                long invalid=items.stream().filter(x->!completed.containsAll(x.course().prerequisites())).count();
                assertEquals(0,done,f[0]);assertEquals(0,invalid,f[0]);assertTrue(items.size()<=3);
                report.add(String.join(",",f[0],method,""+items.size(),""+hits,items.isEmpty()?"NA":String.format(Locale.ROOT,"%.4f",(double)hits/items.size()),""+done,""+invalid,String.join("|",items.stream().map(x->x.course().id()).toList())));
            }
        }
        Files.write(Path.of("target/evaluation-results.csv"),report,StandardCharsets.UTF_8);
    }
    private Set<String> ids(String s){return s.isBlank()?Set.of():new HashSet<>(Arrays.asList(s.split(",")));}
}
