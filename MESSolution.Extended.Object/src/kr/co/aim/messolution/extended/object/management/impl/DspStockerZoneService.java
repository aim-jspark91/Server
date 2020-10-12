package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DspStockerZone;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DspStockerZoneService extends CTORMService<DspStockerZone>{

	public static Log logger = LogFactory.getLog(DspStockerZone.class);

	private final String historyEntity = "DspStockerZoneHist";

	public List<DspStockerZone> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<DspStockerZone> result = super.select(condition, bindSet, DspStockerZone.class);

		return result;
	}

	public DspStockerZone selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(DspStockerZone.class, isLock, keySet);
	}

	public DspStockerZone create(EventInfo eventInfo, DspStockerZone dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DspStockerZone dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DspStockerZone modify(EventInfo eventInfo, DspStockerZone dataInfo)
			throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	/**
	 * @author smkang
	 * @since 2018.12.07
	 * @param eventInfo
	 * @param previousMachineName
	 * @param previousZoneName
	 * @param currentMachineName
	 * @param currentZoneName
	 * @see When a carrier is changed location, UsedShelfCount or EmptyShelfCount of a stocker is changed.
	 */
	public void calculateShelfCount(EventInfo eventInfo, String previousMachineName, String previousZoneName, String currentMachineName, String currentZoneName) {
		try {
			if (StringUtils.isNotEmpty(previousMachineName) && StringUtils.isNotEmpty(previousZoneName)) {
				try {
					DspStockerZone previousDspStockerZoneData = selectByKey(false, new Object[] {previousMachineName, previousZoneName});
					
					previousDspStockerZoneData.setUsedShelfCount(previousDspStockerZoneData.getUsedShelfCount() - 1);
					previousDspStockerZoneData.setEmptyShelfCount(previousDspStockerZoneData.getEmptyShelfCount() + 1);
					previousDspStockerZoneData.setLastEventName(eventInfo.getEventName());
					previousDspStockerZoneData.setLastEventTimekey(eventInfo.getEventTimeKey());
					previousDspStockerZoneData.setLastEventTime(eventInfo.getEventTime());
					previousDspStockerZoneData.setLastEventUser(eventInfo.getEventUser());
					previousDspStockerZoneData.setLastEventComment(eventInfo.getEventComment());
					
					modify(eventInfo, previousDspStockerZoneData);
				} catch (Exception e) {
				}
			}
			
			if (StringUtils.isNotEmpty(currentMachineName) && StringUtils.isNotEmpty(currentZoneName)) {
				try {
					DspStockerZone currentDspStockerZoneData = selectByKey(false, new Object[] {currentMachineName, currentZoneName});
					
					currentDspStockerZoneData.setUsedShelfCount(currentDspStockerZoneData.getUsedShelfCount() + 1);
					currentDspStockerZoneData.setEmptyShelfCount(currentDspStockerZoneData.getEmptyShelfCount() - 1);
					currentDspStockerZoneData.setLastEventName(eventInfo.getEventName());
					currentDspStockerZoneData.setLastEventTimekey(eventInfo.getEventTimeKey());
					currentDspStockerZoneData.setLastEventTime(eventInfo.getEventTime());
					currentDspStockerZoneData.setLastEventUser(eventInfo.getEventUser());
					currentDspStockerZoneData.setLastEventComment(eventInfo.getEventComment());
					
					modify(eventInfo, currentDspStockerZoneData);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			logger.info(e);
		}		
	}
}