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
import org.jdom.Element;

public class DefectValidationResult extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String validationResultList = SMessageUtil.getBodyItemValue(doc, "VALIDATIONRESULT", false);
		String inspectionTime = SMessageUtil.getBodyItemValue(doc, "INSPECTIONTIME", true);

		
		List<Element> defectinfoList = SMessageUtil.getBodySequenceItemList(doc, "DEFECTINFOLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("InsertDefectValidationResult", this.getEventUser(), this.getEventComment(), "", "");

		String productSpecVersion="00001";
		String processOperationVersion="00001";
		
		for (Element defectInfo : defectinfoList)
		{
			String cutNumber = SMessageUtil.getChildText(defectInfo, "CUTNUMBER", true);
			String panelName = SMessageUtil.getChildText(defectInfo, "PANELID", true);
			String defectCode = SMessageUtil.getChildText(defectInfo, "DEFECTCODE", true);
			String originLayer = SMessageUtil.getChildText(defectInfo, "ORIGINLAYER", false);
			String defectSize = SMessageUtil.getChildText(defectInfo, "DEFECTSIZE", true);
			String coordiNate_x = SMessageUtil.getChildText(defectInfo, "COORDINATE_X", true);
			String coordiNate_y = SMessageUtil.getChildText(defectInfo, "COORDINATE_Y", true);
			
			String holdFlag = SMessageUtil.getChildText(defectInfo, "HOLDFLAG", true);
			String mailFlag = SMessageUtil.getChildText(defectInfo, "MAILFLAG", true);
			String userGroupName = SMessageUtil.getChildText(defectInfo, "USERGROUPNAME", true);
			
			
			@SuppressWarnings("unchecked")
	        List<Map<String, Object>> sqlResult =null;
	        
			String sql =  " SELECT MAX(DEFECTRESULTID) AS MAX FROM CT_DEFECTRESULT " ;
	     
	         Map<String, String> bindMap = new HashMap<String, String>();
	         sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
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
			defectResult.setCutNumber(cutNumber);
			defectResult.setCoordiNate_x(coordiNate_x);
			defectResult.setCoordiNate_y(coordiNate_y);
	
			defectResult.setOriginLayer(originLayer);
			
			defectResult.setProcessOperationName(processOperationName);
			defectResult.setProcessOperationVersion(processOperationVersion);
			defectResult.setMachineName(machineName);
			
			defectResult.setDefectCode(defectCode);
			defectResult.setDefectSize(defectSize);
			defectResult.setAutoJudgeFlag("Y");
			defectResult.setNote("");
			
			if(!StringUtils.isEmpty(inspectionTime)){
				SimpleDateFormat sdf = new SimpleDateFormat(TimeStampUtil.FORMAT_SIMPLE_DEFAULT);
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
			

		}
		
		return doc;
	}
}
