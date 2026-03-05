package com.vardan.todo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vardan.todo.enums.AuthProvider;
import com.vardan.todo.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {
    //@Column is about database-level rules.
    @Column(unique = true, nullable = false)//database constraint not validation!!!
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    /*
    Note how we use @Enumerated(EnumType.STRING).
    By default, Hibernate saves enums as numbers (0, 1, 2).
    If you add a new enum later, the numbers shift and your database breaks.
    Always use STRING so it saves the actual word (like "ADMIN") in the database.
    */
    private Role role;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private boolean enabled = true;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todos = new ArrayList<>();

    //implmenting UserDetails abstract methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security expects roles to start with "ROLE_"
        // Example: "ROLE_USER" or "ROLE_ADMIN"
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.password; // Return the hashed password from DB
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;//hetagayum kpoxvi
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;//hetagayum kpoxvi
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;//hetagayum kpoxvi
    }
    @Override
    public boolean isEnabled() {
        return this.enabled; // Uses your private boolean enabled field
    }
}
//mappedBy->// Tells JPA the "Category" class owns the relationship via its 'user' field
//cascade->// If I save/delete a User, automatically save/delete their Categories
//orphanRemoval->// If I remove a Category from this list, delete it from the database too
