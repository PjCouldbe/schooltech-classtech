package ru.pjcouldbe.classtech.docx.filter;

import lombok.RequiredArgsConstructor;

import java.util.function.BiPredicate;

@RequiredArgsConstructor
public enum FilterOperation {
    EQ(
        String::equals
    ),
    NEQ(
        (sv, av) -> ! sv.equals(av)
    ),
    GT(
        (sv, av) -> testNumberValues(sv, av, (si, ai) -> ai > si)
    ),
    GE(
        (sv, av) -> testNumberValues(sv, av, (si, ai) -> ai >= si)
    ),
    LT(
        (sv, av) -> testNumberValues(sv, av, (si, ai) -> ai < si)
    ),
    LE(
        (sv, av) -> testNumberValues(sv, av, (si, ai) -> ai <= si)
    );
    
    private final BiPredicate<String, String> testPredicate;
    
    public boolean test(String sampleValue, String actualValue) {
        return testPredicate.test(
            sampleValue != null ? sampleValue.trim() : "",
            actualValue != null ? actualValue.trim() : ""
        );
    }
    
    private static boolean testNumberValues(String sampleValue, String actualValue, BiPredicate<Integer, Integer> intCondition) {
        return intCondition.test(Integer.parseInt(sampleValue), Integer.parseInt(actualValue));
    }
}
