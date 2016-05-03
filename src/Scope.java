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
	
	public boolean has (Token token) {
		for (SemanticUnit s : this.semanticUnitList) {
			if (token.is(s.getIdentifier())) {
				return true;
			}
		}
		return false;
	}
}
