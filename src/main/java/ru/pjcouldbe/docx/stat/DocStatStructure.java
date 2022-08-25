package ru.pjcouldbe.docx.stat;

import ru.pjcouldbe.data.ClassData;

import java.util.HashMap;
import java.util.function.Consumer;

public class DocStatStructure extends HashMap<String, DocStatEntry> {
    public void addFor(DocStats ds, String key, Consumer<String> textReplacer) {
        computeIfAbsent(ds.name(), k -> new DocStatEntry(ds))
            .addTextRangeForStatKey(key, textReplacer);
    }
    
    public void refillAllStats(ClassData classData) {
        values().forEach(dse -> dse.refillStat(classData));
    }
}
