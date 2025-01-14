# Store-Management-System

This project is a **Retail Store Management System** designed to simplify and streamline the management of products, orders, inventory, and customers. It is built using **Java**, with integration across **MongoDB**, **MySQL**, and **Redis** databases, and a user-friendly interface developed with **Java Swing**. Please find my testing video [here](https://youtu.be/ZAAURR3pwFQ).

---

## **Features**

### **Admin Portal**
The Admin can:
1. **Manage Products**: Add, update, delete, and search for products.
2. **Manage Orders**: Track customer orders and update their status.
3. **Manage Inventory**: Monitor stock levels and restock products.
4. **Manage Customers**: Add, update, delete, and search for customers in the system.

### **Manager Portal**
The Manager can:
1. **View Monthly Earnings**:
   - Calculates monthly earnings by combining data from **MongoDB** and **Redis**.
   - Displays earnings in an organized table for easy tracking.
2. **View Best-Selling Products**:
   - Analyzes product sales data from **MongoDB** and stores results in a **Redis sorted set**.
   - Displays the top-selling products in a table, including:
     - Product ID
     - Product Name (fetched from **MySQL**)
     - Total quantity sold.

### **Customer Portal**
The Customer can:
1.	**Browse Products**: Customers browse the product catalog by category.
2.	**Place Orders**: Add products to a shopping cart and place an order.
3.	**Track Orders**: View order status.
4.	**View Order Details**: See specific details of their previous orders.

---

## **Technologies Used**

- **Java**: Core programming language for business logic and UI.
- **Java Swing**: For creating the graphical user interface.
- **MongoDB**: To store and manage orders and customers.
- **MySQL**: To handle structured data for products and accounts.
- **Redis**: For fast data retrieval (e.g., best-selling products, monthly earnings).
- **Maven**: For project dependency management.

---

## **How It Works**

1. **Admin Panel**:
   - Admins can manage products, customers, and inventory, ensuring all records are up-to-date.
   - Orders can be tracked and updated efficiently.

2. **Manager Panel**:
   - Monthly earnings are fetched by aggregating data from **MongoDB** and cached into **Redis** for quick access.
   - Best-selling products are calculated by summing up product sales from **MongoDB**, storing the results in a Redis sorted set, and fetching product details from **MySQL**.

3. **Database Connections**:
   - MongoDB, MySQL, and Redis are integrated to handle different aspects of the system, ensuring optimal performance and flexibility.

---

## **Key Highlights**

- **Cross-Database Integration**:
  Combines the strengths of MongoDB, MySQL, and Redis to optimize performance and scalability.
  
- **Efficient Data Retrieval**:
  Uses Redis to cache frequently accessed data like monthly earnings and best-selling products.

- **User-Friendly Interface**:
  Designed with Java Swing for ease of use and intuitive navigation.
