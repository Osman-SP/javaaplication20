package javaapplication20;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Gramatica_Gramaticas {
    public String gramatica;
    public int numReglas = 0;
    public LadoIzq[] reglas; // Arreglo de regla con lado izquierdo y lado derecho
    public Set<String> vn = new HashSet<>();
    public Set<String> vt = new HashSet<>();
    public SimbolG simboloInicial;
    analizadorLexico Lexic;
    public int[][] tablaLL1;
    

    
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
        int cols  = arrVT.size();     // arrVT ya tiene $

        // Se crea con -1 en todas las celdas
        int[][] tabla = new int[filas][cols];

        for (int i = 0; i < filas; i++)
            for (int j = 0; j < cols; j++)
                tabla[i][j] = -1; // ⬅️ LLENAR CON -1 DESDE EL INICIO

        // ============================================
        // LLENAR ENTRADAS SIGUIENDO FIRST/FOLLOW
        // ============================================
        for (int i = 0; i < numReglas; i++) {

            String A = reglas[i].simIzq.nombSimb;
            List<SimbolG> alpha = reglas[i].ladoDerecho;
            int idRegla = reglas[i].id;

            Set<SimbolG> firstAlpha = First(alpha);

            boolean tieneEps = false;

            for (SimbolG t : firstAlpha) {

                if (t.nombSimb.equals("epsilon")) {
                    tieneEps = true;
                } else {
                    int fila = idxVN.get(A);
                    int col  = idxVT.get(t.nombSimb);

                    tabla[fila][col] = idRegla;  // ⬅️ REGISTRA LA REGLA
                }
            }

            if (tieneEps) {
                Set<SimbolG> followA = Follow(reglas[i].simIzq);

                for (SimbolG b : followA) {
                    int fila = idxVN.get(A);
                    int col  = idxVT.get(b.nombSimb);

                    tabla[fila][col] = idRegla;
                }
            }
        }

        // ============================================
        // Fila del símbolo $ (última)
        // ============================================
        int filaDollar = arrVN.size();
        int colDollar = idxVT.get("$");

        tabla[filaDollar][colDollar] = 999; // aceptar

        this.tablaLL1 = tabla;
        return tabla;
    }
    
    public void actualizarTokensTerminales(Map<String, Integer> nuevosTokens) {
        if (nuevosTokens == null) return;

        if (tokenVT == null) {
            tokenVT = new java.util.LinkedHashMap<>();
        } else {
            tokenVT.clear();
        }

        // Guardamos el mapa terminal -> token
        tokenVT.putAll(nuevosTokens);

        // Propagamos el token a todos los símbolos terminales de las reglas
        for (int i = 0; i < numReglas; i++) {
            if (reglas[i] == null || reglas[i].ladoDerecho == null) continue;

            for (SimbolG s : reglas[i].ladoDerecho) {
                if (s == null) continue;

                // Solo terminales "reales"
                if (vt.contains(s.nombSimb) && !"epsilon".equals(s.nombSimb) && !"$".equals(s.nombSimb)) {
                    Integer tok = tokenVT.get(s.nombSimb);
                    if (tok != null) {
                        s.token = tok;
                    }
                }
            }
        }
    }
    
    
    public LadoIzq getReglaPorId(int id) {
        for (int i = 0; i < numReglas; i++) {
            if (reglas[i] != null && reglas[i].id == id) {
                return reglas[i];
            }
        }
        return null;
    }

    private String pilaToString(java.util.Deque<String> pila) {
        StringBuilder sb = new StringBuilder();
        java.util.Iterator<String> it = pila.descendingIterator(); // bottom -> top
        while (it.hasNext()) {
            sb.append(it.next()).append(' ');
        }
        return sb.toString().trim();
    }

    private String entradaToString(java.util.List<String> lexemas, int pos) {
        StringBuilder sb = new StringBuilder();
        for (int i = pos; i < lexemas.size(); i++) {
            sb.append(lexemas.get(i)).append(' ');
        }
        return sb.toString().trim();
    }

    public java.util.List<String[]> analizarCadenaLL1(String sigma, String rutaAFDLexico) throws Exception {

        if (tablaLL1 == null) {
            throw new IllegalStateException("La tabla LL(1) no ha sido construida.");
        }
        if (simboloInicial == null) {
            throw new IllegalStateException("No se ha definido símbolo inicial.");
        }
        if (tokenVT == null || tokenVT.isEmpty()) {
            throw new IllegalStateException("No se han asignado tokens a los terminales.");
        }

        java.util.List<String[]> pasos = new java.util.ArrayList<>();

        // 1) Analizador léxico de sigma
        analizadorLexico lex = new analizadorLexico(sigma, rutaAFDLexico);

        // token(int) -> terminal(String)
        java.util.Map<Integer, String> terminalPorToken = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, Integer> e : tokenVT.entrySet()) {
            terminalPorToken.put(e.getValue(), e.getKey());
        }

        java.util.List<String> entradaSimbolos = new java.util.ArrayList<>();
        java.util.List<String> entradaLexemas  = new java.util.ArrayList<>();

        int tk;
        while (true) {
            tk = lex.yylex();
            if (tk == Tokens.FIN) break;

            String term = terminalPorToken.get(tk);
            if (term == null) {
                throw new RuntimeException(
                    "Token léxico sin terminal asociado: código " +
                    tk + ", lexema '" + lex.lexema + "'."
                );
            }

            entradaSimbolos.add(term);      // para LL(1)
            entradaLexemas.add(lex.lexema); // para mostrar
        }

        // Agregar $
        entradaSimbolos.add("$");
        entradaLexemas.add("$");

        int pos = 0;

        // 2) Pila: $ S
        java.util.Deque<String> pila = new java.util.ArrayDeque<>();
        pila.push("$");
        pila.push(simboloInicial.nombSimb);

        boolean aceptado = false;
        boolean error    = false;

        // 3) Bucle LL(1)
        while (!pila.isEmpty() && !error && !aceptado) {

            String top = pila.peek();
            String a   = entradaSimbolos.get(pos);

            String pilaStr    = pilaToString(pila);
            String entradaStr = entradaToString(entradaLexemas, pos);
            String accion;

            // Caso aceptación directa
            if ("$".equals(top) && "$".equals(a)) {
                accion = "accept";
                pasos.add(new String[]{ pilaStr, entradaStr, accion });
                aceptado = true;
                break;
            }

            boolean topEsNoTerminal = (idxVN != null && idxVN.containsKey(top));

            if (!topEsNoTerminal) {
                // === Terminal o $ en la pila ===
                if (top.equals(a)) {
                    pila.pop();
                    pos++;
                    accion = "pop";
                    pasos.add(new String[]{ pilaStr, entradaStr, accion });
                } else {
                    accion = "error: se esperaba '" + top + "' y vino '" + a + "'";
                    pasos.add(new String[]{ pilaStr, entradaStr, accion });
                    error = true;
                }

            } else {
                // === No terminal: usar tabla LL(1) ===
                Integer fila = idxVN.get(top);
                Integer col  = idxVT.get(a);
                if (fila == null || col == null) {
                    accion = "error: símbolo fuera de tabla M[" + top + "," + a + "]";
                    pasos.add(new String[]{ pilaStr, entradaStr, accion });
                    error = true;
                    continue;
                }

                int idRegla = tablaLL1[fila][col];

                if (idRegla == -1) {
                    accion = "error: M[" + top + "," + a + "] = -1";
                    pasos.add(new String[]{ pilaStr, entradaStr, accion });
                    error = true;

                } else if (idRegla == 999) {
                    accion = "accept";
                    pasos.add(new String[]{ pilaStr, entradaStr, accion });
                    aceptado = true;

                } else {
                    LadoIzq regla = getReglaPorId(idRegla);
                    pila.pop(); // quitar A

                    java.util.List<SimbolG> alpha = regla.ladoDerecho;

                    // No empujar epsilon
                    if (!(alpha.size() == 1 &&
                          "epsilon".equals(alpha.get(0).nombSimb))) {

                        for (int k = alpha.size() - 1; k >= 0; k--) {
                            pila.push(alpha.get(k).nombSimb);
                        }
                    }

                    StringBuilder rhs = new StringBuilder();
                    for (SimbolG s : alpha) {
                        rhs.append(s.nombSimb).append(' ');
                    }
                    String produccion = regla.simIzq.nombSimb + " → " + rhs.toString().trim();

                    accion = idRegla + ") " + produccion;
                    pasos.add(new String[]{ pilaStr, entradaStr, accion });
                }
            }
        }

        if (!aceptado && !error) {
            pasos.add(new String[]{
                pilaToString(pila),
                entradaToString(entradaLexemas, pos),
                "error: análisis incompleto"
            });
        }

        return pasos;
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

class PasoLL1 {
    public final String pila;
    public final String cadena;
    public final String accion;

    public PasoLL1(String pila, String cadena, String accion) {
        this.pila = pila;
        this.cadena = cadena;
        this.accion = accion;
    }
}
