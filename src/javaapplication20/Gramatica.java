package javaapplication20;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList; // Necesitas importar ArrayList
import java.util.Arrays;  // Útil para crear listas rápido

public class Gramatica {

    // --- Atributos Limpios ---
    // Solo necesitamos una lista para las reglas
    public List<LadoIzq> todasLasReglas; 
    
    // Conjuntos para terminales y no terminales (se llenarán automáticamente)
    public Set<SimbolG> vn;
    public Set<SimbolG> vt;
    
    public SimbolG simboloInicial;
    
    // El símbolo épsilon debe ser una constante
    public final SimbolG SIMBOLO_EPSILON; 
    public final SimbolG pesos;

    /**
     * Constructor de la Gramática
     */
    public Gramatica() {
        // --- 1. INICIALIZAR LAS COLECCIONES ---
        // Esto es crucial para evitar NullPointerException
        this.todasLasReglas = new ArrayList<>();
        this.vn = new HashSet<>();
        this.vt = new HashSet<>();
        
        // --- 2. DEFINIR EL SÍMBOLO ÉPSILON ---
        // Asignamos un token -1 (o cualquiera que uses) para épsilon
        this.SIMBOLO_EPSILON = new SimbolG("EPSILON", -1, true); 
        this.pesos = new SimbolG("$", -2, true);
        this.vt.add(this.SIMBOLO_EPSILON); // Épsilon es un terminal
        this.vt.add(this.pesos);

        // --- 3. LLAMAR AL MÉTODO QUE CARGA LAS REGLAS ---
        cargarGramatica();
        
        // --- 4. (Opcional pero recomendado) Llenar los conjuntos Vn y Vt ---
        popularConjuntos();
    }

    /**
     * Aquí es donde defines tu gramática.
     * Este es solo un EJEMPLO de la gramática de la calculadora.
     * Debes reemplazar esto con las reglas de tu propia gramática.
     */
    private void cargarGramatica() {
        // --- EJEMPLO DE GRAMÁTICA: E -> T Ep ---
        
        // 1. Definir todos los símbolos
        SimbolG E = new SimbolG("E", -1, false);
        SimbolG Ep = new SimbolG("Ep", -1, false);
        SimbolG T = new SimbolG("T", -1, false);
        SimbolG Tp = new SimbolG("Tp", -1, false);
        SimbolG F = new SimbolG("F", -1, false);
        SimbolG mas = new SimbolG("+", 10, true);
        SimbolG menos = new SimbolG("-", 20, true);
        SimbolG por = new SimbolG("*", 30, true);
        SimbolG entre = new SimbolG("/", 40, true);
        SimbolG parI = new SimbolG("(", 50, true);
        SimbolG parD = new SimbolG(")", 60, true);
        SimbolG num = new SimbolG("num", 70, true);
        
        // 2. Definir Regla 1: E -> T Ep
        List<SimbolG> regla1_ladoDerecho = Arrays.asList(T, Ep);
        LadoIzq regla1 = new LadoIzq(E, regla1_ladoDerecho);
        
        // 3. Definir Regla 2: Ep -> + T Ep
        List<SimbolG> regla2_ladoDerecho = Arrays.asList(mas, T, Ep);
        LadoIzq regla2 = new LadoIzq(Ep, regla2_ladoDerecho);
        
        // 3. Definir Regla 3: Ep -> + T Ep
        List<SimbolG> regla3_ladoDerecho = Arrays.asList(menos, T, Ep);
        LadoIzq regla3 = new LadoIzq(Ep, regla3_ladoDerecho);

        // 4. Definir Regla 3: Ep -> ε
        List<SimbolG> regla4_ladoDerecho = Arrays.asList(SIMBOLO_EPSILON);
        LadoIzq regla4 = new LadoIzq(Ep, regla4_ladoDerecho);
        
        // 5. Definir Regla 4: T -> F Tp
        List<SimbolG> regla5_ladoDerecho = Arrays.asList(F, Tp);
        LadoIzq regla5 = new LadoIzq(T, regla5_ladoDerecho);
        
        // 5. Definir Regla 5: Tp -> * F Tp
        List<SimbolG> regla6_ladoDerecho = Arrays.asList(por, F, Tp);
        LadoIzq regla6 = new LadoIzq(Tp, regla6_ladoDerecho);
        
        // 5. Definir Regla 6: Tp -> / F Tp
        List<SimbolG> regla7_ladoDerecho = Arrays.asList(entre, F, Tp);
        LadoIzq regla7 = new LadoIzq(Tp, regla7_ladoDerecho);
        
        // 5. Definir Regla 7: Tp -> ε
        List<SimbolG> regla8_ladoDerecho = Arrays.asList(SIMBOLO_EPSILON);
        LadoIzq regla8 = new LadoIzq(Tp, regla8_ladoDerecho);
        
        // 5. Definir Regla 8: F -> ( E )
        List<SimbolG> regla9_ladoDerecho = Arrays.asList(parI, E, parD);
        LadoIzq regla9 = new LadoIzq(F, regla9_ladoDerecho);
        
        // 5. Definir Regla 9: F -> num
        List<SimbolG> regla10_ladoDerecho = Arrays.asList(num);
        LadoIzq regla10 = new LadoIzq(F, regla10_ladoDerecho);

        // 6. Añadir las reglas a la lista principal
        this.todasLasReglas.add(regla1);
        this.todasLasReglas.add(regla2);
        this.todasLasReglas.add(regla3);
        this.todasLasReglas.add(regla4);
        this.todasLasReglas.add(regla5);
        this.todasLasReglas.add(regla6);
        this.todasLasReglas.add(regla7);
        this.todasLasReglas.add(regla8);
        this.todasLasReglas.add(regla9);
        this.todasLasReglas.add(regla10);

        // 7. Establecer el símbolo inicial
        this.simboloInicial = E;
    }

    /**
     * Método de utilidad para llenar automáticamente los conjuntos Vn y Vt
     * basándose en las reglas cargadas.
     */
    private void popularConjuntos() {
        for (LadoIzq regla : this.todasLasReglas) {
            // Añadir el símbolo de la izquierda a los No Terminales
            this.vn.add(regla.simIzq);
            
            // Añadir los símbolos de la derecha a Vn o Vt
            for (SimbolG simbolo : regla.ladoDerecho) {
                if (simbolo.esTerminal) {
                    this.vt.add(simbolo);
                } else {
                    this.vn.add(simbolo);
                }
            }
        }
    }

    /**
     * Calcula el conjunto FIRST (Primeros) para una lista de símbolos gramaticales.
     * (Este es tu método, que ya está correcto y no necesita cambios)
     */
    public Set<SimbolG> First(List<SimbolG> l) {
        Set<SimbolG> R = new HashSet<>();

        if (l.isEmpty()) {
            R.add(SIMBOLO_EPSILON);
            return R;
        }

        SimbolG primerSimbolo = l.get(0);

        if (primerSimbolo.esTerminal) {
            R.add(primerSimbolo);
            return R;
        }

        for (LadoIzq regla : todasLasReglas) {
            if (regla.simIzq.equals(primerSimbolo)) { 
                R.addAll(First(regla.ladoDerecho));
            }
        }

        if (R.contains(SIMBOLO_EPSILON)) {
            if (l.size() == 1) {
                return R;
            }
            List<SimbolG> restoDeLaLista = l.subList(1, l.size());
            R.remove(SIMBOLO_EPSILON);
            R.addAll(First(restoDeLaLista));
        }

        return R;
    }
    
    
}