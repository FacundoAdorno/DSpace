package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Collection;
import org.dspace.content.Community;

public class ResultContainer{
	
	private static boolean wasSet = false;
	private String message;
	private static List<DSpaceObject> DSOs = new ArrayList<DSpaceObject>();
	private static List<DSpaceObjectPreview> resultToShow = new ArrayList<DSpaceObjectPreview>();
	
	public static List<DSpaceObjectPreview> getResultsToShow(){
		if(resultToShow.isEmpty() && wasSet){
			SelectionPage.showNoResult();
		}
		return resultToShow;
	}
	
	public static void cleanResults(){
		DSOs = new ArrayList<DSpaceObject>();
		cleanPreviewResult();
		wasSet = false;
	}
	
	public static void cleanPreviewResult(){
		resultToShow = new ArrayList<DSpaceObjectPreview>();
	}
	
	public static void addResultsToShow(List<DSpaceObjectPreview> results){
		resultToShow = results;
	}
	
	public static void addResultToShow(DSpaceObjectPreview result){
		resultToShow.add(result);
	}
	
	public static void addItems(List<Item> items){
		wasSet = true;
		for(Item item: items){
			addItem(item);
		}
	}
	
	public static void addItem(Item item){
		wasSet = true;
		SelectionPage.showItemSelectionMessage();
		resultToShow.add(new DSpaceObjectPreview(item.getHandle(), "dc.title", item.getName(), "-"));
		DSOs.add(item);
	}
	
	public static void addCollections(List<Collection> collections){
		wasSet = true;
		for(Collection coll: collections){
			addCollection(coll);
		}		
	}
	
	public static void addCollection(Collection coll) {
		wasSet = true;
		SelectionPage.showCollectionSelectionMessage();
		resultToShow.add(new DSpaceObjectPreview(coll.getHandle(), "dc.title", coll.getName(), "-"));
		DSOs.add(coll);
	}
	
	public static void addCommunities(List<Community> communities){
		wasSet = true;
		for(Community comm: communities){
			addCommunity(comm);
		}		
	}	
	
	public static void addCommunity(Community comm) {
		wasSet = true;
		SelectionPage.showCommunitySelectionMessage();
		resultToShow.add(new DSpaceObjectPreview(comm.getHandle(), "dc.title", comm.getName(), "-"));
		DSOs.add(comm);
	}
	
	public static List<DSpaceObject> getDSOs(){
		return DSOs;
	}	
	
}
