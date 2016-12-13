package org.dspace.app.xmlui.aspect.ELProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;

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
	
	public void updateItems(List<Condition> conditions, boolean updateAll, String action, List<DSpaceObject> items) throws SQLException, AuthorizeException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		UpdateItem updateItem = new UpdateItem();
		for(DSpaceObject dso : items){
			updateDSO(conditions, updateAll, action, dso, updateItem);
		}
	}
	
	public void updateCollections(List<Condition> conditions, boolean updateAll, String action, List<DSpaceObject> collections) throws SQLException, AuthorizeException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		UpdateCollection updateCollection = new UpdateCollection(); 
		for(DSpaceObject dso : collections){
			updateDSO(conditions, updateAll, action, dso, updateCollection);
		}
	}
	
	public void updateCommunities(List<Condition> conditions, boolean updateAll, String action, List<DSpaceObject> communities) throws SQLException, AuthorizeException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		UpdateCommunity updateCommunity = new UpdateCommunity();
		for(DSpaceObject dso : communities){
			updateDSO(conditions, updateAll, action, dso, updateCommunity);
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
		TransformationAction.setAction(action);
		TransformationAction.setUpdateAll(updateAll);
		TransformationAction.setConditions(conditions);
	}
	
	private void prepareItemsPreview(List<Condition> conditions, boolean updateAll, String action) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		this.prepareGenericPreview(conditions, updateAll, action);
		PreviewManager.showItemPeview(ResultContainer.getDSOs());
	}
	
	private void prepareCollectionsPreview(List<Condition> conditions, boolean updateAll, String action) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		this.prepareGenericPreview(conditions, updateAll, action);
		PreviewManager.showCollectionPeview(ResultContainer.getDSOs());
	}
	
	private void prepareCommunitiesPreview(List<Condition> conditions, boolean updateAll, String action) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		this.prepareGenericPreview(conditions, updateAll, action);
		PreviewManager.showCommunityPeview(ResultContainer.getDSOs());
	}
}
