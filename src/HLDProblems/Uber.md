# Funcational Requirements:
1. user could be able to select pickup and drop location
2. Get options for ride vehicles - car(SUV, Sedan)
3. Get estimated prices on the baisis of vehicle and distance
4. Rider should be atched with nearby available driver
5. Driver should be able to accept / deny 
6. Both rider and river can track each other real time
7. Ratings for each other
8. Payment 

# Non Functional Requirements:
1. Scalability :  Millions of users and drivers
2. CAP theorem : Compromise betweenhigh consistency and availability - For users it should be highly available, for drivers it should be highly consistent
3. Latency : <1 min drivers hould beassisgned, but if not assigned, so the request should be cancelled

# Core Entity:
1. Rider
2. Driver
3. Ride
4. Location
5. Payment / Fare

# API Design:

1. USER:
- GET : /v1/api/getFare?pickUpLat=...&pickUpLong=...&dropLat=...&dropLong=... => List<Fare> with RequestId
- POST : /v1/api/rides/requestRide {body : requestId} => Ride with driver details
- GET : /v1/api/rides/history
- POST : /v1/api/cancel/{riderId}
- POST : /v1/api/ratings/{riderId}

2. DRIVER:
- WS : real time movement on map
- POST : /v1/api/ride/rides {body : requestId, accept/deny} => Ride with driver details
- POST : /v1/api/ride/{rideId}/start
- POST : /v1/api/ride/{rideId}/end
 
# POINTS:
1. Proximity Search
2. Notification service
3. Redis Lock
4. Zookeeper 
5. Better to use separate load balancer for web socket connection
6. Request goes to one driver at a time and then timeout
