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
public class Transicion {
    public char simInf;
    public char simSup;
    public Estado edoDestino;
    
    public Transicion(){
        this.edoDestino = null;
    }
    
    public Transicion(char c, Estado e){
        this.simInf = c;
        this.simSup = c;
        this.edoDestino = e;
    }
    
    public Transicion(char c_inf, char c_sup, Estado e){
        this.simInf = c_inf;
        this.simSup = c_sup;
        this.edoDestino = e;
    }
    
    public boolean esEpsilon() {
        return this.simInf == this.simSup && this.simInf == SimbEsp.EPSILON();
    }

    public boolean esSimbolo(char c){
        return c >= this.simInf && c <= this.simSup;
    }

}
