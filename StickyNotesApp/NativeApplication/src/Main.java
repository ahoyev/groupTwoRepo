import javax.swing.*;
import java.awt.*;
//Added java abstract window tk for UI functionality
/* 
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
    public static void main(String[] args) {

// This exists just for bug testing reasons
System.out.println("Program Started");

    JFrame frame = new JFrame("Welcome to Sticky Note!");
        frame.setLayout(new BorderLayout());
        
    JLabel label = new JLabel("Enter text");
    label.setHorizontalAlignment(SwingConstants.LEFT);
    frame.add(label, BorderLayout.NORTH);

//	label.setVerticalAlignment(SwingConstants.TOP);
//  Removed for now, Alignment setVerticalAlignment is redundant to NORTH.

//  Window Setup

	JTextArea textArea = new JTextArea();
//  textArea.setEditable(true);
	JScrollPane scrollPane = new JScrollPane(textArea);
    frame.add(scrollPane, BorderLayout.CENTER);

// Frame setup

        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}
