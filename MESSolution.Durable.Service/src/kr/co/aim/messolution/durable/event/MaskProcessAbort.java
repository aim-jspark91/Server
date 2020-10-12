package kr.co.aim.messolution.durable.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.extended.object.management.impl.ReserveMaskListService;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class MaskProcessAbort extends AsyncHandler {
	
	private static Log log = LogFactory.getLog(MaskProcessEnd.class);
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", this.getEventUser(), this.getEventComment(), null, null);
		
		String messageName = this.getMessageName();
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		 
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String carrierType = SMessageUtil.getBodyItemValue(doc, "CARRIERTYPE", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String maskQuantity = SMessageUtil.getBodyItemValue(doc, "MASKQUANTITY", false);
		String slotMap = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", false);
		String inputMaskMap = SMessageUtil.getBodyItemValue(doc, "INPUTMASKMAP", false);
		List<Element> maskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);
		
		// getMaskInfoByDurableName 
		List<Durable> durMaskList  = (List<Durable>) CommonUtil.getMaskInfoBydurableName(carrierName,"EVAMask");
		
		//remove reserve data
		List<ReserveMaskList> reserveMaskList = null;
		int resvMaskQTY = 0;
		
		if (durMaskList != null)
		{
			for (Element eleMask : SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false))
			{
				String sMaskName = SMessageUtil.getChildText(eleMask, "MASKNAME", false);
				String sMaskGroupName = SMessageUtil.getChildText(eleMask, "MASKGROUPNAME", false);
				String sMaskPosition = SMessageUtil.getChildText(eleMask, "POSITION", false);
				String sMaskType = SMessageUtil.getChildText(eleMask, "MASKTYPE", false);
				String sMaskRecipeName = SMessageUtil.getChildText(eleMask, "MASKRECIPENAME", false);
				String sMaskUsedLimit = SMessageUtil.getChildText(eleMask, "MASKUSEDLIMIT", false);
				String sProductName = SMessageUtil.getChildText(eleMask, "PRODUCTNAME", false);
				String sMaskOffSetX = SMessageUtil.getChildText(eleMask, "MASK_OFFSET_X", false);
				String sMaskOffSetY = SMessageUtil.getChildText(eleMask, "MASK_OFFSET_Y", false);
				String sMaskOffSetT = SMessageUtil.getChildText(eleMask, "MASK_OFFSET_THETA", false);
				String sMaskThickness = SMessageUtil.getChildText(eleMask, "MASKTHICKNESS", false);
				String sMaskMagnet = SMessageUtil.getChildText(eleMask, "MASKMAGNET", false);
				
				//remove Reserved Data 
				try
				{
					reserveMaskList = ReserveMaskListService.getMaskInfoByCarreierName(machineName, portName, carrierName);
					
					if (reserveMaskList != null)
					{
						resvMaskQTY = reserveMaskList.size();
						
						for (int i = 0; i < resvMaskQTY; i++)
						{
							String reserveConditionID = reserveMaskList.get(i).getConditionID();
							String reserveMachineName = reserveMaskList.get(i).getMachineName();
							String reservePortName = reserveMaskList.get(i).getPortName();
							String reservemaskName = reserveMaskList.get(i).getMaskName();
							String reserveCarrierName = reserveMaskList.get(i).getCarrierName();
							String reservePostion  = reserveMaskList.get(i).getPosition();
							
							if(!StringUtils.equals(reserveMachineName, machineName))
								throw new CustomException("MASK-0014",reserveMachineName);
							
							if(!StringUtils.equals(reservePortName, portName))
								throw new CustomException("MASK-0015",reservePortName);
							
							if(!StringUtils.equals(reserveCarrierName, carrierName)) 
								throw new CustomException("MASK-0017",reserveCarrierName);
							
							if(StringUtils.equals(reservemaskName, sMaskName))
							{
								if (!StringUtils.equals(reservePostion, sMaskPosition))
									throw new CustomException("MASK-0018",reservePostion);
								else
								{
									ReserveMaskList reserveMaskdataInfo = new ReserveMaskList(reserveConditionID);
									
									EventInfo eventRemoveMaskInfo = EventInfoUtil.makeEventInfo("remove", this.getEventUser(), this.getEventComment(), null, null);
									
									ExtendedObjectProxy.getReserveMaskService().remove(eventRemoveMaskInfo, reserveMaskdataInfo);
								}
							}
						}
					}
				}
				catch(Exception e)
				{
					reserveMaskList = null;
					log.info("Mask not reserved.");
				}
				
				//GetMaskData
				Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sMaskName);
				
				//getMaskUDF Data
				Map<String, String> maskUdfs = maskData.getUdfs();
				
				String position = maskUdfs.get("MASKPOSITION");
				
				if(position.equals(sMaskPosition) == false)
				{
					throw new CustomException("MASK-0011",maskData.getKey());
				}
				if(maskData.getUdfs().get("MASKCARRIERNAME").equals(carrierName) == false)
				{
					throw new CustomException("MASK-0008",carrierName);
				}
				// Put data into UDF
				StringBuffer maskOffSetBuffer = new StringBuffer();
				
				if(StringUtils.isNotEmpty(sMaskOffSetX))
				{
					maskOffSetBuffer.append(sMaskOffSetX);
					maskOffSetBuffer.append('^');
				}
				if(StringUtils.isNotEmpty(sMaskOffSetY))
				{
					maskOffSetBuffer.append(sMaskOffSetY);
					maskOffSetBuffer.append('^');
				}
				if(StringUtils.isNotEmpty(sMaskOffSetT))
				{
					maskOffSetBuffer.append(sMaskOffSetT);
					maskOffSetBuffer.append('^');
				}
				//SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setMaskProcessEndInfo(maskUdfs,machineName,unitName,carrierName, maskOffSetBuffer.toString());
				
				SetEventInfo setEventInfo = new SetEventInfo();

				//Make DurableKey
				DurableKey durableKey = new DurableKey();
				durableKey.setDurableName(sMaskName);
				
				// Excute greenTrack API call- setEvent 
				DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfo);
			}
			
		}	
	}
}
