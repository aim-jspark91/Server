package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ChamberIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Administrator
 *
 */
public class ChamberIdleTimeService extends CTORMService<ChamberIdleTime> {
    
    public static Log logger = LogFactory.getLog(ChamberIdleTimeService.class); 
    private final String historyEntity = "ChamberIdleTimeHist";
    
    public List<ChamberIdleTime> select(String condition, Object[] bindSet) throws CustomException {
        return super.select(condition, bindSet, ChamberIdleTime.class);
    }
        
    public ChamberIdleTime selectByKey(boolean isLock, Object[] keySet) throws CustomException {
        return super.selectByKey(ChamberIdleTime.class, isLock, keySet);
    }
        
    public ChamberIdleTime create(EventInfo eventInfo, ChamberIdleTime dataInfo) throws CustomException {
        super.insert(dataInfo);     
        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
        
        return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
    }
    
    public void remove(EventInfo eventInfo, ChamberIdleTime dataInfo) throws CustomException {
        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);      
        super.delete(dataInfo);
    }
    
    public ChamberIdleTime modify(EventInfo eventInfo, ChamberIdleTime dataInfo) throws CustomException {
        super.update(dataInfo);     
        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
        
        return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
    }
}