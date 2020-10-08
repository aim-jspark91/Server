package kr.co.aim.messolution.userprofile.event;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductionPlan;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyProductionPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String no = SMessageUtil.getBodyItemValue(doc, "NO", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String workstage = SMessageUtil.getBodyItemValue(doc, "WORKSTAGE", false);
		String orderNo = SMessageUtil.getBodyItemValue(doc, "ORDERNO", false);
		String orderDate = SMessageUtil.getBodyItemValue(doc, "ORDERDATE", false);
		Timestamp date=TimeStampUtil.getTimestamp(orderDate);
		String workNo = SMessageUtil.getBodyItemValue(doc, "WORKNO", false);
		String productionSpec = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONSPEC", false);
		String construction = SMessageUtil.getBodyItemValue(doc, "CONSTRUCTION", false);
		String layer = SMessageUtil.getBodyItemValue(doc, "LAYER", false);
		String lotType = SMessageUtil.getBodyItemValue(doc, "LOTTYPE", false);
		String input = SMessageUtil.getBodyItemValue(doc, "INPUT", false);
		String output = SMessageUtil.getBodyItemValue(doc, "OUTPUT", false);
		String remark = SMessageUtil.getBodyItemValue(doc, "REMARK", false);
		
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyProductionPlan", getEventUser(), getEventComment(), null, null);
			
			//Modify
			ProductionPlan productionPlanData =ExtendedObjectProxy.getProductionPlanService().selectByKey(false, new Object[] {no});
			productionPlanData.setFactoryName(factoryName);
			productionPlanData.setWorkstage(workstage);
			productionPlanData.setOrderNo(orderNo);
			productionPlanData.setOrderDate(date);
			productionPlanData.setWorkNo(workNo);
			productionPlanData.setProductionSpec(productionSpec);
			productionPlanData.setConstruction(construction);
			productionPlanData.setLayer(layer);
			productionPlanData.setLotType(lotType);
			productionPlanData.setInput(input);
			productionPlanData.setOutput(output);
			productionPlanData.setRemark(remark);
			productionPlanData.setCreateTime(eventInfo.getEventTime());
			productionPlanData.setCreateUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getProductionPlanService().modify(eventInfo, productionPlanData);
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}

		return doc;
	}
}