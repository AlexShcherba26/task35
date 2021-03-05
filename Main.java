import java.io.*;
import java.util.*;

public class Main {

    private static final String ORDERS_FILE = "task35/resources/orders.csv";
    private static final String PRODUCTS_FILE = "task35/resources/products.csv";
    private static final String ORDER_ITEMS_FILE = "task35/resources/order_items.csv";

    public static void main(String[] args) {

        Map<String, Order> ordersMap = readOrders();
        Map<String, Product> productsMap = readProducts();
        List<Order> orders = populateOrders(ordersMap, productsMap);

        Map<String, ProductIncome> dailyIncomes = groupByDateAndProductSum(orders);

        ProductIncome bestProductIncome = dailyIncomes.get("2021-01-21");
        System.out.println(bestProductIncome.getProduct().getName() + " : " + bestProductIncome.getSum());
    }

    public static Map<String, Order> readOrders() {
        Map<String, Order> orders = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ORDERS_FILE)))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Order order = new Order(values[0]);
                String dateOnly = values[1].split("T")[0];
                order.setDate(dateOnly);
                orders.put(order.getId(), order);
            }
        } catch (IOException e) {
            throw new IllegalStateException("failed to read orders", e);
        }

        return orders;
    }

    private static Map<String, Product> readProducts() {
        Map<String, Product> products = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(PRODUCTS_FILE)))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Product product = new Product(values[0], values[1], Integer.parseInt(values[2]));
                products.put(product.getId(), product);
            }
        } catch (IOException e) {
            throw new IllegalStateException("failed to read products", e);
        }

        return products;
    }

    private static List<Order> populateOrders(Map<String, Order> orders, Map<String, Product> products) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ORDER_ITEMS_FILE)));) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                Order order = orders.get(values[0]);
                Product product = products.get(values[1]);
                order.add(new OrderItem(product, Integer.parseInt(values[2])));
                products.put(product.getId(), product);
            }
        } catch (IOException e) {
            throw new IllegalStateException("failed to read products", e);
        }

        List<Order> list = new ArrayList<>();
        for (Order order : orders.values()) {
            list.add(order);
        }

        return list;
    }

    private static Map<String, ProductIncome> groupByDateAndProductSum(List<Order> orders) {
        Map<String, Map<String, ProductIncome>> dailyIncomes = new HashMap<>();
        for (Order order : orders) {
            String date = order.getDate();
            Map<String, ProductIncome> incomes = dailyIncomes.get(date);
            if (incomes == null) {
                incomes = new HashMap<>();
                dailyIncomes.put(date, incomes);
            }

            for (OrderItem item : order.getItems()) {
                ProductIncome income = incomes.get(item.getProduct().getId());
                if (income == null) {
                    income = new ProductIncome(item.getProduct());
                    incomes.put(item.getProduct().getId(), income);
                }
                income.addQuantity(item.getQuantity());
            }
        }

        Map<String, ProductIncome> dailyProductIncomes = new HashMap<>();
        for (Map.Entry<String, Map<String, ProductIncome>> entry : dailyIncomes.entrySet()) {
            ProductIncome maxIncome = null;
            for (ProductIncome income : entry.getValue().values()) {
                if (maxIncome == null) {
                    maxIncome = income;
                    continue;
                }

                if (maxIncome.getSum() < income.getSum()) {
                    maxIncome = income;
                }
            }
            dailyProductIncomes.put(entry.getKey(), maxIncome);
        }

        return dailyProductIncomes;
    }
}

class Order {

    private String id;
    private String date;
    private List<OrderItem> items = new ArrayList<>();

    public Order(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void add(OrderItem item) {
        items.add(item);
    }

    public List<OrderItem> getItems() {
        return items;
    }

}

class Product {

    private String id;
    private String name;
    private int price;

    public Product(String id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
    
}

class OrderItem {
    private Product product;
    private int quantity;

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

}

class ProductIncome {
    private Product product;
    private int sum; // price * quantity

    public ProductIncome(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    public void addQuantity(int quantity) {
        this.sum += product.getPrice() * quantity;
    }

    public int getSum() {
        return sum;
    }
}