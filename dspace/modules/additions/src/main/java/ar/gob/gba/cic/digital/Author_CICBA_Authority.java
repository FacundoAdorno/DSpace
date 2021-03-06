package ar.gob.gba.cic.digital;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
import ar.edu.unlp.sedici.dspace.authority.SPARQLAuthorityProvider;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
//FIXME cambiar  los queries para que levanten autores
public class Author_CICBA_Authority extends AdvancedSPARQLAuthorityProvider {

	protected static final Resource person = ResourceFactory.createResource(NS_FOAF + "Person");
	protected static final Property familyName = ResourceFactory.createProperty(NS_FOAF + "familyName");
	protected static final Property type = ResourceFactory.createProperty(NS_RDF + "type");
	protected static final Property givenName = ResourceFactory.createProperty(NS_FOAF + "givenName");
	protected static final Property mbox = ResourceFactory.createProperty(NS_FOAF + "mbox");
	protected static final Property organization = ResourceFactory.createProperty(NS_FOAF + "Organization");
	protected static final Property linksToOrganisationUnit = ResourceFactory.createProperty(NS_CERIF, "linksToOrganisationUnit");
	protected static final Property title = ResourceFactory.createProperty(NS_DC + "title");	
	protected static final Property siocId = ResourceFactory.createProperty(NS_SIOC + "id");
	protected static final Property startDate = ResourceFactory.createProperty(NS_CERIF + "startDate");
	protected static final Property endDate = ResourceFactory.createProperty(NS_CERIF + "endDate");
			
	@Override
	protected ResIterator getRDFResources(Model model) {
		return model.listSubjectsWithProperty(type, person);
	}

	protected Choice extractChoice(Resource subject) {
		
		String key = subject.getURI();
		String label = subject.getProperty(familyName).getString() + ", " + subject.getProperty(givenName).getString() ;
		String value = label;
		StmtIterator links = subject.listProperties(linksToOrganisationUnit);
		if (links.hasNext()){
			label += getAffiliations(links);
		}
		
		return new Choice(key, value, label);
	}

	@Override
	protected ParameterizedSparqlString getSparqlSearchByIdQuery(String field,
			String key, String locale) {
		ParameterizedSparqlString pqs = new ParameterizedSparqlString();

		pqs.setNsPrefix("foaf", NS_FOAF);
		pqs.setNsPrefix("dc", NS_DC);
		pqs.setNsPrefix("cerif", NS_CERIF);
		pqs.setNsPrefix("rdf", NS_RDF);
		pqs.setNsPrefix("sioc", NS_SIOC);

		pqs.setCommandText("CONSTRUCT { ?person a foaf:Person. ?person foaf:givenName ?name . ?person foaf:mbox ?mail . ?person foaf:familyName ?surname. ?person cerif:linksToOrganisationUnit ?link . ?link cerif:startDate ?inicio. ?link cerif:endDate ?fin . ?link foaf:Organization ?org . ?org dc:title ?affiliation. ?org sioc:id ?id. }\n");
		pqs.append("WHERE {\n");
		pqs.append("?person a foaf:Person ; foaf:givenName ?name ; foaf:familyName ?surname .\n");
		pqs.append("	OPTIONAL {\n");
		pqs.append("	?person foaf:mbox ?mail . \n");
		pqs.append("	} . \n");
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

		pqs.setCommandText("CONSTRUCT { ?person a foaf:Person. ?person foaf:givenName ?name . ?person foaf:mbox ?mail . ?person foaf:familyName ?surname. ?person cerif:linksToOrganisationUnit ?link . ?link cerif:startDate ?inicio. ?link cerif:endDate ?fin . ?link foaf:Organization ?org . ?org dc:title ?affiliation. ?org sioc:id ?id. }\n");
		pqs.append("WHERE {\n");
		pqs.append("?person a foaf:Person ; foaf:givenName ?name; foaf:familyName ?surname. \n");
		pqs.append("	OPTIONAL {\n");
		pqs.append("	?person foaf:mbox ?mail . \n");
		pqs.append("	} . \n");
		pqs.append("	OPTIONAL {\n");
		pqs.append("	?person cerif:linksToOrganisationUnit ?link . ?link cerif:startDate ?inicio; cerif:endDate ?fin; foaf:Organization ?org . ?org dc:title ?affiliation; sioc:id ?id\n");
		pqs.append("	}\n");
		if (!"".equals(text)) {
			String[] tokens = text.split(",");
			if (tokens.length > 1 && tokens[0].trim().length() > 0 && tokens[1].trim().length() > 0) {
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


	private String getAffiliations(StmtIterator links) {
		StringBuilder string = new StringBuilder().append(" (");
		while (links.hasNext()){
			Statement link = links.next();
			
			Resource affiliation = link.getObject().asResource();
			Resource org = affiliation.getProperty(organization).getObject().asResource();			
			String id = org.getProperty(siocId).getString();
			if (!"".equals(id)){
				string.append(id);
			}
			else{
				string.append(org.getProperty(title).getString());
			}
			String start = affiliation.getProperty(startDate).getString();
			String end = affiliation.getProperty(endDate).getString();
			if(!"".equals(start) || !"".equals(end)){
				string.append(getPeriodForFiliation(start, end));
			}

			if (links.hasNext()) string.append(", ");
		};
		return string.append(")").toString();
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
	 * Get the name and email from a Jena Model containing (NS_FOAF + "Person") elements.
	 * @param model
	 * @return return and array with the email in the 0 position and the name in the 1 position
	 */
	public ArrayList<String[]> extractNameAndEmailFromAuthors(Model model){
		ResIterator persons = getRDFResources(model);
		ArrayList<String[]> result = new ArrayList<String[]>();
		while(persons.hasNext()) {
			Resource currentPerson = persons.next();
			if(currentPerson.getProperty(mbox) != null) {
				String[] respuesta = new String[2];				
				respuesta[0] = currentPerson.getProperty(mbox).getString();
				respuesta[1] = currentPerson.getProperty(givenName).getString() + ", " + currentPerson.getProperty(familyName).getString();
				result.add(respuesta);
			}
		}
		return result;
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
//		for (Choice c : cs.values) {
//			System.out.println("\n AUTHORITY = " + c.authority + "\n LABEL = " + c.label + "\n VALUE = " + c.value +"\n" );
//		}
		System.out.println("\n AUTHORITY = " + choice.values[0].authority + "\n LABEL = " + choice.values[0].label + "\n VALUE = " + choice.values[0].value +"\n" );
		String label = s.getLabel("dcterms.creator.author", "localhost/auth/node/313700", "");
		System.out.println(label);
		label.toString();
	}


}
