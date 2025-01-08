import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminView extends JPanel {
    private MainFrame mainFrame;

    public AdminView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton productButton = new JButton("Product");
        JButton inventoryButton = new JButton("Inventory");
        JButton customerButton = new JButton("Customer");
        JButton ordersButton = new JButton("Orders");

        panel.add(productButton);
        panel.add(inventoryButton);
        panel.add(customerButton);
        panel.add(ordersButton);

        JPanel bottomPanel = new JPanel();
        JButton returnButton = new JButton("Return to Homepage");
        bottomPanel.add(returnButton);

        add(panel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        customerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showCustomerPanel();
            }
        });

        productButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showProductPanel();
            }
        });

        ordersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showOrderPanel();
            }
        });

        inventoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showInventoryPanel();
            }
        });

        returnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.setContentPane(new HomePagePanel(mainFrame));
                mainFrame.revalidate();
            }
        });
    }
}
