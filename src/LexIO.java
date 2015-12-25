import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class LexIO {
	
	private ArrayList<Token> validTokens;
	private ArrayList<Error> errors;
	private String [] lines;
	private File inputFile;
	
	public LexIO(File inputFile) {
		
		this.setErrors(new ArrayList<Error>());
		this.setValidTokens(new ArrayList<Token>());
		
		this.setInputFile(inputFile);
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(inputFile.getPath()));
			String contents = new String(encoded, StandardCharsets.UTF_8);
			this.setLines(contents.split(System.lineSeparator()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public LexIO(LexIO pipe) {
		this.setInputFile(pipe.getInputFile());
		this.setLines(pipe.getLines());
		this.setErrors(pipe.getErrors());
		this.setValidTokens(pipe.getValidTokens());
	}

	public void writeIntermediate(String fileName) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			for(String line : lines) {
				writer.println(line);
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public ArrayList<Error> getErrors() {
		return errors;
	}

	public void setErrors(ArrayList<Error> errors) {
		this.errors = errors;
	}

	public ArrayList<Token> getValidTokens() {
		return validTokens;
	}

	public void setValidTokens(ArrayList<Token> validTokens) {
		this.validTokens = validTokens;
	}
	
	public void addToken(Token t) {
		this.validTokens.add(t);
	}

	public String [] getLines() {
		return lines;
	}

	public void setLines(String [] lines) {
		this.lines = lines;
	}

}
