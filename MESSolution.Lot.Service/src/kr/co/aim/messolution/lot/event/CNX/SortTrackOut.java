package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class SortTrackOut extends SyncHandler{
	@SuppressWarnings("unchecked")
	@Override
	public Object doWorks(Document doc)throws CustomException
	{ 
		//ChangeOutTrackOut ( Source )
		//ChangeInTrackOut
		String PQuantity = SMessageUtil.getBodyItemValue(doc, "PRODUCTQUANTITY", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String CarrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String JobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME",true);		
		String JobType = SMessageUtil.getBodyItemValue(doc, "JOBTYPE",true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		String sortTransferDirection = SMessageUtil.getBodyItemValue(doc, "TRANSFERDIRECTION",false);
		String lotName = "";
		String checklot = "FALSE";
		String eventname = "";
		String Sortflow = SMessageUtil.getBodyItemValue(doc, "SORTFLOW", true);
		String Beforeflowname = ""; // EndBank
		String MQCScrapFlag = "";
		
   		boolean aHoldFlag = false;

		String DetailJobType = SMessageUtil.getBodyItemValue(doc, "DETAILJOBTYPE", true);
		
//		int CompareComplete = Integer.parseInt(SMessageUtil.getBodyItemValue(doc, "COMPARECOMPLETE",true));
//		int CompareTotalCST = Integer.parseInt(SMessageUtil.getBodyItemValue(doc, "COMPARETOTALCST",true));
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(eventname, getEventUser(), getEventComment(), null, null);
	
		boolean Check_S_Grade_Product = true;
		
	    // 2018.10.13
		// To change lot history data same with PEX logic , all of logics have been changed 
		Lot trackOutLot = null;
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(CarrierName);
		if(StringUtil.equals(DetailJobType.toUpperCase() , "SPLIT"))
		{
			if(!StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE))
			{
				List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
				List<ProductP> productPSequence = new ArrayList<ProductP>();

				for (Element product : productList )
				{
					// 2019.02.02
					Product tempproductData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(SMessageUtil.getChildText(product, "PRODUCTNAME", false));		
					if(!StringUtil.equals(tempproductData.getProductProcessState(),"Processing"))
					{
						throw new CustomException("PRODUCT-9007");
					}
					
					String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
					String processingInfo = "P";
					String position = SMessageUtil.getChildText(product, "POSITION", false);

					if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
					{
						Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);

						if(StringUtil.isEmpty(lotName))
						{
							lotName = productData.getLotName();
						}

						ProductPGSRC productPGSRC = new ProductPGSRC();
						productPGSRC.setProductName(productName);
						productPGSRC.setPosition(Long.valueOf(position));

						String productGrade = SMessageUtil.getChildText(product, "PRODUCTJUDGE", false);
						if (StringUtil.isEmpty(productGrade))
						{
							productPGSRC.setProductGrade(productData.getProductGrade());
						}
						else
						{
							productPGSRC.setProductGrade(productGrade);
						}
						productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
						productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
						productPGSRC.setReworkFlag("N");

						//productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
						Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(product, "Product");
						productUdfs.put("PROCESSINGINFO", processingInfo);
						productPGSRC.setUdfs(productUdfs);
						productPGSRCSequence.add(productPGSRC);
						
						ProductP productPData = new ProductP();
						productPData.setProductName(productData.getKey().getProductName());

						productPData.setPosition(Long.valueOf(position));
	
						productPSequence.add(productPData);
						
						ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
						MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, JobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
					}
				}

				if(productPGSRCSequence.size() > 0)
				{
					Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
					
					trackOutLot = this.splitGlass(eventInfo, lotData, productPGSRCSequence, CarrierName);

                    this.copyFutureActionCallSplit(lotData, trackOutLot, eventInfo);
                    
					//MESLotServiceProxy.getLotServiceImpl().copyLotHoldCallSplitMerge(lotData, trackOutLot, eventInfo, 
					//        GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
					
					if(StringUtil.isEmpty(trackOutLot.getCarrierName()))
					{
						eventInfo.setEventName("AssignCarrier");
						eventInfo.setCheckTimekeyValidation(false);
						eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
						eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
						
						AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();
						assignCarrierInfo.setCarrierName(durableData.getKey().getDurableName());
						assignCarrierInfo.setProductPSequence(productPSequence);
						
						Map<String, String> assignCarrierUdfs = durableData.getUdfs();
						assignCarrierInfo.setAssignCarrierUdfs(assignCarrierUdfs);

						Map<String, String> lotUdfs = trackOutLot.getUdfs();
						assignCarrierInfo.setUdfs(lotUdfs);
						
						// Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
//						trackOutLot = LotServiceProxy.getLotService().assignCarrier(trackOutLot.getKey(), eventInfo, assignCarrierInfo);
						trackOutLot = MESLotServiceProxy.getLotServiceImpl().assignCarrier(trackOutLot, assignCarrierInfo, eventInfo);
					}
				}
			}
			else  
			{
				if ( productList.size() > 0)
				{
					// 2018.10.14 Because;Empty SourceCST Track-out;
				    List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(CarrierName,false);
				    trackOutLot = lotList.get(0);   			 
				}  
			}			
		}
		
		if(StringUtil.equals(DetailJobType.toUpperCase() , "MERGE"))
		{
			if(!StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE))
			{
				List<Product> productList2 = new ArrayList<Product>();
				String productNameList = "";
				List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
				List<ProductP> productPSequence = new ArrayList<ProductP>();

                String allGlasssScrapFlag = GenericServiceProxy.getConstantMap().Flag_Y;

				for (Element product : productList )
				{
					String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
					String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
					String position = SMessageUtil.getChildText(product, "POSITION", false);
					String productJudge = SMessageUtil.getChildText(product, "PRODUCTJUDGE", false);

					if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
					{
						if(StringUtil.isEmpty(productNameList))
							productNameList = "'" + productName + "'";
						else	
						    productNameList += ",'"+ productName + "'" ;
						
						MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, JobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);						
					}

					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
					Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productName));
					
					productData.setPosition(Long.valueOf(position));
					if(!StringUtil.isEmpty(productJudge))
					{
						productData.setProductGrade(productJudge);
					}
					Map<String,String> udfs = productData.getUdfs();
					udfs.put("PROCESSINGINFO", processingInfo);
					productData.setUdfs(udfs);
					ProductServiceProxy.getProductService().update(productData);
					
					ProductP productPData = new ProductP();
					productPData.setProductName(productData.getKey().getProductName());
					/* 20180809, Modify, Mismatch Slot Position ==>> */
					//productPData.setPosition(productData.getPosition());
					productPData.setPosition(Long.valueOf(position));
					/* <<== 20180809, Modify, Mismatch Slot Position */
					productPSequence.add(productPData);
					
					//ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
					ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
					
					/* 20180930, hhlee, add All Glass Scrap Check ==>> */
					if(!StringUtil.equals(productData.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_S) && 
					        StringUtil.equals(allGlasssScrapFlag, GenericServiceProxy.getConstantMap().Flag_Y))
					{
					    allGlasssScrapFlag = GenericServiceProxy.getConstantMap().Flag_N;
					}
					/* <<== 20180930, hhlee, add All Glass Scrap Check */
				}

				try
				{
					productList2.addAll(ProductServiceProxy.getProductService().select("WHERE PRODUCTNAME IN (" + productNameList + ")" + "ORDER BY LOTNAME" , null));
				}
				catch (Exception ex)
				{
					productList = null;
				}

				Lot targetLot = null;
				List<Lot> lotList = null;
				Lot lotData = null;
				
				if(productList2 != null && productList2.size() > 0) /* <<== 20180813, Add, NullValue Error */
                {
    				lotName = productList2.get(0).getLotName();
    				
    				// Added by smkang on 2018.12.20 - For synchronization of a carrier.
            		List<String> sourceCarrierNameList = new ArrayList<String>();
            		
    				//new add by wghuang 20180715
    				for(int i = 0; i < productList2.size(); i++ )
    				{				
    					if(StringUtil.equals(lotName,  productList2.get(i).getLotName()))
    					{
    						lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
    						
    						// Added by smkang on 2018.12.20 - For synchronization of a carrier.
                			if (StringUtils.isNotEmpty(lotData.getCarrierName()) && !sourceCarrierNameList.contains(lotData.getCarrierName()))
                				sourceCarrierNameList.add(lotData.getCarrierName());
    						
    						if(targetLot == null)
    						{
    							try
    							{
    								lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(CarrierName,false);
    							}
    							catch (Exception ex)
    							{
    								lotList = null;
    							}
    
    							if(lotList.size() == 0 )
    							{
    								eventInfo.setEventName("Create");
    								targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
    							}
    							else
    							{
    								targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
    							}			
    						}
    						
    						ProductPGSRC productPGSRC = new ProductPGSRC();
    						productPGSRC.setProductName(productList2.get(i).getKey().getProductName());
    						productPGSRC.setPosition(productList2.get(i).getPosition());
    						productPGSRC.setProductGrade(productList2.get(i).getProductGrade());
    						productPGSRC.setSubProductQuantity1(productList2.get(i).getSubProductQuantity1());
    						productPGSRC.setSubProductQuantity2(productList2.get(i).getSubProductQuantity2());
    						productPGSRC.setReworkFlag("N");
    						//productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
    						productPGSRCSequence.add(productPGSRC);
    						
    						if(i == productList2.size() -1)
    						{
    							trackOutLot = this.mergeGlass(eventInfo, lotData, productPGSRCSequence, CarrierName, targetLot, allGlasssScrapFlag);
    							/* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
    		                    //FutureAction
    		                    this.copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
    		                    /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
    						}
    					}
    					else
    					{
    						trackOutLot = this.mergeGlass(eventInfo, lotData, productPGSRCSequence, CarrierName, targetLot, allGlasssScrapFlag);
    						lotName = productList2.get(i).getLotName();
    						
    						/* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
    	                    //FutureAction
    	                    this.copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
    	                    /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
    						
    						//new
    						productPGSRCSequence = new ArrayList<ProductPGSRC>();
    						
    						ProductPGSRC productPGSRC = new ProductPGSRC();
    						productPGSRC.setProductName(productList2.get(i).getKey().getProductName());
    						productPGSRC.setPosition(productList2.get(i).getPosition());
    						productPGSRC.setProductGrade(productList2.get(i).getProductGrade());
    						productPGSRC.setSubProductQuantity1(productList2.get(i).getSubProductQuantity1());
    						productPGSRC.setSubProductQuantity2(productList2.get(i).getSubProductQuantity2());
    						productPGSRC.setReworkFlag("N");
    						//productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
    						productPGSRCSequence.add(productPGSRC);
    						
    						if(i == productList2.size() - 1)
    						{
                                /* 20180929, hhlee, add Get Lot Data ==>> */
                                lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
                                /* <<== 20180929, hhlee, add Get Lot Data */
    						    
    							trackOutLot = this.mergeGlass(eventInfo, lotData, productPGSRCSequence, CarrierName, targetLot, allGlasssScrapFlag);
    							
    							/* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
    		                    //FutureAction
    		                    this.copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
    		                    /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
    						}	
    					}
    				}
    				
    
    				if(productPGSRCSequence.size() > 0)
    				{	
    					if(StringUtil.isEmpty(trackOutLot.getCarrierName()))
    					{
    						eventInfo.setEventName("AssignCarrier");
    						eventInfo.setCheckTimekeyValidation(false);
    						eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
    						eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
    						
    						AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();
    						assignCarrierInfo.setCarrierName(durableData.getKey().getDurableName());
    						assignCarrierInfo.setProductPSequence(productPSequence);
    						
    						Map<String, String> assignCarrierUdfs = durableData.getUdfs();
    						assignCarrierInfo.setAssignCarrierUdfs(assignCarrierUdfs);
    
    						Map<String, String> lotUdfs = trackOutLot.getUdfs();
    						assignCarrierInfo.setUdfs(lotUdfs);
    						
    						// Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
    						trackOutLot = MESLotServiceProxy.getLotServiceImpl().assignCarrier(trackOutLot, assignCarrierInfo, eventInfo);
    					}
    				}
    				else  
    				{
    				    try
                        {
                            CommonValidation.checkEmptyCst(CarrierName); 
                        }
                        catch (CustomException ce)
                        {
                            lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(CarrierName,false);
                            trackOutLot = lotList.get(0);     
                        }
    				}
    				 				
/*    				// Added by smkang on 2018.12.20 - For synchronization of a carrier.
	        		for (String sourceCarrierName : sourceCarrierNameList) {
	        	        try {
	        	        	// After deassignCarrier is executed, it is necessary to check this carrier is really changed to Available state.
	        	        	Durable sourceCarrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceCarrierName);
	        	        	if(StringUtil.equals(GenericServiceProxy.getConstantMap().Dur_Available, sourceCarrierData.getDurableState()))
	        	        	{
	        					Element bodyElement = new Element(SMessageUtil.Body_Tag);
	        					bodyElement.addContent(new Element("DURABLENAME").setText(sourceCarrierName));
	        					bodyElement.addContent(new Element("DURABLESTATE").setText(GenericServiceProxy.getConstantMap().Dur_Available));
	        					
	        					// EventName will be recorded triggered EventName.
	        					Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
	        					
	        					MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, sourceCarrierName);
	        	        	}
	        	        } catch (Exception e) {
	        	        	eventLog.warn(e);
	        	        }
	        		}*/
                }
				else /* <<== 20180813, Add, NullValue Error */
                {
                    lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(CarrierName,false);
                    trackOutLot = lotList.get(0);   
                }
			}
			else  /* <<== hhlee, add, 20180721, else Statemenet */
			{
			    if(productList.size() > 0)
			    {
    			    List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(CarrierName,false);
    			    trackOutLot = lotList.get(0);	    
			    }
			}	
		}
		
		if(StringUtil.equals(DetailJobType.toUpperCase(), "TURNOVER"))
		{
			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(CarrierName,false);
			
			List<ProductU> productUSequence = new ArrayList<ProductU>();

			for (Element product : productList )
			{
				String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
				String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);			
				String turnOverFlag = SMessageUtil.getChildText(product, "TURNOVERFLAG", false);
				if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
				{
				    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);				    
					ProductU productU = new ProductU();					
					Map<String, String> productUdfs = productData.getUdfs();
                    productU.setUdfs(productUdfs);
					productU.setProductName(productName);
					productUSequence.add(productU);
				    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
				    MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, JobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
				}
			}

			if(productUSequence.size() > 0)
			{
			    String turnOverProductList = StringUtil.EMPTY;
			    for (ProductU productUData : productUSequence)
		        {
		            if(StringUtil.isEmpty(turnOverProductList))
		            {
		                turnOverProductList = productUData.getProductName();
		            }
		            else
		            {
		                turnOverProductList = turnOverProductList + " , " +  productUData.getProductName() ;
		            }
		        }		    
			    turnOverProductList = "[TurnOver Product] - " + turnOverProductList;	        
		        Map<String,String> lotUdfs = lotList.get(0).getUdfs();   
		        lotUdfs.put("NOTE", turnOverProductList);                 
		        lotList.get(0).setUdfs(lotUdfs);  
		        LotServiceProxy.getLotService().update(lotList.get(0));

				eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNOVER);
				kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
				setEventInfo.setProductQuantity(productUSequence.size());
				setEventInfo.setProductUSequence(productUSequence);
				trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
			}
			else  
            {
			    trackOutLot = lotList.get(0);         
            }   
		}
		
		if(StringUtil.equals(DetailJobType.toUpperCase(), "TURNSIDE"))
		{
			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(CarrierName,false);
			List<ProductU> productUSequence = new ArrayList<ProductU>();

			for (Element product : productList )
			{
				String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
				String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
				
				/* 20180810 , Modify,  TurnOver Flag ==>> */
                String turnSideFlag = SMessageUtil.getChildText(product, "PROCESSTURNFLAG", false);
                /* <<== 20180810 , Modify,  TurnOver Flag */
                
				if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
				{
				    /* 20180810 , Modify,  TurnOver Flag ==>> */
                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);                  
                    ProductU productU = new ProductU();                 
                    Map<String, String> productUdfs = productData.getUdfs();
                                  
                    productU.setUdfs(productUdfs);

					productU.setProductName(productName);
					productUSequence.add(productU);

				    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
				    MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, JobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
				}
			}

			if(productUSequence.size() > 0)
			{
                String turnSideProductList = StringUtil.EMPTY;
                for (ProductU productUData : productUSequence)
                {
                    if(StringUtil.isEmpty(turnSideProductList))
                    {
                        turnSideProductList = productUData.getProductName();
                    }
                    else
                    {
                        turnSideProductList = turnSideProductList + " , " +  productUData.getProductName() ;
                    }
                }
                
                turnSideProductList = "[TurnSide Product] - " + turnSideProductList;
                
                Map<String,String> lotUdfs = lotList.get(0).getUdfs();   
                lotUdfs.put("NOTE", turnSideProductList);                 
                lotList.get(0).setUdfs(lotUdfs);  
                LotServiceProxy.getLotService().update(lotList.get(0));

			    eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNSIDE);
				kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
                setEventInfo.setProductQuantity(productUSequence.size());
				setEventInfo.setProductUSequence(productUSequence);
				trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
			}
			else  /* <<== hhlee, add, 20180721, else Statemenet */
            {
			    trackOutLot = lotList.get(0);       
            }   
		}		
		
		if(StringUtil.equals(DetailJobType.toUpperCase(), "SLOTMAPCHANGE"))
		{
			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(CarrierName,false);

			List<ProductU> productUSequence = new ArrayList<ProductU>();

			/* 20180928, hhlee, add, Lot Note ==>> */
            String SlotMapChangeProductList = StringUtil.EMPTY;
            /* <<== 20180928, hhlee, add, Lot Note */
            
			for (Element product : productList )
			{
				String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
				String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
				String position = SMessageUtil.getChildText(product, "POSITION", false);

				if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
				{
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
					Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productName));

					/* 20180928, hhlee, add, Lot Note ==>> */
					if(StringUtil.isEmpty(SlotMapChangeProductList))
                    {
					    SlotMapChangeProductList = productData.getKey().getProductName() + " from " + productData.getPosition() + " to " + position ;
                    }
                    else
                    {
                        SlotMapChangeProductList = SlotMapChangeProductList + " , " +  productData.getKey().getProductName() + " from " + productData.getPosition() + " to " + position ;
                    }
					/* <<== 20180928, hhlee, add, Lot Note */
					
					productData.setPosition(Long.valueOf(position));
					ProductServiceProxy.getProductService().update(productData);

					ProductU productU = new ProductU();
					productU.setProductName(productName);
					productUSequence.add(productU);
										
					//ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
					ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
					MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, JobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
				}
			}

			if(productUSequence.size() > 0)
			{
			    /* 20180928, hhlee, add, Lot Note ==>> */
			    SlotMapChangeProductList = "[SlotMapChange Product] - " + SlotMapChangeProductList;
                
                Map<String,String> lotUdfs = lotList.get(0).getUdfs();   
                lotUdfs.put("NOTE", SlotMapChangeProductList);                 
                lotList.get(0).setUdfs(lotUdfs); 
                LotServiceProxy.getLotService().update(lotList.get(0));
			    /* <<== 20180928, hhlee, add, Lot Note */
			    
				eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE);
				kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
				/* 20180810 , Add,  ProductQuantity ==>> */
                setEventInfo.setProductQuantity(productUSequence.size());
                /* <<== 20180810 , Add,  ProductQuantity */
				setEventInfo.setProductUSequence(productUSequence);
				trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
			}
			else  /* <<== hhlee, add, 20180721, else Statemenet */
            {
			    trackOutLot = lotList.get(0);			           
            }   
		}		
		
		if(StringUtil.equals(DetailJobType.toUpperCase(), "SCRAP"))
		{
			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(CarrierName,false);			
			List<ProductU> productUSequence = new ArrayList<ProductU>();
			List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

			//List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotList.get(0).getKey().getLotName());
			String processingInfo = StringUtil.EMPTY;
			String productName = StringUtil.EMPTY;
			//String elementProductName = StringUtil.EMPTY;
			for (Element product : productList )
			{	
			    processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
			    productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
			    //elementProductName = StringUtil.EMPTY;
			  
			    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
			    
			    if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
                {
			        ProductU productU = new ProductU();
                    productU.setProductName(productName);
                    productUSequence.add(productU);
                    
                    /* 20181205, hhlee, add, Set productPGSRCSequence for Split ==>> */
                    ProductPGSRC productPGSRC = new ProductPGSRC();
                    
                    productPGSRC.setProductName(productName);
                    productPGSRC.setPosition(productData.getPosition());
                    productPGSRC.setProductGrade(productData.getProductGrade());
                    productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
                    productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
                    productPGSRC.setReworkFlag("N");

                    //productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
                    Map<String, String> productUdfs = productData.getUdfs();
                    productUdfs.put("PROCESSINGINFO", processingInfo);
                    productPGSRC.setUdfs(productUdfs);
                    productPGSRCSequence.add(productPGSRC);
                    /* <<== 20181205, hhlee, add, Set productPGSRCSequence for Split */
                    
                    /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
                    MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, JobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
                    /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
                }
			}
		    			        
			/* <<== 20180811, Modify , The BC will not send Scraped Glass. So, look for the scraped glass in the MES. */

			String scrapFlag = StringUtil.EMPTY;
			String outStageFlag = StringUtil.EMPTY;
			
			/* 20180928, hhlee, add, Lot Note ==>> */
            String scrapProductList = StringUtil.EMPTY;
            /* <<== 20180928, hhlee, add, Lot Note */
            
			if(productUSequence.size() > 0)
			{
			    /* 20181121, hhlee, add, Record Scrap Product Lot Note ==>> */
			    for(ProductU productU : productUSequence)
                {
			        /* 20180928, hhlee, add, Lot Note ==>> */
                    if(StringUtil.isEmpty(scrapProductList))
                    {
                        scrapProductList = productU.getProductName();
                    }
                    else
                    {
                        scrapProductList = scrapProductList + " , " +  productU.getProductName() ;
                    }
                    /* <<== 20180928, hhlee, add, Lot Note */
                }
			    /* 20180928, Add, Sorter JobName , OutStageFlag, ScrapFlag Check ==>> */
			    try
			    {
    			    List<SortJobProduct> sortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(" WHERE JOBNAME = ? AND PRODUCTNAME = ? ", 
    			            new Object[] {JobName, productUSequence.get(0).getProductName()});
    			    scrapFlag = sortJobProductList.get(0).getScrapFlag();
    			    outStageFlag = sortJobProductList.get(0).getOutStageFlag();			    
			    }
			    catch (Exception ex)
		        {
		            eventLog.warn(ex.getStackTrace());
		        }			    
			    /* <<== 20180928, Add, Sorter JobName , OutStageFlag, ScrapFlag Check */
			    
			    trackOutLot = lotList.get(0);
			    /* <<== 20180813. Add, trackOutLot Null Value Error */
			    
			    eventInfo.setEventName("DeassignCarrier");
                eventInfo.setCheckTimekeyValidation(false);
                eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                 
                Lot sourceLotData = null;
                
				if(lotList.get(0).getProductQuantity() == productUSequence.size())
				{					
					MQCScrapFlag = "ALL";
					
					DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();
					
					// Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
					//trackOutLot = LotServiceProxy.getLotService().deassignCarrier(lotList.get(0).getKey(), eventInfo, deassignCarrierInfo);
					trackOutLot = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotList.get(0), deassignCarrierInfo, eventInfo);
				}
				else
				{
//				    for(ProductU productU : productUSequence)
//				    {
//    				    String productnameU = productU.getProductName();
//                        Product productdataU = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productnameU);
//                        kr.co.aim.greentrack.product.management.info.DeassignCarrierInfo deassignCarrierInfo = new kr.co.aim.greentrack.product.management.info.DeassignCarrierInfo();
//                        ProductServiceProxy.getProductService().deassignCarrier(productdataU.getKey(), eventInfo, deassignCarrierInfo);
//    				    
//    			        eventInfo.setCheckTimekeyValidation(false);
//    	                eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//    	                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//    	                
////    	                if(StringUtil.isEmpty(scrapProductList))
////                        {
////    	                    scrapProductList = productU.getProductName();
////                        }
////                        else
////                        {
////                            scrapProductList = scrapProductList + " , " +  productU.getProductName() ;
////                        }
//				    }
				    sourceLotData = trackOutLot;
	                trackOutLot = this.scrapSplitGlass(eventInfo, trackOutLot, productPGSRCSequence, StringUtil.EMPTY, outStageFlag);
				}

				eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP);
				if(StringUtil.equals(outStageFlag,GenericServiceProxy.getConstantMap().Flag_Y))
				{
				    eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_OUTSTAGE);
				}				
				
				scrapProductList = "[Scrap Product] - " + scrapProductList;
                
                Map<String,String> lotUdfs = trackOutLot.getUdfs();   
                lotUdfs.put("NOTE", scrapProductList);                 
                trackOutLot.setUdfs(lotUdfs); 
                LotServiceProxy.getLotService().update(trackOutLot);

                eventInfo.setCheckTimekeyValidation(false);
                eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());

                MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();
                makeScrappedInfo.setProductQuantity(productUSequence.size());
                makeScrappedInfo.setProductUSequence(productUSequence);
                
    			// Start 2019.09.09 Modify By Park Jeong Su Mantis 4721
    			//trackOutLot = LotServiceProxy.getLotService().makeScrapped(trackOutLot.getKey(), eventInfo, makeScrappedInfo);
                trackOutLot = MESLotServiceProxy.getLotServiceImpl().makeScrapped(eventInfo,trackOutLot,makeScrappedInfo);
    			// Start 2019.09.09 Modify By Park Jeong Su Mantis 4721
                
                if(sourceLotData != null)
                {
                    trackOutLot = sourceLotData;
                }
			}			
			else  /* <<== hhlee, add, 20180721, else Statemenet */
            {
			    trackOutLot = lotList.get(0);   
            }   
		}
		
		String beforeFlow = "";
		String beforeOper = "";
		
		// After Track Out;
		if (trackOutLot != null)
		{
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		    trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
		    trackOutLot = LotServiceProxy.getLotService().selectByKeyForUpdate(trackOutLot.getKey());
		    
			beforeFlow = trackOutLot.getProcessFlowName();
			beforeOper = trackOutLot.getProcessOperationName();
		    
			// check yield. if yield is lack, reserve AHold in FutureAction.
			MESLotServiceProxy.getLotServiceImpl().checkYield(trackOutLot.getKey().getLotName(), trackOutLot.getProductSpecName(), trackOutLot.getUdfs().get("ECCODE"), trackOutLot.getProcessFlowName(), trackOutLot.getProcessFlowVersion(), trackOutLot.getProcessOperationName(), eventInfo);
    
		    if(StringUtil.equals(trackOutLot.getLotState(), "Scrapped"))
		    {
		        //20180811
		        /* TrackOut/ChangeSpec cannot be performed if the LotState is Scraped and Empted. */
                SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {JobName, CarrierName});
                sortJobCarrier.settrackflag("OUT");
                ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
		    }
		    else if(StringUtil.equals(trackOutLot.getLotState(), "Emptied")) 
    		{
		    	// ???
    		}
    		else
    		{
    			String[] nodeStackList = null;
    
    			nodeStackList = StringUtil.split(trackOutLot.getNodeStack(), '.');
    
    			if(nodeStackList.length > 0)
    			{
    				for (Element product : productList )
    				{
    					String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
    					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);  					
    					if(!StringUtil.equals(productData.getProductGrade(), "S"))
    					{
    						Check_S_Grade_Product = false;
    					}    					
    				}
    				
    				
    				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setAllProductUdfs(trackOutLot.getKey().getLotName());
    
    				//2018.12.25_hsryu_Delete Logic. 
    				//productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
    
    				Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackList[0]);
    
    				/* 20180731, Add, Change LotProcessState ==>> */
                    trackOutLot.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
                    /* 20181001, hhlee, add, Lot Note ==>> */                         
                    Map<String,String> lotUdfs = trackOutLot.getUdfs();   
                    lotUdfs.put("NOTE", "");                 
                    trackOutLot.setUdfs(lotUdfs);
                    
                    LotServiceProxy.getLotService().update(trackOutLot);
                    /* <<== 20181001, hhlee, add, Lot Note */
                    
                    /* <<== 20180731, Add, Change LotProcessState */
                    
    				if(StringUtil.equals(JobType.toUpperCase(),"CHANGE"))
    				{
    					if(StringUtil.equals(sortTransferDirection.toUpperCase(),"SOURCE"))
    					{
    						eventname = "ChangeOutTrackOut";
    					} else if(StringUtil.equals(sortTransferDirection.toUpperCase(),"TARGET"))
    					{
    						eventname = "ChangeInTrackOut";
    					} else {
    						eventname = "TrackOut";
    					}
    				}	
    				else if(StringUtil.equals(JobType.toUpperCase(),"SCRAP"))
    				{
    					eventname = "ScrapTrackOut";
    				}
    				else if(StringUtil.equals(JobType.toUpperCase(),"TURNSIDE"))
    				{
    					eventname = "TurnSideTrackOut";
    				}	
    				else if(StringUtil.equals(JobType.toUpperCase(),"TURNOVER"))
    				{
    					eventname = "TurnOverTrackOut";
    				}	
    				else if(StringUtil.equals(JobType.toUpperCase(),"SLOTMAPCHANGE"))
    				{
    					eventname = "SlotMapChgTrackOut";
    				} else {
    					eventname = "TrackOut";
    				}
    				
    				Map<String, String> udfs = trackOutLot.getUdfs();
    				
    				String beforeProcessFlowName = trackOutLot.getProcessFlowName(); // sort flow
    				String beforeProcessOperationName = trackOutLot.getProcessOperationName(); // sort operation
    				
    				String NewProcessFlow = udfs.get("BEFOREFLOWNAME");
    				String NewProcessOperation = udfs.get("BEFOREOPERATIONNAME");
    				
    				String CurrentNodeStack = trackOutLot.getNodeStack();
    				
            		eventInfo.setEventComment(eventname);
    				eventInfo.setEventName(eventname);

    				String temp_PortName = "";
    				if(Check_S_Grade_Product)
    				{
    					trackOutLot.setLotGrade("S");
    					trackOutLot.setMachineName("");
    					LotServiceProxy.getLotService().update(trackOutLot);
    					SetEventInfo setEventInfo = new SetEventInfo();
    					LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo, setEventInfo);
        				
    					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    					List<Product> productList3 = LotServiceProxy.getLotService().allUnScrappedProducts(trackOutLot.getKey().getLotName());
    					List<Product> productList3 = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(trackOutLot.getKey().getLotName());

    					for (Product productData : productList3)
        				{
        	                productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
        	                productData.setProductState(GenericServiceProxy.getConstantMap().Prod_InProduction);
        	                ProductServiceProxy.getProductService().update(productData);
        				}
    				}
    				else 
    				{
    					temp_PortName = this.GetPortName(trackOutLot.getKey().getLotName());
    					
    					if(StringUtil.equals(NewProcessOperation,"-"))
        				{   						
    						String[] arrayNodeStack = StringUtil.split(trackOutLot.getNodeStack(), ".");
    						String TempNodeStack = "";
    						if (arrayNodeStack.length > 1) 
    						{
    							TempNodeStack = arrayNodeStack[0];
    						}   					
    						
        					trackOutLot.setProcessFlowName(NewProcessFlow);
        					trackOutLot.setLotHoldState("");
        					trackOutLot.setMachineName("");
        					trackOutLot.setProcessOperationName("-");
        					trackOutLot.setLotProcessState("");
        					trackOutLot.setLotState("Completed");
        					trackOutLot.setNodeStack(TempNodeStack);
        					trackOutLot.setProcessOperationVersion("");       					     					
        					trackOutLot.setLastLoggedOutTime(eventInfo.getEventTime());
        					trackOutLot.setLastLoggedOutUser(eventInfo.getEventUser());
        					trackOutLot.setMachineRecipeName("");

        					Map<String, String> tudfs = trackOutLot.getUdfs();
            				tudfs.put("BEFOREOPERATIONNAME", beforeProcessOperationName);
            				tudfs.put("BEFOREFLOWNAME", beforeProcessFlowName);
            				tudfs.put("PORTNAME", "");
            				tudfs.put("LASTLOGGEDOUTMACHINE", machineName);            				
//            				tudfs.put("LEADTIME", this.GetLeadTime(trackOutLot.getKey().getLotName()));
//            				tudfs.put("WAITTIME", this.GetWaitTime(trackOutLot.getKey().getLotName()));
//            				tudfs.put("PROCTIME", this.GetProcTime(trackOutLot.getKey().getLotName()));
            				
            				LotServiceProxy.getLotService().update(trackOutLot);
            				SetEventInfo setEventInfo = new SetEventInfo();
            				setEventInfo.setUdfs(tudfs);
            				LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo, setEventInfo);    	
            				
            				LotHistoryKey LotHistoryKey = new LotHistoryKey();
            			    LotHistoryKey.setLotName(trackOutLot.getKey().getLotName());
            			    LotHistoryKey.setTimeKey(trackOutLot.getLastEventTimeKey());
            			    
            			    // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            			    LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
            			    LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(LotHistoryKey);
            			        
            				lotHistory.setOldProcessFlowName(beforeProcessFlowName);
            				lotHistory.setOldProcessOperationName(beforeProcessOperationName);
            				lotHistory.setMachineName(machineName);
            				lotHistory.getUdfs().put("PORTNAME", temp_PortName);
            				LotServiceProxy.getLotHistoryService().update(lotHistory);
            				
            				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            				List<Product> productList3 = LotServiceProxy.getLotService().allUnScrappedProducts(trackOutLot.getKey().getLotName());
            				List<Product> productList3 = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(trackOutLot.getKey().getLotName());

            				for (Product productData : productList3)
            	            {   
            	                productData.setProductProcessState("");
            	                productData.setProductState("Completed");
            	                productData.setProductHoldState("");
            	                productData.setProcessFlowName(NewProcessFlow);
            	                productData.setProcessOperationName(NewProcessOperation);
            	                productData.setProcessOperationVersion("");
            	                productData.setNodeStack(TempNodeStack);
            	                
            	                productData.setLastIdleTime(eventInfo.getEventTime());
            	                productData.setLastIdleUser(eventInfo.getEventUser());
            	                productData.setMachineRecipeName("");

            	                Map<String, String> Pudfs = productData.getUdfs();
            	                Pudfs.put("PORTNAME", "");
            	                Pudfs.put("LASTLOGGEDOUTMACHINE", machineName);
//            	                Pudfs.put("LEADTIME", this.GetLeadTime(trackOutLot.getKey().getLotName())); //modfiyby GJJ 20200513 mantis：5487 
//            	                Pudfs.put("WAITTIME", this.GetWaitTime(trackOutLot.getKey().getLotName()));//modfiyby GJJ 20200513 mantis：5487 
//            	                Pudfs.put("PROCTIME", this.GetProcTime(trackOutLot.getKey().getLotName()));//modfiyby GJJ 20200513 mantis：5487 
            	                
            	                ProductServiceProxy.getProductService().update(productData);
            	                kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo2 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
            	                setEventInfo2.setUdfs(Pudfs);
            					ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo2);
            					        					
            		            ProductHistoryKey productHistoryKey = new ProductHistoryKey();
            		            productHistoryKey.setProductName(productData.getKey().getProductName());
            		            productHistoryKey.setTimeKey(productData.getLastEventTimeKey());
            		            
            		            // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            		            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
            		            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
            		            
            					productHistory.setOldProcessFlowName(beforeProcessFlowName);
            					productHistory.setOldProcessOperationName(beforeProcessOperationName);
            					productHistory.getUdfs().put("PORTNAME", temp_PortName);
            					ProductServiceProxy.getProductHistoryService().update(productHistory);
            	            }
        				} else {
        					String[] arrayNodeStack = StringUtil.split(trackOutLot.getNodeStack(), ".");
        					String TempNodeStack = "";
        					if (arrayNodeStack.length > 1) 
        					{
        						for(int i=0; i<arrayNodeStack.length -1; i++)
        						{
        							TempNodeStack = TempNodeStack + arrayNodeStack[i] + ".";
        						}
        					}
        					
        					if(!StringUtil.isEmpty(TempNodeStack))
        					{
        						TempNodeStack = TempNodeStack.substring(0,TempNodeStack.length() -1);
        					}
        					
            				trackOutLot.setMachineName("");
            				trackOutLot.setLastLoggedOutTime(eventInfo.getEventTime());
        					trackOutLot.setLastLoggedOutUser(eventInfo.getEventUser());
        					trackOutLot.setMachineRecipeName("");
        					LotServiceProxy.getLotService().update(trackOutLot);
        					
        					Map<String, String> tudfs = trackOutLot.getUdfs();
            				tudfs.put("PORTNAME", "");
            				tudfs.put("LASTLOGGEDOUTMACHINE", machineName);            				
//            				tudfs.put("LEADTIME", this.GetLeadTime(trackOutLot.getKey().getLotName()));//modfiyby GJJ 20200513 mantis：5487 
//            				tudfs.put("WAITTIME", this.GetWaitTime(trackOutLot.getKey().getLotName()));//modfiyby GJJ 20200513 mantis：5487 
//            				tudfs.put("PROCTIME", this.GetProcTime(trackOutLot.getKey().getLotName()));//modfiyby GJJ 20200513 mantis：5487 
            				
                            List<Product> productList3 = null;
                            try
                            {
                            	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                            	productList3 = LotServiceProxy.getLotService().allUnScrappedProducts(trackOutLot.getKey().getLotName());
                            	productList3 = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(trackOutLot.getKey().getLotName());
                            }
                            catch (Exception ex)
                            {
                                eventLog.error("[SYS-9999] No Product to process");   
                            }
                            
                            for (Product productData : productList3)
                            {
                            	productData.setLastIdleTime(eventInfo.getEventTime());
                            	productData.setLastIdleUser(eventInfo.getEventUser());
                            	productData.setMachineRecipeName("");
                                productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
                                ProductServiceProxy.getProductService().update(productData);
            	                Map<String, String> Pudfs = productData.getUdfs();
            	                Pudfs.put("LASTLOGGEDOUTMACHINE", machineName);
//            	                Pudfs.put("LEADTIME", this.GetLeadTime(trackOutLot.getKey().getLotName()));//modfiyby GJJ 20200513 mantis：5487 
//            	                Pudfs.put("WAITTIME", this.GetWaitTime(trackOutLot.getKey().getLotName()));//modfiyby GJJ 20200513 mantis：5487 
//            	                Pudfs.put("PROCTIME", this.GetProcTime(trackOutLot.getKey().getLotName()));//modfiyby GJJ 20200513 mantis：5487 
            	                Pudfs.put("PORTNAME", "");
                            }
                            // 2019.04.24_hsryu_Change ProductRequestName. TempMixed -> "". Because executed ChangeSpec API, Change WO of Product. 
        					//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
            				ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfoForSort(trackOutLot.getKey().getLotName(),
            						trackOutLot.getProductionType(), trackOutLot.getProductSpecName(), trackOutLot.getProductSpecVersion(),
            						"", "", "", trackOutLot.getSubProductUnitQuantity1(), trackOutLot.getSubProductUnitQuantity2(),
            						trackOutLot.getDueDate(), trackOutLot.getPriority(), trackOutLot.getFactoryName(), trackOutLot.getAreaName(),
            						trackOutLot.getLotState(), trackOutLot.getLotProcessState(), trackOutLot.getLotHoldState(),
            						nodeData.getProcessFlowName(), nodeData.getProcessFlowVersion(), nodeData.getNodeAttribute1(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
            						beforeProcessFlowName,beforeProcessOperationName, NewProcessFlow, NewProcessOperation, CurrentNodeStack, trackOutLot.getUdfs(), productUdfs, true);
            
            				if(!StringUtil.isEmpty(TempNodeStack))
            				{
            					changeSpecInfo.setNodeStack(TempNodeStack);
            				}
            				eventInfo.setBehaviorName("ARRAY");

            				trackOutLot = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, trackOutLot, changeSpecInfo);
            				
            				LotHistoryKey LotHistoryKey = new LotHistoryKey();
            		        LotHistoryKey.setLotName(trackOutLot.getKey().getLotName());
            		        LotHistoryKey.setTimeKey(trackOutLot.getLastEventTimeKey());
            		        
            		        // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            		        LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
            		        LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(LotHistoryKey);
            		        
            				lotHistory.setMachineName(machineName);
            				lotHistory.getUdfs().put("PORTNAME", temp_PortName);
            				LotServiceProxy.getLotHistoryService().update(lotHistory);
            				                        
                            for (Product productData : productList3)
                            {               	              
                            	ProductHistoryKey productHistoryKey = new ProductHistoryKey();
                	            productHistoryKey.setProductName(productData.getKey().getProductName());
                	            productHistoryKey.setTimeKey(productData.getLastEventTimeKey());

                	            // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                	            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
                	            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
                	            
                                producthistory.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
                                producthistory.getUdfs().put("PORTNAME", temp_PortName);
                                ProductServiceProxy.getProductHistoryService().update(producthistory);
                            }    
                            
                            // Mentis 1878 Modify 2018.12.18
                            String MultiHoldString = "SELECT LOTNAME, EVENTNAME, EVENTUSER, EVENTCOMMENT FROM CT_LOTMULTIHOLD WHERE LOTNAME = :LOTNAME";
            				Map<String, String> bindMap = new HashMap<String, String>();
            				bindMap.put("LOTNAME", trackOutLot.getKey().getLotName());
            				@SuppressWarnings("unchecked")
            				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(MultiHoldString, bindMap);

                            if(sqlResult != null && sqlResult.size() > 0)
                            {
                            	aHoldFlag = true;
                            	
                            	for(int i=0; i<sqlResult.size(); i++)
                            	{
                                    eventInfo.setCheckTimekeyValidation(false);
                                    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                            		
                            		// Modify 2018.12.26
                            		// Mentis 1878
                                	trackOutLot.setLotHoldState("Y");
                                	SetEventInfo setEventInfo = new SetEventInfo();
                                	eventInfo.setEventName(sqlResult.get(i).get("EVENTNAME").toString());
                                	eventInfo.setEventUser(sqlResult.get(i).get("EVENTUSER").toString());
                    				Map<String, String> udfsForSort = trackOutLot.getUdfs();
                    				udfsForSort.put("NOTE", sqlResult.get(i).get("EVENTCOMMENT").toString());
                    				LotServiceProxy.getLotService().update(trackOutLot);
                    				LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo, setEventInfo);
                            	}
                            }
        				}
    				}

    				eventInfo.setEventUser(getEventUser());
    				
            		SortJobCarrier SortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {JobName, CarrierName});
            		String SorterOutholdflag =  SortJobCarrier.getoutholdflag();
            		
            		if(StringUtil.equals(SorterOutholdflag, "Y"))
            		{
            			aHoldFlag = true;
            			
            			try {
            				EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("",getEventUser(), getEventComment(), "", "");

            				eventInfo1.setEventTime(TimeStampUtil.getCurrentTimestamp());
            				eventInfo1.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
            				eventInfo1.setReasonCode("STOH");
            				eventInfo1.setReasonCodeType(GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT);
            				eventInfo1.setEventName("SorterOutHold");
            				eventInfo1.setEventComment("Changer has been finished，Please Check !");
            				Map<String, String> udfsForSort = trackOutLot.getUdfs();
            				udfsForSort.put("NOTE", "Changer has been finished，Please Check !");
            				LotServiceProxy.getLotService().update(trackOutLot);
            				
            				if (!trackOutLot.getLotHoldState().equals("Y")) {
            					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(trackOutLot);
            					MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
            					LotServiceProxy.getLotService().makeOnHold(trackOutLot.getKey(), eventInfo1,makeOnHoldInfo);

            					try {
            					//	trackOutLot = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(trackOutLot.getKey().getLotName(),eventInfo1.getReasonCode(), "MFG", "AHOLD",eventInfo1);
            						trackOutLot = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(trackOutLot.getKey().getLotName(),eventInfo1.getReasonCode(), "", "AHOLD",eventInfo1);
            						//MODIFY BY JHIYING ON20191205 MANTIS:5176
            					} catch (Exception e) {
            						eventLog.warn(e);
            					}
            					
            					// -------------------------------------------------------------------------------------------------------------------------------------------
            				} else {
            					kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
            					LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo1,setEventInfo);

            					try {
            					//	trackOutLot = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(trackOutLot.getKey().getLotName(),eventInfo1.getReasonCode(), "MFG", "AHOLD",eventInfo1);
            						trackOutLot = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(trackOutLot.getKey().getLotName(),eventInfo1.getReasonCode(), "", "AHOLD",eventInfo1);
            						//MODIFY BY JHIYING ON20191205 MANTIS:5176
            					} catch (Exception e) {
            						eventLog.warn(e);
            					}
            					// -------------------------------------------------------------------------------------------------------------------------------------------

            					// 2019.03.11_hsryu_Delete ProductHistory Hold Event. 
//            					List<ProductU> productUSequence = MESLotServiceProxy
//            							.getLotInfoUtil().getAllProductUSequence(
//            									trackOutLot);
//
//            					for (ProductU product : productUSequence) 
//            					   {
//            						Product aProduct = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(product.getProductName());
//            						kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfoP = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
//            						ProductServiceProxy.getProductService().setEvent(aProduct.getKey(), eventInfo1, setEventInfoP);
//
//            						try {
//            							MESProductServiceProxy.getProductServiceImpl().addMultiHoldProduct(product.getProductName(),eventInfo1.getReasonCode(), "MFG","AHOLD", eventInfo1);
//            						} catch (Exception e) {
//            							eventLog.warn(e);
//            						}
//            						// -------------------------------------------------------------------------------------------------------------------------------------------
//            					}
            				}
            			} catch (Throwable ex) {
            				eventLog.error(ex.getMessage());
            			}
            		}
            		
    				if(!StringUtil.equals(trackOutLot.getLotState(), "Scrapped"))
    				{
    					//2018.12.18_hsryu_Modify TrackOut Priority Logic. 
    			    	if(!StringUtils.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
    			    	{
    			    		if(!aHoldFlag)
        			   		aHoldFlag = MESLotServiceProxy.getLotServiceUtil().isExistAhold(trackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName);

    			        	if(aHoldFlag)
    			        	{
    			        		trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionBySystemHold(trackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName, eventInfo);
    			        		trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionHold(trackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName, eventInfo, GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
    			        	}
    			        	else
    			        	{
    			        		// 2019.05.30_Delete Logic. 
//    							if(!MESLotServiceProxy.getLotServiceUtil().checkSampling(trackOutLot.getKey().getLotName(),eventInfo))
//    							{
//    								if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(trackOutLot.getKey().getLotName(), eventInfo))
//    								{
//    									//Reserve Change
//    									trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(trackOutLot.getKey().getLotName(), eventInfo);
//    								}
//    							}
    			        		
    			        		// 2019.05.30_hsryu_Modify Logic. if TrackOut and Sampling, Not Execute BHold. So, Check Sampling and BHold at the same time.
    			        		boolean isCheckSampling = false;
    			        		
    			        		isCheckSampling = MESLotServiceProxy.getLotServiceUtil().checkSampling(trackOutLot.getKey().getLotName(), eventInfo);
    			        		
    			        		if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(trackOutLot.getKey().getLotName(), eventInfo) && !isCheckSampling ){
    			        			trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(trackOutLot.getKey().getLotName(), eventInfo);
    			        		}
    			        	}
    			    	}
    				}
    				///* Array 20180807, Add [Process Flag Update] ==>> */            
    	            //MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, trackOutLot, logicalSlotMap);
    	            ///* <<== Array 20180807, Add [Process Flag Update] */
    			}
                
                try
                {
                    SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {JobName, CarrierName});
                    sortJobCarrier.settrackflag("OUT");
                    ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
                }
                catch (Exception ex)
                {
                    eventLog.error(ex.getMessage());
                }
                /* 20180929, Add, SortJobCarrier trackflag update(Requested by shkim) ==>> */
    		}
		}
 		
		else
		{
			//  trackOutLot == null;
		    // 2019.03.26 Empty CST Validation 
            SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {JobName, CarrierName});
            sortJobCarrier.settrackflag("OUT");
            ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
		}
		
		
		// 2018.09.26 
		if(StringUtil.equals(JobType.toUpperCase(), "TURNOVER") || StringUtil.equals(JobType.toUpperCase(), "TURNSIDE"))
		{		
			try
			{
				String ChangeValueQuery = "SELECT PRODUCTNAME FROM CT_SORTJOBPRODUCT WHERE JOBNAME = :JOBNAME";
				Map<String, String> bind_Map = new HashMap<String, String>();
				bind_Map.put("JOBNAME", JobName);
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sql_Result = GenericServiceProxy.getSqlMesTemplate().queryForList(ChangeValueQuery, bind_Map);
				if(sql_Result.size() > 0)
				{
					for(int i=0; i<sql_Result.size(); i++)
					{
						ProductFlag productFlagData = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {sql_Result.get(i).get("productname").toString()});
						if(StringUtil.equals(JobType.toUpperCase(), "TURNOVER"))
						{
							String tempTurnOverFlag = productFlagData.getTurnOverFlag();
							
							if(StringUtil.equals(tempTurnOverFlag, "Y"))
							{
								productFlagData.setTurnOverFlag("N");
							}
							else {
								productFlagData.setTurnOverFlag("Y");
							}
							
						}
						else {
							String tempTurnSideFlag = productFlagData.getProcessTurnFlag();
							
							if(StringUtil.equals(tempTurnSideFlag, "Y"))
							{
								productFlagData.setProcessTurnFlag("N");
							}
							else {
								productFlagData.setProcessTurnFlag("Y");
							}
						}
						ExtendedObjectProxy.getProductFlagService().modify(eventInfo, productFlagData);
					}
				}
			}
			catch (Exception ce)
			{
				throw new CustomException("PRODUCT-0033");
			}
		}
		
		// 2018.10.30
		if(StringUtil.equals(JobType.toUpperCase(), "CHANGE"))
		{			
			if(!Check_S_Grade_Product)
			{
				// Hold
				if(!StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE))
				{
					// When targetCST track-out
					String Ccondition = "where jobname=? and transferdirection = ?";
					Object[] bindSet = new Object[] {JobName, "SOURCE"};
					List<SortJobCarrier> SortJobSourceCarrier = ExtendedObjectProxy.getSortJobCarrierService().select(Ccondition,bindSet);
					for (SortJobCarrier SourceCarrier : SortJobSourceCarrier) {
						String sql = "SELECT REASONCODE, DEPARTMENT, EVENTCOMMENT, EVENTNAME, HOLDTYPE, RESETFLAG, EVENTUSER FROM CT_LOTMULTIHOLD WHERE LOTNAME = :LOTNAME";
						Map<String, String> bindMap = new HashMap<String, String>();
						bindMap.put("LOTNAME", SourceCarrier.getLotName());
						@SuppressWarnings("unchecked")
						List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
						if( sqlResult != null && sqlResult.size() > 0)
						{
							for(int i=0;i<sqlResult.size(); i++)
							{
	        					try {
	        						// 2019.04.10_hsryu_Change NoteComment. Mantis 0003462.
	        						String noteComment = sqlResult.get(i).get("EVENTCOMMENT").toString() + ", inherit from lot[" + SourceCarrier.getLotName() + "]";
	        						// ********* 2019.01.28 *********
	        						eventInfo.setEventName(sqlResult.get(i).get("EVENTNAME").toString());
	        						// ********* 2019.01.28 *********
	        						
	        			            eventInfo.setCheckTimekeyValidation(false);
	        			            eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
	        			            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
	        			            
	        			            // 2019.04.10_hsryu_Insert Logic. Mantis 0003462.
	        			            eventInfo.setEventComment(noteComment);
	        			            
	        			            // ********* 2018.12.26 *********
	        			            // 2019.04.10_hsryu_Change NoteComment. Mantis 0003462.
	        			            //eventInfo.setEventComment(sqlResult.get(i).get("EVENTCOMMENT").toString());
	        			            eventInfo.setEventComment(noteComment);
	        			            eventInfo.setEventUser(sqlResult.get(i).get("EVENTUSER").toString());
	        			            // ********* 2018.12.26 *********
	        			            
	        			            trackOutLot.setLotHoldState("Y");
	        			            Map<String,String> lotUdfs = trackOutLot.getUdfs();   
	        			            //2019.02.27_hsryu_Modfiy Note. Mantis 0002859.
	        			            //lotUdfs.put("NOTE", "[Sorter Hold SourceLotName : " + SourceCarrier.getLotName() + "]");    
	        			            // 2019.04.10_hsryu_Change NoteComment. Mantis 0003462.
	        			            //lotUdfs.put("NOTE", "inherit from lot[" + SourceCarrier.getLotName() + "]");    
	        			            lotUdfs.put("NOTE", noteComment);    
	        			            LotServiceProxy.getLotService().update(trackOutLot);
	        						SetEventInfo setEventInfo = new SetEventInfo();
	        						setEventInfo.setUdfs(lotUdfs);
	        						LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo, setEventInfo);  
	        			            
	        						trackOutLot = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(trackOutLot.getKey().getLotName(),sqlResult.get(i).get("REASONCODE").toString(), sqlResult.get(i).get("DEPARTMENT").toString(), sqlResult.get(i).get("HOLDTYPE").toString(),eventInfo);
	        						
	        						aHoldFlag = true;
	        					} catch (Exception e) {
	        						eventLog.warn(e);
	        					}
							}
						}
					}
					
					// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//					//2019.02.27_hsryu_Insert Logic. 
//					trackOutLot.getUdfs().put("NOTE", "");
//					LotServiceProxy.getLotService().update(trackOutLot);
					Map<String, String> updateUdfs = new HashMap<String, String>();
					updateUdfs.put("NOTE", "");
					MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(trackOutLot, updateUdfs);
				}
								
				// priority
				if(!StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE))
				{
					String Ccondition = "where jobname=? and transferdirection = ?";
					Object[] bindSet = new Object[] {JobName, "SOURCE"};
					List<SortJobCarrier> SortJobSourceCarrier = ExtendedObjectProxy.getSortJobCarrierService().select(Ccondition,bindSet);
					
					for (SortJobCarrier Carrier : SortJobSourceCarrier) {
						String LotName = Carrier.getLotName();
		    			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(LotName);
		    			long tempP = lotData.getPriority();
		    			if(tempP < trackOutLot.getPriority())
		    			{ 
		    				trackOutLot.setPriority(tempP);
		    				LotServiceProxy.getLotService().update(trackOutLot);
		    			}
					}
				}
			}
		}
        String CHECKSTATE = "SELECT CARRIERNAME FROM CT_SORTJOBCARRIER WHERE JOBNAME = :JOBNAME AND ( TRACKFLAG IS NULL OR TRACKFLAG != :TRACKFLAG )";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("JOBNAME", JobName);
		bindMap.put("TRACKFLAG", "OUT");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(CHECKSTATE, bindMap);
		if(sqlResult != null && sqlResult.size() == 0)
		{
			SortJob sortJob = new SortJob("");
			sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {JobName});
			sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ENDED);
			sortJob.setEventTime(eventInfo.getEventTime());
			sortJob.setEventName(eventInfo.getEventName());
			sortJob.setEventUser(eventInfo.getEventUser());
			sortJob.setEventComment(eventInfo.getEventComment());
			ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
			
			eventInfo = EventInfoUtil.makeEventInfo("AssignPort",getEventUser(), getEventComment(), "", "");
			eventInfo.setCheckTimekeyValidation(false);
            eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
			MESLotServiceProxy.getLotInfoUtil().rearrangeSorterPort(JobName, machineName, eventInfo);			
		}

		if( trackOutLot != null)
		{
			trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
			
			if(trackOutLot!=null&&!StringUtil.equals(trackOutLot.getLotState(), "Scrapped"))
			{
				if(!aHoldFlag)
					aHoldFlag = MESLotServiceProxy.getLotServiceUtil().isExistAhold(trackOutLot.getKey().getLotName(), beforeFlow, beforeOper);

				if(aHoldFlag)
		   		{
		   			trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionBySystemHold(trackOutLot.getKey().getLotName(), beforeFlow, beforeOper, eventInfo);
		   			trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionHold(trackOutLot.getKey().getLotName(), beforeFlow, beforeOper, eventInfo, GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
		   		}
		   		else
		   		{
					if(!StringUtils.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
					{
						// 2019.05.30_Delete Logic. 
//						if(!MESLotServiceProxy.getLotServiceUtil().checkSampling(trackOutLot.getKey().getLotName(),eventInfo))
//						{
//							if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(trackOutLot.getKey().getLotName(), eventInfo))
//							{
//								//Reserve Change
//								trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(trackOutLot.getKey().getLotName(), eventInfo);
//							}
//						}
						
		        		// 2019.05.30_hsryu_Modify Logic. if TrackOut and Sampling, Not Execute BHold. So, Check Sampling and BHold at the same time.
		        		boolean isCheckSampling = false;
		        		
		        		isCheckSampling = MESLotServiceProxy.getLotServiceUtil().checkSampling(trackOutLot.getKey().getLotName(), eventInfo);
		        		
		        		if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(trackOutLot.getKey().getLotName(), eventInfo) && !isCheckSampling ){
		        			trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(trackOutLot.getKey().getLotName(), eventInfo);
		        		}

					}
		   		}
			}

			// 2019.04.30
			if(StringUtil.equals(DetailJobType.toUpperCase(), "SCRAP") && StringUtils.isEmpty(MQCScrapFlag))
			{
				// it is not all scrap logic;
				// DeleteJob && MoveToEndBank.
				//MESLotServiceProxy.getLotServiceUtil().checkMQCFlowAfter(trackOutLot, CarrierName, eventInfo);
				MESLotServiceProxy.getLotServiceImpl().checkMQCJobOfScrapLot(trackOutLot.getKey().getLotName(),CarrierName,eventInfo);
			}
			
			// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//			//Note clear - YJYU
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
//			Map<String, String> udfs_note = lotData.getUdfs();
//			udfs_note.put("NOTE", "");
//			eventInfo.setEventComment("");
//			LotServiceProxy.getLotService().update(lotData);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(trackOutLot, updateUdfs);
		}
	
 		return doc;
	}

	private Lot mergeGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName, Lot targetLot, String allGlasssScrapFlag) throws CustomException
	{
		eventLog.info("Merge Lot for TK out");

		List<ProductP> productPSequence = new ArrayList<ProductP>();
		
		/* 20180928, hhlee, add, Lot Note ==>> */
		String mergeProductList = StringUtil.EMPTY;

        for (ProductPGSRC productPGSRC : productPGSRCSequence)
        {
            ProductP productP = new ProductP();
            productP.setProductName(productPGSRC.getProductName());
            productP.setPosition(productPGSRC.getPosition());
            productP.setUdfs(productPGSRC.getUdfs());
            productPSequence.add(productP);
            
            if(StringUtil.isEmpty(mergeProductList))
            {
                mergeProductList = productPGSRC.getProductName();
            }
            else
            {
                mergeProductList = mergeProductList + " , " +  productPGSRC.getProductName() ;
            }
        }
		
        mergeProductList = "[Change Product] - " + mergeProductList;
        
        //2019.04.23_hsryu_Insert Logic. Mantis 0002757.
        try{
    		String woName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(targetLot, productPSequence);
    		
    		if(!StringUtils.isEmpty(woName)) {
    			if(!StringUtils.equals(targetLot.getProductRequestName(), woName)){
    				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    				targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
    				targetLot = LotServiceProxy.getLotService().selectByKeyForUpdate(targetLot.getKey());

    				targetLot.setProductRequestName(woName);
    				LotServiceProxy.getLotService().update(targetLot);
    			}
    		}
        }
        catch(Throwable e){
        	eventLog.warn("Fail update WorkOrder.");
        }
        
        Map<String,String> lotUdfs = lotData.getUdfs();   
        lotUdfs.put("NOTE", mergeProductList);                 
        lotData.setUdfs(lotUdfs);  
        LotServiceProxy.getLotService().update(lotData);
        /* <<== 20180928, hhlee, add, Lot Note */
        
        /* 20181030, hhlee, add, LotPriority Copy(Source Lot Priority < Target Lot Priority) ==>> */
        if(lotData.getPriority() < targetLot.getPriority())
        {
            targetLot.setPriority(lotData.getPriority());                
            LotServiceProxy.getLotService().update(targetLot);
        }
        /* <<== 20181030, hhlee, add, LotPriority Copy(Source Lot Priority < Target Lot Priority) */
        
        /* 20180930, hhlee, add All Glass Scrap Check ==>> */
        TransferProductsToLotInfo  transitionInfo = null;

        /* 20190313, hhlee,  */
        if(StringUtil.equals(allGlasssScrapFlag, GenericServiceProxy.getConstantMap().Flag_Y))
        {
            eventInfo.setBehaviorName("SPECIAL");
        }
        targetLot.getUdfs().put("NOTE", mergeProductList);
        transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
                targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, targetLot.getUdfs(), new HashMap<String, String>());
        
        eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
        eventInfo.setCheckTimekeyValidation(false);
        /* 20181128, hhlee, EventTime Sync */
        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());

        lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
        /* <<== 20190313, hhlee, modify, Mixed ProductSpec */
        
        //2019.04.23_hsryu_Insert Logic. Check Mixed or Not Mixed WO. Mantis 0002757.
        try{
    		if(lotData != null) {
    			if(!StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Emptied)){
    				
    				String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);

    				if(!StringUtils.isEmpty(sourceWOName)) {
    					if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)){
    						lotData.setProductRequestName(sourceWOName);
    						LotServiceProxy.getLotService().update(lotData);

    						// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    						String condition = "where lotname=?" + " and timekey= ? " ;
//    						Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
//    						List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//    						LotHistory lotHistory = arrayList.get(0);
    						LotHistoryKey lotHistoryKey = new LotHistoryKey();
    						lotHistoryKey.setLotName(lotData.getKey().getLotName());
    						lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
    						LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
    						
    						lotHistory.setProductRequestName(sourceWOName);
    						LotServiceProxy.getLotHistoryService().update(lotHistory);
    					}
    				}
    			}
    		}
        }
        catch(Throwable e){
        	eventLog.warn("Fail update WorkOrder.");
        }
        
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		return MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
		return LotServiceProxy.getLotService().selectByKeyForUpdate(targetLot.getKey());
	}
	
    private void copyFutureActionForSplit(Lot parent, Lot child, EventInfo eventInfo)
    {
        List<LotAction> sampleActionList = new ArrayList<LotAction>();
        long lastPosition = 0;
        
        String condition = " WHERE lotName = ? AND factoryName = ? AND actionState = ? ";
        Object[] bindSet = new Object[]{ parent.getKey().getLotName(), parent.getFactoryName(), "Created" };

        try
        {
            sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
            
            for(int i=0; i<sampleActionList.size();i++)
            {
                LotAction lotaction = new LotAction();
                lotaction = sampleActionList.get(i);
                
                if(!StringUtil.equals(lotaction.getHoldCode(), "RLRE")){ // START MODIFY BY JHYING ON20200316 MANTIS:5775 split时 register local run的future action不需要继承

                lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastPositionOfLotAction(child,lotaction.getProcessFlowName(),lotaction.getProcessOperationName()));
                
                lotaction.setLotName(child.getKey().getLotName());
                lotaction.setPosition(lastPosition+1);
                lotaction.setLastEventTime(eventInfo.getEventTime());
                
                // Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//                lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
                lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
                
                //2019.02.27_hsryu_LastEventName, LastEventUser, LastEventComment. not get EventInfo, get lotAction Info. 
                lotaction.setLastEventName(lotaction.getLastEventName());
                lotaction.setLastEventUser(lotaction.getLastEventUser());
                lotaction.setLastEventComment(lotaction.getLastEventComment());
                
                ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
                
                } // END MODIFY BY JHYING ON20200316 MANTIS:5775 split时 register local run的future action不需要继承
               
            }
        }
        catch (Throwable e)
        {
            return;
        }        
    }
    
	private void copyFutureActionCallSplit(Lot parentLot, Lot childLot, EventInfo eventInfo)
    {
        try
        {
            eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
            eventInfo.setEventComment(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
            eventInfo.setCheckTimekeyValidation(false);
            eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
            
            copyFutureActionForSplit(parentLot, childLot, eventInfo);
        }
        catch (Throwable e)
        {
            return;
        }
    }
	private Lot splitGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName) throws CustomException
	{
		eventLog.info("Split Lot for TK out");
		eventInfo.setBehaviorName("ARRAY");
		List<Lot> lotList = null;
		Lot targetLot = null;
		try
		{
			lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
		}
		catch (Exception ex)
		{
			lotList = null;
		}
        
		String splitProductList = StringUtil.EMPTY;

		if(lotList.size() == 0 )
		{
			eventInfo.setEventName("Create");
			targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
		}
		else
		{
			targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
		}

		List<ProductP> productPSequence = new ArrayList<ProductP>();
        for (ProductPGSRC productPGSRC : productPGSRCSequence)
        {
            ProductP productP = new ProductP();
            productP.setProductName(productPGSRC.getProductName());
            productP.setPosition(productPGSRC.getPosition());
            productP.setUdfs(productPGSRC.getUdfs());
            productPSequence.add(productP);
            
            if(StringUtil.isEmpty(splitProductList))
            {
                splitProductList = productPGSRC.getProductName();
            }
            else
            {
                splitProductList = splitProductList + " , " +  productPGSRC.getProductName() ;
            }
        }
        
        splitProductList = "[Change Product] - " + splitProductList;
        		
        Map<String,String> lotUdfs = lotData.getUdfs();   
        lotUdfs.put("NOTE", splitProductList);                 
        lotData.setUdfs(lotUdfs);      
        LotServiceProxy.getLotService().update(lotData);

		TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
														targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());

		eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
		eventInfo.setEventComment(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
		eventInfo.setCheckTimekeyValidation(false);
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());

		lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
		
		/**************************** Check Mixed WO Name *********************************/
		//2019.04.24_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
		try{
			String desWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(targetLot);
			
			if(!StringUtils.isEmpty(desWOName)) {
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
				targetLot = LotServiceProxy.getLotService().selectByKeyForUpdate(targetLot.getKey());

				if(!StringUtils.equals(targetLot.getProductRequestName(), desWOName)) {
					targetLot.setProductRequestName(desWOName);
					LotServiceProxy.getLotService().update(targetLot);
					
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					String condition = "where lotname=?" + " and timekey= ? " ;
//					Object[] bindSet = new Object[]{targetLot.getKey().getLotName(), eventInfo.getEventTimeKey()};
//					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//					LotHistory lotHistory = arrayList.get(0);
					LotHistoryKey lotHistoryKey = new LotHistoryKey();
					lotHistoryKey.setLotName(targetLot.getKey().getLotName());
					lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
					LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
					
					lotHistory.setProductRequestName(desWOName);
					LotServiceProxy.getLotHistoryService().update(lotHistory);
				}
			}
			
			//2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
			String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);
			
			if(!StringUtils.isEmpty(sourceWOName)) {
				if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)) {
					lotData.setProductRequestName(sourceWOName);
					LotServiceProxy.getLotService().update(lotData);
					
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					String condition = "where lotname=?" + " and timekey= ? " ;
//					Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
//					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//					LotHistory lotHistory = arrayList.get(0);
					LotHistoryKey lotHistoryKey = new LotHistoryKey();
					lotHistoryKey.setLotName(lotData.getKey().getLotName());
					lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
					LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
					
					lotHistory.setProductRequestName(sourceWOName);
					LotServiceProxy.getLotHistoryService().update(lotHistory);
				}
			}
		}
		catch(Throwable e){
			eventLog.warn("Fail update WorkOrder.");
		}
		/*********************************************************************************/

		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		return MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
		return LotServiceProxy.getLotService().selectByKeyForUpdate(targetLot.getKey());
	}
	
    private void copyFutureActionCallMerge(Lot sourceLot, Lot destnationLot, EventInfo eventInfo)
    {
        try
        {
            eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
            eventInfo.setCheckTimekeyValidation(false);
            eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
            
            copyFutureActionForMerge(sourceLot, destnationLot, eventInfo);
        }
        catch (Throwable e)
        {
            return;
        }
    }
    private Lot scrapSplitGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName, String outStageFlag) throws CustomException
    {
        eventLog.info("Scrap Split Lot for TK out");

        List<Lot> lotList = null;
        Lot targetLot = null;
        try
        {
            lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
        }
        catch (Exception ex)
        {
            lotList = null;
        }
        
        String splitProductList = StringUtil.EMPTY;
        
        if(lotList.size() == 0 )
        {
            eventInfo.setEventName("Create");
            targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
        }
        else
        {
            targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
        }
        
        List<ProductP> productPSequence = new ArrayList<ProductP>();
        for (ProductPGSRC productPGSRC : productPGSRCSequence)
        {
            ProductP productP = new ProductP();
            productP.setProductName(productPGSRC.getProductName());
            productP.setPosition(productPGSRC.getPosition());
            productP.setUdfs(productPGSRC.getUdfs());
            productPSequence.add(productP);
            
            if(StringUtil.isEmpty(splitProductList))
            {
                splitProductList = productPGSRC.getProductName();
            }
            else
            {
                splitProductList = splitProductList + " , " +  productPGSRC.getProductName() ;
            }
        }
        
        splitProductList = "[Scrap Product] - " + splitProductList;
        
        Map<String,String> lotUdfs = lotData.getUdfs();   
        lotUdfs.put("NOTE", splitProductList);                 
        lotData.setUdfs(lotUdfs);      
        LotServiceProxy.getLotService().update(lotData);
        /* <<== 20180928, hhlee, add, Lot Note */

        TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
                                                        targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());

        // Mentis 2107 
        // Modify 2018.12.26
        if(StringUtil.equals(outStageFlag,GenericServiceProxy.getConstantMap().Flag_Y))
        {
        	eventInfo.setEventName("OutStageSplit");
        }
        else 
        {
        	eventInfo.setEventName("ScrapSplit");
        }

        eventInfo.setCheckTimekeyValidation(false);
        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());

        lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
        
		/**************************** Check Mixed WO Name *********************************/
		//2019.04.24_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
		try{
			String desWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(targetLot);
			
			if(!StringUtils.isEmpty(desWOName)) {
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
				targetLot = LotServiceProxy.getLotService().selectByKeyForUpdate(targetLot.getKey());
				
				if(!StringUtils.equals(targetLot.getProductRequestName(), desWOName)) {
					targetLot.setProductRequestName(desWOName);
					LotServiceProxy.getLotService().update(targetLot);
					
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					String condition = "where lotname=?" + " and timekey= ? " ;
//					Object[] bindSet = new Object[]{targetLot.getKey().getLotName(), eventInfo.getEventTimeKey()};
//					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//					LotHistory lotHistory = arrayList.get(0);
					LotHistoryKey lotHistoryKey = new LotHistoryKey();
					lotHistoryKey.setLotName(targetLot.getKey().getLotName());
					lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
					LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
					
					lotHistory.setProductRequestName(desWOName);
					LotServiceProxy.getLotHistoryService().update(lotHistory);
				}
			}
			
			//2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
			String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);
			
			if(!StringUtils.isEmpty(sourceWOName)) {
				if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)) {
					lotData.setProductRequestName(sourceWOName);
					LotServiceProxy.getLotService().update(lotData);
					
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					String condition = "where lotname=?" + " and timekey= ? " ;
//					Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
//					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//					LotHistory lotHistory = arrayList.get(0);
					LotHistoryKey lotHistoryKey = new LotHistoryKey();
					lotHistoryKey.setLotName(lotData.getKey().getLotName());
					lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
					LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
					
					lotHistory.setProductRequestName(sourceWOName);
					LotServiceProxy.getLotHistoryService().update(lotHistory);
				}
			}
		}
		catch(Throwable e){
			eventLog.warn("Fail update WorkOrder.");
		}
		/*********************************************************************************/

		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        return MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
        return LotServiceProxy.getLotService().selectByKeyForUpdate(targetLot.getKey());
    }
    private void copyFutureActionForMerge(Lot sLot, Lot dLot, EventInfo eventInfo) throws CustomException
    {
        
        List<LotAction> lotActionList = new ArrayList<LotAction>();
        List<LotAction> lotActionList2 = new ArrayList<LotAction>();
        long lastPosition= 0;

        String condition = " WHERE lotName = ? AND factoryName = ? AND actionState = ? ";
        Object[] bindSet = new Object[]{ sLot.getKey().getLotName(), sLot.getFactoryName(), "Created" };

        try
        {
            lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
            
            for(int i=0; i<lotActionList.size();i++)
            {
                LotAction lotaction = new LotAction();
                lotaction = lotActionList.get(i);

                String condition2 = "WHERE lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND "
                        + "processOperationName = ? AND processOperationVersion = ? AND actionState = ? ";
                
                Object[] bindSet2 = new Object[]{ dLot.getKey().getLotName(), dLot.getFactoryName() ,lotaction.getProcessFlowName(),
                        lotaction.getProcessFlowVersion(), lotaction.getProcessOperationName(), lotaction.getProcessOperationVersion(), "Created" };
                
                try
                {
                    lotActionList2 = ExtendedObjectProxy.getLotActionService().select(condition2, bindSet2);
                    
                    for(int j=0; j<lotActionList2.size(); j++)
                    {
                        LotAction lotAction2 = new LotAction();
                        lotAction2 = lotActionList2.get(j);
                        
                        if(StringUtil.equals(lotAction2.getActionName(), lotaction.getActionName()))
                        {
                            lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastPositionOfLotAction(dLot,lotaction.getProcessFlowName(),lotaction.getProcessOperationName()));

                            lotaction.setLotName(dLot.getKey().getLotName());
                            lotaction.setPosition(lastPosition+1);
                            lotaction.setLastEventTime(eventInfo.getEventTime());
                            
                            // Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//                            lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
                            lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
                            
                            ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
                        }
                    }
                }
                catch(Throwable e)
                {
                    lotaction.setLotName(dLot.getKey().getLotName());
                    lotaction.setLastEventTime(eventInfo.getEventTime());
                    
                    // Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//                    lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
                    lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
                    
                    ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
                }
            }
        }
        catch (Throwable e)
        {
            return;
        }
    }
    
    private String GetLeadTime(String lotName)
    {
		// TK-OUT - IF(BEFORE TK-OUT NULL THEN Release TIME ELSE BEFORE TK-OUT END)  
		String strSql_LeadTime = 
				"    WITH LOT_LIST AS (" +
					"                   SELECT ROWNUM NO, LAG(LASTLOGGEDOUTTIME) OVER(ORDER BY TIMEKEY ) BEFORT , LASTLOGGEDOUTTIME ,LOTNAME  " +					
				" 			         FROM LOTHISTORY  " +
				" 			        WHERE LOTNAME = :LOTNAME " +
				" 			         ORDER BY TIMEKEY )			" +
				" 			SELECT TO_CHAR(ROUND((LASTLOGGEDOUTTIME - BEFORT) * 24 *60 *60,2))  AS LEADTIME, BEFORT, LASTLOGGEDOUTTIME " +
				" 			FROM LOT_LIST " +
				" 			WHERE NO IN ( SELECT MAX(NO) NO FROM LOT_LIST ) " ;		
									 
		Map<String, Object> bindMap_LeadTime = new HashMap<String, Object>();
		bindMap_LeadTime.put("LOTNAME", lotName);

		List<Map<String, Object>> List_LeadTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_LeadTime, bindMap_LeadTime);
		if(List_LeadTime != null && List_LeadTime.size() > 0)
		{	
			return List_LeadTime.get(0).get("LEADTIME").toString();
		}  	
    	return "";
    }
    
    private String GetWaitTime(String lotName)
    {
		//BEFORE TK-OUT - TK-IN
		String strSql_WaitTime = 
				" WITH LOT_TKIN AS (      "+    
				"                   SELECT LASTLOGGEDINTIME EVENTTIME ,LOTNAME  " +					
				" 			         FROM LOT  " +
				"      				WHERE  LOTNAME = :LOTNAME ) " +
				"       ,LOT_TKOUT AS (  "+
				"                   SELECT ROWNUM NO, LAG(LASTLOGGEDOUTTIME) OVER(ORDER BY TIMEKEY ) EVENTTIME ,LOTNAME  " +					
				" 			         FROM LOTHISTORY A " +
				" 			        WHERE LOTNAME = :LOTNAME " +
				" 			         ORDER BY TIMEKEY ) " +
				"      SELECT TO_CHAR(ROUND((A.EVENTTIME - B.EVENTTIME ) * 24 *60 *60,2)) WAITTIME  "+
				"      FROM  LOT_TKIN A "+
				"        INNER JOIN LOT_TKOUT B ON A.LOTNAME = B.LOTNAME "+
				"      WHERE 1=1  "+
				"         AND B.NO IN (SELECT MAX(NO) NO FROM LOT_TKOUT) " ;	 ;
 
		Map<String, Object> bindMap_WaitTime = new HashMap<String, Object>();
		bindMap_WaitTime.put("LOTNAME", lotName);
	
		List<Map<String, Object>> List_WaitTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_WaitTime, bindMap_WaitTime);
				
		if(List_WaitTime != null && List_WaitTime.size() > 0)
		{
			return List_WaitTime.get(0).get("WAITTIME").toString();
		}
		
    	return "";
    }
    
    private String GetProcTime(String lotName)
    {
		//TK-OUT - TK-IN
		String strSql_ProcTime = 	 
				"      SELECT  TO_CHAR(ROUND((LASTLOGGEDOUTTIME - LASTLOGGEDINTIME ) * 24 *60 *60,2)) PROCTIME  "+
				"       FROM LOT  "+
				"      WHERE  LOTNAME = :LOTNAME  ";

		Map<String, Object> bindMap_ProcTime = new HashMap<String, Object>();
		bindMap_ProcTime.put("LOTNAME", lotName);
		List<Map<String, Object>> List_ProcTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_ProcTime, bindMap_ProcTime);
			
		if(List_ProcTime != null && List_ProcTime.size() > 0)
		{
			return List_ProcTime.get(0).get("PROCTIME").toString();
		}
		
    	return "";
    }
    
    private String GetPortName(String lotName)
    {
    	String strSql_PortName = "SELECT PORTNAME FROM LOT WHERE LOTNAME = :LOTNAME";
		Map<String, Object> bindMap_PORTNAME = new HashMap<String, Object>();
		bindMap_PORTNAME.put("LOTNAME", lotName);
		List<Map<String, Object>> List_PortName = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_PortName, bindMap_PORTNAME);
			
		if(List_PortName != null && List_PortName.size() > 0)
		{
			return List_PortName.get(0).get("PORTNAME").toString();
		}
    	return "";
    }
    
}
