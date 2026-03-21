package com.vardan.todo.controller;

import com.vardan.todo.dto.request.TodoCreateRequest;
import com.vardan.todo.dto.request.TodoUpdateRequest;
import com.vardan.todo.dto.response.TodoResponse;
import com.vardan.todo.entity.User;
import com.vardan.todo.enums.Priority;
import com.vardan.todo.enums.TodoStatus;
import com.vardan.todo.service.TodoService;
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

import java.util.List;
import java.util.UUID;

@Tag(name = "Todo Management", description = "CRUD operations for todos with search, filter, and pagination")
@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;

    @PostMapping("")
    public ResponseEntity<TodoResponse> createTodo(@AuthenticationPrincipal User user, @Valid @RequestBody TodoCreateRequest request) {
        TodoResponse todoResponse = todoService.createTodo(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(todoResponse);
    }

    @GetMapping("")
    public ResponseEntity<Page<TodoResponse>> getAllTodos(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) TodoStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable)
        // @PageableDefault sets default values when the user doesn't provide ?page=&size=&sort= in the URL
        // Without it, Spring defaults to page=0, size=20, no sorting
    {
        Page<TodoResponse> todos = todoService.getAllTodosForTheUser(user, status, priority, categoryId, search, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(todos);
    }
    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getTodoById(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        TodoResponse  todoResponse = todoService.getTodoById(id, user);
        return ResponseEntity.status(HttpStatus.OK).body(todoResponse);
    }

    @PutMapping("/{id}")
    public  ResponseEntity<TodoResponse> updateTodo(@PathVariable UUID id, @Valid @RequestBody TodoUpdateRequest request, @AuthenticationPrincipal User user)
    {
        TodoResponse todo = todoService.updateTodo(id, request, user);
        return ResponseEntity.status(HttpStatus.OK).body(todo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTodo(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        todoService.softDelete(id, user);
        return ResponseEntity.noContent().build();
    }
}
