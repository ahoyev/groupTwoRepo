import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class NoteModel {
    static final String[] COLOR_KEYS = {"paper","sun","peach","rose","lilac","sky","sage"};

    String id, title, content, color, richContent;
    long createdAt, updatedAt;

    NoteModel() {
        id        = "n_" + System.currentTimeMillis() + "_" + Integer.toHexString(new Random().nextInt(0xFFFFF));
        title     = "";
        content   = "";
        color     = COLOR_KEYS[new Random().nextInt(COLOR_KEYS.length)];
        createdAt = updatedAt = System.currentTimeMillis();
    }

    NoteModel(String id, String title, String content, String color, long createdAt, long updatedAt) {
        this.id = id; this.title = title; this.content = content;
        this.color = color; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    void cycleColor() {
        int idx = Arrays.asList(COLOR_KEYS).indexOf(color);
        color = COLOR_KEYS[(idx + 1) % COLOR_KEYS.length];
    }

    static String formatTime(long ts) {
        long diff = (System.currentTimeMillis() - ts) / 1000;
        if (diff < 60)     return "just now";
        if (diff < 3600)   return (diff / 60) + "m ago";
        if (diff < 86400)  return (diff / 3600) + "h ago";
        if (diff < 604800) return (diff / 86400) + "d ago";
        return new SimpleDateFormat("MMM d").format(new Date(ts));
    }

    String toJson() {
        return "{\"id\":\"" + esc(id) + "\","
             + "\"title\":\"" + esc(title) + "\","
             + "\"content\":\"" + esc(content) + "\","
             + "\"color\":\"" + esc(color) + "\","
             + "\"createdAt\":" + createdAt + ","
             + "\"updatedAt\":" + updatedAt
             + (richContent != null && !richContent.isEmpty() ? ",\"richContent\":\"" + esc(richContent) + "\"" : "")
             + "}";
    }

    static NoteModel fromJson(String json) {
        try {
            NoteModel m = new NoteModel(
                extract(json, "id"),    extract(json, "title"),
                extract(json, "content"), extract(json, "color"),
                Long.parseLong(extractNum(json, "createdAt")),
                Long.parseLong(extractNum(json, "updatedAt")));
            m.richContent = extract(json, "richContent");
            return m;
        } catch (Exception e) { return null; }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"")
                .replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
    }

    private static String extract(String json, String key) {
        String pat = "\"" + key + "\":\"";
        int i = json.indexOf(pat);
        if (i < 0) return "";
        i += pat.length();
        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
            if (esc) { switch(c){case 'n':sb.append('\n');break;case 't':sb.append('\t');break;default:sb.append(c);}; esc=false; }
            else if (c == '\\') esc = true;
            else if (c == '"')  break;
            else sb.append(c);
        }
        return sb.toString();
    }

    private static String extractNum(String json, String key) {
        String pat = "\"" + key + "\":";
        int i = json.indexOf(pat);
        if (i < 0) return "0";
        i += pat.length();
        int end = i;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        return end > i ? json.substring(i, end) : "0";
    }
}
