package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.List;

public class ConditionForUpdateResolver extends ConditionResolver{

	public List<Condition> prepareUpdate(String conditions) throws Exception{
		this.separeteCondition(conditions);
		return this.conditions;
	}
	
	@Override
	public void checkConditions(String oneCondition, MetadataResolver mr) throws Exception{
		
	}
	
}
