package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReserveLotToMachine extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		List<Element> reserveLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangePosition", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		String sProductSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sProcessOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		
		
		//Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
		
		List<ReserveLot> currentBook = getCurrentReserveLotList(sMachineName);
		
		for (Element eleLot : reserveLotList)
		{
			String sLotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			String sPosition = SMessageUtil.getChildText(eleLot, "POSITION", true);
			
			//ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(
			//								sFactoryName, sProductSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			
			ReserveLot reserveData = null;
			
			try
			{
				eventInfo.setEventName("Adjust");
				
				reserveData = ExtendedObjectProxy.getReserveLotService().selectByKey(true, new Object[] {sMachineName, sLotName});
				
				//it is meaning 
				if (reserveData.getPosition() != Long.parseLong(sPosition))
				{
					reserveData.setPosition(Long.parseLong(sPosition));
					reserveData = ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveData);
				}
			}
			catch (greenFrameDBErrorSignal ne)
			{
				if (ne.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				{
					eventInfo.setEventName("Assign");
					
					reserveData = new ReserveLot(sMachineName, sLotName);
					reserveData.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_RESV);
					reserveData.setMachineName(sMachineName);
					reserveData.setProcessOperationName(sProcessOperationName);
					reserveData.setProductSpecName(sProductSpecName);
					reserveData.setPosition(Long.parseLong(sPosition));
					reserveData.setReserveTimeKey(eventInfo.getEventTimeKey());
					
					reserveData = ExtendedObjectProxy.getReserveLotService().create(eventInfo, reserveData);
				}
			}
			finally
			{
				//remove delivered data
				for (int idx=0; idx < currentBook.size(); idx++)
				{
					//Lot ID is unique
					if (reserveData != null && currentBook.get(idx).getLotName().equals(reserveData.getLotName()))
						currentBook.remove(idx);
				}
			}
		}
		
		//unassigned from book with no longer existing reservations
		for (ReserveLot data : currentBook)
		{
			try
			{
				eventInfo.setEventName("Deassign");
				ExtendedObjectProxy.getReserveLotService().remove(eventInfo, data);
			}
			catch (greenFrameDBErrorSignal ne)
			{
				eventLog.warn(String.format("Lot[%s] has not been unassigned from Machine[%s]", data.getLotName(), data.getMachineName()));
			}
			catch (Exception ex)
			{
				eventLog.warn(String.format("Lot[%s] has not been unassigned from Machine[%s]", data.getLotName(), data.getMachineName()));
			}
		}

		return doc;
	}

	/**
	 * get book in EQP, no result means no reservations or any error
	 * 141103 by swcho : service object changed
	 * @author swcho
	 * @since 2014.08.22
	 * @param machineName
	 * @return
	 * @throws CustomException
	 */
	private List<ReserveLot> getCurrentReserveLotList(String machineName)
		throws CustomException
	{
		try
		{
			String query = "machineName = ? AND reserveState <> ? ORDER BY position";
			Object[] bindList = new Object[] {machineName, GenericServiceProxy.getConstantMap().Flag_Y};
			
			List<ReserveLot> result = 
				ExtendedObjectProxy.getReserveLotService().select(query, bindList);
			
			return result;
		}
		catch (greenFrameDBErrorSignal ex)
		{
			//throw new CustomException();
		}
		catch (Exception ex)
		{
			//throw new CustomException();
		}
		
		return new ArrayList<ReserveLot>();
	}
}
