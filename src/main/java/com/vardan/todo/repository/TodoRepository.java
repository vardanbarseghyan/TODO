package com.vardan.todo.repository;

import com.vardan.todo.entity.Todo;
import com.vardan.todo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID> {
    //Find all todos for a specific User where the deleted flag is false.
    List<Todo> findAllByUserAndDeletedFalse(User user);//When returning a List, we usually don't use Optional
    Page<Todo> findAllByUserAndDeletedFalse(User user, Pageable pageable);//When returning a List, we usually don't use Optional

    //Find all todos that belong to a specific Category ID.
    List<Todo> findAllByCategoryId(UUID id);

    Long countByCategoryIdAndDeletedFalse(UUID categoryId);//how many active todos belong to this category
    //this  method only used in Category part.

}
