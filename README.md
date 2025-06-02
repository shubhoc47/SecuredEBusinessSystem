You're right, I have a good overview of the components and structure we've discussed for your "Secured e-Business System." Based on that, I will generate a more precise README.md file.

I will make some assumptions (like the project name and specific plain text passwords for the sample users – you must ensure the corresponding hashes are in your data.sql). Please review and adjust these assumptions if they don't match your final setup.

# Secured e-Business System (Jakarta EE)

This project is a 3-tier enterprise application developed for an online devices sales company, built using Jakarta EE technologies. It features secure user registration with email verification, user login/logout, password recovery, product inventory management (laptops and smartphones), customer record keeping, and order processing with stock management.

## Features Implemented

*   **User Management & Security:**
    *   User self-registration with email verification (simulated via FakeSMTP).
    *   Secure user login and session management.
    *   User logout functionality.
    *   Password recovery process using email verification.
    *   Servlet Filter (`LoginFilter`) to protect application resources, redirecting unauthenticated users to the login page.
    *   Illustrative admin-level access control for certain features based on username (e.g., "admin").
*   **Product Management:**
    *   Adding new Laptop products with specific attributes (Network I/F, Hard Drive, Optical Drive, Ports).
    *   Adding new Smartphone products with specific attributes (Cellular Connectivity, Location, SIM Card Type).
    *   Viewing distinct stock lists for Laptops and Smartphones.
    *   Viewing a consolidated list of all products in stock.
    *   Searching for Laptops by model.
    *   Searching for Smartphones by model.
*   **Customer Management:**
    *   Admin functionality to manually create new customer records.
    *   Listing all registered customer records.
    *   Viewing detailed information for a specific customer, including their complete order history.
    *   Searching for customers by name (partial or full).
*   **Order Management:**
    *   Creating new orders, allowing selection of an existing customer and product.
    *   Automatic decrement of product stock upon successful order creation.
    *   Listing orders (displaying all orders for an admin, or only a user's own orders).
    *   Deleting existing orders, which automatically restores product stock.
    *   Searching for specific orders by Order ID.

## Technologies Used

*   **Jakarta EE Platform:** Jakarta EE 10 (using GlassFish 7.0.x as the runtime)
*   **Presentation Tier:** Jakarta Faces (JSF) 4.0 (Mojarra implementation) with XHTML Facelets
*   **Component Management:** Contexts and Dependency Injection (CDI) for Managed Beans
*   **Business Tier:** Enterprise Jakarta Beans (EJB) - Stateless Session Beans (`ProductEJB`, `CustomerEJB`, `OrderEJB`)
*   **Persistence Tier:** Jakarta Persistence API (JPA) 3.0 - EclipseLink Provider
*   **Security:** Jakarta Servlet Filters (`LoginFilter`), programmatic checks in beans.
*   **Email:** Jakarta Mail API (with FakeSMTP for local simulation)
*   **Database:** MySQL Server 8.0.x
*   **Application Server:** GlassFish 7.0.x
*   **IDE:** Apache NetBeans IDE 17+
*   **Build Tool:** Apache Ant (managed by NetBeans project system)

## Prerequisites

1.  **JDK:** Version 17 or higher.
2.  **Apache NetBeans IDE:** Version 17 or compatible, with Jakarta EE support.
3.  **GlassFish Server:** Version 7.0.x (can be bundled/managed via NetBeans or a separate installation).
4.  **MySQL Server:** Version 8.0.x.
5.  **MySQL Workbench** (or similar SQL client): Recommended for database inspection.
6.  **FakeSMTP (CentreMail):** Download from `http://nilhcem.com/FakeSMTP/download.html`.

## Setup Instructions

### 1. Clone the Repository (If applicable)
```bash
git clone https://github.com/[YOUR_GITHUB_USERNAME]/SecuredEBusinessSystem.git
cd SecuredEBusinessSystem


(Otherwise, open the project directly in NetBeans if you have the source code.)

2. Database Setup

Ensure your MySQL server is running.

Using a MySQL client, create the database if it doesn't exist:

CREATE DATABASE IF NOT EXISTS ebusiness_db;
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
SQL
IGNORE_WHEN_COPYING_END

The application is configured to use default MySQL credentials:

Username: root

Password: pass (as used in our examples)
If your setup differs, update web/WEB-INF/glassfish-resources.xml OR configure the JDBC Resource and Connection Pool directly in the GlassFish Admin Console.

3. GlassFish JDBC Configuration

The project includes web/WEB-INF/glassfish-resources.xml. Upon deployment, GlassFish should attempt to create these resources.

JDBC Connection Pool Details (if manual setup needed):

Name: mysql_ebusinessdb_rootPool

Resource Type: javax.sql.DataSource

Database Vendor: MySql

Datasource Classname: com.mysql.cj.jdbc.MysqlDataSource

Properties:

User: root

Password: pass

DatabaseName: ebusiness_db

ServerName: localhost

PortNumber: 3306

URL: jdbc:mysql://localhost:3306/ebusiness_db?zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=false&allowPublicKeyRetrieval=true

JDBC Resource Details (if manual setup needed):

JNDI Name: jdbc/EBusinessDS

Pool Name: mysql_ebusinessdb_rootPool

4. FakeSMTP Server Setup

Download and run FakeSMTP-x.x.jar.

Set the listening port to 2525.

Click "Start Server". Keep this running for registration/recovery features.

5. Build and Deploy the Application

Open the project in NetBeans IDE.

Ensure your GlassFish server is added in NetBeans (Services tab) and is started.

Ensure FakeSMTP is running on port 2525.

Right-click on the project (SecuredEBusinessSystem) in the "Projects" window.

Select "Clean and Build".

Once successful, right-click again and select "Deploy" (or "Run").

The persistence.xml is configured with eclipselink.ddl-generation="drop-and-create-tables" and jakarta.persistence.sql-load-script-source="data.sql". This will:

Drop and recreate database tables on each deployment.

Execute src/java/META-INF/data.sql to populate initial sample data.

Important: Ensure data.sql contains hashed passwords for users.

Accessing the Application

URL: http://localhost:8080/SecuredEBusinessSystem/ (The context root is SecuredEBusinessSystem unless changed during deployment).

The application starts at index.xhtml.

Sample Users (from data.sql)

(These are examples. Ensure your data.sql creates these users with the hashed versions of the passwords below.)

Admin User:

Username: admin

Password (Plain Text used for hashing): adminpass

Regular User A:

Username: userA

Password (Plain Text used for hashing): alicePass123! (Corresponds to Alice Anderson)

Regular User B:

Username: userB

Password (Plain Text used for hashing): bobPassword456! (Corresponds to Bob Brown)

Project Structure Overview

/src/java: Java source code.

Authentication.Beans: AutenticationBean.java (user auth, registration, recovery), Wuser.java (user login entity).

Authentication.Filters: LoginFilter.java (request security filter).

ebusiness.entities: JPA entities (Product.java, Laptop.java, Smartphone.java, Customer.java, POrder.java).

ebusiness.ejbs: EJB Session Beans (ProductEJB.java, CustomerEJB.java, OrderEJB.java).

ebusiness.controllers: JSF Backing Beans (ProductController.java, CustomerController.java, OrderController.java).

META-INF: Contains persistence.xml and data.sql.

/web: Web resources.

WEB-INF: Deployment descriptors (web.xml, glassfish-web.xml, glassfish-resources.xml, beans.xml).

resources/store/style.css: Primary stylesheet.

.xhtml files: All JSF view pages for the application.

Notes

The "Admin" functionality (e.g., creating customers, seeing all orders) is currently tied to logging in with the username "admin". A more robust role-based security system could be a future enhancement.

This project uses drop-and-create-tables for DDL generation, which is suitable for development and ensures the data.sql script populates a clean schema on each deployment. For data persistence across deployments without re-seeding, change this property in persistence.xml (e.g., to create-tables or validate-tables).

---

**Remember to:**

*   Replace `[YOUR_GITHUB_USERNAME]` in the clone URL.
*   Verify the plain text passwords in the "Sample Users" section match what you actually used to generate the hashes in your `data.sql`.
*   If your project name in NetBeans (and thus the default context root) is different from `SecuredEBusinessSystem`, update that.
*   Fill in any other specific version numbers for JDK, NetBeans, GlassFish, MySQL if you want to be precise.

This README is now much more tailored to the project we've built.
IGNORE_WHEN_COPYING_START
content_copy
download
Use code with caution.
IGNORE_WHEN_COPYING_END