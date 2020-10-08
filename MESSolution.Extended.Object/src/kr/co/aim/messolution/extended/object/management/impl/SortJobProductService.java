package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class SortJobProductService extends CTORMService<SortJobProduct> {
	
	public static Log logger = LogFactory.getLog(SortJobProductService.class);
	
	private final String historyEntity = "SortJobProductHist";
	
	public List<SortJobProduct> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<SortJobProduct> result = super.select(condition, bindSet, SortJobProduct.class);
		
		return result;
	}
	
	public SortJobProduct selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(SortJobProduct.class, isLock, keySet);
	}
	
	public SortJobProduct create(EventInfo eventInfo, SortJobProduct dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, SortJobProduct dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public SortJobProduct modify(EventInfo eventInfo, SortJobProduct dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
	/*
	* Name : createProductInfoElement
	* Desc : This function is createProductInfoElement
	* Author : hykim
	* Date : 2014.11.26
	*/ 
	public Element createSortJobBodyElement(String machineName, String jobName) throws CustomException 
	{
		Element bodyElement = new Element("Body");
		
		//MACHINENAME
		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);   
		bodyElement.addContent(machineNameElement);
		
		//JOBNAME
		Element jobNameElement = new Element("JOBNAME");
		jobNameElement.setText(jobName);   
		bodyElement.addContent(jobNameElement);
		
		//FROMLOTLIST
		bodyElement.addContent(this.createSortJobFromLotInfoElement(jobName));
		
		return bodyElement;
	}	
	
	/*
	* Name : createProductInfoElement
	* Desc : This function is createProductInfoElement
	* Author : hykim
	* Date : 2014.11.26
	*/ 
	public Element createSortJobFromLotInfoElement(String jobName) throws CustomException 
	{
		List<SortJobCarrier> sortCarrierList = ExtendedObjectProxy.getSortJobCarrierService().select("JOBNAME = ?", new Object[]{jobName});
		
		Element fromLotListElement = new Element("FROMLOTLIST");
		for (SortJobCarrier fromLotM : sortCarrierList)  
		{
			String fromLotName = fromLotM.getLotName();
			String fromPortName = fromLotM.getPortName();
			String fromCarrierName = fromLotM.getCarrierName();
			
			
			Element fromLotElement = new Element("FROMLOT");
			
			//LOTNAME
			Element lotNameElement = new Element("LOTNAME");
			lotNameElement.setText(fromLotName);   
			fromLotElement.addContent(lotNameElement);
			
			//PORTNAME
			Element portNameElement = new Element("PORTNAME");
			portNameElement.setText(fromPortName);   
			fromLotElement.addContent(portNameElement);
			
			//CARRIERNAME
			Element carrierNameElement = new Element("CARRIERNAME");
			carrierNameElement.setText(fromCarrierName);   
			fromLotElement.addContent(carrierNameElement);
			
			//SORTERPRODUCTLIST
			fromLotElement.addContent(createSortJobProductInfoElement(jobName, fromCarrierName));
			
			fromLotListElement.addContent(fromLotElement);
		}
		
		return fromLotListElement;
	}
	
	/*
	* Name : createSortJobProductInfoElement
	* Desc : This function is createSortJobProductInfoElement
	* Author : hykim
	* Date : 2014.11.26
	*/ 
	public Element createSortJobProductInfoElement(String jobName, String CarrierName) throws CustomException 
	{
		List<SortJobProduct> sortJobProductList = ExtendedObjectProxy.getSortJobProductService().select("JOBNAME = ? AND FROMCARRIERNAME = ?", new Object[]{jobName, CarrierName});
		
		Element sorterProductListElement = new Element("SORTERPRODUCTLIST");
		for (SortJobProduct sortJobProductM : sortJobProductList)  
		{
			Element soterProductElement = new Element("SORTERPRODUCT");
			
			//PRODUCTNAME
			Element productNameElement = new Element("PRODUCTNAME");
			productNameElement.setText(sortJobProductM.getProductName());   
			soterProductElement.addContent(productNameElement);
			
			//FROMPOSITION
			Element fromPositionElement = new Element("FROMPOSITION");
			fromPositionElement.setText(sortJobProductM.getFromPosition());   
			soterProductElement.addContent(fromPositionElement);
			
			//TOPOSITION
			Element toPortNameElement = new Element("TOPORTNAME");
			toPortNameElement.setText(sortJobProductM.getToPortName());   
			soterProductElement.addContent(toPortNameElement);
			
			//TOCARRIERNAME
			Element toCarrierNameElement = new Element("TOCARRIERNAME");
			toCarrierNameElement.setText(sortJobProductM.getToCarrierName());   
			soterProductElement.addContent(toCarrierNameElement);
			
			//TOPOSITION
			Element toPositionElement = new Element("TOPOSITION");
			toPositionElement.setText(sortJobProductM.getToPosition());   
			soterProductElement.addContent(toPositionElement);
			
			//TURNFLAG
			Element turnFlagElement = new Element("TURNFLAG");
			//turnFlagElement.setText(sortJobProductM.getTurnFlag());   
			turnFlagElement.setText(sortJobProductM.getTurnSideFlag());   
			soterProductElement.addContent(turnFlagElement);
			
			//SCRAPFLAG
			Element scrapFlagElement = new Element("SCRAPFLAG");
			scrapFlagElement.setText(sortJobProductM.getScrapFlag());   
			soterProductElement.addContent(scrapFlagElement);
			
			sorterProductListElement.addContent(soterProductElement);
		}
		
		return sorterProductListElement;
	}	
}
