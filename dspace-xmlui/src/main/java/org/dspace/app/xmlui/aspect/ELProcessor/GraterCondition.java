package org.dspace.app.xmlui.aspect.ELProcessor;

public class GraterCondition extends GenericCondition{

	public static void createCondition(String stringCondition, ConditionResolver conditionManager, MetadataResolver mr) throws Exception{
		if(stringCondition.contains(">")){
			String[] arrayCondition = stringCondition.split("\\>");
			createCondition(arrayCondition, conditionManager, "grater", mr);
		}
	}
	
}
