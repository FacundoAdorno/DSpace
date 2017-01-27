package org.dspace.app.xmlui.aspect.ELProcessor;

public class SelectResolver extends Resolver {

	public void getItemsFromCondition(String condition) throws Exception{
		resolverFactory.getHandleResolver().getItemsFromCondition(condition);;
	}
	
	public void getCollectionsFromCondition(String condition) throws Exception{
		resolverFactory.getHandleResolver().getCollectionsFromCondition(condition);;
	}
	
	public void getCommunitiesFromCondition(String condition) throws Exception{
		resolverFactory.getHandleResolver().getCommunitiesFromCondition(condition);;
	}
	
}
