package org.dspace.app.xmlui.aspect.ELProcessor;

import org.dspace.content.MetadataField;

public class GenericCondition {

	public static void createCondition(String[] arrayCondition, ConditionResolver conditionManager, String typeCondition, MetadataResolver mr) throws Exception{
		
		MetadataField mf = mr.getMetadataFieldFromString(arrayCondition[0]);
		if(mf == null){
			throw new Exception("No existe el metadato"+arrayCondition[0]);
		}
		Condition condition = null;
		if(arrayCondition.length == 2){			
			condition = new Condition(mf, arrayCondition[1], typeCondition, ".+");
		}
		else if(arrayCondition.length == 3){
			condition = new Condition(mf, arrayCondition[2], typeCondition, arrayCondition[1]);
		}else{
			throw new Exception("La condicion de transformacion esta mal formada");
		}
		conditionManager.addCondition(condition);
	}
	
}
