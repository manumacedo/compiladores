import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
	/**
	 * Caracteres válidos para strings
	 */
	static final String validStrings = "(\"[ !#-~]*\"|\'[ !#-~]*\')";

	/**
	 * Qualquer caractere dentro da string, se não houve match da string válida
	 * anterior
	 */
	static final String openStrings = "(\".*?(?:\"|$))|(\'.*?(?:\'|$))";

	/**
	 * Início de comentário de linha
	 */
	static final String lineCommentStart = "(//.*)";

	/**
	 * Comentário de bloco que não se estende para outras linhas
	 */
	static final String inlineBlockComment = "(/\\*.*?\\*/)";

	/**
	 * Início de um comentário de bloco que se estende para outras linhas
	 */
	static final String blockCommentStart = "(/\\*.*)";

	/**
	 * Fim de comentário de bloco
	 */
	static final String blockCommentEnd = "(.*?\\*\\/)";

	/**
	 * Símbolos que delimitam identificadores e números. Símbolos inválidos que
	 * transformam identificadores
	 */
	static final String delimiterSymbols = "[\\[\\]\\-+*/|!&(){}><=,.;\\s]";

	static final String invalidSymbolsWithLetters = "[^\\[\\]\\-+*/|&(){}><=,.;\\s0-9!a-zA-Z_]";
	static final String invalidSymbols = "[^\\[\\]\\-+*/|&(){}><=,.;\\s0-9_]";

	/**
	 * ID mal formado
	 */
	static final String malformedIdentifier = "([a-zA-Z][a-zA-Z0-9_]+" + invalidSymbolsWithLetters + ".*?(?="
			+ delimiterSymbols + "|$))";

	/**
	 * Número e operador mal formado
	 */
	static final String malformedFloat = "(\\d+\\.\\d+" + invalidSymbols + ".*?(?=" + delimiterSymbols + "|$))";
	static final String malformedInt = "(\\d+" + invalidSymbols + ".*?(?=" + delimiterSymbols + "|$))";
	static final String malformedOperator = "(!(?:[^=\\n]|$)|&(?:[^&\\n]|$)|\\|(?:[^\\|\\n]|$))";

	static final String identifiers = "((?<=" + delimiterSymbols + "|^)[a-zA-Z][a-zA-Z0-9_]*(?=" + delimiterSymbols
			+ "|$))";

	/**
	 * Números válidos
	 */
	static final String floatNumbers = "((?<=" + delimiterSymbols + "|^)\\d+\\.\\d+(?=" + delimiterSymbols + "|$))";
	static final String intNumbers = "((?<=" + delimiterSymbols + "|^)\\d+(?=" + delimiterSymbols + "|$))";

	/**
	 * Operadores válidos
	 */
	static final String operators = "([-][-]|[+][+]|[><=]=?|[!][=]|[|][|]|&&|[+\\-*/.])";

	/**
	 * Delimitadores
	 */
	static final String delimiters = "([{}()\\[\\];,])";
	
	static final String invalidExpression = "((?<=" + delimiterSymbols + "|^)([^\\[\\]\\-+*/|&(){}><=,.;\\s0-9!a-zA-Z]" +   ".*?(?="
			+ delimiterSymbols + ")))";
	

	/**
	 * Remove o lixo do código: Strings não fechadas e caracteres constantes não
	 * fechados Strings com Gera os erros, retorna um pipe para a próxima
	 * operação
	 * 
	 * @param io
	 * @return
	 */
	public static LexIO removeTrash(LexIO io) {

		LexIO lexOut = new LexIO(io);
		String[] lines = io.getLines();
		int lineNumber = 0;
		String strError;
		String charError;
		TokenError error = null;

		String allStrings = String.join("|", validStrings, openStrings);

		while (lineNumber < lines.length) {
			StringBuilder builder = new StringBuilder(lines[lineNumber]);

			// System.out.println("Line: " + builder.toString());
			Pattern pattern = Pattern.compile(allStrings);
			Matcher matcher = pattern.matcher(lines[lineNumber]);

			while (matcher.find()) {
				if (matcher.group(2) != null) {
					strError = matcher.group(2);
					error = new TokenError(lineNumber, strError, ErrorType.cadeia_mal_formada);
					lexOut.addError(error);
					System.out.println("On line " + (lineNumber + 1) + ", invalid string, removing " + matcher.start()
							+ " " + matcher.end());
					lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
				} else if (matcher.group(3) != null) {
					charError = matcher.group(3);
					error = new TokenError(lineNumber, charError, ErrorType.caractere_mal_formado);
					lexOut.addError(error);
					System.out.println("On line " + (lineNumber + 1) + ", open character constant, removing "
							+ matcher.start() + " " + matcher.end());
					lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
				} 
			}
			lineNumber++;
		}

		System.out.println("removed open strings ---------");
		lexOut.setLines(lines);
		return lexOut;
	}

	/**
	 * Comentários de linha podem ser // desde que não exista um comentário
	 * aberto antes
	 * 
	 * Comentários de linha também não são considerados dentro de strings
	 * válidas Como a engine é gulosa, um match na string é suficiente.
	 * 
	 * Este while alterna entre dois estados: comentário aberto / comentário
	 * fechado No comentário fechado, ele tenta dar match em 3 possíveis
	 * combinações: -Comentário de bloco em linha; -Comentário de linha usando
	 * //; -Início de comentário de bloco
	 * 
	 * 
	 * No comentário aberto, ele consome as linhas seguintes até encontrar um
	 * padrão de fechar comentário de bloco
	 * 
	 */

	public static LexIO removeComments(LexIO io) {

		LexIO lexOut = new LexIO(io);
		String[] lines = io.getLines();
		String lineCommentsPattern;
		String openCommentPattern;
		boolean isBlockCommentOpen = false;
		int lineNumber = 0;

		String commentError;
		TokenError error = null;

		lineCommentsPattern = String.join("|", validStrings, inlineBlockComment, lineCommentStart, blockCommentStart);

		openCommentPattern = String.join("|", blockCommentEnd, "(^.*?$)");

		lineNumber = 0;
		while (lineNumber < lines.length) {
			StringBuilder builder = new StringBuilder(lines[lineNumber]);
			Pattern pattern;
			boolean skipLine = true;
			if (isBlockCommentOpen)
				pattern = Pattern.compile(openCommentPattern);
			else
				pattern = Pattern.compile(lineCommentsPattern);

			Matcher matcher = pattern.matcher(lines[lineNumber]);

			/*
			 * System.out.println("Trying to match line " + lineNumber +
			 * (isBlockCommentOpen ? " with comments " : " without comments"));
			 */

			while (matcher.find()) {

				// Comentário de bloco aberto

				if (isBlockCommentOpen) {
					if (matcher.group(1) != null) {
						System.out.println(lineNumber + ": replacing block comment end: " + matcher.start() + "  "
								+ matcher.end() + " " + matcher.group(1));
						isBlockCommentOpen = false;
						skipLine = false;
						error = null;
					}
				}

				// Comentário de bloco fechado

				else if (matcher.group(1) != null) {
					System.out.println(lineNumber + ": matched valid string, dont touch it");
					continue;
				} else if (matcher.group(2) != null) {
					System.out.println(lineNumber + ": replacing inline block comment : " + matcher.start() + "  "
							+ matcher.end() + " " + matcher.group(2));
					skipLine = false;
				} else if (matcher.group(4) != null) {
					commentError = matcher.group(4);
					System.out.println(lineNumber + ": replacing comment start: " + matcher.group(4));
					isBlockCommentOpen = true;
					error = new TokenError(lineNumber, commentError, ErrorType.comentario_aberto);
				}

				lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
				break;
			}
			if (skipLine)
				lineNumber++;
		}

		if (error != null)
			lexOut.addError(error);

		System.out.println("removed comments ---------");
		lexOut.setLines(lines);
		return lexOut;
	}

	public static LexIO removeMalformed(LexIO io) {

		LexIO lexOut = new LexIO(io);
		String[] lines = io.getLines();
		int lineNumber = 0;
		String idError;
		String numError;
		String charError;
		String opError;
		String invalidExpressionError;
		TokenError error = null;

		String malformed = String.join("|", validStrings, malformedIdentifier, malformedFloat, malformedInt, operators,
				malformedOperator, invalidExpression);

		while (lineNumber < lines.length) {
			StringBuilder builder = new StringBuilder(lines[lineNumber]);

			 System.out.println("Line: " + builder.toString());
			Pattern pattern = Pattern.compile(malformed);
			Matcher matcher = pattern.matcher(lines[lineNumber]);
			
			while (matcher.find()) {
				if (matcher.group(1) != null) {

					if (matcher.group(1).startsWith("\'") && !matcher.group(1).matches("'[a-zA-Z0-9]'")) {
						charError = matcher.group(1);
						error = new TokenError(lineNumber, charError, ErrorType.caractere_mal_formado);
						lexOut.addError(error);
						System.out.println("On line " + (lineNumber + 1) + ", malformed character constant, removing "
								+ matcher.start() + " " + matcher.end());

						lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
						lineNumber--;
						break;
					}
				} else if (matcher.group(2) != null) {

					idError = matcher.group(2);
					error = new TokenError(lineNumber, idError, ErrorType.id_mal_formado);
					lexOut.addError(error);
					System.out.println(
							"On line " + (lineNumber + 1) + ", malformed identifier, removing " + matcher.group());

					lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
					lineNumber--;
					break;
				} else if (matcher.group(3) != null || matcher.group(4) != null) {

					if (matcher.group(3) != null) {
						numError = matcher.group(3);
					} else {
						numError = matcher.group(4);
					}
					error = new TokenError(lineNumber, numError, ErrorType.nro_mal_formado);
					lexOut.addError(error);
					System.out.println("On line " + (lineNumber + 1) + ", malformed number, removing " + matcher.start()
							+ " " + matcher.end());

					lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
					lineNumber--;
					break;
				} else if (matcher.group(6) != null) {
					opError = matcher.group(6);
					error = new TokenError(lineNumber, opError, ErrorType.operador_mal_formado);
					lexOut.addError(error);
					System.out.println("On line " + (lineNumber + 1) + ", malformed operator, removing "
							+ matcher.start() + " " + matcher.end());

					lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
					lineNumber--;
					break;
				} else if (matcher.group(7) != null) {
					invalidExpressionError = matcher.group(7);
					error = new TokenError(lineNumber, invalidExpressionError, ErrorType.expressao_invalida);
					lexOut.addError(error);
					System.out.println("On line " + (lineNumber + 1) + ", invalid expression, removing "
							+ matcher.start() + " " + matcher.end());
					lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
				}

			}
			lineNumber++;
		}

		lexOut.sortErrors();

		System.out.println("removed malformed stuff ---------");
		return lexOut;
	}

	/**
	 * Transforma os operadores de subtração + números em números negativos, quando aplicado.
	 * 
	 * 
	 * @param io
	 */
	private static void transformNegativeNumbers(LexIO io) {
		ArrayList<Token> tokens;
		tokens = io.getTokens();

		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (token.getType().equals(TokenType.operador) && token.getRepresentation().equals("-")) {
				if (i == 0 || tokens.get(i - 1).isBinaryOperator() || tokens.get(i - 1).isDelimiter()) {
					if (tokens.get(i + 1).isNumber()) {
						tokens.remove(i);
						Token num = tokens.get(i);
						num.setRepresentation("-" + num.getRepresentation());
						tokens.set(i, num);
					}
				}
			}
		}
	}

	/**
	 * Recupera os tokens válidos do arquivo intermediário
	 * 
	 * @param io
	 * @return
	 */
	public static LexIO getValidTokens(LexIO io) {
		LexIO lexOut = new LexIO(io);
		String[] lines = io.getLines();

		String validTokensPattern = String.join("|", validStrings, identifiers, floatNumbers, intNumbers, operators,
				delimiters);

		Pattern pattern = Pattern.compile(validTokensPattern);
		Matcher matcher;
		int lineNumber = 0;

		String strMatch;
		String idMatch;
		String floatMatch;
		String intMatch;
		String operatorMatch;
		String delimiterMatch;
		Token token = null;

		while (lineNumber < lines.length) {
			matcher = pattern.matcher(lines[lineNumber]);

			while (matcher.find()) {
				token = null;
				strMatch = matcher.group(1);
				idMatch = matcher.group(2);
				floatMatch = matcher.group(3);
				intMatch = matcher.group(4);
				operatorMatch = matcher.group(5);
				delimiterMatch = matcher.group(6);
				if (strMatch != null) {
					if (strMatch.startsWith("\""))
						token = new Token(lineNumber, strMatch, TokenType.cadeia_constante);
					else {
						token = new Token(lineNumber, strMatch, TokenType.caractere_constante);
					}
				} else if (idMatch != null) {
					token = new Token(lineNumber, idMatch, TokenType.identificador);
					if (Token.isKeyword(token))
						token.setType(TokenType.palavra_reservada);

				} else if (floatMatch != null) {
					token = new Token(lineNumber, floatMatch, TokenType.numero);
				} else if (intMatch != null) {
					token = new Token(lineNumber, intMatch, TokenType.numero);
				} else if (operatorMatch != null) {
					token = new Token(lineNumber, operatorMatch, TokenType.operador);
				} else if (delimiterMatch != null) {
					token = new Token(lineNumber, delimiterMatch, TokenType.delimitador);
				}
				if (token != null)
					lexOut.addToken(token);
			}
			lineNumber++;
		}

		Lexer.transformNegativeNumbers(lexOut);

		return lexOut;

	}

}
