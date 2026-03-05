package com.vardan.todo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass//This tells JPA (Java Persistence API) that this class is not a table in the database.
@EntityListeners(AuditingEntityListener.class)//It tells Spring to watch this entity and automatically fill in the dates (createdAt/updatedAt) so you don't have to set them manually in your code.
@Getter
@Setter
public abstract class BaseEntity {
    @JdbcTypeCode(Types.BINARY)//Store this Java UUID as 16 raw bytes in the database
    @Column(columnDefinition = "BINARY(16)")//this tells in Hibernate ->The SQL column type in the database is exactly BINARY(16)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)//Generates a long, random string like 550e8400-e29b.... It is globally unique, harder to hack, and better for large systems.
    private UUID id;

    @CreatedDate//When you save a new object for the first time (e.g., repository.save(user)), Spring will automatically grab the current system time and put it here.
    private LocalDateTime createdAt;

    @LastModifiedDate//Every time you update the record later, Spring will automatically overwrite this field with the new current time
    private LocalDateTime  updatedAt;

    //JPA lifecycle hooks
    //They run automatically before saving or updating data.
    @PrePersist//runs only on INSERT
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate//runs only on UPDATE
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
