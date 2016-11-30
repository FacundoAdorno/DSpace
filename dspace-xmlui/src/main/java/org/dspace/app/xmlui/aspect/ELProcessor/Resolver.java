package org.dspace.app.xmlui.aspect.ELProcessor;

import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;

/**
 * @author nico
 *
 */
abstract class Resolver {

	protected static final ItemService itemService = ContentServiceFactory.getInstance().getItemService();
	protected static final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
	protected static final CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
	protected ResolverFactory resolverFactory = new ResolverFactory();
}
