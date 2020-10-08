package kr.co.aim.messolution.product.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;

import org.jdom.Document;

public class GlassTurn extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Turn", getEventUser(), getEventComment(), null, null);
		
		try
		{
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			
			MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
			
			if (CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("EVA"))
			{
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("DEGREE", "Reversed");
				
				MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);
			}
		}
		catch(CustomException e)
		{
			throw e;			
		}
	}
}