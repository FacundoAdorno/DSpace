package org.dspace.discovery.exporter.csv;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.discovery.GenericDiscoverResult.SearchDocument;
import org.dspace.services.factory.DSpaceServicesFactory;

public class StatisticsDSpaceCSV {

	/** The headings of the CSV file */
    protected List<String> headings;

    /** An array list of CSV lines */
    protected ArrayList<HashMap<String, ArrayList<String>>> lines;

    /** A counter of how many CSV lines this object holds */
    protected int counter;

    /** The value separator (defaults to double pipe '||') */
    protected String valueSeparator;

    /** The value separator in an escaped form for using in regexes */
    protected String escapedValueSeparator;

    /** The field separator (defaults to comma) */
    protected String fieldSeparator;

    /** The field separator in an escaped form for using in regexes */
    protected String escapedFieldSeparator;

    /** The authority separator (defaults to double colon '::') */
    protected String authoritySeparator;

    /** The authority separator in an escaped form for using in regexes */
    protected String escapedAuthoritySeparator;

    protected static int MAX_REGISTRIES_PARTIALLY_SAVED = 10000;
    
    /**
     * Create a new instance of a CSV line holder
     */
    public StatisticsDSpaceCSV()
    {
        // Initialise the class
        init();

    }

    /**
     * Initialise this class with values from dspace.cfg
     */
    protected void init()
    {
        // Set the value separator
        setValueSeparator();

        // Set the field separator
        setFieldSeparator();

        // Create the headings
        headings = new ArrayList<>();

        // Create the blank list of items
        lines = new ArrayList<HashMap<String,ArrayList<String>>>();

        // Initialise the counter
        counter = 0;

    }

    /**
     * Set the value separator for multiple values stored in one csv value.
     *
     * Is set in bulkedit.cfg as valueseparator
     *
     * If not set, defaults to double pipe '||'
     */
    private void setValueSeparator()
    {
        // Get the value separator
        valueSeparator = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("bulkedit.valueseparator");
        if ((valueSeparator != null) && (!"".equals(valueSeparator.trim())))
        {
            valueSeparator = valueSeparator.trim();
        }
        else
        {
            valueSeparator = "||";
        }

        // Now store the escaped version
        Pattern spchars = Pattern.compile("([\\\\*+\\[\\](){}\\$.?\\^|])");
        Matcher match = spchars.matcher(valueSeparator);
        escapedValueSeparator = match.replaceAll("\\\\$1");
    }

    /**
     * Set the field separator use to separate fields in the csv.
     *
     * Is set in bulkedit.cfg as fieldseparator
     *
     * If not set, defaults to comma ','.
     *
     * Special values are 'tab', 'hash' and 'semicolon' which will
     * get substituted from the text to the value.
     */
    private void setFieldSeparator()
    {
        // Get the value separator
        fieldSeparator =DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("bulkedit.fieldseparator");
        if ((fieldSeparator != null) && (!"".equals(fieldSeparator.trim())))
        {
            fieldSeparator = fieldSeparator.trim();
            if ("tab".equals(fieldSeparator))
            {
                fieldSeparator = "\t";
            }
            else if ("semicolon".equals(fieldSeparator))
            {
                fieldSeparator = ";";
            }
            else if ("hash".equals(fieldSeparator))
            {
                fieldSeparator = "#";
            }
            else
            {
                fieldSeparator = fieldSeparator.trim();
            }
        }
        else
        {
            fieldSeparator = ",";
        }

        // Now store the escaped version
        Pattern spchars = Pattern.compile("([\\\\*+\\[\\](){}\\$.?\\^|])");
        Matcher match = spchars.matcher(fieldSeparator);
        escapedFieldSeparator = match.replaceAll("\\\\$1");
    }

    /**
     * Add a SearchDocument to the CSV file
     *
     * @param document El registro estadístico a exportar
     */
    public final void addSearchDocument(SearchDocument document) {
    	HashMap<String,ArrayList<String>> line = new HashMap<String,ArrayList<String>>();
    	
        // Populate it
        Map<String, List<String>> fields = document.getSearchFields();
        for (String field: fields.keySet()) {
        	ArrayList<String> values = new ArrayList<String>();
        	for (String value: fields.get(field)) {
        		//Guardamos los valores del metadato de 'statistics' actual
        		values.add(value);
        	}
            if (!headings.contains(field))
            {
                headings.add(field);
            }
            //Guardamos el metadato de 'statistics' en la línea actual
            line.put(field, values);
        }
        
        lines.add(line);
        counter++;
    }

    /**
     * Get the lines in CSV holders
     *
     * @return The lines
     */
    public final ArrayList<HashMap<String, ArrayList<String>>> getCSVLines()
    {
        // Return the lines
        return lines;
    }

    /**
     * Save the CSV file to the given filename
     *
     * @param filename The filename to save the CSV file to
     *
     * @return el archivo en el servidor donde se guardó el CSV para ser retornado al cliente...
     * @throws IOException Thrown if an error occurs when writing the file
     */
    public final FileInputStream save(String filename) throws IOException
    {
     
    	String defaultExportDir = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.dir") + "/exports/statistics-exports";
    	if(filename == null) {
    		filename = "exported_statistics_file";
    	}
    	String fullPathName = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("statistics-discover.exporter.dir", defaultExportDir) + "/" + filename;
    	
    	// Save the file
        BufferedWriter out = new BufferedWriter(
                             new OutputStreamWriter(
                             new FileOutputStream(fullPathName), "UTF-8"));
        
        //Vamos guardando las lineas parcialmente al archivo...
        saveLinesPartiallyToFile(out);
        
        out.flush();
        out.close();
        
        return new FileInputStream(fullPathName);
    }
    
    /**
     * Cada 10000 lineas (o la cantidad seteada en MAX_REGISTRIES_PARTIALLY_SAVED) se va guardando parcialmente el CSV. Si se genera un gran string (con StringBuilder p.e.) cuando la cantidad de registros a exportar
     * es muy grande, pueden producirse un error de memoria "java.lang.OutOfMemoryError: Java heap space". Debido a ésto, guardo el archivo a generar localmente
     * y luego lo retorno al cliente mediante streaming de ese archivo.
     * 
     * @param out	el archivo donde vamos a ir guardando parcialmente las líneas del CSV.
     * @throws IOException 
     */
    private void saveLinesPartiallyToFile(BufferedWriter out) throws IOException {
        StringBuilder partialBuilder = new StringBuilder();
        int partiallyProcessedLinesCount = 0;
        int totalLinesToProcess = counter;
        int processedLinesCount = 0;
        
        //Agregamos el header del csv si es la primera linea procesada
        List<String> headingsCopy = new ArrayList<>(headings);
        Collections.sort(headingsCopy);
        String header = "";
		for (String value : headingsCopy)
        {
			//Chequeamos si es el primer elemento del header, ya que inicialmente esta vacío...
        	if(header == null || header.isEmpty()) {
        		header = value;
        	} else {
        		header = header + fieldSeparator + value;
        	}
        }
		partialBuilder.append(header + "\n");
		partiallyProcessedLinesCount++; /* Sumamos la primer linea de header al contador parcial */ 
		

        while(processedLinesCount <= totalLinesToProcess -1) {	/* ITERACIÓN PRINCIPAL */
        	//Construimos la respuesta a partir de todas las lineas del CSV
        	while(partiallyProcessedLinesCount < MAX_REGISTRIES_PARTIALLY_SAVED && (processedLinesCount <= totalLinesToProcess -1)) {	/* ITERACIÓN SECUNDARIA */
        		//Tenemos que restar 1 ya que la primer linea procesada es el header (que no cuenta como linea...)
        		HashMap<String, ArrayList<String>> line = lines.get(processedLinesCount);
    			for(int header_index = 0; header_index < headingsCopy.size(); header_index++)
    			{
        			String heading = headingsCopy.get(header_index);
        			//Solo agregamos el separador si ya tenemos algun dato agregado para la linea actual...
    				if(header_index != 0) {
    					partialBuilder.append(fieldSeparator);
    				}
    				List<String> values = line.get(heading);
    				partialBuilder.append(valuesToCSV(values, valueSeparator));
    			}
    			partialBuilder.append("\n"); /* Terminamos una linea, ponemos el salto de linea... */
    			partiallyProcessedLinesCount++;
    			processedLinesCount++;
        	}
        	//Guardamos al archivo las lineas de forma parcial
        	out.write(partialBuilder.toString());
        	//Reiniciamos el contador de registros guardados pacialmente
        	partiallyProcessedLinesCount = 0;
        	partialBuilder.setLength(0);
		}
	}

	public FileInputStream getAsInputStream(String fullpathName) {
    	try {
			return new FileInputStream(fullpathName);
		} catch (FileNotFoundException e) {
			return null;
		}
    }

    /**
     * Get the headings used in this CSV file
     *
     * @return The headings
     */
    public List<String> getHeadings()
    {
        return headings;
    }

    /**
     * Internal method to create a CSV formatted String joining a given set of elements
     *
     * @param values The values to create the string from
     * @param valueSeparator value separator
     * @return The line as a CSV formatted String
     */
    protected String valuesToCSV(List<String> values, String valueSeparator)
    {
        // Check there is some content
        if (values == null)
        {
            return "";
        }

        // Get on with the work
        String s;
        if (values.size() == 1)
        {
            s = values.get(0);
        }
        else
        {
            // Concatenate any fields together
            StringBuilder str = new StringBuilder();

            for (String value : values)
            {
                if (str.length() > 0)
                {
                    str.append(valueSeparator);
                }

                str.append(value);
            }

            s = str.toString();
        }

        // Replace internal quotes with two sets of quotes
        return "\"" + s.replaceAll("\"", "\"\"") + "\"";
    }

}
