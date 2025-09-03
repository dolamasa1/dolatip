
package main.dolatip;



import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Demo extends JFrame {
    private JLabel demoLabel;
    private List<Double> sampleData;

    public Demo() {
        // Set up FlatDarkLaf
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatDarkLaf");
        }

        // Create sample data
        sampleData = new ArrayList<>();
        sampleData.add(4200.0);
        sampleData.add(3800.0);
        sampleData.add(4500.0);
        sampleData.add(4100.0);
        sampleData.add(4700.0);
        sampleData.add(4300.0);

        // Configure the tooltip
        Dolatip.Config config = new Dolatip.Config("Transaction History")
                .maxBeforeClick(3)
                .maxAfterClick(6)
                .backgroundColor(new Color(40, 40, 40, 230))
                .width(180)
                .padding(8, 12, 8, 12)
                .cornerRadius(8)
                .offset(10, 0)
                .font(new Font("Segoe UI", Font.PLAIN, 12));

        // Create UI
        setTitle("Dolatip Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBackground(new Color(30, 30, 30));

        demoLabel = new JLabel("Hover over me to see the tooltip");
        demoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        demoLabel.setForeground(Color.WHITE);
        demoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        demoLabel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        // Add hover effect
        demoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                demoLabel.setForeground(new Color(100, 180, 255));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                demoLabel.setForeground(Color.WHITE);
            }
        });

        contentPane.add(demoLabel, BorderLayout.CENTER);
        setContentPane(contentPane);

        // Attach the tooltip
        Dolatip.attachTo(demoLabel, sampleData, config);

        // Add instruction label
        JLabel instruction = new JLabel("Right-click to expand/collapse the tooltip");
        instruction.setHorizontalAlignment(SwingConstants.CENTER);
        instruction.setForeground(Color.GRAY);
        instruction.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instruction.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(instruction, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Demo().setVisible(true);
        });
    }
}