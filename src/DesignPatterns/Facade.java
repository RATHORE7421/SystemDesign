package src.DesignPatterns;

class InventoryService {
    public boolean checkStock(String itemId) {
        System.out.println("Checking stock for item: " + itemId);
        return true;  // assume always in stock
    }
}

class PaymentService {
    public boolean processPayment(String accountId, double amount) {
        System.out.println("Processing payment of ₹" + amount + " for account: " + accountId);
        return true;  // assume payment is successful
    }
}

class ShippingService {
    public void shipItem(String itemId, String address) {
        System.out.println("Shipping item: " + itemId + " to address: " + address);
    }
}

class NotificationService {
    public void sendConfirmation(String email) {
        System.out.println("Sending confirmation email to: " + email);
    }
}

class ShoppingFacade {
    private InventoryService inventory;
    private PaymentService payment;
    private ShippingService shipping;
    private NotificationService notification;

    public ShoppingFacade() {
        this.inventory = new InventoryService();
        this.payment = new PaymentService();
        this.shipping = new ShippingService();
        this.notification = new NotificationService();
    }

    public void placeOrder(String itemId, String userAccount, double amount, String shippingAddress, String email) {
        System.out.println("Placing order...");

        if (!inventory.checkStock(itemId)) {
            System.out.println("Item out of stock.");
            return;
        }

        if (!payment.processPayment(userAccount, amount)) {
            System.out.println("Payment failed.");
            return;
        }

        shipping.shipItem(itemId, shippingAddress);
        notification.sendConfirmation(email);

        System.out.println("Order placed successfully.");
    }
}

public class Facade {
    public static void main(String[] args) {
        ShoppingFacade shoppingFacade = new ShoppingFacade();

        shoppingFacade.placeOrder(
            "ITEM123",
            "ACCT5678",
            2999.99,
            "42 MG Road, Bangalore",
            "priya@example.com"
        );
    }
}

