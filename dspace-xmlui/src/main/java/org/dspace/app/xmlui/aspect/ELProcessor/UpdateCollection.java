package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.core.Context;

public class UpdateCollection extends Update{

	protected static final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
	
	@Override
	public void doUpdate(Context c, DSpaceObject coll, MetadataField metadataField, String newValue) throws SQLException, AuthorizeException{
		Collection collection = collectionService.find(c, coll.getID());
		collectionService.clearMetadata(c, (Collection)collection, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
		collectionService.addMetadata(c, (Collection)collection, metadataField, "es", newValue);		
		collectionService.update(c, (Collection)collection);
	}
	
	public void updateCollection(Collection collection, MetadataField metadataField, String newValue, String regex, boolean updateAll) throws SQLException, AuthorizeException{
		List<MetadataValue> mvList = collectionService.getMetadata(collection, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
		update(mvList, metadataField, newValue, regex, updateAll, collection );
	}
	
	
}
