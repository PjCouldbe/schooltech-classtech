package ru.pjcouldbe.classtech.docx;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import ru.pjcouldbe.classtech.data.ClassData;
import ru.pjcouldbe.classtech.utils.TextUtils;
import ru.pjcouldbe.classtech.docx.filter.Filter;
import ru.pjcouldbe.classtech.docx.filter.FilterParser;
import ru.pjcouldbe.classtech.docx.stat.DocStatParser;
import ru.pjcouldbe.classtech.utils.BookmarkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class DocTableRefiller {
    private static final String ITOG = "итого";
    private static final String NUMBER_COL = "NUMBER";
    private static final List<String> DELIMITERS = List.of(
        "\n",
        " ",
        ", ",
        "; "
    );
    
    private final FilterParser filterParser = new FilterParser();
    private final BookmarkUtils bookmarkUtils = new BookmarkUtils();
    private final TextUtils textUtils = new TextUtils();
    
    
    public void refillAllTables(XWPFDocument doc, ClassData classData) throws XmlException, IOException {
        if (doc.getTables().isEmpty()) {
            return;
        }
        
        for (XWPFTable table : doc.getTables()) {
            TableHeaderInfo thInfo = extractTableHeader(table);
            if (thInfo.columns.length > 0) {
                for (int i = getEndRowIndex(table); i > thInfo.startRow; i--) {
                    table.removeRow(i);
                }
            
                refillTable(classData, table, thInfo);
            }
        }
    }
    
    private int getEndRowIndex(XWPFTable table) {
        int last = table.getRows().size() - 1;
        if (last >= 1 && getRowText(table.getRow(last)).toLowerCase().contains(ITOG)) {
            return last >= 2 && getRowText(table.getRow(last - 1)).isEmpty()
                ? last - 2
                : last - 1;
        }
        
        return last;
    }
    
    private String getRowText(XWPFTableRow row) {
        return row.getTableCells().stream()
            .map(XWPFTableCell::getText)
            .collect(Collectors.joining("\n"))
            .trim();
    }
    
    private TableHeaderInfo extractTableHeader(XWPFTable table) {
        Map<Integer, String> columnsMap = new HashMap<>();
        int maxColumn = 0;
        
        List<Filter> rowFilters = new ArrayList<>();
    
        int startRow;
        for (startRow = 0; startRow < table.getRows().size(); startRow++) {
            int col = 0;
            boolean hasNotBookmarks = true;
            
            for (XWPFTableCell c : table.getRow(startRow).getTableCells()) {
                List<CTBookmark> bookmarkList = c.getParagraphs().get(0).getCTP().getBookmarkStartList();
            
                if ( ! bookmarkList.isEmpty()) {
                    for (CTBookmark ctBookmark : bookmarkList) {
                        String ctBookmarkName = ctBookmark.getName();
                        
                        if (notSkipBookmark(ctBookmarkName)) {
                            if (filterParser.isFilterBookmark(ctBookmarkName)) {
                                rowFilters.add(filterParser.parse(ctBookmarkName));
                            } else {
                                columnsMap.put(col, bookmarkUtils.trimBookmarkName(ctBookmarkName));
                                col++;
                                hasNotBookmarks = false;
                            }
                        }
                    }
                } else if (c.getCTTc().getTcPr().getGridSpan() != null) {
                    int gridWidth = c.getCTTc().getTcPr().getGridSpan().getVal().intValue();
                    col += gridWidth;
                } else {
                    col++;
                }
            }
        
            maxColumn = Math.max(maxColumn, col);
            if (hasNotBookmarks) {
                break;
            }
        }
        
        if (columnsMap.isEmpty()) {
            return new TableHeaderInfo(startRow, new String[0], new ArrayList<>());
        }
    
        int maxIndex = columnsMap.keySet().stream().mapToInt(i -> i).max().orElse(0);
        String[] columns = IntStream.range(0, maxColumn)
            .filter(i -> i <= maxIndex)
            .mapToObj(i -> columnsMap.getOrDefault(i, ""))
            .toArray(String[]::new);
        
        return new TableHeaderInfo(startRow, columns, rowFilters);
    }
    
    private boolean notSkipBookmark(String ctBookmarkName) {
        boolean skip = ctBookmarkName.isEmpty()
            || ctBookmarkName.startsWith("_")
            || ctBookmarkName.startsWith(DocSimpleRefiller.B_PREFIX)
            || ctBookmarkName.startsWith(DocStatParser.STAT_PREFOX);
        
        return ! skip;
    }
    
    private void refillTable(
        ClassData classData,
        XWPFTable table,
        TableHeaderInfo thInfo
    ) throws XmlException, IOException {
        XWPFTableRow oldRow = table.getRow(thInfo.startRow);
        Formats[] rowFormats = getRowFormats(oldRow);
    
        final int newRowsCount = classData.totalStudents();
        
        int rowsAdded = 0;
        for (int row = 0; row < newRowsCount; row++) {
            if (isSatisfiedByFilters(classData, thInfo.rowFilters, row)) {
                CTRow ctrow = CTRow.Factory.parse(oldRow.getCtRow().newInputStream());
                XWPFTableRow newRow = new XWPFTableRow(ctrow, table);
    
                List<XWPFTableCell> cells = newRow.getTableCells();
                for (int c = 0; c < thInfo.columns.length; c++) {
                    refillCell(classData, thInfo.columns, rowFormats, row, cells, c);
                }
    
                table.addRow(newRow, rowsAdded + thInfo.startRow + 1);
                rowsAdded++;
            }
        }
    
        if (rowsAdded == 0) {
            addExtraEmptyRow(classData, table, thInfo, oldRow, rowFormats, newRowsCount);
        }
    
        table.removeRow(thInfo.startRow);
    }
    
    private boolean isSatisfiedByFilters(ClassData classData, List<Filter> rowFilters, int row) {
        for (Filter f : rowFilters) {
            String value = getActualFilterValue(f.key(), classData, row);
            
            if (f.shouldBeFiltered(value)) {
                return false;
            }
        }
        
        return true;
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
    
    private void addExtraEmptyRow(
        ClassData classData,
        XWPFTable table,
        TableHeaderInfo thInfo,
        XWPFTableRow oldRow,
        Formats[] rowFormats,
        int newRowsCount
    ) throws XmlException, IOException {
        CTRow ctrow = CTRow.Factory.parse(oldRow.getCtRow().newInputStream());
        XWPFTableRow newRow = new XWPFTableRow(ctrow, table);
        
        List<XWPFTableCell> cells = newRow.getTableCells();
        for (int c = 0; c < thInfo.columns.length; c++) {
            refillCell(classData, thInfo.columns, rowFormats, newRowsCount + 1, cells, c);
        }
        
        table.addRow(newRow, thInfo.startRow + 1);
    }
    
    private Formats[] getRowFormats(XWPFTableRow oldRow) {
        return oldRow.getTableCells().stream()
            .map(c -> c.getParagraphs().stream().findFirst())
            .map(opt -> {
                ParagraphAlignment alignment = opt.map(XWPFParagraph::getAlignment).orElse(null);
                double lineSpaceBetween = opt.map(XWPFParagraph::getSpacingBetween).orElse(-1.0);
                int spaceAfter = opt.map(XWPFParagraph::getSpacingAfter).orElse(0);
                Integer spaceLeft = opt.map(XWPFParagraph::getIndentationLeft).orElse(null);
                RunFormats rf = opt.map(XWPFParagraph::getRuns)
                    .stream().flatMap(Collection::stream)
                    .findFirst()
                    .map(r -> new RunFormats(r.getFontSize(), r.getFontFamily(), r.isBold(), r.isItalic(), r.getUnderline()))
                    .orElse(new RunFormats(-1, null, false, false, null));
                ListFormats lf = opt.map(XWPFParagraph::getNumID)
                    .map(ListFormats::new)
                    .orElse(null);
            
                return new Formats(alignment, lineSpaceBetween, spaceLeft, spaceAfter, rf, lf);
            })
            .toArray(Formats[]::new);
    }
    
    private void refillCell(
        ClassData classData,
        String[] columns,
        Formats[] cellsFontSize,
        int row,
        List<XWPFTableCell> cells,
        int cellIndex
    ) {
        XWPFTableCell cell = cells.get(cellIndex);
        String column = columns[cellIndex];
        if (column.equals(NUMBER_COL)) {
            return;
        }
        
        if ( ! cell.getParagraphs().isEmpty()) {
            IntStream.range(0, cell.getParagraphs().size()).forEach(r -> cell.removeParagraph(0));
        }
        XWPFParagraph p = cell.addParagraph();
        XWPFRun run = p.createRun();
        
        
        if (cellsFontSize[cellIndex] != null) {
            cellsFontSize[cellIndex].apply(p);
        }
    
        String[] values = textUtils.columnToCellMultiline(column, classData, row, DELIMITERS).split("\n");
        if (values.length > 0) {
            for (int r = 0; r < values.length; r++) {
                run.setText(values[r]);
                if (r != values.length - 1) {
                    run.addBreak();
                }
            }
        }
    }
    
    
    private record TableHeaderInfo(int startRow, String[] columns, List<Filter> rowFilters) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TableHeaderInfo that = (TableHeaderInfo) o;
            return startRow == that.startRow
                && Arrays.equals(columns, that.columns)
                && Objects.equals(rowFilters, that.rowFilters);
        }
    
        @Override
        public int hashCode() {
            int result = Objects.hash(startRow, rowFilters);
            result = 31 * result + Arrays.hashCode(columns);
            return result;
        }
    
        @Override
        public String toString() {
            return "TableHeaderInfo{" +
                "startRow=" + startRow +
                ", columns=" + Arrays.toString(columns) +
                ", rowFilters=" + rowFilters +
                '}';
        }
    }
    
}
