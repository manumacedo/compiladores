import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

	public static LexIO removeComments(LexIO io) {
		// TODO Auto-generated method stub
		return null;
	}

	public static LexIO removeWhitespace(LexIO io) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
	public static LexIO removeUnclosedStuff(LexIO io) {
		
		LexIO lexOut = new LexIO(io);
		String [] lines = io.getLines();
		
		String validStrings = "(\"[ #-~]*\")";
		String openStrings = "(\"[ !#-~]*$)";
		String lineCommentStart = "(//.*)";
		String inlineBlockComment = "(/\\*.*?\\*/)";
		String blockCommentStart = "(/\\*.*)";
		String blockCommentEnd = "(.*?\\*\\/)";
		
		boolean isBlockCommentOpen = false;
		int lineNumber = 0;
		
		/*
		 * Capture group para strings corretas: ("[ #-~]*")
		 * Capture group para strings incorretas: (\"[ !#-~]*$)
		 * 
		 * */
		
		String allStrings = validStrings + "|" + openStrings;
		
		for(String line : lines) {
			// System.out.println("Line: " + line);
			Pattern pattern = Pattern.compile(allStrings);
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
			    if(matcher.group(2) != null){
			    	lines[lineNumber] = line.replace(matcher.group(2), "");
			    }
			}
			lineNumber++;
		}
		
		/*
		 * Comentários de linha podem ser // desde que não exista um comentário aberto antes
		 * 
		 * Comentários de linha também não são considerados dentro de strings válidas
		 * Como a engine é gulosa, um match na string é suficiente.
		 * 
		 * Este while alterna entre dois estados: comentário aberto / comentário fechado
		 * No comentário fechado, ele tenta dar match em 3 possíveis combinações: 
		 * -Comentário de bloco em linha;
		 * -Comentário de linha usando //
		 * -Início de comentário de bloco
		 * 
		 * 
		 * No comentário aberto, ele consome as linhas seguintes até encontrar um padrão de
		 * fechar comentário de bloco 
		 * 
		 */
		
		String lineComments;
		
		
		/*
		 * 
		 * 
		 * 
		 * "    */

		lineComments = validStrings + "|" +  inlineBlockComment + "|" + lineCommentStart + "|" + blockCommentStart;
		
		String openCommentPattern = blockCommentEnd + "|" + "(^.*?$)";
		lineNumber = 0;
		while(lineNumber < lines.length) {
			Pattern pattern;
			if(isBlockCommentOpen)
				pattern = Pattern.compile(openCommentPattern);
			else
				pattern = Pattern.compile(lineComments);
			
			Matcher matcher = pattern.matcher(lines[lineNumber]);

			System.out.println("Trying to match line " + lineNumber + (isBlockCommentOpen? " with comments " : " without comments"));
			while (matcher.find()) {
				if(isBlockCommentOpen) {
					if(matcher.group(1) != null) {
						System.out.println(lineNumber + ": replacing block comment end: " + matcher.start() + "  " + matcher.end() + " " + matcher.group(1));
						StringBuilder builder = new StringBuilder(lines[lineNumber]);
						lines[lineNumber] = builder.delete(matcher.start(), matcher.end()).toString();
						isBlockCommentOpen = false;
						lineNumber--;
					} else {
						System.out.println("Comment is open, deleting line " + lineNumber);
						lines[lineNumber] = "";
					}
					break;
				} else if(matcher.group(1) != null) {
					System.out.println(lineNumber + ": matched valid string, dont touch it");
				} else if(matcher.group(2) != null) {
					System.out.println(lineNumber + ": replacing inline block comment : " + matcher.start() + "  " + matcher.end() + " " + matcher.group(2));
					StringBuilder builder = new StringBuilder(lines[lineNumber]);
					lines[lineNumber] = builder.delete(matcher.start(), matcher.end()).toString();
					lineNumber--;
					break;
				} else if(matcher.group(3) != null) {
					System.out.println(lineNumber + ": replacing line comment: " + matcher.group(3));
					StringBuilder builder = new StringBuilder(lines[lineNumber]);
					lines[lineNumber] = builder.delete(matcher.start(), matcher.end()).toString();
					break;
				} else if (matcher.group(4) != null) {
					System.out.println(lineNumber + ": replacing comment start: " + matcher.group(4));
					StringBuilder builder = new StringBuilder(lines[lineNumber]);
					
					lines[lineNumber] = builder.delete(matcher.start(), matcher.end()).toString();
					isBlockCommentOpen = true;
					break;
				} else {
					System.out.println("No matches for "  + lines[lineNumber]);
					break;
				}
			}	
			lineNumber++;
		}
		
		System.out.println("-----------------");
		
		for(String line : lines) {
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
