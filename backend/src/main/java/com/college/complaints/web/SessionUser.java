package com.college.complaints.web;

import java.io.Serializable;

public record SessionUser(String id, String name, String email, String role) implements Serializable {
}
