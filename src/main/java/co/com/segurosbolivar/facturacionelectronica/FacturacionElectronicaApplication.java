package co.com.segurosbolivar.facturacionelectronica;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class FacturacionElectronicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(FacturacionElectronicaApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
    }
}
