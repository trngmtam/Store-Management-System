import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminOrderPanel extends JPanel {
    private JTextField searchField;
    private JButton searchButton, viewAllButton;
    private JTable orderTable;
    private DefaultTableModel tableModel;

    private ProductDAO productDAO;
    private InventoryDAO inventoryDAO;
    private OrdersDAO ordersDAO;
    private MainFrame mainFrame;

    public AdminOrderPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.productDAO = new ProductDAO();
        this.inventoryDAO = new InventoryDAO();
        this.ordersDAO = new OrdersDAO();
        setLayout(new BorderLayout());

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

        // Search bar and buttons
        JPanel topPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        viewAllButton = new JButton("View All");

        // Add to panel
        topPanel.add(new JLabel("Search by ID/Name:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(viewAllButton);
        add(topPanel, BorderLayout.NORTH);

        // Add ActionListeners
        searchButton.addActionListener(e -> searchOrder());
        viewAllButton.addActionListener(e -> loadOrders());

        // Set up table
        String[] columnNames = {"Order ID", "Customer ID", "Date", "Total Amount", "Status", "Update", "Delete", "View Details"};
        tableModel = new DefaultTableModel(columnNames, 0);
        orderTable = new JTable(tableModel);
        add(new JScrollPane(orderTable), BorderLayout.CENTER);

        // Button renderers and editors
        orderTable.getColumn("Update").setCellRenderer(new ButtonRenderer("Update"));
        orderTable.getColumn("Update").setCellEditor(new ButtonEditor(orderTable, "Update"));

        orderTable.getColumn("Delete").setCellRenderer(new ButtonRenderer("Delete"));
        orderTable.getColumn("Delete").setCellEditor(new ButtonEditor(orderTable, "Delete"));

        orderTable.getColumn("View Details").setCellRenderer(new ButtonRenderer("View Details"));
        orderTable.getColumn("View Details").setCellEditor(new ButtonEditor(orderTable, "View Details"));

        // Load Orders into table
        loadOrders();
    }

    // Method to search for order by orderID or customerID
    private void searchOrder() {
        String searchText = searchField.getText().trim();
        tableModel.setRowCount(0); // Clear existing rows

        if (searchText.matches("\\d+")) { // Check if the input is numeric
            int searchValue = Integer.parseInt(searchText);

            // First, try searching by orderID
            Orders order = ordersDAO.getAllOrdersByOrderID(searchValue);
            if (order != null) {
                // Found a match for orderID, add it to the table
                addOrderRow(order);
            } else {
                // If no match for orderID, try searching by customerID
                List<Orders> orders = ordersDAO.getAllOrdersByCustomerID(searchValue);
                if (!orders.isEmpty()) {
                    for (Orders o : orders) {
                        addOrderRow(o);
                    }
                } else {
                    // If no matches found at all, show a message or leave the table empty
                    JOptionPane.showMessageDialog(null, "No orders found for the given ID.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } else {
            // Handle non-numeric input (if needed)
            JOptionPane.showMessageDialog(null, "Please enter a valid numeric ID.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }


    // Method to load all orders
    private void loadOrders() {
        tableModel.setRowCount(0); // Clear existing rows
        List<Orders> orders = ordersDAO.getAllOrders();
        for (Orders order : orders) {
            addOrderRow(order);
        }
    }

    // Add Orders to table
    private void addOrderRow(Orders order) {
        tableModel.addRow(new Object[]{
                order.getOrderID(),
                order.getCustomerID(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                "Update",
                "Delete",
                "View Details",
        });
    }

    // Renderer for "Update" and "Delete" and "View Details" buttons
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

    // Editor for "Update" and "Delete" buttons with actions
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
                    deleteOrder(row);
                } else if ("View Details".equals(label)) {
                    viewDetails(row);
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
            int orderID = (int) orderTable.getValueAt(row, 0);
            Orders order = ordersDAO.getAllOrdersByOrderID(orderID);

            if (order == null) {
                JOptionPane.showMessageDialog(null, "Order not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog updateDialog = new JDialog((Frame) null, "Update Order", true);
            updateDialog.setSize(500, 400);
            updateDialog.setLayout(new GridLayout(6, 2));

            updateDialog.add(new JLabel("Order ID:"));
            JTextField idField = new JTextField(String.valueOf(order.getOrderID()));
            idField.setEditable(false);
            updateDialog.add(idField);

            updateDialog.add(new JLabel("Customer ID:"));
            JTextField customerIDField = new JTextField(String.valueOf(order.getCustomerID()));
            updateDialog.add(customerIDField);

            updateDialog.add(new JLabel("Order Date:"));
            JTextField orderDateField = new JTextField(String.valueOf(order.getOrderDate()));
            updateDialog.add(orderDateField);

            updateDialog.add(new JLabel("Total Amount:"));
            JTextField totalAmountField = new JTextField(String.valueOf(order.getTotalAmount()));
            updateDialog.add(totalAmountField);

            updateDialog.add(new JLabel("Status:"));
            JTextField statusField = new JTextField(order.getStatus());
            updateDialog.add(statusField);

            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(e -> {
                try {
                    order.setCustomerID(Integer.parseInt(customerIDField.getText().trim()));
                    order.setOrderID(Integer.parseInt(idField.getText().trim()));

                    // Parse the order date from the text field and convert to java.sql.Date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date parsedDate = dateFormat.parse(orderDateField.getText().trim()); // java.util.Date
                    java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime()); // Convert to java.sql.Date
                    order.setOrderDate(sqlDate);

                    order.setTotalAmount(Double.parseDouble(totalAmountField.getText().trim()));
                    order.setStatus(statusField.getText().trim());

                    if (ordersDAO.updateOrder(order)) {
                        updateDialog.dispose();
                        JOptionPane.showMessageDialog(null, "Order updated successfully!");
                        loadOrders();
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to update order.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (ParseException pe) {
                    JOptionPane.showMessageDialog(null, "Invalid date format. Use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            updateDialog.add(submitButton);
            updateDialog.setLocationRelativeTo(null);
            updateDialog.setVisible(true);
        }

        private void deleteOrder(int row) {
            int orderID = (int) orderTable.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this order?", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (ordersDAO.deleteOrder(orderID)) {
                    JOptionPane.showMessageDialog(null, "Order deleted successfully!");
                    loadOrders();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to delete order.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void viewDetails(int row) {
            int orderID = (int) orderTable.getValueAt(row, 0);
            List<Object[]> details = ordersDAO.getOrderDetails(orderID);

            // Create column names for the JTable
            String[] columnNames = {"Product Name", "Quantity", "Unit Price"};

            // Convert the list to an Object[][] for the JTable
            Object[][] data = new Object[details.size()][columnNames.length];
            double totalAmount = 0.0;

            for (int i = 0; i < details.size(); i++) {
                data[i][0] = details.get(i)[0]; // Product Name
                data[i][1] = details.get(i)[1]; // Quantity
                data[i][2] = details.get(i)[2]; // Unit Price
                totalAmount += (int) data[i][1] * (double) data[i][2]; // Calculate total amount
            }

            // Create the JTable with order details
            JTable orderDetailsTable = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(orderDetailsTable);
            JLabel totalAmountLabel = new JLabel("Total Amount: $" + String.format("%.2f", totalAmount));
            totalAmountLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // Create a panel to hold the table and label
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(totalAmountLabel, BorderLayout.SOUTH);

            // Show the dialog with the order details
            JDialog dialog = new JDialog(mainFrame, "Order " + orderID + " Details", true);
            dialog.add(panel);
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(mainFrame);
            dialog.setVisible(true);
        }
    }
}
