package org.dspace.app.xmlui.aspect.discovery;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.lang.StringUtils;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.cocoon.StatisticsDiscoveryExporter;
import org.dspace.app.xmlui.cocoon.StatisticsDiscoveryJSONReportParams.OneVarReports;
import org.dspace.app.xmlui.cocoon.StatisticsDiscoveryJSONReportParams.TwoVarsOneFixedReports;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Cell;
import org.dspace.app.xmlui.wing.element.CheckBox;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Item;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Select;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.StatisticsSearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoveryConfigurationParameters;
import org.dspace.discovery.configuration.DiscoverySearchFilter;
import org.dspace.discovery.exporter.StatisticsJSONExporter;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.xml.sax.SAXException;


public class StatisticsSimpleSearch extends StatisticsAbstractSearch implements CacheableProcessingComponent{

	/**
     * Language Strings
     */
    private static final Message T_title =
            message("xmlui.ArtifactBrowser.SimpleSearch.title");

    private static final Message T_dspace_home =
            message("xmlui.general.dspace_home");

    private static final Message T_trail =
            message("xmlui.ArtifactBrowser.SimpleSearch.trail");

    private static final Message T_search_scope =
        message("xmlui.Discovery.SimpleSearch.search_scope");

    private static final Message T_head =
            message("xmlui.ArtifactBrowser.SimpleSearch.head");
    

//    private static final Message T_search_label =
//            message("xmlui.discovery.SimpleSearch.search_label");

    private static final Message T_go = message("xmlui.general.go");
    private static final Message T_filter_label = message("xmlui.Discovery.SimpleSearch.filter_head");
    private static final Message T_filter_help = message("xmlui.Discovery.SimpleSearch.filter_help");
    private static final Message T_filter_current_filters = message("xmlui.Discovery.AbstractSearch.filters.controls.current-filters.head");
    private static final Message T_filter_new_filters = message("xmlui.Discovery.AbstractSearch.filters.controls.new-filters.head");
    private static final Message T_filter_controls_apply = message("xmlui.Discovery.AbstractSearch.filters.controls.apply-filters");
    private static final Message T_filter_controls_add = message("xmlui.Discovery.AbstractSearch.filters.controls.add-filter");
    private static final Message T_filter_controls_remove = message("xmlui.Discovery.AbstractSearch.filters.controls.remove-filter");
    private static final Message T_filters_show = message("xmlui.Discovery.AbstractSearch.filters.display");
    private static final Message T_filter_contain = message("xmlui.Discovery.SimpleSearch.filter.contains");
    private static final Message T_filter_equals = message("xmlui.Discovery.SimpleSearch.filter.equals");
    private static final Message T_filter_notcontain = message("xmlui.Discovery.SimpleSearch.filter.notcontains");
    private static final Message T_filter_notequals = message("xmlui.Discovery.SimpleSearch.filter.notequals");
//    private static final Message T_filter_authority = message("xmlui.Discovery.SimpleSearch.filter.authority");
//    private static final Message T_filter_notauthority = message("xmlui.Discovery.SimpleSearch.filter.notauthority");
    //DATE OPERATORS
    private static final Message T_filter_from_date = message("xmlui.Discovery.SimpleSearch.filter.from_date");
    private static final Message T_filter_until_date = message("xmlui.Discovery.SimpleSearch.filter.until_date");
    
    private static final Message T_discovery_derived_scope = message("xmlui.Statistics_Discovery.SimpleSearch.discovery_derived_scope");
    private static final Message T_discovery_derived_scope_link = message("xmlui.Statistics_Discovery.SimpleSearch.discovery_derived_scope_link");
    
    private static final Message T_statistics_export_head = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.export-results.head");
    private static final Message T_statistics_export_csv_head = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.export-results-csv.head");
    private static final Message T_statistics_export_csv_submit = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.export-results-csv.controls.submit");
    private static final Message T_statistics_export_json_head = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.export-results-json.head");
    private static final Message T_statistics_export_json_submit = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.export-results-json.controls.submit");
    private static final Message T_statistics_export_json_format_label = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.export-results-json.controls.format_label");
    private static final Message T_statistics_export_json_format_help = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.export-results-json.controls.format_help");
    
    private static final Message T_statistics_report_head = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.head");
    private static final Message T_statistics_report_onevar_head = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.onevar.head");
    private static final Message T_statistics_report_accumulative_label = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.countof.label");
    private static final Message T_statistics_report_timelapse_label = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.timelapse.label");
	private static final Message T_statistics_report_minresults_label = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.minresults.label");
	private static final Message T_statistics_report_minresults_help = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.minresults.help");
	private static final Message T_statistics_report_by_label = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.by.label");
	private static final Message T_statistics_report_update_chart = message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.update_chart");

	
    private String aspectPath = "statistics-discover";
    
	/**
     * Add Page metadata.
     * @throws org.dspace.app.xmlui.wing.WingException passed through.
     * @throws java.sql.SQLException passed through.
     */
    public void addPageMeta(PageMeta pageMeta) throws WingException, SQLException {
        pageMeta.addMetadata("title").addContent(T_title);
        pageMeta.addTrailLink(contextPath + "/", T_dspace_home);

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if ((dso instanceof org.dspace.content.Collection) || (dso instanceof Community)) {
            HandleUtil.buildHandleTrail(context, dso, pageMeta, contextPath, true);
        }

        pageMeta.addTrail().addContent(T_trail);
    }

    /**
     * build the DRI page representing the body of the search query. This
     * provides a widget to generate a new query and list of search results if
     * present.
     * @throws org.xml.sax.SAXException passed through.
     * @throws org.dspace.app.xmlui.wing.WingException passed through.
     * @throws java.sql.SQLException passed through.
     * @throws java.io.IOException passed through.
     * @throws org.dspace.authorize.AuthorizeException passed through.
     */
    public void addBody(Body body) throws SAXException, WingException,
            SQLException, IOException, AuthorizeException {

        Context.Mode originalMode = context.getCurrentMode();
        context.setMode(Context.Mode.READ_ONLY);

        Request request = ObjectModelHelper.getRequest(objectModel);
        String queryString = getQuery();

        // Build the DRI Body
        Division search = body.addDivision("search", "primary");
        search.setHead(T_head);
        //TODO definir una endpoint de búsqueda JSON como Discover para el core SEARCH
//        String searchUrl = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.url") + "/JSON/discovery/search";
//
//        search.addHidden("discovery-json-search-url").setValue(searchUrl);
//        DSpaceObject currentScope = getScope();
//        if(currentScope != null){
//            search.addHidden("discovery-json-scope").setValue(currentScope.getHandle());
//        }
        search.addHidden("contextpath").setValue(contextPath);

        Map<String, String[]> fqs = getParameterFilterQueries();

        Division searchBoxDivision = search.addDivision("discovery-search-box", "discoverySearchBox");

        Division mainSearchDiv = searchBoxDivision.addInteractiveDivision("general-query",
        		aspectPath, Division.METHOD_GET, "discover-search-box");
        
        List searchList = mainSearchDiv.addList("primary-search", List.TYPE_FORM);

//        searchList.setHead(T_search_label);

        //Por una decisión de implementación, tiene mayor prioridad el uso de scopes fijos o dinámicos, en segundo lugar queda el contexto o scope derivado de una consulta Discovery...
        if (getScope() != null) {
        	if (variableScope())
            {
        		DSpaceObject scopeDSO = getScope();
        		if(scopeDSO.getType() == org.dspace.core.Constants.ITEM) {
        			searchList.addItem("Estadísticas relativas al item ");
        			org.dspace.content.Item item = (org.dspace.content.Item)scopeDSO;
        			String handle = item.getHandle();
        			String title = item.getName();
            		searchList.addItem().addXref(DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.url") + "/handle/" + handle).addContent("\""+title+"\"");
        		} else {
        			Select scope = searchList.addItem().addSelect("scope");
        			scope.setLabel(T_search_scope);
        			buildScopeList(scope);
        		}
            }
        } else if (StatisticsDiscoveryUIUtils.isDiscoveryDerivedScope(request)) {
    		//Si el scope es derivado de una búsqueda de Discovery, entonces mostramos la consulta de Discovery de la que deriva el scope...
    		searchList.addItem(T_discovery_derived_scope);
    		String discoveryQueryParams = StatisticsDiscoveryUIUtils.getDiscoveryQueryParam(request);
    		String discoveryQueryPath = request.getContextPath();
    		if(StatisticsDiscoveryUIUtils.existDiscoveryScopeParam(request)) {
    			String handleScopeParam = StatisticsDiscoveryUIUtils.getDiscoveryScopeParam(request);
    			//Add hidden field with the original StatisticsDiscoveryUIUtils.DISCOVERY_SCOPE_PARAM...
    			searchList.addItem().addHidden(StatisticsDiscoveryUIUtils.DISCOVERY_SCOPE_PARAM).setValue(handleScopeParam);
    			if(StatisticsDiscoveryUIUtils.isFixedDiscoverScope(request)) {
    				discoveryQueryPath +=  (!handleScopeParam.startsWith("/")? "/": "") + handleScopeParam + "/discover?";
    			} else {
    				discoveryQueryPath += "/discover?";
    				discoveryQueryParams += "&scope=" + handleScopeParam;
    			}
    		} else {
    			discoveryQueryPath += "/discover?";
    		}
    		searchList.addItem().addXref(discoveryQueryPath + discoveryQueryParams).addContent(T_discovery_derived_scope_link);
        }
        

        Item searchBoxItem = searchList.addItem();
        Text text = searchBoxItem.addText("query");
        text.setValue(queryString);
        searchBoxItem.addButton("submit", "search-icon").setValue(T_go);

        //TODO determinar si es necesario un spellcheckquery para statistics...
//        if(queryResults != null && StringUtils.isNotBlank(queryResults.getSpellCheckQuery()))
//        {
//            Item didYouMeanItem = searchList.addItem("did-you-mean", "didYouMean");
//            didYouMeanItem.addContent(T_did_you_mean);
//            didYouMeanItem.addXref(getSuggestUrl(queryResults.getSpellCheckQuery()), queryResults.getSpellCheckQuery(), "didYouMean");
//        }

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        DiscoveryConfiguration discoveryConfiguration = StatisticsSearchUtils.getDiscoveryConfiguration(dso);
        java.util.List<DiscoverySearchFilter> filterFields = discoveryConfiguration.getSearchFilters();
        java.util.List<String> filterTypes = StatisticsDiscoveryUIUtils.getRepeatableParameters(request, "filtertype");
        java.util.List<String> filterOperators = StatisticsDiscoveryUIUtils.getRepeatableParameters(request, "filter_relational_operator");
        //Buscamos los parametros que tienen EXACTAMENTE la forma "filter" o "filter_N"...
        java.util.List<String> filterValues = StatisticsDiscoveryUIUtils.getRepeatableParametersWithRegex(request,  "^filter(_\\d+)?$");

        if(0 < filterFields.size() && filterTypes.size() == 0)
        {
            //Display the add filters url ONLY if we have no filters selected & filters can be added
            searchList.addItem().addXref("display-filters", T_filters_show);
        }
        addHiddenFormFields("search", request, fqs, mainSearchDiv);


        if(0 < filterFields.size())
        {
            Division searchFiltersDiv = searchBoxDivision.addInteractiveDivision("search-filters",
            		aspectPath, Division.METHOD_GET, "discover-filters-box "/* + (0 < filterTypes.size() ? "" : "hidden")*/);

            Division filtersWrapper = searchFiltersDiv.addDivision("discovery-filters-wrapper");
            filtersWrapper.setHead(T_filter_label);
            filtersWrapper.addPara(T_filter_help);
            Table filtersTable = filtersWrapper.addTable("discovery-filters", 1, 4, "discovery-filters");


            //If we have any filters, show them
            if(filterTypes.size() > 0)
            {

                filtersTable.addRow(Row.ROLE_HEADER).addCell("", Cell.ROLE_HEADER, 1, 4, "new-filter-header").addContent(T_filter_current_filters);
                for (int i = 0; i <  filterTypes.size(); i++)
                {
                    String filterType = filterTypes.get(i);
                    String filterValue = filterValues.get(i);
                    String filterOperator = filterOperators.get(i);
                    
                    String filterConfigurationType = this.getTypeByFilterID(filterType, discoveryConfiguration);
                    //TODO verificar si es un campo del tipo 'DATE' que sea una fecha válida, sino no imprimir el filtro...
                    if(StringUtils.isNotBlank(filterValue) && StringUtils.isNotBlank(filterConfigurationType))
                    {
                    	if(filterConfigurationType.equals(DiscoveryConfigurationParameters.TYPE_DATE)){
                    		//Verificamos si el DATE que viene es un rango, es decir, [2018-01-01T00:00:00.000Z TO 2019-01-01T00:00:00.000Z] por ejemplo
                    		Pattern pattern = Pattern.compile("\\[(.*? TO .*?)\\]");
                            Matcher matcher = pattern.matcher(filterValue);
                            boolean hasPattern = matcher.find();
                            if(hasPattern) {
                            	String dateValue = matcher.group(0);
                                String startDateStr = dateValue.split(" TO ")[0].replace("[", "").trim();
                                String endDateStr = dateValue.split(" TO ")[1].replace("]", "").trim();
                                if(!StatisticsSearchUtils.isValidDate(startDateStr) || !StatisticsSearchUtils.isValidDate(endDateStr)) {
                                	continue;
                                }
                            } else {
                            	//Si no tenemos un rango, entonces tendría que haber una fecha sencilla
                            	if(!StatisticsSearchUtils.isValidDate(filterValue)) {
                            		continue;
                            	}
                            }
                    	}
                        Row row = filtersTable.addRow("used-filters-" + i+1, Row.ROLE_DATA, "search-filter used-filter");
                        addFilterRow(filterFields, i+1, row, filterType, filterOperator, filterValue, filterConfigurationType);
                    }
                }
                filtersTable.addRow("filler-row", Row.ROLE_DATA, "search-filter filler").addCell(1, 4).addContent("");
                filtersTable.addRow(Row.ROLE_HEADER).addCell("", Cell.ROLE_HEADER, 1, 4, "new-filter-header").addContent(T_filter_new_filters);
            }

            int index = filterTypes.size() + 1;
            Row textRow = filtersTable.addRow("filter-new-" + index, Row.ROLE_DATA, "search-filter");
            addFilterRow(filterFields, index, textRow, null, null, null, DiscoveryConfigurationParameters.TYPE_TEXT);
            Row dateRow = filtersTable.addRow("filter-new-" + (index + 1), Row.ROLE_DATA, "search-filter");
            addFilterRow(filterFields, index + 1, dateRow, null, null, null, DiscoveryConfigurationParameters.TYPE_DATE);

            Row filterControlsItem = filtersTable.addRow("filter-controls", Row.ROLE_DATA, "apply-filter");
//            filterControlsItem.addCell(1, 3).addContent("");
            filterControlsItem.addCell(1, 4).addButton("submit_apply_filter", "discovery-apply-filter-button").setValue(T_filter_controls_apply);

            addHiddenFormFields("filter", request, fqs, searchFiltersDiv);

        }


//        query.addPara(null, "button-list").addButton("submit").setValue(T_go);

        // Build the DRI Body
        //Division results = body.addDivision("results", "primary");
        //results.setHead(T_head);
        buildMainForm(search);
        
//        Division exportDivision = search.addDivision("discovery-export-results-box", "discoverySearchBox");
        Division exportDivision = search.addDivision("discovery-export-results-box");
        exportDivision.setHead(T_statistics_export_head);
        buildExportResultsForm(exportDivision);

        // Add the result division
        try {
            buildSearchResultsDivision(search);
        } catch (SearchServiceException e) {
            throw new UIException(e.getMessage(), e);
        }
        
        //Add chart generation section
        Division chartSection = body.addDivision("statistics-discovery-chart-section");
        chartSection.setHead(T_statistics_report_head);
        buildStatisticsChartSection(chartSection);

        context.setMode(originalMode);
    }
    
    /**
     * Agrega una sección que permite la generación de algunos reportes predefinidos sobre el core statistics.
     * @param chartSection
     * @throws WingException 
     */
    private void buildStatisticsChartSection(Division chartSection) throws WingException {
    	chartSection.addPara(T_statistics_report_onevar_head);
    	//Generamos la URL para indicar dónde realizar las consultas al endpoint JSON
    	Map<String, String> parameters = new HashMap<String, String>();
    	String jsonEndpointURL = generateURL(parameters);
    	jsonEndpointURL = addFilterQueriesToUrl(jsonEndpointURL);
    	String jsonEndpointURLOnevar = jsonEndpointURL.replace(aspectPath, aspectPath + "/report/json/onevar");
    	String jsonEndpointURLTwoVarsOneFixed = jsonEndpointURL.replace(aspectPath, aspectPath + "/report/json/twovars-onefixed");
    	
    	//One var charts
    	Division onevarDivision = chartSection.addDivision("statistics_discovery_onevar_chart_options", "discovery-box");
    	Para onevarOptions = onevarDivision.addPara();
    	onevarOptions.addContent(T_statistics_report_accumulative_label);
    	Select onevarCountof = onevarOptions.addSelect("countof");
    	for(OneVarReports.COUNTOF_VALUES countof: OneVarReports.COUNTOF_VALUES.values()){
    		onevarCountof.addOption(countof.toString()).addContent(message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.onevar.countof." + countof.toString()));
    	}
    	onevarOptions.addContent(T_statistics_report_timelapse_label);
    	Select onevarTimelapse = onevarOptions.addSelect("timelapse");
    	onevarTimelapse.addOption("").addContent(message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.onevar.timelapse.novalue"));
    	for(OneVarReports.TIMELAPSE_VALUES timelapse: OneVarReports.TIMELAPSE_VALUES.values()){
    		onevarTimelapse.addOption(timelapse.toString()).addContent(message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.onevar.timelapse." + timelapse.toString()));
    	}
    	onevarOptions.addContent(T_statistics_report_minresults_label);
    	Text onevar_minresults = onevarOptions.addText("min_results");
    	onevar_minresults.setValue("1");
    	onevar_minresults.setHelp(T_statistics_report_minresults_help);
    	onevarOptions.addButton("statistics_onevar_report_bttn", "discovery-apply-filter-button statistics_discovery_report_bttn").setValue(T_statistics_report_update_chart);
//    	onevarOptions.addXref(DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.url") + "/"+ aspectPath + "/report/json/onevar", "path-onevar-onefixed", "hidden","report-onevar-path");
    	onevarOptions.addXref(jsonEndpointURLOnevar, "path-onevar-onefixed", "hidden","report-onevar-path");
    	
    	//Two vars one fixed charts
    	Division twovarsonefixedDivision = chartSection.addDivision("statistics_discovery_twovarsonefixed_chart_options", "discovery-box");
    	Para twovarsonefixedOptions = twovarsonefixedDivision.addPara();
    	twovarsonefixedOptions.addContent(T_statistics_report_accumulative_label);
    	Select twovarsonefixedCountof = twovarsonefixedOptions.addSelect("countof");
    	for(TwoVarsOneFixedReports.COUNTOF_VALUES countof: TwoVarsOneFixedReports.COUNTOF_VALUES.values()){
    		twovarsonefixedCountof.addOption(countof.toString()).addContent(message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.twovarsonefixed.countof." + countof.toString()));
    	}
    	twovarsonefixedOptions.addContent(T_statistics_report_by_label);
    	Select twovarsonefixedBy = twovarsonefixedOptions.addSelect("by");
    	for(TwoVarsOneFixedReports.BY_VALUES timelapse: TwoVarsOneFixedReports.BY_VALUES.values()){
    		twovarsonefixedBy.addOption(timelapse.toString()).addContent(message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.twovarsonefixed.by." + timelapse.toString()));
    	}
    	twovarsonefixedOptions.addContent(T_statistics_report_timelapse_label);
    	Select twovarsonefixedTimelapse = twovarsonefixedOptions.addSelect("timelapse");
    	twovarsonefixedTimelapse.addOption("").addContent(message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.twovarsonefixed.timelapse.novalue"));
    	for(TwoVarsOneFixedReports.TIMELAPSE_VALUES timelapse: TwoVarsOneFixedReports.TIMELAPSE_VALUES.values()){
    		twovarsonefixedTimelapse.addOption(timelapse.toString()).addContent(message("xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.twovarsonefixed.timelapse." + timelapse.toString()));
    	}
    	twovarsonefixedOptions.addContent(T_statistics_report_minresults_label);
    	Text twovarsonefixed_minresults = twovarsonefixedOptions.addText("min_results");
    	twovarsonefixed_minresults.setValue("1");
    	twovarsonefixed_minresults.setHelp(T_statistics_report_minresults_help);
    	twovarsonefixedOptions.addButton("statistics_twovarsonefixed_report_bttn", "discovery-apply-filter-button statistics_discovery_report_bttn").setValue(T_statistics_report_update_chart);
//    	twovarsonefixedOptions.addXref(DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.url") + "/"+ aspectPath + "/report/json/twovars-onefixed", "path-twovars-onefixed", "hidden", "report-twovars_onefixed-path");
    	twovarsonefixedOptions.addXref(jsonEndpointURLTwoVarsOneFixed, "path-twovars-onefixed", "hidden", "report-twovars_onefixed-path");
	}

	/**
     * Agrega la sección para la exportación de resultados en el documento principal en los formatos definidos en la configuración statistics-discovery.cfg
     * @param exportDivision	es el div donde poner todo lo relativo a la exportación de resulados
     * @throws WingException 
     * @throws SQLException 
     */
    private void buildExportResultsForm(Division exportDivision) throws WingException, SQLException {
    	java.util.List<String> exportOptions = StatisticsDiscoveryExporter.getAvailablesExportOptions();
    	Request request = ObjectModelHelper.getRequest(objectModel);
    	Map<String, String[]> fqs = getParameterFilterQueries();
    	
    	for (String exportOption : exportOptions) {
    		exportOption = exportOption.replace("statistics-discovery.exporter.selector.", "");
    		if(exportOption.equals("csv")) {
    			Division csvDiv = exportDivision.addDivision("csv_exportation");
//    			csvDiv.setHead(T_statistics_export_csv_head);
    			csvDiv.addPara(T_statistics_export_csv_head);
    			Division csvExportForm = csvDiv.addInteractiveDivision("csv_export_form","statistics-discover/export/csv", Division.METHOD_GET, "statisticsFromQuery discovery-box");
    			Para csvButtons = csvExportForm.addPara();
    			csvButtons.addButton("statistics_csv_export_submit", "discovery-apply-filter-button").setValue(T_statistics_export_csv_submit);
    			addHiddenFormFields("allParams", request, fqs, csvExportForm);
    		}
    		
    		if(exportOption.equals("json")) {
    			Division jsonDiv = exportDivision.addDivision("json_exportation");
//    			jsonDiv.setHead(T_statistics_export_json_head);
    			jsonDiv.addPara(T_statistics_export_json_head);
    			Division jsonExportForm = jsonDiv.addInteractiveDivision("json_export_form","statistics-discover/export/json", Division.METHOD_GET, "statisticsFromQuery discovery-box");
    			Para jsonButtons = jsonExportForm.addPara();
    			CheckBox prettyFormatCheckbox = jsonButtons.addCheckBox(StatisticsJSONExporter.JsonExporterParams.PRETTY_FORMAT.toString());
    			prettyFormatCheckbox.addOption(1, T_statistics_export_json_format_label);
    			prettyFormatCheckbox.setHelp(T_statistics_export_json_format_help);
    			jsonButtons.addButton("statistics_json_export_submit", "discovery-apply-filter-button").setValue(T_statistics_export_json_submit);
    			addHiddenFormFields("allParams", request, fqs, jsonExportForm);
    		}
		}
	}

	/**
     * Print a filter row. This filter may be already applied or not, i.e. the filter is applied if it was instantiated and has a triplet (type,operator,value) assigned.
     * If filter is 'date' type, then print the operators available for a date.
     * 
     * @param filterFields 	all filters that can be applied
     * @param index 	the position of the filter in the chain of applied (or to apply) filters.
     * @param row 	the Row where to put the filter
     * @param selectedFilterType	tell the type of the filter if it was instantiated
     * @param relationalOperator	tell the operator of the filter if it was instantiated
     * @param value					tell the value of the filter if it was instantiated
     * @param filterType			corresponds to any of the values in DiscoveryConfigurationParameters
     * @throws WingException
     */
    protected void addFilterRow(java.util.List<DiscoverySearchFilter> filterFields, int index, Row row, String selectedFilterType, String relationalOperator, String value, String filterType) throws WingException {

        if(filterType !=null && (filterType.equals(DiscoveryConfigurationParameters.TYPE_TEXT) || filterType.equals(DiscoveryConfigurationParameters.TYPE_HIERARCHICAL))) {
        	Select select = row.addCell("text-filter_" + index, Cell.ROLE_DATA, "selection").addSelect("filtertype_" + index);
        	//For each field found (at least one) add options
            for (DiscoverySearchFilter searchFilter : filterFields)
            {
            	if(searchFilter.getType().equals(DiscoveryConfigurationParameters.TYPE_TEXT) || searchFilter.getType().equals(DiscoveryConfigurationParameters.TYPE_HIERARCHICAL)) {
            		select.addOption(StringUtils.equals(searchFilter.getIndexFieldName(), selectedFilterType), searchFilter.getIndexFieldName(), message("xmlui.ArtifactBrowser.SimpleSearch.filter." + searchFilter.getIndexFieldName()));
            	}
            }
            Select typeSelect = row.addCell("text-filter_operators_" + index, Cell.ROLE_DATA, "selection").addSelect("filter_relational_operator_" + index);
            typeSelect.addOption(StringUtils.equals(relationalOperator, "contains"), "contains", T_filter_contain);
            typeSelect.addOption(StringUtils.equals(relationalOperator, "equals"), "equals", T_filter_equals);
//            typeSelect.addOption(StringUtils.equals(relationalOperator, "authority"), "authority", T_filter_authority);
            typeSelect.addOption(StringUtils.equals(relationalOperator, "notcontains"), "notcontains", T_filter_notcontain);
            typeSelect.addOption(StringUtils.equals(relationalOperator, "notequals"), "notequals", T_filter_notequals);
//            typeSelect.addOption(StringUtils.equals(relationalOperator, "notauthority"), "notauthority", T_filter_notauthority);
            
          //Add a box so we can search for our value
          row.addCell("text-filter_value_" + index, Cell.ROLE_DATA, "discovery-filter-input-cell").addText("filter_" + index, "discovery-filter-input text-input").setValue(value == null ? "" : value);

          //And last add an add button
          Cell buttonsCell = row.addCell("filter-controls_" + index, Cell.ROLE_DATA, "filter-controls");
          buttonsCell.addButton("add-filter_" + index, "filter-control filter-add").setValue(T_filter_controls_add);
          buttonsCell.addButton("remove-filter_" + index, "filter-control filter-remove").setValue(T_filter_controls_remove);
        }
        
        if(filterType !=null && filterType.equals(DiscoveryConfigurationParameters.TYPE_DATE)) {
        	Select select = row.addCell("date-filter_" + index, Cell.ROLE_DATA, "selection").addSelect("filtertype_" + index);
        	//For each field found (at least one) add options
            for (DiscoverySearchFilter searchFilter : filterFields)
            {
            	if(searchFilter.getType().equals(DiscoveryConfigurationParameters.TYPE_DATE)) {
            		select.addOption(StringUtils.equals(searchFilter.getIndexFieldName(), selectedFilterType), searchFilter.getIndexFieldName(), message("xmlui.ArtifactBrowser.SimpleSearch.filter." + searchFilter.getIndexFieldName()));
            	}
            }
            Select typeSelect = row.addCell("date-filter_operators_" + index, Cell.ROLE_DATA, "selection").addSelect("filter_relational_operator_" + index);
            typeSelect.addOption(StringUtils.equals(relationalOperator, "fromDate"), "fromDate", T_filter_from_date);
            typeSelect.addOption(StringUtils.equals(relationalOperator, "untilDate"), "untilDate", T_filter_until_date);
            //Agregamos este campo en el filtro para las fechas para poder realizar búsquedas por rango (p.e., '[2016-01-01T00:00:00Z TO 2017-01-01T00:00:00Z]') o exactas, al estilo Discovery
            typeSelect.addOption(StringUtils.equals(relationalOperator, "equals"), "equals", T_filter_equals);
            
          //Add a box so we can search for our value
          row.addCell("date-filter_value_" + index, Cell.ROLE_DATA, "discovery-filter-input-cell").addText("filter_" + index, "discovery-filter-input date-input").setValue(value == null ? "" : value);
          
       
          //And last add an add button
          Cell buttonsCell = row.addCell("filter-controls_" + index, Cell.ROLE_DATA, "filter-controls");
          buttonsCell.addButton("add-filter_" + index, "filter-control filter-add").setValue(T_filter_controls_add);
          buttonsCell.addButton("remove-filter_" + index, "filter-control filter-remove").setValue(T_filter_controls_remove);
        }
    }

    @Override
    protected String getBasicUrl() throws SQLException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

        return request.getContextPath() + (dso == null ? "" : "/handle/" + dso.getHandle()) + "/" + aspectPath;
    }

    @Override
    protected Map<String, String[]> getParameterFilterQueries(){
        return StatisticsDiscoveryUIUtils.getParameterFilterQueries(ObjectModelHelper.getRequest(objectModel));

    }
    /**
     * Returns all the filter queries for use by discovery
     *  This method returns more expanded filter queries then the getParameterFilterQueries
     * @return an array containing the filter queries
     */
    @Override
	public String[] getFilterQueries() {
    	DSpaceObject dso;
		try {
			dso = getScope();
			return StatisticsDiscoveryUIUtils.getFilterQueries(ObjectModelHelper.getRequest(objectModel), context,dso);
		} catch (SQLException e) {
			// TODO mal manejo de excepción...
			return null;
		}
    }


    /**
     * Get the search query from the URL parameter, if none is found the empty
     * string is returned.
     * @return decoded query.
     * @throws org.dspace.app.xmlui.utils.UIException passed through.
     */
    @Override
	public String getQuery() throws UIException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String query = decodeFromURL(request.getParameter("query"));
        if (query == null)
        {
            return "";
        }
        return query.trim();
    }

    /**
     * Generate a URL to the statistics simple search.
     * @return the URL.
     * @throws org.dspace.app.xmlui.utils.UIException passed through.
     */
    @Override
    protected String generateURL(Map<String, String> parameters)
            throws UIException {
        String query = getQuery();
        if (!"".equals(query) && parameters.get("query") == null)
        {
            parameters.put("query", encodeForURL(query));
        }

        if (parameters.get("page") == null)
        {
            parameters.put("page", String.valueOf(getParameterPage()));
        }

        if (parameters.get("rpp") == null)
        {
            parameters.put("rpp", String.valueOf(getParameterRpp()));
        }


        if (parameters.get("group_by") == null)
        {
            parameters.put("group_by", String.valueOf(this.getParameterGroup()));
        }

        if (parameters.get("sort_by") == null && getParameterSortBy() != null)
        {
            parameters.put("sort_by", String.valueOf(getParameterSortBy()));
        }

        if (parameters.get("order") == null && getParameterOrder() != null)
        {
            parameters.put("order", getParameterOrder());
        }

        if (parameters.get("etal") == null)
        {
            parameters.put("etal", String.valueOf(getParameterEtAl()));
        }
        if(parameters.get("scope") == null && getParameterScope() != null)
        {
            parameters.put("scope", getParameterScope());
        }

        return AbstractDSpaceTransformer.generateURL(aspectPath, parameters);
    }

    /**
     * Since the layout is creating separate forms for each search part
     * this method will add hidden fields containing the values from other form parts
     *
     * @param type the type of our form
     * @param request the request
     * @param fqs the filter queries
     * @param division the division that requires the hidden fields
     * @throws WingException will never occur
     * @throws SQLException
     */
    private void addHiddenFormFields(String type, Request request, Map<String, String[]> fqs, Division division) throws WingException, SQLException {
        if(type.equals("filter") || type.equals("sort") || type.equals("allParams")){
            if(request.getParameter("query") != null){
                division.addHidden("query").setValue(request.getParameter("query"));
            }
            if(request.getParameter("scope") != null){
                division.addHidden("scope").setValue(request.getParameter("scope"));
            }
        }

        //Add the filter queries, current search settings so these remain saved when performing a new search !
        if(type.equals("search") || type.equals("sort") || type.equals("allParams"))
        {
            for (String parameter : fqs.keySet())
            {
                String[] values = fqs.get(parameter);
                for (String value : values) {
                    division.addHidden(parameter).setValue(value);
                }
            }
        }

        if(type.equals("search") || type.equals("filter") || type.equals("allParams")){
            if(request.getParameter("rpp") != null){
                division.addHidden("rpp").setValue(request.getParameter("rpp"));
            }
            if(request.getParameter("sort_by") != null){
                division.addHidden("sort_by").setValue(request.getParameter("sort_by"));
            }
            if(request.getParameter("order") != null){
                division.addHidden("order").setValue(request.getParameter("order"));
            }
        }
        
        //Add hidden fields for the Discovery Derived Context/Scope, if apply and only if the scope is not fixed or dynamic (handle/XX/YY/statistics-discover or scope=XX/YY, respectively)
        if (getScope() == null) {
        	if(StatisticsDiscoveryUIUtils.isDiscoveryDerivedScope(request)) {
        		division.addHidden(StatisticsDiscoveryUIUtils.DISCOVERY_QUERY_PARAM).setValue(StatisticsDiscoveryUIUtils.getDiscoveryQueryParam(request));
        		if(StatisticsDiscoveryUIUtils.existDiscoveryScopeParam(request)) {
        			division.addHidden(StatisticsDiscoveryUIUtils.DISCOVERY_SCOPE_PARAM).setValue(StatisticsDiscoveryUIUtils.getDiscoveryScopeParam(request));
        		}
        		if(!StatisticsDiscoveryUIUtils.isHierarchicalDiscoveryScope(request)) {
        			division.addHidden(StatisticsDiscoveryUIUtils.DISCOVERY_SCOPE_NO_HIERARCHICAL_PARAM).setValue(StatisticsDiscoveryUIUtils.getDiscoveryHierarchicalScopeParam(request));
        		}
        	}
        }
    }

    protected String getSuggestUrl(String newQuery) throws UIException {
        Map parameters = new HashMap();
        parameters.put("query", newQuery);
        return addFilterQueriesToUrl(generateURL(parameters));
    }
    
    
    /**
     * Verificamos el tipo de filtro a partir del ID de un determinado filtro...
     * @param filterId	el ID del filtro al que se quiere saber el filter type
     * @return	el tipo del filtro correspondiente a 'filterId' (alguno de los tipos declarados en DiscoveryConfigurationParameters) 
     * 			ó NULL en caso de no existir el filtro pasado
     */
    protected String getTypeByFilterID(String filterId, DiscoveryConfiguration config) {
    	for (DiscoverySearchFilter filter : config.getSearchFilters()) {
			if(StringUtils.equals(filter.getIndexFieldName(), filterId)) {
				return filter.getType();
			}
		}
    	return null;
    }
    
}
