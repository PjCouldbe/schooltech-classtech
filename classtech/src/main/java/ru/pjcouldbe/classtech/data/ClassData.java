package ru.pjcouldbe.classtech.data;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static java.util.function.Predicate.not;

@RequiredArgsConstructor
public class ClassData {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final ConstTextFunctions constTextFunctions = new ConstTextFunctions();
    private final Map<String, List<String>> manualData;
    private final Map<String, String> constData = Map.of(
        "EDU", "МАОУ \"Гимназия №37\""
    );
    private final UnaryOperator<String> constComputeData =
        key -> constTextFunctions.getOrDefault("CDS_" + key, () -> "").get();
    
    
    public List<String> getAll(final String key) {
        return manualData.getOrDefault(key, new ArrayList<>());
    }
    
    public String get(final String key, final int row) {
        return opt(key, row).orElse("");
    }
    
    public Optional<String> opt(final String key, final int row) {
        return Optional.of(key)
            .map(String::trim)
            .filter(not(String::isEmpty))
            .map(k -> manualData.getOrDefault(k, new ArrayList<>()))
            .map(dataList -> dataList.size() <= row ? "" : dataList.get(row))
            .map(str -> optTrim(str, key))
            .filter(not(String::isBlank))
            .or(() -> Optional.ofNullable(constData.get(key)))
            .filter(not(String::isEmpty))
            .or(() -> Optional.ofNullable(constComputeData.apply(key)))
            .filter(not(String::isEmpty));
    }
    
    private String optTrim(String content, String key) {
        if (key.contains("VNEK_")) {
            return content;
        } else {
            return content.trim();
        }
    }
    
    public int totalStudents() {
        return manualData.entrySet().iterator().next().getValue().size();
    }
}
