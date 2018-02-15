package org.dspace.app.xmlui.aspect.discovery;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.AbstractReader;
import org.apache.excalibur.source.SourceValidity;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.xml.sax.SAXException;

/**
 * Este reader retorna en formato de texto plano <b><i>todos los UUIDs</i></b> de la consulta discovery que viene como parámetro (de la forma handle/xxx/xxx/discover?query ; /discover?query&scope ; ó /discover?query).
 * @author facundo
 *
 */
public class DiscoveryUUIDsInSearch extends AbstractReader implements CacheableProcessingComponent {

	private static final Logger log = Logger.getLogger(DiscoveryUUIDsInSearch.class);

    /**
     * Cached query results
     */
    protected DiscoverResult queryResults;

    /**
     * Cached query arguments
     */
    protected DiscoverQuery queryArgs;
    
    /** The Cocoon request */
    protected Request request;
    
    protected HandleService handleService = HandleServiceFactory.getInstance().getHandleService();
	
    protected Context context;
    
	@Override
	public Serializable getKey() {
		// TODO Completar correctamente
		return null;
	}

	@Override
	public SourceValidity getValidity() {
		// TODO Completar correctamente
		return null;
	}

	@Override
	public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
			throws ProcessingException, SAXException, IOException {
		super.setup(resolver, objectModel, src, par);
		try {
			this.context = ContextUtil.obtainContext(objectModel);
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public void generate() throws IOException, SAXException, ProcessingException {
		try {
			performSearch();
			if(queryResults != null || queryResults.getTotalSearchResults() > 0) {
				StringBuffer sb = new StringBuffer();
				for (Iterator<DSpaceObject> dsoIt = queryResults.getDspaceObjects().iterator(); dsoIt.hasNext();) {
					DSpaceObject dso = dsoIt.next();
					sb.append(dso.getID().toString());
					if(dsoIt.hasNext()) {
						sb.append(",");
					}
				}
				byte[] uuids = String.valueOf(sb).getBytes();
				out.write(uuids);
				out.flush();
			}
		} catch (UIException | SearchServiceException | SQLException e) {
			log.error("Hubo un error mientras se obtenían los UUIDS correspondiente a los DSO de la siguiente consulta en Discovery: \"" + request.getQueryString() + "\".");
			throw new IOException(e);
		}
		
	}
	
	 /**
     * Determine the current scope. This may be derived from the current url
     * handle if present or the scope parameter is given. If no scope is
     * specified then null is returned.
     *
     * @return The current scope.
     */
    private DSpaceObject getScope() throws SQLException {
        request = ObjectModelHelper.getRequest(objectModel);
        String scopeString = request.getParameter("scope");

        // Are we in a community or collection?
        DSpaceObject dso;
        if (scopeString == null || "".equals(scopeString))
        {
            // get the search scope from the url handle
            dso = HandleUtil.obtainHandle(objectModel);
        }
        else
        {
            // Get the search scope from the location parameter
            dso = handleService.resolveToObject(context, scopeString);
        }

        return dso;
    }
    
    public void performSearch() throws SearchServiceException, UIException, SQLException {
        DSpaceObject dso = getScope();
        Request request = ObjectModelHelper.getRequest(objectModel);
        queryArgs = getQueryArgs(context, dso, DiscoveryUIUtils.getFilterQueries(request, context));
        //If we are on a search page performing a search a query may be used
        String query = request.getParameter("query");
        if(query != null && !"".equals(query)){
            // Do standard escaping of some characters in this user-entered query
            query = DiscoveryUIUtils.escapeQueryChars(query);
            queryArgs.setQuery(query);
        }

        //Seteamos un valor muy alto de resultado a obtener, ya que nos interesan todos los resultados...
        queryArgs.setMaxResults(999999);
        queryResults =  getSearchService().search(context, dso,  queryArgs);
    }
    
    public DiscoverQuery getQueryArgs(Context context, DSpaceObject scope, String... filterQueries) {
    	DiscoverQuery queryArgs = new DiscoverQuery();
    	DiscoveryConfiguration discoveryConfiguration = getDiscoveryConfiguration(scope);
    	//Add the default filters
        queryArgs.addFilterQueries(discoveryConfiguration.getDefaultFilterQueries().toArray(new String[discoveryConfiguration.getDefaultFilterQueries().size()]));
        queryArgs.addFilterQueries(filterQueries);
    	
        return queryArgs;
    }
    
    
    protected SearchService getSearchService()
    {
        return DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName(SearchService.class.getName(),SearchService.class);
    }
    
    protected DiscoveryConfiguration getDiscoveryConfiguration(DSpaceObject scope) {
		return SearchUtils.getDiscoveryConfiguration(scope);
	}

}
