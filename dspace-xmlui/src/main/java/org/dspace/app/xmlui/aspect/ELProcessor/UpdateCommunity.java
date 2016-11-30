package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Context;

public class UpdateCommunity extends Update{
	
	protected static final CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
	
	@Override
	public void doUpdate( DSpaceObject comm, MetadataField metadataField, List<String> newValues) throws SQLException, AuthorizeException{
		doDelete((Community)comm, metadataField);
		for(String newValue: newValues){
			doAdd((Community)comm, metadataField, newValue, "");
		}
		communityService.update(c, (Community)comm);
	}
	
	@Override
	public void modify(DSpaceObject comm, List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(Condition condition: conditions){
			List<MetadataValue> mvList = getMetadataValueList(comm, condition.getMetadataField());
			super.update(mvList, condition.getMetadataField(), condition.getMetadataValue(), condition.getRegex(), updateAll, comm, false);
		}		
	}
	
	@Override
	public void add(DSpaceObject comm, List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(Condition condition: conditions){
			this.doAdd((Community)comm, condition.getMetadataField(), "es", condition.getMetadataValue());
		}
	}
	
	private void doAdd( Community comm, MetadataField metadataField, String language, String newValue) throws SQLException{
		communityService.addMetadata(c, comm, metadataField, language, newValue);
	}
	
	@Override
	public void delete(DSpaceObject comm, List<Condition> conditions, boolean updateAll) throws SQLException, AuthorizeException{
		for(Condition condition: conditions){
			doDelete((Community)comm, condition.getMetadataField());
		}
	}
	
	@Override
	protected List<MetadataValue> getMetadataValueList(DSpaceObject dso, MetadataField metadataField){
		return communityService.getMetadata((Community)dso, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
	}
	
	private void doDelete(Community comm, MetadataField metadataField) throws SQLException{
		communityService.clearMetadata(c, comm, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
	}

}
