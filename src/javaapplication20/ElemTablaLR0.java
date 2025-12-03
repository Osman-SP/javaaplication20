/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication20;

/**
 *
 * @author nsofi
 */
public class ElemTablaLR0 {
    public boolean esDesplazamiento; //true si es desplazamiento, false si es reduccion
	public int numEstadoDestino; //numero de estado al que se desplaza
	
	public ElemTablaLR0(){
		esDesplazamiento = true;
		numEstadoDestino = -1;
	}
	
	public ElemTablaLR0(boolean esDesplazamiento, int numEstadoDestino){
		this.esDesplazamiento = esDesplazamiento;
		this.numEstadoDestino = numEstadoDestino;
	}
}
