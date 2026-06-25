CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NULL,
    provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(100) NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_provider UNIQUE (provider, provider_id)
);

CREATE TABLE debate_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_topic VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    side_a_label VARCHAR(120) NULL,
    side_b_label VARCHAR(120) NULL,
    debate_axis VARCHAR(255) NULL,
    side_a_frame VARCHAR(1000) NULL,
    side_b_frame VARCHAR(1000) NULL,
    selected_round_id VARCHAR(60) NULL,
    round_title VARCHAR(255) NULL,
    basic_conditions VARCHAR(1000) NULL,
    candidate_run_id BIGINT NULL,
    selected_candidate_id BIGINT NULL,
    mode VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    peak_reached BOOLEAN NOT NULL DEFAULT FALSE,
    selected_side VARCHAR(30) NULL,
    selected_round_no INT NULL,
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
    debate_session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    share_round_no INT NOT NULL DEFAULT 1,
    title VARCHAR(120) NOT NULL,
    summary_card TEXT NOT NULL,
    share_body TEXT NULL,
    vote_option_a VARCHAR(80) NOT NULL,
    vote_option_b VARCHAR(80) NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_session FOREIGN KEY (debate_session_id) REFERENCES debate_sessions(id),
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_posts_debate_session (debate_session_id),
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

CREATE TABLE ai_round_candidate_runs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_topic VARCHAR(255) NOT NULL,
    mode VARCHAR(30) NOT NULL,
    candidate_count INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    topic_frame_json LONGTEXT NULL,
    final_response_json LONGTEXT NULL,
    error_message TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME NULL,
    CONSTRAINT fk_ai_round_candidate_runs_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_ai_round_candidate_runs_user_created (user_id, created_at),
    INDEX idx_ai_round_candidate_runs_status_created (status, created_at)
);

CREATE TABLE ai_round_candidates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    run_id BIGINT NOT NULL,
    round_id VARCHAR(60) NOT NULL,
    title VARCHAR(255) NOT NULL,
    core_question VARCHAR(255) NOT NULL,
    debate_axis VARCHAR(255) NOT NULL,
    side_a_frame VARCHAR(1000) NOT NULL,
    side_b_frame VARCHAR(1000) NOT NULL,
    sort_order INT NOT NULL,
    candidate_json LONGTEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_round_candidates_run FOREIGN KEY (run_id) REFERENCES ai_round_candidate_runs(id),
    INDEX idx_ai_round_candidates_run_order (run_id, sort_order, id)
);

CREATE TABLE ai_prompt_call_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    run_id BIGINT NOT NULL,
    stage VARCHAR(40) NOT NULL,
    prompt_name VARCHAR(120) NOT NULL,
    model VARCHAR(120) NULL,
    rendered_prompt LONGTEXT NOT NULL,
    raw_response LONGTEXT NULL,
    parsed_response_json LONGTEXT NULL,
    status VARCHAR(30) NOT NULL,
    error_message TEXT NULL,
    latency_ms BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_prompt_call_logs_run FOREIGN KEY (run_id) REFERENCES ai_round_candidate_runs(id),
    INDEX idx_ai_prompt_call_logs_run_stage (run_id, stage)
);

CREATE TABLE ai_debate_turn_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    debate_session_id BIGINT NOT NULL,
    debate_message_id BIGINT NULL,
    round_no INT NOT NULL,
    speaker VARCHAR(30) NOT NULL,
    prompt_name VARCHAR(120) NOT NULL,
    model VARCHAR(120) NULL,
    rendered_prompt LONGTEXT NOT NULL,
    raw_response LONGTEXT NULL,
    parsed_response_json LONGTEXT NULL,
    status VARCHAR(30) NOT NULL,
    error_message TEXT NULL,
    latency_ms BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_debate_turn_logs_session FOREIGN KEY (debate_session_id) REFERENCES debate_sessions(id),
    CONSTRAINT fk_ai_debate_turn_logs_message FOREIGN KEY (debate_message_id) REFERENCES debate_messages(id),
    INDEX idx_ai_debate_turn_logs_session_round (debate_session_id, round_no),
    INDEX idx_ai_debate_turn_logs_status_created (status, created_at)
);
