package ar.gob.gba.cic.digital;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.dspace.content.authority.Choice;
import org.dspace.content.authority.Choices;
import org.dspace.core.ConfigurationManager;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import ar.edu.unlp.sedici.dspace.authority.SPARQLAuthorityProvider;

public abstract class SimpleSPARQLAuthorityProvider extends SPARQLAuthorityProvider {
	
	protected static final String NS_CIC = "http://www.cic.gba.gov.ar/ns#";

	protected String getSparqlEndpoint() {
		String endpoint = ConfigurationManager.getProperty("sparql-authorities", "endpoint.url");
		if (endpoint != null) {
			return endpoint;
		} else {
			throw new NullPointerException("Missing endpoint configuration.");
		}
	}
	
	protected abstract Choice extractChoice(QuerySolution solution);

	protected Choice[] extractChoicesfromQuery(QueryEngineHTTP httpQuery) {
		List<Choice> choices = new LinkedList<Choice>();
		ResultSet results = httpQuery.execSelect();
		while (results.hasNext()) {
			QuerySolution solution = results.next();
			choices.add(this.extractChoice(solution));
		}
		return choices.toArray(new Choice[0]);
	}
	
	public static void main(String[] args) {

		log.addAppender(new WriterAppender(new SimpleLayout(), System.out));
		log.setLevel(Level.TRACE);
		SPARQLAuthorityProvider s = new SimpleSPARQLAuthorityProvider() {
			
			protected String getSparqlEndpoint() {
				return ConfigurationManager.getProperty("sparql-authorities", "endpoint.url");
			}

			@Override
			protected Choice extractChoice(QuerySolution solution) {
				String expressionValue = solution.getResource("experiment")
						.getURI();
				String pValue = solution.getLiteral("description").getString();
				// print the output to stdout
				return new Choice("0", pValue, expressionValue + "\t" + pValue);
			}

			@Override
			protected ParameterizedSparqlString getSparqlSearchByIdQuery(
					String field, String key, String locale) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected ParameterizedSparqlString getSparqlSearchByTextQuery(
					String field, String text, String locale) {
				ParameterizedSparqlString pqs = new ParameterizedSparqlString();
				// pss.setBaseUri("http://example.org/base#");
				pqs.setNsPrefix("atlasterms",
						"http://rdf.ebi.ac.uk/terms/atlas/");
				pqs.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
				pqs.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
				pqs.setCommandText("SELECT DISTINCT ?experiment ?description \n");
				pqs.append("WHERE { \n");
				pqs.append("?experiment a atlasterms:Experiment .");
				pqs.append("?experiment dcterms:description ?description .");
				pqs.append("FILTER regex(?description, ?text, \"i\")");
				pqs.append("} \n");
				pqs.append("ORDER BY ASC(?description)");
				pqs.setLiteral("text", text);
				return pqs;

			}
		};

		Choices cs = s.getMatches("dc.title", "some", null, 0, 10, "en");
		for (Choice c : cs.values) {
			System.out.println("AUTHORITY=" + c.authority + ",LABEL=" + c.label
					+ ",VALUE=" + c.value);
		}
	}


}
