package main.dolatip;

import UI.components.Assets.tooltips.dolatip.Dolatip;
import UI.components.Assets.tooltips.dolatip.DolatipEntry;
import UI.components.Assets.tooltips.dolatip.DolatipPayload;
import UI.components.Assets.tooltips.dolatip.DolatipUsage;
import UI.components.Assets.tooltips.dolatip.DolatipEntry.Trend;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Comprehensive demo showcasing the Dolatip tooltip component.
 * Demonstrates:
 * - Different usage presets (FINANCIAL, STATUS, METRIC)
 * - Upper (always visible) and lower (expandable) sections
 * - Trend indicators, status icons, custom colors
 * - Click-to-expand behaviour on the tooltip itself
 * - Smooth animations and theming
 */
public class Demo extends JFrame {

    public Demo() {
        // Apply FlatLaf dark theme for a modern look
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatDarkLaf: " + ex.getMessage());
        }

        setTitle("Dolatip Demo — Advanced Tooltip Component");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 300));

        // Main content pane with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(UIManager.getColor("Panel.background"));

        // Title
        JLabel titleLabel = new JLabel("Hover any card below to see the tooltip", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Card panel with three interactive demo cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);

        cardsPanel.add(createFinancialCard());
        cardsPanel.add(createStatusCard());
        cardsPanel.add(createMetricCard());

        mainPanel.add(cardsPanel, BorderLayout.CENTER);

        // Instructions
        JLabel instructions = new JLabel(
                "<html><center>💡 <b>How to use:</b> Hover over a card → tooltip appears.<br>" +
                "Click <b>inside the tooltip</b> to expand/collapse the lower section.<br>" +
                "Move mouse away to dismiss.</center></html>",
                SwingConstants.CENTER
        );
        instructions.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instructions.setForeground(UIManager.getColor("Label.disabledForeground"));
        instructions.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        mainPanel.add(instructions, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Creates a card for FINANCIAL usage demo.
     * Shows stock-like data with trend indicators and expanded details.
     */
    private JPanel createFinancialCard() {
        JPanel card = createCardBase("💰 FINANCIAL", DolatipUsage.FINANCIAL);

        JLabel label = new JLabel("AAPL · After Hours", SwingConstants.CENTER);
        styleLabel(label, true);
        card.add(label, BorderLayout.CENTER);

        // Build payload with upper (always visible) and lower (expandable) sections
        DolatipPayload payload = DolatipPayload.builder()
                .usage(DolatipUsage.FINANCIAL)
                .upper(
                        DolatipEntry.ofTrend("Close", 175.32, 172.85),
                        DolatipEntry.ofTrend("Volume", 42_500_000, 38_200_000)
                )
                .lower(
                        DolatipEntry.ofTrend("Open", 173.10, 172.85),
                        DolatipEntry.ofTrend("High", 176.48, 174.20),
                        DolatipEntry.ofTrend("Low", 172.90, 171.50),
                        DolatipEntry.of("52w Range", "124.17 – 198.23", DolatipEntry.DOT_CYAN)
                )
                .build();

        Dolatip.Config config = DolatipUsage.FINANCIAL.toConfig("AAPL (NASDAQ)")
                .offset(15, 5)
                .cornerRadius(12);

        Dolatip.attachTo(label, payload, config);
        return card;
    }

    /**
     * Creates a card for STATUS usage demo.
     * Shows service health checks with status icons (✓, ⚠, ✗).
     */
    private JPanel createStatusCard() {
        JPanel card = createCardBase("🔌 STATUS", DolatipUsage.STATUS);

        JLabel label = new JLabel("System Health", SwingConstants.CENTER);
        styleLabel(label, true);
        card.add(label, BorderLayout.CENTER);

        DolatipPayload payload = DolatipPayload.builder()
                .usage(DolatipUsage.STATUS)
                .upper(
                        DolatipEntry.status("API Gateway", "Operational", Trend.OK),
                        DolatipEntry.status("Database", "Replica lag", Trend.WARN)
                )
                .lower(
                        DolatipEntry.status("Message Queue", "Degraded", Trend.WARN),
                        DolatipEntry.status("Cache Cluster", "Healthy", Trend.OK),
                        DolatipEntry.status("Auth Service", "Timeout", Trend.ERROR),
                        DolatipEntry.header("── Recent Incidents ──"),
                        DolatipEntry.of("12:34 UTC", "High latency", Trend.WARN)
                )
                .build();

        Dolatip.Config config = DolatipUsage.STATUS.toConfig("Service Dashboard")
                .offset(15, 5)
                .cornerRadius(10);

        Dolatip.attachTo(label, payload, config);
        return card;
    }

    /**
     * Creates a card for METRIC usage demo.
     * Shows system metrics with trends and additional lower-section data.
     */
    private JPanel createMetricCard() {
        JPanel card = createCardBase("📊 METRIC", DolatipUsage.METRIC);

        JLabel label = new JLabel("System Load", SwingConstants.CENTER);
        styleLabel(label, true);
        card.add(label, BorderLayout.CENTER);

        DolatipPayload payload = DolatipPayload.builder()
                .usage(DolatipUsage.METRIC)
                .upper(
                        DolatipEntry.ofTrend("CPU", 68.2, 54.7),
                        DolatipEntry.ofTrend("Memory", 4.2, 3.8)
                )
                .lower(
                        DolatipEntry.ofTrend("Disk I/O", 142, 128),
                        DolatipEntry.of("Network In", "12.4 Mbps", DolatipEntry.DOT_BLUE),
                        DolatipEntry.of("Network Out", "8.2 Mbps", DolatipEntry.DOT_CYAN),
                        DolatipEntry.of("Temperature", "58°C", DolatipEntry.DOT_AMBER)
                )
                .build();

        Dolatip.Config config = DolatipUsage.METRIC.toConfig("Performance Metrics")
                .offset(15, 5)
                .cornerRadius(10);

        Dolatip.attachTo(label, payload, config);
        return card;
    }

    // ------------------------- Helper methods -------------------------

    /**
     * Creates a styled card panel with a title and hover effects.
     */
    private JPanel createCardBase(String title, DolatipUsage usage) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(usage.backgroundColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(usage.separatorColor(), 1, true),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(usage.headerTextColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        card.add(titleLabel, BorderLayout.NORTH);

        return card;
    }

    /**
     * Styles an interactive label with hover color change.
     */
    private void styleLabel(JLabel label, boolean interactive) {
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setForeground(Color.WHITE);
        label.setOpaque(false);
        if (interactive) {
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    label.setForeground(new Color(100, 180, 255));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    label.setForeground(Color.WHITE);
                }
            });
        }
    }

    // -----------------------------------------------------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Demo().setVisible(true);
        });
    }
}