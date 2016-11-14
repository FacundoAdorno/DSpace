package org.dspace.app.xmlui.aspect.ELProcessor;

public class GenericCondition {

	public static void createCondition(String[] arrayCondition, ConditionManager conditionManager, String typeCondition){
		
		if(arrayCondition.length == 2){
			Condition condition = new Condition(arrayCondition[0], arrayCondition[1], typeCondition);
			conditionManager.addCondition(condition);
		}
	}
	
}
