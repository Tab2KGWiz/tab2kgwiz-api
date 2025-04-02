# Tabular Data to Knowledge Graph Wizard (tab2kgwiz) - Server

Tab2KGwiz is a Java Spring Boot application designed to transform tabular data into structured knowledge graphs (KGs). The tool leverages a robust Spring Boot backend to process and convert tabular data into RDF format.

## Documentation (Guide)

This project is part of the **TFG** (Trabajo de Fin de Grado) by **Zihan Chen** at the **Universidad de Lleida**. The project aims to provide a user-friendly interface for transforming tabular data into knowledge graphs.

Chen, Z. (2024). [Asistente para la transformación de datos tabulares a Grafos de Conocimiento](https://repositori.udl.cat/items/20ea8d13-c336-46d2-af4b-cb3379931bcf). Guía de la aplicación.

## Features

- **Tabular Data to Knowledge Graph Conversion**: Converts tabular data (e.g., CSV, JSON) into RDF.
- **Dockerized Application**: Includes a Dockerfile for easy containerization and deployment.
- **Spring Boot Framework**: Built with Spring Boot for easy configuration and scalability.
- **REST API**: Provides a REST API to accept tabular data for conversion to a knowledge graph.

## Requirements

### System Requirements

- **Java** (JDK 11 or later)
- **Maven** (for building the project)
- **Docker** (optional, for Docker deployment)

## Getting Started

### 1. Clone the Repository

Clone the repository to your local machine:

```bash
git clone https://github.com/Tab2KGWiz/tab2kgwiz-api.git
cd tab2kgwiz
```

### 2. Build the Project

```bash
mvn clean install
```

This will download the necessary dependencies and compile the project.

### 3. Run the Application Locally

```bash
mvn spring-boot:run
```

Once the application is running, you can access it at <http://localhost:8080>.

## Dockerization

The project comes with a Dockerfile for containerizing the application.

### 1. Build the Docker Image

To build the Docker image, run the following command:

```bash
docker build -t backend .
```

This will create a Docker image tagged backend.

### 2. Run the Docker Container

To run the application inside a Docker container, use the following command:

```bash
docker run -p 3000:3000 -it backend
```

Now, the application will be accessible at <http://localhost:8080>.

## Configuration

You can configure application settings in the application.properties file located in src/main/resources.

## License

This project is licensed under the MIT License.
