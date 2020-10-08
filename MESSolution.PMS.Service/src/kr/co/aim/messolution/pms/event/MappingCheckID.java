package kr.co.aim.messolution.pms.event;

import java.sql.Timestamp;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.MappingItem;
import kr.co.aim.messolution.pms.management.data.PMCode;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class MappingCheckID extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String ModifyResult = SMessageUtil.getBodyItemValue(doc, "MODIFYRESULT", true);
		
		String PMcode       = SMessageUtil.getBodyItemValue(doc, "PMCODE", true);
		String EventUser    = SMessageUtil.getBodyItemValue(doc, "EVENTUSER", true);
		String EventTime    = SMessageUtil.getBodyItemValue(doc, "EVENTTIME", true);
		
		List<Element> CheckIDList = SMessageUtil.getBodySequenceItemList(doc, "CHECKIDLIST", true);
		EventInfo eventInfo = null;
		
		if(ModifyResult.equals("N"))
		{
            eventInfo = EventInfoUtil.makeEventInfo("MappingCheckID", getEventUser(), getEventComment(), null, null);
			
			for(Element checkIDList : CheckIDList)
			{
				String checkID    = SMessageUtil.getChildText(checkIDList, "CHECKID", true);
				
				MappingItem mappingItemData = new MappingItem(PMcode,checkID);
				mappingItemData.setEventUser(EventUser);
				mappingItemData.setEventTime(TimeStampUtil.getTimestamp(EventTime));
						
				try
				{
					mappingItemData = PMSServiceProxy.getMappingItemService().create(eventInfo, mappingItemData);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0083", PMcode,checkID);
				}
			}	
		}
		else
		{
			eventInfo = EventInfoUtil.makeEventInfo("ModifyCheckID", getEventUser(), getEventComment(), null, null);
			
			//Get MappingItemList and delete old mapping data
			List<MappingItem> MappingItemList = null;
			try
			{
				MappingItemList = PMSServiceProxy.getMappingItemService().select("PMCODE = ? ", new Object[] {PMcode});			
			}
			catch (Exception ex)
			{
				eventLog.error(String.format("<<<<<<<<<<<<<<< No Data this PMCODE = %s", PMcode));
			}
			
			if(MappingItemList != null)
			{
				for(MappingItem mappingItemList : MappingItemList)
				{
					String checkID    = mappingItemList.getCheckID();
					
					MappingItem mappingItemData = new MappingItem(PMcode,checkID);
					
					try
					{						
					    PMSServiceProxy.getMappingItemService().delete(mappingItemData);
					}
					catch(Exception ex)
					{
						throw new CustomException("PMS-0083", PMcode,checkID);
					}			
				}			
			}
			
			for(Element checkIDList : CheckIDList)
			{
				String checkID    = SMessageUtil.getChildText(checkIDList, "CHECKID", true);
				
				MappingItem mappingItemData = new MappingItem(PMcode,checkID);
				mappingItemData.setEventUser(EventUser);
				mappingItemData.setEventTime(TimeStampUtil.getTimestamp(EventTime));
						
				try
				{
					mappingItemData = PMSServiceProxy.getMappingItemService().create(eventInfo, mappingItemData);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0083", PMcode,checkID);
				}
			}	
		}	
		
		//PMCode Part			
		PMCode pmCodeData = null;
		pmCodeData = PMSServiceProxy.getPMCodeService().selectByKey(true, new Object[] {PMcode});
		
		//Get
		String machineGroup = pmCodeData.getMachineGroupName();
		String maintenanceName = pmCodeData.getMaintenanceName();
		String groupName      = pmCodeData.getGroupName();
		String machineName   = pmCodeData.getMachineName();
		String unitName   = pmCodeData.getUnitName();
		String machineType = pmCodeData.getMachineType();
		String maintDesc   = pmCodeData.getMaintDesc();
		String maintType   = pmCodeData.getMaintType();
		String maintControlDesc = pmCodeData.getMaintControlDesc();
		String maintPurposeDesc  = pmCodeData.getMaintPurposeDesc();
		String maintFrequency    =pmCodeData.getMaintFrequency();
		String freQuencyValue    =pmCodeData.getFrequencyValue();
		String createUser        =pmCodeData.getCreateUser();
		String maintEarlyValue   = pmCodeData.getMaintEarlyValue();
		String maintLimitValue  = pmCodeData.getMaintLimitValue();
		String triggerID  =pmCodeData.getTriggerID();
		Number maintManPower = pmCodeData.getMaintManPower();
		Timestamp createTime  = pmCodeData.getCreateTime();
		String codeDesc    = pmCodeData.getCodeDesc();
		//String mappingFlag = pmCodeData.getMappingFlag();
		String initializeFlag = pmCodeData.getInitializeFlag();
		String startFlag   = pmCodeData.getStartFlag();
		String factoryName  = pmCodeData.getFactoryName();
				
		//Set
		pmCodeData.setMachineGroupName(machineGroup);
		pmCodeData.setMaintenanceName(maintenanceName);
		pmCodeData.setGroupName(groupName);
		pmCodeData.setMachineName(machineName);
		pmCodeData.setUnitName(unitName);
		pmCodeData.setMachineType(machineType);		
		pmCodeData.setMaintDesc(maintDesc);		
		pmCodeData.setMaintType(maintType);		
		pmCodeData.setMaintControlDesc(maintControlDesc);
		pmCodeData.setMaintPurposeDesc(maintPurposeDesc);
		pmCodeData.setMaintFrequency(maintFrequency);
		pmCodeData.setFrequencyValue(freQuencyValue);
		pmCodeData.setCreateUser(createUser);
		pmCodeData.setMaintEarlyValue(maintEarlyValue);
		pmCodeData.setMaintLimitValue(maintLimitValue);
		pmCodeData.setTriggerID(triggerID);	
		pmCodeData.setMaintManPower(maintManPower);
		pmCodeData.setCreateTime(createTime);
		pmCodeData.setCodeDesc(codeDesc);
		pmCodeData.setStartFlag(startFlag);
		pmCodeData.setFactoryName(factoryName);
		pmCodeData.setMappingFlag("Y");
		pmCodeData.setInitializeFlag(initializeFlag);
		
		try
		{
			pmCodeData = PMSServiceProxy.getPMCodeService().modify(eventInfo, pmCodeData);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0082", PMcode); 
		}		
		return doc;
	}
}
