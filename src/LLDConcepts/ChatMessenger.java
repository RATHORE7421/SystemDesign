// # ═══════════════════════════════════════════════════════════
// # CHAT MESSENGER — Low Level Design (LLD Interview)
// # ═══════════════════════════════════════════════════════════

// # Clarifying Questions to Ask:
// # 1. "Are we designing 1-on-1 chats, group chats, or both?" → 1-on-1 only (groups out of scope)
// # 2. "Should we support multimedia messages?" → Yes: text, image, video, voice note
// # 3. "Do we need read receipts?" → Yes: sent → delivered → read
// # 4. "Can users edit or delete messages?" → Yes, both
// # 5. "What if the recipient is offline?" → Store message, deliver when they come online
// # 6. "Should we show typing indicators?" → Yes

// # ═══════════════════════════════════════════════════════════

// # Requirements:
// # 1. User can send a message to another user (1-on-1 chat)
// # 2. A Conversation holds all messages between two users
// # 3. Message states: SENT → DELIVERED → READ (read receipts)
// # 4. Message types: Text, Image, Video, VoiceNote
// # 5. Messages must be ordered chronologically (by timestamp/sequence number)
// # 6. User can delete a message (delete for me / delete for everyone)
// # 7. User can edit a sent message
// # 8. Typing indicator — "User is typing..."
// # 9. Offline messaging — messages stored and delivered when recipient comes online

// # Error Handling / Edge Cases:
// # 1. Message ordering must be maintained even with network delays
// # 2. Duplicate message prevention (idempotency)
// # 3. Editing/deleting a message that the other person has already read
// # 4. Sending a message to a non-existent user → return error
// # 5. Concurrent edits/deletes on the same message

// # Out of Scope:
// # 1. Group messaging and broadcast channels
// # 2. Online/offline status and last seen
// # 3. End-to-end encryption implementation
// # 4. File sharing (beyond image/video/voice note)
// # 5. Voice/video calling

// # ═══════════════════════════════════════════════════════════
// # Core Entities:
// # ChatService => orchestrator class — sends, delivers, edits, deletes messages
// # User => person who will send or receive message (SRP: data only)
// # ChatHistory => Persists user 1-1 conversation with message history
// # Message => holds sender, receiver, content, status, timestamp
// # MessageContent => Interface (OCP) — Text, Image, Video, VoiceNote
// # MessageState => Enum — SENT, DELIVERED, READ
// # ═══════════════════════════════════════════════════════════

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

// ==================== ENUMS ====================

enum MessageState {
    SENT,
    DELIVERED,
    READ
}

enum DeleteType {
    DELETE_FOR_ME,
    DELETE_FOR_EVERYONE
}

// ==================== MESSAGE CONTENT (OCP — Interface) ====================
// Each message type has different data fields.
// New types (DocumentContent, LocationContent) can be added without modifying existing code.

interface MessageContent {
    String getContentPreview();   // For notification: "sent a photo", "sent a video"
    long getSize();               // Storage size in bytes
}

class TextContent implements MessageContent {
    private String text;

    public TextContent(String text) {
        this.text = text;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }  // For edit functionality

    @Override
    public String getContentPreview() { return text.length() > 50 ? text.substring(0, 50) + "..." : text; }

    @Override
    public long getSize() { return text.getBytes().length; }
}

class ImageContent implements MessageContent {
    private String imageUrl;
    private String thumbnailUrl;
    private int width;
    private int height;
    private long fileSize;

    public ImageContent(String imageUrl, String thumbnailUrl, int width, int height, long fileSize) {
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.width = width;
        this.height = height;
        this.fileSize = fileSize;
    }

    public String getImageUrl() { return imageUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }

    @Override
    public String getContentPreview() { return "📷 Photo"; }

    @Override
    public long getSize() { return fileSize; }
}

class VideoContent implements MessageContent {
    private String videoUrl;
    private String thumbnailUrl;
    private int durationSeconds;
    private long fileSize;

    public VideoContent(String videoUrl, String thumbnailUrl, int durationSeconds, long fileSize) {
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.durationSeconds = durationSeconds;
        this.fileSize = fileSize;
    }

    public String getVideoUrl() { return videoUrl; }
    public int getDurationSeconds() { return durationSeconds; }

    @Override
    public String getContentPreview() { return "🎥 Video (" + durationSeconds + "s)"; }

    @Override
    public long getSize() { return fileSize; }
}

class VoiceNoteContent implements MessageContent {
    private String audioUrl;
    private int durationSeconds;
    private long fileSize;

    public VoiceNoteContent(String audioUrl, int durationSeconds, long fileSize) {
        this.audioUrl = audioUrl;
        this.durationSeconds = durationSeconds;
        this.fileSize = fileSize;
    }

    public String getAudioUrl() { return audioUrl; }
    public int getDurationSeconds() { return durationSeconds; }

    @Override
    public String getContentPreview() { return "🎤 Voice note (" + durationSeconds + "s)"; }

    @Override
    public long getSize() { return fileSize; }
}

// ==================== USER (SRP — data only) ====================
// User does NOT create messages — that's ChatService's responsibility

class User {
    private String userId;
    private String name;
    private String contactNo;
    private Map<String, ChatHistory> conversations;  // conversationId → ChatHistory

    public User(String userId, String name, String contactNo) {
        this.userId = userId;
        this.name = name;
        this.contactNo = contactNo;
        this.conversations = new HashMap<>();
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getContactNo() { return contactNo; }
    public Map<String, ChatHistory> getConversations() { return conversations; }

    public void addConversation(String conversationId, ChatHistory history) {
        conversations.put(conversationId, history);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return userId.equals(((User) o).userId);
    }

    @Override
    public int hashCode() { return userId.hashCode(); }
}

// ==================== MESSAGE (SRP — holds message data) ====================

class Message {
    private String messageId;
    private User sender;
    private User receiver;
    private MessageContent content;
    private LocalDateTime timestamp;
    private MessageState state;
    private boolean isEdited;
    private boolean isDeletedForEveryone;
    private Set<String> deletedForUsers;  // userIds who deleted "for me"
    private LocalDateTime editedAt;

    public Message(String messageId, User sender, User receiver, MessageContent content) {
        this.messageId = messageId;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.state = MessageState.SENT;
        this.isEdited = false;
        this.isDeletedForEveryone = false;
        this.deletedForUsers = new HashSet<>();
    }

    // --- Getters ---
    public String getMessageId() { return messageId; }
    public User getSender() { return sender; }
    public User getReceiver() { return receiver; }
    public MessageContent getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public MessageState getState() { return state; }
    public boolean isEdited() { return isEdited; }
    public boolean isDeletedForEveryone() { return isDeletedForEveryone; }

    // --- State Transitions ---
    public void markDelivered() {
        if (this.state == MessageState.SENT) {
            this.state = MessageState.DELIVERED;
        }
    }

    public void markRead() {
        // Can transition from SENT or DELIVERED to READ
        if (this.state != MessageState.READ) {
            this.state = MessageState.READ;
        }
    }

    // --- Edit ---
    public boolean editContent(MessageContent newContent, User editor) {
        // Only the sender can edit their own message
        if (!editor.getUserId().equals(sender.getUserId())) {
            System.out.println("Only the sender can edit this message.");
            return false;
        }
        this.content = newContent;
        this.isEdited = true;
        this.editedAt = LocalDateTime.now();
        return true;
    }

    // --- Delete ---
    public void deleteForMe(String userId) {
        deletedForUsers.add(userId);
    }

    public void deleteForEveryone(User requester) {
        if (requester.getUserId().equals(sender.getUserId())) {
            this.isDeletedForEveryone = true;
        }
    }

    // Check if message is visible to a specific user
    public boolean isVisibleTo(String userId) {
        if (isDeletedForEveryone) return false;
        return !deletedForUsers.contains(userId);
    }
}

// ==================== CHAT HISTORY (Conversation) ====================

class ChatHistory {
    private String conversationId;
    private User userA;
    private User userB;
    private List<Message> messageHistory;
    private AtomicInteger messageCounter;

    public ChatHistory(String conversationId, User userA, User userB) {
        this.conversationId = conversationId;
        this.userA = userA;
        this.userB = userB;
        this.messageHistory = new ArrayList<>();
        this.messageCounter = new AtomicInteger(0);
    }

    public String getConversationId() { return conversationId; }
    public User getUserA() { return userA; }
    public User getUserB() { return userB; }
    public List<Message> getMessageHistory() { return messageHistory; }

    public void addMessage(Message message) {
        messageHistory.add(message);
    }

    public String generateMessageId() {
        return conversationId + "-MSG-" + messageCounter.incrementAndGet();
    }

    // Get the other participant in this conversation
    public User getOtherUser(User user) {
        return user.getUserId().equals(userA.getUserId()) ? userB : userA;
    }

    // Get messages visible to a specific user (respects delete-for-me)
    public List<Message> getVisibleMessages(String userId) {
        List<Message> visible = new ArrayList<>();
        for (Message msg : messageHistory) {
            if (msg.isVisibleTo(userId)) {
                visible.add(msg);
            }
        }
        return visible;
    }
}

// ==================== CHAT SERVICE (Orchestrator) ====================
// SRP: Coordinates all messaging operations
// Delegates content handling to MessageContent, data to Message, history to ChatHistory

class ChatService {
    private Map<String, User> users;              // userId → User
    private Map<String, ChatHistory> conversations; // conversationId → ChatHistory

    public ChatService() {
        this.users = new ConcurrentHashMap<>();
        this.conversations = new ConcurrentHashMap<>();
    }

    // --- User Management ---
    public void registerUser(User user) {
        users.put(user.getUserId(), user);
        System.out.println("User registered: " + user.getName());
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    // --- Conversation Management ---
    // Generate a deterministic conversationId from two userIds (order-independent)
    private String getConversationId(String userId1, String userId2) {
        // Sort to ensure "A-B" and "B-A" produce the same ID
        String first = userId1.compareTo(userId2) < 0 ? userId1 : userId2;
        String second = userId1.compareTo(userId2) < 0 ? userId2 : userId1;
        return first + "_" + second;
    }

    private ChatHistory getOrCreateConversation(User sender, User receiver) {
        String convId = getConversationId(sender.getUserId(), receiver.getUserId());

        return conversations.computeIfAbsent(convId, id -> {
            ChatHistory history = new ChatHistory(id, sender, receiver);
            sender.addConversation(id, history);
            receiver.addConversation(id, history);
            System.out.println("New conversation created: " + id);
            return history;
        });
    }

    // ========== SEND MESSAGE ==========
    public Message sendMessage(String senderId, String receiverId, MessageContent content) {
        // Validate users exist
        User sender = users.get(senderId);
        User receiver = users.get(receiverId);

        if (sender == null) {
            System.out.println("Error: Sender not found — " + senderId);
            return null;
        }
        if (receiver == null) {
            System.out.println("Error: Receiver not found — " + receiverId);
            return null;
        }

        // Get or create conversation
        ChatHistory conversation = getOrCreateConversation(sender, receiver);

        // Create message (Factory logic inside service — SRP)
        String messageId = conversation.generateMessageId();
        Message message = new Message(messageId, sender, receiver, content);

        // Add to conversation history
        conversation.addMessage(message);

        System.out.println("[" + sender.getName() + " → " + receiver.getName() + "] "
                + content.getContentPreview()
                + " | Status: " + message.getState()
                + " | ID: " + messageId);

        return message;
    }

    // ========== DELIVER MESSAGE (triggered when recipient's device ACKs) ==========
    public void deliverMessage(String messageId, String conversationId) {
        ChatHistory history = conversations.get(conversationId);
        if (history == null) return;

        for (Message msg : history.getMessageHistory()) {
            if (msg.getMessageId().equals(messageId)) {
                msg.markDelivered();
                System.out.println("Message " + messageId + " → DELIVERED ✓✓");
                return;
            }
        }
    }

    // ========== MARK AS READ (triggered when recipient opens the chat) ==========
    public void markConversationAsRead(String conversationId, String readerId) {
        ChatHistory history = conversations.get(conversationId);
        if (history == null) return;

        for (Message msg : history.getMessageHistory()) {
            // Only mark messages RECEIVED by this user (not sent by them)
            if (msg.getReceiver().getUserId().equals(readerId) && msg.getState() != MessageState.READ) {
                msg.markRead();
            }
        }
        System.out.println("All messages in " + conversationId + " marked READ by " + readerId);
    }

    // ========== EDIT MESSAGE ==========
    public boolean editMessage(String messageId, String conversationId, String editorId, MessageContent newContent) {
        ChatHistory history = conversations.get(conversationId);
        if (history == null) return false;

        User editor = users.get(editorId);
        if (editor == null) return false;

        for (Message msg : history.getMessageHistory()) {
            if (msg.getMessageId().equals(messageId)) {
                boolean success = msg.editContent(newContent, editor);
                if (success) {
                    System.out.println("Message " + messageId + " edited to: "
                            + newContent.getContentPreview() + " (edited)");
                }
                return success;
            }
        }
        return false;
    }

    // ========== DELETE MESSAGE ==========
    public void deleteMessage(String messageId, String conversationId, String userId, DeleteType deleteType) {
        ChatHistory history = conversations.get(conversationId);
        if (history == null) return;

        User requester = users.get(userId);
        if (requester == null) return;

        for (Message msg : history.getMessageHistory()) {
            if (msg.getMessageId().equals(messageId)) {
                if (deleteType == DeleteType.DELETE_FOR_ME) {
                    msg.deleteForMe(userId);
                    System.out.println("Message " + messageId + " deleted for " + userId);
                } else if (deleteType == DeleteType.DELETE_FOR_EVERYONE) {
                    msg.deleteForEveryone(requester);
                    System.out.println("Message " + messageId + " deleted for everyone");
                }
                return;
            }
        }
    }

    // ========== TYPING INDICATOR (transient — not stored) ==========
    public void sendTypingIndicator(String senderId, String receiverId) {
        User sender = users.get(senderId);
        User receiver = users.get(receiverId);
        if (sender != null && receiver != null) {
            // In real system: push via WebSocket to receiver's device
            System.out.println(sender.getName() + " is typing...");
        }
    }

    // ========== DELIVER PENDING MESSAGES (when user comes online) ==========
    public void deliverPendingMessages(String userId) {
        User user = users.get(userId);
        if (user == null) return;

        System.out.println("Delivering pending messages for " + user.getName() + "...");

        for (ChatHistory history : user.getConversations().values()) {
            for (Message msg : history.getMessageHistory()) {
                // Messages where this user is the receiver AND status is SENT
                if (msg.getReceiver().getUserId().equals(userId) && msg.getState() == MessageState.SENT) {
                    msg.markDelivered();
                    System.out.println("  Delivered: " + msg.getMessageId()
                            + " from " + msg.getSender().getName());
                }
            }
        }
    }

    // ========== DISPLAY CHAT HISTORY ==========
    public void displayChatHistory(String conversationId, String viewerId) {
        ChatHistory history = conversations.get(conversationId);
        if (history == null) {
            System.out.println("No conversation found.");
            return;
        }

        System.out.println("\n===== Chat: " + history.getUserA().getName()
                + " ↔ " + history.getUserB().getName() + " =====");

        for (Message msg : history.getVisibleMessages(viewerId)) {
            String editTag = msg.isEdited() ? " (edited)" : "";
            String stateIcon = "";
            switch (msg.getState()) {
                case SENT:      stateIcon = "✓"; break;
                case DELIVERED: stateIcon = "✓✓"; break;
                case READ:      stateIcon = "✓✓ (read)"; break;
            }

            System.out.println("  [" + msg.getSender().getName() + "] "
                    + msg.getContent().getContentPreview() + editTag
                    + "  " + stateIcon
                    + "  (" + msg.getTimestamp().toLocalTime() + ")");
        }
        System.out.println("==========================================\n");
    }
}

// ==================== DEMO ====================

public class ChatMessenger {
    public static void main(String[] args) {
        ChatService chatService = new ChatService();

        // 1. Register users
        User alice = new User("u1", "Alice", "+91-9876543210");
        User bob = new User("u2", "Bob", "+91-9876543211");
        chatService.registerUser(alice);
        chatService.registerUser(bob);

        // 2. Alice sends messages to Bob
        System.out.println("\n--- Sending Messages ---");
        Message msg1 = chatService.sendMessage("u1", "u2", new TextContent("Hey Bob! How are you?"));
        Message msg2 = chatService.sendMessage("u2", "u1", new TextContent("Hi Alice! I'm good, thanks!"));
        Message msg3 = chatService.sendMessage("u1", "u2",
                new ImageContent("https://img.example.com/photo.jpg", "https://img.example.com/thumb.jpg", 1920, 1080, 2048000));
        Message msg4 = chatService.sendMessage("u1", "u2",
                new VoiceNoteContent("https://audio.example.com/voice.ogg", 15, 128000));

        // 3. Simulate: Bob's device ACKs (messages delivered)
        System.out.println("\n--- Bob Comes Online (messages delivered) ---");
        String convId = "u1_u2";
        chatService.deliverPendingMessages("u2");

        // 4. Bob opens the chat (messages marked as read)
        System.out.println("\n--- Bob Opens Chat (messages read) ---");
        chatService.markConversationAsRead(convId, "u2");

        // 5. Display chat
        chatService.displayChatHistory(convId, "u1");

        // 6. Alice edits a message
        System.out.println("--- Alice Edits a Message ---");
        chatService.editMessage(msg1.getMessageId(), convId, "u1", new TextContent("Hey Bob! How's it going?"));

        // 7. Bob deletes a message "for me"
        System.out.println("\n--- Bob Deletes a Message (for me) ---");
        chatService.deleteMessage(msg3.getMessageId(), convId, "u2", DeleteType.DELETE_FOR_ME);

        // 8. Display chat from Bob's perspective (image should be hidden)
        chatService.displayChatHistory(convId, "u2");

        // 9. Display chat from Alice's perspective (image still visible)
        chatService.displayChatHistory(convId, "u1");

        // 10. Typing indicator
        System.out.println("--- Typing Indicator ---");
        chatService.sendTypingIndicator("u1", "u2");

        // 11. Try sending to non-existent user
        System.out.println("\n--- Edge Case: Non-existent User ---");
        chatService.sendMessage("u1", "u999", new TextContent("Hello?"));
    }
}