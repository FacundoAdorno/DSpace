/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.List;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.joda.time.DateTime;

/**
 * Filtra por accesos en determinados lapsos de tiempoCI	
 * 
 * @author gordo
 *
 */
public class AccessInTime extends RuleType {
	
	private DateTime timeIterator;
	
	private DateTime endDate;
	
	public void getSettings(String prefix) throws MissingArgumentException
	{		
		settings.put("startDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".startDate"));
    	settings.put("endDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".endDate"));
    	settings.put("type", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".type"));
    	settings.put("count", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".count"));
    	settings.put("gap", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".gap"));
    	
    	timeIterator  = new DateTime(settings.get("startDateStr"));
		endDate = new DateTime(settings.get("endDateStr"));
    	
	}
	
	public void buildQuery()
	{	
		solrQuery.setQuery("time:["+timeIterator.toString()+"Z TO "+getDateLimit().toString()+"Z]");
		solrQuery.setParam("type", "/"+settings.get("type")+"/");
		solrQuery.setFacet(true);
		solrQuery.addFacetField("ip");
		solrQuery.addFacetField("type");
		solrQuery.setFacetMinCount(Integer.parseInt(settings.get("count")));
	}
	
	
	/*
	 *la fecha límite varia dependiendo de si se está analizando un período grande de tiempo, por ejemplo, los accesos del ultimo año
	 *o si se estan analizando lapsos de tiempo pequeños dentro de un lapso mayor, por ejemplo, los accesos hora a hora durante
	 *el ultimo año
	 */
	public DateTime getDateLimit(){
		return ("0".equals(settings.get("gap"))) ? endDate : timeIterator.plusHours(Integer.parseInt(settings.get("gap")));
	}
	
	public void runQuery() throws SolrServerException{
		QueryResponse response = solrServer.query(solrQuery);
		
    	if(response.getFacetField("ip").getValues().size() > 0)
    	{
    		List<Count> list = response.getFacetField("ip").getValues();
    		for(Count c: list)
    		{
    			String[] str = c.toString().split(" ");
    			String ip = str[0];
    			String access = str[1]
    					.replace("(", "")
    					.replace(")", "");
    			    			
    			String report = "ip: "+ip+" date: "+timeIterator.toString()+" and "+getDateLimit().toString()+" got "+access+" access \n";
    			ipFoundList.add(new CandidateIP(ip, Integer.parseInt(access), report));
    		}
    	}
	}
	
	public List<CandidateIP> eval() throws SolrServerException 
	{
		if("0".equals(settings.get("gap"))){
    		runQuery();
		}else{
			while(timeIterator.isBefore(endDate)){
				runQuery();
				timeIterator = timeIterator.plusHours(Integer.parseInt(settings.get("gap")));
	        	solrQuery.setQuery("time:["+timeIterator.toString()+"Z TO "+timeIterator.plusHours(Integer.parseInt(settings.get("gap"))).toString()+"Z]");
			}
		}
		
		return ipFoundList;
		
	}
}