package ru.pjcouldbe.classtech.docx.stat;

import java.util.List;

public class DocStatParser {
    public static final String STAT_PREFOX = "STAT_";
    private static final String SEP = "_";
    
    public DocStatStructure buildDocStatStructure(List<BookmarkReplacer> bookmarks) {
        DocStatStructure res = new DocStatStructure();
        for (BookmarkReplacer b : bookmarks) {
            String bname = b.getName();
            if (bname.startsWith(STAT_PREFOX)) {
                DocStatEntryDescriptor dsed = parseStatDescriptor(bname);
                res.addFor(dsed.docStat, dsed.subKey, b.getTextSubstitutor());
            }
        }
        
        return res;
    }
    
    private DocStatEntryDescriptor parseStatDescriptor(String bookmarkName) {
        bookmarkName = bookmarkName.substring(STAT_PREFOX.length());
        
        try {
            int fstUs = bookmarkName.indexOf(SEP);
            String docStat = bookmarkName.substring(0, fstUs).trim().toUpperCase();
            
            int sndUs = bookmarkName.indexOf(SEP, fstUs + SEP.length());
            if (sndUs < 0) {
                sndUs = bookmarkName.length();
            }
            String subKey = bookmarkName.substring(fstUs + SEP.length(), sndUs);
            
            return new DocStatEntryDescriptor(DocStats.valueOf(docStat), subKey);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    
    
    private record DocStatEntryDescriptor(DocStats docStat, String subKey) { }
}
