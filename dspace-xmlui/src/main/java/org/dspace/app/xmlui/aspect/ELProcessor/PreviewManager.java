package org.dspace.app.xmlui.aspect.ELProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dspace.content.Item;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;

public class PreviewManager{
	
	private static List<Item> items;
	private static List<Collection> collections;
	private static List<Community> communities;
	private static String action;
	private static List<Condition> conditions;
	private static boolean updateAll;
	private static List<DSpaceObjectPreview> previews;
	
	public static void cleanPreviews(){
		previews = new ArrayList<DSpaceObjectPreview>();
	}
	
	
	public static List<DSpaceObjectPreview> showPreview(SelectionPage sp) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		if(!items.isEmpty()){
			showItemPeview();
		}else if(!collections.isEmpty()){
			showCollectionPeview();
		}else if(!communities.isEmpty()){
			showCommunityPeview();
		}else{
			
		}
		return previews;
	}
	
	public static void addPreview(DSpaceObjectPreview preview){
		getPreviews().add(preview);
	}
	
	public static void addPreview(List<DSpaceObjectPreview> previewList){
		getPreviews().addAll(previewList);
	}
	
	public static void executeUpdate(UpdateResolver ur) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, AuthorizeException{
		if(!items.isEmpty()){
			ur.updateItems(conditions, updateAll, action, items);
		}else if(!collections.isEmpty()){
			ur.updateCollections(conditions, updateAll, action, collections);
		}else if(!communities.isEmpty()){
			ur.updateCommunities(conditions, updateAll, action, communities);
		}
	}
	
	private static void showItemPeview() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		showGenericPreview(new UpdateItem(), (List<DSpaceObject>)(List<?>) items);
	}
	
	private static void showCollectionPeview() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		showGenericPreview(new UpdateCollection(), (List<DSpaceObject>)(List<?>) collections);
	}

	private static void showCommunityPeview() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		showGenericPreview(new UpdateCommunity(), (List<DSpaceObject>)(List<?>) communities);
	}
	
	private static void showGenericPreview(Update update, List<DSpaceObject> DSOs) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Method m = update.getClass().getMethod(action+"Preview", List.class, List.class, boolean.class );
		m.invoke(update, DSOs, conditions, updateAll);
	}
	
	
	
	
	
	public static List<Item> getItems() {
		return items;
	}
	public static void setItems(List<Item> items) {
		PreviewManager.items = items;
	}
	public static List<Collection> getCollections() {
		return collections;
	}
	public static void setCollections(List<Collection> collections) {
		PreviewManager.collections = collections;
	}
	public static List<Community> getCommunities() {
		return communities;
	}
	public static void setCommunities(List<Community> communitiesList) {
		communities = communitiesList;
	}
	public static String getAction() {
		return action;
	}
	public static void setAction(String action) {
		PreviewManager.action = action;
	}
	public static List<Condition> getConditions() {
		return conditions;
	}
	public static void setConditions(List<Condition> conditions) {
		PreviewManager.conditions = conditions;
	}
	public static boolean getUpdateAll() {
		return updateAll;
	}
	public static void setUpdateAll(boolean updateAll) {
		PreviewManager.updateAll = updateAll;
	}

	public static List<DSpaceObjectPreview> getPreviews() {
		if(previews == null){
			previews = new ArrayList<DSpaceObjectPreview>();
		}
		return previews;
	}

	public static void setPreviews(List<DSpaceObjectPreview> previews) {
		PreviewManager.previews = previews;
	}
	
	
	
	
	

}
