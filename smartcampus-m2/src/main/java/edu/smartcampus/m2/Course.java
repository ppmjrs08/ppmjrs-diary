package edu.smartcampus.m2;

import java.util.Set;

public record Course(String id, String title, String description, Set<String> prerequisites, boolean active) {
    public Course { prerequisites = Set.copyOf(prerequisites); }
}
