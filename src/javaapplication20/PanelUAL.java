/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package javaapplication20;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
/**
 *
 * @author osman
 */
public class PanelUAL extends javax.swing.JPanel {

    private JTable miTabla;
    private DefaultTableModel tableModel;
    private javax.swing.JButton btnProcesar;
    private AFN afnG = new AFN();
    
    public PanelUAL() {
        initComponents(); // Método de NetBeans
        
        this.setLayout(new BorderLayout());

        // --- 1. CREAR EL MODELO DE LA TABLA (CON LA MODIFICACIÓN) ---
        String[] columnas = {"AFN", "Seleccionar", "Token"};
        tableModel = new DefaultTableModel(columnas, 0) {
            
            // --- ESTE ES EL MÉTODO CLAVE ---
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) { // La segunda columna (índice 1)
                    return Boolean.class; // ...es de tipo Booleano.
                }
                // Las demás columnas usan el tipo por defecto (Object o String)
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Hacemos que la columna 0 no sea editable, las demás sí.
                return column > 0;
            }
        };

        // --- 2. CREAR LA JTABLE ---
        miTabla = new JTable(tableModel);
        //Crear el objeto Font SansSerif con un estilo y tamaño
        java.awt.Font sansSerifFont = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12);
    
        // Aplicar fuente a las celdas de la tabla
        miTabla.setFont(sansSerifFont);
    
        //Aplicar la fuente al encabezado de la tabla (opcional, pero recomendado)
        miTabla.getTableHeader().setFont(sansSerifFont.deriveFont(java.awt.Font.BOLD, 13f));
        
        // --- 3. YA NO NECESITAMOS CONFIGURAR UN EDITOR ESPECIAL PARA LA COLUMNA 1 ---
        // La tabla lo hará automáticamente gracias a getColumnClass()

        // --- 4. AÑADIR LA TABLA A UN JSCROLLPANE Y LUEGO AL PANEL ---
        JScrollPane scrollPane = new JScrollPane(miTabla);
        this.add(scrollPane, BorderLayout.CENTER);
        
        // --- NUEVO CÓDIGO PARA EL BOTÓN ---

        // 1. Creamos la instancia del botón
        btnProcesar = new javax.swing.JButton("Procesar Seleccionados");
        btnProcesar.setFont(sansSerifFont.deriveFont(java.awt.Font.BOLD, 14f));
        btnProcesar.setBackground(new Color(102,0,0));
        btnProcesar.setForeground(new Color(255,255,255));

        // 2. Le añadimos el ActionListener con toda la lógica de procesamiento
        btnProcesar.addActionListener(e -> {
        StringBuilder resumen = new StringBuilder("Se han etiquetado y guardado los siguientes AFN en la colección final:\n");
        int filasProcesadas = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean estaSeleccionado = (Boolean) tableModel.getValueAt(i, 1);

            if (estaSeleccionado) {
                try {
                    // --- 1. LEER LOS DATOS DE LA FILA ---
                    int idAutomata = (Integer) tableModel.getValueAt(i, 0);
                    String tokenStr = (String) tableModel.getValueAt(i, 2);

                    if (tokenStr.isEmpty()) {
                        // Si el campo de token está vacío, saltamos esta fila.
                        System.out.println("Fila " + i + " omitida: el campo de token está vacío.");
                        continue; 
                    }

                    // --- 2. CONVERTIR EL TOKEN A ENTERO (FORMA CORRECTA) ---
                    int token = Integer.parseInt(tokenStr);

                    // --- 3. RECUPERAR EL AFN ORIGINAL DEL MAPA 'afnC' ---
                    AFN automataSeleccionado = AFN.afnC.get(idAutomata);

                    if (automataSeleccionado == null) {
                        System.out.println("No se encontró el AFN con ID " + idAutomata);
                        continue;
                    }

                    // --- 4. ASIGNAR EL NUEVO TOKEN (EL CORAZÓN DE LA SOLUCIÓN) ---
                    // Recorremos todos los estados de aceptación del autómata...
                    for (Estado edoAcept : automataSeleccionado.edosAcept) {
                        // ...y le asignamos el token que el usuario escribió en la tabla.
                        edoAcept.token = token;
                    }

                    // --- 5. GUARDAR EL AFN YA MODIFICADO EN EL MAPA 'afnG' ---
                    // Usamos tu método GuardarAFNG, que lo pone en el mapa 'afnG'.
                    AFN.afnG.put(idAutomata, automataSeleccionado);

                    filasProcesadas++;
                    resumen.append(" - AFN con ID: ").append(idAutomata)
                           .append(" ahora tiene el Token: ").append(token).append("\n");

                } catch (NumberFormatException ex) {
                    // Capturamos el error si el usuario no escribió un número en el campo de token.
                    int filaReal = i + 1;
                    javax.swing.JOptionPane.showMessageDialog(this, "Error en la Fila " + filaReal + ": El token debe ser un número entero.");
                }
            }
        }
        
        AFN granAFN = AFN.unirColeccion();
    
        // Obtenemos una referencia a la ventana principal y guardamos el resultado ahí
        MiPanel ventanaPrincipal = (MiPanel) javax.swing.SwingUtilities.getWindowAncestor(this);
        ventanaPrincipal.elGranAFN = granAFN;

        javax.swing.JOptionPane.showMessageDialog(this, "¡Colección unida y guardada en la ventana principal!");

    });

        // 3. Añadimos el botón a la parte inferior del panel
        this.add(btnProcesar, BorderLayout.SOUTH);

        
        // --- 5. LLENAR LA TABLA CON LOS DATOS INICIALES ---
        actualizarTabla();
    }

    /**
     * Este método lee el mapa estático de AFNs y llena la tabla.
     * Puedes llamarlo desde MiPanel cuando se muestre este panel.
     */
    public void actualizarTabla() {
        System.out.println("Actualizando tabla... Encontrados " + AFN.afnC.size() + " autómatas.");
    
        tableModel.setRowCount(0);

        for (Integer id : AFN.afnC.keySet()) {
            // En la segunda posición, pasamos 'false' para que el checkbox aparezca desmarcado.
            tableModel.addRow(new Object[]{
                id,       // Columna 0: El ID (Integer/Object)
                false,    // Columna 1: El valor para el CheckBox (Boolean)
                ""        // Columna 2: El valor para el TextField (String)
            });
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setPreferredSize(new java.awt.Dimension(600, 500));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
