package com.vardan.todo.service;

import com.vardan.todo.dto.request.TodoCreateRequest;
import com.vardan.todo.dto.response.TodoResponse;
import com.vardan.todo.entity.Category;
import com.vardan.todo.entity.Todo;
import com.vardan.todo.entity.User;
import com.vardan.todo.enums.AuthProvider;
import com.vardan.todo.enums.Priority;
import com.vardan.todo.enums.Role;
import com.vardan.todo.enums.TodoStatus;
import com.vardan.todo.exception.CategoryDoesNotBelongToUserException;
import com.vardan.todo.exception.ResourceNotFoundException;
import com.vardan.todo.exception.UnauthorizedException;
import com.vardan.todo.mapper.TodoMapper;
import com.vardan.todo.repository.CategoryRepository;
import com.vardan.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TodoMapper todoMapper;

    @InjectMocks
    private TodoService todoService;

    // Shared test data — reused across all tests
    private User user;
    private User otherUser;
    private Todo todo;
    private TodoResponse todoResponse;
    private UUID todoId;

    @BeforeEach
        // Runs before EVERY test — creates fresh data each time
    void setUp() {
        todoId = UUID.randomUUID();

        // Create the main test user
        user = User.builder()
                .email("vardan@test.com")
                .password("encoded-password")
                .firstName("Vardan")
                .lastName("Barseghyan")
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();
        user.setId(UUID.randomUUID());  // ID is in BaseEntity, not covered by @Builder

        // Create a second user for ownership tests
        otherUser = User.builder()
                .email("other@test.com")
                .password("encoded-password")
                .firstName("Other")
                .lastName("User")
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();
        otherUser.setId(UUID.randomUUID());

        // Create a test todo (what the mapper and repository will return)
        todo = Todo.builder()
                .title("Buy groceries")
                .description("Milk, eggs, bread")
                .status(TodoStatus.PENDING)
                .priority(Priority.HIGH)
                .deleted(false)
                .user(user)
                .build();
        todo.setId(todoId);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());

        // Create the expected response DTO
        todoResponse = TodoResponse.builder()
                .id(todoId)
                .title("Buy groceries")
                .description("Milk, eggs, bread")
                .status(TodoStatus.PENDING)
                .priority(Priority.HIGH)
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("createTodo - should succeed without category")
    void createTodo_WithoutCategory_Success() {
        // ===== ARRANGE =====
        // Create the request (no categoryId — it's null by default)
        TodoCreateRequest request = TodoCreateRequest.builder()
                .title("Buy groceries")
                .description("Milk, eggs, bread")
                .priority(Priority.HIGH)
                .build();
        // Program the mocks — tell each fake what to do when called
        // Mock 1: "When mapper.toEntity is called with these exact args, return our test todo"
        when(todoMapper.toEntity(request, user, null)).thenReturn(todo);
        //                                    ^^^^ null because no category

        // Mock 2: "When repository.save is called with any Todo, return it back"
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        // Mock 3: "When mapper.toResponse is called with our todo, return the response DTO"
        when(todoMapper.toResponse(todo)).thenReturn(todoResponse);

        // ===== ACT =====
        // Call the REAL method on the REAL service (with fake dependencies)
        TodoResponse result = todoService.createTodo(request, user);

        // ===== ASSERT =====
        // Check the returned data is correct
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Buy groceries");
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(result.getStatus()).isEqualTo(TodoStatus.PENDING);

        // Verify the right methods were called
        verify(todoRepository, times(1)).save(any(Todo.class));  // save WAS called
        verify(categoryRepository, never()).findById(any());      // category lookup was NOT called
    }

    @Test
    @DisplayName("createTodo - should throw exception when category not found")
    void createTodo_CategoryNotFound_ThrowsException() {
        // ===== ARRANGE =====
        UUID fakeCategoryId = UUID.randomUUID();

        TodoCreateRequest request = TodoCreateRequest.builder()
                .title("Some todo")
                .categoryId(fakeCategoryId)  // This category doesn't exist
                .build();

        // Program the mock: category lookup returns empty
        when(categoryRepository.findById(fakeCategoryId)).thenReturn(Optional.empty());

        // We DON'T program todoMapper or todoRepository because
        // the method should CRASH before reaching those lines

        // ===== ACT & ASSERT =====
        // We expect the method to throw ResourceNotFoundException
        assertThatThrownBy(() -> todoService.createTodo(request, user))
                .isInstanceOf(ResourceNotFoundException.class);

        // Verify that save was NEVER called — the method stopped before saving
        verify(todoRepository, never()).save(any());
        // Verify that toEntity was NEVER called — mapper was never reached
        verify(todoMapper, never()).toEntity(any(), any(), any());
    }

    @Test
    @DisplayName("createTodo - should throw exception when category belongs to another user")
    public void createTodo_CategoryBelongsToDifferentUser_ThrowsException()
    {
        UUID categoryId =  UUID.randomUUID();

        Category otherUserCategory = Category.builder()
                .name("Other's Work")
                .color("#FF0000")
                .user(otherUser)  // ← belongs to the wrong user!
                .build();
        otherUserCategory.setId(categoryId);

        TodoCreateRequest request = TodoCreateRequest.builder()
                .title("Sneaky todo")
                .categoryId(categoryId)
                .build();
        // Program the mock: category IS found, but it belongs to otherUser
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(otherUserCategory));

        // ===== ACT & ASSERT =====
        assertThatThrownBy(() -> todoService.createTodo(request, user))
                .isInstanceOf(CategoryDoesNotBelongToUserException.class);

        // Service should have stopped — nothing saved, no entity created
        verify(todoRepository, never()).save(any());
        verify(todoMapper, never()).toEntity(any(), any(), any());
    }
    @Test
    @DisplayName("createTodo - should succeed with valid category")
    void createTodo_WithValidCategory_Success() {
        // ===== ARRANGE =====
        UUID categoryId = UUID.randomUUID();

        // Category belongs to OUR user — this is valid
        Category category = Category.builder()
                .name("Work")
                .color("#FF5733")
                .user(user)  // ← same user who is creating the todo
                .build();
        category.setId(categoryId);

        TodoCreateRequest request = TodoCreateRequest.builder()
                .title("Finish report")
                .priority(Priority.HIGH)
                .categoryId(categoryId)
                .build();

        // Program ALL the mocks — the full happy path
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(todoMapper.toEntity(request, user, category)).thenReturn(todo);
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);
        when(todoMapper.toResponse(todo)).thenReturn(todoResponse);

        // ===== ACT =====
        TodoResponse result = todoService.createTodo(request, user);

        // ===== ASSERT =====
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Buy groceries");

        // Verify the FULL chain was called
        verify(categoryRepository, times(1)).findById(categoryId);  // category was looked up
        verify(todoMapper, times(1)).toEntity(request, user, category);  // mapper was called with the category
        verify(todoRepository, times(1)).save(any(Todo.class));  // todo was saved
        verify(todoMapper, times(1)).toResponse(todo);  // response was created
    }
    /////////////////////////////////////////getAllTodosForTheUser/////////////////
// =====================================================
// GET ALL TODOS TESTS
// =====================================================

//    @Test
//    @DisplayName("getAllTodos - should return paginated todos for the user")
//    void getAllTodos_ReturnsTodos_Success() {
//        // ===== ARRANGE =====
//        // Create a Pageable object — same as what Spring creates from ?page=0&size=10
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // Create a second todo so we test with multiple items
//        Todo secondTodo = Todo.builder()
//                .title("Finish homework")
//                .description("Math and Science")
//                .status(TodoStatus.PENDING)
//                .priority(Priority.MEDIUM)
//                .deleted(false)
//                .user(user)
//                .build();
//        secondTodo.setId(UUID.randomUUID());
//        secondTodo.setCreatedAt(LocalDateTime.now());
//        secondTodo.setUpdatedAt(LocalDateTime.now());
//
//        TodoResponse secondResponse = TodoResponse.builder()
//                .id(secondTodo.getId())
//                .title("Finish homework")
//                .status(TodoStatus.PENDING)
//                .priority(Priority.MEDIUM)
//                .build();
//
//        // PageImpl is the concrete implementation of Page interface.
//        // It wraps a List and adds pagination metadata.
//        // Parameters: content list, pageable, total elements
//        Page<Todo> todoPage = new PageImpl<>(List.of(todo, secondTodo), pageable, 2);
//
//        // Program the mocks
//        when(todoRepository.findAllByUserAndDeletedFalse(user, pageable)).thenReturn(todoPage);
//        when(todoMapper.toResponse(todo)).thenReturn(todoResponse);
//        when(todoMapper.toResponse(secondTodo)).thenReturn(secondResponse);
//
//        // ===== ACT =====
//        Page<TodoResponse> result = todoService.getAllTodosForTheUser(user, pageable);
//
//        // ===== ASSERT =====
//        // Check the content
//        assertThat(result.getContent()).isNotNull();
//        assertThat(result.getContent().size()).isEqualTo(2);
//        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Buy groceries");
//        assertThat(result.getContent().get(1).getTitle()).isEqualTo("Finish homework");
//
//        // Check the pagination metadata
//        assertThat(result.getTotalElements()).isEqualTo(2);
//        assertThat(result.getTotalPages()).isEqualTo(1);   // 2 items with size 10 = 1 page
//        assertThat(result.getNumber()).isEqualTo(0);        // we're on page 0
//
//        // Verify repository was called with correct params
//        verify(todoRepository, times(1)).findAllByUserAndDeletedFalse(user, pageable);
//        // Verify mapper was called once for EACH todo
//        verify(todoMapper, times(2)).toResponse(any(Todo.class));
//    }

//    @Test
//    @DisplayName("getAllTodos - should return empty page when user has no todos")
//    void getAllTodos_NoTodos_ReturnsEmptyPage() {
//        // ===== ARRANGE =====
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // Empty list — this user has no todos
//        Page<Todo> emptyPage = new PageImpl<>(List.of(), pageable, 0);
//
//        when(todoRepository.findAllByUserAndDeletedFalse(user, pageable)).thenReturn(emptyPage);
//
//        // We DON'T program the mapper because it should never be called
//        // (there are no todos to convert)
//
//        // ===== ACT =====
//        Page<TodoResponse> result = todoService.getAllTodosForTheUser(user, pageable);
//
//        // ===== ASSERT =====
//        assertThat(result.getContent()).isNotNull();
//        assertThat(result.getContent().size()).isEqualTo(0);  // empty list
//        assertThat(result.getTotalElements()).isEqualTo(0);
//        assertThat(result.getTotalPages()).isEqualTo(0);       // 0 items = 0 pages
//
//        // Verify repository was still called (we always ask the database)
//        verify(todoRepository, times(1)).findAllByUserAndDeletedFalse(user, pageable);
//        // Verify mapper was NEVER called (no todos to convert)
//        verify(todoMapper, never()).toResponse(any());
//    }
    /// /////////////////////getTodoById/////////////////////////////
    @Test
    @DisplayName("getTodoById -> todo not found by Id")
    public void getTodoByIdNotFound_ReturnsNotFound() {
        when(todoRepository.findById(todoId)).thenReturn(Optional.empty());
//        TodoResponse todoResponse = todoService.getTodoById(todoId, user);//ete throw a anelu methodi kanchy pti miangamic assertThatThrownBy-i mej grenq.
        assertThatThrownBy(() -> todoService.getTodoById(todoId, user))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(todoRepository, times(1)).findById(todoId);
        verify(todoMapper, never()).toResponse(any());
    }
    @Test
    @DisplayName("getTodoById -> todo was soft deleted")
    public void getTodoByIdSoftDeletedTodo()
    {
        todo.setDeleted(true);
        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        assertThatThrownBy(() -> todoService.getTodoById(todoId, user))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(todoRepository, times(1)).findById(todoId);
        verify(todoMapper, never()).toResponse(any());

        assertThat(todo.isDeleted()).isTrue();
    }
    @Test
    @DisplayName("getTodoById -> todo belongs to different user")
    public void getTodoByIdTodosDifferentUser()
    {
        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        assertThatThrownBy(() -> todoService.getTodoById(todoId, otherUser))
                .isInstanceOf(UnauthorizedException.class);

        verify(todoRepository, times(1)).findById(todoId);
        verify(todoMapper, never()).toResponse(any());
    }
    @Test
    @DisplayName("getTodoById -> todo found and belongs to user — success")
    public void getTodoByIdSuccess()
    {
        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(todoMapper.toResponse(todo)).thenReturn(todoResponse);

        TodoResponse result  = todoService.getTodoById(todoId, user);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(todoId);
        assertThat(result.getTitle()).isEqualTo("Buy groceries");
        assertThat(result.getDescription()).isEqualTo("Milk, eggs, bread");
        assertThat(result.getStatus()).isEqualTo(TodoStatus.PENDING);
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);

        verify(todoRepository, times(1)).findById(todoId);
        verify(todoMapper, times(1)).toResponse(todo);
    }
}
