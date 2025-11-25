package javaapplication20;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Gramatica_Gramaticas {
    public String gramatica;
    public int numReglas = 0;
    public LadoIzq[] reglas; // Arreglo de regla con lado izquierdo y lado derecho
    public Set<String> vn = new HashSet<>();
    public Set<String> vt = new HashSet<>();
    public SimbolG simboloInicial;
    analizadorLexico Lexic;
    
    // Arreglos finales
    public List<String> arrVN;   // [E, E', T, T', F]
    public List<String> arrVT;   // [mas, menos, prod, div, parI, parD, num, $]

    // Mapas para localizar rápido índices
    public java.util.Map<String, Integer> idxVN; // E -> 0
    public java.util.Map<String, Integer> idxVT; // num -> 6

    // Si quieres token numérico tipo 10,20,30...
    public java.util.Map<String, Integer> tokenVT; 

    public Gramatica_Gramaticas(String sigma, String afd, int maxReglas) {
        gramatica = sigma;
        Lexic = new analizadorLexico(gramatica, afd);

        reglas = new LadoIzq[maxReglas];
        for (int i = 0; i < maxReglas; i++) {
            reglas[i] = new LadoIzq();
        }
    }
    
    public void construirArreglosSimbolos() {

        // LinkedHashSet mantiene orden de inserción
        java.util.LinkedHashSet<String> vnOrden = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<String> vtOrden = new java.util.LinkedHashSet<>();

        // 1) Recorremos reglas en orden y vamos registrando aparición
        for (int i = 0; i < numReglas; i++) {
            if (reglas[i].simIzq == null) continue;

            // LHS siempre es VN
            vnOrden.add(reglas[i].simIzq.nombSimb);

            // RHS puede tener VT o VN
            List<SimbolG> lado = reglas[i].ladoDerecho;
            if (lado == null) continue;

            for (SimbolG s : lado) {
                String x = s.nombSimb;
                if ("epsilon".equals(x)) continue; // epsilon no va en VT

                // si ya está en VN, es no terminal
                if (vnOrden.contains(x) || vn.contains(x)) {
                    vnOrden.add(x);
                } else {
                    vtOrden.add(x);
                }
            }
        }

        // 2) Convertimos a listas (arreglos)
        arrVN = new ArrayList<>(vnOrden);

        arrVT = new ArrayList<>(vtOrden);
        if (!arrVT.contains("$")) arrVT.add("$"); // agregar $ al final

        // 3) Construimos mapas índice
        idxVN = new java.util.LinkedHashMap<>();
        for (int i = 0; i < arrVN.size(); i++) {
            idxVN.put(arrVN.get(i), i);
        }

        idxVT = new java.util.LinkedHashMap<>();
        for (int i = 0; i < arrVT.size(); i++) {
            idxVT.put(arrVT.get(i), i);
        }

        // 4) Token numérico estilo profe (10, 20, 30...)
        tokenVT = new java.util.LinkedHashMap<>();
        for (int i = 0; i < arrVT.size(); i++) {
            tokenVT.put(arrVT.get(i), (i + 1) * 10);
        }
    }


    private String norm(String lex) {
        return lex.trim(); // sin toLowerCase para respetar mayúsculas
    }

    public boolean iniEval() {
        // ✅ Reset total por si analizas varias veces
        numReglas = 0;
        simboloInicial = null;
        vn.clear();
        vt.clear();

        // Reiniciar analizador léxico
        Lexic.SetSigma(gramatica);

        // 1. Llamar al símbolo inicial G()
        if (G()) {
            // 2. Debe venir fin de cadena
            int token = Lexic.yylex();
            if (token == Tokens.FIN) {
                return true;
            } else {
                System.out.println("Error: Sobran símbolos después de analizar la gramática.");
            }
        } else {
            System.out.println("Error: La gramática no cumple con la estructura G → Reglas.");
        }

        return false;
    }

    // G -> Reglas
    public boolean G() {
        return Reglas();
    }

    public boolean Reglas() {
        int token;
        if (Regla()) {
            token = Lexic.yylex();
            if (token == Tokens.PUNTO_Y_COMA) {
                return ReglasP();
            }
        }
        return false;
    }

    public boolean ReglasP() {
        statusLexic EdoLexico = Lexic.getEstadoAnalizadorLexico();

        if (Regla()) {
            int token = Lexic.yylex();
            if (token == Tokens.PUNTO_Y_COMA) {
                return ReglasP();
            }
            return false;
        }

        Lexic.setEstadoAnalizadorLexico(EdoLexico);
        return true;
    }

    public boolean Regla() {
        EnvoltorioString refLexema = new EnvoltorioString("");
        if (LadoIzq(refLexema)) {
            int token = Lexic.yylex();
            if (token == Tokens.FLECHA) {
                return LadosDerechos(refLexema.valor); // ✅ acepta OR '|'
            }
        }
        return false;
    }

    // LadoIzq devuelve el lexema del símbolo izquierdo mediante EnvoltorioString.valor
    public boolean LadoIzq(EnvoltorioString lexemaLadoIzq) {
        int token = Lexic.yylex();
        if (token == Tokens.SIMBOLO) {

            String nombre = norm(Lexic.lexema);

            SimbolG s = new SimbolG(nombre, -1, false);
            s.esTerminal = false;

            vn.add(nombre);
            vt.remove(nombre);

            lexemaLadoIzq.valor = nombre;
            return true;
        }
        return false;
    }

    public boolean LadosDerechos(String lexemaLadoIzq) {
        if (LadoDerecho(lexemaLadoIzq)) {
            return LadosDerechosP(lexemaLadoIzq);
        }
        return false;
    }

    public boolean LadosDerechosP(String lexemaLadoIzq) {
        int token = Lexic.yylex();
        if (token == Tokens.OR) {
            if (LadoDerecho(lexemaLadoIzq)) {
                return LadosDerechosP(lexemaLadoIzq);
            }
            return false;
        }
        Lexic.undoToken();
        return true;
    }

    public boolean LadoDerecho(String lexemaLadoIzq) {
        List<SimbolG> l = new ArrayList<>();

        if (SecSimbolos(l)) {

            // ✅ prohibimos epsilon implícito (regla vacía)
            if (l.isEmpty()) return false;

            SimbolG izq = new SimbolG(lexemaLadoIzq, -1, false);
            reglas[numReglas].simIzq = izq;
            reglas[numReglas].ladoDerecho = l;
            reglas[numReglas].id = numReglas + 1;
            numReglas++;

            // ✅ Clasificación REAL usando vn, no esTerminal provisional
            for (SimbolG s : l) {
                if ("epsilon".equals(s.nombSimb)) continue; // epsilon no va a vt/vn

                if (vn.contains(s.nombSimb)) {
                    s.esTerminal = false;
                    vn.add(s.nombSimb);
                    vt.remove(s.nombSimb);
                } else {
                    s.esTerminal = true;
                    vt.add(s.nombSimb);
                }
            }

            if (simboloInicial == null) simboloInicial = izq;
            return true;
        }
        return false;
    }

    // Secuencia de símbolos
    public boolean SecSimbolos(List<SimbolG> l) {
        int token = Lexic.yylex();
        if (token == Tokens.SIMBOLO) {

            String lex = norm(Lexic.lexema);
            SimbolG s = new SimbolG(lex, -1, !vn.contains(lex));
            if (vn.contains(lex)) s.esTerminal = false;

            if (SecSimbolosP(l)) {
                l.add(0, s);
                return true;
            }
            return false;
        }
        Lexic.undoToken(); // ε
        return true;
    }

    // SecSimbolosP
    public boolean SecSimbolosP(List<SimbolG> l) {
        int token = Lexic.yylex();
        if (token == Tokens.SIMBOLO) {

            String lex = norm(Lexic.lexema);
            SimbolG s = new SimbolG(lex, -1, !vn.contains(lex));
            if (vn.contains(lex)) s.esTerminal = false;

            if (SecSimbolosP(l)) {
                l.add(0, s);
                return true;
            }
            return false;
        }
        Lexic.undoToken(); // ε
        return true;
    }

    // ---------------- First y Follow ----------------

    public Set<SimbolG> First(List<SimbolG> l) {
        Set<SimbolG> R = new HashSet<>();
        if (l == null || l.isEmpty()) return R;

        SimbolG primero = l.get(0);

        if ("epsilon".equals(primero.nombSimb)) {
            R.add(SimbolG.EPSILON());
            return R;
        }

        // Terminal real = no está en vn
        if (!vn.contains(primero.nombSimb)) {
            R.add(primero);
            return R;
        }

        // No terminal real: expandir reglas
        for (int i = 0; i < numReglas; i++) {
            if (reglas[i].simIzq != null &&
                reglas[i].simIzq.nombSimb.equals(primero.nombSimb)) {

                R.addAll(First(reglas[i].ladoDerecho));
            }
        }

        // Propagar epsilon en secuencia
        boolean contieneEps = false;
        for (SimbolG s : R) {
            if ("epsilon".equals(s.nombSimb)) {
                contieneEps = true;
                break;
            }
        }

        if (contieneEps) {
            R.remove(SimbolG.EPSILON());
            if (l.size() > 1) {
                R.addAll(First(l.subList(1, l.size())));
            } else {
                R.add(SimbolG.EPSILON());
            }
        }

        return R;
    }

    public Set<SimbolG> Follow(SimbolG s) {
        return Follow(s, new HashSet<>());
    }

    private Set<SimbolG> Follow(SimbolG s, Set<String> visited) {
        Set<SimbolG> R = new HashSet<>();

        if (s == null) return R;

        // No-terminal real
        if (!vn.contains(s.nombSimb)) return R;

        // Evitar ciclos
        if (visited.contains(s.nombSimb)) return R;
        visited.add(s.nombSimb);

        if (simboloInicial != null && s.nombSimb.equals(simboloInicial.nombSimb)) {
            R.add(SimbolG.DOLAR());
        }

        for (int i = 0; i < numReglas; i++) {
            List<SimbolG> lado = reglas[i].ladoDerecho;
            if (lado == null) continue;

            for (int j = 0; j < lado.size(); j++) {

                if (!lado.get(j).nombSimb.equals(s.nombSimb)) continue;

                if (j == lado.size() - 1) {
                    if (reglas[i].simIzq != null &&
                        !reglas[i].simIzq.nombSimb.equals(s.nombSimb)) {

                        R.addAll(Follow(reglas[i].simIzq, visited));
                    }

                } else {
                    List<SimbolG> sublista = lado.subList(j + 1, lado.size());
                    Set<SimbolG> aux = First(sublista);

                    boolean tieneEps = false;
                    for (SimbolG sg : aux) {
                        if ("epsilon".equals(sg.nombSimb)) {
                            tieneEps = true;
                            break;
                        }
                    }

                    if (tieneEps) {
                        aux.remove(SimbolG.EPSILON());
                        R.addAll(aux);
                        if (reglas[i].simIzq != null) {
                            R.addAll(Follow(reglas[i].simIzq, visited));
                        }
                    } else {
                        R.addAll(aux);
                    }
                }
            }
        }

        return R;
    }
    
    public int[][] TablaLL1() {

        int filas = arrVN.size() + 1; // +1 para fila $
        int cols  = arrVT.size();     // VT ya incluye $

        int[][] tabla = new int[filas][cols];

        for (int i = 0; i < filas; i++)
            java.util.Arrays.fill(tabla[i], -1); // vacío

        // 1) Llenar VN x VT con números de regla
        for (int i = 0; i < numReglas; i++) {
            String A = reglas[i].simIzq.nombSimb;
            List<SimbolG> alpha = reglas[i].ladoDerecho;
            int idRegla = reglas[i].id;

            Set<SimbolG> firstAlpha = First(alpha);
            boolean tieneEps = false;

            for (SimbolG t : firstAlpha) {
                if ("epsilon".equals(t.nombSimb)) {
                    tieneEps = true;
                } else {
                    int f = idxVN.get(A);
                    int c = idxVT.get(t.nombSimb);
                    tabla[f][c] = idRegla;
                }
            }

            if (tieneEps) {
                Set<SimbolG> followA = Follow(reglas[i].simIzq);
                for (SimbolG b : followA) {
                    int f = idxVN.get(A);
                    int c = idxVT.get(b.nombSimb);
                    tabla[f][c] = idRegla;
                }
            }
        }

        // 2) Fila $ (última): accept en columna $
        int filaPesos = arrVN.size();
        int colPesos = idxVT.get("$");
        tabla[filaPesos][colPesos] = 999; // por ejemplo 999 = accept

        return tabla;
    }

}

// ----------------- clases auxiliares -----------------

class EnvoltorioString {
    public String valor;
    public EnvoltorioString(String s) {
        this.valor = s;
    }
    @Override
    public String toString() {
        return valor;
    }
}

final class Tokens {
    private Tokens() {}
    public static final int SIMBOLO = 10;
    public static final int FLECHA  = 20;
    public static final int OR      = 30;
    public static final int PUNTO_Y_COMA = 40;
    public static final int FIN = 0;
}
