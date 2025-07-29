package src.HLDProblems;
// Requirements:
// - Choose pickup & drop point & the fare would be calculated
// - Search for the driver
// - Driver can pick/deny the request & navigate to pick & drop off location
// - OTP shared & verified, Ride started 
// - Tracking the movement
// - Payment 

// Non functional requuients:
// - Daily active users: 100M
// - Availability ❌, its consistency actually - context is driver, need to mention it explicitly, hghtly available except matching 
// - Caching in case of location 
// - Low latency - driver can be able to pick the request in less than 1 min  
// - Handle high throughput, in peak hours

// Core entities:
// - Ride - Start location, end location, start time, end time, Driver, fare, 
// - Driver -  isAvailable, list<Ride>, 

// API:
// - 

public class Uber {
    
}
