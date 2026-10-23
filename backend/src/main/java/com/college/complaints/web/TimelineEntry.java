package com.college.complaints.web;

import java.time.LocalDateTime;

public record TimelineEntry(
        Integer id,
        String type,
        String authorRole,
        String authorName,
        String message,
        LocalDateTime createdAt) {
}
