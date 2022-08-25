package ru.pjcouldbe.docx;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFRun;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RunFormats {
    int fontSize;
    String fontFamily;
    boolean bold;
    boolean italic;
    UnderlinePatterns underline;
    
    public RunFormats(XWPFRun r) {
        this.fontSize = r.getFontSize();
        this.fontFamily = r.getFontFamily();
        this.bold = r.isBold();
        this.italic = r.isItalic();
        this.underline = r.getUnderline();
    }
    
    public void apply(XWPFRun r) {
        if (fontSize > 0) {
            r.setFontSize(fontSize);
        }
        if (fontFamily != null && !fontFamily.isEmpty()) {
            r.setFontFamily(fontFamily);
        }
        r.setBold(bold);
        r.setItalic(italic);
        if (underline != null) {
            r.setUnderline(underline);
        }
    }
}
