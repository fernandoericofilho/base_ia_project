package com.base.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun baseProjectOpenApi(): OpenAPI =
        OpenAPI().info(
            Info()
                .title("Base IA Project")
                .description("Projeto didático de arquitetura em camadas (Controller -> Mapper -> Service -> Repository)")
                .version("v1")
        )
}
