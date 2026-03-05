package com.vardan.todo.repository;

import com.vardan.todo.entity.Category;
import com.vardan.todo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    //A way to get a List of categories that belong to a specific User
    List<Category> findAllByUser(User user);
}
