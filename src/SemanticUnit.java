
public class SemanticUnit {
	
	private String identifier;
	private String type;
	private SemanticCategory category;
	
	public SemanticUnit (String identifier, String type, SemanticCategory category) {
		this.setIdentifier(identifier);
		this.setType(type);
		this.setCategory(category);
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
