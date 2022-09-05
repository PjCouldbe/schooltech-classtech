package ru.pjcouldbe.compettech.docx;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import ru.pjcouldbe.classtech.data.ClassData;
import ru.pjcouldbe.classtech.docx.DocSimpleRefiller;
import ru.pjcouldbe.classtech.docx.DocTableRefiller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class SingleCompetTableRefiller {
    private final DocTableRefiller docTableRefiller = new DocTableRefiller();
    private final DocSimpleRefiller docSimpleRefiller = new DocSimpleRefiller();
    
    public void fillDoc(File f, ClassData classData) throws XmlException, IOException {
        log.info("Start processing: " + f.getName());
        
        byte[] outDocBytes;
        try (
            InputStream in = new FileInputStream(f);
            XWPFDocument doc = new XWPFDocument(in);
            ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ) {
            docTableRefiller.refillAllTables(doc, classData);
            docSimpleRefiller.refillAllSimples(doc);
            
            doc.write(bos);
            outDocBytes = bos.toByteArray();
        }
        
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(outDocBytes);
        }
    }
}
