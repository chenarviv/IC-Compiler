package Types;

public abstract class Types {
	
	private String name;
	private int id;
	
	public Types(String name, int id){
		this.name = name;
		this.id = id;
	}
	
	public String getName(){
		return name;
	}
	
	public int getId(){
		return id;
	}
	
	public abstract boolean isSubtypeOf(Types t); //checks if a given Types object is a sub type of the current Types object
	public abstract String toString();
}
