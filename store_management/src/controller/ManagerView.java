import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ManagerView extends JPanel {
// TODO create buttons for view
    public ManagerView(JFrame frame) {
        // Set layout to GridBagLayout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Customer Portal button
        JButton recentCustomerButton = new JButton("View Recent Customers");
        recentCustomerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(new ManagerRecentPanel(frame));
                frame.revalidate();
            }
        });

        // Set constraints for Customer Portal button
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 5);
        add(recentCustomerButton, gbc);

        // Admin Portal button
        JButton viewEarningButton = new JButton("View Monthly Earnings");
        viewEarningButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(new ManagerEarningPanel(frame));
                frame.revalidate();
            }
        });

        // Set constraints for Admin Portal button
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(viewEarningButton, gbc);

        // Manager portal button
        JButton productButton = new JButton("View Best-Selling Products");
        productButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(new ManagerProductPanel(frame));
                frame.revalidate();
            }
        });

        gbc.gridx = 1;
        gbc.gridy = 3;
        add(productButton, gbc);

        // Log Out
        JButton logOutButton = new JButton("Log Out");
        logOutButton.addActionListener(e -> {
            frame.setContentPane(new HomePagePanel(frame));
            frame.revalidate();
        });

        gbc.gridx = 1;
        gbc.gridy = 4;
        add(logOutButton, gbc);

    }
}
