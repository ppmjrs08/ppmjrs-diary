package edu.smartcampus.m2;

import jakarta.servlet.http.HttpServletRequest;

/** Replace with the team's verified shared principal; never trust request studentId. */
public interface StudentIdentity { String resolve(HttpServletRequest request); }
