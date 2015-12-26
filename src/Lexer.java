import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MalformedObjectNameException;

public class Lexer {
	static final String validStrings = "(\"[ #-~]*\"|\'[ !#-~]*\')"; // qualquer caractere
																	 // entre 32 e 126 exceto
																	 // o 34 ("), entre aspas
																	 // duplas ou simples
	
	static final String openStrings = "(\".*?(?:\"|$))|(\'.*?(?:\'|$))"; // qualquer caractere
																		 // com aspas duplas ou simples
	
	static final String lineCommentStart = "(//.*)"; // Início de comentário de
														// linha, "//"
	
	static final String inlineBlockComment = "(/\\*.*?\\*/)"; // Comentário de
																// bloco "/*
																// aaaa */"
	
	static final String blockCommentStart = "(/\\*.*)"; // Início de comentário
														// de bloco "/* aaaaa"
	
	static final String blockCommentEnd = "(.*?\\*\\/)"; // Fim de comentário de

	
	static final String delimiterSymbols = "[\\[\\]\\-+*/|&(){}><=,.;\\s]";
	static final String invalidSymbolsWithLetters = "[^\\[\\]\\-+*/|&(){}><=,.;\\s0-9a-zA-Z_]";
	static final String invalidSymbols = "[^\\[\\]\\-+*/|&(){}><=,.;\\s0-9_]";
	
	
	static final String malformedIdentifier = "([a-zA-Z][a-zA-Z0-9_]+"
			+ invalidSymbolsWithLetters + ".*?(?="
			+ delimiterSymbols + "|$))";
	
	
	static final String malformedFloat = "(\\d+\\.\\d+"
			+ invalidSymbols + ".*?(?="
			+ delimiterSymbols + "|$))";
	
	static final String malformedInt = "(\\d+"
			+ invalidSymbols + ".*?(?="
			+ delimiterSymbols + "|$))";
	
	
	static final String identifiers = "((?<=" 
			+ delimiterSymbols + "|^)[a-zA-Z][a-zA-Z0-9_]*(?="   // indentificadores seguidos de operador/delimitador
			+ delimiterSymbols + "|$))";
	
	static final String floatNumbers = "((?<="
			+ delimiterSymbols + "|^)\\d+\\.\\d+(?="
			+ delimiterSymbols + "|$))";
	
	
	static final String intNumbers = "";
	
	
	/**
	 * Remove o lixo do código: 
	 * Strings não fechadas e caracteres constantes não fechados
	 * Strings com 
	 * Gera os erros, retorna um pipe para a próxima operação
	 * 
	 * @param io
	 * @return
	 */
	public static LexIO removeTrash(LexIO io) {

		LexIO lexOut = new LexIO(io);
		String[] lines = io.getLines();
		int lineNumber = 0;

		String allStrings = String.join("|", validStrings, openStrings);

		while(lineNumber < lines.length){
			StringBuilder builder = new StringBuilder(lines[lineNumber]);
			
			//System.out.println("Line: " + builder.toString());
			Pattern pattern = Pattern.compile(allStrings);
			Matcher matcher = pattern.matcher(lines[lineNumber]);
			
			while (matcher.find()) {
				if (matcher.group(2) != null) {
					System.out.println("On line " + (lineNumber + 1) + ", invalid string, removing " + matcher.start() + " " + matcher.end());
					lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
				} else if (matcher.group(3) != null) {
					System.out.println("On line " + (lineNumber + 1) + ", open character constant, removing " + matcher.start() + " " + matcher.end());
					lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
				} 
			}
			lineNumber++;
		}
		
		System.out.println("removed open strings ---------");

		for (String line : lines) {
			//System.out.println(line);
		}

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
	 * fechado 
	 * No comentário fechado, ele tenta dar match em 3 possíveis
	 * combinações: 
	 * -Comentário de bloco em linha; 
	 * -Comentário de linha usando //;
	 * -Início de comentário de bloco
	 * 
	 * 
	 * No comentário aberto, ele consome as linhas seguintes até encontrar
	 * um padrão de fechar comentário de bloco
	 * 
	 */
	
	public static LexIO removeComments(LexIO io) {
		
		LexIO lexOut = new LexIO(io);
		String[] lines = io.getLines();
		String lineCommentsPattern;
		String openCommentPattern;
		boolean isBlockCommentOpen = false;
		int lineNumber = 0;
		
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

			/*System.out.println("Trying to match line " + lineNumber
					+ (isBlockCommentOpen ? " with comments " : " without comments")); */
			
			
			
			while (matcher.find()) {
				
				// Comentário de bloco aberto
				
				if (isBlockCommentOpen) {
					if(matcher.group(1) != null) {
						System.out.println(lineNumber + ": replacing block comment end: " + matcher.start() + "  "
								+ matcher.end() + " " + matcher.group(1));
					isBlockCommentOpen = false;
					skipLine = false;
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
					System.out.println(lineNumber + ": replacing comment start: " + matcher.group(4));
					isBlockCommentOpen = true;
				}
				
				lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
				break;
			}
			if(skipLine)
				lineNumber++;
		}
		
		System.out.println("removed comments ---------");
		lexOut.setLines(lines);
		return lexOut;
	}
	
	public static LexIO removeMalformed(LexIO io) {
		
		LexIO lexOut = new LexIO(io);
		String[] lines = io.getLines();
		int lineNumber = 0;

		String malformed = String.join("|", validStrings, malformedIdentifier, malformedFloat, malformedInt);

		while(lineNumber < lines.length){
			StringBuilder builder = new StringBuilder(lines[lineNumber]);
			
			//System.out.println("Line: " + builder.toString());
			Pattern pattern = Pattern.compile(malformed);
			Matcher matcher = pattern.matcher(lines[lineNumber]);
			
			while (matcher.find()) {
				if (matcher.group(1) != null) {
					if(!matcher.group(1).matches("'[a-zA-Z0-9]'")) {
						System.out.println("On line " + (lineNumber + 1) + ", malformed character constant, removing " + matcher.start() + " " + matcher.end());
						lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
						lineNumber--;
						break;
					}	
				} else if (matcher.group(2) != null) {
					System.out.println("On line " + (lineNumber + 1) + ", malformed identifier, removing " + matcher.start() + " " + matcher.end());
					lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
					lineNumber--;
					break;
				} else if (matcher.group(3) != null || matcher.group(4) != null) {
					System.out.println("On line " + (lineNumber + 1) + ", malformed number, removing " + matcher.start() + " " + matcher.end());
					lines[lineNumber] = builder.replace(matcher.start(), matcher.end(), " ").toString();
					lineNumber--;
					break;
				}
				
			}
			lineNumber++;
		}
		
		System.out.println("removed malformed stuff ---------");
		return lexOut;
	}

	public static LexIO removeWhitespace(LexIO io) {
		// TODO Auto-generated method stub
		return null;
	}

	public static LexIO getValidTokens(LexIO io) {
		LexIO lexOut = new LexIO(io);
		String[] lines = io.getLines();

		String validTokensPattern = String.join("|", validStrings, identifiers, floatNumbers);
		
		Pattern pattern = Pattern.compile(validTokensPattern);
		Matcher matcher;
		int lineNumber = 0;
		
		String strMatch;
		String idMatch;
		String numMatch;
		Token token = null;
		
		
		while(lineNumber < lines.length) {
			 matcher = pattern.matcher(lines[lineNumber]);
			 
			 while(matcher.find()) {
				 
				 strMatch  = matcher.group(1);
				 idMatch = matcher.group(2);
				 numMatch = matcher.group(3);
				 
				 if (strMatch != null){
					 if(strMatch.startsWith("\""))
						 token = new Token(lineNumber, strMatch, TokenType.STRING);
					 else {
						 if(strMatch.matches("'[a-zA-Z0-9]'")) { // valid chars
							 token = new Token(lineNumber, strMatch, TokenType.CHAR);
						 } else {
							 // char error
						 }
					 }
				 } else if (idMatch != null){
					 token = new Token(lineNumber, idMatch, TokenType.ID);
					 if(Token.isKeyword(token))
						 token.setType(TokenType.KEYWORD);
					 
				 } else if (numMatch != null){
					 token = new Token(lineNumber, numMatch, TokenType.NUM);
					 
					 
				 }/* else if (matcher.group(4) != null){
					 
				 } else if (matcher.group(5) != null){
					 
				 } else if (matcher.group(6) != null){
					 
				 }
				 */
				 if(token != null)
					 lexOut.addToken(token);
			 }
			 lineNumber++;
		}
		
		return lexOut;
		
	}
	
	private static LexIO getOperators(LexIO io) {
		return null;
	}

}
