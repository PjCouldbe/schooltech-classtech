package ru.pjcouldbe.competclaims.claims;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import ru.pjcouldbe.classtech.data.ClassData;
import ru.pjcouldbe.classtech.xls.XLSReader;
import ru.pjcouldbe.competclaims.args.StudentList;
import ru.pjcouldbe.competclaims.args.StudentListArgsParser;
import ru.pjcouldbe.competclaims.utils.CompetClaimsUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class ClaimsRefiller {
    private final SingleClaimRefiller singleClaimRefiller = new SingleClaimRefiller();
    private final CompetClaimsUtils competClaimsUtils = new CompetClaimsUtils();
    private final StudentListArgsParser studentListParser = new StudentListArgsParser();
    
    public void copyAllClaimsOn(String[] args) throws IOException {
        final XLSReader xlsReader = new XLSReader();
        final ClassData classData = xlsReader.readClassData(Paths.get(args[0]));
    
        try (XWPFDocument templateDoc = openTemplateClaim(args[1])) {
            int templateStudent = competClaimsUtils.getTemplateStudentNumber(templateDoc, classData);
            StudentList studentList = studentListParser.parseFromArgs(
                subArrayArgsFromThird(args), classData, templateStudent);
    
            for (int studentNum : studentList.toArray()) {
                singleClaimRefiller.refillClaimDocFor(
                    templateDoc, classData, getTemplateDocFile(args[1]), templateStudent, studentNum);
            }
        }
    }
    
    private File getTemplateDocFile(String path) {
        return Paths.get(path).toFile();
    }
    
    private XWPFDocument openTemplateClaim(String path) throws IOException {
        if (path.endsWith(".doc")) {
            throw new IllegalArgumentException("Заявка должна быть в формате .docx, не .doc!");
        }
        
        return new XWPFDocument(
            new FileInputStream(Paths.get(path).toFile())
        );
    }
    
    private String[] subArrayArgsFromThird(String[] args) {
        if (args.length == 2) {
            return new String[0];
        }
        
        String[] res = new String[args.length - 2];
        System.arraycopy(args, 2, res, 0, res.length);
        return res;
    }
}
