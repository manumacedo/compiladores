import java.util.ArrayList;
import java.util.TreeMap;

public class MethodUnit {
	
	private TreeMap<String, SemanticUnit> constants;
	private TreeMap<String, SemanticUnit> variables;
	private ArrayList<SemanticUnit> parameters;
	
	private String identifier;
	private String returnType;
	
	public MethodUnit(String identifier) {
		this.parameters = new ArrayList<>();
		this.constants = new TreeMap<>();
		this.variables = new TreeMap<>();
		
		this.setIdentifier(identifier);
	}
	
	public void addParameter (SemanticUnit parameter) {
		this.parameters.add(parameter);
	}
	
	public boolean equalsTo(MethodUnit anotherMethod) {
		if(!this.returnType.equals(anotherMethod.returnType))
			return false;
		
		for (int i = 0; i < parameters.size(); i++)
			if(!this.parameters.get(i).hasSameTypeAs(anotherMethod.parameters.get(i)))
				return false;
		
		return true;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
}
