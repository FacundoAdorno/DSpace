package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.DSpaceObject;

public class TransformationAction extends Action{

	protected static List<DSpaceObject> result= new ArrayList();
	protected static DSpaceObject dso = null;
	
	//Item
	public static void modifyFirstItems(String condition, String newValues) throws Exception{
		transformItem(condition, newValues, false, "modify");
	}
	
	public static void modifyAllItems(String condition, String newValues) throws Exception{
		transformItem(condition, newValues, true, "modify");
	}
	
	public static void addItemMetadata(String condition, String newValues) throws Exception{
		transformItem(condition, newValues, true, "add");
	}
	
	public static void deleteItemMetadata(String condition, String newValues) throws Exception{
		transformItem(condition, newValues, true, "delete");
	}

	//Collection
	public static void modifyFirstCollections(String condition, String newValues) throws Exception{
		transformCollection(condition, newValues, false, "modify");
	}
	
	public static void modifyAllCollections(String condition, String newValues) throws Exception{
		transformCollection(condition, newValues, true, "modify");
	}
	
	public static void addCollectionMetadata(String condition, String newValues) throws Exception{
		transformCollection(condition, newValues, true, "add");
	}
	
	public static void deleteCollectionMetadata(String condition, String newValues) throws Exception{
		transformCollection(condition, newValues, true, "delete");
	}
	
	//Community
	public static void modifyFirstCommunities(String condition, String newValues) throws Exception{
		transformCommunity(condition, newValues, false, "modify");
	}
	
	public static void modifyAllCommunities(String condition, String newValues) throws Exception{
		transformCommunity(condition, newValues, true, "modify");
	}
	
	public static void addCommunityMetadata(String condition, String newValues) throws Exception{
		transformCommunity(condition, newValues, true, "add");
	}
	
	public static void deleteCommunityMetadata(String condition, String newValues) throws Exception{
		transformCommunity(condition, newValues, true, "delete");
	}
	
	private static void transformItem(String condition, String newValues, boolean updateAll, String action) throws Exception{
		cleanResults();
		cleanPreview();
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyItems(condition, newValues, updateAll, action);
	}
	
	private static void transformCollection(String condition, String newValues, boolean updateAll, String action) throws Exception{
		cleanResults();
		cleanPreview();
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyCollections(condition, newValues, false, action);
	}
	
	private static void transformCommunity(String condition, String newValues, boolean updateAll, String action) throws Exception{
		cleanResults();
		cleanPreview();
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyCommunities(condition, newValues, false, action);
	}
	
	private static void cleanPreview(){
		PreviewManager.cleanPreviews();
	}
	
}
