-- 1. Create Users Table
CREATE TABLE users (
                       id BINARY(16) PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       role VARCHAR(20) NOT NULL,
                       provider VARCHAR(20) NOT NULL,
                       enabled BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL
);

-- 2. Create Categories Table
CREATE TABLE categories (
                            id BINARY(16) PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            color VARCHAR(7),
                            user_id BINARY(16) NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP NOT NULL,
                            CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Create Todos Table
CREATE TABLE todos (
                       id BINARY(16) PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                       priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
                       due_date TIMESTAMP NULL,
                       deleted BOOLEAN DEFAULT FALSE,
                       user_id BINARY(16) NOT NULL,
                       category_id BINARY(16),
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,
                       CONSTRAINT fk_todo_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                       CONSTRAINT fk_todo_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- 4. Create Refresh Tokens Table
CREATE TABLE refresh_tokens (
                                id BINARY(16) PRIMARY KEY,
                                token VARCHAR(255) NOT NULL UNIQUE,
                                expires_at TIMESTAMP NOT NULL, -- Matches Lead's requirement
                                user_id BINARY(16) NOT NULL,
                                created_at TIMESTAMP NOT NULL,
                                updated_at TIMESTAMP NOT NULL,
                                CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);