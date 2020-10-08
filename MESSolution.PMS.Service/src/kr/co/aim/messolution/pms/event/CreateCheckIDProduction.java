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
import kr.co.aim.messolution.pms.management.data.CheckID;
import kr.co.aim.messolution.pms.management.data.CheckList;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CreateCheckIDProduction extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String ModifyResult = SMessageUtil.getBodyItemValue(doc, "MODIFYRESULT", true);
		
		String FactoryName  = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String MachineGroup = SMessageUtil.getBodyItemValue(doc, "MACHINEGROUP", true);
		
		String CreateUser   = SMessageUtil.getBodyItemValue(doc, "CREATEUSER", true);
		String CreateTime   = SMessageUtil.getBodyItemValue(doc, "CREATETIME", true);
		String DepartMent   = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		
		List<Element> CheckList = SMessageUtil.getBodySequenceItemList(doc, "CHECKLIST", true);
		
		EventInfo eventInfo = null;
		
		if(ModifyResult.equals("N"))//N
		{
			eventInfo = EventInfoUtil.makeEventInfo("CreateCheckID", getEventUser(), getEventComment(), null, null);
			String CheckID = this.CreateCheckID(FactoryName, MachineGroup);
			
			//CheckID Part		
			CheckID checkIDData = new CheckID(CheckID);
			checkIDData.setMachineGroupName(MachineGroup);
			checkIDData.setCreateUser(CreateUser);
			checkIDData.setCreateTime(TimeStampUtil.getTimestamp(CreateTime));
			checkIDData.setDepartMent(DepartMent);
			try
			{
				checkIDData = PMSServiceProxy.getCheckIDService().create(eventInfo, checkIDData);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS_0086", CheckID);
			}
			
			//CheckList Part
			for(Element checkList : CheckList)
			{
				String No              = SMessageUtil.getChildText(checkList, "NO", true);
				String CheckItemType   = SMessageUtil.getChildText(checkList, "CHECKITEMTYPE", true);
				String CheckItemName   = SMessageUtil.getChildText(checkList, "CHECKITEMNAME", true);
				//String CheckDesc       = SMessageUtil.getChildText(checkList, "CHECKDESCRIPTION", true);
				String PartName        = SMessageUtil.getChildText(checkList, "PARTNAME", false);
				String PartID          = SMessageUtil.getChildText(checkList, "PARTID", false);
				String UseQuantity     = SMessageUtil.getChildText(checkList, "USEQUANTITY", false);
				
				CheckList checkListData  = new CheckList(No,CheckID);
				checkListData.setItemType(CheckItemType);
				checkListData.setItemName(CheckItemName);
				//checkListData.setCheckDesc(CheckDesc);
				checkListData.setPartName(PartName);
				checkListData.setPartID(PartID);
				checkListData.setUseQuantity(UseQuantity);
				
				try
				{
					checkListData = PMSServiceProxy.getCheckListService().create(eventInfo, checkListData);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0084",CheckID);
				}
			}
						
			//return 
			Document rtnDoc = new Document();
			rtnDoc = (Document)doc.clone();
			rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "CHECKID", CheckID);
			return rtnDoc;		
		}
		else//Y
		{
			eventInfo = EventInfoUtil.makeEventInfo("ModifyCheckID", getEventUser(), getEventComment(), null, null);
			String CheckID    = SMessageUtil.getBodyItemValue(doc, "CHECKID", true);
			
			//Delete CheckID
			CheckID checkIDData = new CheckID(CheckID);		
			
			try
			{
				 PMSServiceProxy.getCheckIDService().delete(checkIDData);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS_0086", CheckID);
			}
			
			//Delete CheckList
			//Get 
			List<CheckList> checkListData = null;
			try
			{		
                checkListData = PMSServiceProxy.getCheckListService().select("CHECKID = ?", new Object[] {CheckID});
			}
			catch (Exception ex)
			{
				eventLog.error(String.format("<<<<<<<<<<<<<<< No Data this CheckID = %s", CheckID));
			}
			
			if(checkListData != null)
			{
				for(CheckList checkList : checkListData)
				{
					String NO = checkList.getNo().toString();
					
					CheckList checkListDataInfo  = new CheckList(NO,CheckID);
					PMSServiceProxy.getCheckListService().delete(checkListDataInfo);
				}
			}
			
			//Create CheckID
			CheckID CheckIDData = new CheckID(CheckID);
			CheckIDData.setMachineGroupName(MachineGroup);
			CheckIDData.setCreateUser(CreateUser);
			CheckIDData.setCreateTime(TimeStampUtil.getTimestamp(CreateTime));
			CheckIDData.setDepartMent(DepartMent);
			
			try
			{
				CheckIDData = PMSServiceProxy.getCheckIDService().create(eventInfo, CheckIDData);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS_0086", CheckID);
			}
			
			//Create CheckList
			for(Element checkList : CheckList)
			{
				String No              = SMessageUtil.getChildText(checkList, "NO", true);
				String CheckItemType   = SMessageUtil.getChildText(checkList, "CHECKITEMTYPE", true);
				String CheckItemName   = SMessageUtil.getChildText(checkList, "CHECKITEMNAME", true);
				//String CheckDesc       = SMessageUtil.getChildText(checkList, "CHECKDESCRIPTION", true);
				String PartName        = SMessageUtil.getChildText(checkList, "PARTNAME", false);
				String PartID          = SMessageUtil.getChildText(checkList, "PARTID", false);
				String UseQuantity     = SMessageUtil.getChildText(checkList, "USEQUANTITY", false);
				
				CheckList CheckListData  = new CheckList(No,CheckID);
				CheckListData.setItemType(CheckItemType);
				CheckListData.setItemName(CheckItemName);
				//CheckListData.setCheckDesc(CheckDesc);
				CheckListData.setPartName(PartName);
				CheckListData.setPartID(PartID);
				CheckListData.setUseQuantity(UseQuantity);
				
				try
				{
					CheckListData = PMSServiceProxy.getCheckListService().create(eventInfo, CheckListData);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0084",CheckID);
				}
			}
					
			return doc;
		}		
	}
	
	public String CreateCheckID(String Factory, String MachineGroup)  
			throws CustomException
	{
		String NewCheckID = "";
		   
		String CHECK = "CK";
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
		}
		   
		   
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("CHECK", CHECK);
		nameRuleAttrMap.put("FABCODE", FABCODE);
		nameRuleAttrMap.put("FACTORYCODE", FACTORYCODE);
		nameRuleAttrMap.put("MACHINEGROUP", MACHINEGROUP);
		   
		//PMCODE Generate	 		
		try
		{
			int createQty = 1;
			List<String> lstName = CommonUtil.generateNameByNamingRule("CheckIDNaming", nameRuleAttrMap, createQty);
			NewCheckID = lstName.get(0);
		}
		catch(Exception ex)
		{
			new CustomException("PMCODE-9011", ex.getMessage());
		}
			
		return NewCheckID;
	}
}