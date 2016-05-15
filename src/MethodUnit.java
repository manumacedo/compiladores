import java.util.ArrayList;
import java.util.TreeMap;

public class MethodUnit {
	private TreeMap<String, SemanticUnit> variables;
	private ArrayList<SemanticUnit> parameters;
	
	private String identifier;
	private String returnType;
	
	public MethodUnit(String identifier) {
		this.parameters = new ArrayList<>();
		this.variables = new TreeMap<>();
		
		this.setIdentifier(identifier);
	}
	
	public SemanticUnit getUnit (String identifier) {
		if (this.hasVariable(identifier))
			return this.variables.get(identifier);
		
		return null;
	}
	
	public void addVariable (SemanticUnit variable) {
		this.variables.put(variable.getIdentifier(), variable);
	}
	
	public boolean hasVariable (String id) {
		return this.variables.containsKey(id);
	}
	
	public void addParameter (SemanticUnit parameter) {
		this.parameters.add(parameter);
	}
	
	public boolean equalsTo(MethodUnit anotherMethod) {

		/*
		 * Retorno diferente = false
		 * Quantidade de parametros diferente = false
		 * Tipo dos parametros diferente = false
		 * 
		 */
		
		if(!this.returnType.equals(anotherMethod.returnType))
			return false;
		
		if(this.parameters.size() != anotherMethod.parameters.size())
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
	
	public String getReturnType () {
		return this.returnType;
	}

	public void setReturnType(String type) {
		this.returnType = type;
		
	}
	
}
