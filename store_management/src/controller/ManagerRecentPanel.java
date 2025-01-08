import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManagerRecentPanel extends JPanel {
    private JTable recentCustomerTable;
    private DefaultTableModel tableModel;

    public ManagerRecentPanel(JFrame frame) {
        setLayout(new BorderLayout());
        String[] columnNames = {"Customer ID", "Name", "Phone", "Address", "Latest Order Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        recentCustomerTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(recentCustomerTable);
        add(scrollPane, BorderLayout.CENTER);

        JButton returnButton = new JButton("Return");
        add(returnButton, BorderLayout.SOUTH);
        returnButton.addActionListener(e -> {
            frame.setContentPane(new ManagerView(frame));
            frame.revalidate();
        });

        loadRecentCustomers();
    }

    private void loadRecentCustomers() {
        ManagerDAO managerDAO = new ManagerDAO();
        List<String[]> recentCustomers = managerDAO.getRecentCustomerDetails();

        // Populate the JTable
        for (String[] customer : recentCustomers) {
            tableModel.addRow(customer);
        }
    }
}
