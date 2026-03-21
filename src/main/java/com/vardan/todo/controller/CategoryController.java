package com.vardan.todo.controller;

import com.vardan.todo.dto.request.CategoryCreateRequest;
import com.vardan.todo.dto.request.CategoryUpdateRequest;
import com.vardan.todo.dto.response.CategoryResponse;
import com.vardan.todo.entity.User;
import com.vardan.todo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Category Management", description = "CRUD operations for categories. "
        + "Categories help organize todos into groups like Work, Personal, Shopping, etc.")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(
            summary = "Get all categories",
            description = "Returns a paginated list of all categories for the authenticated user. "
                    + "Each category includes a todoCount showing how many active (non-deleted) "
                    + "todos are assigned to it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("")
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(@Parameter(hidden = true) @AuthenticationPrincipal User user,
                                                                   Pageable pageable) {
        Page<CategoryResponse> categories = categoryService.getAllCategories(user, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    @Operation(
            summary = "Get a single category by ID",
            description = "Returns a specific category with its todoCount. "
                    + "The category must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated or category belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById( @Parameter(hidden = true) @AuthenticationPrincipal User user,
                                                             @Parameter(description = "UUID of the category") @PathVariable UUID id) {
        CategoryResponse category = categoryService.getCategoryById(user, id);
        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @Operation(
            summary = "Create a new category",
            description = "Creates a new category for the authenticated user. "
                    + "Category names must be unique per user — you cannot have two categories "
                    + "with the same name. Color is optional and must be a valid hex code (e.g., #FF5733)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error — name is blank or color format is invalid"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Duplicate category — a category with this name already exists")
    })
    @PostMapping("")
    public ResponseEntity<CategoryResponse> createCategory( @Parameter(hidden = true) @AuthenticationPrincipal User user,
                                                            @Valid @RequestBody CategoryCreateRequest categoryRequest)
    {
        CategoryResponse category = categoryService.createCategory(user, categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @Operation(
            summary = "Update a category",
            description = "Partially updates a category. Only provided fields will be changed. "
                    + "If renaming, the new name must not conflict with an existing category name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error — name too long or invalid color format"),
            @ApiResponse(responseCode = "401", description = "Not authenticated or category belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate category — another category with this name already exists")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "UUID of the category to update") @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateRequest categoryRequest) {
        CategoryResponse category = categoryService.updateCategory(user, id, categoryRequest);
        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @Operation(
            summary = "Delete a category",
            description = "Permanently deletes a category. This operation is only allowed if "
                    + "no active (non-deleted) todos are assigned to the category. "
                    + "If todos are still using this category, reassign or delete them first."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated or category belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete — category still has active todos")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategoryById(@Parameter(hidden = true) @AuthenticationPrincipal User user,
                                                     @Parameter(description = "UUID of the category to delete") @PathVariable UUID id) {
        categoryService.deleteCategoryById(user, id);
        return ResponseEntity.noContent().build();
    }
}
