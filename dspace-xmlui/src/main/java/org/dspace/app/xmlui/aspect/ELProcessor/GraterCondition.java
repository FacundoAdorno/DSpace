package org.dspace.app.xmlui.aspect.ELProcessor;

public class GraterCondition extends GenericCondition{

	public static void createCondition(String stringCondition, ConditionManager conditionManager){
		if(stringCondition.contains(">")){
			String[] arrayCondition = stringCondition.split("\\>");
			createCondition(arrayCondition, conditionManager, "grater");
		}
	}
	
}
