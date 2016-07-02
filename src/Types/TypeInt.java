package Types;

public class TypeInt extends Types {

	public TypeInt() {
		super("int", 1);
	}

	@Override
	public boolean isSubtypeOf(Types t) {
		if(t==null){
			return false;
		}
		else if(!(t.getClass().equals(this.getClass()))){
			return false;
		}
		else if (t.getName().equals("int")){
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
