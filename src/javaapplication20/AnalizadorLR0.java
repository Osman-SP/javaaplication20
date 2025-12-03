package javaapplication20;

import java.util.HashSet;
import java.util.LinkedList;
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
    
    
    public LR0_Conj_Sj[] estadosLR0;   // estados S0, S1, S2,...
    public int numEstados;

    public String[][] actionLR0;       // tabla ACTION [estado][terminal]
    public int[][] gotoLR0;            // tabla GOTO   [estado][noTerminal]


     public AnalizadorLR0(String cadGramatica, String afdGramGram){
        gram = cadGramatica;
        // Este AFD es el que reconoce los símbolos de la gramática (SIMBOLO, FLECHA, OR, PUNTOYCOMA, etc.)
        descRecG = new Gramatica_Gramaticas(cadGramatica, afdGramGram, 5001);
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
            // Obtener la regla
            LadoIzq regla = descRecG.reglas[it.numRegla];
            if (regla == null) continue;

            String lhs = regla.simIzq.nombSimb;

            sb.append('[').append(lhs).append(" → ");

            // Lado derecho con el punto
            for (int i = 0; i < regla.ladoDerecho.size(); i++) {
                if (i == it.posPunto) sb.append("· ");
                sb.append(regla.ladoDerecho.get(i).nombSimb).append(' ');
            }

            // Si el punto está al final
            if (it.posPunto == regla.ladoDerecho.size())
                sb.append("·");

            sb.append("], ");
        }

        // eliminar coma final
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

        // Analizar gramática con el descenso recursivo
        descRecG.iniEval();

        // v = vt U vn
        v = new HashSet<>();
        v.clear();
        for (String simb : descRecG.vt) {
            v.add(simb);
        }
        for (String simb : descRecG.vn) {
            v.add(simb);
        }

        resultIrA = new Inf_IrA[1000];

        // S0: Cerradura({ [regla 0, punto 0] })
        conjItems.clear();
        conjItems.add(new ItemLR0(0, 0)); // asumimos que la regla 0 es S' -> S

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

        j++; // contador de conjuntos Sj

        // Construcción de C vía cerradura/IrA
        while (!Q.isEmpty()) {
            conjSj = Q.poll();

            // Para cada símbolo de v = vt U vn
            for (String simb : v) {
                // IrA(Sj, simb)
                sjAux = IrA_LR0(conjSj.Sj, simb);

                if (sjAux.isEmpty()) {
                    continue;
                }

                // Verificar si este SjAux ya existe en C
                existe = false;

                for (LR0_Conj_Sj elemSj : C) {
                    // Comparamos los conjuntos de items por igualdad
                    if (elemSj.Sj.equals(sjAux)) {
                        existe = true;

                        resultIrA[numRenglonesIrA] = new Inf_IrA();
                        resultIrA[numRenglonesIrA].Si = elemSj.j;   // estado destino
                        resultIrA[numRenglonesIrA].irA_Sj = conjSj.j; // estado origen
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
                    resultIrA[numRenglonesIrA].Si = j;            // estado destino
                    resultIrA[numRenglonesIrA].irA_Sj = conjSj.j; // estado origen
                    resultIrA[numRenglonesIrA].irA_Simbolo = simb;
                    // resultIrA[numRenglonesIrA].ConjuntoItems = ObtenerCadenaItems(sjAux);
                    numRenglonesIrA++;

                    j++;
                    C.add(conjSjAux);
                    Q.offer(conjSjAux);
                }
            }
        }
        
        this.numEstados = C.size();
        this.estadosLR0 = new LR0_Conj_Sj[numEstados];
        for (LR0_Conj_Sj s : C) {
            this.estadosLR0[s.j] = s;  // S_j va en la posición j
        }

        // Construcción de arreglos de terminales y no terminales
        vt  = new SimbolG[descRecG.vt.size()]; // columnas posibles
        vt2 = new String[descRecG.vt.size()];  // nombres de terminales
        vt3 = new int[descRecG.vt.size()];     // tokens, si los asignas después
        vn  = new String[descRecG.vn.size()];  // no terminales

        j = 0;
        // Se llenan los arreglos de terminales
        for (String s : descRecG.vt) {
            vt[j] = new SimbolG(s, -1, true); // falta asignarle token real
            vt2[j] = s;
            // vt3[j] = ... // cuando tengas los tokens
            j++;
        }

        j = 0;
        // Se llena el arreglo de no terminales
        for (String s : descRecG.vn) {
            vn[j++] = s;
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
                LadoIzq reg = descRecG.reglas[item.numRegla];
                if (reg == null || reg.ladoDerecho == null) continue;

                int len = reg.ladoDerecho.size();

                // ¿el punto está al final? A -> α ·
                if (item.posPunto == len) {

                    // CASO ACCEPT: S' -> S ·
                    if (item.numRegla == 0) {  // asumimos regla 0 es S' -> S
                        Integer colPesos = idxTerm.get("$");
                        if (colPesos != null) {
                            actionLR0[i][colPesos] = "acc";
                        }
                    } else {
                        // CASO REDUCE: A -> α ·   (regla item.numRegla)
                        String accionReduce = "r" + item.numRegla;

                        // LR(0) puro: reduce para TODOS los terminales (incluyendo $)
                        for (java.util.Map.Entry<String,Integer> e : idxTerm.entrySet()) {
                            int col = e.getValue();

                            // si la celda está vacía, ponemos reduce
                            if (actionLR0[i][col] == null || actionLR0[i][col].isEmpty()) {
                                actionLR0[i][col] = accionReduce;
                            } else {
                                // aquí podrías detectar conflictos shift/reduce o reduce/reduce
                                // por ahora los dejamos silenciosos o podrías imprimir un warning
                                // System.out.println("Conflicto en estado " + i + ", simbolo " + e.getKey());
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
        // J empieza siendo el conjunto que nos dan
        HashSet<ItemLR0> J = new HashSet<>(conjItems);

        boolean cambio;

        do {
            cambio = false;

            // Copia para poder iterar aunque J crezca
            HashSet<ItemLR0> copia = new HashSet<>(J);

            for (ItemLR0 it : copia) {
                LadoIzq regla = descRecG.reglas[it.numRegla];
                if (regla == null || regla.ladoDerecho == null) continue;

                // ¿el punto NO está al final de la producción?
                if (it.posPunto < regla.ladoDerecho.size()) {
                    SimbolG B = regla.ladoDerecho.get(it.posPunto);

                    // Si lo que sigue del punto es un NO TERMINAL
                    // descRecG.vn es el conjunto de no terminales
                    if (descRecG.vn.contains(B.nombSimb)) {
                        String nombreB = B.nombSimb;

                        // Para cada regla B -> γ
                        for (int r = 0; r < descRecG.numReglas; r++) {
                            LadoIzq regB = descRecG.reglas[r];
                            if (regB == null || regB.simIzq == null) continue;

                            if (regB.simIzq.nombSimb.equals(nombreB)) {
                                ItemLR0 nuevo = new ItemLR0(r, 0);

                                // Si se agregó algo nuevo, repetiremos el ciclo
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
            LadoIzq regla = descRecG.reglas[it.numRegla];
            if (regla == null || regla.ladoDerecho == null) continue;

            // ¿el punto está antes de algún símbolo?
            if (it.posPunto < regla.ladoDerecho.size()) {
                SimbolG s = regla.ladoDerecho.get(it.posPunto);

                // ¿ese símbolo es X?
                if (s.nombSimb.equals(X)) {
                    // Creamos el item con el punto adelantado
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

        // --- 1. Construir lista de símbolos de entrada ---
        java.util.List<String> entrada = new java.util.ArrayList<>();
        for (String tok : sigma.trim().split("\\s+")) {
            if (!tok.isEmpty()) entrada.add(tok);
        }
        entrada.add("$");

        // --- 2. Mapas de terminales / no terminales ---
        int numTerm = vt2.length + 1; // + "$"
        String[] terminals = new String[numTerm];
        for (int i = 0; i < vt2.length; i++) terminals[i] = vt2[i];
        terminals[numTerm - 1] = "$";

        java.util.Map<String, Integer> idxTerm = new java.util.HashMap<>();
        for (int i = 0; i < terminals.length; i++) idxTerm.put(terminals[i], i);

        java.util.Map<String, Integer> idxNoTerm = new java.util.HashMap<>();
        for (int i = 0; i < vn.length; i++) idxNoTerm.put(vn[i], i);

        // --- 3. Pila ---
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
            String a = entrada.get(iEntrada);

            String pilaStr   = String.join(" ", pilaVista);
            String cadenaStr = String.join(" ", entrada.subList(iEntrada, entrada.size()));
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

            // SHIFT
            if (accion.startsWith("d")) {
                int nuevoEstado = Integer.parseInt(accion.substring(1));
                accionStr = "shift " + nuevoEstado;
                pasos.add(new String[]{pilaStr, cadenaStr, accionStr});

                pilaEstados.push(nuevoEstado);
                pilaVista.add(a);
                pilaVista.add(String.valueOf(nuevoEstado));
                iEntrada++;
                continue;
            }

            // REDUCE
            if (accion.startsWith("r")) {
                int numReg = Integer.parseInt(accion.substring(1));
                LadoIzq reg = descRecG.reglas[numReg];
                int len = reg.ladoDerecho.size();
                String A = reg.simIzq.nombSimb;

                StringBuilder sb = new StringBuilder();
                sb.append("reduce r").append(numReg).append(": ").append(A).append(" →");
                for (SimbolG s : reg.ladoDerecho) sb.append(" ").append(s.nombSimb);
                accionStr = sb.toString();

                pasos.add(new String[]{pilaStr, cadenaStr, accionStr});

                // Pop de la pila
                for (int k = 0; k < len; k++) {
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

            // ACCEPT
            if ("acc".equals(accion)) {
                accionStr = "accept";
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
