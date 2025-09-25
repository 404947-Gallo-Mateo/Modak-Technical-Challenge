# Backend Technical Challenge for Modak
## Rate-Limited Notification Service

--------

# About this project
This project is a notification system that also prevents excessive notifications from being sent to receiving users, 
based on predefined rules for each type of notification. These rules define the notification limit and the time period.  
> To ensure compliance with these rules, the token bucket algorithm was implemented. In this case, it allows the 
notification to be sent only if there are enough tokens available; otherwise, it indicates how much time (in seconds) remains until the next token is regenerated.
This algorithm only generates tokens when the bucket is not full, precisely calculates the waiting time, is easy to 
configure in terms of limits and time periods, and works in a thread-safe manner using StampedLock.

Try it! Execute the project and go to http://localhost:8080/swagger-ui.html  

Check the Postman Collection 'Modak Rate Limit Challenge.postman_collection.json' at \tc\src\main\resources

--------

# Technologies used
-  Spring Boot 3.5.6 - Framework
-  Maven 4.0.0 - Dependencies Management
-  Java 21 - Programming language
-  Spring Data JPA - Data persistence
-  H2 Database - Embedded database
-  ModelMapper - Object mapping Entity/DTO
-  JUnit 5 + Mockito 5.2.0 - Testing


--------

# How to add a new NotificationType 
  1. Go to class NotificationType then add a new enum with his specific key
   ```java
   // example:
   public enum NotificationType {
    // name("key"),
    SPAM("spam"), ...
   ```
  2. In the application-dev.properties file (this may vary depending on the profile used, e.g., "dev", "prod", etc.), the following three parameters must be added: 
   - [key].max-requests=1
   - [key].duration=365
   - [key].unit=DAYS

   ```properties
    # example:
    rate.limit.rules.spam.max-requests=1
    rate.limit.rules.spam.duration=365
    rate.limit.rules.news.unit=DAYS
   ```
--------

# NotificationTypes already included

| NotificationType | Max Requests | Duration | Time Unit |    
|------------------|:------------:|:--------:|:----------|
| STATUS           |      2       |    2     | MINUTES   |  
| NEWS             |      1       |    1     | DAYS      |
| MARKETING        |      3       |    1     | HOURS     |
| URGENT           |      10      |    1     | MINUTES   |
| WEEKLY_REPORT    |      1       |    7     | DAYS      |


--------

# Structure  
> Below I will briefly explain each folder and its contents.

## *src ➝ main ➝ ...*
### *config*
> aqui se encuentran las clases de configuracion.
- *MappersConfig* - Contains configuration for modelMapper, mergerMapper y objetMapper
- *RateLimitConfig* - Contains RateLimitRules defined in application-dev.properties and a method to access a specific Rule 

--------

### *controllers*
> Controller layer – exposes REST-type endpoints through various specialized controllers.
- *NotificationAuditController* - Exposes endpoints to query all audit records of the notifications that have been sent
- *NotificationController* - Provides two endpoints:
    - "/send" for sending a notification to a user, specifying the message, notification type, and user ID.  
    - "/rate-limit-status/{userId}" to check how many notifications, and of which type, can currently be sent to a user based on their available tokens.

--------

### *exceptions*
> It contains the global exception handler and 2 custom exceptions.
- *GlobalExceptionHandler* - Handles both Java and custom exceptions
- *RateLimitExceededException* - Thrown when the notification limit defined in the specific Rule of the NotificationType is exceeded
- *RuleNotFoundException* - Thrown when no Rule is found for the NotificationType passed as a parameter (its rules are not defined in application-dev.properties)

--------

### *models*
> It contains the models, DTOs, enums, entities, etc. used in the project.
- *NotificationResponse* - Sent as a response to a request to the /send endpoint. It contains the message, notification 
                        status, notification ID, number of notifications available to send (for this user and NotificationType), and creation date.
- *RateLimitRule* - Contains the rules for each NotificationType. The tokensPerSecond attribute is calculated based on 
                    the rules defined in application-dev.properties.
- *TockenBucket* - Contains the attributes of maximum capacity, tokens generated per second, available tokens, the 
                   Instant of the last refill, and the lock (StampedLock object). The latter allows blocking access to 
                   the TokenBucket until the ongoing operation finishes (a read operation blocks writes but allows reads; 
                   a write operation blocks both reads and writes).

### *models ➝ DTOs*
- *NotificationAuditDTO* - Data Transfer Object for NotificationAuditEntity

### *models ➝ entities*
- *NotificationAuditEntity* - Entity that stores the Notification Type, user ID, message, creation date, and status of a notification

### *models ➝ enums*
- *NotificationType* - Enum that indicates the Notification Type and its key to access the rules defined in application-dev.properties

### *models ➝ records*
- *NotificationRequest* - Record that specifies the parameters required to send a notification: message, user ID, and Notification Type

--------

### *repositories*
> Data access layer, contains different repositories to access various tables and queries. 
- *NotificationAuditRepository* - allows interaction with the database, specifically with the NotificationAuditEntity table

--------

### *services*
> Define the contract that any implementing class must fulfill
- *NotificationAuditService* - Service layer interface for the NotificationAuditEntity object, returns NotificationAuditDTO
- *NotificationService* - Interface that defines the methods required for the Notification Service
- *RateLimitService* - interface that defines the methods required for the Notification Rate Limiting Service

### *services ➝ Impl*
> Implementations of the methods defined in the interface contracts.
- *MockGateway* - “simulates” a Gateway, simply logging to the console which user a notification was sent to
- *NotificationAuditServiceImpl* - contains several methods to query the NotificationAuditEntity table. It can search by user ID, notification type, status, and date ranges
- *NotificationServiceImpl* - implements the logic of two methods:
    - sendNotification() sends a notification to a user, verifying if they have the required tokens. After sending, 
                         it processes how many send requests remain and stores the notification information in the database.
    - getRateLimitStatus() queries how many notifications (and of which type) can be sent to a user according to their 
                           currently available tokens.
- *RateLimitServiceImpl* - implements the methods related to managing TokenBuckets and enforcing Rules:  
    - isAllowed() looks up the specific Rule, then the user’s TokenBucket, and finally determines whether they have the required token.  
    - getUserBucket() retrieves the TokenBucket specified by the parameters: user ID, notification type, and Rule.  
    - getCurrentUsage() obtains the number of requests available for each active TokenBucket of the given user.  
    - getRemainingQuota() indicates the number of tokens available for a specific user and notification type.   
    - getRetryAfterSeconds() returns how many seconds remain until the next token becomes available.  

---------

## *src ➝ test ➝ ...*
### *services*
> Contains Unit Tests for all methods in each Service
- *NotificationAuditServiceUnitTests*
- *NotificationServiceUnitTests*
- *RateLimitServiceUnitTests*

