package org.dspace.app.xmlui.aspect.ELProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.core.Context;

public class UpdateResolver extends Resolver {
	
	public void modifyItems(String condition, String newValues, boolean updateAll, String action) throws Exception{
		resolverFactory.getHandleResolver().getItemsFromCondition(condition);
		List<Condition> conditions = prepareConditions(newValues, action);
		this.prepareItemsPreview(conditions, updateAll, action);
		//updateItems(conditions, updateAll, action);
	}
	
	public void modifyCollections(String condition, String newValues, boolean updateAll, String action) throws Exception{
		resolverFactory.getHandleResolver().getCollectionsFromCondition(condition);
		List<Condition> conditions = prepareConditions(newValues, action);
		this.prepareCollectionsPreview(conditions, updateAll, action);
		//updateCollections(conditions, updateAll, action);
	}
	
	public void modifyCommunities(String condition, String newValues, boolean updateAll, String action) throws Exception{
		resolverFactory.getHandleResolver().getCommunitiesFromCondition(condition);
		List<Condition> conditions = prepareConditions(newValues, action);
		this.prepareCommunitiesPreview(conditions, updateAll, action);
		//updateCommunities(conditions, updateAll, action);
	}
	
	public void executeUpdate() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, AuthorizeException{
		PreviewManager.executeUpdate(this);
	}
	
	public void updateItems(List<Condition> conditions, boolean updateAll, String action, List<Item> items) throws SQLException, AuthorizeException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		UpdateItem updateItem = new UpdateItem();
		for(Item item : items){
			updateDSO(conditions, updateAll, action, item, updateItem);
		}
	}
	
	public void updateCollections(List<Condition> conditions, boolean updateAll, String action, List<Collection> collections) throws SQLException, AuthorizeException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		UpdateCollection updateCollection = new UpdateCollection(); 
		for(Collection coll : collections){
			updateDSO(conditions, updateAll, action, coll, updateCollection);
		}
	}
	
	public void updateCommunities(List<Condition> conditions, boolean updateAll, String action, List<Community> communities) throws SQLException, AuthorizeException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		UpdateCommunity updateCommunity = new UpdateCommunity();
		for(Community comm : communities){
			updateDSO(conditions, updateAll, action, comm, updateCommunity);
		}
	}
	
	private void updateDSO(List<Condition> conditions, boolean updateAll, String action, DSpaceObject dso, Update update) throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Method m = update.getClass().getMethod(action, DSpaceObject.class, List.class, boolean.class );
		dso = TransactionManager.reload(dso);
		m.invoke(update, dso, conditions, updateAll);
	}
	
	private List<Condition> prepareConditions(String newValues, String action) throws Exception{
		if(action == "delete"){
			return resolverFactory.getConditionDeleteResolver().prepareUpdate(newValues);
		}
		else{
			return resolverFactory.getConditionAddModifyResolver().prepareUpdate(newValues);
		}
	}
	
	private void prepareGenericPreview(List<Condition> conditions, boolean updateAll, String action){
		PreviewManager.setAction(action);
		PreviewManager.setUpdateAll(updateAll);
		PreviewManager.setConditions(conditions);
	}
	
	private void prepareItemsPreview(List<Condition> conditions, boolean updateAll, String action){
		this.prepareGenericPreview(conditions, updateAll, action);
		PreviewManager.setItems(ResultContainer.getItems());
	}
	
	private void prepareCollectionsPreview(List<Condition> conditions, boolean updateAll, String action){
		this.prepareGenericPreview(conditions, updateAll, action);
		PreviewManager.setCollections(ResultContainer.getCollections());
	}
	
	private void prepareCommunitiesPreview(List<Condition> conditions, boolean updateAll, String action){
		this.prepareGenericPreview(conditions, updateAll, action);
		PreviewManager.setCommunities(ResultContainer.getCommunities());
	}
}
