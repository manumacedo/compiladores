
public class SemanticUnit {
	
	
	private Scope scope;
	private String identifier;
	private String type;
	private SemanticCategory category;
	
	public SemanticUnit (Scope scope, String identifier, String type, SemanticCategory category) {
		this.setScope(scope);
		this.setIdentifier(identifier);
		this.setType(type);
		this.setCategory(category);
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public SemanticCategory getCategory() {
		return category;
	}

	public void setCategory(SemanticCategory category) {
		this.category = category;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
}
