package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;
import org.jdom.Element;

public class LoadProbeCard  extends SyncHandler  {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Mount",this.getEventUser(), this.getEventComment(), "", "");
		
		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc,"DURABLELIST", false)) 
		{
			String sDurableName = SMessageUtil.getChildText(eledur,"DURABLENAME", true);
			String sMachineName = SMessageUtil.getChildText(eledur,"MACHINENAME", true);
			String sUnitName = SMessageUtil.getChildText(eledur,"UNITNAME", false);
			String sProbePosition = SMessageUtil.getChildText(eledur,"POSITION", false);
			
			Durable ProbeData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
			
			Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
			
			String operationMode = CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE");

			//2017.7.31 zhongsl MaskPosition validation
			MESDurableServiceProxy.getDurableServiceUtil().checkExistPositionForProbe(sMachineName, sUnitName, sProbePosition,"ProbeCard");
			
			if(StringUtil.isEmpty(operationMode))
			{
				operationMode = GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP;
			}
			
			if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
			{
				//OperationMode:NORMAL
				if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(sMachineName).size() <=0)
				{
				    MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardDataForOPI(ProbeData.getKey().getDurableName(),GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,sUnitName,sMachineName,sProbePosition,eventInfo);
				}
				/* 20180814, Add, When there is no probecard which a mount becomes to Unit ==>> */
				/*else if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).size() <=0)
                {
				    MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardDataForOPI(ProbeData.getKey().getDurableName(),GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,sUnitName,sMachineName,sProbePosition,eventInfo);
                }*/
				/* <<== 20180814, Add, When there is no probecard which a mount becomes to Unit */
				else
				{			
					//One Unit MaxPBcount = 4, One Machine 2Uits = 8
					//if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(sMachineName).size() > 8)
						//throw new CustomException("PROBECARD-0010", sMachineName);
															
					if(!MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(sMachineName).get(0).getDurableSpecName().equals(ProbeData.getDurableSpecName()))
						throw new CustomException("PROBECARD-0002", sMachineName, 
						        MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(sMachineName).get(0).getKey().getDurableName(),
						        MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(sMachineName).get(0).getDurableSpecName(),
						        ProbeData.getKey().getDurableName(),ProbeData.getDurableSpecName());
					
					/* 20180926, hhlee, add ProbeCard Count = 0 ==>> */
					/*try
					{
    					//check ProbeCardCondition
    					MESDurableServiceProxy.getDurableServiceUtil().checkProbeCardCondition(sMachineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"),true);
					}
					catch(CustomException ce)
			        {
					    if(!ce.errorDef.getErrorCode().equals("PROBECARD-0008"))
			            {
					        throw ce;
			            }			            
			        }*/
					/* <<== 20180926, hhlee, add ProbeCard Count = 0 */
					
					//execute
					MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardDataForOPI(ProbeData.getKey().getDurableName(),GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,sUnitName,sMachineName,sProbePosition,eventInfo);
				}
			}
			else if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
			{
				//OperationMode:INDP
				//means no probeCard in this unit
				if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).size() <=0)
				{
				    MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardDataForOPI(ProbeData.getKey().getDurableName(),GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,sUnitName,sMachineName,sProbePosition,eventInfo);
				}
				else
				{
					if(MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).size() > 4)
						throw new CustomException("PROBECARD-0007", sUnitName, sMachineName);
						
					if(!MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).get(0).getDurableSpecName().equals(ProbeData.getDurableSpecName()))
						throw new CustomException("PROBECARD-0003",sMachineName + "-" + sUnitName,  
						        MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).get(0).getKey().getDurableName(),
						        MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(sUnitName).get(0).getDurableSpecName(),
						        ProbeData.getKey().getDurableName(),
						        ProbeData.getDurableSpecName());
					
					/* 20180926, hhlee, add ProbeCard Count = 0 ==>> */
					/*try
                    {
					    //check ProbeCardCondition
	                    MESDurableServiceProxy.getDurableServiceUtil().checkProbeCardCondition(sMachineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"),true);
                    }
                    catch(CustomException ce)
                    {
                        if(!ce.errorDef.getErrorCode().equals("PROBECARD-0008"))
                        {
                            throw ce;
                        }                       
                    }*/
					/* <<== 20180926, hhlee, add ProbeCard Count = 0 */
					
					//execute
					MESDurableServiceProxy.getDurableServiceUtil().updateProbeCardDataForOPI(ProbeData.getKey().getDurableName(),GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,sUnitName,sMachineName,sProbePosition,eventInfo);
				}
			}
		}
		return doc;
	}
}
