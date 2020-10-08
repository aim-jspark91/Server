package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.PMCode;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;

public class ModifyPMCode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String PMcode 		      = SMessageUtil.getBodyItemValue(doc, "PMCODE", true);
		String MaintType 		  = SMessageUtil.getBodyItemValue(doc, "MAINTTYPE", false);
		String MachineGroup       = SMessageUtil.getBodyItemValue(doc, "MACHINEGROUP", false);
		
		String MaintenanceName    = SMessageUtil.getBodyItemValue(doc, "MAINTENANCENAME", false);
		String GroupName	      = SMessageUtil.getBodyItemValue(doc, "GROUPNAME", false);
		
		String MachineName        = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		//String UnitName 	      = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);		
		//String MachineType 	      = SMessageUtil.getBodyItemValue(doc, "MACHINETYPE", false);

		//String MaintDesc          = SMessageUtil.getBodyItemValue(doc, "MAINTDESC", false);
		

		//String MaintControlDesc   = SMessageUtil.getBodyItemValue(doc, "MAINTCONTROLDESC", false);
		//String MaintPurposeDesc   = SMessageUtil.getBodyItemValue(doc, "MAINTPURPOSEDESC", false);
		
		String MaintFreQuncy      = SMessageUtil.getBodyItemValue(doc, "MAINTFREQUENCY", false);
		String FreQuencyValue     = SMessageUtil.getBodyItemValue(doc, "FREQUENCYVALUE", false);
		String CreateUser         = SMessageUtil.getBodyItemValue(doc, "CREATEUSER", false);
		
		String MaintEarlyValue    = SMessageUtil.getBodyItemValue(doc, "MAINTEARLYVALUE", false);
		String MaintLimitValue    = SMessageUtil.getBodyItemValue(doc, "MAINTLIMITVALUE", false);
		//String TriggerID          = SMessageUtil.getBodyItemValue(doc, "TRIGGERID", false);
		
		String MaintManPower       = SMessageUtil.getBodyItemValue(doc, "MAINTMANPOWER", false);
		String CreateTime          = SMessageUtil.getBodyItemValue(doc, "CREATETIME", false);
		//String CodeDesc            = SMessageUtil.getBodyItemValue(doc, "CODEDESC", false);

		String MappingFlag         = SMessageUtil.getBodyItemValue(doc, "MAPPINGFLAG", false);
		
		//String InitializeFlag      = "";
		//String PMComment           = "";
		String StartFlag           = SMessageUtil.getBodyItemValue(doc, "STARTFLAG", false);
		String FactoryName         = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		
		EventInfo eventInfo       = EventInfoUtil.makeEventInfo("ModifyPMCode", getEventUser(), getEventComment(), null, null);
			
		PMCode pmData = PMSServiceProxy.getPMCodeService().selectByKey(true, new Object[]{PMcode});
		
		//Get
		String InitializeFlag =  pmData.getInitializeFlag();
		String PMComment      = pmData.getPmComment();
	    FactoryName    = pmData.getFactoryName();
	    String UnitName       = pmData.getUnitName();
	    String MachineType    = pmData.getMachineType();
	    String MaintDesc      = pmData.getMaintDesc();
	    String MaintControlDesc = pmData.getMaintControlDesc();
	    String MaintPurposeDesc = pmData.getMaintPurposeDesc();
	    String TriggerID        = pmData.getTriggerID();
	    String CodeDesc         = pmData.getCodeDesc();
	    
		//Set
		pmData.setInitializeFlag(InitializeFlag);
		pmData.setPmComment(PMComment);
		pmData.setFactoryName(FactoryName);
		pmData.setUnitName(UnitName);
		pmData.setMachineType(MachineType);
		pmData.setMaintDesc(MaintDesc);
		pmData.setMaintControlDesc(MaintControlDesc);
		pmData.setMaintPurposeDesc(MaintPurposeDesc);
		pmData.setTriggerID(TriggerID);
		pmData.setCodeDesc(CodeDesc);
		
		if(StringUtil.isNotEmpty(MachineGroup))
			pmData.setMachineGroupName(MachineGroup);
			
		if(StringUtil.isNotEmpty(MaintenanceName))
			pmData.setMaintenanceName(MaintenanceName);
		
		if(StringUtil.isNotEmpty(GroupName))
			pmData.setGroupName(GroupName);
		
		if(StringUtil.isNotEmpty(MachineName))
			pmData.setMachineName(MachineName);
		
		//if(StringUtil.isNotEmpty(UnitName))
		//	pmData.setUnitName(UnitName);
		
		//if(StringUtil.isNotEmpty(MachineType))
		//	pmData.setMachineType(MachineType);		
		
		//if(StringUtil.isNotEmpty(MaintDesc))
		//	pmData.setMaintDesc(MaintDesc);		
		
		if(StringUtil.isNotEmpty(MaintType))
			pmData.setMaintType(MaintType);		
		
		//if(StringUtil.isNotEmpty(MaintControlDesc))
		//	pmData.setMaintControlDesc(MaintControlDesc);
		
		//if(StringUtil.isNotEmpty(MaintPurposeDesc))
		//	pmData.setMaintPurposeDesc(MaintPurposeDesc);
		
		if(StringUtil.isNotEmpty(MaintFreQuncy))
			pmData.setMaintFrequency(MaintFreQuncy);
		
		if(StringUtil.isNotEmpty(FreQuencyValue))
			pmData.setFrequencyValue(FreQuencyValue);
		
		if(StringUtil.isNotEmpty(CreateUser))
			pmData.setCreateUser(CreateUser);
		
		if(StringUtil.isNotEmpty(MaintEarlyValue))
			pmData.setMaintEarlyValue(MaintEarlyValue);
		
		if(StringUtil.isNotEmpty(MaintLimitValue))
			pmData.setMaintLimitValue(MaintLimitValue);
		
		//if(StringUtil.isNotEmpty(TriggerID))
		//	pmData.setTriggerID(TriggerID);	
		
		if(StringUtil.isNotEmpty(MaintManPower))
		{
			int iMaintManPower      = Integer.valueOf(MaintManPower);	
			pmData.setMaintManPower(iMaintManPower);
		}
					
		if(StringUtil.isNotEmpty(CreateTime))
			pmData.setCreateTime(TimeStampUtil.getTimestamp(CreateTime));
		
		//if(StringUtil.isNotEmpty(CodeDesc))
		//	pmData.setCodeDesc(CodeDesc);
		
		if(StringUtil.isNotEmpty(PMComment))
			pmData.setPmComment(PMComment);
		
		if(StringUtil.isNotEmpty(StartFlag))
			pmData.setStartFlag(StartFlag);
		
		if(StringUtil.isNotEmpty(MappingFlag))
			pmData.setMappingFlag(MappingFlag);
								
		try
		{
			pmData = PMSServiceProxy.getPMCodeService().modify(eventInfo, pmData);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0087", PMcode);
		}	

		return doc;	
	}
}
