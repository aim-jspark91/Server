
package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.CreateInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;

import org.jdom.Document;
import org.jdom.Element;

public class CreateLot extends SyncHandler {
	/**
	 * 151106 by xzquan : Create CreateLot
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sProductRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "ASSIGNEDMACHINENAME", true);
		String sPlanReleasedTime = SMessageUtil.getBodyItemValue(doc, "PLANRELEASEDTIME", true);
		String sProcessFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String sDefaultPriority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", true);
		String sHoldFlag = SMessageUtil.getBodyItemValue(doc, "HOLDFLAG", true);
		String sEndBank = SMessageUtil.getBodyItemValue(doc, "ENDBANK", false);
		Element eLotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), "", "");

		//Product Request Key & Data
		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(sProductRequestName);
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		
		//Product Request Plan Key & Data
		ProductRequestPlanKey pPlanKey = new ProductRequestPlanKey();
		pPlanKey.setProductRequestName(sProductRequestName);
		pPlanKey.setAssignedMachineName(sMachineName);
		pPlanKey.setPlanReleasedTime(TimeUtils.getTimestamp(sPlanReleasedTime));
		ProductRequestPlan pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
		
		//Product Spec Data
		ProductSpec specData = GenericServiceProxy.getSpecUtil().getProductSpec(sFactoryName, pData.getProductSpecName(),
									GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		
		//Set Production Type
		String sProductionType = specData.getProductionType();
		
		//Machine Data
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);

		//1. Velidation
        //1) Check Product Request Plan Hold State
/*		if(pPlanData.getProductRequestHoldState().equals(GenericServiceProxy.getConstantMap().Prq_OnHold))
		{
			throw new CustomException("PRODUCTREQUEST-0001", sProductRequestName);
		}*/
		
		//2) Check Product Request Plan State
		if(pPlanData.getProductRequestPlanState().equals(GenericServiceProxy.getConstantMap().PrqPlan_Completed))
		{
			throw new CustomException("PRODUCTREQUEST-0007", sProductRequestName);
		}
		
		//3) Check Current Time & Due Date
//		if(eventInfo.getEventTime().after(pData.getPlanFinishedTime()))
//		{
//			throw new CustomException("PRODUCTREQUEST-0019", eventInfo.getEventTime().toString(), pData.getPlanFinishedTime().toString());
//		}
		
		//4) All Product Qty Check(Plan vs Released Qty, CreateProductQty)
		//4.1 Get Created Lot Product Quantity Sum
		int createProductQty = 0;
		try
		{
			String condition = "WHERE productRequestName = ? AND machineName = ? AND planReleasedTime = ? AND reserveState <> ?";
			Object[] bindSet = new Object[] {pPlanKey.getProductRequestName(), pPlanKey.getAssignedMachineName(), 
					pPlanKey.getPlanReleasedTime(), GenericServiceProxy.getConstantMap().RESV_STATE_END};
			
			List<ReserveLot> lotList = new ArrayList<ReserveLot>();
			
			lotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);
			for(ReserveLot dLot : lotList)
			{
				LotKey lKey = new LotKey(dLot.getLotName());
				createProductQty += LotServiceProxy.getLotService().selectByKey(lKey).getCreateProductQuantity();
			}
		}
		catch(Exception e)
		{
			eventLog.warn(String.format("Created Product Quantity is 0.", ""));
			createProductQty = 0;
		}
		
		//4.2 Get Product Quantity
		int iProductQuantity = 0;
		for (@SuppressWarnings("rawtypes")
		Iterator iLot = eLotList.getChildren().iterator(); iLot.hasNext();)
		{
			Element eLot = (Element) iLot.next();
			int iProductQty = Integer.parseInt(SMessageUtil.getChildText(eLot, "PRODUCTQUANTITY", true));
			iProductQuantity += iProductQty;
		}
		
		if(pPlanData.getPlanQuantity() < createProductQty + pPlanData.getReleasedQuantity() + iProductQuantity)
		{
			throw new CustomException("PRODUCTREQUEST-0014", "");
		}
		
		//20171220, kyjung
		//2. Generate Lot Name List
		//Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		//nameRuleAttrMap.put("FACTORYNAME", sFactoryName);
		//nameRuleAttrMap.put("PRODUCTSPECNAME", specData.getKey().getProductSpecName());
		
		//List<String> lotNameList = CommonUtil.generateNameByNamingRule("GlassLotNaming", nameRuleAttrMap, eLotList.getChildren().size());
		
		List<String> argSeq = new ArrayList<String>();
		//20200910 mgkang "GlassLotNaming" -> "ArrayLotNaming"
		//List<String> lotNameList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("ArrayLotNaming", argSeq, eLotList.getChildren().size());
		List<String> lotNameList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("GlassLotNaming", argSeq, eLotList.getChildren().size());
		
		int i = 0;
		
		//3. Create Lot
		for (@SuppressWarnings("rawtypes")
		Iterator iLot = eLotList.getChildren().iterator(); iLot.hasNext();)
		{			
			Element eLot = (Element) iLot.next();
			double iProductQty = Double.parseDouble(SMessageUtil.getChildText(eLot, "PRODUCTQUANTITY", true));
			//1711.dmlee
			String sPriority = sDefaultPriority;
			//---------
			int iNo = MESLotServiceProxy.getLotServiceUtil().reserveLotPosition(pPlanData);
			String sLotName = lotNameList.get(i++);

			//Create Lot
			Map<String, String> udfs = specData.getUdfs();
			
			udfs.put("ECCODE", pPlanData.getUdfs().get("ECCode"));
			udfs.put("DEPARTMENTNAME", pPlanData.getUdfs().get("departmentName"));
			
			Timestamp tLotDueDate = pData.getPlanFinishedTime();
			CreateInfo createInfo =  MESLotServiceProxy.getLotInfoUtil().createInfo(tLotDueDate,
					specData.getKey().getFactoryName(),
					sLotName, "",
					Long.parseLong(sPriority),
					sProcessFlowName,
					GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
					"",
					"", "",
					sProductionType, 
					iProductQty,
					sProductRequestName, "", "",
					specData.getKey().getProductSpecName(), specData.getKey().getProductSpecVersion(),
					specData.getProductType(), specData.getSubProductType(), specData.getSubProductUnitQuantity1(),
					0,
					udfs);
			
			Lot lotData = MESLotServiceProxy.getLotServiceImpl().createLot(eventInfo, createInfo);
			
			//Reserve
			try
			{

				ReserveLot reserveData = new ReserveLot(sMachineName, sLotName);
				Map<String,String> reserveUdf = reserveData.getUdfs();

				reserveData.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_RESV);
				reserveData.setMachineName(sMachineName);
				reserveData.setProductSpecName(specData.getKey().getProductSpecName());
				reserveData.setPosition(iNo);
				reserveData.setReserveTimeKey(eventInfo.getEventTimeKey());
				reserveData.setProductRequestName(sProductRequestName);
				reserveData.setPlanReleasedTime(pPlanKey.getPlanReleasedTime());
				reserveData.setReserveUser(eventInfo.getEventUser());
				reserveData.setHoldFlag(sHoldFlag);
				reserveData.setDepartmentName(pPlanData.getUdfs().get("departmentName"));
				reserveData = ExtendedObjectProxy.getReserveLotService().create(eventInfo, reserveData);
			}
			catch (CustomException ex)
			{
				eventLog.warn(String.format("Lot[%s] is failed to Reserve", sLotName));
			}
		}
		
		List<Element> eleLotList = new ArrayList<Element>();

		for (String lotName : lotNameList)
		{
			eleLotList.add(setCreatedLotList(lotName));
		}
		
		//call by value so that reply would be modified
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "RETURNLOTLIST", eleLotList);
		return doc;
	}
	
	/**
	 * scribe Lot as form of Element type
	 * @author xzquan
	 * @since 2015.11.04
	 * @param lotName
	 * @return
	 */
	private Element setCreatedLotList(String lotName)
	{
		Element eleLot = new Element("LOT");
		
		try
		{
			XmlUtil.addElement(eleLot, "LOTNAME", lotName);
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Scribing Lot[%s] is failed so that skip", lotName));
		}
		
		return eleLot;
	}
}
