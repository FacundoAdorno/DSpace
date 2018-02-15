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
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.discovery.SearchService;
import org.dspace.services.factory.DSpaceServicesFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class DiscoveryUIUtils {

    private static SearchService searchService = null;

    static {
        searchService = DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName(SearchService.class.getName(),SearchService.class);
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
    
    /**
     * Returns the parameters used so it can be used in a url
     * @param request the cocoon request
     * @return the parameters used on this page
     */
    public static String retrieveParameters(Request request) throws UnsupportedEncodingException, UIException {
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
    
    /**
     * Encode the given string for URL transmission.
     * 
     * @param unencodedString
     *            The unencoded string.
     * @return The encoded string
     * @throws org.dspace.app.xmlui.utils.UIException if the encoding is unsupported.
     */
    public static String encodeForURL(String unencodedString) throws UIException
    {
    	if (unencodedString == null)
        {
            return "";
        }

        try
        {
            return URLEncoder.encode(unencodedString,Constants.DEFAULT_ENCODING);
        }
        catch (UnsupportedEncodingException uee)
        {
            throw new UIException(uee);
        }

    }
}
