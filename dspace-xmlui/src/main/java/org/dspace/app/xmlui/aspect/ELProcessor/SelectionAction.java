package org.dspace.app.xmlui.aspect.ELProcessor;

public class SelectionAction extends Action{
	
	public static void selectItems(String value) throws Exception{
		cleanResult();		
		new ResolverFactory().getSelectResolver().getItemsFromCondition(value);
	}	
	
	public static void selectCollections(String value) throws Exception{
		cleanResult();
		new ResolverFactory().getSelectResolver().getCollectionsFromCondition(value);
	}
	
	public static void selectCommunities(String value) throws Exception{
		cleanResult();
		new ResolverFactory().getSelectResolver().getCommunitiesFromCondition(value);
	}	
	
}
