LaunchIQ - Windows-targeted Test Automation Launcher (with WebDriverManager)

This package is optimized for Windows and uses WebDriverManager so you don't need to manage chromedriver manually.

How to run:
1. Install Java 17 and Maven on Windows.
2. (Optional) Set JAVA_HOME and add Maven to PATH.
3. Unzip the package to a folder, e.g., C:\LaunchIQ
4. Double-click LaunchIQ_Start.bat to start the Spring Boot app (or run 'mvn spring-boot:run').
5. First-time setup: open http://localhost:8080/setup to create admin user.
6. Login at http://localhost:8080/admin/login and configure Cloud/SMTP settings.
7. Use http://localhost:8080/launcher to run tests and watch progress.

Notes:
- The database will be created at C:\LaunchIQ\data\launchiq_windows.db by default (ensure the path is writable).
- To change paths or AES key, edit application.properties or pass -D overrides in the .bat file.
=======
# LaunchIQ
Automation LaunchIQ Launcher
