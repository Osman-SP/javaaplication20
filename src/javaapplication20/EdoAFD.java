/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication20;

/**
 *
 * @author nsofi
 */
public class EdoAFD {
    public int[] transAFD = new int[256];
    public int id;
    public int token;
    
    public EdoAFD(){
        transAFD = new int[256];
        id = -1;
        token = -1;
        
        for(int i=0; i<256; i++){
            transAFD[i] = -1;
        }
    }
}
