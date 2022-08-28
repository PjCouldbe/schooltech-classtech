package ru.pjcouldbe.competclaims.utils;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import ru.pjcouldbe.classtech.data.ClassData;
import ru.pjcouldbe.classtech.utils.TextUtils;

import java.util.List;

public class CompetClaimsUtils {
    private static final String DETECTOR = "FIO";
    private final TextUtils textUtils = new TextUtils();
    
    public int getTemplateStudentNumber(XWPFDocument templateDoc, ClassData classData) {
        return textUtils.getAllParagraphs(templateDoc)
            .stream()
            .filter(
                p -> p.getCTP().getBookmarkStartList()
                    .stream()
                    .map(CTBookmark::getName)
                    .anyMatch(bname -> bname.contains(DETECTOR))
            )
            .findFirst()
            .map(XWPFParagraph::getText)
            .map(ptext -> {
                List<String> allFios = classData.getAll("FIOS");
                for (int i = 0; i < allFios.size(); i++) {
                    if (ptext.contains(allFios.get(i))) {
                        return i;
                    }
                }
                
                return -1;
            })
            .orElse(-1);
    }
}
