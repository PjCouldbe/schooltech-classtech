package ru.pjcouldbe.docx;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import ru.pjcouldbe.data.ClassData;
import ru.pjcouldbe.docx.stat.BookmarkReplacer;
import ru.pjcouldbe.docx.stat.DocStatParser;
import ru.pjcouldbe.docx.stat.DocStatStructure;
import ru.pjcouldbe.utils.TextUtils;

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
