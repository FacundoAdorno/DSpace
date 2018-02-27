/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.discovery;

import org.apache.cocoon.environment.Request;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.factory.ContentServiceFactoryImpl;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.discovery.StatisticsSearchService;
import org.dspace.discovery.StatisticsSearchUtils;
import org.dspace.discovery.configuration.DiscoverySearchFilter;
import org.dspace.discovery.configuration.StatisticsDiscoveryCombinedFilterFacet;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO Hacer un refactor de esta clase, crear una estructura jerárquica con la clase DiscoveryUIUtils, teniendo como superclase GenericSolrUIUtils
 * 
 * @author facundo
 *
 */
public class StatisticsDiscoveryUIUtils {

    private static StatisticsSearchService statisticsSearchService = null;

    static {
        statisticsSearchService = StatisticsSearchUtils.getStatisticsSearchService();
    }
    
    private static final Logger log = Logger.getLogger(StatisticsDiscoveryUIUtils.class);
    
    /**
     * Este parámetro representa la consulta Discovery en el caso de que el contexto de la estadística sean los DSO de una consulta Discovery
     */
    public static String DISCOVERY_QUERY_PARAM = "discovery_query";
    /**
     * Este parámetro representa el scope de una consulta Discovery (fijo ó variable, es decir, 'handle/xx/yy/discover' ó 'scope=xx/yy') en el caso de 
     * que el contexto de la estadística sean los DSO de una consulta Discovery.
     */
    public static String DISCOVERY_SCOPE_PARAM = "discovery_scope";



    /**
     * Returns a list of the filter queries for use in rendering pages, creating page more urls, ....
     * @param request user's request.
     * @return an array containing the filter queries
     */
    public static Map<String, String[]> getParameterFilterQueries(Request request) {
        Map<String, String[]> fqs = new HashMap<String, String[]>();

        List<String> filterTypes = getRepeatableParameters(request, "filtertype");
        List<String> filterOperators = getRepeatableParameters(request, "filter_relational_operator");
        List<String> filterValues = StatisticsDiscoveryUIUtils.getRepeatableParametersWithRegex(request,  "^filter(_\\d+)?$");

        for (int i = 0; i < filterTypes.size(); i++) {
            String filterType = filterTypes.get(i);
            String filterValue = filterValues.get(i);
            String filterOperator = filterOperators.get(i);

            fqs.put("filtertype_" + i, new String[]{filterType});
            fqs.put("filter_relational_operator_" + i, new String[]{filterOperator});
            fqs.put("filter_" + i, new String[]{filterValue});
        }
        return fqs;
    }

    /**
     * Returns all the filter queries for use by discovery
     * @param request user's request.
     * @param context session context.
     * @param scope is the scope (fixed or dynamic) of the current statistics-discovery query
     * @return an array containing the filter queries
     */
    public static String[] getFilterQueries(Request request, Context context, DSpaceObject scope) {
        try {
            List<String> allFilterQueries = new ArrayList<String>();
            List<String> filterTypes = getRepeatableParameters(request, "filtertype");
            List<String> filterOperators = getRepeatableParameters(request, "filter_relational_operator");
            List<String> filterValues = StatisticsDiscoveryUIUtils.getRepeatableParametersWithRegex(request,  "^filter(_\\d+)?$");

            for (int i = 0; i < filterTypes.size(); i++) {
                String filterType = filterTypes.get(i);
                String filterOperator = filterOperators.get(i);
                String filterValue = filterValues.get(i);

                if(StringUtils.isNotBlank(filterValue)){
                	DiscoverySearchFilter discoveryFilter = StatisticsSearchUtils.getDiscoveryFilterByName(filterType, scope);
                	if(discoveryFilter != null && discoveryFilter.getFilterType().equals(StatisticsDiscoveryCombinedFilterFacet.FILTER_TYPE_COMBINED_FACET)) {
                		//Formamos el 'fq' de la forma '(combinedField_1:XXX OR combinedField_2:XXX OR ...)'
                		StringBuilder combinedFilterQuery = new StringBuilder();
                		for (Iterator<String> metadataFields = discoveryFilter.getMetadataFields().iterator(); metadataFields.hasNext();) {
                			combinedFilterQuery.append(statisticsSearchService.toFilterQuery(context, metadataFields.next(), filterOperator, filterValue).getFilterQuery());
                			if(metadataFields.hasNext()) {
                				combinedFilterQuery.append(" OR ");
                			}
                		}
                		allFilterQueries.add(combinedFilterQuery.toString());
                	} else {
                		allFilterQueries.add(statisticsSearchService.toFilterQuery(context, (filterType.equals("*") ? "" : filterType), filterOperator, filterValue).getFilterQuery());
                	}
                }
            }
            
            /**
             * TODO: ¿Será necesario extrear este if a un método para poder así obtener independientemente los filter queries para el contexto Discovery? 
             * Solr posee un mecanismo para evitar que las consultas muy largas  degraden la performance. Para solventar esto, existe la propiedad <maxBooleanClauses> que por defecto viene seteada a 1024.
             * Al pasarse de este límite, SOLR falla y retorna un exit code 400 cuando se hace el request al servidor (ver https://stackoverflow.com/questions/32213657/solr-post-request-returns-400-java-when-size-of-request-is-too-large)
             * Cuando sucede lo anterior, la consulta al core de 'statistics' retorna resultados vacios...
             * 
             * Manejando esto de los filterQueries de los DSO de forma independiente, quizas evite futuros problemas si tengo mas de <maxBooleanClauses> condiciones.
             * Si se partiera la consulta en muchos pedazos, y se realizaran multiples consultas, unificando luego los resultados, quizás sería mas eficiente.
             * 
             */
            //Verificamos si el contexto o el scope de la consulta es derivado de una resultado de búsqueda de Discovery, aunque sólamente es válido si no existe un scope fijo o dinámico (handle/XX/YY/statistics-discover ó scope=XX/YY, respectivamente)... 
            if(scope == null) {
            	if(isDiscoveryDerivedScope(request)) {
            		StringBuilder filterQuery = new StringBuilder();
            		List<String> scopeUUIDs = getSpecificDiscoveryContext(request); 
            		for (Iterator<String> uuids = scopeUUIDs.iterator(); uuids.hasNext();) {
            			filterQuery.append("(");
            			DSpaceObject dso = getDSOByUUID(context, uuids.next());
            			filterQuery.append(statisticsSearchService.filterQueryForDSO(dso));
            			filterQuery.append(")");
            			if(uuids.hasNext()) {
            				filterQuery.append(" OR ");
            			}
            		}
            		if(filterQuery.toString() != null || !filterQuery.toString().isEmpty()) {
            			allFilterQueries.add(filterQuery.toString());
            		}
            	}
            }

            return allFilterQueries.toArray(new String[allFilterQueries.size()]);
        }
        catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            return new String[0];
        }
    }

    /**
     * Dada un filtro, un operador y un valor, se procede quitar esos parámetros de la URL pasada como parámetro.
     * Por ejemplo: si se tiene la URL "http://localhost:8088/tesina_dspace_cic/statistics-discover?filtertype_0=ip&filter_relational_operator_0=equals&filter_0=163.10.34.195&filtertype=countryCode&filter_relational_operator=equals&filter=AR"
     * y se reciben los parametros (filtro:"ip", operador:"equals", valor:"163.10.34.195"), la URL resultante sería "http://localhost:8088/tesina_dspace_cic/statistics-discover?&filtertype=countryCode&filter_relational_operator=equals&filter=AR"
     * 
     * @param url	es la URL a la que se le van a quitar los parámetros 
     * @param filterName	es el nombre del filtro
     * @param filterOperator	es el nombre del operador
     * @param filterValue	es el valor del filtro
     * @return	la URL sin los parámetros
     */
    public static String getUrlWithoutFilter(String url, String filterName, String filterOperator, String filterValue) {
        Pattern filterOperatorPattern;
        Pattern filterValuePattern;
        Matcher matcherOperator;
        Matcher matcherValue;
        String filterNumber;
        String filterOperatorPatternString;
        String filterValuePatternString;
        String result = url;
        
        //Creamos una expresión regular de 3 grupos para buscar por nombre de filtro, donde la expresión del medio (2) indica el numero relativo a los filtros (o índice), por ejemplo 'filtertype_6' resultaria en 6...
    	//El número de filtro o grupo del medio puede ser opcional para casos donde solo haya un único filtro aplicado.
    	String filterTypePatternString = "(filtertype_?)(\\d+)?=("+filterName+")";
        Pattern filterTypePattern = Pattern.compile(filterTypePatternString);
        Matcher matcherType = filterTypePattern.matcher(url);
        
        //Buscamos todas las expresiones que hagan match y vamos iterando sobre cada uno de los índices
        while(matcherType.find()){
        	filterNumber = matcherType.group(2);
        	if(filterNumber != null) {
        		//Entonces hay uno o mas filtros de la forma 'filtertype_N=XXX'
        		filterOperatorPatternString = "filter_relational_operator_"+filterNumber+"="+filterOperator+"&?";
        		filterValuePatternString = "filter_"+filterNumber+"="+filterValue+"&?";
        	} else {
        		//Sino, hay un único filtro de la forma 'filtertype=XXX'
        		filterOperatorPatternString = "filter_relational_operator="+filterOperator+"&?";
        		filterValuePatternString = "filter="+filterValue+"&?";
        	}
        	filterOperatorPattern = Pattern.compile(filterOperatorPatternString);
        	matcherOperator= filterOperatorPattern.matcher(url);
        	filterValuePattern = Pattern.compile(filterValuePatternString);
        	matcherValue = filterValuePattern.matcher(url);
        	//Buscamos el operador y valor asociado al índice actual, donde por cada índice debería existir sólo uno unico operador y valor
        	if(matcherOperator.find() && matcherValue.find()) {
        		//Si concuerdan el filterName, el filterOperator y el filterValue, entonces tenemos que borrar explícitimante esos valores
        		String specificFilterTypePatternString = "filtertype_" + filterNumber + "="+filterName;
        		result = applyReplacement(Pattern.compile(specificFilterTypePatternString), result, "");
        		result = applyReplacement(filterOperatorPattern, result, "");
        		result = applyReplacement(filterValuePattern, result, "");
        		break;
        	}
        }
        return result;
    }
    
    /**
     * Esta función aplica un patrón de expresión regular sobre un texto objetivo y reemplaza todas las ocurrencias del patrón con el reemplazo indicado.
     * @param pattern	es el patrón de expresión regular
     * @param targetToReplace	es el texto objetivo sobre el que se aplica la expresión regular
     * @param replacement	es el texto usada para reemplazar las ocurrencias del patrón indicado
     * @return
     */
    private static String applyReplacement(Pattern pattern, String targetToReplace, String replacement) {
    	StringBuffer sb = new StringBuffer();
    	Matcher matcher = pattern.matcher(targetToReplace);
    	while(matcher.find()) {
    		matcher.appendReplacement(sb, replacement);
    	}
    	matcher.appendTail(sb);
    	return sb.toString();
    }
    
    public static List<String> getRepeatableParameters(Request request, String prefix){
        TreeMap<String, String> result = new TreeMap<String, String>();

        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameter = (String) parameterNames.nextElement();
            if(parameter.startsWith(prefix)){
                result.put(parameter, request.getParameter(parameter));
            }
        }
        return new ArrayList<String>(result.values());
    }
    
    /**
     * Busca nombres de parámetros (parámetro es 'nombre=valor') que tengan coincidencia con la expresión regular pasada como parámetro.
     * @param request	es el objeto que representa el petición HTTP
     * @param regex		es la expresión regular
     * @return	la lista de valores que tengan coincidencia con la expresión regular pasada como parámetro
     */
    public static List<String> getRepeatableParametersWithRegex(Request request, String regex){
        TreeMap<String, String> result = new TreeMap<String, String>();

        Enumeration parameterNames = request.getParameterNames();
        Pattern regexPattern = Pattern.compile(regex);
        while (parameterNames.hasMoreElements()) {
            String parameter = (String) parameterNames.nextElement();
        	Matcher matcherRegex = regexPattern.matcher(parameter);
            if(matcherRegex.find()){
                result.put(parameter, request.getParameter(parameter));
            }
        }
        return new ArrayList<String>(result.values());
    }

    /**
     * Escape colon-space sequence in a user-entered query, based on the
     * underlying search service. This is intended to let end users paste in a
     * title containing colon-space without requiring them to escape the colon.
     *
     * @param query user-entered query string
     * @return query with colon in colon-space sequence escaped
     */
    public static String escapeQueryChars(String query)
    {
        return StringUtils.replace(query, ": ", "\\: ");
    }
    
    /**
     * Revisamos si la fecha pasada como parámetro es parseable al formato UTC. Por ejemplo, si se recibe la fecha en el formato 
     * hora Argentina '2017-06-10T00:00:00-03:00' (ART), se verificará si se podrá pasar al formato universal '2017-06-10T03:00:00Z' (UTC).
     * 
     * @param dateRepresentation	es el String correspondiente a la fecha que se quiere parsear a UTC
     * @return true si es parseable
     */
    public static boolean isValidDate(String dateRepresentation) {
    	try{
    		new DateTime(dateRepresentation, DateTimeZone.UTC );
    		return true;
		} catch (IllegalArgumentException e) {
			//Cuando se le pasa un String no válido al constructor de DateTime, entonces salta la IllegalArgumentException...
			return false;
		}
    }
    
    /**
     * Determina si el scope de la actual consulta es derivado de "/discover", es decir, si el scope son los DSO resultantes de una consulta realizada en "/discover"..
     * @param request
     * @return
     */
    public static boolean isDiscoveryDerivedScope(Request request) {
    	return (request.getParameter(DISCOVERY_QUERY_PARAM) != null && !request.getParameter(DISCOVERY_QUERY_PARAM).isEmpty());
    }
    
    /**
     * Get the value of the <code>StatisticsDiscoveryUIUtils.DISCOVERY_QUERY_PARAM</code> parameter.
     * @param request
     */
    public static String getDiscoveryQueryParam(Request request) {
    	if(isDiscoveryDerivedScope(request)) {
    		return request.getParameter(DISCOVERY_QUERY_PARAM);
    	}
    	return null;
    }
    
    /**
     * Retorna una lista de identificadores correspondientes al scope derivado desde "/discover".
     * @param request
     * @return
     */
    public static List<String> getSpecificDiscoveryContext(Request request){
    	List<String> result = new ArrayList<String>();
    	if(isDiscoveryDerivedScope(request)) {
    		StringBuilder uuids = getUUIDsFromAspect(request);
    		if(uuids!= null && uuids.length() > 0) {
    			for (String uuid : uuids.toString().split(",")) {
    				result.add(uuid);
    			}
    		}
    	}
    	return result;
    }
    
    /**
	 * Buscamos el DSO (Item, Comunidad ó Colección) asociado a un UUID dado.
	 * @param uuid
	 * @return el DSO (Item, Comunidad ó Colección) asociado a ese uuid, o NULL en caso contrario.
	 * @throws SQLException if the 
	 */
	public static DSpaceObject getDSOByUUID(Context context, String uuid) throws SQLException {
		ItemService itemService = ContentServiceFactoryImpl.getInstance().getItemService();
		CollectionService collectionService = ContentServiceFactoryImpl.getInstance().getCollectionService();
		CommunityService communityService = ContentServiceFactoryImpl.getInstance().getCommunityService();
		try {
			UUID uuid_ = UUID.fromString(uuid);
			if(itemService.find(context, uuid_) != null) {
				return itemService.find(context, uuid_);
			} else if(collectionService.find(context, uuid_) != null){
				return collectionService.find(context, uuid_);
			} else if(communityService.find(context, uuid_) != null){
				return communityService.find(context, uuid_);
			}
			//Si no matchea con ningun DSO, entonces retornamos null
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} 
	}
	
	/**
	 * Connect to "discovery/uuids?<discovery_query_string>" and obtain all the UUIDs related to the discovery query.
	 * @param request
	 * @return StringBuilder containing the response of "discovery/uuids?<discovery_query_string>"
	 */
	private static StringBuilder getUUIDsFromAspect(Request request) {
		HttpURLConnection connection;
		URL url;
		StringBuilder sb = new StringBuilder();
		
		//TODO revisar que esté bien esta nueva lógica!
		String scopeHandle = getDiscoveryScopeParam(request);
		String urlStr;
		if(scopeHandle != null && !scopeHandle.isEmpty()) {
			if(scopeHandle.startsWith("/handle/") || scopeHandle.startsWith("handle/")) {
				scopeHandle = scopeHandle.replaceFirst("/?handle/", "");
			}
			urlStr = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.url") + 
					(isFixedDiscoverScope(request)? ("/handle/" + scopeHandle):"") + "/discover/uuids?" +  getDiscoveryQueryParam(request) + (isVariableDiscoveryScope(request)?("&scope=" + scopeHandle):"");
		} else {
			urlStr = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.url") + "/discover/uuids?" +  getDiscoveryQueryParam(request);
		}
		
//		String urlStr = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.url") + 
//				"/discover" + (isFixedDiscoverScope(request)? ("/handle/" + scope.getHandle()):"") + "/uuids?" +  getDiscoveryQueryParam(request);
		
		try {
			url = new URL(urlStr);
			connection = (HttpURLConnection)url.openConnection(); //this can give 401
			if (200 <= connection.getResponseCode() && connection.getResponseCode() <= 299) {
				//CONEXIÓN EXITOSA!!!
				BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
				String output;
				while ((output = br.readLine()) != null) {
					sb.append(output);
				}
			}
		} catch (MalformedURLException e) {
			log.error("La url '" + urlStr + "' esta mal formada.");
		} catch (IOException e) {
			log.error("No me puedo conectar con la URL '" + urlStr + "'.");
		}
		return sb;
	}
	
	public static boolean existDiscoveryScopeParam(Request request) {
		return (request.getParameter(DISCOVERY_SCOPE_PARAM) != null && !request.getParameter(DISCOVERY_SCOPE_PARAM).isEmpty());
	}
	
	public static boolean isFixedDiscoverScope(Request request) {
		return (existDiscoveryScopeParam(request) && request.getParameter(DISCOVERY_SCOPE_PARAM).startsWith("handle/"));
	}
	
	public static boolean isVariableDiscoveryScope(Request request) {
		return (existDiscoveryScopeParam(request) && !request.getParameter(DISCOVERY_SCOPE_PARAM).startsWith("handle/"));
	}
	
	public static String getDiscoveryScopeParam(Request request) {
		return request.getParameter(DISCOVERY_SCOPE_PARAM);
	}
	
	/**
	 * Retorna una construcción de los parámetros propios de Discovery en el caso de que el contexto de las estadísticas sean los DSO derivados de una consulta en Discovery.
	 * Por ejemplo, retorna 'discovery_query=&lt;query&gt;&discovery_scope=&lt;scope&gt;'.
	 * @throws UnsupportedEncodingException 
	 */
	public static String getAllDiscoveryQueryParams(Request request) throws UnsupportedEncodingException {
		StringBuilder discoveryQueryScopeParams = new StringBuilder();
		if(StatisticsDiscoveryUIUtils.isDiscoveryDerivedScope(request)) {
        	discoveryQueryScopeParams.append(StatisticsDiscoveryUIUtils.DISCOVERY_QUERY_PARAM);
        	discoveryQueryScopeParams.append("=");
        	discoveryQueryScopeParams.append(URLEncoder.encode(StatisticsDiscoveryUIUtils.getDiscoveryQueryParam(request), "UTF-8"));
        	if(StatisticsDiscoveryUIUtils.existDiscoveryScopeParam(request)) {
        		discoveryQueryScopeParams.append("&");
        		discoveryQueryScopeParams.append(StatisticsDiscoveryUIUtils.DISCOVERY_SCOPE_PARAM);
        		discoveryQueryScopeParams.append("=");
        		discoveryQueryScopeParams.append(URLEncoder.encode(StatisticsDiscoveryUIUtils.getDiscoveryScopeParam(request), "UTF-8"));
        	}
        }
		return discoveryQueryScopeParams.toString();
	}
	
}

