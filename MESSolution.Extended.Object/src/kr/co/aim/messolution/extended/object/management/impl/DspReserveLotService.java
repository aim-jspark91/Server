package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DspReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DspReserveLotService extends CTORMService<DspReserveLot>{

	public static Log logger = LogFactory.getLog(DspReserveLot.class);

	private final String historyEntity = "DspReserveLotHist";

	public List<DspReserveLot> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<DspReserveLot> result = super.select(condition, bindSet, DspReserveLot.class);

		return result;
	}

	public DspReserveLot selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(DspReserveLot.class, isLock, keySet);
	}

	public DspReserveLot create(EventInfo eventInfo, DspReserveLot dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DspReserveLot dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DspReserveLot modify(EventInfo eventInfo, DspReserveLot dataInfo)
			throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	/**
	 * @author smkang
	 * @since 2018.12.05
	 * @param lotName
	 * @param carrierName
	 * @see According to Feng Huanyan's request, if a user executes transport manually or track in to another machine, reserved lot information is removed.
	 */
	public void ignoreReserveLot(EventInfo eventInfo, String lotName, String carrierName, String destinationMachineName) {
		try {
			if (StringUtils.isNotEmpty(lotName) || StringUtils.isNotEmpty(carrierName)) {
				// Modified by smkang on 2018.12.25 - MachineName is unnecessary to be compared.
//				String condition = "(LOTNAME = ? OR CARRIERNAME = ?) AND MACHINENAME != ? AND RESERVESTATE = ?";
//				Object[] bindSet = new Object[] {lotName, carrierName, destinationMachineName, GenericServiceProxy.getConstantMap().RESV_STATE_RESV};
				String condition = "(LOTNAME = ? OR CARRIERNAME = ?) AND RESERVESTATE = ?";
				Object[] bindSet = new Object[] {lotName, carrierName, GenericServiceProxy.getConstantMap().RESV_STATE_RESV};
				
				List<DspReserveLot> dspReserveLotList = ExtendedObjectProxy.getDspReserveLotService().select(condition, bindSet);
				
				if (dspReserveLotList != null && dspReserveLotList.size() > 0) {
					String reasonComment = "";
					if (eventInfo.getEventName().contains("Transport"))
						reasonComment = "transport to " + destinationMachineName + " by " + eventInfo.getEventUser() + ".";
					else if (eventInfo.getEventName().contains("TrackIn"))
						reasonComment = "starting to run at " + destinationMachineName + ".";
					else if (eventInfo.getEventName().contains("Clean"))
						reasonComment = "starting to clean at " + destinationMachineName + ".";
					
					for (DspReserveLot dspReserveLot : dspReserveLotList) {
						String eventComment = "ReserveLot[" + dspReserveLot.getLotName() + "," + dspReserveLot.getCarrierName() + "," + dspReserveLot.getMachineName() + "] is removed because of " + reasonComment;
						
						// Modified by smkang on 2018.12.25 - If reserved machine and destination machine are same, event name will be recorded as 'Completed'.
						//									  This is discussed with Liu Hongwei.
//						EventInfo reserveLotEventInfo = EventInfoUtil.makeEventInfo("ReserveCancel", eventInfo.getEventUser(), eventComment, eventInfo.getReasonCodeType(), eventInfo.getReasonCode());
						EventInfo reserveLotEventInfo = EventInfoUtil.makeEventInfo(StringUtils.equals(dspReserveLot.getMachineName(), destinationMachineName) ? "Completed" : "ReserveCancel", eventInfo.getEventUser(), eventComment, eventInfo.getReasonCodeType(), eventInfo.getReasonCode());
						
						remove(reserveLotEventInfo, dspReserveLot);
						
						logger.info(eventComment);
					}
				}
			}
		} catch (Exception e) {
			logger.info(e);
		}
	}
}