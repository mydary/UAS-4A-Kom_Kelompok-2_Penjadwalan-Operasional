import login.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Pastikan tampilan menggunakan gaya bawaan sistem
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Gagal mengatur LookAndFeel.");
        }

        // Jalankan tampilan login
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
