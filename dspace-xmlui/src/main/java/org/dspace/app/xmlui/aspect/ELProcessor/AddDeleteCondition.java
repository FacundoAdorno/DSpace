package org.dspace.app.xmlui.aspect.ELProcessor;

public class AddDeleteCondition extends GenericCondition{
	
	public static void createCondition(String stringCondition, ConditionResolver conditionManager, MetadataResolver mr) throws Exception{
		String[] arrayCondition = new String[2];
		arrayCondition[0] = stringCondition;
		arrayCondition[1] = "";
		createCondition(arrayCondition, conditionManager, "", mr);
	}

}
