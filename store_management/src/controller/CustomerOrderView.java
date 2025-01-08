import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class CustomerOrderView extends JPanel {
    private MainFrame mainFrame;
    private JTable orderTable;
    private DefaultTableModel orderTableModel;

    private int customerID;
    private OrdersDAO ordersDAO;

    public CustomerOrderView (MainFrame mainFrame, int customerID) {
        this.mainFrame = mainFrame;
        this.customerID = customerID;
        this.ordersDAO = new OrdersDAO();

        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout());
        add(topPanel, BorderLayout.NORTH);

        // Log Out
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);
        JButton logOutButton = new JButton("Log Out");
        bottomPanel.add(logOutButton);
        logOutButton.addActionListener(e -> {
            mainFrame.setContentPane(new HomePagePanel(mainFrame));
            mainFrame.revalidate();
        });

        JButton returnButton = new JButton("Add Order");
        bottomPanel.add(returnButton);
        returnButton.addActionListener(e -> {
            mainFrame.setContentPane(new CustomerProductView(mainFrame, customerID));
            mainFrame.revalidate();
        });

        // "View All" button
        JButton viewAllButton = new JButton("View All");
        topPanel.add(viewAllButton);
        viewAllButton.addActionListener(e -> loadAllOrders());

        // Table to display orders
        String[] columnNames = {"Order ID", "Order Date", "Total Amount", "Status", " "};
        orderTableModel = new DefaultTableModel(columnNames, 0);
        orderTable = new JTable(orderTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only the last column is editable
            }
        };
        add(new JScrollPane(orderTable), BorderLayout.CENTER);

        // Add "View Details" button functionality in the table
        orderTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        orderTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

        loadAllOrders();
    }

    // Method to load all orders
    private void loadAllOrders() {
        orderTableModel.setRowCount(0);
        List<Orders> orders = ordersDAO.getAllOrdersByCustomerID(customerID);
        for (Orders order : orders) {
            addOrderRow(order);
        }
    }
    private void addOrderRow(Orders order) {
        orderTableModel.addRow(new Object[]{
                order.getOrderID(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                "View Details"
        });
    }

    // Define ButtonRenderer and ButtonEditor classes for the "View Details" button
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            //setText("View Details");
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int orderID;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("View Details");
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    openOrderDetailsDialog(orderID);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            orderID = (int) table.getValueAt(row, 0); // Get order ID from the row
            button.setText(value == null ? "View Details" : value.toString());
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return button.getText();
        }
    }

    private void openOrderDetailsDialog(int orderID) {
        OrdersDAO ordersDAO = new OrdersDAO();
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
