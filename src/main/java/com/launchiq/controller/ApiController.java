
package com.launchiq.controller;

import com.launchiq.service.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ExecutionService executionService;

    @PostMapping("/execute")
    public Map<String,Object> execute(@RequestParam String runId, @RequestParam String suite) {
        Map<String,Object> m = new HashMap<>();
        String suitePath = "src/test/resources/testng.xml";
        Map<String,String> sys = new HashMap<>();
        sys.put("suite", suite);
        sys.put("product", "SampleProduct");
        sys.put("environment", "QA");
        sys.put("browser", "chrome");
        sys.put("cloud", "Local");
        executionService.startRun(runId, suitePath, sys);
        m.put("message", "started");
        m.put("runId", runId);
        return m;
    }

    @PostMapping("/stop")
    public Map<String,Object> stop(@RequestParam String runId) {
        executionService.requestStop(runId);
        Map<String,Object> m = new HashMap<>();
        m.put("message", "stop requested");
        return m;
    }

    @GetMapping("/progress")
    public Map<String,Object> progress(@RequestParam String runId) {
        Map<String,Object> m = new HashMap<>();
        m.put("progress", executionService.getProgress(runId));
        m.put("status", executionService.getStatus(runId));
        m.put("elapsed", executionService.getElapsedSeconds(runId) + "s");
        Map<String,Integer> c = executionService.getCounters(runId);
        m.put("total", c.getOrDefault("total",0));
        m.put("executed", c.getOrDefault("executed",0));
        m.put("passed", c.getOrDefault("passed",0));
        m.put("failed", c.getOrDefault("failed",0));
        m.put("skipped", c.getOrDefault("skipped",0));
        return m;
    }

    @GetMapping("/history")
    public List<Map<String,Object>> history() {
        List<Map<String,Object>> out = new ArrayList<>();
        try (var conn = java.sql.DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("app.db.path","C:/LaunchIQ/data/launchiq_windows.db"));
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT * FROM EXECUTIONS ORDER BY id DESC LIMIT 50")) {
            while (rs.next()) {
                Map<String,Object> r = new HashMap<>();
                r.put("id", rs.getInt("id"));
                r.put("suite_name", rs.getString("suite_name"));
                r.put("product", rs.getString("product"));
                r.put("environment", rs.getString("environment"));
                r.put("browser", rs.getString("browser"));
                r.put("status", rs.getString("status"));
                r.put("run_date", rs.getString("run_date"));
                out.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    @PostMapping("/admin/cleanup-users")
    public Map<String,Object> cleanupUsers() {
        Map<String,Object> m = new HashMap<>();
        int deleted = com.launchiq.service.DBHelper.deleteAllUsers();
        m.put("deleted", deleted);
        return m;
    }

    @GetMapping("/debug/users")
    public Map<String,Object> debugUsers() {
        Map<String,Object> out = new HashMap<>();
        try (var conn = java.sql.DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("app.db.path","C:/LaunchIQ/data/launchiq_windows.db"));
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT email, role FROM USERS")) {
            List<Map<String,String>> users = new ArrayList<>();
            while (rs.next()) {
                Map<String,String> u = new HashMap<>();
                u.put("email", rs.getString("email"));
                u.put("role", rs.getString("role"));
                users.add(u);
            }
            out.put("users", users);
        } catch (Exception e) {
            out.put("error", e.getMessage());
        }
        return out;
    }
}
