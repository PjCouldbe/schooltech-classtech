package ru.pjcouldbe.classtech.docx.stat;

import ru.pjcouldbe.classtech.data.ClassData;

import java.util.Map;

@FunctionalInterface
public interface DocStat {
    Map<String, String> computeStat(ClassData classData);
}
