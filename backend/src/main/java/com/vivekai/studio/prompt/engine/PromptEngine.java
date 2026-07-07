package com.vivekai.studio.prompt.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class PromptEngine {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)(?:\\|([^}]+))?\\}\\}");

    /**
     * Validates prompt template syntax for mismatched brackets
     */
    public void validateTemplate(String template) {
        if (template == null) return;
        
        int openCount = 0;
        int index = 0;
        while (index < template.length()) {
            if (template.startsWith("{{", index)) {
                openCount++;
                index += 2;
            } else if (template.startsWith("}}", index)) {
                openCount--;
                if (openCount < 0) {
                    throw new IllegalArgumentException("Mismatched brackets: Found '}}' without preceding '{{'");
                }
                index += 2;
            } else {
                index++;
            }
        }
        
        if (openCount > 0) {
            throw new IllegalArgumentException("Mismatched brackets: Found unclosed '{{' placeholder");
        }
    }

    /**
     * Resolves variables matching pattern {{var_name}} or {{var_name|default_value}}
     */
    public String resolveTemplate(String template, Map<String, String> inputs) {
        if (template == null) return null;
        
        validateTemplate(template);
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String defaultValue = matcher.group(2);
            String resolvedValue = inputs != null ? inputs.get(varName) : null;
            
            if (resolvedValue == null) {
                resolvedValue = defaultValue;
            }
            
            if (resolvedValue == null) {
                log.warn("Breached variable placeholder '{}' lacks mapping values or defaults", varName);
                throw new IllegalArgumentException("Required template variable '" + varName + "' is missing");
            }
            
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolvedValue));
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
}
