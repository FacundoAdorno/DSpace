package org.dspace.app.xmlui.aspect.ELProcessor;

public class LikeCondition extends GenericCondition{

	public static void createCondition(String stringCondition, ConditionResolver conditionManager, MetadataResolver mr) throws Exception{
		if(stringCondition.contains("~")){
			String[] arrayCondition = stringCondition.split("\\~");
			createCondition(arrayCondition, conditionManager, "contains", mr);
		}
	}

}
