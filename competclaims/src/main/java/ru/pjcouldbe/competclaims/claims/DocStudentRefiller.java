package ru.pjcouldbe.competclaims.claims;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import ru.pjcouldbe.classtech.data.ClassData;
import ru.pjcouldbe.classtech.docx.DocSimpleRefiller;
import ru.pjcouldbe.classtech.docx.stat.DocStatParser;
import ru.pjcouldbe.classtech.utils.BookmarkUtils;
import ru.pjcouldbe.classtech.utils.TextUtils;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class DocStudentRefiller {
    private static final List<String> DELIMITERS = List.of(
        "\n",
        " ",
        ", ",
        "; "
    );
    
    private final BookmarkUtils bookmarkUtils = new BookmarkUtils();
    private final TextUtils textUtils = new TextUtils();
    
    
    public void refill(XWPFDocument doc, ClassData classData, int student) {
        refillSimpleParagraphs(doc, classData, student);
        
        if (doc.getTables().isEmpty()) {
            return;
        }
        
        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    refillCellIfNeeded(classData, cell, student);
                }
            }
        }
    }
    
    private void refillSimpleParagraphs(XWPFDocument doc, ClassData classData, int student) {
        for (XWPFParagraph p : textUtils.getAllParagraphs(doc)) {
            CTP ctp = p.getCTP();
            List<CTBookmark> bookmarkStartList = ctp.getBookmarkStartList();
        
            for (CTBookmark b : bookmarkStartList) {
                if (notSkipBookmark(b.getName())) {
                    String bname = bookmarkUtils.trimBookmarkName(b.getName());
                    String[] values = textUtils.columnToCellMultiline(bname, classData, student, DELIMITERS).split("\n");
                    
                    Range<Integer> runsRange = textUtils.getRunsRangeWithinBookmark(b);
                    textUtils.replaceRunsRangeWithTextMultiline(runsRange, p, values);
                }
            }
        }
    }
    
    private boolean notSkipBookmark(String ctBookmarkName) {
        boolean skip = ctBookmarkName.isEmpty()
            || ctBookmarkName.startsWith("_")
            || ctBookmarkName.startsWith(DocSimpleRefiller.B_PREFIX)
            || ctBookmarkName.startsWith(DocStatParser.STAT_PREFOX);
        
        return ! skip;
    }
    
    private void refillCellIfNeeded(
        ClassData classData,
        XWPFTableCell cell,
        int student
    ) {
        if (cell.getParagraphs().isEmpty() && cell.getParagraphs().get(0).getCTP().getBookmarkStartList().isEmpty()) {
            return;
        }
    
        List<CTBookmark> bookmarks = cell.getParagraphs().get(0).getCTP().getBookmarkStartList();
        for (CTBookmark b : bookmarks) {
            if (notSkipBookmark(b.getName())) {
                String bname = bookmarkUtils.trimBookmarkName(b.getName());
                Range<Integer> runsRange;
                XWPFParagraph p;
    
                if ( ! cell.getParagraphs().isEmpty()) {
                    IntStream.range(1, cell.getParagraphs().size()).forEach(r -> cell.removeParagraph(1));
                    p = cell.getParagraphs().get(0);
                    runsRange = Range.between(0, Math.max(0, p.getRuns().size() - 1));
                } else {
                    p = cell.addParagraph();
                    p.createRun();
                    runsRange = Range.between(0, 0);
                }
    
                String[] values = textUtils.columnToCellMultiline(bname, classData, student, DELIMITERS).split("\n");
                textUtils.replaceRunsRangeWithTextMultiline(runsRange, p, values);
            }
        }
    }
    
}
