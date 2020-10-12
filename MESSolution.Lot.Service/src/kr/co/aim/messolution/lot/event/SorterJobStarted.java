package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.jdom.Document;
import org.jdom.Element;

public class SorterJobStarted extends AsyncHandler {
	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SortJobStart", getEventUser(), getEventComment(), null, null);

		// parsing
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName    = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);

		SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {jobName});

		sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED);
		sortJob.setEventComment(eventInfo.getEventComment());
		sortJob.setEventName(eventInfo.getEventName());
		sortJob.setEventTime(eventInfo.getEventTime());
		sortJob.setEventUser(eventInfo.getEventUser());		
		
		ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);

		/* 20180811, Modify, doBatchProcessStart use ==>> */
		this.doBatchProcessStart(doc, eventInfo, machineName, jobName);
		/* <<== 20180811, Modify, doBatchProcessStart use */
		
		/* 20180811, delete, doBatchProcessStart use ==>> */
		////TrackIn Lot ('WAIT' state)
		//this.TrackInLot(doc, jobName, machineName);
		/* <<== 20180811, delete, doBatchProcessStart use */
	}

	/**
	 * batch TK IN
	 * @author swcho
	 * @since 2015.03.11
	 * @param doc
	 * @param machineName
	 * @param jobName
	 */
	private void doBatchProcessStart(Document doc, EventInfo eventInfo, String machineName, String jobName)
	{
		try
		{
			//make template
			writeProcessStart(doc, machineName);

			// get sort Job carrier list
	        List<SortJobCarrier> sortJobCarrierList = new ArrayList <SortJobCarrier>();
	        
	        List<SortJob> sortJobList = new ArrayList <SortJob>();

	        try
	        {
	            sortJobCarrierList = ExtendedObjectProxy.getSortJobCarrierService().select("jobName = ?", new Object[] {jobName});
	        }
	        catch (NotFoundSignal ne)
	        {
	            sortJobCarrierList = new ArrayList <SortJobCarrier>();
	        }
	        catch(Exception e)
	        {
	            sortJobCarrierList = new ArrayList <SortJobCarrier>();
	        }
	        
            /* 20181205, hhlee, add, all product of sortjob,  SORTPRODUCTSTATE = "Started" ==>> */
	        
			for (SortJobCarrier sortCarrier : sortJobCarrierList)
			{
				String carrierName = sortCarrier.getCarrierName();
				String portName = sortCarrier.getPortName();
                
				try
	            {
				    sortJobList = ExtendedObjectProxy.getSortJobService().select("jobName = ?", new Object[] {sortCarrier.getJobName()});
	            }
	            catch (NotFoundSignal ne)
	            {
	                sortJobList = new ArrayList <SortJob>();
	            }
				
				/* 20180929, Add, SortJobCarrier trackflag update(Requested by shkim) ==>> */
				try
		        {
				    /* 20190220, hhlee, add, added EventName */
				    eventInfo.setEventName("SortJobStart");
		            SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {jobName, carrierName});
		            sortJobCarrier.settrackflag(GenericServiceProxy.getConstantMap().SORT_JOBCARRIER_TRACKFLAG_IN);
		            ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
		        }
		        catch (Exception ex)
		        {
		            eventLog.error(ex.getMessage());
		        }
				/* <<== 20180929, Add, SortJobCarrier trackflag update(Requested by shkim) */
				
				try
                {				
    				Lot lotData = CommonUtil.getLotInfoBydurableName(carrierName);
    
    				if (lotData != null)                
    				{
    					if(lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_Wait))
    					{
        					String mesMachineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
        					        lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
        					
        					SMessageUtil.setBodyItemValue(doc, "PORTNAME", portName);
        					SMessageUtil.setBodyItemValue(doc, "CARRIERNAME", carrierName);
        					SMessageUtil.setBodyItemValue(doc, "LOTNAME", lotData.getKey().getLotName());
        					SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", mesMachineRecipeName);
        
        					/* 20180928, add,  Sorter Job Validation ==>> */
        					SMessageUtil.setBodyItemValue(doc, "SORTJOBNAME", sortCarrier.getJobName());
        					SMessageUtil.setBodyItemValue(doc, "SORTJOBTYPE", sortJobList.get(0).getJobType());
        					SMessageUtil.setBodyItemValue(doc, "SORTTRANSFERDIRECTION", sortCarrier.getTransferDirection());
        					                                    
        			        /* <<== 20180928, add,  Sorter Job Validation */
        			        
        					Lot trackInLot = MESLotServiceProxy.getLotServiceImpl().LotProcessStartTrackIn(doc, eventInfo);
        					
        					/* 20190313, hhlee, add, all product of sortjob,  SORTPRODUCTSTATE = "EXECUTING" ==>> */
        					///* 20181205, hhlee, add, all product of sortjob,  SORTPRODUCTSTATE = "Started" ==>> */
        		            //try
        		            //{
        		            //    List<SortJobProduct> sortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(" JOBNAME = ? AND FROMCARRIERNAME = ? ", new Object[] {jobName, carrierName});
        		            //    
        		            //    if(sortJobProductList != null && sortJobProductList.size() > 0)
        		            //    {
        		            //        for(SortJobProduct sortJobProduct : sortJobProductList) 
        		            //        {
        		            //            MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, jobName, sortJobProduct.getProductName(), GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_EXECUTING);
        		            //        }
        		            //    }
        		            //    
        		            //}
        		            //catch (NotFoundSignal ne)
        		            //{
        		            //    //sortJobProductList = new ArrayList <SortJobProduct>();
        		            //    eventLog.warn(ne.getStackTrace());
        		            //}
        		            //catch(Exception e)
        		            //{
        		            //    //sortJobProductList = new ArrayList <SortJobProduct>();
        		            //    eventLog.warn(e.getStackTrace());
        		            //}        		            
        		            ///* <<== 20181205, hhlee, add, all product of sortjob,  SORTPRODUCTSTATE = "Started" */

                            if(!StringUtil.equals(sortCarrier.getTransferDirection(),
                                    GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET))
                            {
                                MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, jobName, sortCarrier.getCarrierName());
                            }
                            /* <<== 20190313, hhlee, add, all product of sortjob,  SORTPRODUCTSTATE = "EXECUTING" */
    					}
    					else
    					{
    					    eventLog.error(String.format("[ This Lot(%s) state is not WAIT ]", lotData.getKey().getLotName()));
    					}
    				}
    				else
    				{
    				    eventLog.error(String.format("[ This Lot(CarrierName:%s,PortName:%s) is not exist ]",carrierName, portName));
    				}
			    }
				catch(Exception e)
	            {
	                eventLog.error(String.format("[ This Lot(CarrierName:%s,PortName:%s) Track In failed ]",carrierName, portName));
	            }
				
				
			}
			
						
			try
            {
                GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
            }
            catch(Exception ex)
            {
                eventLog.warn("FMC Report Failed!");
            }
		}
		catch (CustomException ce)
		{
			eventLog.error("[ failed to do batch LotProcessStarted ]");
		}
	}

//	/**
//     * batch TK IN
//     * @author swcho
//     * @since 2015.03.11
//     * @param doc
//     * @param machineName
//     * @param jobName
//     */
//    private void doBatchProcessStart(Document doc, String machineName, String jobName)
//    {
//        try
//        {
//            //make template
//            writeProcessStart(doc, machineName);
//
//            //for (SortJobCarrier sortCarrier : ExtendedObjectProxy.getSortJobCarrierService().select("jobName = ? AND carrierName = ?", new Object[] {jobName, ""}))
//            for (SortJobCarrier sortCarrier : ExtendedObjectProxy.getSortJobCarrierService().select("jobName = ? ", new Object[] {jobName}))
//            {
//                String carrierName = sortCarrier.getCarrierName();
//                String portName = sortCarrier.getPortName();
//
//                Lot lotData = CommonUtil.getLotInfoBydurableName(carrierName);
//
//                if (lotData != null)
//                {
//                    //track in
//                    ListOrderedMap recipeData = PolicyUtil.getMachineRecipeName(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), "");
//
//                    SMessageUtil.setBodyItemValue(doc, "PORTNAME", portName);
//                    SMessageUtil.setBodyItemValue(doc, "CARRIERNAME", carrierName);
//                    SMessageUtil.setBodyItemValue(doc, "LOTNAME", lotData.getKey().getLotName());
//                    SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", CommonUtil.getValue(recipeData, "MACHINERECIPENAME"));
//
//                    GenericServiceProxy.getESBServive().send(GenericServiceProxy.getESBServive().getSendSubject("PEXsvr"), doc);
//                }
//            }
//        }
//        catch (CustomException ce)
//        {
//            eventLog.error("failed to do batch LotProcessStarted");
//        }
//    }

	/**
	 * to make LotProcessStart format
	 * @author swcho
	 * @since 2015.03.11
	 * @param doc
	 * @param machineName
	 * @throws CustomException
	 */
	private void writeProcessStart(Document doc, String machineName)
		throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "LotProcessStarted");

		Element bodyElement = SMessageUtil.getBodyElement(doc);

		bodyElement.removeContent();
		
		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);
        bodyElement.addContent(machineNameElement);
        
		Element portElement = new Element("PORTNAME");
		portElement.setText("");
		bodyElement.addContent(portElement);

		Element lotElement = new Element("LOTNAME");
        lotElement.setText("");
        bodyElement.addContent(lotElement);
        
		Element carrierElement = new Element("CARRIERNAME");
		carrierElement.setText("");
		bodyElement.addContent(carrierElement);		

		Element recipeElement = new Element("MACHINERECIPENAME");
		recipeElement.setText("");
		bodyElement.addContent(recipeElement);
		
		Element unitNameElement = new Element("UNITNAME");
		unitNameElement.setText("");
        bodyElement.addContent(unitNameElement);
        
        /* 20180928, add,  Sorter Job Validation ==>> */
        Element sortJobNameElement = new Element("SORTJOBNAME");
        sortJobNameElement.setText("");
        bodyElement.addContent(sortJobNameElement);
        
        Element sortJobTypeElement = new Element("SORTJOBTYPE");
        sortJobTypeElement.setText("");
        bodyElement.addContent(sortJobTypeElement);
        
        Element sortJobDirectionElement = new Element("SORTTRANSFERDIRECTION");
        sortJobDirectionElement.setText("");
        bodyElement.addContent(sortJobDirectionElement);
        /* <<== 20180928, add,  Sorter Job Validation */
        
        Element productListElement = new Element("PRODUCTLIST");
        productListElement.setText("");
        bodyElement.addContent(productListElement);
	}

	/**
	 * write track in request.
	 * @author jhlee
	 * @since 2016.04.23
	 * @param doc
	 * @param lotName
	 * @param machineName
	 * @param portName
	 * @throws CustomException
	 */
	private Document writeTrackInRequest(Document doc, String lotName, String machineName, String portName)
			throws CustomException {
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "AutoTrackInLot");
		SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", "");
		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

		Element element1 = new Element("LOTNAME");
		element1.setText(lotName);
		eleBodyTemp.addContent(element1);

		Element element2 = new Element("MACHINENAME");
		element2.setText(machineName);
		eleBodyTemp.addContent(element2);

		Element element3 = new Element("PORTNAME");
		element3.setText(portName);
		eleBodyTemp.addContent(element3);

		// overwrite
		doc.getRootElement().addContent(eleBodyTemp);

		return doc;
	}
}