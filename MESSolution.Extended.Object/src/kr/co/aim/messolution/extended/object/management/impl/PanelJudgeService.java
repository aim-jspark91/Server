package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PanelJudgeService extends CTORMService<PanelJudge> {
	
public static Log logger = LogFactory.getLog(PanelJudge.class);
	
	private final String historyEntity = "PanelJudgeHistory";
	
	public List<PanelJudge> select(String condition, Object[] bindSet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			List<PanelJudge> result = super.select(condition, bindSet, PanelJudge.class);
			
			return result;
		}
		catch (greenFrameDBErrorSignal de)
		{
			if (de.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(de.getDataKey(), de.getSql());
			else
				throw new CustomException("SYS-8001", de.getSql());
		}
	}
	
	public PanelJudge selectByKey(boolean isLock, Object[] keySet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			return super.selectByKey(PanelJudge.class, isLock, keySet);
		}
		catch (greenFrameDBErrorSignal de)
		{
			if (de.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(de.getDataKey(), de.getSql());
			else
				throw new CustomException("SYS-8001", de.getSql());
		}
	}
	
	public PanelJudge create(EventInfo eventInfo, PanelJudge dataInfo)
		throws CustomException
	{
		// Added by smkang on 2018.12.12
		if (StringUtils.isEmpty(dataInfo.getLastEventName()))		dataInfo.setLastEventName(eventInfo.getEventName());
		if (StringUtils.isEmpty(dataInfo.getLastEventUser()))		dataInfo.setLastEventUser(eventInfo.getEventUser());
		if (dataInfo.getLastEventTime() == null)					dataInfo.setLastEventTime(eventInfo.getEventTime());
		if (StringUtils.isEmpty(dataInfo.getLastEventComment()))	dataInfo.setLastEventComment(eventInfo.getEventComment());
				
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, PanelJudge dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public PanelJudge modify(EventInfo eventInfo, PanelJudge dataInfo)
		throws CustomException
	{
		// Added by smkang on 2018.12.12
		if (StringUtils.isEmpty(dataInfo.getLastEventName()))		dataInfo.setLastEventName(eventInfo.getEventName());
		if (StringUtils.isEmpty(dataInfo.getLastEventUser()))		dataInfo.setLastEventUser(eventInfo.getEventUser());
		if (dataInfo.getLastEventTime() == null)					dataInfo.setLastEventTime(eventInfo.getEventTime());
		if (StringUtils.isEmpty(dataInfo.getLastEventComment()))	dataInfo.setLastEventComment(eventInfo.getEventComment());
		
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
	/**
	 * checkExistProductName
	 * @author Lhkim
	 * @since 2015.03.02
	 * @param panelName
	 * @return boolean
	 * @throws CustomException
	 */
	public boolean checkExistProductName(String panelName) throws CustomException{
		
		
		boolean existProduct = false;
		
		String condition = "WHERE PANELNAME = ?";
		
		Object[] bindSet = new Object[] { panelName};
	 try
	 {
		 List<PanelJudge> sqlResult =  ExtendedObjectProxy.getPanelJudgeService().select(condition, bindSet);
			 
		 return existProduct = true;
	 }
	 catch (NotFoundSignal ex)
	 {
		 return existProduct = false;
	 }
	}
	
	/**
     * 
     * @Name     getPanelProductJudge
     * @since    2018. 7. 22.
     * @author   Administrator
     * @contents Get Panel Product Judge
     *           ARRAY : [ O : Not Inspection | G : Good  | X : No good | R : Rework ]
     *            OLED : [ O : Good | X : No good ]
     * @param productName
     * @param productType
     * @param isOX
     * @return
     * @throws CustomException
     */
    public String getPanelProductJudge(String productName, String productType, boolean isOX) throws CustomException
    {
        String panelProductJudge = StringUtil.EMPTY;
        
        Map<String, Object> bindMap = new HashMap<String, Object>();
        
        String strSql = StringUtil.EMPTY;  
        strSql = strSql + " SELECT                                                                                                      \n";
        
        if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_GLASS))
        {
            strSql = strSql + "       SQ.GLASSNAME,                                                                                     \n";                     
        }
        else if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_CUT))
        {
            strSql = strSql + "       SQ.HQGLASSNAME,                                                                                   \n";
        }
        else
        {
            strSql = strSql + "       SQ.GLASSNAME,                                                                                     \n";
        }
        
        strSql = strSql + "         REPLACE(SUBSTR(MAX(SYS_CONNECT_BY_PATH(SQ.PANELJUDGE, ',')), 2), ',', '') AS PANELPRODUCTJUDGES     \n"
                        + "    FROM (                                                                                                   \n"
                        + "          SELECT PJ.PANELNAME,                                                                               \n";
        if(isOX)
        {
            strSql = strSql + "                 CASE WHEN (PJ.PANELJUDGE = 'N' OR PJ.PANELJUDGE = 'X')                                  \n"
                            + "                       THEN 'X' ELSE 'O' END AS PANELJUDGE,                                              \n";
        }
        else
        {
            strSql = strSql + "                 PJ.PANELJUDGE,                                                                          \n";
        }
        
        strSql = strSql + "                 PJ.PANELGRADE,                                                                              \n"
                        + "                 PJ.XAXIS1,                                                                                  \n"
                        + "                 PJ.YAXIS1,                                                                                  \n"
                        + "                 PJ.XAXIS2,                                                                                  \n"
                        + "                 PJ.YAXIS2,                                                                                  \n"
                        + "                 PJ.GLASSNAME,                                                                               \n"
                        + "                 PJ.HQGLASSNAME,                                                                             \n"
                        + "                 PJ.CUTTYPE,                                                                                 \n"
                        + "                 PJ.LASTEVENTNAME,                                                                           \n"
                        + "                 PJ.LASTEVENTUSER,                                                                           \n"
                        + "                 PJ.LASTEVENTTIME,                                                                           \n"
                        + "                 PJ.LASTEVENTCOMMENT,                                                                        \n"
                        + "                 PJ.PROCESSOPERATIONNAME,                                                                    \n"
                        + "                 PJ.MACHINENAME,                                                                             \n"
                        + "                 PJ.ASSEMBLEDPANELNAME,                                                                      \n"
                        + "                 PJ.PRODUCTSPECTYPE,                                                                         \n"
                        + "                 PJ.PRODUCTSPECNAME,                                                                         \n"
                        + "                 PJ.PRODUCTREQUESTNAME,                                                                      \n"
                        + "                 ROW_NUMBER () OVER (PARTITION BY PJ.GLASSNAME ORDER BY PJ.HQGLASSNAME,                      \n"
                        + "                                            SUBSTR(PJ.PANELNAME, LENGTH(PJ.PANELNAME) - 1),                  \n"
                        + "                                                 SUBSTR(PJ.PANELNAME, LENGTH(PJ.PANELNAME) - 3 , 2)) AS RNUM \n"
                        + "            FROM CT_PANELJUDGE PJ                                                                            \n"
                        + "           WHERE 1=1                                                                                         \n";
        if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_GLASS))
        {                        
            strSql = strSql + "         AND PJ.GLASSNAME = :GLASSNAME                                                                   \n";
            bindMap.put("GLASSNAME", productName);
        }
        else if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_CUT))
        {
            strSql = strSql + "         AND PJ.HQGLASSNAME = :HQGLASSNAME                                                               \n";
            bindMap.put("HQGLASSNAME", productName);
        }               
        else
        {     
            strSql = strSql + "         AND PJ.GLASSNAME = :GLASSNAME                                                                   \n";
            bindMap.put("GLASSNAME", productName);
        }
        strSql = strSql + "          ) SQ                                                                                               \n"
                        + "   WHERE 1=1                                                                                                 \n"
                        + "     START WITH SQ.RNUM = 1                                                                                  \n"
                        + "     CONNECT BY PRIOR SQ.RNUM = SQ.RNUM - 1                                                                  \n";
        if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_GLASS))
        {                
            strSql = strSql + "   GROUP BY SQ.GLASSNAME                                                                                 \n";
        }
        else if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_CUT))
        {
            strSql = strSql + "   GROUP BY SQ.HQGLASSNAME                                                                               \n";
        }
        else
        {                
            strSql = strSql + "   GROUP BY SQ.GLASSNAME                                                                                 \n";
        }
                        
        List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

        if(sqlResult != null && sqlResult.size() > 0)
        {
            panelProductJudge = sqlResult.get(0).get("PANELPRODUCTJUDGES").toString();              
        }
        
        return panelProductJudge;
        
//        String panelProductJudge = StringUtil.EMPTY;
//        
//        Map<String, Object> bindMap = new HashMap<String, Object>();
//        
//        String strSql = StringUtil.EMPTY;       
//        strSql = strSql + " SELECT PJ.PANELNAME,                                                                               \n";
//        if(isOX)
//        {
//            strSql = strSql + "              CASE WHEN (PJ.PANELJUDGE = 'N' OR PJ.PANELJUDGE = 'X') THEN 'X' ELSE 'O' END AS PANELJUDGE, \n" ;
//        }
//        else
//        {
//            strSql = strSql + "              PJ.PANELJUDGE,                                                                          \n";
//        }
//        strSql = strSql + "              PJ.PANELGRADE,                                                                              \n"
//                        + "              PJ.XAXIS1,                                                                                  \n"
//                        + "              PJ.YAXIS1,                                                                                  \n"
//                        + "              PJ.XAXIS2,                                                                                  \n"
//                        + "              PJ.YAXIS2,                                                                                  \n"
//                        + "              PJ.GLASSNAME,                                                                               \n"
//                        + "              PJ.HQGLASSNAME,                                                                             \n"
//                        + "              PJ.CUTTYPE,                                                                                 \n"
//                        + "              PJ.LASTEVENTNAME,                                                                           \n"
//                        + "              PJ.LASTEVENTUSER,                                                                           \n"
//                        + "              PJ.LASTEVENTTIME,                                                                           \n"
//                        + "              PJ.LASTEVENTCOMMENT                                                                         \n"
//                        + "        FROM CT_PANELJUDGE PJ                                                                             \n"
//                        + "       WHERE 1=1                                                                                          \n";
//        if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_GLASS))
//        {                        
//            strSql = strSql + "         AND PJ.GLASSNAME = :GLASSNAME                                                                \n";
//            bindMap.put("GLASSNAME", productName);
//        }
//        else if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_CUT))
//        {
//            strSql = strSql + "         AND PJ.HQGLASSNAME = :HQGLASSNAME                                                            \n";
//            bindMap.put("HQGLASSNAME", productName);
//        }               
//        else
//        {     
//            strSql = strSql + "         AND PJ.GLASSNAME = :GLASSNAME                                                               \n";
//            bindMap.put("GLASSNAME", productName);
//        }            
//        //strSql = strSql + "        ORDER BY PJ.HQGLASSNAME, PJ.PANELNAME                                                             \n"
//        strSql = strSql + "        ORDER BY PJ.HQGLASSNAME, SUBSTR(PJ.PANELNAME, LENGTH(PJ.PANELNAME) - 1), SUBSTR(PJ.PANELNAME, LENGTH(PJ.PANELNAME) - 3 , 2) \n";
//                
//        List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//
//        if(sqlResult != null && sqlResult.size() > 0)
//        {
//            panelProductJudge = StringUtil.EMPTY;
//            for(int i = 0; i < sqlResult.size(); i++)
//            {
//                panelProductJudge = panelProductJudge + sqlResult.get(i).get("PANELJUDGE").toString();
//            }                  
//        }
//        
//        return panelProductJudge;
        
//        String panelProductJudge = StringUtil.EMPTY;
//        
//        Map<String, Object> bindMap = new HashMap<String, Object>();
//        
//        String strSql = StringUtil.EMPTY;       
//        strSql = strSql + " SELECT                                                                                                   \n";
//
//        if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_GLASS))
//        {
//            strSql = strSql + "       SQ.GLASSNAME                                                                                   \n";
//                       
//        }
//        else if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_CUT))
//        {
//            strSql = strSql + "       SQ.HQGLASSNAME                                                                                 \n";
//        }
//        else
//        {
//            strSql = strSql + "       SQ.GLASSNAME                                                                                   \n";
//        }
//        
//        strSql = strSql + "       ,REPLACE(SUBSTR(MAX(SYS_CONNECT_BY_PATH(SQ.PANELJUDGE, ',')), 2), ',', '') AS PANELPRODUCTJUDGES   \n"
//                        + " FROM (SELECT PJ.PANELNAME,                                                                               \n";
//        if(isOX)
//        {
//            strSql = strSql + "              CASE WHEN PJ.PANELJUDGE = 'X' THEN 'X' ELSE 'O' END AS PANELJUDGE,                      \n" ;
//        }
//        else
//        {
//            strSql = strSql + "              PJ.PANELJUDGE,                                                                          \n";
//        }
//        strSql = strSql + "              PJ.PANELGRADE,                                                                              \n"
//                        + "              PJ.XAXIS1,                                                                                  \n"
//                        + "              PJ.YAXIS1,                                                                                  \n"
//                        + "              PJ.XAXIS2,                                                                                  \n"
//                        + "              PJ.YAXIS2,                                                                                  \n"
//                        + "              PJ.GLASSNAME,                                                                               \n"
//                        + "              PJ.HQGLASSNAME,                                                                             \n"
//                        + "              PJ.CUTTYPE,                                                                                 \n"
//                        + "              PJ.LASTEVENTNAME,                                                                           \n"
//                        + "              PJ.LASTEVENTUSER,                                                                           \n"
//                        + "              PJ.LASTEVENTTIME,                                                                           \n"
//                        + "              PJ.LASTEVENTCOMMENT,                                                                        \n"
//                        + "              ROW_NUMBER () OVER (PARTITION BY PJ.GLASSNAME ORDER BY PJ.HQGLASSNAME,PJ.PANELNAME) AS RNUM \n"   
//                        + "        FROM CT_PANELJUDGE PJ                                                                             \n"
//                        + "       WHERE 1=1                                                                                          \n";
//        if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_GLASS))
//        {                        
//            strSql = strSql + "         AND PJ.GLASSNAME = :GLASSNAME                                                                \n";
//            bindMap.put("GLASSNAME", productName);
//        }
//        else if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_CUT))
//        {
//            strSql = strSql + "         AND PJ.HQGLASSNAME = :HQGLASSNAME                                                            \n";
//            bindMap.put("HQGLASSNAME", productName);
//        }               
//        else
//        {     
//            strSql = strSql + "         AND PJ.GLASSNAME = :GLASSNAME                                                               \n";
//            bindMap.put("GLASSNAME", productName);
//        }            
//        //strSql = strSql + "        ORDER BY PJ.HQGLASSNAME, PJ.PANELNAME                                                             \n"
//        strSql = strSql + "        ORDER BY PJ.HQGLASSNAME, SUBSTR(PJ.PANELNAME, LENGTH(PJ.PANELNAME) - 1), SUBSTR(PJ.PANELNAME, LENGTH(PJ.PANELNAME) - 3 , 2) \n"
//                        + "        ) SQ                                                                                              \n"
//                        + "  START WITH SQ.RNUM = 1                                                                                  \n"
//                        + " CONNECT BY PRIOR SQ.RNUM = SQ.RNUM - 1                                                                   \n";
//        if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_GLASS))
//        {                
//            strSql = strSql + "   GROUP BY SQ.GLASSNAME                                                                              \n";
//        }
//        else if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().PRODUCTTYPE_CUT))
//        {
//            strSql = strSql + "   GROUP BY SQ.HQGLASSNAME                                                                            \n";
//        }
//        else
//        {                
//            strSql = strSql + "   GROUP BY SQ.GLASSNAME                                                                              \n";
//        }    
//        
//        List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//
//        if(sqlResult != null && sqlResult.size() > 0)
//        {
//            panelProductJudge = sqlResult.get(0).get("PANELPRODUCTJUDGES").toString();            
//        }
//        
//        return panelProductJudge;
    }

}
