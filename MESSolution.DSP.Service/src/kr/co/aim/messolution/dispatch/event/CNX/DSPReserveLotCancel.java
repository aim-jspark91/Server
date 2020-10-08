package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspReserveLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DSPReserveLotCancel extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element reserveList = SMessageUtil.getBodySequenceItem(doc, "RESERVELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveCancel", this.getEventUser(), this.getEventComment(), "", "");
		
		if(reserveList != null)
		{
			for(Object obj : reserveList.getChildren())
			{
				Element element = (Element)obj;
				String machineName = SMessageUtil.getChildText(element, "MACHINENAME", true);
				String carrierName = SMessageUtil.getChildText(element, "CARRIERNAME", true);
				String lotName = SMessageUtil.getChildText(element, "LOTNAME", true);

				DspReserveLot reserveLotData = null;

				try
				{
					reserveLotData = ExtendedObjectProxy.getDspReserveLotService().selectByKey(false, new Object[] {machineName, carrierName});
				}
				catch (Exception ex)
				{
					reserveLotData = null;
				}

				if(reserveLotData == null)
				{
					throw new CustomException("RECIPE-0009", "");
				}

				long position = reserveLotData.getPosition();
				
				//reserveLotData = new DspReserveLot(machineName, carrierName);
				reserveLotData.setLastEventUser(eventInfo.getEventUser());
				reserveLotData.setLastEventComment(eventInfo.getEventComment());
				reserveLotData.setLastEventTime(eventInfo.getEventTime());
				reserveLotData.setLastEventTimekey(eventInfo.getEventTimeKey());
				reserveLotData.setLastEventName(eventInfo.getEventName());

				ExtendedObjectProxy.getDspReserveLotService().remove(eventInfo, reserveLotData);
				
				String sql = " SELECT MACHINENAME, CARRIERNAME, POSITION "
						+ " FROM CT_DSPRESERVELOT A "
						+ " WHERE A.MACHINENAME = :MACHINENAME "
						+ " AND A.POSITION > :POSITION "
						+ " ORDER BY A.POSITION ";

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("MACHINENAME", machineName);
				bindMap.put("POSITION", Long.toString(position));

				List<Map<String, Object>> result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
				if ( result.size() > 0 )
				{
					EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("ChangePostion", this.getEventUser(), this.getEventComment(), "", "");
					
					for (int i = 0; i < result.size(); i++ )
					{
						String resMachineName = (String)result.get(i).get("MACHINENAME");
						String resCarrierName = (String)result.get(i).get("CARRIERNAME");
						long resPosition = Long.parseLong(result.get(i).get("POSITION").toString());
						
						DspReserveLot resData = null;
						try
						{
							resData = ExtendedObjectProxy.getDspReserveLotService().selectByKey(false, new Object[] {resMachineName, resCarrierName});
						}
						catch (Exception ex)
						{
							resData = null;
						}

						if(resData == null)
						{
							throw new CustomException("RECIPE-0009", "");
						}
						
						//resData = new DspReserveLot(resMachineName, resCarrierName);
						resData.setPosition(resPosition - 1);
						resData.setLastEventUser(eventInfo.getEventUser());
						resData.setLastEventComment(eventInfo.getEventComment());
						resData.setLastEventTime(eventInfo.getEventTime());
						resData.setLastEventTimekey(eventInfo.getEventTimeKey());
						resData.setLastEventName(eventInfo.getEventName());
						
						ExtendedObjectProxy.getDspReserveLotService().modify(eventInfo1, resData);
					}
				}	
			}
		}
		
		return doc;
	}
}
