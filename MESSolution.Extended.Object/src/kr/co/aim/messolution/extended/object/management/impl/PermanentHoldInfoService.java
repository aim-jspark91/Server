package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PermanentHoldInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PermanentHoldInfoService extends CTORMService<PermanentHoldInfo>{
	
public static Log logger = LogFactory.getLog(PermanentHoldInfo.class);
	
	private final String historyEntity = "PermanentHoldInfoHist";
	
	public List<PermanentHoldInfo> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<PermanentHoldInfo> result = super.select(condition, bindSet, PermanentHoldInfo.class);
			
			return result;
		}
		
		public PermanentHoldInfo selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(PermanentHoldInfo.class, isLock, keySet);
		}
		
		public PermanentHoldInfo create(EventInfo eventInfo, PermanentHoldInfo dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, PermanentHoldInfo dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public PermanentHoldInfo modify(EventInfo eventInfo, PermanentHoldInfo dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		

		public void createPermanentHoldInfo(Lot lotData, String productSpecName, String productSpecVersion, String ecCode, String processFlowName, 
				String processFlowVersion, String processOperationName, String processOperationVersion, String description, String reasonCode, String department, EventInfo eventInfo)
						throws CustomException
		{		

			List<PermanentHoldInfo> permanentHoldList = new ArrayList<PermanentHoldInfo>();
			long lastPosition = 0;

			String condition = "WHERE lotName = ? AND factoryName = ? AND (productSpecName = ? or productSpecName = ?) AND (productSpecVersion = ? or productSpecVersion = ?) "
					+ "AND (ecCode = ? or ecCode = ?) AND processFlowName = ? AND (processFlowVersion = ? OR processFlowVersion = ?) "
					+ "AND processOperationName = ? AND (processOperationVersion = ? OR processOperationVersion = ?) order by position desc ";

			Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), productSpecName, "*", productSpecVersion, "*",
										ecCode, "*", processFlowName, processFlowVersion, "*", processOperationName, processOperationVersion, "*"};

			try
			{
				permanentHoldList = ExtendedObjectProxy.getPermanentHoldInfoService().select(condition, bindSet);
			}
			catch(Throwable e)
			{
				//not exist permanentHoldInfo..
			}

			if(permanentHoldList.size()>0)
			{
				PermanentHoldInfo lastPositionPermanentHoldInfo = permanentHoldList.get(0);
				lastPosition = lastPositionPermanentHoldInfo.getPosition();
			}

			PermanentHoldInfo createPermanentHold = new PermanentHoldInfo();
			createPermanentHold.setLotname(lotData.getKey().getLotName());
			createPermanentHold.setFactoryName(lotData.getFactoryName());
			createPermanentHold.setEcCode(ecCode);
			createPermanentHold.setProductSpecName(productSpecName);
			createPermanentHold.setProductSpecVersion(productSpecVersion);
			createPermanentHold.setProcessflowName(processFlowName);
			createPermanentHold.setProcessflowVersion(processFlowVersion);
			createPermanentHold.setProcessOperationName(processOperationName);
			createPermanentHold.setProcessOperationVersion(processOperationVersion);
			createPermanentHold.setPosition(lastPosition+1);
			createPermanentHold.setActionState("Created");
			createPermanentHold.setDescription(description);
			createPermanentHold.setReasonCode(reasonCode);
			createPermanentHold.setDepartmentName(department);
			createPermanentHold.setLastEventUser(eventInfo.getEventUser());
			createPermanentHold.setLastEventName(eventInfo.getEventName());;
			createPermanentHold.setLastEventTime(eventInfo.getEventTime());
			createPermanentHold.setLastEventTimeKey(eventInfo.getEventTimeKey());
			createPermanentHold.setLastEventComment(eventInfo.getEventComment());

			ExtendedObjectProxy.getPermanentHoldInfoService().create(eventInfo, createPermanentHold);
		}
}
