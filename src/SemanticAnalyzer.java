import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeMap;

public class SemanticAnalyzer {
	
	private final boolean ACCESS = true;
	private final boolean DECL = false; 
	
	private ArrayList<Token> tokens;
	private Stack<Token> scopeStack;
	private int currentTokenIndex;
	
	private TreeMap<Token, Scope> scopeTree;
	
	
	public SemanticAnalyzer (LexIO io) {
		this.tokens = io.getTokens();
		this.scopeStack = new Stack<>();
		this.currentTokenIndex = 0;
		this.scopeTree = new TreeMap<>();
	}
	
	private Token currentToken() {
		if(currentTokenIndex < tokens.size())
			return tokens.get(this.currentTokenIndex);
		
		return new Token(-1, "end", null);
	}
	
	private Token nextToken() {
		return tokens.get(this.currentTokenIndex++); 
	}
	
	private Token lookAhead() {
		return tokens.get(this.currentTokenIndex+1);
	}
	
	private void parseTerminals(String... terminals) {
		for (String terminal : terminals ) {
			this.parseTerminal(terminal);
		}
	}
	
	private void parseTerminal(String terminal) {
		System.out.println("Parsing " + terminal);
		Token next = this.nextToken();
		if(!next.is(terminal)) {
			System.out.println("Wow, deu ruim. Expected " + terminal + "  got " + next.getRepresentation());
		};
	}
	
	private void parsePrimitiveType() {
		// <tipo_primitivo> ::= 'int' | 'char' | 'bool' | 'string' | 'float'
		Token next = this.nextToken();
		System.out.println("Parsing primitive " + next.getRepresentation());
	}
	
	private void parseIdentifier(boolean isAccess){
		Token next = this.nextToken();
		if(next.isIdentifier())
			System.out.println("Parsing " + next.getRepresentation() + (isAccess ? "  - access" : " - decl"));
		else
			System.out.println("Wow, deu ruim. Expected Identifier, got " + next.getRepresentation());
	}
	
	
	private void parseProgram() {
		// <arquivo> ::= <constantes> <variaveis> <pre_main>
		parseConstants();
		parseVariables();
		parsePreMain();
	}
	
	private void parseConstants() {
		// <constantes> ::= <const><constantes> | <lambda> 
		
		if(this.currentToken().is("const")){
			parseConstant();
			parseConstants();
		}
	}
	
	private void parseConstant() {
		// <const>::= 'const' '{' <bloco_constantes> '}'
		
		parseTerminals("const", "{");
		parseConstBlock();
		parseTerminal("}");
	}
	
	private void parseConstBlock() {
		// <bloco_constantes> ::= <tipo_primitivo> <lista_const> | <lambda>
		if(this.currentToken().isPrimitiveType()) {
			parsePrimitiveType();
			parseConstantList();
		}
		
	}
	
	private void parseConstantList() {
		// <lista_const> ::= Identifier '=' <atribuicao_costante> <aux_declaracao>
		parseIdentifier(ACCESS);
		parseTerminal("=");
		parseConstantAssignment();
		parseConstantListExtension();
	}
	
	private void parseConstantListExtension() {
		// <aux_declaracao> ::= ','<lista_const> | ';' <bloco_constantes>
		
		if(this.currentToken().is(",")) {
			parseTerminal(",");
			parseConstantList();
		} else if (this.currentToken().is(";")) {
			this.parseTerminal(";");
			this.parseConstBlock();
		}
	}
	
	private void parseConstantAssignment () {
		// <atribuicao_costante> ::= Numero | Char1 | Cadeia | <boolean>
		
		Token next = this.nextToken();
		if(next.isBool() || next.isChar() || next.isString() || next.isNumber())
			System.out.println("Parsing primitive assignment " + next.getRepresentation());
		else 
			System.out.println("Error on assignment, got " + next);
	}
	
	private void parseVariables() {
		// <variaveis> ::= <declaracao_variavel> <variaveis> | <lambda>
		
		if(this.currentToken().isPrimitiveType()) {
			parseVariableDeclaration();
			parseVariables();
		}
	}
	
	private void parseVariableDeclaration() {
		// <declaracao_variavel> ::= <tipo_primitivo> Identifier <lista_variavel>
		parsePrimitiveType();
		parseIdentifier(DECL);
		parseVariableList();
	}
	
	private void parseVariableList(){
		// <lista_variavel> ::= ',' Identifier <lista_variavel> | ';'
		
		if(this.currentToken().is(",")) {
			parseTerminal(",");
			parseIdentifier(DECL);
			parseVariableList();
		} else if(this.currentToken().is(";")) {
			parseTerminal(";");
		}
	}
	
	private void parsePreMain() {
		// <pre_main> ::= <main><classes> | <classe> <pre_main>
		
		if(this.currentToken().is("void")){
			parseMain();
			parseClasses();
		} else if (this.currentToken().is("class")) {
			parseClass();
			parsePreMain();
		}
	}
	
	private void parseMain() {
		//	<main> ::= 'void' 'main' '(' ')' '{' <conteudo_metodo> '}'
		
		parseTerminals("void", "main", "(", ")", "{");
		parseMethodContents();
		parseTerminal("}");
	}
	
	private void parseClasses () {
		// <classes> ::= <classe> <classes> |<lambda>
		
		if(this.currentToken().is("class")) {
			parseClass();
			parseClasses();
		}
	}
	
	private void parseClass() {
		// <classe> ::= 'class' Identifier <expressao_heranca> '{' <conteudo_classe> '}'
		
		parseTerminal("class");
		parseIdentifier(DECL);
		parseInheritance();
		parseTerminal("{");
		parseClassContents();
		parseTerminal("}");
	}
	
	private void parseInheritance() {
		// <expressao_heranca> ::= '>' Identifier | <lambda>
		if(this.currentToken().is(">")){
			parseTerminal (">");
			parseIdentifier(ACCESS);
		}
	}
	
	private void parseClassContents() {
		// <conteudo_classe> ::= <id_declaracao><conteudo_classe> | <const> <conteudo_classe>|<lambda>
		
		if(this.currentToken().is("const")) {
			parseConstants();
			parseClassContents();
		} else if (this.currentToken().isType() || this.currentToken().is("void")) {
			parseDeclaration();
			parseClassContents();
		}
	}
	
	private void parseDeclaration() {
		// <id_declaracao> ::= <tipo> Identifier <comp_id> | 'void' Identifier '(' <decl_parametros> ')' '{' <conteudo_metodo> '}' 
	
		if(this.currentToken().isType()) {
			parseType();
			parseIdentifier(DECL);
			parseIdentifierExtension();
		} else if (this.currentToken().is("void")) {
			parseTerminal("void");
			parseIdentifier(DECL);
			parseTerminal("(");
			parseParametersDeclaration();
			parseTerminals(")", "{");
			parseMethodContents();
			parseTerminal("}");
		}
	}
	
	private void parseIdentifierExtension () {
		// <comp_id> ::=  '['<indice>']' <lista_vetor> | '(' <decl_parametros> ')' '{' <conteudo_metodo> 'return' <retorno> '}' | <lista_variavel>
		
		switch (this.currentToken().getRepresentation()) {
			case "[":
				parseVectorDeclaration();
				parseVectorList();
			break;
				
			case "(":
				parseTerminal("(");
				parseParametersDeclaration();
				parseTerminals(")", "{");
				parseMethodContents();
				parseTerminal("return");
				parseReturn();
				parseTerminal("}");
			break;
			
			case ",":
			case ";":
				parseVariableList();
			break;
		}
		
	}
	
	private void parseVectorIndex() {
		
		if(this.currentToken().isNumber()) {
			this.nextToken();
		} else if (this.currentToken().isIdentifier()) {
			parseIdentifier(ACCESS);
		}
		
	}
	
	private void parseVectorList() {
		// <lista_vetor>::=',' Identifier '['<indice>']' <lista_vetor>  | ';'
		
		if(this.currentToken().is(",")) {
			parseTerminal(",");
			parseIdentifier(DECL);
			parseVectorDeclaration();
			parseVectorList();
		} else if (this.currentToken().is(";")){
			this.parseTerminal(";");
		}
	}
	
	private void parseVectorDeclaration() {
		if(this.currentToken().is("[")) {
			parseTerminal("[");
			parseVectorIndex();
			parseTerminal("]");
		}
	}
	
	private void parseType() {
		if(this.currentToken().isPrimitiveType()) {
			parsePrimitiveType();
		} else if (this.currentToken().isIdentifier()) {
			parseIdentifier(ACCESS);
		}
	}
	
	private void parseParametersDeclaration () {
		// <decl_parametros> ::= <tipo> Identifier <var_vet> <lista_parametros> | <lambda>
		
		if(this.currentToken().isType()) {
			parseParameter();
		}
	}
	
	private void parseParameter() {
		parseType();
		parseIdentifier(DECL);
		parseVectorDeclaration();
		parseParametersList();
	}
	
	private void parseParametersList() {
		//<lista_parametros> ::= ','<tipo> Identifier <var_vet> <lista_parametros> | <lambda>
		
		if(this.currentToken().is(",")) {
			parseTerminal(",");
			parseParameter();
		}
	}
	
	private void parseMethodContents () {
		// <conteudo_metodo> ::= <comando><conteudo_metodo> | <lambda>
		if (this.currentToken().isCommand()) {
			parseCommand();
			parseMethodContents();
		}
	}
	
	private void parseReturn() {
		parseAssignment();
		parseTerminal(";");
	}
	
	private void parseCommand() {
		
		if(this.currentToken().is("read")) {
			parseRead();
		} else if (this.currentToken().is("write")) {
			parseWrite();
		} else if (this.currentToken().isType() && this.lookAhead().isIdentifier()) {
			// <tipo> Identifier <id_decl>
			
			parseType();
			parseIdentifier(DECL);
			parseVectorDeclaration();  // TODO vector list != var list (or is it?)
			parseVectorList();
		} else if (this.currentToken().isIdentifier()) {
			// Identifier <id_comando>
			
			parseIdentifier(ACCESS);
			parseCommandExtension();
		} else if (this.currentToken().is("if")) {
			
			parseIf();
			
		} else if (this.currentToken().is("while")) {
			
			parseWhile();
			
		}
		
	}
	
	
	private void parseCommandExtension() {
		//<id_comando> ::= '(' <parametros> ')' ';' | '.' Identifier <acesso_objeto> ';' | '=' <atribuicao> ';' | '[' <indice> ']' '=' <atribuicao> ';'
	
		switch (this.currentToken().getRepresentation()) {
			case "(":
				parseTerminal("(");
				parseParameters();
				parseTerminal(")");
			break;
			
			case ".": 
				parseTerminal(".");
				parseIdentifier(ACCESS);
				parseObjectAccess();
			break;
			
			case "=":
				parseTerminal("=");
				parseAssignment();
			break;
			
			case "[":
				parseTerminal("[");
				parseVectorIndex();
				parseTerminals("]", "=");
				parseAssignment();
			break;
		}
		
		parseTerminal(";");
	}
	
	private void parseObjectAccess(){
		if(this.currentToken().is("(")){
			parseTerminal("(");
			parseParameters();
			parseTerminal(")");
		} else if (this.currentToken().is("=")) {
			parseTerminal("=");
			parseAssignment();
		}
	}
	
	private void parseAssignment() {
		switch (this.currentToken().getRepresentation()) {
        case "(":
            parseTerminal("(");
            parseAssignment();
            parseTerminal(")");
            parseOperator();
            break;
        case "++":
        case "--":
            parseIdentifierAccess();
            break;
        case "true":
        case "false":
            parseLogicalOperator();
            break;
        case "-":
            parseTerminal("-");
            parseNegative();
            break;
        default:
            if(this.currentToken().isIdentifier()) {
            	parseIdentifierAccess();
            } else if (this.currentToken().isNumber()) {
            	parseNumericalOperand();
            } else if (this.currentToken().isChar() || this.currentToken().isString()) {
            	this.nextToken();
            }
        }
    }
	
	private void parseParameters() {
		if(this.currentToken().isAssignable()) {
			parseAssignment();
		}
	}
	
	private void parseOperator() { 
		if(this.currentToken().isRelationalOperator()) {
			// <operador_relacional> <exp_aritmetica><op_logico>
			parseRelationalOperator();
			parseArithmeticExpression();
			parseLogicalOperator();
		} else if (this.currentToken().isArithmeticOperator()) {
			//<operador_aritmetico><exp_aritmetica><exp_relacional_opcional>
			parseArithmeticOperator();
			parseArithmeticExpression();
			parseOptionalRelationalExpression();
		} else if (this.currentToken().isEqualityOperator()) {
			// <operador_igualdade><atribuicao>
			parseEqualityOperator();
			parseAssignment();
		} else if (this.currentToken().isLogicalOperator()) {
			// <operador_logico><exp>
			parseLogicalOperator();
			parseExpression();
		}
		
	}
	
	
	
	
	
	
	
	
	
	private void pushScope (Token scopedToken) {
		this.scopeStack.push(scopedToken);
	}
	
	private void addError (String error, int line) {
		System.out.println("Erro na linha " + line + " " + error);
	}
	
	private void checkType (String declarationType, Token currentToken, Token nextToken) {
		switch (declarationType) {
			case "int":
				if(!nextToken.isInteger())
					addError("Tipo inv�lido, inteiro esperado", currentToken.getLine());
			break;
			
			case "float":
				if(!nextToken.isFloat())
					addError("Tipo inv�lido, decimal esperado", currentToken.getLine());
			break;
			
			case "string":
				if(!nextToken.isString())
					addError("Tipo inv�lido, cadeia constante esperada", currentToken.getLine());
			break;
			
			case "char":
				if(!nextToken.isChar())
					addError("Tipo inv�lido, caractere constante esperado", currentToken.getLine());
			break;
			
		}
	}

	private void checkTypeResolvingObjects (Token assignee, Token currentToken, Token nextToken) {
		
	}
	
	
	
	
	
	public void execute() {
		this.parseProgram();
		/*Token global = new Token(0, "global", TokenType.global);
		scopeTree.put(global, new Scope(global, null));
		pushScope(global);
		
		
		Stack <String> delimiters = new Stack<>();
		
		
		boolean isDeclaringIdentifier = false;
		boolean isDeclaringMethod = false;
		boolean isDeclaringMethodParameters = false;
		boolean isCallingMethod = false;
		boolean isAssigningIdentifier = false;
		boolean isDeclaringConstant = false;
		boolean isDeclaringClass = false;
		Declaration currentDeclaration = null;
		
		while (this.currentTokenIndex < this.tokens.size()) {
			Token currentToken = this.nextToken();
			
			
			System.out.println(currentToken.getRepresentation());
			
			switch (currentToken.getType()) {
			case cadeia_constante:
				break;
			case caractere_constante:
				break;
			case delimitador:
				
				if(currentToken.is ("{"))
					delimiters.push("{");
				
				
				
				if(currentToken.is("}")) {
					delimiters.pop();
					
					if(delimiters.empty()) {
						isDeclaringConstant = false;
						isDeclaringMethod = false;
						isDeclaringClass = false;
					}
				}
				
				break;
			case global:
				break;
			case identificador:
				
				if(isDeclaringConstant) {
					
					
				} else {
					if(this.lookAhead().is("(")  && isDeclaringIdentifier) {
						isDeclaringMethod = true;
					}
				}
				
				break;
			case inteiro:
				
				break;
			case decimal:
				break;
			case operador:
				if(isDeclaringConstant) { // atribui��o na declara��o
					if(currentToken.is("=")) {
						this.checkType(currentDeclaration, currentToken);
					}
				} else { // atribui��o no c�digo
					this.checkTypeResolvingObjects(currentDeclaration, currentToken);					
				}
				break;
			case palavra_reservada:
				
				if(currentToken.is("const")) {
					isDeclaringConstant = true;
				}
				
				if(currentToken.isPrimitiveType()) {
					currentDeclaration = new Declaration(currentToken.getRepresentation(), null);
					isDeclaringIdentifier = true;
				}
				
				break;
			default:
			}
			
			
		}
		*/
		
	}
	
	
}