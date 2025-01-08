import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomePagePanel extends JPanel {
    public HomePagePanel(JFrame frame) {
        // Set layout to GridBagLayout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Welcome message setup
        JLabel welcomeLabel = new JLabel("Welcome to the Retail Store Management System");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Set constraints for welcome label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 20, 10); // add padding around the label
        gbc.anchor = GridBagConstraints.CENTER;
        add(welcomeLabel, gbc);

        // Customer Portal button
        JButton customerPortalButton = new JButton("Customer Portal");
        customerPortalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(new CustomerLoginPanel(frame));
                frame.revalidate();
            }
        });

        // Set constraints for Customer Portal button
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 5);
        add(customerPortalButton, gbc);

        // Admin Portal button
        JButton adminPortalButton = new JButton("Admin Portal");
        adminPortalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(new AdminLoginPanel(frame));
                frame.revalidate();
            }
        });

        // Set constraints for Admin Portal button
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(adminPortalButton, gbc);

        // Manager portal button
        JButton managerPortalButton = new JButton("Manager Portal");
        managerPortalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(new ManagerLoginPanel(frame));
                frame.revalidate();
            }
        });

        gbc.gridx = 1;
        gbc.gridy = 3;
        add(managerPortalButton, gbc);
    }
}
