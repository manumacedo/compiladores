import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeMap;

public class SemanticAnalyzer {
	
	private final boolean ACCESS = true;
	private final boolean DECL = false;
	
	private ArrayList<Token> tokens;
	private Stack<Scope> scopeStack;
	private int currentTokenIndex;
	
	private TreeMap<SemanticUnit, Scope> scopeTree;
	private SemanticUnit declaringUnit;
	private ErrorHandler handler;
	
	private TreeMap<String, ClassUnit> classes;
	private TreeMap<String, SemanticUnit> globals;
	
	
	private ClassUnit declaringClassUnit;
	private ClassUnit currentClass;
	private MethodUnit currentMethod;
	private String currentAssignment;
	
	
	// ------------------------- global tem um map de classes, variaveis e constantes
	// --------------- cada class tem um map de metodos
	// --------- cada metodo tem um map de variaveis
	
	public SemanticAnalyzer (LexIO io) {		
		this.tokens = io.getTokens();
		this.scopeStack = new Stack<>();
		this.currentTokenIndex = 0;
		this.scopeTree = new TreeMap<>();
		
		this.handler = new ErrorHandler();
		
		this.scopeStack.push(new Scope(new Token(-1, "GLOBAL", null), null));
		
		
		this.classes = new TreeMap<>();
		this.globals = new TreeMap<>();
		
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
	
	private String parsePrimitiveType() {
		// <tipo_primitivo> ::= 'int' | 'char' | 'bool' | 'string' | 'float'
		Token next = this.nextToken();
		
		
		System.out.println("Parsing primitive " + next.getRepresentation());
		return next.getRepresentation();
	}
	
	private String parseIdentifier(boolean isAccess){
		Token next = this.nextToken();
		
		
		if(next.isIdentifier()){
			System.out.println("Parsing " + next.getRepresentation() + (isAccess ? "  - access" : " - decl"));
			
			if(isAccess) {
				
			} else {
				//this.scopeStack.peek().insertUnit(new SemanticUnit(next.getRepresentation(), currentDeclarationType, currentCategory));
			}
			return next.getRepresentation();
		}
		else {
			System.out.println("Wow, deu ruim. Expected Identifier, got " + next.getRepresentation());
		}
		return null;
	}
	
	
	private void parseProgram() {
		// <arquivo> ::= <constantes> <variaveis> <pre_main>
		parseConstants();
		parseVariables();
		parsePreMain();
		
		this.handler.output();
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
			
			String type = parsePrimitiveType();
		
			this.declaringUnit = new SemanticUnit(null, type, SemanticCategory.constant, this.scopeStack.peek());
			
			parseConstantList();
		}
		
	}
	
	private void parseConstantList() {
		// <lista_const> ::= Identifier '=' <atribuicao_costante> <aux_declaracao>
		String id = parseIdentifier(DECL);
		this.declaringUnit.setIdentifier(id);
		if(currentClass != null) {
			
			if(globals.containsKey(id) && globals.get(id).getCategory() == SemanticCategory.constant ) {
				handler.add(this.currentToken().getLine(), "Constante global não pode ser sobrescrita - " + id);
			} else if(currentClass.hasOwnAttribute(id)) {
				handler.add(this.currentToken().getLine(), "Identificador já declarado - " + id);
			} else {
				this.declaringUnit.setIdentifier(id);
				currentClass.addAttribute(this.declaringUnit);
				// TODO classes.get(currentClass.getIdentifier())
			}
			
		} else if(this.globals.containsKey(id)) {
			handler.add(this.currentToken().getLine(), "Identificador já declarado - " + id);
		} else {
			//this.scopeStack.peek().insertUnit(declaringUnit);
			globals.put(id, this.declaringUnit);
		}
		
		parseTerminal("=");
		parseConstantAssignment();
		parseConstantListExtension();
	}
	
	private void parseConstantListExtension() {
		// <aux_declaracao> ::= ','<lista_const> | ';' <bloco_constantes>
		
		if(this.currentToken().is(",")) {
			parseTerminal(",");
			
			this.declaringUnit = new SemanticUnit(null, declaringUnit.getType(), SemanticCategory.constant, this.scopeStack.peek());
			
			parseConstantList();
		} else if (this.currentToken().is(";")) {
			this.parseTerminal(";");
			this.parseConstBlock();
		}
	}
	
	private void parseConstantAssignment () {
		// <atribuicao_costante> ::= Numero | Char1 | Cadeia | <boolean>
		
		Token next = this.nextToken();
		if(next.isBool() || next.isChar() || next.isString() || next.isNumber()){
			System.out.println("Parsing primitive assignment " + next.getRepresentation());
		
			if(!next.hasType(declaringUnit.getType())) {
				handler.add(this.currentToken().getLine(), "Tipo inválido na atribuição da constante - " + declaringUnit.getType());
			}
			
		} else 
			System.out.println("Error on assignment, got " + next);
	}
	
	private void parseVariables() {
		// <variaveis> ::= <declaracao_variavel> <variaveis> | <lambda>
		Token curr = this.currentToken();
		if(curr.isPrimitiveType()) {
			
			this.declaringUnit = new SemanticUnit(null, this.currentToken().getRepresentation(), SemanticCategory.variable, this.scopeStack.peek());
			
			parseVariableDeclaration();
			parseVariables();
		}
	}
	
	private boolean isDeclared(String id, Scope currentScope, boolean recursive) {
		if(recursive) {
			if(currentScope.has(id)) {
				return true;
			} else if (currentScope.getParent() != null) {
				return this.isDeclared(id, currentScope.getParent(), recursive);
			} else {
				return false;
			}
			
		} else {
			return currentScope.has(id);
		}
	}
	
	private boolean isDeclaredLocally(String id) {
		return isDeclared(id, this.scopeStack.peek(), false);
	}
	
	private SemanticUnit findIdentifier (String id) {
		SemanticUnit ret;
		
		if(this.currentMethod != null) {
			ret = this.currentMethod.getUnit(id);
			if(ret != null)
				return ret;
		}
		
		if(this.currentClass != null) {
			ret = this.currentClass.getUnit(id);
			if(ret != null)
				return ret;
		}
		
		ret = globals.get(id);
		if(ret != null)
			return ret;
		
		return null;
	}
	
	private boolean isDeclaredAnywhere(String id) {
		
		if(this.currentMethod != null) {
			if(this.currentMethod.hasVariable(id))
				return true;
		}
		
		if (this.currentClass != null) {
			if (this.currentClass.hasOwnAttribute(id)  || this.currentClass.hasInheritedAttribute(id))
				return true;
		}
		
		return this.globals.containsKey(id);
	}
	
	private void parseVariableDeclaration() {
		// <declaracao_variavel> ::= <tipo_primitivo> Identifier <lista_variavel>
		parsePrimitiveType();
		
		String id = parseIdentifier(DECL);
		
		if(globals.containsKey(id)) {
			handler.add(this.currentToken().getLine(), "Identificador já declarado - " + id);
		} else {
			this.declaringUnit.setIdentifier(id);
			this.scopeStack.peek().insertUnit(declaringUnit);
			
			this.globals.put(id, declaringUnit);
		}		

		parseVariableList();
	}
	
	private void parseVariableList(){
		// <lista_variavel> ::= ',' Identifier <lista_variavel> | ';'
		
		if(this.currentToken().is(",")) {
			parseTerminal(",");
			
			String id = parseIdentifier(DECL);
			this.declaringUnit = new SemanticUnit(id, this.declaringUnit.getType(), SemanticCategory.variable, this.scopeStack.peek());
			
			if(classes.containsKey(id)) {
				handler.add(this.currentToken().getLine(), "Variável tem o mesmo identificador de uma classe - " + id);
			}
			
			if(isDeclaredLocally(id)) {
				handler.add(this.currentToken().getLine(), "Identificador já declarado - " + id);
			}
			
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
		this.currentMethod = new MethodUnit("main");
		
		parseTerminals("void", "main", "(", ")", "{");
		parseMethodContents();
		parseTerminal("}");
		
		this.currentMethod = null;
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
		String id = parseIdentifier(DECL);
		
		this.currentClass = new ClassUnit(id);
		
		if(this.classes.containsKey(id) || this.globals.containsKey(id)) {
			handler.add(this.currentToken().getLine(), "Identificador já declarado - " + id);
		} else {
			this.classes.put(id, currentClass);
		}
		
		parseInheritance();
		parseTerminal("{");
		parseClassContents();
		parseTerminal("}");
		this.currentClass = null;
	}
	
	private void parseInheritance() {
		// <expressao_heranca> ::= '>' Identifier | <lambda>
		if(this.currentToken().is(">")){
			parseTerminal (">");
			String id = parseIdentifier(ACCESS);
			
			if(!this.classes.containsKey(id)) {
				handler.add(this.currentToken().getLine(), "Identificador da herança não declarado - " + id);
			} else if (id.equals(currentClass.getIdentifier())) {
				handler.add(this.currentToken().getLine(), "Classe não pode herdar de si mesma");
			} else {
				this.currentClass.inheritFrom(this.classes.get(id));
			}
			
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
			
			String type = parseType();
			String id = parseIdentifier(DECL);
			
			this.declaringUnit = new SemanticUnit(id, type, null, null);
			
			if(classes.containsKey(id)) {
				handler.add(this.currentToken().getLine(), "Variável tem o mesmo identificador de uma classe - " + id);
			}
			
			if(currentClass.hasOwnAttribute(id)) {
				handler.add(this.currentToken().getLine(), "Identificador já declarado - " + id);
			} else if (currentClass.hasInheritedAttribute(id)) {
				handler.add(this.currentToken().getLine(), "Identificador já declarado pela herança - " + id);
			}
			
			parseIdentifierExtension();
		} else if (this.currentToken().is("void")) {
			parseTerminal("void");
			String id = parseIdentifier(DECL);
			
			this.currentMethod = new MethodUnit(id);
			
			if(currentClass.hasOwnMethod(id) || currentClass.hasOwnAttribute(id)) {
				handler.add(this.currentToken().getLine(), "Identificador já declarado - " + id);
			} else if(currentClass.hasInheritedAttribute(id)) {
				handler.add(this.currentToken().getLine(), "Identificador já declarado pela herança - " + id);
			} else if (currentClass.hasInheritedMethod(id)) {
				
				if (!this.currentMethod.equalsTo(this.currentClass.getInheritedMethod(id))) {
					handler.add(this.currentToken().getLine(), "Override inválido, assinaturas dos métodos não conferem");
				}
				
			} else {
				this.currentClass.addMethod(this.currentMethod);
			}
			
			parseTerminal("(");
			parseParametersDeclaration();
			parseTerminals(")", "{");
			parseMethodContents();
			parseTerminal("}");
			this.currentMethod = null;
		}
	}
	
	private void parseIdentifierExtension () {
		// <comp_id> ::=  '['<indice>']' <lista_vetor> | '(' <decl_parametros> ')' '{' <conteudo_metodo> 'return' <retorno> '}' | <lista_variavel>
		
		String declaringIdentifier = this.declaringUnit.getIdentifier();
		
		switch (this.currentToken().getRepresentation()) {
			case "[":
				this.declaringUnit.setCategory(SemanticCategory.vector);
				
				
				this.currentClass.addAttribute(this.declaringUnit);
				
				parseVectorDeclaration();
				parseVectorList();
			break;
				
			case "(":
				
				this.currentMethod = new MethodUnit(declaringIdentifier);
				this.currentMethod.setReturnType(this.declaringUnit.getType());
				parseTerminal("(");
				parseParametersDeclaration();
				
				if(this.currentClass.hasInheritedMethod(declaringIdentifier)) {
					if (!this.currentMethod.equalsTo(this.currentClass.getInheritedMethod(declaringIdentifier))) {
						handler.add(this.currentToken().getLine(), "Override inválido, assinaturas dos métodos não conferem");
					}
				}  else {
					this.currentClass.addMethod(this.currentMethod);
				}
				
				parseTerminals(")", "{");
				parseMethodContents();
				parseTerminal("return");
				parseReturn();
				parseTerminal("}");
				this.currentMethod = null;
			break;
			
			case ",":
			case ";":
				
				if(classes.containsKey(declaringIdentifier)) {
					handler.add(this.currentToken().getLine(), "Variável tem o mesmo identificador de uma classe - " + declaringIdentifier);
				} else if (globals.containsKey(declaringIdentifier) && globals.get(declaringIdentifier).getCategory() == SemanticCategory.constant) {
					handler.add(this.currentToken().getLine(), "Constante global não pode ser sobrescrita - " + declaringIdentifier);
				} else {
					this.currentClass.addAttribute(this.declaringUnit);
				}
				parseVariableList();
			break;
		}
		
	}
	
	private void parseVectorIndex() {
		
		if(this.currentToken().isNumber()) {
			
			if(!currentToken().isInteger()) {
				handler.add(this.currentToken().getLine(), "Tipo inválido para tamanho do vetor - " + currentToken().getRepresentation());
			}
			
			parseTerminal(this.currentToken().getRepresentation());
			
			
			
		} else if (this.currentToken().isIdentifier()) {
			String id = parseIdentifier(ACCESS);
			
			if(!isDeclaredAnywhere(id)) {
				handler.add(this.currentToken().getLine(), "Identificador não declarado - " + id);
			} else if (!this.findIdentifier(id).getType().equals("int")){
				handler.add(this.currentToken().getLine(), "Tipo inválido para tamanho do vetor - " + this.findIdentifier(id).getType());
			}
		}
		
	}
	
	private void parseVectorList() {
		// <lista_vetor>::=',' Identifier '['<indice>']' <lista_vetor>  | ';'
		
		if(this.currentToken().is(",")) {
			parseTerminal(",");
			
			// TODO declaring
			String id = parseIdentifier(DECL);
			boolean isVector = parseVectorDeclaration();
			
			this.declaringUnit = new SemanticUnit(id, this.declaringUnit.getType(), isVector ? SemanticCategory.vector : SemanticCategory.variable, null);
			
			if(this.currentMethod != null) { // im on a method
				if(this.currentMethod.hasVariable(id)) {
					handler.add(this.currentToken().getLine(), "Identificador já declarado - " + id);
				} else {
					this.currentMethod.addVariable(this.declaringUnit);
				}
				
			} else { // im on a class
				this.currentClass.addAttribute(this.declaringUnit);
			}
			
			
			parseVectorList();
		} else if (this.currentToken().is(";")){
			this.parseTerminal(";");
		}
	}
	
	private boolean parseVectorDeclaration() {
		if(this.currentToken().is("[")) {
			parseTerminal("[");
			parseVectorIndex();
			parseTerminal("]");
			
			return true;
		}
		
		return false;
	}
	
	private String parseType() {
		if(this.currentToken().isPrimitiveType()) {
			return parsePrimitiveType();
		} else if (this.currentToken().isIdentifier()) {
			
			String id = parseIdentifier(ACCESS);
			
			if(!classes.containsKey(id)) {
				handler.add(this.currentToken().getLine(), "Identificador não declarado - " + id);
			}
			
			return id;
		}
		
		return null;
	}
	
	private void parseParametersDeclaration () {
		// <decl_parametros> ::= <tipo> Identifier <var_vet> <lista_parametros> | <lambda>
		
		if(this.currentToken().isType()) {
			parseParameter();
		}
	}
	
	private void parseParameter() {
		String type = parseType();
		String id = parseIdentifier(DECL);
		
		boolean isVector = parseVectorDeclaration();
		
		SemanticUnit parameter = new SemanticUnit(id, type, isVector ? SemanticCategory.vector : SemanticCategory.variable, null);
		this.currentMethod.addParameter(parameter);
		
		SemanticUnit globalConst = this.globals.get(parameter.getIdentifier());
		SemanticUnit localConst = this.currentClass.getUnit(parameter.getIdentifier());
		
		
		if ((globalConst != null) || (localConst != null)){
			
			handler.add(this.currentToken().getLine(), "Parâmetros não podem ter o mesmo nome que constantes - " + parameter.getIdentifier());
		}
		
		this.currentMethod.addVariable(parameter);
		
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
		String returnType = parseAssignment();
		if(returnType == null) {
			//handler.add(this.currentToken().getLine(), "Tipo de retorno inválido - " + returnType);
		} else if(!this.currentMethod.getReturnType().equals(returnType)) {
			handler.add(this.currentToken().getLine(), "Tipo de retorno inválido - " + returnType);
		}
		
		parseTerminal(";");
	}
	
	private void parseCommand() {
		
		if(this.currentToken().is("read")) {
			recRead();
		} else if (this.currentToken().is("write")) {
			recWrite();
		} else if (this.currentToken().isType() && this.lookAhead().isIdentifier()) {
			// <tipo> Identifier <id_decl>
			
			String type = parseType();
			String id = parseIdentifier(DECL);

			boolean isVector = parseVectorDeclaration();  // TODO vector list != var list (or is it?)
			
			this.declaringUnit = new SemanticUnit(id, type, isVector ? SemanticCategory.vector : SemanticCategory.variable, null);
			
			if(this.currentMethod.hasVariable(id)) {
				handler.add(this.currentToken().getLine(), "Identificador já declarado - " + id);
			} else if (globals.containsKey(id) && globals.get(id).getCategory() == SemanticCategory.constant) {
				handler.add(this.currentToken().getLine(), "Constante global não pode ser sobrescrita - " + id);
			} else {
				this.currentMethod.addVariable(this.declaringUnit);
			}
			
			parseVectorList();
		} else if (this.currentToken().isIdentifier()) {
			// Identifier <id_comando>
			
			String id = parseIdentifier(ACCESS);
			this.currentAssignment = id;
			parseCommandExtension();
		} else if (this.currentToken().is("if")) {
			recIf();
			
		} else if (this.currentToken().is("while")) {
			
			recWhile();
		}
		
	}

	private void parseCommandExtension() {
		//<id_comando> ::= '(' <parametros> ')' ';' | '.' Identifier <acesso_objeto> ';' | '=' <atribuicao> ';' | '[' <indice> ']' '=' <atribuicao> ';'
		
		SemanticUnit unit = this.findIdentifier(this.currentAssignment);
		if(unit == null) {
			handler.add(this.currentToken().getLine(), "Identificador não declarado - " + this.currentAssignment);
		}
		
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
				if(unit != null && unit.getCategory() == SemanticCategory.constant) {
					handler.add(this.currentToken().getLine(), "Constante não pode ser atribuída - " + unit.getIdentifier());
				}
				parseTerminal("=");
				String type = parseAssignment();
				
				if (unit != null && (unit.getCategory() != SemanticCategory.constant) && !unit.getType().equals(type)) {
					handler.add(this.currentToken().getLine(), "Tipo inválido na atribuição - " + type);
				}
				
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
	
	//  (((( ++a )))) + 
	
	private String parseAssignment() {
		switch (this.currentToken().getRepresentation()) {
        case "(":
            parseTerminal("(");
            this.currentAssignment = parseAssignment();
            parseTerminal(")");
            parseOperator();
            return this.currentAssignment;
        case "++":
        case "--":
            return parseIdentifierAccess();
        case "true":
        case "false":
            recOpLogico();
            return "bool";
        case "-":
            parseTerminal("-");
            return recNegativo();
        default:
            if(this.currentToken().isIdentifier()) {
            	return parseIdentifierAccess();
            } else if (this.currentToken().isNumber()) {
            	
            	if(this.currentToken().isFloat())
            		this.currentAssignment = "float";
            	else
            		this.currentAssignment = "int";
            	
            	parseTerminal(this.currentToken().getRepresentation());            	
            	recOperadorNumero();
            	return this.currentAssignment;
            } else if (this.currentToken().isChar()) {
            	parseTerminal(this.currentToken().getRepresentation());
            	return "char";
			} else if ( this.currentToken().isString()) {
				parseTerminal(this.currentToken().getRepresentation());
            	parseTerminal(this.currentToken().getRepresentation());
            	return "string";
			}
        }
		return null;
    }
	
	private String recNegativo() {
		String ret;
        switch (this.currentToken().getRepresentation()) {
            case "(":
                parseTerminal("(");
                ret = recNegativo();
                parseTerminal(")");
                return ret;
            case "++":
            case "--":
                return parseIdentifierAccess();
            default:
            	if(this.currentToken().isNumber()){
                    parseTerminal(this.currentToken().getRepresentation());
                    return recOperadorNumero();
            	} else if (this.currentToken().isIdentifier())  {
            		return parseIdentifierAccess();
            	}
        }
        
        return null;
    }
	
	private String recOperadorNumero() {
        switch (this.currentToken().getRepresentation()) {
            case ">":
            case "<":
            case ">=":
            case "<=":
                parseTerminal(this.currentToken().getRepresentation());
                recExpAritmetica();
                recOpLogico();
                return "bool";
            case "+":
            case "-":
            case "*":
            case "/":
            	parseTerminal(this.currentToken().getRepresentation());
                String ret = recExpAritmetica();
                recExpRelacionalOpcional();
                return ret;
            case "==":
            case "!=":
            	parseTerminal(this.currentToken().getRepresentation());
                recExpAritmetica();
                recOpLogico();
                return "bool";
            default:
                break;
        }
        
        return null;
    }
	
	private void parseParameters() {
		if(this.currentToken().isAssignable()) {
			parseAssignment();
		}
	}
	
	private String recExpRelacionalOpcional() {
        switch (this.currentToken().getRepresentation()) {
            case ">":
            case "<":
            case ">=":
            case "<=":
            	parseTerminal(this.currentToken().getRepresentation());
                recExpAritmetica();
                recOpLogico();
                return "bool";
            default:
                return null;
        }
    }
	
	private String parseOperator() { 
		if(this.currentToken().isRelationalOperator()) {
			// <operador_relacional> <exp_aritmetica><op_logico>
			recOpRelacional();
			recExpAritmetica();
			recOpLogico();
			return "bool";
		} else if (this.currentToken().isArithmeticOperator()) {
			//<operador_aritmetico><exp_aritmetica><exp_relacional_opcional>
			parseArithmeticOperator();
			recExpAritmetica();
			return recExpRelacionalOpcional();
		} else if (this.currentToken().isEqualityOperator()) {
			// <operador_igualdade><atribuicao>
			parseEqualityOperator();
			return parseAssignment();
		} else if (this.currentToken().isLogicalOperator()) {
			// <operador_logico><exp>
			recOpLogico();
			recExp();
			return "bool";
		}
		
		return null;
		
	}
	
	private void parseEqualityOperator() {
		parseTerminal(this.currentToken().getRepresentation());
	}

	private void parseArithmeticOperator() {
		parseTerminal(this.currentToken().getRepresentation());
	}

	private void recOpLogico() {
        switch (this.currentToken().getRepresentation()) {
            case "==":
                parseTerminal("==");
                recExpLogica();
                break;
            case "!=":
            	parseTerminal("!=");
                recExpLogica();
                break;
            case "&&":
            	parseTerminal("&&");
                recExp();
                break;
            case "||":
            	parseTerminal("||");
                recExp();
                break;
            default:
                break;
        }
    }
	
	private String parseIdentifierAccess() {
		String id;
		
		switch (this.currentToken().getRepresentation()) {
            case "++":
            case "--":
                parseTerminal(this.currentToken().getRepresentation());
                id = parseIdentifier(ACCESS);
                if(!this.currentMethod.hasVariable(id)) {
                	handler.add(this.currentToken().getLine(), "Identificador não declarado - " + id);
                	return null;
                }
                
                if(!this.currentMethod.getUnit(id).getType().equals("int")) {
                	handler.add(this.currentToken().getLine(), "Incremento inválido, variável não é um inteiro - " + id);
                }
                
                return recOperacao();
            default:
                if(this.currentToken().isIdentifier()) {
                	id = parseIdentifier(ACCESS);
                    recAcesso();
                    recOperacao();
                    
                    SemanticUnit unit = findIdentifier(id);
                    if(unit != null)
                    	return unit.getType();
                    else
                    	handler.add(this.currentToken().getLine(), "Identificador não declarado - " + id);
                }
        }
		
		return null;
    }

    private void recAcesso() {
        switch (this.currentToken().getRepresentation()) {
            case "[":
                parseTerminal("[");
                parseVectorIndex();
                parseTerminal("]");
                break;
            case "(":
                parseTerminal("(");
                recParametros();
                parseTerminal(")");
                break;
            case "++":
                parseTerminal("++");
                break;
            case "--":
                parseTerminal("--");
                break;
            case ".":
                parseTerminal(".");
                parseIdentifier(ACCESS);
                recChamadaMetodo();
                break;
            default:
                break;
        }
    }

    private String recOperacao() {
        switch (this.currentToken().getRepresentation()) {
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "==":
            case "!=":
            case "&&":
            case "||":
            	parseOperator();
            	return "bool";
            case "+":
            case "-":
            case "*":
            case "/":
            	return parseOperator();
        }
        
        return null;
    }

    private void recChamadaMetodo() {
        switch (this.currentToken().getRepresentation()) {
            case "(":
                parseTerminal("(");
                recParametros();
                parseTerminal(")");
                break;
            default:
                break;

        }
    }

    private void recParametros() {
        switch (this.currentToken().getRepresentation()) {
            case "(":
                parseAssignment();
                recNovoParametro();
                break;
            case "true":
                parseAssignment();
                recNovoParametro();
                break;
            case "false":
                parseAssignment();
                recNovoParametro();
                break;
            case "-":
                parseAssignment();
                recNovoParametro();
                break;
            case "++":
                parseAssignment();
                recNovoParametro();
                break;
            case "--":
                parseAssignment();
                recNovoParametro();
                break;
            default:
                switch (this.currentToken().getType()) {
                    case identificador:
                    case inteiro:
                    case decimal:
                    case cadeia_constante:
                    case caractere_constante:
                        parseAssignment();
                        recNovoParametro();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private void recNovoParametro() {
        switch (this.currentToken().getRepresentation()) {
            case ",":
                parseTerminal(",");
                recParametros();
                break;
            default:
                break;
        }
    }

    private void recInicializaObjeto() {
        parseTerminal("new");
        String id = parseIdentifier(ACCESS);
        if(!isDeclaredAnywhere(id)) {
        	handler.add(this.currentToken().getLine(), "Identificador não declarado - " + id);
        }
        
        parseTerminal(";");
    }

    private void recWhile() {
        parseTerminal("while");
        parseTerminal("(");
        recExp();
        parseTerminal(")");
        parseTerminal("{");
        recConteudoEstrutura();
        parseTerminal("}");
    }

    private void recRead() {
        parseTerminal("read");
        parseTerminal("(");
        parseIdentifier(ACCESS);
        recListaRead();
        parseTerminal(")");
        parseTerminal(";");
    }

    private void recListaRead() {
        switch (this.currentToken().getRepresentation()) {
            case ",":
                parseTerminal(",");
                parseIdentifier(ACCESS);
                recListaRead();
                break;
            default:
                break;
        }
    }

    private void recWrite() {
        parseTerminal("write");
        parseTerminal("(");
        recParametrosWrite();
        parseTerminal(")");
        parseTerminal(";");
    }

    private void recParametrosWrite() {
        recImprimiveis();
        recNovoParametroWrite();
    }

    private void recNovoParametroWrite() {

        switch (this.currentToken().getRepresentation()) {
            case ",":
                parseTerminal(",");
                recParametrosWrite();
                break;
            default:
                break;
        }
    }

    private void recImprimiveis() {
        switch (this.currentToken().getType()) {
            case identificador:
                parseIdentifier(ACCESS);
                recOpWrite();
                break;
                
            case decimal:
            case inteiro:
            case cadeia_constante:
            case caractere_constante:
                parseTerminal(this.currentToken().getRepresentation());
                recOpWrite();
                break;
            default:
                switch (this.currentToken().getRepresentation()) {
                    case "(":
                        parseTerminal("(");
                        recImprimiveis();
                        parseTerminal(")");
                        break;
                    default:
                        // erroSintatico("falta identificador, numero, cadeia constante, caracter consatante ou (");
                        break;
                }
        }
    }

    private void recOpWrite() {
        switch (this.currentToken().getRepresentation()) {
            case "+":
                parseTerminal("+");
                recExpAritmetica();
                break;
            case "-":
                parseTerminal("-");
                recExpAritmetica();
                break;
            case "*":
                parseTerminal("*");
                recExpAritmetica();
                break;
            case "/":
                parseTerminal("");
                recExpAritmetica();
                break;
            default:
                break;
        }
    }

    private void recIf() {
        parseTerminal("if");
        parseTerminal("(");
        String type = recExp();
        
        if(type != "bool") {
        	handler.add(this.currentToken().getLine(), "Expressão do if deve ser booleana - " + type);
        }
        parseTerminal(")");
        parseTerminal("{");
        recConteudoEstrutura();
        parseTerminal("}");
        recComplementoIf();
    }

    private void recComplementoIf() {
        switch (this.currentToken().getRepresentation()) {
            case "else":
                parseTerminal("else");
                parseTerminal("{");
                recConteudoEstrutura();
                parseTerminal("}");
                break;
            default:
                break;
        }
    }

    private void recConteudoEstrutura() {
    	if(Token.isKeyword(currentToken()) || currentToken().isIdentifier()) {
    		recComandoEstrutura();
            recConteudoEstrutura();
    	}
    }

    private void recComandoEstrutura() {
        switch (this.currentToken().getRepresentation()) {
            case "read":
                recRead();
                break;
            case "write":
                recWrite();
                break;
            case "new":
                recInicializaObjeto();
                recInicializaObjeto();
                break;
            case "if":
                recIf();
                break;
            case "while":
                recWhile();
                break;
            default:
                if (this.currentToken().isIdentifier()) {
                    String id = parseIdentifier(ACCESS);
                    this.currentAssignment = id;
                    parseCommandExtension();

                }
                break;

        }
    }

    private String recExp() {
    	
    	String type = null;
    	String id = null;
    	
        switch (this.currentToken().getRepresentation()) {
            case "true":
                parseTerminal("true");
                
                return recComplementoLogico();
            case "false":
                parseTerminal("false");
                return recComplementoLogico();
            case "++":
                parseTerminal("++");
                id = parseIdentifier(ACCESS);
                type = recIdExp(id);
                recComplementoAritmetico1(type);
                break;
            case "--":
                parseTerminal("--");
                id = parseIdentifier(ACCESS);
                type = recIdExp(id);
                recComplementoAritmetico1(type);
                break;
            case "(":
                parseTerminal("(");
                type = recExp();
                parseTerminal(")");
                break;
            default:
                switch (this.currentToken().getType()) {
                    case identificador:
                        id = parseIdentifier(ACCESS);
                        
                        if(findIdentifier(id) != null) {
                        	type = findIdentifier(id).getType();
                        } else {
                        	handler.add(this.currentToken().getLine(), "Identificador não declarado - " + id);
                        }
                        
                        recIdExpArit(id);
                        String rep = recComplementoAritmetico1(type);
                        if(rep != null)
                        	type = rep;
                        
                        break;
                    case inteiro:
                    case decimal:
                        parseTerminal(this.currentToken().getRepresentation());
                        recComplementoAritmetico();
                        type = recOpRelacional();
                    default:
                        break;
                }

        }
        
        return type;
    }

    private String recComplementoAritmetico1(String factorType) {
    	String type = null;
    	
        switch (this.currentToken().getRepresentation()) {
            case "+":
            case "-":
            case "*":
            case "/":
                parseTerminal(this.currentToken().getRepresentation());
                type = recFatorAritmetico();
                
                if (type != null && !type.equals(factorType)) {
                	handler.add(this.currentToken().getLine(), "Não é possível operar com tipos diferentes");
                }
                	
                
                String rep = recOpIdRelacional();
      
                if(rep != null)
                	type = rep;
                
                break;
            default:
            	String ret = recOpIdLogico();
            	if(factorType != null && !factorType.equals(ret))
            		handler.add(this.currentToken().getLine(), "Operação não permitida com tipos diferentes - " + factorType);
            		
                return ret;
        }
        
        return type;
    }

    private String recExpLogica() {
    	
    	String type = null;
    	String id;
    	 
        switch (this.currentToken().getRepresentation()) {
            case "true":
                parseTerminal("true");
                return recComplementoLogico();
            case "false":
                parseTerminal("false");
                return recComplementoLogico();
            case "++":
                parseTerminal("++");
                
                id = parseIdentifier(ACCESS);
                if(findIdentifier(id) == null) {
                	handler.add(this.currentToken().getLine(), "Identificador não declarado - " + id);
                } else if (findIdentifier(id).getCategory() == SemanticCategory.constant) {
                	handler.add(this.currentToken().getLine(), "Constantes não podem ser incrementadas - " + id);
                } else if (findIdentifier(id).getCategory() == SemanticCategory.vector) {
                	handler.add(this.currentToken().getLine(), "Vetores não podem ser incrementados - " + id);
                } else if (!findIdentifier(id).getType().equals("int")) {
                	handler.add(this.currentToken().getLine(), "Somente inteiros podem ser incrementados - " + id);
                }
                
                type = recIdExp(id);
                recComplementoAritmetico();
                recOpIdLogico();
                break;
            case "--":
                parseTerminal("--");
                id = parseIdentifier(ACCESS);
                
                if(findIdentifier(id) == null) {
                	handler.add(this.currentToken().getLine(), "Identificador não declarado - " + id);
                } else if (findIdentifier(id).getCategory() == SemanticCategory.constant) {
                	handler.add(this.currentToken().getLine(), "Constantes não podem ser incrementadas - " + id);
                } else if (findIdentifier(id).getCategory() == SemanticCategory.vector) {
                	handler.add(this.currentToken().getLine(), "Vetores não podem ser incrementados - " + id);
                } else if (!findIdentifier(id).getType().equals("int")) {
                	handler.add(this.currentToken().getLine(), "Somente inteiros podem ser incrementados - " + id);
                }
                
                
                type = recIdExp(id);
                recComplementoAritmetico();
                recOpIdLogico();
                break;
            case "(":
                parseTerminal("(");
                recExpLogica();
                parseTerminal(")");
                recComplementoExpLogica();
                break;
            default:
                switch (this.currentToken().getType()) {
                    case identificador:
                        id = parseIdentifier(ACCESS);
                        type = recIdExpArit(id);
                        if(!type.equals(recComplementoAritmetico())) {
                        	handler.add(this.currentToken().getLine(), "Tipos inválidos na expressão - " + type);
                        }
                        recOpIdLogico();
                        break;
                    case decimal:
                    case inteiro:
                        parseTerminal(this.currentToken().getRepresentation());
                        recComplementoAritmetico();
                        recCoOpRelacional();
                        break;
                    default:
                        break;
                }

        }
        
        return type;
    }

    private void recCoOpRelacional() {
        switch (this.currentToken().getRepresentation()) {
            case ">":
                recOpRelacional();
                break;
            case "<":
                recOpRelacional();
                break;
            case ">=":
                recOpRelacional();
                break;
            case "<=":
                recOpRelacional();
                break;
            case "==":
                recOpRelacional();
                break;
            case "!=":
                recOpRelacional();
                break;
            case "&&":
                parseTerminal("&&");
                recExp();
                break;
            case "||":
                parseTerminal("||");
                recExp();
                break;
            default:
                break;
        }
    }

    private void recComplementoExpLogica() {
        switch (this.currentToken().getRepresentation()) {
            case ">":
                parseTerminal(">");
                recFatorAritmetico();
                recComplementoLogico();
                break;
            case "<":
                parseTerminal("<");
                recFatorAritmetico();
                recComplementoLogico();
                break;
            case ">=":
                parseTerminal(">=");
                recFatorAritmetico();
                recComplementoLogico();
                break;
            case "<=":
                parseTerminal("<=");
                recFatorAritmetico();
                recComplementoLogico();
                break;
            case "==":
                parseTerminal("==");
                recExpLogica();
                break;
            case "!=":
                parseTerminal("!=");
                recExpLogica();
                break;
            case "&&":
                parseTerminal("&&");
                recExp();
                break;
            case "||":
                parseTerminal("||");
                recExp();
                break;
            case "+":
                parseTerminal("+");
                recFatorAritmetico();
                recComplementoLogico();
                break;
            case "-":
                parseTerminal("-");
                recFatorAritmetico();
                recComplementoLogico();
                break;
            case "*":
                parseTerminal("*");
                recFatorAritmetico();
                recComplementoLogico();
                break;
            case "/":
                parseTerminal("/");
                recFatorAritmetico();
                recComplementoLogico();
                break;
            default:
                break;
        }
    }

    private String recIdExp(String id) {
    	String ret;

        switch (this.currentToken().getRepresentation()) {
        	case "(":
                parseTerminal("(");
                recParametros();
                parseTerminal(")");
            case ".":
                parseTerminal(".");                
                String attr = parseIdentifier(ACCESS);
                recChamadaMetodo();
                return this.currentClass.getUnit(attr).getType();
            case "[":
                parseTerminal("[");
                parseVectorIndex();
                parseTerminal("]");
                return "vector";
            default:
                break;
        }
        
        return findIdentifier(id).getType();
    }

    private String recOpIdLogico() {
    	
    	String type;
    	
        switch (this.currentToken().getRepresentation()) {
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "==":
            case "!=":
                return recOpIdRelacional();
            case "&&":
                parseTerminal("&&");
                return recExp();
            case "||":
                parseTerminal("||");
                return recExp();
            default:
            	return null;
        }
    }

    private String recComplementoLogico() {
        switch (this.currentToken().getRepresentation()) {
            case "==":
                parseTerminal("==");
                recExpLogica();
                break;
            case "!=":
                parseTerminal("!=");
                recExpLogica();
                break;
            case "&&":
                parseTerminal("&&");
                recExp();
                break;
            case "||":
                parseTerminal("||");
                recExp();
                break;
            default:
                break;
        }
        return "bool";
    }

    private String recOpRelacional() {
    	
    	String type = null;
    	
        switch (this.currentToken().getRepresentation()) {
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "==":
            case "!=":
                parseTerminal(this.currentToken().getRepresentation());
                type = recExpAritmetica();
                recOpLogico();
                break;
            default:
                // erroSintatico("falta operador: >, <, >=, <=, ==, !=");
                break;
        }
        
        return type;
    }

    private String recOpIdRelacional() {
    	
    	String type = null;
    	
        switch (this.currentToken().getRepresentation()) {
            case ">":
            case "<":
            case ">=":
            case "<=":
                parseTerminal(this.currentToken().getRepresentation());
                type = recExpAritmetica();
                recOpLogico();
                break;
            case "==":
            case "!=":
                parseTerminal(this.currentToken().getRepresentation());
                type = recExpLogica();
                break;
            default:
                // erroSintatico("falta operador: >, <, >=, <=, ==, !=");
                break;
        }
        
        return type;
    }

    private String recExpAritmetica() {
        switch (this.currentToken().getRepresentation()) {
            case "++":
            case "--":
            case "(":
                return recFatorAritmetico();
            case "-":
                parseTerminal("-");
                return recExpAritmetica();
            default:
                if (this.currentToken().isIdentifier() || this.currentToken().isNumber()) {
                    return recFatorAritmetico();
                }
        }
        return null;
    }

    private String recFatorAritmetico() {
    	String type = null;
    	
        switch (this.currentToken().getRepresentation()) {
            case "++":
            case "--":
                recIdAritmetico();
                return recComplementoAritmetico();
            case "-":
                parseTerminal("-");
                return recExpAritmetica();
            case "(":
                recFatorAritmetico();
                return recComplementoAritmetico();
            default:
                if (this.currentToken().isIdentifier()) {
                    type = recIdAritmetico();
                    
                    String rep = recComplementoAritmetico();
                    
                    if (rep != null)
                    	type = rep;
                    
                } else if (this.currentToken().isNumber()) {
                    
                	if(this.currentToken().isInteger())
                		type = "int";
                	else if (this.currentToken().isFloat())
                		type = "float";
                	
                	parseTerminal(this.currentToken().getRepresentation());
                    String rep = recComplementoAritmetico();
                    if (rep != null)
                    	type = rep;
                }
                break;
        }
        
        return type;
    }

    private String recIdAritmetico() {
        
    	String id;
    	String type = null;
    	
    	switch (this.currentToken().getRepresentation()) {
            case "++":
            case "--":
                parseTerminal(this.currentToken().getRepresentation());
                
                id = parseIdentifier(ACCESS);
                
                if(!currentMethod.getUnit(id).getType().equals("int")) {
                	handler.add(this.currentToken().getLine(), "Incremento inválido, variável não é um inteiro - " + id);                	
                }
                return this.currentMethod.getUnit(id).getType();
            default:
                if (this.currentToken().isIdentifier()) {
                    id = parseIdentifier(ACCESS);
                    
                    if(findIdentifier(id) !=  null) {
                    	type = findIdentifier(id).getType();
                    } else {
                    	handler.add(this.currentToken().getLine(), "Identificador não declarado - " + id);
                    }
                    
                    recIdExpArit(id);
                }
        }
    	
    	return type;
    }

    private String recIdExpArit(String id) {
    	
    	String type = null;
    	
        switch (this.currentToken().getRepresentation()) {
            case "(":
            	if(!this.currentClass.hasOwnMethod(id) &&
            	   !this.currentClass.hasInheritedMethod(id)) {
            		handler.add(this.currentToken().getLine(), "Método não definido");
            	}

                type = recIdExp(id);
                break;
            case ".":
            	
            	if(!this.currentClass.hasOwnAttribute(id) &&
             	   !this.currentClass.hasInheritedAttribute(id)) {
             			handler.add(this.currentToken().getLine(), "Atributo não definido");
             	}
            
                type = recIdExp(id);
                break;
            case "[":
            	
            	if(!this.currentMethod.hasVariable(id) &&
            	   !(this.currentMethod.getUnit(id).getCategory() == SemanticCategory.vector)) {
            		
            		handler.add(this.currentToken().getLine(), "O identificador acessado não é um vetor");
              	}
            	
                recIdExp(id);
                type = findIdentifier(id).getType();
                break;
            case "++":
            case "--":
                parseTerminal(this.currentToken().getRepresentation());
                type = findIdentifier(id).getType();
                break;
            default:
                break;
        }
        
        return type;
    }

    private String recComplementoAritmetico() {
        switch (this.currentToken().getRepresentation()) {
            case "+":
            case "-":
            case "*":
            case "/":
                parseTerminal(this.currentToken().getRepresentation());
                return recFatorAritmetico();
            default:
                break;
        }
        
        return null;
    }

	private void addError (String error, int line) {
		System.out.println("Erro na linha " + line + " " + error);
	}
	
	private void checkType (String declarationType, Token currentToken, Token nextToken) {
		switch (declarationType) {
			case "int":
				if(!nextToken.isInteger())
					addError("Tipo inválido, inteiro esperado", currentToken.getLine());
			break;
			
			case "float":
				if(!nextToken.isFloat())
					addError("Tipo inválido, decimal esperado", currentToken.getLine());
			break;
			
			case "string":
				if(!nextToken.isString())
					addError("Tipo inválido, cadeia constante esperada", currentToken.getLine());
			break;
			
			case "char":
				if(!nextToken.isChar())
					addError("Tipo inválido, caractere constante esperado", currentToken.getLine());
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
				if(isDeclaringConstant) { // atribuição na declaração
					if(currentToken.is("=")) {
						this.checkType(currentDeclaration, currentToken);
					}
				} else { // atribuição no código
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
