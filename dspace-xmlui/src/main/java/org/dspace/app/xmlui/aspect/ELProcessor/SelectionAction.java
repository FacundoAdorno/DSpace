package org.dspace.app.xmlui.aspect.ELProcessor;

public class SelectionAction extends Action{
	
	public static void selectItems(String value) throws Exception{
		cleanResults();		
		new ResolverFactory().getHandleResolver().getItemsFromCondition(value);
		setResult();
	}	
	
	public static void selectCollections(String value) throws Exception{
		cleanResults();
		new ResolverFactory().getHandleResolver().getCollectionsFromCondition(value);
		setResult();
	}
	
	public static void selectCommunities(String value) throws Exception{
		cleanResults();
		new ResolverFactory().getHandleResolver().getCommunitiesFromCondition(value);
		setResult();
	}	
	
}
