/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

/**
 * @deprecated
 */
package org.dspace.ipfiltering;


/*
 * NOTA
 * 
 * 
 * Esta clase no se usa, la hice para probar ejecutar las consultas de forma concurrente
 * 
 */


import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.joda.time.DateTime;

import java.util.List;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.dspace.core.Constants;
public class ASTQueryThread extends Thread {

	private DateTime startDate;
	
	private DateTime endDate;
	
	private String type;
	
	private String count;
	
	private Rule ownerRule;
	
	private SolrQuery query;
		
	public ASTQueryThread(DateTime startDate, DateTime endDate, String type, String count, Rule ownerRule, SolrQuery solrQuery) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.type = type;
		this.count = count;
		this.ownerRule = ownerRule;
		this.query = solrQuery;
	}

	public SolrQuery getQuery()
	{
		return query;
	}
	
	public void setQuery(SolrQuery query)
	{
		this.query = query;
	}
	
//	public void run()
//	{
//		DateTime timeIterator = startDate;
//		while (!timeIterator.equals(endDate))
//    	{
//    		query.setFacet(true);
//    		query.setParam("facet.field", "ip");
//    		query.setParam("facet.mincount", count);
//    		query.setQuery("time:["+timeIterator.toString()+"Z TO "+timeIterator.plusHours(1).toString()+"Z]-isBot:true AND type:"+type);
//        	
//        	QueryResponse response;
//			try {
//				response = RuleType.getSolrServerInstance().query(query);
//				List<Count> list = response.getFacetFields().get(0).getValues();
//	        	if(response.getFacetFields().get(0).getValues().size() > 0)
//	        	{
//	        		for(Count c: list)
//	        		{
//	        			String[] str = c.toString()
//	        					.replace("[", "")
//	        					.replace("]", "")
//	        					.split(" ");
//	        			String ip = str[0];
//	        			String access = str[1]
//	        					.replace("(", "")
//	        					.replace(")", "");
//	        			String report = "ip: "+ip+" date: "+timeIterator.getYear()+"-"+timeIterator.getMonthOfYear()+"-"+timeIterator.getDayOfMonth()+" between "+timeIterator.getHourOfDay()+" and "+timeIterator.plusHours(1).getHourOfDay()+" got "+access+" access - type: "+Constants.typeText[Integer.valueOf(type)];
//	        			ownerRule.addCandidate(ip, Integer.parseInt(access), report);
//	        		}		
//	        	}
//	        	timeIterator = timeIterator.plusHours(1);
//			} catch (SolrServerException e) {
//				e.printStackTrace();
//			}
//    	}
//	}
	
}
