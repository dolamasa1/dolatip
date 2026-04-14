package UI.components.Assets.tooltips.dolatip;

import java.awt.Color;
import java.text.NumberFormat;

/**
 * Immutable data model for a single row in a Dolatip tooltip.
 * Each entry carries its own dot color, optional row text color, and an optional trend icon.
 *
 * Factory shortcuts cover the most common patterns:
 *   DolatipEntry.of("Volume", "12,400", Color.CYAN)
 *   DolatipEntry.ofTrend("Price", 142.5, 139.0)
 *   DolatipEntry.status("API", "OK", Trend.OK)
 */
public final class DolatipEntry {

    // ── Trend enum ────────────────────────────────────────────────────────────

    public enum Trend {
        UP,     // ▲ green
        DOWN,   // ▼ red
        FLAT,   // ● gray
        OK,     // ● green
        WARN,   // ● amber
        ERROR,  // ● red
        INFO,   // ● blue
        NONE    // no icon rendered
    }

    // ── Preset dot colors ─────────────────────────────────────────────────────

    public static final Color DOT_GREEN  = new Color(76,  175, 80);
    public static final Color DOT_RED    = new Color(244, 67,  54);
    public static final Color DOT_AMBER  = new Color(255, 152, 0);
    public static final Color DOT_BLUE   = new Color(33,  150, 243);
    public static final Color DOT_PURPLE = new Color(156, 39,  176);
    public static final Color DOT_CYAN   = new Color(0,   188, 212);
    public static final Color DOT_GRAY   = new Color(158, 158, 158);

    // ── Fields ────────────────────────────────────────────────────────────────

    /** Left-side descriptive label. Empty string = value-only row. */
    public final String label;

    /** Right-aligned value string. Pre-format as needed (currency, %, etc.). */
    public final String value;

    /** Color of the small dot rendered to the left of the label. */
    public final Color dotColor;

    /**
     * Per-row override for the value text color.
     * {@code null} means "use the usage default".
     */
    public final Color rowColor;

    /** Optional trend/status indicator rendered as a small icon before the dot. */
    public final Trend trend;

    // ── Constructor (private – use Builder or factories) ──────────────────────

    private DolatipEntry(Builder b) {
        this.label    = b.label    != null ? b.label    : "";
        this.value    = b.value    != null ? b.value    : "";
        this.dotColor = b.dotColor != null ? b.dotColor : DOT_GRAY;
        this.rowColor = b.rowColor;          // nullable
        this.trend    = b.trend    != null ? b.trend    : Trend.NONE;
    }

    // ── Static factories ──────────────────────────────────────────────────────

    /**
     * Basic entry: label + value + dot color, no trend icon.
     *
     * <pre>DolatipEntry.of("Revenue", "$1,240", DolatipEntry.DOT_GREEN)</pre>
     */
    public static DolatipEntry of(String label, String value, Color dotColor) {
        return new Builder(label, value).dot(dotColor).build();
    }

    /**
     * Convenience overload — uses the given Trend to auto-select dot color.
     *
     * <pre>DolatipEntry.of("Status", "Healthy", Trend.OK)</pre>
     */
    public static DolatipEntry of(String label, String value, Trend trend) {
        return new Builder(label, value).dot(dotForTrend(trend)).trend(trend).build();
    }

    /**
     * Numeric trend row: compares {@code current} vs {@code previous} and picks
     * UP/DOWN/FLAT automatically. Value is formatted with the default NumberFormat.
     *
     * <pre>DolatipEntry.ofTrend("Close", 142.50, 139.00)</pre>
     */
    public static DolatipEntry ofTrend(String label, double current, double previous) {
        Trend t = current > previous ? Trend.UP
                : current < previous ? Trend.DOWN
                : Trend.FLAT;
        return new Builder(label, formatNumber(current))
                .dot(dotForTrend(t))
                .trend(t)
                .build();
    }

    /**
     * Status row – suitable for health checks, service monitors, etc.
     *
     * <pre>DolatipEntry.status("DB", "Connected", Trend.OK)</pre>
     */
    public static DolatipEntry status(String label, String value, Trend status) {
        return new Builder(label, value)
                .dot(dotForTrend(status))
                .trend(status)
                .build();
    }

    /**
     * Plain divider label with no value (dot not shown when label+value are both blank,
     * but you can use this as a sub-header in the lower section by passing a label only).
     *
     * <pre>DolatipEntry.header("Recent")</pre>
     */
    public static DolatipEntry header(String label) {
        return new Builder(label, "").dot(DOT_GRAY).build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** True when this entry carries a visible trend icon (not NONE / FLAT). */
    public boolean hasTrendIcon() {
        return trend != Trend.NONE && trend != Trend.FLAT;
    }

    /** Returns the HTML-safe trend symbol for this entry, or empty string. */
    public String trendSymbol() {
        return switch (trend) {
            case UP    -> "&#9650;";   // ▲
            case DOWN  -> "&#9660;";   // ▼
            case OK    -> "&#10003;";  // ✓
            case WARN  -> "&#9888;";   // ⚠
            case ERROR -> "&#10007;";  // ✗
            case INFO  -> "&#8505;";   // ℹ
            default    -> "";
        };
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    static Color dotForTrend(Trend t) {
        return switch (t) {
            case UP, OK   -> DOT_GREEN;
            case DOWN     -> DOT_RED;
            case WARN     -> DOT_AMBER;
            case ERROR    -> DOT_RED;
            case INFO     -> DOT_BLUE;
            default       -> DOT_GRAY;
        };
    }

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    static String formatNumber(double v) {
        if (Math.abs(v) >= 1_000_000) return String.format("%.2fM", v / 1_000_000);
        if (Math.abs(v) >= 1_000)     return String.format("%.1fK", v / 1_000);
        return NUMBER_FORMAT.format(v);
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {
        private final String label;
        private final String value;
        private Color  dotColor;
        private Color  rowColor;
        private Trend  trend;

        public Builder(String label, String value) {
            this.label = label;
            this.value = value;
        }

        /** Dot color on the left of this row. */
        public Builder dot(Color c)   { this.dotColor = c; return this; }

        /** Per-row value text color override (null = usage default). */
        public Builder color(Color c) { this.rowColor = c; return this; }

        /** Trend/status icon type. */
        public Builder trend(Trend t) { this.trend = t;    return this; }

        public DolatipEntry build()   { return new DolatipEntry(this); }
    }
}
