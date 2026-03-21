package com.vardan.todo.repository;

import com.vardan.todo.entity.Todo;
import com.vardan.todo.entity.User;
import com.vardan.todo.enums.Priority;
import com.vardan.todo.enums.TodoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID> {
    //Find all todos for a specific User where the deleted flag is false.
    List<Todo> findAllByUserAndDeletedFalse(User user);//When returning a List, we usually don't use Optional
    Page<Todo> findAllByUserAndDeletedFalse(User user, Pageable pageable);//When returning a List, we usually don't use Optional
    //es findAllByUserAndDeletedFalse el chenq ogtagorcum,poxarinuma sran searchAndFilter-@.

    //Find all todos that belong to a specific Category ID.
    List<Todo> findAllByCategoryId(UUID id);

    Long countByCategoryIdAndDeletedFalse(UUID categoryId);//how many active todos belong to this category
    //this  method only used in Category part.

    @Query("SELECT t FROM Todo t WHERE t.user = :user AND t.deleted = false " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND (:search IS NULL OR " +
            "     LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Todo> searchAndFilter(
            @Param("user") User user,
            @Param("status") TodoStatus status,
            @Param("priority") Priority priority,
            @Param("categoryId") UUID categoryId,
            @Param("search") String search,
            Pageable pageable
    );

}
