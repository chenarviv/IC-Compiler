package Types;

public class TypeNull extends Types {

	public TypeNull() {
		super("null", 3);
	}

	@Override
	public boolean isSubtypeOf(Types t) {
		if (t==null){
			return false;
		}
		else if(t.getName().equals("null")){
			return true;
		}
		else if(t.getClass().equals(TypeClass.class)){
			return true;
		}
		else if(t.getClass().equals(TypeArray.class)){
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
