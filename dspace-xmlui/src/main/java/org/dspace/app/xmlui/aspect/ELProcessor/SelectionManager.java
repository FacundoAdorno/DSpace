package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.List;
import java.util.UUID;

import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;

public class SelectionManager extends Manager{
	
	public static void selectItems(String value) throws Exception{
				
		handleManager.getItemsFromCondition(value);
	}	
	
	public static void selectCollections(String value) throws Exception{
		
		handleManager.getCollectionsFromCondition(value);
	}
	
	public static void selectCommunities(String value) throws Exception{
		
		handleManager.getCommunitiesFromCondition(value);
	}
	
	private static String identifySelection(String selection) throws Exception{
		
		String selectionType;
		if(selection.contains("=")){
			//they are selecting by condition
			selectionType="condition";
		}else{
			//it is by handle
			selectionType="handle";
		}
		
		return selectionType;
	
	}
	
	
	
}
