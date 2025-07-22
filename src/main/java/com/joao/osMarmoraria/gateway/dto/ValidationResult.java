package com.joao.osMarmoraria.gateway.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para resultado de validação
 */
public class ValidationResult {

    private boolean valid;
    private List<ValidationError> errors;
    private List<ValidationWarning> warnings;
    private String summary;

    // Construtores
    public ValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public ValidationResult(boolean valid) {
        this();
        this.valid = valid;
    }

    // Getters e Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
        this.valid = errors == null || errors.isEmpty();
    }

    public List<ValidationWarning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<ValidationWarning> warnings) {
        this.warnings = warnings;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    // Métodos utilitários
    public void addError(String field, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new ValidationError(field, message));
        this.valid = false;
    }

    public void addError(String field, String message, String code) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new ValidationError(field, message, code));
        this.valid = false;
    }

    public void addWarning(String field, String message) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(new ValidationWarning(field, message));
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }

    public int getWarningCount() {
        return warnings != null ? warnings.size() : 0;
    }

    // Classes internas
    public static class ValidationError {
        private String field;
        private String message;
        private String code;

        public ValidationError() {}

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public ValidationError(String field, String message, String code) {
            this.field = field;
            this.message = message;
            this.code = code;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static class ValidationWarning {
        private String field;
        private String message;

        public ValidationWarning() {}

        public ValidationWarning(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}