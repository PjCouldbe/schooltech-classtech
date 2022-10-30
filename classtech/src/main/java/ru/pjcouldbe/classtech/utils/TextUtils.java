package ru.pjcouldbe.classtech.utils;

import org.apache.commons.lang3.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.w3c.dom.Node;
import ru.pjcouldbe.classtech.data.ClassData;
import ru.pjcouldbe.classtech.docx.RunFormats;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class TextUtils {
    private static final String BM_CL_DELIM = "\\d";
    private static final String BM_DELIM = "\"\\d\"";
    public static final String RUN_NODE_NAME = "w:r";
    public static final String BOOKMARK_END_TAG = "w:bookmarkEnd";

    public String columnToCellMultiline(String column, ClassData classData, int row, List<String> delimiters) {
        column = column.replaceAll(BM_CL_DELIM, "\"$0\"");
        Map<String, String> contentByKeys = Arrays.stream(column.split(BM_DELIM))
            .filter(not(String::isEmpty))
            .map(String::trim)
            .collect(Collectors.toMap(
                Function.identity(),
                key -> classData.get(key, row)
            ));
        
        String content = column;
        for (Map.Entry<String, String> entry : contentByKeys.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            content = content.replace(key, value);
        }
        
        for (int i = 0; i < delimiters.size(); i++) {
            content = content.replace("\"" + i + "\"", delimiters.get(i));
        }
        
        return content;
    }
    
    public Range<Integer> getRunsRangeWithinBookmark(CTBookmark b) {
        int firstIndex = getFirstRunIndex(b);
        int size = getRunsCountWithinBookmark(b);
        return Range.between(firstIndex, Math.max(firstIndex, firstIndex + size - 1));
    }
    
    private int getFirstRunIndex(CTBookmark b) {
        int index = 0;
        
        Node firstNode = b.getDomNode();
        Node prevNode = firstNode.getPreviousSibling();
        while (prevNode != null) {
            String nodeName = prevNode.getNodeName();
            if (nodeName.equals(RUN_NODE_NAME)) {
                ++index;
            }
            
            prevNode = prevNode.getPreviousSibling();
        }
        
        return index;
    }
    
    private int getRunsCountWithinBookmark(CTBookmark b) {
        int amount = 0;
        
        Node firstNode = b.getDomNode();
        Node nextNode = firstNode.getNextSibling();
        while (nextNode != null) {
            String nodeName = nextNode.getNodeName();
            if (nodeName.equals(RUN_NODE_NAME)) {
                ++amount;
            }
            if (nodeName.equals(BOOKMARK_END_TAG)) {
                break;
            }
            
            nextNode = nextNode.getNextSibling();
        }
        
        return amount;
    }
    
    public void replaceRunsRangeWithText(Range<Integer> runsRange, XWPFParagraph p, String text) {
        List<XWPFRun> pruns = p.getRuns();
        for (int r = runsRange.getMaximum(); r > runsRange.getMinimum(); r--) {
            p.removeRun(r);
        }
    
        XWPFRun runToEdit;
        if (runsRange.getMinimum() < pruns.size()) {
            runToEdit = pruns.get(runsRange.getMinimum());
        } else {
            runToEdit = p.createRun();
        
            RunFormats rf = new RunFormats(pruns.get(pruns.size() - 1));
            rf.apply(runToEdit);
        }
    
        runToEdit.setText(text, 0);
    }
    
    public void replaceRunsRangeWithTextMultiline(Range<Integer> runsRange, XWPFParagraph p, String[] lines) {
        List<XWPFRun> pruns = p.getRuns();
        for (int r = runsRange.getMaximum(); r > runsRange.getMinimum(); r--) {
            p.removeRun(r);
        }
        
        XWPFRun runToEdit;
        if (runsRange.getMinimum() < pruns.size()) {
            runToEdit = pruns.get(runsRange.getMinimum());
        } else {
            runToEdit = p.createRun();
            
            RunFormats rf = new RunFormats(pruns.get(pruns.size() - 1));
            rf.apply(runToEdit);
        }
    
        if (lines.length > 0) {
            for (int r = 0; r < lines.length; r++) {
                if (r == 0) {
                    runToEdit.setText(lines[r], 0);
                } else {
                    runToEdit.addBreak();
                    runToEdit.setText(lines[r]);
                }
            }
        }
    }
    
    public List<XWPFParagraph> getAllParagraphs(XWPFDocument doc) {
        List<XWPFParagraph> res = new ArrayList<>(doc.getParagraphs());
        res.addAll(
            doc.getTables().stream()
                .map(XWPFTable::getRows)
                .flatMap(Collection::stream)
                .map(XWPFTableRow::getTableCells)
                .flatMap(Collection::stream)
                .map(XWPFTableCell::getParagraphs)
                .flatMap(Collection::stream)
                .toList()
        );
        
        return res;
    }
    
    public XWPFDocument copyDocument(XWPFDocument srcDoc) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            srcDoc.write(bos);
            
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
                return new XWPFDocument(bis);
            }
        }
    }
}
