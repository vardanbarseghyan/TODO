package com.vardan.todo.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for returning Category data to the user.
 *
 * Simple and flat — no nested objects.
 * We include todoCount so the user can see how many todos
 * are assigned to each category without making a separate API call.
 *
 * We do NOT include the User object (same reason as TodoResponse — security).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryResponse {

    private UUID id;
    private String name;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // How many active (non-deleted) todos belong to this category.
    // This is a useful piece of info for the frontend — for example,
    // showing "Work (5)" or "Personal (12)" in a sidebar.
    private long todoCount;
}