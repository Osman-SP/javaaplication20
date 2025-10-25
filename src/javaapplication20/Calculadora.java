package javaapplication20;

public class Calculadora {
    String Expresion;
    public float result;
    public String ExprPost;
    public analizadorLexico L; // Instancia del analizador léxico

    // ... Constructor de Calculadora ...

    public boolean iniEval(){
        int token;
        float v_inicial = 0.0f; 
        String postFijo_inicial = "";

        EnvoltorioFloat refV = new EnvoltorioFloat(v_inicial);
        EnvoltorioString refPostFijo = new EnvoltorioString(postFijo_inicial);

        // Llamada a la función E con los envoltorios
        if(E(refV, refPostFijo)){ 
            token = L.yylex();
            if(token == 0){ // Fin de cadena
                this.result = refV.flotante; 
                this.ExprPost = refPostFijo.valor.trim(); // Limpiar espacios al inicio/final
                return true;
            }
        }
        return false;
    }
    
    
    // E -> T Ep
    public boolean E(EnvoltorioFloat v, EnvoltorioString Post){
        if(T(v, Post)){
            if(Ep(v, Post)){
                return true;
            }
        }
        return false;
    }
    
    // Ep -> + T Ep | - T Ep | e
    public boolean Ep(EnvoltorioFloat v, EnvoltorioString Post){
        int token;
        EnvoltorioFloat refV2 = new EnvoltorioFloat(0.0f);
        EnvoltorioString refPost2 = new EnvoltorioString("");
        
        token = L.yylex();
        if(token == 10 || token == 20){ // + o -
            if(T(refV2, refPost2)){
                // Cálculo de valor
                v.flotante = v.flotante + (token == 10 ? refV2.flotante : -refV2.flotante);
                
                // Generación de Postfijo
                Post.valor = Post.valor + " " + refPost2.valor + " " + (token == 10 ? "+" : "-");
                
                if(Ep(v, Post))
                    return true;
            }
            return false;
        }
        L.undoToken();
        return true;
    }

    // T -> P Tp
    public boolean T(EnvoltorioFloat v, EnvoltorioString Post){
        if(P(v, Post)){
            if(Tp(v, Post)){
                return true;
            }
        }
        return false;
    }
    
    // Tp -> * P Tp | / P Tp | e
    public boolean Tp(EnvoltorioFloat v, EnvoltorioString Post){
        int token;
        EnvoltorioFloat refV2 = new EnvoltorioFloat(0.0f);
        EnvoltorioString refPost2 = new EnvoltorioString("");
        
        token = L.yylex();
        if(token == 30 || token == 40){ // * o /
            if(P(refV2, refPost2)){
                // Cálculo de valor
                v.flotante = v.flotante * (token == 30 ? refV2.flotante : 1/refV2.flotante);
                
                // Generación de Postfijo
                Post.valor = Post.valor + " " + refPost2.valor + " " + (token == 30 ? "*" : "/");
                
                if(Tp(v, Post))
                    return true;
            }
            return false;
        }
        L.undoToken();
        return true;
    }

    // P -> F Pp
    public boolean P(EnvoltorioFloat v, EnvoltorioString Post){
        if(F(v, Post)){
            if(Pp(v, Post)){
                return true;
            }
        }
        return false;
    }
    
    // Pp -> ^ F Pp | e
    public boolean Pp(EnvoltorioFloat v, EnvoltorioString Post){
        int token;
        EnvoltorioFloat refV2 = new EnvoltorioFloat(0.0f);
        EnvoltorioString refPost2 = new EnvoltorioString("");
        
        token = L.yylex();
        if (token == 50){ // ^
            if (F(refV2, refPost2)){
                // Cálculo de valor: Math.pow opera con double
                v.flotante = (float)Math.pow(v.flotante, refV2.flotante); 
                
                // Generación de Postfijo
                Post.valor = Post.valor +" "+ refPost2.valor + " ^";
                
                if (Pp(v, Post)){
                    return true;
                } 
            }
            return false;
        }
        L.undoToken();
        return true;
    }
    
    // F -> ( E ) | FUNC ( E ) | PI | NUM
    public boolean F(EnvoltorioFloat v, EnvoltorioString Post){
        int token;
        token = L.yylex();
        String nombreFuncion;
        
        switch(token){
            case 70: // par_i '('
                if(E(v, Post)){
                    token = L.yylex();
                    if(token == 80) // par_d ')'
                        return true;
                }
                return false;
            
            case 60: // FUNC (sin, cos, tan, etc.)
                /* Guardar el nombre de la función (el lexema) para poder compararlo y saber
                  de que funcion se trata*/
                nombreFuncion = L.lexema.toLowerCase();
                
                token = L.yylex();
                if (token == 70) { // Debe seguir un '('
                    if (E(v, Post)) { // Evaluar la expresión dentro de los paréntesis
                        token = L.yylex();
                        if (token == 80) { // Debe seguir un ')'
                            
                            //Calculo del valor
                            v.flotante = aplicarFuncionTrigonometrica(nombreFuncion, v.flotante);
                            // Generación de Postfijo
                            Post.valor = Post.valor + " " + nombreFuncion; 
                            return true;
                        }
                    }
                }
                return false;

            case 90: // PI
                v.flotante = (float)Math.PI;
                Post.valor = "pi";
                return true;
                
            case 100: // NUM
                try {
                    v.flotante = Float.parseFloat(L.lexema);
                    Post.valor = L.lexema;
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
        }
        return false;
    }

    
    /** * Aplica la función trigonométrica al valor dado, 
     * convirtiendo la entrada de grados a radianes antes del cálculo. 
     */
    private float aplicarFuncionTrigonometrica(String func, float valor) {
        
        // --- CONVERSIÓN DE GRADOS A RADIANES ---
        valor = (float) Math.toRadians(valor); 
        // ---------------------------------------

        switch (func.toLowerCase()) {
            case "sin":
                return (float) Math.sin(valor);
            case "cos":
                return (float) Math.cos(valor);
            case "tan":
                return (float) Math.tan(valor);
            case "cot":
                // cot(x) = 1 / tan(x)
                if (Math.tan(valor) == 0) return Float.NaN; // Manejar división por cero
                return (float) (1.0 / Math.tan(valor));
            case "sec":
                // sec(x) = 1 / cos(x)
                if (Math.cos(valor) == 0) return Float.NaN;
                return (float) (1.0 / Math.cos(valor));
            case "csc":
                // csc(x) = 1 / sin(x)
                if (Math.sin(valor) == 0) return Float.NaN;
                return (float) (1.0 / Math.sin(valor));
            case "asin":
                // Las inversas ya operan sobre un valor sin unidades angulares
                return (float) Math.asin(valor); 
            case "acos":
                return (float) Math.acos(valor);
            case "atan":
                return (float) Math.atan(valor);
            case "acot":
                // acot(x) = atan(1/x)
                return (float) Math.atan(1.0 / valor);
            case "asec":
                // asec(x) = acos(1/x)
                return (float) Math.acos(1.0 / valor);
            case "acsc":
                // acsc(x) = asin(1/x)
                return (float) Math.asin(1.0 / valor);
            default:
                // Error o función no reconocida
                return Float.NaN; 
        }
    }
}


//Clases para pasar simular el paso por referencia del valor
class EnvoltorioFloat{
    public float flotante;
    
    public EnvoltorioFloat(float f){
        this.flotante=f;
    }
}

class EnvoltorioString {
    public String valor;
    public EnvoltorioString(String s) {
        this.valor = s;
    }
}