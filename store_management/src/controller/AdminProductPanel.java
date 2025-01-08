import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AdminProductPanel extends JPanel {
    private JTextField searchField;
    private JButton addProductButton, searchButton, viewAllButton;

    private JTable productTable;
    private DefaultTableModel tableModel;
    private ProductDAO productDAO;
    private InventoryDAO inventoryDAO;
    private MainFrame mainFrame;

    public AdminProductPanel(MainFrame mainFrame) {
        this.productDAO = new ProductDAO();
        this.inventoryDAO = new InventoryDAO();
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // Search bar and buttons
        JPanel topPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        addProductButton = new JButton("Add Product");
        viewAllButton = new JButton("View All");

        // Add to panel
        topPanel.add(new JLabel("Search by ID/Name:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(viewAllButton);
        topPanel.add(addProductButton);
        add(topPanel, BorderLayout.NORTH);

        // Add ActionListener
        addProductButton.addActionListener(e -> openAddProductDialog());
        searchButton.addActionListener(e -> searchProduct());
        viewAllButton.addActionListener(e -> loadProducts());

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
        String[] columnNames = {"Product ID", "Name", "Description", "Price", "Quantity", "Update", "Delete", "Review"};
        tableModel = new DefaultTableModel(columnNames, 0);
        productTable = new JTable(tableModel);
        add(new JScrollPane(productTable), BorderLayout.CENTER);

        // Set column widths
        productTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        productTable.getColumnModel().getColumn(2).setPreferredWidth(250); // Address

        // Button renderers and editors
        productTable.getColumn("Update").setCellRenderer(new ButtonRenderer("Update"));
        productTable.getColumn("Update").setCellEditor(new ButtonEditor(productTable, "Update"));

        productTable.getColumn("Delete").setCellRenderer(new ButtonRenderer("Delete"));
        productTable.getColumn("Delete").setCellEditor(new ButtonEditor(productTable, "Delete"));

        productTable.getColumn("Review").setCellRenderer(new ButtonRenderer("Review"));
        productTable.getColumn("Review").setCellEditor(new ButtonEditor(productTable, "Review"));

        // Load Products into Table
        loadProducts();
    }

    // Method to search for product
    private void searchProduct() {
        String searchText = searchField.getText().trim();
        tableModel.setRowCount(0); // Clear existing rows
        List<Product> products;

        if (searchText.matches("\\d+")) { // If searchText is an ID
            Product product = productDAO.getProductByID(Integer.parseInt(searchText));

            if (product != null) {
                int quantity = inventoryDAO.getQuantityByProductID(product.getProductID()); // Get quantity for product ID
                addProductRow(product, quantity);
            }
        } else { // If searchText is a name
            products = productDAO.getProductByName(searchText);

            for (Product product : products) {
                int quantity = inventoryDAO.getQuantityByProductID(product.getProductID()); // Get quantity for each product by ID
                addProductRow(product, quantity);
            }
        }
    }

    // Method to load all products
    private void loadProducts() {
        tableModel.setRowCount(0); // Clear existing rows

        // Retrieve all products
        List<Product> products = productDAO.getAllProduct();
        for (Product product : products) {
            int quantity = inventoryDAO.getQuantityByProductID(product.getProductID());
            addProductRow(product, quantity);
        }
    }

    // Add Products to table
    private void addProductRow(Product product, int quantity) {
        tableModel.addRow(new Object[]{
                product.getProductID(),
                product.getProductName(),
                product.getProductDescription(),
                product.getProductPrice(),
                quantity,
                "Update",
                "Delete",
                "Review"
        });
    }

    // Open the Add Product dialog
    private void openAddProductDialog() {
        JDialog addDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Product", true);
        addDialog.setSize(500,400);
        addDialog.setLayout(new GridLayout(6, 2));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField quantityField = new JTextField();

        addDialog.add(new JLabel("Product ID:"));
        addDialog.add(idField);
        addDialog.add(new JLabel("Name:"));
        addDialog.add(nameField);
        addDialog.add(new JLabel("Description:"));
        addDialog.add(descField);
        addDialog.add(new JLabel("Price:"));
        addDialog.add(priceField);
        addDialog.add(new JLabel("Quantity:"));
        addDialog.add(quantityField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Add product logic here
                Product newProduct = new Product(
                    Integer.parseInt(idField.getText()),
                    nameField.getText(),
                    descField.getText(),
                    Double.parseDouble(priceField.getText())
                );
                productDAO.addProduct(newProduct);

                int quantity = Integer.parseInt(quantityField.getText());
                inventoryDAO.addQuantity(newProduct.getProductID(), quantity);

                loadProducts();
                addDialog.dispose();
            }
        });

        addDialog.add(submitButton);
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
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
                    deleteCustomer(row);
                } else if ("Review".equals(label)) {
                    openReviewForm(row);
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
            Product product = productDAO.getProductByID(productID);
            Inventory inventory = inventoryDAO.getInventoryByProductID(productID);

            if (product == null) {
                JOptionPane.showMessageDialog(null, "Product not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog updateDialog = new JDialog((Frame) null, "Update Product", true);
            updateDialog.setSize(500,400);
            updateDialog.setLayout(new GridLayout(6, 2));

            updateDialog.add(new JLabel("ID:"));
            JTextField idField = new JTextField(String.valueOf(product.getProductID()));
            idField.setEditable(false);
            updateDialog.add(idField);

            updateDialog.add(new JLabel("Name:"));
            JTextField nameField = new JTextField(product.getProductName());
            updateDialog.add(nameField);

            updateDialog.add(new JLabel("Description:"));
            JTextField descriptionField = new JTextField(product.getProductDescription());
            updateDialog.add(descriptionField);

            updateDialog.add(new JLabel("Price:"));
            JTextField priceField = new JTextField(String.valueOf(product.getProductPrice()));
            updateDialog.add(priceField);

            updateDialog.add(new JLabel("Quantity:"));
            JTextField quantityField = new JTextField(String.valueOf(inventory.getQuantity()));
            updateDialog.add(quantityField);

            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(e -> {
                product.setProductName(nameField.getText().trim());
                product.setProductDescription(descriptionField.getText().trim());
                product.setProductPrice(Double.parseDouble(priceField.getText().trim()));
                int newQuantity = Integer.parseInt(quantityField.getText().trim());
                inventory.setQuantity(newQuantity);

                if (productDAO.updateProduct(product)) {
                    if (inventoryDAO.updateQuantity(productID, newQuantity)) {
                        updateDialog.dispose();
                        JOptionPane.showMessageDialog(null, "Product updated successfully!");
                        loadProducts();
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to update product.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to update product.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            updateDialog.add(submitButton);
            updateDialog.setLocationRelativeTo(null);
            updateDialog.setVisible(true);
        }

        private void deleteCustomer(int row) {
            int productID = (int) table.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this product?", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (inventoryDAO.deleteInventory(productID)) {
                    if (productDAO.deleteProduct(productID)) {
                        JOptionPane.showMessageDialog(null, "Product deleted successfully.");
                        loadProducts();
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to delete product.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                        JOptionPane.showMessageDialog(null, "Failed to delete product.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void openReviewForm(int row) {
            int productID = (int) productTable.getValueAt(row, 0);

            // Create a dialog for reviews
            JDialog reviewDialog = new JDialog((Frame) null, "Reviews for Product ID: " + productID, true);
            reviewDialog.setSize(600, 400);
            reviewDialog.setLayout(new BorderLayout());

            // Review table setup
            String[] columnNames = {"Customer ID", "Star Rated", "Feedback"};
            DefaultTableModel reviewTableModel = new DefaultTableModel(columnNames, 0);
            JTable reviewTable = new JTable(reviewTableModel);

            // Fetch reviews from the DAO
            ReviewDAO reviewDAO = new ReviewDAO();
            List<Review> reviews = reviewDAO.getReviewsByProductID(productID);

            // Populate the table
            for (Review review : reviews) {
                reviewTableModel.addRow(new Object[]{
                        review.getCustomerID(),
                        review.getStar(),
                        review.getFeedback()
                });
            }

            reviewDialog.add(new JScrollPane(reviewTable), BorderLayout.CENTER);

            // Add Close Button
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> reviewDialog.dispose());
            JPanel bottomPanel = new JPanel(new FlowLayout());
            bottomPanel.add(closeButton);
            reviewDialog.add(bottomPanel, BorderLayout.SOUTH);

            reviewDialog.setLocationRelativeTo(null);
            reviewDialog.setVisible(true);
        }

    }
}
