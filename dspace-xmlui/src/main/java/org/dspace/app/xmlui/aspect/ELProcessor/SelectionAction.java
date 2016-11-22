package org.dspace.app.xmlui.aspect.ELProcessor;

import org.dspace.core.Context;

public class SelectionAction extends Action{
	
	public static void selectItems(String value) throws Exception{
				
		new ResolverFactory().getHandleResolver().getItemsFromCondition(value);
		setResult();
	}	
	
	public static void selectCollections(String value) throws Exception{
		
		new ResolverFactory().getHandleResolver().getCollectionsFromCondition(value);
		setResult();
	}
	
	public static void selectCommunities(String value) throws Exception{
		
		new ResolverFactory().getHandleResolver().getCommunitiesFromCondition(value);
		setResult();
	}	
	
	private static void setResult(){
		ResultContainer.setSelectionPage();
		ResultContainer.cleanResults();
	}
	
}
