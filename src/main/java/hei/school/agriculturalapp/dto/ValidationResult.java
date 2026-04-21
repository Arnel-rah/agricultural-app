package hei.school.agriculturalapp.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ValidationResult {
    private final List<String> errors = new ArrayList<>();
    private final Map<String, String> fieldErrors = new HashMap<>();

    public void addError(String field, String message) {
        fieldErrors.put(field, message);
        errors.add(field + ": " + message);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public String getErrorMessage() {
        return String.join("; ", errors);
    }
}
