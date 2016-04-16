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
            recConst();
            recConteudoClasse();
            break;
        case "const":
            recIdDeclaracao();
            recConteudoClasse();
            break;
        default:
            if (proximo.getTipo().equals("palavra_reservada") || proximo.getTipo().equals("id")) {
                recIdDeclaracao();
                recConteudoClasse();
                break;
            } else if (!proximo.getValor().equals("}") && !proximo.getValor().equals("class")) { //recuperaçao do erro, verifica se acabou o bloco, ou surgiu outra classe
                erroSintatico("falta declaraçao de variavel ou de metodo");
                proximo = proximo();
                recConteudoClasse();
            }
            break;
    }
	}
	
	public void idDeclaration(ArrayList<Token> tokens, int i, Parsing parser){
		
	}
	
	public void idComplement(ArrayList<Token> tokens, int i, Parsing parser){
	}
}