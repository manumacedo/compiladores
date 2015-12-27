import java.util.ArrayList;
import java.util.Arrays;

public class Token {
	
	private static final ArrayList<String> keywords = new ArrayList<String>(
			Arrays.asList(
					"class", "const", "else", 
					"if", "new", "read", 
					"write", "return", "void", 
					"while", "int", "float", 
					"bool", "string", "char", 
					"true", "false", "main")
	);
	
	private TokenType type;
	private String representation;
	private int line;
	
	public Token (int line, String representation, TokenType type){
		
		this.line = line;
		this.representation = representation;
		this.type = type;
	}
	
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public String getRepresentation() {
		return representation;
	}
	public void setRepresentation(String representation) {
		this.representation = representation;
	}
	public TokenType getType() {
		return type;
	}
	public void setType(TokenType type) {
		this.type = type;
	}
	public static boolean isKeyword(Token t){
		return keywords.contains(t.getRepresentation());
	}
	
	public boolean isNumber(){
		return this.type == TokenType.NUM;
	}
	
	public boolean isBinaryOperator(){
		String binaryOperators = "([><=]=?|[!][=]|[|][|]|&&|[+\\-*/])";
		return this.representation.matches(binaryOperators);
	}
	
	public boolean isDelimiter(){
		String delimiters = "([,.;\\[\\]\\(\\)\\{\\}])";
		return this.representation.matches(delimiters);
	}
	
}
