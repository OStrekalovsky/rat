package net.ostrekalovsky.rat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableConfigurationProperties(RATProps.class)
@EnableScheduling
public class ReceiptsAnalyzerTool implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(ReceiptsAnalyzerTool.class, args);
    }

}
