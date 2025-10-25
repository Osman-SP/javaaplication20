/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication20;
import java.util.HashSet;
import java.util.Set;
/**
 *
 * @author nsofi
 */
public class Estado {
    public int idEdo;
    public boolean edoAcept;
    public int token;
    public Set<Transicion> transiciones;
    public static int numEstado=0;
    
    public Estado(){
        this.idEdo = numEstado++;
        this.edoAcept = false;
        this.token = -1;
        this.transiciones = new HashSet<>();
    }
}
