package kr.co.aim.messolution.pms.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.SparePart;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.GenSqlLobValue;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.springframework.jdbc.support.lob.LobHandler;

public class CreateSparePart extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String PartID 		 = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
		String PartName      = SMessageUtil.getBodyItemValue(doc, "PARTNAME", true);
		String PartGroup 	 = SMessageUtil.getBodyItemValue(doc, "PARTGROUP", true);
		String VendorID  	 = SMessageUtil.getBodyItemValue(doc, "VENDORNAME", true);		
		String Quantity  	 = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		String SafeQuantity  = SMessageUtil.getBodyItemValue(doc, "SAFEQUANTITY", true);
		String WarningQty    = SMessageUtil.getBodyItemValue(doc, "WARNINGQUANTITY", true);
		String PartSpec 	 = SMessageUtil.getBodyItemValue(doc, "PARTSPEC", true);	
		String PurchaseCycle = SMessageUtil.getBodyItemValue(doc, "PURCHASECYCLE", false);
		String Location      = SMessageUtil.getBodyItemValue(doc, "LOCATION", false);
		String Unit          = SMessageUtil.getBodyItemValue(doc, "UNIT", false);
		String GroupName     = SMessageUtil.getBodyItemValue(doc, "GROUPNAME", true);
		String PartAttribute = SMessageUtil.getBodyItemValue(doc, "PARTATTRIBUTE", true);	
		String PartType      = SMessageUtil.getBodyItemValue(doc, "PARTTYPE", false);
		String MaterialCode  = SMessageUtil.getBodyItemValue(doc, "MATERIALCODE", false);
		String PartDesc      = SMessageUtil.getBodyItemValue(doc, "PARTDESC", false);
		String UseDesc       = SMessageUtil.getBodyItemValue(doc, "USEDESCRIPTION", false);	
		//String PartImage     = SMessageUtil.getBodyItemValue(doc, "IMAGE", false);

		
		Number NotInQuantity       = 0;
		Number PurchaseCompleteQty = 0;
		int UnitPrice              = 0;
		int UseFrequency           = 0;

			
		int quantity      = Integer.valueOf(Quantity);
		int safeQuantity  = Integer.valueOf(SafeQuantity);
		int warningQty    = Integer.valueOf(WarningQty);

		
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("CreateSparePart", getEventUser(), getEventComment(), null, null);
		
		SparePart sparePartData = new SparePart(PartID);
		
		sparePartData.setPartID(PartID);
		sparePartData.setPartName(PartName);
		sparePartData.setPartGroup(PartGroup);
		sparePartData.setVendorID(VendorID);
		sparePartData.setQuantity(quantity);
		sparePartData.setSafeQuantity(safeQuantity);
		sparePartData.setWarningQuantity(warningQty);
		sparePartData.setUseFrequency(UseFrequency);
		sparePartData.setUseDescription(UseDesc);
		sparePartData.setPartSpec(PartSpec);
		sparePartData.setPurchaseCycle(PurchaseCycle);
		sparePartData.setLocation(Location);
		sparePartData.setUnit(Unit);
		sparePartData.setUnitPrice(UnitPrice);
		sparePartData.setPartDescription(PartDesc);
		sparePartData.setPartAttribute(PartAttribute);
		sparePartData.setNotInQuantity(NotInQuantity);
		sparePartData.setPurchaseCompleteQty(PurchaseCompleteQty);
		sparePartData.setGroupName(GroupName);
		sparePartData.setPartType(PartType);
		sparePartData.setMaterialCode(MaterialCode);
				
		try
		{
			sparePartData = PMSServiceProxy.getSparePartService().create(eventInfo, sparePartData);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0063", PartID);
		}
				
		//Save PartImageFile		
		//InserPartImage(PartImage,PartID,PartGroup);
		
		return doc;			
	}
	
	public void InserPartImage(String ImageFile,String ParID,String PartGroup) throws CustomException
	{
		//String sTable = "PMS_SPAREPART" ;
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
			
			//greenFrameServiceProxy.getSqlTemplate().update(inserSql, insertBindMap);
			GenericServiceProxy.getSqlMesTemplate().update(inserSql, insertBindMap);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0097");
		}
	}
}
