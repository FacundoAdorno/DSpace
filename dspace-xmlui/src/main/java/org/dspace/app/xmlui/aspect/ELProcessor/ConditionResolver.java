package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.ArrayList;
import java.util.List;

abstract class ConditionResolver extends Resolver{

	protected List<Condition> conditions;
	
	public void addCondition(Condition condition){
		this.conditions.add(condition);
	}
	
	protected void separeteCondition(String condition) throws Exception{
		this.conditions = new ArrayList<Condition>();
		String[] splitConditions = condition.split("\\,");
		MetadataResolver mr = resolverFactory.getMetadataResolver();
		for(String oneCondition : splitConditions){
			checkConditions(oneCondition, mr);
		}		
	}
	
	protected void checkConditions(String oneCondition, MetadataResolver mr) throws Exception{}
}
