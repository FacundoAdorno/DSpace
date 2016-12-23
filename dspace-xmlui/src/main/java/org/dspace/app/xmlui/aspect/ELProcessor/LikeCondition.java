package org.dspace.app.xmlui.aspect.ELProcessor;

public class LikeCondition extends GenericCondition{

	public static boolean createCondition(String stringCondition, ConditionResolver conditionManager, MetadataResolver mr) throws Exception{
		if(stringCondition.contains("~")){
			if(stringCondition.contains("^~")){
				String[] arrayCondition = stringCondition.split("\\^~");
				createCondition(arrayCondition, conditionManager, "doesnt_contains", mr);
			}else{
				String[] arrayCondition = stringCondition.split("\\~");
				createCondition(arrayCondition, conditionManager, "contains", mr);
			}
			return true;
		}
		return false;
	}

}
