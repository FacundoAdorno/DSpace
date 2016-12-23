package org.dspace.app.xmlui.aspect.ELProcessor;

public class ExistsMetadataCondition extends GenericCondition{

	public static void createCondition(String stringCondition, ConditionResolver conditionManager, MetadataResolver mr) throws Exception{
		String[] arrayCondition = new String[2];
		arrayCondition[1] = "";
		if(stringCondition.trim().startsWith("^")){
			arrayCondition[0] = stringCondition.split("\\^")[1];
			createCondition(arrayCondition, conditionManager, "doesnt_exist", mr);
		}else{
			arrayCondition[0] = stringCondition;
			createCondition(arrayCondition, conditionManager, "exists", mr);
		}
	}

}
