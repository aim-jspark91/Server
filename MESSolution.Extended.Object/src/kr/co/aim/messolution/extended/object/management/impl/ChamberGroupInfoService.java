package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ChamberGroupInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/* hhlee, 2018.03.31, Add */
public class ChamberGroupInfoService extends CTORMService<ChamberGroupInfo> {
    
    public static Log logger = LogFactory.getLog(ChamberGroupInfo.class);
    
    private final String historyEntity = "";
    
    public List<ChamberGroupInfo> select(String condition, Object[] bindSet)
        throws CustomException
    {
        try
        {
            List<ChamberGroupInfo> result = super.select(condition, bindSet, ChamberGroupInfo.class);
            
            return result;
        }
        catch (greenFrameDBErrorSignal de)
        {
            throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("MACHINE-9199", fe.getMessage());
        }
    }
    
    public ChamberGroupInfo selectByKey(boolean isLock, Object[] keySet)
        throws CustomException
    {
        try
        {
            return super.selectByKey(ChamberGroupInfo.class, isLock, keySet);
        }
        catch (greenFrameDBErrorSignal de)
        {
            throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("MACHINE-9199", fe.getMessage());
        }
    }
    
    public ChamberGroupInfo create(EventInfo eventInfo, ChamberGroupInfo dataInfo)
        throws CustomException
    {
        try
        {
            super.insert(dataInfo);
            
            super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
            
            return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
        }
        catch (greenFrameDBErrorSignal de)
        {
            throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("MACHINE-9199", fe.getMessage());
        }
    }
    
    public void remove(EventInfo eventInfo, ChamberGroupInfo dataInfo)
        throws CustomException
    {
        try
        {
            super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
            
            super.delete(dataInfo);
        }
        catch (greenFrameDBErrorSignal de)
        {
            throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("MACHINE-9199", fe.getMessage());
        }
    }
    
    public ChamberGroupInfo modify(EventInfo eventInfo, ChamberGroupInfo dataInfo)
        throws CustomException
    {
        try
        {
            super.update(dataInfo);
            
            super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
            
            return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
        }
        catch (greenFrameDBErrorSignal de)
        {
            throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("MACHINE-9199", fe.getMessage());
        }
    }
    
    public String getValidateChamberGroupName(String machineName, String machineGroupName, String machineLevel) throws CustomException
    {
        String chamberGroupName = StringUtil.EMPTY;
        
        try
        {
            String strSql =   " SELECT MG.MACHINEGROUPNAME AS CHAMBERGROUPNAME,                                             \n"
                            + "        MGM.MACHINENAME,                                                                     \n"
                            + "        CG.PORTNAME,                                                                         \n"
                            + "        CG.RECIPENAME,                                                                       \n"
                            + "        MQ.MACHINENAME,                                                                      \n"
                            + "        MQ.UNITNAME,                                                                         \n"
                            + "        MQ.SUBUNITNAME                                                                       \n"
                            + "   FROM CT_CHAMBERGROUPINFO CG,                                                              \n"
                            + "        MACHINEGROUP MG,                                                                     \n"
                            + "        CT_MACHINEGROUPMACHINE MGM,                                                          \n"
                            + "        (SELECT SQ.LV                                                                        \n"
                            + "               ,SQ.COMBINEMACHINE                                                            \n"
                            + "               ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 1) AS MACHINENAME               \n"
                            + "               ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 2) AS UNITNAME                  \n"
                            + "               ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 3) AS SUBUNITNAME               \n"
                            + "               ,SQ.FACTORYNAME,SQ.AREANAME,SQ.SUPERMACHINENAME                               \n"
                            + "               ,SQ.MACHINEGROUPNAME,SQ.PROCESSCOUNT,SQ.RESOURCESTATE                         \n"
                            + "               ,SQ.E10STATE,SQ.COMMUNICATIONSTATE,SQ.MACHINESTATENAME                        \n"
                            + "               ,SQ.LASTEVENTNAME,SQ.LASTEVENTTIMEKEY,SQ.LASTEVENTTIME                        \n"
                            + "               ,SQ.LASTEVENTUSER,SQ.LASTEVENTCOMMENT,SQ.LASTEVENTFLAG                        \n"
                            + "               ,SQ.REASONCODETYPE,SQ.REASONCODE,SQ.MCSUBJECTNAME                             \n"
                            + "               ,SQ.OPERATIONMODE,SQ.DSPFLAG,SQ.FULLSTATE                                     \n"
                            + "               ,SQ.ONLINEINITIALCOMMSTATE                                                    \n"
                            + "           FROM (                                                                            \n"
                            + "                 SELECT LEVEL LV                                                             \n"
                            + "                       ,SUBSTR(SYS_CONNECT_BY_PATH(M.MACHINENAME, '-'), 2) AS COMBINEMACHINE \n"
                            + "                       ,M.*                                                                  \n"
                            + "                       FROM MACHINE M                                                        \n"
                            + "                 START WITH M.SUPERMACHINENAME IS NULL                                       \n"
                            + "                        AND M.MACHINENAME = :MACHINENAME                                     \n"
                            + "           CONNECT BY PRIOR M.MACHINENAME = M.SUPERMACHINENAME                               \n"
                            + "                 ORDER BY M.MACHINENAME                                                      \n"
                            + "                 ) SQ                                                                        \n"
                            + "         WHERE 1=1                                                                           \n"
                            + "           AND SQ.LV = :MACHINELEVEL                                                         \n"
                            + "         ORDER BY SQ.LV, SQ.COMBINEMACHINE                                                   \n"
                            + "        )MQ                                                                                  \n"
                            + "  WHERE 1=1                                                                                  \n"
                            + "    AND CG.CHAMBERGROUPNAME = :CHAMBERGROUPNAME                                              \n"
                            + "    AND CG.CHAMBERGROUPNAME = MG.MACHINEGROUPNAME                                            \n"
                            + "    AND MG.MACHINEGROUPNAME = MGM.MACHINEGROUPNAME                                           \n"
                            + "    AND MGM.MACHINEGROUPNAME = CG.CHAMBERGROUPNAME                                           \n"
                            + "    AND CG.MACHINENAME = MGM.MACHINENAME                                                     \n"
                            + "    AND CG.MACHINENAME = MQ.SUBUNITNAME                                                      \n"
                            + "  ORDER BY MG.MACHINEGROUPNAME, MGM.MACHINENAME, CG.PORTNAME                                 \n";
                
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("MACHINENAME", machineName);            
            bindMap.put("MACHINELEVEL", machineLevel);
            bindMap.put("CHAMBERGROUPNAME", machineGroupName);

            List<Map<String, Object>> chamberGroupNameList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
            
            if ( chamberGroupNameList != null && chamberGroupNameList.size() > 0 )
            {
                chamberGroupName = chamberGroupNameList.get(0).get("CHAMBERGROUPNAME").toString();
            }
        }
        catch (Exception ex)
        {
            logger.info("[getValidateChamberGroupName] " + ex);
        }        
        
        return chamberGroupName;
    }
}
