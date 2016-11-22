package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.core.Context;

public class UpdateResolver extends Resolver {
	
	public void modifyItems(String condition, String newValues) throws Exception{
		factoryManager.getHandleResolver().getItemsFromCondition(condition);
		factoryManager.getConditionResolver().prepareItemUpdate(newValues);
	}
	
	public void modifyCollections(String condition, String newValues) throws Exception{
		factoryManager.getHandleResolver().getCollectionsFromCondition(condition);
		factoryManager.getConditionResolver().prepareCollectionUpdate(newValues);
	}
	
	public void modifyCommunities(String condition, String newValues) throws Exception{
		factoryManager.getHandleResolver().getCommunitiesFromCondition(condition);
		factoryManager.getConditionResolver().prepareCommunityUpdate(newValues);
	}
	
	public void updateItems(List<Condition> newValues) throws SQLException, AuthorizeException{
		for(Item item : ResultContainer.getItems()){
			for(Condition con: newValues){
				this.updateItem( (Item)TransactionManager.reload(item), con.getMetadataField(), con.getMetadataValue());
			}			
		}
	}
	
	public void updateCollections(List<Condition> newValues) throws SQLException, AuthorizeException{
		for(Collection coll : ResultContainer.getCollections()){
			for(Condition con: newValues){
				this.updateCollection(coll, con.getMetadataField(), con.getMetadataValue());
			}			
		}
	}
	
	public void updateCommunities(List<Condition> newValues) throws SQLException, AuthorizeException{
		for(Community comm : ResultContainer.getCommunities()){
			for(Condition con: newValues){
				this.updateCommunity(comm, con.getMetadataField(), con.getMetadataValue());
			}			
		}
	}
		
	public void updateItem(Item item, MetadataField metadataField, String newValue) throws SQLException, AuthorizeException{
		Context c = TransactionManager.getContext();
		itemService.clearMetadata(c, item, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
		itemService.addMetadata(c, item, metadataField, "es", newValue);		
		itemService.update(c, item);
	}
	
	public void updateCollection(Collection coll, MetadataField metadataField, String newValue) throws SQLException, AuthorizeException{
		Context c = TransactionManager.getContext();
		Collection collection = collectionService.find(c, coll.getID());
		collectionService.clearMetadata(c, collection, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
		collectionService.addMetadata(c, collection, metadataField, "es", newValue);		
		collectionService.update(c, collection);
	}
	
	public void updateCommunity(Community comm, MetadataField metadataField, String newValue) throws SQLException, AuthorizeException{
		Context c = TransactionManager.getContext();
		Community community = communityService.find(c, comm.getID());
		communityService.clearMetadata(c, community, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
		communityService.addMetadata(c, community, metadataField, "es", newValue);		
		communityService.update(c, community);
	}
	
	
//	private static void updateItemsFromCollection(Collection collection, MetadataField metadataField, String newValue) throws SQLException, AuthorizeException{
//		Iterator<Item> iterator=itemService.findAllByCollection(MainProcessor.getContext(), collection);		
//		while(iterator.hasNext()){
//			Item item = iterator.next();
//			updateItem(item, metadataField, newValue);
//		}
//	}
//	
//	private static void updateItemsFromCommunity(Community community, MetadataField metadataField, String newValue) throws SQLException, AuthorizeException{
//		for(Community com: community.getSubcommunities()){
//			updateItemsFromCommunity(com, metadataField, newValue);
//		}
//		for(Collection col: community.getCollections()){
//			updateItemsFromCollection(col, metadataField, newValue);
//		}
//	}
		
}
