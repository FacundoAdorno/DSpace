package org.dspace.app.xmlui.aspect.ELProcessor;

import java.sql.SQLException;

import javax.el.ELProcessor;

import org.apache.commons.collections.IteratorUtils;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;

public class InstanciadorEL {
	
	private ELProcessor processor;
	private final ItemService itemService = ContentServiceFactory.getInstance().getItemService();
	private final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
	private final CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
	

	public ELProcessor instanciar(Context context) throws SQLException {
		processor=new ELProcessor();
		
		processor.getELManager().defineBean("Items", IteratorUtils.toList(itemService.findAll(context)));
		try{
			processor.defineFunction("seleccionar", "item", "org.dspace.app.xmlui.aspect.ELProcessor.SelectionManager", "selectItems");
			processor.defineFunction("seleccionar", "coleccion", "org.dspace.app.xmlui.aspect.ELProcessor.SelectionManager", "selectCollections");
			processor.defineFunction("seleccionar", "comunidad", "org.dspace.app.xmlui.aspect.ELProcessor.SelectionManager", "selectCommunities");
			processor.defineFunction("transformar", "item", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationManager", "modifyItemsFromHandle");			
		}catch (Exception e){
			e.printStackTrace();
		}
		return processor;
	}	
}
