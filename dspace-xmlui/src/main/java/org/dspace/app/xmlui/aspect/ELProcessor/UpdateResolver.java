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
			for(Condition con: conditions){
				Method m = updateItem.getClass().getMethod(action, Item.class ,MetadataField.class, String.class, String.class, boolean.class );
				item = (Item) TransactionManager.reload(item);
				m.invoke(updateItem, item, con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll );
				//updateItem.updateItem( item, con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll);
			}			
		}
	}
	
	private void updateCollections(List<Condition> conditions, boolean updateAll, String action) throws SQLException, AuthorizeException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		UpdateCollection updateCollection = new UpdateCollection(); 
		for(Collection coll : ResultContainer.getCollections()){
			for(Condition con: conditions){
				Method m = updateCollection.getClass().getMethod(action, Collection.class ,MetadataField.class, String.class, String.class, boolean.class );
				coll = (Collection) TransactionManager.reload(coll);
				m.invoke(updateCollection, coll, con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll );
				//updateCollection.updateCollection(coll, con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll);
			}			
		}
	}
	
	private void updateCommunities(List<Condition> conditions, boolean updateAll, String action) throws SQLException, AuthorizeException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		UpdateCommunity updateCommunity = new UpdateCommunity();
		for(Community comm : ResultContainer.getCommunities()){
			for(Condition con: conditions){
				Method m = updateCommunity.getClass().getMethod(action, Community.class ,MetadataField.class, String.class, String.class, boolean.class );
				comm = (Community) TransactionManager.reload(comm);
				m.invoke(updateCommunity, comm, con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll );
				//updateCommunity.updateCommunity(comm, con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll);
			}			
		}
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
