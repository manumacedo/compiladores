import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;

public class LexIO {

	private ArrayList<Token> tokens;
	private ArrayList<TokenError> errors;
	private String[] lines;
	private File inputFile;

	public LexIO(File inputFile) {

		this.setErrors(new ArrayList<TokenError>());
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
		this.setValidTokens(pipe.getTokens());
	}

	public void writeIntermediate(String fileName) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			for (String line : lines) {
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

	public ArrayList<TokenError> getErrors() {
		return errors;
	}

	public void setErrors(ArrayList<TokenError> arrayList) {
		this.errors = arrayList;
	}

	public ArrayList<Token> getTokens() {
		return tokens;
	}

	public void setValidTokens(ArrayList<Token> tokens) {
		this.tokens = tokens;
	}

	public void addToken(Token token) {
		this.tokens.add(token);
	}

	public void addError(TokenError t) {
		this.errors.add(t);
	}

	public String[] getLines() {
		return lines;
	}

	public void setLines(String[] lines) {
		this.lines = lines;
	}

	public void sortErrors() {
		this.errors.sort(new Comparator<TokenError>() {
			@Override
			public int compare(TokenError error, TokenError anotherError) {
				return error.getLine() - anotherError.getLine();
			}
		});

	}

	public void writeOutput(String filename) {
		System.out.println(filename);
		PrintWriter writer;
		try {
			writer = new PrintWriter(filename, "UTF-8");
			for (Token token : tokens) {
				writer.println(token.getRepresentation() + " " + token.getType() + " " + (token.getLine() + 1));
			}

			writer.println();

			for (TokenError error : errors) {
				writer.println(error.getRepresentation() + " " + error.getType() + " " + " " + (error.getLine() + 1));
			}

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

}
