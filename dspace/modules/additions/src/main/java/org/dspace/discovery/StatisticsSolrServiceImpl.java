package org.dspace.discovery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.FacetParams;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.discovery.configuration.DiscoveryConfigurationParameters;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.core.Constants;

public class StatisticsSolrServiceImpl implements StatisticsSearchService {

	private Logger log = Logger.getLogger(StatisticsSolrServiceImpl.class);
	private HttpSolrServer solr;
	protected static String STATISTICS_TYPE_FIELD = "statistics_type";
	protected static String DSO_TYPE_FIELD = "type";
	protected static String SCOPE_TYPE_FIELD = "scopeType";
	protected static String IS_BOT_FIELD = "isBot";
	
	@Override
	public StatisticsDiscoverResult search(Context context, DiscoverQuery query)
			throws StatisticsSearchServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatisticsDiscoverResult search(Context context, DSpaceObject dso, DiscoverQuery discoveryQuery)
			throws StatisticsSearchServiceException {

		if(dso != null)
        {
            if (dso instanceof Community)
            {
            	Community comm = (Community)dso;
            	String owningCommFilter = "owningComm:(" + comm.getID() + " OR " + comm.getLegacyId() + ")";
            	//Incluimos este objeto al contexto también...
            	String commFilter = "id:(" + comm.getID() + " OR (" + comm.getLegacyId() + "AND type:" + Constants.COMMUNITY + "))";
                discoveryQuery.addFilterQueries(owningCommFilter + " OR " + commFilter);
            } else if (dso instanceof Collection)
            {
            	Collection coll = (Collection)dso;
            	String owningCollFilter = "owningColl:(" + coll.getID() + " OR " + coll.getLegacyId() + ")";
            	//Incluimos este objeto al contexto también...
            	String collFilter = "id:(" + coll.getID() + " OR (" + coll.getLegacyId() + "AND type:" + Constants.COLLECTION + "))";
                discoveryQuery.addFilterQueries(owningCollFilter + " OR " + collFilter);
            } else if (dso instanceof Item)
            {
            	Item item = (Item)dso;
            	discoveryQuery.addFilterQueries("id:(" + item.getID() + " OR (" + item.getLegacyId() + "AND type:" + Constants.ITEM + "))");
            }
        }
		
		return search(context, discoveryQuery, false);
	}

	@Override
	public StatisticsDiscoverResult search(Context context, DiscoverQuery discoveryQuery, boolean includeBots)
			throws StatisticsSearchServiceException {
		try {
            if(getSolr() == null){
                return new StatisticsDiscoverResult();
            }
            SolrQuery solrQuery = resolveToSolrQuery(context, discoveryQuery, includeBots);


            QueryResponse queryResponse = getSolr().query(solrQuery, SolrRequest.METHOD.POST);
            return retrieveResult(context, discoveryQuery, queryResponse);

        } catch (Exception e)
        {
            throw new org.dspace.discovery.StatisticsSearchServiceException(e.getMessage(),e);
        }
	}

	public StatisticsDiscoverResult search(Context context, DSpaceObject dso, DiscoverQuery query,
			boolean includeBots) throws StatisticsSearchServiceException {
		// TODO Auto-generated method stub
		return null;
	}
	
	///////////////////
	//
	//////////////////
	
	protected HttpSolrServer getSolr() {
		if ( solr == null)
        {
            String solrService = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("solr-statistics.server");

            UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
            if (urlValidator.isValid(solrService)||ConfigurationManager.getBooleanProperty("discovery","solr.url.validation.enabled",true))
            {
                log.debug("Solr URL: " + solrService);
				solr = new HttpSolrServer(solrService);

				solr.setBaseURL(solrService);
				solr.setUseMultiPartPost(true);
            }
            else
            {
                log.error("Error while initializing solr, invalid url: " + solrService);
            }
        }

        return solr;
	}
	
	protected SolrQuery resolveToSolrQuery(Context context, DiscoverQuery discoveryQuery, boolean includeBots)
    {
        SolrQuery solrQuery = new SolrQuery();

        String query = "*:*";
        if(discoveryQuery.getQuery() != null)
        {
        	query = discoveryQuery.getQuery();
		}

        solrQuery.setQuery(query);

        if(discoveryQuery.getSearchFields().size() > 0) {
        	// Add any search fields to our query. This is the limited list
        	// of fields that will be returned in the solr result
        	for(String fieldName : discoveryQuery.getSearchFields())
        	{
        		solrQuery.addField(fieldName);
        	}
        	// Also ensure a few key obj identifier fields are returned with every query
        	solrQuery.addField(STATISTICS_TYPE_FIELD);
        }

        if (!includeBots)
        {
        	solrQuery.addFilterQuery("NOT("+IS_BOT_FIELD+":true)");
		}

        for (int i = 0; i < discoveryQuery.getFilterQueries().size(); i++)
        {
            String filterQuery = discoveryQuery.getFilterQueries().get(i);
            solrQuery.addFilterQuery(filterQuery);
        }
//        if(discoveryQuery.getDSpaceObjectFilter() != -1)
//        {
//            solrQuery.addFilterQuery(RESOURCE_TYPE_FIELD + ":" + discoveryQuery.getDSpaceObjectFilter());
//        }

        for (int i = 0; i < discoveryQuery.getFieldPresentQueries().size(); i++)
        {
            String filterQuery = discoveryQuery.getFieldPresentQueries().get(i);
            solrQuery.addFilterQuery(filterQuery + ":[* TO *]");
        }

        if(discoveryQuery.getStart() != -1)
        {
            solrQuery.setStart(discoveryQuery.getStart());
        }

        if(discoveryQuery.getMaxResults() != -1)
        {
            solrQuery.setRows(discoveryQuery.getMaxResults());
        }

        if(discoveryQuery.getSortField() != null)
        {
            SolrQuery.ORDER order = SolrQuery.ORDER.asc;
            if(discoveryQuery.getSortOrder().equals(DiscoverQuery.SORT_ORDER.desc))
                order = SolrQuery.ORDER.desc;

            solrQuery.addSortField(discoveryQuery.getSortField(), order);
        }

        for(String property : discoveryQuery.getProperties().keySet())
        {
            List<String> values = discoveryQuery.getProperties().get(property);
            solrQuery.add(property, values.toArray(new String[values.size()]));
        }

        List<DiscoverFacetField> facetFields = discoveryQuery.getFacetFields();
        if(0 < facetFields.size())
        {
            //Only add facet information if there are any facets
            for (DiscoverFacetField facetFieldConfig : facetFields)
            {
                String field = facetFieldConfig.getField();
                solrQuery.addFacetField(field);

                // Setting the facet limit in this fashion ensures that each facet can have its own max
                solrQuery.add("f." + field + "." + FacetParams.FACET_LIMIT, String.valueOf(facetFieldConfig.getLimit()));
                String facetSort;
                if(DiscoveryConfigurationParameters.SORT.COUNT.equals(facetFieldConfig.getSortOrder()))
                {
                    facetSort = FacetParams.FACET_SORT_COUNT;
                }else{
                    facetSort = FacetParams.FACET_SORT_INDEX;
                }
                solrQuery.add("f." + field + "." + FacetParams.FACET_SORT, facetSort);
                if (facetFieldConfig.getOffset() != -1)
                {
                    solrQuery.setParam("f." + field + "."
                            + FacetParams.FACET_OFFSET,
                            String.valueOf(facetFieldConfig.getOffset()));
                }
                if(facetFieldConfig.getPrefix() != null)
                {
                    solrQuery.setFacetPrefix(field, facetFieldConfig.getPrefix());
                }
            }

            List<String> facetQueries = discoveryQuery.getFacetQueries();
            for (String facetQuery : facetQueries)
            {
                solrQuery.addFacetQuery(facetQuery);
            }

            if(discoveryQuery.getFacetMinCount() != -1)
            {
                solrQuery.setFacetMinCount(discoveryQuery.getFacetMinCount());
            }

            solrQuery.setParam(FacetParams.FACET_OFFSET, String.valueOf(discoveryQuery.getFacetOffset()));
        }

        //Add any configured search plugins !
        List<StatisticsSolrServiceSearchPlugin> solrServiceSearchPlugins = DSpaceServicesFactory.getInstance().getServiceManager().getServicesByType(StatisticsSolrServiceSearchPlugin.class);
        for (StatisticsSolrServiceSearchPlugin searchPlugin : solrServiceSearchPlugins)
        {
            searchPlugin.additionalSearchParameters(context, discoveryQuery, solrQuery);
        }
        return solrQuery;
    }
	
	
	protected StatisticsDiscoverResult retrieveResult(Context context, DiscoverQuery query, QueryResponse solrQueryResponse) throws SQLException {
		StatisticsDiscoverResult result = new StatisticsDiscoverResult();

        if(solrQueryResponse != null)
        {
            result.setSearchTime(solrQueryResponse.getQTime());
            result.setStart(query.getStart());
            result.setMaxResults(query.getMaxResults());
            result.setTotalSearchResults(solrQueryResponse.getResults().getNumFound());

            List<String> searchFields = query.getSearchFields();
            for (SolrDocument doc : solrQueryResponse.getResults())
            {
            	//TODO lo hacemos asi?
            	//Chequeamos si no hay ningun campo que se quiere mostrar específicamente, entonces mostramos todos los campos por ahora...
            	if(query.getSearchFields().isEmpty()) {
                	searchFields = new ArrayList<String>(doc.getFieldNames());
                }
                DiscoverResult.SearchDocument resultDoc = new DiscoverResult.SearchDocument();
                //Add information about our search fields
                for (String field : searchFields)
                {
                    List<String> valuesAsString = new ArrayList<String>();
                    for (Object o : doc.getFieldValues(field))
                    {
                        valuesAsString.add(String.valueOf(o));
                    }
                    resultDoc.addSearchField(field, valuesAsString.toArray(new String[valuesAsString.size()]));
                }
                result.addSearchDocument(resultDoc);
            }

            //Resolve our facet field values
            List<FacetField> facetFields = solrQueryResponse.getFacetFields();
            if(facetFields != null)
            {
                for (int i = 0; i <  facetFields.size(); i++)
                {
                    FacetField facetField = facetFields.get(i);
                    DiscoverFacetField facetFieldConfig = query.getFacetFields().get(i);
                    List<FacetField.Count> facetValues = facetField.getValues();
                    if (facetValues != null)
                    {
                        if(facetFieldConfig.getType().equals(DiscoveryConfigurationParameters.TYPE_DATE) && facetFieldConfig.getSortOrder().equals(DiscoveryConfigurationParameters.SORT.VALUE))
                        {
                            //If we have a date & are sorting by value, ensure that the results are flipped for a proper result
                           Collections.reverse(facetValues);
                        }

                        for (FacetField.Count facetValue : facetValues)
                        {
                            String displayedValue = transformDisplayedValue(context, facetField.getName(), facetValue.getName());
                            String field = facetField.getName();
                            String authorityValue = null;
                            String sortValue = facetValue.getName();
                            String filterValue = facetValue.getName();
                            if (StringUtils.isNotBlank(authorityValue))
                            {
                                filterValue = authorityValue;
                            }
                            result.addFacetResult(
                                    field,
                                    new DiscoverResult.FacetResult(filterValue,
                                            displayedValue, authorityValue,
                                            sortValue, facetValue.getCount()));
                        }
                    }
                }
            }

            if(solrQueryResponse.getFacetQuery() != null)
            {
				// just retrieve the facets in the order they where requested!
				// also for the date we ask it in proper (reverse) order
				// At the moment facet queries are only used for dates
                LinkedHashMap<String, Integer> sortedFacetQueries = new LinkedHashMap<String, Integer>(solrQueryResponse.getFacetQuery());
                for(String facetQuery : sortedFacetQueries.keySet())
                {
                    //TODO: do not assume this, people may want to use it for other ends, use a regex to make sure
                    //We have a facet query, the values looks something like: dateissued.year:[1990 TO 2000] AND -2000
                    //Prepare the string from {facet.field.name}:[startyear TO endyear] to startyear - endyear
                    String facetField = facetQuery.substring(0, facetQuery.indexOf(":"));
                    String name = "";
                    String filter = "";
                    if (facetQuery.indexOf('[') > -1 && facetQuery.lastIndexOf(']') > -1)
                    {
                        name = facetQuery.substring(facetQuery.indexOf('[') + 1);
                        name = name.substring(0, name.lastIndexOf(']')).replaceAll("TO", "-");
                        filter = facetQuery.substring(facetQuery.indexOf('['));
                        filter = filter.substring(0, filter.lastIndexOf(']') + 1);
                    }

                    Integer count = sortedFacetQueries.get(facetQuery);

                    //No need to show empty years
                    if(0 < count)
                    {
                        result.addFacetResult(facetField, new DiscoverResult.FacetResult(filter, name, null, name, count));
                    }
                }
            }
        }

        return result;
    }

	protected String transformDisplayedValue(Context context, String field, String value) {
		if(field.equals(DSO_TYPE_FIELD) || field.equals(SCOPE_TYPE_FIELD)) {
			int dsoType = Integer.parseInt(value);
			ArrayList<String> constants = new ArrayList<String>(Arrays.asList(Constants.typeText));
			//chequeamos si el DSO_TYPE_FIELD está dentro de los valores de constantes por las dudas
			if(constants.size() >= dsoType) {
				return constants.get(dsoType);
			}
		}
		
		return value;
	}

	@Override
    public DiscoverFilterQuery toFilterQuery(Context context, String field, String operator, String value) throws SQLException{
        DiscoverFilterQuery result = new DiscoverFilterQuery();

        StringBuilder filterQuery = new StringBuilder();
        if(StringUtils.isNotBlank(field) && StringUtils.isNotBlank(value))
        {
        	//TODO agregar nuevo tipo filtro "String"
        	//TODO modificar lógica para los campos del tipo "String"
            filterQuery.append(field);
            if("equals".equals(operator))
            {
            }
            else if ("authority".equals(operator))
            {
            }
            //Date fields operators
            else if ("fromDate".equals(operator) || "untilDate".equals(operator)) {
            	
            }
            else if ("notequals".equals(operator)
                    || "notcontains".equals(operator)
                    || "notauthority".equals(operator))
            {
                filterQuery.insert(0, "-");
            }
            filterQuery.append(":");
            if("equals".equals(operator) || "notequals".equals(operator))
            {
                //DO NOT ESCAPE RANGE QUERIES !
                if(!value.matches("\\[.*TO.*\\]"))
                {
                    value = ClientUtils.escapeQueryChars(value);
                    filterQuery.append(value);
                }
                else
                {
                	if (value.matches("\\[\\d{1,4} TO \\d{1,4}\\]"))
                	{
                		int minRange = Integer.parseInt(value.substring(1, value.length()-1).split(" TO ")[0]);
                		int maxRange = Integer.parseInt(value.substring(1, value.length()-1).split(" TO ")[1]);
                		value = "["+String.format("%04d", minRange) + " TO "+ String.format("%04d", maxRange) + "]";
                	}
                	filterQuery.append(value);
                }
            }
            //Date fields operators
            else if("fromDate".equals(operator) || "untilDate".equals(operator)) {
            	switch (operator) {
				case "fromDate":
					filterQuery.append("[" + value + " TO *]");
					break;
				case "untilDate":
					filterQuery.append("[* TO " + value + "]");
					break;
				}
            }
            else{
                //DO NOT ESCAPE RANGE QUERIES !
                if(!value.matches("\\[.*TO.*\\]"))
                {
                    value = ClientUtils.escapeQueryChars(value);
                    filterQuery.append("(").append(value).append(")");
                }
                else
                {
                    filterQuery.append(value);
                }
            }

            result.setDisplayedValue(transformDisplayedValue(context, field, value));
        }

        result.setFilterQuery(filterQuery.toString());
        return result;
    }
	
	public String filterQueryForDSO(DSpaceObject dso) {
		return filterQueryForDSO(dso, false);
	}
	
	public String filterQueryForDSOInHierarchy(DSpaceObject dso) {
		return filterQueryForDSO(dso, true);
	}
	
	/**
	 * 
	 * @param dso es el objeto principal relacionado al filtro
	 * @param isHierarchicalQuery	si es 'true' determina si el filtro a retornar debe incluir a los sucesores del DSO pasado como parámetro (owningComm, owningColl, owningItem).
	 * @return
	 */
	private String filterQueryForDSO(DSpaceObject dso, boolean isHierarchicalQuery) {
		if(dso instanceof Item || dso instanceof Collection || dso instanceof Community) {
			//Un DSO puede o no tener un legacyID, dependiendo de la versión de DSpace en la que fue creado
			Integer legacyID = null;
			String dsoType = null;
			String hierarchicalFilterField = null;
			StringBuilder fq = new StringBuilder();
			if(dso instanceof Item) {
				Item item = (Item) dso;
				legacyID = item.getLegacyId();
				dsoType = String.valueOf(item.getType());
				hierarchicalFilterField = "owningItem";
			} else if(dso instanceof Collection) {
				Collection collection = (Collection) dso;
				legacyID = collection.getLegacyId();
				dsoType = String.valueOf(collection.getType());
				hierarchicalFilterField = "owningColl";
			} else if(dso instanceof Community) {
				Community community = (Community) dso;
				legacyID = community.getLegacyId();
				dsoType = String.valueOf(community.getType());
				hierarchicalFilterField = "owningComm";
			}
			//TODO sería mejor utilizar una formatter reemplazando los parametros (por ejemplo, format("El item tiene UUID %s", uuid)) 
			fq.append("id:"); 
			fq.append(dso.getID().toString());
			//Has legacy ID?
			if(legacyID != null) {
				fq.append(" OR (id:");
				fq.append(legacyID.toString());
				fq.append(" AND type:");
				fq.append(dsoType);
				fq.append(")");
			}
			
			if(isHierarchicalQuery) {
				fq.append(" OR (");
				fq.append(hierarchicalFilterField);
				fq.append(":(");
				fq.append(dso.getID().toString());
				if(legacyID != null) {
					fq.append(" OR ");
					fq.append(legacyID.toString());
				}
				fq.append("))");
			}
			
			return fq.toString();
				
		} else {
			return null;
		}
	}
	
	
	

}
