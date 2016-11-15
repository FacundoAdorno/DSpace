package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.DSpaceObject;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;

/**
 * @author nico
 *
 */
public class Manager {

	protected static final ItemService itemService = ContentServiceFactory.getInstance().getItemService();
	protected static final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
	protected static final CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
	protected FactoryManager factoryManager = new FactoryManager();	

}
