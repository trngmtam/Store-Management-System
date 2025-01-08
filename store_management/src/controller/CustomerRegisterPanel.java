import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CustomerRegisterPanel extends JPanel {
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField addressField;
    private JPasswordField passwordField;

    private int currentCustomerID;

    private CustomerDAO customerDAO;
    private AccountDAO accountDAO;

    public CustomerRegisterPanel(JFrame frame) {
        this.accountDAO = new AccountDAO();
        this.customerDAO = new CustomerDAO();
        this.currentCustomerID = customerDAO.generateCustomerID();

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Register New Customer");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Name:"), gbc);

        nameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Phone Number:"), gbc);

        phoneField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Address:"), gbc);

        addressField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 3;
        add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 4;
        add(passwordField, gbc);

        JButton registerButton = new JButton("Register");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        add(registerButton, gbc);

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (nameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty() || addressField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Add new customer
                Customer newCustomer = new Customer(
                        currentCustomerID,
                        nameField.getText().trim(),
                        phoneField.getText().trim(),
                        addressField.getText().trim()
                );
                // Add new account
                Account newAccount = new Account(
                        currentCustomerID,
                        passwordField.getText().trim()
                );

                if (customerDAO.addCustomer(newCustomer)) {
                    if (accountDAO.addAccount(newAccount)) {
                        JOptionPane.showMessageDialog(null, "Registration successful! Your ID is " + currentCustomerID, "Success", JOptionPane.INFORMATION_MESSAGE);
                        frame.setContentPane(new CustomerLoginPanel(frame)); // Navigate back to CustomerLoginPanel
                        frame.revalidate();
                    } else {
                        JOptionPane.showMessageDialog(null, "Registration failed!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Registration failed!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
