package lir;
import slp.*;
import SymbolTable.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.lang.StringBuilder;


public class ClassOffsetsTable {
	
	String icClassName;
	
	private Map<String,Method> methodNameToMethod;
	private Map<String,String> uniqueMethodsName;
	private Map<String,Integer> methodNameToMethodOffset;
	private Map<String,Field> fieldNameToField;
	private Map<Field,Integer> fieldToFieldOffset;
	private int formalsCounter;
	private int methodsCounter;

	public ClassOffsetsTable(ICClass icClass){
		this.icClassName = icClass.getName();
		this.formalsCounter = 1;
		this.methodsCounter = 0;
		fieldNameToField = new HashMap<String, Field>();
		methodNameToMethod = new HashMap<String, Method>();
		methodNameToMethodOffset = new HashMap<String, Integer>();
		fieldToFieldOffset = new HashMap<Field, Integer>();	
		uniqueMethodsName = new HashMap<String,String>();
		
		for (Field field : icClass.getFields()) {//add offset of the class's fields
			fieldNameToField.put(field.getName(), field);
			fieldToFieldOffset.put(field, formalsCounter);
			formalsCounter++;
		}
		for (Method method : icClass.getMethods()){ //add offset of the class's Virtual Methods
			if(!isStaticMethod(method)){
				methodNameToMethod.put("_" + icClassName + "_" + method.getName(), method);
				methodNameToMethodOffset.put("_" + icClassName + "_" + method.getName(), methodsCounter);
				uniqueMethodsName.put(method.getName(),"_" + icClassName + "_" + method.getName());
				methodsCounter++;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public ClassOffsetsTable(ICClass icClass, ClassOffsetsTable superClassLayout){//builder for extending class
		this.icClassName = icClass.getName();
		this.formalsCounter = 1;
		this.methodsCounter = 0;
		
		//get all tables from super class and update offset counters
		this.fieldNameToField = (HashMap<String, Field>)((HashMap<String, Field>)superClassLayout.getMapFieldNameToFIeld()).clone();
		this.fieldToFieldOffset = (HashMap<Field, Integer>) ((HashMap<Field, Integer>)superClassLayout.getMapFieldToFieldOffset()).clone();
		this.formalsCounter = fieldToFieldOffset.size();//counter starts from new size
		this.methodNameToMethod = (HashMap<String, Method>) ((HashMap<String, Method>)superClassLayout.getMapMethodNameToMethod()).clone();
		this.methodNameToMethodOffset = (HashMap<String, Integer>) ((HashMap<String, Integer>)superClassLayout.getMapMethodNameToMethodOffset()).clone();
		this.uniqueMethodsName= (HashMap<String, String>) (((HashMap<String, String>) superClassLayout.getUniqueMethodsNames()).clone());
		this.methodsCounter = methodNameToMethodOffset.size();
		
		for (Field field : icClass.getFields()) {//add new fields offset
			this.fieldToFieldOffset.put(field, formalsCounter);
			formalsCounter++;
		}	
		for (Method method : icClass.getMethods()) { //add new methods offset
			if(!isStaticMethod(method)){
				String methodName=checkIfMethodExistsInOffsetsTable(uniqueMethodsName,method.getName());
				
				if(methodName!=null){//method name exists in superclass - override it				
				 	int methodOffsetNum = this.methodNameToMethodOffset.get(methodName);//get old offset
					this.methodNameToMethodOffset.remove(methodName);
					this.methodNameToMethod.remove(methodName);
					this.methodNameToMethod.put("_" + icClassName + "_" + method.getName(), method);
					this.methodNameToMethodOffset.put("_" + icClassName + "_" + method.getName(), methodOffsetNum);//override method on old offset
					this.uniqueMethodsName.put(method.getName(),"_" + icClassName + "_" + method.getName());
				}
				else{ //method not in super class (new method) - add it with new offset
					this.methodNameToMethod.put("_" + icClassName + "_" + method.getName(), method);
					this.methodNameToMethodOffset.put("_" + icClassName + "_" + method.getName(), this.methodsCounter);
					this.uniqueMethodsName.put(method.getName(),"_" + icClassName + "_" + method.getName());
					this.methodsCounter++;
				}
			}
		}
	}

	public String getClassName(){
		return this.icClassName;
	}

	public Map<Field, Integer> getMapFieldToFieldOffset(){
		return this.fieldToFieldOffset;
	}

	public Map<String, Integer> getMapMethodNameToMethodOffset(){
		return this.methodNameToMethodOffset;
	}

	public Map<String, Method> getMapMethodNameToMethod(){
		return this.methodNameToMethod;
	}

	public Map<String, Field> getMapFieldNameToFIeld(){
		return this.fieldNameToField;
	}

	public int getFieldsOffsetsTableSize(){
		int size = 4*(fieldToFieldOffset.size() + 1);
		return size;
	}
	
	public Map<String,String> getUniqueMethodsNames(){
		return this.uniqueMethodsName;
	}

	public Method getMethodFromName(String methodName){	//get "foo" return method "classA_foo"	
		String name=this.uniqueMethodsName.get(methodName);
		Method method=this.methodNameToMethod.get(name);		
		return method;
	}

	public int getMethodOffsetFromName(String methodName){//get "foo" return method "classA_foo" offset
		String name=this.uniqueMethodsName.get(methodName);
		return this.methodNameToMethodOffset.get(name);
	}

	
	public int getFieldOffsetFromFieldName(String fieldName){
		Field fObj = this.fieldNameToField.get(fieldName);
		return this.fieldToFieldOffset.get(fObj);
	}

	private boolean isStaticMethod(Method method){
		String methodName = method.getName();
		SymbolTable methodScope = method.getEnclosingScope();
		SymbolEntity methodSymbol = (SymbolEntity) methodScope.searchEntityInThisTable(methodName, SymbolCategories.STATIC_METHOD);
		if(methodSymbol == null){
			return false;
		}
		else{
			return true;
		}
	}
	
	public String translateClassOffsetsTable(){
		StringBuilder transClassOffsetTable= new StringBuilder();
		String[] dvMethods = new String[methodsCounter];
		for (String methodName : this.methodNameToMethod.keySet()){
			dvMethods[methodNameToMethodOffset.get(methodName)] = methodName;
		}
		transClassOffsetTable.append("_DV_" + this.icClassName + ": [");
		int dvCounter = 1;
		for (int i=0; i<dvMethods.length; i++){
			transClassOffsetTable.append(dvMethods[i]);
			if(dvCounter < this.methodNameToMethod.keySet().size()){
				transClassOffsetTable.append(",");
			}
			dvCounter ++;
		}		
		transClassOffsetTable.append("]");	
		return transClassOffsetTable.toString();
	}

	private String checkIfMethodExistsInOffsetsTable(Map<String,String> methodsNames, String methodName){	
		if (methodsNames.containsKey(methodName)){
			return methodsNames.get(methodName);
		}	
		return null;
	}

}
