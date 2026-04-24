import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

// Refactored from Jacob Hefley — kept his 5-method constructor structure:
// configureUIManager / configureFrame / initializeComponents / buildLayout / registerListeners
public class StickyNote extends JFrame {

    // [key index] → [light bg, dark bg, light border, dark border]
    // keys: paper, sun, peach, rose, lilac, sky, sage (match NoteModel.COLOR_KEYS order)
    private static final Color[][] NOTE_COLORS = {
        { new Color(0xffffff), new Color(0x201e1c), new Color(0xebe8e4), new Color(0x2e2a26) },
        { new Color(0xfdf3bd), new Color(0x3a3320), new Color(0xf4e58b), new Color(0x4a4028) },
        { new Color(0xfde1cb), new Color(0x3a2819), new Color(0xf8c9a4), new Color(0x4a3321) },
        { new Color(0xfbdbe0), new Color(0x3a2025), new Color(0xf5c0c8), new Color(0x4a2830) },
        { new Color(0xebddf7), new Color(0x2c2238), new Color(0xd5bef0), new Color(0x3a2c49) },
        { new Color(0xd4e7fe), new Color(0x1e2a3d), new Color(0xb0d0fb), new Color(0x28384f) },
        { new Color(0xd4f2db), new Color(0x1d3124), new Color(0xaae3b8), new Color(0x274330) },
    };

    static Color noteBg(String key, boolean dark) {
        int i = Arrays.asList(NoteModel.COLOR_KEYS).indexOf(key);
        return NOTE_COLORS[i < 0 ? 0 : i][dark ? 1 : 0];
    }

    static Color noteBorder(String key, boolean dark) {
        int i = Arrays.asList(NoteModel.COLOR_KEYS).indexOf(key);
        return NOTE_COLORS[i < 0 ? 0 : i][dark ? 3 : 2];
    }

    static Color bg(boolean dark)     { return dark ? new Color(0x131110) : new Color(0xfaf9f6); }
    static Color fg(boolean dark)     { return dark ? new Color(0xede9e3) : new Color(0x1c1a17); }
    static Color muted(boolean dark)  { return dark ? new Color(0x8a847c) : new Color(0x8a827a); }
    static Color border(boolean dark) { return dark ? new Color(0x2a2622) : new Color(0xebe6df); }

    boolean         isDark   = false;
    List<NoteModel> notes;
    NoteStorage     storage;
    String          activeId = null;

    private JPanel     headerPanel;
    private JButton    backBtn;
    private JLabel     appTitleLabel;
    private JLabel     subLabel;
    private JLabel     saveStatusLabel;
    private JButton    themeBtn;
    private JButton    newNoteButton;   // kept Jacob's field name
    private JPanel     contentPanel;
    private CardLayout cardLayout;
    private JPanel     listViewPanel;
    private JPanel     cardsPanel;
    private NoteEditor editor;

    // Jacob Hefley — original constructor structure
    public StickyNote() {
        storage = new NoteStorage();
        notes   = storage.loadAll();
        configureUIManager();
        configureFrame();
        initializeComponents();
        buildLayout();
        registerListeners();
    }

    // Jacob Hefley — sets default Swing fonts
    private void configureUIManager() {
        UIManager.put("Label.font",     new Font("DialogInput", Font.PLAIN, 18));
        UIManager.put("Button.font",    new Font("DialogInput", Font.PLAIN, 12));
        UIManager.put("TextField.font", new Font("DialogInput", Font.PLAIN, 18));
    }

    // Jacob Hefley — configures the JFrame
    private void configureFrame() {
        setTitle("Quick Notes");
        setLayout(new BorderLayout());
        setSize(900, 650);
        setMinimumSize(new Dimension(620, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // Jacob Hefley — creates components (expanded with new widgets)
    private void initializeComponents() {
        backBtn         = new JButton("←");
        appTitleLabel   = new JLabel("Quick Notes");
        subLabel        = new JLabel(noteCountText());
        saveStatusLabel = new JLabel("auto-saves");
        themeBtn        = new JButton("☾");
        newNoteButton   = makePrimaryBtn("+ New");
        cardLayout      = new CardLayout();
        contentPanel    = new JPanel(cardLayout);
        cardsPanel      = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));
        listViewPanel   = new JPanel(new BorderLayout());
        editor          = new NoteEditor(this);

        styleIconBtn(backBtn);
        styleIconBtn(themeBtn);
        backBtn.setVisible(false);
        appTitleLabel.setFont(new Font("Georgia", Font.ITALIC | Font.BOLD, 20));
        subLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        saveStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
    }

    // Jacob Hefley — assembles the layout (expanded for multi-panel design)
    private void buildLayout() {
        headerPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(border(isDark));
                g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        appTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(appTitleLabel);
        titleBlock.add(subLabel);
        left.add(backBtn);
        left.add(buildIconBox());
        left.add(titleBlock);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(saveStatusLabel);
        right.add(themeBtn);
        right.add(newNoteButton);

        headerPanel.add(left,  BorderLayout.WEST);
        headerPanel.add(right, BorderLayout.EAST);

        cardsPanel.setOpaque(true);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JScrollPane scroll = new JScrollPane(cardsPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        listViewPanel.add(scroll, BorderLayout.CENTER);
        listViewPanel.putClientProperty("scroll", scroll);

        contentPanel.add(listViewPanel, "list");
        contentPanel.add(editor, "editor");
        cardLayout.show(contentPanel, "list");

        add(headerPanel,  BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        applyTheme();
        refreshCards();
    }

    // Jacob Hefley — registers listeners (expanded; TextFormatter wired inside NoteEditor)
    private void registerListeners() {
        newNoteButton.addActionListener(e -> createNote());
        backBtn.addActionListener(e -> showList());
        themeBtn.addActionListener(e -> {
            isDark = !isDark;
            themeBtn.setText(isDark ? "☀" : "☾");
            applyTheme();
        });
    }

    void createNote() {
        NoteModel note = new NoteModel();
        notes.add(note);
        storage.save(note);
        openEditor(note);
    }

    void openEditor(NoteModel note) {
        activeId = note.id;
        backBtn.setVisible(true);
        newNoteButton.setVisible(false);
        subLabel.setText("editing a note");
        editor.loadNote(note);
        cardLayout.show(contentPanel, "editor");
    }

    void showList() {
        activeId = null;
        backBtn.setVisible(false);
        newNoteButton.setVisible(true);
        notes.sort((a, b) -> Long.compare(a.createdAt, b.createdAt));
        refreshCards();
        cardLayout.show(contentPanel, "list");
    }

    void deleteNote(String id) {
        notes.removeIf(n -> n.id.equals(id));
        storage.delete(id);
        showList();
    }

    void setSaveStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            switch (status) {
                case "saving": saveStatusLabel.setText("saving…");   break;
                case "saved":  saveStatusLabel.setText("✓ saved");    break;
                default:       saveStatusLabel.setText("auto-saves"); break;
            }
        });
    }

    void refreshCards() {
        cardsPanel.removeAll();
        if (notes.isEmpty()) {
            cardsPanel.setLayout(new GridBagLayout());
            cardsPanel.add(buildEmptyState());
        } else {
            cardsPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 16, 16));
            for (NoteModel note : notes) cardsPanel.add(new NoteCard(note, this));
        }
        subLabel.setText(noteCountText());
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    void applyTheme() {
        Color bgC = bg(isDark), fgC = fg(isDark), mutedC = muted(isDark);
        getContentPane().setBackground(bgC);
        headerPanel.setBackground(bgC);
        contentPanel.setBackground(bgC);
        cardsPanel.setBackground(bgC);
        listViewPanel.setBackground(bgC);
        editor.setBackground(bgC);
        Object scroll = listViewPanel.getClientProperty("scroll");
        if (scroll instanceof JScrollPane) {
            ((JScrollPane) scroll).setBackground(bgC);
            ((JScrollPane) scroll).getViewport().setBackground(bgC);
        }
        appTitleLabel.setForeground(fgC);
        subLabel.setForeground(mutedC);
        saveStatusLabel.setForeground(mutedC);
        backBtn.setForeground(fgC);
        themeBtn.setForeground(fgC);
        newNoteButton.setBackground(fgC);
        newNoteButton.setForeground(bgC);
        editor.applyTheme(isDark);
        refreshCards();
        headerPanel.repaint();
    }

    private JPanel buildIconBox() {
        JPanel box = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDark ? new Color(0x221e1a) : new Color(0xf0ebe3));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(fg(isDark));
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                String s = "✎";
                g2.drawString(s, (getWidth()-fm.stringWidth(s))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(32, 32));
        return box;
    }

    private void styleIconBtn(JButton btn) {
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    JButton makePrimaryBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private JPanel buildEmptyState() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel headline = new JLabel("<html><center><i>a blank page,<br>waiting patiently.</i></center></html>");
        headline.setFont(new Font("Georgia", Font.ITALIC, 36));
        headline.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("<html><center>Jot down a thought, a reminder,<br>or something you don't want to forget.<br>Everything saves itself.</center></html>");
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(14, 0, 22, 0));
        JButton btn = makePrimaryBtn("Write your first note");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(fg(isDark));
        btn.setForeground(bg(isDark));
        btn.addActionListener(e -> createNote());
        p.add(Box.createVerticalGlue());
        p.add(headline);
        p.add(sub);
        p.add(btn);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private String noteCountText() {
        int n = notes.size();
        return n + " " + (n == 1 ? "note" : "notes");
    }

    // Fixes FlowLayout preferred height so cards wrap correctly inside JScrollPane
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container t) { return layout(t, true);  }
        @Override public Dimension minimumLayoutSize(Container t)   { return layout(t, false); }
        private Dimension layout(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int w = target.getWidth(); if (w == 0) w = Integer.MAX_VALUE;
                Insets ins = target.getInsets();
                int maxW = w - ins.left - ins.right - getHgap()*2;
                Dimension dim = new Dimension(0, 0);
                int rowW = 0, rowH = 0;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (!m.isVisible()) continue;
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowW + d.width > maxW && rowW > 0) { dim.width = Math.max(dim.width, rowW); dim.height += rowH + getVgap(); rowW = 0; rowH = 0; }
                    if (rowW > 0) rowW += getHgap();
                    rowW += d.width; rowH = Math.max(rowH, d.height);
                }
                dim.width   = Math.max(dim.width, rowW);
                dim.height += rowH;
                dim.width  += ins.left + ins.right + getHgap()*2;
                dim.height += ins.top  + ins.bottom + getVgap()*2;
                return dim;
            }
        }
    }
}
