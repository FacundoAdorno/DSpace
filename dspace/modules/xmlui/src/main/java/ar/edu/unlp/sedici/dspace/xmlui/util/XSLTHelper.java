package ar.edu.unlp.sedici.dspace.xmlui.util;

import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.dspace.app.util.Util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xpath.NodeSet;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XNodeSetForDOM;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.dspace.services.factory.DSpaceServicesFactory;;

public class XSLTHelper {
	
	private final static Logger log = Logger.getLogger(XSLTHelper.class);
	
	public static String replaceAll(String source, String regex, String replacement) {
		return source.replaceAll(regex, replacement);
	}
	
	public static String getLugarDesarrollo(String source) {
 		int first = source.indexOf("(");
 		int last = source.indexOf(")");
 		if (first != -1 && last != -1) {
 			return source.substring(first + 1, last);
 		} else {
 			return source;
 		}
 	}
	
	public static String stripDash(String source) {
		if (source.endsWith("-")) {
			return source.substring(0, source.length() - 1);
		} else {
			return source;
		}
	}
	
	public static String escapeURL(String url) throws NullPointerException{
		if (url == null){
			try{
				throw new NullPointerException();
			}catch (Exception e) {
				log.error("escapeURL: Se recibe null como url", e);
			}
		}else{
			try{
				return Util.encodeBitstreamName(url);
			}catch (Exception e){
				log.error("Cannot escape the url specified: " + url);
			}
		}
		return "";
	}
	

	public static String getFileExtension(String filename) {
		return filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();
	}
		

	public static String formatearFecha(String date,String language){
		//Si la fecha viene con el formato aaaa-mm-ddTminutos:segundos:milesimasZ me quedo solo con la fecha
		 String parsedDate=date.split("T")[0];
		 Locale locale=null;
		 String toReplace="";
		 DateTime dt = new DateTime();
		 DateTimeFormatter fmt; 
		 String resul,finalDate;
		 String day="";
		 String month="";
		 String[] formats = {"yyyy-MM-dd","yyyy-MM","yyyy"};
		 String[] finalFormats = {"dd-MMMM-yyyy","MMMM-yyyy","yyyy"};
		 for(int i=0;i<formats.length;i++)
		 {
			 try
			 {
				 fmt = DateTimeFormat.forPattern(formats[i]);
				 fmt.parseDateTime(parsedDate);
				 if(formats[i]!="yyyy")
				 {
					 month=fmt.parseDateTime(parsedDate).monthOfYear().getAsText()+"-";
				 }
				 if(formats[i]=="yyyy-MM-dd")
				 {
					 day=fmt.parseDateTime(parsedDate).getDayOfMonth()+"-";
				 }				 
				 finalDate=day+month+parsedDate.split("-")[0];
			  	 fmt = DateTimeFormat.forPattern(finalFormats[i]);			  
			 	 switch (language)
			 	 {
				 case "en":
					 	locale=Locale.US;
						toReplace=" of ";
						break;
				
		 		 default:
		 			locale=Locale.getDefault();
					toReplace=" de ";
					break;
				 }
			 	resul= fmt.parseDateTime(finalDate).toString(finalFormats[i],locale);
				resul=resul.replace("-",toReplace);
			 	return resul;
				 
			 }
			 catch (java.lang.IllegalArgumentException e)
			 {
				
			 }
		 }
		 return "";
		

	}
	
	/*
	 * Retorna un conjunto de property keys desde el dspace.cfg cuyo prefijo coincida.
	 */
	public static XNodeSet getPropertyKeys(ExpressionContext context, String prefix){
		
		 java.util.List<String> keys = DSpaceServicesFactory.getInstance().getConfigurationService().getPropertyKeys(prefix);
		 return collectionToNodeSet(context, keys);
	
	}
	
	/*
	 * Crea un conjunto de nodos texto a partir de una colección de Objetos
	 */
	private static XNodeSet collectionToNodeSet(ExpressionContext context, List list){
		XNodeSet result = null ;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance() ;
            DocumentBuilder dBuilder;
            dBuilder = dbf.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            NodeSet ns = new NodeSet();

            
            for(int i=0; i < list.size(); i++){
            	ns.addNode( doc.createTextNode(list.get(i).toString())) ;
            }

            result = new XNodeSetForDOM((NodeList)ns, context.getXPathContext());

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
        return result ;
	}
	
}

