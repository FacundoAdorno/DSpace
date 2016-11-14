package org.dspace.app.xmlui.aspect.ELProcessor;

public class EqualCondition extends GenericCondition{

	public static void createCondition(String stringCondition, ConditionManager conditionManager){
		if(stringCondition.contains("=")){
			String[] arrayCondition = stringCondition.split("\\=");
			createCondition(arrayCondition, conditionManager, "like");
		}
	}
	
}
