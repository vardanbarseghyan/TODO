package com.vardan.todo.dto.request;

import com.vardan.todo.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for creating a new Todo.
 *
 * WHY do we use a DTO instead of accepting the Todo entity directly?
 * -----------------------------------------------------------------
 * If we accepted the entity, the user could send dangerous fields like:
 *   - "id": "some-uuid"        → they choose their own ID
 *   - "user": { "id": "..." }  → they assign the todo to another user
 *   - "deleted": true           → they create an already-deleted todo
 *
 * The DTO acts as a FILTER — it only accepts the fields WE allow.
 * The mapper then safely converts this DTO into an entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodoCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description; // optional — user doesn't have to provide a description

    private Priority priority; // optional — defaults to MEDIUM in the entity if not provided

    private LocalDateTime dueDate; // optional — not every todo needs a deadline

    private UUID categoryId; // optional — user can assign a category, or leave it null
    // We only accept the category ID, not the whole Category object.
    // The service will look up the Category by this ID in the database.
}
