package org.dspace.app.xmlui.aspect.ELProcessor;

import static org.dspace.app.xmlui.aspect.ELProcessor.Action.cleanResult;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;

public class TransformationAction extends Action{
	
	private static List<DSpaceObject> DSOs;
	private static String whichDSO;
	private static String action;
	private static List<Condition> conditions;
	private static boolean updateAll;
	
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
		cleanResult();
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyItems(condition, newValues, updateAll, action);
	}
	
	private static void transformCollection(String condition, String newValues, boolean updateAll, String action) throws Exception{
		cleanResult();
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyCollections(condition, newValues, false, action);
	}
	
	private static void transformCommunity(String condition, String newValues, boolean updateAll, String action) throws Exception{
		cleanResult();
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyCommunities(condition, newValues, false, action);
	}
	
	public static void executeUpdate() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, AuthorizeException{
		UpdateResolver ur = new ResolverFactory().getUpdateResolver();
		if(whichDSO.equals("item")){
			ur.updateItems(conditions, updateAll, action, DSOs);
		}else if(whichDSO.equals("collection")){
			ur.updateCollections(conditions, updateAll, action, DSOs);
		}else if(whichDSO.equals("community")){
			ur.updateCommunities(conditions, updateAll, action, DSOs);
		}
		SelectionPage.showSuccessfulTransformation();
	}
	
	public static String getAction() {
		return action;
	}
	public static void setAction(String action) {
		TransformationAction.action = action;
	}
	public static List<Condition> getConditions() {
		return conditions;
	}
	public static void setConditions(List<Condition> conditions) {
		TransformationAction.conditions = conditions;
	}
	public static boolean getUpdateAll() {
		return updateAll;
	}
	public static void setUpdateAll(boolean updateAll) {
		TransformationAction.updateAll = updateAll;
	}
	public static List<DSpaceObject> getDSOs() {
		return DSOs;
	}
	public static void setDSOs(List<DSpaceObject> dSOs) {
		DSOs = dSOs;
	}
	public static String getWhichDSO() {
		return whichDSO;
	}
	public static void setWhichDSO(String whichDSO) {
		TransformationAction.whichDSO = whichDSO;
	}
	
}
