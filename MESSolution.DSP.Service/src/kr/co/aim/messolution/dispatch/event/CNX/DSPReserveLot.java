package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class DSPReserveLot extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element reserveList = SMessageUtil.getBodySequenceItem(doc, "RESERVELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Reserve", this.getEventUser(), this.getEventComment(), "", "");
		
		if(reserveList != null)
		{
			for(Object obj : reserveList.getChildren())
			{
				Element element = (Element)obj;
				String machineName = SMessageUtil.getChildText(element, "MACHINENAME", true);
				String carrierName = SMessageUtil.getChildText(element, "CARRIERNAME", true);
				String lotName = SMessageUtil.getChildText(element, "LOTNAME", true);
				String processOperationName = SMessageUtil.getChildText(element, "PROCESSOPERATIONNAME", true);
				String lotProcessState = SMessageUtil.getChildText(element, "LOTPROCESSSTATE", true);

				
				String note="";
				
				// 2019.09.06 Modify By Park Jeong Su Mantis 4752
				if(StringUtils.equals("A2CCL010", machineName)){
					
				}
				else{
					Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
					
					if(!StringUtils.equals(GenericServiceProxy.getConstantMap().Lot_Wait, lot.getLotProcessState()) || !StringUtils.equals(lotProcessState, lot.getLotProcessState())){
						note +="lotProcessState is not Wait\n";
					}
					if(!StringUtils.equals(processOperationName, lot.getProcessOperationName())){
						note+="LotProcessOperationName is Different\n";
					}
				}
				// 2019.09.06 Modify By Park Jeong Su Mantis 4752
				
				if(!StringUtils.isEmpty(note)){
					note+="Please Click View button";
					throw new CustomException("COMMON-0001",note);
				}
				
				DspReserveLot reserveLotData = null;

				try
				{
					reserveLotData = ExtendedObjectProxy.getDspReserveLotService().selectByKey(false, new Object[] {machineName, carrierName});
				}
				catch (Exception ex)
				{
					reserveLotData = null;
				}

				if(reserveLotData != null)
				{
					throw new CustomException("RECIPE-0009");
				}

				String sql = " SELECT DECODE(POSITION, '-', '0', POSITION + 1) POSITION FROM "
						+ " ( SELECT NVL(MAX(A.POSITION), -1) POSITION " 
						+ " FROM CT_DSPRESERVELOT A "
						+ " WHERE A.MACHINENAME = :MACHINENAME ) ";

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("MACHINENAME", machineName);

				String calPosition = "";
				List<Map<String, Object>> result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
				if ( result.size() > 0 )
				{
					calPosition = (String)result.get(0).get("POSITION");
				}
				else
				{
					calPosition = "0";
				}

				long lPosition = Long.parseLong(calPosition);

				reserveLotData = new DspReserveLot(machineName, carrierName);
				reserveLotData.setLotName(lotName);
				reserveLotData.setPosition(lPosition);
				reserveLotData.setReserveState(GenericServiceProxy.getConstantMap().DSPSTATUS_RESERVED);
				reserveLotData.setLastEventUser(eventInfo.getEventUser());
				reserveLotData.setLastEventComment(eventInfo.getEventComment());
				reserveLotData.setLastEventTime(eventInfo.getEventTime());
				reserveLotData.setLastEventTimekey(eventInfo.getEventTimeKey());
				reserveLotData.setLastEventName(eventInfo.getEventName());

				ExtendedObjectProxy.getDspReserveLotService().create(eventInfo, reserveLotData);
			}
		}
		
		return doc;
	}
}
