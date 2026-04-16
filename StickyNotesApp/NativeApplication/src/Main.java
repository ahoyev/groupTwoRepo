import javax.swing.*;

/**
 * Main class with main method invoked on app start.
 * @version 1.0.0
 * @author Dr. Jody Paul
 */
public class Main {
    /** Private constructor to prevent instantiation of entry point class. */
    private Main() { }

    /**
     * Invoked on start.
     * @param args ignored
     */
    static void main(String[] args) {
        JFrame frame = new JFrame("Welcome to Sticky Note!");
        JLabel label = new JLabel("Enter text");
	label.setHorizontalAlignment(SwingConstants.CENTER);
	label.setVerticalAlignment(SwingConstants.TOP);
        frame.add(label);
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	JTextArea textArea = new JTextArea();
	JScrollPane  scrollPane = new JScrollPane(textArea);  
	frame.setLocationRelativeTo(null);
	frame.add(scrollPane);
        frame.setVisible(true);
    }
}
