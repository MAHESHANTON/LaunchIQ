package com.launchiq.service;

import com.launchiq.config.RepoConfig;
import com.launchiq.util.AESEncryption;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class RepoRunnerService {

    private final RepoConfig repoConfig;

    public RepoRunnerService(RepoConfig repoConfig) {
        this.repoConfig = repoConfig;
    }

    public void runExternalMavenSuite(String runId) throws Exception {
        if (!repoConfig.isEnabled()) {
            throw new IllegalStateException("Repo execution not enabled");
        }
        Path workDir = Files.createTempDirectory("launchiq-repo-");
        try {
            String authUrl = buildAuthUrl(repoConfig.getUrl(), repoConfig.getTokenEnc(), repoConfig.getUsernameEnc(), repoConfig.getPasswordEnc());
            exec(workDir.getParent(), cmd("git", "clone", "--depth", "1", "-b", repoConfig.getBranch(), authUrl, workDir.toString()));

            // Run mvn with suite file
            Path reportsOut = Paths.get(repoConfig.getReportsDir(), "run-" + runId + "-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
            Files.createDirectories(reportsOut);

            List<String> mvn = new ArrayList<>();
            for (String s : cmd("mvn", "-q", "-Dsurefire.suiteXmlFiles=" + repoConfig.getSuiteFile(), "test")) {
                mvn.add(s);
            }
            exec(workDir, mvn.toArray(new String[0]));

            // Copy reports if exist
            safeCopyDirectory(workDir.resolve("target/surefire-reports"), reportsOut.resolve("surefire-reports"));
            safeCopyByGlob(workDir.resolve("target"), "extent*", reportsOut);
        } finally {
            // cleanup
            deleteRecursive(workDir);
        }
    }

    private static void exec(Path cwd, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (cwd != null) pb.directory(cwd.toFile());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line; while ((line = br.readLine()) != null) { System.out.println(line); }
        }
        int code = p.waitFor();
        if (code != 0) throw new RuntimeException("Command failed (" + String.join(" ", command) + ") exit=" + code);
    }

    private static String[] cmd(String... parts) {
        // On Windows, run directly since mvn and git are on PATH per README
        return parts;
    }

    private static String buildAuthUrl(String baseUrl, String tokenEnc, String userEnc, String passEnc) {
        if (tokenEnc != null && !tokenEnc.isBlank()) {
            String token = AESEncryption.decrypt(tokenEnc);
            return baseUrl.replace("https://", "https://" + url(token) + "@");
        }
        if (userEnc != null && passEnc != null && !userEnc.isBlank() && !passEnc.isBlank()) {
            String u = url(AESEncryption.decrypt(userEnc));
            String p = url(AESEncryption.decrypt(passEnc));
            return baseUrl.replace("https://", "https://" + u + ":" + p + "@");
        }
        return baseUrl;
    }

    private static String url(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static void safeCopyDirectory(Path from, Path to) {
        if (from == null) return;
        if (!Files.exists(from)) return;
        try {
            Files.createDirectories(to);
            try (var stream = Files.walk(from)) {
                stream.forEach(src -> {
                    try {
                        Path dest = to.resolve(from.relativize(src).toString());
                        if (Files.isDirectory(src)) {
                            Files.createDirectories(dest);
                        } else {
                            Files.createDirectories(dest.getParent());
                            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException ignored) {}
                });
            }
        } catch (IOException ignored) {}
    }

    private static void safeCopyByGlob(Path base, String glob, Path outDir) {
        if (base == null || !Files.exists(base)) return;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(base, glob)) {
            for (Path p : ds) {
                if (Files.isDirectory(p)) {
                    safeCopyDirectory(p, outDir.resolve(p.getFileName().toString()));
                } else {
                    Files.createDirectories(outDir);
                    Files.copy(p, outDir.resolve(p.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException ignored) {}
    }

    private static void deleteRecursive(Path p) {
        if (p == null) return;
        try {
            if (!Files.exists(p)) return;
            try (var walk = Files.walk(p)) {
                walk.sorted((a,b)->b.compareTo(a)).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (IOException ignored) {}
                });
            }
        } catch (IOException ignored) {}
    }
}


