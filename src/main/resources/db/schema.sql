CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE debate_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    topic VARCHAR(255) NOT NULL,
    mode VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    peak_reached BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    stopped_at DATETIME NULL,
    CONSTRAINT fk_debate_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_debate_sessions_user_created (user_id, created_at)
);

CREATE TABLE debate_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    debate_session_id BIGINT NOT NULL,
    speaker VARCHAR(30) NOT NULL,
    round_no INT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_debate_messages_session FOREIGN KEY (debate_session_id) REFERENCES debate_sessions(id),
    INDEX idx_debate_messages_session_round (debate_session_id, round_no, id)
);

CREATE TABLE debate_summaries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    debate_session_id BIGINT NOT NULL UNIQUE,
    core_arguments TEXT NOT NULL,
    highlight TEXT NULL,
    decision_criteria TEXT NULL,
    remaining_issue TEXT NULL,
    summary_text TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_debate_summaries_session FOREIGN KEY (debate_session_id) REFERENCES debate_sessions(id)
);

CREATE TABLE posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    debate_session_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    summary_card TEXT NOT NULL,
    vote_option_a VARCHAR(80) NOT NULL,
    vote_option_b VARCHAR(80) NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_session FOREIGN KEY (debate_session_id) REFERENCES debate_sessions(id),
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_posts_public_created (is_public, created_at)
);

CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_comments_post_created (post_id, created_at)
);

CREATE TABLE post_votes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    choice VARCHAR(1) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_votes_post FOREIGN KEY (post_id) REFERENCES posts(id),
    CONSTRAINT fk_post_votes_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_post_votes_post_user UNIQUE (post_id, user_id),
    INDEX idx_post_votes_post_choice (post_id, choice)
);
