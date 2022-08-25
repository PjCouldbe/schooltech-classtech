package ru.pjcouldbe.docx;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import ru.pjcouldbe.data.ConstTextFunctions;
import ru.pjcouldbe.utils.BookmarkUtils;
import ru.pjcouldbe.utils.TextUtils;

import java.util.List;

@Slf4j
public class DocSimpleRefiller {
    public static final String B_PREFIX = "CDS_";
    
    
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final ConstTextFunctions bmToTextFunctions = new ConstTextFunctions();
    private final BookmarkUtils bookmarkUtils = new BookmarkUtils();
    private final TextUtils textUtils = new TextUtils();
    
    public void refillAllSimples(XWPFDocument doc) {
        for (XWPFParagraph p : textUtils.getAllParagraphs(doc)) {
            CTP ctp = p.getCTP();
            List<CTBookmark> bookmarkStartList = ctp.getBookmarkStartList();
            
            for (CTBookmark b : bookmarkStartList) {
                String bookMarkName = bookmarkUtils.trimBookmarkName(b.getName());
                
                if (bookMarkName.startsWith(B_PREFIX)) {
                    String targetText = bmToTextFunctions.getOrDefault(bookMarkName, () -> "").get();
    
                    Range<Integer> runsRange = textUtils.getRunsRangeWithinBookmark(b);
                    textUtils.replaceRunsRangeWithText(runsRange, p, targetText);
                }
            }
        }
    }
    
}
