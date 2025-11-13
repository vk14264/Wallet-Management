package com.example.wallet.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Wallet Management API", version = "1.0", description = "JWT-secured wallet system"),
        security = {@SecurityRequirement(name = "bearerAuth")}
)

@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfig {
}

//@Bean
//public OpenAPI customOpenAPI() {
//    return new OpenAPI()
//            .info(new Info()
//                    .title("Wallet Management API")
//                    .version("1.0")
//                    .description("JWT-secured wallet system"))
//            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
//            .components(new io.swagger.v3.oas.models.Components()
//                    .addSecuritySchemes("bearerAuth",
//                            new SecurityScheme()
//                                    .name("bearerAuth")
//                                    .type(SecurityScheme.Type.HTTP)
//                                    .scheme("bearer")
//                                    .bearerFormat("JWT")));
//}