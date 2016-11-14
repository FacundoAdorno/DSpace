package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.List;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Collection;
import org.dspace.content.Community;

public class ResultManager extends Manager{

	
//	public List<Item> convertResultToItemList(){
//		return (List<Item>)(List<?>) result;
//	}
//	
//	public List<Collection> convertResultToCollectionList(){
//		return (List<Collection>)(List<?>) result;
//	}
//	
//	public List<Community> convertResultToCommunityList(){
//		return (List<Community>)(List<?>) result;
//	}
	
	public void setResultItems(List<Item> items){
		MainProcessor.addResultItems(items);
	}
	
	public void addItem(Item item){
		MainProcessor.addResultItem(item);
	}
	
	public void setResultCollections(List<Collection> collections){
		MainProcessor.addResultCollections(collections);
	}
	
	public void setResultCommunities(List<Community> communities){
		MainProcessor.addResultCommunities(communities);
	}

	public void addCollection(Collection coll) {
		MainProcessor.addResultCollection(coll);
	}
	
	public void addCommunity(Community comm) {
		MainProcessor.addResultCommunity(comm);
	}
	
}
