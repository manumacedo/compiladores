import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
	static final String validStrings = "(\"[ #-~]*\"|\'[ !#-~]*\')"; // qualquer caractere
														// entre 32 e 126 exceto
														// o 34 ("), entre aspas
														// duplas ou simples
	
	static final String openStrings = "(\"[ !#-~]*$|\'[ !#-~]*$)"; // qualquer caractere
														// entre 32 e 126 exceto
														// o 34 ("), come�ando
														// com aspas duplas ou simples
	
	static final String lineCommentStart = "(//.*)"; // In�cio de coment�rio de
														// linha, "//"
	
	static final String inlineBlockComment = "(/\\*.*?\\*/)"; // Coment�rio de
																// bloco "/*
																// aaaa */"
	
	static final String blockCommentStart = "(/\\*.*)"; // In�cio de coment�rio
														// de bloco "/* aaaaa"
	
	static final String blockCommentEnd = "(.*?\\*\\/)"; // Fim de coment�rio de
															// bloco "aaaa */"

	public static LexIO removeComments(LexIO io) {
		// TODO Auto-generated method stub
		return null;
	}

	public static LexIO removeWhitespace(LexIO io) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Remove o lixo do c�digo: Coment�rios, strings n�o fechadas e caracteres constantes n�o fechados
	 * 
	 * @param io
	 * @return
	 */
	public static LexIO removeTrash(LexIO io) {

		LexIO lexOut = new LexIO(io);
		String[] lines = io.getLines();
		String lineCommentsPattern;
		String openCommentPattern;

		boolean isBlockCommentOpen = false;
		int lineNumber = 0;

		String allStrings = validStrings + "|" + openStrings;

		for (String line : lines) {
			 System.out.println("Line: " + line);
			Pattern pattern = Pattern.compile(allStrings);
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
				if (matcher.group(2) != null) {
					lines[lineNumber] = line.replace(matcher.group(2), "");
				}
			}
			lineNumber++;
		}
		
		/*
		 * Coment�rios de linha podem ser // desde que n�o exista um coment�rio
		 * aberto antes
		 * 
		 * Coment�rios de linha tamb�m n�o s�o considerados dentro de strings
		 * v�lidas Como a engine � gulosa, um match na string � suficiente.
		 * 
		 * Este while alterna entre dois estados: coment�rio aberto / coment�rio
		 * fechado 
		 * No coment�rio fechado, ele tenta dar match em 3 poss�veis
		 * combina��es: 
		 * -Coment�rio de bloco em linha; 
		 * -Coment�rio de linha usando //;
		 * -In�cio de coment�rio de bloco
		 * 
		 * 
		 * No coment�rio aberto, ele consome as linhas seguintes at� encontrar
		 * um padr�o de fechar coment�rio de bloco
		 * 
		 */

		lineCommentsPattern = validStrings + "|" + inlineBlockComment + "|" + lineCommentStart + "|"
				+ blockCommentStart;

		openCommentPattern = blockCommentEnd + "|" + "(^.*?$)";
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

			System.out.println("Trying to match line " + lineNumber
					+ (isBlockCommentOpen ? " with comments " : " without comments"));
			while (matcher.find()) {
				
				// Coment�rio de bloco aberto
				
				if (isBlockCommentOpen) {
					if(matcher.group(1) != null) {
						System.out.println(lineNumber + ": replacing block comment end: " + matcher.start() + "  "
								+ matcher.end() + " " + matcher.group(1));
					isBlockCommentOpen = false;
					skipLine = false;
					}
				}
				
				// Coment�rio de bloco fechado
				
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
				
				lines[lineNumber] = builder.delete(matcher.start(), matcher.end()).toString();
				break;
			}
			if(skipLine)
				lineNumber++;
		}

		System.out.println("-----------------");

		for (String line : lines) {
			System.out.println(line);
		}

		lexOut.setLines(lines);

		return lexOut;
	}

	public static LexIO getValidTokens(LexIO io) {
		// TODO Auto-generated method stub
		return null;
	}

}
