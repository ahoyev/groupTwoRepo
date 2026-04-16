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

// Testing output
System.out.println("Program Started");

// I have no clue what this does, but DON'T TOUCH IT.
SwingUtilities.invokeLater(() -> {

    JFrame frame = new JFrame("Welcome to Sticky Note!");
        frame.setLayout(new BorderLayout());
        
    JLabel label = new JLabel("Enter text");
    label.setHorizontalAlignment(SwingConstants.LEFT);
    frame.add(label, BorderLayout.NORTH);

    JTextPane textPane = new JTextPane();

//    JTextArea textArea = new JTextArea();
    
// Start user picks color

    JButton colorPick = new JButton("Change Color");
    
    //Jbutton initialization

    colorPick.addActionListener(e -> {

        Color chosenColor = JColorChooser.showDialog(
            frame,
            "Choose Color",
            frame.getContentPane().getBackground()
        );
        
//This actually chnages the color, don't remove 'if', needs the check or breaks.
        if (chosenColor != null) {
            //sets frame then text area.
            frame.getContentPane().setBackground(chosenColor);
            textPane.setBackground(chosenColor);
        }
    });

//	label.setVerticalAlignment(SwingConstants.TOP);
//  Removed for now, Alignment setVerticalAlignment is redundant to NORTH.

//  Window Setup

	JScrollPane scrollPane = new JScrollPane(textPane);
    frame.add(scrollPane, BorderLayout.CENTER);

// Frame setup
        frame.add(colorPick, BorderLayout.SOUTH);
        
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        });
    }
}
