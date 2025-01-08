import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.List;

public class AdminInventoryPanel extends JPanel {
    private JTextField searchField;
    private JButton searchButton, viewAllButton;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private InventoryDAO inventoryDAO;
    private MainFrame mainFrame;

    public AdminInventoryPanel(MainFrame mainFrame) {
        this.inventoryDAO = new InventoryDAO();
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // Search bar and buttons
        JPanel topPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        viewAllButton = new JButton("View All");

        // Add to panel
        topPanel.add(new JLabel("Search by Product ID:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(viewAllButton);
        add(topPanel, BorderLayout.NORTH);

        // Add ActionListener
        searchButton.addActionListener(e -> searchInventory());
        viewAllButton.addActionListener(e -> loadInventory());

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

        // Setup Table
        String[] columnNames = {"Product ID", "Quantity", "Location", "Last Updated", "Update", "Delete"};
        tableModel = new DefaultTableModel(columnNames, 0);
        inventoryTable = new JTable(tableModel);
        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        // Set column widths
        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Location
        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Timestamp

        // Button renderers and editors
        inventoryTable.getColumn("Update").setCellRenderer(new ButtonRenderer("Update"));
        inventoryTable.getColumn("Update").setCellEditor(new ButtonEditor(inventoryTable, "Update"));

        inventoryTable.getColumn("Delete").setCellRenderer(new ButtonRenderer("Delete"));
        inventoryTable.getColumn("Delete").setCellEditor(new ButtonEditor(inventoryTable, "Delete"));

        // Load Inventory into Table
        loadInventory();
    }

    // Method to search for inventory
    private void searchInventory() {
        String searchText = searchField.getText().trim();
        tableModel.setRowCount(0); // Clear existing rows

        if (searchText.matches("\\d+")) { // If searchText is an ID
            Inventory inventory = inventoryDAO.getInventoryByProductID(Integer.parseInt(searchText));

            if (inventory != null) {
                addInventoryRow(inventory);
            } else {
                JOptionPane.showMessageDialog(null, "No inventory found for the given Product ID.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please enter a valid Product ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to load all inventory
    private void loadInventory() {
        tableModel.setRowCount(0); // Clear existing rows

        // Retrieve all inventory
        List<Inventory> inventories = inventoryDAO.getAllInventory();
        for (Inventory inventory : inventories) {
            addInventoryRow(inventory);
        }
    }

    // Add Inventory to table
    private void addInventoryRow(Inventory inventory) {
        tableModel.addRow(new Object[]{
                inventory.getProductID(),
                inventory.getQuantity(),
                inventory.getLocation(),
                inventory.getLastUpdated(),
                "Update",
                "Delete"
        });
    }

    // Renderer for "Update" and "Delete" buttons
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
                    deleteInventory(row);
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
            int productID = (int) table.getValueAt(row, 0);
            Inventory inventory = inventoryDAO.getInventoryByProductID(productID);

            if (inventory == null) {
                JOptionPane.showMessageDialog(null, "Inventory not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog updateDialog = new JDialog((Frame) null, "Update Inventory", true);
            updateDialog.setSize(400, 300);
            updateDialog.setLayout(new GridLayout(5, 2));

            updateDialog.add(new JLabel("Product ID:"));
            JTextField idField = new JTextField(String.valueOf(inventory.getProductID()));
            idField.setEditable(false);
            updateDialog.add(idField);

            updateDialog.add(new JLabel("Quantity:"));
            JTextField quantityField = new JTextField(String.valueOf(inventory.getQuantity()));
            updateDialog.add(quantityField);

            updateDialog.add(new JLabel("Location:"));
            JTextField locationField = new JTextField(inventory.getLocation());
            updateDialog.add(locationField);

            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(e -> {
                int newQuantity = Integer.parseInt(quantityField.getText().trim());
                String newLocation = locationField.getText().trim();
                inventory.setQuantity(newQuantity);
                inventory.setLocation(newLocation);
                inventory.setLastUpdated(new Timestamp(System.currentTimeMillis()));

                if (inventoryDAO.updateInventory(inventory)) {
                    updateDialog.dispose();
                    JOptionPane.showMessageDialog(null, "Inventory updated successfully!");
                    loadInventory();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to update inventory.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            updateDialog.add(submitButton);
            updateDialog.setLocationRelativeTo(null);
            updateDialog.setVisible(true);
        }

        private void deleteInventory(int row) {
            int productID = (int) table.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this inventory record?", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (inventoryDAO.deleteInventory(productID)) {
                    JOptionPane.showMessageDialog(null, "Inventory deleted successfully.");
                    loadInventory();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to delete inventory.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
