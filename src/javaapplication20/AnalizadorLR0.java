package javaapplication20;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author nsofi
 */
public class AnalizadorLR0 {
    public Inf_IrA[] resultIrA;
    public int idAutGram;
    public Gramatica_Gramaticas descRecG;
    public int numRenglonesIrA;
    public analizadorLexico lexGram;
    public String gram;
    public String sigma;
    public int[][] tablaLR0;
    public Queue<RenglonLR0> RenglonesLR0;
    public String[] vt2, vn;
    public SimbolG[] vt;
    public int[] vt3;
    public HashSet<String> v;
    public LadoIzq reglaAumentada;
    private String rutaAFDGramGram;
    private String rutaAFDLexico; 
    
    
    public LR0_Conj_Sj[] estadosLR0;   // estados S0, S1, S2,...
    public int numEstados;

    public String[][] actionLR0;       // tabla ACTION [estado][terminal]
    public int[][] gotoLR0;            // tabla GOTO   [estado][noTerminal]


    public AnalizadorLR0(String cadGramatica, String afdGramGram){
        gram = cadGramatica;
        rutaAFDGramGram = afdGramGram;  // ⬅ guardamos la ruta
        // Este AFD es el que reconoce los símbolos de la gramática (SIMBOLO, FLECHA, OR, PUNTOYCOMA, etc.)
        descRecG = new Gramatica_Gramaticas(cadGramatica, afdGramGram, 5001);
    }
    
    public void setRutaAFDLexico(String ruta) {
        this.rutaAFDLexico = ruta;
    }
     
    private LadoIzq obtenerReglaLR0(int numRegla) {
        LadoIzq r;

        if (numRegla == 0) {
            r = reglaAumentada;
        } else {
            r = descRecG.reglas[numRegla - 1];
        }

        // --- SOLO PARA LR(0): tratar "epsilon" como producción vacía ---
        if (r.ladoDerecho.size() == 1 && 
            "epsilon".equals(r.ladoDerecho.get(0).nombSimb)) {

            // Creamos UNA COPIA VACÍA de la regla
            LadoIzq vacia = new LadoIzq();
            vacia.simIzq = r.simIzq;
            vacia.ladoDerecho = new java.util.ArrayList<>(); // ← ahora ε real
            return vacia;
        }

        return r;
    }


    // Si quieres seguir teniendo el constructor de un solo parámetro, 
    // lo puedes dejar como algo “por defecto”, pero ya no será el que use tu panel:
    public AnalizadorLR0(String cadGramatica){
        gram = cadGramatica;
        // OJO: aquí seguirías usando algo fijo, solo déjalo si te sirve para pruebas rápidas
        String archAFDlexiGramGramPorDefecto = "archivoAnalizadorLexicoGramGram";
        descRecG = new Gramatica_Gramaticas(cadGramatica, archAFDlexiGramGramPorDefecto, 5001);
    }
    
        public String ObtenerCadenaItems(HashSet<ItemLR0> items) {
        StringBuilder sb = new StringBuilder();

        for (ItemLR0 it : items) {
            // Obtener la regla (0 = aumentada, >0 = normal)
            LadoIzq regla = obtenerReglaLR0(it.numRegla);
            if (regla == null) continue;

            String lhs = regla.simIzq.nombSimb;

            sb.append('[').append(lhs).append(" → ");

            // Lado derecho con el punto
            for (int i = 0; i < regla.ladoDerecho.size(); i++) {
                if (i == it.posPunto) sb.append("■ ");
                sb.append(regla.ladoDerecho.get(i).nombSimb).append(' ');
            }

            // Si el punto está al final
            if (it.posPunto == regla.ladoDerecho.size())
                sb.append("■");

            sb.append("], ");
        }

        if (sb.length() >= 2)
            sb.setLength(sb.length() - 2);

        return sb.toString();
    }



    public void CrearTablaLR0() {
        int j;
        boolean existe;

        // Conjunto de todos los Sj
        HashSet<LR0_Conj_Sj> C = new HashSet<>();

        // Conjunto Sj (estado LR0)
        LR0_Conj_Sj conjSj = new LR0_Conj_Sj();
        LR0_Conj_Sj conjSjAux;

        // Conjunto temporal de Items para armarlos
        HashSet<ItemLR0> conjItems = new HashSet<>();

        // Aux temporal para guardar el resultado de un IrA
        HashSet<ItemLR0> sjAux;

        // Sj sin analizar, quedan en una cola
        Queue<LR0_Conj_Sj> Q = new LinkedList<>();

        // 1) Analizar gramática con el descenso recursivo
        descRecG.iniEval();

        // ======================================================
        // 2) Crear regla aumentada S' -> S   (SOLO PARA LR(0))
        //    Regla 0 en el mundo LR(0)
        // ======================================================
        String S = descRecG.simboloInicial.nombSimb;
        SimbolG Sprima = new SimbolG(S + "'", -1, false);

        this.reglaAumentada = new LadoIzq();
        this.reglaAumentada.simIzq = Sprima;
        this.reglaAumentada.ladoDerecho = new java.util.ArrayList<>();
        // RHS: S
        this.reglaAumentada.ladoDerecho.add(new SimbolG(S, -1, false));

        // S' se considera no terminal SOLO para LR(0)
        descRecG.vn.add(Sprima.nombSimb);

        // ======================================================
        // 3) Construir arreglos ordenados VT y VN (arrVT, arrVN)
        //    y usarlos para vt2, vn, v
        // ======================================================
        descRecG.construirArreglosSimbolos();

        // arrVT incluye "$" al final; para LR(0) vt2 NO debe incluir "$"
        java.util.List<String> vtLista = new java.util.ArrayList<>();
        if (descRecG.arrVT != null) {
            for (String s2 : descRecG.arrVT) {
                if (!"$".equals(s2)) {
                    vtLista.add(s2);
                }
            }
        }

        // Terminales ordenados
        vt2 = vtLista.toArray(new String[0]);
        vt  = new SimbolG[vt2.length];
        vt3 = new int[vt2.length];  // por si después asignas tokens

        for (int i = 0; i < vt2.length; i++) {
            vt[i] = new SimbolG(vt2[i], -1, true);
        }

        // No terminales ordenados
        if (descRecG.arrVN != null) {
            vn = descRecG.arrVN.toArray(new String[0]);
        } else {
            vn = new String[0];
        }

        // v = VT ∪ VN   (símbolos sobre los que se calcula IrA)
        v = new HashSet<>();
        for (String s2 : vt2) v.add(s2);
        for (String s2 : vn)  v.add(s2);

        // ======================================================
        // 4) Construcción de C vía Cerradura / IrA
        // ======================================================

        resultIrA = new Inf_IrA[1000];

        // S0: Cerradura({ [regla 0, punto 0] })  =>  S' -> · S
        conjItems.clear();
        conjItems.add(new ItemLR0(0, 0)); // 0 = regla aumentada S' -> S

        j = 0;
        conjSj.Sj = CerraduraLR0(conjItems);
        conjSj.j = j;
        C.add(conjSj);
        Q.offer(conjSj); // en Q está S0 inicialmente

        numRenglonesIrA = 0;
        resultIrA[numRenglonesIrA] = new Inf_IrA();
        resultIrA[numRenglonesIrA].Si = 0;
        resultIrA[numRenglonesIrA].irA_Sj = -1;
        resultIrA[numRenglonesIrA].irA_Simbolo = "";
        resultIrA[numRenglonesIrA].ConjuntoItems = ObtenerCadenaItems(conjSj.Sj);
        numRenglonesIrA++;

        j++; // siguiente índice de estado

        // Construcción de C con cerradura + IrA
        while (!Q.isEmpty()) {
            conjSj = Q.poll();

            // Para cada símbolo de v = vt ∪ vn
            for (String simb : v) {
                // IrA(Sj, simb)
                sjAux = IrA_LR0(conjSj.Sj, simb);

                if (sjAux.isEmpty()) {
                    continue;
                }

                // Verificar si este conjunto ya existe en C
                existe = false;

                for (LR0_Conj_Sj elemSj : C) {
                    if (elemSj.Sj.equals(sjAux)) {
                        existe = true;

                        resultIrA[numRenglonesIrA] = new Inf_IrA();
                        resultIrA[numRenglonesIrA].Si = elemSj.j;      // estado destino
                        resultIrA[numRenglonesIrA].irA_Sj = conjSj.j;  // estado origen
                        resultIrA[numRenglonesIrA].irA_Simbolo = simb;
                        // resultIrA[numRenglonesIrA].ConjuntoItems = ObtenerCadenaItems(sjAux);
                        numRenglonesIrA++;

                        break;
                    }
                }

                if (!existe) {
                    // Nuevo conjunto Sj
                    conjSjAux = new LR0_Conj_Sj();
                    conjSjAux.Sj = sjAux;
                    conjSjAux.j = j;

                    resultIrA[numRenglonesIrA] = new Inf_IrA();
                    resultIrA[numRenglonesIrA].Si = j;               // estado destino
                    resultIrA[numRenglonesIrA].irA_Sj = conjSj.j;    // estado origen
                    resultIrA[numRenglonesIrA].irA_Simbolo = simb;
                    resultIrA[numRenglonesIrA].ConjuntoItems = ObtenerCadenaItems(sjAux);
                    numRenglonesIrA++;

                    j++;
                    C.add(conjSjAux);
                    Q.offer(conjSjAux);
                }
            }
        }

        // ======================================================
        // 5) Copiar los estados a arreglo por índice j
        // ======================================================
        this.numEstados = C.size();
        this.estadosLR0 = new LR0_Conj_Sj[numEstados];

        for (LR0_Conj_Sj sEstado : C) {
            // S_j va en la posición j
            if (sEstado.j >= 0 && sEstado.j < numEstados) {
                this.estadosLR0[sEstado.j] = sEstado;
            }
        }
    }

    
    public void construirTablasLR0() {

        if (estadosLR0 == null || numEstados == 0) {
            throw new IllegalStateException("Primero llama a CrearTablaLR0().");
        }

        // ----- 1. Preparamos lista de terminales + "$" -----
        int numTerm = vt2.length + 1;   // vt2 = arreglo de terminales (sin $)
        String[] terminals = new String[numTerm];
        for (int i = 0; i < vt2.length; i++) {
            terminals[i] = vt2[i];
        }
        terminals[numTerm - 1] = "$";  // último terminal es $

        int numNoTerm = vn.length;

        // Mapas para obtener índice de columna
        java.util.Map<String,Integer> idxTerm = new java.util.HashMap<>();
        for (int i = 0; i < terminals.length; i++) {
            idxTerm.put(terminals[i], i);
        }

        java.util.Map<String,Integer> idxNoTerm = new java.util.HashMap<>();
        for (int i = 0; i < vn.length; i++) {
            idxNoTerm.put(vn[i], i);
        }

        // ----- 2. Creamos e inicializamos ACTION y GOTO -----
        actionLR0 = new String[numEstados][numTerm];
        gotoLR0   = new int[numEstados][numNoTerm];

        for (int i = 0; i < numEstados; i++) {
            for (int j = 0; j < numTerm; j++) {
                actionLR0[i][j] = "";      // vacío = error
            }
            for (int j = 0; j < numNoTerm; j++) {
                gotoLR0[i][j] = -1;        // -1 = sin transición
            }
        }

        // ----- 3. SHIFT y GOTO a partir de resultIrA -----
        // resultIrA[k]: IrA( irA_Sj --(simbolo)--> Si )
        for (int k = 0; k < numRenglonesIrA; k++) {
            Inf_IrA t = resultIrA[k];
            if (t == null) continue;
            if (t.irA_Sj < 0) continue;  // la fila inicial que pusimos con -1

            int from = t.irA_Sj;   // estado origen
            int to   = t.Si;       // estado destino
            String X = t.irA_Simbolo;

            if (idxTerm.containsKey(X)) {        // es terminal -> SHIFT
                int col = idxTerm.get(X);
                String accion = "d" + to;        // d = desplazamiento (shift)
                // aquí podrías revisar conflictos si actionLR0[from][col] ya tenía algo
                actionLR0[from][col] = accion;
            } else if (idxNoTerm.containsKey(X)) { // es no terminal -> GOTO
                int col = idxNoTerm.get(X);
                gotoLR0[from][col] = to;
            }
        }

        // ----- 4. REDUCE y ACCEPT a partir de los items de cada estado -----
            for (int i = 0; i < numEstados; i++) {
                LR0_Conj_Sj estado = estadosLR0[i];
                if (estado == null || estado.Sj == null) continue;

                        for (ItemLR0 item : estado.Sj) {
                LadoIzq reg = obtenerReglaLR0(item.numRegla);
                if (reg == null || reg.ladoDerecho == null) continue;

                int len = reg.ladoDerecho.size();

                if (item.posPunto == len) {

                    // CASO ACCEPT: regla 0 = S' -> S
                    if (item.numRegla == 0) {
                        Integer colPesos = idxTerm.get("$");
                        if (colPesos != null) {
                            actionLR0[i][colPesos] = "acc";
                        }
                    } else {
                        // CASO REDUCE: A -> α ·   (regla item.numRegla)
                        String accionReduce = "r" + item.numRegla;

                        for (java.util.Map.Entry<String,Integer> e : idxTerm.entrySet()) {
                            int col = e.getValue();

                            if (actionLR0[i][col] == null || actionLR0[i][col].isEmpty()) {
                                actionLR0[i][col] = accionReduce;
                            } else {
                                // Aquí podrías avisar de conflictos
                                System.out.println("Conflicto en estado " + i + ", simbolo " + e.getKey());
                            }
                        }
                    }
                }
            }
        }
    }


    // -------------------------------------------------------
    // CERRADURA / MOVER / IR_A
    // -------------------------------------------------------

    // Cerradura de UN solo item: Cerradura({item})
    public HashSet<ItemLR0> CerraduraItemLR0(ItemLR0 item) {
        HashSet<ItemLR0> conjuntoInicial = new HashSet<>();
        conjuntoInicial.add(item);
        return CerraduraLR0(conjuntoInicial);
    }

    // Cerradura de un conjunto de items LR(0)
        public HashSet<ItemLR0> CerraduraLR0(HashSet<ItemLR0> conjItems) {
        HashSet<ItemLR0> J = new HashSet<>(conjItems);
        boolean cambio;

        do {
            cambio = false;

            HashSet<ItemLR0> copia = new HashSet<>(J);

            for (ItemLR0 it : copia) {
                LadoIzq regla = obtenerReglaLR0(it.numRegla);
                if (regla == null || regla.ladoDerecho == null) continue;

                if (it.posPunto < regla.ladoDerecho.size()) {
                    SimbolG B = regla.ladoDerecho.get(it.posPunto);

                    if (descRecG.vn.contains(B.nombSimb)) {
                        String nombreB = B.nombSimb;

                        // Para cada regla REAL B -> γ  (índice r en reglas[], pero numRegla = r+1)
                        for (int r = 0; r < descRecG.numReglas; r++) {
                            LadoIzq regB = descRecG.reglas[r];
                            if (regB == null || regB.simIzq == null) continue;

                            if (regB.simIzq.nombSimb.equals(nombreB)) {
                                int numRegLR0 = r + 1;   // ⚠️ desplazamiento
                                ItemLR0 nuevo = new ItemLR0(numRegLR0, 0);

                                if (J.add(nuevo)) {
                                    cambio = true;
                                }
                            }
                        }
                    }
                }
            }

        } while (cambio);

        return J;
    }


    // Mover(I, X): desplaza el punto sobre X (sin cerradura)
    public HashSet<ItemLR0> MoverLR0(HashSet<ItemLR0> I, String X) {
        HashSet<ItemLR0> movidos = new HashSet<>();

        for (ItemLR0 it : I) {
            LadoIzq regla = obtenerReglaLR0(it.numRegla);
            if (regla == null || regla.ladoDerecho == null) continue;

            if (it.posPunto < regla.ladoDerecho.size()) {
                SimbolG s = regla.ladoDerecho.get(it.posPunto);

                if (s.nombSimb.equals(X)) {
                    movidos.add(new ItemLR0(it.numRegla, it.posPunto + 1));
                }
            }
        }

        return movidos;
    }


    // IrA(I, X) = Cerradura( Mover(I, X) )
    public HashSet<ItemLR0> IrA_LR0(HashSet<ItemLR0> I, String X) {
        HashSet<ItemLR0> movidos = MoverLR0(I, X);
        if (movidos.isEmpty()) {
            return new HashSet<>();
        }
        return CerraduraLR0(movidos);
    }
    
    public java.util.List<String[]> analizarCadenaLR0(String sigma) {
        java.util.List<String[]> pasos = new java.util.ArrayList<>();

        if (rutaAFDLexico == null || rutaAFDLexico.isEmpty()) {
            throw new IllegalStateException("No se ha configurado la ruta del AFD léxico para sigma.");
        }

        // ================================
        // 1) LÉXICO: sigma -> TERMINALES + LEXEMAS
        // ================================
        analizadorLexico lex = new analizadorLexico(sigma, rutaAFDLexico);

        java.util.List<String> entrada        = new java.util.ArrayList<>(); // nombres de terminal (num, mas,...)
        java.util.List<String> entradaLexemas = new java.util.ArrayList<>(); // lexemas originales (3.8, +, ...)

        // token(int) -> nombre de terminal(String) según vt2/vt3
        java.util.Map<Integer, String> terminalPorToken = new java.util.HashMap<>();

        for (int i = 0; i < vt2.length; i++) {
            int tok = vt3[i];
            if (tok != 0 && tok != -1) { // según cómo inicialices vt3
                terminalPorToken.put(tok, vt2[i]);  // ej: 100 -> "num"
            }
        }

        int tk;
        while (true) {
            tk = lex.yylex();
            if (tk == SimbEsp.FIN()) break;

            String term = terminalPorToken.get(tk);
            if (term == null) {
                throw new RuntimeException(
                    "Token léxico sin terminal LR(0) asociado: código " +
                    tk + ", lexema '" + lex.lexema + "'."
                );
            }

            entrada.add(term);             // p.ej. "num"
            entradaLexemas.add(lex.lexema); // p.ej. "3.8"
        }
        entrada.add("$");
        entradaLexemas.add("$");

        // ================================
        // 2) Mapas de terminales / no terminales
        // ================================
        int numTerm = vt2.length + 1; // + "$"
        String[] terminals = new String[numTerm];
        for (int i = 0; i < vt2.length; i++) terminals[i] = vt2[i];
        terminals[numTerm - 1] = "$";

        java.util.Map<String, Integer> idxTerm = new java.util.HashMap<>();
        for (int i = 0; i < terminals.length; i++) idxTerm.put(terminals[i], i);

        java.util.Map<String, Integer> idxNoTerm = new java.util.HashMap<>();
        for (int i = 0; i < vn.length; i++) idxNoTerm.put(vn[i], i);

        // ================================
        // 3) Pila LR(0)
        // ================================
        java.util.Stack<Integer> pilaEstados = new java.util.Stack<>();
        java.util.List<String> pilaVista = new java.util.ArrayList<>();

        pilaEstados.push(0);
        pilaVista.add("$");
        pilaVista.add("0");

        int iEntrada = 0;
        boolean aceptada = false;
        boolean error = false;

        while (true) {
            int estado = pilaEstados.peek();
            String a = entrada.get(iEntrada);  // "num", "mas", ... o "$"

            String pilaStr   = String.join(" ", pilaVista);
            // ⬇⬇⬇ aquí usamos LOS LEXEMAS ORIGINALES
            String cadenaStr = String.join(" ",
                    entradaLexemas.subList(iEntrada, entradaLexemas.size()));
            String accionStr;

            Integer colTerm = idxTerm.get(a);
            if (colTerm == null) {
                accionStr = "Error: símbolo '" + a + "' no es terminal conocido";
                pasos.add(new String[]{pilaStr, cadenaStr, accionStr});
                error = true;
                break;
            }

            String accion = actionLR0[estado][colTerm];

            if (accion == null || accion.isEmpty()) {
                accionStr = "Error: ACTION[" + estado + ", " + a + "] vacío";
                pasos.add(new String[]{pilaStr, cadenaStr, accionStr});
                error = true;
                break;
            }

            // =========================
            // SHIFT  -> "d<numEstado>"
            // =========================
            if (accion.startsWith("d")) {
                int nuevoEstado = Integer.parseInt(accion.substring(1));
                accionStr = "d" + nuevoEstado;  // <-- solo d5, d10, etc.

                pasos.add(new String[]{pilaStr, cadenaStr, accionStr});

                pilaEstados.push(nuevoEstado);
                pilaVista.add(a); // aquí seguimos mostrando nombre de terminal
                pilaVista.add(String.valueOf(nuevoEstado));
                iEntrada++;
                continue;
            }

            // =========================
            // REDUCE -> "r<numRegla>"
            // =========================
            if (accion.startsWith("r")) {
                int numReg = Integer.parseInt(accion.substring(1));
                LadoIzq reg = obtenerReglaLR0(numReg);
                int len = reg.ladoDerecho.size();
                String A = reg.simIzq.nombSimb;

                // Solo "rX"
                accionStr = "r" + numReg;
                pasos.add(new String[]{pilaStr, cadenaStr, accionStr});

                // Pop de la pila
                for (int k2 = 0; k2 < len; k2++) {
                    pilaEstados.pop();
                    if (!pilaVista.isEmpty()) pilaVista.remove(pilaVista.size() - 1);
                    if (!pilaVista.isEmpty()) pilaVista.remove(pilaVista.size() - 1);
                }

                int estadoT = pilaEstados.peek();
                Integer colNoT = idxNoTerm.get(A);
                if (colNoT == null) {
                    pasos.add(new String[]{
                            String.join(" ", pilaVista),
                            cadenaStr,
                            "Error: no se encuentra GOTO col para " + A});
                    error = true;
                    break;
                }
                int estadoGoto = gotoLR0[estadoT][colNoT];
                if (estadoGoto < 0) {
                    pasos.add(new String[]{
                            String.join(" ", pilaVista),
                            cadenaStr,
                            "Error: GOTO[" + estadoT + "," + A + "] indefinido"});
                    error = true;
                    break;
                }

                pilaEstados.push(estadoGoto);
                pilaVista.add(A);
                pilaVista.add(String.valueOf(estadoGoto));
                continue;
            }

            // =========================
            // ACCEPT -> "acc"
            // =========================
            if ("acc".equals(accion)) {
                accionStr = "acc";
                pasos.add(new String[]{pilaStr, cadenaStr, accionStr});
                aceptada = true;
                break;
            }

            // Acción rara
            accionStr = "Acción desconocida: " + accion;
            pasos.add(new String[]{pilaStr, cadenaStr, accionStr});
            error = true;
            break;
        }

        if (!aceptada && !error) {
            pasos.add(new String[]{"", "", "La cadena no fue aceptada"});
        }

        return pasos;
    }

}
