package src.DesignPatterns;

import java.util.*;

// Eager Initialization
class DBConnection {
    private static final DBConnection db = new DBConnection();
    DBConnection() {}

    public static DBConnection getInstance() {
        return db;
    }
}

// Lazy not thread safe
class DBConnectionLazyNotThreaded {
    private static DBConnectionLazyNotThreaded db;
    DBConnectionLazyNotThreaded() {}

    public static DBConnectionLazyNotThreaded getInstance() {
        if(db==null) {
            return new DBConnectionLazyNotThreaded();
        }
        return db;
    }
}

// Lazy thread safe, double checked locking
class DBConnectionLazyThreaded {
    private static DBConnectionLazyThreaded db;
    DBConnectionLazyThreaded() {}

    public static DBConnectionLazyThreaded getInstance() {
        if(db==null) {
            synchronized(DBConnectionLazyThreaded.class)
            { 
                if(db == null) {
                    return db = new DBConnectionLazyThreaded();
                }
            }
        }
        return db;
    }
}

// Using Sungleton as Cache
class Cache {
    public static Cache instance;
    Map<String, String> mp = new HashMap<>();

    Cache() {
    }

    public static Cache getInstance() {
        if(instance==null) {
            synchronized(Cache.class)
            { 
                if(instance == null) {
                    System.out.println("creating new cache instance");
                    return instance= new Cache();
                }
            }
        }
        return instance;
    }

    public String get(String key) {
        return mp.getOrDefault(key, "");
    }

    public String put(String key, String val) {
        return mp.put(key, val);
    }
}

public class Singleton {
    public static void main(String[] args) {
        DBConnection ins1 = DBConnection.getInstance();
        DBConnection ins2 = DBConnection.getInstance();
        System.out.println(ins1 == ins2);

        Cache ch = Cache.getInstance();
        
        ch.put("Priya", "Vinay");
        ch.put("Vinay", "Loves Priya");

        System.out.println("1"+ch.get("Vinay"));

        Cache ch1 = Cache.getInstance();
        System.out.println("2"+ ch1.get("Vinay"));
     
  
    }
}
