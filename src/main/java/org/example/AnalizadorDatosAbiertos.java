package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AnalizadorDatosAbiertos {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce la ruta del archivo a analizar:");
        String rutaArchivo = scanner.nextLine();

        if (rutaArchivo.endsWith(".csv")) {
            List<String[]> datosCSV = parsearCSV(rutaArchivo);
            mostrarResumenCSV(datosCSV);
        } else if (rutaArchivo.endsWith(".json")) {
            JsonObject datosJSON = parsearJSON(rutaArchivo);
            mostrarResumenJSON(datosJSON);
        } else if (rutaArchivo.endsWith(".xml")) {
            Document datosXML = parsearXML(rutaArchivo);
            mostrarResumenXML(datosXML);
        } else {
            System.out.println("Formato de archivo no soportado.");
        }
    }

    public static List<String[]> parsearCSV(String rutaArchivo) {
        List<String[]> registros = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] valores = linea.split(",");
                registros.add(valores);
            }
        } catch (Exception e) {
            System.out.println("Error al leer el archivo CSV: " + e.getMessage());
        }
        return registros;
    }

    public static JsonObject parsearJSON(String rutaArchivo) {
        JsonObject jsonObject = null;
        try (FileReader reader = new FileReader(rutaArchivo)) {
            jsonObject = new Gson().fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            System.out.println("Error al leer el archivo JSON: " + e.getMessage());
        }
        return jsonObject;
    }

    public static Document parsearXML(String rutaArchivo) {
        Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(rutaArchivo);
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            System.out.println("Error al leer el archivo XML: " + e.getMessage());
        }
        return doc;
    }

    public static void mostrarResumenCSV(List<String[]> datos) {
        if (datos.isEmpty()) {
            System.out.println("No se encontraron datos.");
            return;
        }
        System.out.println("Resumen del archivo CSV:");
        System.out.println("Número total de filas: " + datos.size());
        System.out.println("Número de columnas: " + datos.get(0).length);

        // Mostrar los primeros 5 registros
        System.out.println("\nPrimeros 5 registros:");
        for (int i = 0; i < Math.min(5, datos.size()); i++) {
            System.out.println(String.join(" | ", datos.get(i)));
        }

        // Calcular mínimo, máximo y promedio para datos numéricos
        calcularEstadisticasNumericas(datos);
    }

    public static void mostrarResumenJSON(JsonObject datos) {
        if (datos == null || datos.entrySet().isEmpty()) {
            System.out.println("No se encontraron datos.");
            return;
        }
        System.out.println("Resumen del archivo JSON:");
        System.out.println("Número de campos: " + datos.size());

        // Mostrar los primeros 5 registros
        int contador = 0;
        for (String clave : datos.keySet()) {
            if (contador >= 5) break;
            System.out.println(clave + ": " + datos.get(clave));
            contador++;
        }

        // Calcular estadísticas para campos numéricos
        calcularEstadisticasNumericasJSON(datos);
    }

    public static void mostrarResumenXML(Document datos) {
        if (datos == null) {
            System.out.println("No se encontraron datos.");
            return;
        }
        System.out.println("Resumen del archivo XML:");
        Element root = datos.getDocumentElement();
        NodeList elementos = root.getChildNodes();
        System.out.println("Elemento raíz: " + root.getNodeName());
        System.out.println("Número de nodos: " + elementos.getLength());

        // Mostrar los primeros 5 registros
        System.out.println("\nPrimeros 5 registros:");
        for (int i = 0; i < Math.min(5, elementos.getLength()); i++) {
            System.out.println(elementos.item(i).getTextContent().trim());
        }

        // Calcular estadísticas para campos numéricos
        calcularEstadisticasNumericasXML(elementos);
    }

    // Función para calcular estadísticas en CSV
    public static void calcularEstadisticasNumericas(List<String[]> datos) {
        // Asumimos que los datos numéricos están a partir de la segunda columna
        int columnas = datos.get(0).length;
        for (int col = 1; col < columnas; col++) {
            try {
                double min = Double.MAX_VALUE, max = Double.MIN_VALUE, sum = 0;
                int count = 0;
                for (int row = 1; row < datos.size(); row++) {
                    double valor = Double.parseDouble(datos.get(row)[col]);
                    min = Math.min(min, valor);
                    max = Math.max(max, valor);
                    sum += valor;
                    count++;
                }
                System.out.println("Columna " + col + " - Mínimo: " + min + ", Máximo: " + max + ", Promedio: " + (sum / count));
            } catch (NumberFormatException e) {
                System.out.println("Columna " + col + " no es numérica.");
            }
        }
    }

    // Función para calcular estadísticas en JSON
    public static void calcularEstadisticasNumericasJSON(JsonObject datos) {
        for (String clave : datos.keySet()) {
            JsonElement valor = datos.get(clave);
            if (valor.isJsonPrimitive() && valor.getAsJsonPrimitive().isNumber()) {
                double num = valor.getAsDouble();
                System.out.println("Campo: " + clave + " - Valor numérico: " + num);
                // Aquí puedes agregar lógica para calcular estadísticas adicionales si es necesario
            }
        }
    }

    // Función para calcular estadísticas en XML
    public static void calcularEstadisticasNumericasXML(NodeList nodos) {
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE, sum = 0;
        int count = 0;
        for (int i = 0; i < nodos.getLength(); i++) {
            String contenido = nodos.item(i).getTextContent().trim();
            try {
                double valor = Double.parseDouble(contenido);
                min = Math.min(min, valor);
                max = Math.max(max, valor);
                sum += valor;
                count++;
            } catch (NumberFormatException e) {
                // No es un valor numérico, continuar
            }
        }
        if (count > 0) {
            System.out.println("Mínimo: " + min + ", Máximo: " + max + ", Promedio: " + (sum / count));
        } else {
            System.out.println("No se encontraron valores numéricos.");
        }
    }
}
