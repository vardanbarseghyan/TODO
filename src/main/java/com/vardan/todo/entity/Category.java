package com.vardan.todo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "categories")
public class Category extends BaseEntity{
    @Column(nullable = false)
    private String name;
    private String color;// Stores hex codes like #FFFFFF

    @ManyToOne(fetch = FetchType.LAZY)// Don't load User data until I call .getUser()
    @JoinColumn(name = "user_id", nullable = false)// Defines the actual Foreign Key column name in the MySQL table
    private User user;
}
