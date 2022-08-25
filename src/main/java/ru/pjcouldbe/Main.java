package ru.pjcouldbe;

import ru.pjcouldbe.data.ClassData;
import ru.pjcouldbe.docx.DocsFiller;
import ru.pjcouldbe.xls.XLSReader;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }
    
    public void run(String[] args) throws Exception {
        Path pth = getDirPath(args);
        XLSReader xlsReader = new XLSReader();
        DocsFiller docsFiller = new DocsFiller();

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