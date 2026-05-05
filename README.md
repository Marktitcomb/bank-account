# bank-account
Bank Account application for calculating account balance.
Includes feature for producing random credits and debits between 200 - 500,000 to an account and
a batch processing service for auditing. 

## Prerequisites 
- **Java 21**
- **Maven 3.8+**

## Running 
Run the main function in the BankAccountOrchestrator class

Or via bash
mvn clean compile dependency:copy-dependencies 
java -cp "target/classes;target/dependency/*" org.pretend.bank.BankAccountOrchestrator

## API

### Get Balance

GET http://localhost:8080 

or 

GET http://localhost:8080/pretend-bank/balance

**Response:**
{"balance":808124.57}