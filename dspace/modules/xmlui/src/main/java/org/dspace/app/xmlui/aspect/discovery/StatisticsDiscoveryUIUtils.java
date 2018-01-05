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
import org.dspace.core.Context;
import org.dspace.discovery.StatisticsSearchService;
import org.dspace.discovery.StatisticsSearchUtils;
import org.dspace.services.factory.DSpaceServicesFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class StatisticsDiscoveryUIUtils {

    private static StatisticsSearchService searchService = null;

    static {
        searchService = StatisticsSearchUtils.getStatisticsSearchService();
    }



    /**
     * Returns a list of the filter queries for use in rendering pages, creating page more urls, ....
     * @param request user's request.
     * @return an array containing the filter queries
     */
    public static Map<String, String[]> getParameterFilterQueries(Request request) {
        Map<String, String[]> fqs = new HashMap<String, String[]>();

        List<String> filterTypes = getRepeatableParameters(request, "filtertype");
        List<String> filterOperators = getRepeatableParameters(request, "filter_relational_operator");
        List<String> filterValues = getRepeatableParameters(request, "filter");

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
            List<String> filterValues = getRepeatableParameters(request, "filter");

            for (int i = 0; i < filterTypes.size(); i++) {
                String filterType = filterTypes.get(i);
                String filterOperator = filterOperators.get(i);
                String filterValue = filterValues.get(i);

                if(StringUtils.isNotBlank(filterValue)){
                    allFilterQueries.add(searchService.toFilterQuery(context, (filterType.equals("*") ? "" : filterType), filterOperator, filterValue).getFilterQuery());
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
}

