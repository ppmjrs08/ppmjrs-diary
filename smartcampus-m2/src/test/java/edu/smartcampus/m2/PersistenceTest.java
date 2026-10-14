package edu.smartcampus.m2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.concurrent.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("demo")
@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:persistence;MODE=MySQL;DB_CLOSE_DELAY=-1")
class PersistenceTest {
    @Autowired RecommendationService service;
    @Autowired JdbcTemplate db;
    @Test void simultaneousFirstRequestsSaveProfileAndAllHistories()throws Exception {
        String user="new-"+UUID.randomUUID();var pool=Executors.newFixedThreadPool(4);var gate=new CountDownLatch(1);List<Future<?>> futures=new ArrayList<>();
        try{
            for(int i=0;i<4;i++)futures.add(pool.submit(()->{gate.await();service.recommend(user,"database","",3);return null;}));
            gate.countDown();for(var f:futures)f.get(20,TimeUnit.SECONDS);
            assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM m2_profile WHERE student_id=?",Integer.class,user));
            assertEquals(4,db.queryForObject("SELECT COUNT(*) FROM m2_history WHERE student_id=?",Integer.class,user));
        }finally{pool.shutdownNow();}
    }
}
