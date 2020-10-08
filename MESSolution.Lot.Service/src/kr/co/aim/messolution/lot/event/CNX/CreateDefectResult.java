package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectResult;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CreateDefectResult extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		
		String panelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", false);
		String cutNumber = SMessageUtil.getBodyItemValue(doc, "CUTNUMBER", false);
		String coordiNate_x = SMessageUtil.getBodyItemValue(doc, "COORDINATE_X", false);
		String coordiNate_y = SMessageUtil.getBodyItemValue(doc, "COORDINATE_Y", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String originLayer = SMessageUtil.getBodyItemValue(doc, "ORIGINLAYER", false);
		String defectCode = SMessageUtil.getBodyItemValue(doc, "DEFECTCODE", true);
		String defectSize = SMessageUtil.getBodyItemValue(doc, "DEFECTSIZE", false);
		String autoJudgeFlag = SMessageUtil.getBodyItemValue(doc, "AUTOJUDGEFLAG", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		String inspectionTime = SMessageUtil.getBodyItemValue(doc, "INSPECTIONTIME", true);
		
		String holdFlag = SMessageUtil.getBodyItemValue(doc, "HOLDFLAG", true);
		String mailFlag = SMessageUtil.getBodyItemValue(doc, "MAILFLAG", true);
		String userGroupName = SMessageUtil.getBodyItemValue(doc, "USERGROUPNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateDefectResult", this.getEventUser(), this.getEventComment(), "", "");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sqlResult =null;
        
		String sql =  " SELECT MAX(DEFECTRESULTID) AS MAX FROM CT_DEFECTRESULT " ;
     
         Map<String, String> bindMap = new HashMap<String, String>();
         
         try {
        	 sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		} catch (Exception e) {
			// TODO: handle exception
		}
         
         long maxNum;
		if(sqlResult==null || sqlResult.size()==0 ||  sqlResult.get(0).get("MAX")==null   )
		{
			maxNum=1;
		}else
		{
			maxNum = Long.valueOf( sqlResult.get(0).get("MAX").toString() )+1;
		}
         
		DefectResult defectResult = new DefectResult(maxNum);

		defectResult.setFactoryName(factoryName);
		defectResult.setProductSpecName(productSpecName);
		defectResult.setProductSpecVersion(productSpecVersion);
		defectResult.setLotName(lotName);
		defectResult.setCarrierName(carrierName);
		defectResult.setProductName(productName);
		defectResult.setPanelName(panelName);
		

//		if(!StringUtils.isEmpty(cutNumber)){
//			defectResult.setCutNumber(Integer.parseInt(cutNumber));
//		}
		defectResult.setCutNumber(cutNumber);
//		if(!StringUtils.isEmpty(coordiNate_x)){
//			defectResult.setCoordiNate_x(Integer.parseInt(coordiNate_x));
//		}
		defectResult.setCoordiNate_x(coordiNate_x);
//		if(!StringUtils.isEmpty(coordiNate_y)){
//			defectResult.setCoordiNate_y(Integer.parseInt(coordiNate_y));
//		}
		defectResult.setCoordiNate_y(coordiNate_y);

		defectResult.setOriginLayer(originLayer);
		
		defectResult.setProcessOperationName(processOperationName);
		defectResult.setProcessOperationVersion(processOperationVersion);
		defectResult.setMachineName(machineName);
		
		defectResult.setDefectCode(defectCode);
//		if(!StringUtils.isEmpty(defectSize)){
//			defectResult.setDefectSize(Integer.parseInt(defectSize));
//		}
		defectResult.setDefectSize(defectSize);
		defectResult.setAutoJudgeFlag(autoJudgeFlag);
		defectResult.setNote(note);
		
		if(!StringUtils.isEmpty(inspectionTime)){
			SimpleDateFormat sdf = new SimpleDateFormat(TimeStampUtil.FORMAT_DEFAULT);
			Date date=null;
			try {
				date = sdf.parse(inspectionTime);
				defectResult.setInspectionTime(new Timestamp(date.getTime()));
			} catch (ParseException e) 
			{
			}
		}
		
		defectResult.setHoldFlag(holdFlag);
		defectResult.setMailFlag(mailFlag);
		defectResult.setUserGroupName(userGroupName);
		
		
		defectResult.setLastEventComment(eventInfo.getEventComment());
		defectResult.setLastEventName(eventInfo.getEventName());
		defectResult.setLastEventTime(eventInfo.getEventTime());
		defectResult.setLastEventTimeKey(eventInfo.getEventTimeKey());
		defectResult.setLastEventUser(eventInfo.getEventUser());
		
		ExtendedObjectProxy.getDefectResultService().create(eventInfo, defectResult);
		
		return doc;
	}
}
