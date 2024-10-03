package com.global.book_network.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "medbh",
                        email = "mohamedbenhafsia.dev@gmail.com",
                        url = "https://github.com/Mamadobh"
                ),
                description = "OpenApi docummentaion for spring boot project ",
                title = "OpenAPi specification - BookNetwork ",
                license = @License(
                        name = "Licence name",
                        url = "https://some-url.com"
                ),
                termsOfService = "Term Of Services "
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8889/api/v1"
                ),
                @Server(
                        description = "PROD ENV",
                        url = "https://github.com/Mamadobh"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"

                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
