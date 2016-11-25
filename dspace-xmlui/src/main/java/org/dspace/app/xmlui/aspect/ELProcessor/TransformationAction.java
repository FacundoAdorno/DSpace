package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.DSpaceObject;

public class TransformationAction extends Action{

	protected static List<DSpaceObject> result= new ArrayList();
	protected static DSpaceObject dso = null;
	
	public static void modifyFirstItems(String condition, String newValues) throws Exception{
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyFirstItems(condition, newValues);
		setResult();
	}
	
	public static void modifyAllItems(String condition, String newValues) throws Exception{
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyAllItems(condition, newValues);
		setResult();
	}	
	
	public static void modifyFirstCollections(String condition, String newValues) throws Exception{
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyFirstCollections(condition, newValues);
		setResult();
	}
	
	public static void modifyAllCollections(String condition, String newValues) throws Exception{
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyAllCollections(condition, newValues);
		setResult();
	}
	
	public static void modifyFirstCommunities(String condition, String newValues) throws Exception{
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyFirstCommunities(condition, newValues);
		setResult();
	}
	
	public static void modifyAllCommunities(String condition, String newValues) throws Exception{
		ResolverFactory rf= new ResolverFactory();
		rf.getUpdateResolver().modifyAllCommunities(condition, newValues);
		setResult();
	}
	
}
