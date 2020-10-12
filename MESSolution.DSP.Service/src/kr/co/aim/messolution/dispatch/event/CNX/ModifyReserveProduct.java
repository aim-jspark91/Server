package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspReserveProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ModifyReserveProduct extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String reserveName = SMessageUtil.getBodyItemValue(doc, "RESERVENAME", true);
		String positionName = SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		String productionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		//String machineGroupName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String machineGroupName = "-";
		String setCount = SMessageUtil.getBodyItemValue(doc, "SETCOUNT", true);
		//String currentCount = SMessageUtil.getBodyItemValue(doc, "CURRENTCOUNT", true);
		String currentCount = "";
		String useFlag = SMessageUtil.getBodyItemValue(doc, "USEFLAG", true);
		//	Delete	2019.01.29	AIM.YUNJM	Mantis : 0002626		
//		String recycleFlag = SMessageUtil.getBodyItemValue(doc, "RECYCLEFLAG", false);
		String skipFlag = SMessageUtil.getBodyItemValue(doc, "SKIPFLAG", true);
		//	DELETE 2019.03.07 AIM.YUNJM	Mantis : 0002970
		//	ADD	2019.01.29	AIM.YUNJM	Mantis : 0002626
//		String activeFlag = SMessageUtil.getBodyItemValue(doc, "ACTIVEFLAG", false);
		
		List<Map<String, Object>> reserveProductPositionInfo = MESDSPServiceProxy.getDSPServiceUtil().getReserveProductPosition_Modify(machineName,portName,reserveName);
		
		if(reserveProductPositionInfo.size() > 0 )
		{
			for(int i = 0; i < reserveProductPositionInfo.size(); i ++)
			{
				if(reserveProductPositionInfo.get(i).get("POSITION").equals(positionName))
				{
					throw new CustomException("DSP-0000", productSpecName);
				}
			}
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
			
		DspReserveProduct reserveProductData = null;
		
		try
		{
			reserveProductData = ExtendedObjectProxy.getDspReserveProductService().selectByKey(false, new Object[] {machineName, unitName, portName, reserveName});
		}
		catch (Exception ex)
		{
			throw new CustomException("DSP-0005", machineName, unitName, portName, reserveName);
		}
		
		// 2019.05.16_hsryu_Add validation. Mantis 0003880.
		this.validationModify(reserveProductData, productionType, productSpecName, ecCode, processFlowName, processOperationName, setCount, positionName);
		
		reserveProductData.setMachineGroupName(machineGroupName);
//		reserveProductData.setRecycleFlag(recycleFlag);
		reserveProductData.setSkipFlag(skipFlag);
		reserveProductData.setProductionType(productionType);
		reserveProductData.setProductSpecName(productSpecName);
		reserveProductData.setEcCode(ecCode);
		reserveProductData.setPositionName(positionName);
		reserveProductData.setProcessFlowName(processFlowName);
		reserveProductData.setProcessOperationName(processOperationName);
		reserveProductData.setUseFlag(useFlag);
		reserveProductData.setSetCount(Long.parseLong(setCount));
		//reserveProductData.setCurrentCount(Long.parseLong(currentCount));
		reserveProductData.setLastEventUser(eventInfo.getEventUser());
		reserveProductData.setLastEventComment(eventInfo.getEventComment());
		reserveProductData.setLastEventTime(eventInfo.getEventTime());
		reserveProductData.setLastEventTimekey(eventInfo.getEventTimeKey());
		reserveProductData.setLastEventName(eventInfo.getEventName());
//		reserveProductData.setActiveFlag(activeFlag);
		
		if(StringUtils.equals(reserveProductData.getReserveState(), "")){
			reserveProductData.setReserveState(GenericServiceProxy.getConstantMap().DSPSTATUS_RESERVED);
		}
		
		ExtendedObjectProxy.getDspReserveProductService().modify(eventInfo, reserveProductData);
		
		return doc;
	}
	
	// 2019.05.16_hsryu_Insert Logic. Mantis 0003880.
	private void validationModify(DspReserveProduct dspReserveProduct, String productionType, String productSpecName,
			String ecCode, String processFlowName, String processOperationName, String setCount, String position) throws CustomException {
		
		// if ReserveState is 'Completed', can't modify.
		if(StringUtils.equals(dspReserveProduct.getReserveState(), GenericServiceProxy.getConstantMap().DSPSTATUS_COMPLETED)){
			//ReserveState is Completed ! It cannot be modified.
			throw new CustomException("DSP-0003");
		}
//		2019.05.24 Modify dskim by Mantis 0004023
//		// if ReserveState is 'Executing', can't modify except 'USEFLAG','SKIPFLAG'.
//		if(StringUtils.equals(dspReserveProduct.getReserveState(), GenericServiceProxy.getConstantMap().DSPSTATUS_EXECUTING)){
//			if(!StringUtils.equals(dspReserveProduct.getProductionType(), productionType)
//				||!StringUtils.equals(dspReserveProduct.getEcCode(), ecCode)
//				||!StringUtils.equals(dspReserveProduct.getProcessFlowName(), processFlowName)
//				||!StringUtils.equals(dspReserveProduct.getProcessOperationName(), processOperationName)
//				||!StringUtils.equals(String.valueOf(dspReserveProduct.getSetCount()), setCount)
//				||!StringUtils.equals(dspReserveProduct.getPosition(), position)){
//				//if ReserveState is ''Executing'', You can only change ''UseFlag'' & ''SkipFlag'
//				throw new CustomException("DSP-0004");
//			}
//		}
		
		if(dspReserveProduct.getCurrentCount() != 0 && (Integer.parseInt(setCount) < dspReserveProduct.getCurrentCount() 
				|| Integer.parseInt(setCount) == dspReserveProduct.getCurrentCount()))
		{
			//setCount[{0}] must be greater than CurrnetCount[{1}].
			throw new CustomException("DSP-0006", setCount, dspReserveProduct.getCurrentCount());
		}
	}
}
