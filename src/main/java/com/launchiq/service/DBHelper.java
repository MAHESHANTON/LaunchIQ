
package com.launchiq.service;

import com.launchiq.util.AESEncryption;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class DBHelper {

    private static String dbPath = System.getProperty("app.db.path", "C:/LaunchIQ/data/launchiq_windows.db");

    private static Connection getConnection() throws SQLException {
        // ensure folder exists
        File f = new File(dbPath);
        f.getParentFile().mkdirs();
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    public static void initDb() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS USERS (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  name TEXT,
                  email TEXT UNIQUE,
                  password_enc TEXT,
                  role TEXT,
                  created_at TEXT
                );
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS CLOUD_SETTINGS (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  provider TEXT,
                  username_enc TEXT,
                  access_key_enc TEXT,
                  grid_url_enc TEXT,
                  updated_at TEXT
                );
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS EMAIL_SETTINGS (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  host TEXT,
                  port INTEGER,
                  username_enc TEXT,
                  password_enc TEXT,
                  default_send INTEGER,
                  updated_at TEXT
                );
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS EXECUTION_CONFIG (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  default_browser TEXT,
                  default_mode TEXT,
                  retry_count INTEGER,
                  timeout_seconds INTEGER,
                  report_path TEXT,
                  updated_at TEXT
                );
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS EXECUTIONS (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  suite_name TEXT,
                  product TEXT,
                  environment TEXT,
                  browser TEXT,
                  execution_location TEXT,
                  total_tests INTEGER,
                  executed INTEGER,
                  passed INTEGER,
                  failed INTEGER,
                  skipped INTEGER,
                  status TEXT,
                  report_path TEXT,
                  run_date TEXT
                );
            """);
            System.out.println("[LaunchIQ] DB initialized -> " + dbPath);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void createUser(String name, String email, String plainPassword) throws SQLException {
        String enc = AESEncryption.encrypt(plainPassword);
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO USERS (name,email,password_enc,role,created_at) VALUES (?,?,?,?,?)")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, enc);
            ps.setString(4, "Admin");
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    public static void ensureDefaultAdmin() {
        String email = "a.maheshanand@resulticks.com";
        String name = "Administrator";
        String password = "test@321";
        try (Connection c = getConnection()) {
            try (PreparedStatement check = c.prepareStatement("SELECT COUNT(1) FROM USERS WHERE email = ?")) {
                check.setString(1, email);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        createUser(name, email, password);
                        System.out.println("[LaunchIQ] Seeded default admin user: " + email);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int deleteAllUsers() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            return s.executeUpdate("DELETE FROM USERS");
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static Map<String,Object> validateUser(String email, String plainPassword) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM USERS WHERE email=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String enc = rs.getString("password_enc");
                    String dec = AESEncryption.decrypt(enc);
                    if (dec.equals(plainPassword)) {
                        Map<String,Object> user = new HashMap<>();
                        user.put("id", rs.getInt("id"));
                        user.put("name", rs.getString("name"));
                        user.put("email", rs.getString("email"));
                        return user;
                    }
                }
            }
        }
        return null;
    }

    public static void saveCloudSettings(String provider, String username, String accessKey, String gridUrl) throws SQLException {
        String uenc = AESEncryption.encrypt(username);
        String kenc = AESEncryption.encrypt(accessKey);
        String genc = AESEncryption.encrypt(gridUrl);
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO CLOUD_SETTINGS (provider,username_enc,access_key_enc,grid_url_enc,updated_at) VALUES (?,?,?,?,?)")) {
            ps.setString(1, provider);
            ps.setString(2, uenc);
            ps.setString(3, kenc);
            ps.setString(4, genc);
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    public static Map<String,String> getCloudSettings(String provider) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM CLOUD_SETTINGS WHERE provider = ? ORDER BY id DESC LIMIT 1")) {
            ps.setString(1, provider);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String,String> m = new HashMap<>();
                    m.put("provider", rs.getString("provider"));
                    m.put("username", AESEncryption.decrypt(rs.getString("username_enc")));
                    m.put("accessKey", AESEncryption.decrypt(rs.getString("access_key_enc")));
                    m.put("gridUrl", AESEncryption.decrypt(rs.getString("grid_url_enc")));
                    return m;
                }
            }
        }
        return null;
    }

    public static void saveExecutionRecord(String suite, String product, String env, String browser, String location, int total, int passed, int failed, int skipped, String status, String reportPath) {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO EXECUTIONS (suite_name,product,environment,browser,execution_location,total_tests,executed,passed,failed,skipped,status,report_path,run_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, suite);
            ps.setString(2, product);
            ps.setString(3, env);
            ps.setString(4, browser);
            ps.setString(5, location);
            ps.setInt(6, total);
            ps.setInt(7, total);
            ps.setInt(8, passed);
            ps.setInt(9, failed);
            ps.setInt(10, skipped);
            ps.setString(11, status);
            ps.setString(12, reportPath);
            ps.setString(13, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
