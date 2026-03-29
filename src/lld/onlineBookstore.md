# Online Bookstore Low-Level Design

## 1. Requirements

### Functional Requirements
1.  **Search & Filter**: Users can search books and filter by category (Genre, Language, etc.).
2.  **Book Details**: View metadata (Title, Author, Price, Pages, Ratings, Language, Genre).
3.  **Cart & Favorites**: Users can add books to a shopping cart or mark them as "Starred" (Favorites).
4.  **Checkout & Payment**: Supports checkout process with multiple payment options.
5.  **Payment Handling**:
    *   **Success**: Generate a receipt/invoice.
    *   **Failure**: Trigger refund process (if deducted) or error message.

### Non-Functional Requirements
1.  **Consistency & Concurrency**:
    *   **Inventory Management**: Ensure no double feedback/booking for the last book copy. Use database locking (Optimistic/Pessimistic) or transactions.
    *   **Payment Safety**: Transactions must be atomic. Double payments should be prevented (Idempotency).
2.  **Availability**: The system should be highly available (uptime) for searching and browsing.
3.  **Scalability**:
    *   The system should handle a growing number of concurrent users and books.
    *   **Loose Coupling**: Components (Payment, Search, Inventory) should be independent to allow separate scaling.

## 2. Core Entities
*   **Book**: Represents the product (ISBN, Title, Author, Price).
*   **Inventory**: Manages stock levels for books.
*   **User/Member**: The customer interacting with the system.
*   **Cart**: A temporary holding place for books before purchase.
*   **Order**: A confirmed purchase request.
*   **Payment**: Record of a transaction.
*   **SearchService**: Interface for finding books.

## 3. Class Diagram / Relationships
*To be defined...*
