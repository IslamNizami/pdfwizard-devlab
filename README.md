# PDFWizard — Dynamic PDF Generation & Management Platform 🧙‍♂️📄

PDFWizard is a robust, backend-only Java Spring Boot microservice designed to automate the generation, storage, manipulation, and delivery of PDF documents. Users can interact with the system via RESTful APIs to dynamically create PDFs from structured JSON payloads, manage files securely on AWS S3, and deliver them directly via email. 

This project is built with a strong focus on clean architecture, performance optimization (Redis Caching), and production-grade reliability (Global Exception Handling, Audit Logging, and Scheduled Jobs).

## ✨ Key Features

* **Dynamic PDF Generation:** Create PDF files from JSON payloads using Apache PDFBox.
* **Bonus Manipulations:** Automatically overlay **Watermarks** (transparent angled text) and embed **QR Codes** (via ZXing) onto generated documents.
* **Multipart File Operations:** Upload and **Merge** multiple PDFs into a single document, or **Split** a multi-page PDF into individual files.
* **Cloud Storage & Metadata:** Stores actual PDF blobs in **AWS S3** while keeping lightweight, searchable metadata in **PostgreSQL**.
* **Redis Caching:** Heavily accessed document metadata is cached using Redis to minimize database hits.
* **Email Delivery:** Integrated with Spring Mail (SMTP) to send PDF documents directly to users as email attachments.
* **Dynamic Search & Filtering:** Utilizes JPA Specifications for paginated filtering based on file name, creator, and date ranges.
* **Audit Logging:** Automatically tracks document lifecycles (CREATE, DOWNLOAD, DELETE, MERGE, SPLIT) in a dedicated audit table.
* **Scheduled Cleanup:** A Spring Scheduler cron job automatically purges files and metadata older than 7 days.
* **API Documentation:** Fully documented interactive endpoints using Swagger / OpenAPI 3.

## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.3.4 (Web, Data JPA, Validation, Mail, Cache)
* **Database & Cache:** PostgreSQL 16, Redis 7 (Containerized via Docker)
* **Cloud Storage:** AWS SDK v2 (S3)
* **PDF Processing:** Apache PDFBox 3.x
* **QR Code Generation:** Google ZXing
* **Build Tool:** Gradle
* **API Documentation:** Springdoc OpenAPI (Swagger UI)

## 🚀 Getting Started

### Prerequisites
* Java 21 installed
* Docker and Docker Compose installed
* An AWS Account (Access Key, Secret Key, and S3 Bucket) or LocalStack
* A Gmail account with an "App Password" (for sending emails)

### 1. Clone the repository
```bash
git clone [https://github.com/yourusername/pdfwizard.git](https://github.com/yourusername/pdfwizard.git)
cd pdfwizard

 Start Infrastructure (PostgreSQL & Redis)The docker-compose.yml is configured to use ports 5433 (Postgres) and 6380 (Redis) to prevent conflicts with local services.Bashdocker-compose up -d
3. Configure PropertiesA template configuration file is provided. Rename it and fill in your secrets.Bashcp src/main/resources/application.example.yml src/main/resources/application.yml
Open application.yml and provide your real AWS S3 credentials, PostgreSQL password, and Gmail App Password. (Note: application.yml is git-ignored for security).4. Run the ApplicationBash./gradlew bootRun
The application will start on port 5151.📖 API Documentation & UsageOnce the application is running, access the interactive Swagger UI to explore and test the endpoints:👉 http://localhost:5151/swagger-ui.htmlCore Endpoints OverviewMethodEndpointDescriptionPOST/api/pdf/createGenerates a new PDF from JSON (supports watermark & QR).GET/api/pdf/{id}Retrieves PDF metadata (cached via Redis).GET/api/pdf/{id}/downloadDownloads the actual PDF file from AWS S3.DELETE/api/pdf/{id}Deletes the PDF from S3, PostgreSQL, and Redis Cache.POST/api/pdf/mergeMerges multiple uploaded PDF files into one.POST/api/pdf/splitSplits a single PDF into multiple page-by-page documents.POST/api/pdf/send-emailEmails a stored PDF as an attachment.GET/api/pdf/listReturns a paginated list of all documents.GET/api/pdf/searchDynamic paginated search (by name, user, dates).Example Payload for PDF CreationJSON{
  "fileName": "monthly_report.pdf",
  "title": "Monthly Financial Report",
  "content": "This is a dynamically generated PDF document.",
  "watermark": "CONFIDENTIAL",
  "qrPayload": "[https://islamnizami.com](https://islamnizami.com)"
}
🏗️ Architecture HighlightException Handling: Centralized @RestControllerAdvice captures validation faults, missing files, or S3 errors and returns consistent ProblemDetail JSON responses.Separation of Concerns: StorageService strictly handles S3 I/O, PdfEngineService handles PDFBox generation, and PdfManagementService orchestrates DB tranactions and Caching.
👨‍💻 AuthorIslam NizamiPortfolio: islamnizami.comLinkedIn: Islam Nizami
