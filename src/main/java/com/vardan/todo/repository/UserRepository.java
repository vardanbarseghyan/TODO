package com.vardan.todo.repository;

import com.vardan.todo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    //A way to find a user by their email.
    Optional<User> findByEmail(String email);//In Spring, we use Optional for find methods in case the user isn't found
    //A way to check if an email exists
    boolean existsByEmail(String email);//emailWasExist
}
