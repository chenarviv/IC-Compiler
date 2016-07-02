package Types;
import slp.*;
import Semantic.TableOfTypes;

public class TypeClass extends Types {
	
	private ICClass classObject;
	
	public TypeClass(ICClass classObj, int id) {
		super(classObj.getName(), id);
		this.classObject = classObj;
	}

	public TypeClass(String className) {
		super(className, 0);
		classObject=null;
	}
	
	public ICClass getClassObject() {
		return classObject;
	}
	
	@Override
	public boolean isSubtypeOf(Types t) {
		if (t==null){
			return false;
		}
		else if(t.getClass().equals(TypeClass.class)){
			if(classObject.getName().equals(t.getName())){
				return true;
			}
			else {
				TypeClass clst = TableOfTypes.classTypeByName(classObject.getSuperClassName());
				if (clst==null){
					return false;
				}
				else{
					return clst.isSubtypeOf(t);	
				}
			}	
		}
		else{
			return false;	
		}
	}

	@Override
	public String toString() {
		if (classObject.getSuperClassName() != null){
			TypeClass clst = TableOfTypes.classTypeByName(classObject.getSuperClassName());
			if (clst==null){
				//TODO: to change this returned string
				return "Error with type table generation, or with throwing error at extands in symbol table generation";
			}
			else{
				return "    " + this.getId() + ": Class: " + this.getName() + ", Superclass ID: " + clst.getId();
			}
		}
		else{
			return "    " + this.getId() + ": Class: " + this.getName();
		}
	}
}
