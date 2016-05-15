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
		
		if(isDeclaredLocally(id)) {
			handler.add(this.currentToken().getLine(), "Constante já declarada");
		} else {
			this.declaringUnit.setIdentifier(id);
			this.scopeStack.peek().insertUnit(declaringUnit);
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
				handler.add(this.currentToken().getLine(), "Tipo inválido na atribuição da constante");
			}
			
		} else 
			System.out.println("Error on assignment, got " + next);
	}
	
	private void parseVariables() {
		// <variaveis> ::= <declaracao_variavel> <variaveis> | <lambda>
		
		if(this.currentToken().isPrimitiveType()) {
			
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
	
	private boolean isDeclaredAnywhere(String id) {
		return isDeclared(id, this.scopeStack.peek(), true);
	}
	
	private void parseVariableDeclaration() {
		// <declaracao_variavel> ::= <tipo_primitivo> Identifier <lista_variavel>
		parsePrimitiveType();
		
		String id = parseIdentifier(DECL);
		
		if(isDeclaredLocally(id)) {
			handler.add(this.currentToken().getLine(), "Variável já declarada");
		} else {
			this.declaringUnit.setIdentifier(id);
			this.scopeStack.peek().insertUnit(declaringUnit);
		}		

		parseVariableList();
	}
	
	private void parseVariableList(){
		// <lista_variavel> ::= ',' Identifier <lista_variavel> | ';'
		
		if(this.currentToken().is(",")) {
			parseTerminal(",");
			
			String id = parseIdentifier(DECL);
			this.declaringUnit = new SemanticUnit(id, this.declaringUnit.getType(), SemanticCategory.variable, this.scopeStack.peek());
			
			if(isDeclaredLocally(id)) {
				handler.add(this.currentToken().getLine(), "Erro, variável já declarada");
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
		String id = parseIdentifier(DECL);
		
		
		
		this.declaringClassUnit = new ClassUnit(id);
		if(isDeclaredLocally(id)) {
			handler.add(this.currentToken().getLine(), "Não foi possível declarar classe - Identificador já declarado");
		}
		
		
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
			parseTerminal(this.currentToken().getRepresentation());
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
			recRead();
		} else if (this.currentToken().is("write")) {
			recWrite();
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
			
			recIf();
			
		} else if (this.currentToken().is("while")) {
			
			recWhile();
			
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
            recOpLogico();
            break;
        case "-":
            parseTerminal("-");
            recNegativo();
            break;
        default:
            if(this.currentToken().isIdentifier()) {
            	parseIdentifierAccess();
            } else if (this.currentToken().isNumber()) {
            	parseTerminal(this.currentToken().getRepresentation());
            	recOperadorNumero();
            } else if (this.currentToken().isChar() || this.currentToken().isString()) {
            	parseTerminal(this.currentToken().getRepresentation());
            }
        }
    }
	
	private void recNegativo() {
        switch (this.currentToken().getRepresentation()) {
            case "(":
                parseTerminal("(");
                recNegativo();
                parseTerminal(")");
                break;
            case "++":
            case "--":
                parseIdentifierAccess();
                break;
            default:
            	if(this.currentToken().isNumber()){
                    parseTerminal(this.currentToken().getRepresentation());
                    recOperadorNumero();
            	} else if (this.currentToken().isIdentifier())  {
            		parseIdentifierAccess();
            	}
        }
    }
	
	private void recOperadorNumero() {
        switch (this.currentToken().getRepresentation()) {
            case ">":
            case "<":
            case ">=":
            case "<=":
                parseTerminal(this.currentToken().getRepresentation());
                recExpAritmetica();
                recOpLogico();
                break;
            case "+":
            case "-":
            case "*":
            case "/":
            	parseTerminal(this.currentToken().getRepresentation());
                recExpAritmetica();
                recExpRelacionalOpcional();
                break;
            case "==":
            case "!=":
            	parseTerminal(this.currentToken().getRepresentation());
                recExpAritmetica();
                recOpLogico();
                break;
            default:
                break;
        }
    }
	
	private void parseParameters() {
		if(this.currentToken().isAssignable()) {
			parseAssignment();
		}
	}
	
	private void recExpRelacionalOpcional() {
        switch (this.currentToken().getRepresentation()) {
            case ">":
            case "<":
            case ">=":
            case "<=":
            	parseTerminal(this.currentToken().getRepresentation());
                recExpAritmetica();
                recOpLogico();
                break;
            default:
                break;
        }
    }
	
	private void parseOperator() { 
		if(this.currentToken().isRelationalOperator()) {
			// <operador_relacional> <exp_aritmetica><op_logico>
			recOpRelacional();
			recExpAritmetica();
			recOpLogico();
		} else if (this.currentToken().isArithmeticOperator()) {
			//<operador_aritmetico><exp_aritmetica><exp_relacional_opcional>
			parseArithmeticOperator();
			recExpAritmetica();
			
		} else if (this.currentToken().isEqualityOperator()) {
			// <operador_igualdade><atribuicao>
			parseEqualityOperator();
			parseAssignment();
		} else if (this.currentToken().isLogicalOperator()) {
			// <operador_logico><exp>
			recOpLogico();
			recExp();
		}
		
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
	
	private void parseIdentifierAccess() {
        switch (this.currentToken().getRepresentation()) {
            case "++":
                parseTerminal("++");
                parseIdentifier(ACCESS);
                recOperacao();
                break;
            case "--":
                parseTerminal("--");
                parseIdentifier(ACCESS);
                recOperacao();
                break;
            default:
                if(this.currentToken().isIdentifier()) {
                	parseIdentifier(ACCESS);
                    recAcesso();
                    recOperacao();
                }
        }
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

    private void recOperacao() {
        switch (this.currentToken().getRepresentation()) {
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "+":
            case "-":
            case "*":
            case "/":
            case "==":
            case "!=":
            case "&&":
            case "||":
                parseOperator();
                break;
        }
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
        parseIdentifier(ACCESS);
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
        recExp();
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
            case "char":
            case "int":
            case "bool":
            case "string":
            case "float":
                parseTerminal(this.currentToken().getRepresentation());
                parseIdentifier(ACCESS);
                parseDeclaration();
                break;
            default:
                if (this.currentToken().isIdentifier()) {
                    parseIdentifier(ACCESS);
                    parseCommandExtension();

                } else {
                    // erroSintatico("falta um comando: identificador ou palavra reservada");
                }
                break;

        }
    }

    private void recExp() {
        switch (this.currentToken().getRepresentation()) {
            case "true":
                parseTerminal("true");
                recComplementoLogico();
                break;
            case "false":
                parseTerminal("false");
                recComplementoLogico();
                break;
            case "++":
                parseTerminal("++");
                parseIdentifier(ACCESS);
                recIdExp();
                recComplementoAritmetico1();
                break;
            case "--":
                parseTerminal("--");
                parseIdentifier(ACCESS);
                recIdExp();
                recComplementoAritmetico1();
                break;
            case "(":
                parseTerminal("(");
                recExp();
                parseTerminal(")");
                break;
            default:
                switch (this.currentToken().getType()) {
                    case identificador:
                        parseIdentifier(ACCESS);
                        recIdExpArit();
                        recComplementoAritmetico1();
                        break;
                    case inteiro:
                    case decimal:
                        parseTerminal(this.currentToken().getRepresentation());
                        recComplementoAritmetico();
                        recOpRelacional();
                        break;
                    default:
//                        // erroSintatico("falta identificador, numero, boolean, (, ou operador: ++, --");
//                        while(!proximo.getTipo().equals("palavra_reservada") && !this.currentToken().getRepresentation().equals(")") && !this.currentToken().getRepresentation().equals("{") && !this.currentToken().getRepresentation().equals("}")){
//                            proximo=proximo();
//                        }
                        break;
                }

        }
    }

    private void recComplementoAritmetico1() {
        switch (this.currentToken().getRepresentation()) {
            case "+":
                parseTerminal("+");
                recFatorAritmetico();
                recOpIdRelacional();
                break;
            case "-":
                parseTerminal("-");
                recFatorAritmetico();
                recOpIdRelacional();
                break;
            case "*":
                parseTerminal("*");
                recFatorAritmetico();
                recOpIdRelacional();
                break;
            case "/":
                parseTerminal("/");
                recFatorAritmetico();
                recOpIdRelacional();
                break;
            default:
                recOpIdLogico();
                break;

        }
    }

    private void recExpLogica() {
        switch (this.currentToken().getRepresentation()) {
            case "true":
                parseTerminal("true");
                recComplementoLogico();
                break;
            case "false":
                parseTerminal("false");
                recComplementoLogico();
                break;
            case "++":
                parseTerminal("++");
                parseIdentifier(ACCESS);
                recIdExp();
                recComplementoAritmetico();
                recOpIdLogico();
                break;
            case "--":
                parseTerminal("--");
                parseIdentifier(ACCESS);
                recIdExp();
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
                        parseIdentifier(ACCESS);
                        recIdExpArit();
                        recComplementoAritmetico();
                        recOpIdLogico();
                        break;
                    case decimal:
                    case inteiro:
                        parseTerminal(this.currentToken().getRepresentation());
                        recComplementoAritmetico();
                        recCoOpRelacional();
                        break;
                    default:
//                        // erroSintatico("falta identificador, numero, boolean, (, ou operador: ++, --");
//                        while(!proximo.getTipo().equals("palavra_reservada") && !this.currentToken().getRepresentation().equals(")") && !this.currentToken().getRepresentation().equals("{") && !this.currentToken().getRepresentation().equals("}")){
//                            proximo=proximo();
//                        }
                        break;
                }

        }
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

    private void recIdExp() {
        switch (this.currentToken().getRepresentation()) {
            case "(":
                parseTerminal("(");
                recParametros();
                parseTerminal(")");
                break;
            case ".":
                parseTerminal(".");
                parseIdentifier(ACCESS);
                recChamadaMetodo();
                break;
            case "[":
                parseTerminal("[");
                parseVectorIndex();
                parseTerminal("]");
                break;
            default:
                break;
        }
    }

    private void recOpIdLogico() {
        switch (this.currentToken().getRepresentation()) {
            case ">":
                recOpIdRelacional();
                break;
            case "<":
                recOpIdRelacional();
                break;
            case ">=":
                recOpIdRelacional();
                break;
            case "<=":
                recOpIdRelacional();
                break;
            case "==":
                recOpIdRelacional();
                break;
            case "!=":
                recOpIdRelacional();
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

    private void recComplementoLogico() {
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

    private void recOpRelacional() {
        switch (this.currentToken().getRepresentation()) {
            case ">":
                parseTerminal(">");
                recExpAritmetica();
                recOpLogico();
                break;
            case "<":
                parseTerminal("<");
                recExpAritmetica();
                recOpLogico();
                break;
            case ">=":
                parseTerminal(">=");
                recExpAritmetica();
                recOpLogico();
                break;
            case "<=":
                parseTerminal("<=");
                recExpAritmetica();
                recOpLogico();
                break;
            case "==":
                parseTerminal("==");
                recExpAritmetica();
                recOpLogico();
                break;
            case "!=":
                parseTerminal("!=");
                recExpAritmetica();
                recOpLogico();
                break;
            default:
                // erroSintatico("falta operador: >, <, >=, <=, ==, !=");
                break;
        }
    }

    private void recOpIdRelacional() {
        switch (this.currentToken().getRepresentation()) {
            case ">":
                parseTerminal(">");
                recExpAritmetica();
                recOpLogico();
                break;
            case "<":
                parseTerminal("<");
                recExpAritmetica();
                recOpLogico();
                break;
            case ">=":
                parseTerminal(">=");
                recExpAritmetica();
                recOpLogico();
                break;
            case "<=":
                parseTerminal("<=");
                recExpAritmetica();
                recOpLogico();
                break;
            case "==":
                parseTerminal("==");
                recExpLogica();
                break;
            case "!=":
                parseTerminal("!=");
                recExpLogica();
                break;
            default:
                // erroSintatico("falta operador: >, <, >=, <=, ==, !=");
                break;
        }
    }

    private void recExpAritmetica() {
        switch (this.currentToken().getRepresentation()) {
            case "++":
                recFatorAritmetico();
                break;
            case "--":
                recFatorAritmetico();
                break;
            case "-":
                parseTerminal("-");
                recExpAritmetica();
                break;
            case "(":
                recFatorAritmetico();
                break;
            default:
                if (this.currentToken().isIdentifier() || this.currentToken().isNumber()) {
                    recFatorAritmetico();
                    break;
                }
                // erroSintatico("falta identificar, numero, ( ou operador: ++, --, -");
//                        while(!proximo.getTipo().equals("id") && !proximo.getTipo().equals("palavra_reservada") && !this.currentToken().getRepresentation().equals(")") && !this.currentToken().getRepresentation().equals("{") && !this.currentToken().getRepresentation().equals("}")){
//                            proximo=proximo();
//                        }
                break;
        }

    }

    private void recFatorAritmetico() {
        switch (this.currentToken().getRepresentation()) {
            case "++":
                recIdAritmetico();
                recComplementoAritmetico();
                break;
            case "--":
                recIdAritmetico();
                recComplementoAritmetico();
                break;
            case "-":
                parseTerminal("-");
                recExpAritmetica();
                break;
            case "(":
                recFatorAritmetico();
                recComplementoAritmetico();
                break;
            default:
                if (this.currentToken().isIdentifier()) {
                    recIdAritmetico();
                    recComplementoAritmetico();
                    break;
                } else if (this.currentToken().isNumber()) {
                    parseTerminal(this.currentToken().getRepresentation());
                    recComplementoAritmetico();
                    break;
                }
                // erroSintatico("falta numero, identificador, (, ou operador: ++, --, -");
//                while (!proximo.getTipo().equals("palavra_reservada") && !this.currentToken().getRepresentation().equals(")") && !this.currentToken().getRepresentation().equals("{") && !this.currentToken().getRepresentation().equals("}") && !this.currentToken().getRepresentation().equals(";")) {
//                    proximo = proximo();
//                }
                break;
        }
    }

    private void recIdAritmetico() {
        switch (this.currentToken().getRepresentation()) {
            case "++":
            case "--":
                parseTerminal("--");
                parseIdentifier(ACCESS);
                break;
            default:
                if (this.currentToken().isIdentifier()) {
                    parseIdentifier(ACCESS);
                    recIdExpArit();
                }
                // erroSintatico("falta identificador ou operador: ++, --");
                break;
        }
    }

    private void recIdExpArit() {
        switch (this.currentToken().getRepresentation()) {
            case "(":
                recIdExp();
                break;
            case ".":
                recIdExp();
                break;
            case "[":
                recIdExp();
                break;
            case "++":
                parseTerminal("++");
                break;
            case "--":
                parseTerminal("--");
                break;
            default:
                break;
        }
    }

    private void recComplementoAritmetico() {
        switch (this.currentToken().getRepresentation()) {
            case "+":
                parseTerminal("+");
                recFatorAritmetico();
                break;
            case "-":
                parseTerminal("-");
                recFatorAritmetico();
                break;
            case "*":
                parseTerminal("*");
                recFatorAritmetico();
                break;
            case "/":
                parseTerminal("/");
                recFatorAritmetico();
                break;
            default:
                break;
        }
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
