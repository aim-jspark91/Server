package kr.co.aim.messolution.lot.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class MQCLotServiceUtil implements ApplicationContextAware {
	private static  Log log = LogFactory.getLog(MQCLotServiceUtil.class);
	
	public MQCLotServiceUtil() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		
	}
	

	/**
	 * ProcessingMQC
	 * MQC
	 * @author hwlee89
	 * @since 2016.11.23
	 * @param 
	 * @param 
	 * @return
	 * @throws CustomException
	 */
	/*public void processMQC(EventInfo eventInfo, Lot trackOutLot, String machineName, List<Element> productList) throws CustomException
	{
		if(trackOutLot.getProductionType().equals("D"))
		{
			List<MQCPlan> mqcPlanList = ExtendedObjectProxy.getMQCPlanService().getMQCPlanList(trackOutLot);
			
			if(mqcPlanList != null && mqcPlanList.size() > 0)
			{
				ProcessOperationSpec mqcOperationData = CommonUtil.getProcessOperationSpec(trackOutLot.getFactoryName(), trackOutLot.getProcessOperationName());
				
				// 2. DummyUsedCount ++
				if(mqcOperationData.getProcessOperationType().equals("Production"))
				{
					MESLotServiceProxy.getLotServiceUtil().dummyUsedCounting(eventInfo, trackOutLot);
				}
				
				ExtendedObjectProxy.getMQCLotService().completedMQCLot(eventInfo, trackOutLot, trackOutLot.getProcessOperationName(), mqcPlanList.get(0).getJobName(), machineName);
				
				ExtendedObjectProxy.getMQCProductService().insertMQCProduct(eventInfo, trackOutLot, mqcPlanList.get(0).getJobName(), machineName, productList);
				
				ExtendedObjectProxy.getMQCPolicyService().removeMqcPOSProduct(eventInfo, trackOutLot, machineName);
			}
		}
	}
	
	*//**
	 * MQCforPU
	 * MQCforPU
	 * @author hwlee89
	 * @since 2016.11.23
	 * @param 
	 * @param 
	 * @return
	 * @throws CustomException
	 *//*
	public void MQCforPU(EventInfo eventInfo, Lot trackOutLot, List<Element> productList,  String machineName, Port portData, String carrierName) throws CustomException
	{
		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
		{
			Lot MLot = null;
			if(trackOutLot.getSourceLotName() != null && !trackOutLot.getSourceLotName().isEmpty())
			{
				MLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getSourceLotName());
			}
			else
			{
				return;
			}
			
			if(MLot.getDestinationLotName() == null || MLot.getDestinationLotName().isEmpty())
			{
				return;
			}
			
			if(this.checkMergeLot(eventInfo, trackOutLot) == true)
			{
				return;
			}
			
			if( StringUtil.equals(MLot.getKey().getLotName(), trackOutLot.getSourceLotName()) && StringUtil.equals(MLot.getDestinationLotName(), trackOutLot.getKey().getLotName()) )
			{
				// Get Mother MQCPlan
				List<MQCPlan> mqcPlanList = ExtendedObjectProxy.getMQCPlanService().getMQCPlanList(MLot);
				
				if(mqcPlanList == null)
				{
					return;
				}
				
				// For New Job 
				MESLotServiceProxy.getMQCLotServiceUtil().processMQC(eventInfo, MLot, machineName, productList);
				
				// Create Child MQCPlan
				String newJobName = "MQC-" + trackOutLot.getKey().getLotName() + "-" + TimeUtils.getCurrentEventTimeKey();
				ExtendedObjectProxy.getMQCPlanService().createMQCPlan(eventInfo, newJobName, 0, mqcPlanList.get(0).getFactoryName(), mqcPlanList.get(0).getProductSpecName(),
						mqcPlanList.get(0).getProcessFlowName(), trackOutLot.getKey().getLotName(), "Processing", "", 0, 0, "", "", carrierName, productList);
				
				// Get Mother MQCLot
				List<MQCLot> mqcLotList = ExtendedObjectProxy.getMQCLotService().getCreatedMQCLot(MLot); 
				
				for(MQCLot mqcLotData : mqcLotList)
				{
					boolean chk = false;
					// Get Mother MQCPOSProduct
					List<MQCPOSProduct> mqcPOSProductList = null;
					mqcPOSProductList = ExtendedObjectProxy.getMQCPolicyService().getMqcPOSProduct(mqcLotData.getLotName(), mqcLotData.getFactoryName(),
							mqcLotData.getProcessOperationName(), mqcLotData.getProductSpecName(), mqcLotData.getProcessFlowName(), mqcLotData.getMachineName());
					
					if(mqcPOSProductList != null)
					{
						for(MQCPOSProduct mqcPOSProductData : mqcPOSProductList)
						{
							for(Element eleProduct : productList)
							{
								String productName =  SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
								if(StringUtil.equals(productName, mqcPOSProductData.getProductName()))
								{
									// Create Child MQCPOSProduct
									ExtendedObjectProxy.getMQCPolicyService().createMqcPOSProduct(eventInfo, trackOutLot.getKey().getLotName(), mqcPOSProductData.getProductName(),
											mqcPOSProductData.getFactoryName(), mqcPOSProductData.getProductSpecName(), mqcPOSProductData.getProcessFlowName(),
											mqcPOSProductData.getProcessOperationName(), mqcPOSProductData.getMachineName(), mqcPOSProductData.getRecipeName());
									
									// Remove Mother MQCPOSProduct
									ExtendedObjectProxy.getMQCPolicyService().delete(mqcPOSProductData);									
									chk = true;
									break;
								}
							}
						}
						
						if(chk == true)
						{
							// Create Child MQCLot For POSProduct
							ExtendedObjectProxy.getMQCLotService().createdMQCLot(eventInfo, newJobName, mqcLotData.getFactoryName(), mqcLotData.getProductSpecName(), mqcLotData.getProcessFlowName(),
									mqcLotData.getProcessOperationName(), trackOutLot.getKey().getLotName(), mqcLotData.getMachineName(), mqcLotData.getRecipeName(), "Created", carrierName);
							
							// Get Mother MQCPOSProduct
							List<MQCPOSProduct> chkmqcPOSProductList = null;
							chkmqcPOSProductList = ExtendedObjectProxy.getMQCPolicyService().getMqcPOSProduct(mqcLotData.getLotName(), mqcLotData.getFactoryName(),
									mqcLotData.getProcessOperationName(), mqcLotData.getProductSpecName(), mqcLotData.getProcessFlowName(), mqcLotData.getMachineName());
							
							if(chkmqcPOSProductList == null)
							{
								// Update Mother MQCLot
								mqcLotData.setJobState("Completed");
								ExtendedObjectProxy.getMQCLotService().update(mqcLotData);
							}
							else
							{
								//
							}
							
						}
					}
					
					if(mqcPOSProductList == null)
					{
						// Create Child MQCLot For AllProduct
						ExtendedObjectProxy.getMQCLotService().createdMQCLot(eventInfo, newJobName, mqcLotData.getFactoryName(), mqcLotData.getProductSpecName(), mqcLotData.getProcessFlowName(),
								mqcLotData.getProcessOperationName(), trackOutLot.getKey().getLotName(), mqcLotData.getMachineName(), mqcLotData.getRecipeName(), "Created", carrierName);
						
						// Update Mother MQCLot For AllProduct
//						mqcLotData.setJobState("Changed");
//						ExtendedObjectProxy.getMQCLotService().update(mqcLotData);
					}
					
				}
				
				// 추후 삭제 가능
				// Pause MQC MotherLot No exist MQCJob in currentOper
				
				if(StringUtil.equals(MLot.getLotProcessState(), "RUN"))
				{
					List<Element> mProductList = new ArrayList<Element>();
					List<Product> pProductList =  ProductServiceProxy.getProductService().allUnScrappedProductsByLot(MLot.getKey().getLotName());
					
					for(int i = 0;i<pProductList.size();i++)
					{
						Element productElement = new Element("PRODUCT");
						
						Element productNameElement = new Element("PRODUCTNAME");
						productNameElement.setText(pProductList.get(i).getKey().getProductName());
						productElement.addContent(productNameElement);
						
						Element positionElement = new Element("POSITION");
						positionElement.setText(Long.toString(pProductList.get(i).getPosition()));
						productElement.addContent(positionElement);
						
						mProductList.add(productElement);
					}
					
					List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(mProductList);
					
					eventInfo.setEventName("CancelTrackIn");
					MLot = MESLotServiceProxy.getLotServiceUtil().cancelTrackIn(eventInfo, MLot, null, "", "", "", productPGSRCSequence, new HashMap<String, String>(), new HashMap<String,String>());
				}
				
				List<MQCLot> mMQCLotList = null;
				try
				{
					String condition = " WHERE 1=1 AND mqcJobName = ? AND processOperationName = ? AND lotName = ? AND jobState = 'Created' ";
					Object[] bindSet = new Object[]{mqcPlanList.get(0).getJobName(), MLot.getProcessOperationName(), MLot.getKey().getLotName()};
					
					mMQCLotList = ExtendedObjectProxy.getMQCLotService().select(condition, bindSet);
				}
				catch(Exception ex)
				{
					
				}
				
				if(mMQCLotList == null)
				{
					MESLotServiceProxy.getMQCLotServiceUtil().pauseMQC(eventInfo, MLot);
					
					mqcPlanList.get(0).setMQCState("Completed");
					
					eventInfo.setEventName("Pause");
					ExtendedObjectProxy.getMQCPlanService().modify(eventInfo, mqcPlanList.get(0));
					
					this.deleteSampleData(MLot);
					ExtendedObjectProxy.getMQCLotService().completedMQCLot(eventInfo, MLot, mqcPlanList.get(0).getJobName());
					ExtendedObjectProxy.getMQCPolicyService().removeAllMqcPOSProduct(eventInfo, MLot);
					
					ProductRequestKey pKey = new ProductRequestKey();
					pKey.setProductRequestName(MLot.getProductRequestName());
					ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
					
					String productSpecName = productRequestData.getProductSpecName();
					ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(MLot.getFactoryName(), productSpecName, "00001");
					
					List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(MLot.getKey().getLotName());
					for(ProductU productU : productUdfs)
					{
						String dummyUsedCount = productU.getUdfs().get("DUMMYUSEDCOUNT");
						if(dummyUsedCount.isEmpty() || Integer.parseInt(dummyUsedCount) < 1)
						{
							dummyUsedCount = "0";
						}
						Map<String, String> udfs = productU.getUdfs();
						udfs.put("DUMMYUSEDCOUNT", String.valueOf(Integer.parseInt(dummyUsedCount)+1));
						productU.setUdfs(udfs);
					}
					
					ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(MLot.getKey().getLotName(),
							MLot.getProductionType(), productSpecData.getKey().getProductSpecName(), productSpecData.getKey().getProductSpecVersion(), MLot.getProductSpec2Name(),
							MLot.getProductSpec2Version(), MLot.getProductRequestName(), MLot.getSubProductUnitQuantity1(), MLot.getSubProductQuantity2(), MLot.getDueDate(), MLot.getPriority(),
							MLot.getFactoryName(), MLot.getAreaName(), MLot.getLotState(), MLot.getLotProcessState(), MLot.getLotHoldState(),
							MLot.getProcessFlowName(), MLot.getProcessFlowVersion(), MLot.getProcessOperationName(), MLot.getProcessOperationVersion(),
							CommonUtil.getValue(MLot.getUdfs(), "RETURNFLOWNAME"), CommonUtil.getValue(MLot.getUdfs(), "RETURNOPERATIONNAME"),
							"", "", "",
							MLot.getUdfs(), productUdfs,
							true);
					
//					eventInfo.setEventName("ChangeOper");
					eventInfo.setEventName("SpePauseMQCJob");
					MLot = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, MLot, changeSpecInfo);
					
				}
			}
		}
	}
	
	*//**
	 * MQCforPU
	 * MQCforPU
	 * @author hwlee89
	 * @since 2016.11.23
	 * @param 
	 * @param 
	 * @return
	 * @throws CustomException
	 *//*
	public void MQCforPUEnd(EventInfo eventInfo, Lot trackOutLot, List<Element> productList,  String machineName, Port portData, String carrierName) throws CustomException
	{
		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
		{
			Lot MLot = null;
			if(trackOutLot.getSourceLotName() != null && !trackOutLot.getSourceLotName().isEmpty())
			{
				MLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getSourceLotName());
			}
			else
			{
				return;
			}
			
			if(MLot.getDestinationLotName() == null || MLot.getDestinationLotName().isEmpty())
			{
				return;
			}
			
			if(this.checkMergeLot(eventInfo, trackOutLot) == true)
			{
				return;
			}
			
			if( StringUtil.equals(MLot.getKey().getLotName(), trackOutLot.getSourceLotName()) && StringUtil.equals(MLot.getDestinationLotName(), trackOutLot.getKey().getLotName()) )
			{
				// Get Mother MQCPlan
				List<MQCPlan> mqcPlanList = ExtendedObjectProxy.getMQCPlanService().getMQCPlanList(MLot);
				
				if(mqcPlanList == null)
				{
					return;
				}
				
				// For New Job 
				MESLotServiceProxy.getMQCLotServiceUtil().processMQC(eventInfo, MLot, machineName, productList);
				
				// Create Child MQCPlan
				String newJobName = "MQC-" + trackOutLot.getKey().getLotName() + "-" + TimeUtils.getCurrentEventTimeKey();
				ExtendedObjectProxy.getMQCPlanService().createMQCPlan(eventInfo, newJobName, 0, mqcPlanList.get(0).getFactoryName(), mqcPlanList.get(0).getProductSpecName(),
						mqcPlanList.get(0).getProcessFlowName(), trackOutLot.getKey().getLotName(), "Processing", "", 0, 0, "", "", carrierName, productList);
				
				// Get Mother MQCLot
				List<MQCLot> mqcLotList = ExtendedObjectProxy.getMQCLotService().getCreatedMQCLot(MLot);
				
				for(MQCLot mqcLotData : mqcLotList)
				{
					boolean chk = false;
					// Get Mother MQCPOSProduct
					List<MQCPOSProduct> mqcPOSProductList = null;
					mqcPOSProductList = ExtendedObjectProxy.getMQCPolicyService().getMqcPOSProduct(mqcLotData.getLotName(), mqcLotData.getFactoryName(),
							mqcLotData.getProcessOperationName(), mqcLotData.getProductSpecName(), mqcLotData.getProcessFlowName(), mqcLotData.getMachineName());
					
					if(mqcPOSProductList != null)
					{
						for(MQCPOSProduct mqcPOSProductData : mqcPOSProductList)
						{
							for(Element eleProduct : productList)
							{
								String productName =  SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
								if(StringUtil.equals(productName, mqcPOSProductData.getProductName()))
								{
									// Create Child MQCPOSProduct
									ExtendedObjectProxy.getMQCPolicyService().createMqcPOSProduct(eventInfo, trackOutLot.getKey().getLotName(), mqcPOSProductData.getProductName(),
											mqcPOSProductData.getFactoryName(), mqcPOSProductData.getProductSpecName(), mqcPOSProductData.getProcessFlowName(),
											mqcPOSProductData.getProcessOperationName(), mqcPOSProductData.getMachineName(), mqcPOSProductData.getRecipeName());
									
									// Remove Mother MQCPOSProduct
									ExtendedObjectProxy.getMQCPolicyService().delete(mqcPOSProductData);									
									chk = true;
									break;
								}
							}
						}
						
						if(chk == true)
						{
							// Create Child MQCLot For POSProduct
							ExtendedObjectProxy.getMQCLotService().createdMQCLot(eventInfo, newJobName, mqcLotData.getFactoryName(), mqcLotData.getProductSpecName(), mqcLotData.getProcessFlowName(),
									mqcLotData.getProcessOperationName(), trackOutLot.getKey().getLotName(), mqcLotData.getMachineName(), mqcLotData.getRecipeName(), "Created", carrierName);
							
							// Get Mother MQCPOSProduct
							List<MQCPOSProduct> chkmqcPOSProductList = null;
							chkmqcPOSProductList = ExtendedObjectProxy.getMQCPolicyService().getMqcPOSProduct(mqcLotData.getLotName(), mqcLotData.getFactoryName(),
									mqcLotData.getProcessOperationName(), mqcLotData.getProductSpecName(), mqcLotData.getProcessFlowName(), mqcLotData.getMachineName());
							
							if(chkmqcPOSProductList == null)
							{
								// Update Mother MQCLot
								mqcLotData.setJobState("Completed");
								ExtendedObjectProxy.getMQCLotService().update(mqcLotData);
							}
							else
							{
								//
							}
							
						}
					}
					
					if(mqcPOSProductList == null)
					{
						// Create Child MQCLot For AllProduct
						ExtendedObjectProxy.getMQCLotService().createdMQCLot(eventInfo, newJobName, mqcLotData.getFactoryName(), mqcLotData.getProductSpecName(), mqcLotData.getProcessFlowName(),
								mqcLotData.getProcessOperationName(), trackOutLot.getKey().getLotName(), mqcLotData.getMachineName(), mqcLotData.getRecipeName(), "Created", carrierName);
						
						// Update Mother MQCLot For AllProduct
//						mqcLotData.setJobState("Changed");
//						ExtendedObjectProxy.getMQCLotService().update(mqcLotData);
					}
					
				}
				
				// 추후 삭제 가능
				// Pause MQC MotherLot No exist MQCJob in currentOper				
				List<MQCLot> mMQCLotList = null;
				try
				{
					String condition = " WHERE 1=1 AND mqcJobName = ? AND processOperationName = ? AND lotName = ? AND jobState = 'Created' ";
					Object[] bindSet = new Object[]{mqcPlanList.get(0).getJobName(), MLot.getProcessOperationName(), MLot.getKey().getLotName()};
					
					mMQCLotList = ExtendedObjectProxy.getMQCLotService().select(condition, bindSet);
				}
				catch(Exception ex)
				{
					
				}
				
				if(mMQCLotList == null)
				{
					MESLotServiceProxy.getMQCLotServiceUtil().pauseMQC(eventInfo, MLot);
					
					
					mqcPlanList.get(0).setMQCState("Completed");
					
					eventInfo.setEventName("Pause");
					ExtendedObjectProxy.getMQCPlanService().modify(eventInfo, mqcPlanList.get(0));
					
					this.deleteSampleData(MLot);
					ExtendedObjectProxy.getMQCLotService().completedMQCLot(eventInfo, MLot, mqcPlanList.get(0).getJobName());
					ExtendedObjectProxy.getMQCPolicyService().removeAllMqcPOSProduct(eventInfo, MLot);
					
					ProductRequestKey pKey = new ProductRequestKey();
					pKey.setProductRequestName(MLot.getProductRequestName());
					ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
					
					String productSpecName = productRequestData.getProductSpecName();
					ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(MLot.getFactoryName(), productSpecName, "00001");
					
					List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(MLot.getKey().getLotName());
					for(ProductU productU : productUdfs)
					{
						String dummyUsedCount = productU.getUdfs().get("DUMMYUSEDCOUNT");
						if(dummyUsedCount.isEmpty() || Integer.parseInt(dummyUsedCount) < 1)
						{
							dummyUsedCount = "0";
						}
						Map<String, String> udfs = productU.getUdfs();
						udfs.put("DUMMYUSEDCOUNT", String.valueOf(Integer.parseInt(dummyUsedCount)+1));
						productU.setUdfs(udfs);
					}
					
					ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(MLot.getKey().getLotName(),
							MLot.getProductionType(), productSpecData.getKey().getProductSpecName(), productSpecData.getKey().getProductSpecVersion(), MLot.getProductSpec2Name(),
							MLot.getProductSpec2Version(), MLot.getProductRequestName(), MLot.getSubProductUnitQuantity1(), MLot.getSubProductQuantity2(), MLot.getDueDate(), MLot.getPriority(),
							MLot.getFactoryName(), MLot.getAreaName(), MLot.getLotState(), MLot.getLotProcessState(), MLot.getLotHoldState(),
							MLot.getProcessFlowName(), MLot.getProcessFlowVersion(), MLot.getProcessOperationName(), MLot.getProcessOperationVersion(),
							CommonUtil.getValue(MLot.getUdfs(), "RETURNFLOWNAME"), CommonUtil.getValue(MLot.getUdfs(), "RETURNOPERATIONNAME"),
							"", "", "",
							MLot.getUdfs(), productUdfs,
							true);
					
					eventInfo.setEventName("PauseMQCJob");
					MLot = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, MLot, changeSpecInfo);
					
				}
			}
		}
	}	
	
	public void deleteSampleData(Lot lotData) throws CustomException
	{
		try
		{
			String condition = "WHERE 1=1 AND lotName = ? AND factoryName = ? AND productSpecName = ? AND processflowName = ? ";
			Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName()};
			List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
			
			for(SampleProduct sampleProductData : sampleProductList)
			{
				ExtendedObjectProxy.getSampleProductService().delete(sampleProductData);
			}		
		}
		catch(Exception ex)
		{
			
		}
		
		try
		{
			String condition = "WHERE 1=1 AND lotName = ? AND factoryName = ? AND productSpecName = ? AND processFlowName = ? ";
			Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName()};
			List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
			
			for(SampleLot sampleLotData : sampleLotList)
			{
				ExtendedObjectProxy.getSampleLotService().delete(sampleLotData);
			}
		}
		catch(Exception ex)
		{
			
		}
	}

	public boolean checkMergeLot(EventInfo eventInfo, Lot trackOutLot)
		throws CustomException
	{
		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(trackOutLot);
		
		List<Lot> lotList = null;
		try
		{
			String condition = " WHERE 1=1 AND DestinationLotName = ? ";
			Object[] bindSet = new Object[]{trackOutLot.getKey().getLotName()};
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			
		}
		
		if(lotList != null && lotList.size() > 1)
		{
			if(StringUtil.equals(processFlowData.getProcessFlowType(), "MQC"))
			{
				// Create Child MQCPlan
				eventInfo.setEventName("MQCMerged");
				String newJobName = "MQC-" + trackOutLot.getKey().getLotName() + "-" + TimeUtils.getCurrentEventTimeKey();
				ExtendedObjectProxy.getMQCPlanService().createMQCPlan(eventInfo, newJobName, 0, trackOutLot.getFactoryName(), trackOutLot.getProductSpecName(),
						trackOutLot.getProcessFlowName(), trackOutLot.getKey().getLotName(), "Processing", "", 0, 0, "", "", "", new ArrayList<Element>());
				
				return true;
			}
		}

		return false;
	}
	
	public Lot CancelTrackInForMQC(EventInfo eventInfo, Lot lotData)
		throws CustomException
	{
		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		
		if(StringUtil.equals(processFlowData.getProcessFlowType(), "MQC"))
		{
			ProductRequestKey pKey = new ProductRequestKey();
			pKey.setProductRequestName(lotData.getProductRequestName());
			ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
			
			String productSpecName = productRequestData.getProductSpecName();
			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), productSpecName, "00001");
			
			List<MQCPlan> CMqcPlanList = ExtendedObjectProxy.getMQCPlanService().getMQCPlanList(lotData);
			
			if(CMqcPlanList == null || CMqcPlanList.size() < 1)
			{				
				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());
				
				ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
						lotData.getProductionType(), productSpecData.getKey().getProductSpecName(), productSpecData.getKey().getProductSpecVersion(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
						lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
						lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
						lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						CommonUtil.getValue(lotData.getUdfs(), "RETURNFLOWNAME"), CommonUtil.getValue(lotData.getUdfs(), "RETURNOPERATIONNAME"),
						"", "", "",
						lotData.getUdfs(), productUdfs,
						true);
				
				eventInfo.setEventName("PauseMQCJob");
				lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			}
		}
		
		return lotData;
	}
	
	public Lot pauseMQC(EventInfo eventInfo, Lot lotData)
		throws CustomException
	{
		List<MQCPlan> mqcPlanList = ExtendedObjectProxy.getMQCPlanService().getMQCPlanList(lotData);
		
		if(mqcPlanList != null && mqcPlanList.size() > 0)
		{
			mqcPlanList.get(0).setMQCState("Completed");
			
			eventInfo.setEventName("Pause");
			ExtendedObjectProxy.getMQCPlanService().modify(eventInfo, mqcPlanList.get(0));
			
			this.deleteSampleData(lotData);
			
			ExtendedObjectProxy.getMQCLotService().completedMQCLot(eventInfo, lotData, mqcPlanList.get(0).getJobName());
			
			ExtendedObjectProxy.getMQCPolicyService().removeAllMqcPOSProduct(eventInfo, lotData);
			
			ProductRequestKey pKey = new ProductRequestKey();
			pKey.setProductRequestName(lotData.getProductRequestName());
			ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
			
			String productSpecName = productRequestData.getProductSpecName();
			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), productSpecName, "00001");
			
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());
			for(ProductU productU : productUdfs)
			{
				String dummyUsedCount = productU.getUdfs().get("DUMMYUSEDCOUNT");
				if(dummyUsedCount.isEmpty() || Integer.parseInt(dummyUsedCount) < 1)
				{
					dummyUsedCount = "0";
				}
				Map<String, String> udfs = productU.getUdfs();
				udfs.put("DUMMYUSEDCOUNT", String.valueOf(Integer.parseInt(dummyUsedCount)+1));
				productU.setUdfs(udfs);
			}
			
			ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
					lotData.getProductionType(), productSpecData.getKey().getProductSpecName(), productSpecData.getKey().getProductSpecVersion(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
					lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
					lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
					lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
					CommonUtil.getValue(lotData.getUdfs(), "RETURNFLOWNAME"), CommonUtil.getValue(lotData.getUdfs(), "RETURNOPERATIONNAME"),
					"", "", "",
					lotData.getUdfs(), productUdfs,
					true);

			eventInfo.setEventName("PauseMQCJob");
			lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		}*/
		
		//return lotData;
	//}
}
