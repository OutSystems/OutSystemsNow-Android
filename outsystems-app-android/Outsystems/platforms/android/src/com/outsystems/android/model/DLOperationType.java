package com.outsystems.android.model;

public enum DLOperationType {
	
	dlLoginOperation("login"),
	dlOpenUrlOperation("openurl"),
	dlInvalidOperation("invalid");
	
	public final String name;
	
	DLOperationType(String name){
		this.name = name;
	}
	
		
	public String toString(){
		return this.getName();
	}
	
	public String getName(){
		return this.name;
	}
	

	public static DLOperationType getOperationType(String name){
		DLOperationType result = dlInvalidOperation;
		
		for(DLOperationType t: DLOperationType.values()){
			if(t.getName().equalsIgnoreCase(name)){
				result = t;
				break;
			}
		}
		
		return result;
	}

}
