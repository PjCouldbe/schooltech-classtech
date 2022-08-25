package ru.pjcouldbe.docx.filter;

import lombok.Value;

@Value
public class Filter {
    String key;
    FilterOperation op;
    String value;
    
    public boolean isPassed(String actual) {
        return op.test(value, actual);
    }
    
    public boolean shouldBeFiltered(String actual) {
        return ! isPassed(actual);
    }
}
