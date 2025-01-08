import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import javax.swing.table.TableCellRenderer;

public class AdminCustomerPanel extends JPanel {

    private JTextField searchField;
    private JButton addButton, searchButton, viewAllButton;
    private JTable customerTable;
    private DefaultTableModel tableModel;

    private CustomerDAO customerDAO;
    private AccountDAO accountDAO;
    private MainFrame mainFrame;

    public AdminCustomerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.customerDAO = new CustomerDAO();
        this.accountDAO = new AccountDAO();

        setLayout(new BorderLayout());

        // Search bar and buttons
        JPanel topPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(15);
        addButton = new JButton("Add");
        searchButton = new JButton("Search");
        viewAllButton = new JButton("View All");

        topPanel.add(new JLabel("Search by ID/Name:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(viewAllButton);
        topPanel.add(addButton);
        add(topPanel, BorderLayout.NORTH);

        // Return to admin view
        JButton returnButton = new JButton("Return");
        returnButton.addActionListener(e -> {
            mainFrame.setContentPane(new AdminView(mainFrame));
            mainFrame.revalidate();
        });
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(returnButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Log Out
        JButton logOutButton = new JButton("Log Out");
        bottomPanel.add(logOutButton, BorderLayout.CENTER);
        logOutButton.addActionListener(e -> {
            mainFrame.setContentPane(new HomePagePanel(mainFrame));
            mainFrame.revalidate();
        });

        // Table setup with columns
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Phone", "Address", "Update", "Delete"}, 0);
        customerTable = new JTable(tableModel) {
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5;
            }
        };

        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        // Set column widths
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        customerTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Address

        // Button renderers and editors
        customerTable.getColumn("Update").setCellRenderer(new ButtonRenderer("Update"));
        customerTable.getColumn("Update").setCellEditor(new ButtonEditor(customerTable, "Update"));

        customerTable.getColumn("Delete").setCellRenderer(new ButtonRenderer("Delete"));
        customerTable.getColumn("Delete").setCellEditor(new ButtonEditor(customerTable, "Delete"));

        // Event listeners
        viewAllButton.addActionListener(e -> loadAllCustomers());
        searchButton.addActionListener(e -> searchCustomers());
        addButton.addActionListener(e -> openAddCustomerForm());

        loadAllCustomers();
    }

    private void searchCustomers() {
        String searchText = searchField.getText().trim();
        tableModel.setRowCount(0); // Clear existing rows
        List<Customer> customers;

        if (searchText.matches("\\d+")) { // If it's an ID
            Customer customer = customerDAO.getCustomerByID(Integer.parseInt(searchText));
            if (customer != null) {
                customers = List.of(customer);
            } else {
                customers = List.of();
            }
        } else {
            customers = customerDAO.searchCustomerByName(searchText);
        }

        for (Customer customer : customers) {
            addCustomerRow(customer);
        }
    }

    private void loadAllCustomers() {
        tableModel.setRowCount(0); // Clear existing rows
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer customer : customers) {
            addCustomerRow(customer);
        }
    }

    private void addCustomerRow(Customer customer) {
        tableModel.addRow(new Object[]{
                customer.getCustomerID(),
                customer.getCustomerName(),
                customer.getCustomerPhone(),
                customer.getCustomerAddress(),
                "Update",
                "Delete"
        });
    }

    private void openAddCustomerForm() {
        JDialog addDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Customer", true);
        addDialog.setSize(500, 400);
        addDialog.setLayout(new GridLayout(5, 2));

        // Fields for customer info
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField addressField = new JTextField();

        addDialog.add(new JLabel("ID:"));
        addDialog.add(idField);
        addDialog.add(new JLabel("Name:"));
        addDialog.add(nameField);
        addDialog.add(new JLabel("Phone:"));
        addDialog.add(phoneField);
        addDialog.add(new JLabel("Address:"));
        addDialog.add(addressField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            if (idField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty() || addressField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (phoneField.getText().trim().length() != 10) {
                JOptionPane.showMessageDialog(this, "Please enter a valid phone number", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Customer newCustomer = new Customer(
                    Integer.parseInt(idField.getText().trim()),
                    nameField.getText().trim(),
                    phoneField.getText().trim(),
                    addressField.getText().trim()
            );

            if (customerDAO.addCustomer(newCustomer)) {
                JOptionPane.showMessageDialog(this, "Customer added successfully!");
                loadAllCustomers();
                addDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add customer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addDialog.add(submitButton, BorderLayout.CENTER);
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
    }

    // Renderer for buttons
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String label) {
            setText(label);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Editor for buttons with actions
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private int row;
        private JTable table;

        public ButtonEditor(JTable table, String label) {
            super(new JCheckBox());
            this.table = table;
            this.label = label;
            button = new JButton(label);
            button.setOpaque(true);

            button.addActionListener(e -> {
                fireEditingStopped();
                if ("Update".equals(label)) {
                    openUpdateForm(row);
                } else if ("Delete".equals(label)) {
                    deleteCustomer(row);
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            button.setText(label);
            return button;
        }

        public Object getCellEditorValue() {
            return label; // Keep the label consistent
        }

        private void openUpdateForm(int row) {
            int customerId = (int) table.getValueAt(row, 0);
            Customer customer = customerDAO.getCustomerByID(customerId);

            if (customer == null) {
                JOptionPane.showMessageDialog(null, "Customer not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog updateDialog = new JDialog((Frame) null, "Update Customer", true);
            updateDialog.setSize(500, 400);
            updateDialog.setLayout(new GridLayout(5, 2));

            updateDialog.add(new JLabel("ID:"));
            JTextField idField = new JTextField(String.valueOf(customer.getCustomerID()));
            idField.setEditable(false);
            updateDialog.add(idField);

            updateDialog.add(new JLabel("Name:"));
            JTextField nameField = new JTextField(customer.getCustomerName());
            updateDialog.add(nameField);

            updateDialog.add(new JLabel("Phone:"));
            JTextField phoneField = new JTextField(customer.getCustomerPhone());
            updateDialog.add(phoneField);

            updateDialog.add(new JLabel("Address:"));
            JTextField addressField = new JTextField(customer.getCustomerAddress());
            updateDialog.add(addressField);

            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(e -> {
                customer.setCustomerName(nameField.getText().trim());
                customer.setCustomerPhone(phoneField.getText().trim());
                customer.setCustomerAddress(addressField.getText().trim());

                if (customerDAO.updateCustomer(customer)) {
                    updateDialog.dispose();
                    JOptionPane.showMessageDialog(null, "Customer updated successfully!");
                    loadAllCustomers();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to update customer.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            updateDialog.add(submitButton);
            updateDialog.setLocationRelativeTo(null);
            updateDialog.setVisible(true);
        }

        private void deleteCustomer(int row) {
            int customerId = (int) table.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this customer?", "Delete Confirmation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean accountDeleted = false;

                // Check if the account exists and try to delete it
                if (accountDAO.accountExists(customerId)) {
                    accountDeleted = accountDAO.deleteAccount(customerId);
                } else {
                    accountDeleted = true; // No account exists, so treat as already deleted
                }

                // Delete the customer if the account is deleted successfully or if no account existed
                if (accountDeleted && customerDAO.deleteCustomer(customerId)) {
                    JOptionPane.showMessageDialog(null, "Customer deleted successfully.");
                    loadAllCustomers();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to delete customer.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    }
}
