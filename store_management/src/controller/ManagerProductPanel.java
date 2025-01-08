import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManagerProductPanel extends JPanel {
    private JTable productTable;
    private DefaultTableModel tableModel;

    public ManagerProductPanel(JFrame frame) {
        setLayout(new BorderLayout());
        String[] columnNames = {"Product ID", "Product Name", "Total Sold"};
        tableModel = new DefaultTableModel(columnNames, 0);
        productTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        JButton returnButton = new JButton("Return");
        add(returnButton, BorderLayout.SOUTH);
        returnButton.addActionListener(e -> {
            frame.setContentPane(new ManagerView(frame));
            frame.revalidate();
        });

        loadBestSellingProducts();
    }

    private void loadBestSellingProducts() {
        ManagerDAO managerDAO = new ManagerDAO();
        List<String[]> bestSellingProducts = managerDAO.getBestSellingProducts();

        // Populate the JTable
        for (String[] product : bestSellingProducts) {
            tableModel.addRow(product);
        }
    }
}
