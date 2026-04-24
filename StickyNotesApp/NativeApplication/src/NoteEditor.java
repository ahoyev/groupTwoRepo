import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class NoteEditor extends JPanel {
    private static final String[] FONT_NAMES = {
        "Arial", "Georgia", "Times New Roman", "Courier New", "Verdana"
    };
    private static final String[] FONT_SIZES = {
        "8","10","12","14","16","18","24","36","48","72"
    };

    private final StickyNote app;
    NoteModel currentNote;

    private JTextPane     titleField;
    private JTextPane     textPane;
    private JButton       fontBtn, sizeBtn;
    private JButton       boldBtn, italicBtn;
    private JButton       highlightBtn, clearBtn;
    private JLabel        timestampLabel;
    private JButton       colorBtn, deleteBtn;
    private JPanel        card;
    private Timer         saveTimer;

    private Color  currentHighlightColor = new Color(0xfef08a);
    private String currentFontFamily     = "Arial";
    private int    currentFontSize       = 14;
    private DocumentListener saveDocListener;

    NoteEditor(StickyNote app) {
        this.app = app;
        setOpaque(true);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        setBorder(BorderFactory.createEmptyBorder(24, 48, 14, 48));

        card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (currentNote != null) {
                    g2.setColor(StickyNote.noteBg(currentNote.color, app.isDark));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(StickyNote.noteBorder(currentNote.color, app.isDark));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 32, 28, 32));

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        top.add(buildToolbar(), BorderLayout.NORTH);
        top.add(buildSeparator(), BorderLayout.SOUTH);

        titleField = new JTextPane() {
            @Override protected void paintComponent(Graphics g) {
                setOpaque(false);
                super.paintComponent(g);
            }
            @Override public boolean getScrollableTracksViewportWidth() { return true; }
        };
        titleField.setFont(new Font("Georgia", Font.ITALIC, 28));
        titleField.setBorder(BorderFactory.createEmptyBorder(8, 0, 10, 0));
        titleField.setOpaque(false);

        textPane = new JTextPane() {
            @Override public boolean getScrollableTracksViewportWidth() { return true; }
        };
        textPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        textPane.setOpaque(false);
        textPane.setBorder(null);

        JScrollPane scroll = new JScrollPane(textPane,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        JPanel titleWrapper = new JPanel(new BorderLayout());
        titleWrapper.setOpaque(false);
        titleWrapper.add(top,        BorderLayout.NORTH);
        titleWrapper.add(titleField, BorderLayout.CENTER);

        inner.add(titleWrapper, BorderLayout.NORTH);
        inner.add(scroll,       BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 2, 0));

        timestampLabel = new JLabel("just now");
        timestampLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

        JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomRight.setOpaque(false);
        colorBtn  = makeOutlineBtn("⬤  change color");
        deleteBtn = makeOutlineBtn("✕  delete");
        colorBtn.addActionListener(e -> {
            if (currentNote != null) { currentNote.cycleColor(); card.repaint(); scheduleAutosave(); }
        });
        deleteBtn.addActionListener(e -> {
            if (currentNote == null) return;
            boolean isEmpty = currentNote.title.trim().isEmpty() && currentNote.content.trim().isEmpty();
            if (isEmpty || confirmDelete()) app.deleteNote(currentNote.id);
        });
        bottomRight.add(colorBtn);
        bottomRight.add(deleteBtn);

        bottomBar.add(timestampLabel, BorderLayout.WEST);
        bottomBar.add(bottomRight,   BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        wireListeners();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 1));
        bar.setOpaque(false);

        fontBtn = makeDropdownBtn(currentFontFamily);
        sizeBtn = makeDropdownBtn(String.valueOf(currentFontSize));

        boldBtn      = makeFormatBtn("B",  new Font(Font.SANS_SERIF, Font.BOLD, 13));
        italicBtn    = makeFormatBtn("I",  new Font("Georgia", Font.ITALIC, 13));
        clearBtn     = makeFormatBtn("T×", new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        highlightBtn = makeSwatchBtn("H",  currentHighlightColor);

        boldBtn.setToolTipText("Bold");
        italicBtn.setToolTipText("Italic");
        highlightBtn.setToolTipText("Highlight Color");
        clearBtn.setToolTipText("Clear Formatting");

        bar.add(fontBtn);
        bar.add(sizeBtn);
        bar.add(makeSep());
        bar.add(boldBtn);
        bar.add(italicBtn);
        bar.add(makeSep());
        bar.add(highlightBtn);
        bar.add(makeSep());
        bar.add(clearBtn);

        return bar;
    }

    private JPanel buildSeparator() {
        JPanel sep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(StickyNote.border(app.isDark));
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(1, 1));
        return sep;
    }

    private void wireListeners() {
        textPane.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "insert-break");

        titleField.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "move-to-body");
        titleField.getActionMap().put("move-to-body", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { textPane.requestFocusInWindow(); }
        });

        fontBtn.addActionListener(e -> {
            JTextPane target = titleField.isFocusOwner() ? titleField : textPane;
            int ss = target.getSelectionStart(), se = target.getSelectionEnd();
            JPopupMenu menu = new JPopupMenu();
            for (String font : FONT_NAMES) {
                JMenuItem item = new JMenuItem(font);
                item.setFont(new Font(font, Font.PLAIN, 13));
                item.addActionListener(ae -> {
                    currentFontFamily = font;
                    fontBtn.setText(font + " ▾");
                    SwingUtilities.invokeLater(() -> {
                        target.requestFocusInWindow();
                        if (se > ss) { target.setSelectionStart(ss); target.setSelectionEnd(se); TextFormatter.setFontFamily(target, font); }
                        StyleConstants.setFontFamily(target.getInputAttributes(), font);
                    });
                });
                menu.add(item);
            }
            menu.show(fontBtn, 0, fontBtn.getHeight());
        });

        sizeBtn.addActionListener(e -> {
            JTextPane target = titleField.isFocusOwner() ? titleField : textPane;
            int ss = target.getSelectionStart(), se = target.getSelectionEnd();
            JPopupMenu menu = new JPopupMenu();
            for (String sz : FONT_SIZES) {
                JMenuItem item = new JMenuItem(sz);
                item.addActionListener(ae -> {
                    try {
                        int size = Integer.parseInt(sz);
                        currentFontSize = size;
                        sizeBtn.setText(sz + " ▾");
                        SwingUtilities.invokeLater(() -> {
                            target.requestFocusInWindow();
                            if (se > ss) { target.setSelectionStart(ss); target.setSelectionEnd(se); TextFormatter.setFontSize(target, size); }
                            StyleConstants.setFontSize(target.getInputAttributes(), size);
                        });
                    } catch (NumberFormatException ignored) {}
                });
                menu.add(item);
            }
            menu.show(sizeBtn, 0, sizeBtn.getHeight());
        });

        boldBtn.addActionListener(e -> {
            JTextPane target = titleField.isFocusOwner() ? titleField : textPane;
            int start = target.getSelectionStart(), end = target.getSelectionEnd();
            boolean isBold = StyleConstants.isBold(start != end
                ? target.getStyledDocument().getCharacterElement(start).getAttributes()
                : target.getInputAttributes());
            if (start != end) TextFormatter.boldText(target);
            StyleConstants.setBold(target.getInputAttributes(), !isBold);
            boldBtn.putClientProperty("active", !isBold);
            boldBtn.repaint();
            target.requestFocusInWindow();
        });

        italicBtn.addActionListener(e -> {
            JTextPane target = titleField.isFocusOwner() ? titleField : textPane;
            int start = target.getSelectionStart(), end = target.getSelectionEnd();
            boolean isItalic = StyleConstants.isItalic(start != end
                ? target.getStyledDocument().getCharacterElement(start).getAttributes()
                : target.getInputAttributes());
            if (start != end) TextFormatter.italic(target);
            StyleConstants.setItalic(target.getInputAttributes(), !isItalic);
            italicBtn.putClientProperty("active", !isItalic);
            italicBtn.repaint();
            target.requestFocusInWindow();
        });

        highlightBtn.addActionListener(e -> {
            JTextPane target = titleField.isFocusOwner() ? titleField : textPane;
            showHighlightPopup(target, target.getSelectionStart(), target.getSelectionEnd());
        });

        clearBtn.addActionListener(e -> {
            JTextPane target = titleField.isFocusOwner() ? titleField : textPane;
            TextFormatter.clearFormat(target);
            MutableAttributeSet ia = target.getInputAttributes();
            ia.removeAttributes(ia.copyAttributes());
            boldBtn.putClientProperty("active", false);
            italicBtn.putClientProperty("active", false);
            boldBtn.repaint();
            italicBtn.repaint();
            target.requestFocusInWindow();
        });

        saveDocListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { scheduleAutosave(); }
            @Override public void removeUpdate(DocumentEvent e)  { scheduleAutosave(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        };
        titleField.getDocument().addDocumentListener(saveDocListener);
        textPane.getDocument().addDocumentListener(saveDocListener);

        saveTimer = new Timer(400, e -> performSave());
        saveTimer.setRepeats(false);
    }

    private void showHighlightPopup(JTextPane target, int selStart, int selEnd) {
        Color[] presets = {
            new Color(0xfef08a), new Color(0x86efac), new Color(0x67e8f9),
            new Color(0xfda4af), new Color(0xfdba74)
        };

        JPopupMenu popup = new JPopupMenu();
        popup.setLayout(new BorderLayout());
        popup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(StickyNote.border(app.isDark), 1),
            BorderFactory.createEmptyBorder(5, 6, 5, 6)));

        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        inner.setOpaque(true);
        inner.setBackground(StickyNote.bg(app.isDark));

        JButton noneBtn = makeSwatchItem(null, "No highlight");
        noneBtn.addActionListener(ae -> {
            popup.setVisible(false);
            SwingUtilities.invokeLater(() -> {
                target.requestFocusInWindow();
                if (selEnd > selStart) { target.setSelectionStart(selStart); target.setSelectionEnd(selEnd); TextFormatter.clearHighlight(target); }
            });
        });
        inner.add(noneBtn);

        for (Color c : presets) {
            JButton sw = makeSwatchItem(c, null);
            sw.addActionListener(ae -> {
                currentHighlightColor = c;
                updateSwatch(highlightBtn, c);
                popup.setVisible(false);
                SwingUtilities.invokeLater(() -> {
                    target.requestFocusInWindow();
                    if (selEnd > selStart) { target.setSelectionStart(selStart); target.setSelectionEnd(selEnd); }
                    TextFormatter.setHighlightColor(target, c);
                });
            });
            inner.add(sw);
        }

        JButton customBtn = new JButton("…");
        customBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        customBtn.setForeground(StickyNote.muted(app.isDark));
        customBtn.setToolTipText("Custom color");
        customBtn.setFocusable(false);
        customBtn.setBorderPainted(false);
        customBtn.setContentAreaFilled(false);
        customBtn.setOpaque(false);
        customBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        customBtn.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        customBtn.addActionListener(ae -> {
            popup.setVisible(false);
            Color c = JColorChooser.showDialog(NoteEditor.this, "Highlight Color", currentHighlightColor);
            if (c != null) {
                currentHighlightColor = c;
                updateSwatch(highlightBtn, c);
                SwingUtilities.invokeLater(() -> {
                    target.requestFocusInWindow();
                    if (selEnd > selStart) { target.setSelectionStart(selStart); target.setSelectionEnd(selEnd); TextFormatter.setHighlightColor(target, c); }
                });
            }
        });
        inner.add(customBtn);

        popup.add(inner);
        popup.show(highlightBtn, 0, highlightBtn.getHeight());
    }

    void loadNote(NoteModel note) {
        currentNote = note;
        MutableAttributeSet ia = textPane.getInputAttributes();
        ia.removeAttributes(ia.copyAttributes());
        boldBtn.putClientProperty("active", false);
        italicBtn.putClientProperty("active", false);
        titleField.setText(note.title);
        if (note.richContent != null && note.richContent.startsWith("rtf64:")) {
            try {
                byte[] rtfBytes = java.util.Base64.getDecoder().decode(note.richContent.substring(6));
                RTFEditorKit kit = new RTFEditorKit();
                Document doc = kit.createDefaultDocument();
                kit.read(new ByteArrayInputStream(rtfBytes), doc, 0);
                stripForegroundColors((StyledDocument) doc);
                textPane.setDocument(doc);
                textPane.getDocument().addDocumentListener(saveDocListener);
            } catch (Exception ex) {
                textPane.setText(note.content);
            }
        } else {
            textPane.setText(note.content);
        }
        textPane.setCaretPosition(0);
        timestampLabel.setText("Saved " + NoteModel.formatTime(note.updatedAt));
        applyTheme(app.isDark);
        card.repaint();
        if (note.title.isEmpty()) titleField.requestFocusInWindow();
        else textPane.requestFocusInWindow();
    }

    void applyTheme(boolean dark) {
        setBackground(StickyNote.bg(dark));
        Color fgC    = StickyNote.fg(dark);
        Color mutedC = StickyNote.muted(dark);
        Color bdrC   = StickyNote.border(dark);
        Color hoverBg = dark ? new Color(0x2a2622) : new Color(0xf0ebe3);

        titleField.setForeground(fgC);
        titleField.setCaretColor(fgC);
        textPane.setForeground(fgC);
        textPane.setCaretColor(fgC);

        for (JButton btn : new JButton[]{fontBtn, sizeBtn}) {
            btn.setForeground(fgC);
            btn.putClientProperty("hover-bg", hoverBg);
            btn.putClientProperty("border-color", bdrC);
            btn.repaint();
        }

        for (JButton btn : new JButton[]{boldBtn, italicBtn, clearBtn, highlightBtn}) {
            btn.setForeground(fgC);
            btn.putClientProperty("hover-bg", hoverBg);
            btn.putClientProperty("active-bc", fgC);
            btn.repaint();
        }

        timestampLabel.setForeground(mutedC);
        for (JButton btn : new JButton[]{colorBtn, deleteBtn}) {
            btn.setForeground(mutedC);
            btn.putClientProperty("border-color", bdrC);
            btn.repaint();
        }

        card.repaint();
        repaint();
    }

    private void stripForegroundColors(StyledDocument doc) {
        int len = doc.getLength();
        int i = 0;
        while (i < len) {
            Element elem = doc.getCharacterElement(i);
            int end = Math.min(elem.getEndOffset(), len);
            AttributeSet as = elem.getAttributes();
            if (as.isDefined(StyleConstants.Foreground)) {
                MutableAttributeSet copy = new SimpleAttributeSet(as);
                copy.removeAttribute(StyleConstants.Foreground);
                doc.setCharacterAttributes(i, end - i, copy, true);
            }
            i = end;
        }
    }

    private void scheduleAutosave() {
        if (currentNote == null) return;
        app.setSaveStatus("saving");
        saveTimer.restart();
    }

    private void performSave() {
        if (currentNote == null) return;
        currentNote.title   = titleField.getText();
        currentNote.content = textPane.getText();
        try {
            RTFEditorKit kit = new RTFEditorKit();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            kit.write(out, textPane.getDocument(), 0, textPane.getDocument().getLength());
            currentNote.richContent = "rtf64:" + java.util.Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception ex) {
            currentNote.richContent = "";
        }
        currentNote.updatedAt = System.currentTimeMillis();
        app.storage.save(currentNote);
        app.setSaveStatus("saved");
        timestampLabel.setText("Saved just now");
        Timer reset = new Timer(1200, e -> app.setSaveStatus("idle"));
        reset.setRepeats(false);
        reset.start();
    }

    private JPanel makeSep() {
        JPanel sep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(StickyNote.border(app.isDark));
                g.drawLine(0, 3, 0, getHeight() - 3);
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(5, 26));
        return sep;
    }

    private JButton makeDropdownBtn(String label) {
        JButton btn = new JButton(label + " ▾") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    Object hbg = getClientProperty("hover-bg");
                    if (hbg instanceof Color) {
                        g2.setColor((Color) hbg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    }
                }
                Object bc = getClientProperty("border-color");
                if (bc instanceof Color) {
                    g2.setColor((Color) bc);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return btn;
    }

    private JButton makeFormatBtn(String label, Font font) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                Object hbg = getClientProperty("hover-bg");
                Object abc = getClientProperty("active-bc");
                if (active && abc instanceof Color) {
                    Color ac = (Color) abc;
                    g2.setColor(new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 35));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.setColor(new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 160));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 6, 6);
                } else if (getModel().isRollover() && hbg instanceof Color) {
                    g2.setColor((Color) hbg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(font);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
        return btn;
    }

    private JButton makeSwatchBtn(String label, Color swatchColor) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    Object hbg = getClientProperty("hover-bg");
                    if (hbg instanceof Color) {
                        g2.setColor((Color) hbg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    }
                }
                Object sc = getClientProperty("swatch");
                if (sc instanceof Color) {
                    g2.setColor((Color) sc);
                    g2.fillRect(4, getHeight() - 5, getWidth() - 8, 3);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.putClientProperty("swatch", swatchColor);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(3, 7, 6, 7));
        return btn;
    }

    private JButton makeSwatchItem(Color fill, String tooltip) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (fill == null) {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 4, 4);
                    g2.setColor(new Color(0xef4444));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawLine(getWidth()-4, 3, 3, getHeight()-4);
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 4, 4);
                } else {
                    g2.setColor(fill);
                    g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 4, 4);
                    if (getModel().isRollover()) {
                        g2.setColor(new Color(0, 0, 0, 40));
                        g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 4, 4);
                    }
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 4, 4);
                }
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(26, 26));
        if (tooltip != null) btn.setToolTipText(tooltip);
        btn.setFocusable(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateSwatch(JButton btn, Color c) {
        btn.putClientProperty("swatch", c);
        btn.repaint();
    }

    boolean confirmDelete() {
        boolean[] confirmed = {false};
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog d = new JDialog(owner instanceof Frame ? (Frame) owner : null, true);
        d.setUndecorated(true);

        Color bg  = StickyNote.bg(app.isDark);
        Color fg  = StickyNote.fg(app.isDark);
        Color bdr = StickyNote.border(app.isDark);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(bg);
        root.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bdr, 1),
            BorderFactory.createEmptyBorder(22, 26, 18, 26)));

        JLabel msg = new JLabel("Delete this note?");
        msg.setFont(new Font("Georgia", Font.ITALIC, 17));
        msg.setForeground(fg);

        JLabel sub = new JLabel("This can't be undone.");
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        sub.setForeground(StickyNote.muted(app.isDark));
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(msg);
        text.add(sub);

        JButton cancelBtn = app.makePrimaryBtn("Cancel");
        cancelBtn.setBackground(bdr);
        cancelBtn.setForeground(fg);
        cancelBtn.addActionListener(e -> d.dispose());

        JButton delBtn = app.makePrimaryBtn("Delete");
        delBtn.setBackground(new Color(0xdc2626));
        delBtn.setForeground(Color.WHITE);
        delBtn.addActionListener(e -> { confirmed[0] = true; d.dispose(); });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(bg);
        btns.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        btns.add(cancelBtn);
        btns.add(delBtn);

        root.add(text, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);

        d.add(root);
        d.pack();
        d.setMinimumSize(new Dimension(270, d.getPreferredSize().height));
        d.setLocationRelativeTo(NoteEditor.this);
        d.setVisible(true);
        return confirmed[0];
    }

    private JButton makeOutlineBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Object bc = getClientProperty("border-color");
                if (bc instanceof Color) {
                    g2.setColor((Color) bc);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }
}
