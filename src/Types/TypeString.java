package Types;

public class TypeString extends Types {

	public TypeString (){
		super("string", 4);
	}
	
	@Override
	public boolean isSubtypeOf(Types t) {
		if (t==null){
			return false;
		}
		else if(!(t.getClass().equals(this.getClass()))){
			return false;
		}
		else if (t.getName().equals("string")){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public String toString() {
		return "    " + this.getId() + ": Primitive type: " + this.getName();
	}
}