/**
 * 
 */
package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

/**
 * @author Administrator
 *
 */
public class CSTForceQuitCommand extends AsyncHandler{

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME" , true);	
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME" , true);
		
		/*** 2019.03.06_hsryu_Add Validation. Request CIM ***/
		Lot lotData = null;
		
		try{
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierName);
		}
		catch(Throwable e){
			eventLog.info("LotData is not exist. CarrierName :" + carrierName);
		}
		
		if( lotData != null ){
			try{
				if(StringUtils.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run)){
					throw new CustomException("LOT-9003", lotData.getKey().getLotName() +"(LotProcessState:"+lotData.getLotProcessState() + ")");
				}
				
				//START MODIFY BY JHIYING ON20191111  MANTIS:5165
				String StartCheckResult= CommonUtil.getValue(lotData.getUdfs(), "STARTCHECKRESULT");
				if(StringUtils.equals(StartCheckResult, "Y")){
					throw new CustomException("LOT-4005",  "");
				}
					
				// end MODIFY BY JHIYING ON20191111  MANTIS:5165
			}
			catch (Exception e) {
				eventLog.error(e);
				
				if (e instanceof CustomException) {
					SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ((CustomException) e).errorDef.getErrorCode());
					SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ((CustomException) e).errorDef.getLoc_errorMessage());
				} else {
					SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, (e != null) ? e.getClass().getName() : "SYS-0000");
					SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, (e != null && StringUtils.isNotEmpty(e.getMessage())) ? e.getMessage() : "Unknown exception is occurred.");
				}

				GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
				throw new CustomException("LOT-9003", lotData.getKey().getLotName() +"(LotProcessState:"+lotData.getLotProcessState() + ")");
			}
		}
		/****************************************************/
		
		//get line machine
		Machine machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		SMessageUtil.setBodyItemValue(doc, "MACHINENAME", machineName, true);
				
		String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
		
		try{
			 if(StringUtils.isEmpty(targetSubjectName))
			    	throw new CustomException("LOT-9006", machineName);
		}
		catch (Exception e) {
			eventLog.error(e);
			
			if (e instanceof CustomException) {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ((CustomException) e).errorDef.getErrorCode());
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ((CustomException) e).errorDef.getLoc_errorMessage());
			} else {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, (e != null) ? e.getClass().getName() : "SYS-0000");
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, (e != null && StringUtils.isNotEmpty(e.getMessage())) ? e.getMessage() : "Unknown exception is occurred.");
			}

			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
			throw new CustomException("LOT-9006", machineName);
		}
		 
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
	}
}
