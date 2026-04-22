import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;

//Start formatter class
public class TextFormatter {

//Start highlight method
public static void highlight(JTextPane textPane){
    StyledDocument doc = textPane.getStyledDocument();

    //This actually does the color highlight.

    int start = textPane.getSelectionStart();
    int end = textPane.getSelectionEnd();
    
    if (start == end) return;
    
     boolean isHighlighted = StyleConstants.getBackground(
        doc.getCharacterElement(start).getAttributes()
    ).equals(Color.YELLOW);

    Style style = textPane.addStyle("Highlight", null);

    if (isHighlighted) {
        // Remove highlight, assumes background color is white.
        StyleConstants.setBackground(style, Color.WHITE);
    } 
    
    else {
        // Apply highlight
        StyleConstants.setBackground(style, Color.YELLOW);
    }

    doc.setCharacterAttributes(start, end - start, style, false);
    
}

//Start bold text method
public static void boldText(JTextPane textPane) {
    StyledDocument doc = textPane.getStyledDocument();
    
    int start = textPane.getSelectionStart();
    int end = textPane.getSelectionEnd();

    if (start == end) return;

      boolean isBold = StyleConstants.isBold(
        doc.getCharacterElement(start).getAttributes()
    );

    Style style = textPane.addStyle("Bold", null);

    // Toggle bold
    StyleConstants.setBold(style, !isBold);

    doc.setCharacterAttributes(start, end - start, style, false);
}
}

//End Text formatting