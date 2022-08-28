package ru.pjcouldbe.competclaims.claims;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import ru.pjcouldbe.classtech.data.ClassData;
import ru.pjcouldbe.classtech.docx.DocSimpleRefiller;
import ru.pjcouldbe.classtech.docx.DocStatRefiller;
import ru.pjcouldbe.classtech.utils.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class SingleClaimRefiller {
    private final TextUtils textUtils = new TextUtils();
    private final DocStudentRefiller docStudentRefiller = new DocStudentRefiller();
    private final DocSimpleRefiller docSimpleRefiller = new DocSimpleRefiller();
    private final DocStatRefiller docStatRefiller = new DocStatRefiller();
    
    public void refillClaimDocFor(
        XWPFDocument templateDOc,
        ClassData classData,
        File sourceFile,
        int templateStudent,
        int student
    )
        throws IOException
    {
        String studFioShort = getFioShort(student, classData);
        log.info("Start processing student: " + studFioShort);
    
        byte[] outDocBytes;
        try (
            XWPFDocument doc = textUtils.copyDocument(templateDOc);
            ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ) {
            docStudentRefiller.refill(doc, classData, student);
            docSimpleRefiller.refillAllSimples(doc);
            docStatRefiller.refillAllStats(doc, classData);
        
            doc.write(bos);
            outDocBytes = bos.toByteArray();
        }
    
        String sourceFio = getFioShort(templateStudent, classData);
        try (FileOutputStream fos = new FileOutputStream(getTargetFile(sourceFile, sourceFio, studFioShort))) {
            fos.write(outDocBytes);
        }
    }
    
    private String getFioShort(int student, ClassData classData) {
        return classData.get("FIOS", student);
    }
    
    private File getTargetFile(File sourceFile, String sourceFio, String targetFio) {
        String sourceName = sourceFile.getName();
        String targetName = sourceName.replace(sourceFio, targetFio);
        
        if (targetName.equals(sourceName)) {
            targetName = targetFio + ' ' + sourceName;
        }
        
        return new File(sourceFile.getParent() + File.separator + targetName);
    }
}
