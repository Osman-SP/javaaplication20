
package javaapplication20;
import java.util.HashSet;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AFD {
    EdoAFD[] edosAFD;
    Set<Character> alfabeto;
    int numEdos;
    
    public AFD(){
        numEdos = 0;
        this.alfabeto = new HashSet<>();
    }
    
    public AFD(int n){
        edosAFD = new EdoAFD[n];
        numEdos = 0;
        this.alfabeto = new HashSet<>();
    }
    
    public AFD(int n, Set<Character> alf){
        edosAFD = new EdoAFD[n];
        numEdos = 0;
        this.alfabeto = new HashSet<>();
        alfabeto.addAll(alf);
    }
    
    public static AFD leerAFDdeArchivo(String rutaArchivo) throws IOException, NumberFormatException {
        AFD afd = null;
        List<Character> alfabetoOrdenado = new ArrayList<>();

        // Usamos try-with-resources para asegurar que el lector se cierre
        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {

            // --- PASO 1: LEER LOS METADATOS ---

            // Línea 1: Número de estados
            int numEstados = Integer.parseInt(reader.readLine().trim());
            afd = new AFD(numEstados);
            // Inicializamos los objetos EdoAFD dentro del arreglo
            for (int i = 0; i < numEstados; i++) {
                afd.edosAFD[i] = new EdoAFD();
            }

            // Línea 2: Alfabeto
            String lineaAlfabeto = reader.readLine();
            if (lineaAlfabeto != null) {
                int indice = 0;
                while (indice < lineaAlfabeto.length()) {
                    char simbolo = lineaAlfabeto.charAt(indice);

                    afd.alfabeto.add(simbolo);
                    alfabetoOrdenado.add(simbolo); // Guardamos el orden para leer la tabla

                    indice++;
                    if (indice < lineaAlfabeto.length() && lineaAlfabeto.charAt(indice) == ' ') {
                        indice++;
                    }
                }
            }

            // Línea 3: Estado inicial (lo leemos para avanzar, aunque ya sabemos que es 0)
            reader.readLine(); 

            // --- PASO 2: OMITIR LÍNEAS DE SEPARACIÓN Y ENCABEZADO DE LA TABLA ---
            reader.readLine(); // La línea en blanco
            reader.readLine(); // La línea de encabezado ("\t a \t b \t Edo. Acept")

            // --- PASO 3: LEER LA TABLA DE TRANSICIONES 
            for (int i = 0; i < numEstados; i++) {

                // Lee la siguiente línea del archivo y quita espacios en blanco al inicio o final.
                String lineaTabla = reader.readLine().trim();

                // Divide la línea en un arreglo de strings, usando el carácter de tabulación ('\t') como separador.
                String[] columnas = lineaTabla.split("\t");

                // El primer elemento del arreglo (índice 0) es siempre el ID del estado actual. Se convierte a entero.
                int idEstadoActual = Integer.parseInt(columnas[0]);

                // Usando el ID, se obtiene la referencia al objeto EdoAFD correspondiente que ya fue pre-creado
                // y almacenado en el arreglo principal del AFD.
                EdoAFD estado = afd.edosAFD[idEstadoActual];

                // Se asigna el ID al objeto.
                estado.id = idEstadoActual;

                // Se obtiene el último elemento del arreglo, que corresponde a la columna de token.
                int token = Integer.parseInt(columnas[columnas.length - 1]);

                // Se asigna el token leído al estado actual. Si no es un estado de aceptación, este valor será -1.
                estado.token = token;

                // Bucle interno para procesar las columnas que corresponden a las transiciones del alfabeto.
                // Itera sobre la lista 'alfabetoOrdenado' para mantener el orden correcto de las columnas.
                for (int j = 0; j < alfabetoOrdenado.size(); j++) {

                    // En cada iteración, se obtiene el símbolo del alfabeto.
                    char simbolo = alfabetoOrdenado.get(j);

                    // Se lee el ID del estado de destino de esa columna y se convierte a entero.
                    int idDestino = Integer.parseInt(columnas[j + 1]);

                    // Se guarda la transición en la tabla del estado. El arreglo 'transAFD' se indexa
                    // directamente por el valor ASCII del 'simbolo' para un acceso rápido y directo en el futuro.
                    estado.transAFD[(int)simbolo] = idDestino;
                }
            }
        }

        return afd;
    }
}
