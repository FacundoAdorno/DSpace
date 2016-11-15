/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.services.factory.DSpaceServicesFactory;

public class Rule{
	
	/**
	 * nombre que identifica a la regla
	 */
	protected String name;
	
	/**
	 * algoritmo de búsqueda asignado
	 */
	protected RuleType ruleType;

	/**
	 * lista de pesos
	 */
	protected String[] weights = {};
	
	/**
	 * query prearmada con filtro de whitelisty el -isBot:true
	 */
	protected SolrQuery premadeSolrQuery = new SolrQuery();
	
	public Rule(String name, String ruleType, HashMap<String, CandidateIP> ipList, SolrQuery premadeSolrQuery) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		this.name = name;
		//
		this.ruleType = (RuleType) Class.forName("org.dspace.ipfiltering."+ruleType).newInstance();
		this.premadeSolrQuery = premadeSolrQuery;
		weights = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty(name+".weights");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public SolrQuery getSolrQuery()
	{
		return premadeSolrQuery;
	}
	
	public List<CandidateIP> run() throws SolrServerException, MissingArgumentException 
	{
		List<CandidateIP> ipList = ruleType.run(premadeSolrQuery, this.name);
		for(CandidateIP ip: ipList){
			ip.setProbabilities(this.getWeight(ip.getOccurrences()));
		}
		return ipList;	
	}
	
	public Float getWeight(Integer access){
		Float weight = 1f;
		for(String w: weights){
			if(access > Integer.parseInt(w.split("=")[0]))
			{
				continue;
			}
			weight = Float.parseFloat(w.split("=")[1]);
			break;
		}
		return weight;
	}
	
}
