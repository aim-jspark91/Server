package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotMultiHold;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class DataReset extends SyncHandler 
{
	public Object doWorks(Document doc) throws CustomException 
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String eventComment = SMessageUtil.getBodyItemValue(doc, "EVENTCOMMENT", true);
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", true);
		
		//start modify by jhiying on20191227 mantis :5514 
		 List<Lot> LotList=null;
		 try{
		 LotList = LotServiceProxy.getLotService().select("WHERE PROCESSOPERATIONNAME IN (SELECT PROCESSOPERATIONNAME "
		 		+ "FROM PROCESSOPERATIONSPEC WHERE DETAILPROCESSOPERATIONTYPE = ?) AND LOTPROCESSSTATE = ? "
		 		+ "AND LOTNAME IN (SELECT LOTNAME FROM CT_LOTMULTIHOLD A WHERE A.REASONCODE = ?) "
		 		+ "AND LOTNAME = ?",new Object[]{"DUMMY","WAIT","GHLD",lotName});
		 if(LotList!=null && LotList.size()>0)
		 {
			 
		 }
		 }catch (Exception e){
			 throw new CustomException("DATARESET-0001","");			 
		 }
		//end modify by jhiying on20191227 mantis :5514
			 
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Reset", getEventUser(), "", "", "");
		
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		
		Object[] keySet = new Object[]{lotData.getKey().getLotName(),reasonCode,department,eventComment,Integer.parseInt(seq)};
		try {
			LotMultiHold lotMultiHold =  ExtendedObjectProxy.getLotMultiHoldService().selectByKey(false, keySet);
			if(StringUtils.equals("Y", lotMultiHold.getResetFlag())){
				throw new CustomException("COMMON-0001","ResetFlag Update Error. already ResetFlag is Y");	
			}
			lotMultiHold.setResetFlag("Y");
			ExtendedObjectProxy.getLotMultiHoldService().modify(eventInfo, lotMultiHold);
		} catch (Exception e) {
			throw new CustomException("COMMON-0001","ResetFlag Update Error. already ResetFlag is Y");
		}
		
		List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
		SetEventInfo setEventInfo = MESLotServiceProxy.getLotInfoUtil().setEventInfo(lotData, lotData.getProductQuantity(), productUSequence);
		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//		lotData.getUdfs().put("NOTE", note);
//		setEventInfo.setUdfs(lotData.getUdfs());
		setEventInfo.getUdfs().put("NOTE", note);
		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		
		GenericServiceProxy.getMessageLogService().getLog().debug(new StringBuffer("document : ").append(JdomUtils.toString(doc)).toString());
		return doc;
	}
}
