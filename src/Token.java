import java.util.ArrayList;
import java.util.Arrays;

public class Token implements Comparable<Token>{
	
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
	
	public boolean isCommand () {
		return this.isAny("read", "write", "if", "while", "new") || this.isType();
	}
	
	public boolean isType() {
		return this.isPrimitiveType() || this.isIdentifier();
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
	
	public boolean isInteger () {
		return this.type == TokenType.inteiro;
	}
	
	public boolean isChar () {
		return this.type == TokenType.caractere_constante;
	}
	
	public boolean isBool () {
		return this.isAny("true", "false");
	}
	
	public boolean isString () {
		return this.type == TokenType.cadeia_constante;
	}
	
	public boolean isIdentifier () {
		return this.type == TokenType.identificador;
	}
	
	public boolean isFloat () {
		return this.type == TokenType.decimal;
	}
	
	public boolean hasType (String type) {		
		switch (type) {
		case "int":
			return this.isInteger();
		case "float":
			return this.isFloat();
		case "char":
			return this.isChar();
		case "string":
			return this.isString();
		case "bool":
			return this.isBool();
		default:
			return this.is(type);
		}
	}
	
	
	public boolean isNumber(){
		return (this.type == TokenType.decimal) || (this.type == TokenType.inteiro);
	}
	
	public boolean isBinaryOperator(){
		String binaryOperators = "([><=]=?|[!][=]|[|][|]|&&|[+\\-*/.])";
		return this.representation.matches(binaryOperators);
	}
	
	public boolean isDelimiter(){
		String delimiters = "([,;\\[\\]\\(\\)\\{\\}])";
		return this.representation.matches(delimiters);
	}
	
	public boolean isPrimitiveType() {
		return this.isAny("int", "float", "bool", "string", "char");
	}
	
	public boolean is (String rep) {
		return this.getRepresentation().equals(rep);
	}
	
	public boolean isAny(String... args) {
		for (String check : args) {
			if(this.is(check))
				return true;
		}
		
		return false;
	}
	
	public boolean isAssignable() {
		return this.isAny("-", "++", "--", "(", "true", "false") || this.isIdentifier() || this.isPrimitive();
	}
	
	public boolean isPrimitive () {
		return this.isNumber() || this.isChar() || this.isString();
	}

	
	@Override
	public int compareTo(Token anotherToken) {
		return this.getRepresentation().compareTo(anotherToken.getRepresentation());
	}
	
	@Override
	public String toString() {
		return this.getRepresentation();
	}

	public boolean isLogicalOperator() {
		return this.isAny("&&", "||");
	}

	public boolean isEqualityOperator() {
		return this.isAny("==", "!=");
	}

	public boolean isArithmeticOperator() {
		return this.isAny("+", "-", "*", "/");
	}
	
	public boolean isRelationalOperator() {
		return this.isAny(">", "<", ">=", "<=");
	}
}
