package org.dspace.app.xmlui.aspect.ELProcessor;

public class SelectionAction{
	
	public static void selectItems(String value) throws Exception{
				
		new FactoryManager().getHandleManager().getItemsFromCondition(value);
		setResult();
	}	
	
	public static void selectCollections(String value) throws Exception{
		
		new FactoryManager().getHandleManager().getCollectionsFromCondition(value);
		setResult();
	}
	
	public static void selectCommunities(String value) throws Exception{
		
		new FactoryManager().getHandleManager().getCommunitiesFromCondition(value);
		setResult();
	}	
	
	private static void setResult(){
		ResultContainer.setSelectionPage();
		ResultContainer.cleanResults();
	}
	
}
