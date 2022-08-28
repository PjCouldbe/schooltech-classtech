package ru.pjcouldbe.classtech.docx.filter;

public class FilterParser {
    private static final String FILTER_PREFIX = "FILTER_";
    private static final String SEP = "__";
    private static final String ISMULTICH_FILTER = "ISMULTICHILD";
    
    public Filter parse(String bookmarkName) {
        try {
            bookmarkName = bookmarkName.substring(FILTER_PREFIX.length());
            
            if (bookmarkName.equals(ISMULTICH_FILTER)) {
                return new Filter("MULTICHILDCOUNT", FilterOperation.GE, "3");
            }
    
            int fstUS = bookmarkName.indexOf(SEP);
            String key = bookmarkName.substring(0, fstUS);
    
            int sndUS = bookmarkName.indexOf(SEP, fstUS + SEP.length());
            FilterOperation op = FilterOperation.valueOf(bookmarkName.substring(fstUS + SEP.length(), sndUS));
    
            int trdUS = bookmarkName.indexOf(SEP, sndUS + SEP.length());
            if (trdUS < 0) {
                trdUS = bookmarkName.length();
            }
            String value = bookmarkName.substring(sndUS + SEP.length(), trdUS).trim();
          
            return new Filter(key, op, value);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public boolean isFilterBookmark(String bookmarkName) {
        return bookmarkName.startsWith(FILTER_PREFIX);
    }
}
