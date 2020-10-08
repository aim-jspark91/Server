
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
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
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

public class CreateLotView extends SyncHandler {
	/**
	 * 200825 by xzquan : Create CreateLot
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sProductSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true); 
		String sProcessFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String sPriority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", true);
		String sProductionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", true);
		String sECCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		String sDepartmentName = SMessageUtil.getBodyItemValue(doc, "DEPARTMENTNAME", false);
		double sProductQuantity = Double.parseDouble(SMessageUtil.getBodyItemValue(doc, "PRODUCTQUANTITY", true));
		int sLotQuantity = Integer.parseInt(SMessageUtil.getBodyItemValue(doc, "LOTQUANTITY", true));
		Timestamp sDueDate = TimeStampUtil.getTimestamp(SMessageUtil.getBodyItemValue(doc, "DUEDATE", true)); 
		Element eLotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), "", "");

		//Product Spec Data
		ProductSpec specData = CommonUtil.getProductSpecByProductSpecName(sFactoryName, sProductSpecName, "00001");
		
		int createProductQty = 0;
		
		List<String> argSeq = new ArrayList<String>();
		List<String> lotNameList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("ArrayLotNaming", argSeq, eLotList.getChildren().size());
		
		//3. Create Lot
		for (int i=0; i<sLotQuantity; i++ )
		//Iterator iLot = eLotList.getChildren().iterator(); iLot.hasNext();)
		{
			String sLotName = lotNameList.get(i);

			//Create Lot
			Map<String, String> udfs = specData.getUdfs();
			
			udfs.put("ECCODE", sECCode);
			udfs.put("DEPARTMENTNAME", sDepartmentName);
			
			CreateInfo createInfo =  MESLotServiceProxy.getLotInfoUtil().createInfo(sDueDate,
					sFactoryName,
					sLotName, 
					"",
					Long.parseLong(sPriority),
					sProcessFlowName,
					GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
					"",	"", "",
					sProductionType, 
					sProductQuantity,
					"", "", "",
					sProductSpecName, "00001",
					specData.getProductType(), 
					specData.getSubProductType(), 
					specData.getSubProductUnitQuantity1(),
					0,
					udfs);
			
			Lot lotData = MESLotServiceProxy.getLotServiceImpl().createLot(eventInfo, createInfo);			
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
