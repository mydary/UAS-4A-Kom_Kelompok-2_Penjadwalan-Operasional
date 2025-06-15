package util;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {
    private int radius;

    public RoundedButton(String label, int radius) {
        super(label);
        this.radius = radius;
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.decode("#be375f")); // pastikan warna teks terlihat
        setFont(new Font("Poppins", Font.BOLD, 12));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Latar belakang rounded
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        // Teks tombol
        FontMetrics fm = g2.getFontMetrics();
        int stringWidth = fm.stringWidth(getText());
        int stringHeight = fm.getAscent();
        g2.setColor(getForeground());
        g2.drawString(
                getText(),
                (getWidth() - stringWidth) / 2,
                (getHeight() + stringHeight) / 2 - 3
        );
        g2.dispose();
    }


    @Override
    public void updateUI() {
        super.updateUI();
    }

    @Override
    protected void paintBorder(Graphics g) {
        // tidak ada border
    }
}
