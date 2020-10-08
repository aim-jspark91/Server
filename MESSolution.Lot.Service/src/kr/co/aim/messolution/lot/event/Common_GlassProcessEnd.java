package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

import org.jdom.Document;

public class Common_GlassProcessEnd extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		
		//for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		HashMap<String, String> assignCarrierUdfs = new HashMap<String, String>();
		HashMap<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		
		String lotJudge = "";			
		
		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(lotName);
				
		lotData.getUdfs().put("UNITNAME", "");
		
		String decideSampleNodeStack = "";
		
		//TK OUT
		MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, lotData, null, 
												lotData.getCarrierName() , lotJudge, "", "",
												productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, false,null);
		
		// Repair
		//afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().startAlteration(eventInfo, afterTrackOutLot, afterTrackOutLot.getLotGrade());
		
		//success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
	}
}
