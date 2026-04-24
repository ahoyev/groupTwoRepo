import javax.swing.*;
import java.awt.*;

/**
 * StickyNote is the main window for the application.
 * Right now this class is still responsible for:
 * - creating the Swing user interface
 * - wiring button clicks to the existing TextFormatter logic
 * - handles note background color changes
 * - opening a new note window
 * 
 * This class should become more "view only"
 */

public class StickyNote extends JFrame{

    // Main text area where the user types and selects text.
    private JTextPane textPane; 

    // Button that opens another sticky note window.
    private JButton newNoteButton;
    
    // Button that toggles bold formatting on selected text.
    private JButton boldButton;

    // Button that toggles highlight formatting on selected text.
    private JButton highlightButton;

    // Button that lets the user change the note background color.
    private JButton colorPickButton;

    /*
     * The constructor 
     */
    public StickyNote(){

        configureUIManager();
        configureFrame();
        initializeComponents();
        buildLayout();
        registerListeners();
    }

    /*
     * UI Manager: Sets default fonts for Swing components used in this window.
     */   
        private void configureUIManager(){
        UIManager.put("Label.font", new Font("DialogInput", Font.PLAIN, 18));
        UIManager.put("Button.font", new Font("DialogInput", Font.PLAIN, 12));
        UIManager.put("TextField.font", new Font("DialogInput", Font.PLAIN, 18));
    }

    /*
     * Configures the frame itself.
     * StickyNote extends JFrame so we can configure this window directly instead of creating a second JFrame object
     * inside the class. 
     */
    private void configureFrame() {
        setTitle("QuickNotes.");
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.YELLOW); // Default note color is yellow.
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /*
     * Creates the Swing components used by the UI.
     * This method only creates the objects it does not place them on the screen yet.
     */
    private void initializeComponents() {
        textPane = new JTextPane();
        textPane.setBackground(Color.YELLOW);

        newNoteButton = new JButton("New Note");
        boldButton = new JButton("Bold");
        highlightButton = new JButton("Highlight");
        colorPickButton = new JButton("Change Color");
    }

    /*
     * Builds the visual layout of the window.
     * Layout structure:
     * - NORTH: title label + button row
     * - CENTER: scrollable text pane
     * Use the top panel so the label and buttons can both exists on the north area without replacing eachother.
     */
    private void buildLayout() {      
        JLabel label = new JLabel("Enter text");
        label.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(colorPickButton);
        buttonPanel.add(boldButton);
        buttonPanel.add(highlightButton);
        buttonPanel.add(newNoteButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(label, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(textPane);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /*
     * Connects button clicks to the current application behavior.
     * For now, this class still directly calls TextFormatter and handles simple UI actions.
     * These listeners can be moved into controller classes to control logic. This is where we can
     * begin to move into controller classes and use JUnit testing.
     */
    private void registerListeners() {

        /*
         * Applies or removes highlight formatting to the currently selected text.
         * Formatting behavior is inside TextFormatter
         */
        highlightButton.addActionListener(e -> {
            TextFormatter.highlight(textPane);
        });

        /*
         * Applies or removes bold formatting to the currently selected text.
         * Formatting behavior is inside TextFormatter
         */
        boldButton.addActionListener(e -> {
            TextFormatter.boldText(textPane);
        });

        /*
         * Opens a color chooser dialog and changes the note background color if the user picks a valid color.
         * 
         */
        colorPickButton.addActionListener(e -> {
            Color chosenColor = JColorChooser.showDialog(
                    this,
                    "Choose Color",
                    getContentPane().getBackground()
            );

            if (chosenColor != null) {
                getContentPane().setBackground(chosenColor);
                textPane.setBackground(chosenColor);
            }
        });

        /*
         * Opens a new sticky note window.
         * Each note currently exists as its own independent JFrame.
         */
        newNoteButton.addActionListener(e -> {
            new StickyNote().setVisible(true);
        });
    }
}   

