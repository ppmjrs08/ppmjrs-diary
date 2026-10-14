package edu.smartcampus.m2;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RecommenderTest {
    final List<Course> courses = List.of(
        new Course("CS101", "Programming", "java variables loops", Set.of(), true),
        new Course("CS201", "Machine Learning", "machine learning prediction", Set.of("CS101"), true),
        new Course("CS301", "Deep Learning", "machine learning neural networks", Set.of("CS101", "CS201"), true),
        new Course("CS999", "Hidden Machine Learning", "machine learning", Set.of(), false));
    final Recommender engine = new Recommender();

    @Test void recommendsEligibleRelevantCourse() {
        var r = engine.rank(courses, "machine learning", Set.of("CS101"), 3);
        assertEquals(List.of("CS201"), r.stream().map(x -> x.course().id()).toList());
        assertTrue(r.get(0).score() > 0 && r.get(0).score() <= 1);
        assertEquals(List.of("learning", "machine"), r.get(0).matchingTerms());
    }
    @Test void requiresEveryPrerequisite() {
        assertTrue(engine.rank(courses, "machine learning", Set.of("CS201"), 3).isEmpty());
    }
    @Test void excludesCompletedAndInactiveCourses() {
        assertEquals("CS301", engine.rank(courses, "machine learning", Set.of("CS101", "CS201"), 3).get(0).course().id());
    }
    @Test void unknownWordsReturnEmpty() { assertTrue(engine.rank(courses, "astrophysics", Set.of(), 3).isEmpty()); }
    @Test void emptyCatalogReturnsEmpty() { assertTrue(engine.rank(List.of(), "java", Set.of(), 3).isEmpty()); }
    @Test void stopwordsReturnEmpty() { assertTrue(engine.rank(courses, "I want to learn about", Set.of(), 3).isEmpty()); }
    @Test void matchingIsCaseInsensitive() { assertEquals(engine.rank(courses, "JAVA", Set.of(), 3), engine.rank(courses, "java", Set.of(), 3)); }
    @Test void blankInterestIsRejected() { assertThrows(IllegalArgumentException.class, () -> engine.rank(courses, " ", Set.of(), 3)); }
    @Test void rejectsLimitOutsideOneToThree() { for (int k : new int[]{0,4}) assertThrows(IllegalArgumentException.class, () -> engine.rank(courses, "java", Set.of(), k)); }
    @Test void tiesAreStableAndLimitIsApplied() {
        var same = List.of(new Course("B", "Java", "java", Set.of(), true), new Course("A", "Java", "java", Set.of(), true));
        assertEquals("A", engine.rank(same, "java", Set.of(), 1).get(0).course().id());
    }
    @Test void keywordBaselineUsesSameEligibility() {
        assertEquals(List.of("CS201"), engine.baseline(courses, "machine learning", Set.of("CS101"), 3).stream().map(x->x.course().id()).toList());
    }
}
