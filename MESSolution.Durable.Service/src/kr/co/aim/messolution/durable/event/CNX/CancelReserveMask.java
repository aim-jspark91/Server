package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CancelReserveMask extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserve", this.getEventUser(), this.getEventComment(), "", "");
		SetEventInfo setEventInfo = new SetEventInfo();
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		if (eleBody != null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "RESERVEMASKLIST", true))
			{
				String maskName = SMessageUtil.getChildText(eledur, "MASKNAME", true);
				
				Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
				
				List <ReserveMaskList> resvMaskList = getReservedMaskInfo(maskName);
				
				if (resvMaskList != null)
				{
					for (ReserveMaskList resvInfo : resvMaskList)
					{
						EventInfo eventRemoveMaskInfo = EventInfoUtil.makeEventInfo("CancelReserve", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
						
						ExtendedObjectProxy.getReserveMaskService().remove(eventRemoveMaskInfo, resvInfo);
					}
				}
				
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
			}
		}
		return doc;	
	}
	
	/**
	* @Name : getReservedMaskInfo
	* @Desc : returns list of reservedInfo of a maskName 
	* @author jhlee
	* @date 2016.06.30
	* @param maskName
	*/
     public static List<ReserveMaskList> getReservedMaskInfo(String maskName) throws CustomException
     {
         List<ReserveMaskList> resvMaskList = new ArrayList<ReserveMaskList>();
         
         try
         {
             resvMaskList = ExtendedObjectProxy.getReserveMaskService().select("WHERE maskName = ?", new Object[] { maskName });
         }
         catch(greenFrameDBErrorSignal de)
         {
             if(de.getErrorCode().equals("MASK-0009"))
            	 throw new CustomException("MASK-0007", maskName); 
         }
         return resvMaskList;
     }

}
