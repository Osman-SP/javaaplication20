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

    public Gramatica_Gramaticas(String sigma, String afd, int maxReglas) {
        gramatica = sigma;
        Lexic = new analizadorLexico(gramatica, afd);
        vn.clear();
        vt.clear();
        // Inicializar arreglo de reglas
        reglas = new LadoIzq[maxReglas];
        for (int i = 0; i < maxReglas; i++) {
            reglas[i] = new LadoIzq();
        }
    }
    
    public boolean iniEval() {
    // Reiniciar analizador léxico
    Lexic.SetSigma(gramatica);

    // 1. Llamar al símbolo inicial G()
    if (G()) {
        // 2. Debe venir fin de cadena
        int token = Lexic.yylex();
        if (token == Tokens.FIN) {
            // Gramática válida
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
        // Guardar estado léxico para poder retroceder (ε)
        statusLexic EdoLexico = Lexic.getEstadoAnalizadorLexico();

        if (Regla()) {
            int token = Lexic.yylex();
            if (token == Tokens.PUNTO_Y_COMA) {
                return ReglasP();
            }
            return false;
        }
        // No vino una regla -> retroceder (equivale a ε)
        Lexic.setEstadoAnalizadorLexico(EdoLexico);
        return true;
    }

    public boolean Regla() {
        EnvoltorioString refLexema = new EnvoltorioString("");
        if (LadoIzq(refLexema)) {
            int token = Lexic.yylex();
            if (token == Tokens.FLECHA) {
                return LadoDerecho(refLexema.valor);
            }
        }
        return false;
    }

    // LadoIzq devuelve el lexema del símbolo izquierdo mediante EnvoltorioString.valor
    public boolean LadoIzq(EnvoltorioString lexemaLadoIzq) {
        int token = Lexic.yylex();
        if (token == Tokens.SIMBOLO) {
            String nombre = Lexic.lexema;
            SimbolG s = new SimbolG(nombre, -1, false);
            s.esTerminal = false;
            vn.add(nombre);
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
            SimbolG izq = new SimbolG(lexemaLadoIzq, -1, false);
            reglas[numReglas].simIzq = izq;
            reglas[numReglas].ladoDerecho = l;
            numReglas++;
            // Añadir símbolos del lado derecho al conjunto de terminales/no terminales
            for (SimbolG s : l) {
                if (s.esTerminal) vt.add(s.nombSimb);
                else vn.add(s.nombSimb);
            }
            // Si es la primera regla, marcar símbolo inicial
            if (simboloInicial == null) simboloInicial = izq;
            return true;
        }
        return false;
    }

    // Secuencia de símbolos: recursiva, agrega en orden (primero leído -> primero en la lista)
    public boolean SecSimbolos(List<SimbolG> l) {
        int token = Lexic.yylex();
        if (token == Tokens.SIMBOLO) {
            SimbolG s = new SimbolG(Lexic.lexema, -1, !vn.contains(Lexic.lexema)); // si no está en vn -> terminal provisional
            // Corregir: si el símbolo ya está declarado como no terminal lo marcamos
            if (vn.contains(Lexic.lexema)) s.esTerminal = false;
            if (SecSimbolosP(l)) {
                l.add(0, s); // insertar al inicio para mantener orden leído -> izquierda
                return true;
            }
            return false;
        }
        Lexic.undoToken(); // ε
        return true;
    }

    // SecSimbolosP -> permite más símbolos en la secuencia
    public boolean SecSimbolosP(List<SimbolG> l) {
        int token = Lexic.yylex();
        if (token == Tokens.SIMBOLO) {
            SimbolG s = new SimbolG(Lexic.lexema, -1, !vn.contains(Lexic.lexema));
            if (vn.contains(Lexic.lexema)) s.esTerminal = false;
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

        // Si es terminal o epsilon
        if (primero.esTerminal || "epsilon".equals(primero.nombSimb)) {
            R.add(primero);
            return R;
        }

        // primero es no terminal: buscar reglas con ese lado izquierdo
        for (int i = 0; i < numReglas; i++) {
            if (reglas[i].simIzq != null && reglas[i].simIzq.nombSimb.equals(primero.nombSimb)) {
                R.addAll(First(reglas[i].ladoDerecho));
            }
        }

        // si R contiene epsilon, propagar hacia la derecha
        boolean contieneEps = false;
        SimbolG eps = SimbolG.EPSILON();
        for (SimbolG s : R) {
            if ("epsilon".equals(s.nombSimb)) {
                contieneEps = true;
                break;
            }
        }
        if (contieneEps) {
            R.remove(eps);
            if (l.size() > 1) {
                R.addAll(First(l.subList(1, l.size())));
            } else {
                R.add(eps);
            }
        }
        return R;
    }

    public Set<SimbolG> Follow(SimbolG s) {
        Set<SimbolG> R = new HashSet<>();
        if (s.esTerminal) return R;

        if (s.equals(simboloInicial)) {
            R.add(SimbolG.DOLAR());
        }

        for (int i = 0; i < numReglas; i++) {
            List<SimbolG> lado = reglas[i].ladoDerecho;
            if (lado == null) continue;
            for (int j = 0; j < lado.size(); j++) {
                if (lado.get(j).nombSimb.equals(s.nombSimb)) {
                    if (j == lado.size() - 1) {
                        if (!s.equals(reglas[i].simIzq)) {
                            R.addAll(Follow(reglas[i].simIzq));
                        }
                    } else {
                        List<SimbolG> sublista = lado.subList(j + 1, lado.size());
                        Set<SimbolG> aux = First(sublista);
                        SimbolG epsSym = SimbolG.EPSILON();
                        boolean tieneEps = false;
                        for (SimbolG sg : aux) {
                            if ("epsilon".equals(sg.nombSimb)) { tieneEps = true; break; }
                        }
                        if (tieneEps) {
                            aux.remove(epsSym);
                            R.addAll(aux);
                            R.addAll(Follow(reglas[i].simIzq));
                        } else {
                            R.addAll(aux);
                        }
                    }
                }
            }
        }
        return R;
    }
}


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

