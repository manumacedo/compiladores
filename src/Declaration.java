
public class Declaration {
	
	private String type;
	private SemanticCategory category;
	
	
	public Declaration (String type, SemanticCategory category) {
		this.setType(type);
		this.setCategory(category);
	}


	public SemanticCategory getCategory() {
		return category;
	}


	public void setCategory(SemanticCategory category) {
		this.category = category;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}
	
}
