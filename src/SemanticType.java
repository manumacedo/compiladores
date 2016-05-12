
public class SemanticType {
	
	private String representation;
	private boolean isPrimitive;
	private Scope parent;

	public SemanticType (String representation, boolean isPrimitive, Scope parent) {
		this.setRepresentation(representation);
		this.setPrimitive(isPrimitive);
		this.setParent(parent);
		
	}

	public String getRepresentation() {
		return representation;
	}

	public void setRepresentation(String representation) {
		this.representation = representation;
	}

	public boolean isPrimitive() {
		return isPrimitive;
	}

	public void setPrimitive(boolean isPrimitive) {
		this.isPrimitive = isPrimitive;
	}

	public Scope getParent() {
		return parent;
	}

	public void setParent(Scope parent) {
		this.parent = parent;
	}
	
}
