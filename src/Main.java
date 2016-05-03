import java.io.File;
import java.util.ArrayList;


public class Main {
	private static final String IN_DIR = "input";
	private static final String OUT_DIR = "output";
	
	public static void main(String [] args) throws InterruptedException {
		
		ArrayList<File> fileList = Utils.listFiles(IN_DIR);
		
		for (File file : fileList) {
			
				System.out.println("Scanning file " + file.getPath());
			
				LexIO io = new LexIO(file);
				
				LexIO noTrash = Lexer.removeTrash(io);
				LexIO noComments = Lexer.removeComments(noTrash);
				LexIO noMalformed = Lexer.removeMalformed(noComments);
				LexIO tokens = Lexer.getValidTokens(noMalformed);
				
//				tokens.writeOutput(OUT_DIR + "/" + file.getName());
				
				
//				for(Token token: tokens.getTokens()) {
//					System.out.println(  token.getRepresentation() + " " + token.getType() + " " + token.getLine());
//				}
//
//				Thread.sleep(100); // used for debug
//				for(TokenError error : tokens.getErrors()) {
//					System.err.println( error.getRepresentation() + " " + error.getType() + " " + " " + error.getLine());
//				}
				
				new SemanticAnalyzer(tokens).execute();
				
		}
	}
}