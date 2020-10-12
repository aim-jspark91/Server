package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspReserveProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateReserveProductSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		//String machineGroupName = SMessageUtil.getBodyItemValue(doc, "MACHINEGROUPNAME", true);
		String machineGroupName = "-";
		String PortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String productionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		//	Delete	2019.01.29	AIM.YUNJM	Mantis : 0002626	
//		String recycleFlag = SMessageUtil.getBodyItemValue(doc, "RECYCLEFLAG", false);
		//String ReserveState = SMessageUtil.getBodyItemValue(doc, "RESERVESTATE", true);
		String reserveState = GenericServiceProxy.getConstantMap().DSPSTATUS_RESERVED;
		String ReserveName = "";
		
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String useFlag = SMessageUtil.getBodyItemValue(doc, "USEFLAG", true);
		String setCount = SMessageUtil.getBodyItemValue(doc, "SETCOUNT", true);
		String currentCount = SMessageUtil.getBodyItemValue(doc, "CURRENTCOUNT", true);
		String skipFlag = SMessageUtil.getBodyItemValue(doc, "SKIPFLAG", true);
		String positionName = SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		//	DELETE 2019.03.07 AIM.YUNJM	Mantis : 0002970
		//	ADD	2019.01.29	AIM.YUNJM	Mantis : 0002626
//		String activeFlag = SMessageUtil.getBodyItemValue(doc, "ACTIVEFLAG", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		ReserveName = MESDSPServiceProxy.getDSPServiceUtil().getNextPositionForDSPReserveProductSpec(machineName);
			
		DspReserveProduct dspReserveProduct = null;
		
		try
		{
			dspReserveProduct = ExtendedObjectProxy.getDspReserveProductService().selectByKey(false, new Object[] {machineName, PortName, ReserveName, unitName});
					
		}
		catch (Exception ex)
		{
			dspReserveProduct = null;
			
		}
		
		if(dspReserveProduct != null)
		{
			throw new CustomException("DSP-0000", productSpecName);
		}
		dspReserveProduct = new DspReserveProduct(machineName, PortName,ReserveName, unitName);
		dspReserveProduct.setMachineGroupName(machineGroupName);
		dspReserveProduct.setReserveState(reserveState);;
//		dspReserveProduct.setRecycleFlag(recycleFlag);
		dspReserveProduct.setSkipFlag(skipFlag);
		dspReserveProduct.setProductionType(productionType);
		dspReserveProduct.setProductSpecName(productSpecName);
		dspReserveProduct.setEcCode(ecCode);
		dspReserveProduct.setPositionName(positionName);
		dspReserveProduct.setProcessFlowName(processFlowName);
		dspReserveProduct.setProcessOperationName(processOperationName);
		dspReserveProduct.setUseFlag(useFlag);
		dspReserveProduct.setSetCount(Long.parseLong(setCount));
//		dspReserveProduct.setActiveFlag(activeFlag);
		try {
			dspReserveProduct.setCurrentCount(Long.parseLong(currentCount));
		} catch (Exception e) {
			throw new CustomException("Long Parse Error");
		}
		dspReserveProduct.setLastEventUser(eventInfo.getEventUser());
		dspReserveProduct.setLastEventComment(eventInfo.getEventComment());
		dspReserveProduct.setLastEventTime(eventInfo.getEventTime());
		dspReserveProduct.setLastEventTimekey(eventInfo.getEventTimeKey());
		dspReserveProduct.setLastEventName(eventInfo.getEventName());
		
		ExtendedObjectProxy.getDspReserveProductService().create(eventInfo, dspReserveProduct);
		
	//	List<Map<String, Object>> reserveProductSpecInfo = MESDSPServiceProxy.getDSPServiceUtil().getReserveProductData(machineName, PortName, productSpecName);
		
//		List<Map<String, Object>> reserveProductPositionInfo = MESDSPServiceProxy.getDSPServiceUtil().getReserveProductPosition(machineName,PortName);
//		
//		if(reserveProductSpecInfo.size() > 0)
//		{
//			throw new CustomException("SPEC-0001", productSpecName);
//		}
		
		
//		if(reserveProductPositionInfo.size() > 0 )
//		{
//			for(int i = 0; i < reserveProductPositionInfo.size(); i ++)
//			{
//				if(reserveProductPositionInfo.get(i).get("POSITION").equals(Position))
//				{
//					throw new CustomException("DSP-0000", productSpecName);
//				}
//			}
//		}
	
//		MESDSPServiceProxy.getDSPServiceImpl().insertDSPReserveProductSpec(
//				eventInfo, machineName,machineGroupName,PortName ,productionType, productSpecName, ecCode,recycleFlag, ReserveName,
//				processFlowName, processOperationName, useFlag, setCount, currentCount, skipFlag, positionName );

		return doc;
	}

}
