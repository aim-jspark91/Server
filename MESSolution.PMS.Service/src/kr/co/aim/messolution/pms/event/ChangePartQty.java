package kr.co.aim.messolution.pms.event;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BulletinBoard;
import kr.co.aim.messolution.pms.management.data.BulletinBoardArea;
import kr.co.aim.messolution.pms.management.data.SparePart;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.GenSqlLobValue;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.springframework.jdbc.support.lob.LobHandler;

public class ChangePartQty extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String PartID              = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
		String PartName            = SMessageUtil.getBodyItemValue(doc, "PARTNAME", false);
		String PartSpec            = SMessageUtil.getBodyItemValue(doc, "PARTSPEC", false);
		String Quantity            = SMessageUtil.getBodyItemValue(doc, "QUANTITY", false);
		String SafeQuantity        = SMessageUtil.getBodyItemValue(doc, "SAFEQUANTITY", false);
		String WarningQty          = SMessageUtil.getBodyItemValue(doc, "WARNINGQUANTITY", false);
		String Unit                = SMessageUtil.getBodyItemValue(doc, "UNIT", false);
		String PartType            = SMessageUtil.getBodyItemValue(doc, "PARTTYPE", false);
		String MaterialCode        = SMessageUtil.getBodyItemValue(doc, "MATERIALCODE", false);
		String PartAttribute       = SMessageUtil.getBodyItemValue(doc, "PARTATTRIBUTE", false);
		String PartGroup           = SMessageUtil.getBodyItemValue(doc, "PARTGROUP", false);
		String VendorID            = SMessageUtil.getBodyItemValue(doc, "VENDORNAME", false);		
		String Location            = SMessageUtil.getBodyItemValue(doc, "LOCATION", false);
		String PurchaseCycle       = SMessageUtil.getBodyItemValue(doc, "PURCHASECYCLE", false);		
		String GroupName           = SMessageUtil.getBodyItemValue(doc, "GROUPNAME", false);
		String PartDesc            = SMessageUtil.getBodyItemValue(doc, "PARTDESCRIPTION", false);
		String UseDesc             = SMessageUtil.getBodyItemValue(doc, "USEDESCRIPTION", false);
		//String PartImage           = SMessageUtil.getBodyItemValue(doc, "IMAGE", false);
		
		int UnitPrice              = 0;//useless
				
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("ModifySparePart", getEventUser(), getEventComment(), null, null);
	
		//Modify SparePart Info 
		SparePart sparePartDataInfo = null;
				
		//get
		sparePartDataInfo = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {PartID});
		
		Number NotInQuantity       = sparePartDataInfo.getNotInQuantity();
		Number PurchaseCompleteQty = sparePartDataInfo.getPurchaseCompleteQty();
		int UseFrequency            = 0;
		
		//set
		sparePartDataInfo = new SparePart(PartID);
		sparePartDataInfo.setPartID(PartID);
		
		if(StringUtil.isNotEmpty(PartGroup))
		    sparePartDataInfo.setPartGroup(PartGroup);
		
		if(StringUtil.isNotEmpty(PartName))
		    sparePartDataInfo.setPartName(PartName);
		
		if(StringUtil.isNotEmpty(PartSpec))
		    sparePartDataInfo.setPartSpec(PartSpec);
		
		if(StringUtil.isNotEmpty(Location))
		sparePartDataInfo.setLocation(Location);
		
		if(StringUtil.isNotEmpty(Quantity))
		    sparePartDataInfo.setQuantity(Integer.parseInt(Quantity));
		
		if(StringUtil.isNotEmpty(WarningQty))
		    sparePartDataInfo.setWarningQuantity(Integer.parseInt(WarningQty));
		
		if(StringUtil.isNotEmpty(SafeQuantity))
		    sparePartDataInfo.setSafeQuantity(Integer.parseInt(SafeQuantity));
			
		if(StringUtil.isNotEmpty(Unit))
		    sparePartDataInfo.setUnit(Unit);
			
		if(StringUtil.isNotEmpty(PartType))
		    sparePartDataInfo.setPartType(PartType);
		
		if(StringUtil.isNotEmpty(MaterialCode))
		    sparePartDataInfo.setMaterialCode(MaterialCode);
		
		if(StringUtil.isNotEmpty(PurchaseCycle))
		    sparePartDataInfo.setPurchaseCycle(PurchaseCycle);	
		
		if(StringUtil.isNotEmpty(PartDesc))
		    sparePartDataInfo.setPartDescription(PartDesc);
		
		if(StringUtil.isNotEmpty(UseDesc))
		    sparePartDataInfo.setUseDescription(UseDesc);
				
		if(StringUtil.isNotEmpty(PartAttribute))
		    sparePartDataInfo.setPartAttribute(PartAttribute);	
		
		if(StringUtil.isNotEmpty(VendorID))
		    sparePartDataInfo.setVendorID(VendorID);
		
		if(StringUtil.isNotEmpty(GroupName))
		    sparePartDataInfo.setGroupName(GroupName);
		
		sparePartDataInfo.setNotInQuantity(NotInQuantity);
		sparePartDataInfo.setPurchaseCompleteQty(PurchaseCompleteQty);	
		sparePartDataInfo.setUnitPrice(UnitPrice);	
		sparePartDataInfo.setUseFrequency(UseFrequency);
				
		try
		{
			sparePartDataInfo = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePartDataInfo);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0057", PartID);
		}
		
		InsertBoard(PartID,getEventUser());    //Inser Modify data to Board.
	
		
		/*
		InserPartImage(PartImage,PartID,PartGroup);
		
		//test part 		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT PARTIMAGE " + "\n")
					.append("    FROM PMS_SPAREPART " + "\n")
					.append("WHERE PARTID = ? " + "\n");
		
		Object[] bindArray = new Object[] {PartID};
		
		List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuilder.toString(), bindArray);
						
		String Imagevalue = "";
		
		Object value = result.get(0).get("PARTIMAGE");
		
		List Result = greenFrameServiceProxy.getSqlTemplate().queryForList("SELECT PARTIMAGE FROM PMS_SPAREPART WHERE PARTID = ?", "xp-007-wenguantest");
		
		Object value2 = Result.get(0);
				
		String binary = StringUtil.substring(result.get(0).get("PARTIMAGE").toString(), 0, 10);
		
		String binary2 = value.getClass().toString();
		
		eventLog.info(binary);
		
		Connection connection = DBConnect();
		
		System.out.println("连接成功： "+connection);
		*/
			
		return doc;
	}
	
	public void InsertBoard(String PartID, String User) throws CustomException
	{
		String [] scopeShopList = new String[]{"Administrator","AMOLED General Manager","AM EQP","AMOLED Design Department","AMOLED EN Production","AMOLED EV Production","AMOLED LTPS Production","MaterialControl","Purchase"
				                               ,"Facility Department","TFT General Manager","TFT EQP","TFT Array Production","TFT CELL Production","TFT CF Production","TFT 1st technical Department","TFT 2st technical Department","AMOLED 1st technical Department","AMOLED 2st technical Department"};
		
		String no;
		try
		{
			String currentTime = TimeUtils.getCurrentEventTimeKey();		
		    no = currentTime.substring(2, 14) + currentTime.substring(17);			
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}	
		
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyBoardPMS", getEventUser(), getEventComment(), null, null);
			
			String title = "SparePart Modify";
			String comments = "SparePart: " +" [" +PartID + "] "+ " was modified by " + " [ " + User + " ] " +"." + "\n" + "You can check detail info with SparePartHistory Function.";
			
			//Create
			BulletinBoard boardData = new BulletinBoard("Administrator", no);
			boardData.setTitle(title);
			boardData.setCreateTime(eventInfo.getEventTime());
			boardData.setCreateUser(eventInfo.getEventUser());
			boardData.setComments(comments);
			
			boardData = PMSServiceProxy.getBulletinBoardService().create(eventInfo, boardData);
			
			for(String eleshop : scopeShopList)
			{
				String scopeShopName = eleshop.toString();
				
				BulletinBoardArea boardAreaData = new BulletinBoardArea(scopeShopName, no);
				boardAreaData = PMSServiceProxy.getBulletinBoardAreaService().create(eventInfo, boardAreaData);
			}
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}		
	}
	
	public void InserPartImage(String ImageFile,String ParID,String PartGroup) throws CustomException
	{
		try
		{
			LobHandler lobHandler = greenFrameServiceProxy.getLobHandler();		
			//String inserSql = "INSERT INTO PMS_SPAREPART(partid,partimage,partgroup) VALUES (:partid,:image,:partgroup) ";		
			String inserSql = "UPDATE PMS_SPAREPART SET partimage =:partimage WHERE partid =:partid AND partgroup =:partgroup ";
			
			Map<String, Object> insertBindMap = new HashMap<String, Object>();
			byte[] imageContents = ImageFile.getBytes();
			insertBindMap.put("partid", ParID);
			insertBindMap.put("partgroup", PartGroup);
			insertBindMap.put("partimage", new GenSqlLobValue(imageContents,lobHandler));
			
			//This greenFrameService doesn't use now
			//greenFrameServiceProxy.getSqlTemplate().update(inserSql, insertBindMap); 
			GenericServiceProxy.getSqlMesTemplate().update(inserSql, insertBindMap);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0097");
		}
	}
	
	public static Connection DBConnect() throws CustomException
	{
	     String driverUrl = "oracle.jdbc.driver.OracleDriver";  
	  
	     String url = "jdbc:oracle:thin:@localhost:1521:orcl";  
	     
	     //String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL = TCP)(HOST = 10.16.111.245)(PORT = 1521)))(CONNECT_DATA =(SERVICE_NAME = testdb1))))";  
	  
	     //String username = "TRULYMESTST";  	      
	     //String password = "trulymestst";
	     
	       String username = "TRULYMESLOCAL";  	      
	       String password = "trulymeslocal";
	     
	     Connection connection = null ;
	     
		try 
		{
	    	 Class.forName(driverUrl);  
             connection = DriverManager.getConnection(url, username, password);  
             connection.close();      	 
	    } 		
	    catch (ClassNotFoundException e) 
		{  	    	 
            e.printStackTrace();  
	    } 
		catch (SQLException e) 
		{
	        e.printStackTrace(); 
	    }  
		
	    return connection ;	     
	}
}
