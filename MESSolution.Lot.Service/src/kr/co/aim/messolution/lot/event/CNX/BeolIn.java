package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.LotService;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.impl.LotServiceImpl;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.jdom.Document;
import org.jdom.Element;

public class BeolIn extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("BankOut", getEventUser(), getEventComment(), "", "");
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String actionType = SMessageUtil.getBodyItemValue(doc, "ACTIONTYPE", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = "00001";
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = "00001";
		
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		String processFlowName = productSpecData.getProcessFlowName();
		String processFlowVersion = productSpecData.getProcessFlowVersion();
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);		
				
		//get release qty
		for (Element eLotData : lotList) 
		{
			String lotName = eLotData.getChild("LOTNAME").getText();
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
						
			if(!lotData.getProductSpecName().equals( productSpecName ) || 
					!lotData.getProductSpecVersion().equals( productSpecVersion ) )
			{
				lotData.setProductSpecName(productSpecName);
				lotData.setProductSpecVersion(productSpecVersion);
				lotData.setProcessFlowName(processFlowName);
				lotData.setProcessFlowVersion(processFlowVersion);
				
				if(lotData.getProcessFlowName().equals( productSpecData.getProcessFlowName() ))
				{
					lotData.setProcessOperationName(processOperationName);
					lotData.setProcessOperationVersion(processOperationVersion);
					
					LotServiceProxy.getLotService().update(lotData);
					SetEventInfo setEventInfo = new SetEventInfo();
					LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);									
				}
				else
				{
					String operationName = getFirstOperationName(productSpecData.getKey().getFactoryName(), productSpecData.getProcessFlowName());
					if(operationName!="")
					{
						lotData.setProcessOperationName(operationName);
						lotData.setProcessOperationVersion(processOperationVersion);
						
						LotServiceProxy.getLotService().update(lotData);
						SetEventInfo setEventInfo = new SetEventInfo();
						LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
					}
				}
			}
		}
		return doc;
	}
	
	public String getFirstOperationName(String factoryName,String processFlowName)
	{
		String operationName ="";
		String sql ="SELECT NODEID,NODETYPE,PROCESSOPERATIONNAME,PROCESSOPERATIONVERSION FROM V_PROCESSFLOWSEQ WHERE FACTORYNAME=:FACTORYNAME AND PROCESSFLOWNAME=:PROCESSFLOWNAME "
				 + " AND NODETYPE='ProcessOperation' ORDER BY POSITION ";
		Map<String,Object> args = new HashMap<String,Object>();
		args.put( "FACTORYNAME", factoryName );
		args.put( "PROCESSFLOWNAME", processFlowName );
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		if(result.size()>0)
		{
			operationName = (String)result.get( 0 ).get( "PROCESSOPERATIONNAME" );
		}
				
        return operationName;
				
	}

}
