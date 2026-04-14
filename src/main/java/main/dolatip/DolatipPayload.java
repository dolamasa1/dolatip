package UI.components.Assets.tooltips.dolatip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Immutable payload for a Dolatip tooltip.
 *
 * A payload has two sections:
 * <ul>
 *   <li><b>upper</b> — always visible once expanded; shown with normal body font.</li>
 *   <li><b>lower</b> — revealed below a separator when the user clicks to expand;
 *       shown with the smaller font from {@link DolatipUsage}.</li>
 * </ul>
 *
 * <pre>
 * // Typical financial ticker
 * DolatipPayload payload = DolatipPayload.builder()
 *     .usage(DolatipUsage.FINANCIAL)
 *     .upper(
 *         DolatipEntry.ofTrend("Close",  142.50, 139.00),
 *         DolatipEntry.ofTrend("Volume", 8_420,  7_900)
 *     )
 *     .lower(
 *         DolatipEntry.ofTrend("Open",   140.10, 141.20),
 *         DolatipEntry.ofTrend("High",   144.80, 143.50),
 *         DolatipEntry.ofTrend("Low",    139.30, 138.00),
 *         DolatipEntry.of("52w High", "$182.94", DolatipEntry.DOT_CYAN),
 *         DolatipEntry.of("52w Low",  "$120.01", DolatipEntry.DOT_PURPLE)
 *     )
 *     .build();
 *
 * // Status monitor
 * DolatipPayload payload = DolatipPayload.builder()
 *     .usage(DolatipUsage.STATUS)
 *     .upper(
 *         DolatipEntry.status("API",      "OK",       Trend.OK),
 *         DolatipEntry.status("Database", "OK",       Trend.OK)
 *     )
 *     .lower(
 *         DolatipEntry.status("Queue",    "Degraded", Trend.WARN),
 *         DolatipEntry.status("Cache",    "OK",       Trend.OK),
 *         DolatipEntry.status("Auth",     "Error",    Trend.ERROR)
 *     )
 *     .build();
 *
 * // Legacy numeric list (backwards-compatible)
 * DolatipPayload legacy = DolatipPayload.fromTrends(myDoubleList);
 * </pre>
 */
public final class DolatipPayload {

    // ── Fields ────────────────────────────────────────────────────────────────

    /** Entries shown in the upper section (always visible once tooltip is open). */
    public final List<DolatipEntry> upper;

    /** Entries shown in the lower section below the separator (revealed on click). */
    public final List<DolatipEntry> lower;

    /** Visual preset driving fonts, colors, and default width. */
    public final DolatipUsage usage;

    // ── Constructor ───────────────────────────────────────────────────────────

    private DolatipPayload(Builder b) {
        this.upper = Collections.unmodifiableList(new ArrayList<>(b.upper));
        this.lower = Collections.unmodifiableList(new ArrayList<>(b.lower));
        this.usage = b.usage != null ? b.usage : DolatipUsage.DEFAULT;
    }

    // ── Convenience queries ───────────────────────────────────────────────────

    /** Returns {@code true} when there are any lower-section entries. */
    public boolean hasLower() {
        return !lower.isEmpty();
    }

    /** Returns {@code true} when the payload is completely empty. */
    public boolean isEmpty() {
        return upper.isEmpty() && lower.isEmpty();
    }

    // ── Static factories ──────────────────────────────────────────────────────

    /**
     * Backwards-compatible factory — converts the original {@code List<Double>}
     * trend data into a payload. The first value is treated as the most recent.
     * Up to {@code maxUpper} entries go into the upper section; the rest go lower.
     */
    public static DolatipPayload fromTrends(List<Double> data, int maxUpper) {
        if (data == null || data.isEmpty()) return empty();
        Builder b = new Builder().usage(DolatipUsage.DEFAULT);

        for (int i = 0; i < data.size(); i++) {
            double prev    = i > 0 ? data.get(i - 1) : data.get(i);
            DolatipEntry e = DolatipEntry.ofTrend("", data.get(i), prev);
            if (i < maxUpper) b.upper(e);
            else              b.lower(e);
        }
        return b.build();
    }

    /**
     * Backwards-compatible factory using the original defaults
     * (2 upper, rest lower).
     */
    public static DolatipPayload fromTrends(List<Double> data) {
        return fromTrends(data, 2);
    }

    /** Returns an empty payload (tooltip will not be shown for this). */
    public static DolatipPayload empty() {
        return new Builder().build();
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    /** Returns a new {@link Builder}. */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<DolatipEntry> upper = new ArrayList<>();
        private final List<DolatipEntry> lower = new ArrayList<>();
        private DolatipUsage usage = DolatipUsage.DEFAULT;

        /**
         * Append one or more entries to the upper section.
         *
         * <pre>
         * .upper(
         *     DolatipEntry.ofTrend("Price", 142.5, 139.0),
         *     DolatipEntry.of("Ticker", "AAPL", DolatipEntry.DOT_CYAN)
         * )
         * </pre>
         */
        public Builder upper(DolatipEntry... entries) {
            upper.addAll(Arrays.asList(entries));
            return this;
        }

        /** Append a list of entries to the upper section. */
        public Builder upper(List<DolatipEntry> entries) {
            upper.addAll(entries);
            return this;
        }

        /**
         * Append one or more entries to the lower section (shown after separator,
         * revealed on click).
         */
        public Builder lower(DolatipEntry... entries) {
            lower.addAll(Arrays.asList(entries));
            return this;
        }

        /** Append a list of entries to the lower section. */
        public Builder lower(List<DolatipEntry> entries) {
            lower.addAll(entries);
            return this;
        }

        /**
         * Visual preset for this payload's tooltip.
         * Defaults to {@link DolatipUsage#DEFAULT} when not called.
         */
        public Builder usage(DolatipUsage u) {
            this.usage = u;
            return this;
        }

        public DolatipPayload build() {
            return new DolatipPayload(this);
        }
    }
}
