package org.dspace.app.xmlui.aspect.ELProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.dspace.content.DSpaceObject;

public class PreviewManager{
	
	public static void showItemPeview(List<DSpaceObject> items) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		TransformationAction.setDSOs(items);
		TransformationAction.setWhichDSO("item");
		ResultContainer.cleanPreviewResult();
		showGenericPreview(new UpdateItem(), items);
		SelectionPage.showItemPreviewMessage();
	}
	
	public static void showCollectionPeview(List<DSpaceObject> collections) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		TransformationAction.setDSOs(collections);
		TransformationAction.setWhichDSO("collection");
		ResultContainer.cleanPreviewResult();
		showGenericPreview(new UpdateCollection(), collections);
		SelectionPage.showCollectionPreviewMessage();
	}

	public static void showCommunityPeview(List<DSpaceObject> communities) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		TransformationAction.setDSOs(communities);
		TransformationAction.setWhichDSO("community");
		ResultContainer.cleanPreviewResult();
		showGenericPreview(new UpdateCommunity(), communities);
		SelectionPage.showCommunityPreviewMessage();
	}
	
	private static void showGenericPreview(Update update, List<DSpaceObject> DSOs) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Method m = update.getClass().getMethod(TransformationAction.getAction()+"Preview", List.class, List.class, boolean.class );
		m.invoke(update, DSOs, TransformationAction.getConditions(), TransformationAction.getUpdateAll());
	}
	
}
