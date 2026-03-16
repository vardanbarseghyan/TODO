package com.vardan.todo.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing Category.
 *
 * Same as TodoUpdateRequest — all fields are optional for partial updates.
 * If the user only wants to change the color, they send just { "color": "#00FF00" }
 * and the name stays unchanged.
 *
 * Notice: no @NotBlank on name here (unlike CategoryCreateRequest).
 * If we required name on update, the user couldn't update just the color
 * without also resending the name they don't want to change.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryUpdateRequest {

    @Size(max = 50, message = "Category name must be less than 50 characters")
    private String name; // optional — only update if provided

    @Pattern(regexp = "^#[A-Fa-f0-9]{3,6}$", message = "Color must be a valid hex code (e.g., #FF5733)")
    private String color; // optional — only update if provided
}