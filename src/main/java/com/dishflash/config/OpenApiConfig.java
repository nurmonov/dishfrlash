package com.dishflash.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dishflash API")
                        .version("1.0.0")
                        .description("Shifrlangan ma'lumotlar bazasiga ega E-commerce platforma\n\n" +
                                "Barcha API'lar ochiq (token talab qilmaydi). Auth/login endpoint'i " +
                                "JWT token qaytaradi, lekin majburiy emas."));
    }
}
