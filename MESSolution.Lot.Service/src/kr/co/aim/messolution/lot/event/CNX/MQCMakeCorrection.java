package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCJobOper;
import kr.co.aim.messolution.extended.object.management.data.MQCJobPosition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;

import org.jdom.Document;
import org.jdom.Element;

public class MQCMakeCorrection extends SyncHandler 
{
	public Object doWorks(Document doc) throws CustomException 
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MQCMakeCorrection", getEventUser(), "MQCMakeCorrection", "", "");

		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String MQCJobName = SMessageUtil.getBodyItemValue(doc, "MQCJOBNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		
		List<Element> processOperationList = SMessageUtil.getBodySequenceItemList(doc, "PROCESSOPERATIONLIST", false);
		List<Element> deleteProcessOperationList = SMessageUtil.getBodySequenceItemList(doc, "DELETEPROCESSOPERATIONLIST", false);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		
		// 새롭게 추가 된 Operation MQC Job Operation 에 추가하기 
		for (Element processOperation : processOperationList)
		{
			String processOperationName = SMessageUtil.getChildText(processOperation, "PROCESSOPERATIONNAME", true);
			//YHU 20190710 Add ~ Mantis 4337 =====>>
			ProcessOperationSpecKey poSpecKey = new ProcessOperationSpecKey(System.getProperty("shop").toString(), processOperationName, "00001");
			ProcessOperationSpec poSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(poSpecKey);
			
			String machineName = SMessageUtil.getChildText(processOperation, "MACHINENAME", false);
			//<<===== YHU 20190710 Add ~ Mantis 4337
			
			MQCJobOper MQCJobOper = null;
			try {
				MQCJobOper = ExtendedObjectProxy.getMQCJobOperService().selectByKey(false, new Object[]{MQCJobName,processOperationName,"00001"});
			} catch (Exception e) {
				MQCJobOper = new MQCJobOper(MQCJobName,processOperationName,"00001");
				MQCJobOper.setLastEventComment(eventInfo.getEventComment());
				MQCJobOper.setLastEventName(eventInfo.getEventName());
				MQCJobOper.setLastEventTime(eventInfo.getEventTime());
				MQCJobOper.setLastEventTimeKey(eventInfo.getEventTimeKey());
				MQCJobOper.setLastEventUser(eventInfo.getEventUser());
				//YHU 20190710 Add ~ Mantis 4337 =====>>
				MQCJobOper.setmachineGroupName(poSpec.getUdfs().get("MACHINEGROUPNAME").toString());
				MQCJobOper.setmachineName(machineName);
				//<<===== YHU 20190710 Add ~ Mantis 4337
				
				ExtendedObjectProxy.getMQCJobOperService().create(eventInfo, MQCJobOper);
			}
		}
		// 새롭게 추가 된 Operation의 Product MQC Job Position 에 추가하기
		for (Element product : productList)
		{
			String processOperationName = SMessageUtil.getChildText(product, "PROCESSOPERATIONNAME", true);
			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			String position = SMessageUtil.getChildText(product, "POSITION", true);
			//YHU 20190710 Add ~ Mantis 4337
			String recipeName = SMessageUtil.getChildText(product, "RECIPENAME", false);
			
			MQCJobPosition MQCJobPosition =null;
			try {
				MQCJobPosition = ExtendedObjectProxy.getMQCJobPositionService().selectByKey(false, new Object[]{MQCJobName,processOperationName,"00001",position});
			} catch (Exception e) {
				MQCJobPosition = new MQCJobPosition(MQCJobName, processOperationName, "00001", Long.parseLong(position));
				MQCJobPosition.setmqcCountUp(0);
				MQCJobPosition.setproductName(productName);
				MQCJobPosition.setLastEventComment(eventInfo.getEventComment());
				MQCJobPosition.setLastEventName(eventInfo.getEventName());
				MQCJobPosition.setLastEventTime(eventInfo.getEventTime());
				MQCJobPosition.setLastEventTimeKey(eventInfo.getEventTimeKey());
				MQCJobPosition.setLastEventUser(eventInfo.getEventUser());
				//YHU 20190710 Add ~ Mantis 4337
				MQCJobPosition.setrecipeName(recipeName);
				
				ExtendedObjectProxy.getMQCJobPositionService().create(eventInfo, MQCJobPosition);
			}
		}
		
		// 삭제 된 Operation의 Product MQC Job Operation 에서 삭제하기
		for (Element deleteProcessOepration : deleteProcessOperationList)
		{
			String processOperationName = SMessageUtil.getChildText(deleteProcessOepration, "PROCESSOPERATIONNAME", true);
			
			MQCJobOper MQCJobOper = null;
			try {
				MQCJobOper = ExtendedObjectProxy.getMQCJobOperService().selectByKey(false, new Object[]{MQCJobName,processOperationName,"00001"});
			} catch (Exception e) {

			}
			
			if(MQCJobOper!=null){
				ExtendedObjectProxy.getMQCJobOperService().remove(eventInfo, MQCJobOper);
			}

			String condition = "WHERE MQCJOBNAME = ? AND PROCESSOPERATIONNAME = ?";
			Object[] bindSet = new Object[]{MQCJobName,processOperationName};

			List<MQCJobPosition> MQCJobPositionList = ExtendedObjectProxy.getMQCJobPositionService().select(condition, bindSet);
			
			for(MQCJobPosition position : MQCJobPositionList){
				ExtendedObjectProxy.getMQCJobPositionService().remove(eventInfo, position);
			}
			
		}


		return doc;
	}
}