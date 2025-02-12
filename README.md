# city-data-management
## Business Requirement
Implement a simple application which organizes data for a city. It should store data for the city’s water supply, electricity and waste. All three need to be tracked with own attributes (which you can define creatively in a way it makes sense to you). 

At least one of the data streams should come in through integration, one should be uploaded and one should be entered manually. The user should be able to analyze the data on some UI. All implemented features should be covered by automated tests.

## High Level Diagram and Components
<img width="1428" alt="image" src="https://github.com/user-attachments/assets/8803d64f-a582-4d07-b0e4-f6285c62f8fd" />

### Component description
**GeoDNS:** return different IP addresses based on the user's geographic location. This helps distribute traffic efficiently between different data centers, ensuring faster response times, reduced latency, and better load balancing.

**API Gateway** acts as the entry point for all external and internal client requests. It provides key functionalities such as authentication, request routing, rate limiting, logging, and load balancing.

**Front End** provides a browser-based UI for users to interact with the system. It allows users to upload data, view reports, analyze trends and more features.

**Ingestion Service** is responsible for receiving, validating, and processing incoming data from multiple sources (file uploading, entered manually from UI or third party provider integration) before storing it in the database or passing it to other services via messaging systems (e.g., Kafka).

**Object Storage** When a user uploads a file (CSV, JSON, XML), the Ingestion Service stores the raw file in Object Storage. This ensures data durability, so even if processing fails, the original file remains available for reprocessing.

**Water service** is responsible for managing and processing data related to a city's water supply. It ensures that water-related data is stored, processed, and made available for analysis.

**Electricity Service** is responsible for handling and managing electricity-related data, including power consumption and outages. It ensures that electricity data is stored, processed, and made available for real-time monitoring and analytics.

**Waste Service** is responsible for tracking, managing, and analyzing waste collection and disposal data in the city. It ensures efficient waste management by processing incoming data, storing structured records, and exposing insights for monitoring and analytics.

**Kafka** serves as the central event-streaming platform for your city data management system. It decouples services, ensures scalability, and enables real-time data processing by allowing services to publish and subscribe to events asynchronously.

**Provider Integration Gateway** acts as a bridge between external data providers and our system. It ensures seamless data ingestion from third-party sources, transforming, validating, and publishing data for further processing.

**Analytics Service** is responsible for processing, aggregating, and analyzing city data collected from different sources, such as water supply, electricity, and waste services via messaging systems. It provides insights, reports, and visualizations to help users monitor trends, detect anomalies, and make informed decisions.

## Functional Design
### Entered data manually flow
![image](https://github.com/user-attachments/assets/2f76eb20-d215-4c9c-9354-ae46365780d1)

### Bulk import through uploading file flow
![image](https://github.com/user-attachments/assets/42f573b1-7ba1-47e2-ad39-09c479c270b9)

### Integrate with provider flow
![image](https://github.com/user-attachments/assets/c5d359bf-744e-4c3c-af32-6ce78f38bbe1)


### Get metric information
![image](https://github.com/user-attachments/assets/64a0a083-a9ab-44d0-9824-f656f48202f4)

## Data Management
### Ingestion Service
**uploads** Collection
```json
{
  "_id": "65d1a7f6b23e8a001c3a5e92", // Unique file ID
  "fileName": "city_data.csv", // Name of the uploaded file
  "filePath": "s3://city-storage/uploads/city_data.csv", // Object Storage location
  "status": "PROCESSING", // PENDING, PROCESSING,  COMPLETED, FAILED
  "uploadedBy": "123", // The user uploaded file
  "createdAt": "2025-02-11T12:30:00Z", // Timestamp of file upload
  "updatedAt": "2025-02-11T12:35:00Z" // Last status update
}
```

**tasks** Collection
```json
{
  "_id": "65d1a82fb23e8a001c3a5e94", // Unique task ID
  "source": "UPLOAD", // source of task: ENTERED_MANUALLY, UPLOAD, INTEGRATION
  "serviceType": "WATER", // Type of data: WATER, ELECTRICITY, WASTE
  "uploadId": "65d1a7f6b23e8a001c3a5e92", // Links to uploads._id, available for UPLOAD source only.
  "rawData": { // raw data, base on source and service_type
    "customerId": "12345",
    "consumption": 20.5,
    "billingCycle": "2025-02"
  },
  "status": "COMPLETED", // PENDING, PROCESSING,  COMPLETED, FAILED
  "createdAt": "2025-02-11T12:31:00Z" // Task creation timestamp
}
```

### Water Service
**water_usage** Collection
```json
{
  "_id": "65d1a8a0b23e8a001c3a5e96", // Unique record ID
  "taskId": "65d1a82fb23e8a001c3a5e94", // Links to tasks._id
  "customerId": "12345", // Unique customer identifier
  "consumption": 20.5, // Water usage in cubic meters
  "billingCycle": "2025-02", // YYYY-MM format
  "createdAt": "2025-02-11T12:40:00Z" // Record creation timestamp
}
```

### Electricity Service
**electricity_usage** Collection
```json
{
  "_id": "65d1a900b23e8a001c3a5e98", // Unique record ID
  "taskId": "65d1a82fb23e8a001c3a5e94", // Links to tasks._id
  "customerId": "12345", // Unique customer identifier
  "consumption": 350.2, // Electricity usage in kWh
  "billingCycle": "2025-02", // YYYY-MM format
  "createdAt": "2025-02-11T12:45:00Z" // Record creation timestamp
}
```

### Waste Service
**waste_usage** Collection
```json
{
  "_id": "65d1a950b23e8a001c3a5e9a", // Unique record ID
  "taskId": "65d1a82fb23e8a001c3a5e94", // Links to tasks._id
  "customerId": "12345", // Unique customer identifier
  "wasteAmount": 15.2, // Weight of waste in kg
  "collectionDate": "2025-02-15", // Scheduled collection date
  "createdAt": "2025-02-11T12:50:00Z" // Record creation timestamp
}
```
### Analytics Service
**aggregated_data** Collection
```json
{
  "_id": "65d1a9f0b23e8a001c3a5e9c", // Unique record ID
  "serviceType": "WATER", // Type of data: WATER, ELECTRICITY, WASTE
  "metricName": "TOTAL_USAGE", // Type of metric (TOTAL_USAGE, AVERAGE_USAGE...)
  "value": 325.7, // Aggregated metric value
  "timeBucket": "2025-02-11T13:00:00Z" // Time-based aggregation (e.g., hourly)
}
```

## Scalability
### API Gateway
- Can be horizontally scaled by adding more instances behind a load balancer.
- Stateless, meaning new instances can be spun up dynamically.

### Ingestion Service
- Can be horizontally scaled by adding more instances
- Each instance can have multiple consumers or 1 consumer but pull batch data from Kafka and parallel process message.

### Kafka
- Kafka scales linearly by increasing the number of brokers and partitions.
- Replication factor ensures high availability and fault tolerance.
- Different topics for each type of data (water, electricity, waste, etc.) prevent congestion.

### Water, Electricity, and Waste Services
- These services consume events asynchronously, meaning they scale independently.
- Stateless services can be horizontally scaled using Kubernetes or a similar orchestration tool.
- Each service writes to its own dedicated database, reducing contention.

### Analytics Service
- Asynchronous processing ensures that data aggregation can scale independently.
- Uses Kafka consumers, which can be horizontally scaled.
- Writes aggregated data to a separate database to avoid interfering with real-time operations.

### Object Storage
- Cloud-based object storage (e.g., AWS S3, GCS, MinIO) is inherently scalable.

### Provider Integration Gateway
- Works as a middleware, routing data between external providers and Kafka.
- Stateless, meaning it can be horizontally scaled.
- Asynchronous processing ensures high availability.

### Database Layer (MongoDB)
- Can be horizontally scaled using sharding.
- Read replicas can be used to distribute read-heavy workloads.
