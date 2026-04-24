import javax.swing.JTextPane;
import javax.swing.text.*;
import java.awt.Color;

// Ellie Key — original bold/highlight methods
// Extended with italic, font family, font size, text color, highlight color, clear format
public class TextFormatter {

    // Ellie Key
    public static void highlight(JTextPane textPane) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end   = textPane.getSelectionEnd();
        if (start == end) return;
        boolean isHighlighted = StyleConstants.getBackground(
            doc.getCharacterElement(start).getAttributes()).equals(Color.YELLOW);
        Style style = textPane.addStyle("Highlight", null);
        StyleConstants.setBackground(style, isHighlighted ? Color.WHITE : Color.YELLOW);
        doc.setCharacterAttributes(start, end - start, style, false);
    }

    // Ellie Key
    public static void boldText(JTextPane textPane) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end   = textPane.getSelectionEnd();
        if (start == end) return;
        boolean isBold = StyleConstants.isBold(doc.getCharacterElement(start).getAttributes());
        Style style = textPane.addStyle("Bold", null);
        StyleConstants.setBold(style, !isBold);
        doc.setCharacterAttributes(start, end - start, style, false);
    }

    public static void italic(JTextPane textPane) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end   = textPane.getSelectionEnd();
        if (start == end) return;
        boolean isItalic = StyleConstants.isItalic(doc.getCharacterElement(start).getAttributes());
        Style style = textPane.addStyle("Italic", null);
        StyleConstants.setItalic(style, !isItalic);
        doc.setCharacterAttributes(start, end - start, style, false);
    }

    public static void setFontFamily(JTextPane textPane, String family) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end   = textPane.getSelectionEnd();
        if (start == end) return;
        Style style = textPane.addStyle("FontFamily", null);
        StyleConstants.setFontFamily(style, family);
        doc.setCharacterAttributes(start, end - start, style, false);
    }

    public static void setFontSize(JTextPane textPane, int size) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end   = textPane.getSelectionEnd();
        if (start == end) return;
        Style style = textPane.addStyle("FontSize", null);
        StyleConstants.setFontSize(style, size);
        doc.setCharacterAttributes(start, end - start, style, false);
    }

    public static void setTextColor(JTextPane textPane, Color color) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end   = textPane.getSelectionEnd();
        if (start == end) return;
        Style style = textPane.addStyle("TextColor", null);
        StyleConstants.setForeground(style, color);
        doc.setCharacterAttributes(start, end - start, style, false);
    }

    public static void setHighlightColor(JTextPane textPane, Color color) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end   = textPane.getSelectionEnd();
        if (start == end) return;
        Style style = textPane.addStyle("HighlightColor", null);
        StyleConstants.setBackground(style, color);
        doc.setCharacterAttributes(start, end - start, style, false);
    }

    // Strips all character-level formatting from the selection
    public static void clearFormat(JTextPane textPane) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end   = textPane.getSelectionEnd();
        if (start == end) return;
        Style plain = StyleContext.getDefaultStyleContext()
            .getStyle(StyleContext.DEFAULT_STYLE);
        doc.setCharacterAttributes(start, end - start, plain, true);
    }

    // Removes only the background highlight from the selection, leaving other formatting intact
    public static void clearHighlight(JTextPane textPane) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end   = textPane.getSelectionEnd();
        if (start == end) return;
        int i = start;
        while (i < end) {
            Element elem = doc.getCharacterElement(i);
            int elemEnd = Math.min(elem.getEndOffset(), end);
            AttributeSet as = elem.getAttributes();
            if (as.isDefined(StyleConstants.Background)) {
                MutableAttributeSet copy = new SimpleAttributeSet(as);
                copy.removeAttribute(StyleConstants.Background);
                doc.setCharacterAttributes(i, elemEnd - i, copy, true);
            }
            i = elemEnd;
        }
    }
}
