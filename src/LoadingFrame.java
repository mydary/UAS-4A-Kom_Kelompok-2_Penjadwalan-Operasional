import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import login.LoginFrame;
import util.XamppManager;
public class LoadingFrame extends JFrame {
    private XamppManager xamppManager;

    public LoadingFrame(XamppManager xamppManager) {
        this.xamppManager = xamppManager;
        setUndecorated(true);
        setSize(640, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Load animasi GIF dari resource
        JLabel loadingLabel = new JLabel(new ImageIcon(getClass().getResource("/loading.gif")));
        add(loadingLabel, BorderLayout.CENTER);

        // Timer
        Timer timer = new Timer(7000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LoginFrame(xamppManager).setVisible(true);
                dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

}
