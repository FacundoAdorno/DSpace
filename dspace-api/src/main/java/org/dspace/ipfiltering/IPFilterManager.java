/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.statistics.factory.StatisticsServiceFactory;
import org.dspace.statistics.service.SolrLoggerService;

public class IPFilterManager
{

	private SolrQuery premadeSolrQuery = new SolrQuery();
	
	private static final Logger log = Logger.getLogger(IPFilterManager.class);
	
	/**
	 * whitelist
	 */
	private String[] whitelist = {};
	
	/**
	 * Rules to be executed
	 */
	private String[] rules = {};
	
	/**
	 * Lista de ips candidatas con su valor correspondiente a la probabilidad de que sea un bot
	 */
	private HashMap<String, CandidateIP> ipList = new HashMap<String, CandidateIP>();
	
	/**
	 * How to show results
	 * 
	 */
	private ResultViewer viewer;
	
	public IPFilterManager(String[] commandLineRules) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		rules = commandLineRules;
		
		whitelist = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty("ipFilter.whitelist");
		if(whitelist.length > 0){
			String filterQuery = Arrays.toString(whitelist)
					.replaceAll(", ", " -")
					.replace("[", "-")
					.replace("]", "");
	    	premadeSolrQuery.addFilterQuery("ip:("+filterQuery+")");
		}
		premadeSolrQuery.addFilterQuery("-isBot: true");
		
		//Set ResultViewer
		String viewerStr = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("ipFilter.resultViewer");
		if ((viewerStr == null) || ("".equals(viewerStr))){
			viewer = new DetailedView();
		}else{
			viewer = (ResultViewer) Class.forName("org.dspace.ipfiltering."+viewerStr).newInstance();
		};

	}
	
	public void addCandidate(CandidateIP ip)
	{
		if(ipList.containsKey(ip.getIp()))
		{
			ipList.get(ip.getIp())
				.addOccurrence(ip.getProbabilities())
				.addToReport(ip.getReport());
		}
		else
		{
			ipList.put(ip.getIp(), ip);
		}
	}	
	
	private ArrayList<Rule> getRules() throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		ArrayList<Rule> rulesList = new ArrayList<Rule>();
		
		//if there are no rules specified on command line, get rules from configuration file
		if(rules.length == 0){
			rules = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty("ipFilter.rules");
		}
		//if a ruleset is specified in command line and is a ruleset
		else if(rules[0].startsWith("ruleSet.")){
			rules = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty(rules[0]+".rules");
		}	
		
		
		if ((rules == null) || ("".equals(rules[0])))
        {
			log.warn("no rules specified");
        }else{
        	//create rule instances list
    		for(String rule: rules){
    			String ruleType = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(rule+".ruleType");
    			rulesList.add(new Rule(rule, ruleType, ipList, premadeSolrQuery));
    		}
        }
		
		return rulesList;
	}
	
	
	public void filter() throws SolrServerException, InstantiationException, IllegalAccessException, ClassNotFoundException, MissingArgumentException
	{
		ArrayList<Rule> rulesList = this.getRules();
		
		for(Rule rule: rulesList)
		{
			List<CandidateIP> ruleIpList = rule.run();
			for (CandidateIP ip: ruleIpList){
				addCandidate(ip);
			} 
		}
		
		//Show results
		if(ipList.size() > 0){
			if(DSpaceServicesFactory.getInstance().getConfigurationService().getBooleanProperty("markBots", false)){
				//Marca los ip como bot
				SolrLoggerService solrLoggerService = StatisticsServiceFactory.getInstance().getSolrLoggerService();
				for(CandidateIP candidate : ipList.values()){
					solrLoggerService.markRobotsByIP(candidate.getIp());					
				}
			}
			log.info(viewer.buildView(ipList));
		}
		else
		{
			log.info("Bot-detection - No result found");
		}
	}
}