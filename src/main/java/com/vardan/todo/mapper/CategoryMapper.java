package com.vardan.todo.mapper;

import com.vardan.todo.dto.request.CategoryCreateRequest;
import com.vardan.todo.dto.response.CategoryResponse;
import com.vardan.todo.entity.Category;
import com.vardan.todo.entity.User;
import org.springframework.stereotype.Component;
/**
 * CategoryMapper — translates between Category DTOs and Entity.
 *
 * Same role as TodoMapper:
 *   Request DTO → Entity (for saving to database)
 *   Entity → Response DTO (for sending to user)
 *
 * The todoCount parameter in toResponse() requires the service to calculate
 * how many non-deleted todos belong to this category. The mapper doesn't
 * do this calculation itself — it just receives the number and puts it
 * in the response. This keeps the mapper "dumb" (just mapping fields)
 * and the service "smart" (handling business logic).
 */
@Component
public class CategoryMapper {

    /**
     * Converts a CategoryCreateRequest into a Category entity.
     *
     * @param request  The DTO from the user's request
     * @param user     The logged-in user (from @AuthenticationPrincipal)
     */
    public Category toEntity(CategoryCreateRequest request, User user) {
        return Category.builder()
                .name(request.getName())
                .color(request.getColor())
                .user(user)
                .build();
    }

    /**
     * Converts a Category entity into a CategoryResponse.
     *
     * @param category   The entity from the database
     * @param todoCount  Number of active (non-deleted) todos in this category
     */
    public CategoryResponse toResponse(Category category, long todoCount) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .color(category.getColor())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .todoCount(todoCount)
                .build();
    }
}
