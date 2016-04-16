import java.util.ArrayList;

public class grammar {
	
	private int numberTokens = 0; // quantos tokens já foram lidos
	private ArrayList<Token> tokens; //lista com todos os tokens recebidos, em ordem de ecscopo
	private ArrayList<String> error; //lista com os erros sintaticos
	private Token current; //token atual

	/**
	 * Método para pegar o proximo token a ser analisado
	 * @return 
	 * 
	 */
	public Token nextToken(){ 
		if (numberTokens < tokens.size()) { 
            return tokens.get(numberTokens++);
        } else {
        	
            return new Token(-1, "End of File", null);  //cria um token de fim de arquivo. 
        }
	}
	
	/**
	 * Metodo para comparar  e conduzir ao proximo token
	 * @param expected
	 */
	
	public void consumeToken(String expected){
		if ((current.getRepresentation().equals(expected) || current.getType().equals(expected)) && (!current.getRepresentation().equals("End of File"))) { //verifica se o token atual e o que era esperado
            current = nextToken();
        } else {
            //metodo que gera o erro  
        }
		
	}
	
	/**
	 * Metodo para reconhecer a estrutura do arquivo
	 */
	public void fileStructure(){
		
	}
	
	/**
	 * Metodo que reconhece as produções para estrutura de uma classe
	 * 
	 */
	public void classStructure(){
		 switch (current.getRepresentation()) {
         case "class":
        	 consumeToken("class");
        	 consumeToken("identificador");
        	 expressionHeritage();
        	 consumeToken("{");
        	 contentClass();
        	 consumeToken("}");
             //atual = new Simbolos();
             //atual.setCategoria(Simbolos.CLASS);
             //atual.setNome(proximo.getValor());
             //escopo.addFilho(atual);
            // Simbolos anterior = escopo; //salva o antigo escopo
             //escopo = atual; //o novo escopo e a classe atual
             //escopo = anterior; //volta o escopo para o pai da classe.
             break;
         default:
             //metodo do erro sintatico
             break;
     }
	}
	/**
	 * Metodo que reconhece a herança da classe
	 * 
	 */
	public void expressionHeritage(){
		switch (current.getRepresentation()) {
        case ">":
            consumeToken(">");
            consumeToken("identificador");
            break;
        default:
            break;
		}
	}
	
	/**
	 * Metodo para reconhecer o conteudo da classe
	 * 
	 */
	public void contentClass(){
		switch (current.getRepresentation()) {
        case "void":
            idDeclaration();
            contentClass();
            break;
        case "const":
            constantDeclaration();
            contentClass();
            break;
        default:
            if (current.getType().equals("palavra_reservada") || current.getType().equals("identificador")) {
                idDeclaration();
                contentClass();
                break;
            } else if (!current.getRepresentation().equals("}") && !current.getRepresentation().equals("class")) {
                //erro - erroSintatico("falta declaraçao de variavel ou de metodo");
                current = nextToken();
                contentClass();
            }
            break;
		}
	}
	
	/**
	 * Reconhece Declaração de 
	 * 
	 */
	public void idDeclaration(){
		//atual = new Simbolos();
        switch (current.getRepresentation()) {
            case "void":
                consumeToken("void");
                consumeToken("identificador");
                consumeToken("(");
                paramDeclaration();
                consumeToken(")");
                consumeToken("{");
                contentMethod();
                consumeToken("}");
                //atual.setCategoria(Simbolos.MET);
                //atual.setconsumeToken(Simbolos.VOID);
                //atual.setNome(current.getValor());
                //escopo.addFilho(atual);
                //Simbolos anterior = escopo;
                //escopo = atual;
                //escopo = anterior;
                break;
            case "char":
                consumeToken("char");
                consumeToken("identificador");
                idComplement();
                //atual.setconsumeToken(Simbolos.CHAR);
                //atual.setNome(current.getValor());
                break;
            case "int":
                consumeToken("int");
                consumeToken("identificador");
                idComplement();
                //atual.setconsumeToken(Simbolos.INT);
                //atual.setNome(current.getValor());
                break;
            case "bool":
                consumeToken("bool");
                consumeToken("identificador");
                idComplement();
                //atual.setconsumeToken(Simbolos.BOOL);
                //atual.setNome(current.getValor());
                break;
            case "string":
                consumeToken("string");
                consumeToken("identificador");
                idComplement();
                //atual.setconsumeToken(Simbolos.STRING);
                //atual.setNome(current.getValor());
                break;
            case "float":
                consumeToken("float");
                consumeToken("identificador");
                idComplement();
                //atual.setconsumeToken(Simbolos.FLOAT);
                //atual.setNome(current.getValor());
                break;
            default:
                if (current.getType().equals("identificador")) {
                    consumeToken("identificador");
                    consumeToken("identificador");
                    idComplement();
                    //atual.setconsumeToken(Simbolos.OBJECT);
                    //atual.setNome(current.getValor());
                    break;
                } else {
                    //error - erroSintatico("espera um tipo: id, int, float, char, string, bool, void");
                }
                break;
        }
	}
	
	/**
	 * Reconhece as declarações de constantes
	 */
	public void constantDeclaration(){
		switch (current.getRepresentation()) {
        case "const":
            consumeToken("const");
            consumeToken("{");
            constantBlock();
            consumeToken("}");
            break;
        default:
            //erro - erroSintatico("Esperava um bloco de contantes");
            break;
		}
	}
	
	/**
	 * Reconhece as declarações de parametro
	 */
	public void paramDeclaration(){
		//atual = new Simbolos();
        switch (current.getRepresentation()) {
            case "char":
                consumeToken("char");
                consumeToken("id");
                recVarVet();
                recListaParametros();
                //atual.setTipo(Simbolos.CHAR);
                //atual.setNome(proximo.getValor());
                break;
            case "int":
                consumeToken("int");
                consumeToken("id");
                recVarVet();
                recListaParametros();
                //atual.setTipo(Simbolos.INT);
                //atual.setNome(proximo.getValor());
                break;
            case "bool":
                consumeToken("bool");
                consumeToken("id");
                recVarVet();
                recListaParametros();
                //atual.setTipo(Simbolos.BOOL);
                //atual.setNome(proximo.getValor());
                break;
            case "string":
                consumeToken("string");
                consumeToken("id");
                recVarVet();
                recListaParametros();
               // atual.setTipo(Simbolos.STRING);
                //atual.setNome(proximo.getValor());
                break;
            case "float":
                consumeToken("float");
                consumeToken("id");
                recVarVet();
                recListaParametros();
               //atual.setTipo(Simbolos.FLOAT);
                //atual.setNome(proximo.getValor());
                break;
            default:
                if (current.getType().equals("id")) {
                	consumeToken("id");
                    consumeToken("id");
                    recVarVet();
                    recListaParametros();
                  //atual.setTipo(Simbolos.OBJECT);
                    //atual.setNome(proximo.getValor());
                }
                break;
        }
	}
	
	/**
	 * Reconhece as declarações de blocos de constantes
	 */
	public void constantBlock(){
		//atual = new Simbolos();
        //atual.setCategoria(Simbolos.CONST);
        switch (current.getRepresentation()) {
            case "char":
                consumeToken("char");
                recListaConst();
              //atual.setTipo(Simbolos.CHAR);
                break;
            case "int":
                consumeToken("int");
                recListaConst();
              //atual.setTipo(Simbolos.INT);
                break;
            case "bool":
                consumeToken("bool");
                recListaConst();
              //atual.setTipo(Simbolos.BOOL);
                break;
            case "string":
                consumeToken("string");
                recListaConst();
              //atual.setTipo(Simbolos.STRING);
                break;
            case "float":
                consumeToken("float");
                recListaConst();
              //atual.setTipo(Simbolos.FLOAT);
                break;
            default:
                if (!current.getRepresentation().equals("}")) {
                    //erro - erroSintatico("falta palavra reservada: int, char, bool, string, float");
                    current = nextToken();
                    constantBlock();
                }
                break;
        }
	}
	
	/**
	 * Reconhece o Conteudo de metodo
	 */
	public void contentMethod(){
		switch (current.getType()) {
        case "palavra_reservada":
            if (current.getRepresentation().equals("return")) {
                break;
            }
            recComando();
            contentMethod();
            break;
        case "id":
            recComando();
            contentMethod();
            break;
        default:
            if (!current.getRepresentation().equals("}")) {
                //erro - erroSintatico("Conteudo de médoto inválido, espera um comando.");
                current = nextToken();
                contentMethod();
            }
            break;
		}
	}
	
	/**
	 * Reconhece o complemento de identificadores 
	 * 
	 */
	public void idComplement(){
		 switch (current.getRepresentation()) {
         case "[":
             //atual.setTipo(Simbolos.VET);
             consumeToken("[");
             recIndice();
             consumeToken("]");
             recListaVetor();
             break;
         case "(":
             //atual.setTipo(Simbolos.MET);
             //escopo.addFilho(atual);
             //Simbolos anterior = escopo;
             //escopo = atual;
        	//escopo = anterior;
             consumeToken("(");
             recDeclParametros();
             consumeToken(")");
             consumeToken("{");
             contentMethod();
             consumeToken("return");
             recRetorno();
             consumeToken("}");
             break;
         case ",":
             //atual.setTipo(Simbolos.VAR);
             recListaVariavel();
             break;
         case ";":
            // atual.setTipo(Simbolos.VAR);
             //escopo.addFilho(atual);
             consumeToken(";");
             break;
         default:
             //error - erroSintatico("falta ; ou , ou [ ou (");
             break;
     }
	}
}