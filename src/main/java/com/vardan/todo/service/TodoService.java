package com.vardan.todo.service;

import com.vardan.todo.dto.request.TodoCreateRequest;
import com.vardan.todo.dto.request.TodoUpdateRequest;
import com.vardan.todo.dto.response.TodoResponse;
import com.vardan.todo.entity.Category;
import com.vardan.todo.entity.Todo;
import com.vardan.todo.entity.User;
import com.vardan.todo.exception.CategoryDoesNotBelongToUserException;
import com.vardan.todo.exception.EmailAlreadyExistsException;
import com.vardan.todo.exception.ResourceNotFoundException;
import com.vardan.todo.exception.UnauthorizedException;
import com.vardan.todo.mapper.TodoMapper;
import com.vardan.todo.repository.CategoryRepository;
import com.vardan.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;
    private final TodoMapper todoMapper;

    private Category checkCategoryIdNotNullAndIfCategoryBelongInUser(UUID categoryId, User user) {
        Category category = null;
        if (categoryId != null) {//stugum enq ardyoq usery tramadrela categoryId-n te che
            category = categoryRepository.findById(categoryId)//pordzum enq db-ic vercnel et id-iv category-in.
                    .orElseThrow(() -> new ResourceNotFoundException("can not find category with id " + categoryId));
            //We should also verify that this category belongs to the logged-in user (you don't want user A assigning a todo to user B's category.)
            if (!category.getUser().getId().equals(user.getId())) {
                throw new CategoryDoesNotBelongToUserException("You cannot assign a Todo to a category that doesn't belong to you");
            }
        }
        return category;
    }

    private Todo findTodoByIdAndCheckIfTodoBelongInUser(UUID id, User user)
    {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));
        if (todo.isDeleted()) {
            throw new ResourceNotFoundException("Todo", "id", id);
        }
        if(!todo.getUser().getId().equals(user.getId()))
            throw new UnauthorizedException();
        return todo;
    }
    public TodoResponse createTodo(TodoCreateRequest request, User user)
    {
        Category category = checkCategoryIdNotNullAndIfCategoryBelongInUser(request.getCategoryId(), user);
        Todo newTodo = todoMapper.toEntity(request, user, category);
        todoRepository.save(newTodo);
        return todoMapper.toResponse(newTodo);
    }

    public Page<TodoResponse> getAllTodosForTheUser(User user, Pageable pageable)
    {
        Page<Todo> todoPage = todoRepository.findAllByUserAndDeletedFalse(user, pageable);
        //page.map(todoMapper::toResponse) does the same thing as your old for-loop:

//        List<TodoResponse> todosResponse = new ArrayList<>();
//        for(Todo todo : todos)
//        {
//            TodoResponse todoResponse = todoMapper.toResponse(todo);
//            todosResponse.add(todoResponse);
//        }

        Page<TodoResponse> responsePage = todoPage.map(todoMapper::toResponse);
        return responsePage;
    }
    public TodoResponse getTodoById(UUID id, User user)
    {
//        Todo todo = todoRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));
//        if (todo.isDeleted()) {
//            //what make here?
//        }
//        if(todo.getUser().getId() != user.getId())
//            throw new UnauthorizedException();
        Todo todo = findTodoByIdAndCheckIfTodoBelongInUser(id, user);
        return todoMapper.toResponse(todo);
    }
    public TodoResponse updateTodo(UUID id, TodoUpdateRequest request, User user)
    {
//        Todo todo = todoRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));
//        if (todo.isDeleted()) {
//            //what make here?
//        }
//        if(todo.getUser().getId() != user.getId())
//            throw new UnauthorizedException();
        Todo todo = findTodoByIdAndCheckIfTodoBelongInUser(id, user);

        if (request.getTitle() != null)
            todo.setTitle(request.getTitle());
        if (request.getDescription() != null)
            todo.setDescription(request.getDescription());
        todo.setStatus(request.getStatus() != null ? request.getStatus() : todo.getStatus());
        todo.setPriority(request.getPriority() != null ? request.getPriority() : todo.getPriority());
        todo.setDueDate(request.getDueDate() != null ? request.getDueDate() : todo.getDueDate());

        Category category = checkCategoryIdNotNullAndIfCategoryBelongInUser(request.getCategoryId(), user);
        if (category != null) {
            todo.setCategory(category);
        }
        todoRepository.save(todo);
        return todoMapper.toResponse(todo);
    }

    public void softDelete(UUID id, User user)
    {
        Todo todo = findTodoByIdAndCheckIfTodoBelongInUser(id, user);
        todo.setDeleted(true);//this is mean`findAllByUserAndDeletedFalse() will no longer return it.
        todoRepository.save(todo);
    }
}
