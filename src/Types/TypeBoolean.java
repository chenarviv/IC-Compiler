package Types;

public class TypeBoolean extends Types {

	public TypeBoolean() {
		super("boolean", 2);
	}

	@Override
	public boolean isSubtypeOf(Types t) {
		if(t==null){
			return false;
		}
		else if(!(t.getClass().equals(this.getClass()))){
			return false;
		}
		else if (t.getName().equals("boolean")){
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
