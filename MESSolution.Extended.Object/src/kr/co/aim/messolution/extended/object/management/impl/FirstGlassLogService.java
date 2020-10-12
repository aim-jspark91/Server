package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLog;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FirstGlassLogService extends CTORMService<FirstGlassLog> {
public static Log logger = LogFactory.getLog(FirstGlassLogService.class);
	
	private final String historyEntity = "";
	
	public List<FirstGlassLog> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<FirstGlassLog> result = super.select(condition, bindSet, FirstGlassLog.class);
		
		return result;
	}
	
	public FirstGlassLog selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(FirstGlassLog.class, isLock, keySet);
	}
	
	public FirstGlassLog create(EventInfo eventInfo, FirstGlassLog dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, FirstGlassLog dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public FirstGlassLog modify(EventInfo eventInfo, FirstGlassLog dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void insertFirstGlassLog(EventInfo eventInfo, String id, String eventDate, String recipeId, String productSpecName, String tactTime,
			String barCode,	String hmds, String glassThickness, String aoiDefectQuantity, String aoiInspectionMachine,
			String lineWidthMax, String lineWidthAverage, String lineWidthMin, String lineWidthInspectionMachine,
			String overlayMax, String overlayAverage, String overlayMin, String overlayPerfect, String exposureSpeed, String illuminance,
			String esdExist, String existAroundPhoto, String perfectMark, String exposureProgramName, String judge,
			String operator, String exposureMachineName, String lastEventUser)
				throws CustomException 
	{
		try
		{
			FirstGlassLog firstGlassLogData = new FirstGlassLog();
			
			firstGlassLogData.setId(id);
			firstGlassLogData.setEventDate(ConvertUtil.convertToTimeStamp(eventDate));
			firstGlassLogData.setRecipeId(recipeId);
			firstGlassLogData.setProductSpecName(productSpecName);
			firstGlassLogData.setTactTime(tactTime);
			firstGlassLogData.setBarCode(barCode);
			firstGlassLogData.setHmds(hmds);
			firstGlassLogData.setGlassThickness(glassThickness);
			firstGlassLogData.setAoiDefectQuantity(aoiDefectQuantity);
			firstGlassLogData.setAoiInspectionMachine(aoiInspectionMachine);
			firstGlassLogData.setLineWidthMax(lineWidthMax);
			firstGlassLogData.setLineWidthAverage(lineWidthAverage);
			firstGlassLogData.setLineWidthMin(lineWidthMin);
			firstGlassLogData.setLineWidthInspectionMachine(lineWidthInspectionMachine);
			firstGlassLogData.setOverlayMax(overlayMax);
			firstGlassLogData.setOverlayAverage(overlayAverage);
			firstGlassLogData.setOverlayMin(overlayMin);
			firstGlassLogData.setOverlayPerfect(overlayPerfect);
			firstGlassLogData.setExposureSpeed(exposureSpeed);
			firstGlassLogData.setIlluminance(illuminance);
			firstGlassLogData.setEsdExist(esdExist);
			firstGlassLogData.setExistAroundPhoto(existAroundPhoto);
			firstGlassLogData.setPerfectMark(perfectMark);
			firstGlassLogData.setExposureProgramName(exposureProgramName);
			firstGlassLogData.setJudge(judge);
			firstGlassLogData.setOperator(operator);
			firstGlassLogData.setExposureMachineName(exposureMachineName);
			firstGlassLogData.setLastEventUser(lastEventUser);
			
			firstGlassLogData.setTimeKey(eventInfo.getEventTimeKey());
			
			ExtendedObjectProxy.getFirstGlassService().insert(firstGlassLogData);
			
		}
		catch(Exception e)
		{
			
		}
	}
	
	public void updateFirstGlassLog(EventInfo eventInfo, String id, String eventDate, String recipeId, String productSpecName, String tactTime,
			String barCode,	String hmds, String glassThickness, String aoiDefectQuantity, String aoiInspectionMachine,
			String lineWidthMax, String lineWidthAverage, String lineWidthMin, String lineWidthInspectionMachine,
			String overlayMax, String overlayAverage, String overlayMin, String overlayPerfect, String exposureSpeed, String illuminance,
			String esdExist, String existAroundPhoto, String perfectMark, String exposureProgramName, String judge,
			String operator, String exposureMachineName, String lastEventUser)
				throws CustomException 
	{
		try
		{
			FirstGlassLog firstGlassLogData = new FirstGlassLog();
			
			firstGlassLogData.setId(id);
			firstGlassLogData.setEventDate(ConvertUtil.convertToTimeStamp(eventDate));
			firstGlassLogData.setRecipeId(recipeId);
			firstGlassLogData.setProductSpecName(productSpecName);
			firstGlassLogData.setTactTime(tactTime);
			firstGlassLogData.setBarCode(barCode);
			firstGlassLogData.setHmds(hmds);
			firstGlassLogData.setGlassThickness(glassThickness);
			firstGlassLogData.setAoiDefectQuantity(aoiDefectQuantity);
			firstGlassLogData.setAoiInspectionMachine(aoiInspectionMachine);
			firstGlassLogData.setLineWidthMax(lineWidthMax);
			firstGlassLogData.setLineWidthAverage(lineWidthAverage);
			firstGlassLogData.setLineWidthMin(lineWidthMin);
			firstGlassLogData.setLineWidthInspectionMachine(lineWidthInspectionMachine);
			firstGlassLogData.setOverlayMax(overlayMax);
			firstGlassLogData.setOverlayAverage(overlayAverage);
			firstGlassLogData.setOverlayMin(overlayMin);
			firstGlassLogData.setOverlayPerfect(overlayPerfect);
			firstGlassLogData.setExposureSpeed(exposureSpeed);
			firstGlassLogData.setIlluminance(illuminance);
			firstGlassLogData.setEsdExist(esdExist);
			firstGlassLogData.setExistAroundPhoto(existAroundPhoto);
			firstGlassLogData.setPerfectMark(perfectMark);
			firstGlassLogData.setExposureProgramName(exposureProgramName);
			firstGlassLogData.setJudge(judge);
			firstGlassLogData.setOperator(operator);
			firstGlassLogData.setExposureMachineName(exposureMachineName);
			firstGlassLogData.setLastEventUser(lastEventUser);
			
			firstGlassLogData.setTimeKey(eventInfo.getEventTimeKey());
			
			ExtendedObjectProxy.getFirstGlassService().update(firstGlassLogData);
			
		}
		catch(Exception e)
		{
			
		}
	}
	
	public void deleteFirstGlassLog(EventInfo eventInfo, FirstGlassLog firstGlassLogDate)
				throws CustomException 
	{
		try
		{			
			ExtendedObjectProxy.getFirstGlassService().delete(firstGlassLogDate);
		}
		catch(Exception e)
		{
			
		}
	}
	
}
