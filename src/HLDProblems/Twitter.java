package src.HLDProblems;
// The things that I though

// # How I though flow should be
// - All requirements
// - How the flow should be
// - What classes / Interface I will make
// - About relationships between class

// # Requirements:
// 1. Post a tweet - 😊
// 2. In feed with your followers tweets, famous tweet also be visible
//  - they should be visible in order they are tweeted - 🆗, but better 
//  - Every thing we see on twitter is timeline: 3 TYPES of TIMELINES: 
//  - 1. User could see thwir own tweets & retweets, 2. Trending Hashtags & tweets as per you search 3. HOMW page, you see your followers & generixc tweets
// Could see trending hashtage as perlocation & other filters(category etc)
// 3. You can follow people, increase & decrease followers - ❌ I didn't included it 
// -----Aboce three were most imp
// 4. Like & share & comments on tweets
// 5. Receive notifications on tweet
// 6. Tweets sorted on the basis of category

// ----Talking about requiremtns that I missed & they way I shoul present them or the keywordds that should be used
// 1. 

// #NFRs:
// 1. Average no. of users using twitter 😊 - count = 300M+❌
// 2. what is imp - Availability - No. of machines, Size of machine- RAM & Hardisk😊 - Eventual consitency❌
// 3. What cache to use😊 - Read Heavy❌ - Writes = 600 writes/s, Reads = 600,000 tweets/s
// No of characters per tweet ❌
// 4. Latency - Low

// Questions:
// 1. What about GPUs, why are they used, are they same as servers, are servers only GPUs?

// FollowUps: 
// 1. How do you handle a person who has huge followers
// 2. Thinking to save data in cache in UserTimeline & HomeTimeLine, But what about a person who is very inactive, isin't it would be unnecessary use of cache

// Tech used:
// 1. DBs = Cassandra
// 2. Caching = Redis
// 3. Stream = Apache Storm, Kafka Streams
 
// HLD Diagram, draw.io
// https://app.diagrams.net/#G1W2CS7OWSeGEsbds8ZBYbNvGLeQZUKe-g#%7B%22pageId%22%3A%22udC_Wkb1uGv7LdA8Ti4_%22%7D
public class Twitter {
    
}

