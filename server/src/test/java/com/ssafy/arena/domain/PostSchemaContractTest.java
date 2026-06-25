package com.ssafy.arena.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PostSchemaContractTest {
    @Test
    void postsAllowMultipleSharesForOneDebateSessionAndStoreSharedRound() throws IOException {
        String schema = Files.readString(Path.of("src/main/resources/db/schema.sql"));
        String postsDefinition = schema.substring(
                schema.indexOf("CREATE TABLE posts"),
                schema.indexOf("CREATE TABLE comments")
        );

        assertThat(postsDefinition).contains("share_round_no INT NOT NULL DEFAULT 1");
        assertThat(postsDefinition).doesNotContain("debate_session_id BIGINT NOT NULL UNIQUE");
    }
}
