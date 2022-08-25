package ru.pjcouldbe.docx;

import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.XmlException;
import ru.pjcouldbe.data.ClassData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
public class DocsFiller {
    private final SingleDocumentRefiller docFiller = new SingleDocumentRefiller();
    
    public void fillAllDocs(Path path, ClassData classData) throws Exception {
        try (Stream<Path> fileStream = Files.walk(path)) {
            AtomicInteger totalDocs = new AtomicInteger(0);
            fileStream.filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(this::isReadable)
                .filter(this::isClassRegisterFile)
                .forEach(f -> {
                    try {
                        docFiller.fillDoc(f, classData);
                        totalDocs.incrementAndGet();
                    } catch (IOException | XmlException e) {
                        log.error("Error processing: f.getName()", e);
                    }
                });
    
            log.info("Processed total: {} docs", totalDocs.get());
        }
    }
    
    private boolean isReadable(File f) {
        return f.exists() && f.canRead();
    }
    
    private boolean isClassRegisterFile(File f) {
        String fileName = f.getName().toLowerCase();
        return fileName.contains(".doc");
    }
}
