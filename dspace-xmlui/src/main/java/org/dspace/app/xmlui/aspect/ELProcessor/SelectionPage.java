package org.dspace.app.xmlui.aspect.ELProcessor;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Item;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.xml.sax.SAXException;

public class SelectionPage extends AbstractDSpaceTransformer implements CacheableProcessingComponent {
    
    /** language strings */
    private static final Message T_title =
        message("xmlui.ArtifactBrowser.Contact.title");
    
    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");
    
    private static final Message T_trail = 
        message("xmlui.ArtifactBrowser.Contact.trail");
    
    private static final Message T_head = 
        message("xmlui.ArtifactBrowser.Contact.head");
    
    private static final Message T_para1 =
        message("xmlui.ArtifactBrowser.Contact.para1");

    private static java.util.List<org.dspace.content.Item> itemsResult;
    private static java.util.List<org.dspace.content.Collection> collectionsResult;
    private static java.util.List<org.dspace.content.Community> communitiesResult;
	
    private static final ConfigurationService configurationService =DSpaceServicesFactory.getInstance().getConfigurationService();
    
    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     */
    public Serializable getKey() 
    {
       return "1";
    }

    /**
     * Generate the cache validity object.
     */
    public SourceValidity getValidity() 
    {
        return NOPValidity.SHARED_INSTANCE;
    }
    
    
	@Override
    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {
        pageMeta.addMetadata("title").addContent(T_title);
        
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        pageMeta.addTrail().addContent(T_trail);
    }

    @Override
    public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException, ProcessingException {
    	Division contact = body.addDivision("contact","primary");
        
        contact.setHead(T_head);
        
        String name = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.name");
        contact.addPara(T_para1.parameterize(name));
        
        String[] options=(configurationService.getProperty("el.processor.options", null).split(","));
        
        List list = contact.addList("options");
        
        for(String option:options){
        	String description = configurationService.getProperty("el.processor.description."+option);
        	Item oneItem= list.addItem();
        	oneItem.addText("description").setValue(description);
        	oneItem.addText("identifier").setValue(option);
        }
        
        Division resultado=contact.addDivision("Resultado");
        List losResultados=resultado.addList("resultatos");
        if(!getCommunitiesResult().isEmpty()){
        	List communities = losResultados.addList("communities");
        	for(Community comm: getCommunitiesResult()){            	
            	addDSOResult(communities, comm);
            }
        }
        if(!getCollectionsResult().isEmpty()){
        	List collections = losResultados.addList("collections");
        	for(Collection coll: getCollectionsResult()){
            	addDSOResult(collections, coll);
            }
        }
        if(!getItemsResult().isEmpty()){
        	List items = losResultados.addList("items");
        	for(org.dspace.content.Item item: getItemsResult()){
            	addDSOResult(items, item);
            }
        }
        
        SelectionPage.cleanVariables();
    }
    
    private void addDSOResult(List items,DSpaceObject dso) throws WingException{
    	Item anItem = items.addItem();
    	anItem.addText("handle").setValue(dso.getHandle());
    	anItem.addText("name").setValue(dso.getName());
    }
    
    public static void cleanVariables(){
    	SelectionPage.setCollectionsResult(new java.util.ArrayList<org.dspace.content.Collection>());
    	SelectionPage.setCommunitiesResult(new java.util.ArrayList<org.dspace.content.Community>());
    	SelectionPage.setItemsResult(new java.util.ArrayList<org.dspace.content.Item>());
    }

	public static java.util.List<org.dspace.content.Item> getItemsResult() {
		if(SelectionPage.itemsResult == null){
			return new java.util.ArrayList<org.dspace.content.Item>();
		}
		return SelectionPage.itemsResult;
	}

	public static void setItemsResult(java.util.List<org.dspace.content.Item> itemsResult) {
		SelectionPage.itemsResult = itemsResult;
	}

	public static java.util.List<org.dspace.content.Collection> getCollectionsResult() {
		if(SelectionPage.collectionsResult == null){
			return new java.util.ArrayList<org.dspace.content.Collection>();
		}
		return SelectionPage.collectionsResult;
	}

	public static void setCollectionsResult(java.util.List<org.dspace.content.Collection> collectionsResult) {
		SelectionPage.collectionsResult = collectionsResult;
	}

	public static java.util.List<org.dspace.content.Community> getCommunitiesResult() {
		if(SelectionPage.communitiesResult == null){
			return new java.util.ArrayList<org.dspace.content.Community>();
		}
		return SelectionPage.communitiesResult;
	}

	public static void setCommunitiesResult(java.util.List<org.dspace.content.Community> communitiesResult) {
		SelectionPage.communitiesResult = communitiesResult;
	}

}