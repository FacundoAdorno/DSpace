package org.dspace.app.xmlui.aspect.ELProcessor;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
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
	
    private static final ConfigurationService configurationService =DSpaceServicesFactory.getInstance().getConfigurationService();
    private static final String itemSelection = "Item Selection";
    private static final String collectionSelection = "Collection Selection";
    private static final String communitySelection = "Community Selection";
    private static final String itemPreview = "Item Preview";
    private static final String collectionPreview = "Collection Preview";
    private static final String communityPreview = "Community Preview";
    private static final String noMessage = "";
    private static final String noResult = "No hay resultados para la consulta";
    private static final String successfulTransformation = "La transformacion se realizo con exito!";
    
    private static String message = noMessage;
    
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
        
        Division noResult = contact.addDivision("Message");
    	noResult.addPara(message);
    	
        try{
        	java.util.List<DSpaceObjectPreview> previews = ResultContainer.getResultsToShow();
        	List previewList = contact.addList("preview");
        	for(DSpaceObjectPreview preview: previews){
        		Item anItem = previewList.addItem();
        		anItem.addText("handle").setValue(preview.getHandle());
        		anItem.addText("metadata").setValue(preview.getMetadataName());
        		anItem.addText("Current Value").setValue(preview.getOldValue());
        		anItem.addText("New Value").setValue(preview.getNewValue());
            }
        }
        catch(Exception e){
        	//mensaje de error ?
        }
        ResultContainer.cleanPreviewResult();
    }
    
    public static void showNoMessage(){
    	message = noMessage;
    }
    
    public static void showItemSelectionMessage(){
    	message = itemSelection;
    }
    
    public static void showCollectionSelectionMessage(){
    	message = collectionSelection;
    }
    
    public static void showCommunitySelectionMessage(){
    	message = communitySelection;
    }
    
    public static void showItemPreviewMessage(){
    	message = itemPreview;
    }
    
    public static void showCollectionPreviewMessage(){
    	message = collectionPreview;
    }
    
    public static void showCommunityPreviewMessage(){
    	message = communityPreview;
    }
    
    public static void showSuccessfulTransformation(){
    	message = successfulTransformation;
    }
    
    public static void showNoResult(){
    	message = noResult;
    }


}