import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class NoteCard extends JPanel {
    static final int CARD_W = 260;
    static final int CARD_H = 185;
    private static final int PAD   = 6;
    private static final int ARC   = 16;
    private static final int INNER = 20;

    private final NoteModel  note;
    private final StickyNote app;
    private boolean hovered = false;

    NoteCard(NoteModel note, StickyNote app) {
        this.note = note;
        this.app  = app;
        setOpaque(false);
        setPreferredSize(new Dimension(CARD_W, CARD_H));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                if (hovered && isInDeleteZone(e.getX(), e.getY())) {
                    boolean isEmpty = (note.title == null || note.title.trim().isEmpty())
                        && (note.content == null || note.content.trim().isEmpty());
                    if (isEmpty || confirmDelete()) app.deleteNote(note.id);
                } else {
                    app.openEditor(note);
                }
            }
        });
    }

    private boolean confirmDelete() {
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
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        return confirmed[0];
    }

    private boolean isInDeleteZone(int mx, int my) {
        int bx = CARD_W - PAD - 36, by = PAD + 8;
        return mx >= bx && mx <= bx + 26 && my >= by && my <= by + 26;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (hovered) {
            g2.setColor(new Color(40, 30, 20, app.isDark ? 50 : 18));
            g2.fillRoundRect(PAD+1, PAD+5, CARD_W-PAD*2, CARD_H-PAD*2, ARC, ARC);
        }

        g2.setColor(StickyNote.noteBg(note.color, app.isDark));
        g2.fillRoundRect(PAD, PAD, CARD_W-PAD*2, CARD_H-PAD*2, ARC, ARC);
        g2.setColor(StickyNote.noteBorder(note.color, app.isDark));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(PAD, PAD, CARD_W-PAD*2-1, CARD_H-PAD*2-1, ARC, ARC);

        int x = PAD + INNER, y = PAD + INNER;
        int maxW = CARD_W - PAD*2 - INNER*2;
        Color fgC = StickyNote.fg(app.isDark), mutedC = StickyNote.muted(app.isDark);

        g2.setFont(new Font("Georgia", Font.ITALIC, 16));
        FontMetrics tfm = g2.getFontMetrics();
        boolean untitled = note.title == null || note.title.trim().isEmpty();
        g2.setColor(untitled ? mutedC : fgC);
        g2.drawString(truncate(g2, untitled ? "Untitled" : note.title.trim(), hovered ? maxW-28 : maxW), x, y + tfm.getAscent());
        y += tfm.getHeight() + 6;

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        FontMetrics bfm = g2.getFontMetrics();
        String body = note.content == null ? "" : note.content.trim();
        int maxBodyBottom = CARD_H - PAD - 22;

        if (body.isEmpty()) {
            g2.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            g2.setColor(new Color(mutedC.getRed(), mutedC.getGreen(), mutedC.getBlue(), 140));
            g2.drawString("Empty", x, y + bfm.getAscent());
        } else {
            g2.setColor(new Color(fgC.getRed(), fgC.getGreen(), fgC.getBlue(), 190));
            outer:
            for (String raw : body.split("\n")) {
                String rem = raw;
                while (!rem.isEmpty()) {
                    if (y + bfm.getAscent() > maxBodyBottom) break outer;
                    if (bfm.stringWidth(rem) > maxW) {
                        // binary search for wrap point, prefer word boundary
                        int lo = 0, hi = rem.length();
                        while (lo < hi-1) { int mid=(lo+hi)/2; if(bfm.stringWidth(rem.substring(0,mid))<=maxW) lo=mid; else hi=mid; }
                        int sp = rem.lastIndexOf(' ', lo);
                        int at = sp > 0 ? sp : lo;
                        g2.drawString(rem.substring(0, at), x, y + bfm.getAscent());
                        rem = rem.substring(at).trim();
                    } else {
                        g2.drawString(rem, x, y + bfm.getAscent());
                        rem = "";
                    }
                    y += bfm.getHeight();
                }
            }
        }

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g2.setColor(mutedC);
        g2.drawString(NoteModel.formatTime(note.updatedAt), x, CARD_H - PAD - 8);

        if (hovered) {
            int bx = CARD_W - PAD - 36, by = PAD + 8;
            g2.setColor(app.isDark ? new Color(0,0,0,55) : new Color(255,255,255,160));
            g2.fillRoundRect(bx, by, 26, 26, 8, 8);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g2.setColor(StickyNote.fg(app.isDark));
            FontMetrics dfm = g2.getFontMetrics();
            String x_str = "✕";
            g2.drawString(x_str, bx + (26-dfm.stringWidth(x_str))/2, by + 17);
        }

        g2.dispose();
    }

    private static String truncate(Graphics2D g2, String text, int maxW) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(text) <= maxW) return text;
        while (text.length() > 1 && fm.stringWidth(text+"…") > maxW) text = text.substring(0, text.length()-1);
        return text + "…";
    }
}
