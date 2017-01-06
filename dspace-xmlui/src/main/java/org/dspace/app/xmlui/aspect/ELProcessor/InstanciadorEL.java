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
			//Item
			processor.defineFunction("seleccionar", "item", "org.dspace.app.xmlui.aspect.ELProcessor.SelectionAction", "selectItems");
			processor.defineFunction("transformarPrimera", "item", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "modifyFirstItems");
			processor.defineFunction("transformar", "item", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "modifyAllItems");
			processor.defineFunction("agregar", "item", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "addItemMetadata");
			processor.defineFunction("eliminar", "item", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "deleteItemMetadata");
			//collection
			processor.defineFunction("seleccionar", "coleccion", "org.dspace.app.xmlui.aspect.ELProcessor.SelectionAction", "selectCollections");
			processor.defineFunction("transformarPrimera", "coleccion", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "modifyFirstCollections");
			processor.defineFunction("transformar", "coleccion", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "modifyAllCollections");
			processor.defineFunction("agregar", "coleccion", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "addCollectionMetadata");
			processor.defineFunction("eliminar", "coleccion", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "deleteCollectionMetadata");
			//community
			processor.defineFunction("seleccionar", "comunidad", "org.dspace.app.xmlui.aspect.ELProcessor.SelectionAction", "selectCommunities");
			processor.defineFunction("transformarPrimera", "comunidad", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "modifyFirstCommunities");
			processor.defineFunction("transformar", "comunidad", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "modifyAllCommunities");
			processor.defineFunction("agregar", "comunidad", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "addCommunityMetadata");
			processor.defineFunction("eliminar", "comunidad", "org.dspace.app.xmlui.aspect.ELProcessor.TransformationAction", "deleteCommunityMetadata");
		}catch (Exception e){
			e.printStackTrace();
		}
		return processor;
	}	
}
