package Types;
import slp.*;
import Semantic.TableOfTypes;

public class TypeMethod extends Types {

	private Method method;

	public TypeMethod(Method method, int id) {
		super(method.getName(), id);
		this.method = method;
	}


	public Types getReturnType(){//return type of the method
		if(this.method.getType().getDimension() > 0){
			return TableOfTypes.arrayType(this.method.getType());
		}
		else if(this.method.getType().getName().equals(DataType.BOOLEAN.getDescription())){
			return TableOfTypes.typeBool;
		}
		else if(this.method.getType().getName().equals(DataType.INT.getDescription())){
			return TableOfTypes.typeInt;
		}
		else if(this.method.getType().getName().equals(DataType.STRING.getDescription())){
			return TableOfTypes.typeString;
		}
		else if(this.method.getType().getName().equals(DataType.VOID.getDescription())){
			return TableOfTypes.typeVoid;
		}
		else{
			return TableOfTypes.classTypeByName(this.method.getType().getName());
		}
	}


	public Method getMethod() {
		return this.method;
	}

	@Override
	public boolean isSubtypeOf(Types t) {
		if (t==null){
			return false;
		}
		else if(t.getClass().equals(this.getClass())){
			TypeMethod mtype = (TypeMethod) t;
			if(method.getName().equals(mtype.getMethod().getName())){
				if(!this.getReturnType().isSubtypeOf(mtype.getReturnType())){
					return false;
				}
				else if(method.getFormals().size()==mtype.getMethod().getFormals().size()){
					int i = 0;
					boolean check = true;
					for (Formal superFormal : mtype.getMethod().getFormals()) {
						Formal subFormal = method.getFormals().get(i);
						i++;
						if (!(TableOfTypes.typeToTypesConverter(subFormal.getType()).isSubtypeOf(TableOfTypes.typeToTypesConverter(superFormal.getType())))){
							check = false;
						}
					}
					return check;
				}
				else{
					return false;
				}
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}


	private String returnTypeToString(){
		if(method.getType().getDimension()==0)
			return method.getType().getName();
		else{
			String par = "";
			for(int i = 0; i < method.getType().getDimension(); i++){
				par = par+"[]";
			}
			return method.getType().getName() + par;}
	}


	@Override
	public String toString() {
		String str = "";
		int j = 0;
		for (Formal formal : method.getFormals()) {
			if (j == (method.getFormals().size()-1)){
				if (formal.getType().getDimension() == 0){
					str = str + formal.getType().getName();
				}
				else{
					String par = "";
					for(int i = 0; i < formal.getType().getDimension(); i++){
						par = par+"[]";
					}
					str = str + formal.getType().getName() + par;
				}
			}
			else{
				if (formal.getType().getDimension() == 0){
					str = str + formal.getType().getName() + ", ";
				}
				else{
					String par = "";
					for(int i = 0; i < formal.getType().getDimension(); i++){
						par = par+"[]";
					}
					str = str + formal.getType().getName() + par+ ", ";
				}
			}
			j++;
		}
		return "    " + this.getId() + ": Method type: {" + str + " -> " + returnTypeToString() + "}";
	}

}