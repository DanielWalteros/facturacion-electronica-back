package co.com.segurosbolivar.facturacionelectronica.util;

public final class ConstantsUtil {

    private ConstantsUtil() {
        // Utility class — no instantiation
    }

    // Input parameters (existentes)
    public static final String IP_FECHA_INICIO = "IP_FECHA_INICIO";
    public static final String IP_FECHA_FIN = "IP_FECHA_FIN";
    public static final String IP_NUM_POLIZA = "IP_NUM_POLIZA";
    public static final String IP_ID_INT_FAC = "IP_ID_INT_FAC";
    public static final String IP_NUM_SECU_POL = "IP_NUM_SECU_POL";

    // Input parameters (nuevos V2)
    public static final String IP_NRO_DOCUMENTO = "IP_NRO_DOCUMENTO";
    public static final String IP_PAGINA = "IP_PAGINA";
    public static final String IP_TAMANO = "IP_TAMANO";
    public static final String IP_TOP_N = "IP_TOP_N";

    // Output parameters (V1 — se mantiene para compatibilidad)
    public static final String OP_CURSOR = "OP_CURSOR";

    // Output parameters (nuevos V2 — lowercase post-normalizeKeys)
    public static final String OP_DATA = "op_data";
    public static final String OP_RESULTADO = "op_resultado";
    public static final String OP_ARRERRORES = "op_arrerrores";
}
