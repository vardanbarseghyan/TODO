package com.vardan.todo.dto.response;


import com.vardan.todo.enums.Priority;
import com.vardan.todo.enums.TodoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for returning Todo data to the user.
 *
 * WHY don't we just return the Todo entity directly?
 * ---------------------------------------------------
 * If we returned the entity, the JSON would include:
 *   - The full User object (with hashed password! → SECURITY RISK)
 *   - The full Category object (with its own User reference → infinite loop risk)
 *   - Internal fields like "deleted" (the user doesn't need to see this)
 *
 * The response DTO gives us FULL CONTROL over what the user sees.
 * We flatten nested objects into simple IDs and names.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodoResponse {

    private UUID id;
    private String title;
    private String description;
    private TodoStatus status;      // "PENDING", "IN_PROGRESS", "DONE"
    private Priority priority;      // "LOW", "MEDIUM", "HIGH"
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Instead of returning the full Category object, we return just the useful parts.
    // This avoids circular references and keeps the response clean.
    private UUID categoryId;
    private String categoryName;

    // We do NOT include: user (security risk), deleted flag (internal detail)
}
