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
		 * 
		 * 
		 * Comentários de linha podem ser // desde que não exista um comentário aberto antes
		 * 
		  
		 	exemplo: o // não é comentário de linha, pois o /* anterior existe; 
		 	 *//*
		  
		*
		* 
		* 
		* Comentários de linha também não são considerados dentro de strings válidas
		* Como a engine é gulosa, um match na string é suficiente.
		* 
		*/
		String lineComments;
		
		
		/*
		 * 
		 * 
		 * 
		 * "    */

		lineComments = validStrings + "|" + lineCommentStart + "|" + inlineBlockComment + "|" + blockCommentStart + "|" + blockCommentEnd;
		lineNumber = 0;
		while(lineNumber < lines.length) {
			
			Pattern pattern = Pattern.compile(lineComments);
			Matcher matcher = pattern.matcher(lines[lineNumber]);
			
			
			/*
			if(isBlockCommentOpen) {
				Pattern pattern = Pattern.compile(blockCommentEnd);
				Matcher matcher = pattern.matcher(lines[lineNumber]);
				if(matcher.find()) {
					System.out.println(matcher.group(1) + "   closing comment block on line " + lineNumber);
					lines[lineNumber] = lines[lineNumber].replace(matcher.group(1), "");
					isBlockCommentOpen = false;
					continue;
				}
				
				System.out.println("comment block open, deleting line " + lineNumber);
				lines[lineNumber] = "";
				
			} else {
				Pattern pattern = Pattern.compile(validStrings);
				Matcher matcher = pattern.matcher(lines[lineNumber]);
				
				if(matcher.find()) {
					System.out.println(matcher.group(1) + "   found valid string " + lineNumber);
					startIndex = matcher.start();
					startLen = matcher.end();
				}
				
				pattern = Pattern.compile(inlineBlockComment);
				matcher = pattern.matcher(lines[lineNumber]);

				if(matcher.find()) {
					
					if(matcher.start() <= startIndex) {
						System.out.println(matcher.group(1) + "   removing inline comment block " + lineNumber);
						lines[lineNumber] = lines[lineNumber].replace(matcher.group(1), "");
						continue;
					}
				}
				
				pattern = Pattern.compile(lineCommentStart);
				matcher = pattern.matcher(lines[lineNumber]);
				if(matcher.find()) {
					if(matcher.start() <= startIndex) {
						System.out.println(matcher.group(1) + "   removing line comment " + lineNumber);
						lines[lineNumber] = lines[lineNumber].replace(matcher.group(1), "");
					}
					continue;
				}
				
				
				
				pattern = Pattern.compile(blockCommentStart);
				matcher = pattern.matcher(lines[lineNumber]);
				if(matcher.find()) {
					System.out.println(matcher.group(1) + "   opening comment block on line " + lineNumber);
					lines[lineNumber] = lines[lineNumber].replace(matcher.group(1), "");
					isBlockCommentOpen = true;
					continue;
				}

			}
			lineNumber++;
			*/
			
			System.out.println("Trying to match line " + lineNumber);
			while (true) {
				
				if(matcher.find()) {
				
					System.out.println(lineNumber + ": match " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + " " + matcher.group(4) + " " + matcher.group(5));
	
					if(matcher.group(2) != null) {
						System.out.println(lineNumber + ": replacing line comment : " + matcher.start() + "  " + matcher.end() + " " + matcher.group(2));
						
						StringBuilder builder = new StringBuilder(lines[lineNumber]);
						lines[lineNumber] = builder.delete(matcher.start(), matcher.end()).toString();
						
					} else if(matcher.group(3) != null) {
						System.out.println(lineNumber + ": replacing inline block comment: " + matcher.group(3));
						StringBuilder builder = new StringBuilder(lines[lineNumber]);
						lines[lineNumber] = builder.delete(matcher.start(), matcher.end()).toString();
					
					} else if (matcher.group(4) != null) {
						System.out.println(lineNumber + ": replacing comment start: " + matcher.group(4));
						StringBuilder builder = new StringBuilder(lines[lineNumber]);
						lines[lineNumber] = builder.delete(matcher.start(), matcher.end()).toString();
						isBlockCommentOpen = true;
						
					} else if (matcher.group(5) != null && isBlockCommentOpen) {
						System.out.println(lineNumber + ": comment were open, closing at: " + matcher.start());
						isBlockCommentOpen = false;
					} else {
						System.out.println("No matches for "  + lines[lineNumber]);
						break;
					}
				} else {
					if (isBlockCommentOpen){
						if(lineNumber < lines.length){
							System.out.println(lineNumber + ": block comment open, deleting: " + lines[lineNumber]);
							lines[lineNumber] = "";
							break;
						}
					}
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
