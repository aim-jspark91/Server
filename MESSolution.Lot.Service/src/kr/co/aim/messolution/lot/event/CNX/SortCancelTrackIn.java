package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

import org.jdom.Document;
import org.jdom.Element;

public class SortCancelTrackIn extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String JobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), null, null);
		
		String condition = "where jobname=?";
		Object[] bindSet = new Object[] {JobName};
		List<SortJobCarrier> SortJobCarrier = null;
		SortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().select(condition,bindSet);
		for (SortJobCarrier Carrier : SortJobCarrier)
		{
			String LotName = Carrier.getLotName();
			if(!StringUtil.isEmpty(LotName))
			{
				Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(LotName);
				String LotProcessState = lotData.getLotProcessState();
				if(!StringUtil.equals(LotProcessState,"RUN"))
				{
					throw new CustomException("SORT-0009");
				}
				
				String carrierName = Carrier.getCarrierName();
				String machineName = Carrier.getMachineName();
				String portName = Carrier.getPortName();
				
				Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

				List<Element> productList = new ArrayList<Element>();
				
				String Query = "SELECT P.POSITION, P.PRODUCTNAME, P.PRODUCTGRADE FROM PRODUCT P, LOT L  WHERE P.LOTNAME = L.LOTNAME AND L.CARRIERNAME = :CARRIERNAME AND PRODUCTSTATE IN (:PRODUCTSTATE)";
				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("CARRIERNAME", carrierName);
				bindMap.put("PRODUCTSTATE", GenericServiceProxy.getConstantMap().Prod_InProduction);
				
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sqlResult2 = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
				if(sqlResult2 != null && sqlResult2.size() > 0)
				{
					for(int J=0; J<sqlResult2.size(); J++)
					{
						Element eleBodyTemp = new Element("PRODUCT");
						
		    			Element element1 = new Element("PRODUCTNAME");
		    			element1.setText(sqlResult2.get(J).get("PRODUCTNAME").toString());
		    			eleBodyTemp.addContent(element1);
		    			
		    			Element element2 = new Element("POSITION");
		    			element2.setText(sqlResult2.get(J).get("POSITION").toString());
		    			eleBodyTemp.addContent(element2);
		    			
		    			Element element3 = new Element("PRODUCTJUDGE");
		    			element3.setText(sqlResult2.get(J).get("PRODUCTGRADE").toString());
		    			eleBodyTemp.addContent(element3);
		    			
		    			productList.add(eleBodyTemp);
					}
				}

				Lot cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().getTrackOutLot(eventInfo, carrierName, LotName, productList);
				
				if (cancelTrackInLot == null)
				{
					throw new CustomException("LOT-XXXX", carrierName);
				}
				
				for (Element productEle : productList) 
				{
					String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
					Product productData = MESProductServiceProxy .getProductServiceUtil().getProductData(productName);
					
					// Modified by smkang on 2019.05.28 - ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//					Map<String, String> productUdfs = productData.getUdfs();
//					productUdfs.put("PORTNAME", "");
//					
//					kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
//					setEventInfo.setUdfs(productUdfs);
//					ProductServiceProxy.getProductService().update(productData);
					Map<String, String> updateUdfs = new HashMap<String, String>();
					updateUdfs.put("PORTNAME", "");
					MESProductServiceProxy.getProductServiceImpl().updateProductWithoutHistory(productData, updateUdfs);
				}

				for (Element productEle : productList )
				{   
					String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
					Product productData = MESProductServiceProxy .getProductServiceUtil().getProductData(productName);						
					MESProductServiceProxy.getProductServiceImpl().ExitedCancelQTime(eventInfo, productData, "TrackIn");
				}	
						
				List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(productList, machineName);

				String recipeName = lotData.getMachineRecipeName();
				
				if(StringUtil.isNotEmpty(recipeName))
				{
					MESProductServiceProxy.getProductServiceImpl().cancelTIRecipeIdleTimeLot(machineName, recipeName, eventInfo);
				}

				eventInfo.setEventName("CancelTrackIn");
				cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().cancelTrackIn(eventInfo, cancelTrackInLot, portData, "", carrierName, "", productPGSRCSequence, new HashMap<String, String>(), new HashMap<String,String>());
				
				// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				Lot lotData_Port = MESLotServiceProxy.getLotInfoUtil().getLotData(LotName);
//				Map<String, String> udfs_note = lotData_Port.getUdfs();
//				udfs_note.put("PORTNAME", "");
//				LotServiceProxy.getLotService().update(lotData_Port);
				Map<String, String> updateUdfs = new HashMap<String, String>();
				updateUdfs.put("PORTNAME", "");
				MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(cancelTrackInLot, updateUdfs);
			}
		}
		
		if( SortJobCarrier != null)
		{
			String TempQuery = "SELECT CARRIERNAME FROM CT_SORTJOBCARRIER WHERE JOBNAME = :JOBNAME";
			Map<String, String> bindMap2 = new HashMap<String, String>();
			bindMap2.put("JOBNAME", JobName);
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult3 = GenericServiceProxy.getSqlMesTemplate().queryForList(TempQuery, bindMap2);
			
			if(sqlResult3 != null && sqlResult3.size() > 0)
			{
				for(int k=0; k<sqlResult3.size(); k++)
				{
					MESLotServiceProxy.getLotServiceUtil().UpdateSortCSTInfo(sqlResult3.get(k).get("CARRIERNAME").toString(), eventInfo);
				}
			}
			
		    SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {JobName});
		    
		    sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CANCELED);
	        sortJob.setEventComment(eventInfo.getEventComment());
	        sortJob.setEventName(eventInfo.getEventName());
	        sortJob.setEventTime(eventInfo.getEventTime());
	        sortJob.setEventUser(eventInfo.getEventUser());
	        
	        ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
		}
		
		return doc;
	}
}
