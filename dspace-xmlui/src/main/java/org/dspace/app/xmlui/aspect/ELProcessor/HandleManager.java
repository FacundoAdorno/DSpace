package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;

public class HandleManager extends Manager{

	protected static final HandleService handleService = HandleServiceFactory.getInstance().getHandleService();
	protected List<DSpaceObject> result= new ArrayList();
	protected DSpaceObject dso = null;	
	
	/**
	 * Find the corresponding DSO for the handle, and sets the dso variable 
	 * 
	 * @param handle
	 * @throws Exception
	 */
	public void handleResolver(String handleCondition) throws Exception{
		String handle = handleCondition.split("=")[1].trim();
		try{
			dso=handleService.resolveToObject(MainProcessor.getContext(), handle);
		}
		catch(Exception e){
			throw new Exception("The're is no DSO with the handle:" +handle);
		}
	}
	
	public void getItemsFromCondition(String condition) throws Exception{
		
		condition = this.identifyHandle(condition);
		List<UUID> uuids = new ArrayList<UUID>();
		
		if(dso instanceof Item){
			//quieren un item y me pasaron un handle de item, devuelvo el item
			ResultContainer.addItem((Item) dso);
			return;
		}else if (dso instanceof Collection){
			uuids.add(((Collection) dso).getID());			
		}else if (dso instanceof Community){
			getCollectionsFromCommunity((Community)dso);
			for(DSpaceObject col: result){
				uuids.add(((Collection) col).getID());
			}
		}
		factoryManager.getConditionManager().getItemsFromCondition(condition, uuids);
		
	}
	
	public void getCollectionsFromCondition(String condition) throws Exception{
		
		condition = this.identifyHandle(condition);
		List<UUID> uuids = new ArrayList<UUID>();
		
		if(dso instanceof Item){
			throw new Exception("The handle is from an Item");
		}else if(dso instanceof Collection){
			//quieren una coleccion y me pasaron un handle de una, devuelvo la coleccion
			ResultContainer.addCollection((Collection) dso);
			return;
		}else if(dso instanceof Community){
			getCollectionsFromCommunity((Community)dso);
			ResultContainer.addCollections((List<Collection>)(List) result);
		}
		
		factoryManager.getConditionManager().getCollectionsFromCondition(condition);
	}
	
	public void getCommunitiesFromCondition(String condition) throws Exception{
		
		condition = this.identifyHandle(condition);
		List<UUID> uuids = new ArrayList<UUID>();
		
		if(dso instanceof Item){
			throw new Exception("The handle is from an Item");
		}else if(dso instanceof Collection){
			throw new Exception("The handle is from a Collection");
		}else if(dso instanceof Community){
			ResultContainer.addCommunity((Community)dso);
		}
		
		factoryManager.getConditionManager().getCommunitiesFromCondition(condition);
	}

	private void getCollectionsFromCommunity(Community com){
		for(Community community: com.getSubcommunities()){
			getCollectionsFromCommunity(community);
		}
		result.addAll(com.getCollections());
	}
	
	private String identifyHandle(String condition) throws Exception{
		if(condition.contains("handle")){
			for(String oneCondition: condition.split(",")){
				if(oneCondition.contains("handle")){
					this.handleResolver(oneCondition.trim());
					return condition.replaceAll(oneCondition,"");
				}
			}
		}
		return condition;
	}
	
}
