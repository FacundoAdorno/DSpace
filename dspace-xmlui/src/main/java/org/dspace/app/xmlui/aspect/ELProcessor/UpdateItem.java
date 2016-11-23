package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;

public class UpdateItem extends Update{

	protected static final ItemService itemService = ContentServiceFactory.getInstance().getItemService();
	
	@Override
	public void doUpdate(Context c, DSpaceObject item, MetadataField metadataField, List<String> newValues) throws SQLException, AuthorizeException{
		item = TransactionManager.reload(item);
		itemService.clearMetadata(c, (Item)item, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
		for(String newValue : newValues){
			itemService.addMetadata(c, (Item)item, metadataField, "es", newValue);		
			itemService.update(c, (Item)item);
		}		
	}
	
	public void updateItem(Item item, MetadataField metadataField, String newValue, String regex, boolean updateAll) throws SQLException, AuthorizeException{
		List<MetadataValue> mvList = itemService.getMetadata(item, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
		update(mvList, metadataField, newValue, regex, updateAll, item );
	}
	
	
}
