package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReworkLot;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReworkLotService extends CTORMService<ReworkLot>{
	
	public static Log logger = LogFactory.getLog(ReworkLot.class);
	
	private final String historyEntity = "ReworkLotHist";
	
	public List<ReworkLot> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<ReworkLot> result = super.select(condition, bindSet, ReworkLot.class);
			
			return result;
		}
		
		public ReworkLot selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(ReworkLot.class, isLock, keySet);
		}
		
		public ReworkLot create(EventInfo eventInfo, ReworkLot dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, ReworkLot dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public ReworkLot modify(EventInfo eventInfo, ReworkLot dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		/*
		* Name : increaseProductReworkCount
		* Desc : This function is increaseProductReworkCount
		* Author : hsryu
		* Date : 2018.02.20
		*/ 
		public void increaseReworkCount(List<Product> productList, Lot lotData, String beforeFlow, String beforeOper, String toFlow, String toOper, String departmentName, EventInfo eventInfo)
				throws CustomException
		{		
			List<ListOrderedMap> resultList = PolicyUtil.getProductReworkLimit(lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), lotData.getUdfs().get("ECCODE"),
					beforeFlow, toFlow, beforeOper, toOper);
			
			if(resultList.size() > 0)
			{
				try
				{
					eventInfo.setEventName("IncreaseReworkCount");

					String lotname = lotData.getKey().getLotName();
					String factoryName = resultList.get(0).get("FACTORYNAME").toString();
					String beforeProcessFlowName = resultList.get(0).get("PROCESSFLOWNAME").toString();
					String beforeProcessFlowVersion = resultList.get(0).get("PROCESSFLOWVERSION").toString();
					String beforeProcessOperationName = resultList.get(0).get("PROCESSOPERATIONNAME").toString();
					String beforeProcessOperationVersion = resultList.get(0).get("PROCESSOPERATIONVERSION").toString();
					String toProcessFlowName = resultList.get(0).get("TOPROCESSFLOWNAME").toString();
					String toProcessOperationName = CommonUtil.getFirstOperation(factoryName, toProcessFlowName).getKey().getProcessOperationName(); // firstOper
					long limitCount = resultList.get(0).get("REWORKCOUNT")!=null?Long.parseLong(resultList.get(0).get("REWORKCOUNT").toString()):0;

					//if limitCount = 0, not count.
					if(!Long.toString(limitCount).equals("0"))
					{
						try
						{
							ReworkLot reworkLot = ExtendedObjectProxy.getreworkLotService().selectByKey(false, 
									new Object[]{lotname,factoryName,beforeProcessFlowName,
									beforeProcessFlowVersion,beforeProcessOperationName,beforeProcessOperationVersion,toProcessFlowName,"00001",toProcessOperationName,"00001"});
							
							reworkLot.setCurrentCount(reworkLot.getCurrentCount() + 1);
							reworkLot.setReworkCount(limitCount);							
							reworkLot.setReasonCode(eventInfo.getReasonCode());
							reworkLot.setReworkDepartmentName(departmentName);
							reworkLot.setLastEventUser(eventInfo.getEventUser());
							reworkLot.setLastEventComment(eventInfo.getEventComment());
							reworkLot.setLastEventTime(eventInfo.getEventTime());
							reworkLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
							reworkLot.setLastEventName(eventInfo.getEventName());
							reworkLot.setOriginalLotGrade(lotData.getLotGrade());
							ExtendedObjectProxy.getreworkLotService().modify(eventInfo, reworkLot);
						}

						catch(Exception ex)
						{
							ReworkLot reworkLot = new ReworkLot();
							reworkLot.setLotname(lotname);
							reworkLot.setFactoryName(factoryName);
							reworkLot.setProcessflowName(beforeProcessFlowName);
							reworkLot.setProcessflowVersion(beforeProcessFlowVersion);
							reworkLot.setProcessOperationName(beforeProcessOperationName);
							reworkLot.setProcessOperationVersion(beforeProcessOperationVersion);
							reworkLot.setReworkProcessflowName(toProcessFlowName);
							reworkLot.setReworkProcessflowVersion("00001");
							reworkLot.setReworkProcessOperationName(toProcessOperationName);
							reworkLot.setReworkProcessOperationVersion("00001");
							reworkLot.setReworkCount(limitCount);
							reworkLot.setReasonCode(eventInfo.getReasonCode());
							reworkLot.setReworkDepartmentName(departmentName);
							reworkLot.setCurrentCount(1);
							reworkLot.setLastEventUser(eventInfo.getEventUser());
							reworkLot.setLastEventName(eventInfo.getEventName());;
							reworkLot.setLastEventTime(eventInfo.getEventTime());
							reworkLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
							reworkLot.setLastEventComment(eventInfo.getEventComment());
							reworkLot.setOriginalLotGrade(lotData.getLotGrade());

							ExtendedObjectProxy.getreworkLotService().create(eventInfo, reworkLot);
						}

						for (Product product : productList)
						{
							Product productData = ProductServiceProxy.getProductService().selectByKey(product.getKey());
							String productName = productData.getKey().getProductName();

							try
							{
								ReworkProduct reworkProduct = ExtendedObjectProxy.getReworkProductService().selectByKey(false, 
										new Object[]{productName,lotname,factoryName,beforeProcessFlowName,
										beforeProcessFlowVersion,beforeProcessOperationName,beforeProcessOperationVersion,toProcessFlowName,"00001",toProcessOperationName,"00001"});

								reworkProduct.setCurrentCount(reworkProduct.getCurrentCount() + 1);
								reworkProduct.setReworkCount(limitCount);
								reworkProduct.setReasonCode(eventInfo.getReasonCode());
								reworkProduct.setReworkDepartmentName(departmentName);
								reworkProduct.setLastEventUser(eventInfo.getEventUser());
								reworkProduct.setLastEventComment(eventInfo.getEventComment());
								reworkProduct.setLastEventTime(eventInfo.getEventTime());
								reworkProduct.setLastEventTimeKey(eventInfo.getEventTimeKey());
								reworkProduct.setLastEventName(eventInfo.getEventName());
								reworkProduct.setOriginalProductGrade(product.getProductGrade());
								
								ExtendedObjectProxy.getReworkProductService().modify(eventInfo, reworkProduct);
							}
							catch(Exception ex)
							{
								ReworkProduct reworkProduct = new ReworkProduct();
								reworkProduct.setProductName(productName);
								reworkProduct.setLotname(lotname);
								reworkProduct.setFactoryName(factoryName);
								reworkProduct.setProcessflowName(beforeProcessFlowName);
								reworkProduct.setProcessflowVersion(beforeProcessFlowVersion);
								reworkProduct.setProcessOperationName(beforeProcessOperationName);
								reworkProduct.setProcessOperationVersion(beforeProcessOperationVersion);
								reworkProduct.setReworkProcessflowName(toProcessFlowName);
								reworkProduct.setReworkProcessflowVersion("00001");
								reworkProduct.setReworkProcessOperationName(toProcessOperationName);
								reworkProduct.setReworkProcessOperationVersion("00001");
								reworkProduct.setReworkCount(limitCount);
								reworkProduct.setCurrentCount(1);
								reworkProduct.setReasonCode(eventInfo.getReasonCode());
								reworkProduct.setReworkDepartmentName(departmentName);
								reworkProduct.setOriginalProductGrade(product.getProductGrade());
								reworkProduct.setLastEventUser(eventInfo.getEventUser());
								reworkProduct.setLastEventName(eventInfo.getEventName());;
								reworkProduct.setLastEventTime(eventInfo.getEventTime());
								reworkProduct.setLastEventTimeKey(eventInfo.getEventTimeKey());
								reworkProduct.setLastEventComment(eventInfo.getEventComment());

								ExtendedObjectProxy.getReworkProductService().create(eventInfo, reworkProduct);
							}
						}
					}
					else
					{
						try
						{
							ReworkLot reworkLot = ExtendedObjectProxy.getreworkLotService().selectByKey(false, 
									new Object[]{lotname,factoryName,beforeProcessFlowName,
									beforeProcessFlowVersion,beforeProcessOperationName,beforeProcessOperationVersion,toProcessFlowName,"00001",toProcessOperationName,"00001"});
							
							reworkLot.setCurrentCount(reworkLot.getCurrentCount() + 1);
							reworkLot.setReworkCount(limitCount);							
							reworkLot.setReasonCode(eventInfo.getReasonCode());
							reworkLot.setReworkDepartmentName(departmentName);
							reworkLot.setLastEventUser(eventInfo.getEventUser());
							reworkLot.setLastEventComment(eventInfo.getEventComment());
							reworkLot.setLastEventTime(eventInfo.getEventTime());
							reworkLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
							reworkLot.setLastEventName(eventInfo.getEventName());
							reworkLot.setOriginalLotGrade(lotData.getLotGrade());
							ExtendedObjectProxy.getreworkLotService().modify(eventInfo, reworkLot);
						}

						catch(Exception ex)
						{
							ReworkLot reworkLot = new ReworkLot();
							reworkLot.setLotname(lotname);
							reworkLot.setFactoryName(factoryName);
							reworkLot.setProcessflowName(beforeProcessFlowName);
							reworkLot.setProcessflowVersion(beforeProcessFlowVersion);
							reworkLot.setProcessOperationName(beforeProcessOperationName);
							reworkLot.setProcessOperationVersion(beforeProcessOperationVersion);
							reworkLot.setReworkProcessflowName(toProcessFlowName);
							reworkLot.setReworkProcessflowVersion("00001");
							reworkLot.setReworkProcessOperationName(toProcessOperationName);
							reworkLot.setReworkProcessOperationVersion("00001");
							reworkLot.setReworkCount(limitCount);
							reworkLot.setReasonCode(eventInfo.getReasonCode());
							reworkLot.setReworkDepartmentName(departmentName);
							reworkLot.setCurrentCount(1);
							reworkLot.setLastEventUser(eventInfo.getEventUser());
							reworkLot.setLastEventName(eventInfo.getEventName());;
							reworkLot.setLastEventTime(eventInfo.getEventTime());
							reworkLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
							reworkLot.setLastEventComment(eventInfo.getEventComment());
							reworkLot.setOriginalLotGrade(lotData.getLotGrade());

							ExtendedObjectProxy.getreworkLotService().create(eventInfo, reworkLot);
						}

						for (Product product : productList)
						{
							Product productData = ProductServiceProxy.getProductService().selectByKey(product.getKey());
							String productName = productData.getKey().getProductName();

							try
							{
								ReworkProduct reworkProduct = ExtendedObjectProxy.getReworkProductService().selectByKey(false, 
										new Object[]{productName,lotname,factoryName,beforeProcessFlowName,
										beforeProcessFlowVersion,beforeProcessOperationName,beforeProcessOperationVersion,toProcessFlowName,"00001",toProcessOperationName,"00001"});

								reworkProduct.setCurrentCount(reworkProduct.getCurrentCount() + 1);
								reworkProduct.setReworkCount(limitCount);
								reworkProduct.setReasonCode(eventInfo.getReasonCode());
								reworkProduct.setReworkDepartmentName(departmentName);
								reworkProduct.setLastEventUser(eventInfo.getEventUser());
								reworkProduct.setLastEventComment(eventInfo.getEventComment());
								reworkProduct.setLastEventTime(eventInfo.getEventTime());
								reworkProduct.setLastEventTimeKey(eventInfo.getEventTimeKey());
								reworkProduct.setLastEventName(eventInfo.getEventName());
								reworkProduct.setOriginalProductGrade(product.getProductGrade());
								
								ExtendedObjectProxy.getReworkProductService().modify(eventInfo, reworkProduct);
							}
							catch(Exception ex)
							{
								ReworkProduct reworkProduct = new ReworkProduct();
								reworkProduct.setProductName(productName);
								reworkProduct.setLotname(lotname);
								reworkProduct.setFactoryName(factoryName);
								reworkProduct.setProcessflowName(beforeProcessFlowName);
								reworkProduct.setProcessflowVersion(beforeProcessFlowVersion);
								reworkProduct.setProcessOperationName(beforeProcessOperationName);
								reworkProduct.setProcessOperationVersion(beforeProcessOperationVersion);
								reworkProduct.setReworkProcessflowName(toProcessFlowName);
								reworkProduct.setReworkProcessflowVersion("00001");
								reworkProduct.setReworkProcessOperationName(toProcessOperationName);
								reworkProduct.setReworkProcessOperationVersion("00001");
								reworkProduct.setReworkCount(limitCount);
								reworkProduct.setCurrentCount(1);
								reworkProduct.setReasonCode(eventInfo.getReasonCode());
								reworkProduct.setReworkDepartmentName(departmentName);
								reworkProduct.setOriginalProductGrade(product.getProductGrade());
								reworkProduct.setLastEventUser(eventInfo.getEventUser());
								reworkProduct.setLastEventName(eventInfo.getEventName());;
								reworkProduct.setLastEventTime(eventInfo.getEventTime());
								reworkProduct.setLastEventTimeKey(eventInfo.getEventTimeKey());
								reworkProduct.setLastEventComment(eventInfo.getEventComment());

								ExtendedObjectProxy.getReworkProductService().create(eventInfo, reworkProduct);
							}
						}
					}
				}
				catch(Exception ex)
				{
					logger.info("ReworkCount is 0 or null.");
				}
			}
			else
			{
				throw new CustomException("RW-0002");
			}
		}
		
		
		
		/*
		* Name : increaseProductReworkCount
		* Desc : This function is increaseProductReworkCount
		* Author : hsryu
		* Date : 2018.02.20
		*/ 
		public void decreaseReworkCount(List<Product> productList, Lot lotData, String rwFlow, String rwOper, String returnFlow, String returnOper, EventInfo eventInfo)
				throws CustomException
		{
 			ArrayList<String> arrPrdName = new ArrayList<String>();
			List<ReworkLot> reworkLotList = new ArrayList<ReworkLot>();
			List<ReworkProduct> reworkProductList = new ArrayList<ReworkProduct>();
			
			String lastReasonCode = "";
			String lastReworkDepartment = "";
			
			String condition = "WHERE lotName = ? AND (factoryName = ? OR factoryName = ?) AND (processFlowName = ? OR processFlowName = ?) AND (processFlowVersion = ? OR processFlowVersion = ?) "
					+ "AND (processOperationName = ? OR processOperationName = ?) "
					+ "AND (processOperationVersion = ? OR processOperationVersion = ?) AND reworkProcessFlowName = ? AND reworkProcessFlowVersion = ? AND reworkProcessOperationName = ? AND reworkProcessOperationVersion = ? "
					+ "ORDER BY DECODE (FACTORYNAME, ?, 9999, 0), DECODE (PROCESSFLOWNAME, ?, 9999, 0), DECODE (PROCESSFLOWVERSION, ?, 9999, 0), "
					+ "DECODE (PROCESSOPERATIONNAME, ?, 9999, 0), DECODE (PROCESSOPERATIONVERSION, ?, 9999, 0) ";
			
			Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), "*", returnFlow, "*", "00001", "*",
											returnOper, "*", "00001", "*", rwFlow, "00001", rwOper, "00001", "*", "*", "*", "*", "*"};
			
			try
			{
				ReworkLot reworkLot = new ReworkLot();
				reworkLotList = ExtendedObjectProxy.getreworkLotService().select(condition, bindSet);
				
				reworkLot = reworkLotList.get(0);
				
				if(reworkLot.getCurrentCount()==1)
				{
					eventInfo.setEventName("DeleteReworkLotCount");
					ExtendedObjectProxy.getreworkLotService().remove(eventInfo, reworkLot);
				}
				else if(reworkLot.getCurrentCount()>1)
				{
					
					String histStrSql = "SELECT REASONCODE, " +
							"         REWORKDEPARTMENTNAME " +
							"    FROM CT_REWORKLOTHIST " +
							"   WHERE     LOTNAME = :LOTNAME " +
							"         AND FACTORYNAME = :FACTORYNAME " +
							"         AND PROCESSFLOWNAME = :PROCESSFLOWNAME " +
							"         AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
							"         AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
							"         AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
							"         AND REWORKPROCESSFLOWNAME = :REWORKPROCESSFLOWNAME " +
							"         AND REWORKPROCESSFLOWVERSION = :REWORKPROCESSFLOWVERSION " +
							"         AND REWORKPROCESSOPERATIONNAME = :REWORKPROCESSOPERATIONNAME " +
							"         AND REWORKPROCESSOPERATIONVERSION = :REWORKPROCESSOPERATIONVERSION " +
							" ORDER BY TIMEKEY DESC ";
					
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("LOTNAME", lotData.getKey().getLotName());
					bindMap.put("FACTORYNAME", lotData.getFactoryName());
					bindMap.put("PROCESSFLOWNAME", returnFlow);
					bindMap.put("PROCESSFLOWVERSION", "00001");
					bindMap.put("PROCESSOPERATIONNAME", returnOper);
					bindMap.put("PROCESSOPERATIONVERSION", "00001");
					bindMap.put("REWORKPROCESSFLOWNAME", rwFlow);
					bindMap.put("REWORKPROCESSFLOWVERSION", "00001");
					bindMap.put("REWORKPROCESSOPERATIONNAME", rwOper);
					bindMap.put("REWORKPROCESSOPERATIONVERSION", "00001");

					Object[] histBindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), returnFlow, "00001",
							returnOper, "00001", rwFlow, "00001", rwOper, "00001" };
					
					List<Map<String, Object>> reworkLotHist = GenericServiceProxy.getSqlMesTemplate().queryForList(histStrSql, histBindSet);
					
					if(reworkLotHist.size()>1)
					{
						lastReasonCode = reworkLotHist.get(1).get("REASONCODE").toString();
						lastReworkDepartment = reworkLotHist.get(1).get("REWORKDEPARTMENTNAME").toString();
					}
					
					eventInfo.setEventName("DecreaseReworkLotCount");
					reworkLot.setCurrentCount(reworkLot.getCurrentCount()-1);
					reworkLot.setReasonCode(lastReasonCode);
					reworkLot.setReworkDepartmentName(lastReworkDepartment);
					reworkLot.setLastEventUser(eventInfo.getEventUser());
					reworkLot.setLastEventTime(eventInfo.getEventTime());
					reworkLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
					reworkLot.setLastEventName(eventInfo.getEventName());
					reworkLot.setLastEventComment(eventInfo.getEventComment());
					ExtendedObjectProxy.getreworkLotService().modify(eventInfo, reworkLot);
					
				}
			}
			catch(Exception ex)
			{
				//2018.11.12_hsryu_Delete Logic For ChangeGrade S->G, ReturnFlow = 'Rework'
				//throw new CustomException("REWORK-0001",lotData.getKey().getLotName()); 
				logger.warn("ReworkLotData[" +lotData.getKey().getLotName() + "] is not exist.");

			}
			
			for(Product product : productList)
			{
				
				String pCondition = "WHERE productName = ? AND lotName = ? AND (factoryName = ? OR factoryName = ?) AND (processFlowName = ? OR processFlowName = ?) AND (processFlowVersion = ? OR processFlowVersion = ?) "
						+ "AND (processOperationName = ? OR processOperationName = ?) "
						+ "AND (processOperationVersion = ? OR processOperationVersion = ?) AND reworkProcessFlowName = ? AND reworkProcessFlowVersion = ? AND reworkProcessOperationName = ? AND reworkProcessOperationVersion = ? "
						+ "ORDER BY DECODE (FACTORYNAME, ?, 9999, 0), DECODE (PROCESSFLOWNAME, ?, 9999, 0), DECODE (PROCESSFLOWVERSION, ?, 9999, 0),"
						+ "DECODE (PROCESSOPERATIONNAME, ?, 9999, 0), DECODE (PROCESSOPERATIONVERSION, ?, 9999, 0)";

				Object[] pBindSet = new Object[]{product.getKey().getProductName(), lotData.getKey().getLotName(), lotData.getFactoryName(), "*", returnFlow, "*", "00001", "*",
												returnOper, "*", "00001", "*", rwFlow, "00001", rwOper, "00001", "*", "*", "*", "*", "*"};
				
				try
				{
					ReworkProduct reworkProduct = new ReworkProduct();
					reworkProductList = ExtendedObjectProxy.getReworkProductService().select(pCondition, pBindSet);
					
					reworkProduct = reworkProductList.get(0);
					
					if(reworkProduct.getCurrentCount()==1)
					{
						eventInfo.setEventName("DeleteReworkProductCount");
						ExtendedObjectProxy.getReworkProductService().remove(eventInfo, reworkProduct);
					}
					else if(reworkProduct.getCurrentCount()>1)
					{
						eventInfo.setEventName("DecreaseReworkProductCount");
						reworkProduct.setCurrentCount(reworkProduct.getCurrentCount()-1);
						reworkProduct.setLastEventUser(eventInfo.getEventUser());
						reworkProduct.setLastEventTime(eventInfo.getEventTime());
						reworkProduct.setLastEventTimeKey(eventInfo.getEventTimeKey());
						reworkProduct.setLastEventName(eventInfo.getEventName());
						reworkProduct.setLastEventComment(eventInfo.getEventComment());
						ExtendedObjectProxy.getReworkProductService().modify(eventInfo, reworkProduct);
					}
				}
				catch(Exception ex)
				{
					//2018.11.12_hsryu_Delete Logic For ChangeGrade S->G, ReturnFlow = 'Rework'
					//throw new CustomException("REWORK-0002",product.getKey().getProductName()); 
					logger.warn("ReworkProduct[" +product.getKey().getProductName() + "] is not exist.");
				}
			}
		}	
}
