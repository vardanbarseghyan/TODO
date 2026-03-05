package com.vardan.todo.repository;

import com.vardan.todo.entity.RefreshToken;
import com.vardan.todo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    //Find the token object by the token string itself.
    Optional<RefreshToken> findByToken(String token);


    //A way to delete all tokens for a specific User (used during logout).
    @Transactional
    void deleteByUser(User user);
}
