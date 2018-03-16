package org.dspace.discovery.exporter.csv;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
    protected List<StatisticsDSpaceCSVLine> lines;

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
        lines = new ArrayList<>();

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
        // Create the CSV line
        StatisticsDSpaceCSVLine line = new StatisticsDSpaceCSVLine();

        // Populate it
        Map<String, List<String>> fields = document.getSearchFields();
        for (String field: fields.keySet()) {
        	for (String value: fields.get(field)) {
        		line.add(field, value);
        	}
            if (!headings.contains(field))
            {
                headings.add(field);
            }
        }
        
        lines.add(line);
        counter++;
    }

    /**
     * Get the lines in CSV holders
     *
     * @return The lines
     */
    public final List<StatisticsDSpaceCSVLine> getCSVLines()
    {
        // Return the lines
        return lines;
    }

    /**
     * Get the CSV lines as an array of CSV formatted strings
     *
     * @return the array of CSV formatted Strings
     */
    public final String[] getCSVLinesAsStringArray()
    {
        // Create the headings line
        String[] csvLines = new String[counter + 1];
        List<String> headingsCopy = new ArrayList<>(headings);
        Collections.sort(headingsCopy);
        for (String value : headingsCopy)
        {
        	//Chequeamos si estamos comenzando a generar la línea, ya que inicialmente esta vacía...
        	if(csvLines[0]==null || csvLines[0].isEmpty()) {
        		csvLines[0] = value;
        	} else {
        		csvLines[0] = csvLines[0] + fieldSeparator + value;
        	}
        }

        Iterator<StatisticsDSpaceCSVLine> i = lines.iterator();
        int c = 1;
        while (i.hasNext())
        {
            csvLines[c++] = i.next().toCSV(headingsCopy, fieldSeparator, valueSeparator);
        }

        return csvLines;
    }

    /**
     * Save the CSV file to the given filename
     *
     * @param filename The filename to save the CSV file to
     *
     * @throws IOException Thrown if an error occurs when writing the file
     */
    public final void save(String filename) throws IOException
    {
        // Save the file
        BufferedWriter out = new BufferedWriter(
                             new OutputStreamWriter(
                             new FileOutputStream(filename), "UTF-8"));
        for (String csvLine : getCSVLinesAsStringArray()) {
            out.write(csvLine + "\n");
        }
        out.flush();
        out.close();
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
     * Return the csv file as one long formatted string
     *
     * @return The formatted String as a csv
     */
    @Override
    public final String toString()
    {
        // Return the csv as one long string
        StringBuilder csvLines = new StringBuilder();
        String[] lines = this.getCSVLinesAsStringArray();
        for (String line : lines)
        {
            csvLines.append(line).append("\n");
        }
        return csvLines.toString();
    }

}
