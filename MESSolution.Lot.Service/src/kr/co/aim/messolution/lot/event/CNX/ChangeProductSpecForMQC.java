package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.data.ProductKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeProductSpecForMQC extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String LimitCount = SMessageUtil.getBodyItemValue(doc, "LIMITCOUNT", true);
		String ReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String ReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String ProductionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", true);
		String BeforeProductionType = SMessageUtil.getBodyItemValue(doc, "BEFOREPRODUCTIONTYPE", true);
		List<Element> eleProductList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMQCSpec", getEventUser(), getEventComment(), null, null);
		SetEventInfo setEventInfo = new SetEventInfo();
		kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo2 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
		// Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		// modify by jhiying on 20191008 mantis: 4968 start
		if (!lotData.getLotState().toString().equals("Completed") )
		{
			throw new CustomException("MQC-0088", "");
		}	
		// modify by jhiying on 20191008 mantis: 4968 end

		eventInfo.setBehaviorName("ARRAY");
		eventInfo.setReasonCode(ReasonCode);
		eventInfo.setReasonCodeType(ReasonCodeType);
	
		String sql = "SELECT EDV.ENUMVALUE FROM ENUMDEF ED JOIN ENUMDEFVALUE EDV ON ED.ENUMNAME = EDV.ENUMNAME WHERE ED.ENUMNAME = :ENUMNAME AND TAG = :TAG";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("ENUMNAME", "EndBank");
		bindMap.put("TAG", "MQCA");
				
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		String NewEndBank = "";
		if(sqlResult.size() > 0)
		{
			NewEndBank = sqlResult.get(0).get("ENUMVALUE").toString();
		}			
		//2018.09.21 dmlee : old Product RequestName
		String oldProductRequestName = lotData.getProductRequestName();
					
		// ********** Mentis 2607 **********
		String ChangeproductNameList = StringUtil.EMPTY;
		String LotNote = "[Reason Code] - " + ReasonCode;
		for(Element eleProduct : eleProductList)
		{
			if(StringUtil.isEmpty(ChangeproductNameList))
			{
				ChangeproductNameList = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
			}
			else
			{
				ChangeproductNameList = ChangeproductNameList + " , " +  SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
			}
		}
		ChangeproductNameList = "[Change Product List] - " + ChangeproductNameList;
		LotNote = LotNote + " , " + ChangeproductNameList;
		// ********** Mentis 2607 **********
		
		String OldProductSpecName = lotData.getProductSpecName();
		if(StringUtils.equals(BeforeProductionType,"DMQC"))
		{// Mentis 1342
			lotData.setProductSpecName("M-COMMON");
		}
		
		lotData.setProductionType(ProductionType);
		lotData.setProductRequestName("");
		lotData.setProcessOperationName("-");
		lotData.setProcessOperationVersion("");
		
		String temptimekey = eventInfo.getEventTimeKey();
		
		setEventInfo.getUdfs().put("ENDBANK", NewEndBank);
		//2018.09.18 dmlee : old Product Request
		setEventInfo.getUdfs().put("OLDPRODUCTREQUESTNAME", oldProductRequestName);
		//2018.09.18 dmlee ---------------------
		Map<String, String> udfs = lotData.getUdfs();
		udfs.put("NOTE", LotNote);
		LotServiceProxy.getLotService().update(lotData);
		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		
		// 2019.02.27
        LotHistoryKey LotHistoryKey = new LotHistoryKey();
        LotHistoryKey.setLotName(lotData.getKey().getLotName());
        LotHistoryKey.setTimeKey(temptimekey);
        
        // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
        // LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
        LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(LotHistoryKey);
        
        LotHistory.setOldProductionType(BeforeProductionType);
		if(StringUtils.equals(BeforeProductionType,"DMQC"))
		{
			LotHistory.setOldProductSpecName(OldProductSpecName);
		}
		LotServiceProxy.getLotHistoryService().update(LotHistory);
		
		for(Element eleProduct : eleProductList)
		{
			// Mentis 3058 
			// 2019.03.20
			String FromProductSpec = "";
			if(StringUtils.equals(BeforeProductionType,"DMQC"))
			{
				FromProductSpec = OldProductSpecName;
			}
			else
			{
				FromProductSpec = SMessageUtil.getChildText(eleProduct, "MQCUSEPRODUCTSPEC", false);
			}

			if(FromProductSpec != null && SMessageUtil.getChildText(eleProduct, "TOPRODUCTSPEC", false) != null)
			{
				if(!StringUtil.isEmpty(FromProductSpec) && !StringUtil.isEmpty(SMessageUtil.getChildText(eleProduct, "TOPRODUCTSPEC", false)))
				{
					if(!StringUtil.equals(FromProductSpec, SMessageUtil.getChildText(eleProduct, "TOPRODUCTSPEC", false)))
					{
						if(!checkPolicy(FromProductSpec, SMessageUtil.getChildText(eleProduct, "TOPRODUCTSPEC", false)))
						{
							throw new CustomException("MQC-0077", "");
						}
					}
				}
			}
			
			String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
			String toProductSpecName = SMessageUtil.getChildText(eleProduct, "TOPRODUCTSPEC", false);
			String toecCode = SMessageUtil.getChildText(eleProduct, "TOECCODE", false);
			String toprocessflow = SMessageUtil.getChildText(eleProduct, "TOPROCESSFLOW", false);

			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
			// Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productName));
						
			// 2019.04.18		
			if(!StringUtils.equals(BeforeProductionType,"DMQC"))
			{
				if(StringUtil.equals(productData.getUdfs().get("MQCCOUNT"),"0"))
				{
					throw new CustomException("MQC-0177", "");
				}
			}

			if(StringUtils.equals(BeforeProductionType,"DMQC"))
			{// Mentis 1342
				productData.setProductSpecName("M-COMMON");
			}
			productData.setProductionType(ProductionType);
			productData.setProductRequestName("");
			productData.setProcessOperationName("-");
			productData.setProcessOperationVersion("");

			setEventInfo2.getUdfs().put("MQCLIMITCOUNT", String.valueOf(LimitCount));
			setEventInfo2.getUdfs().put("MQCCOUNT", String.valueOf(0));
			
			// Mentis 1342 add By ParkJeongSu
			setEventInfo2.getUdfs().put("MQCUSEPRODUCTSPEC", toProductSpecName);
			setEventInfo2.getUdfs().put("MQCUSEECCODE", toecCode);
			setEventInfo2.getUdfs().put("MQCUSEPROCESSFLOW", toprocessflow);
			ProductServiceProxy.getProductService().update(productData);						
			ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo2);
			
			// 2019.04.18
            ProductHistoryKey productHistoryKey = new ProductHistoryKey();
            productHistoryKey.setProductName(productData.getKey().getProductName());
            productHistoryKey.setTimeKey(temptimekey);

            // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
            // ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
            
			productHistory.setOldProductionType(BeforeProductionType);
			if(StringUtils.equals(BeforeProductionType,"DMQC"))
			{
				productHistory.setOldProductSpecName(OldProductSpecName);
			}
			ProductServiceProxy.getProductHistoryService().update(productHistory);	
		}

		// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
		// Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		// Map<String, String> udfs_note = lotData_Note.getUdfs();
		// udfs_note.put("NOTE", "");
		// LotServiceProxy.getLotService().update(lotData_Note);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		
		return doc;
	}

	// Mentis 3058 
	// 2019.03.20
	private boolean checkPolicy(String FromSpec, String ToSpec)
	{
		String Query = "SELECT DISTINCT P.MATERIALSPECNAME FROM TPPOLICY T JOIN POSBOM P ON T.CONDITIONID = P.CONDITIONID WHERE T.PRODUCTSPECNAME = :PRODUCTSPECNAME";
		try
		{
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("PRODUCTSPECNAME", FromSpec);				
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> FromsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
			if(FromsqlResult != null && FromsqlResult.size() > 0)
			{
				bindMap.clear();
				bindMap.put("PRODUCTSPECNAME", ToSpec);
				List<Map<String, Object>> TosqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
				if(TosqlResult != null && TosqlResult.size() > 0)
				{
					String FromMaterialSpec = "";
					String ToMaterialSpec = "";
					
					for(int i=0; i<FromsqlResult.size(); i++)
					{
						for(int j=0; j<TosqlResult.size(); j++)
						{
							if(FromsqlResult.get(i).get("MATERIALSPECNAME") != null && TosqlResult.get(j).get("MATERIALSPECNAME") != null)
							{
								FromMaterialSpec = FromsqlResult.get(i).get("MATERIALSPECNAME").toString();
								ToMaterialSpec = TosqlResult.get(j).get("MATERIALSPECNAME").toString();
								
								if(StringUtil.equals(FromMaterialSpec, ToMaterialSpec))
								{
									return true;
								}
							}
							else
							{
								return false;
							}
						}
					}
				}
				else 
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		catch(Exception ex)
		{
			return false;
		}
		
		return false;
	}
}