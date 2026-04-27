package co.com.segurosbolivar.facturacionelectronica.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.adapter.v3")
public class DatabaseAdapterV3Properties {

    private String baseUrl;
    private String adapterPath = "/api/v3/database/adapter";
    private String ejecutor = "ejecutor_tronador";
    private String clientId;
    private String clientSecret;
    private String owner = "OPS$PUMA";
    private String channel;
    private String channelOperation;
    private int timeoutSeconds = 30;
    private String dateFormat = "yyyy-MM-dd";
    private String packageName = "SIM_PCK_FACTURA_ELECTRONICA";
    private Procedures procedures = new Procedures();

    @Data
    public static class Procedures {
        private String dashboardKpis = "PRC_GET_DASHBOARD_KPIS";
        private String seguimientoFacturas = "PRC_GET_SEGUIMIENTO_FACTURAS";
        private String docFactura = "PRC_GET_DOC_FACTURA";
        private String detalleLog = "PRC_GET_DETALLE_LOG";
    }

    public DateTimeFormatter getDateFormatter() {
        return DateTimeFormatter.ofPattern(dateFormat);
    }

    public String getFullUrl() {
        return baseUrl + "/" + ejecutor + adapterPath;
    }
}
