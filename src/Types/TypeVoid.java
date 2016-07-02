package Types;

public class TypeVoid extends Types {

	public TypeVoid() {
		super("void", 5);
	}

	@Override
	public boolean isSubtypeOf(Types t) {
		if (t==null){
			return false;
		}
		else if(!(t.getClass().equals(this.getClass()))){
			return false;
		}
		else if (t.getName().equals("void")){
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