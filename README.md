# Feedback Viewer

This is a web application for viewing and managing feedback data submitted by users. It is built with Spring Boot Java on the backend, and uses MongoDB to store the data. The frontend is implemented with HTML, CSS, and JavaScript.

## Features

The Feedback Viewer provides the following features:

- **View feedback**: Users can browse the list of feedback entries, which includes information such as a comment, Page URL, institution, and more.
- **Filter feedback**: Users can filter the feedback entries based on various criteria, such as date range, section, theme, and institution.
- **Export feedback**: Users can export the feedback entries to a CSV or Excel file for further analysis.

## Installation

To install and run the Feedback Viewer, follow these steps:

1. Clone the repository: `git clone https://github.com/alpha-canada-ca/feedback-viewer.git`
2. Install the required dependencies: `./mvnw install`
3. Start the application: `./mvnw spring-boot:run`

The application should now be accessible at `http://localhost:8080`.

## Configuration

The Feedback Viewer can be configured by modifying the following properties in the `application.properties` file:

- `spring.data.mongodb.uri`: The connection string for the MongoDB database.

## Contributing

Contributions to the Feedback Viewer are welcome! To contribute, follow these steps:

1. Fork the repository.
2. Create a new branch for your changes: `git checkout -b my-feature-branch`.
3. Make your changes and commit them: `git commit -m "Add new feature"`.
4. Push your changes to your fork: `git push origin my-feature-branch`.
5. Submit a pull request to the main repository.
