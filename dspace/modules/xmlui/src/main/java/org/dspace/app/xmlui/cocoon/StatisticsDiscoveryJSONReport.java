package org.dspace.app.xmlui.cocoon;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.AbstractReader;
import org.dspace.app.xmlui.aspect.discovery.StatisticsSimpleSearch;
import org.dspace.app.xmlui.cocoon.StatisticsDiscoveryJSONReportParams.OneVarReports;
import org.dspace.app.xmlui.cocoon.StatisticsDiscoveryJSONReportParams.OneVarReports.COUNTOF_VALUES;
import org.dspace.app.xmlui.cocoon.StatisticsDiscoveryJSONReportParams.OneVarReports.TIMELAPSE_VALUES;
import org.dspace.app.xmlui.cocoon.StatisticsDiscoveryJSONReportParams.TwoVarsOneFixedReports;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.discovery.DiscoverFacetField;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverQuery.SORT_ORDER;
import org.dspace.discovery.GenericDiscoverResult.FacetResult;
import org.dspace.discovery.GenericDiscoverResult.SearchDocument;
import org.dspace.discovery.StatisticsDiscoverResult;
import org.dspace.discovery.StatisticsDiscoverResult.DateRangeFacetResult;
import org.dspace.discovery.StatisticsSearchServiceException;
import org.dspace.discovery.StatisticsSearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfigurationParameters;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import org.dspace.core.I18nUtil;

public class StatisticsDiscoveryJSONReport extends AbstractReader {
	
	public static String ONEVAR_REPORT = "onevar";
	public static String TWOVARS_ONEFIXED_REPORT = "twovars-onefixed";
	public static String TYPE_OF_REPORT_PARAM = "typeOfReport";
	
	/**
     * How big a buffer should we use when reading from the bitstream before
     * writing to the HTTP response?
     */
    protected static final int BUFFER_SIZE = 8192;
    
    /**
     * El Encoding por defecto utilizado en la exportación.
     */
    protected static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");
	
    private static final String T_statistics_report_json_country_head = I18nUtil.getMessage("xmlui.ArtifactBrowser.StatisticsSimpleSearch.reports.onevar.countryCode");

	
	/** El tipo de reporte a generar */
	protected String reportType;
	
	/** The Cocoon response */
    protected Response response;

    /** The Cocoon request */
    protected Request request;
    
    // Holders of query params
    private String countOfParam;
    private String byParam;
    private String timelapseParam;
    private int minResultsCount = 1;
    
    private StatisticsSimpleSearch statisticsSimpleSearch = null;
    
    private StatisticsDiscoverResult qResults = null;
    
    private ArrayList<String> dateLabels = null;
	
	@Override
	public void generate() throws IOException, SAXException, ProcessingException {
		BufferedInputStream input;
		try {
			input = searchAndStream();
		} catch (UIException | StatisticsSearchServiceException e) {
			throw new ProcessingException(e);
		} catch (SQLException e) {
			throw new IOException(e);
		}
		
		if(input != null){
            byte[] buffer = new byte[BUFFER_SIZE];

            response.setContentType("application/json; charset=" + DEFAULT_ENCODING.displayName());
            response.setHeader("Content-Disposition","attachment; filename=report.json");
            response.setHeader("Content-Length", String.valueOf(input.available()));
            int length;
            while ((length = input.read(buffer)) > -1)
            {
                out.write(buffer, 0, length);
            }
        }
        out.flush();
	}
	
	@Override
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    		throws ProcessingException, SAXException, IOException {
    	super.setup(resolver, objectModel, src, par);
    	
    	this.request = ObjectModelHelper.getRequest(objectModel);
    	this.response = ObjectModelHelper.getResponse(objectModel);
    	statisticsSimpleSearch = new StatisticsSimpleSearch();
    	
    	//Verificamos los parámetros pasados al reader
    	
    	String typeOfReportParam;
		try {
			typeOfReportParam = par.getParameter(TYPE_OF_REPORT_PARAM);
		} catch (ParameterException e1) {
			throw new ProcessingException("Debe especificar el tipo de reporte a generar.");
		}
		if(typeOfReportParam != null && typeOfReportParam.equalsIgnoreCase(ONEVAR_REPORT)) {
			reportType = ONEVAR_REPORT.toLowerCase();
			countOfParam = getQueryParamValue(OneVarReports.COUNTOF_PARAM.toString());
			if(countOfParam == null) {
				//Lanzamos una excepción indicando que al menos debe existir el parámetro 'countof' para generar el reporte
				throw new ProcessingException("Debe existir el parámetro '" + OneVarReports.COUNTOF_PARAM + "' en este tipo de reporte");
			}
			timelapseParam = getQueryParamValue(OneVarReports.TIMELAPSE_PARAM.toString());
			if( !OneVarReports.checkParametersValues(countOfParam,timelapseParam) ) {
				throw new ProcessingException("Alguno de los valores de los parámetros no son válidos para este tipo de reporte.");
			}
		} else if(typeOfReportParam != null && typeOfReportParam.equalsIgnoreCase(TWOVARS_ONEFIXED_REPORT)) {
			reportType = TWOVARS_ONEFIXED_REPORT.toLowerCase();
			countOfParam = getQueryParamValue(TwoVarsOneFixedReports.COUNTOF_PARAM.toString());
			byParam = getQueryParamValue(TwoVarsOneFixedReports.BY_PARAM.toString());
			if(countOfParam == null || byParam == null) {
				//Lanzamos una excepción indicando que al menos debe existir el parámetro 'countof' y el parámetro'by' para generar el reporte de dos variables
				throw new ProcessingException("Debe existir los parámetros '" + TwoVarsOneFixedReports.COUNTOF_PARAM + "' y '" + TwoVarsOneFixedReports.BY_PARAM + "' en este tipo de reporte");
			}
			timelapseParam = getQueryParamValue(TwoVarsOneFixedReports.TIMELAPSE_PARAM.toString());
			if( !TwoVarsOneFixedReports.checkParametersValues(countOfParam,byParam,timelapseParam) ) {
				throw new ProcessingException("Alguno de los valores de los parámetros no son válidos para este tipo de reporte.");
			}
		}
		String minResultsParam = getQueryParamValue(StatisticsDiscoveryJSONReportParams.MINRESULTS);
		if(minResultsParam != null && minResultsParam.length() > 0) {
			try {
			    int minresultsvalue = Integer.parseInt(minResultsParam);
			    if(minresultsvalue >= 1) {
			    	minResultsCount = minresultsvalue;
			    }
			} catch (NumberFormatException e) {
			    //Si el parametro no puede parsearse a int, entonces "Do nothing"...
			}
		}
		
		//Verificamos si se seteó bien el tipo de reporte
		if(reportType == null) {
			throw new ProcessingException("Debe especificar el tipo de reporte a generar.");
		}
    	
    }
	
	@Override
	public void recycle() {
		super.recycle();
		this.reportType = null;
		this.response = null;
		this.request = null;
		this.countOfParam = null;
		this.byParam = null;
		this.timelapseParam = null;
		this.statisticsSimpleSearch = null;
		this.qResults = null;
		this.dateLabels = null;
	}
	
	private BufferedInputStream searchAndStream() throws SQLException, UIException, StatisticsSearchServiceException {
		DiscoverQuery qArgs;
		Context context = ContextUtil.obtainContext(objectModel);
		
		Context.Mode originalMode = context.getCurrentMode();
        context.setMode(Context.Mode.READ_ONLY);

        Request request = ObjectModelHelper.getRequest(objectModel);
		
     // set the object model on the simple search object
        statisticsSimpleSearch.objectModel = objectModel;
        statisticsSimpleSearch.context = ContextUtil.obtainContext(objectModel);
        String queryString = statisticsSimpleSearch.getQuery();
        DSpaceObject scope = statisticsSimpleSearch.getScope();
        String[] fqs = statisticsSimpleSearch.getFilterQueries();
        
     // prepare query from SatisticsSimpleSearch object
        qArgs = statisticsSimpleSearch.prepareQuery(scope, queryString, fqs);
        
     // no paging required
        qArgs.setStart(0);
        
        //Seteamos la cantidad de resultados en 0 ya que no interesa los documentos...
        qArgs.setMaxResults(0);
        
        ByteArrayOutputStream outputStream = handleResultsByType(qArgs,context,scope);
        
        context.setMode(originalMode);
        
        return new BufferedInputStream( new ByteArrayInputStream(outputStream.toByteArray()));
                
	}
	
	/**
	 * Maneja la consulta segun el tipo de reporte que queremos generar... 
	 * @param qArgs
	 * @param scope 
	 * @param context 
	 * @return 
	 * @throws StatisticsSearchServiceException 
	 */
	private ByteArrayOutputStream handleResultsByType(DiscoverQuery qArgs, Context context, DSpaceObject scope) throws StatisticsSearchServiceException {
		JsonFactory factory = new JsonFactory();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			JsonGenerator generator = factory.createGenerator(output);
				generator.useDefaultPrettyPrinter();
			// Comenzamos a escribir la respuesta JSON con "{"
			generator.writeStartObject();
//			if(reportType != null && reportType.equalsIgnoreCase(ONEVAR_REPORT)) {
				boolean withTimelapse = false;
				String minDate; String maxDate;
				String solrFieldName;
				String solrTimelapseGap = "";
				String messageKeyForTitle;
				if(reportType.equalsIgnoreCase(ONEVAR_REPORT)) {
					//No verifico que exista un enumerativo válido porque ya se valida en el #setup()
					OneVarReports.COUNTOF_VALUES countOf = OneVarReports.COUNTOF_VALUES.getEnumByValue(countOfParam);
					OneVarReports.TIMELAPSE_VALUES timelapse = OneVarReports.TIMELAPSE_VALUES.getEnumByValue(timelapseParam);
					solrFieldName = countOf.getSolrFieldName();
					messageKeyForTitle = "xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.onevar.title." + countOf.toString();
					if(timelapse != null) {
						solrTimelapseGap = timelapse.getSolrGap();
						messageKeyForTitle = messageKeyForTitle + ".for-timelapse." + timelapse.toString();
					}
				} else {
					TwoVarsOneFixedReports.COUNTOF_VALUES countOf = TwoVarsOneFixedReports.COUNTOF_VALUES.getEnumByValue(countOfParam);
					TwoVarsOneFixedReports.TIMELAPSE_VALUES timelapse = TwoVarsOneFixedReports.TIMELAPSE_VALUES.getEnumByValue(timelapseParam);
					//Aplico la restricción principal a la consulta solr para el reporte, determinada por el parametro 'countof'
					qArgs.addFilterQueries(countOf.getFilterQuery());
					//No verifico que exista un enumerativo válido porque ya se valida en el #setup()
					TwoVarsOneFixedReports.BY_VALUES by = TwoVarsOneFixedReports.BY_VALUES.getEnumByValue(byParam);
					messageKeyForTitle = "xmlui.ArtifactBrowser.StatisticsSimpleSearch.report-statistics.twovarsonefixed.title." + countOf.toString() + ".specific-for." + by.toString();
					solrFieldName = by.getSolrFieldName();
					if(timelapse != null) {
						solrTimelapseGap = timelapse.getSolrGap();
						messageKeyForTitle = messageKeyForTitle + ".for-timelapse." + timelapse.toString();
					}
				}
				DiscoverFacetField facet = new DiscoverFacetField(
						solrFieldName, DiscoveryConfigurationParameters.TYPE_TEXT, -1, DiscoveryConfigurationParameters.SORT.COUNT);
				qArgs.addFacetField(facet);
				qArgs.setFacetMinCount(minResultsCount);		//TODO solo deberían tener minCount=1 los reportes de cantidad sin fecha, es decir, timelapse...
				//Hacemos la busqueda
				qResults = StatisticsSearchUtils.getStatisticsSearchService().search(context, scope, qArgs);
				if(timelapseParam != null) {
					withTimelapse = true;
					//Remuevo el facet ya que no lo necesito para realizar las siguientes consultas por timelapse
					qArgs.getFacetFields().remove(facet);
					minDate = getMinDate(context, scope, qArgs);
					maxDate = getMaxDate(context, scope, qArgs);
					qArgs.addProperty("facet", "true");
					qArgs.addProperty("f.time.facet.mincount", "0");
					qArgs.addProperty("facet.range", TIMELAPSE_VALUES.getSolrField());
					qArgs.addProperty("facet.range.start", minDate);
					qArgs.addProperty("facet.range.end", maxDate);
					qArgs.addProperty("facet.range.gap", solrTimelapseGap);
				}
				//escribimos los resultados en la salida JSON
				generateSimpleStringField(generator, "report_name", I18nUtil.getMessage(messageKeyForTitle));
				//Init "data" object
				generator.writeFieldName("data");
				generator.writeStartObject();
				List<FacetResult> results = qResults.getFacetResult(solrFieldName);
				if(results!=null && results.size() > 0) {
					ArrayList<String> values = new ArrayList<String>();
					
					for(java.util.Iterator<FacetResult> resultsIterator = results.iterator(); resultsIterator.hasNext();) {
						FacetResult facetValue = resultsIterator.next();
						if(withTimelapse) {
							//TODO para el caso de las IPs, hay que escapar el signo "." con "\." para evitar que se desencadene un lenta busqueda por expresión regular
							//Iteramos sobre cada valor del facet general para consultar el range.facet en el timelapse especificado en la petición...
							String facetValueFilterQuery = solrFieldName + ":" + StatisticsSearchUtils.getExactMatchFilter(StatisticsSearchUtils.escapeRegexReservedChars(facetValue.getAsFilterQuery()));
							qArgs.addFilterQueries(facetValueFilterQuery);
							qResults = StatisticsSearchUtils.getStatisticsSearchService().search(context, scope, qArgs);
							if(dateLabels==null) {
								initializeDateLabels(qResults);
								generateArrayStringFields(generator,"dateLabel",dateLabels);
							}
							List<DateRangeFacetResult> rangeDateFacetValues = qResults.getDateRangeFacetResults().get(TIMELAPSE_VALUES.getSolrField());
							for ( DateRangeFacetResult rangeValue : rangeDateFacetValues) {
								values.add(String.valueOf(rangeValue.getCount()));
							}
							qArgs.getFilterQueries().remove(facetValueFilterQuery);
						} else {
							values.add(String.valueOf(facetValue.getCount()));
						}
						generateArrayStringFields(generator, facetValue.getDisplayedValue(),values);
						//Limpiamos la lista de valores para reutilizarla en el siguiente facet value
						values.clear();
					}
				}
				//End "data" object
				generator.writeEndObject();
//			} else if(reportType != null && reportType.equalsIgnoreCase(TWOVARS_ONEFIXED_REPORT)) {
//				
//			}
			//Escribimos el final de la respuesta
			generator.writeEndObject();
			generator.close();
		}catch (IOException e) {
			throw new StatisticsSearchServiceException(e);
		}
		
		return output;
	}
	
	/**
	 * Inicializa un array con los labels de fechas (p.e. "2018-01-01").
	 * @param results
	 */
	private void initializeDateLabels(StatisticsDiscoverResult results) {
		dateLabels = new ArrayList<String>();
		for (DateRangeFacetResult dateRangeResult : results.getDateRangeFacetResults().get(TIMELAPSE_VALUES.getSolrField())) {
			dateLabels.add(dateRangeResult.getDisplayedValue());
		}
	}

	/**
	 * Search for a request parameter value
	 * @param paramName  the param for search its value
	 * @return the value or null if doesnt exists the param
	 */
	private String getQueryParamValue(String paramName) {
		Map<String,String> requestParameters = request.getParameters();
		for (String parameter : requestParameters.keySet()) {
			if(parameter.equalsIgnoreCase(paramName)) {
				return request.getParameter(parameter);
			}
		}
		return null; //if nothing found
	}
	
	private void generateSimpleStringField(JsonGenerator generator, String fieldName, String value) throws IOException{
		generator.writeStringField(fieldName, value);
	}
	
	private void generateArrayStringFields(JsonGenerator generator, String fieldName, List<String> values) throws IOException{
		generator.writeFieldName(fieldName);
		generator.writeStartArray();
		for (String value : values) {
			generator.writeString(value);
		}
		generator.writeEndArray();
	}
	
	/**
	 * get the minimun date of the solr results, returns null if no results
	 * @param context
	 * @param scope
	 * @param qArgs
	 * @return date of the solr results, returns null if no results
	 * @throws StatisticsSearchServiceException
	 */
	private String getMinDate(Context context, DSpaceObject scope, DiscoverQuery qArgs) throws StatisticsSearchServiceException {
		return getFirstDate(context, scope, qArgs, SORT_ORDER.asc);
	}
	
	private String getMaxDate(Context context, DSpaceObject scope, DiscoverQuery qArgs) throws StatisticsSearchServiceException {
		return getFirstDate(context, scope, qArgs, SORT_ORDER.desc);
	}
	
	private String getFirstDate(Context context, DSpaceObject scope, DiscoverQuery qArgs, SORT_ORDER order) throws StatisticsSearchServiceException {
		SORT_ORDER originalSortOrder = qArgs.getSortOrder();
		String originalSortField = qArgs.getSortField();
		int originalMaxResults = qArgs.getMaxResults();
		qArgs.setSortField(TIMELAPSE_VALUES.getSolrField(), order);
		qArgs.setMaxResults(1);
		StatisticsDiscoverResult result = StatisticsSearchUtils.getStatisticsSearchService().search(context, scope, qArgs);
		//volvemos los parametros de qArgs a como estaban antes
		qArgs.setSortField(originalSortField, originalSortOrder);
		qArgs.setMaxResults(originalMaxResults);
		ArrayList<SearchDocument> documents = result.getAllResults();
		if(documents.size() == 0) {
			return null;
		} else {
			//Considero que todos los registros tienen el campo 'time' ya que es un campo obligatorio...
			try {
				return StatisticsSearchUtils.getUTCFromCompleteDate(documents.get(0).getSearchFields().get(TIMELAPSE_VALUES.getSolrField()).get(0));
			} catch (ParseException e) {
				throw new StatisticsSearchServiceException("El registro en el core statistics [ID:"+ documents.get(0).getSearchFields().get("uuid").get(0) + "] tiene el campo 'time' corrupto...", e);
			}
		}
	}
}
