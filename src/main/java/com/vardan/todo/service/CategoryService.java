package com.vardan.todo.service;

import com.vardan.todo.dto.request.CategoryCreateRequest;
import com.vardan.todo.dto.request.CategoryUpdateRequest;
import com.vardan.todo.dto.response.CategoryResponse;
import com.vardan.todo.entity.Category;
import com.vardan.todo.entity.User;
import com.vardan.todo.exception.DuplicateCategoryException;
import com.vardan.todo.exception.ResourceNotFoundException;
import com.vardan.todo.exception.UnauthorizedException;
import com.vardan.todo.mapper.CategoryMapper;
import com.vardan.todo.repository.CategoryRepository;
import com.vardan.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final TodoRepository todoRepository;
    private final CategoryMapper categoryMapper;//mihat CategoryMapper:: sencel pordzel!!!

    private Category findCategoryByIdAndCheckIfTodoBelongInUser(UUID id, User user) {
//        Category category = categoryRepository.getReferenceById(id);
        Category category = categoryRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("Category which id was " + id + ", not founded"));
        if (!category.getUser().getId().equals(user.getId()))
            throw new UnauthorizedException("Category not belong in user");
        return category;
    }

    @Cacheable(value = "user_categories", key = "#user.id")
    public List<CategoryResponse> getAllCategoriesCached(User user)
    {
        List<Category> categories = categoryRepository.findAllByUser(user);
        return categories.stream().map(category -> {
            long todoCount = todoRepository.countByCategoryIdAndDeletedFalse(category.getId());
            return categoryMapper.toResponse(category, todoCount);
        }).toList();
    }


    public Page<CategoryResponse> getAllCategoriesPageable(User user, Pageable pageable)
    {
        Page<Category> categories = categoryRepository.findAllByUser(user, pageable);
        Page<CategoryResponse> responsePage = categories.map(category -> {
            long todoCount = todoRepository.countByCategoryIdAndDeletedFalse(category.getId());
            return categoryMapper.toResponse(category, todoCount);
        });
        return responsePage;
    }

    public CategoryResponse getCategoryById(User user, UUID id)
    {
        Category category = findCategoryByIdAndCheckIfTodoBelongInUser(id, user);
        Long todoCount = todoRepository.countByCategoryIdAndDeletedFalse(category.getId());
        return categoryMapper.toResponse(category, todoCount);
    }

    @CacheEvict(value = "user_categories", key = "#user.id")
    public CategoryResponse createCategory(User user, CategoryCreateRequest categoryRequest)
    {
        Category category = categoryMapper.toEntity(categoryRequest, user);
        if (categoryRepository.existsByNameAndUser(category.getName(), user))
            throw new DuplicateCategoryException("Category with name " + category.getName() + " already exists");
        categoryRepository.save(category);
        return categoryMapper.toResponse(category, 0);
    }

    @CacheEvict(value = "user_categories", key = "#user.id")
    public CategoryResponse updateCategory(User user, UUID id, CategoryUpdateRequest  categoryRequest)
    {
        Category category = findCategoryByIdAndCheckIfTodoBelongInUser(id, user);
        if (categoryRequest.getName() != null) {
            if (categoryRepository.existsByNameAndUser(categoryRequest.getName(), user))
                throw new DuplicateCategoryException("Category with name " + categoryRequest.getName() + " already exists");
            category.setName(categoryRequest.getName());
        }
        if (categoryRequest.getColor() != null)
            category.setColor(categoryRequest.getColor());
        categoryRepository.save(category);
        Long todoCount =  todoRepository.countByCategoryIdAndDeletedFalse(category.getId());
        return categoryMapper.toResponse(category, todoCount);
    }

    @CacheEvict(value = "user_categories", key = "#user.id")
    public String deleteCategoryById(User user, UUID id){
        if (todoRepository.countByCategoryIdAndDeletedFalse(id) > 0)
            throw new RuntimeException("Cannot delete category: it has X active todo");
        categoryRepository.deleteById(id);
        return "Category deleted";
    }
}
