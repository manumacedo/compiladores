
public class SemanticUnit{
	
	private String identifier;
	private String type;
	private SemanticCategory category;
	private Scope parent;
	
	public SemanticUnit (String identifier, String type, SemanticCategory category, Scope parent)  {
		this.setIdentifier(identifier);
		this.setType(type);
		this.setCategory(category);
		this.setParent(parent);
		
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

	public Scope getParent() {
		return parent;
	}

	public void setParent(Scope parent) {
		this.parent = parent;
	}
	
	public String toString () {
		return this.getIdentifier() + " " + this.getType() + " " + this.getCategory();
	}
	
}
