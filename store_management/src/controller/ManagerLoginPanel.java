import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ManagerLoginPanel extends JPanel {
    private JTextField emailField;
    private JPasswordField passwordField;

    public ManagerLoginPanel(JFrame frame) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Manager Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Email:"), gbc);

        emailField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(passwordField, gbc);

        JButton loginButton = new JButton("LOG IN");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(loginButton, gbc);

        JButton returnButton = new JButton("Return to Homepage");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(returnButton, gbc);

        // Add login button action listener
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                // Placeholder admin credentials
                if (email.equals("manager@gmail.com") && password.equals("manager")) {
                    JOptionPane.showMessageDialog(frame, "Login successful!");
                    ((MainFrame) frame).showManagerView(); // Navigate to ManagerView
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        returnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(new HomePagePanel(frame));
                frame.revalidate();
            }
        });
    }
}
