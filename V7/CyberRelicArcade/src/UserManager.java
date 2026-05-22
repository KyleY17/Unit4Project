import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * UserManager — Handles user registration, login, credential storage, high scores, coins, and shop purchases.
 * 
 * User data is stored in a local CSV file for simplicity.
 * Format: username,password_hash,highscore,coins,shop_items
 * Where shop_items is a semicolon-separated list of purchased item IDs
 */
public class UserManager {
    private static final String USERS_FILE = "users.txt";
    private final Map<String, String>  users;           // username -> hashed_password
    private final Map<String, Long> highScores;        // username -> high score
    private final Map<String, Long> coins;             // username -> coins
    private final Map<String, Set<String>> shopItems;  // username -> set of purchased item IDs

    public UserManager() {
        users = new HashMap<>();
        highScores = new HashMap<>();
        coins = new HashMap<>();
        shopItems = new HashMap<>();
        loadUsers();
    }

    /**
     * Load users from file. If file doesn't exist, start with empty user list.
     */
    private void loadUsers() {
        try {
            if (Files.exists(Paths.get(USERS_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
                for (String line : lines) {
                    String[] parts = line.split(",", -1);  // -1 to keep empty strings
                    if (parts.length >= 2) {
                        String username = parts[0];
                        String passwordHash = parts[1];
                        users.put(username, passwordHash);
                        
                        // Load high score if it exists
                        if (parts.length >= 3 && !parts[2].isEmpty()) {
                            try {
                                long score = Long.parseLong(parts[2]);
                                highScores.put(username, score);
                            } catch (NumberFormatException e) {
                                highScores.put(username, 0L);
                            }
                        } else {
                            highScores.put(username, 0L);
                        }
                        
                        // Load coins if it exists
                        if (parts.length >= 4 && !parts[3].isEmpty()) {
                            try {
                                long userCoins = Long.parseLong(parts[3]);
                                coins.put(username, userCoins);
                            } catch (NumberFormatException e) {
                                coins.put(username, 0L);
                            }
                        } else {
                            coins.put(username, 0L);
                        }
                        
                        // Load shop items if they exist
                        if (parts.length >= 5 && !parts[4].isEmpty()) {
                            Set<String> items = new HashSet<>();
                            String[] itemIds = parts[4].split(";");
                            for (String itemId : itemIds) {
                                if (!itemId.isEmpty()) {
                                    items.add(itemId);
                                }
                            }
                            shopItems.put(username, items);
                        } else {
                            shopItems.put(username, new HashSet<>());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    /**
     * Save users to file.
     */
    private void saveUsers() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String username : users.keySet()) {
                String passwordHash = users.get(username);
                long highScore = highScores.getOrDefault(username, 0L);
                long userCoins = coins.getOrDefault(username, 0L);
                Set<String> items = shopItems.getOrDefault(username, new HashSet<>());
                String itemsStr = String.join(";", items);
                
                sb.append(username).append(",")
                  .append(passwordHash).append(",")
                  .append(highScore).append(",")
                  .append(userCoins).append(",")
                  .append(itemsStr).append("\n");
            }
            Files.write(Paths.get(USERS_FILE), sb.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    /**
     * Simple hash function for passwords. In production, use bcrypt or similar.
     */
    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    /**
     * Register a new user. Returns true if successful, false if user already exists.
     */
    public boolean register(String username, String password) {
        // Validate inputs
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return false;
        }

        username = username.trim();

        // Check if user already exists
        if (users.containsKey(username)) {
            return false;
        }

        // Add user and save
        users.put(username, hashPassword(password));
        highScores.put(username, 0L);
        coins.put(username, 0L);
        shopItems.put(username, new HashSet<>());
        saveUsers();
        return true;
    }

    /**
     * Authenticate a user. Returns true if credentials are correct.
     */
    public boolean login(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        username = username.trim();

        // Check if user exists and password matches
        if (!users.containsKey(username)) {
            return false;
        }

        String storedHash = users.get(username);
        String providedHash = hashPassword(password);

        return storedHash.equals(providedHash);
    }

    /**
     * Check if a username already exists.
     */
    public boolean userExists(String username) {
        return users.containsKey(username.trim());
    }

    /**
     * Get the high score for a user.
     */
    public long getHighScore(String username) {
        if (username == null) return 0L;
        return highScores.getOrDefault(username.trim(), 0L);
    }

    /**
     * Save a high score for a user if it's higher than their current high score.
     * Returns true if a new high score was set.
     */
    public boolean saveHighScore(String username, long score) {
        if (username == null)
             return false;
        
        username = username.trim();
        if (!users.containsKey(username))
             return false;

        long currentHighScore = highScores.getOrDefault(username, 0L);
        if (score > currentHighScore) {
            highScores.put(username, score);
            saveUsers();
            return true;
        }
        return false;
    }

    /**
     * Get top 10 players sorted by high score (descending).
     * Returns a list of [username, score] string pairs.
     */
    public List<String[]> getTopScores(int limit) {
        List<String[]> topScores = new ArrayList<>();
        
        highScores.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(limit)
            .forEach(entry -> topScores.add(new String[]{entry.getKey(), String.valueOf(entry.getValue())}));
        
        return topScores;
    }

    /**
     * Get coins for a user.
     */
    public long getCoins(String username) {
        if (username == null) return 0L;
        return coins.getOrDefault(username.trim(), 0L);
    }

    /**
     * Add coins to a user's balance.
     */
    public void addCoins(String username, long amount) {
        if (username == null || amount < 0) return;
        
        username = username.trim();
        if (!users.containsKey(username)) return;
        
        long currentCoins = coins.getOrDefault(username, 0L);
        coins.put(username, currentCoins + amount);
        saveUsers();
    }

    /**
     * Subtract coins from a user's balance. Returns true if successful.
     */
    public boolean spendCoins(String username, long amount) {
        if (username == null || amount < 0) return false;
        
        username = username.trim();
        if (!users.containsKey(username)) return false;
        
        long currentCoins = coins.getOrDefault(username, 0L);
        if (currentCoins < amount) return false;
        
        coins.put(username, currentCoins - amount);
        saveUsers();
        return true;
    }

    /**
     * Check if a user has purchased a shop item.
     */
    public boolean hasPurchasedItem(String username, String itemId) {
        if (username == null || itemId == null) return false;
        
        username = username.trim();
        Set<String> items = shopItems.getOrDefault(username, new HashSet<>());
        return items.contains(itemId);
    }

    /**
     * Purchase a shop item for a user.
     */
    public void purchaseItem(String username, String itemId) {
        if (username == null || itemId == null) return;
        
        username = username.trim();
        if (!users.containsKey(username)) return;
        
        Set<String> items = shopItems.getOrDefault(username, new HashSet<>());
        items.add(itemId);
        shopItems.put(username, items);
        saveUsers();
    }

    /**
     * Get all purchased items for a user.
     */
    public Set<String> getPurchasedItems(String username) {
        if (username == null) return new HashSet<>();
        
        username = username.trim();
        return new HashSet<>(shopItems.getOrDefault(username, new HashSet<>()));
    }
}
