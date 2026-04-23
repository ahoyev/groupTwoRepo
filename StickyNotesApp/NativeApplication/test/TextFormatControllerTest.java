import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.awt.Color;
import java.beans.Transient;


public class TextFormatControllerTest {
 
    @Test
    public void decideAction_returnsNone_whenNoSelection(){
        TextFormatController controller = new TextFormatController();

        FormatAction result = controller.decideAction(false, false);

        assertEquals(FormatAction.NONE, result);
    }

    @Test
    public void testApplyHighlight_returnsPink(){
        TextFormatController controller = new TextFormatController();
        Color result = controller.applyHighlight();
        assertEquals(Color.PINK, result);
    }

    @Test
    public void testRemoveHighlight_returnsPassedInBackgroundColor(){
        TextFormatController controller = new TextFormatController();
        Color result = controller.removeHighlight(Color.PINK);
        assertEquals(Color.PINK, result);
    }
}