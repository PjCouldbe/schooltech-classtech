package ru.pjcouldbe.classtech.docx.filter;

public record Filter(String key, FilterOperation op, String value) {
    public boolean isPassed(String actual) {
        return op.test(value, actual);
    }
    
    public boolean shouldBeFiltered(String actual) {
        return !isPassed(actual);
    }
}
