package ru.pjcouldbe.competclaims.args;

import ru.pjcouldbe.classtech.data.ClassData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StudentListArgsParser {
    public StudentList parseFromArgs(String[] args, ClassData classData, int templateStudent) throws IOException {
        if (args.length == 0) {
            return new StudentList(
                IntStream.range(1, classData.totalStudents() + 1).toArray()
            );
        }
    
        int[] students;
        Optional<File> studentListFile = Optional.of(args[0]).map(File::new)
            .filter(File::isFile)
            .filter(File::canRead);
        if (studentListFile.isPresent()) {
            try (Stream<String> stream = Files.lines(studentListFile.get().toPath())) {
                String[] studentArgs = stream.toArray(String[]::new);
                students = parseStudentListByArray(studentArgs, classData, templateStudent);
            }
        } else {
            students = parseStudentListByArray(args, classData, templateStudent);
        }
        
        return new StudentList(students);
    }
    
    private int[] parseStudentListByArray(String[] args, ClassData classData, int templateStudent) {
        try {
            if (args[0].matches("\\d+")) {
                return Arrays.stream(args)
                        .mapToInt(Integer::parseInt)
                        .map(i -> i - 1)
                        .toArray();
            } else {
                List<String> allFio = classData.getAll("FIO");
                return Arrays.stream(args)
                    .mapToInt(prefix -> {
                        for (int i = 0; i < allFio.size(); i++) {
                            if (i != templateStudent && allFio.get(i).startsWith(prefix)) {
                                return i;
                            }
                        }
                        
                        return -1;
                    })
                    .filter(i -> i > 0)
                    .toArray();
            }
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    
}
