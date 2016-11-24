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
	
	public void doUpdate(Context c, DSpaceObject comm, MetadataField metadataField, List<String> newValues) throws SQLException, AuthorizeException{
		Community community = (Community)TransactionManager.reload(comm);
		communityService.clearMetadata(c, (Community)community, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
		for(String newValue: newValues){
			communityService.addMetadata(c, (Community)community, metadataField, "es", newValue);
		}
		communityService.update(c, (Community)community);
	}
	
	public void updateCommunity(Community community, MetadataField metadataField, String newValue, String regex, boolean updateAll) throws SQLException, AuthorizeException{
		List<MetadataValue> mvList = communityService.getMetadata(community, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
		update(mvList, metadataField, newValue, regex, updateAll, community );
	}

}
