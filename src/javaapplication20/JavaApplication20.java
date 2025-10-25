package javaapplication20;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author nsofi
 */
public class JavaApplication20 {

    /**
     * @param args Najera Garcia Sofia Denisse
     */
    public static void main(String[] args) {
        /*AFN miAfn = new AFN();
        
        miAfn.CrearBasico('a');
        System.out.println("AFN Básico Creado:");
        System.out.println("Número de estados: " + miAfn.estados.size());
        System.out.println("Estado inicial ID: " + miAfn.edoInicial.idEdo);
        
        System.out.print("Estados de aceptación ID: ");
        for (Estado e : miAfn.edosAcept) {
            System.out.print(e.idEdo + " ");
        }
        System.out.println();
        
        System.out.println("Transiciones:");
        for (Estado e : miAfn.estados) {
            if (!e.transiciones.isEmpty()) {
                for (Transicion t : e.transiciones) {
                    System.out.println("Estado " + e.idEdo + " --" + t.simInf + "--> Estado " + t.edoDestino.idEdo);
                }
            }
        }
        
        System.out.println("");
        
        AFN miafn2 = new AFN();
        
        miafn2.CrearBasico('a', 'z');
        
        System.out.println("AFN Básico Creado:");
        System.out.println("Número de estados: " + miafn2.estados.size());
        System.out.println("Estado inicial ID: " + miafn2.edoInicial.idEdo);
        
        System.out.print("Estados de aceptación ID: ");
        for (Estado e : miafn2.edosAcept) {
            System.out.print(e.idEdo + " ");
        }
        System.out.println();
        
        System.out.println("Alfabeto:");
        for (Character c : miafn2.alfabeto) {
            System.out.print(c + " ");
        }
        System.out.println();
        
        System.out.println("Transiciones:");
        for (Estado e : miafn2.estados) {
            if (!e.transiciones.isEmpty()) {
                for (Transicion t : e.transiciones) {
                    System.out.println("Estado " + e.idEdo + " --(" + t.simInf + "-" + t.simSup + ")--> Estado " + t.edoDestino.idEdo);
                }
            }
        }*/
        
        /*AFN afn3 = new AFN();
        AFN afn4 = new AFN();
        
        afn3.CrearBasico('a');
        afn4.CrearBasico('b');
        
        afn3.UnirAFN(afn4);
        
        System.out.println("Alfabeto");
        for(Character c: afn3.alfabeto){
            System.out.print(c + " ");
        }
        
        System.out.println("");
        
        System.out.println("Transiciones");
        
        for(Estado e: afn3.estados){
            if(!e.transiciones.isEmpty()){
                for(Transicion t: e.transiciones){
                    System.out.println("Estado" + e.idEdo + "--(" + t.simInf + "-" + t.simSup + ")--> Estado " + t.edoDestino.idEdo);
                }
            }
        }
        
        System.out.println("");*/
        
        /*AFN afn5 = new AFN();
        AFN afn6 = new AFN();
        
        afn5.CrearBasico('a');
        afn6.CrearBasico('c');
        
        afn5.Concatenacion(afn5, afn6);
        System.out.println("Alfabeto");
        
        for(Character c: afn5.alfabeto){
            System.out.print(c + " ");
        }
        System.out.println("");
        
        System.out.println("Número de estados: " + afn5.estados.size());
        System.out.println("");
        
        for(Estado e: afn5.estados){
            if(!e.transiciones.isEmpty()){
                for(Transicion t: e.transiciones){
                    System.out.println("Estado" + e.idEdo + "--(" + t.simInf + "-" + t.simSup + ")--> Estado " + t.edoDestino.idEdo);
                }
            }
        }*/
        
        AFN afn7 = new AFN();
        
        afn7.CrearBasico('a');
        afn7.Opcional();
        
        System.out.println("Alfabeto");
        
        for(Character c: afn7.alfabeto){
            System.out.print(c + " ");
        }
        System.out.println("");
        
        System.out.println("Número de estados: " + afn7.estados.size());
        System.out.println("");
        
        for(Estado e: afn7.estados){
            if(!e.transiciones.isEmpty()){
                for(Transicion t: e.transiciones){
                    System.out.println("Estado" + e.idEdo + "--(" + t.simInf + "-" + t.simSup + ")--> Estado " + t.edoDestino.idEdo);
                }
            }
        }
        
    }
    
}
