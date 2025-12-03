/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication20;

/**
 *
 * @author nsofi
 */
public class RenglonLR0 {
    public int idEdo;
    public ElemTablaLR0[] acciones;
	
	public RenglonLR0(int idEdoLR, int numSimbolos){
		idEdo = idEdoLR;
		acciones = new ElemTablaLR0[numSimbolos];
		for(int i=0; i<numSimbolos; i++){
			acciones[i] = new ElemTablaLR0();
		}
	}
        
}
