package org.dspace.app.xmlui.aspect.ELProcessor;

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
	
	private List<Condition> modifyItems(String condition, String newValues) throws Exception{
		factoryManager.getHandleResolver().getItemsFromCondition(condition);
		return factoryManager.getConditionUpdateResolver().prepareUpdate(newValues);
	}
	
	public void modifyFirstItems(String condition, String newValues) throws Exception{
		List<Condition> conditions = this.modifyItems(condition, newValues);
		updateItems(conditions, false);
	}
	
	public void modifyAllItems(String condition, String newValues) throws Exception{
		List<Condition> conditions = this.modifyItems(condition, newValues);
		updateItems(conditions, true);
	}
	
	public List<Condition> modifyCollections(String condition, String newValues) throws Exception{
		factoryManager.getHandleResolver().getCollectionsFromCondition(condition);
		return factoryManager.getConditionUpdateResolver().prepareUpdate(newValues);
	}
	
	public void modifyAllCollections(String condition, String newValues) throws Exception{
		List<Condition> conditions = modifyCollections(condition, newValues);
		updateCollections(conditions, true);
	}
	
	public void modifyFirstCollections(String condition, String newValues) throws Exception{
		List<Condition> conditions = modifyCollections(condition, newValues);
		updateCollections(conditions, false);
	}
	
	public List<Condition> modifyCommunities(String condition, String newValues) throws Exception{
		factoryManager.getHandleResolver().getCommunitiesFromCondition(condition);
		return factoryManager.getConditionUpdateResolver().prepareUpdate(newValues);
	}
	
	public void modifyAllCommunities(String condition, String newValues) throws Exception{
		List<Condition> conditions = modifyCommunities(condition, newValues);
		updateCommunities(conditions, true);
	}
	
	public void modifyFirstCommunities(String condition, String newValues) throws Exception{
		List<Condition> conditions = modifyCommunities(condition, newValues);
		updateCommunities(conditions, false);
	}
	
	private void updateItems(List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(Item item : ResultContainer.getItems()){
			for(Condition con: conditions){
				new UpdateItem().updateItem( (Item)TransactionManager.reload(item), con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll);
			}			
		}
	}
	
	public void updateCollections(List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(Collection coll : ResultContainer.getCollections()){
			for(Condition con: conditions){
				new UpdateCollection().updateCollection(coll, con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll);
			}			
		}
	}
	
	public void updateCommunities(List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(Community comm : ResultContainer.getCommunities()){
			for(Condition con: conditions){
				new UpdateCommunity().updateCommunity(comm, con.getMetadataField(), con.getMetadataValue(), con.getRegex(), updateAll);
			}			
		}
	}
}
