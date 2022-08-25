package ru.pjcouldbe.docx.stat;

import com.ibm.icu.text.Transliterator;
import ru.pjcouldbe.data.ClassData;
import ru.pjcouldbe.data.ConstTextFunctions;
import ru.pjcouldbe.utils.IntPair;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

enum DocStats implements DocStat {
    GENDER() {
        @Override
        public Map<String, String> computeStat(ClassData classData) {
            return Map.of(
                "ALL", Integer.toString(classData.totalStudents()),
                "F", Long.toString(classData.getAll(GEN).stream().filter("ж"::equals).count()),
                "M", Long.toString(classData.getAll(GEN).stream().filter("м"::equals).count())
            );
        }
    },
    BIRTHDATE() {
        @Override
        public Map<String, String> computeStat(ClassData classData) {
            List<String> genders = classData.getAll(GEN);
            List<String> birthdates = classData.getAll("BIRTHDATE");
    
            SortedMap<Integer, IntPair> studentsByYears = new TreeMap<>();
            for (int i = 0; i < birthdates.size(); i++) {
                if (birthdates.get(i) == null || birthdates.get(i).isEmpty()) {
                    continue;
                }
                
                int year = LocalDate.parse(birthdates.get(i), DateTimeFormatter.ofPattern("dd.MM.yyyy")).getYear();
                boolean isMale = genders.get(i).equals("м");
    
                IntPair p = studentsByYears.computeIfAbsent(year, k -> new IntPair());
                if (isMale) {
                    p.incFirst();
                } else {
                    p.incSecond();
                }
            }
            
            Map<String, String> res = new HashMap<>();
            int i = 1;
            final int START_YEAR = Integer.parseInt(new ConstTextFunctions().get("CDS_YEAR_S").get());
            for (Map.Entry<Integer, IntPair> entry : studentsByYears.entrySet()) {
                res.put("Y" + i, Integer.toString(entry.getKey()));
                res.put("Y" + i + "A", Integer.toString(entry.getValue().getFirst() + entry.getValue().getSecond()));
                res.put("Y" + i + "M", Integer.toString(entry.getValue().getFirst()));
                res.put("Y" + i + "F", Integer.toString(entry.getValue().getSecond()));
                res.put("Y" + i + "Y", Integer.toString(START_YEAR - entry.getKey()));
                i++;
            }
            
            return res;
        }
    },
    NATION() {
        @Override
        @SuppressWarnings("java:S5854")
        public Map<String, String> computeStat(ClassData classData) {
            final String SMESH = "смеш";
            final Transliterator toLatinTrans = Transliterator.getInstance("Russian-Latin/BGN");
            
            Map<String, List<String>> natToStudents = new HashMap<>();
            List<String> nats = classData.getAll("NATIONAL");
            List<String> fios = classData.getAll("FIO");
            for (int i = 0; i < nats.size(); i++) {
                String nat = nats.get(i);
                if (nat != null && ! nat.isEmpty()) {
                    nat = nat.replaceAll("((ий)|(ая)|(ин)|(ка))$", "");
                    
                    natToStudents.computeIfAbsent(nat, k -> new ArrayList<>()).add(fios.get(i));
                }
            }
            
            UnaryOperator<String> keyTransform = key -> {
                if (key.startsWith(SMESH + '.')) {
                    return Arrays.stream(key.substring(SMESH.length() + 1).split("[\\s.+()-]+"))
                        .filter(Predicate.not(String::isEmpty))
                        .map(toLatinTrans::transliterate)
                        .map(String::toUpperCase)
                        .collect(Collectors.joining());
                } else {
                    return toLatinTrans.transliterate(key.substring(0, 3)).toUpperCase();
                }
            };
    
            Map<String, String> res = new HashMap<>();
            res.put("ALL", Long.toString(classData.totalStudents()));
            natToStudents.forEach((n, v) -> {
                String finKey = keyTransform.apply(n);
                int total = v.size();
                
                res.put(finKey, Long.toString(total));
                if (total <= 3) {
                    res.put(finKey + "STR", '(' + String.join(", ", v) + ')');
                }
            });
    
            return res;
        }
    },
    EDU() {
        @Override
        public Map<String, String> computeStat(ClassData classData) {
            Set<String> allKeys = Set.of(
                "MID",
                "STPROF",
                "MIDSPEC",
                "MIDTECH",
                "HIGHNOT",
                "HIGH"
            );
            Map<String, Long> eduMotherMap = classData.getAll("EDUMOTHER").stream()
                .filter(Predicate.not(String::isEmpty))
                .collect(groupingBy(
                    edu -> "M" + toEduKey(edu),
                    counting()
                ));
            Map<String, Long> eduFatherMap = classData.getAll("EDUFATHER").stream()
                .filter(Predicate.not(String::isEmpty))
                .collect(groupingBy(
                    edu -> "F" + toEduKey(edu),
                    counting()
                ));
            
            List<String> fiomothers = classData.getAll("FIOMOTHER");
            List<String> fiofathers = classData.getAll("FIOFATHER");
            long noMoms = fiomothers.stream()
                .filter(this::isAbsent)
                .count();
            long noPaps = fiofathers.stream()
                .filter(this::isAbsent)
                .count();
            
            long keeping = 0;
            long orphan = 0;
            for (int i = 0; i < fiofathers.size(); i++) {
                if (isAbsent(fiomothers.get(i)) && isAbsent(fiofathers.get(i))) {
                    keeping++;
                }
            }
    
            Map<String, String> res = new HashMap<>();
            allKeys.stream()
                .map(s -> "M" + s)
                .forEach(key -> {
                    long total = eduMotherMap.getOrDefault(key, 0L);
                    res.put(key, Long.toString(total));
                    res.put(key + "STR", amountToMotherSubscription(total));
                });
            allKeys.stream()
                .map(s -> "F" + s)
                .forEach(key -> {
                    long total = eduFatherMap.getOrDefault(key, 0L);
                    res.put(key, Long.toString(total));
                    res.put(key + "STR", amountToFatherSubscription(total));
                });
            res.put("NOMOM", Long.toString(noMoms));
            res.put("NOFAT", Long.toString(noPaps));
            res.put("KEEP", Long.toString(keeping));
            res.put("ORPHAN", Long.toString(orphan));
            
            return res;
        }
        
        private String toEduKey(String edu) {
            edu = edu.toLowerCase();
            if (edu.contains("средн")) {
                if (edu.contains("спец")) {
                    return "MIDSPEC";
                } else if (edu.contains("техн")) {
                    return "MIDTECH";
                } else {
                    return "MID";
                }
            } else if (edu.contains("высшее")) {
                if (edu.contains("неокон")) {
                    return "HIGHNOT";
                } else {
                    return "HIGH";
                }
            } else {
                return "STPROF";
            }
        }
        
        private boolean isAbsent(String fio) {
            return fio.contains("(баб.)") || fio.contains("умер") || fio.trim().equals("-");
        }
        
        private String amountToMotherSubscription(long total) {
            return amountToSubscription(total, "мам");
        }
    
        private String amountToFatherSubscription(long total) {
            return amountToSubscription(total, "пап");
        }
    
        private String amountToSubscription(long total, String prefix) {
            if (total / 10 != 1) {
                long mod = total % 10;
                if (mod == 1) {
                    return prefix + 'а';
                } else if (mod != 0 && mod < 5) {
                    return prefix + 'ы';
                }
            }
        
            return prefix;
        }
    },
    FAMILY() {
        @Override
        public Map<String, String> computeStat(ClassData classData) {
            return Map.of(
                "ALL", Integer.toString(classData.totalStudents()),
                "FULL", Long.toString(classData.getAll("ISFULL").stream().filter("полная"::equals).count()),
                "NOTFULL", Long.toString(classData.getAll("ISFULL").stream().filter("неполная"::equals).count()),
                "MULTI", Long.toString(multichilds(classData))
            );
        }
    
        private long multichilds(ClassData classData) {
            return classData.getAll("MULTICHILDCOUNT").stream()
                .filter(Predicate.not(String::isEmpty))
                .mapToInt(Integer::parseInt)
                .filter(n -> n >= 3)
                .count();
        }
    },
    VNEK() {
        @Override
        public Map<String, String> computeStat(ClassData classData) {
            Set<String> allKeys = Set.of(
                "SPORT",
                "DANCE",
                "DRAW",
                "MUSIC",
                "DECOR",
                "EDU"
            );
            Map<String, Long> countByTypes = classData.getAll("VNEKTYPE").stream()
                .map(String::trim)
                .filter(Predicate.not(String::isEmpty))
                .map(str -> str.split("\\n+"))
                .flatMap(arr -> Arrays.stream(arr).filter(Predicate.not(String::isEmpty)).distinct())
                .collect(groupingBy(
                    String::toUpperCase,
                    counting()
                ));
            
            return allKeys.stream().collect(Collectors.toMap(
                Function.identity(),
                type -> Long.toString(countByTypes.getOrDefault(type, 0L))
            ));
        }
    },
    SUMMEROT() {
        @Override
        public Map<String, String> computeStat(ClassData classData) {
            Map<String, String> res = new HashMap<>();
            res.putAll(statForMonth(classData, "JUNEOT"));
            res.putAll(statForMonth(classData, "JULYOT"));
            res.putAll(statForMonth(classData, "AUGUSTOT"));
            
            return res;
        }
        
        private Map<String, String> statForMonth(ClassData classData, String monthKey) {
            final String MONTH = monthKey.substring(0, monthKey.length() - 2);
            Set<String> allKeys = Set.of(
                MONTH + "SL",
                MONTH + "LAG",
                MONTH + "DER",
                MONTH + "DACHA",
                MONTH + "CITY",
                MONTH + "SANT",
                MONTH + "HOME",
                MONTH + "OTHER"
            );
            Map<String, Long> countByTypes = classData.getAll(monthKey + "TYPE").stream()
                .map(String::trim)
                .filter(Predicate.not(String::isEmpty))
                .collect(groupingBy(
                    k -> MONTH + k.toUpperCase(),
                    counting()
                ));
            
            return allKeys.stream().collect(Collectors.toMap(
                Function.identity(),
                type -> Long.toString(countByTypes.getOrDefault(type, 0L))
            ));
        }
    },
    GTOZDOR() {
        @Override
        public Map<String, String> computeStat(ClassData classData) {
            List<String> zdorGrs = classData.getAll("ZDOR_GR");
            List<String> zdorRess = classData.getAll("ZDOR_RES");
            
            final Set<String> allKeys = Set.of(
                "SPEC",
                "MAIN",
                "PRENOT",
                "PRE"
            );
            Map<String, Long> countByType = IntStream.range(0, zdorGrs.size())
                .filter(i -> !zdorGrs.get(i).isEmpty())
                .mapToObj(i -> zdorToKey(zdorGrs.get(i), zdorRess.get(i)))
                .collect(groupingBy(
                    Function.identity(),
                    counting()
                ));
    
            Map<String, String> res = new HashMap<>();
            res.put("ALL", Long.toString(classData.totalStudents()));
            allKeys.forEach(
                key -> res.put(key, Long.toString(countByType.getOrDefault(key, 0L)))
            );
    
            return res;
        }
        
        private String zdorToKey(String zdorGr, String zdorRes) {
            zdorGr = zdorGr.toLowerCase().trim();
            zdorRes = zdorRes.toLowerCase().trim();
            if (zdorGr.contains("спецгруппа")) {
                return "SPEC";
            } else if (zdorGr.contains("основн")) {
                return "MAIN";
            } else {
                if (zdorRes.contains("не допущен")) {
                    return "PRENOT";
                } else {
                    return "PRE";
                }
            }
        }
    }
    ;
    
    private static final String GEN = "GENDER";
}
