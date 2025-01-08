import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CustomerLoginPanel extends JPanel {
    private JTextField IDField;
    private JPasswordField passwordField;
    private AccountDAO accountDAO;

    public CustomerLoginPanel(JFrame frame) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Customer Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Customer ID:"), gbc);

        IDField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(IDField, gbc);

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

        JButton registerButton = new JButton("Register");
        gbc.gridy = 4;
        add(registerButton, gbc);

        JButton passwordButton = new JButton("Create Password");
        gbc.gridy = 5;
        add(passwordButton, gbc);

        JButton returnButton = new JButton("Return to Homepage");
        gbc.gridy = 6;
        add(returnButton, gbc);


        // Add login button action listener
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String ID = IDField.getText();
                String password = new String(passwordField.getPassword());
                accountDAO = new AccountDAO();

                // Retrieve the customer record from the database
                int customerID = accountDAO.validateCustomer(ID, password);
                if (customerID != 0) {
                    JOptionPane.showMessageDialog(frame, "Login successful!");
                    ((MainFrame) frame).showCustomerView(customerID); // Navigate to CustomerView with customerID
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid ID or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add register button action listener (for registration form)
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(new CustomerRegisterPanel(frame));
                frame.revalidate();
            }
        });

        passwordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                accountDAO = new AccountDAO();
                JDialog dialog = new JDialog(frame, "Create Account", true);
                dialog.setSize(400,300);
                dialog.setLayout(new GridLayout(5, 2));

                JTextField idField = new JTextField();
                dialog.add(new JLabel("ID:"));
                dialog.add(idField);

                JPasswordField passwordField = new JPasswordField();
                dialog.add(new JLabel("Password:"));
                dialog.add(passwordField);

                JButton submitButton = new JButton("Submit");
                dialog.add(submitButton);
                submitButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (idField.getText().trim().length() == 0 || passwordField.getText().trim().length() == 0) {
                            JOptionPane.showMessageDialog(frame, "Please enter ID and Password.", "Failed", JOptionPane.ERROR_MESSAGE);
                        }

                        Account account = new Account(
                                Integer.parseInt(idField.getText().trim()),
                                passwordField.getText().trim()
                        );

                        if (accountDAO.addAccount(account)) {
                            JOptionPane.showMessageDialog(frame, "Password successfully added!");
                            dialog.dispose();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Failed to add password!", null, JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                dialog.setLocationRelativeTo(frame);
                dialog.setVisible(true);
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
