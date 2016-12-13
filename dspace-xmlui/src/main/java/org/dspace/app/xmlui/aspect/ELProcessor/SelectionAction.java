package org.dspace.app.xmlui.aspect.ELProcessor;

public class SelectionAction extends Action{
	
	public static void selectItems(String value) throws Exception{
		cleanResult();		
		new ResolverFactory().getHandleResolver().getItemsFromCondition(value);
	}	
	
	public static void selectCollections(String value) throws Exception{
		cleanResult();
		new ResolverFactory().getHandleResolver().getCollectionsFromCondition(value);
	}
	
	public static void selectCommunities(String value) throws Exception{
		cleanResult();
		new ResolverFactory().getHandleResolver().getCommunitiesFromCondition(value);
	}	
	
}
