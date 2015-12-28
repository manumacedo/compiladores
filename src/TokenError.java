
public class TokenError extends Error{

	private ErrorType type;
	private String representation;
	private int line;

	public TokenError (int line, String representation, ErrorType type){

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
	public ErrorType getType() {
		return type;
	}
	public void setType(ErrorType type) {
		this.type = type;
	}


}
