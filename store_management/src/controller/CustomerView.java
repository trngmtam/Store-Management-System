import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CustomerView extends JPanel {
    private MainFrame mainFrame;
    private int customerID;

    public CustomerView(MainFrame mainFrame, int customerID) {
        this.mainFrame = mainFrame;
        this.customerID = customerID;

        // Set layout to GridBagLayout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // JLabel setup
        JLabel welcomeLabel = new JLabel("Welcome to the Customer Portal!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Set constraints for welcome label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        add(welcomeLabel, gbc);

        // View Products button
        JButton viewProductsButton = new JButton("View Products");
        viewProductsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openProductView();
            }
        });

        // Set constraints for View Products button
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        add(viewProductsButton, gbc);

        // View Order button
        JButton viewOrderButton = new JButton("View Order");
        viewOrderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openOrderView();
            }
        });

        // Set constraints for View Order button
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(viewOrderButton, gbc);

        // Log Out Button
        JButton logOutButton = new JButton("Log Out");
        logOutButton.addActionListener(e -> {
            mainFrame.setContentPane(new HomePagePanel(mainFrame));
            mainFrame.revalidate();
        });

        gbc.gridx = 1;
        gbc.gridy = 3;
        add(logOutButton, gbc);
    }

    private void openProductView() {
        mainFrame.setContentPane(new CustomerProductView(mainFrame, customerID));
        mainFrame.revalidate();
    }

    private void openOrderView() {
        mainFrame.setContentPane(new CustomerOrderView(mainFrame, customerID));
        mainFrame.revalidate();
    }
}
