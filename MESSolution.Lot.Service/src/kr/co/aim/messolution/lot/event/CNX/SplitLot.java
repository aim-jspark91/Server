package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
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
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class SplitLot extends SyncHandler {

	@Override
		
	public Object doWorks(Document doc) throws CustomException {
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String parentLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String parentDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", false);
		
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), null, null);
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(parentLotName);
		
		//validation
		//CommonValidation.checkLotState(lotData);
		//CommonValidation.checkLotProcessState(lotData);
		
		
				
		if (!parentDurableName.isEmpty())
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(parentDurableName);
			CommonValidation.CheckDurableHoldState(durableData);
		}
		
		Boolean isLotHold = false;
		isLotHold = lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().FLAG_Y);
		
		//Call Function
		Map<String, String> childInfo = LotSplit(doc, lotData, isLotHold);
//		Element eleBody = SMessageUtil.getBodyElement(doc);
//		Map<String, String> childInfo = new HashMap<String, String>();
//		
//		for (Element eleDurable : SMessageUtil.getSubSequenceItemList(eleBody, "DURABLELIST", true))
//		{
//			//Check Durable Hold
//			String desDurableName = SMessageUtil.getChildText(eleDurable, "DURABLENAME", true);			
//			Durable desDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(desDurableName);			
//			CommonValidation.CheckDurableHoldState(desDurableData);
//			
//			//Get ProductList
//			List<Element> eleProductList = SMessageUtil.getSubSequenceItemList(eleDurable, "PRODUCTLIST", true);			
//			List<ProductP> productList = new ArrayList<ProductP>();
//		    for(Element eleProduct : eleProductList)
//		    {
////		    	List<Element> eleProductInfo = SMessageUtil.getSubSequenceItemList(eleProduct, "PRODUCT", true);
//		    	
//		    	ProductP p = new ProductP();
//		    	p.setProductName(SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true));
//		    	int position = Integer.parseInt(SMessageUtil.getChildText(eleProduct, "POSITION", true));
//		    	p.setPosition(position);
//		    	productList.add(p);
//		    }			
//			
//			//Get Child Lot Name
//			List<String> argSeq = new ArrayList<String>();
//			List<String> lotNameList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("GlassLotNaming", argSeq, 1);
//			int i = 0;
//			String childLotName = lotNameList.get(i++);
//			
//			//Get SplitInfo
//			String productQuantity = String.valueOf(eleProductList.size());
//			SplitInfo splitInfo = MESLotServiceProxy.getLotInfoUtil().splitInfo(lotData, desDurableName, childLotName, productList, productQuantity);
//			
//			Lot childLot = MESLotServiceProxy.getLotServiceImpl().splitLot(eventInfo, lotData, splitInfo);
//			
//			childInfo.put(childLot.getCarrierName(), childLot.getKey().getLotName());
//		}
		
		//Write Result
		setNextInfo(doc, childInfo);
		
		return doc;
	}
	
	/* YHU 20180208
	 * Split Work
	 * */
	private Map<String, String> LotSplit(Document doc, Lot lotData, Boolean isLotHold) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);
		Map<String, String> childInfo = new HashMap<String, String>();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), null, null);
		eventInfo.setBehaviorName("ARRAY");
		
		String desDurableName = SMessageUtil.getBodyItemValue(doc, "TARGETDURABLENAME", false);	
		List<ProductP> productList = new ArrayList<ProductP>();
		String srcCarrierName = lotData.getCarrierName();
		
		//2019.01.08_hsryu_Check SortJob!
		CommonValidation.checkExistSortJob(lotData.getFactoryName(), srcCarrierName);
		CommonValidation.checkExistSortJob(lotData.getFactoryName(), desDurableName);
		
		for (Element eleDurable : SMessageUtil.getSubSequenceItemList(eleBody, "DURABLELIST", true))
		{
			//Check Durable Hold
			Durable desDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(desDurableName);			
			CommonValidation.CheckDurableHoldState(desDurableData);
			CommonValidation.checkDurableDirtyState(desDurableData);
			
			//Get ProductList
			List<Element> eleProductList = SMessageUtil.getSubSequenceItemList(eleDurable, "PRODUCTLIST", true);			
			
		    for(Element eleProduct : eleProductList)
		    {		    	
		    	ProductP p = new ProductP();
		    	p.setProductName(SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true));
		    	int position = Integer.parseInt(SMessageUtil.getChildText(eleProduct, "POSITION", true));
		    	p.setPosition(position);
		    	productList.add(p);
		    }
		    
			//Get Child Lot Name
		    String lotName = StringUtil.substring(lotData.getKey().getLotName(), 0, 8);
			List<String> argSeq = new ArrayList<String>();
			argSeq.add(lotName);
			List<String> lstName = NameServiceProxy.getNameGeneratorRuleDefService().generateName("GlassSplitLotNaming", argSeq, 1);
			
			int i = 0;
			String childLotName = lstName.get(i++);
			
			//Get SplitInfo
			String productQuantity = String.valueOf(eleProductList.size());
			SplitInfo splitInfo = MESLotServiceProxy.getLotInfoUtil().splitInfo(lotData, desDurableName, childLotName, productList, productQuantity);
			
//			lotData = MESLotServiceProxy.getLotServiceImpl().splitLot(eventInfo, lotData, splitInfo);
			MESLotServiceProxy.getLotServiceImpl().splitLot(eventInfo, lotData, splitInfo);
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Lot childLot = MESLotServiceProxy.getLotInfoUtil().getLotData(childLotName);
			Lot childLot = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(childLotName));
			
			/**************************** Check Mixed WO Name *********************************/
			//2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
			String desWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(childLot);
			
			if(!StringUtils.isEmpty(desWOName)) {
				if(!StringUtils.equals(childLot.getProductRequestName(), desWOName)) {
					childLot.setProductRequestName(desWOName);
					LotServiceProxy.getLotService().update(childLot);
					
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					String condition = "where lotname=?" + " and timekey= ? " ;
//					Object[] bindSet = new Object[]{childLot.getKey().getLotName(), eventInfo.getEventTimeKey()};
//					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//					LotHistory lotHistory = arrayList.get(0);
					LotHistoryKey lotHistoryKey = new LotHistoryKey();
					lotHistoryKey.setLotName(childLot.getKey().getLotName());
					lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
					LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
					
					lotHistory.setProductRequestName(desWOName);
					LotServiceProxy.getLotHistoryService().update(lotHistory);
				}
			}
			
			try {
				//2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
				lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());
				
				String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);
				
				if(!StringUtils.isEmpty(sourceWOName)) {
					if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)) {
						lotData.setProductRequestName(sourceWOName);
						LotServiceProxy.getLotService().update(lotData);
						
						// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//						String condition = "where lotname=?" + " and timekey= ? " ;
//						Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
//						List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//						LotHistory lotHistory = arrayList.get(0);
						LotHistoryKey lotHistoryKey = new LotHistoryKey();
						lotHistoryKey.setLotName(lotData.getKey().getLotName());
						lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
						LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
						
						lotHistory.setProductRequestName(sourceWOName);
						LotServiceProxy.getLotHistoryService().update(lotHistory);
					}
				}
			}
			catch(Throwable e) {
				eventLog.warn("Fail Update WO!");
			}
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			childLot = MESLotServiceProxy.getLotInfoUtil().getLotData(childLot.getKey().getLotName());
//			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
			childLot = LotServiceProxy.getLotService().selectByKeyForUpdate(childLot.getKey());
			lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());
			/*********************************************************************************/
			
			if(StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
			{
				childLot.setProcessOperationName("-");
				childLot.setProcessOperationVersion("");
				childLot.setLotState(GenericServiceProxy.getConstantMap().Lot_Completed);
				childLot.setLotProcessState("");
				childLot.setLotHoldState("");
				LotServiceProxy.getLotService().update(childLot);
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				String condition = "where lotname=?" + " and timekey= ? " ;
//				Object[] bindSet = new Object[]{childLot.getKey().getLotName(),eventInfo.getEventTimeKey()};
//				List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//				LotHistory lotHistory = arrayList.get(0);
				LotHistoryKey lotHistoryKey = new LotHistoryKey();
				lotHistoryKey.setLotName(childLot.getKey().getLotName());
				lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
				LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
				
				lotHistory.setProcessOperationName("-");
				lotHistory.setProcessOperationVersion("");
				lotHistory.setLotState(GenericServiceProxy.getConstantMap().Lot_Completed);
				lotHistory.setLotProcessState("");
				lotHistory.setLotHoldState("");
				LotServiceProxy.getLotHistoryService().update(lotHistory);
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(childLot.getKey().getLotName());
				List<Product> pProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(childLot.getKey().getLotName());
				
				for(Product productData : pProductList)
				{
					productData.setProcessOperationName("-");
					productData.setProcessOperationVersion("");
					productData.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
					productData.setProductProcessState("");
					productData.setProductHoldState("");
					
					ProductServiceProxy.getProductService().update(productData);
					
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                    String pCondition = " where productname=?" + " and timekey= ? " ;
//                    Object[] pBindSet = new Object[]{productData.getKey().getProductName(), eventInfo.getEventTimeKey()};
//                    List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
//                    ProductHistory producthistory = pArrayList.get(0);
					ProductHistoryKey productHistoryKey = new ProductHistoryKey();
					productHistoryKey.setProductName(productData.getKey().getProductName());
					productHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
					ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
                    
                    producthistory.setProcessOperationName("-");
                    producthistory.setProcessOperationVersion("");
                    producthistory.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
                    producthistory.setProductProcessState("");
                    producthistory.setProductHoldState("");
                    ProductServiceProxy.getProductHistoryService().update(producthistory);                        
				}
			}
			else
			{
				//Set Lot Hold State according to Parent
				if(isLotHold)
				{				
					setHoldChildLot(lotData,srcCarrierName, childLot);				
				}
				
				//FutureAction
				copyFutureAction(lotData, childLot,eventInfo);
			}
			
			//2018.11.12_hsryu_if all ProductGrade = 'S', LotGrade = 'S'.
			/*******************CHECK ALL PRODUCTGRADE S**************************************/
			List<Product> aProductList = LotServiceProxy.getLotService().allUnScrappedProducts(childLot.getKey().getLotName());
			
			boolean isSGrade = true;
			
			for(Product productData : aProductList)
			{
				if(!StringUtils.equals(productData.getProductGrade(), GenericServiceProxy.getConstantMap().LotGrade_S))
				{
					isSGrade = false;
				}
			}
			
			if(isSGrade)
			{
				childLot.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_S);
				LotServiceProxy.getLotService().update(childLot);
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				String condition = "where lotname=?" + " and timekey= ? " ;
//				Object[] bindSet = new Object[]{childLot.getKey().getLotName(),eventInfo.getEventTimeKey()};
//				List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//				LotHistory lotHistory = arrayList.get(0);
				LotHistoryKey lotHistoryKey = new LotHistoryKey();
				lotHistoryKey.setLotName(childLot.getKey().getLotName());
				lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
				LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
				
				lotHistory.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_S);
				LotServiceProxy.getLotHistoryService().update(lotHistory);
			}
			/********************************************************************************/
			//2019.07._kns_if all ProductGrade = 'S', LotGrade = 'S' - parentLotGrade
			aProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			
			isSGrade = true;
			
			for(Product productData : aProductList)
			{
				if(!StringUtils.equals(productData.getProductGrade(), GenericServiceProxy.getConstantMap().LotGrade_S))
				{
					isSGrade = false;
					break;
				}
			}
			
			if(isSGrade)
			{
				lotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_S);
				LotServiceProxy.getLotService().update(lotData);
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				String condition = "where lotname=?" + " and timekey= ? " ;
//				Object[] bindSet = new Object[]{childLot.getKey().getLotName(),eventInfo.getEventTimeKey()};
//				List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//				LotHistory lotHistory = arrayList.get(0);
				LotHistoryKey lotHistoryKey = new LotHistoryKey();
				lotHistoryKey.setLotName(lotData.getKey().getLotName());
				lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
				LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
				
				lotHistory.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_S);
				LotServiceProxy.getLotHistoryService().update(lotHistory);
			}
			/********************************************************************************/
			
			
			childInfo.put(childLot.getCarrierName(), childLot.getKey().getLotName());
		}
		
		return childInfo;
	}
	
	private void setHoldChildLot(Lot parent, String srcCarrierName, Lot child) throws CustomException
	{
		List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(child);

//		EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("HoldBySplit", getEventUser(), "", "", "");
//		
//		eventInfo1.setEventTime(TimeStampUtil.getCurrentTimestamp());
//		eventInfo1.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
//		eventInfo1.setReasonCode("SPLIT-0001");
//		eventInfo1.setReasonCodeType(GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT);
//		eventInfo1.setEventComment("Split Hold by Source CST["+srcCarrierName+"] Hold.");
//		
//		if(!child.getLotHoldState().equals("Y"))
//		{
//			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(child);
//			MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
////			String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(child.getKey().getLotName());
////			makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
//			LotServiceProxy.getLotService().makeOnHold(child.getKey(), eventInfo1, makeOnHoldInfo);
//		}
//		else
//		{	
//			SetEventInfo setEventInfo = new SetEventInfo();
////			String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(child.getKey().getLotName());
////			setEventInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
//			LotServiceProxy.getLotService().setEvent(child.getKey(), eventInfo1, setEventInfo);			
//			// -------------------------------------------------------------------------------------------------------------------------------------------
//		}

		List<LotMultiHold> parentLotMultiHoldList = new ArrayList<LotMultiHold>();
		
		try {
			parentLotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select(" where lotName= ? ", new Object[]{parent.getKey().getLotName()});
		} catch (Exception e) {
			parentLotMultiHoldList = null;
			eventLog.info("ParentLot [" + parent.getKey().getLotName() + "] is not exist MultiHold" );
		}
		
		if(parentLotMultiHoldList!=null)
		{
			for(LotMultiHold parentLotMultiHold : parentLotMultiHoldList)
			{
				// 2019.04.10_hsryu_Change NoteComment. Mantis 0003462.
				String NoteComment = parentLotMultiHold.getEventComment() + ", inherit from lot[" +parent.getKey().getLotName() + "]";
				// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//				child.getUdfs().put("NOTE", NoteComment);
				
				//2019.02.27_hsryu_Modify "" -> get ParentLotMultiHoldInfo. EventName, EventUser, EventComment, ReasonCode.
				EventInfo eventInfo1 = EventInfoUtil.makeEventInfo(parentLotMultiHold.getEventName(), parentLotMultiHold.getEventUser(), NoteComment,"HoldLot", parentLotMultiHold.getReasonCode());
				
				MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
				// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//				makeOnHoldInfo.setUdfs(child.getUdfs());
				makeOnHoldInfo.getUdfs().put("NOTE", NoteComment);
				LotServiceProxy.getLotService().makeOnHold(child.getKey(), eventInfo1, makeOnHoldInfo);
				//eventInfo1.setEventComment(parentLotMultiHold.getEventComment() + "_Split Hold by Source CST["+srcCarrierName+"] Hold.");
				
				try {
					child = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(child.getKey().getLotName(), parentLotMultiHold.getReasonCode(), parentLotMultiHold.getDepartment(),parentLotMultiHold.getHoldType(), eventInfo1);
				} catch (Exception e) {
					eventLog.warn(e);
				}
			}
			
			// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//			child.getUdfs().put("NOTE", "");
//			LotServiceProxy.getLotService().update(child);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(child, updateUdfs);
		}
	}
	
	/* YHU 20180208
	 * Copy FutureAction From Parent Lot To Child Lot
	 * */
	private void copyFutureAction(Lot parent, Lot child, EventInfo eventInfo)
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
				
				lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastPositionOfLotAction(child,lotaction.getProcessFlowName(),lotaction.getProcessOperationName()));
				
				lotaction.setLotName(child.getKey().getLotName());
				lotaction.setPosition(lastPosition+1);
				lotaction.setLastEventTime(eventInfo.getEventTime());
				
				// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//				lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
				
				ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
			}
		}
		catch (Throwable e)
		{
			return;
		}		
	}
	
	/**
	 * must be in here at last point of event
	 * @author swcho
	 * @since 2015-11-27
	 * @param doc
	 * @param childInfo
	 */
	private void setNextInfo(Document doc, Map<String, String> childInfo)
	{
		try
		{
			StringBuilder strComment = new StringBuilder();
			
			for (String durableName : childInfo.keySet())
			{
				strComment.append(String.format("Lot[%s] is divided into CST[%s]", childInfo.get(durableName), durableName)).append("\n");
			}
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Note after TK OUT is nothing");
		}
	}
}
