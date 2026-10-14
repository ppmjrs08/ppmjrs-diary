package edu.smartcampus.m2;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.time.LocalDateTime;

@Mapper
public interface LearningMapper {
    record Prerequisite(String courseId,String prerequisiteId) {}
    record Profile(String interests,String goals) {}
    record History(long id,String queryText,String resultsJson,LocalDateTime createdAt) {}
    @Select("SELECT course_id, prerequisite_id FROM m2_prerequisite ORDER BY course_id, prerequisite_id") List<Prerequisite> prerequisites();
    @Select("SELECT course_id FROM m2_completed WHERE student_id=#{studentId} ORDER BY course_id") List<String> completed(String studentId);
    @Select("SELECT interests, goals FROM m2_profile WHERE student_id=#{studentId}") Profile profile(String studentId);
    @Insert("INSERT INTO m2_profile(student_id,interests,goals) VALUES(#{studentId},#{interests},#{goals}) ON DUPLICATE KEY UPDATE interests=#{interests}, goals=#{goals}") void saveProfile(@Param("studentId")String studentId,@Param("interests")String interests,@Param("goals")String goals);
    @Insert("INSERT INTO m2_history(student_id,query_text,results_json,created_at) VALUES(#{studentId},#{query},#{results},CURRENT_TIMESTAMP)") void addHistory(@Param("studentId")String studentId,@Param("query")String query,@Param("results")String results);
    @Select("SELECT id, query_text, results_json, created_at FROM m2_history WHERE student_id=#{studentId} ORDER BY id DESC LIMIT 20") List<History> history(String studentId);
}
