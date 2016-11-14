package org.dspace.app.xmlui.aspect.ELProcessor;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.util.HashUtil;
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
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.app.xmlui.wing.element.TextArea;
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

    private static java.util.List<org.dspace.content.Item> itemsResult = new ArrayList<org.dspace.content.Item>();
    private static java.util.List<org.dspace.content.Collection> collectionsResult = new ArrayList<org.dspace.content.Collection>();
    private static java.util.List<org.dspace.content.Community> communitiesResult = new ArrayList<org.dspace.content.Community>();
	
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
        for(Community com: this.communitiesResult){
        	List communities = losResultados.addList("communities");
        	Item aCommunity = communities.addItem();
        	aCommunity.addText("valor").setValue(com.getName());
        }
        for(Collection col: this.collectionsResult){
        	List collections = losResultados.addList("collections");
        	Item aCollection = collections.addItem();
        	aCollection.addText("valor").setValue(col.getName());
        }
        for(org.dspace.content.Item item: this.itemsResult){
        	List items = losResultados.addList("items");
        	Item anItem = items.addItem();
        	anItem.addText("valor").setValue(item.getName());
        }
    }

	public static java.util.List<org.dspace.content.Item> getItemsResult() {
		return itemsResult;
	}

	public static void setItemsResult(java.util.List<org.dspace.content.Item> itemsResult) {
		SelectionPage.itemsResult = itemsResult;
	}

	public static java.util.List<org.dspace.content.Collection> getCollectionsResult() {
		return collectionsResult;
	}

	public static void setCollectionsResult(java.util.List<org.dspace.content.Collection> collectionsResult) {
		SelectionPage.collectionsResult = collectionsResult;
	}

	public static java.util.List<org.dspace.content.Community> getCommunitiesResult() {
		return communitiesResult;
	}

	public static void setCommunitiesResult(java.util.List<org.dspace.content.Community> communitiesResult) {
		SelectionPage.communitiesResult = communitiesResult;
	}
    
    

}