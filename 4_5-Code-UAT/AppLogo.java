import javax.swing.*;
import java.awt.Image;

public class AppLogo  {
    private ImageIcon icon;
    private JLabel imageLabel;
    AppLogo() {
        icon = new ImageIcon("logo.png");
        icon = new ImageIcon(icon.getImage().getScaledInstance(100, 90, Image.SCALE_SMOOTH));
        imageLabel = new JLabel(icon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public JLabel getLabel() {
        return imageLabel;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}