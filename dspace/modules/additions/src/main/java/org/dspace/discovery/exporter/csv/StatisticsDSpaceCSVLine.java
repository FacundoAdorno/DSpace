package org.dspace.discovery.exporter.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class StatisticsDSpaceCSVLine {
    
	Map<String, ArrayList<String>> statisticsRegistry;
	
	/**
     * Create a new CSV line
     *
     */
    public StatisticsDSpaceCSVLine()
    {
        this.statisticsRegistry = new TreeMap<>();
    }

    /**
     * Add a new metadata value to this line
     *
     * @param key The metadata key (e.g. 'statistics_type')
     * @param value The metadata value
     */
    public void add(String key, String value)
    {
        // Create the array list if we need to
        if (statisticsRegistry.get(key) == null)
        {
        	statisticsRegistry.put(key, new ArrayList<String>());
        }

        // Store the item if it is not null
        if (value != null)
        {
        	statisticsRegistry.get(key).add(value);
        }
    }

    /**
     * Get all the values that match the given metadata key. Will be null if none exist.
     *
     * @param key The metadata key
     * @return All the elements that match
     */
    public List<String> get(String key)
    {
        // Return any relevant values
        return statisticsRegistry.get(key);
    }

    /**
     * Get all the metadata keys that are represented in this line
     *
     * @return An enumeration of all the keys
     */
    public Set<String> keys()
    {
        // Return the keys
        return statisticsRegistry.keySet();
    }

    /**
     * Write this line out as a CSV formatted string, in the order given by the headings provided
     *
     * @param headings The headings which define the order the elements must be presented in
     * @param fieldSeparator separator between metadata fields
     * @param valueSeparator separator between metadata values (within a field)
     * @return The CSV formatted String
     */
    protected String toCSV(List<String> headings, String fieldSeparator, String valueSeparator)
    {
        StringBuilder bits = new StringBuilder();

        for (String heading : headings)
        {
        	//Solo agregamos el separador si ya tenemos algun dato agregado...
        	if(bits.length() > 0) {
        		bits.append(fieldSeparator);
        	}
            List<String> values = statisticsRegistry.get(heading);
            if (values != null && !"collection".equals(heading))
            {
                bits.append(valueToCSV(values, valueSeparator));
            }
        }

        return bits.toString();
    }

    /**
     * Internal method to create a CSV formatted String joining a given set of elements
     *
     * @param values The values to create the string from
     * @param valueSeparator value separator
     * @return The line as a CSV formatted String
     */
    protected String valueToCSV(List<String> values, String valueSeparator)
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
