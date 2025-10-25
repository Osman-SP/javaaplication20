/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication20;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
/**
 *
 * @author osman
 */
public class ErrorRowRenderer extends DefaultTableCellRenderer{
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        // 1. Llama al método original para obtener el componente por defecto (un JLabel).
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // 2. Obtenemos el valor del token de la primera columna (índice 0) de la fila actual.
        // El modelo de la tabla nos da los datos.
        int token = (Integer) table.getModel().getValueAt(row, 0);

        // 3. Comprobamos la condición: ¿Es un token de error?
        if (token == SimbEsp.ERROR()) {
            // Si es un error, pintamos el fondo de rojo y el texto de blanco.
            c.setBackground(new Color(255, 102, 102)); // Un rojo suave
            c.setForeground(Color.WHITE);
        } else {
            // 4. MUY IMPORTANTE: Si NO es un error, restauramos los colores por defecto.
            // Si no haces esto, la tabla reutilizará el color rojo para otras filas al hacer scroll.
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            } else {
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());
            }
        }

        return c; // Devuelve el componente ya pintado.
    }
}
