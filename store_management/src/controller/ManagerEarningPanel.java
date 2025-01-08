import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManagerEarningPanel extends JPanel {
    private JTable earningTable;
    private DefaultTableModel tableModel;

    public ManagerEarningPanel(JFrame frame) {
        setLayout(new BorderLayout());

        String[] columnNames = {"Month", "Total Earnings"};
        tableModel = new DefaultTableModel(columnNames, 0);
        earningTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(earningTable);
        add(scrollPane, BorderLayout.CENTER);

        JButton returnButton = new JButton("Return");
        add(returnButton, BorderLayout.SOUTH);
        returnButton.addActionListener(e -> {
            frame.setContentPane(new ManagerView(frame));
            frame.revalidate();
        });

        loadMonthlyEarnings();
    }

    private void loadMonthlyEarnings() {
        ManagerDAO managerDAO = new ManagerDAO();
        List<String[]> monthlyEarnings = managerDAO.getMonthlyEarnings();

        // Populate the JTable
        for (String[] earnings : monthlyEarnings) {
            tableModel.addRow(earnings);
        }
    }
}
