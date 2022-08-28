package ru.pjcouldbe.competclaims;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import ru.pjcouldbe.classtech.data.ClassData;
import ru.pjcouldbe.classtech.xls.XLSReader;
import ru.pjcouldbe.competclaims.args.StudentList;
import ru.pjcouldbe.competclaims.args.StudentListArgsParser;
import ru.pjcouldbe.competclaims.claims.ClaimsRefiller;
import ru.pjcouldbe.competclaims.utils.CompetClaimsUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    private final ClaimsRefiller claimsRefiller = new ClaimsRefiller();
    
    
    public static void main(String[] args) throws IOException {
        new Main().run(args);
    }
    
    private void run(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                "Требуется минимум 2 аргумента: путь к папке с данными класса " +
                    "и путь к заявке, которую надо скопировать в формате .docx"
            );
        }
    
        claimsRefiller.copyAllClaimsOn(args);
    }
    
}