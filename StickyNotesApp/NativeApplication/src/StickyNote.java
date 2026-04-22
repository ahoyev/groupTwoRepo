import javax.swing.*;
import java.awt.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class StickyNote extends JFrame{
    
    public StickyNote(){

    //UI Manager Changes (Font for frames, Monospace used.)
    
    UIManager.put("Label.font", new Font("DialogInput", Font.PLAIN, 18));
    UIManager.put("Button.font", new Font("DialogInput", Font.PLAIN, 12));
    UIManager.put("TextField.font", new Font("DialogInput", Font.PLAIN, 18));

        JFrame frame = new JFrame("QuickNotes.");
        frame.getContentPane().setBackground(Color.DARK_GRAY);
        frame.setLayout(new BorderLayout());
        
    JLabel label = new JLabel("Enter text");
    label.setHorizontalAlignment(SwingConstants.LEFT);
    frame.add(label, BorderLayout.NORTH);

    JTextPane textPane = new JTextPane();
    
//Ui and graphic control button

    JButton newNoteButton = new JButton("New Note");
    JButton boldButton = new JButton("Bold");
    JButton highlightButton = new JButton("Highlight");
    JButton colorPick = new JButton("Change Color");


//Action listeners, these call >>TextFormatter on clicks.

    highlightButton.addActionListener(e -> {
        TextFormatter.highlight(textPane);
    });

    boldButton.addActionListener(e -> {
        TextFormatter.boldText(textPane);
    });
    

// Start user picks color

    colorPick.addActionListener(e -> {

        Color chosenColor = JColorChooser.showDialog(
            frame,
            "Choose Color",
            frame.getContentPane().getBackground()
        );
        
//This actually chnages the color, don't remove 'if', needs the check or breaks.
        if (chosenColor != null) {
            frame.getContentPane().setBackground(chosenColor);
            textPane.setBackground(chosenColor);
        }
    });


    newNoteButton.addActionListener(e -> {
        new StickyNote().setVisible(true);
    });
    
//	label.setVerticalAlignment(SwingConstants.TOP);
//  Removed for now, Alignment setVerticalAlignment is redundant to NORTH.

//  Window Setup

	JScrollPane scrollPane = new JScrollPane(textPane);
    frame.add(scrollPane, BorderLayout.CENTER);

// Frame setup
        JPanel buttonPanel = new JPanel();

        buttonPanel.add(colorPick);
        buttonPanel.add(boldButton);
        buttonPanel.add(highlightButton);
        buttonPanel.add(newNoteButton);
        
        frame.add(buttonPanel, BorderLayout.NORTH);

    //    frame.add(colorPick, BorderLayout.SOUTH);
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

}
