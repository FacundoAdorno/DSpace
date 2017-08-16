package org.dspace.content.authority;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dspace.app.requestitem.RequestItemAuthor;
import org.dspace.app.requestitem.RequestItemMetadataStrategy;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.content.authority.service.MetadataAuthorityService;
import org.dspace.content.service.ItemService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.core.service.PluginService;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class CICRequestItemMetadataAuthority extends RequestItemMetadataStrategy{
	
    @Autowired(required = true)
    protected MetadataAuthorityService metadataAuthorityService;
    @Autowired(required = true)
    protected ChoiceAuthorityService choiceAuthorityService;
    @Autowired(required = true)
    protected PluginService pluginService;
    
    private String email;
    private String fullname;
	
	public RequestItemAuthor getRequestItemAuthor(Context context, Item item)
			throws SQLException {
		
		if (emailMetadata != null)
		{
			List<MetadataValue> vals = itemService.getMetadataByMetadataString(item, emailMetadata);
			if (vals.size() > 0)
			{
				email = vals.iterator().next().getValue();
				String[] metadata = emailMetadata.split("\\.");
				String fieldKey=null;
				if(metadata.length == 2){
					fieldKey = metadataAuthorityService.makeFieldKey(metadata[0],metadata[1],null);
				}else if (metadata.length == 3){
					fieldKey = metadataAuthorityService.makeFieldKey(metadata[0],metadata[1],metadata[2]);
				}
				if(choiceAuthorityService.isChoicesConfigured(fieldKey)){
					this.getNameAndEmail(fieldKey, email,item.getOwningCollection(), 0, 0, null);					
				}
				if(email != null && fullname != null){
					RequestItemAuthor author = new RequestItemAuthor(
							fullname, email);
					return author;
				}else{
					return super.getRequestItemAuthor(context, item);
				}
			}
		}
		return super.getRequestItemAuthor(context, item);
	}
	
	public void setEmailMetadata(String emailMetadata) {
		this.emailMetadata = emailMetadata;
	}

	public void setFullNameMetadata(String fullNameMetadata) {
		this.fullNameMetadata = fullNameMetadata;
	}
	
	private void getNameAndEmail(String fieldKey, String text, Collection collection, int offset, int limit, String locale){
		ar.gob.gba.cic.digital.Author_CICBA_Authority cicAuth = (ar.gob.gba.cic.digital.Author_CICBA_Authority) pluginService.getNamedPlugin(ChoiceAuthority.class, "Author_CICBA_Authority");
		ParameterizedSparqlString parameterizedSparqlString = cicAuth.getSparqlEmailByTextQuery(fieldKey, text, locale);
		Query query = QueryFactory.create(parameterizedSparqlString.toString(),	Syntax.syntaxSPARQL_10);
		query.setOffset(offset);
		if (limit == 0)
			query.setLimit(Query.NOLIMIT);
		else
			query.setLimit(limit);
		String endpoint = ConfigurationManager.getProperty("sparql-authorities", "endpoint.url");
		QueryEngineHTTP httpQuery = new QueryEngineHTTP(endpoint, query);
		httpQuery.setAllowDeflate(false);
		httpQuery.setAllowGZip(false);
		Model results=httpQuery.execConstruct();
		ArrayList<String[]> authors = cicAuth.extractNameAndEmailFromAuthors(results);
		if (authors.size() > 0) {
			String[] author=authors.get(0);
			this.email = author[0];
			this.fullname = author[1];
		}
	}

}
