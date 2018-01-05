package org.dspace.app.xmlui.aspect.discovery;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.lang.StringUtils;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Cell;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Item;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Select;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.StatisticsSearchUtils;
import org.dspace.discovery.StatisticsSolrServiceImpl;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoverySearchFilter;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.xml.sax.SAXException;

public class StatisticsSimpleSearchOld extends SimpleSearch {

	private String aspectPath = "statistics-discover";
	


//	Dummy Test Facu....
//
//	public void addBody(Body body) throws SAXException, WingException,
//    SQLException, IOException, AuthorizeException {
//		
//		Request request = ObjectModelHelper.getRequest(objectModel);
//		String query = request.getParameter("query");
//		
//		StatisticsSolrServiceImpl statsSearchService = StatisticsSearchUtils.getStatisticsSearchService();
//		statsSearchService.search(context, query, 0, 100, null);
//	}
	
	@Override
	protected DiscoveryConfiguration getDiscoveryConfiguration(DSpaceObject dso) {
		return StatisticsSearchUtils.getDiscoveryConfiguration(dso);
	}
	
	 @Override
    protected String getBasicUrl() throws SQLException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

        return request.getContextPath() + (dso == null ? "" : "/handle/" + dso.getHandle()) + "/" + aspectPath;
    }
	 
	 /**
     * Query DSpace for a list of all items / collections / or communities that
     * match the given search query.
     *
     *
     * @param scope the dspace object parent
     */
    public void performSearch(DSpaceObject scope) throws UIException, SearchServiceException {

        if (queryResults != null) {
            return;
        }
        
//        this.queryResults = StatisticsSearchUtils.getStatisticsSearchService().search(context, scope, prepareQuery(scope, getQuery(), getFilterQueries()));
    }
}
