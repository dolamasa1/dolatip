package UI.components.Assets.tooltips.dolatip;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

/**
 * Singleton floating tooltip that renders a two-section card:
 *
 * <pre>
 * ┌─────────────────────────┐
 * │ Title                   │  ← header (always visible)
 * ├─────────────────────────┤
 * │ ● Label         Value   │  ┐
 * │ ▼ Label         Value   │  ┘  upper section (always visible)
 * ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤  ← separator (click to expand/collapse)
 * │ ● Label       Value     │  ┐
 * │ ● Label       Value     │  ┘  lower section (smaller font, toggled)
 * └─────────────────────────┘
 * </pre>
 *
 * <h3>Typical usage</h3>
 * <pre>
 * // Build payload
 * DolatipPayload payload = DolatipPayload.builder()
 *     .usage(DolatipUsage.FINANCIAL)
 *     .upper(
 *         DolatipEntry.ofTrend("Close",  142.50, 139.00),
 *         DolatipEntry.ofTrend("Volume", 8_420,  7_900)
 *     )
 *     .lower(
 *         DolatipEntry.ofTrend("Open",  140.10, 141.20),
 *         DolatipEntry.ofTrend("High",  144.80, 143.50),
 *         DolatipEntry.ofTrend("Low",   139.30, 138.00)
 *     )
 *     .build();
 *
 * // Attach to any Swing component
 * Dolatip.attachTo(myLabel, payload, DolatipUsage.FINANCIAL.toConfig("Price History"));
 * </pre>
 *
 * <h3>Legacy usage (List&lt;Double&gt;)</h3>
 * <pre>
 * Dolatip.attachTo(component, myDoubleList, config);
 * </pre>
 */
public class Dolatip {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static Dolatip sharedInstance;

    private final JWindow window;
    private Config        config;
    private Timer         animationTimer;
    private float         currentOpacity;
    private DolatipPayload currentPayload;
    private boolean       isExpanded = false;
    private Component     parentComponent;

    private Dolatip(Config config) {
        this.config = config;
        this.window = new JWindow();
        initializeWindow();
    }

    public static synchronized Dolatip getInstance(Config config) {
        if (sharedInstance == null) {
            sharedInstance = new Dolatip(config);
        } else {
            sharedInstance.config = config;
        }
        return sharedInstance;
    }

    // ── Window setup ──────────────────────────────────────────────────────────

    private void initializeWindow() {
        window.setBackground(new Color(0, 0, 0, 0));
        window.setFocusableWindowState(false);
        window.setAlwaysOnTop(true);
        window.setType(Window.Type.POPUP);
    }

    private void positionWindow() {
        if (parentComponent == null || !parentComponent.isShowing()) return;
        Point parentLoc = parentComponent.getLocationOnScreen();
        int x = parentLoc.x + parentComponent.getWidth() + config.offsetX;
        int y = parentLoc.y + config.offsetY;

        Rectangle screen = window.getGraphicsConfiguration().getBounds();
        int maxX = screen.x + screen.width  - window.getWidth();
        int maxY = screen.y + screen.height - window.getHeight();

        window.setLocation(
                Math.max(screen.x, Math.min(x, maxX)),
                Math.max(screen.y, Math.min(y, maxY))
        );
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Attaches Dolatip to a component using a {@link DolatipPayload}.
     * Any previously registered Dolatip listener is replaced.
     */
    public static void attachTo(JComponent component, DolatipPayload payload, Config config) {
        removeExistingListeners(component);
        component.addMouseListener(new DolatipMouseAdapter(payload, config));
    }

    /**
     * Legacy overload — accepts the original {@code List<Double>} and converts
     * it to a payload automatically.
     */
    public static void attachTo(JComponent component, List<Double> data, Config config) {
        attachTo(component, DolatipPayload.fromTrends(data, config.maxBeforeClick), config);
    }

    /** Programmatically show the tooltip anchored to the given component. */
    public void show(Component parent, DolatipPayload payload) {
        if (payload == null || payload.isEmpty()) return;
        this.currentPayload = payload;
        this.parentComponent = parent;
        this.isExpanded = false;

        // Sync config width with usage when the payload carries a usage
        if (payload.usage != DolatipUsage.DEFAULT
                && config.width == Config.DEFAULT_WIDTH) {
            config.width = payload.usage.defaultWidth;
        }

        if (!window.isDisplayable()) initializeWindow();
        updateContent();
        positionWindow();
        startShowAnimation();
    }

    public void hide() {
        startHideAnimation();
    }

    /** Toggles the lower section visibility (click handler on the card). */
    public void toggleExpanded() {
        if (currentPayload != null && currentPayload.hasLower()) {
            Dimension oldSize = window.getSize();
            isExpanded = !isExpanded;
            updateContent();
            window.pack();
            animateWindowResize(oldSize, window.getSize());
            positionWindow();
        }
    }

    // ── Content rendering ─────────────────────────────────────────────────────

    private void updateContent() {
        JLabel content = new JLabel(buildHTML()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2,
                        config.cornerRadius, config.cornerRadius);

                // Background (UIManager key overrides config for theme integration)
                Color bg = uiColor("Dolatip.background", resolvedBg());
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        config.cornerRadius, config.cornerRadius);

                super.paintComponent(g2);
                g2.dispose();
            }
        };

        content.addMouseListener(new ContentMouseAdapter(this));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(8, 12, 10, 12));
        window.setContentPane(content);
        window.pack();
    }

    /**
     * Builds the full HTML string for the JLabel.
     *
     * Layout (table-based for reliable Swing HTML rendering):
     * <pre>
     *  header bar
     *  ── upper rows (dot · label · value) ──
     *  ── separator (if lower non-empty and expanded) ──
     *  ── lower rows (smaller font) ──
     *  ── expand hint (if lower non-empty and collapsed) ──
     * </pre>
     */
    private String buildHTML() {
        DolatipUsage usage   = currentPayload.usage;
        Color headerBg       = uiColor("Dolatip.headerBackground", resolvedHeaderBg());
        Color headerFg       = uiColor("Dolatip.foreground",       resolvedHeaderFg());
        Color primaryFg      = uiColor("Dolatip.foreground",       usage.primaryTextColor);
        Color secondaryFg    = uiColor("Dolatip.secondaryForeground", usage.secondaryTextColor);
        Color lowerFg        = usage.lowerTextColor;
        Color sepColor       = usage.separatorColor();

        int w          = config.width;
        int bodySize   = usage.bodyFontSize;
        int smallSize  = usage.smallFontSize;

        StringBuilder h = new StringBuilder();
        h.append("<html><body style='width:").append(w).append("px; margin:0; padding:0;'>");

        // ── Header ────────────────────────────────────────────────────────────
        h.append("<div style='background:").append(hex(headerBg))
         .append("; color:").append(hex(headerFg))
         .append("; padding:6px 8px; margin:-8px -12px 8px -12px;")
         .append(" border-radius:").append(config.cornerRadius - 2).append("px ")
         .append(config.cornerRadius - 2).append("px 0 0;")
         .append(" font-weight:bold; font-size:").append(bodySize).append("px;'>")
         .append(config.title)
         .append("</div>");

        // ── Upper section ─────────────────────────────────────────────────────
        List<DolatipEntry> upperList = currentPayload.upper;
        if (!upperList.isEmpty()) {
            h.append("<table width='").append(w).append("' cellpadding='2' cellspacing='0' border='0'>");
            for (int i = 0; i < upperList.size(); i++) {
                DolatipEntry e  = upperList.get(i);
                Color rowColor  = e.rowColor != null ? e.rowColor
                                : i == 0 ? primaryFg : secondaryFg;
                appendRow(h, e, rowColor, bodySize, /* bold first */ i == 0);
            }
            h.append("</table>");
        }

        // ── Lower section (expanded) ──────────────────────────────────────────
        if (currentPayload.hasLower() && isExpanded) {
            // Separator
            h.append("<hr noshade size='1' color='").append(hex(sepColor))
             .append("' style='margin:5px 0;'/>");

            h.append("<table width='").append(w).append("' cellpadding='1' cellspacing='0' border='0'>");
            for (DolatipEntry e : currentPayload.lower) {
                Color rowColor = e.rowColor != null ? e.rowColor : lowerFg;
                appendRow(h, e, rowColor, smallSize, false);
            }
            h.append("</table>");
        }

        // ── Expand hint (collapsed and has lower data) ────────────────────────
        if (currentPayload.hasLower() && !isExpanded) {
            h.append("<div style='color:").append(hex(sepColor))
             .append("; font-size:").append(smallSize)
             .append("px; text-align:center; margin-top:5px;'>")
             .append("&#8230; click to expand")
             .append("</div>");
        }

        h.append("</body></html>");
        return h.toString();
    }

    /**
     * Appends a single data row to the HTML StringBuilder.
     *
     * Row layout (3-column table row):
     * <pre>
     * | dot (10px) | label (grow) | value (right) |
     * </pre>
     *
     * The trend icon (▲/▼/✓/⚠) is rendered inline before the dot when present.
     */
    private void appendRow(StringBuilder h, DolatipEntry e,
                            Color rowColor, int fontSize, boolean bold) {
        String dotHex    = hex(e.dotColor);
        String colorHex  = hex(rowColor);
        String valueStyle = bold
                ? "font-weight:bold; font-size:" + fontSize + "px; color:" + colorHex + ";"
                : "font-size:" + fontSize + "px; color:" + colorHex + ";";
        String labelStyle = "font-size:" + (fontSize - 1) + "px; color:" + colorHex + ";";

        h.append("<tr>");

        // Dot cell
        h.append("<td width='16' valign='middle' align='center'>")
         .append("<font color='").append(dotHex).append("' style='font-size:9px;'>&#9679;</font>")
         .append("</td>");

        // Label cell (with optional trend icon prefix)
        h.append("<td valign='middle'>");
        if (e.hasTrendIcon()) {
            String iconColor = trendIconColor(e.trend);
            h.append("<font color='").append(iconColor)
             .append("' style='font-size:9px;'>").append(e.trendSymbol())
             .append("</font>&nbsp;");
        }
        if (!e.label.isEmpty()) {
            h.append("<font style='").append(labelStyle).append("'>")
             .append(e.label).append("</font>");
        }
        h.append("</td>");

        // Value cell
        h.append("<td align='right' valign='middle'>")
         .append("<font style='").append(valueStyle).append("'>")
         .append(e.value)
         .append("</font></td>");

        h.append("</tr>");
    }

    // ── Animation ─────────────────────────────────────────────────────────────

    private void startShowAnimation() {
        stopAnimation();
        currentOpacity = 0f;
        float target   = targetOpacity();
        animationTimer = new Timer(15, e -> {
            currentOpacity = Math.min(1f, currentOpacity + 0.12f);
            window.setOpacity(currentOpacity * target);
            if (currentOpacity >= 1f) animationTimer.stop();
        });
        window.setVisible(true);
        animationTimer.start();
    }

    private void startHideAnimation() {
        stopAnimation();
        float target = targetOpacity();
        animationTimer = new Timer(15, e -> {
            currentOpacity = Math.max(0f, currentOpacity - 0.18f);
            window.setOpacity(currentOpacity * target);
            if (currentOpacity <= 0f) {
                window.setVisible(false);
                isExpanded = false;
                animationTimer.stop();
            }
        });
        animationTimer.start();
    }

    private void animateWindowResize(Dimension from, Dimension to) {
        final long      start     = System.currentTimeMillis();
        final int       duration  = 180;
        final Dimension startSize = new Dimension(from);
        final Dimension endSize   = new Dimension(to);
        new Timer(16, e -> {
            float p = (System.currentTimeMillis() - start) / (float) duration;
            if (p >= 1f) {
                window.setSize(endSize);
                positionWindow();
                ((Timer) e.getSource()).stop();
            } else {
                // Ease-out quad
                p = 1f - (1f - p) * (1f - p);
                window.setSize(
                        (int) (startSize.width  + (endSize.width  - startSize.width)  * p),
                        (int) (startSize.height + (endSize.height - startSize.height) * p)
                );
                positionWindow();
            }
        }).start();
    }

    private void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();
    }

    private float targetOpacity() {
        return 1.0f - (config.transparency / 100f);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String hex(Color c) {
        if (c == null) return "#888888";
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static Color uiColor(String key, Color fallback) {
        Color c = UIManager.getColor(key);
        return c != null ? c : fallback;
    }

    private static String trendIconColor(DolatipEntry.Trend t) {
        return switch (t) {
            case UP, OK   -> "#4CAF50";
            case DOWN     -> "#F44336";
            case WARN     -> "#FF9800";
            case ERROR    -> "#F44336";
            case INFO     -> "#2196F3";
            default       -> "#9E9E9E";
        };
    }

    private Color resolvedBg()       { return config.backgroundColor; }
    private Color resolvedHeaderBg() { return config.headerBackground; }
    private Color resolvedHeaderFg() { return config.headerTextColor;  }

    private static void removeExistingListeners(JComponent c) {
        for (MouseListener l : c.getMouseListeners()) {
            if (l instanceof DolatipMouseAdapter) c.removeMouseListener(l);
        }
    }

    // ── Config ────────────────────────────────────────────────────────────────

    /**
     * Fluent configuration for a Dolatip tooltip instance.
     *
     * Start from a {@link DolatipUsage} preset for sensible defaults:
     * <pre>
     * Dolatip.Config cfg = DolatipUsage.FINANCIAL.toConfig("Price History")
     *     .maxAfterClick(10)
     *     .offset(12, -4);
     * </pre>
     */
    public static final class Config {

        static final int DEFAULT_WIDTH = 180;

        final String title;
        Color  backgroundColor = new Color(40, 40, 40, 200);
        Color  headerBackground = new Color(50, 50, 50);
        Color  headerTextColor  = Color.WHITE;
        Color  textColor        = new Color(200, 200, 200);
        Color  highlightColor   = Color.WHITE;
        int    width            = DEFAULT_WIDTH;
        int    maxBeforeClick   = 2;
        int    maxAfterClick    = 8;
        int    cornerRadius     = 12;
        int    offsetX          = 10;
        int    offsetY          = 0;
        int    transparency     = 0;

        public Config(String title) { this.title = title; }

        public Config transparency(int p)         { this.transparency     = p;      return this; }
        public Config width(int w)                { this.width            = w;      return this; }
        public Config maxBeforeClick(int m)       { this.maxBeforeClick   = m;      return this; }
        public Config maxAfterClick(int m)        { this.maxAfterClick    = m;      return this; }
        public Config offset(int x, int y)        { this.offsetX = x; this.offsetY = y; return this; }
        public Config backgroundColor(Color c)    { this.backgroundColor  = c;      return this; }
        public Config textColor(Color c)          { this.textColor        = c;      return this; }
        public Config highlightColor(Color c)     { this.highlightColor   = c;      return this; }
        public Config headerBackground(Color c)   { this.headerBackground = c;      return this; }
        public Config headerTextColor(Color c)    { this.headerTextColor  = c;      return this; }
        public Config cornerRadius(int r)         { this.cornerRadius     = r;      return this; }
        public Config font(Font font)             { return this; } // reserved for future
    }

    // ── Mouse adapters ────────────────────────────────────────────────────────

    private static final class DolatipMouseAdapter extends MouseAdapter {
        private final DolatipPayload payload;
        private final Config         config;

        DolatipMouseAdapter(DolatipPayload payload, Config config) {
            this.payload = payload;
            this.config  = config;
        }

        @Override public void mouseEntered(MouseEvent e) {
            Dolatip.getInstance(config).show((Component) e.getSource(), payload);
        }

        @Override public void mouseExited(MouseEvent e) {
            Dolatip.getInstance(config).hide();
        }
    }

    private static final class ContentMouseAdapter extends MouseAdapter {
        private final Dolatip owner;

        ContentMouseAdapter(Dolatip owner) { this.owner = owner; }

        @Override public void mouseEntered(MouseEvent e) {
            // Keep card fully opaque while the cursor is on it
            owner.window.setOpacity(owner.targetOpacity());
        }

        @Override public void mouseExited(MouseEvent e) {
            owner.hide();
        }

        @Override public void mouseClicked(MouseEvent e) {
            owner.toggleExpanded();
        }
    }
}
