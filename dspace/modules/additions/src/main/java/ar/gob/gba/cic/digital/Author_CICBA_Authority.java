package ar.gob.gba.cic.digital;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.dspace.content.authority.Choice;
import org.dspace.content.authority.Choices;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import ar.edu.unlp.sedici.dspace.authority.SPARQLAuthorityProvider;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
//FIXME cambiar  los queries para que levanten autores
public class Author_CICBA_Authority extends CICBAAuthority {

	@Override
	protected ParameterizedSparqlString getSparqlSearchByIdQuery(String field,
			String key, String locale) {
		ParameterizedSparqlString pqs = new ParameterizedSparqlString();

		pqs.setNsPrefix("foaf", NS_FOAF);
		pqs.setNsPrefix("dc", NS_DC);
		pqs.setNsPrefix("cerif", NS_CERIF);
		pqs.setNsPrefix("rdf", NS_RDF);
		pqs.setNsPrefix("sioc", NS_SIOC);

//		pqs.setCommandText("SELECT ?person ?name ?surname ?affiliation\n");
		pqs.setCommandText("CONSTRUCT { ?person a foaf:Person. ?person foaf:givenName ?name . ?person foaf:mbox ?mail . ?person foaf:familyName ?surname. ?person cerif:linksToOrganisationUnit ?link . ?link cerif:startDate ?inicio. ?link cerif:endDate ?fin . ?link foaf:Organization ?org . ?org dc:title ?affiliation. ?org sioc:id ?id. }\n");
		pqs.append("WHERE {\n");
		pqs.append("?person a foaf:Person ; foaf:givenName ?name ; foaf:familyName ?surname; foaf:mbox ?mail .\n");
		pqs.append("	OPTIONAL {\n");
		pqs.append("	?person cerif:linksToOrganisationUnit ?link . ?link cerif:startDate ?inicio; cerif:endDate ?fin; foaf:Organization ?org . ?org dc:title ?affiliation; sioc:id ?id\n");
		pqs.append("	}\n");
		pqs.append("FILTER(REGEX(?person, ?key, \"i\"))\n");
		pqs.append("}\n");
		pqs.append("ORDER BY ?inicio");

		pqs.setLiteral("key", key);
		return pqs;
	}

	@Override
	protected ParameterizedSparqlString getSparqlSearchByTextQuery(
			String field, String text, String locale) {
		ParameterizedSparqlString pqs = new ParameterizedSparqlString();

		pqs.setNsPrefix("foaf", NS_FOAF);
		pqs.setNsPrefix("dc", NS_DC);
		pqs.setNsPrefix("cerif", NS_CERIF);
		pqs.setNsPrefix("rdf", NS_RDF);
		pqs.setNsPrefix("sioc", NS_SIOC);

//		pqs.setCommandText("SELECT DISTINCT ?person ?name ?surname ?mail ?link\n");
		pqs.setCommandText("CONSTRUCT { ?person a foaf:Person. ?person foaf:givenName ?name . ?person foaf:mbox ?mail . ?person foaf:familyName ?surname. ?person cerif:linksToOrganisationUnit ?link . ?link cerif:startDate ?inicio. ?link cerif:endDate ?fin . ?link foaf:Organization ?org . ?org dc:title ?affiliation. ?org sioc:id ?id. }\n");
		pqs.append("WHERE {\n");
		pqs.append("?person a foaf:Person ; foaf:givenName ?name ; foaf:mbox ?mail ; foaf:familyName ?surname. \n");
		pqs.append("OPTIONAL {\n");
		pqs.append("?person cerif:linksToOrganisationUnit ?link . ?link cerif:startDate ?inicio; cerif:endDate ?fin; foaf:Organization ?org . ?org dc:title ?affiliation; sioc:id ?id\n");
		pqs.append("}\n");
		if (!"".equals(text)) {
			String[] tokens = text.split(",");
			if (tokens.length > 1) {
				pqs.append("FILTER(REGEX(?name, ?text2, \"i\") && REGEX(?surname, ?text1, \"i\"))\n");
				pqs.setLiteral("text1", tokens[0].trim());
				pqs.setLiteral("text2", tokens[1].trim());
			} else {
				pqs.append("FILTER(REGEX(?name, ?text, \"i\") || REGEX(?surname, ?text, \"i\") || REGEX(?id, ?text, \"i\"))\n");
				pqs.setLiteral("text", tokens[0]);
			}
		}
		pqs.append("}\n");
		pqs.append("ORDER BY ASC(?surname)\n");
		
		return pqs;
	}


	@Override
	protected Choice extractChoice(QuerySolution solution) {
		String key = solution.getResource("person").getURI();
		String name = solution.getLiteral("name").getString();
		String surname = solution.getLiteral("surname").getString();
		
		String label = surname + ", " + name;
		String value = label;
		
		if (solution.contains("affiliation")) {
			String affiliation = solution.getLiteral("affiliation").getString();
			value = value + " (" + affiliation + ")";
		}
		
		return new Choice(key, value, label);
	}
	
	private Choice extractChoice(Resource subject) {
		
		Model model = subject.getModel();
		StmtIterator it = model.listStatements();
//		while (it.hasNext()){
//			Statement s = it.next();
//			String fafafa = s.getSubject().toString() +", "+ s.getPredicate().toString() +", "+ s.getObject().toString() +", ";
//			fafafa.toString();
//		}
		String key = subject.getURI();
		Property familyName = model.getProperty(NS_FOAF + "familyName");
		Property givenName = model.getProperty(NS_FOAF + "givenName");
		String label = subject.getProperty(familyName).getString() + ", " + subject.getProperty(givenName).getString() ;
		String value = label;
		Property link = model.getProperty(NS_CERIF, "linksToOrganisationUnit");
		StmtIterator links = subject.listProperties(link);
		if (links.hasNext()){
			label += getFiliations(links, model);
		}
		
		return new Choice(key, value, label);
	}

	private String getFiliations(StmtIterator links, Model model) {
		String filiations = " (";
		while (links.hasNext()){
			Resource filiacion = model.getResource(links.next().getObject().toString());
			RDFNode orgURI = filiacion.getProperty(model.getProperty(NS_FOAF + "Organization")).getObject();
			Resource organization = model.getResource(orgURI.toString());
			String id = organization.getProperty(model.getProperty(NS_SIOC + "id")).getString();
			filiations += (!"".equals(id)) ? id : organization.getProperty(model.getProperty(NS_DC + "title")).getString();

			String start = filiacion.getProperty(ResourceFactory.createProperty(NS_CERIF + "startDate")).getString();
			String end = filiacion.getProperty(ResourceFactory.createProperty(NS_CERIF + "endDate")).getString();
			if(!"".equals(start) || !"".equals(end)){
				filiations += getPeriodForFiliation(start, end);
			}

			if (links.hasNext()) filiations += ", ";
		};
		return filiations += ")";
	}

	private String getPeriodForFiliation(String start, String end) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy");
			if (!"".equals(start)){
				start = df.format(new java.util.Date((Long.valueOf(start)*1000)));
			}
			if (!"".equals(end)){
				end = df.format(new java.util.Date((Long.valueOf(end)*1000)));
			}
		return " [" + start + " - "+  end + "]";
	}


	public ParameterizedSparqlString getSparqlEmailByTextQuery(String field,
			String text, String locale) {
		return  this.getSparqlSearchByTextQuery(field,text,locale);		
	}
	
	
	/**
	 * @param solution
	 * @return return and array with the email in the 0 position and the name in the 1 position
	 */
	public String[] extractNameAndEmail(QuerySolution solution){
		String[] respuesta = new String[2];
		respuesta[0] = solution.getLiteral("mail").getString();
		respuesta[1] = solution.getLiteral("name").getString();
		return respuesta;
	}

	protected Choice[] extractChoicesfromQuery(QueryEngineHTTP httpQuery) {
		List<Choice> choices = new LinkedList<Choice>();
		
		Model model = httpQuery.execConstruct(ModelFactory.createDefaultModel());
		Property type = model.getProperty(NS_RDF, "type");
		Resource person = model.getResource("http://xmlns.com/foaf/0.1/Person");
		ResIterator subjects = model.listSubjectsWithProperty(type, person);
		while (subjects.hasNext()){
			choices.add(this.extractChoice(subjects.next()));
		};		
		return choices.toArray(new Choice[0]);
	}

	public static void main(String[] args) {

		log.addAppender(new WriterAppender(new SimpleLayout(), System.out));
		log.setLevel(Level.TRACE);
		SPARQLAuthorityProvider s = new Author_CICBA_Authority() {
			
			protected String getSparqlEndpoint() {
				return "http://localhost/auth/sparql";
			}
		};
		

		Choices choice = s.getBestMatch("dcterms.creator.author", "Reval", null, "");
		Choices cs = s.getMatches("dcterms.creator.author", "Reval", null, 0, 100, "");
		for (Choice c : cs.values) {
			System.out.println("\n AUTHORITY = " + c.authority + "\n LABEL = " + c.label + "\n VALUE = " + c.value +"\n" );
		}
//		System.out.println("\n AUTHORITY = " + choice.values[0].authority + "\n LABEL = " + choice.values[0].label + "\n VALUE = " + choice.values[0].value +"\n" );
//		String label = s.getLabel("dcterms.creator.author", "localhost/auth/node/313700", "");
//		System.out.println(label);
//		label.toString();
	}

	@Override
	public String getLabel(String field, String key, String locale) {

		ParameterizedSparqlString query = this.getSparqlSearchByIdQuery(field,
				key, locale);

		Choice[] choices = evalSparql(query, 0, 0);
		if (choices.length == 0)
			return null;
		else
			return choices[0].label;
	}}
