package lld;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// ==========================================
// ENUMS
// ==========================================
enum Genre {
    FICTION, NON_FICTION, SCI_FI, MYSTERY, TECHNOLOGY
}

enum BookFormat {
    HARDCOVER, PAPERBACK, EBOOK
}

enum OrderStatus {
    PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}

enum PaymentStatus {
    UNPAID, PENDING, COMPLETED, FAILED, REFUNDED
}

// ==========================================
// CORE ENTITIES (MODELS)
// ==========================================

class Book {
    private String isbn;
    private String title;
    private String author;
    private double price;
    private Genre genre;
    private int pages;

    public Book(String isbn, String title, String author, double price, Genre genre, int pages) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.genre = genre;
        this.pages = pages;
    }

    // Getters
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public double getPrice() { return price; }
    public Genre getGenre() { return genre; }
    
    @Override
    public String toString() {
        return String.format("'%s' by %s ($%.2f)", title, author, price);
    }
}

class User {
    private String userId;
    private String name;
    private String email;

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public String getName() { return name; }
}

class CartItem {
    private Book book;
    private int quantity;

    public CartItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return book.getPrice() * quantity;
    }

    public Book getBook() { return book; }
    public int getQuantity() { return quantity; }
}

class Cart {
    private Map<String, CartItem> items = new HashMap<>();

    public void add(Book book, int quantity) {
        items.compute(book.getIsbn(), (key, existing) -> {
            if (existing == null) return new CartItem(book, quantity);
            return new CartItem(book, existing.getQuantity() + quantity);
        });
    }

    public void remove(String isbn) {
        items.remove(isbn);
    }

    public double getTotal() {
        return items.values().stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }
    
    public void clear() {
        items.clear();
    }
}

class Order {
    private String orderId;
    private User user;
    private List<CartItem> items;
    private double totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;

    public Order(User user, List<CartItem> items, double totalAmount) {
        this.orderId = UUID.randomUUID().toString();
        this.user = user;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public double getTotalAmount() { return totalAmount; }
    public String getOrderId() { return orderId; }

    @Override
    public String toString() {
        return "Order[" + orderId + "] Total: $" + totalAmount + " Status: " + status;
    }
}

// ==========================================
// INTERFACES (SERVICES & STRATEGIES)
// ==========================================

interface SearchService {
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
    List<Book> searchByGenre(Genre genre);
}

interface PaymentStrategy {
    boolean pay(double amount);
}

// ==========================================
// IMPLEMENTATIONS
// ==========================================

class BookSearchService implements SearchService {
    private List<Book> inventory;

    public BookSearchService(List<Book> inventory) {
        this.inventory = inventory;
    }

    @Override
    public List<Book> searchByTitle(String title) {
        return inventory.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        return inventory.stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> searchByGenre(Genre genre) {
        return inventory.stream()
                .filter(b -> b.getGenre() == genre)
                .collect(Collectors.toList());
    }
}

// --- Payment Strategies ---

class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;

    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public boolean pay(double amount) {
        System.out.println("Processing Credit Card payment of $" + amount + " using " + cardNumber);
        // Simulate processing
        return true; 
    }
}

class UPIPayment implements PaymentStrategy {
    private String upiId;

    public UPIPayment(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public boolean pay(double amount) {
        System.out.println("Processing UPI payment of $" + amount + " using ID " + upiId);
        return true;
    }
}

// ==========================================
// MAIN FACADE / CONTROLLER
// ==========================================

class BookstoreSystem {
    private List<Book> catalog;
    private SearchService searchService;
    private Map<String, User> users;
    // In-memory order storage
    private Map<String, Order> orders; 

    public BookstoreSystem() {
        this.catalog = new ArrayList<>();
        this.searchService = new BookSearchService(catalog);
        this.users = new HashMap<>();
        this.orders = new ConcurrentHashMap<>();
    }

    public void addBook(Book book) {
        catalog.add(book);
    }
    
    public void addUser(User user) {
        // Simple add
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public Order checkout(User user, Cart cart, PaymentStrategy paymentMethod) {
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty!");
        }

        double total = cart.getTotal();
        Order order = new Order(user, new ArrayList<>(cart.getItems()), total);
        
        // Process Payment
        boolean paymentSuccess = paymentMethod.pay(total);
        if (paymentSuccess) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            order.setStatus(OrderStatus.CONFIRMED);
            System.out.println("Payment successful. Order placed: " + order.getOrderId());
            
            // In a real system, we would reserve inventory here or before payment
            orders.put(order.getOrderId(), order);
            cart.clear(); // Empty cart after successful purchase
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.CANCELLED);
            System.out.println("Payment failed.");
        }
        
        return order;
    }
}

// ==========================================
// DRIVER CODE
// ==========================================

public class OnlineBookstore {
    public static void main(String[] args) {
        BookstoreSystem bookstore = new BookstoreSystem();

        // 1. Setup Data
        Book b1 = new Book("101", "Design Patterns", "GoF", 50.0, Genre.TECHNOLOGY, 400);
        Book b2 = new Book("102", "Clean Code", "Uncle Bob", 45.0, Genre.TECHNOLOGY, 300);
        Book b3 = new Book("103", "The Hobbit", "J.R.R. Tolkien", 20.0, Genre.FICTION, 350);
        
        bookstore.addBook(b1);
        bookstore.addBook(b2);
        bookstore.addBook(b3);

        User user = new User("u1", "Priya", "priya@example.com");

        // 2. User Searches
        System.out.println("--- Searching 'Design' ---");
        List<Book> results = bookstore.getSearchService().searchByTitle("Design");
        results.forEach(System.out::println);

        // 3. Add to Cart
        System.out.println("\n--- Adding to Cart ---");
        Cart cart = new Cart();
        cart.add(b1, 1);
        cart.add(b3, 2); // 2 copies of Hobbit
        System.out.println("Cart Total: $" + cart.getTotal());

        // 4. Checkout with Credit Card
        System.out.println("\n--- Checkout (Credit Card) ---");
        PaymentStrategy ccPayment = new CreditCardPayment("1234-5678-9876-5432");
        bookstore.checkout(user, cart, ccPayment);
        
        // 5. Checkout with UPI (Empty cart test)
        System.out.println("\n--- Checkout (Empty Cart) ---");
        try {
            bookstore.checkout(user, cart, new UPIPayment("priya@upi"));
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}



let's say:



kpiname  secondpartcategorycode   categprycode   product  revenue

Cash In      rt                                  bulkdisbursementsend  1000
Cash In      whs                                  bulkdisbursementsend  1000
Cash In      sagnt                                bulkdisbursementsend  1000
Cash In      rts                                  bulkdisbursementsend  1000
Cash In      rt                                  bulkdisbursementsend  1000
Merchant                                           merchant             100

