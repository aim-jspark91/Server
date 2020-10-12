package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.Product;
import kr.co.aim.messolution.fgms.management.data.ShipRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.area.AreaServiceProxy;
import kr.co.aim.greentrack.area.management.data.Area;
import kr.co.aim.greentrack.area.management.data.AreaKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;

import org.jdom.Document;

public class CompleteShipRequest extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String inVoiceNo        = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		String lastEventUser    = SMessageUtil.getBodyItemValue(doc, "LASTEVENTUSER", true);
		String lastEventComment = SMessageUtil.getBodyItemValue(doc, "LASTEVENTCOMMENT", true);

		Timestamp lastEventTime = ConvertUtil.getCurrTimeStampSQL();
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_TIMEKEY);
		String lastEventTimeKey = formatter.format(lastEventTime);
		Timestamp completeTime = lastEventTime;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");

		ShipRequest ShipRequestData = null;
		ShipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(true, new Object[] {inVoiceNo});
		
		try{
			ShipRequestData = new ShipRequest(inVoiceNo);
			ShipRequestData.setShipRequestState("Completed");
			ShipRequestData.setCompleteTime(completeTime);
			ShipRequestData.setCompleteUser(eventInfo.getEventUser());
			ShipRequestData.setLastEventName("Complete");
			ShipRequestData.setLastEventTimeKey(lastEventTimeKey);
			ShipRequestData.setLastEventTime(lastEventTime);
			ShipRequestData.setLastEventUser(lastEventUser);
			ShipRequestData.setLastEventComment(lastEventComment);
			
			FGMSServiceProxy.getShipRequestService().modify(eventInfo, ShipRequestData);
		}catch (Exception ex)
		{
			eventLog.error(String.format( "Modify CT_SHIPREQUEST Fail [ inVoiceNo = %s] ", inVoiceNo));
		}
		
		String invoiceType = ShipRequestData.getInVoiceType();
		
		List<ProcessGroup> processGroupList = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getPalletListFromInvoiceNo(inVoiceNo);
		
		if(invoiceType.equals("RT"))
		{
			eventInfo.setEventName("StockOut");
			
			for(ProcessGroup pallet : processGroupList)
			{
				String palletName = pallet.getKey().getProcessGroupName();
				String areaName   = pallet.getUdfs().get("LOCATION");
				
				List<Product> productList = null;

				productList = FGMSServiceProxy.getProductService().select("PALLETNAME = ? AND STOCKSTATE = ?", new Object[] {palletName, "StockIn"});

				for (Product productData : productList)
				{
					try
					{
						productData.setProductName(productData.getProductName());
//						productData.setBoxName(productData.getBoxName());
//						productData.setPalletName(productData.getPalletName());
						productData.setProductSpecName(productData.getProductSpecName());
//						productData.setFgCode(productData.getFgCode());
						productData.setProductType(productData.getProductType());
						productData.setSubProductType(productData.getSubProductType());
						productData.setProductGrade(productData.getProductGrade());
						productData.setCrateName(productData.getCrateName());
						productData.setStockState("StockOut");
						
						FGMSServiceProxy.getProductService().modify(eventInfo, productData);
						
//						eventLog.error(String.format("Modify Product [%s, %s] Success", productData.getProductName(), productData.getPalletName()));
					}
					catch (Exception ex)
					{
//						eventLog.error(String.format("Modify Fail [%s, %s] ", productData.getProductName(), productData.getPalletName()));
					}
				}
				
				//Update Location Info(Area)
				AreaKey areaKey = new AreaKey();
				areaKey.setAreaName(areaName);
						
				Area areaData = null;
				areaData = AreaServiceProxy.getAreaService().selectByKey(areaKey);
						
				Map<String, String> areaUdfs = new HashMap<String,String>();
				areaUdfs.put("LocationState", GenericServiceProxy.getConstantMap().FGMS_LOCATION_EMPTY);
				areaUdfs.put("PalletName", palletName);
				areaData.setUdfs(areaUdfs);
						
				AreaServiceProxy.getAreaService().update(areaData);
			
			}
		}
		else if(invoiceType.equals("ETC"))
		{
			eventInfo.setEventName("StockOut");
			
			for(ProcessGroup pallet : processGroupList)
			{
				String palletName = pallet.getKey().getProcessGroupName();
				String areaName   = pallet.getUdfs().get("LOCATION");
				
				/*
				ProcessGroupKey processGroupKey = new ProcessGroupKey();
				processGroupKey.setProcessGroupName(palletName);
				
				ProcessGroup processGroup =
						ProcessGroupServiceProxy.getProcessGroupService().selectByKey(processGroupKey);

				ChangeSpecInfo aChangeSpecInfo = new ChangeSpecInfo();
				aChangeSpecInfo.setProductionType(productionType);
				
				ProcessGroup changeSpec = MESProcessGroupServiceProxy.getProcessGroupServiceImpl().changeSpec(processGroup, aChangeSpecInfo, eventInfo);
				*/
				//Update Location Info(Area)
				AreaKey areaKey = new AreaKey();
				areaKey.setAreaName(areaName);
						
				Area areaData = null;
				areaData = AreaServiceProxy.getAreaService().selectByKey(areaKey);
						
				Map<String, String> areaUdfs = new HashMap<String,String>();
				areaUdfs.put("LocationState", GenericServiceProxy.getConstantMap().FGMS_LOCATION_EMPTY);
				areaUdfs.put("PalletName", palletName);
				areaData.setUdfs(areaUdfs);
						
				AreaServiceProxy.getAreaService().update(areaData);
			}
		}
		else
		{
			eventInfo.setEventName("StockOut");
			
			for(ProcessGroup pallet : processGroupList)
			{
				String palletName = pallet.getKey().getProcessGroupName();
				String areaName   = pallet.getUdfs().get("LOCATION");
				
				List<Product> productList = null;

				productList = FGMSServiceProxy.getProductService().select("PALLETNAME = ? AND STOCKSTATE = ?", new Object[] {palletName, "StockIn"});

				for (Product productData : productList)
				{
					try
					{
						productData.setProductName(productData.getProductName());
//						productData.setBoxName(productData.getBoxName());
//						productData.setPalletName(productData.getPalletName());
						productData.setProductSpecName(productData.getProductSpecName());
//						productData.setFgCode(productData.getFgCode());
						productData.setProductType(productData.getProductType());
						productData.setSubProductType(productData.getSubProductType());
						productData.setProductGrade(productData.getProductGrade());
						productData.setCrateName(productData.getCrateName());
						productData.setStockState("StockOut");
						
						FGMSServiceProxy.getProductService().modify(eventInfo, productData);
						
//						eventLog.error(String.format("Modify Product [%s, %s] Success", productData.getProductName(), productData.getPalletName()));
					}
					catch (Exception ex)
					{
//						eventLog.error(String.format("Modify Fail [%s, %s] ", productData.getProductName(), productData.getPalletName()));
					}
				}
				
				String CurrentTime = ConvertUtil.getCurrTimeStampSQL().toString();
				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("STOCKSTATE", "Shipped");
				udfs.put("SHIPTIME", CurrentTime);
				udfs.put("SHIPUSER", eventInfo.getEventUser());
				udfs.put("LOCATION", "");

				Map<String, String> materialUdfs = new HashMap<String, String>();
				Map<String, String> subMaterialUdfs = new HashMap<String, String>();
				subMaterialUdfs.put("SHIPTIME", CurrentTime);
				subMaterialUdfs.put("SHIPUSER", eventInfo.getEventUser());
				
				MESProcessGroupServiceProxy.getProcessGroupServiceImpl().
					makeShipped(eventInfo, palletName, "FGMS", "", "Y", materialUdfs, subMaterialUdfs, subMaterialUdfs);
				
				//Update Location Info(Area)
				AreaKey areaKey = new AreaKey();
				areaKey.setAreaName(areaName);
						
				Area areaData = null;
				areaData = AreaServiceProxy.getAreaService().selectByKey(areaKey);
						
				Map<String, String> areaUdfs = new HashMap<String,String>();
				areaUdfs.put("LocationState", GenericServiceProxy.getConstantMap().FGMS_LOCATION_EMPTY);
				areaUdfs.put("PalletName", palletName);
				areaData.setUdfs(areaUdfs);
						
				AreaServiceProxy.getAreaService().update(areaData);
			}
		}

		return doc;
	}
}
