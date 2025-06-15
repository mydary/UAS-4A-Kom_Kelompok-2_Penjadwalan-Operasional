import util.XamppManager;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        XamppManager xamppManager = new XamppManager();
        xamppManager.startXampp();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Gagal mengatur LookAndFeel.");
        }

        SwingUtilities.invokeLater(() -> {
            new LoadingFrame(xamppManager).setVisible(true);
        });
    }
}
