package ru.pjcouldbe.data;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.function.Supplier;

public class ConstTextFunctions extends HashMap<String, Supplier<String>> {
    public ConstTextFunctions() {
        put(
            "CDS_CLASS",
            () -> {
                int yearCount = Period.between(LocalDate.of(2022, 7, 1), LocalDate.now()).getYears();
                return Integer.toString(yearCount % 4 + 1);
            }
        );
        put("CDS_LITER", () -> "А");
        put("CDS_TEACHER", () -> "Филиппова Елена Юрьевна");
        put("CDS_TEACHER_S", () -> "Филиппова Е. Ю.");
        put(
            "CDS_YEAR",
            () -> {
                int yearCount = Period.between(LocalDate.of(2022, 7, 1), LocalDate.now()).getYears();
                int startYear = yearCount + 2022;
                return startYear + "-" + (startYear + 1);
            }
        );
        put(
            "CDS_YEAR_S",
            () -> {
                int yearCount = Period.between(LocalDate.of(2022, 7, 1), LocalDate.now()).getYears();
                int startYear = yearCount + 2022;
                return Integer.toString(startYear);
            }
        );
        put(
            "CDS_YEAR_E",
            () -> {
                int yearCount = Period.between(LocalDate.of(2022, 7, 1), LocalDate.now()).getYears();
                int endYear = yearCount + 2022 + 1;
                return Integer.toString(endYear);
            }
        );
        put(
            "CDS_EDU_FULL",
            () -> "МАОУ \"Гимназия №37\" - " + get("CDS_CLASS").get() + get("CDS_LITER").get() + " класс"
        );
    }
}
