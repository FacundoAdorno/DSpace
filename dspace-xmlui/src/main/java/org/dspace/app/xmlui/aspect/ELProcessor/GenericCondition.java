package org.dspace.app.xmlui.aspect.ELProcessor;

import org.dspace.content.MetadataField;

public class GenericCondition {

	public static void createCondition(String[] arrayCondition, ConditionResolver conditionManager, String typeCondition, MetadataResolver mr) throws Exception{
		
		if(arrayCondition.length == 2){
			MetadataField mf = mr.getMetadataFieldFromString(arrayCondition[0]);
			if(mf == null){
				throw new Exception("No existe el metadato"+arrayCondition[0]);
			}
			Condition condition = new Condition(mf, arrayCondition[1], typeCondition);
			conditionManager.addCondition(condition);
		}
	}
	
}
