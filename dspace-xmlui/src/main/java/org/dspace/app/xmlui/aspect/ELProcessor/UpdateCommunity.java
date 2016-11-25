package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;
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
	
	public void doUpdate( DSpaceObject comm, MetadataField metadataField, List<String> newValues) throws SQLException, AuthorizeException{
		delete((Community)comm, metadataField, "", "", false);
		for(String newValue: newValues){
			add((Community)comm, metadataField, newValue, "", false);
		}
		communityService.update(c, (Community)comm);
	}
	
	public void modify(Community comm, MetadataField metadataField, String newValue, String regex, boolean updateAll) throws SQLException, AuthorizeException{
		List<MetadataValue> mvList = communityService.getMetadata(comm, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
		super.update(mvList, metadataField, newValue, regex, updateAll, comm);
	}
	
	public void add(Community comm, MetadataField metadataField, String newValue, String regex, boolean updateAll) throws SQLException, AuthorizeException{
		communityService.addMetadata(c, (Community)comm, metadataField, "es", newValue);
	}
	
	public void delete(Community comm, MetadataField metadataField, String newValue, String regex, boolean updateAll) throws SQLException, AuthorizeException{
		communityService.clearMetadata(c, (Community)comm, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
	}

}
