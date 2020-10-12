package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLog;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CommonFirstGlassLog extends SyncHandler {

	private String CREATE = "CreateFirstGlassLog";
	private String DELETE = "DeleteFirstGlassLog";
	private String MODIFY = "ModifyFirstGlassLog";
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String eventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", true);
		String id = SMessageUtil.getBodyItemValue(doc, "ID", true);
		String eventDate = SMessageUtil.getBodyItemValue(doc, "EVENTDATE", false);
		String recipeId = SMessageUtil.getBodyItemValue(doc, "RECIPEID", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String tactTime = SMessageUtil.getBodyItemValue(doc, "TACTTIME", false);
		String barCode = SMessageUtil.getBodyItemValue(doc, "BARCODE", false);
		String hmds = SMessageUtil.getBodyItemValue(doc, "HMDS", false);
		String glassThickness = SMessageUtil.getBodyItemValue(doc, "GLASSTHICKNESS", false);
		String aoiDefectQuantity = SMessageUtil.getBodyItemValue(doc, "AOIDEFECTQUANTITY", false);
		String aoiInspectionMachine = SMessageUtil.getBodyItemValue(doc, "AOIINSPECTIONMACHINE", false);
		String lineWidthMax = SMessageUtil.getBodyItemValue(doc, "LINEWIDTHMAX", false);
		String lineWidthAverage = SMessageUtil.getBodyItemValue(doc, "LINEWIDTHAVERAGE", false);
		String lineWidthMin = SMessageUtil.getBodyItemValue(doc, "LINEWIDTHMIN", false);
		String lineWidthInspectionMachine = SMessageUtil.getBodyItemValue(doc, "LINEWIDTHINSPECTIONMACHINE", false);
		String overlayMax = SMessageUtil.getBodyItemValue(doc, "OVERLAYMAX", false);
		String overlayAverage = SMessageUtil.getBodyItemValue(doc, "OVERLAYAVERAGE", false);
		String overlayMin = SMessageUtil.getBodyItemValue(doc, "OVERLAYMIN", false);
		String overlayPerfect = SMessageUtil.getBodyItemValue(doc, "OVERLAYPERFECT", false);
		String exposureSpeed = SMessageUtil.getBodyItemValue(doc, "EXPOSURESPEED", false);
		String illuminance = SMessageUtil.getBodyItemValue(doc, "ILLUMINANCE", false);
		String esdExist = SMessageUtil.getBodyItemValue(doc, "ESDEXIST", false);
		String existAroundPhoto = SMessageUtil.getBodyItemValue(doc, "EXISTAROUNDPHOTO", false);
		String PerfectMark = SMessageUtil.getBodyItemValue(doc, "PERFECTMARK", false);
		String exposureProgramName = SMessageUtil.getBodyItemValue(doc, "EXPOSUREPROGRAMNAME", false);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", false);
		String operator = SMessageUtil.getBodyItemValue(doc, "OPERATOR", false);
		String exposureMachineName = SMessageUtil.getBodyItemValue(doc, "EXPOSUREMACHINENAME", false);
		String lastEventUser = "";
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		lastEventUser = eventInfo.getEventUser();
		
		List<FirstGlassLog> firstGlassLogList = null;
		
		if(StringUtils.equals(CREATE, eventName))
		{
			eventInfo.setEventName("Create");
			
			try
			{
				String condition = " WHERE ID = ?";
				Object[] bindSet = new Object[] {id};
				firstGlassLogList = ExtendedObjectProxy.getFirstGlassService().select(condition, bindSet);
			}
			catch(Exception ex)
			{
				
			}
			
			
			if(firstGlassLogList != null)
			{
				throw new CustomException();
			}
			
			ExtendedObjectProxy.getFirstGlassService().insertFirstGlassLog(eventInfo ,id, eventDate, recipeId, productSpecName, tactTime,
					barCode, hmds, glassThickness, aoiDefectQuantity, aoiInspectionMachine,
					lineWidthMax, lineWidthAverage, lineWidthMin, lineWidthInspectionMachine,
					overlayMax, overlayAverage, overlayMin, overlayPerfect, exposureSpeed, illuminance,
					esdExist, existAroundPhoto, PerfectMark, exposureProgramName, judge,
					operator, exposureMachineName, lastEventUser);
		}
		
		if(StringUtils.equals(MODIFY, eventName))
		{
			eventInfo.setEventName("Update");
			
			String condition = " WHERE ID = ?";
			Object[] bindSet = new Object[] {id};
			firstGlassLogList = ExtendedObjectProxy.getFirstGlassService().select(condition, bindSet);
			
			ExtendedObjectProxy.getFirstGlassService().updateFirstGlassLog(eventInfo ,id, eventDate, recipeId, productSpecName, tactTime,
					barCode, hmds, glassThickness, aoiDefectQuantity, aoiInspectionMachine,
					lineWidthMax, lineWidthAverage, lineWidthMin, lineWidthInspectionMachine,
					overlayMax, overlayAverage, overlayMin, overlayPerfect, exposureSpeed, illuminance,
					esdExist, existAroundPhoto, PerfectMark, exposureProgramName, judge,
					operator, exposureMachineName, lastEventUser);
		}
		
		if(StringUtils.equals(DELETE, eventName))
		{
			eventInfo.setEventName("Delete");
			
			String condition = " WHERE ID = ?";
			Object[] bindSet = new Object[] {id};
			firstGlassLogList = ExtendedObjectProxy.getFirstGlassService().select(condition, bindSet);
			
			ExtendedObjectProxy.getFirstGlassService().deleteFirstGlassLog(eventInfo, firstGlassLogList.get(0));
		}
		
		
		return doc;
	}

}
