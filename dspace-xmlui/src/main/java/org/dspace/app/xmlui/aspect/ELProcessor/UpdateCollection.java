package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.core.Context;

public class UpdateCollection extends Update{

	protected static final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
	
	public void doUpdate( DSpaceObject coll, MetadataField metadataField, List<String> newValues) throws SQLException, AuthorizeException{
		doDelete((Collection)coll, metadataField);
		for(String newValue: newValues){			
			doAdd((Collection)coll, metadataField, newValue, "");
		}
		collectionService.update(c, (Collection)coll);
	}
	
	@Override
	public void modify(DSpaceObject coll, List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{		
		for(Condition condition: conditions){
			List<MetadataValue> mvList = collectionService.getMetadata((Collection)coll, condition.getMetadataField().getMetadataSchema().getName(), condition.getMetadataField().getElement(), condition.getMetadataField().getQualifier(), Item.ANY);
			super.update(mvList, condition.getMetadataField(), condition.getMetadataValue(), condition.getRegex(), updateAll, coll);
		}
	}
	
	@Override
	public void add(DSpaceObject coll, List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(Condition condition: conditions){
			this.doAdd((Collection)coll, condition.getMetadataField(), "es", condition.getMetadataValue());
		}
		
	}
	
	private void doAdd(Collection coll, MetadataField metadataField, String language, String newValue) throws SQLException{
		collectionService.addMetadata(c, coll, metadataField, language, newValue);
	}
	
	@Override
	public void delete(DSpaceObject coll, List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(Condition condition: conditions){
			doDelete((Collection)coll, condition.getMetadataField());
		}
	}
	
	private void doDelete(Collection coll, MetadataField metadataField) throws SQLException{
		collectionService.clearMetadata(c, coll, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
	}
	
	
}
