package com.vardan.todo.dto.request;


import com.vardan.todo.enums.Priority;
import com.vardan.todo.enums.TodoStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for updating an existing Todo.
 *
 * KEY DIFFERENCE from TodoCreateRequest:
 * - Title is NOT @NotBlank here — it's optional.
 * - Status is included here (user can change PENDING → IN_PROGRESS → DONE).
 *
 * WHY are all fields optional?
 * ----------------------------
 * This supports PARTIAL UPDATES. The user might only want to change the status
 * without touching anything else. So the service should only update fields
 * that are NOT null in this request — if a field is null, leave the existing value alone.
 *
 * Example: User sends { "status": "DONE" }
 * → Only update the status, keep the existing title, description, priority, etc.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodoUpdateRequest {

    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title; // optional — only update if provided

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description; // optional

    private TodoStatus status; // optional — this is the main reason for updating!
    // Allowed values: PENDING, IN_PROGRESS, DONE

    private Priority priority; // optional

    private LocalDateTime dueDate; // optional

    private UUID categoryId; // optional — user can reassign to a different category
}