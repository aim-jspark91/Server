package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CorresSampleLot;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
//import kr.co.aim.greentrack.lot.LotServiceProxy; modify byJHIYING on20190830 mantis:4700
import kr.co.aim.greentrack.lot.management.data.Lot;
//import kr.co.aim.greentrack.lot.management.info.SetEventInfo; modify byJHIYING on20190830 mantis:4700

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelFutureAction extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		List<Element> futureActionElementList = SMessageUtil.getBodySequenceItemList(doc, "FUTUREACTIONLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelFutureAction", this.getEventUser(), this.getEventComment(), "", "");
		
		for (Element eledur : futureActionElementList)
		{
			String slotName = SMessageUtil.getChildText(eledur, "LOTNAME", true);
			String sFactoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String sProcessFlowName = SMessageUtil.getChildText(eledur, "PROCESSFLOWNAME", true);
			String sProcessOperationName = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONNAME", true);
			String sPosition = SMessageUtil.getChildText(eledur, "POSITION", true);
			String sActionName = SMessageUtil.getChildText(eledur, "ACTIONNAME", true);
			String sampleProcessFlowName = SMessageUtil.getChildText(eledur, "SAMPLEPROCESSFLOWNAME", false);
			
			List<LotAction> sampleActionList = new ArrayList<LotAction>();

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(slotName);
			
			// Modified by smkang on 2018.12.05 - According to Liu Hongwei's request, if HOLDPERMANENTFLAG is Y, future action of Executed state can be also canceled.
//			String condition = " WHERE 1=1 AND lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND processOperationName = ?"
//					+ " AND processOperationVersion = ? AND position = ? AND actionName = ? AND actionState = ? ";
//			Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), sFactoryName, sProcessFlowName, "00001",
//					sProcessOperationName, "00001", sPosition, sActionName ,"Created"};
			String condition = " WHERE LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ?" +
								" AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND POSITION = ? AND ACTIONNAME = ?" +
								" AND (ACTIONSTATE = ? OR ACTIONSTATE = 'Merged' OR (ACTIONSTATE = ? AND HOLDPERMANENTFLAG = ?))";
			Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), sFactoryName, sProcessFlowName, "00001",
								sProcessOperationName, "00001", sPosition, sActionName,
								GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED, GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED, "Y"};
			
			if(StringUtils.equals("Sampling", sActionName))
			{
				ExtendedObjectProxy.getLotActionService().checkFlowNameAndNodeStack(slotName, sampleProcessFlowName);
			}
			
			try
			{
				sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
				
				LotAction lotAction = new LotAction();
				
				lotAction = sampleActionList.get(0);
				
				
				//2019.08.26 dmlee : SUM Actual SamplePosition [Reserve Sampling] - [System Sampling]
				try
				{
					
					//CT_SAMPLELOT Merge
					List<SampleLot> sampleLotList = null;
					try
					{
						sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? and fromProcessOperationName = ? and fromProcessOperationVersion = ? "
								, new Object[] {lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"),
								lotAction.getSampleProcessFlowName(), lotAction.getSampleProcessFlowVersion(), lotAction.getProcessOperationName(), lotAction.getProcessOperationVersion() });	
						
					}
					catch(greenFrameDBErrorSignal ne)
					{
						eventLog.info("Already Decide Sampling Data Non Exist (CT_SAMPLELOT)");
					}
					
					if(sampleLotList != null)
					{
						
						//2019.09.04 dmlee : Request By CIM (All Sample Info Cancel)
						for(SampleLot sampleLotData : sampleLotList)
						{	
							eventLog.info("Already Decide Sampling Data Exist, SUM Actual Sample Position ... (CT_SAMPLELOT)");
							
							eventInfo.setEventName("SplitActualPosition");

							sampleLotData.setManualSamplePosition("");
							sampleLotData.setActualSamplePosition(sampleLotData.getSystemSamplePosition());
							ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotData);
						}
					}
				}
				catch(Exception ex)
				{
					eventLog.error("SUM Actual Sample Position Fail ! (CT_SAMPLELOT)");
				}
					
				try
				{
					//CT_CORRESSAMPLELOT
					List<CorresSampleLot> corresSampleLotList = null;
					try
					{
						corresSampleLotList = ExtendedObjectProxy.getCorresSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? and fromProcessOperationName = ? and fromProcessOperationVersion = ? "
								, new Object[] {lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"),
								lotAction.getSampleProcessFlowName(), lotAction.getSampleProcessFlowVersion(), lotAction.getProcessOperationName(), lotAction.getProcessOperationVersion() });	
						
					}
					catch(greenFrameDBErrorSignal ne)
					{
						eventLog.info("Already Decide Sampling Data Non Exist (CT_CORRESSAMPLELOT)");
					}
					
					if(corresSampleLotList != null)
					{
						
						//2019.09.04 dmlee : Request By CIM (All Sample Info Cancel)
						for(CorresSampleLot corresSampleLotData : corresSampleLotList)
						{	
							eventLog.info("Already Decide Sampling Data Exist, SUM Actual Sample Position ... (CT_CORRESSAMPLELOT)");
							
							eventInfo.setEventName("SplitActualPosition");

							corresSampleLotData.setManualSamplePosition("");
							corresSampleLotData.setActualSamplePosition(corresSampleLotData.getSystemSamplePosition());
							ExtendedObjectProxy.getCorresSampleLotService().modify(eventInfo, corresSampleLotData);
						}
					}
				}
				catch(Exception ex)
				{
					eventLog.error("SUM Actual Sample Position Fail ! (CT_CORRESSAMPLELOT)");
				}
				//2019.08.26 dmlee : SUM Actual SamplePosition End-----------------------------------
				
				
				//Remove CT_LOTACTION
				lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CANCELED);
				ExtendedObjectProxy.getLotActionService().remove(eventInfo, lotAction);
				
			
				/*//2019.08.30 dmlee : Record Lot History (Mantis 4700)
				SetEventInfo setEventInfo = new SetEventInfo();
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);*/ //modify by jhiying on 20190830 mantis:4700
			}
			catch(Exception ex)
			{
				throw new CustomException("ACTION-0001",slotName, sProcessFlowName, sProcessOperationName, sActionName); 
			}
			if(StringUtils.equals(sampleActionList.get(0).getHoldCode(), "RLRE")){
				throw new CustomException("ACTION-2020",""); 
				
			}//add by jhying on20200315 mantis:5775
		}
		
		return doc;
	}

}
