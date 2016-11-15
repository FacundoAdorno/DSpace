package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Collection;
import org.dspace.content.Community;

public class ResultContainer{
	
	private static List<Item> items = new ArrayList<Item>();
	private static List<Collection> collections = new ArrayList<Collection>();
	private static List<Community> communities = new ArrayList<Community>();
	
	public static void cleanResults(){
		items = new ArrayList<Item>();
		collections = new ArrayList<Collection>();
		communities = new ArrayList<Community>();
	}
	
	public static void setSelectionPage(){
		SelectionPage.addItems(items);
		SelectionPage.addCollections(collections);
		SelectionPage.addCommunities(communities);
	}
	
	public static void addItems(List<Item> items){
		ResultContainer.items.addAll(items);
	}
	
	public static void addItem(Item item){
		items.add(item);
	}
	
	public static void addCollections(List<Collection> collections){
		ResultContainer.collections.addAll(collections);
	}
	
	public static void addCollection(Collection coll) {
		collections.add(coll);
	}
	
	public static void addCommunities(List<Community> communities){
		ResultContainer.communities.addAll(communities);
	}	
	
	public static void addCommunity(Community comm) {
		communities.add(comm);
	}
	
}
