package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SpcProcessedOperation;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SpcProcessedOperationService extends CTORMService<SpcProcessedOperation> {

    public static Log logger = LogFactory.getLog(SpcProcessedOperationService.class);

    private final String historyEntity = "";

    public List<SpcProcessedOperation> select(String condition, Object[] bindSet)
            throws CustomException {

        List<SpcProcessedOperation> result = super.select(condition, bindSet,
                SpcProcessedOperation.class);

        return result;
    }

    public SpcProcessedOperation selectByKey(boolean isLock, Object[] keySet)
            throws CustomException, greenFrameDBErrorSignal {
        try
        {
            return super.selectByKey(SpcProcessedOperation.class, isLock, keySet);
        }
        catch(greenFrameDBErrorSignal ns)
        {
            throw ns;
        }

    }

    public SpcProcessedOperation create(EventInfo eventInfo, SpcProcessedOperation dataInfo)
            throws CustomException {

        super.insert(dataInfo);

        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

        return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());

    }

    public void remove(EventInfo eventInfo, SpcProcessedOperation dataInfo)
            throws CustomException {

        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

        super.delete(dataInfo);

    }

    public SpcProcessedOperation modify(EventInfo eventInfo, SpcProcessedOperation dataInfo)
            throws CustomException {
        super.update(dataInfo);

        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

        return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());

    }

    //public void setSpcProcessedOperationData(EventInfo eventInfo, Lot lotData, 
    //        String productName, String machineName, String machineRecipeName, String eventTimeKey) throws CustomException
    //{
    //    try
    //    {
    //        SpcProcessedOperation spcProcessedOperationData = new SpcProcessedOperation(eventInfo.getEventTimeKey(), productName);
    //                    
    //        spcProcessedOperationData.setFactoryName(lotData.getFactoryName());
    //        spcProcessedOperationData.setLotName(lotData.getKey().getLotName());
    //        spcProcessedOperationData.setProductSpecName(lotData.getProductSpecName());
    //        spcProcessedOperationData.setProcessFlowName(lotData.getProcessFlowName());
    //        spcProcessedOperationData.setProcessOperationName(lotData.getProcessOperationName());
    //        spcProcessedOperationData.setMachineName(machineName);
    //        spcProcessedOperationData.setMachineRecipeName(machineRecipeName);
    //        spcProcessedOperationData.setCreateTime(eventInfo.getEventTime());
    //        spcProcessedOperationData.setEvnetName(eventInfo.getEventName());
    //        spcProcessedOperationData.setEventTimekey(eventTimeKey);
    //        spcProcessedOperationData.setEvnetUser(eventInfo.getEventUser());
    //        
    //        spcProcessedOperationData = this.create(eventInfo, spcProcessedOperationData);
    //        
    //    }
    //    catch (Exception ex)
    //    {
    //        logger.warn(String.format("Set setSpcProcessedOperationData failed.[%s - %s - %s] ", productName, lotData.getKey().getLotName(), lotData.getProductSpecName()));           
    //    }
    //}
    
    
}