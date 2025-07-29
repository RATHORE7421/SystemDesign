package src.HLDProblems;
// -----What I thought-----
// 1. Long URL needs to be shortened to short one, which should be unique & avoid collisions for differnt URL and same for same URL, even though it might have been asked to shorten multiple times
//    - Support custom alias
//    - Support expiration time
// 2. Functionality to redirtect when clicking on short URL
// 3. Going to use, hashmap, hash function, the unique code that has been retruned from the hash function would convert it to string & then woukd make short URL via it

// NFRs
// 1. Cache - Read heavy  - to reduce latency (~200ms)
// 2. Scale to support 100M Daily active users & 1B urls
// 2. Consistency ismore imp here, since need to redirect on the URL always❌, eventual consistency & availability

// Core entities:
// 1. Long url, 2. Short URL, 3. User

// API: 
// POST -> shortURL(long url, custom Alias, Expiry time)
// 

// More things to do: 
// 1. differnt approaches for shortning url
// 2. Calculate request per second and size of DB used

// class URL {
//     public String shortUrl;
//     public String longUrl;
//     public int expirationTime;
//     public String alias;

//     public void URL(String shortUrl, String longUrl, Int expirationTime, String alias) {

//     }
// }

// class ShortUrl{
//     private static counter = 0;
//     Set<Integer> aliasSet = new HashSet<>();
//     Set<String, String> aliasSet = new HashSet<>();
    
//     public String getShortUrl(String longUrl, String expirationTime, String alias) {
//         String stringUniqueCode = alias;
//         if(alias){
//             bool duplicate = checkAvailability(alias);
//             if(duplicate){
//                 return new IllegalArgumentException("Alias already exists.");
//             }
//         }else{
//             do{
//                 stringUniqueCode = convertToString(++counter);
//             } while(checkAvailability(stringUniqueCode));
//         }
//         String shortUrl = createShortUrl(stringUniqueCode);
//         URL url = new URL(shortUrl, longUrl, expirationTime, alias);
//         return shortUrl;
//     }

//     private boolean checkAvailability(String alias) {
//         if(aliasSet.contains(alias))
//             return true;
//         return false;
//     }

//     private String convertToString(int hashCode) {
//         String value = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
//         String ans = new StringBuilder();
//         while(hashCode>0) {
//             ans.append(value.charAt((int)(hashCode % 62)));
//             hashCode/=62;
//         }
//         ans.reverse.toString();
//         return ans;
//     }

//     private String createShortUrl(String code) {
//         return "https://" + code + ".com";
//     }
// }

// public static void main(String args[]) {
//     Scanner sc = new Scanner(System.in);
//     String longUrl = sc.nextLine();
//     String expirationTime = sc.nextInt();
//     String alias = sc.nextLine();

//     ShortUrl objShort = new ShortUrl();
//     String shortUrl = objShort.getShortUrl(longUrl, expirationTime, alias);

//     redirectToCorrectLocation(shortUrl);
// }
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Entity class
class URLMapping {
    private final String shortUrlKey;
    private final String originalURL;
    private final Date creationDate;
    private final Date expirationDate;
    private int clickCount;

    public URLMapping(String shortUrlKey, String originalUrl, Date creationDate, Date expirationDate) {
        this.shortUrlKey = shortUrlKey;
        this.originalURL = originalUrl;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.clickCount = 0;
    }

    public String getShortUrlKey() { return shortUrlKey; }
    public String getOriginalUrl() { return originalURL; }
    public Date getCreationDate() { return creationDate; }
    public Date getExpirationDate() { return expirationDate; }
    public int getClickCount() { return clickCount; }
    public void incrementClickCount() { this.clickCount++; }
}

// Repository Layer
interface URLRepository {
    void save(URLMapping urlMapping);
    URLMapping findByKey(String shortUrlKey);
    boolean exists(String key);
}

class InMemoryURLRepository implements URLRepository {
    private final Map<String, URLMapping> storage = new ConcurrentHashMap<>();

    @Override
    public void save(URLMapping urlMapping) {
        storage.put(urlMapping.getShortUrlKey(), urlMapping);
    }

    @Override
    public URLMapping findByKey(String shortUrlKey) {
        return storage.get(shortUrlKey);
    }

    @Override
    public boolean exists(String key) {
        return storage.containsKey(key);
    }
}

class Base62Encoder {
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encode(long value) {
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int)(value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }
}

// Service Layer
class UrlShortnerService {
    private final URLRepository repository;
    private long idCounter = 100000; // starting ID to avoid short URLs like "a"
    private final Set<String> reservedWords = Set.of("admin", "help", "about");

    public UrlShortnerService(URLRepository repo) {
        this.repository = repo;
    }

    public String shortenUrl(String longUrl, Date expirationDate, Optional <String> customAlias) {
        String key;
        if(customAlias.isPresent()) {
            key = customAlias.get();
            validateCustomAlias(key);
            if (repository.exists(key)) {
                throw new IllegalArgumentException("Alias already exists.");
            }
        }
        else {
            do {
                key = Base62Encoder.encode(idCounter++);
            } while (repository.exists(key));
        }

        URLMapping shortUrlMapping = new URLMapping(key, longUrl, new Date(), expirationDate);
        repository.save(shortUrlMapping);
        return key;
    }

    private void validateCustomAlias(String alias) {
        if (!alias.matches("[a-zA-Z0-9_-]+") || reservedWords.contains(alias)) {
            throw new IllegalArgumentException("Invalid or reserved alias");
        }
    }

    public String redirect(String shortUrl) {
        URLMapping mapping = repository.findByKey(shortUrl);
        if (mapping == null) {
            throw new NoSuchElementException("Short URL not found");
        }
        if (new Date().after(mapping.getExpirationDate())) {
            throw new IllegalStateException("Link expired");
        }
        mapping.incrementClickCount();
        return mapping.getOriginalUrl();
    }
}

// Controller / Simulation  
public class UrlShortnerApp{
    public static void main(String[] args){
        URLRepository repo = new InMemoryURLRepository(); 
        UrlShortnerService service = new UrlShortnerService(repo);

        String shortKey1 = service.shortenUrl(
                                            "https://leetcode.com/problems/shortest-path-in-binary-matrix/",
                                            addDays(30), Optional.empty());
        String shortKey2 = service.shortenUrl("https://registrationandtouristcare.uk.gov.in/dham_detail.php?dham=Mg==",
                                            addDays(30), Optional.of("kedarnath"));

        System.out.println("Shortened URl1: " + shortKey1);
        System.out.println("Shortened URl2: " + shortKey2);

        System.out.println("Redirecting key 1: " + service.redirect(shortKey1));
        System.out.println("Redirecting key 2: " + service.redirect("kedarnath"));
    }

    private static Date addDays(int days){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }
}
