import java.util.ArrayList;

public class ErrorHandler {
	
	class Error {
		String message;
		int line;
		
		public Error(int line, String message) {
			this.line = line;
			this.message = message;
		}
	}
	
	ArrayList<Error> errorList;
	
	public ErrorHandler () {
		this.errorList = new ArrayList<>();
	}
	
	public void add(int line, String message) {
		this.errorList.add(new Error(line+1, message));
	}
	
	
	
	public void output () {
		for (Error err : this.errorList) {
			System.out.println(err.line + " " + err.message);
		}
	}
}
