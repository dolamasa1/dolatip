
package main.dolatip;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

public class Dolatip {
    private static final NumberFormat NF = NumberFormat.getInstance();
    private static final int DEFAULT_ANIMATION_DURATION = 300;
    
    private final JWindow window;
    private final Config config;
    private Timer animationTimer;
    private float currentOpacity;
    private List<Double> currentData;
    private boolean isExpanded = false;
    private Component parentComponent;

    private Dolatip(Config config) {
        this.config = config;
        this.window = new JWindow();
        initializeWindow();
    }

private void initializeWindow() {
    window.setBackground(new Color(0, 0, 0, 0));
    window.setFocusableWindowState(false);
    window.setAlwaysOnTop(true);
    
    // Add type hint for Java 7+ compatibility
    window.setType(Window.Type.POPUP);
}

private void positionWindow() {
    Point parentLocation = parentComponent.getLocationOnScreen();
    int x = parentLocation.x + parentComponent.getWidth() + config.offsetX;
    int y = parentLocation.y + config.offsetY;
    
    // Ensure tooltip stays within screen bounds
    Rectangle screenBounds = window.getGraphicsConfiguration().getBounds();
    int maxX = screenBounds.x + screenBounds.width - window.getWidth();
    int maxY = screenBounds.y + screenBounds.height - window.getHeight();
    
    window.setLocation(
        Math.min(x, maxX),
        Math.min(y, maxY)
    );
}



// In the Dolatip class, update the attachTo method
public static void attachTo(JComponent component, List<Double> data, Config config) {
    // Remove existing DolatipMouseAdapter listeners
    MouseListener[] listeners = component.getMouseListeners();
    for (MouseListener listener : listeners) {
        if (listener instanceof DolatipMouseAdapter) {
            component.removeMouseListener(listener);
        }
    }
    Dolatip tooltip = new Dolatip(config);
    component.addMouseListener(new DolatipMouseAdapter(tooltip, data));
}

public void toggleExpanded() {
        if (currentData.size() > config.maxBeforeClick) {
            isExpanded = !isExpanded;
            updateContent();
            window.pack();
            animateWindowResize(window.getSize(), window.getPreferredSize());
            positionWindow();
        }
    }

// Ensure the window is reinitialized if needed when shown again
public void show(Component parent, List<Double> data) {
    if (data == null || data.isEmpty()) return;
    
    this.currentData = data;
    this.parentComponent = parent;
    
    SwingUtilities.invokeLater(() -> {
        if (window == null || !window.isDisplayable()) {
            initializeWindow(); // Reinitialize if window was disposed
        }
        updateContent();
        positionWindow();
        startShowAnimation();
    });
}

    public void hide() {
        startHideAnimation();
    }
    private void updateContent() {
        JLabel content = new JLabel(createHTMLContent()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow effect
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillRoundRect(1, 1, getWidth(), getHeight(), config.cornerRadius, config.cornerRadius);
                
                // Main background
                g2d.setColor(config.backgroundColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), config.cornerRadius, config.cornerRadius);
                
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        
// Modify the mouseClicked handler in updateContent()
content.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseEntered(MouseEvent e) {
        window.setOpacity(1f); // Changed tooltipWindow to window
    }

    @Override
    public void mouseExited(MouseEvent e) {
        startHideAnimation();
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (currentData.size() > config.maxBeforeClick) {
            Dimension oldSize = window.getSize();
            isExpanded = !isExpanded;
            updateContent();
            window.pack();
            Dimension newSize = window.getSize();
            animateWindowResize(oldSize, newSize);
            positionWindow();
        }
    }
});
        
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        window.setContentPane(content);
        window.pack();
    }

    private String createHTMLContent() {
        int count = isExpanded ? 
            Math.min(config.maxAfterClick, currentData.size()) : 
            Math.min(config.maxBeforeClick, currentData.size());
        
        StringBuilder html = new StringBuilder("<html><body style='width:")
            .append(config.width).append("px;margin:0;padding:0;'>");

        // Header with original gradient
        html.append("<div style='"
            + "background: linear-gradient(to right, #2b2b2b, #404040);"
            + "color: #fff;"
            + "padding: 6px;"
            + "margin: -8px -8px 8px -8px;"
            + "border-radius: 4px 4px 0 0;"
            + "font-weight: 500;"
            + "'>").append(config.title).append("</div>");

        if (!currentData.isEmpty()) {
            html.append("<div style='"
                + "display: flex;"
                + "flex-direction: column;"
                + "gap: 4px;"
                + "max-height: 200px;"
                + "overflow-y: auto;"
                + "'>");

            double previous = !currentData.isEmpty() ? currentData.get(0) : 0;
            for (int i = 0; i < count; i++) {
                double current = currentData.get(i);
                String trendIcon;
                String trendColor;
                String bgColor = (i % 2 == 0) ? "rgba(255,255,255,0.05)" : "transparent";

                if (current > previous) {
                    trendIcon = "▲";
                    trendColor = "#4CAF50";
                } else if (current < previous) {
                    trendIcon = "▼";
                    trendColor = "#F44336";
                } else {
                    trendIcon = "●";
                    trendColor = "#9E9E9E";
                }

                html.append(String.format("<div style='"
                    + "display: flex;"
                    + "justify-content: space-between;"
                    + "align-items: center;"
                    + "padding: 4px 8px;"
                    + "background: %s;"
                    + "border-radius: 3px;"
                    + "'>"
                    + "<span style='color: %s; font-size: 12px;'>%s</span>"
                    + "<span style='color: %s; font-weight: 500;'>%s</span>"
                    + "</div>",
                    bgColor,
                    trendColor, trendIcon,
                    (i == 0) ? "#FFF" : "#BDBDBD",
                    NF.format(current)
                ));

                previous = current;
            }

            html.append("</div>");
            
            // Original footer style
            html.append("<div style='"
                + "margin-top: 8px;"
                + "padding-top: 8px;"
                + "border-top: 1px solid rgba(255,255,255,0.1);"
                + "color: #9E9E9E;"
                + "font-size: 0.9em;"
                + "'>")
                .append("Last ").append(count).append(" transactions")
                .append("</div>");
        } else {
            html.append("<div style='color: #9E9E9E; text-align: center; padding: 12px 0;'>")
                .append("No transaction history")
                .append("</div>");
        }

        return html.append("</body></html>").toString();
    }
   private Point calculateTargetPosition() {
        Point location = parentComponent.getLocationOnScreen();
        return new Point(
            location.x + parentComponent.getWidth() + config.offsetX,
            location.y + config.offsetY
        );
    }

   // Add these new methods to the Dolatip class
private void animateWindowResize(Dimension from, Dimension to) {
    final int duration = 300; // Animation duration in ms
    final long startTime = System.currentTimeMillis();
    final Dimension startSize = new Dimension(from);
    final Dimension targetSize = new Dimension(to);
    final Point startPos = window.getLocation();
    final Point targetPos = calculateTargetPosition();

    new Timer(16, new ActionListener() { // ~60 FPS
        public void actionPerformed(ActionEvent e) {
            float progress = (System.currentTimeMillis() - startTime) / (float)duration;
            progress = Math.min(progress, 1.0f);
            
            // Cubic easing function
            float easedProgress = (float) (Math.cos((progress + 1) * Math.PI) / 2.0f) + 0.5f;
            
            // Animate size
            int currentWidth = (int) (startSize.width + 
                (targetSize.width - startSize.width) * easedProgress);
            int currentHeight = (int) (startSize.height + 
                (targetSize.height - startSize.height) * easedProgress);
            window.setSize(currentWidth, currentHeight);
            
            // Animate position
            int newX = (int) (startPos.x + (targetPos.x - startPos.x) * easedProgress);
            int newY = (int) (startPos.y + (targetPos.y - startPos.y) * easedProgress);
            window.setLocation(newX, newY);

            if (progress >= 1.0f) {
                ((Timer)e.getSource()).stop();
                window.setSize(targetSize);
                window.setLocation(targetPos);
            }
        }
    }).start();
}

    private String getTrendIcon(int index) {
        if (index >= currentData.size() - 1) return "●";
        double current = currentData.get(index);
        double previous = currentData.get(index + 1);
        
        if (current > previous) return "▲";
        if (current < previous) return "▼";
        return "●";
    }



private void startShowAnimation() {
    if (animationTimer != null && animationTimer.isRunning()) return;
    
    currentOpacity = 0f;
    final float targetOpacity = 1.0f - (config.transparency / 100f);
    
    animationTimer = new Timer(15, e -> {
        currentOpacity = Math.min(1f, currentOpacity + 0.05f);
        window.setOpacity(currentOpacity * targetOpacity);
        if (currentOpacity >= 1f) animationTimer.stop();
    });
    window.setVisible(true);
    animationTimer.start();
}

// Update the startHideAnimation method to dispose the window
private void startHideAnimation() {
    if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();
    
    final float targetOpacity = 1.0f - (config.transparency / 100f);
    
    animationTimer = new Timer(15, e -> {
        currentOpacity = Math.max(0f, currentOpacity - 0.08f);
        window.setOpacity(currentOpacity * targetOpacity);
        if (currentOpacity <= 0f) {
            window.setVisible(false);
            window.dispose(); // Dispose the window to free resources
            animationTimer.stop();
        }
    });
    animationTimer.start();
}

    private static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

public static class Config {
    private final String title;
    private Color backgroundColor = new Color(40, 40, 40, 76);
    private Color headerBackground = new Color(43, 43, 43);
    private Color headerTextColor = Color.WHITE;
    private Color textColor = new Color(189, 189, 189);
    private Color highlightColor = Color.WHITE;
    private Color trendColor = new Color(158, 158, 158);
    private Color rowBackground = new Color(255, 255, 255, 25);
    private int width = 160;
    private int maxBeforeClick = 2;
    private int maxAfterClick = 6;
    private int paddingTop = 8;
    private int paddingBottom = 8;
    private int paddingLeft = 12;
    private int paddingRight = 12;
    private int cornerRadius = 8;
    private int trendIconSize = 12;
    private int offsetX = 5;
    private int offsetY = 0;
    private int transparency = 0;
    private Font font = new Font("Segoe UI", Font.PLAIN, 12);

    public Config(String title) {
        this.title = title;
    }

    public Config transparency(int percent) {
        this.transparency = Math.max(0, Math.min(100, percent));
        return this;
    }

    public Config backgroundColor(Color color) { this.backgroundColor = color; return this; }
    public Config headerBackground(Color color) { this.headerBackground = color; return this; }
    public Config headerTextColor(Color color) { this.headerTextColor = color; return this; }
    public Config textColor(Color color) { this.textColor = color; return this; }
    public Config highlightColor(Color color) { this.highlightColor = color; return this; }
    public Config trendColor(Color color) { this.trendColor = color; return this; }
    public Config rowBackground(Color color) { this.rowBackground = color; return this; }
    public Config width(int width) { this.width = width; return this; }
    public Config maxBeforeClick(int max) { this.maxBeforeClick = max; return this; }
    public Config maxAfterClick(int max) { this.maxAfterClick = max; return this; }
    public Config padding(int top, int right, int bottom, int left) {
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        this.paddingLeft = left;
        return this;
    }
    public Config cornerRadius(int radius) { this.cornerRadius = radius; return this; }
    public Config trendIconSize(int size) { this.trendIconSize = size; return this; }
    public Config offset(int x, int y) { this.offsetX = x; this.offsetY = y; return this; }
    public Config font(Font font) { this.font = font; return this; }
}

private static class DolatipMouseAdapter extends MouseAdapter {

   private final Dolatip tooltip;
    private final List<Double> data;
    private Timer clickTimer;
    public DolatipMouseAdapter(Dolatip tooltip, List<Double> data) {
        this.tooltip = tooltip;
        this.data = data;
    }

    @Override
public void mouseEntered(MouseEvent e) {
    Component parent = SwingUtilities.getRootPane((Component) e.getSource());
    if (parent != null) {
        tooltip.window.setLocationRelativeTo(parent);
    }
    tooltip.show((Component) e.getSource(), data);
}

    @Override
    public void mouseExited(MouseEvent e) {
        tooltip.hide();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (clickTimer != null && clickTimer.isRunning()) return;
        
        clickTimer = new Timer(300, evt -> {
            tooltip.toggleExpanded();
            clickTimer.stop();
        });
        clickTimer.setRepeats(false);
        clickTimer.start();
    }
}
}
