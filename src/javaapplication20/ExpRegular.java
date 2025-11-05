package javaapplication20;

public class ExpRegular {

    private analizadorLexico L; // El analizador léxico para la EXPRESIÓN REGULAR
    // Almacén para el AFN resultante
    public AFN resultado;
    String expresion;

    // --- Definición de Tokens (temporal)---
    private static final int T_UNION = 10;      // |
    private static final int T_CONCAT = 20;     // & 
    private static final int T_POS = 30;        // +
    private static final int T_KLEENE = 40;     // *
    private static final int T_OPC = 50;        // ?
    private static final int T_PARENI = 60;     // (
    private static final int T_PAREND = 70;     // )
    private static final int T_SIMBOLO = 80;    
    private static final int T_CORCHI = 90;   // [
    private static final int T_CORCHD = 100;  // ]
    private static final int T_GUION = 110;     // -
    

    public ExpRegular(String sigma, String afd) {
        expresion = sigma;
        L = new analizadorLexico(expresion, afd);
    }
    
    public ExpRegular(String afd) {
        L = new analizadorLexico(expresion, afd);
    }
    
    public void SetExpresion(String sigma){
        expresion = sigma;
        L.SetSigma(sigma);
    }
    

    public boolean IniConversion() {
        int token;
        AFN f = new AFN(); 
        
        if (E(f)) {
            // Verificamos que hemos llegado al final de la expresión
            token = L.yylex();
            if (token == SimbEsp.FIN()) {
                this.resultado = f; // Guardamos el resultado final
                return true;
            }
        }
        return false;
    }

    // E -> T Ep
    private boolean E(AFN f) {
        if (T(f)) {
            if (Ep(f)) {
                return true;
            }
        }
        return false;
    }

    // Ep -> | T Ep | ε (epsilon)
    private boolean Ep(AFN f) {
        int token = L.yylex();
        
        if (token == T_UNION) {
            AFN f2 = new AFN(); 
            if (T(f2)) {
                f.UnirAFN(f2); 
                if (Ep(f)) {
                    return true;
                }
            }
            return false;
        }
        
        L.undoToken(); 
        return true;
    }

    // T -> C Tp
    private boolean T(AFN f) {
        if (C(f)) {
            if (Tp(f)) {
                return true;
            }
        }
        return false;
    }

    // Tp -> . C Tp | ε
    private boolean Tp(AFN f) {
        int token = L.yylex();
        
        if (token == T_CONCAT) {
            AFN f2 = new AFN(); 
            if (C(f2)) {
                f.Concatenacion(f, f2); 
                
                if (Tp(f)) {
                    return true;
                }
            }
            return false;
        }
        
        L.undoToken();
        return true;
    }

    // C -> F Cp
    private boolean C(AFN f) {
        if (F(f)) {
            if (Cp(f)) {
                return true;
            }
        }
        return false;
    }

    // Cp -> + Cp | * Cp | ? Cp | ε
    private boolean Cp(AFN f) {
        int token = L.yylex();
        
        switch (token) {
            case T_POS:
                f.CerraduraPositiva();
                break;
            case T_KLEENE:
                f.CerraduraKleen();
                break;
            case T_OPC:
                f.Opcional();
                break;
            default:
                L.undoToken();
                return true; 
        }
        return Cp(f); // Busca más operadores (ej. a*+)
    }

    // F -> ( E ) | SIMBOLO | [ RANGO ]
    private boolean F(AFN f) {
        int token = L.yylex();
        
        switch (token) {
            case T_PARENI: // (
                if (E(f)) {
                    token = L.yylex();
                    if (token == T_PAREND) { // )
                        return true;
                    }
                }
                return false;

            case T_SIMBOLO: 
                char simb1 = L.lexema.charAt(0);
                if(simb1 == 92)
                    simb1 = L.lexema.charAt(1);
                f.CrearBasico(simb1);
                return true;

            case T_CORCHI: // [
                char s1, s2;
                token = L.yylex();
                if (token == T_SIMBOLO) {
                    s1 = L.lexema.charAt(0);
                    token = L.yylex();
                    if (token == T_GUION) { // -
                        token = L.yylex();
                        if (token == T_SIMBOLO) {
                            s2 = L.lexema.charAt(0);
                            token = L.yylex();
                            if (token == T_CORCHD) { // ]
                                if (s1 > s2) return false;
                                f.CrearBasico(s1, s2);
                                return true;
                            }
                        }
                    }
                }
                return false; // Error en la sintaxis del rango

            default:
                L.undoToken();
                return false;
        }
    }
}