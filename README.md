# My Crypto Price Viewer Project

Hey there! 👋

This is a project I'm working on for my self. The main goal is to build a simple web application using Java and Spring Boot that fetches current cryptocurrency prices using the CoinGecko API and displays them nicely on a webpage.

Basically, it's a way for me to learn about:
- Making API calls from a Java backend
- Building web interfaces with Spring Boot and Thymeleaf
- Handling and displaying data dynamically
- Making my application more robust by handling external API limitations

### Features

- Fetches current prices and other market data (like 24h change, market cap) for top cryptocurrencies.
- Displays the data in a simple, easy-to-read table or card format on a webpage.
- Allows selecting different base currencies (like EUR, USD).
- **Includes basic API rate limit handling:** The application now attempts to retry API calls if a rate limit is hit, waiting for a configurable delay.
- **Configurable API retry logic:** You can easily adjust the maximum number of retries and the delay between retries via the `application.properties` file.

### Technologies Used

- **Java:** The core programming language.
- **Spring Boot:** Makes building the web application much easier.
- **Maven:** For managing project dependencies and building the project.
- **CoinGecko API:** The source for all the crypto data.
- **Thymeleaf:** A templating engine to create dynamic HTML pages.
- **Jackson:** To easily handle the JSON data from the API.
- **Lombok:** Reduces boilerplate code (like getters/setters).
- **Bootstrap:** For some quick and easy styling on the frontend.

### Getting Started

Here's how to get this project up and running on your local machine.

#### Prerequisites

- Java Development Kit (JDK) 17 or newer installed.
- Maven installed.
- An IDE like IntelliJ IDEA, Eclipse, or VS Code with Java/Spring Boot extensions (recommended).

#### Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/zerox80/CoinGecko-WebApp.git](https://github.com/zerox80/CoinGecko-WebApp.git)
    cd CoinGecko-WebApp
    ```
2.  **Build the project with Maven:**
    Open your terminal in the project root directory and run:
    ```bash
    mvn clean install
    ```
    This will download all the necessary dependencies (thanks, Maven!) and build the project.

### Configuration

- Make sure the `application.properties` file is located directly in `src/main/resources/`. I moved it there to ensure Spring Boot picks up the configuration correctly.
- You can configure the API retry behavior in `src/main/resources/application.properties` using the following properties:
    ```properties
    api.coingecko.retry.max-attempts=3
    api.coingecko.retry.delay-ms=60000
    ```
    Adjust the values as needed.

### How to Run

You can run the Spring Boot application directly from your IDE or using Maven.

#### From your IDE

- Open the project in your IDE.
- Find the main application class (`CoinGeckoWebAppApplication.java` in `src/main/java/org/zerox80/coingeckowebapp`).
- Right-click on it and select "Run 'CoinGeckoWebAppApplication.main()'".

#### Using Maven

- Open your terminal in the project root directory.
- Run the Spring Boot Maven plugin:
    ```bash
    mvn spring-boot:run
    ```

### Usage

Once the application is running (you'll see messages in the console, including something like "Started CoinGeckoWebAppApplication"), open your web browser and go to: http://localhost:8080/
