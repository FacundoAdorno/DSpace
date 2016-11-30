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
	public void doUpdate(DSpaceObject item, MetadataField metadataField, List<String> newValues) throws SQLException, AuthorizeException{	
		doDelete((Item)item, metadataField);
		for(String newValue : newValues){
			doAdd((Item)item, metadataField, newValue, "");
		}	
		itemService.update(c, (Item)item);
	}
	
	@Override
	public void modify(DSpaceObject item, List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(Condition condition: conditions){
			List<MetadataValue> mvList = itemService.getMetadata((Item)item, condition.getMetadataField().getMetadataSchema().getName(), condition.getMetadataField().getElement(), condition.getMetadataField().getQualifier(), Item.ANY);
			super.update(mvList, condition.getMetadataField(), condition.getMetadataValue(), condition.getRegex(), updateAll, item);
		}
		
	}
	
	@Override
	public void add(DSpaceObject item, List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(Condition condition: conditions){
			this.doAdd((Item)item, condition.getMetadataField(), "es", condition.getMetadataValue());
		}		
	}
	
	private void doAdd( Item item, MetadataField metadataField, String language, String newValue) throws SQLException{
		itemService.addMetadata(c, item, metadataField, language, newValue);
	}
	
	@Override
	public void delete(DSpaceObject item, List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(Condition condition: conditions){
			doDelete((Item)item, condition.getMetadataField());
		}		
	}
	
	private void doDelete(Item item, MetadataField metadataField) throws SQLException{
		itemService.clearMetadata(c, (Item)item, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
	}
	
}
