/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.core;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.StandardBasicTypes;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Hibernate implementation used by DSO Database Access Objects , includes commonly used methods
 * Each DSO Database Access Objects should extend this class to prevent code duplication.
 *
 * @author kevinvandevelde at atmire.com
 * @param <T> class type
 */
public abstract class AbstractHibernateDSODAO<T extends DSpaceObject> extends AbstractHibernateDAO<T>
{
    public T findByLegacyId(Context context, int legacyId, Class<T> clazz) throws SQLException
    {
        Criteria criteria = createCriteria(context, clazz);
        criteria.add(Restrictions.eq("legacyId", legacyId));
        return uniqueResult(criteria);
    }
    
    enum OP {equals,not_equals,like,not_like,contains,doesnt_contain,exists,doesnt_exist,matches,doesnt_match,grater,lower;}
    
    public Criteria findByMetadataQuery(Criteria criteria, String dso, Logger log, Context context, List<List<MetadataField>> listFieldList, List<String> query_op, List<String> query_val, List<UUID> collectionUuids, String regexClause, int offset, int limit){
    	criteria.setFirstResult(offset);
    	criteria.setMaxResults(limit);
    	
    	if (!collectionUuids.isEmpty()){
			DetachedCriteria dcollCriteria = DetachedCriteria.forClass( org.dspace.content.Collection.class, "coll");
        	dcollCriteria.setProjection(Projections.property("coll.id"));
        	dcollCriteria.add(Restrictions.eqProperty("coll.id", "item.owningCollection"));
			dcollCriteria.add(Restrictions.in("coll.id", collectionUuids));
			criteria.add(Subqueries.exists(dcollCriteria));
    	}
    	
        int index = Math.min(listFieldList.size(), Math.min(query_op.size(), query_val.size()));
        StringBuilder sb = new StringBuilder();

        for(int i=0; i<index; i++) {
        	OP op = OP.valueOf(query_op.get(i));
        	if (op == null) {
        		log.warn("Skipping Invalid Operator: " + query_op.get(i));
        		continue;
        	}
        	
        	if (op == OP.matches || op == OP.doesnt_match) {
        		if (regexClause.isEmpty()) {
            		log.warn("Skipping Unsupported Regex Operator: " + query_op.get(i));
            		continue;
        		}
        	}
        	
        	DetachedCriteria subcriteria = DetachedCriteria.forClass(MetadataValue.class,"mv");
        	subcriteria.add(Property.forName("mv.dSpaceObject").eqProperty(dso+".id"));
        	subcriteria.setProjection(Projections.property("mv.dSpaceObject"));
        	
        	if (!listFieldList.get(i).isEmpty()) {
        		subcriteria.add(Restrictions.in("metadataField", listFieldList.get(i)));
        	}
        	
        	sb.append(op.name() + " ");
        	if (op == OP.equals || op == OP.not_equals){
    			subcriteria.add(Property.forName("mv.value").eq(query_val.get(i)));
    			sb.append(query_val.get(i));
        	} else if (op == OP.like || op == OP.not_like){
    			subcriteria.add(Property.forName("mv.value").like(query_val.get(i)));        		        		
    			sb.append(query_val.get(i));
        	} else if (op == OP.contains || op == OP.doesnt_contain){
    			subcriteria.add(Property.forName("mv.value").like("%"+query_val.get(i)+"%"));        		        		
    			sb.append(query_val.get(i));
        	} else if (op == OP.matches || op == OP.doesnt_match) {
            	subcriteria.add(Restrictions.sqlRestriction(regexClause, query_val.get(i), StandardBasicTypes.STRING));
    			sb.append(query_val.get(i));        		
        	} else if (op == OP.grater) {
            	subcriteria.add(Property.forName("mv.value").gt(query_val.get(i)));
    			sb.append(query_val.get(i));        		
        	} else if (op == OP.lower) {
            	subcriteria.add(Property.forName("mv.value").lt(query_val.get(i)));
    			sb.append(query_val.get(i));        		
        	} else if (op == OP.exists) {
            	subcriteria.add(Property.forName("mv.value").isNotNull());
    			sb.append(query_val.get(i));        		
        	} else if (op == OP.exists) {
            	subcriteria.add(Property.forName("mv.value").isNull());
    			sb.append(query_val.get(i));        		
        	}
        	
        	
        	if (op == OP.exists || op == OP.equals || op == OP.like || op == OP.contains || op == OP.matches) {
        		criteria.add(Subqueries.exists(subcriteria));
        	} else {
        		criteria.add(Subqueries.notExists(subcriteria));        		
        	}
        }
     	log.debug(String.format("Running custom query with %d filters", index));

        return criteria;
    }


    /**
     * Add left outer join on all metadata fields which are passed to this function.
     * The identifier of the join will be the toString() representation of the metadata field.
     * The joineded metadata fields can then be used to query or sort.
     * @param query
     * @param tableIdentifier
     * @param metadataFields
     */
    protected void addMetadataLeftJoin(StringBuilder query, String tableIdentifier, Collection<MetadataField> metadataFields)
    {
        for (MetadataField metadataField : metadataFields) {
            query.append(" left join ").append(tableIdentifier).append(".metadata ").append(metadataField.toString());
            query.append(" WITH ").append(metadataField.toString()).append(".metadataField.id").append(" = :").append(metadataField.toString());
        }
    }

    /**
     * Using the metadata tables mapped in the leftJoin, this function creates a where query which can check the values
     * Values can be checked using a like or an "=" query, this is determined by the "operator" parameter
     * When creating a query, the "queryParam" string can be used set as parameter for the query.
     *
     * @param query the already existing query builder, all changes will be appended
     * @param metadataFields the metadatafields who's metadata value should be queried
     * @param operator can either be "=" or "like"
     * @param additionalWhere additional where query
     */
    protected void addMetadataValueWhereQuery(StringBuilder query, List<MetadataField> metadataFields, String operator, String additionalWhere)
    {
        if(CollectionUtils.isNotEmpty(metadataFields) || StringUtils.isNotBlank(additionalWhere)){
            //Add the where query on metadata
            query.append(" WHERE ");
            for (int i = 0; i < metadataFields.size(); i++) {
                MetadataField metadataField = metadataFields.get(i);
                if(StringUtils.isNotBlank(operator))
                {
                    query.append(" (");
                    query.append("lower(STR(" + metadataField.toString()).append(".value)) ").append(operator).append(" lower(:queryParam)");
                    query.append(")");
                    if(i < metadataFields.size() - 1)
                    {
                        query.append(" OR ");
                    }
                }
            }

            if(StringUtils.isNotBlank(additionalWhere))
            {
                if(CollectionUtils.isNotEmpty(metadataFields))
                {
                    query.append(" OR ");
                }
                query.append(additionalWhere);
            }

        }
    }

    protected void addMetadataSortQuery(StringBuilder query, List<MetadataField> metadataSortFields, List<String> columnSortFields)
    {

        if(CollectionUtils.isNotEmpty(metadataSortFields)){
            query.append(" ORDER BY ");
            for (int i = 0; i < metadataSortFields.size(); i++) {
                MetadataField metadataField = metadataSortFields.get(i);
                query.append("STR(").append(metadataField.toString()).append(".value)");
                if(i != metadataSortFields.size() -1)
                {
                    query.append(",");
                }
            }
        }else if(CollectionUtils.isNotEmpty(columnSortFields))
        {
            query.append(" ORDER BY ");
            for (int i = 0; i < columnSortFields.size(); i++) {
                String sortField = columnSortFields.get(i);
                query.append(sortField);
                if(i != columnSortFields.size() -1)
                {
                    query.append(",");
                }
            }
        }
    }

}
