package io.github.koyan9.tvremote;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AdaptiveTvRemoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdaptiveTvRemoteApplication.class, args);
    }
}


