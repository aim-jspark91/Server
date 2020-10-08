package kr.co.aim.messolution.pms.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.PMCode;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CreatePMCode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		//String PMcode 		      = SMessageUtil.getBodyItemValue(doc, "PMCODE", true);
		String MaintType 		  = SMessageUtil.getBodyItemValue(doc, "MAINTTYPE", false);
		String MachineGroup       = SMessageUtil.getBodyItemValue(doc, "MACHINEGROUP", true);
		
		String MaintenanceName    = SMessageUtil.getBodyItemValue(doc, "MAINTENANCENAME", true);
		String GroupName	      = SMessageUtil.getBodyItemValue(doc, "GROUPNAME", true);
		
		String MachineName        = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String UnitName 	      = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);		
		String MachineType 	      = SMessageUtil.getBodyItemValue(doc, "MACHINETYPE", false);

		String MaintDesc          = SMessageUtil.getBodyItemValue(doc, "MAINTDESC", false);
		

		String MaintControlDesc   = SMessageUtil.getBodyItemValue(doc, "MAINTCONTROLDESC", false);
		String MaintPurposeDesc   = SMessageUtil.getBodyItemValue(doc, "MAINTPURPOSEDESC", false);
		
		String MaintFreQuncy      = SMessageUtil.getBodyItemValue(doc, "MAINTFREQUENCY", true);
		String FreQuencyValue     = SMessageUtil.getBodyItemValue(doc, "FREQUENCYVALUE", false);
		String CreateUser         = SMessageUtil.getBodyItemValue(doc, "CREATEUSER", false);
		
		String MaintEarlyValue    = SMessageUtil.getBodyItemValue(doc, "MAINTEARLYVALUE", true);
		String MaintLimitValue    = SMessageUtil.getBodyItemValue(doc, "MAINTLIMITVALUE", true);
		String TriggerID          = SMessageUtil.getBodyItemValue(doc, "TRIGGERID", false);
		
		String MaintManPower       = SMessageUtil.getBodyItemValue(doc, "MAINTMANPOWER", true);
		String CreateTime          = SMessageUtil.getBodyItemValue(doc, "CREATETIME", true);
		String CodeDesc            = SMessageUtil.getBodyItemValue(doc, "CODEDESC", false);

		String MappingFlag       = SMessageUtil.getBodyItemValue(doc, "MAPPINGFLAG", true);
		
		List<Element> machineShopList = SMessageUtil.getBodySequenceItemList(doc, "MACHINESHOPLIST", false);
		
		String InitializeFlag      = "";
		String PMComment           = "";
		
		String StartFlag           = SMessageUtil.getBodyItemValue(doc, "STARTFLAG", true);
		String FactoryName         = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		
		EventInfo eventInfo       = EventInfoUtil.makeEventInfo("CreatePMCode", getEventUser(), getEventComment(), null, null);	
		
		//String PMCode = this.CreatePMCode(FactoryName, MachineGroup);			
		
		if(MachineName.equals("Multy"))
		{  	 
			Document rtnDoc = new Document();
			String PMCode1="";
			try
			{  
				if(machineShopList != null)
				{
					for(Element eleshop : machineShopList)
					{ 	
					 PMCode1 = this.CreatePMCode(FactoryName, MachineGroup);	
					PMCode pmData = new PMCode(PMCode1);
					pmData.setMachineGroupName(MachineGroup);
					pmData.setMaintenanceName(MaintenanceName);
					pmData.setGroupName(GroupName);			
					pmData.setUnitName(UnitName);
					pmData.setMachineType(MachineType);		
					pmData.setMaintDesc(MaintDesc);		
					pmData.setMaintType(MaintType);		
					pmData.setMaintControlDesc(MaintControlDesc);
					pmData.setMaintPurposeDesc(MaintPurposeDesc);
					pmData.setMaintFrequency(MaintFreQuncy);
					pmData.setFrequencyValue(FreQuencyValue);
					pmData.setCreateUser(CreateUser);
					pmData.setMaintEarlyValue(MaintEarlyValue);
					pmData.setMaintLimitValue(MaintLimitValue);
					pmData.setTriggerID(TriggerID);	
					int iMaintManPower      = Integer.valueOf(MaintManPower);	
					pmData.setMaintManPower(iMaintManPower);
					pmData.setCreateTime(TimeStampUtil.getTimestamp(CreateTime));
					pmData.setCodeDesc(CodeDesc);
					pmData.setPmComment(PMComment);
					pmData.setStartFlag(StartFlag);
					pmData.setFactoryName(FactoryName);
					pmData.setMappingFlag(MappingFlag);
					pmData.setInitializeFlag(InitializeFlag);
						
						String MachineShopName = eleshop.getChildText("MACHINESHOPNAME");
						pmData.setMachineName(MachineShopName);						 
						pmData = PMSServiceProxy.getPMCodeService().create(eventInfo, pmData);
						
						
						rtnDoc = (Document)doc.clone();
						rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "PMCODE", PMCode1);
					}
				}
				//pmData = PMSServiceProxy.getPMCodeService().create(eventInfo, pmData);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0082" ,PMCode1);
			}
		//return 
//		Document rtnDoc = new Document();
//		rtnDoc = (Document)doc.clone();
//		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "PMCODE", PMCode1);
		return rtnDoc;	
		}		
		
		else
		{
			String PMCode = this.CreatePMCode(FactoryName, MachineGroup);
			PMCode pmData = new PMCode(PMCode);			
			pmData.setMachineGroupName(MachineGroup);
			pmData.setMaintenanceName(MaintenanceName);
			pmData.setGroupName(GroupName);
			pmData.setMachineName(MachineName);
			pmData.setUnitName(UnitName);
			pmData.setMachineType(MachineType);		
			pmData.setMaintDesc(MaintDesc);		
			pmData.setMaintType(MaintType);		
			pmData.setMaintControlDesc(MaintControlDesc);
			pmData.setMaintPurposeDesc(MaintPurposeDesc);
			pmData.setMaintFrequency(MaintFreQuncy);
			pmData.setFrequencyValue(FreQuencyValue);
			pmData.setCreateUser(CreateUser);
			pmData.setMaintEarlyValue(MaintEarlyValue);
			pmData.setMaintLimitValue(MaintLimitValue);
			pmData.setTriggerID(TriggerID);	
			int iMaintManPower      = Integer.valueOf(MaintManPower);	
			pmData.setMaintManPower(iMaintManPower);
			pmData.setCreateTime(TimeStampUtil.getTimestamp(CreateTime));
			pmData.setCodeDesc(CodeDesc);
			pmData.setPmComment(PMComment);
			pmData.setStartFlag(StartFlag);
			pmData.setFactoryName(FactoryName);
			pmData.setMappingFlag(MappingFlag);
			pmData.setInitializeFlag(InitializeFlag);
						
			try
			{
				pmData = PMSServiceProxy.getPMCodeService().create(eventInfo, pmData);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0082", PMCode);
			}
		//return 
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "PMCODE", PMCode);
		return rtnDoc;				
	}
		}
	
   public String CreatePMCode(String Factory, String MachineGroup)  throws CustomException
   {
	   String NewPMCode = "";
	   
	   String PM = "PM";
	   String FABCODE = "";
	   String FACTORYCODE ="";
	   String MACHINEGROUP = MachineGroup.substring(3,6);
	   
	   if( Factory.equals("ARRAY") || Factory.equals("CF") || Factory.equals("CELL"))
	   {
		   FABCODE = "T";
		   
		   if(Factory.equals("ARRAY"))
			   FACTORYCODE = "A";
		   else if(Factory.equals("CF"))
			   FACTORYCODE = "F";
		   else if(Factory.equals("CELL"))
			   FACTORYCODE = "C";
	   }
	   else
	   {
		   FABCODE = "A";
		   
		   if(Factory.equals("LTPS"))
			   FACTORYCODE = "L";
		   else if(Factory.equals("OLED"))
			   FACTORYCODE = "O";
		   else if (Factory.equals("MOD")){
			   FACTORYCODE = "M";
		 }
	   }
	   
	   
	   Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
	   nameRuleAttrMap.put("PM", PM);
	   nameRuleAttrMap.put("FABCODE", FABCODE);
	   nameRuleAttrMap.put("FACTORYCODE", FACTORYCODE);
	   nameRuleAttrMap.put("MACHINEGROUP", MACHINEGROUP);
	   
	   //PMCODE Generate	 		
	   try
		{
			int createQty = 1;
			List<String> lstName = CommonUtil.generateNameByNamingRule("PMCodeNaming", nameRuleAttrMap, createQty);
			NewPMCode = lstName.get(0);
		}
		catch(Exception ex)
		{
			new CustomException("PMCODE-9011", ex.getMessage());
		}
		
		return NewPMCode;
   }
	
}
