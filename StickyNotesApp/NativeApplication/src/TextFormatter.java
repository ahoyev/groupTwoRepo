import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;

//Start formatter class
public class TextFormatter {

    private static final TextFormatController controller = new TextFormatController();
//Start highlight method
public static void highlight(JTextPane textPane) {
    StyledDocument doc = textPane.getStyledDocument();

    int start = textPane.getSelectionStart();
    int end = textPane.getSelectionEnd();

    boolean hasSelection = start != end;
    if (!hasSelection) {
        return;
    }

    Color currentTextBackground = StyleConstants.getBackground(
        doc.getCharacterElement(start).getAttributes()
    );

    boolean currentlyApplied = controller.applyHighlight().equals(currentTextBackground);

    FormatAction action = controller.decideAction(hasSelection, currentlyApplied);

    Style style = textPane.addStyle("Highlight", null);

    if (action == FormatAction.REMOVE) {
        StyleConstants.setBackground(style, controller.removeHighlight(textPane.getBackground()));
    } else if (action == FormatAction.APPLY) {
        StyleConstants.setBackground(style, controller.applyHighlight());
    } else {
        return;
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