package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class MaskStateReport extends AsyncHandler{
	
	@Override
	public void doWorks(Document doc) throws CustomException {
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskStateReport", getEventUser(), "Host synchronized EAS", "Host Synchronize with EAS", "Synchronize Host Db");
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		//String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		
		List<Element> maskList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "MASKLIST", false);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		
		for (Element maskElement : maskList)
		{
			String maskName = SMessageUtil.getChildText(maskElement, "MASKNAME", true);
			String position = SMessageUtil.getChildText(maskElement, "POSITION", false);
			String unitName = SMessageUtil.getChildText(maskElement, "UNITNAME", false);
			String subUnitName = SMessageUtil.getChildText(maskElement, "SUBUNITNAME", false);
			String usedCount = SMessageUtil.getChildText(maskElement, "USEDCOUNT", false);
			String usedCountLimit = SMessageUtil.getChildText(maskElement, "USEDCOUNTLIMIT", false);
			String maskAoiState  = SMessageUtil.getChildText(maskElement, "MASKAOISTATE", false);
			String maskPPAState = SMessageUtil.getChildText(maskElement, "MASKPPASTATE", false);
			String maskJudge = SMessageUtil.getChildText(maskElement, "MASKJUDGE", false);
			
			// Validation if mask data is exist at host,then update the following data,else do nothing.
			List <Durable> sqlResult = new ArrayList <Durable>();
			try
			{
				sqlResult = DurableServiceProxy.getDurableService().select("DURABLENAME = ?", new Object[] {maskName});
			}
			catch (NotFoundSignal ne)
			{
				sqlResult = new ArrayList <Durable>();
			}
			if (sqlResult.size() > 0)
			{
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
				Durable maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(maskName));
				
				//remove Mask reserve Data and update maskCSTData
				{
					String condition = "WHERE maskName =?";
					
					Object[] bindSet = new Object[] {maskName};
					ReserveMaskList resvMaskList = null;
					try
					{
						resvMaskList = ExtendedObjectProxy.getReserveMaskService().select(condition, bindSet).get(0);
					}
					catch(greenFrameDBErrorSignal de)
					{				
						resvMaskList=null;
					}
					
					if (resvMaskList != null)
					{		
						if(resvMaskList.getCarrierName().isEmpty())
						{
							EventInfo eventRemoveMaskInfo = EventInfoUtil.makeEventInfo("Remove", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);							
							ExtendedObjectProxy.getReserveMaskService().remove(eventRemoveMaskInfo, resvMaskList);
						}

					}
					
					//handle MaskCSTData
					String maskCST = CommonUtil.getValue(maskData.getUdfs(), "MASKCARRIERNAME");
					if(!maskCST.isEmpty())
					{
						// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//						Durable maskCSTData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskCST);
						Durable maskCSTData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(maskCST));
						
						if (maskCSTData.getLotQuantity() > 0)
							maskCSTData.setLotQuantity(maskCSTData.getLotQuantity() - 1);
						
						String maskTransportState = CommonUtil.getValue(maskCSTData.getUdfs(), "TRANSPORTSTATE");
						
						if(maskCSTData.getLotQuantity()<1 && maskTransportState.equals("OUTSTK"))
						{
							maskCSTData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
						}
						
						DurableServiceProxy.getDurableService().update(maskCSTData);						
						SetEventInfo setCSTEvent = new SetEventInfo();
						setCSTEvent.setUdfs(maskCSTData.getUdfs()); 						
						eventInfo.setEventName("update");
						
						MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskCSTData, setCSTEvent, eventInfo);
					}
				}
				
				Map<String, String> udfs = maskData.getUdfs();
				udfs.put("MACHINENAME", machineName);
				if(!position.isEmpty() && Integer.parseInt(position) > 0)
				{
					udfs.put("MASKPOSITION", position);
				}
				udfs.put("UNITNAME", unitName);
				udfs.put("POSITIONNAME", subUnitName);
				udfs.put("AOISTATE", maskAoiState);
				udfs.put("PPASTATE",maskPPAState);
				udfs.put("JUDGE", maskJudge);
				udfs.put("MASKCARRIERNAME", "");		
				udfs.put("TRANSPORTSTATE", "STORED");	
				
				if(maskData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available))
				{
					maskData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
				}
				
				if(StringUtils.isNotEmpty(maskAoiState) || StringUtils.isNotEmpty(maskPPAState))
				{
					udfs.put("INSPECTSTATE", "Inspected");
				}
				
				DurableServiceProxy.getDurableService().update(maskData);	
				//udfs.put("USEDCOUNT", usedCount);
				//udfs.put("USEDCOUNTLIMIT", usedCountLimit);
				setEventInfo.setUdfs(udfs);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);				
			}
			else
				continue;
		}
		
	}
}
