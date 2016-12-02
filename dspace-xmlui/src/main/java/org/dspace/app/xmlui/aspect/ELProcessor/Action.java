package org.dspace.app.xmlui.aspect.ELProcessor;

public class Action {
	
	protected static void setResult(){
		ResultContainer.setSelectionPage();
		cleanResult();
	}
	
	protected static void cleanResults(){
		cleanSelection();
		cleanResult();
	}
	
	private static void cleanSelection(){
		SelectionPage.cleanVariables();
	}
	
	private static void cleanResult(){
		ResultContainer.cleanResults();
	}

}
