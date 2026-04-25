package com.smartcampus.exception;

public class LinkedResourceNotFoundException extends RuntimeException {

    private final String fieldName;
    private final String fieldValue;

    public LinkedResourceNotFoundException(String fieldName, String fieldValue, String message) {
        super(message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() { return fieldName; }
    public String getFieldValue() { return fieldValue; }
}
