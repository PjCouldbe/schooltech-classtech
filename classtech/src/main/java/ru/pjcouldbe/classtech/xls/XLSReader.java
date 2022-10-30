package ru.pjcouldbe.classtech.xls;

import com.monitorjbl.xlsx.StreamingReader;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.pjcouldbe.classtech.data.ClassData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class XLSReader {
    protected final String dataFileName;
    
    public ClassData readClassData(Path dir) throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        Map<Integer, String> titleIndex = new HashMap<>();
        
        final String dataFilePath = dir.toAbsolutePath() + File.separator + dataFileName;
        try (
            InputStream in = new FileInputStream(dataFilePath);
            Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(in)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum() - 1;
            
            Iterator<Row> rowIterator = sheet.rowIterator();
            rowIterator.next().cellIterator().forEachRemaining(cell -> {
                String title = cell.getStringCellValue().trim();
                if ( ! title.isEmpty()) {
                    titleIndex.put(cell.getColumnIndex(), title);
                    result.put(title, new ArrayList<>(rowCount));
                }
            });
            
            rowIterator.forEachRemaining(
                row -> row.cellIterator().forEachRemaining(
                    cell -> result.get(titleIndex.get(cell.getColumnIndex())).add(cell.getStringCellValue())
                )
            );
        }
    
        int expectedRows = result.values().stream().map(List::size).max(Comparator.naturalOrder()).orElse(0);
        if (result.values().stream().anyMatch(v -> v.size() != expectedRows)) {
            throw new IllegalStateException("Some rows contains null cells");
        }
        
        return new ClassData(result);
    }
}
