import java.util.ArrayList;

public class ErrorHandler {
	
	private class Error {
		private String message;
		private int line;
		
		public Error(int line, String message) {
			this.line = line;
			this.message = message;
		}
	}
	
	private ArrayList<Error> errorList;
	
	public ErrorHandler () {
		this.errorList = new ArrayList<>();
	}
	
	public void add(int line, String message) {
		this.errorList.add(new Error(line, message));
	}
	
	public void output () {
		for (Error err : this.errorList) {
			System.out.println(err.line + " " + err.message);
		}
	}
}
