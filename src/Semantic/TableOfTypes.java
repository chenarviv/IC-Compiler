package Semantic;
import java.util.HashMap;
import java.util.LinkedList;

import java.util.Map;

import slp.*;
import Types.TypeArray;
import Types.TypeBoolean;
import Types.TypeClass;
import Types.TypeInt;
import Types.TypeMethod;
import Types.TypeNull;
import Types.TypeString;
import Types.TypeVoid;
import Types.Types;


public class TableOfTypes {

	public static TypeBoolean typeBool;
	public static TypeInt typeInt;
	public static TypeNull typeNull;
	public static TypeString typeString;
	public static TypeVoid typeVoid;
	public static int id;
	private static Map<Type,TypeArray> singularArrayTypes;// maps element types to array types	
	private static Map<String,TypeClass> singularClassTypes;//maps element types to class types
	private static Map<Method,TypeMethod> singularMethodTypes;//maps element types to method types
	private static HashMap<Integer,Method> methodsMap = new HashMap<Integer,Method>();//a map for methods by creation order
	static int methodsCounter=0;

	public static void TableOfTypesInit(){
		id = 5;
		typeBool = new TypeBoolean();
		typeInt = new TypeInt();
		typeNull = new TypeNull();
		typeString = new TypeString();
		typeVoid = new TypeVoid();
		singularArrayTypes = new HashMap<Type, TypeArray>();
		singularClassTypes = new HashMap<String, TypeClass>();
		singularMethodTypes = new HashMap<Method, TypeMethod>();
		
		Type mainParamType = new PrimitiveType(-1, DataType.STRING);
		mainParamType.incrementDimension();
		arrayType(mainParamType);	
		Type mainReturnType = new PrimitiveType(-1, DataType.VOID);
		Formal fMainMethod = new Formal(-1,mainParamType, "args");
		LinkedList<Formal> lMainFormals = new LinkedList<Formal>();
		lMainFormals.add(fMainMethod);
		Method methodMainSig = new StaticMethod(-1,mainReturnType,"main", lMainFormals, null);
		methodType(methodMainSig);	
	}
	
	public static TypeClass classType(ICClass classAST) {//returns singular class type object
		if (singularClassTypes.containsKey(classAST.getName())){//return already created class
			return singularClassTypes.get(classAST.getName());
		}
		else{
			id++;
			TypeClass clst = new TypeClass(classAST, id);//create and return new class
			singularClassTypes.put(classAST.getName(),clst);			
			return clst;
		}
	}
	
	public static TypeClass classTypeByName(String className){
		if (singularClassTypes.containsKey(className)){//return already created class
			return singularClassTypes.get(className);
		}
		else{
			return null; //class type object doesn't exist
		}
	}


	public static TypeMethod methodType(Method method) {
		if (singularMethodTypes.containsKey(method)){//return already created method
			return singularMethodTypes.get(method);
		}
		else{//create and return new method
			id++;
			TypeMethod mtdt = new TypeMethod(method,id);
			singularMethodTypes.put(method, mtdt);
			methodsMap.put(methodsCounter, method);
			methodsCounter++;
			return mtdt;
		}
	}

	public static TypeMethod getMainMethodType() {//returns the main method type
		return singularMethodTypes.get(methodsMap.get(0));
	}
	
	public static TypeArray arrayType(Type elemType) { //returns singular array type object
		if (singularArrayTypes.containsKey(elemType)) {
			
			return singularArrayTypes.get(elemType);//return already created array type 
		}
		else{// array type doesn’t exist
			id++;
			TypeArray arrt = new TypeArray(elemType, id);//create and return the new array
			singularArrayTypes.put(elemType,arrt);	
			return arrt;
		}
	}
	
	public static Types typeToTypesConverter(Type t){
		if(t == null) { return null; }
		else{
			if (t.getDimension()>0){
				return TableOfTypes.arrayType(t);
			}
			else if(t.getName().equals(DataType.INT.getDescription())){
				return TableOfTypes.typeInt;
			}
			else if(t.getName().equals(DataType.BOOLEAN.getDescription())){
				return TableOfTypes.typeBool;
			}
			else if(t.getName().equals(DataType.STRING.getDescription())){
				return TableOfTypes.typeString;
			}
			else if(t.getName().equals(DataType.VOID.getDescription())){
				return TableOfTypes.typeVoid;
			}
			else{
				return TableOfTypes.classTypeByName(t.getName());
			}
		}
	}
}