package ru.pjcouldbe.docx;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
class Formats {
    ParagraphAlignment alignment;
    double lineSpaceBetween;
    Integer spaceLeft;
    int spaceAfter;
    int fontSize;
    String fontFamily;
    boolean bold;
    boolean italic;
    UnderlinePatterns underline;
    ListFormats listFormats;
    
    public Formats(
        ParagraphAlignment alignment,
        double lineSpaceBetween,
        Integer spaceLeft,
        int spaceAfter,
        RunFormats rf,
        ListFormats lf
    ) {
        this(alignment, lineSpaceBetween, spaceLeft, spaceAfter,
            rf.fontSize, rf.fontFamily, rf.bold, rf.italic, rf.underline, lf);
    }
    
    public void apply(XWPFParagraph p) {
        if (alignment != null) {
            p.setAlignment(alignment);
        }
        if (lineSpaceBetween > 0.0) {
            p.setSpacingBetween(lineSpaceBetween);
        }
        if (spaceLeft != null) {
            p.setIndentationLeft(spaceLeft);
            p.setIndentFromLeft(spaceLeft);
        }
        if (spaceAfter >= 0) {
            p.setSpacingAfter(spaceAfter);
        }
        if (fontSize > 0) {
            p.getRuns().forEach(r -> r.setFontSize(fontSize));
        }
        if (fontFamily != null && !fontFamily.isEmpty()) {
            p.getRuns().forEach(r -> r.setFontFamily(fontFamily));
        }
        p.getRuns().forEach(r -> r.setBold(bold));
        p.getRuns().forEach(r -> r.setItalic(italic));
        if (underline != null) {
            p.getRuns().forEach(r -> r.setUnderline(underline));
        }
        
        if (listFormats != null) {
            listFormats.apply(p);
        }
    }
    
    public RunFormats toRunFormats() {
        return new RunFormats(fontSize, fontFamily, bold, italic, underline);
    }
}
