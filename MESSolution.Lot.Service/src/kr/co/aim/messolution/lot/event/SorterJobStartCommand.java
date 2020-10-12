package kr.co.aim.messolution.lot.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class SorterJobStartCommand extends AsyncHandler {

	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);

		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);

		//get line machine
		Machine machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		String sourceTransferDirection = StringUtil.EMPTY;
		String targetTransferDirection = StringUtil.EMPTY;
		
		if (StringUtils.equals(jobName, GenericServiceProxy.getConstantMap().INSERT_LOG_NONE))
		{
		    //Body
            this.noSorterJobgenerateBodyTemplate(SMessageUtil.getBodyElement(doc), jobName, machineName, StringUtil.EMPTY, portName, carrierName);
		}
		else
		{
    		//get SortJob
    		SortJob sortjob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {jobName});
            
    		/* 20180809, Add, Sorter Job Product List ==>> */
    		String sorterjobName = sortjob.getJobName();
    		String sorterjobType = sortjob.getJobType();
    		/* <<== 20180809, Add, Sorter Job Product List */
    		
    		if(StringUtil.equals(sortjob.getJobState(), GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED))
    		{
    			throw new CustomException("SYS-9999", "Sorter Job Download", "Please Check JobState! (" + sortjob.getJobState() + ")");
    		}

    		//Body
    		this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), sorterjobName, machineName);

    		/* 20180809, Add, Sorter Job Product List ==>> */
    		if(StringUtil.equals(sorterjobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_CHANGE))
    		{
    		    sourceTransferDirection = GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE;
    		    targetTransferDirection = GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET;
    		}
    		else if(StringUtil.equals(sorterjobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE))
    		{
    		    sourceTransferDirection = GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_BOTH;
                targetTransferDirection = GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_BOTH;
    		}
    		else if(StringUtil.equals(sorterjobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNOVER))
    		{
    		    sourceTransferDirection = GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_ALONE;
                targetTransferDirection = GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_ALONE;
    		}
    		else if(StringUtil.equals(sorterjobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNSIDE))
            {
    		    sourceTransferDirection = GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_ALONE;
                targetTransferDirection = GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_ALONE;
            }
    		else if(StringUtil.equals(sorterjobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP))
            {
    		    sourceTransferDirection = GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_ALONE;
                targetTransferDirection = GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_ALONE;
            }
    		else
    		{    		    
    		}
    		/* <<== 20180809, Add, Sorter Job Product List */   
    		
    		//fromLotList
    		try
    		{
    		    /* 20180809, Add, Sorter Job Product List ==>> */
    			//String condition = "WHERE JOBNAME = ? AND TRANSFERDIRECTION = 'TARGET' ORDER BY PORTNAME ";
    		    String condition = "WHERE JOBNAME = ? AND TRANSFERDIRECTION = ? ORDER BY PORTNAME ";
    		    /* <<== 20180809, Add, Sorter Job Product List */
    		    
    			Object[] bindSet = new Object[]{sorterjobName, targetTransferDirection};
    			List<SortJobCarrier> testSortJobCarrierList = ExtendedObjectProxy.getSortJobCarrierService().select(condition, bindSet);

    			{
    			    /* 20180809, Add, Sorter Job Product List ==>> */
    			    //condition = "WHERE JOBNAME = ? AND TRANSFERDIRECTION = 'SOURCE' ";
    			    condition = "WHERE JOBNAME = ? AND TRANSFERDIRECTION = ? ";
    			    /* <<== 20180809, Add, Sorter Job Product List */
    			    
    				bindSet = new Object[]{sorterjobName, sourceTransferDirection};
    				List<SortJobCarrier> fromLotList = ExtendedObjectProxy.getSortJobCarrierService().select(condition, bindSet);

    				for(SortJobCarrier fromLot : fromLotList)
    				{
    					Element fromLotListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "FROMLOTLIST", true);

    					for(SortJobCarrier testFromLot : testSortJobCarrierList)
    					{
    					    /* 20180809, Add, Sorter Job Product List ==>> */
    					    //Element fromLotElement = this.generateFromLotElement(fromLot.getLotName(), fromLot.getPortName(), fromLot.getCarrierName(), sorterjobName, testFromLot.getPortName());
    					    Element fromLotElement = this.generateFromLotElement(fromLot.getLotName(), fromLot.getPortName(), fromLot.getCarrierName(), sorterjobName, testFromLot.getPortName(), sorterjobType);
    					    /* <<== 20180809, Add, Sorter Job Product List */
    					    
    					    /* 20180930, modify, Sorter Job Product List ==>> */
    					    if(fromLotElement != null)
    					    {
    					        fromLotListElement.addContent(fromLotElement);
    					    }
    					    /* <<== 20180930, modify, Sorter Job Product List */
    					}
    				}
    			}
    		}
    		catch(Exception ex)
    		{

    		}
		}

		//MES-BC protocol
		//SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", machineData.getKey().getMachineName());

		String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");

		/* 20180914, hhlee, modify, Because I go to the case where the log record is in trouble ==>> */
		SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("PEMsvr"));
		//GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
        GenericServiceProxy.getESBServive().recordMessagelogAftersendBySender(targetSubjectName, doc, "EISSender");
        /* <<== 20180914, hhlee, modify, Because I go to the case where the log record is in trouble */
	}

	private Element generateBodyTemplate(Element bodyElement, String jobName,String machineName) throws CustomException
	{
		bodyElement.removeContent();

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

	/**
	 * 
	 * @Name     generateFromLotElement
	 * @since    2018. 8. 9.
	 * @author   hhlee
	 * @contents Sorter Product List
	 *           
	 * @param lotName
	 * @param portName
	 * @param carrierName
	 * @param jobName
	 * @param toPortName
	 * @param jobType
	 * @return
	 * @throws CustomException
	 */
	private Element generateFromLotElement(String lotName, String portName, String carrierName, String jobName, String toPortName, String jobType)
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
            String condition = StringUtil.EMPTY;
            //condition = "WHERE JOBNAME = ? AND FROMCARRIERNAME = ? AND FROMLOTNAME = ? AND FROMPORTNAME = ? AND TOPORTNAME = ? ORDER BY FROMPOSITION";
            condition = " WHERE JOBNAME = ? AND FROMCARRIERNAME = ? AND FROMLOTNAME = ? AND FROMPORTNAME = ? AND TOPORTNAME = ? ";
            Object[] bindSet = null;
            
            if(StringUtil.equals(jobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE))
            {
                condition = condition + " AND FROMPORTNAME = TOPORTNAME AND FROMPOSITION <> TOPOSITION ";
                bindSet = new Object[]{jobName, carrierName, lotName, portName, toPortName};
            }
            else if(StringUtil.equals(jobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNOVER))
            {
                condition = condition + " AND FROMPORTNAME = TOPORTNAME AND NVL(TURNOVERFLAG, ?) = ? ";
                bindSet = new Object[]{jobName, carrierName, lotName, portName, toPortName, "N", "Y"};
            }
            else if(StringUtil.equals(jobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNSIDE))
            {
                condition = condition + " AND FROMPORTNAME = TOPORTNAME AND NVL(TURNSIDEFLAG, ?) = ? ";
                bindSet = new Object[]{jobName, carrierName, lotName, portName, toPortName, "N", "Y"};
            }
            else if(StringUtil.equals(jobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP))
            {
                condition = condition + " AND (NVL(SCRAPFLAG, ?) = ? OR  NVL(OUTSTAGEFLAG, ?) = ?)";
                bindSet = new Object[]{jobName, carrierName, lotName, portName, toPortName, "N", "Y", "N", "Y"};
            }
            else
            {
            	bindSet = new Object[]{jobName, carrierName, lotName, portName, toPortName};
            }

            condition = condition + " ORDER BY FROMPOSITION ";
            
            List<SortJobProduct> sortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(condition, bindSet);

            for(SortJobProduct sortJobProduct : sortJobProductList)
            {
                sorterProductListElement = XmlUtil.getChild(fromLotElement, "SORTERPRODUCTLIST", true);
                Element sortProductElement = null;
                if(StringUtil.equals(jobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP))
                {
                    sortProductElement = this.generateSorterProductElement(sortJobProduct.getProductName(), sortJobProduct.getFromPosition(),
                            StringUtil.EMPTY, StringUtil.EMPTY, sortJobProduct.getToPosition(),
                               sortJobProduct.getTurnSideFlag(), sortJobProduct.getScrapFlag(),sortJobProduct.getTurnOverFlag(),sortJobProduct.getOutStageFlag());
                }
                else
                {
                    sortProductElement = this.generateSorterProductElement(sortJobProduct.getProductName(), sortJobProduct.getFromPosition(),
                            sortJobProduct.getToPortName(), sortJobProduct.getToCarrierName(), sortJobProduct.getToPosition(),
                                sortJobProduct.getTurnSideFlag(), sortJobProduct.getScrapFlag(),sortJobProduct.getTurnOverFlag(),sortJobProduct.getOutStageFlag());
                }
                sorterProductListElement.addContent(sortProductElement);
            }

        }
        catch(Exception ex)
        {
            eventLog.warn(ex.getStackTrace());
            fromLotElement = null;
        }

        return fromLotElement;
    }
	
	/**
	 * @Name     noSorterJobgenerateBodyTemplate
	 * @since    2018. 5. 22.
	 * @author   hhlee
	 * @contents No Sorter Job generate Body Template
	 * @param bodyElement
	 * @param jobName
	 * @param machineName
	 * @param lotName
	 * @param portName
	 * @param carrierName
	 * @return
	 * @throws CustomException
	 */
	private Element noSorterJobgenerateBodyTemplate(Element bodyElement, String jobName,String machineName,
	                                                 String lotName, String portName, String carrierName) throws CustomException
    {
	    bodyElement.removeContent();

        Element machineNameElement = new Element("MACHINENAME");
        machineNameElement.setText(machineName);
        bodyElement.addContent(machineNameElement);

        Element jobNameElement = new Element("JOBNAME");
        jobNameElement.setText(jobName);
        bodyElement.addContent(jobNameElement);

        Element fromLotListElement = new Element("FROMLOTLIST");
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

        fromLotListElement.addContent(fromLotElement);
        bodyElement.addContent(fromLotListElement);

        return bodyElement;
    }


}
