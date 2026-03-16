package com.vardan.todo.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for creating a new Category.
 *
 * Categories are simple — just a name and an optional color.
 * The user is NOT included here because we get it from @AuthenticationPrincipal,
 * same pattern as TodoCreateRequest.
 */

@Data
@Builder
//@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name must be less than 50 characters")
    private String name;

    // Optional hex color code like "#FF5733" or "#fff"
    // @Pattern uses a regex (regular expression) to validate the format:
    //   ^       = start of string
    //   #       = must start with #
    //   [A-Fa-f0-9] = only hex characters allowed (A-F, a-f, 0-9)
    //   {3,6}   = between 3 and 6 characters after the #
    //   $       = end of string
    // So it accepts: #FFF, #fff, #FF5733, #ff5733
    // But rejects: red, 123456, ##FFFF, #GGG
    @Pattern(regexp = "^#[A-Fa-f0-9]{3,6}$", message = "Color must be a valid hex code (e.g., #FF5733)")
    private String color; // optional — user can leave it null
}
