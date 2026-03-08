package org.example.mapgame;

import javax.swing.SwingUtilities;

public class MapFragmentGameApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow().showWindow());
    }
}
