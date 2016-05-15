import java.util.TreeMap;

public class ClassUnit {
	private TreeMap<String, MethodUnit> methods;
	private TreeMap<String, SemanticUnit> attributes;
	
	
	private String identifier;
	
	public ClassUnit (String identifier) {
		this.methods = new TreeMap<>();
		this.attributes = new TreeMap<>();
		
		this.setIdentifier(identifier);
	}
	
	public void inheritFrom(ClassUnit parent) {
		this.methods.putAll(parent.methods);
		this.attributes.putAll(parent.attributes);
	}
	
	public boolean hasAttribute (String attribute) {
		return this.attributes.containsKey(attribute);
	}
	
	public boolean hasMethod(String method) {
		return this.methods.containsKey(method);
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
}
