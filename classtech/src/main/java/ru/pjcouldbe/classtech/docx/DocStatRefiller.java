package ru.pjcouldbe.classtech.docx;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import ru.pjcouldbe.classtech.data.ClassData;
import ru.pjcouldbe.classtech.docx.stat.BookmarkReplacer;
import ru.pjcouldbe.classtech.docx.stat.DocStatParser;
import ru.pjcouldbe.classtech.utils.TextUtils;
import ru.pjcouldbe.classtech.docx.stat.DocStatStructure;

import java.util.List;

public class DocStatRefiller {
    private final DocStatParser docStatParser = new DocStatParser();
    private final TextUtils textUtils = new TextUtils();
    
    public void refillAllStats(XWPFDocument doc, ClassData classData) {
        List<BookmarkReplacer> allBookmarks = textUtils.getAllParagraphs(doc).stream()
            .flatMap(
                p -> p.getCTP().getBookmarkStartList().stream().map(b -> new BookmarkReplacer(b, p))
            )
            .toList();
        DocStatStructure statStructure = docStatParser.buildDocStatStructure(allBookmarks);
        
        statStructure.refillAllStats(classData);
    }
}
