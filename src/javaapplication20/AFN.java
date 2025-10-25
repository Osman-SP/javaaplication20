package javaapplication20;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class AFN {
    public Set<Estado> estados;
    public Estado edoInicial;
    public Set<Character> alfabeto;
    public Set<Estado> edosAcept;
    public static Map<Integer, AFN> afnC = new HashMap<>();
    public static Map<Integer, AFN> afnG = new HashMap<>();
    
    public AFN(){
        this.estados = new HashSet<>();
        this.edoInicial = null;
        this.alfabeto = new HashSet<>();
        this.edosAcept = new HashSet<>();
        
    }
    
    public AFN CrearBasico(char c){
        Estado e1,e2;
        e1 = new Estado();
        e2 = new Estado();
        this.estados.add(e1);
        this.estados.add(e2);
        this.edoInicial = e1;
        e1.transiciones.add(new Transicion(c, e2));
        e2.edoAcept = true;
        this.alfabeto.add(c);
        this.edosAcept.add(e2);
        return this;
    }
    
    public AFN CrearBasico(char c1, char c2){
        Estado e1,e2;
        e1 = new Estado();
        e2 = new Estado();
        this.estados.add(e1);
        this.estados.add(e2);
        this.edoInicial = e1;
        e1.transiciones.add(new Transicion(c1,c2,e2));
        e2.edoAcept = true;
        this.edosAcept.add(e2);
        
        for(int i=(int)c1; i<=(int)c2; i++){
            this.alfabeto.add((char)i);
        }
        
        return this;
    }
    
    public AFN CrearBasico(int ascii1, int ascii2) { // 1. La firma del método ahora acepta dos enteros (int)
        Estado e1, e2;
        e1 = new Estado();
        e2 = new Estado();
        this.estados.add(e1);
        this.estados.add(e2);
        this.edoInicial = e1;

        // 2. Convertimos (casteamos) los enteros a char para el constructor de Transicion
        e1.transiciones.add(new Transicion((char) ascii1, (char) ascii2, e2));

        e2.edoAcept = true;
        this.edosAcept.add(e2);

        // 3. El ciclo for ya no necesita el casteo (int) porque los parámetros ya son enteros
        for (int i = ascii1; i <= ascii2; i++) {
            this.alfabeto.add((char) i);
        }

        return this;
    }
    
    public AFN UnirAFN(AFN f2){
        Estado e1,e2;
        e1 = new Estado();
        e2 = new Estado();
        this.estados.add(e1);
        this.estados.add(e2);
        
        e1.transiciones.add(new Transicion(SimbEsp.EPSILON(), this.edoInicial));
        
        e1.transiciones.add(new Transicion(SimbEsp.EPSILON(), f2.edoInicial));
        
        for (Estado e : this.edosAcept){
            e.transiciones.add(new Transicion(SimbEsp.EPSILON(), e2));
            e.edoAcept = false;
        }
        
        for (Estado e : f2.edosAcept){
            e.transiciones.add(new Transicion(SimbEsp.EPSILON(), e2));
            e.edoAcept = false;
        }
        
        e2.edoAcept = true;
        
        this.edoInicial = e1;
        this.estados.addAll(f2.estados);
        this.estados.add(e1);
        this.estados.add(e2);
        this.edosAcept.clear();
        this.edosAcept.add(e2);
        
        this.alfabeto.addAll(f2.alfabeto);
        f2 = null;
        return this;
    }
    
    public AFN Concatenacion(AFN f1, AFN f2){
        for(Estado e: this.edosAcept){
            for(Transicion t: f2.edoInicial.transiciones){
                e.transiciones.add(t);
            }
            e.edoAcept = false;
        }
        
        this.edosAcept.clear();
        this.edosAcept.addAll(f2.edosAcept);
        this.alfabeto.addAll(f2.alfabeto);
        f2.estados.remove(f2.edoInicial);
        this.estados.addAll(f2.estados);
        return this;
    }
    
    public AFN CerraduraPositiva(){
        Estado e1,e2;
        e1 = new Estado();
        e2 = new Estado();
        
        for (Estado e: this.edosAcept){
            e.transiciones.add(new Transicion(SimbEsp.EPSILON(), this.edoInicial));
            e.transiciones.add(new Transicion(SimbEsp.EPSILON(), e2));
            e.edoAcept = false;
        }
        
        e1.transiciones.add(new Transicion(SimbEsp.EPSILON(), this.edoInicial));
        
        this.edoInicial = e1;
        this.edosAcept.clear();
        this.edosAcept.add(e2);
        this.estados.add(e1);
        this.estados.add(e2);
        
        return this;
    }
    
    public AFN CerraduraKleen(){
        Estado e1,e2;
        e1 = new Estado();
        e2 = new Estado();
        
        for(Estado e: this.edosAcept){
            e.transiciones.add(new Transicion(SimbEsp.EPSILON(), this.edoInicial));
            e.transiciones.add(new Transicion(SimbEsp.EPSILON(), e2));
            e.edoAcept = false;
        }
        
        e1.transiciones.add(new Transicion(SimbEsp.EPSILON(), this.edoInicial));
        
        this.edoInicial = e1;
        this.edosAcept.clear();
        this.edosAcept.add(e2);
        this.estados.add(e1);
        this.estados.add(e2);
        
        e1.transiciones.add(new Transicion(SimbEsp.EPSILON(), e2));
        
        return this;
        
    }
    
    public AFN Opcional(){
        Estado e1,e2;
        e1 = new Estado();
        e2 = new Estado();
        
        for(Estado e: this.edosAcept){
            e.transiciones.add(new Transicion(SimbEsp.EPSILON(), e2));
            e.edoAcept = false;
        }
        
        e1.transiciones.add(new Transicion(SimbEsp.EPSILON(), this.edoInicial));
        
        this.edoInicial = e1;
        this.edosAcept.clear();
        this.edosAcept.add(e2);
        this.estados.add(e1);
        this.estados.add(e2);
        
        e1.transiciones.add(new Transicion(SimbEsp.EPSILON(), e2));
        
        return this;
        
    }
    
    public Set<Estado> CerraduraEpsilon(Estado e){
        Set<Estado> C = new HashSet<>();
        Stack<Estado> P = new Stack<>();
     
        Estado e2;
        
        P.push(e);
        
        while(!P.isEmpty()){
            e2 = P.pop();
            if(!C.contains(e2)){
                C.add(e2);
                for(Transicion t: e2.transiciones){
                    if(t.esEpsilon()){
                        P.push(t.edoDestino);
                    }
                }
            }
        }
        
        return C;
    }
    
    public Set<Estado> CerraduraEpsilon(Set<Estado> C){
        Set<Estado> R = new HashSet<>();
        for(Estado e: C){
            R.addAll(CerraduraEpsilon(e));
        }
        
        return R;
    }
    
    public Set<Estado> Mover(Estado e, char c){
        Set<Estado> R = new HashSet<>();
        
        for(Transicion t: e.transiciones){
            if(t.esSimbolo(c)){
                R.add(t.edoDestino);
            }
        }
        
        return R;
    }
    
    public Set<Estado> Mover(Set<Estado> E, char c){
        Set<Estado> R = new HashSet<>();
        
        for(Estado e: E){
            for(Transicion t: e.transiciones){
                if(t.esSimbolo(c)){
                    R.add(t.edoDestino);
                }
            }
        }
        
        return R;
    }
    
    public Set<Estado> Ir_a(Estado e, char c){
        return CerraduraEpsilon(Mover(e,c));
    }
    
    public Set<Estado> Ir_a(Set<Estado> e, char c){
        return CerraduraEpsilon(Mover(e,c));
    }
    
    
    
    public void GuardarAFNc(AFN afn, int id){
        afnC.put(id, afn);
    }
    
    public void GuardarAFNG(AFN afn, int id){
        afnG.put(id, afn);
    }
    
   public static AFN unirColeccion() {
       if (AFN.afnG.isEmpty()) {
           return new AFN();
       }

       // El nuevo AFN que será el resultado final.
       AFN granAFN = new AFN();

       // Se crea un único estado inicial.
       Estado nuevoInicio = new Estado();

       // Se configura en el nuevo AFN.
       granAFN.edoInicial = nuevoInicio;

       // Se recorre cada AFN guardado en los VALORES del mapa.
       for (AFN afnActual : AFN.afnG.values()) {
           // Se conecta el nuevo inicio con el inicio de cada AFN.
           nuevoInicio.transiciones.add(new Transicion(SimbEsp.EPSILON(), afnActual.edoInicial));

           // Simplemente los añadimos al conjunto de estados de aceptación del granAFN.
           granAFN.edosAcept.addAll(afnActual.edosAcept);

           // Se traspasan todos los estados y el alfabeto al gran AFN.
           granAFN.estados.addAll(afnActual.estados);
           granAFN.alfabeto.addAll(afnActual.alfabeto);
       }

       // Finalmente, solo se añade el nuevo estado de inicio al conjunto total.
       granAFN.estados.add(nuevoInicio);

       return granAFN;
   }
   
   public AFD convertirAFD() {
        if (this.edoInicial == null) return new AFD();

        // --- Las estructuras ahora almacenan objetos ElemSj ---
        Queue<ElemSj> edosDFA_sinAnalizar = new LinkedList<>();
        List<ElemSj> edosDFA_analizados = new ArrayList<>();
        List<EdoAFD> tablaTemporal = new ArrayList<>();

        // --- 1. INICIALIZACIÓN ---
        Set<Estado> S0 = this.CerraduraEpsilon(this.edoInicial);

        // --- Creamos el primer objeto ElemSj ---
        ElemSj E0 = new ElemSj();
        E0.id = 0;
        E0.S = S0;

        edosDFA_analizados.add(E0);
        edosDFA_sinAnalizar.add(E0);

        // --- 2. PROCESAMIENTO DE ESTADOS (Bucle Principal) ---
        while (!edosDFA_sinAnalizar.isEmpty()) {
            // --- Obtenemos el ElemSj completo de la cola ---
            ElemSj E_actual = edosDFA_sinAnalizar.poll();
            Set<Estado> S = E_actual.S; // Extraemos el conjunto
            int idEstadoS = E_actual.id; // Extraemos el ID

            EdoAFD nuevaFila = new EdoAFD();
            nuevaFila.id = idEstadoS;

            // --- 3. BUCLE INTERNO: CALCULAR TRANSICIONES ---
            for (char simbolo : this.alfabeto) {
                Set<Estado> U = this.Ir_a(S, simbolo);

                if (!U.isEmpty()) {
                    // Reemplazamos indexOf() con una búsqueda manual ---
                    int idEstadoU = -1;
                    boolean encontrado = false;
                    for(ElemSj elem : edosDFA_analizados) {
                        if (elem.S.equals(U)) {
                            idEstadoU = elem.id;
                            encontrado = true;
                            break;
                        }
                    }

                    if (!encontrado) { // Si es un estado nuevo
                        // Creamos y guardamos un nuevo objeto ElemSj ---
                        idEstadoU = edosDFA_analizados.size();
                        ElemSj E_nuevo = new ElemSj();
                        E_nuevo.id = idEstadoU;
                        E_nuevo.S = U;

                        edosDFA_analizados.add(E_nuevo);
                        edosDFA_sinAnalizar.add(E_nuevo);
                    }

                    nuevaFila.transAFD[(int)simbolo] = idEstadoU;
                }
            }
            // Aseguramos que la tabla temporal se llene en el orden correcto
            while (tablaTemporal.size() <= idEstadoS) {
                tablaTemporal.add(null);
            }
            tablaTemporal.set(idEstadoS, nuevaFila);
        }

        // --- 4. IDENTIFICAR ESTADOS DE ACEPTACIÓN ---
        for (ElemSj elemActual : edosDFA_analizados) {
            Set<Estado> conjunto = elemActual.S;
            EdoAFD estadoActualAFD = tablaTemporal.get(elemActual.id);

            for (Estado edoAFN : conjunto) {
                if (this.edosAcept.contains(edoAFN)) {
                    if (estadoActualAFD.token == -1 || estadoActualAFD.token > edoAFN.token) {
                        estadoActualAFD.token = edoAFN.token;
                    }
                }
            }
        }

        // --- 5. CONSTRUIR EL OBJETO AFD FINAL ---
        int numEstadosFinal = edosDFA_analizados.size();
        AFD afdFinal = new AFD(numEstadosFinal);
        afdFinal.alfabeto = this.alfabeto;
        afdFinal.edosAFD = tablaTemporal.toArray(new EdoAFD[0]);

        return afdFinal;
    }
}
