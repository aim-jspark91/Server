package kr.co.aim.messolution.pms.event;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.Maintenance;
import kr.co.aim.messolution.pms.management.data.MaintenanceCheck;
import kr.co.aim.messolution.pms.management.data.PMCode;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreatePM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		//String MaintenanceID      = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEID", true);
		
		String RecreateFlag      = SMessageUtil.getBodyItemValue(doc, "RECREATEFLAG", true); //After EvaluationPM and CancelPM recreatePM with same PMCOde		
		String PMcode 		     = SMessageUtil.getBodyItemValue(doc, "PMCODE", true);
		String GroupName         = SMessageUtil.getBodyItemValue(doc, "GROUPNAME", true);
		String MaintenanceName   = SMessageUtil.getBodyItemValue(doc, "MAINTENANCENAME", true);
		String MachineName       = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		//String UnitName 	     = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		//String MachineType       = SMessageUtil.getBodyItemValue(doc, "MACHINETYPE", false);
		String MaintType         = SMessageUtil.getBodyItemValue(doc, "MAINTTYPE", false);
		//String MaintDesc         = SMessageUtil.getBodyItemValue(doc, "MAINTDESC", false);
		//String MaintControlDesc  = SMessageUtil.getBodyItemValue(doc, "MAINTCONTROLDESC", false);
		//String MaintPurposelDesc = SMessageUtil.getBodyItemValue(doc, "MAINTPURPOSEDESC", false);
		String MaintPlanDate     = SMessageUtil.getBodyItemValue(doc, "MAINTPLANTIME", true);
		String MaintStatus       = SMessageUtil.getBodyItemValue(doc, "MAINTSTATUS", true);
		String MaintEarlyDate    = SMessageUtil.getBodyItemValue(doc, "MAINTEARLYDATE", true);
		String MaintLimitDate    = SMessageUtil.getBodyItemValue(doc, "MAINTLIMITDATE", true);
		String StartFlag         = SMessageUtil.getBodyItemValue(doc, "STARTFLAG", true); //for PMCode 
		
		List<Element> CheckItemList = SMessageUtil.getBodySequenceItemList(doc, "CHECKITEMLIST", true);
		
		EventInfo eventInfo      = EventInfoUtil.makeEventInfo("CreatePM", getEventUser(), getEventComment(), null, null);
		
		String MaintenanceID = this.CreateMaintenanceID();
		
		if(RecreateFlag.equals("Y"))
		{
			//Maintenance test
			Maintenance maintenanceData = new Maintenance(MaintenanceID);	
			maintenanceData.setPmCode(PMcode);
			maintenanceData.setGroupName(GroupName);
			maintenanceData.setMaintName(MaintenanceName);
			maintenanceData.setMachineName(MachineName);
			maintenanceData.setUnitName("");
			maintenanceData.setMachineType("");
			maintenanceData.setMaintType(MaintType);
			maintenanceData.setMaintDesc("");
			maintenanceData.setMaintControlDesc("");
			maintenanceData.setMaintPurposeDesc("");
			maintenanceData.setMaintPlanDate(TimeStampUtil.getTimestamp(MaintPlanDate));
			maintenanceData.setMaintStatus(MaintStatus);
			maintenanceData.setMaintEarlyDate(TimeStampUtil.getTimestamp(MaintEarlyDate));
			maintenanceData.setMaintLimitDate(TimeStampUtil.getTimestamp(MaintLimitDate));
			
			try
			{
				maintenanceData = PMSServiceProxy.getMaintenanceService().create(eventInfo, maintenanceData);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0085", MaintenanceID,PMcode); 
			}
			
            //CompleteCheckPM part			
			for(Element checkItemList : CheckItemList)
			{
				String CheckNo	  = SMessageUtil.getChildText(checkItemList, "CHECKNO", true);
				String CheckID	  = SMessageUtil.getChildText(checkItemList, "CHECKID", true);
				String ItemType	  = SMessageUtil.getChildText(checkItemList, "ITEMTYPE", true);
				String ItemName	  = SMessageUtil.getChildText(checkItemList, "ITEMNAME", true);
				//String CheckDesc	  = SMessageUtil.getChildText(checkItemList, "CHECKDESC", true);
				
				String PartID	  = SMessageUtil.getChildText(checkItemList, "PARTID", false);
				String PartName	  = SMessageUtil.getChildText(checkItemList, "PARTNAME", false);
				String UseQty	  = SMessageUtil.getChildText(checkItemList, "USEQUANTITY", false);
				
				MaintenanceCheck CPMData = new MaintenanceCheck(MaintenanceID,CheckID,CheckNo);
				CPMData.setItemType(ItemType);
				CPMData.setItemName(ItemName);
				CPMData.setCheckDesc("");
				CPMData.setPartID(PartID);
				CPMData.setPartName(PartName);
				CPMData.setUseQuantity(UseQty);
				CPMData.setCheckResult("");
				
				try
				{
					CPMData = PMSServiceProxy.getCompleteCheckPMService().create(eventInfo, CPMData);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0082", PMcode); 
				}			
			}	
			
			return doc;
		}
		else
		{
			//Maintenance
			Maintenance maintenanceData = new Maintenance(MaintenanceID);	
			maintenanceData.setPmCode(PMcode);
			maintenanceData.setGroupName(GroupName);
			maintenanceData.setMaintName(MaintenanceName);
			maintenanceData.setMachineName(MachineName);
			maintenanceData.setUnitName("");
			maintenanceData.setMachineType("");
			maintenanceData.setMaintType(MaintType);
			maintenanceData.setMaintDesc("");
			maintenanceData.setMaintControlDesc("");
			maintenanceData.setMaintPurposeDesc("");
			maintenanceData.setMaintPlanDate(TimeStampUtil.getTimestamp(MaintPlanDate));
			maintenanceData.setMaintStatus(MaintStatus);
			maintenanceData.setMaintEarlyDate(TimeStampUtil.getTimestamp(MaintEarlyDate));
			maintenanceData.setMaintLimitDate(TimeStampUtil.getTimestamp(MaintLimitDate));
			
			try
			{
				maintenanceData = PMSServiceProxy.getMaintenanceService().create(eventInfo, maintenanceData);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0085", MaintenanceID,PMcode); 
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
			String mappingFlag = pmCodeData.getMappingFlag();
			String initializeFlag = pmCodeData.getInitializeFlag();
			//String startFlag   = pmCodeData.getStartFlag();
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
			pmCodeData.setStartFlag(StartFlag);
			pmCodeData.setFactoryName(factoryName);
			pmCodeData.setMappingFlag(mappingFlag);
			pmCodeData.setInitializeFlag(initializeFlag);
			
			try
			{
				pmCodeData = PMSServiceProxy.getPMCodeService().modify(eventInfo, pmCodeData);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0082", PMcode); 
			}
			
			//CompleteCheckPM part
			
			for(Element checkItemList : CheckItemList)
			{
				String CheckNo	  = SMessageUtil.getChildText(checkItemList, "CHECKNO", true);
				String CheckID	  = SMessageUtil.getChildText(checkItemList, "CHECKID", true);
				String ItemType	  = SMessageUtil.getChildText(checkItemList, "ITEMTYPE", true);
				String ItemName	  = SMessageUtil.getChildText(checkItemList, "ITEMNAME", true);
				//String CheckDesc	  = SMessageUtil.getChildText(checkItemList, "CHECKDESC", true);
				
				String PartID	  = SMessageUtil.getChildText(checkItemList, "PARTID", false);
				String PartName	  = SMessageUtil.getChildText(checkItemList, "PARTNAME", false);
				String UseQty	  = SMessageUtil.getChildText(checkItemList, "USEQUANTITY", false);
				
				MaintenanceCheck CPMData = new MaintenanceCheck(MaintenanceID,CheckID,CheckNo);
				CPMData.setItemType(ItemType);
				CPMData.setItemName(ItemName);
				CPMData.setCheckDesc("");
				CPMData.setPartID(PartID);
				CPMData.setPartName(PartName);
				CPMData.setUseQuantity(UseQty);
				CPMData.setCheckResult("");
				
				try
				{
					CPMData = PMSServiceProxy.getCompleteCheckPMService().create(eventInfo, CPMData);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0082", PMcode); 
				}			
			}			
			
			//return 
			Document rtnDoc = new Document();
			rtnDoc = (Document)doc.clone();
			rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "MAINTENANCEID", MaintenanceID);
			return rtnDoc;
		}
		
	}
	
	public String CreateMaintenanceID()
			throws CustomException
	{
		String NewMaintenanceID = "";
		   
		String currentDate = TimeUtils.getCurrentEventTimeKey();
		String Date = currentDate.substring(0, 8);
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("MAINT", "Maint");
		nameRuleAttrMap.put("DATE", Date);
		nameRuleAttrMap.put("HYPHEN", "-");

		//MaintenanceID Generate	 		
		try
		{
			int createQty = 1;
			List<String> lstName = CommonUtil.generateNameByNamingRule("MaintenanceNaming", nameRuleAttrMap, createQty);
			NewMaintenanceID = lstName.get(0);
		}
		catch(Exception ex)
		{
			new CustomException("PMCODE-9011", ex.getMessage());
		}
			
		return NewMaintenanceID;
	}
}

