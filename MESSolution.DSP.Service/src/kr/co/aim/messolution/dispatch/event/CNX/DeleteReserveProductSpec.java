package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspReserveProduct;
import kr.co.aim.messolution.extended.object.management.data.DspReserveProductNon;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteReserveProductSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
	
		List<Element> reserveList = SMessageUtil.getBodySequenceItemList(doc, "RESERVELIST", true);
		String reserveFlag = SMessageUtil.getBodyItemValue(doc, "RESERVEFLAG", true);
		
		
		for(Element reserve : reserveList)
		{
				
		String machineName = SMessageUtil.getChildText(reserve, "MACHINENAME", true);
		String unitName = SMessageUtil.getChildText(reserve, "UNITNAME", true);
		String portName = SMessageUtil.getChildText(reserve, "PORTNAME", true);
		String reserveName = SMessageUtil.getChildText(reserve, "RESERVENAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", getEventUser(), getEventComment(), null, null);
		if(StringUtil.equals("TRUE", reserveFlag.toUpperCase())){
			DspReserveProduct reserveProduct = null;
			try
			{
				reserveProduct = ExtendedObjectProxy.getDspReserveProductService().selectByKey(false, new Object[] {machineName, unitName, portName,reserveName });
						
			}
			catch (Exception ex)
			{
				reserveProduct = null;
				
			}
			
			if(reserveProduct == null)
			{
				throw new CustomException("DspReserveProduct Delete Error");
			}
			
			ExtendedObjectProxy.getDspReserveProductService().delete(reserveProduct);
		}
		else{
			DspReserveProductNon dspReserveProductNon = null;
			try
			{
				dspReserveProductNon = ExtendedObjectProxy.getDspReserveProductNonService().selectByKey(false, new Object[] {machineName, unitName, portName,reserveName });
						
			}
			catch (Exception ex)
			{
				dspReserveProductNon = null;
				
			}
			
			if(dspReserveProductNon == null)
			{
				throw new CustomException("DspReserveProductNon Delete Error");
			}
			
			ExtendedObjectProxy.getDspReserveProductNonService().delete(dspReserveProductNon);
		}
		

		}
		
		return doc;
	}

}
