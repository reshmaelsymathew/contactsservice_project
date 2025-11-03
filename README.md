#  contactsservice_project

This project is a Spring Boot application that provides a Contacts Service, handling GET and POST operations and utilizing **PostgreSQL** for data persistence and **Apache Kafka** for asynchronous event publishing.

---

##  Prerequisites

Ensure the following prerequisites are installed and running before starting the application:

* **Java Development Kit (JDK) 17+**
* **Apache Maven**
* **PostgreSQL (Version 16.9-2 recommended)**
* **Apache Kafka (Version 2.13-3.7.0 recommended)**
* **Docker** (for building and running the containerized version)
* **Postman** (for testing the API)

---

##  Setup and Configuration Guide

Follow these sequential steps to set up the necessary infrastructure.

### 1. Database Setup (PostgreSQL)

1.  **Install PostgreSQL:** Install **PostgreSQL 16.9-2** (or a compatible version).
2.  **Create Database:** After installation, create the required database named `contactdb` using the appropriate management tool (e.g., pgAdmin, psql). Refer to the scripts contacts.sql in the `resources/sql` folder.
3.  **Create Table:** Create the table named `contacts` within the `contactdb` using  script contactsdb_create.sql in the `resources/sql` folder.

### 2. Kafka Setup and Topic Creation

1.  **Install Kafka:** Install **Apache Kafka 2.13-3.7.0**.
2.  **Start Zookeeper:** Start the Zookeeper service from your Kafka installation directory:

    ```bash
    zookeeper-server-start.bat ..\..\config\zookeeper.properties
    ```

3.  **Start Kafka Broker:** In a separate terminal, start the Kafka broker service:

    ```bash
    .\kafka-server-start.bat ..\..\config\server.properties
    ```

4.  **Create Topic:** Create the mandatory Kafka topic named `contactevent_topic` for publishing contact events:

    ```bash
    .\bin\windows\kafka-topics.bat --create --topic contactevent_topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
    ```

---

##  Build and Run

### 1. Build the Project

Navigate to the root directory of the project and use Maven to build the executable JAR:

```bash
mvn clean install
```

### 2. Start the Contacts Service using the following command:

```bash
java -jar target/contactsservice-0.0.1-SNAPSHOT.jar
```
### 3. Test the service using postman collections under resources/postman_collection

##  Docker Deployment
```bash
docker build -t contactservice:latest .
```
