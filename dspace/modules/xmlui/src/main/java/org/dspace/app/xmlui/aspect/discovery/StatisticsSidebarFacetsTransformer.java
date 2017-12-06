package org.dspace.app.xmlui.aspect.discovery;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Options;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.discovery.DiscoverFacetField;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.StatisticsDiscoverResult;
import org.dspace.discovery.StatisticsSearchServiceException;
import org.dspace.discovery.StatisticsSearchUtils;
import org.dspace.discovery.StatisticsSolrServiceImpl2;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoveryConfigurationParameters;
import org.dspace.discovery.configuration.DiscoverySearchFilterFacet;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.xml.sax.SAXException;

public class StatisticsSidebarFacetsTransformer extends SidebarFacetsTransformer{
	
	private static final Message T_FILTER_HEAD = message("xmlui.discovery.AbstractFiltersTransformer.filters.head");
    private static final Message T_VIEW_MORE = message("xmlui.discovery.AbstractFiltersTransformer.filters.view-more");
    
    private static final Logger log = Logger.getLogger(StatisticsSidebarFacetsTransformer.class);
	
	public void addOptions(Options options) throws SAXException, WingException, SQLException, IOException, AuthorizeException {

        Context.Mode originalMode = context.getCurrentMode();
        context.setMode(Context.Mode.READ_ONLY);

        Request request = ObjectModelHelper.getRequest(objectModel);

        try {
            performSearch();
        }catch (Exception e){
            log.error("Error while searching for sidebar facets", e);

            return;
        }

        if (this.queryResults != null) {
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
            java.util.List<String> fqs = Arrays.asList(DiscoveryUIUtils.getFilterQueries(request, context));

            DiscoveryConfiguration discoveryConfiguration = StatisticsSearchUtils.getDiscoveryConfiguration(dso);
            java.util.List<DiscoverySearchFilterFacet> facets = discoveryConfiguration.getSidebarFacets();

            if (facets != null && 0 < facets.size()) {

                List browse = null;

                for (DiscoverySearchFilterFacet field : facets) {
                    //Retrieve our values
                    java.util.List<DiscoverResult.FacetResult> facetValues = queryResults.getFacetResult(field.getIndexFieldName());
                    //Check if we are dealing with a date, sometimes the facet values arrive as dates !
                    if(facetValues.size() == 0 && field.getType().equals(DiscoveryConfigurationParameters.TYPE_DATE)){
                        facetValues = queryResults.getFacetResult(field.getIndexFieldName() + ".year");
                    }

                    int shownFacets = field.getFacetLimit()+1;

                    //This is needed to make sure that the date filters do not remain empty
                    if (facetValues != null && 0 < facetValues.size()) {

                        if(browse == null){
                            //Since we have a value it is safe to add the sidebar (doing it this way will ensure that we do not end up with an empty sidebar)
                            browse = options.addList("discovery");

                            browse.setHead(T_FILTER_HEAD);
                        }

                        Iterator<DiscoverResult.FacetResult> iter = facetValues.iterator();

                        List filterValsList = browse.addList(field.getIndexFieldName());

                        filterValsList.setHead(message("xmlui.ArtifactBrowser.AdvancedSearch.type_" + field.getIndexFieldName()));

                        for (int i = 0; i < shownFacets; i++) {

                            if (!iter.hasNext())
                            {
                                //When we have an hierarchical facet always show the "view more" they may want to filter the children of the top nodes
                                if(field.getType().equals(DiscoveryConfigurationParameters.TYPE_HIERARCHICAL)){
                                    addViewMoreUrl(filterValsList, dso, request, field);
                                }
                                break;
                            }

                            DiscoverResult.FacetResult value = iter.next();

                            if (i < shownFacets - 1) {
                                String displayedValue = value.getDisplayedValue();
                                String filterQuery = value.getAsFilterQuery();
                                String filterType = value.getFilterType();
                                if (fqs.contains(getSearchService().toFilterQuery(context, field.getIndexFieldName(), value.getFilterType(), value.getAsFilterQuery()).getFilterQuery())) {
                                    filterValsList.addItem(Math.random() + "", "selected").addContent(displayedValue + " (" + value.getCount() + ")");
                                } else {
                                    String paramsQuery = retrieveParameters(request);

                                    filterValsList.addItem().addXref(
                                            contextPath +
                                                    (dso == null ? "" : "/handle/" + dso.getHandle()) +
                                                    "/statistics-discover?" +
                                                    paramsQuery +
                                                    "filtertype=" + field.getIndexFieldName() +
                                                    "&filter_relational_operator="+ filterType  +
                                                    "&filter=" + encodeForURL(filterQuery),
                                            displayedValue + " (" + value.getCount() + ")"
                                    );
                                }
                            }
                            //Show a "view more" url should there be more values, unless we have a date
                            if (i == shownFacets - 1 && !field.getType().equals(DiscoveryConfigurationParameters.TYPE_DATE)/*&& facetField.getGap() == null*/) {
                                addViewMoreUrl(filterValsList, dso, request, field);
                            }
                        }
                    }
                }
            }
        }

        context.setMode(originalMode);
    }
	
	
	private void addViewMoreUrl(List facet, DSpaceObject dso, Request request, DiscoverySearchFilterFacet field) throws WingException, UnsupportedEncodingException {
        String parameters = retrieveParameters(request);
        facet.addItem().addXref(
                contextPath +
                        (dso == null ? "" : "/handle/" + dso.getHandle()) +
                        "/search-filter?" + parameters + BrowseFacet.FACET_FIELD + "=" + field.getIndexFieldName()+"&"+BrowseFacet.ORDER+"="+field.getSortOrderFilterPage(),
                T_VIEW_MORE

        );
    }
	
	/**
     * Returns the parameters used so it can be used in a url
     * @param request the cocoon request
     * @return the parameters used on this page
     */
    private String retrieveParameters(Request request) throws UnsupportedEncodingException, UIException {
        java.util.List<String> parameters = new ArrayList<String>();
        if(StringUtils.isNotBlank(request.getParameter("query"))){
            parameters.add("query=" + encodeForURL(request.getParameter("query")));
        }

        if(StringUtils.isNotBlank(request.getParameter("scope"))){
            parameters.add("scope=" + request.getParameter("scope"));
        }
        if(StringUtils.isNotBlank(request.getParameter("sort_by"))){
            parameters.add("sort_by=" + request.getParameter("sort_by"));
        }
        if(StringUtils.isNotBlank(request.getParameter(BrowseFacet.ORDER))){
            parameters.add(BrowseFacet.ORDER+"=" + request.getParameter(BrowseFacet.ORDER));
        }
        if(StringUtils.isNotBlank(request.getParameter("rpp"))){
            parameters.add("rpp=" + request.getParameter("rpp"));
        }

        Map<String, String[]> parameterFilterQueries = DiscoveryUIUtils.getParameterFilterQueries(request);
        for(String parameter : parameterFilterQueries.keySet()){
            for (int i = 0; i < parameterFilterQueries.get(parameter).length; i++) {
                String value = parameterFilterQueries.get(parameter)[i];
                parameters.add(parameter + "=" + encodeForURL(value));
            }

        }
        //Join all our parameters using an "&" sign
        String parametersString = StringUtils.join(parameters.toArray(new String[parameters.size()]), "&");
        if(StringUtils.isNotEmpty(parametersString)){
            parametersString += "&";
        }
        return parametersString;
    }
    
    protected SearchService getSearchService()
    {
        return StatisticsSearchUtils.getStatisticsSearchService();
    }
    
    
    protected DiscoveryConfiguration getDiscoveryConfiguration(DSpaceObject scope) {
		return StatisticsSearchUtils.getDiscoveryConfiguration(scope);
	}
    
    
    
    ////////////////// TESTING NUEVA IMPL		/////////////////
    /////////////////////////////////////////////////////////////
    public void performSearch() throws SearchServiceException, UIException, SQLException {
        DSpaceObject dso = getScope();
        Request request = ObjectModelHelper.getRequest(objectModel);
        //TODO crear un DiscoveryUIUtils para Statistics...	 
        queryArgs = getQueryArgs(context, dso, DiscoveryUIUtils.getFilterQueries(request, context));
        //If we are on a search page performing a search a query may be used
        String query = request.getParameter("query");
        if(query != null && !"".equals(query)){
            // Do standard escaping of some characters in this user-entered query
            query = DiscoveryUIUtils.escapeQueryChars(query);
            queryArgs.setQuery(query);
        }

        //We do not need to retrieve any dspace objects, only facets
        queryArgs.setMaxResults(0);
        ////PRUEBAAAAA
        try {
        	StatisticsDiscoverResult result = getSearchService2().search(context, dso,  queryArgs);
			//Ahora obtenemos todos para probar
//        	queryArgs.setMaxResults(100);
			result = getSearchService2().search(context, dso,  queryArgs);
		} catch (StatisticsSearchServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        queryResults =  getSearchService().search(context, dso,  queryArgs);
    }
    
    /**
     * Determine the current scope. This may be derived from the current url
     * handle if present or the scope parameter is given. If no scope is
     * specified then null is returned.
     *
     * @return The current scope.
     */
    private DSpaceObject getScope() throws SQLException {
        Request request = ObjectModelHelper.getRequest(objectModel);
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
    
    protected StatisticsSolrServiceImpl2 getSearchService2()
    {
    	return StatisticsSearchUtils.getStatisticsSearchService2();
    }

}
