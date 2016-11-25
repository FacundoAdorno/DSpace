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
		delete((Collection)coll, metadataField, "", "", false);
		for(String newValue: newValues){			
			add((Collection)coll, metadataField, newValue, "", false);
		}
		collectionService.update(c, (Collection)coll);
	}
	
	public void modify(Collection coll, MetadataField metadataField, String newValue, String regex, boolean updateAll) throws SQLException, AuthorizeException{
		List<MetadataValue> mvList = collectionService.getMetadata(coll, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
		super.update(mvList, metadataField, newValue, regex, updateAll, coll);
	}
	
	public void add(Collection coll, MetadataField metadataField, String newValue, String regex, boolean updateAll) throws SQLException, AuthorizeException{
		collectionService.addMetadata(c, (Collection)coll, metadataField, "es", newValue);
	}
	
	public void delete(Collection coll, MetadataField metadataField, String newValue, String regex, boolean updateAll) throws SQLException, AuthorizeException{
		collectionService.clearMetadata(c, (Collection)coll, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
	}
	
	
}
