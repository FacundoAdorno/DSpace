package org.dspace.discovery;

import java.sql.SQLException;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;

public interface StatisticsSearchService {
	
	/**
     * Convenient method to call @see #search(Context, DSpaceObject,
     * DiscoverQuery) with a null DSpace Object as scope (i.e. all the
     * repository)
     * 
     * @param context
     *            DSpace Context object.
     * @param query
     *            the discovery query object.
     * @throws StatisticsSearchServiceException if search error
     */
    StatisticsDiscoverResult search(Context context, DiscoverQuery query)
            throws StatisticsSearchServiceException;
    
    /**
     * Convenient method to call @see #search(Context, DSpaceObject,
     * DiscoverQuery, boolean) with includeWithdrawn=false
     * 
     * @param context
     *            DSpace Context object
     * @param dso
     *            a DSpace Object to use as scope of the search (only results
     *            within this object)
     * @param query
     *            the discovery query object
     * @throws StatisticsSearchServiceException if search error
     */
    StatisticsDiscoverResult search(Context context, DSpaceObject dso, DiscoverQuery query)
            throws StatisticsSearchServiceException;
    
    /**
     * 
     * @param context
     *            DSpace Context object.
     * @param query
     *            the discovery query object.
     * @param includeBots
     *            use <code>true</code> to include bots access in the results.
     * @throws StatisticsSearchServiceException if search error
     */
    StatisticsDiscoverResult search(Context context, DiscoverQuery query,
            boolean includeBots) throws StatisticsSearchServiceException;
    
    /**
     * 
     * @param context
     *            DSpace Context object
     * @param dso
     *            a DSpace Object to use as scope of the search (only results
     *            within this object)
     * @param query
     *            the discovery query object
     * @param includeBots
     *            use <code>true</code> to include bots access in the results.
     * 
     * @throws StatisticsSearchServiceException if search error
     */
    StatisticsDiscoverResult search(Context context, DSpaceObject dso, DiscoverQuery query, boolean includeBots) throws StatisticsSearchServiceException;
    
    
    /**
     * Transforms the given string field and value into a filter query
     * @param context the DSpace context
     * @param field the field of the filter query
     * @param value the filter query value
     * @return a filter query
     * @throws SQLException if database error
     */
    DiscoverFilterQuery toFilterQuery(Context context, String field, String operator, String value) throws SQLException;
    
    /**
	 * Creamos un filtro correspondiente al DSO (Item, Colección, Comunidad) pasado como parámetro
	 * @param dso
	 * @return un string representando el filtro relacionado a ese DSO, o NULL en caso de que el DSO no sea un Item, Colección o Comunidad.
	 */
    String filterQueryForDSO(DSpaceObject dso);
    
    /**
	 * Creamos un filtro correspondiente al DSO (Item, Colección, Comunidad) pasado como parámetro y sus 'sucesores' (owningComm, owningColl, owningItem).
	 * @param dso
	 * @param isHierarchicalQuery	si es 'true' determina si el filtro a retornar debe incluir a los sucesores del DSO pasado como parámetro (owningComm, owningColl, owningItem). 
	 * @return un string representando el filtro relacionado a ese DSO y para la obtención de sus sucesores, o NULL en caso de que el DSO no sea un Item, Colección o Comunidad.
	 */
    String filterQueryForDSOInHierarchy(DSpaceObject dso);
}
