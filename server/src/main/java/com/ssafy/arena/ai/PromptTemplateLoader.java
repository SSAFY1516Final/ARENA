package com.ssafy.arena.ai;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PromptTemplateLoader {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_]+)\\s*}}");

    public String load(String fileName) {
        String path = "prompts/" + fileName;
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalArgumentException("Prompt file not found: " + fileName);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Prompt file could not be read: " + fileName, ex);
        }
    }

    public String render(String template, Map<String, ?> variables) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder rendered = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            if (!variables.containsKey(key)) {
                throw new IllegalArgumentException("Missing prompt placeholder value: " + key);
            }
            Object value = variables.get(key);
            matcher.appendReplacement(rendered, Matcher.quoteReplacement(value == null ? "null" : String.valueOf(value)));
        }
        matcher.appendTail(rendered);
        return rendered.toString();
    }

    public String loadAndRender(String fileName, Map<String, ?> variables) {
        return render(load(fileName), variables);
    }
}
