import java.io.File;
import java.util.ArrayList;


public class Main {
	private static final String IN_DIR = "input";
	private static final String OUT_DIR = "output";
	
	public static void main(String [] args) {
		
		ArrayList<File> fileList = Utils.listFiles(IN_DIR);
		
		for (File file : fileList) {
			
				System.out.println("Scanning file " + file.getPath());
			
				LexIO io = new LexIO(file);
				
				LexIO noOpen = Lexer.removeTrash(io);
				noOpen.writeIntermediate(OUT_DIR + "/noOpen.txt");
				
				System.exit(0);
				
				LexIO noComments = Lexer.removeComments(noOpen);
				
				noComments.writeIntermediate(OUT_DIR + "/noComments.txt");
				
				LexIO noWhitespace = Lexer.removeWhitespace(noComments);
				
				noWhitespace.writeIntermediate(OUT_DIR + "/noWhitespace.txt");
				
				LexIO tokens = Lexer.getValidTokens(noWhitespace);
		}
	}
}