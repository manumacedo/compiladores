import java.util.Map.Entry;
import java.util.TreeMap;

public class ClassUnit {
	private TreeMap<String, MethodUnit> methods;
	private TreeMap<String, SemanticUnit> attributes;
	
	private TreeMap<String, MethodUnit> inheritedMethods;
	private TreeMap<String, SemanticUnit> inheritedAttributes;
	
	
	private String identifier;
	
	public ClassUnit (String identifier) {
		this.methods = new TreeMap<>();
		this.attributes = new TreeMap<>();
		
		this.inheritedMethods = new TreeMap<>();
		this.inheritedAttributes = new TreeMap<>();
		
		this.setIdentifier(identifier);
	}
	
	public void inheritFrom(ClassUnit parent) {
		if(!parent.methods.isEmpty())
			this.inheritedMethods.putAll(parent.methods);
		
		if(!parent.attributes.isEmpty())
			this.inheritedAttributes.putAll(parent.attributes);
		if(parent.inheritedMethods != null && !parent.inheritedMethods.isEmpty())
			this.inheritedMethods.putAll(parent.inheritedMethods);
		
		if(parent.inheritedAttributes != null && !parent.inheritedAttributes.isEmpty())
			this.inheritedAttributes.putAll(parent.inheritedAttributes);
	}
	
	public boolean hasInheritedAttribute (String id) {
		return this.inheritedAttributes.containsKey(id);
	}
	
	public boolean hasInheritedMethod (String id) {
		return this.inheritedMethods.containsKey(id);
	}
	
	public boolean isInherited (String identifier) {
		return hasInheritedAttribute(identifier) || hasInheritedMethod(identifier);
	}
	
	public boolean hasOwnAttribute (String attribute) {
		return this.attributes.containsKey(attribute);
	}
	
	public boolean hasOwnMethod(String method) {
		return this.methods.containsKey(method);
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public void addAttribute (SemanticUnit attribute) {
		this.attributes.put(attribute.getIdentifier(), attribute);
	}
	
	public void addMethod (MethodUnit method) {
		this.methods.put(method.getIdentifier(), method);
	}

	public boolean equalsTo(ClassUnit anotherClass) {
		return this.getIdentifier().equals(anotherClass.getIdentifier());
	}
	
	@Override
	public String toString() {
		String out = this.getIdentifier() + "  ->  a: [";
		boolean first = true;
		
		for(Entry<String, SemanticUnit> unit : this.attributes.entrySet()) {
			if(! first)
				out += ", ";
			
			first = false;
			
			out += unit.getKey() + "(" + unit.getValue().getType() + ")";
		}
		first = true;
		out += "] -- m: [";
		
		for(Entry<String, MethodUnit> unit : this.methods.entrySet()) {
			
			if(! first)
				out += ", ";
			
			first = false;
			
			out += unit.getKey() + "(" + unit.getValue().getReturnType() + ")";;
		}
		
		first = true;
		out += "] --- inh-a: [";
		for(Entry<String, SemanticUnit> unit : this.inheritedAttributes.entrySet()) {
			
			if(! first)
				out += ", ";
			
			first = false;
			out += unit.getKey() + "(" + unit.getValue().getType() + ")";
		}
		first = true;
		out += "] -- inh-m: [";
		for(Entry<String, MethodUnit> unit : this.inheritedMethods.entrySet()) {
			if(! first)
				out += ", ";
			
			first = false;
			
			out += unit.getKey() + "(" + unit.getValue().getReturnType() + ")";
		}
		out += "]";
		
		return out;
		
	}

	public MethodUnit getInheritedMethod (String id) {
		return this.inheritedMethods.get(id);
	}
	
	public SemanticUnit getUnit(String id) {
		if(this.attributes.containsKey(id))
			return this.attributes.get(id);
		
		if(this.inheritedAttributes.containsKey(id))
			return this.inheritedAttributes.get(id);
		
		return null;
	}
	
}
