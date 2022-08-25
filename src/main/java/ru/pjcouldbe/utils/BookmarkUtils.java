package ru.pjcouldbe.utils;

public class BookmarkUtils {
    public String trimBookmarkName(String bookmarkName) {
        bookmarkName = bookmarkName.trim();
        
        int lastCh;
        for (lastCh = bookmarkName.length() - 1; lastCh >= 0; lastCh--) {
            if ( ! Character.isDigit(bookmarkName.charAt(lastCh))) {
                break;
            }
        }
        
        return bookmarkName.substring(0, lastCh + 1);
    }
}
