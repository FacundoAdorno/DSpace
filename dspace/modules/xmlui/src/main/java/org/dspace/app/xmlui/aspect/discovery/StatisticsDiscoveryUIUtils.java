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
import org.dspace.content.DSpaceObject;
import org.dspace.content.factory.ContentServiceFactoryImpl;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.discovery.StatisticsSearchService;
import org.dspace.discovery.StatisticsSearchUtils;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
    
    public static String DISCOVERY_QUERY_PARAM = "discovery_query";
    public static String SCOPE_DSO_UUIDS_PARAM = "scope_dso_uuids";



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
     * @return an array containing the filter queries
     */
    public static String[] getFilterQueries(Request request, Context context) {
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
                    allFilterQueries.add(statisticsSearchService.toFilterQuery(context, (filterType.equals("*") ? "" : filterType), filterOperator, filterValue).getFilterQuery());
                }
            }
            
            //Verificamos si el contexto que queremos derivar 
            if(isDiscoveryDerivedScope(request)) {
            	String filterQuery;
            	for (String uuid : getSpecificDiscoveryContext(request)) {
					DSpaceObject dso = getDSOByUUID(context, uuid);
					filterQuery = statisticsSearchService.filterQueryForDSO(dso);
					if(filterQuery != null || !filterQuery.isEmpty()) {
						allFilterQueries.add(filterQuery);
					}
				}
            	//TODO terminar, ahora falta conectar desde la vista de SimpleSearch (Discovery) con esta funcionalidad
            	a;
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
        	StringBuffer sb = new StringBuffer();
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
    	return (request.getParameter(DISCOVERY_QUERY_PARAM) != null && !request.getParameter(SCOPE_DSO_UUIDS_PARAM).isEmpty() && request.getParameter(SCOPE_DSO_UUIDS_PARAM)!= null && !request.getParameter(SCOPE_DSO_UUIDS_PARAM).isEmpty());
    }
    
    public static String getDiscoveryQueryParam(Request request) {
    	if(request.getParameter(DISCOVERY_QUERY_PARAM) != null && !request.getParameter(SCOPE_DSO_UUIDS_PARAM).isEmpty()) {
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
    		for (String uuid : request.getParameter(SCOPE_DSO_UUIDS_PARAM).split(",")) {
				result.add(uuid);
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
    
}

