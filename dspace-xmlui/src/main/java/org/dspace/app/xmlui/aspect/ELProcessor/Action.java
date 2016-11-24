package org.dspace.app.xmlui.aspect.ELProcessor;

public class Action {
	
	protected static void setResult(){
		SelectionPage.cleanVariables();
		ResultContainer.setSelectionPage();
		ResultContainer.cleanResults();
	}

}
