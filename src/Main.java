import java.io.File;
import java.util.ArrayList;


public class Main {
	private static final String IN_DIR = "input";
	private static final String OUT_DIR = "output";
	
	public static void main(String [] args) {
		
		
		
		
		/*
		 * 
		 * test zone
		 * 
		 * */

		/*
		 * 
		 * 
		 * 
		 * */
		
		
		ArrayList<File> fileList = Utils.listFiles(IN_DIR);
		
		for (File file : fileList) {
			
				System.out.println("Scanning file " + file.getPath());
			
				LexIO io = new LexIO(file);
				
				LexIO noTrash = Lexer.removeTrash(io);
				noTrash.writeIntermediate(OUT_DIR + "/noTrash.txt");
				
				
				
				LexIO noComments = Lexer.removeComments(noTrash);
				
				noComments.writeIntermediate(OUT_DIR + "/noComments.txt");
				
				System.exit(0);
				
				/*
				LexIO noWhitespace = Lexer.removeWhitespace(noComments);
				
				noWhitespace.writeIntermediate(OUT_DIR + "/noWhitespace.txt");
				
				LexIO tokens = Lexer.getValidTokens(noWhitespace);
				 */
		}
	}
}