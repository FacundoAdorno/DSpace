/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 * 
 * busca palabras claves como bot, spider, crawler, etc en los user agent
 * 
 * @author gordo
 *
 */
public class UAChecker extends RuleType{

	private String[] substr;
	
	public void getSettings(String prefix)
	{
		substr = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty(prefix+".substr");
	}
	
	@Override
	protected void buildQuery() {
		String queryString = "";
		if(substr.length > 0)
		{
			for(String str: substr)
			{
				queryString += ".*"+str+".*|";
			}
		}
		solrQuery.setQuery("userAgent:/("+queryString+".*bot.*|.*crawler.*|.*spider.*)/");
		solrQuery.setFacet(true);
    	solrQuery.addFacetField("userAgent");
    	solrQuery.addFacetField("ip");
    	solrQuery.setFacetMinCount(1);
	}
	
	@Override
	public List<CandidateIP> eval() throws SolrServerException {
    	QueryResponse response = solrServer.query(solrQuery);
    	 	
    	if(response.getFacetField("userAgent").getValues().size() > 0)
    	{
    		List<Count> list = response.getFacetField("ip").getValues();
    		String[] str = list.get(0).toString().split(" ");
			String access = str[1]
					.replace("(", "")
					.replace(")", "");
    		FacetField UA = response.getFacetField("userAgent");
    		FacetField ip = response.getFacetField("ip");
    		String report = UA.toString();
    		ipFoundList.add(new CandidateIP(ip.toString(), Integer.parseInt(access), report));
    	}
		return ipFoundList;
	}
}
