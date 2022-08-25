package ru.pjcouldbe.docx;

import lombok.Value;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.math.BigInteger;

@Value
public class ListFormats {
    BigInteger numId;
    
    public void apply(XWPFParagraph p) {
        if (numId != null) {
            p.setNumID(numId);
        }
    }
}
