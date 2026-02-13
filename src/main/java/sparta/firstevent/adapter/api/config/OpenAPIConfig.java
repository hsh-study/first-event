package sparta.firstevent.adapter.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "First Event API",
        version = "1.0.0",
        description = "API Documentation for FirstEvent Application"
    )
)
public class OpenAPIConfig {

    @Bean
    public GroupedOpenApi membersApi() {
        return GroupedOpenApi.builder()
            .group("members")
            .pathsToMatch("/api/members/**")
            .build();
    }

    @Bean
    public GroupedOpenApi eventsApi() {
        return GroupedOpenApi.builder()
            .group("events")
            .pathsToMatch("/api/events/**")
            .build();
    }

    @Bean
    public GroupedOpenApi adminEvensApi() {
        return GroupedOpenApi.builder()
            .group("admin-events")
            .pathsToMatch("/api/admin/events/**")
            .build();
    }
}
