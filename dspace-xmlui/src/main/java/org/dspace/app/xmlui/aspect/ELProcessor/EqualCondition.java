package org.dspace.app.xmlui.aspect.ELProcessor;

public class EqualCondition extends GenericCondition{

	public static boolean createCondition(String stringCondition, ConditionResolver conditionManager, MetadataResolver mr) throws Exception{
		if(stringCondition.contains("=")){
			if(stringCondition.contains("^=")){
				String[] arrayCondition = stringCondition.split("\\^=");
				createCondition(arrayCondition, conditionManager, "not_like", mr);
			}else{
				String[] arrayCondition = stringCondition.split("\\=");
				createCondition(arrayCondition, conditionManager, "like", mr);
			}
			return true;
		}
		return false;
	}
	
}
