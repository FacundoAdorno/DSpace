package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.DSpaceObject;

public class TransformationAction extends Action{

	protected static List<DSpaceObject> result= new ArrayList();
	protected static DSpaceObject dso = null;
	
	public static void modifyItems(String condition, String newValues) throws Exception{
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyItems(condition, newValues);
	}	
	
	public static void modifyCollections(String condition, String newValues) throws Exception{
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyCollections(condition, newValues);
	}
	
	public static void modifyCommunities(String condition, String newValues) throws Exception{
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyCommunities(condition, newValues);
	}
	
}
