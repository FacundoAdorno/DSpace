package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.dspace.app.xmlui.utils.ContextUtil;

public class ExecuteTransformation extends AbstractAction{

	private static ExpressionModule mainProcessor;	
	
	@Override
	public Map act(Redirector redirector, SourceResolver resolver, Map objectModel,
            String source, Parameters parameters) throws Exception {
		// TODO Auto-generated method stub
		
		this.instanciateMainProcessor().executeTransformation(ContextUtil.obtainContext(objectModel));
		return null;
	}
	
	public  ExpressionModule instanciateMainProcessor() {
		if(mainProcessor==null){
			mainProcessor=new ExpressionModule();
		}
		return mainProcessor;
	}

}
