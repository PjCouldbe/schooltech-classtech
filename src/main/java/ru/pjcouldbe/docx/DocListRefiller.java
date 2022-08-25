package ru.pjcouldbe.docx;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTMarkupRange;
import ru.pjcouldbe.data.ClassData;
import ru.pjcouldbe.docx.filter.Filter;
import ru.pjcouldbe.docx.filter.FilterParser;
import ru.pjcouldbe.utils.BookmarkUtils;
import ru.pjcouldbe.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DocListRefiller {
    private static final String NUMBER_COL = "NUMBER";
    private static final List<String> DELIMITERS = List.of(
        " - ",
        " ",
        ", ",
        "; "
    );
    
    private final FilterParser filterParser = new FilterParser();
    private final BookmarkUtils bookmarkUtils = new BookmarkUtils();
    private final TextUtils textUtils = new TextUtils();
    
    public void refillAllLists(XWPFDocument doc, ClassData classData) {
        List<List<XWPFParagraph>> docLists = findAllLists(doc);
        for (List<XWPFParagraph> docList : docLists) {
            ListHeaderInfo lstInfo = extractListHeader(docList);
            if ( ! lstInfo.contentDescriptors.isEmpty() && lstInfo.firstParagraph != null) {
                for (int p = docList.size() - 1; p > 0; p--) {
                    doc.removeBodyElement(
                        doc.getPosOfParagraph(docList.get(p))
                    );
                }
        
                refillList(classData, lstInfo, doc);
            }
        }
    }
    
    private List<List<XWPFParagraph>> findAllLists(XWPFDocument doc) {
        return new ArrayList<>(
            doc.getParagraphs().stream()
                .filter(p -> p.getNumID() != null)
                .collect(Collectors.groupingBy(XWPFParagraph::getNumID))
                .values()
        );
    }
    
    private ListHeaderInfo extractListHeader(List<XWPFParagraph> list) {
        if (list.isEmpty()) {
            return new ListHeaderInfo(null, new ArrayList<>(), new ArrayList<>());
        }
        
        List<String> contentBookmarks = new ArrayList<>();
        List<Filter> rowFilters = new ArrayList<>();
        List<CTBookmark> bookmarkList = list.get(0).getCTP().getBookmarkStartList();
    
        for (CTBookmark ctBookmark : bookmarkList) {
            String ctBookmarkName = ctBookmark.getName();
        
            if (notSkipBookmark(ctBookmarkName)) {
                if (filterParser.isFilterBookmark(ctBookmarkName)) {
                    rowFilters.add(filterParser.parse(ctBookmarkName));
                } else {
                    contentBookmarks.add(bookmarkUtils.trimBookmarkName(ctBookmarkName));
                }
            }
        }
        
        return new ListHeaderInfo(list.get(0), contentBookmarks, rowFilters);
    }
    
    private boolean notSkipBookmark(String ctBookmarkName) {
        boolean skip = ctBookmarkName.isEmpty()
            || ctBookmarkName.startsWith("_")
            || ctBookmarkName.startsWith(DocSimpleRefiller.B_PREFIX);
        
        return ! skip;
    }
    
    private void refillList(
        ClassData classData,
        ListHeaderInfo lstInfo,
        XWPFDocument doc
    ) {
        Formats lstFormats = getParaFormats(lstInfo.firstParagraph);
    
        final int newRowsCount = classData.totalStudents();
        
        int rowsAdded = 0;
        XWPFParagraph prevP = lstInfo.firstParagraph;
        
        for (int row = 0; row < newRowsCount; row++) {
            if (isSatisfiedByFilters(classData, lstInfo.rowFilters, row)) {
                XmlCursor paraCursor = prevP.getCTP().newCursor();
                paraCursor.toNextSibling();
                XWPFParagraph newP = doc.insertNewParagraph(paraCursor);
    
                if (rowsAdded == 0) {
                    restoreBookmarks(newP, lstInfo);
                }
                refillPara(classData, lstInfo.contentDescriptors, lstFormats, row, newP);
        
                prevP = newP;
                rowsAdded++;
            }
        }
    
        if (rowsAdded == 0) {
            addExtraEmptyPara(classData, lstInfo, doc, lstFormats, newRowsCount);
        } else {
            doc.removeBodyElement(
                doc.getPosOfParagraph(lstInfo.firstParagraph)
            );
        }
    }
    
    private boolean isSatisfiedByFilters(ClassData classData, List<Filter> rowFilters, int row) {
        for (Filter f : rowFilters) {
            String value = getActualFilterValue(f.getKey(), classData, row);
            
            if (f.shouldBeFiltered(value)) {
                return false;
            }
        }
        
        return true;
    }
    
    private void restoreBookmarks(XWPFParagraph newP, ListHeaderInfo lstInfo) {
        List<CTBookmark> bookmarkStarts = lstInfo.firstParagraph.getCTP().getBookmarkStartList();
        List<CTMarkupRange> bookmarkEnds = lstInfo.firstParagraph.getCTP().getBookmarkEndList();
    
        for (int i = 0; i < bookmarkStarts.size(); i++) {
            CTBookmark oldB = bookmarkStarts.get(i);
            CTBookmark bookmark = newP.getCTP().addNewBookmarkStart();
            bookmark.setName(oldB.getName());
            bookmark.setId(oldB.getId());
            newP.getCTP().addNewBookmarkEnd().setId(bookmarkEnds.get(i).getId());
        }
    }
    
    private String getActualFilterValue(String key, ClassData classData, final int row) {
        if (key.equals(NUMBER_COL)) {
            return Integer.toString(row + 1);
        }
        
        return classData.opt(key, row)
            .orElse(getDefaultFilterValue(key));
    }
    
    private String getDefaultFilterValue(String key) {
        if (key.equals("MULTICHILDCOUNT")) {
            return "1";
        } else if (key.contains("IS")) {
            return "0";
        } else {
            return "";
        }
    }
    
    private void addExtraEmptyPara(
        ClassData classData,
        ListHeaderInfo lstInfo,
        XWPFDocument doc,
        Formats formats,
        int newRowsCount
    ) {
        XmlCursor cursor = lstInfo.firstParagraph.getCTP().newCursor();
        cursor.toNextSibling();
        XWPFParagraph newP = doc.insertNewParagraph(cursor);
        
        refillPara(classData, lstInfo.contentDescriptors, formats, newRowsCount + 1, newP);
    }
    
    private Formats getParaFormats(XWPFParagraph p) {
        ParagraphAlignment alignment = p.getAlignment();
        double lineSpaceBetween = p.getSpacingBetween();
        int spaceAfter = p.getSpacingAfter();
        int spaceLeft = p.getIndentationLeft();
        RunFormats runFormats = p.getRuns()
            .stream()
            .findFirst()
            .map(r -> new RunFormats(r.getFontSize(), r.getFontFamily(), r.isBold(), r.isItalic(), r.getUnderline()))
            .orElse(new RunFormats(-1, null, false, false, null));
    
        return new Formats(alignment, lineSpaceBetween, spaceLeft, spaceAfter, runFormats,  new ListFormats(p.getNumID()));
    }
    
    private void refillPara(
        ClassData classData,
        List<String> contentDescriptors,
        Formats formats,
        int row,
        XWPFParagraph p
    ) {
        XWPFRun run = p.createRun();
        
        String[] values = contentDescriptors.stream()
            .map(ct -> textUtils.columnToCellMultiline(ct, classData, row, DELIMITERS))
            .collect(Collectors.joining("\n"))
            .split("\n");
    
        formats.apply(p);
        
        if (values.length > 0) {
            for (int r = 0; r < values.length; r++) {
                run.setText(values[r]);
                if (r != values.length - 1) {
                    run.addBreak();
                }
            }
        }
    }
    
    
    private record ListHeaderInfo(XWPFParagraph firstParagraph, List<String> contentDescriptors, List<Filter> rowFilters) { }
}
