package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.service.ConsumableServiceUtil;
import kr.co.aim.messolution.durable.service.DurableServiceImpl;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaterialConsumed;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.generic.util.StringUtils;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.service.LotInfoUtil;
import kr.co.aim.messolution.lot.service.LotServiceUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.LotService;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.impl.LotServiceImpl;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.org.apache.bcel.internal.generic.RETURN;

public class UnbondMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Remove", getEventUser(), getEventComment(), "", "");
		
		Element body = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		List<Element> eMaterialList= SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
		
		for (Element eMateirl : eMaterialList)
		{
			String productName = eMateirl.getChildText("PRODUCTNAME");
			String materialName = eMateirl.getChildText("MATERIALNAME");
			String materialID = eMateirl.getChildText("MATERIALID");			
			
			ExtendedObjectProxy.getMaterialConsumedService().delete(MaterialConsumed.class, new Object[] {productName,materialName,materialID});
		}
		
		eventLog.info( "Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey() );
		
		return doc;
	}
}
