package co.com.segurosbolivar.facturacionelectronica.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Facturación Electrónica Consulta API")
                        .description("API REST para consulta de facturación electrónica - Portal de Autogestión. Expone 8 endpoints: Dashboard KPIs, Errores Agrupados, Duplicados, Tiempo Promedio Emisión, Top Productos Fallas, Tracker, Documento Factura y Logs.")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Equipo Facturación Electrónica")
                                .email("facturacion@segurosbolivar.com")));
    }
}
