package org.dspace.app.xmlui.aspect.discovery;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.HashUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.SourceValidity;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.DSpaceValidity;
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
import org.dspace.discovery.DiscoveryDateFacetField;
import org.dspace.discovery.GenericDiscoverResult.SearchDocument;
import org.dspace.discovery.StatisticsDiscoverResult;
import org.dspace.discovery.StatisticsSearchServiceException;
import org.dspace.discovery.StatisticsSearchUtils;
import org.dspace.discovery.StatisticsSolrServiceImpl;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoveryConfigurationParameters;
import org.dspace.discovery.configuration.DiscoverySearchFilterFacet;
import org.dspace.discovery.configuration.StatisticsDiscoveryCombinedFilterFacet;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.joda.time.DateTime;
import org.xml.sax.SAXException;

public class StatisticsSidebarFacetsTransformer extends AbstractDSpaceTransformer implements CacheableProcessingComponent{
	
    private static final Logger log = Logger.getLogger(StatisticsSidebarFacetsTransformer.class);
    
    /**
     * Cached query results
     */
    protected StatisticsDiscoverResult queryResults;

    /**
     * Cached query arguments
     */
    protected DiscoverQuery queryArgs;

    /**
     * Cached validity object
     */
    protected SourceValidity validity;
    
    
    private static final Message T_FILTER_HEAD = message("xmlui.discovery.AbstractFiltersTransformer.filters.head");
    private static final Message T_VIEW_MORE = message("xmlui.discovery.AbstractFiltersTransformer.filters.view-more");

    protected HandleService handleService = HandleServiceFactory.getInstance().getHandleService();

    protected StatisticsSolrServiceImpl getSearchService()
    {
        return StatisticsSearchUtils.getStatisticsSearchService();
    }

    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     * @return the key.
     */
    @Override
    public Serializable getKey() {
        try {
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
            if (dso != null)
            {
                return HashUtil.hash(dso.getHandle());
            }else{
                return "0";
            }
        }
        catch (SQLException sqle) {
            // Ignore all errors and just return that the component is not
            // cachable.
            return "0";
        }
    }

    /**
     * Generate the cache validity object.
     * <p>
     * The validity object will include the collection being viewed and
     * all recently submitted items. This does not include the community / collection
     * hierarchy, when this changes they will not be reflected in the cache.
     * @return validity.
     */
    @Override
    public SourceValidity getValidity() {
        if (this.validity == null) {

            try {
                Context.Mode originalMode = context.getCurrentMode();
                context.setMode(Context.Mode.READ_ONLY);

                DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
                DSpaceValidity val = new DSpaceValidity();

                // Retrieve any facet results to add to the validity key
                performSearch();

                // Add the actual collection;
                if (dso != null)
                {
                    val.add(context, dso);
                }

                for (String facetField : queryResults.getFacetResults().keySet()) {
                    val.add(facetField);

                    java.util.List<StatisticsDiscoverResult.FacetResult> facetValues = queryResults.getFacetResults().get(facetField);
                    for (StatisticsDiscoverResult.FacetResult facetValue : facetValues) {
                        val.add(facetField + facetValue.getAsFilterQuery() + facetValue.getCount());
                    }
                }

                this.validity = val.complete();
                context.setMode(originalMode);
            }
            catch (Exception e) {
                log.error(e.getMessage(),e);
            }
            //TODO: dependent on tags as well :)
        }
        return this.validity;
    }


     public void performSearch() throws UIException, SQLException, StatisticsSearchServiceException {
        DSpaceObject dso = getScope();
        Request request = ObjectModelHelper.getRequest(objectModel);
        //TODO crear un DiscoveryUIUtils para Statistics...	 
        queryArgs = getQueryArgs(context, dso, StatisticsDiscoveryUIUtils.getFilterQueries(request, context,dso));
        //If we are on a search page performing a search a query may be used
        String query = request.getParameter("query");
        if(query != null && !"".equals(query.trim())){
            // Do standard escaping of some characters in this user-entered query
            query = StatisticsDiscoveryUIUtils.escapeQueryChars(query);
            queryArgs.setQuery(StatisticsSearchUtils.generateDefaultFields(query,dso));
        }

        //We do not need to retrieve any dspace objects, only facets
        queryArgs.setMaxResults(0);
        queryResults =  getSearchService().search(context, dso,  queryArgs);
    }

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
            java.util.List<String> fqs = Arrays.asList(StatisticsDiscoveryUIUtils.getFilterQueries(request, context, dso));

            DiscoveryConfiguration discoveryConfiguration = StatisticsSearchUtils.getDiscoveryConfiguration(dso);
            java.util.List<DiscoverySearchFilterFacet> facets = discoveryConfiguration.getSidebarFacets();

            if (facets != null && 0 < facets.size()) {

                List browse = null;

                for (DiscoverySearchFilterFacet field : facets) {
                    //Retrieve our values
                	java.util.List<StatisticsDiscoverResult.FacetResult> facetValues = new ArrayList<StatisticsDiscoverResult.FacetResult>();
                	if(field.getFilterType().equals(StatisticsDiscoveryCombinedFilterFacet.FILTER_TYPE_COMBINED_FACET)) {
                		facetValues.addAll(StatisticsDiscoverResult.getCombinedFacetValues(queryResults, (StatisticsDiscoveryCombinedFilterFacet)field));
                	}else {
                		facetValues.addAll(queryResults.getFacetResult(field.getIndexFieldName()));
                	}
                	//TODO eliminar condicion ya que quedó de más....
                    //Check if we are dealing with a date, sometimes the facet values arrive as dates !
                    if(facetValues.size() == 0 && field.getType().equals(DiscoveryConfigurationParameters.TYPE_DATE)){
                        facetValues = queryResults.getFacetResult(field.getIndexFieldName());
                    }

                    int shownFacets = field.getFacetLimit()+1;

                    //This is needed to make sure that the date filters do not remain empty
                    if (facetValues != null && 0 < facetValues.size()) {

                        if(browse == null){
                            //Since we have a value it is safe to add the sidebar (doing it this way will ensure that we do not end up with an empty sidebar)
                            browse = options.addList("discovery");

                            browse.setHead(T_FILTER_HEAD);
                        }

                        Iterator<StatisticsDiscoverResult.FacetResult> iter = facetValues.iterator();

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

                            StatisticsDiscoverResult.FacetResult value = iter.next();

                            if (i < shownFacets - 1) {
                                String displayedValue = value.getDisplayedValue();
                                String filterQuery = value.getAsFilterQuery();
                                String filterType = value.getFilterType();
                                String paramsQuery = retrieveParameters(request);
                                if (fqs.contains(getSearchService().toFilterQuery(context, field.getIndexFieldName(), value.getFilterType(), value.getAsFilterQuery()).getFilterQuery())) {
                                	org.dspace.app.xmlui.wing.element.Item item = filterValsList.addItem(Math.random() + "", "selected");
                                	item.addContent(displayedValue + " (" + value.getCount() + ")");
                                	String escapedFilterQuery = filterQuery; 
                                	//Si el parámetro es de tipo fecha, entonces conseguimos el string con la fecha 
                                	if(field.getType().equals(DiscoveryConfigurationParameters.TYPE_DATE)) {
                                		//reemplazamos el simbolo "[" por "\[" para que lo tome la expresión regular dentro del getUrlWithoutFilter...
                                		//TODO esto habria que hacerlo dentro del metodo getUrlWithoutFilter
                                		escapedFilterQuery = filterQuery.replace("[","\\[").replace("]", "\\]");
                                	}
                                	String urlWithoutFilter = StatisticsDiscoveryUIUtils.getUrlWithoutFilter(contextPath + (dso == null ? "" : "/handle/" + dso.getHandle()) + 
                                			"/statistics-discover?" + paramsQuery, field.getIndexFieldName(), value.getFilterType(), escapedFilterQuery);
                                	item.addXref(urlWithoutFilter,"", null, "removeFacet");
                                	
                                } else {
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

    /**
     * Returns the parameters used so it can be used in a url
     * @param request the cocoon request
     * @return the parameters used on this page
     * @throws SQLException 
     */
    private String retrieveParameters(Request request) throws UnsupportedEncodingException, UIException, SQLException {
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
      //Add hidden fields for the Discovery Derived Context/Scope, if apply and only if the scope is not fixed or dynamic (handle/XX/YY/statistics-discover or scope=XX/YY, respectively)
        if (getScope() == null) {
	        if(StatisticsDiscoveryUIUtils.isDiscoveryDerivedScope(request)){
	        	parameters.add(StatisticsDiscoveryUIUtils.DISCOVERY_QUERY_PARAM + "=" + URLEncoder.encode(StatisticsDiscoveryUIUtils.getDiscoveryQueryParam(request), "UTF-8"));
	        	if(StatisticsDiscoveryUIUtils.existDiscoveryScopeParam(request)){
	        		parameters.add(StatisticsDiscoveryUIUtils.DISCOVERY_SCOPE_PARAM + "=" + URLEncoder.encode(StatisticsDiscoveryUIUtils.getDiscoveryScopeParam(request), "UTF-8"));
	        	}
	        	if(!StatisticsDiscoveryUIUtils.isHierarchicalDiscoveryScope(request)){
	        		parameters.add(StatisticsDiscoveryUIUtils.DISCOVERY_SCOPE_NO_HIERARCHICAL_PARAM + "=" + URLEncoder.encode(StatisticsDiscoveryUIUtils.getDiscoveryHierarchicalScopeParam(request), "UTF-8"));
	        	}
	        }
        }
        Map<String, String[]> parameterFilterQueries = StatisticsDiscoveryUIUtils.getParameterFilterQueries(request);
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

    
    private void addViewMoreUrl(List facet, DSpaceObject dso, Request request, DiscoverySearchFilterFacet field) throws WingException, UnsupportedEncodingException, SQLException {
        String parameters = retrieveParameters(request);
        facet.addItem().addXref(
                contextPath +
                        (dso == null ? "" : "/handle/" + dso.getHandle()) +
                        "/statistics-search-filter?" + parameters + BrowseFacet.FACET_FIELD + "=" + field.getIndexFieldName()+"&"+BrowseFacet.ORDER+"="+field.getSortOrderFilterPage(),
                T_VIEW_MORE

        );
    }

    public DiscoverQuery getQueryArgs(Context context, DSpaceObject scope, String... filterQueries) {
        DiscoverQuery queryArgs = new DiscoverQuery();

        DiscoveryConfiguration discoveryConfiguration = getDiscoveryConfiguration(scope);
        java.util.List<DiscoverySearchFilterFacet> facets = discoveryConfiguration.getSidebarFacets();

        log.debug("facets for scope, " + scope + ": " + (facets != null ? facets.size() : null));




        if (facets != null){
            queryArgs.setFacetMinCount(1);
        }

        //Add the default filters
        queryArgs.addFilterQueries(discoveryConfiguration.getDefaultFilterQueries().toArray(new String[discoveryConfiguration.getDefaultFilterQueries().size()]));
        queryArgs.addFilterQueries(filterQueries);

        /** enable faceting of search results */
        if (facets != null){
            for (DiscoverySearchFilterFacet facet : facets) {
                if(facet.getType().equals(DiscoveryConfigurationParameters.TYPE_DATE)){
                    String dateFacet = facet.getIndexFieldName();
                    try{
                        //Get a range query so we can create facet queries ranging from our first to our last date
                        //Attempt to determine our oldest & newest year by checking for previously selected filters
                        int oldestYear = -1;
                        int newestYear = -1;
                        for (String filterQuery : filterQueries) {
                            if(filterQuery.startsWith(dateFacet + ":")){
                                //Check for a range
                                Pattern pattern = Pattern.compile("\\[(.*? TO .*?)\\]");
                                Matcher matcher = pattern.matcher(filterQuery);
                                boolean hasPattern = matcher.find();
                                if(hasPattern){
                                    filterQuery = matcher.group(0);
                                    //We have a range
                                    String tmpOldYearStr = StatisticsSearchUtils.getYearFromDate(filterQuery.split(" TO ")[0].replace("[", "").trim());
                                    String tmpNewYearStr = StatisticsSearchUtils.getYearFromDate(filterQuery.split(" TO ")[1].replace("]", "").trim());

                                    if(tmpOldYearStr != null && tmpNewYearStr != null) {
                                    	//Resolve our range to a first & last year
                                    	int tempOldYear = Integer.parseInt(tmpOldYearStr);
                                    	int tempNewYear = Integer.parseInt(tmpNewYearStr);
                                    	//Check if we have a further filter (or a first one found)
                                    	if(tempNewYear < newestYear || oldestYear < tempOldYear || newestYear == -1){
	                                        oldestYear = tempOldYear;
	                                        newestYear = tempNewYear;
                                    	}
                                    }

                                }else{
                                    if(filterQuery.indexOf(" OR ") != -1){
                                        //Should always be the case
                                        filterQuery = filterQuery.split(" OR ")[0];
                                    }
                                    String tmpOldYearStr1 = StatisticsSearchUtils.getYearFromDate(filterQuery.split(":")[1].trim());
                                    //We should have a single date
                                    if(tmpOldYearStr1 != null) {
                                    	oldestYear = Integer.parseInt(tmpOldYearStr1);
                                    	newestYear = oldestYear;
                                    	//No need to look further
                                    	break;
                                    }
                                }
                            }
                        }
                        //Check if we have found a range, if not then retrieve our first & last year using Solr
                        if(oldestYear == -1 && newestYear == -1){

                            DiscoverQuery yearRangeQuery = new DiscoverQuery();
                            yearRangeQuery.setMaxResults(1);
                            //Set our query to anything that has this value
                            yearRangeQuery.addFieldPresentQueries(dateFacet);
                            //Set sorting so our last value will appear on top
                            yearRangeQuery.setSortField(dateFacet, DiscoverQuery.SORT_ORDER.asc);
                            yearRangeQuery.addFilterQueries(filterQueries);
                            yearRangeQuery.addSearchField(dateFacet);
                            StatisticsDiscoverResult lastYearResult = getSearchService().search(context, scope, yearRangeQuery);

                            if(0 < lastYearResult.getAllResults().size()){
                            	SearchDocument searchDocument = lastYearResult.getAllResults().get(0);
                            	if(searchDocument != null && 0 < searchDocument.getSearchFieldValues(dateFacet).size()){
                            		//Las fechas retornadas por solr tienen el formato de Date.toString()...
                            		String yearForDate = StatisticsSearchUtils.getYearFromCompleteDate(searchDocument.getSearchFieldValues(dateFacet).get(0));
                            		oldestYear = Integer.parseInt(yearForDate);
                            	}
                            }
                            //Now get the first year
                            yearRangeQuery.setSortField(dateFacet, DiscoverQuery.SORT_ORDER.desc);
                            StatisticsDiscoverResult firstYearResult = getSearchService().search(context, scope, yearRangeQuery);
                            if( 0 < lastYearResult.getAllResults().size()){
                            	SearchDocument searchDocument = firstYearResult.getAllResults().get(0);
                            	if(searchDocument != null && 0 < searchDocument.getSearchFieldValues(dateFacet).size()){
                            		//Las fechas retornadas por solr tienen el formato de Date.toString()...
                            		String yearForDate = StatisticsSearchUtils.getYearFromCompleteDate(searchDocument.getSearchFieldValues(dateFacet).get(0));
                            		newestYear = Integer.parseInt(yearForDate);
                            	}
                            }
                            //No values found!
                            if(newestYear == -1 || oldestYear == -1)
                            {
                                continue;
                            }

                        }

                        int gap = 1;
                        //TODO acomodar este algoritmo para que un facet con rango cada 5 y 2 años (array [5,2,1] son los posibles rango aceptados)
                        /**
                        //Attempt to retrieve our gap using the algorithm below
                        int yearDifference = newestYear - oldestYear;
                        if(yearDifference != 0){
                            while (10 < ((double)yearDifference / gap)){
                                gap *= 10;
                            }
                        }
                        **/
                        // We need to determine our top year so we can start our count from a clean year
                        // Example: 2001 and a gap from 10 we need the following result: 2010 - 2000 ; 2000 - 1990 hence the top year
                        int topYear = (int) (Math.ceil((float) (newestYear)/gap)*gap);

                        if(gap == 1){
                        	//Sumamos en 1 el año máximo (el 'newestYear') para que incluya las fechas entre este año y el siguiente, p.e. si el año máximo es 2018 entonces nos interesa las fechas entre 2018 y el 2019=(2018+1)...
                        	topYear++;
                        }
                        java.util.List<String> facetQueries = new ArrayList<String>();
                        //Create facet queries but limit them to 11 (11 == when we need to show a "show more" url)
                        for(int year = topYear; year > oldestYear && (facetQueries.size() < 11); year-=gap){
                            //Add a filter to remove the last year only if we aren't the last year
                            int bottomYear = year - gap;
                            //Make sure we don't go below our last year found
                            if(bottomYear < oldestYear)
                            {
                                bottomYear = oldestYear;
                            }

                            //Also make sure we don't go above our newest year
                            int currentTop = year;
//                            if((year == topYear))
//                            {
//                                currentTop = newestYear;
//                            }
//                            else
//                            {
//                                //We need to do -1 on this one to get a better result
//                                currentTop--;
//                            }
                            String completeBottomYearUTCDateTime = StatisticsSearchUtils.getDateTimeStringFromDate(String.valueOf(bottomYear));
                            String completeCurrentTopYearUTCDateTime = StatisticsSearchUtils.getDateTimeStringFromDate(String.valueOf(currentTop));
                            facetQueries.add(dateFacet + ":[" + completeBottomYearUTCDateTime + " TO " + completeCurrentTopYearUTCDateTime + "]");
                        }
                        for (String facetQuery : facetQueries) {
                            queryArgs.addFacetQuery(facetQuery);
                        }
                    }catch (Exception e){
                        log.error(LogManager.getHeader(context, "Error in Discovery while setting up date facet range", "date facet: " + dateFacet), e);
                    }
                }else{
                    int facetLimit = facet.getFacetLimit();
                    //Add one to our facet limit to make sure that if we have more then the shown facets that we show our "show more" url
                    facetLimit++;
                    if(facet.getFilterType().equals(StatisticsDiscoveryCombinedFilterFacet.FILTER_TYPE_COMBINED_FACET)){
                    	//Si el facet es combinado, entonces agregamos dos o mas facets, uno por campo dentro de la configuración del facet
                    	for (String metadataField: facet.getMetadataFields()) {
                    		queryArgs.addFacetField(new DiscoverFacetField(metadataField, facet.getType(), facetLimit, facet.getSortOrderSidebar()));
    					}
                    }else {
                    	queryArgs.addFacetField(new DiscoverFacetField(facet.getIndexFieldName(), facet.getType(), facetLimit, facet.getSortOrderSidebar()));
                    }
                }
            }
        }
        return queryArgs;
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


    @Override
    public void recycle() {
        queryResults = null;
        queryArgs = null;
        validity = null;
        super.recycle();
    }
    
    
    protected DiscoveryConfiguration getDiscoveryConfiguration(DSpaceObject scope) {
		return StatisticsSearchUtils.getDiscoveryConfiguration(scope);
	}
	
}
