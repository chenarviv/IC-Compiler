package slp;

public abstract class Type extends ASTNode{
	
	private String name;
	
	private int dimension = 0; // for array
	
	public int getDimension() {
		return dimension;
	}
	
	public Type (int line)
	{
		super(line);
	}

	public void incrementDimension() {
		++dimension;
	}
	
	public void setDimention(int newDim){
		if(newDim >= 0){
			this.dimension = newDim;
		}
		else{
			System.out.println("Error: Dimention is not valid");
		}
	}
	
	public String getName()
	{
		return this.name; 
	}
	
	
}
