package ru.pjcouldbe.classtech.docx.stat;

import lombok.Value;
import ru.pjcouldbe.classtech.data.ClassData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Value
public class DocStatEntry {
    DocStat docStat;
    Map<String, List<Consumer<String>>> statSubKeyToTextReplacers = new HashMap<>();
    
    public void addTextRangeForStatKey(String key, Consumer<String> textReplacer) {
        statSubKeyToTextReplacers.computeIfAbsent(key, k -> new ArrayList<>()).add(textReplacer);
    }
    
    public void refillStat(ClassData classData) {
        Map<String, String> actualStat = docStat.computeStat(classData);
        statSubKeyToTextReplacers.forEach((subKey, textReplacers) -> {
            String actualSubStat = actualStat.getOrDefault(subKey, "");
            textReplacers.forEach(tr -> tr.accept(actualSubStat));
        });
    }
}
