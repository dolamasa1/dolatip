package UI.components.Assets.tooltips.dolatip;

import java.awt.Color;
import java.awt.Font;

/**
 * Visual preset that controls fonts, colors, and default sizing for a Dolatip tooltip.
 *
 * Pass into {@link DolatipPayload.Builder#usage(DolatipUsage)} or as the default
 * in {@link Dolatip.Config}.
 *
 * <pre>
 * DolatipPayload payload = DolatipPayload.builder()
 *     .usage(DolatipUsage.FINANCIAL)
 *     .upper(DolatipEntry.ofTrend("Close", 142.50, 139.00))
 *     .build();
 * </pre>
 *
 * Each preset exposes:
 *  - body / small font sizes for upper vs lower sections
 *  - background and header colors
 *  - primary / secondary text colors
 *  - separator line color
 *  - default tooltip width
 */
public enum DolatipUsage {

    /**
     * General-purpose — neutral dark card, matches most UIs.
     */
    DEFAULT(
            13, 11, 180,
            new Color(40, 40, 40, 220),
            new Color(55, 55, 55),
            Color.WHITE,
            new Color(210, 210, 210),
            new Color(150, 150, 150),
            new Color(80, 80, 80)
    ),

    /**
     * Financial data — slightly wider, warmer dark card,
     * emphasises the leading value with a brighter white.
     */
    FINANCIAL(
            14, 11, 210,
            new Color(28, 34, 46, 225),
            new Color(38, 46, 64),
            new Color(245, 245, 250),
            new Color(185, 198, 220),
            new Color(130, 148, 175),
            new Color(70, 82, 105)
    ),

    /**
     * Metrics / analytics — cool blue-grey tones,
     * well-suited for dashboards and monitoring panels.
     */
    METRIC(
            13, 11, 190,
            new Color(25, 38, 56, 220),
            new Color(34, 52, 78),
            new Color(220, 232, 248),
            new Color(160, 185, 215),
            new Color(110, 140, 175),
            new Color(60, 85, 115)
    ),

    /**
     * Status / health indicators — neutral card designed to
     * let the colored dots dominate the visual hierarchy.
     */
    STATUS(
            13, 11, 185,
            new Color(32, 32, 36, 218),
            new Color(46, 46, 52),
            new Color(240, 240, 245),
            new Color(195, 195, 200),
            new Color(140, 140, 148),
            new Color(75, 75, 82)
    ),

    /**
     * Compact / minimal — small fonts, tightest width,
     * for dense UIs where screen real estate is at a premium.
     */
    MINIMAL(
            11, 10, 148,
            new Color(44, 44, 46, 210),
            new Color(56, 56, 58),
            new Color(230, 230, 232),
            new Color(175, 175, 178),
            new Color(125, 125, 128),
            new Color(70, 70, 73)
    ),

    /**
     * Light theme — suitable for apps that use a light Look &amp; Feel.
     * Overrides the global {@code UIManager} dark color keys.
     */
    LIGHT(
            13, 11, 185,
            new Color(250, 250, 252, 235),
            new Color(232, 234, 238),
            new Color(30, 30, 34),
            new Color(60, 62, 68),
            new Color(110, 112, 118),
            new Color(200, 202, 206)
    );

    // ── Fields ────────────────────────────────────────────────────────────────

    /** Font size (px) for the upper section rows. */
    public final int bodyFontSize;

    /** Font size (px) for the lower section rows. */
    public final int smallFontSize;

    /** Default tooltip width in px (Config.width default when using this usage). */
    public final int defaultWidth;

    /** Tooltip card background color. */
    public final Color backgroundColor;

    /** Header bar background color. */
    public final Color headerBackground;

    /** Header title text color. */
    public final Color headerTextColor;

    /** Upper-section primary text color (first row / highlight). */
    public final Color primaryTextColor;

    /** Upper-section secondary text color (remaining rows). */
    public final Color secondaryTextColor;

    /** Lower-section text color (smaller, de-emphasised). */
    public final Color lowerTextColor;

    // ── Constructor ───────────────────────────────────────────────────────────

    DolatipUsage(int bodyFontSize, int smallFontSize, int defaultWidth,
                 Color backgroundColor, Color headerBackground,
                 Color headerTextColor, Color primaryTextColor,
                 Color secondaryTextColor, Color lowerTextColor) {
        this.bodyFontSize      = bodyFontSize;
        this.smallFontSize     = smallFontSize;
        this.defaultWidth      = defaultWidth;
        this.backgroundColor   = backgroundColor;
        this.headerBackground  = headerBackground;
        this.headerTextColor   = headerTextColor;
        this.primaryTextColor  = primaryTextColor;
        this.secondaryTextColor = secondaryTextColor;
        this.lowerTextColor    = lowerTextColor;
    }

    // ── Derived helpers ───────────────────────────────────────────────────────

    /** Separator line color (derived from lowerTextColor at ~40% opacity). */
    public Color separatorColor() {
        return new Color(
                lowerTextColor.getRed(),
                lowerTextColor.getGreen(),
                lowerTextColor.getBlue(),
                100   // ~40% on a dark bg
        );
    }

    /**
     * Returns a {@link Dolatip.Config} pre-populated with this usage's visual settings.
     * Further customisation is possible via chained builder calls.
     *
     * <pre>
     * Dolatip.Config cfg = DolatipUsage.FINANCIAL.toConfig("Price History")
     *     .maxBeforeClick(3)
     *     .maxAfterClick(10);
     * </pre>
     */
    public Dolatip.Config toConfig(String title) {
        return new Dolatip.Config(title)
                .width(defaultWidth)
                .backgroundColor(backgroundColor)
                .headerBackground(headerBackground)
                .headerTextColor(headerTextColor)
                .highlightColor(primaryTextColor)
                .textColor(secondaryTextColor);
    }
}
