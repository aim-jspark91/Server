package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;
import org.jdom.Element;

public class CompleteForceSample extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException 
	{
		String lotName 			   = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String returnFlowName 	   = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true);
		String returnOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATION", true);
		String beforeOperationName = "";
		   
		Element element = doc.getDocument().getRootElement();
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		Map<String, String> udfs = MESLotServiceProxy.getLotServiceUtil().setNamedValueSequence(lotName, element);
		
		//GVO Rule.
		//MESLotServiceProxy.getLotServiceUtil().updateProductGrades(lotName, productGrade);
 
		beforeOperationName = MESLotServiceProxy.getLotServiceUtil().getBeforeOperationName(returnFlowName, returnOperationName);
		udfs.put("BEFOREOPERATIONNAME", beforeOperationName);
		
		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteForceSample", getEventUser(), getEventComment(), "", "");
		
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		if(StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
		{
			changeSpecInfo.setAreaName(lotData.getAreaName());
			changeSpecInfo.setDueDate(lotData.getDueDate());
			changeSpecInfo.setFactoryName(lotData.getFactoryName());
			changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
			changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
			changeSpecInfo.setLotState(lotData.getLotState());
			changeSpecInfo.setNodeStack("");
			changeSpecInfo.setPriority(lotData.getPriority());
			changeSpecInfo.setProcessFlowName(returnFlowName);
			changeSpecInfo.setProcessFlowVersion("00001");
			changeSpecInfo.setProcessOperationName(returnOperationName);
			changeSpecInfo.setProcessOperationVersion("00001");
			changeSpecInfo.setProductionType(lotData.getProductionType());
			//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
			//changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
			changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
			changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
			changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
			changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
			//changeSpecInfo.setProductUdfs(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
			changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());
			
			Map<String, String> lotUdfs = lotData.getUdfs();	
			
			lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName()); 
			lotUdfs.put("BEFOREFLOWNAME"	 , lotData.getProcessFlowName()); 
			
			changeSpecInfo.setUdfs(lotUdfs);
			
			String sql = "SELECT NODEID FROM NODE " + " WHERE FACTORYNAME = ? "
					+ "   AND PROCESSFLOWNAME = ? "
					+ "   AND PROCESSFLOWVERSION = ? "
					+ "   AND NODEATTRIBUTE1 = ? " + "   AND NODEATTRIBUTE2 = '00001' "
					+ "   AND NODETYPE = 'ProcessOperation' ";
	
			Object[] bind = new Object[] { changeSpecInfo.getFactoryName(),changeSpecInfo.getProcessFlowName(), 
					changeSpecInfo.getProcessFlowVersion(), changeSpecInfo.getProcessOperationName()};
	
			String[][] result = null;
			result = greenFrameServiceProxy.getSqlTemplate().queryForStringArray(sql, bind);
	
			if (result.length == 0) 
			{
				throw new CustomException("Node-0001", 
						lotData.getProductSpecName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessOperationName());
			} 
			else{
				String sToBeNodeStack = (String)result[0][0];
				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}
		}
		else
		{
			throw new CustomException("LOT-9003", lotName+"(LotProcessState:"+lotData.getLotProcessState()+")");
		}
		
		Lot afterLotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		
		afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
						 
		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
					 
		if( processFlowData.getProcessFlowType().equals("Inspection"))	
		{
//			MESLotServiceProxy.getLotServiceUtil().deleteForceSampling(
//					eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName());
			ExtendedObjectProxy.getSampleLotService().deleteForceSampling(eventInfo, lotName,
					lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName());
		}
		
		return doc;
	}
}
