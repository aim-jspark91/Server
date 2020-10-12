package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProcessedOperation;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessedOperationService extends CTORMService<ProcessedOperation> {
    
    public static Log logger = LogFactory.getLog(ProcessedOperationService.class);
    
    private final String historyEntity = "";
    
    public List<ProcessedOperation> select(String condition, Object[] bindSet)
        throws CustomException
    {
        List<ProcessedOperation> result = super.select(condition, bindSet, ProcessedOperation.class);
        
        return result;
    }
    
    public ProcessedOperation selectByKey(boolean isLock, Object[] keySet)
        throws CustomException
    {
        return super.selectByKey(ProcessedOperation.class, isLock, keySet);
    }
    
    public ProcessedOperation create(EventInfo eventInfo, ProcessedOperation dataInfo)
        throws CustomException
    {
        super.insert(dataInfo);
        
        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
        
        return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
    }
    
    public void remove(EventInfo eventInfo, ProcessedOperation dataInfo)
        throws CustomException
    {
        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
        
        super.delete(dataInfo);
    }
    
    public ProcessedOperation modify(EventInfo eventInfo, ProcessedOperation dataInfo)
        throws CustomException
    {
        super.update(dataInfo);
        
        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
        
        return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
    }
    
    /**
     * @Name     setLotInfoDownLoadSendProcessList
     * @since    2018. 6. 1.
     * @author   hhlee
     * @contents Set LotInfoDownLoadSend Process List
     * @param processlistElement
     * @param factoryName
     * @param productName
     * @return
     * @throws CustomException
     */
    public List<Map<String, Object>> getProcessedOperationList(String productName) throws CustomException
    {
        List<Map<String, Object>> processOperationData = null;
        try
        {
            String strSql = StringUtil.EMPTY;
            /* 20190123, hhlee, modify, change Query ==>> */
            //strSql = strSql + " SELECT MQ.PRODUCTNAME,                                                                     \n";
            //strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,1),  ' ') AS PROCESSMACHINENAME,   \n";
            //strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,2),  ' ') AS PROCESSOPERATIONNAME, \n";
            //strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,3),  ' ') AS FACTORYNAME,          \n";
            //strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,4),  ' ') AS PRODUCTSPECNAME,      \n";
            //strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,5),  ' ') AS PROCESSFLOWNAME,      \n";
            //strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,6),  ' ') AS MACHINERECIPENAME,    \n";
            //strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,7),  ' ') AS LOTNAME,              \n";
            //strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,8),  ' ') AS EVENTTIMEKEY,         \n";
            //strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,9),  ' ') AS UNITNAMELIST,         \n";
            //strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,10), ' ') AS UNITRECIPELIST        \n";
            //strSql = strSql + "   FROM (                                                                                   \n";
            //strSql = strSql + "         SELECT T.NO, SQ.PRODUCTNAME,                                                       \n";
            //strSql = strSql + "                (CASE WHEN T.NO = 1 THEN SQ.AT1                                             \n";
            //strSql = strSql + "                      WHEN T.NO = 2 THEN SQ.AT2                                             \n";
            //strSql = strSql + "                      WHEN T.NO = 3 THEN SQ.AT3                                             \n";
            //strSql = strSql + "                      WHEN T.NO = 4 THEN SQ.AT4                                             \n";
            //strSql = strSql + "                      WHEN T.NO = 5 THEN SQ.AT5                                             \n";
            //strSql = strSql + "                      END) AS PROCESSOPERATION                                              \n";
            //strSql = strSql + "           FROM (                                                                           \n";
            //strSql = strSql + "                 SELECT PO.PRODUCTNAME AS PRODUCTNAME,                                      \n";
            //strSql = strSql + "                        PO.ATTRIBUTE1 AS AT1,                                               \n";
            //strSql = strSql + "                        PO.ATTRIBUTE2 AS AT2,                                               \n";
            //strSql = strSql + "                        PO.ATTRIBUTE3 AS AT3,                                               \n";
            //strSql = strSql + "                        PO.ATTRIBUTE4 AS AT4,                                               \n";
            //strSql = strSql + "                        PO.ATTRIBUTE5 AS AT5                                                \n";
            //strSql = strSql + "                   FROM CT_PROCESSEDOPERATION PO                                            \n";
            //strSql = strSql + "                  WHERE 1=1                                                                 \n";
            //strSql = strSql + "                    AND PO.PRODUCTNAME = :PRODUCTNAME                                       \n";
            //strSql = strSql + "                 )SQ,                                                                       \n";
            //strSql = strSql + "                 (SELECT LEVEL NO                                                           \n";
            //strSql = strSql + "                    FROM DUAL CONNECT BY LEVEL <= 5) T                                      \n";
            //strSql = strSql + "         )MQ                                                                                \n";
            //strSql = strSql + "  ORDER BY MQ.NO                                                                            \n";            
            strSql = " SELECT MQ.PRODUCTNAME,                                                                                    \n"  
                   + "        TRIM(REGEXP_SUBSTR(REPLACE(MQ.PROCESSOPERATION, ',' ,', '),'[^,]+',1,1))  AS PROCESSMACHINENAME,   \n"
                   + "        TRIM(REGEXP_SUBSTR(REPLACE(MQ.PROCESSOPERATION, ',' ,', '),'[^,]+',1,2))  AS PROCESSOPERATIONNAME, \n"
                   + "        TRIM(REGEXP_SUBSTR(REPLACE(MQ.PROCESSOPERATION, ',' ,', '),'[^,]+',1,3))  AS FACTORYNAME,          \n"
                   + "        TRIM(REGEXP_SUBSTR(REPLACE(MQ.PROCESSOPERATION, ',' ,', '),'[^,]+',1,4))  AS PRODUCTSPECNAME,      \n"
                   + "        TRIM(REGEXP_SUBSTR(REPLACE(MQ.PROCESSOPERATION, ',' ,', '),'[^,]+',1,5))  AS PROCESSFLOWNAME,      \n"
                   + "        TRIM(REGEXP_SUBSTR(REPLACE(MQ.PROCESSOPERATION, ',' ,', '),'[^,]+',1,6))  AS MACHINERECIPENAME,    \n"
                   + "        TRIM(REGEXP_SUBSTR(REPLACE(MQ.PROCESSOPERATION, ',' ,', '),'[^,]+',1,7))  AS LOTNAME,              \n"
                   + "        TRIM(REGEXP_SUBSTR(REPLACE(MQ.PROCESSOPERATION, ',' ,', '),'[^,]+',1,8))  AS EVENTTIMEKEY,         \n"
                   + "        TRIM(REGEXP_SUBSTR(REPLACE(MQ.PROCESSOPERATION, ',' ,', '),'[^,]+',1,9))  AS UNITNAMELIST,         \n"
                   /* 20190214, hhlee, add, PROCSUBUNITLIST added ==>> */
                   + "        TRIM(REGEXP_SUBSTR(REPLACE(MQ.PROCESSOPERATION, ',' ,', '),'[^,]+',1,10)) AS UNITRECIPELIST,       \n"
                   + "        TRIM(REGEXP_SUBSTR(REPLACE(MQ.PROCESSOPERATION, ',' ,', '),'[^,]+',1,11)) AS SUBUNITNAMELIST       \n"
                   /* <<== 20190214, hhlee, add, PROCSUBUNITLIST added */
                   + "   FROM (                                                                                                  \n"
                   + "         SELECT T.NO, SQ.PRODUCTNAME,                                                                      \n"
                   + "                (CASE WHEN T.NO = 1 THEN SQ.AT1                                                            \n"
                   + "                      WHEN T.NO = 2 THEN SQ.AT2                                                            \n"
                   + "                      WHEN T.NO = 3 THEN SQ.AT3                                                            \n"
                   + "                      WHEN T.NO = 4 THEN SQ.AT4                                                            \n"
                   + "                      WHEN T.NO = 5 THEN SQ.AT5                                                            \n"
                   + "                      END) AS PROCESSOPERATION                                                             \n"
                   + "           FROM (                                                                                          \n"
                   + "                 SELECT PO.PRODUCTNAME AS PRODUCTNAME,                                                     \n"
                   + "                        PO.ATTRIBUTE1 AS AT1,                                                              \n"
                   + "                        PO.ATTRIBUTE2 AS AT2,                                                              \n"
                   + "                        PO.ATTRIBUTE3 AS AT3,                                                              \n"
                   + "                        PO.ATTRIBUTE4 AS AT4,                                                              \n"
                   + "                        PO.ATTRIBUTE5 AS AT5                                                               \n"
                   + "                   FROM CT_PROCESSEDOPERATION PO                                                           \n"
                   + "                  WHERE 1=1                                                                                \n"
                   + "                    AND PO.PRODUCTNAME = :PRODUCTNAME                                                      \n"
                   + "                 )SQ,                                                                                      \n"
                   + "                 (SELECT LEVEL NO                                                                          \n"
                   + "                    FROM DUAL CONNECT BY LEVEL <= 5) T                                                     \n"
                   + "         )MQ                                                                                               \n"
                   + "  WHERE 1=1                                                                                                \n"
                   + "    AND MQ.PROCESSOPERATION IS NOT NULL                                                                    \n"
                   + "  ORDER BY MQ.NO                                                                                             ";
            /* <<== 20190123, hhlee, modify, change Query */
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("PRODUCTNAME", productName);
            
            processOperationData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

        }
        catch (Exception ex)
        {
            logger.warn("[getProcessedOperationList] Data Query Failed");;
        }

        return processOperationData;
    }
}
