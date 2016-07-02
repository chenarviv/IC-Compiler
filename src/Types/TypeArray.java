package Types;
import slp.*;


public class TypeArray extends Types {

	private Type arrayType;
	private Type elementType;

	public TypeArray (Type elemType, int id){
		super(elemType.getName(), id);
		this.arrayType = elemType;
		this.elementType = getElementType(this.arrayType);
	}

	public Type getArrayType() {
		return arrayType;
	}

	public Type getElementType() {
		return this.elementType;
	}
	
	@Override
	public boolean isSubtypeOf(Types t) {
		if (t == null){
			return false;
		}
		else if(t.getClass().equals(this.getClass())){
			if((arrayType.getName().equals(((TypeArray) t).getArrayType().getName()))
					&&(arrayType.getDimension()==((TypeArray) t).getArrayType().getDimension())){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	private Type getElementType(Type arrayType){//type of the elements in an array
		String typeName = arrayType.getName();
		if(arrayType.getClass().equals(PrimitiveType.class)){
			PrimitiveType elementType;
			if(typeName.compareTo("int") == 0){
				elementType = new PrimitiveType(-1, DataType.INT);
			}
			else if(typeName.compareTo("string") == 0){
				elementType = new PrimitiveType(-1, DataType.STRING);
			}
			else{
				elementType = new PrimitiveType(-1, DataType.BOOLEAN);
			}
			elementType.setDimention(arrayType.getDimension() - 1);
			return elementType;
			
		}
		else{
			UserType elementType = new UserType(-1, typeName);
			elementType.setDimention(arrayType.getDimension() - 1);
			return elementType;
		}
	}
	
	@Override
	public String toString() {
		String par = "";
		for(int i = 0; i < this.arrayType.getDimension(); i++){
			par = par+"[]";
		}
		return "    " + this.getId() + ": Array type: " + this.arrayType.getName() + par;
	}
}
