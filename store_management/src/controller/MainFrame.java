import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        // Set the title and default size
        setTitle("Retail Store Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Start with HomePagePanel
        setContentPane(new HomePagePanel(this));
        revalidate();
    }

    public void showCustomerView(int customerID) {
        setContentPane(new CustomerView(this, customerID));
        revalidate();
    }

    public void showCustomerPanel() {
        setContentPane(new AdminCustomerPanel(this));
        revalidate();
    }

    public void showAdminView() {
        setContentPane(new AdminView(this));
        revalidate();
    }

    public void showProductPanel() {
        setContentPane(new AdminProductPanel(this));
        revalidate();
    }

    public void showOrderPanel() {
        setContentPane(new AdminOrderPanel(this));
        revalidate();
    }

    public void showInventoryPanel() {
        setContentPane(new AdminInventoryPanel(this));
        revalidate();
    }

    public void showManagerView() {
        setContentPane(new ManagerView(this));
        revalidate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
