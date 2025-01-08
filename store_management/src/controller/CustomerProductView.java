import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerProductView extends JPanel {
    private MainFrame mainFrame;
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private Map<Integer, Integer> cart; // Holds productID and quantity

    JTextField searchField;
    private int currentOrderID;
    private int customerID;

    private ProductDAO productDAO;
    private OrdersDAO ordersDAO;

    public CustomerProductView(MainFrame mainFrame, int customerID) {
        this.mainFrame = mainFrame;
        this.customerID = customerID;
        this.cart = new HashMap<>();
        this.productDAO = new ProductDAO();
        this.ordersDAO = new OrdersDAO();

        // Generate a unique orderID for the session
        this.currentOrderID = ordersDAO.generateOrderID();

        setLayout(new BorderLayout());

        // Bottom panel with "View Order" button
        JPanel bottomPanel = new JPanel();
        JButton viewOrderButton = new JButton("View Order");
        bottomPanel.add(viewOrderButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Log Out
        JButton logOutButton = new JButton("Log Out");
        bottomPanel.add(logOutButton);
        logOutButton.addActionListener(e -> {
            mainFrame.setContentPane(new HomePagePanel(mainFrame));
            mainFrame.revalidate();
        });

        // Top panel with search bar, "View Cart", and "View All" buttons
        JPanel topPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        JButton viewCartButton = new JButton("View Cart");
        JButton viewAllButton = new JButton("View All");
        JButton searchButton = new JButton("Search");

        topPanel.add(new JLabel("Search Product:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(viewAllButton);
        topPanel.add(viewCartButton);
        add(topPanel, BorderLayout.NORTH);

        // Add actions to buttons
        viewAllButton.addActionListener(e -> loadAllProducts());
        viewCartButton.addActionListener(e -> openCartView());
        searchButton.addActionListener(e -> searchProduct());
        viewOrderButton.addActionListener(e -> {
            mainFrame.setContentPane(new CustomerOrderView(mainFrame, customerID));
            mainFrame.revalidate();
        });

        // Table to display products
        String[] columnNames = {"Product ID", "Name", "Description", "Unit Price", " ", "Review", "Review"};
        productTableModel = new DefaultTableModel(columnNames, 0);
        productTable = new JTable(productTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 4 && column <= 6;
            }
        };

        // Set column widths
        productTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        add(new JScrollPane(productTable), BorderLayout.CENTER);

        // Add button functionality in the table
        productTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        productTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

        productTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        productTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

        productTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        productTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        loadAllProducts();
    }

    // Search product
    private void searchProduct() {
        String searchText = searchField.getText().trim();
        productTableModel.setRowCount(0); // Clear existing rows
        List<Product> products;

        if (searchText.matches("\\d+")) { // If it's an ID
            Product product = productDAO.getProductByID(Integer.parseInt(searchText));
            if (product != null) {
                products = List.of(product);
            } else {
                products = List.of();
            }
        } else {
            products = productDAO.getProductByName(searchText);
        }

        for (Product product : products) {
            addProductRow(product);
        }
    }

    // Load all products into the table
    private void loadAllProducts() {
        // Clear existing rows
        productTableModel.setRowCount(0);
        List<Product> products = productDAO.getAllProduct();
        // Add products to the table
        for (Product product : products) {
            addProductRow(product);
        }
    }

    private void addProductRow(Product product) {
        productTableModel.addRow(new Object[]{
                product.getProductID(),
                product.getProductName(),
                product.getProductDescription(),
                product.getProductPrice(),
                "Add to Cart",
                "Add Review",
                "View Review"
        });
    }

    // Renderer for the "Add to Cart" button
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Set button text based on the column index
            switch (column) {
                case 4: // Add to Cart
                    setText("Add to Cart");
                    break;
                case 5: // Add Review
                    setText("Add Review");
                    break;
                case 6: // View Review
                    setText("View Review");
                    break;
                default:
                    setText("");
            }
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean isPushed;
        private int productID;
        private int columnIndex;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            productID = (int) table.getValueAt(row, 0); // Get Product ID
            columnIndex = column; // Track the current column index
            isPushed = true;

            // Set button label based on column
            if (column == 4) {
                button.setText("Add to Cart");
            } else if (column == 5) {
                button.setText("Add Review");
            } else if (column == 6) {
                button.setText("View Review");
            } else {
                button.setText(value == null ? "" : value.toString());
            }

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Perform actions based on the column index
                if (columnIndex == 4) {
                    handleAddToCart(productID);
                } else if (columnIndex == 5) {
                    handleAddReview(productID);
                } else if (columnIndex == 6) {
                    handleViewReview(productID);
                }
            }
            isPushed = false;
            return button.getText();
        }

        private void handleAddToCart(int productID) {
            if (!cart.containsKey(productID)) {
                // First time adding product to cart
                cart.put(productID, 1);
                button.setText("1");
            } else {
                // Increase quantity
                int quantity = cart.get(productID) + 1;
                cart.put(productID, quantity);
                button.setText(String.valueOf(quantity));
            }
        }

        private void handleAddReview(int productID) {
            JDialog reviewDialog = new JDialog(mainFrame, "Add Review", true);
            reviewDialog.setLayout(new GridLayout(3, 2));

            JLabel starLabel = new JLabel("Stars (out of 5):");
            JTextField starField = new JTextField();

            JLabel feedbackLabel = new JLabel("Feedback:");
            JTextField feedbackField = new JTextField();

            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(e -> {
                try {
                    int star = Integer.parseInt(starField.getText().trim());
                    String feedback = feedbackField.getText().trim();

                    if (star < 1 || star > 5) {
                        JOptionPane.showMessageDialog(reviewDialog, "Stars must be between 1 and 5.");
                    } else {
                        ReviewDAO reviewDAO = new ReviewDAO();
                        reviewDAO.addReview(customerID, productID, star, feedback);
                        JOptionPane.showMessageDialog(reviewDialog, "Review added successfully!");
                        reviewDialog.dispose();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(reviewDialog, "Invalid star rating. Please enter a number.");
                }
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> reviewDialog.dispose());

            reviewDialog.add(starLabel);
            reviewDialog.add(starField);
            reviewDialog.add(feedbackLabel);
            reviewDialog.add(feedbackField);
            reviewDialog.add(submitButton);
            reviewDialog.add(cancelButton);

            reviewDialog.setSize(400, 300);
            reviewDialog.setLocationRelativeTo(mainFrame);
            reviewDialog.setVisible(true);
        }

        private void handleViewReview(int productID) {
            JDialog reviewDialog = new JDialog(mainFrame, "View Reviews", true);
            reviewDialog.setLayout(new BorderLayout());

            DefaultTableModel reviewTableModel = new DefaultTableModel(new Object[]{"Customer ID", "Stars", "Feedback"}, 0);
            JTable reviewTable = new JTable(reviewTableModel);

            ReviewDAO reviewDAO = new ReviewDAO();
            List<Review> reviews = reviewDAO.getReviewsByProductID(productID);
            for (Review review : reviews) {
                reviewTableModel.addRow(new Object[]{
                        review.getCustomerID(),
                        review.getStar(),
                        review.getFeedback()
                });
            }

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> reviewDialog.dispose());

            JButton myReviewButton = new JButton("My Review");
            myReviewButton.addActionListener(e -> openMyReviewDialog(productID));

            JPanel bottom = new JPanel();
            bottom.add(closeButton);
            bottom.add(myReviewButton);

            reviewDialog.add(new JScrollPane(reviewTable), BorderLayout.CENTER);
            reviewDialog.add(bottom, BorderLayout.SOUTH);

            reviewDialog.setSize(500, 400);
            reviewDialog.setLocationRelativeTo(mainFrame);
            reviewDialog.setVisible(true);
        }
    }

    // Open the cart view as a dialog
    private void openCartView() {
        JDialog cartDialog = new JDialog(mainFrame, "Current Items", true);
        cartDialog.setSize(400, 300);
        cartDialog.setLayout(new BorderLayout());

        DefaultTableModel cartTableModel = new DefaultTableModel(new Object[]{"Product ID", "Product Name", "Quantity", "Unit Price"}, 0);
        JTable cartTable = new JTable(cartTableModel);

        cartTable.getColumnModel().getColumn(1).setPreferredWidth(150);

        double totalAmount = 0.0;

        List<Object[]> cartItems = ordersDAO.getCartDetails(cart);
        for (Object[] item : cartItems) {
            cartTableModel.addRow(item);
            totalAmount += (int) item[2] * (double) item[3];
        }

        // Total amount label
        JLabel totalLabel = new JLabel("Total: $" + String.format("%.2f", totalAmount));
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Place Order button
        JButton placeOrderButton = new JButton("Place Order");
        placeOrderButton.addActionListener(e -> {
            ordersDAO.saveOrderToDatabase(currentOrderID, customerID, cart);
            JOptionPane.showMessageDialog(this, "Order placed successfully!");
            cartDialog.dispose();
        });

        // Reset Cart button
        JButton resetCartButton = new JButton("Reset Cart");
        resetCartButton.addActionListener(e -> {
            cartDialog.dispose();
            cart.clear();
        });

        // Panel for Place Order and Reset Cart button
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
        buttonPanel.add(totalLabel);
        buttonPanel.add(placeOrderButton);
        buttonPanel.add(resetCartButton);

        cartDialog.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        cartDialog.add(buttonPanel, BorderLayout.SOUTH);

        cartDialog.setSize(400, 300);
        cartDialog.setLocationRelativeTo(mainFrame);
        cartDialog.setVisible(true);
    }

    private void openMyReviewDialog(int productID) {
        JDialog myReviewDialog = new JDialog(mainFrame, "My Review", true);
        myReviewDialog.setLayout(new BorderLayout());

        DefaultTableModel myReviewTableModel = new DefaultTableModel(new Object[]{"Product ID", "Stars", "Feedback", "Delete", "Update"}, 0);
        JTable myReviewTable = new JTable(myReviewTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4; // Only Delete and Update columns are editable
            }
        };

        ReviewDAO reviewDAO = new ReviewDAO();
        List<Review> myReviews = reviewDAO.getReviewsByCustomerID(customerID); // Fetch reviews by current customer
        for (Review review : myReviews) {
            myReviewTableModel.addRow(new Object[]{
                    review.getProductID(),
                    review.getStar(),
                    review.getFeedback(),
                    "Delete",
                    "Update"
            });
        }

        // Renderer for "Delete" and "Update" buttons
        class ButtonRenderer extends JButton implements TableCellRenderer {
            public ButtonRenderer() {
                setOpaque(true);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setText((value != null) ? value.toString() : ""); // Use the cell value for label
                return this;
            }
        }

        // Editor for "Delete" and "Update" buttons with actions
        class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
            private JButton button;
            private String action;
            private int row;

            public ButtonEditor(JTable table) {
                button = new JButton();
                button.setOpaque(true);

                button.addActionListener(e -> {
                    fireEditingStopped();
                    if ("Delete".equals(action)) {
                        deleteReview(row);
                    } else if ("Update".equals(action)) {
                        openUpdateForm(row);
                    }
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                this.action = (String) value;
                this.row = row;
                button.setText(action);
                return button;
            }

            @Override
            public Object getCellEditorValue() {
                return action;
            }

            public void deleteReview(int row) {
                int productIDToDelete = (int) myReviewTableModel.getValueAt(row, 0);
                boolean isDeleted = reviewDAO.deleteReview(customerID, productIDToDelete); // Attempt to delete

                if (isDeleted) {
                    myReviewTableModel.removeRow(row); // Remove row from table model
                    JOptionPane.showMessageDialog(myReviewDialog, "Review deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(myReviewDialog, "Failed to delete the review. Please try again.");
                }
            }


            private void openUpdateForm(int row) {
                int productIDToUpdate = (int) myReviewTableModel.getValueAt(row, 0);
                int currentStar = (int) myReviewTableModel.getValueAt(row, 1);
                String currentFeedback = (String) myReviewTableModel.getValueAt(row, 2);
                openUpdateReviewDialog(productIDToUpdate, currentStar, currentFeedback);
            }

            private void openUpdateReviewDialog(int productID, int currentStar, String currentFeedback) {
                JDialog updateDialog = new JDialog(mainFrame, "Update Review", true);
                updateDialog.setLayout(new GridLayout(3, 2));

                JLabel starLabel = new JLabel("Stars (1-5):");
                JTextField starField = new JTextField(String.valueOf(currentStar));

                JLabel feedbackLabel = new JLabel("Feedback:");
                JTextField feedbackField = new JTextField(currentFeedback);

                JButton updateButton = new JButton("Update");
                updateButton.addActionListener(e -> {
                    try {
                        int updatedStar = Integer.parseInt(starField.getText().trim());
                        String updatedFeedback = feedbackField.getText().trim();

                        if (updatedStar < 1 || updatedStar > 5) {
                            JOptionPane.showMessageDialog(updateDialog, "Stars must be between 1 and 5.");
                        } else {
                            reviewDAO.updateReview(customerID, productID, updatedStar, updatedFeedback);
                            myReviewTableModel.setValueAt(updatedStar, row, 1);
                            myReviewTableModel.setValueAt(updatedFeedback, row, 2);
                            JOptionPane.showMessageDialog(updateDialog, "Review updated successfully!");
                            updateDialog.dispose();
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(updateDialog, "Invalid star rating. Please enter a number.");
                    }
                });

                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(e -> updateDialog.dispose());

                updateDialog.add(starLabel);
                updateDialog.add(starField);
                updateDialog.add(feedbackLabel);
                updateDialog.add(feedbackField);
                updateDialog.add(updateButton);
                updateDialog.add(cancelButton);

                updateDialog.setSize(400, 300);
                updateDialog.setLocationRelativeTo(mainFrame);
                updateDialog.setVisible(true);
            }
        }

        myReviewTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        myReviewTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(myReviewTable));

        myReviewTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        myReviewTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(myReviewTable));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> myReviewDialog.dispose());

        myReviewDialog.add(new JScrollPane(myReviewTable), BorderLayout.CENTER);
        myReviewDialog.add(closeButton, BorderLayout.SOUTH);

        myReviewDialog.setSize(500, 400);
        myReviewDialog.setLocationRelativeTo(mainFrame);
        myReviewDialog.setVisible(true);
    }

}
