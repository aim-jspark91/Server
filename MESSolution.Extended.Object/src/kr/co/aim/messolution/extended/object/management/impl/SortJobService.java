package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class SortJobService extends CTORMService<SortJob> {
	
	public static Log logger = LogFactory.getLog(SortJobService.class);
	
	private final String historyEntity = "SortJobHist";
	
	public List<SortJob> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<SortJob> result = super.select(condition, bindSet, SortJob.class);
		
		return result;
	}
	
	public SortJob selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(SortJob.class, isLock, keySet);
	}
	
	public SortJob create(EventInfo eventInfo, SortJob dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, SortJob dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public SortJob modify(EventInfo eventInfo, SortJob dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
		
//	/**
//	 * Sorter job Glass selection
//	 * @author swcho
//	 * @since 2015.03.10
//	 * @param productData
//	 * @param sampleLot
//	 * @return
//	 * @throws CustomException
//	 */
//	public String getSortJobFlag(Product productData, List<ListOrderedMap> sortJobList)
//		throws CustomException
//	{  
//		
//		String samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
//		
//		for (ListOrderedMap sortJob : sortJobList)
//		{
//			String fromProductName = CommonUtil.getValue(sortJob, "PRODUCTNAME");
//			String fromPosition = CommonUtil.getValue(sortJob, "FROMPOSITION");
//			
//			//convert
//			long position = 0;
//			try
//			{
//				position = Long.parseLong(fromPosition);
//			}
//			catch (Exception ex)
//			{
//				logger.warn("Position parsing failed");
//			}
//
//			if (productData.getKey().getProductName().equals(fromProductName) && productData.getPosition() == position)
//			{
//				samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
//				
//				break;
//			}
//		}
//		
//		return samplingFlag;
//	}
	
	/**
     * Sorter job Glass selection
     * @author swcho
     * @since 2015.03.10
     * @param productData
     * @param sampleLot
     * @return
     * @throws CustomException
     */
    public String getSortJobFlag(Product productData, List<ListOrderedMap> sortJobList)
        throws CustomException
    {  
        
        String samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
        
        for (ListOrderedMap sortJob : sortJobList)
        {
            String fromProductName = CommonUtil.getValue(sortJob, "PRODUCTNAME");
            String fromPosition = CommonUtil.getValue(sortJob, "FROMPOSITION");
            
            /* 20180809, Modify , Sorter Product Slot Sel Info ==>> */
            String jobType = CommonUtil.getValue(sortJob, "JOBTYPE");
            String fromPortName = CommonUtil.getValue(sortJob, "FROMPORTNAME");
            String toPortName = CommonUtil.getValue(sortJob, "TOPORTNAME");
            String toPostion = CommonUtil.getValue(sortJob, "TOPOSITION");
            String turnSideFlag = CommonUtil.getValue(sortJob, "TURNSIDEFLAG");
            String turnOverFlag = CommonUtil.getValue(sortJob, "TURNOVERFLAG");
            String scrapFlag = CommonUtil.getValue(sortJob, "SCRAPFLAG");
            String outStageFlag = CommonUtil.getValue(sortJob, "OUTSTAGEFLAG");
            /* <<== 20180809, Modify , Sorter Product Slot Sel Info */
            
            //convert
            long position = 0;
            try
            {
                position = Long.parseLong(fromPosition);
            }
            catch (Exception ex)
            {
                logger.warn("Position parsing failed");
            }

            if (productData.getKey().getProductName().equals(fromProductName) && productData.getPosition() == position)
            {
                //samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
                /* 20180809, Modify , Sorter Product Slot Sel Info ==>> */
                //
                if(StringUtil.equals(jobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE))
                {
                    if(StringUtil.equals(fromPortName, toPortName) &&  !StringUtil.equals(fromPosition,toPostion))
                    {
                        samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
                    }
                }
                else if(StringUtil.equals(jobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNOVER))
                {
                    if(StringUtil.equals(fromPortName, toPortName) && 
                            StringUtil.equals(turnOverFlag, GenericServiceProxy.getConstantMap().FLAG_Y))
                    {
                        samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
                    }                    
                }
                else if(StringUtil.equals(jobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNSIDE))
                {
                    if(StringUtil.equals(fromPortName, toPortName) && 
                            StringUtil.equals(turnSideFlag, GenericServiceProxy.getConstantMap().FLAG_Y))
                    {
                        samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
                    }      
                }
                else if(StringUtil.equals(jobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP))
                {
                    if(StringUtil.equals(scrapFlag, GenericServiceProxy.getConstantMap().FLAG_Y) || 
                            StringUtil.equals(outStageFlag, GenericServiceProxy.getConstantMap().FLAG_Y))
                    {
                        samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
                    }   
                }
                else if(StringUtil.equals(jobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_CHANGE))
                {        
                    samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
                }
                /* <<== 20180809, Modify , Sorter Product Slot Sel Info */
                break;
            }
        }
        
        return samplingFlag;
    }
    
	public void AutoDownloadSortJob(EventInfo eventInfo, Document doc, String machineName)
			throws CustomException
	{
		//get line machine
		Machine machineData	= CommonUtil.getMachineInfo(machineName);
		
		try
		{
			String condition = "WHERE MACHINENAME = ? AND JOBSTATE IN (?, ?) ORDER BY SEQ ";
			Object[] bindSet = new Object[]{machineName, "RESERVED", "CANCELED"};
			
			List<SortJob> reservedSortJoblist = ExtendedObjectProxy.getSortJobService().select(condition, bindSet);
			
			//get SortJob
			SortJob sortjob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {reservedSortJoblist.get(0).getJobName()});
			
			String sorterjobName = sortjob.getJobName();
			
			eventInfo.setEventName("SorterJobStartCommand");
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", eventInfo.getEventName());
			SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", "");
			
			//Body
			this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), sorterjobName, machineName);
			
			//fromLotList
			try
			{
				condition = "WHERE JOBNAME = ? AND TRANSFERDIRECTION = ? ORDER BY PORTNAME ";
				bindSet = new Object[]{sorterjobName, "TARGET"};
				List<SortJobCarrier> testSortJobCarrierList = ExtendedObjectProxy.getSortJobCarrierService().select(condition, bindSet);
				
				{
					condition = "WHERE JOBNAME = ? AND TRANSFERDIRECTION = ?";
					bindSet = new Object[]{sorterjobName, "SOURCE"};
					List<SortJobCarrier> fromLotList = ExtendedObjectProxy.getSortJobCarrierService().select(condition, bindSet);
					
					for(SortJobCarrier fromLot : fromLotList)
					{
						Element fromLotListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "FROMLOTLIST", true);
						
						for(SortJobCarrier testFromLot : testSortJobCarrierList)
						{
							Element fromLotElement = this.generateFromLotElement(fromLot.getLotName(), fromLot.getPortName(), fromLot.getCarrierName(), sorterjobName, testFromLot.getPortName());
							
							fromLotListElement.addContent(fromLotElement);
						}
					}
				}
			}
			catch(Exception ex)
			{
				
			}		
			
			//MES-EAP protocol
			SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", machineData.getKey().getMachineName());
			
			String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
			
			GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
		}
		catch(Exception ex)
		{
			logger.debug("Not Found Reserve Sort Job");
		}
	}
	
	public Element generateBodyTemplate(Element bodyElement, String jobName,String machineName) throws CustomException
	{
//		Element machineNameElement = new Element("MACHINENAME");
//		machineNameElement.setText(machineName);
//		bodyElement.addContent(machineNameElement);
//		
//		Element jobNameElement = new Element("JOBNAME");
//		jobNameElement.setText(jobName);
//		bodyElement.addContent(jobNameElement);
		
		bodyElement.removeChild("MACHINENAME");
		bodyElement.removeChild("JOBNAME");
		
		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);
		bodyElement.addContent(machineNameElement);
		
		Element jobNameElement = new Element("JOBNAME");
		jobNameElement.setText(jobName);
		bodyElement.addContent(jobNameElement);
		
		Element fromLotListElement = new Element("FROMLOTLIST");
		fromLotListElement.setText("");
		bodyElement.addContent(fromLotListElement);
		
		return bodyElement;
	}
	
	private Element generateSorterProductElement(String productName, String fromPosition, String toPortName, String toCarrierName,
            String toPosition, String turnSideFlag, String scrapFlag, String trunOverFlag, String outStageFlag)
            throws CustomException
    {
        Element sorterProductElement = new Element("SORTERPRODUCT");

        Element productNameElement = new Element("PRODUCTNAME");
        productNameElement.setText(productName);
        sorterProductElement.addContent(productNameElement);

        Element fromPositionElement = new Element("FROMPOSITION");
        fromPositionElement.setText(fromPosition);
        sorterProductElement.addContent(fromPositionElement);

        Element toPortNameElement = new Element("TOPORTNAME");
        toPortNameElement.setText(toPortName);
        sorterProductElement.addContent(toPortNameElement);

        Element toCarrierNameElement = new Element("TOCARRIERNAME");
        toCarrierNameElement.setText(toCarrierName);
        sorterProductElement.addContent(toCarrierNameElement);

        Element toPositionElement = new Element("TOPOSITION");
        toPositionElement.setText(toPosition);
        sorterProductElement.addContent(toPositionElement);

        Element turnSideFlagElement = new Element("TURNSIDEFLAG");
        turnSideFlagElement.setText(turnSideFlag);
        sorterProductElement.addContent(turnSideFlagElement);

        Element turnOverFlagElement = new Element("TURNOVERFLAG");
        turnOverFlagElement.setText(trunOverFlag);
        sorterProductElement.addContent(turnOverFlagElement);

        Element outStageFlagElement = new Element("OUTSTAGEFLAG");
        outStageFlagElement.setText(outStageFlag);
        sorterProductElement.addContent(outStageFlagElement);

        Element scrapFlagElement = new Element("SCRAPFLAG");
        scrapFlagElement.setText(scrapFlag);
        sorterProductElement.addContent(scrapFlagElement);

        return sorterProductElement;
    }
	
//	public Element generateSorterProductElement(String productName, String fromPosition, String toPortName, String toCarrierName,
//			String toPosition, String turnFlag, String scrapFlag, String inspectFlag)
//			throws CustomException
//	{
//		Element sorterProductElement = new Element("SORTERPRODUCT");
//		
//		Element productNameElement = new Element("PRODUCTNAME");
//		productNameElement.setText(productName);
//		sorterProductElement.addContent(productNameElement);
//		
//		Element fromPositionElement = new Element("FROMPOSITION");
//		fromPositionElement.setText(fromPosition);
//		sorterProductElement.addContent(fromPositionElement);
//		
//		Element toPortNameElement = new Element("TOPORTNAME");
//		toPortNameElement.setText(toPortName);
//		sorterProductElement.addContent(toPortNameElement);
//		
//		Element toCarrierNameElement = new Element("TOCARRIERNAME");
//		toCarrierNameElement.setText(toCarrierName);
//		sorterProductElement.addContent(toCarrierNameElement);
//		
//		Element toPositionElement = new Element("TOPOSITION");
//		toPositionElement.setText(toPosition);
//		sorterProductElement.addContent(toPositionElement);
//		
//		Element turnFlagElement = new Element("TURNFLAG");
//		turnFlagElement.setText(turnFlag);
//		sorterProductElement.addContent(turnFlagElement);
//		
//		/*Element degreeElement = new Element("TURNDEGREE");
//		degreeElement.setText(turnDegree);
//		sorterProductElement.addContent(degreeElement);*/
//		
//		Element scrapFlagElement = new Element("SCRAPFLAG");
//		scrapFlagElement.setText(scrapFlag);
//		sorterProductElement.addContent(scrapFlagElement);
//		
//		Element inspectFlagElement = new Element("INSPECTFLAG");
//		inspectFlagElement.setText("N");
//		sorterProductElement.addContent(inspectFlagElement);
//		
//		return sorterProductElement;
//	}
	
	public Element generateFromLotElement(String lotName, String portName, String carrierName, String jobName, String toPortName)
			throws CustomException
	{
		Element fromLotElement = new Element("FROMLOT");
		
		Element lotNameElement = new Element("LOTNAME");
		lotNameElement.setText(lotName);
		fromLotElement.addContent(lotNameElement);
		
		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(portName);
		fromLotElement.addContent(portNameElement);
		
		Element carrierNameElement = new Element("CARRIERNAME");
		carrierNameElement.setText(carrierName);
		fromLotElement.addContent(carrierNameElement);
		
		Element sorterProductListElement = new Element("SORTERPRODUCTLIST");
		sorterProductListElement.setText("");
		fromLotElement.addContent(sorterProductListElement);
		
		try
		{
			String condition = "WHERE JOBNAME = ? AND FROMCARRIERNAME = ? AND FROMLOTNAME = ? AND FROMPORTNAME = ? AND TOPORTNAME = ? ORDER BY FROMPOSITION";
			Object[] bindSet = new Object[]{jobName, carrierName, lotName, portName, toPortName};
			List<SortJobProduct> sortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(condition, bindSet);
			
			for(SortJobProduct sortJobProduct : sortJobProductList)
			{
				sorterProductListElement = XmlUtil.getChild(fromLotElement, "SORTERPRODUCTLIST", true);
				
				Element sortProductElement = this.generateSorterProductElement(sortJobProduct.getProductName(), sortJobProduct.getFromPosition(),
						sortJobProduct.getToPortName(), sortJobProduct.getToCarrierName(), sortJobProduct.getToPosition(),
						sortJobProduct.getTurnSideFlag(), sortJobProduct.getScrapFlag(),sortJobProduct.getTurnOverFlag(),sortJobProduct.getOutStageFlag());
				
				sorterProductListElement.addContent(sortProductElement);
			}
			
		}
		catch(Exception ex)
		{
			
		}
	
		return fromLotElement;
	}
	
	public int getMaxSequence(String machineName)
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT NVL(MAX(SEQ)+1,1)		\n")
		.append("FROM CT_SORTJOB C		\n")
		.append("WHERE C.MACHINENAME=:MACHINENAME		\n")
		.append("  AND C.JOBSTATE IN ('RESERVED','CANCELED')		\n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();		
		bindMap.put("MACHINENAME", machineName);
		
		int seq = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForInt(queryBuffer.toString(), bindMap);
		
		return seq;
	}
	
	/**
	 * get reserved sorter job
	 * @author swcho
	 * @since 2016.07.20
	 * @param machineName
	 * @return
	 * @throws CustomException
	 */
	public List<SortJob> getReservedJobList(String machineName) throws CustomException
	{
		List<SortJob> result;
		
		try
		{
			//including executing & waiting stuffs
			String condition = " WHERE machineName = ? AND jobName = ? AND jobState NOT IN (?, ?, ?) ORDER BY seq ";
			Object[] bindSet = new Object[]{machineName, "CREATE", "ABORT", "ENDED"};
			
			result = ExtendedObjectProxy.getSortJobService().select(condition, bindSet);
		}
		catch (Exception ex)
		{
			result = new ArrayList<SortJob>();
		}
		
		return result;
	}
	
	public String getSortPortType(String machineName,String portName,String carrierName) throws CustomException
	{
		String portType="";
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT C.TRANSFERDIRECTION FROM CT_SORTJOBCARRIER C,CT_SORTJOB S ");
		sb.append(" WHERE 1=1  AND C.JOBNAME=S.JOBNAME ");
		sb.append(" AND C.CARRIERNAME=? AND C.MACHINENAME=? AND PORTNAME=? AND S.JOBSTATE=? ");
		
		Object[] args = new Object[]{carrierName,machineName,portName,"STARTED"};

		List<ListOrderedMap> resultList =  greenFrameServiceProxy.getSqlTemplate().queryForList(sb.toString(), args);
		
		if(resultList.size()>0)
		{
			portType = resultList.get(0).get("TRANSFERDIRECTION").toString();
			
			if(portType.equals("SOURCE"))
			{
				portType = "PL";
			}
			if(portType.equals("TARGET"))
			{
				portType="PU";
			}
		}
		else
		{
			throw new CustomException("SORT-0003","");
		}

		return portType;
	}
	
	public String getSortJobType(String carrierName) throws CustomException
	{
		String portType = "";
		
//		String strSql = "SELECT JOBTYPE " +
//				"  FROM CT_SORTJOB S, CT_SORTJOBCARRIER SC " +
//				" WHERE     S.JOBNAME = SC.JOBNAME " +
//				"       AND SC.CARRIERNAME = :CARRIERNAME " +
//				"       AND S.JOBSTATE = :JOBSTATE ";
		String strSql = "SELECT DETAILJOBTYPE AS JOBTYPE " +
                "  FROM CT_SORTJOB S, CT_SORTJOBCARRIER SC " +
                " WHERE     S.JOBNAME = SC.JOBNAME " +
                "       AND SC.CARRIERNAME = :CARRIERNAME " +
                "       AND S.JOBSTATE = :JOBSTATE ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("CARRIERNAME", carrierName);
		bindMap.put("JOBSTATE", "STARTED");

		List<Map<String, Object>> sortJobType = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(sortJobType != null && sortJobType.size() > 0)
		{
			portType = (String)sortJobType.get(0).get("JOBTYPE");
		}

		return portType;
	}
	
	public List<Map<String, Object>> getSortJobNameByCarrierName(String carrierName) throws CustomException
    {
        String jobName = "";
        
//      String strSql = "SELECT JOBTYPE " +
//              "  FROM CT_SORTJOB S, CT_SORTJOBCARRIER SC " +
//              " WHERE     S.JOBNAME = SC.JOBNAME " +
//              "       AND SC.CARRIERNAME = :CARRIERNAME " +
//              "       AND S.JOBSTATE = :JOBSTATE ";
        String strSql = "SELECT S.JOBNAME AS JOBNAME, S.JOBTYPE AS JOBTYPE, S.DETAILJOBTYPE AS DETAILJOBTYPE " +
                "  FROM CT_SORTJOB S, CT_SORTJOBCARRIER SC " +
                " WHERE     S.JOBNAME = SC.JOBNAME " +
                "       AND SC.CARRIERNAME = :CARRIERNAME " +
                "       AND S.JOBSTATE = :JOBSTATE ";

        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("CARRIERNAME", carrierName);
        bindMap.put("JOBSTATE", "STARTED");

        List<Map<String, Object>> sortJobList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
        
        if(sortJobList != null && sortJobList.size() > 0)
        {
            jobName = (String)sortJobList.get(0).get("JOBNAME");
        }

        //return jobName;
        return sortJobList;
    }
	
	public String getTransferDirection(String carrierName) throws CustomException
	{
		String transferDirection = "";
		
		String strSql = "SELECT SC.TRANSFERDIRECTION " +
				"  FROM CT_SORTJOB S, CT_SORTJOBCARRIER SC " +
				" WHERE     S.JOBNAME = SC.JOBNAME " +
				"       AND SC.CARRIERNAME = :CARRIERNAME " +
				"       AND S.JOBSTATE = :JOBSTATE ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("CARRIERNAME", carrierName);
		bindMap.put("JOBSTATE", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED);

		List<Map<String, Object>> sortTransferDirection = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(sortTransferDirection != null && sortTransferDirection.size() > 0)
		{
			transferDirection = (String)sortTransferDirection.get(0).get("TRANSFERDIRECTION");
		}

		return transferDirection;
	}
	
	public String getScrapFlagAndOutStageFlag(String carrierName, String outStageFlag) throws CustomException
    {
        String returnFlag = GenericServiceProxy.getConstantMap().Flag_N;
        
//        String strSql = "SELECT SC.TRANSFERDIRECTION " +
//                "  FROM CT_SORTJOB S, CT_SORTJOBCARRIER SC, CT_SORTJOBPRODUCT SP " +
//                " WHERE 1=1 " +
//                "   AND SC.CARRIERNAME = :CARRIERNAME " +
//                "   AND SP.CARRIERNAME = SC.CARRIERNAME " +
//                "   AND S.JOBNAME = SC.JOBNAME " +
//                "   AND SC.JOBNAME = SP.JOBNAME " +
//                "   AND S.JOBSTATE = :JOBSTATE " ;
        String strSql = " SELECT SP.JOBNAME,                                              \n" +
                        "        NVL(SP.SCRAPFLAG, 'N') AS SCRAPFLAG,                     \n" +
                        "        NVL(SP.OUTSTAGEFLAG, 'N') AS OUTSTAGEFLAG                \n" +
                        "   FROM CT_SORTJOB S, CT_SORTJOBCARRIER SC, CT_SORTJOBPRODUCT SP \n" +
                        "  WHERE 1=1                                                      \n" +
                        "    AND SC.CARRIERNAME = :CARRIERNAME                            \n" +
                        "    AND SP.FROMCARRIERNAME = SC.CARRIERNAME                      \n" +
                        "    AND SP.FROMPORTNAME = SC.PORTNAME                            \n" +
                        "    AND S.JOBNAME = SC.JOBNAME                                   \n" +
                        "    AND SC.JOBNAME = SP.JOBNAME                                  \n" +
                        "    AND S.JOBSTATE = :JOBSTATE                                   \n" +
                        " GROUP BY SP.JOBNAME, SP.SCRAPFLAG, SP.OUTSTAGEFLAG              \n" ;

        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("CARRIERNAME", carrierName);
        bindMap.put("JOBSTATE", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED);

        List<Map<String, Object>> sortScrapAndOutStageFlag = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
        
        if(sortScrapAndOutStageFlag != null && sortScrapAndOutStageFlag.size() > 0)
        {
            if(StringUtil.equals(outStageFlag, GenericServiceProxy.getConstantMap().Flag_Y) && 
                    StringUtil.equals(sortScrapAndOutStageFlag.get(0).get("OUTSTAGEFLAG").toString(), GenericServiceProxy.getConstantMap().Flag_Y))
            {
                //returnFlag = sortScrapAndOutStageFlag.get(0).get("OUTSTAGEFLAG").toString();
                returnFlag = GenericServiceProxy.getConstantMap().SORT_JOBTYPE_OUTSTAGE;
            }
            else
            {
                //returnFlag = sortScrapAndOutStageFlag.get(0).get("SCRAPFLAG").toString();
                returnFlag = GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP;
            }
        }

        return returnFlag;
    }
	
	/**
     * @author wghuang
     * @param durableData
     * @param lotData
     * @param machineData
     * @param portData
     * @return SortJobList
     * @throws CustomException
     */
    public List<ListOrderedMap>getSortJobList(Durable durableData, Lot lotData, Machine machineData, Port portData) throws CustomException
    {
        List<ListOrderedMap> sortJobList = new ArrayList<ListOrderedMap>();

        try
        {
            StringBuffer sqlBuffer = new StringBuffer();
            sqlBuffer.append("SELECT J.jobName, J.jobState, C.machineName, C.portName, C.carrierName, P.fromLotName, ").append("\n")
                    .append("        P.productName, P.fromPosition, J.jobType, P.fromPortName, P.toPortName, P.toPosition, ").append("\n")
                    .append("        NVL(P.turnSideFlag, ?) AS turnSideFlag, NVL(P.turnOverFlag, ?) AS turnOverFlag, ").append("\n")
                    .append("        NVL(P.scrapFlag, ?) AS scrapFlag, NVL(P.outStageFlag, ?) AS outStageFlag ").append("\n")
                    .append("       FROM CT_SortJob J, CT_SortJobCarrier C, CT_SortJobProduct P").append("\n")
                    .append("   WHERE J.jobName = C.jobName").append("\n")
                    .append("    AND C.jobName = P.jobName").append("\n")
                    .append("    AND C.machineName = P.machineName").append("\n")
                    .append("    AND C.carrierName = P.fromCarrierName").append("\n")
                    .append("    AND C.carrierName = ?").append("\n")
                    .append("    AND C.lotName = ?").append("\n")
                    .append("    AND C.machineName = ?").append("\n")
                    .append("    AND C.portName = ?").append("\n")
                    .append("    AND J.jobState IN (?,?,?)");

            Object[] bindList = new Object[] { "N", "N", "N", "N", durableData.getKey().getDurableName(), lotData.getKey().getLotName(),
                                                machineData.getKey().getMachineName(), portData.getKey().getPortName(),
                                                //GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED};
                                                GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED};

            sortJobList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindList);
        }
        catch (Exception ex)
        {
            logger.debug("No sorter job");
            throw new CustomException("SYS-9999", "SortJob", "No job for Product");
        }
        return sortJobList;
    }
    
    /**
     * 
     * @Name     setEventName
     * @since    2018. 9. 28.
     * @author   hhlee
     * @contents 
     *           
     * @param sortJobName
     * @param sortTransFerDirection
     * @param trackInEventName
     * @return
     * @throws CustomException
     */
    public String setSortEventName(String sortJobName, String sortTransFerDirection, String trackInEventName) throws CustomException
    {        
        if(StringUtil.isNotEmpty(sortJobName))
        {
            if(StringUtil.equals(sortTransFerDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE) ||
                    StringUtil.equals(sortTransFerDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_ALONE))
            {
                trackInEventName = GenericServiceProxy.getConstantMap().CHANGEROUT_TRACKIN; 
            }
            else if(StringUtil.equals(sortTransFerDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET))
            {
                trackInEventName = GenericServiceProxy.getConstantMap().CHANGERIN_TRACKIN; 
            }
            else if(StringUtil.equals(sortTransFerDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_BOTH))
            {
                trackInEventName = GenericServiceProxy.getConstantMap().CHANGERINOUT_TRACKIN; 
            }
            else
            {      
            }            
        }
        
        return trackInEventName;
    }
    
    
    /**
     * 
     * @Name     setSortEventComment
     * @since    2018. 9. 28.
     * @author   hhlee
     * @contents 
     *           
     * @param sortJobName
     * @param sortJobType
     * @param sortTransFerDirection
     * @param lotEventName
     * @return
     * @throws CustomException
     */
    public String setSortEventComment(String carrierName, String sortJobName, String sortJobType, 
            String sortTransFerDirection, String lotEventName, String getOutStageFlag) throws CustomException
    {     
        String trackInEventComment = StringUtil.EMPTY;
        
        String scrapOutStageType = this.getScrapFlagAndOutStageFlag(carrierName, GenericServiceProxy.getConstantMap().Flag_Y);
        
        if(StringUtil.isNotEmpty(sortJobName))
        {            
            if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_CHANGE))
            {
                if(StringUtil.equals(sortTransFerDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE))
                {
                    trackInEventComment = "ChangeOut" + lotEventName;
                }
                else if(StringUtil.equals(sortTransFerDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET))
                {
                    trackInEventComment = "ChangeIn" + lotEventName;
                }
            }
            else if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE))                
            {
                trackInEventComment = "SlotMapChg" + lotEventName;
            }
            else if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNSIDE))                
            {
                trackInEventComment = "TurnSide" + lotEventName;
            }
            else if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNOVER))                
            {
                trackInEventComment = "TurnOver" + lotEventName;
            }
            else if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP))                
            {
                trackInEventComment = scrapOutStageType + lotEventName;
                
                //if(StringUtil.equals(getOutStageFlag, GenericServiceProxy.getConstantMap().Flag_Y) && 
                //        StringUtil.equals(outStageFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                //{
                //    trackInEventComment = scrapOutStageType + lotEventName;
                //}
                //else
                //{
                //    trackInEventComment = scrapOutStageType + lotEventName;
                //}                
            }
            else
            {                
            }
        }
        else
        {
            trackInEventComment = "-----";
        }
        
        return trackInEventComment;
    }
    
    /**
     * 
     * @Name     setSortLotNote
     * @since    2018. 9. 28.
     * @author   hhlee
     * @contents 
     *           
     * @param carrierName
     * @param sortJobName
     * @param sortJobType
     * @param sortTransFerDirection
     * @param trackInEventName
     * @return
     * @throws CustomException
     */
    public String setSortLotNote(String carrierName, String sortJobName, String sortJobType, String sortTransFerDirection, String trackInEventName) throws CustomException
    {
        String lotNote = StringUtil.EMPTY;
        
        if(StringUtil.isNotEmpty(sortJobName))
        {
            List<SortJobProduct> sortJobProductList = null;
            try
            {  
                if(StringUtil.equals(sortTransFerDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET))
                {
                    sortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(" JOBNAME = ? AND TOCARRIERNAME = ? ORDER BY TOPORTNAME, TOPOSITION ", new Object[] {sortJobName, carrierName});
                }
                else
                {
                    sortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(" JOBNAME = ? AND FROMCARRIERNAME = ? ORDER BY FROMPORTNAME, FROMPOSITION ", new Object[] {sortJobName, carrierName});
                }
                
                if(sortJobProductList != null && sortJobProductList.size() >0)
                {                    
                    for(SortJobProduct sortJobProductData : sortJobProductList)
                    {
                        if(StringUtil.isEmpty(lotNote))
                        {
                            if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE))
                            {
                                lotNote = sortJobProductData.getProductName() + "FromSlot_" + sortJobProductData.getFromPosition() + "ToSlot_" + sortJobProductData.getToPosition();
                            }
                            else
                            {
                                lotNote = sortJobProductData.getProductName();
                            }
                        }
                        else
                        {
                            if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE))
                            {
                                lotNote = lotNote + " , " + sortJobProductData.getProductName() + "FromSlot_" + sortJobProductData.getFromPosition() + "ToSlot_" + sortJobProductData.getToPosition();
                            }
                            else
                            {
                                lotNote = lotNote + " , " + sortJobProductData.getProductName();
                            }
                        }                        
                    }
                    lotNote = "[" + trackInEventName + "] " + lotNote;                    
                }
            }
            catch (Exception ex)
            {  
                logger.error(ex.getStackTrace());
            }            
        }
        
        return lotNote;
    }
    
    /**
     * 
     * @Name     getSortJobProductByJobNameandFromLotName
     * @since    2018. 12. 5.
     * @author   hhlee
     * @contents Inquery Sorter Job Product List
     *           
     * @param jobName
     * @param fromLotName
     * @return
     * @throws CustomException
     */
    public List<Map<String, Object>> getSortJobProductByJobNameandFromLotName(String jobName, String fromLotName) throws CustomException
    {
        String strSql = " SELECT SJ.JOBNAME, SJP.PRODUCTNAME, SJP.MACHINENAME,                             \n"               
                      + "        SJP.FROMLOTNAME, SJP.FROMCARRIERNAME, SJP.FROMPORTNAME, SJP.FROMPOSITION, \n"
                      + "     SJP.TOLOTNAME, SJP.TOCARRIERNAME, SJP.TOPORTNAME, SJP.TOPOSITION             \n"
                      + "   FROM CT_SORTJOB SJ, CT_SORTJOBPRODUCT SJP                                      \n"
                      + "  WHERE 1=1                                                                       \n"
                      + "    AND SJ.JOBNAME = :JOBNAME                                                     \n"
                      + "    AND SJ.JOBNAME = SJP.JOBNAME                                                  \n"
                      + "    AND SJP.FROMLOTNAME = :FROMLOTNAME                                            \n";

        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("JOBNAME", jobName);
        bindMap.put("FROMLOTNAME", fromLotName);

        List<Map<String, Object>> sortJobPorductList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
              
        return sortJobPorductList;      
    }
    
    /**
     * 
     * @Name     sortJobCanceled
     * @since    2019. 4. 9.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineName
     * @param jobName
     * @param resultDescription
     * @throws CustomException
     */
    public void sortJobCanceled(EventInfo eventInfo, String machineName ,String jobName, String resultDescription) throws CustomException
    {
        try
        {
            eventInfo.setEventName("CancelSortJob");
            
            int seq = this.getMaxSequence(machineName);
            
            SortJob sortJob = this.selectByKey(false, new Object[] {jobName});
            
            sortJob.setSeq(seq);
            sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CANCELED);
            
            sortJob.setNote(resultDescription);
            
            sortJob.setEventComment(eventInfo.getEventComment());
            sortJob.setEventName(eventInfo.getEventName());
            sortJob.setEventTime(eventInfo.getEventTime());
            sortJob.setEventUser(eventInfo.getEventUser());     
            
            this.modify(eventInfo, sortJob);
        }
        catch (Exception ex)
        {
            logger.error("[sortJobCanceled] - " + ex);
        }
    }
}
