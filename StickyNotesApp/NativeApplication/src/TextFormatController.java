import java.awt.Color;

public class TextFormatController {
    // Highlight the text.
    // Remove the highlight.
    // Goes back to selected note background color.

/*
 * Define the highlight color as pink
 */
private static final Color HIGHLIGHT_COLOR = Color.PINK;

    public Color applyHighlight() {
        return HIGHLIGHT_COLOR;
}

    public FormatAction decideAction(boolean hasSelection, boolean currentlyApplied) {
        if(!hasSelection){
            return FormatAction.NONE;
        }
        if(currentlyApplied){
            return FormatAction.REMOVE;
        }
            return FormatAction.APPLY;
        }    

         public Color removeHighlight(Color noteBackgroundColor) {
                return noteBackgroundColor; // Assuming yellow is the highlight color
            }
}
