package org.swyp.dessertbee;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DessertBeeApplication {

    public static void main(String[] args) {
        loadEnv();
        SpringApplication.run(DessertBeeApplication.class, args);
    }

    private static void loadEnv() {
        System.out.println("🚀 애플리케이션 시작 전에 환경 변수 로드 중...");

        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .directory("src/main/resources")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            System.out.println("✅ " + entry.getKey() + ": " + entry.getValue());
        });

        System.out.println("✅ 환경 변수 로드 완료!");
    }
}
