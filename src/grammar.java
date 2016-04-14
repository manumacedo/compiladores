
public class grammar {

	public void fileStructure(){
		
	}
	
	public int classStructure(ArrayList<Token> tokens, int i, Parsing parser){
		if(tokens.get(i).getRepresentation().equals("class")){
			if(tokens.get(i+1).getType().equals("identificador")){
				i = this.expressionHeritage(tokens, i, parser);
				if(tokens.get(i).getType().equals("{")){
					i = this.contentClass(tokens, i, parser);
					
				}
			}
		}
	}
	
	public int expressionHeritage(ArrayList<Token> tokens, int i, Parsing parser){
		if(tokens.get(i).getRepresentation().equals(">")){
			if(tokens.get(i+1).getType().equals("identificador")){
				return i+1;
			}
		}else if(tokens.get(i).getRepresentation().equals(" ")){
			return i+1;
		}
		else{
			//erro
			return i+1;
		}
	}
	
	public int contentClass(ArrayList<Token> tokens, int i, Parsing parser){
		if
	}
}