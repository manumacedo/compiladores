import java.util.ArrayList;

public class Scope {
	
	private Scope parent;
	private Token token;
	private ArrayList<SemanticUnit> semanticUnitList;
	
	
	public Scope (Token token, Scope parent) {
		this.token = token;
		this.parent = parent;
		this.semanticUnitList = new ArrayList<>();
	}
	
	public void insertUnit(SemanticUnit unit) {
		this.semanticUnitList.add(unit);
	}
	
	public boolean has (String id) {
		for (SemanticUnit s : this.semanticUnitList) {
			if (id.equals(s.getIdentifier())) {
				return true;
			}
		}
		return false;
	}
	
	public Scope getParent () {
		return this.parent;
	}
	
	public String toString() {
		return this.token.getRepresentation() + " <- " + (this.parent == null ? null : this.parent.toString());
	}
}
