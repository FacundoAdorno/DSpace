/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.dspace.services.factory.DSpaceServicesFactory;

/**
 * 
 * Esta clase realiza la busqueda por subgrupo de ip xxx.xxx.xxx.---
 * 
 * @author gordo
 *
 */
public class SubnetSearch extends RuleType{

	private Map<String, List<String>> occurrences = new HashMap<String, List<String>>();
	
	@Override
	public void getSettings(String prefix) {
		this.settings.put("cant", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".cant"));
	}

	@Override
	protected void buildQuery() {
		solrQuery.setRows(0);
    	solrQuery.setFacet(true);
    	solrQuery.setParam("facet.field", "ip");
	}
	
	@Override
	public List<CandidateIP> eval() throws SolrServerException {
				
    	QueryResponse response = solrServer.query(solrQuery);
    	
    	List<Count> list = response.getFacetFields().get(0).getValues();
    	for(Count c: list)
    	{
    		String[] str = c.toString().split(" ");
    		String ip = str[0];
    		
    		String[] auxIP = ip.split("\\.");
    		String subIP = auxIP[0]+"."+auxIP[1]+"."+auxIP[2];

    		List<String> subIPList = new ArrayList<String>();;
    		if(occurrences.containsKey(subIP))
    		{
    			subIPList = occurrences.get(subIP);
    			subIPList.add(ip);
    			occurrences.put(subIP, subIPList);
    		}
    		else
    		{
    			subIPList.add(ip);
    			occurrences.put(subIP, subIPList);
    		}	
    	}
    	for(Entry<String, List<String>> ent: occurrences.entrySet())
    	{
    		List<String> subIPList = ent.getValue();
    		Integer ocurs = subIPList.size();
    		if(ocurs >= Integer.valueOf(settings.get("cant")))
    		{
    			for(String ip: subIPList)
    			{
    				String report = "presente en la subnet "+ent.getKey()+" junto con otra/s "+(ocurs-1)+" ips";
    				ipFoundList.add(new CandidateIP(ip, ocurs, report));
    			}
    		}
    	}
		return ipFoundList;
	}
}
