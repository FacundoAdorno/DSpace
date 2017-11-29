/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SpellingParams;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.discovery.configuration.DiscoveryConfigurationParameters;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.storage.rdbms.DatabaseUtils;

public class StatisticsSolrServiceImpl extends SolrServiceImpl{
	
	private HttpSolrServer solr;
	
	private static final Logger log = Logger.getLogger(StatisticsSolrServiceImpl.class);
	
	@Override
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
				// Dummy/test query to search for Item (type=2) of ID=1
//                    SolrQuery solrQuery = new SolrQuery()
//                            .setQuery(RESOURCE_TYPE_FIELD + ":2 AND " + RESOURCE_ID_FIELD + ":1");
				// Only return obj identifier fields in result doc
//                    solrQuery.setFields(RESOURCE_TYPE_FIELD, RESOURCE_ID_FIELD);
//                    solr.query(solrQuery);
            }
            else
            {
                log.error("Error while initializing solr, invalid url: " + solrService);
            }
        }

        return solr;
	}
	
	
	 //========== SearchService implementation
    @Override
    public DiscoverResult search(Context context, DiscoverQuery query) throws SearchServiceException
    {
        return search(context, query, false);
    }

    @Override
    public DiscoverResult search(Context context, DSpaceObject dso,
            DiscoverQuery query)
            throws SearchServiceException
    {
        return search(context, dso, query, false);
    }

    @Override
    public DiscoverResult search(Context context, DSpaceObject dso, DiscoverQuery discoveryQuery, boolean includeUnDiscoverable) throws SearchServiceException {
        if(dso != null)
        {
//            if (dso instanceof Community)
//            {
//                discoveryQuery.addFilterQueries("location:m" + dso.getID());
//            } else if (dso instanceof Collection)
//            {
//                discoveryQuery.addFilterQueries("location:l" + dso.getID());
//            } else if (dso instanceof Item)
//            {
//                discoveryQuery.addFilterQueries(HANDLE_FIELD + ":" + dso.getHandle());
//            }
        }
        return search(context, discoveryQuery, includeUnDiscoverable);

    }
	
	@Override
	protected SolrQuery resolveToSolrQuery(Context context, DiscoverQuery discoveryQuery, boolean includeUnDiscoverable)
    {
        SolrQuery solrQuery = new SolrQuery();

        String query = "*:*";
        if(discoveryQuery.getQuery() != null)
        {
        	query = discoveryQuery.getQuery();
		}

        solrQuery.setQuery(query);

        // Add any search fields to our query. This is the limited list
        // of fields that will be returned in the solr result
        for(String fieldName : discoveryQuery.getSearchFields())
        {
            solrQuery.addField(fieldName);
        }
        // Also ensure a few key obj identifier fields are returned with every query
//        solrQuery.addField(HANDLE_FIELD);
//        solrQuery.addField(RESOURCE_TYPE_FIELD);
//        solrQuery.addField(RESOURCE_ID_FIELD);

//        if(discoveryQuery.isSpellCheck())
//        {
//            solrQuery.setParam(SpellingParams.SPELLCHECK_Q, query);
//            solrQuery.setParam(SpellingParams.SPELLCHECK_COLLATE, Boolean.TRUE);
//            solrQuery.setParam("spellcheck", Boolean.TRUE);
//        }
//
//        if (!includeUnDiscoverable)
//        {
//        	solrQuery.addFilterQuery("NOT(withdrawn:true)");
//        	solrQuery.addFilterQuery("NOT(discoverable:false)");
//		}

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
                String field = transformFacetField(facetFieldConfig, facetFieldConfig.getField(), false);
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

//        if(0 < discoveryQuery.getHitHighlightingFields().size())
//        {
//            solrQuery.setHighlight(true);
//            solrQuery.add(HighlightParams.USE_PHRASE_HIGHLIGHTER, Boolean.TRUE.toString());
//            for (DiscoverHitHighlightingField highlightingField : discoveryQuery.getHitHighlightingFields())
//            {
//                solrQuery.addHighlightField(highlightingField.getField() + "_hl");
//                solrQuery.add("f." + highlightingField.getField() + "_hl." + HighlightParams.FRAGSIZE, String.valueOf(highlightingField.getMaxChars()));
//                solrQuery.add("f." + highlightingField.getField() + "_hl." + HighlightParams.SNIPPETS, String.valueOf(highlightingField.getMaxSnippets()));
//            }
//
//        }

        //Add any configured search plugins !
        List<StatisticsSolrServiceSearchPlugin> solrServiceSearchPlugins = DSpaceServicesFactory.getInstance().getServiceManager().getServicesByType(StatisticsSolrServiceSearchPlugin.class);
        for (StatisticsSolrServiceSearchPlugin searchPlugin : solrServiceSearchPlugins)
        {
            searchPlugin.additionalSearchParameters(context, discoveryQuery, solrQuery);
        }
        return solrQuery;
    }
	
	@Override
	protected String transformFacetField(DiscoverFacetField facetFieldConfig, String field, boolean removePostfix)
    {
		//Siempre retornamos el nobre del campo...
		return field;
		
    }
}
