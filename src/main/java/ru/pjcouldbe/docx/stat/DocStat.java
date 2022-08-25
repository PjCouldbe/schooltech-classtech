package ru.pjcouldbe.docx.stat;

import ru.pjcouldbe.data.ClassData;

import java.util.Map;

@FunctionalInterface
public interface DocStat {
    Map<String, String> computeStat(ClassData classData);
}
