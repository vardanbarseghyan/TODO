package com.vardan.todo.controller;

import com.vardan.todo.dto.request.CategoryCreateRequest;
import com.vardan.todo.dto.request.CategoryUpdateRequest;
import com.vardan.todo.dto.response.CategoryResponse;
import com.vardan.todo.entity.Category;
import com.vardan.todo.entity.User;
import com.vardan.todo.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    @GetMapping("")
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(@AuthenticationPrincipal User user, Pageable pageable) {
        Page<CategoryResponse> categories = categoryService.getAllCategories(user, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        CategoryResponse category = categoryService.getCategoryById(user, id);
        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @PostMapping("")
    public ResponseEntity<CategoryResponse> createCategory(@AuthenticationPrincipal User user, @Valid @RequestBody CategoryCreateRequest categoryRequest) {
        CategoryResponse category = categoryService.createCategory(user, categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@AuthenticationPrincipal User user, @PathVariable UUID id, @Valid @RequestBody CategoryUpdateRequest categoryRequest) {
        CategoryResponse category = categoryService.updateCategory(user, id, categoryRequest);
        return ResponseEntity.status(HttpStatus.OK).body(category);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategoryById(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        categoryService.deleteCategoryById(user, id);
        return ResponseEntity.noContent().build();
    }
}
