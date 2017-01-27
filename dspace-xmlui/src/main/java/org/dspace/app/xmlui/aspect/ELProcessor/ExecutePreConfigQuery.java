package org.dspace.app.xmlui.aspect.ELProcessor;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;

public class ExecutePreConfigQuery extends AbstractAction{

	private static ExpressionModule mainProcessor;	
	
	@Override
	public Map act(Redirector redirector, SourceResolver resolver, Map objectModel,
            String source, Parameters parameters) throws Exception {

		Request request = ObjectModelHelper.getRequest(objectModel);
		
		String query=request.getParameter("query");
		this.instanciateMainProcessor().process(query, ContextUtil.obtainContext(objectModel));
		return null;
	}

	public  ExpressionModule instanciateMainProcessor() {
		if(mainProcessor==null){
			mainProcessor=new ExpressionModule();
		}
		return mainProcessor;
	}

	
}
