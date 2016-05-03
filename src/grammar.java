import java.util.ArrayList;

public class grammar {
	
	private int numberTokens = 0; // quantos tokens já foram lidos
	private ArrayList<Token> tokens; //lista com todos os tokens recebidos, em ordem de ecscopo
	private ArrayList<String> error; //lista com os erros sintaticos
	private Token current; //token atual

	public Token nextToken(){ 
		if (numberTokens < tokens.size()) { 
            return tokens.get(numberTokens++);
        } else {
        	
            return new Token(-1, "End of File", null);  //cria um token de fim de arquivo. 
        }
	}
	
	public void consumeToken(String expected){
		if ((current.getRepresentation().equals(expected) || current.getType().equals(expected)) && (!current.getRepresentation().equals("End of File"))) { //verifica se o token atual e o que era esperado
            current = nextToken();
        } else {
            //metodo que gera o erro  
        }
		
	}
	
	public void fileStructure(){
		
	}
	
	public void classStructure(){
		 switch (current.getRepresentation()) {
         case "class":
        	 consumeToken("class");
        	 consumeToken("identificador");
        	 expressionHeritage();
        	 consumeToken("{");
        	 contentClass();
        	 consumeToken("}");
             break;
         default:
             //metodo do erro sintatico
             break;
     }
	}
	
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
	
	public void idDeclaration(){
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
            case "char":
                consumeToken("char");
                consumeToken("identificador");
                idComplement();
                break;
            case "int":
                consumeToken("int");
                consumeToken("identificador");
                idComplement();
                break;
            case "bool":
                consumeToken("bool");
                consumeToken("identificador");
                idComplement();
                break;
            case "string":
                consumeToken("string");
                consumeToken("identificador");
                idComplement();
                break;
            case "float":
                consumeToken("float");
                consumeToken("identificador");
                idComplement();
                break;
            default:
                if (current.getType().equals("identificador")) {
                    consumeToken("identificador");
                    consumeToken("identificador");
                    idComplement();
                    break;
                } else {
                    //error - erroSintatico("espera um tipo: id, int, float, char, string, bool, void");
                }
                break;
        }
	}
	
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
	
	public void paramDeclaration(){
        switch (current.getRepresentation()) {
            case "char":
                consumeToken("char");
                consumeToken("identificador");
                vectorDeclaration();
                paramList();
                break;
            case "int":
                consumeToken("int");
                consumeToken("identificador");
                vectorDeclaration();
                paramList();
                break;
            case "bool":
                consumeToken("bool");
                consumeToken("identificador");
                vectorDeclaration();
                paramList();
                break;
            case "string":
                consumeToken("string");
                consumeToken("identificador");
                vectorDeclaration();
                paramList();
                break;
            case "float":
                consumeToken("float");
                consumeToken("identificador");
                vectorDeclaration();
                paramList();
                break;
            default:
                if (current.getType().equals("identificador")) {
                	consumeToken("identificador");
                    consumeToken("identificador");
                    vectorDeclaration();
                    paramList();
                }
                break;
        }
	}
	
	public void constantBlock(){
        switch (current.getRepresentation()) {
            case "char":
                consumeToken("char");
                constantList();
                break;
            case "int":
                consumeToken("int");
                constantList();
                break;
            case "bool":
                consumeToken("bool");
                constantList();
                break;
            case "string":
                consumeToken("string");
                constantList();
                break;
            case "float":
                consumeToken("float");
                constantList();
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
	
	public void contentMethod(){
		switch ((String)current.getType()) {
        case "palavra_reservada":
            if (current.getRepresentation().equals("return")) {
                break;
            }
            commandDeclaration();
            contentMethod();
            break;
        case "identificador":
            commandDeclaration();
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
	
	public void idComplement(){
		 switch (current.getRepresentation()) {
         case "[":
             consumeToken("[");
             indexDeclaration();
             consumeToken("]");
             vectorList();
             break;
         case "(":
             consumeToken("(");
             paramDeclaration();
             consumeToken(")");
             consumeToken("{");
             contentMethod();
             consumeToken("return");
             returnDeclaration();
             consumeToken("}");
             break;
         case ",":
             recListaVariavel();
             break;
         case ";":
             consumeToken(";");
             break;
         default:
             //error - erroSintatico("falta ; ou , ou [ ou (");
             break;
		 }
	}
	
	public void vectorDeclaration(){
		switch (current.getRepresentation()) {
        case "[":
            consumeToken("[");
            indexDeclaration();
            consumeToken("]");
            break;
        default:
            break;
		}
	}
	
	public void paramList(){
		switch (current.getRepresentation()) {
        case ",":
            consumeToken(",");
            recTipo();
            consumeToken("id");
            vectorDeclaration();
            paramList();
            break;
        default:
            break;
		}
	}
	
	public void constantList(){
        consumeToken("id");
        consumeToken("=");
        recAtribuicaoConstante();
        recAuxiliarDeclaracao();
	}
	
	public void commandDeclaration(){
		switch (current.getRepresentation()) {
        case "read":
            recRead();
            break;
        case "write":
            recWrite();
            break;
        case "new":
            recInicializaObjeto();
            break;
        case "if":
            recIf();
            break;
        case "while":
            recWhile();
            break;
        case "char":
            consumeToken("char");
            consumeToken("identificador");
            idDeclaration();
            break;
        case "int":
            consumeToken("int");
            consumeToken("identificador");
            idDeclaration();
            break;
        case "bool":
            consumeToken("bool");
            consumeToken("identificador");
            idDeclaration();
            break;
        case "string":
            consumeToken("string");
            consumeToken("identificador");
            idDeclaration();
            break;
        case "float":
            consumeToken("float");
            consumeToken("identificador");
            idDeclaration();
            break;
        default:
            if (current.getType().equals("identificador")) {
                consumeToken("identificador");
                if (current.getType().equals("identificador")) {
                    consumeToken("identificador");
                    idDeclaration();
                } else {
                    recIdComando();
                }

            } else {
                //erro - erroSintatico("falta identificador ou palavra reservada: read, write, new, if, while, char, int, float, string, bool");
            }
            break;

			}
		}
	
	public void indexDeclaration(){
		 switch (current.getType()) {
         case "identificador":
             consumeToken("identificador");
             break;
         case "numero":
             consumeToken("numero");
             break;
         default:
             //erro - erroSintatico("falta identificador ou numero");
             break;
		 }
	}
	
	public void vectorList(){
		switch (current.getRepresentation()) {
        case ",":
            consumeToken(",");
            consumeToken("id");
            consumeToken("[");
            indexDeclaration();
            consumeToken("]");
            vectorList();
            break;
        case ";":
            consumeToken(";");
            break;
        default:
            while (!current.getRepresentation().equals(",") && !current.getRepresentation().equals(";") && !current.getType().equals("palavra_reservada")) {
                //erroSintatico("falta , ou ;");
                current = nextToken();
            }
            break;
		}
	}
	
	public void returnDeclaration(){
		recAtribuicao();
        consumeToken(";");
	}
	
	public void variableList(){
		switch (current.getRepresentation()) {
        case ",":
            consumeToken(",");
            consumeToken("identificador");
            variableList();
            break;
        case ";":
            consumeToken(";");
            break;
        default:
            while (!current.getRepresentation().equals(",") && !current.getRepresentation().equals(";") && !current.getType().equals("palavra_reservada")) {
                //erroSintatico("falta , ou ;");
                current = nextToken();
            }
            break;
		}
	}
	
	
	
		
	}