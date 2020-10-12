package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.extended.object.management.data.LotMultiHold;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class MergeLot extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Merge", getEventUser(), getEventComment(), "", "");
		eventInfo.setBehaviorName("SPECIAL");

		String dstLotName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONLOTNAME", false);
		String dstDurableName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONDURABLENAME", true);
		boolean isDesLotSGrade = false;
		
		Lot dstLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(dstLotName);		
		Durable dstDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(dstDurableName);
		isDesLotSGrade = StringUtils.equals(dstLotData.getLotGrade(), "S")?true:false;
		
		CommonValidation.CheckDurScrapped(dstDurableData);
		//CommonValidation.CheckDurableHoldState(dstDurableData);
		//CommonValidation.checkDurableDirtyState(dstDurableData);

		//CommonValidation.checkLotState(dstLotData);
		//checkLotProcessState(dstLotData);
		//CommonValidation.checkLotHoldState(dstLotData);
		
		// Added by smkang on 2018.12.20 - For synchronization of a carrier.
		List<String> sourceCarrierNameList = new ArrayList<String>();
				
		Map<String, List<ProductP>> productPSequences = GetProductList(doc);
		
		for(String lotName : productPSequences.keySet())
		{
			String srcLotName = lotName;
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Lot srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(srcLotName);
			Lot srcLotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(srcLotName));

			String srcCarrierName = srcLotData.getCarrierName();
			//CommonValidation.checkLotState(srcLotData);
			//checkLotProcessState(srcLotData);
			//CommonValidation.checkLotHoldState(srcLotData);
			
			// Added by smkang on 2018.12.20 - For synchronization of a carrier.
			if (StringUtils.isNotEmpty(srcCarrierName) && !sourceCarrierNameList.contains(srcCarrierName))
				sourceCarrierNameList.add(srcCarrierName);
			
			Boolean isLotHold = false;
			isLotHold = srcLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().FLAG_Y);

			String srcDurableName = srcLotData.getCarrierName();
			Durable srcDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(srcDurableName);
			CommonValidation.CheckDurScrapped(srcDurableData);
			CommonValidation.CheckDurableHoldState(srcDurableData);
			
			//2019.01.08_hsryu_Check SortJob!
			CommonValidation.checkExistSortJob(srcLotData.getFactoryName(), srcDurableName);
			CommonValidation.checkExistSortJob(dstLotData.getFactoryName(), dstDurableName);
			
			List<ProductP> productPSequence = productPSequences.get(lotName);
			
//			if(isDesLotSGrade)
//			{
//				for(ProductP product : productPSequence)
//				{
//					eventInfo.setEventName("ChangeSpecForSGradeMerge");
//					
//					Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(product.getProductName());
//					Map<String,String> productUdfs = productData.getUdfs();
//					Map<String,String> lotUdfs = dstLotData.getUdfs();
//
//					SetEventInfo setEventInfo = new SetEventInfo();
//					setEventInfo.setUdfs(productUdfs);
//					ProductServiceProxy.getProductService().update(productData);
//					MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);	
//				}
//			}
//			else
//			{
//				if(!StringUtil.equals(srcLotData.getLotState(), dstLotData.getLotState()))
//				{
//					throw new CustomException("MERGE-0001", srcLotData.getLotState(), dstLotData.getLotState());
//				}
//				
//				if(!StringUtil.equals(srcLotData.getLotProcessState(), dstLotData.getLotProcessState()))
//				{
//					throw new CustomException("MERGE-0002", srcLotData.getLotProcessState(), dstLotData.getLotProcessState());
//				}
//			}
			
			if(!isDesLotSGrade)
			{
				if(!StringUtil.equals(srcLotData.getLotState(), dstLotData.getLotState()))
				{
					throw new CustomException("MERGE-0001", srcLotData.getLotState(), dstLotData.getLotState());
				}
				
				if(!StringUtil.equals(srcLotData.getLotProcessState(), dstLotData.getLotProcessState()))
				{
					throw new CustomException("MERGE-0002", srcLotData.getLotProcessState(), dstLotData.getLotProcessState());
				}
			}
			
			eventInfo.setEventName("Merge");
			
			//2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
			//2019.02.25_hsryu_Insert Logic. Mantis 0002757.
			String woName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(dstLotData, productPSequence);
			
			if(!StringUtils.isEmpty(woName)) {
				if(!StringUtils.equals(dstLotData.getProductRequestName(), woName)){
					// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					dstLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(dstLotData.getKey().getLotName());
					dstLotData = LotServiceProxy.getLotService().selectByKeyForUpdate(dstLotData.getKey());

					dstLotData.setProductRequestName(woName);
					LotServiceProxy.getLotService().update(dstLotData);
				}
			}

			TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
					dstLotData.getKey().getLotName(), productPSequence.size(), productPSequence, dstLotData.getUdfs(), srcDurableData.getUdfs());
			
			
			// Start 2019.09.09 Modify By Park Jeong Su Mantis 4721 
			//srcLotData = LotServiceProxy.getLotService().transferProductsToLot(srcLotData.getKey(), eventInfo, transitionInfo);
			srcLotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, srcLotData, transitionInfo);
			// End 2019.09.09 Modify By Park Jeong Su Mantis 4721
			
			
			//2019.04.23_hsryu_Insert Logic. Check Mixed or Not Mixed WO. Mantis 0002757.
			if(srcLotData != null) {
				if(!StringUtils.equals(srcLotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Emptied)){
					
					String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(srcLotData);

					if(!StringUtils.isEmpty(sourceWOName)) {
						if(!StringUtils.equals(srcLotData.getProductRequestName(), sourceWOName)){
							srcLotData.setProductRequestName(sourceWOName);
							LotServiceProxy.getLotService().update(srcLotData);

							// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//							String condition = "where lotname=?" + " and timekey= ? " ;
							String condition = "WHERE LOTNAME = ? AND TIMEKEY = ? FOR UPDATE";

							Object[] bindSet = new Object[]{srcLotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
							List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
							LotHistory lotHistory = arrayList.get(0);
							lotHistory.setProductRequestName(sourceWOName);
							LotServiceProxy.getLotHistoryService().update(lotHistory);
							
							srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(srcLotData.getKey().getLotName());
						}
					}
				}
			}
			
			if(StringUtils.equals(dstLotData.getLotState(), "Completed"))
			{
				for(ProductP product : productPSequence)
				{
					// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(product.getProductName());
					Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(product.getProductName()));
					
					productData.setProcessOperationName("-");
					productData.setProcessOperationVersion("");
					productData.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
					productData.setProductProcessState("");
					productData.setProductHoldState("");
					
					ProductServiceProxy.getProductService().update(productData);
					
					// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                    String pCondition = " where productname=?" + " and timekey= ? " ;
                    String pCondition = "WHERE PRODUCTNAME = ? AND TIMEKEY = ? FOR UPDATE";
                    
                    Object[] pBindSet = new Object[]{productData.getKey().getProductName(), eventInfo.getEventTimeKey()};
                    List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
                    ProductHistory producthistory = pArrayList.get(0);
                    Map<String, String> productHistUdfs = producthistory.getUdfs();
                    producthistory.setProcessOperationName("-");
                    producthistory.setProcessOperationVersion("");
                    producthistory.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
                    producthistory.setProductProcessState("");
                    producthistory.setProductHoldState("");
                    ProductServiceProxy.getProductHistoryService().update(producthistory);                        
				}
			}

			//FutureAction
			copyFutureAction(srcLotData, dstLotData,eventInfo);
			
			if(!StringUtils.equals(dstLotData.getLotState(), "Completed"))
			{
				if(isLotHold)
				{
					//2019.02.27_Modify Same SortMerge.
//					EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("HoldByMerge", getEventUser(), "", "", "");
//					
//					eventInfo1.setEventTime(TimeStampUtil.getCurrentTimestamp());
//					eventInfo1.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
//					eventInfo1.setReasonCode("MERGE-0001");
//					eventInfo1.setReasonCodeType(GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT);
//					eventInfo1.setEventComment("Merge Hold by Source CST[" + srcCarrierName + "] Hold.");
//					
//					if(!dstLotData.getLotHoldState().equals("Y"))
//					{
//						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(dstLotData);
//						MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
////						String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(dstLotData.getKey().getLotName());
////						makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
//						LotServiceProxy.getLotService().makeOnHold(dstLotData.getKey(), eventInfo1, makeOnHoldInfo);
//						// -------------------------------------------------------------------------------------------------------------------------------------------
//					}
//					else
//					{	
//						kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////						String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(dstLotData.getKey().getLotName());
////						setEventInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
//						LotServiceProxy.getLotService().setEvent(dstLotData.getKey(), eventInfo1, setEventInfo);
//						// -------------------------------------------------------------------------------------------------------------------------------------------
//					}
					
					//2019.03.27_hsryu_Add Logic. if not exist thie Logic, ProductQuantity Error! 
					dstLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(dstLotData.getKey().getLotName());

					List<LotMultiHold> parentLotMultiHoldList = new ArrayList<LotMultiHold>();
					
					try {
						parentLotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select(" where lotName= ? ", new Object[]{srcLotData.getKey().getLotName()});
					} catch (Exception e) {
						parentLotMultiHoldList = null;
						eventLog.info("ParentLot [" + srcLotData.getKey().getLotName() + "] is not exist MultiHold" );
					}
					
					if(parentLotMultiHoldList!=null)
					{
						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(dstLotData);

						for(LotMultiHold parentLotMultiHold : parentLotMultiHoldList)
						{
							// 2019.04.10_hsryu_Change NoteComment. Mantis 0003462.
							String NoteComment = parentLotMultiHold.getEventComment() + ", inherit from lot[" +srcLotData.getKey().getLotName() + "]";
							// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
							//dstLotData.getUdfs().put("NOTE", NoteComment);
							
							//2019.02.27_hsryu_Modify "" -> get ParentLotMultiHoldInfo. EventName, EventUser, EventComment, ReasonCode.
							EventInfo eventInfo1 = EventInfoUtil.makeEventInfo(parentLotMultiHold.getEventName(), parentLotMultiHold.getEventUser(), NoteComment ,"HoldLot", parentLotMultiHold.getReasonCode());
							
							MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
							// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
							//makeOnHoldInfo.setUdfs(dstLotData.getUdfs());
							makeOnHoldInfo.getUdfs().put("NOTE", NoteComment);
							LotServiceProxy.getLotService().makeOnHold(dstLotData.getKey(), eventInfo1, makeOnHoldInfo);
							// 2019.04.10_hsryu_Change NoteComment. Mantis 0003462. 
							//eventInfo1.setEventComment(parentLotMultiHold.getEventComment() + "_Merge Hold by Source CST["+srcCarrierName+"] Hold.");
							
							try {
								dstLotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(dstLotData.getKey().getLotName(), parentLotMultiHold.getReasonCode(), parentLotMultiHold.getDepartment(),parentLotMultiHold.getHoldType(), eventInfo1);
							} catch (Exception e) {
								eventLog.warn(e);
							}
						}
						
						// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//						dstLotData.getUdfs().put("NOTE", "");
//						LotServiceProxy.getLotService().update(dstLotData);
						Map<String, String> updateUdfs = new HashMap<String, String>();
						updateUdfs.put("NOTE", "");
						MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(dstLotData, updateUdfs);
					}
				}
			}

			setNextInfo(doc, srcLotData, dstLotData);
		}
		
		/*// Added by smkang on 2018.12.20 - For synchronization of a carrier.
		for (String sourceCarrierName : sourceCarrierNameList) {
			// Added by smkang on 2018.11.09 - For synchronization of a carrier state and lot quantity, common method will be invoked.
	        try {
	        	// After deassignCarrier is executed, it is necessary to check this carrier is really changed to Available state.
	        	Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceCarrierName);
	        	if(StringUtil.equals(GenericServiceProxy.getConstantMap().Dur_Available, durableData.getDurableState()))
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
		}
		*/
		return doc;
	}
	
	private void setNextInfo(Document doc, Lot srcLot, Lot dstLot)
	{
		try
		{
			StringBuilder strComment = new StringBuilder();
			
			strComment.append(String.format("Lot[%s] is merged in CST[%s] from CST[%s]", srcLot.getKey().getLotName(), dstLot.getCarrierName(), srcLot.getCarrierName()));
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Note after TK OUT is nothing");
		}
	}
	
	/* YHU 20180208
	 * Copy FutureAction From Parent Lot To Child Lot
	 * */
	private void copyFutureAction(Lot sLot, Lot dLot, EventInfo eventInfo)
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
//							lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
							lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
							
							//2019.02.27_hsryu_LastEventName, LastEventUser, LastEventComment. not get EventInfo, get lotAction Info. 
							lotaction.setLastEventName(lotaction.getLastEventName());
							lotaction.setLastEventUser(lotaction.getLastEventUser());
							lotaction.setLastEventComment(lotaction.getLastEventComment());
							
							ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
						}
					}
				}
				catch(Throwable e)
				{
					lotaction.setLotName(dLot.getKey().getLotName());
					lotaction.setLastEventTime(eventInfo.getEventTime());
					
					// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//					lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
					lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
					
					//2019.02.27_hsryu_LastEventName, LastEventUser, LastEventComment. not get EventInfo, get lotAction Info. 
					lotaction.setLastEventName(lotaction.getLastEventName());
					lotaction.setLastEventUser(lotaction.getLastEventUser());
					lotaction.setLastEventComment(lotaction.getLastEventComment());
					
					ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
				}
			}
		}
		catch (Throwable e)
		{
			return;
		}
	}
		
	private Map<String, List<ProductP>> GetProductList(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);
		String destinationLot = SMessageUtil.getChildText(eleBody, "DESTINATIONLOTNAME", true);
		
		Map<String, List<ProductP>> lotsProductList = new LinkedHashMap<String, List<ProductP>>();
		
		for (Element eleDurable : SMessageUtil.getSubSequenceItemList(eleBody, "PRODUCTLIST", true))
		{
	    	ProductP p = new ProductP();
	    	p.setProductName(SMessageUtil.getChildText(eleDurable, "PRODUCTNAME", true));
	    	int position = Integer.parseInt(SMessageUtil.getChildText(eleDurable, "POSITION", true));
	    	p.setPosition(position);
	    	
	    	String lotName = SMessageUtil.getChildText(eleDurable, "LOTNAME", true);
	    	if(lotName.equalsIgnoreCase(destinationLot))
	    	{
	    		continue;
	    	}
	    	
	    	if(!lotsProductList.containsKey(lotName))
	    	{
	    		lotsProductList.put(lotName, new LinkedList<ProductP>());
	    	}
	    	
	    	lotsProductList.get(lotName).add(p);
		}
		
		return lotsProductList;
	}
}