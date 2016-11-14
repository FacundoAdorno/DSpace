/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.dao.ItemDAO;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.eperson.EPerson;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.StandardBasicTypes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Hibernate implementation of the Database Access Object interface class for the Item object.
 * This class is responsible for all database calls for the Item object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class ItemDAOImpl extends AbstractHibernateDSODAO<Item> implements ItemDAO
{
    private static final Logger log = Logger.getLogger(ItemDAOImpl.class);

    protected ItemDAOImpl()
    {
        super();
    }

    @Override
    public Iterator<Item> findAll(Context context, boolean archived) throws SQLException {
        Query query = createQuery(context, "FROM Item WHERE inArchive= :in_archive");
        query.setParameter("in_archive", archived);
        return iterate(query);
    }

    @Override
    public Iterator<Item> findAll(Context context, boolean archived, boolean withdrawn) throws SQLException {
        Query query = createQuery(context, "FROM Item WHERE inArchive= :in_archive or withdrawn = :withdrawn");
        query.setParameter("in_archive", archived);
        query.setParameter("withdrawn", withdrawn);
        return iterate(query);
    }

    @Override
    public Iterator<Item> findAll(Context context, boolean archived,
            boolean withdrawn, boolean discoverable, Date lastModified)
            throws SQLException
    {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("SELECT i FROM Item i");
        queryStr.append(" WHERE (inArchive = :in_archive OR withdrawn = :withdrawn)");
        queryStr.append(" AND discoverable = :discoverable");

        if(lastModified != null)
        {
            queryStr.append(" AND last_modified > :last_modified");
        }

        Query query = createQuery(context, queryStr.toString());
        query.setParameter("in_archive", archived);
        query.setParameter("withdrawn", withdrawn);
        query.setParameter("discoverable", discoverable);
        if(lastModified != null)
        {
            query.setTimestamp("last_modified", lastModified);
	}
        return iterate(query);
    }

    @Override
    public Iterator<Item> findBySubmitter(Context context, EPerson eperson) throws SQLException {
        Query query = createQuery(context, "FROM Item WHERE inArchive= :in_archive and submitter= :submitter");
        query.setParameter("in_archive", true);
        query.setParameter("submitter", eperson);
        return iterate(query);
    }

    @Override
    public Iterator<Item> findBySubmitter(Context context, EPerson eperson, MetadataField metadataField, int limit) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT item FROM Item as item ");
        addMetadataLeftJoin(query, Item.class.getSimpleName().toLowerCase(), Collections.singletonList(metadataField));
        query.append(" WHERE item.inArchive = :in_archive");
        query.append(" AND item.submitter =:submitter");
        addMetadataSortQuery(query, Collections.singletonList(metadataField), null);

        Query hibernateQuery = createQuery(context, query.toString());
        hibernateQuery.setParameter(metadataField.toString(), metadataField.getID());
        hibernateQuery.setParameter("in_archive", true);
        hibernateQuery.setParameter("submitter", eperson);
        hibernateQuery.setMaxResults(limit);
        return iterate(hibernateQuery);
    }

    @Override
    public Iterator<Item> findByMetadataField(Context context, MetadataField metadataField, String value, boolean inArchive) throws SQLException {
        String hqlQueryString = "SELECT item FROM Item as item join item.metadata metadatavalue WHERE item.inArchive=:in_archive AND metadatavalue.metadataField = :metadata_field";
        if(value != null)
        {
            hqlQueryString += " AND STR(metadatavalue.value) = :text_value";
        }
        Query query = createQuery(context, hqlQueryString);

        query.setParameter("in_archive", inArchive);
        query.setParameter("metadata_field", metadataField);
        if(value != null)
        {
            query.setParameter("text_value", value);
        }
        return iterate(query);
    }

    
    
    @Override
    public Iterator<Item> findByMetadataQuery(Context context, List<List<MetadataField>> listFieldList, List<String> query_op, List<String> query_val, List<UUID> collectionUuids, String regexClause, int offset, int limit) throws SQLException {
    	Criteria criteria = createCriteria(context, Item.class, "item");
    	criteria = findByMetadataQuery(criteria, "item", log, context, listFieldList, query_op, query_val, collectionUuids, regexClause, offset, limit );
    	return criteria.list().iterator();
    }

    @Override
    public Iterator<Item> findByAuthorityValue(Context context, MetadataField metadataField, String authority, boolean inArchive) throws SQLException {
        Query query = createQuery(context, "SELECT item FROM Item as item join item.metadata metadatavalue WHERE item.inArchive=:in_archive AND metadatavalue.metadataField = :metadata_field AND metadatavalue.authority = :authority");
        query.setParameter("in_archive", inArchive);
        query.setParameter("metadata_field", metadataField);
        query.setParameter("authority", authority);
        return iterate(query);
    }

    @Override
    public Iterator<Item> findArchivedByCollection(Context context, Collection collection, Integer limit, Integer offset) throws SQLException {
        Query query = createQuery(context, "select i from Item i join i.collections c WHERE :collection IN c AND i.inArchive=:in_archive");
        query.setParameter("collection", collection);
        query.setParameter("in_archive", true);
        if(offset != null)
        {
            query.setFirstResult(offset);
        }
        if(limit != null)
        {
            query.setMaxResults(limit);
        }
        return iterate(query);
    }

    @Override
    public Iterator<Item> findAllByCollection(Context context, Collection collection) throws SQLException {
        Query query = createQuery(context, "select i from Item i join i.collections c WHERE :collection IN c");
        query.setParameter("collection", collection);

        return iterate(query);
    }

    @Override
    public int countItems(Context context, Collection collection, boolean includeArchived, boolean includeWithdrawn) throws SQLException {
        Query query = createQuery(context, "select count(i) from Item i join i.collections c WHERE :collection IN c AND i.inArchive=:in_archive AND i.withdrawn=:withdrawn");
        query.setParameter("collection", collection);
        query.setParameter("in_archive", includeArchived);
        query.setParameter("withdrawn", includeWithdrawn);

        return count(query);
    }
    
    @Override
    public int countItems(Context context, List<Collection> collections, boolean includeArchived, boolean includeWithdrawn) throws SQLException {
        if (collections.size() == 0) {
            return 0;
        }
        Query query = createQuery(context, "select count(distinct i) from Item i " +
                                            "join i.collections collection " +
                                            "WHERE collection IN (:collections) AND i.inArchive=:in_archive AND i.withdrawn=:withdrawn");
        query.setParameterList("collections", collections);
        query.setParameter("in_archive", includeArchived);
        query.setParameter("withdrawn", includeWithdrawn);

        return count(query);
    }

    @Override
    public Iterator<Item> findByLastModifiedSince(Context context, Date since)
            throws SQLException
    {
        Query query = createQuery(context, "SELECT i FROM item i WHERE last_modified > :last_modified");
        query.setTimestamp("last_modified", since);
        return iterate(query);
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM Item"));
    }

    @Override
    public int countItems(Context context, boolean includeArchived, boolean includeWithdrawn) throws SQLException {
        Query query = createQuery(context, "SELECT count(*) FROM Item i WHERE i.inArchive=:in_archive AND i.withdrawn=:withdrawn");
        query.setParameter("in_archive", includeArchived);
        query.setParameter("withdrawn", includeWithdrawn);
        return count(query); 
    }
}
