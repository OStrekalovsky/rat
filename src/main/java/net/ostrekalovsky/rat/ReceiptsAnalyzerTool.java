package net.ostrekalovsky.rat;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableConfigurationProperties(RATProps.class)
@EnableScheduling
public class ReceiptsAnalyzerTool implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(ReceiptsAnalyzerTool.class, args);
    }

    /**
     * Declare timed aspect to add support of the {@link io.micrometer.core.annotation.Timed}
     * to measure method execution time
     *
     * @param registry metrics registry
     * @return aspect.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
