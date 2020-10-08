package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;
import org.jdom.Element;

public class UnShipLot extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{		 
		Element lotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		List<Product> productList = null;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unship", getEventUser(), getEventComment(), null, null);
				
		if (lotList != null)
		{
			for ( @SuppressWarnings("rawtypes")
			Iterator iteratorLotList = lotList.getChildren().iterator(); iteratorLotList.hasNext();)
			{
				Element lotE = (Element) iteratorLotList.next();
				
				String lotName  = SMessageUtil.getChildText(lotE, "LOTNAME", true);

				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
				
				String shipProcessOperationName = CommonUtil.getProcessOperationNameByDetailProcessOperationType(lotData.getFactoryName(), lotData.getProcessFlowName(), "SHIP");
				String shipNodeStack = CommonUtil.getNodeStack(lotData.getFactoryName(), lotData.getProcessFlowName(), shipProcessOperationName);
				
				if(StringUtil.isNotEmpty(lotData.getCarrierName()))
				{
					Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

					if( StringUtil.equals(durableData.getUdfs().get("RECEIVEFLAG"), "Y") )
					{
						// can not unship
						throw new CustomException("LOT-9024", lotData.getKey().getLotName()); 
					}
				}
				
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);
				productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotName);
				
/*				//20180504, kyjung, QTime
				for (Product productData : productList )
				{     
					MESProductServiceProxy.getProductServiceImpl().ExitedCancelQTime(eventInfo, productData, "Ship");
				}*/
				
				CheckTurnFlag(productList);
				
				lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
				lotData.setLotProcessState("");
				lotData.setLotHoldState("");
				lotData.setDestinationFactoryName("");
				lotData.setProcessOperationName(shipProcessOperationName);
				lotData.setNodeStack(shipNodeStack);
				
				// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//				Map<String, String> udfs = lotData.getUdfs();
//				udfs.put("SHIPBANK", "");
//				udfs.put("SHIPBANKSTATE", "");
				LotServiceProxy.getLotService().update(lotData);
								
				SetEventInfo setEventInfo = new SetEventInfo();
				// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//				setEventInfo.setUdfs(udfs);
				setEventInfo.getUdfs().put("SHIPBANK", "");
				setEventInfo.getUdfs().put("SHIPBANKSTATE", "");

				eventInfo.setEventName("UnShip");
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
				
				for(Product allProduct : productList)
				{
					allProduct.setProductState(GenericServiceProxy.getConstantMap().Prod_InProduction);
					allProduct.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
					allProduct.setProductHoldState(GenericServiceProxy.getConstantMap().Prq_NotOnHold);
					allProduct.setDestinationFactoryName("");
					allProduct.setProcessOperationName(shipProcessOperationName);
					allProduct.setNodeStack(shipNodeStack);
					
					ProductServiceProxy.getProductService().update(allProduct);
					
					kr.co.aim.greentrack.product.management.info.SetEventInfo productSetEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
					ProductServiceProxy.getProductService().setEvent(allProduct.getKey(), eventInfo, productSetEventInfo);
				}				
				/*
				MakeUnShippedInfo makeUnShippedInfo = 
						MESLotServiceProxy.getLotInfoUtil().makeUnShippedInfo(lotData, lotData.getAreaName(), productUdfs); 
				
				EventInfo eventInfo2 = EventInfoUtil.makeEventInfo("CancelShip", getEventUser(), getEventComment(), "", "");
				
				Lot aLot = MESLotServiceProxy.getLotServiceImpl().unShipLot(eventInfo, lotData, makeUnShippedInfo);
				*/

				//Increment Work Order Finished Quantity
				//MESLotServiceProxy.getLotServiceUtil().incrementWorkOrderFinishQty(eventInfo, lotData.getProductRequestName(), (int) -lotData.getProductQuantity());
			}
		}
		
		return doc;
	}
	
	public static void CheckTurnFlag(List<Product> productList) throws CustomException
	{
		ArrayList<String> arrPrdName = new ArrayList<String>();
		
		for(Product productName : productList)
		{
				// TurnFlag column is not exist. will insert col.
			// No mentis list , 2019.03.12 by shkim , PRODUCT >> CT_PRODUCTFLAG
			String sql = "SELECT PROCESSTURNFLAG TURNSIDEFLAG, TURNOVERFLAG FROM CT_PRODUCTFLAG "
					+ "WHERE PRODUCTNAME = :productname ";
				
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("productname", productName.getKey().getProductName());
				
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if(sqlResult.size() > 0)
				{
					String turnSideFlag = (String)sqlResult.get(0).get("TURNSIDEFLAG");	
					String turnOverFlag = (String)sqlResult.get(0).get("TURNOVERFLAG");
					
					if(!StringUtil.isEmpty(turnSideFlag)||!StringUtil.isEmpty(turnOverFlag))
					{
						if(StringUtil.equals(turnSideFlag, "Y")||StringUtil.equals(turnOverFlag, "Y"))
						{
							arrPrdName.add(productName.getKey().getProductName());
						}
					}
				}
		}
		
		if(arrPrdName.size()>0)
		{
			String existTurnFlagProductList = "";
			
			for(int i=0;i<arrPrdName.size();i++)
			{
				existTurnFlagProductList += "[" + arrPrdName.get(i).toString() + "]";
			}
		
			throw new CustomException("TURN-0001", existTurnFlagProductList); 
		}
	}
	
	public static boolean CheckERPFinishWorkOrder(String productRequestName) throws CustomException
	{
		try
		{		
			String sql = "SELECT A.PRODUCTREQUESTNAME,A.PRODUCTREQUESTSTATE FROM CT_MESPRODUCTREQUEST A,CT_ERPPRODUCTREQUEST B "
					+ "WHERE A.PRODUCTREQUESTNAME = B.PRODUCTREQUESTNAME "
					+ "AND A.PRODUCTREQUESTNAME = :productrequestname AND A.PRODUCTREQUESTSTATE = :productrequeststate";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("productrequestname", productRequestName);
			bindMap.put("productrequeststate", "Completed");
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = 
					GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if(sqlResult.size() > 0)
			{
				return true;
			}
			
		}
		catch (greenFrameDBErrorSignal ex)
		{
			//throw new CustomException();
		}
		catch (Exception ex)
		{
			//throw new CustomException();
		}
		
		return false;
	}
}
