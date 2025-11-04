
package com.launchiq.config;

import com.launchiq.service.DBHelper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        DBHelper.initDb();
        DBHelper.ensureDefaultAdmin();
    }
}
