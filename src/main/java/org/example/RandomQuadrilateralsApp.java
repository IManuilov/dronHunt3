package org.example;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

public class RandomQuadrilateralsApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RandomQuadrilateralsApp::createAndShowUi);
    }

    private static void createAndShowUi() {
        JFrame frame = new JFrame("Random Rectangles");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new ArtPanel(1000, 700, 2500));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static class ArtPanel extends JPanel {
        private final int shapeCount;
        private final Random random = new Random();

        private ArtPanel(int width, int height, int shapeCount) {
            this.shapeCount = shapeCount;
            setPreferredSize(new Dimension(width, height));
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int panelWidth = getWidth();
            int panelHeight = getHeight();

            for (int i = 0; i < shapeCount; i++) {
                int width = 5 + random.nextInt(30);
                int height = 5 + random.nextInt(30);

                int x = random.nextInt(Math.max(1, panelWidth - width + 1));
                int y = random.nextInt(Math.max(1, panelHeight - height + 1));

                Color color = new Color(
                        random.nextInt(256),
                        random.nextInt(256),
                        random.nextInt(256),
                        110 + random.nextInt(146)
                );

                g2.setColor(color);
                g2.fillRect(x, y, width, height);
            }
        }
    }
}
