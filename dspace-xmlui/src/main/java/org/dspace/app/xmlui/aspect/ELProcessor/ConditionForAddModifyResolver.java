package org.dspace.app.xmlui.aspect.ELProcessor;

public class ConditionForAddModifyResolver extends ConditionForUpdateResolver{

	@Override
	public void checkConditions(String oneCondition, MetadataResolver mr) throws Exception{
		RegexCondition.createCondition(oneCondition, this, mr);
	}
	
	
}
