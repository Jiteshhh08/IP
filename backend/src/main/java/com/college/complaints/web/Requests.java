package com.college.complaints.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class Requests {

    private Requests() {
    }

    public record Register(
            @NotBlank(message = "Student ID is required")
            @Pattern(regexp = "[A-Za-z0-9]{3,20}", message = "Student ID must be 3-20 letters/digits")
            String studentId,

            @NotBlank(message = "Name is required")
            @Size(max = 100, message = "Name must be at most 100 characters")
            String name,

            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
            String password) {
    }

    public record Login(
            @NotBlank(message = "Student ID or email is required") String identifier,
            @NotBlank(message = "Password is required") String password) {
    }

    public record AdminLogin(
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Password is required") String password) {
    }

    public record Complaint(
            @NotBlank(message = "Title is required")
            @Size(max = 150, message = "Title must be at most 150 characters")
            String title,

            @NotBlank(message = "Category is required") String category,

            @NotBlank(message = "Description is required")
            @Size(max = 5000, message = "Description must be at most 5000 characters")
            String description) {
    }

    public record StatusUpdate(
            @NotBlank(message = "Status is required") String status,
            @Size(max = 2000, message = "Remark must be at most 2000 characters") String remark) {
    }
}
