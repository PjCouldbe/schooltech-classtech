package ru.pjcouldbe.compettech;

import ru.pjcouldbe.classtech.data.ClassData;
import ru.pjcouldbe.classtech.xls.XLSReader;
import ru.pjcouldbe.compettech.docx.CompetDocsFiller;
import ru.pjcouldbe.compettech.xls.CompetXLSReader;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }
    
    public void run(String[] args) throws Exception {
        Path pth = getDirPath(args);
        XLSReader xlsReader = new CompetXLSReader();
        CompetDocsFiller docsFiller = new CompetDocsFiller();
        
        ClassData classData = xlsReader.readClassData(pth);
        docsFiller.fillAllDocs(pth, classData);
    }
    
    private Path getDirPath(String[] args) {
        if (args.length <= 0) {
            return Paths.get("").toAbsolutePath();
        } else {
            return Paths.get(args[0]);
        }
    }
}