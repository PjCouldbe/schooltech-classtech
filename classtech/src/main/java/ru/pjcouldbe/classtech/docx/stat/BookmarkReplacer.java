package ru.pjcouldbe.classtech.docx.stat;

import lombok.Value;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import ru.pjcouldbe.classtech.utils.TextUtils;

import java.util.function.Consumer;

@Value
public class BookmarkReplacer {
    private static final TextUtils TEXT_UTILS = new TextUtils();
    
    String name;
    Consumer<String> textSubstitutor;
    
    public BookmarkReplacer(CTBookmark bookmark, XWPFParagraph paragraph) {
        this.name = bookmark.getName();
        this.textSubstitutor = txt -> TEXT_UTILS.replaceRunsRangeWithText(
            TEXT_UTILS.getRunsRangeWithinBookmark(bookmark),
            paragraph,
            txt
        );
    }
}
