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
	
	private List<Condition> prepareItemConditions(String condition, String newValues, String action) throws Exception{
		resolverFactory.getHandleResolver().getItemsFromCondition(condition);
		return prepareConditions(newValues, action);
	}
	
	public void modifyItems(String condition, String newValues, boolean updateAll, String action) throws Exception{
		List<Condition> conditions = this.prepareItemConditions(condition, newValues, action);
		updateItems(conditions, updateAll, action);
	}
	
	private List<Condition> prepareCollectionConditions(String condition, String newValues, String action) throws Exception{
		resolverFactory.getHandleResolver().getCollectionsFromCondition(condition);
		return prepareConditions(newValues, action);
	}
	
	public void modifyCollections(String condition, String newValues, boolean updateAll, String action) throws Exception{
		List<Condition> conditions = prepareCollectionConditions(condition, newValues, action);
		updateCollections(conditions, updateAll, action);
	}
	
	private List<Condition> prepareCommunityConditions(String condition, String newValues, String action) throws Exception{
		resolverFactory.getHandleResolver().getCommunitiesFromCondition(condition);
		return prepareConditions(newValues, action);
	}
	
	public void modifyCommunities(String condition, String newValues, boolean updateAll, String action) throws Exception{
		List<Condition> conditions = prepareCommunityConditions(condition, newValues, action);
		updateCommunities(conditions, updateAll, action);
	}
	
	private void updateItems(List<Condition> conditions, boolean updateAll, String action) throws SQLException, AuthorizeException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		UpdateItem updateItem = new UpdateItem();
		for(Item item : ResultContainer.getItems()){
			updateDSO(conditions, updateAll, action, item, updateItem);
			//updateItem.updateItem( item, con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll);
		}
	}
	
	private void updateCollections(List<Condition> conditions, boolean updateAll, String action) throws SQLException, AuthorizeException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		UpdateCollection updateCollection = new UpdateCollection(); 
		for(Collection coll : ResultContainer.getCollections()){
			updateDSO(conditions, updateAll, action, coll, updateCollection);
			//updateCollection.updateCollection(coll, con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll);
		}
	}
	
	private void updateCommunities(List<Condition> conditions, boolean updateAll, String action) throws SQLException, AuthorizeException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		UpdateCommunity updateCommunity = new UpdateCommunity();
		for(Community comm : ResultContainer.getCommunities()){
			updateDSO(conditions, updateAll, action, comm, updateCommunity);
			//updateCommunity.updateCommunity(comm, con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll);
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
}
