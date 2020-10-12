/**
 * Histories:
 * 

 */     


package kr.co.aim.messolution.product.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CorresSampleLot;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.ObjectAttributeDef;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author sjlee
 *
 */
public class ProductServiceUtil implements ApplicationContextAware 
{
	private static Log log = LogFactory.getLog(ProductServiceUtil.class);

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */

	@Override
    public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub

	}

	//add by wghuang 2018.05.27
	public List<ProductU> makeProductUSequence(List<Product> productList) throws CustomException
	{
		List<ProductU> productUSequence = new ArrayList<ProductU>();

		for (Product product : productList)
		{
			String productName = product.getKey().getProductName();
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			
			ProductU productP = new ProductU();
			productP.setProductName(productName);
			
			Map<String, String> productUdfs = productData.getUdfs();
			
			productP.setUdfs(productUdfs);

			productUSequence.add(productP);
			
		}
	
		return productUSequence;
	}	
	
	/**
	 * appending error handler
	 * @author swcho
	 * @since 2014.08.27
	 * @param productName
	 * @return
	 * @throws CustomException
	 */
	public  Product getProductData(String productName) throws CustomException
	{
		try
		{
			ProductKey productKey = new ProductKey();
			productKey.setProductName(productName);
			
			Product productData = ProductServiceProxy.getProductService().selectByKey(productKey);
	
			return productData;
		}
	    catch (NotFoundSignal e) 
	    {
	    	throw new CustomException("PRODUCT-9001", productName);	
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());	
	    }
	} 
	
	/*
	* Name : getProductByProductName
	* Desc : This function is getProductByProductName
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public Product getProductByProductName(String productName) throws CustomException
	{
		ProductKey productKey = new ProductKey();
		productKey.setProductName(productName);
		
		Product productData = null;
		productData = ProductServiceProxy.getProductService().selectByKey(productKey);
		
		return productData;
	}
	
	/*
	* Name : setNamedValueSimpleSequence
	* Desc : This function is setNamedValueSimpleSequence
	* Author : AIM Systems, Inc
	* Date : 2013.01.13
	*/
	public  Map<String, String> setNamedValueSimpleSequence(String productName, Element element) throws FrameworkErrorSignal, NotFoundSignal
	{
		if(log.isInfoEnabled()){
			log.info("productName = " + productName);
		}
		
		Map<String, String> namedValueMap = new HashMap<String, String>();
		
		ProductKey productKey = new ProductKey();
		productKey.setProductName(productName);
		try{
//			Product product = ProductServiceProxy.getProductService().selectByKey(productKey);
//			namedValueMap = product.getUserColumns();
		}catch(NotFoundSignal ne){
			
		}
					
		List<ObjectAttributeDef> objectAttributeDefs = greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames("Product", "ExtendedC");

		if ( objectAttributeDefs != null )
		{
			for (ObjectAttributeDef objectAttributeDef : objectAttributeDefs) {
				
				String name = "";
				String value = "";
				
				if ( element != null )
				{
					String attributeName = objectAttributeDef.getAttributeName();
					String childText = element.getChildText(attributeName);
					
					if(log.isDebugEnabled()){
						log.debug("AttributName : " + attributeName);
						log.debug("ElementText : " + childText);
					}
					
					if ( childText != null )
					{
						name  = attributeName;
						value = childText;
						break;
					}
					else
					{
						break;
					}
				}
				else
				{			
				}
				if ( !name.isEmpty() )
					namedValueMap.put(name, value);
			}
		}
		else
		{	
		}
		return namedValueMap;
	}
	
	/*
	* Name : getProductSpecByProductName
	* Desc : This function is getProductSpecByProductName
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public ProductSpec getProductSpecByProductName ( Product productData ) throws CustomException {
	
		ProductSpecKey productSpecKey = new ProductSpecKey();
		productSpecKey.setFactoryName(productData.getFactoryName());
		productSpecKey.setProductSpecName(productData.getProductSpecName());
		productSpecKey.setProductSpecVersion(productData.getProductSpecVersion());
		
		ProductSpec productSpecData = null;
		productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);
		
		return productSpecData;
	}
	
	/*
	* Name : checkProductState_InProduction
	* Desc : This function is checkProductState_InProduction
	* Author : AIM Systems, Inc
	* Date : 2011.05.27
	*/
	public void checkProductState_InProduction(Product productData) throws CustomException
	{
		String productName  = productData.getKey().getProductName(); 
		String productState = productData.getProductState();
		
		if(!StringUtils.equals(productState, GenericServiceProxy.getConstantMap().Prod_InProduction))
		{
			throw new CustomException("PRODUCT-9006", productName, productState);
		}
	}
	
	public List<ProductU> setProductUSequence(String productName)
			throws FrameworkErrorSignal, NotFoundSignal
	{
		ProductU productU = new ProductU();
		List<ProductU> productUList = new ArrayList<ProductU>();
		productU.setProductName(productName);
		productUList.add(productU);
		 
		return productUList;
	} 
	
	/*
	* Name : setNamedValueSequence
	* Desc : This function is setNamedValueSequence
	* Author : JHYEOM
	* Date : 2014.05.07
	*/
	public  Map<String, String> setNamedValueSequence(String productName, Element element) throws FrameworkErrorSignal, NotFoundSignal
	{ 
		if(log.isInfoEnabled()){
			log.info("productName = " + productName);
		}
		
		Map<String, String> namedValueMap = new HashMap<String, String>();
		ProductKey productKey = new ProductKey();
		productKey.setProductName(productName);
		
		try{
			Product product = ProductServiceProxy.getProductService().selectByKey(productKey);
			namedValueMap = product.getUdfs();
		}catch(NotFoundSignal ne){}
		
		List<ObjectAttributeDef> objectAttributeDefs = greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames("Product", "ExtendedC");

		if ( objectAttributeDefs != null )
		{
			for ( int i = 0; i < objectAttributeDefs.size(); i++ )
			{				
				String name = "";
				String value = "";
				
				if ( element != null )
				{
					if ( element.getChildText(objectAttributeDefs.get(i).getAttributeName()) != null )
					{
						name  = objectAttributeDefs.get(i).getAttributeName();
						value = element.getChildText(objectAttributeDefs.get(i).getAttributeName());

						if (StringUtil.isNotEmpty(name) && StringUtil.isNotEmpty(value))
							namedValueMap.put(name, value);
					}
				}
			}
		}

		return namedValueMap;
	}
	
	/*
	* Name : generatePanelName
	* Desc : This function is generatePanelName
	* Author : jhyeom
	* Date : 2015.02.05
	*/
	public List<String> generatePanelName(String productName, int x, int y)
	{
		int valueX = x/2;
		int valueY = y/2;
		
		List<String> panelNames = new ArrayList<String>(valueX*valueY);
		
		char[] cs = "A".toCharArray();
		
		for (int i = 0; i < valueY; i++)
		{
			char[] cs1 = "A".toCharArray();
			StringBuilder panelName = new StringBuilder();
			
			if(cs[0] == 'I' || cs[0] == 'O')
			{
				cs[0] += 1;
			}
			
			for (int j = 0; j < valueX; j++)
			{
				
				panelName.setLength(0);
				
				panelName.append(productName);
				
				
				panelName.append(cs);
				
				if(cs1[0] == 'I' || cs1[0] == 'O')
				{
					cs1[0] += 1;
				}
				
				panelName.append(cs1);
				
				panelNames.add(panelName.toString());
				cs1[0] += 1;
			}
			
			cs[0] += 1;
		}
		
		return panelNames;
	}
	
	/*
	* Name : generateGlassName
	* Desc : This function is generateGlassName
	* Author : jhyeom
	* Date : 2014.04.29
	*/ 
	public List<String> generateGlassName(String productName, int subProductUnitQty)
	{
		List<String> panelNames = new ArrayList<String>(subProductUnitQty);
		
		//char[] cs = "A".toCharArray();
		
		for (int i = 0; i < subProductUnitQty; i++)
		{
			/*char[] cs1 = "A".toCharArray();
			StringBuilder panelName = new StringBuilder();

			panelName.setLength(0);
				
			panelName.append(productName);
			panelName.append(cs);
			if(i < 9)
			{ 
				panelName.append(i+1);
			}
			else
			{
				panelName.append(cs1);
				cs1[0] += 1;
				if(cs1[0] == 'I' || cs1[0] == 'O')
				cs1[0] += 1;
			}
				
			panelNames.add(panelName.toString());

			cs[0] += 1;*/
			
			panelNames.add(new StringBuilder(productName).append(String.valueOf(i+1)).toString());
		}
		
		return panelNames;
	}
	
	/*
	* Name : generateProductName
	* Desc : This function is generateProductName
	* Author : AIM Systems, Inc
	* Date : 2014.10.16
	*/
	public List<String> generateProductName( String ruleName, String prefix , double quantity ) throws CustomException
	{
		List<String> argSeq = new ArrayList<String>(); 
		argSeq.add(prefix); 

		List<String> names = new ArrayList<String>();
		names = NameServiceProxy.getNameGeneratorRuleDefService().generateName(ruleName, argSeq, (long) quantity);
		
		return names;		
			
	}
	
	/**
	 * logical slot map in carrier
	 * 160426 by swcho : only by CST
	 * @since 2015.08.27
	 * @author swcho
	 * @param durableData
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public String getSlotMapInfo(Durable durableData)
		throws CustomException
	{
		StringBuffer normalSlotInfoBuffer = new StringBuffer();
		
		// Get Durable's Capacity
		long iCapacity = durableData.getCapacity(); 
		
		// Get Product's Slot , These are not Scrapped Product.
		List<Product> productList = new ArrayList<Product>();
		
		try
		{
			productList = ProductServiceProxy.getProductService().select("carrierName = ? AND productState = ?",
							new Object[] {durableData.getKey().getDurableName(), GenericServiceProxy.getConstantMap().Prod_InProduction});
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "Product", "No avaliable Product");
		}
		
		// Make Durable Normal SlotMapInfo
		for(int i = 0; i < iCapacity; i++)
		{
			normalSlotInfoBuffer.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
		}
		
		log.debug("Default Slot Map : " + normalSlotInfoBuffer);
		
		for(int i = 0; i < productList.size(); i++)
		{
			try
			{
				int index = (int)productList.get(i).getPosition() - 1;
				
				normalSlotInfoBuffer.replace(index, index+1, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
			}
			catch (Exception ex)
			{
				log.error("Position conversion failed");
				normalSlotInfoBuffer.replace(i, i+1, GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
			}
		}
		
		log.info("Current Slot Map : " + normalSlotInfoBuffer);
		
		return normalSlotInfoBuffer.toString();
	}
	
	/**
     * logical slot map in carrier
     * 160426 by swcho : only by CST
     * @since 2015.08.27
     * @author swcho
     * @param durableData
     * @param lotData
     * @return
     * @throws CustomException
     */
    public String getSlotMapInfo(Durable durableData, boolean isNotIncludeSGrade)
        throws CustomException
    {
        StringBuffer normalSlotInfoBuffer = new StringBuffer();
        
        // Get Durable's Capacity
        long iCapacity = durableData.getCapacity(); 
        
        // Get Product's Slot , These are not Scrapped Product.
        List<Product> productList = new ArrayList<Product>();
        
        try
        {
            productList = ProductServiceProxy.getProductService().select("carrierName = ? AND productState = ?",
                            new Object[] {durableData.getKey().getDurableName(), GenericServiceProxy.getConstantMap().Prod_InProduction});
        }
        catch (Exception ex)
        {
            throw new CustomException("SYS-9999", "Product", "No avaliable Product");
        }
        
        // Make Durable Normal SlotMapInfo
        for(int i = 0; i < iCapacity; i++)
        {
            normalSlotInfoBuffer.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
        }
        
        log.debug("Default Slot Map : " + normalSlotInfoBuffer);
        
        for(int i = 0; i < productList.size(); i++)
        {
            try
            {
                int index = (int)productList.get(i).getPosition() - 1;
                
                normalSlotInfoBuffer.replace(index, index+1, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
                
                if(isNotIncludeSGrade && StringUtil.equals(productList.get(i).getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_S))
                {
                    normalSlotInfoBuffer.replace(index, index+1, GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
                }
            }
            catch (Exception ex)
            {
                log.error("Position conversion failed");
                normalSlotInfoBuffer.replace(i, i+1, GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
            }
        }
        
        log.info("Current Slot Map : " + normalSlotInfoBuffer);
        
        return normalSlotInfoBuffer.toString();
    }
	/**
	 * logical slot map in carrier
	 * 160426 by swcho : only by CST
	 * @since 2015.08.27
	 * @author swcho
	 * @param durableData
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public String getSlotMap(Durable durableData)
		throws CustomException
	{
		StringBuffer normalSlotInfoBuffer = new StringBuffer();
		
		// Get Durable's Capacity
		long iCapacity = durableData.getCapacity(); 
		
		// Get Product's Slot , These are not Scrapped Product.
		List<Product> productList = new ArrayList<Product>();
		
		try
		{
			productList = ProductServiceProxy.getProductService().select("carrierName = ? AND productState = ?",
							new Object[] {durableData.getKey().getDurableName(), GenericServiceProxy.getConstantMap().Prod_InProduction});
		}
		catch (Exception ex)
		{
			log.debug("CST Product List is Empty : " + durableData.getKey().getDurableName());
		}
		
		// Make Durable Normal SlotMapInfo
		for(int i = 0; i < iCapacity; i++)
		{
			normalSlotInfoBuffer.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
		}
		
		log.debug("Default Slot Map : " + normalSlotInfoBuffer);
		
		for(int i = 0; i < productList.size(); i++)
		{
			try
			{
				int index = (int)productList.get(i).getPosition() - 1;
				
				normalSlotInfoBuffer.replace(index, index+1, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
			}
			catch (Exception ex)
			{
				log.error("Position conversion failed");
				normalSlotInfoBuffer.replace(i, i+1, GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
			}
		}
		
		log.info("Current Slot Map : " + normalSlotInfoBuffer);
		
		return normalSlotInfoBuffer.toString();
	}
	
	/**
	 * @since 2018.07.13
	 * @author smkang
	 * @param lotName
	 * @return containsTrackFlag
	 * @see Check TrackFlag of at least one product is Y, or not.
	 */
	public boolean containsTrackFlag(String lotName) {
		String condition = "WHERE LOTNAME = ? AND TRACKFLAG = ?";
		Object[] bindSet = new Object[] { lotName, "Y" };
		
		try {
			List<Product> productDataList = ProductServiceProxy.getProductService().select(condition, bindSet);

			return (productDataList != null && productDataList.size() > 0);
		} catch (Exception e) {
			return false;
		}
	}
		
	/**
	 * 
	 * @Name     setProdutProcessFlag
	 * @since    2018. 8. 7.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param eventInfo
	 * @param lotList
	 * @param logicalSlotMap
	 * @throws CustomException
	 */
	public void setProdutProcessFlag(EventInfo eventInfo, Lot lotData, String logicalSlotMap, boolean noteFlag)
	        throws CustomException
	{
		// 2019.05.29_hsryu_Add Condition. if LotState is Completed, Not executed Logic. 
		if(!StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed)){
			//2019.01.28_hsryu_New EventInfo For ProcessFlag.
	 		EventInfo eventInfoForProcessFlag = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), 
					"", null, null);
			
			eventInfoForProcessFlag.setEventTime(eventInfo.getEventTime());
			eventInfoForProcessFlag.setEventTimeKey(eventInfo.getEventTimeKey());

	        String samplingFlag = StringUtil.EMPTY;

	        try
	        {	       
	            //avail Product list
	            List<Product> productList = null;
	            try
	            {
	            	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//	                productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
	                productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
	            }
	            catch (Exception ex)
	            {
	                //throw new CustomException("SYS-9999", "Product", "No Product to process");
	                log.error("[SYS-9999] No Product to process");   
	            }

	            //base flow info
	            ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

	            //waiting step
	            ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());

	            String actualSamplePosition = StringUtil.EMPTY;
	            /* 20190312, hhlee, add Check Validation */
	            //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
	            /* 20190522, hhlee, modify, add check validation SamplingFlow */
	            //if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
	            //        !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
	            //        && !flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
	            if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
	                    flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
	                    !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
	            {
	                /* 20181108, hhlee, modify, add ActualSamplePositionUpdate Parameter ==>> */
	                //actualSamplePosition = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(eventInfo, lotData, logicalSlotMap);
	                actualSamplePosition = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(eventInfoForProcessFlag, lotData, logicalSlotMap, true);
	                /* <<== 20181108, hhlee, modify, add ActualSamplePositionUpdate Parameter */
	            } 
	            
	            for (Product productData : productList)
	            {	                
	                samplingFlag = StringUtil.EMPTY;
	                
	                //Glass selection
	                //161228 by swcho : additional sampling
	                /* 20190312, hhlee, add Check Validation */
	                //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
	                /* 20190522, hhlee, modify, add check validation SamplingFlow */
	                //if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
	                //        !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
	                //        && !flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
	                if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
	                        flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
	                        !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
	                {
	                    /* 20180602, Need Sampling Logic ==>> */
	                    /* If 'ProcessUperationType' is 'Inspection' then additional Sampling Logic is required.
	                    Set SlotSel to " Y " for the entire operation of Glass. */
	                    /* 20190522, hhlee, modify, default = 'N' */
	                    //samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
	                    samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
	                    
	                    if(!StringUtil.isEmpty(actualSamplePosition))
	                    {
	                        /* 20190522, hhlee, modify, default = 'N' */
	                        //samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
	                        
	                        String[] actualsamplepostion = actualSamplePosition.trim().split(",");
	                        for(int i = 0; i < actualsamplepostion.length; i++ )
	                        {
	                            if(productData.getPosition() == Long.parseLong(actualsamplepostion[i]))
	                            {
	                                samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
	                                break;
	                            }
	                        }     
	                    }
	                    /* <<== 20180602, Need Sampling Logic */
	                }
	                else
	                {
	                    //repair case
	                    if (StringUtil.equals(operationData.getDetailProcessOperationType(), 
	                            GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
	                    {
	                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_P))
	                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
	                        else
	                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
	                    }
	                    //rework case
	                    else if (StringUtil.equals(flowData.getProcessFlowType(), 
	                            GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_REWORK))
	                    {
	                        /* 20190515, hhlee, add, Rework (SlotSel)Logic  */
	                        samplingFlag = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProductForBranchRework(productData, 
	                                GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_REWORK);                         
	                        //if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_R))
	                        //    samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
	                        //else
	                        //    samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
	                        /* <<== 20190515, hhlee, add, Rework (SlotSel)Logic  */
	                    }
	                    //MQC SlotMap
	                    else if (StringUtil.equals(flowData.getProcessFlowType(), "MQC"))
	                    {
	                        /* 20190117, hhlee, modify, MQC Positon validation */
	                        /* 20181020, hhlee, modify, MQC RecipeName valiable ==>> */
	                        //mqcProductRecipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
	                        //productRecipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);                        
	                        /* 20190117, hhlee, modify, MQC Positon */
	                        List<Map<String, Object>> slotPostionInfoList = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProduct(productData);
	                        String slotPostion = StringUtil.EMPTY;
	                        if(slotPostionInfoList != null)
	                        {
	                            /* 20190124, hhlee, modify , add logic null value check */
	                            //slotPostion = slotPostionInfoList.get(0).get("POSITION").toString();
	                            slotPostion = ( slotPostionInfoList.get(0).get("POSITION") != null ? 
	                                    slotPostionInfoList.get(0).get("POSITION").toString() : StringUtil.EMPTY);                                                
	                        }
	                        if(StringUtil.isEmpty(slotPostion) || 
	                                productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
	                        {
	                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
	                        }
	                        else
	                        {
	                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
	                        }                        
	                        /* <<== 20181020, hhlee, modify, MQC RecipeName valiable */
	                    }
	                    /* 20190513, hhlee, add, Branch (SlotSel)Logic  */
	                    else if (StringUtil.equals(flowData.getProcessFlowType(), 
	                            GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_BRANCH))
	                    {
	                        /* 20190515, hhlee, add, Branch (SlotSel)Logic  */
	                        samplingFlag = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProductForBranchRework(productData, 
	                                GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_BRANCH);   
	                    }
	                    else
	                    {
	                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
	                        {
	                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
	                        }
	                        else
	                        {
	                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
	                        }	                        
	                    }                    
	                }
	                
	                if(StringUtil.isNotEmpty(samplingFlag))
	                {                       
	                    Map<String, String> productUdfs = productData.getUdfs();
	                    productUdfs.put("PROCESSFLAG", samplingFlag);
	                    ProductServiceProxy.getProductService().update(productData);
	                    /* 20190129, hhlee, modify */
	                    //String pCondition = " where productname=?" + " and timekey= ? " ;
	                    //Object[] pBindSet = new Object[]{productData.getKey().getProductName(), productData.getLastEventTimeKey()};
//	                    String pCondition = " where productname= ? and timekey= (select max(timekey) from producthistory where productname= ? ) " ;
//	                    Object[] pBindSet = new Object[]{productData.getKey().getProductName(), productData.getKey().getProductName()};
//	                    List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
//	                    ProductHistory producthistory = pArrayList.get(0);
	                    ProductHistoryKey productHistoryKey = new ProductHistoryKey();
	    	            productHistoryKey.setProductName(productData.getKey().getProductName());
	    	            productHistoryKey.setTimeKey(productData.getLastEventTimeKey());

	    	            // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//	    	            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
	    	            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
	    	            
	                    Map<String, String> productHistUdfs = producthistory.getUdfs();
	                    productHistUdfs.put("PROCESSFLAG", samplingFlag);
	                    ProductServiceProxy.getProductHistoryService().update(producthistory);                        
	                }
	            }

	            //2019.01.28_hsryu_Insert NoteFlag. if event except TrackOut, don't have to leave note.
	            if(noteFlag)
	            {
	                /* 20190312, hhlee, add Check Validation */
	                //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
	                /* 20190523, hhlee, modify, add check validation SamplingFlow */
	                //if (operationData.getProcessOperationType().equals("Inspection") && 
	                //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
	                //            && !flowData.getProcessFlowType().equals("MQC"))
	                if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
	                        flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
	                        !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))              
	            	{
	            		//2019.03.06_hsryu_First Operation in Sampling, memory Note. 
	            		if(!CommonValidation.checkFirstOperation(lotData,"SAMPLING"))
	            		{
	            			this.setNoteSampleInfo(productList, actualSamplePosition, lotData, eventInfoForProcessFlag, null, null, null);
	            		}
	            	}
	            }
	        }
	        catch (Exception ex)
	        {
	            log.warn("Set Produt ProcessFlag update failed");        
		    }
		}
    }
	
	public void setNoteSampleInfo(List<Product> productList, String samplePosition, Lot lotData, EventInfo eventInfo, SampleLot sampleLotData, String oldManualPosition, CorresSampleLot corressSampleLotData)
	{
		ArrayList<String> sampleProductName = new ArrayList<String>();
		String[] arrRealPosition = StringUtil.split(samplePosition,",");
		
		// 2019.07.29 Park Jeong Su Mantis 4289
		String strSlotAndProductName = StringUtils.EMPTY;
        String tempNodeStack = lotData.getNodeStack();
        String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
        int count = arrNodeStack.length;
		Map<String, String> nodeInfo = CommonUtil.getNodeInfo(arrNodeStack[count-2]);
		
		try {
			
			String condition ="LOTNAME = ? AND FACTORYNAME = ?  AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION =? AND PROCESSOPERATIONNAME =?  AND PROCESSOPERATIONVERSION =? AND ACTIONNAME = ? " ;
			Object[] bindSet = new Object[]{lotData.getKey().getLotName(),lotData.getFactoryName(),(String)nodeInfo.get("PROCESSFLOWNAME"),(String)nodeInfo.get("PROCESSFLOWVERSION"),(String)nodeInfo.get("PROCESSOPERATIONNAME"),(String)nodeInfo.get("PROCESSOPERATIONVERSION"),GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING};

			List<LotAction> lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
			
			if(lotActionList!=null && lotActionList.size()>0){
				strSlotAndProductName+="ReasonCode[ "+ (String)lotActionList.get(0).getReasonCode() + "] Department[ " + (String)lotActionList.get(0).getSampleDepartmentName()+" ]";
			}
		} catch (Exception e) {
			log.info("lotActionList No Data");
		}
		if(StringUtils.isEmpty(strSlotAndProductName)){
			try {
				String condition ="LOTNAME = ? AND FACTORYNAME = ?  AND PRODUCTSPECNAME = ? AND ECCODE = ? AND PROCESSFLOWNAME = ?  AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ?  AND PROCESSOPERATIONVERSION = ? " ;
				condition +="AND SAMPLEPROCESSFLOWNAME = ? AND FROMPROCESSOPERATIONNAME = ? ";
				Object[] bindSet = new Object[]{lotData.getKey().getLotName(),lotData.getFactoryName(),lotData.getProductSpecName(),(String)lotData.getUdfs().get("ECCODE"),(String)nodeInfo.get("PROCESSFLOWNAME"),(String)nodeInfo.get("PROCESSFLOWVERSION"),(String)nodeInfo.get("PROCESSOPERATIONNAME"),(String)nodeInfo.get("PROCESSOPERATIONVERSION"),lotData.getProcessFlowName(),(String)nodeInfo.get("PROCESSOPERATIONNAME")};

				List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
				
				if(sampleLotList!=null && sampleLotList.size()>0){
					strSlotAndProductName+="ReasonCode[ "+ (String)sampleLotList.get(0).getReasonCode() + "] Department[ " + (String)sampleLotList.get(0).getSampleDepartmentName()+" ]";
				}
			} catch (Exception e) {
				log.info("sampleLotList No Data");
			}
		}
		// 2019.07.29 Park Jeong Su Mantis 4289
		
		strSlotAndProductName += " // SamplingSlot = ";
				
		if(productList.size()>0)
		{
			for(int i=0; i<arrRealPosition.length;i++)
			{
				for(int j=0; j<=productList.size(); j++)
				{
					Product product = productList.get(j);
					
					if(Integer.parseInt(arrRealPosition[i])==(product.getPosition()))
					{
						sampleProductName.add(product.getKey().getProductName());
						break;
					}
				}
			}
		}
		
		//make text for lotNote.. ex) SamplingSlot = [3,productName1][4,productName2]
		for(int i=0; i<arrRealPosition.length;i++)
		{	
			strSlotAndProductName += "[" + arrRealPosition[i] + "," + sampleProductName.get(i).toString() + "]";
		}
		
		//2019.09.03 dmlee : Record Sampling Info (Mantis 4696)
		if(sampleLotData != null)
		{
			strSlotAndProductName += " // ReserveSamplingBy:{"+eventInfo.getEventUser()+"} ReserveSlot:{"+oldManualPosition+"} && SystemSlot:{"+sampleLotData.getSystemSamplePosition()+"} && ForceSampling{"+sampleLotData.getManualSamplePosition()+"} = ActualSlot{"+sampleLotData.getActualSamplePosition()+"}";
		}
		else if(corressSampleLotData != null)
		{
			strSlotAndProductName += " // ReserveSamplingBy:{"+eventInfo.getEventUser()+"} ReserveSlot:{"+oldManualPosition+"} && SystemSlot:{"+corressSampleLotData.getSystemSamplePosition()+"} && ForceSampling{"+corressSampleLotData.getManualSamplePosition()+"} = ActualSlot{"+corressSampleLotData.getActualSamplePosition()+"}";
		}

		
		//2019.01.25_hsryu_Mantis 0002598.
		if(StringUtils.isNotEmpty(eventInfo.getEventComment()))
		{
			strSlotAndProductName += " & Sampling Desc :" + eventInfo.getEventComment();
		}

		//for LotNote..
		//2019.02.20_hsryu_Delete Logic.
		Map<String, String> udfs = lotData.getUdfs();
		udfs.put("NOTE", strSlotAndProductName);

		LotServiceProxy.getLotService().update(lotData);

		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		String cond = "where lotname=?" + " and timekey= ? " ;
//		Object[] bind = new Object[]{lotData.getKey().getLotName(),lotData.getLastEventTimeKey()};
//		List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(cond, bind);
//		LotHistory lotHistory = arrayList.get(0);
		LotHistoryKey lotHistoryKey = new LotHistoryKey();
		lotHistoryKey.setLotName(lotData.getKey().getLotName());
		lotHistoryKey.setTimeKey(lotData.getLastEventTimeKey());
		LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
		
		Map<String, String> hUdfs = lotHistory.getUdfs();
		hUdfs.put("NOTE", strSlotAndProductName);
		LotServiceProxy.getLotHistoryService().update(lotHistory);
	}
		
	/**
	 * 
	 * @Name     getProductSlotSel
	 * @since    2018. 11. 7.
	 * @author   hhlee
	 * @contents Get Product Slot Selection
	 *           
	 * @param lotData
	 * @param durableData
	 * @param actualSamplePosition
	 * @return
	 * @throws CustomException
	 */
	public String getProductSlotSelection(Lot lotData, Durable durableData, String actualSamplePosition) throws CustomException
    {
        String samplingFlag = StringUtil.EMPTY;
        StringBuffer slotMapTemp = new StringBuffer();    
        int position = 0;
        try
        {   
            /* 1. Default SlotMap Setting */
            for (long i=0; i<durableData.getCapacity(); i++)
            {
                slotMapTemp.append(GenericServiceProxy.getConstantMap().E_PRODUCT_NOT_IN_SLOT);
            }
            
            /* 2. Get Product List */
            List<Product> productList = null;
            productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
            
            /* 3. Get Flow Data */
            ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

            /* 4. Get OperationSpec Data */
            ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
            
            /* 5. Setting SlotSel */
            for (Product productData : productList)
            {                   
                /* 5.1. Check Sampling */
                samplingFlag = StringUtil.EMPTY;
                /* 20190312, hhlee, add Check Validation */
                //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
                /* 20190523, hhlee, modify, add check validation SamplingFlow */
                //if (operationData.getProcessOperationType().equals("Inspection") && 
                //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
                //            && !flowData.getProcessFlowType().equals("MQC"))
                if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
                        flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
                        !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))  
                {
                    /* 20190523, hhlee, modify, default = 'N' */
                    //samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
                    samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
                    
                    if(!StringUtil.isEmpty(actualSamplePosition))
                    {
                        /* 20190523, hhlee, modify, default = 'N' */
                        //samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;     
                        
                        String[] actualsamplepostion = actualSamplePosition.trim().split(",");
                        for(int i = 0; i < actualsamplepostion.length; i++ )
                        {
                            if(productData.getPosition() == Long.parseLong(actualsamplepostion[i]))
                            {
                                samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
                                break;
                            }
                        }     
                    }
                }
                else
                {
                    /* 5.2. Check Repair */
                    //if (StringUtil.equals(operationData.getDetailProcessOperationType(), "REP"))
                    if (flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) && 
                            StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
                    {
                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_P))
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                        }
                        else
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
                        }
                    }
                    /* 5.3. Check Rework */
                    else if (StringUtil.equals(flowData.getProcessFlowType(), "Rework"))
                    {
                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_R))
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                        }
                        else
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
                        }
                    }
                    /* 5.4. Check MQC */
                    //MODIFY BYjhiying ON2019.9.17 MANTIS:4802 ADD ProductionType(), "MQCA"
                    //else if (StringUtil.equals(flowData.getProcessFlowType(), "MQC"))
                    else if (StringUtil.equals(flowData.getProcessFlowType(), "MQC") || StringUtil.equals(lotData.getProductionType(), "MQCA"))
                    {
                        /* 20190117, hhlee, modify, MQC Positon validation */
                        /* 20181020, hhlee, modify, MQC RecipeName valiable ==>> */
                        //mqcProductRecipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
                        //productRecipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);                        
                        /* 20190117, hhlee, modify, MQC Positon */
                    	//add BY GJJ 20200410 start mantis:6005
                    	List<Map<String, Object>> slotPostionInfoList= new  ArrayList<Map<String,Object>>();
                    	if (StringUtil.equals(flowData.getProcessFlowType(), "MQC")) {
                            slotPostionInfoList = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProduct(productData);
						} else {
							slotPostionInfoList = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProductForBranch(productData);
						}
                        //List<Map<String, Object>> slotPostionInfoList = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProduct(productData);
                    	//add BY GJJ 20200410 end mantis:6005
                    	
                    	String slotPostion = StringUtil.EMPTY;
                        if(slotPostionInfoList != null)
                        {
                            /* 20190124, hhlee, modify , add logic null value check */
                            //slotPostion = slotPostionInfoList.get(0).get("POSITION").toString();
                            slotPostion = ( slotPostionInfoList.get(0).get("POSITION") != null ? 
                                    slotPostionInfoList.get(0).get("POSITION").toString() : StringUtil.EMPTY);                                                
                        }
                        if(StringUtil.isEmpty(slotPostion) || 
                                productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
                        }
                        else
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                        }                        
                        /* <<== 20181020, hhlee, modify, MQC RecipeName valiable */
                    }
                    /* 5.5. Others */
                    else
                    {
                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
                        }
                        else
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                        }                           
                    }                    
                }
                
                /* 5.6. Set SlotSel Position */
                position = (int)productData.getPosition();
                if (samplingFlag.equalsIgnoreCase(GenericServiceProxy.getConstantMap().Flag_Y))
                {
                    slotMapTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().E_PRODUCT_IN_SLOT);
                }
                else
                {
                    slotMapTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().E_PRODUCT_NOT_IN_SLOT);
                }
                
                /* 20190315, hhlee, Debug Log write */
                log.info(String.format("SLOTSEL - SampleFlag=%s, [ProcessFlowType=%s, ProcessOperationType=%s, DetailProcessOperationType=%s, ProductGrade=%s]", 
                        samplingFlag, flowData.getProcessFlowType(), operationData.getProcessOperationType(), 
                        operationData.getDetailProcessOperationType(), productData.getProductGrade()));
            }
        }
        catch (Exception ex)
        {
            log.warn("GETPRODUCTSLOTSEL : " + ex.getLocalizedMessage() + " , " + ex.getStackTrace());        
        }
        
        log.debug("ProductSlotSel : " + slotMapTemp.toString());
        
        return slotMapTemp.toString();
    }

	/**
	 * 
	 * @Name     validateProductElement
	 * @since    2018. 12. 11.
	 * @author   hhlee
	 * @contents Validate Product Element
	 *           
	 * @param productListElement
	 * @return
	 * @throws CustomException
	 */
	public String validateProductElement(List<Element> productListElement)  throws CustomException
	{	    
	    String notValidateProduct = StringUtil.EMPTY;
        String elementProductName = StringUtil.EMPTY;       
        
        try
        {
            for (Element productElement : productListElement )
            {
                elementProductName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", false);
                
                try
                {
                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(elementProductName);                    
                }            
                catch (Exception ex)
                {
                    log.warn("[01] Not Exist Product : [" + elementProductName +  "] " + ex.getLocalizedMessage() + " , " + ex.getStackTrace());
                    if(StringUtil.isEmpty(notValidateProduct))
                    {
                        notValidateProduct =  elementProductName; 
                    }
                    else
                    {
                        notValidateProduct =  notValidateProduct +  "," + elementProductName;
                    }
                }
            }    
        }  
        catch (Exception ex)
        {
            log.warn("[00] Not Exist Product : " + ex.getLocalizedMessage() + " , " + ex.getStackTrace());            
        }
        
        return notValidateProduct;
	    
//	    String notValidateProduct = StringUtil.EMPTY;
//	    String elementProductName = StringUtil.EMPTY;
//	    
//	    try
//	    {
//    	    for (Element productElement : productListElement )
//            {
//                elementProductName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", false);
//                
//                ProductKey productKey = new ProductKey();
//                productKey.setProductName(elementProductName);
//                
//                Product productData = ProductServiceProxy.getProductService().selectByKey(productKey);
//                if(productData == null)
//                {
//                    notValidateProduct =  notValidateProduct + elementProductName + ",";
//                }
//            }
//	    }
//	    catch (Exception ex)
//        {
//            log.warn("Not Exist Product : [" + elementProductName +  "] " + ex.getLocalizedMessage() + " , " + ex.getStackTrace());
//            notValidateProduct =  notValidateProduct + elementProductName + ",";
//        }
//	    
//	    notValidateProduct =  StringUtil.substring(notValidateProduct, 1, notValidateProduct.length() -1);
//	    
//	    return notValidateProduct;
	}
	
	/**
	 * 
	 * @Name     getActualSlotByProductProcessFlag
	 * @since    2018. 12. 20.
	 * @author   hhlee
	 * @contents Get Work Actual Slot by Product ProcessFlag 
	 *           
	 * @param lotName
	 * @return
	 * @throws CustomException
	 */
	public String getActualSlotByProductProcessFlag(String lotName)  throws CustomException
	{ 
	    String actualSlot = StringUtil.EMPTY;
	    String strSql = StringUtil.EMPTY;
	    try
        {
	        strSql = " SELECT SQ.LOTNAME AS LOTNAME,                                                          \n" 
	               + "        SUBSTR(MAX(SYS_CONNECT_BY_PATH(SQ.POSITION, ',')), 2) AS ACTUALWORKSLOT         \n"
	               + "   FROM (                                                                               \n"
	               + "         SELECT P.LOTNAME,                                                              \n"
	               + "                P.POSITION,                                                             \n"
	               + "                ROW_NUMBER () OVER (PARTITION BY P.LOTNAME ORDER BY P.POSITION) AS RNUM \n"
	               + "           FROM PRODUCT P                                                               \n"
	               + "          WHERE 1=1                                                                     \n"
	               + "            AND P.LOTNAME = :LOTNAME                                                    \n"
	               + "            AND P.PROCESSFLAG = :PROCESSFLAG                                            \n"
	               + "          ORDER BY P.POSITION                                                           \n"
	               + "        ) SQ                                                                            \n"
	               + "  WHERE 1=1                                                                             \n"
	               + "  START WITH SQ.RNUM = 1                                                                \n"
	               + " CONNECT BY PRIOR SQ.RNUM = SQ.RNUM - 1                                                 \n"
	               + "  GROUP BY SQ.LOTNAME                                                                     ";  
	        
	        Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("LOTNAME", lotName);
            bindMap.put("PROCESSFLAG", GenericServiceProxy.getConstantMap().E_PRODUCT_IN_SLOT);
           
            List<Map<String, Object>> actualSlotData = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);
            
            if ( actualSlotData != null && actualSlotData.size() > 0 )
            {
                actualSlot = actualSlotData.get(0).get("ACTUALWORKSLOT").toString();
            }
        }
	    catch (Exception ex)
        {
            log.warn("[getActualSlotByProductProcessFlag] Data Query Failed");;
        }
	    	    
	    return actualSlot;
	}	
	
	
	//2019.01.16_hsryu
	public List<Product> getProductListByDurable(String durableName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		
		// Get Product's Slot , These are not Scrapped Product.
		List<Product> productList = new ArrayList<Product>();
		
		try
		{
			productList = ProductServiceProxy.getProductService().select("carrierName = ? AND productState <> ?",
							new Object[] {durableData.getKey().getDurableName(), GenericServiceProxy.getConstantMap().Prod_Scrapped});
		}
		catch (Exception ex)
		{
			productList = null;
			log.info("Product is not exist in " + durableName);
		}
		
		return productList;
	}
	/**
	 * 
	 * @Name	getProductListByLotNameForUpdate
	 * @since    2019. 09. 09.
	 * @author	jspark
	 * @contents            
	 * @param	lotName
	 * @return	List<Product>
	 */
	public List<Product> getProductListByLotNameForUpdate(String lotName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		List<Product> productList = new ArrayList<Product>();
		
		try
		{
			productList = ProductServiceProxy.getProductService().select(" lotName = ? ", new Object[] {lotName});
			for(Product product : productList){
				ProductServiceProxy.getProductService().selectByKeyForUpdate(product.getKey());
			}
		}
		catch (Exception ex)
		{
			productList = null;
			log.info("Product is not exist in " + lotName);
		}
		
		return productList;
	}

	/**
	 * 
	 * @Name     getSubUnitNameListByProductElement
	 * @since    2019. 2. 19.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param productElement
	 * @return
	 */
	public String getSubUnitNameListByProductElement(Element productElement)
	{
	    String subUnitNameList = StringUtil.EMPTY;
	    
	    String productName = StringUtil.EMPTY;
        String processedUnitName = StringUtil.EMPTY;
        String processedSubUnitName = StringUtil.EMPTY;
        String lotName = StringUtil.EMPTY;
        
        List<Element> processedUnitElement = null;
        List<Element> processedSubUnitElement = null;
	    
	    try
        {
	        productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", false);
            lotName =  SMessageUtil.getChildText(productElement, "LOTNAME", false);
            
	        processedUnitElement = SMessageUtil.getSubSequenceItemList(productElement, "PROCESSEDUNITLIST", false);
	        
	        if(processedUnitElement != null && processedUnitElement.size() > 0)
	        {
	            for (Element processUnit : processedUnitElement )
                {
	                processedUnitName = SMessageUtil.getChildText(processUnit, "PROCESSEDUNITNAME", false);
	                processedSubUnitElement = SMessageUtil.getSubSequenceItemList(processUnit, "PROCESSEDSUBUNITLIST", false);
                    
	                if(processedSubUnitElement != null && processedSubUnitElement.size() > 0)
                    {
                        for (Element processSubUnit : processedSubUnitElement )
                        {
                            processedSubUnitName = SMessageUtil.getChildText(processSubUnit, "PROCESSEDSUBUNITNAME", false);
                            if(StringUtil.isNotEmpty(processedUnitName) && StringUtil.isNotEmpty(processedSubUnitName))
                            {
                                subUnitNameList += processedUnitName + "=" + processedSubUnitName + "|";
                            }
                        }	                        
                    }
                }
	        }
            
	        subUnitNameList = StringUtil.substring(subUnitNameList, 0, subUnitNameList.length() - 1);        
        }
	    catch (Exception ex)
        {
	        subUnitNameList = StringUtil.EMPTY;
            log.warn(String.format("[getSubUnitNameListByProductElement] Get SubUnitNameList failed.[ProductName=%s - LotName=%s] ", productName, lotName));            
        }
	    
	    return subUnitNameList;	    
	}
	
	public static void productBatchSetEvent ( List<Product> productDataList, EventInfo eventInfo, Map<String, Object> changeColumns ) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
	{
		
		log.info( "Product Batch SetEvent Start = " + eventInfo.getEventName() + ", Product Count = " + String.valueOf(productDataList.size()));
		
		String sql = "Update Product Set ";
		String columns = "";
		
		Field[] fields = Product.class.getDeclaredFields();
		
		Map<String, String> productColName = new HashMap<String, String>();
		Map<String, String> histColName = new HashMap<String, String>();
		Map<String, String> oldColName = new HashMap<String, String>();
		//Generat Update Query
		for( Field field : fields )
		{
			if ( field.getName().equals("key") )
			{
				continue;
			}
			
			if ( columns.length() != 0 )
			{
				columns = columns + ", ";
			}
			
			columns += field.getName() + " = ?";
			productColName.put(field.getName().toUpperCase(), field.getName());
		}
		sql += columns;
		sql += " Where productname = ? ";
		
		List<Object[]> batchArgs = new LinkedList<Object[]>();
		List<ProductHistory> productHistoryList = new LinkedList<ProductHistory>();
		Field[] histfields = ProductHistory.class.getDeclaredFields();
		
		for( Field field : histfields)
		{
			if ( field.getName().equals("key") )
			{
				continue;
			}
			histColName.put(field.getName().toUpperCase(), field.getName());
			
			if ( field.getName().toUpperCase().startsWith("OLD"))
			{
				oldColName.put(field.getName().toUpperCase(), field.getName());
			}
		}
		
		for( Product productData : productDataList)
		{
			ProductHistory productHistory = new ProductHistory();
			ProductServiceProxy.getProductHistoryDataAdaptor().setHV( new Product(), productData, productHistory );
			
			// Set Old Data.
			Set<String> oldArr = oldColName.keySet();
			for (String key : oldArr)
			{
				String columName = key.replace("OLD","");
				String oldName = oldColName.get(key);
				if ( productColName.containsKey(columName))
				{
					String productfieldName = productColName.get(columName);
					Field productField = productData.getClass().getDeclaredField(productfieldName);
					Field histField = productHistory.getClass().getDeclaredField(oldName) ;
					
					productField.setAccessible(true);
					histField.setAccessible( true );
					Object value = productField.get(productData);
					ObjectUtil.copyFieldValue( histField, productHistory, value);
				}
			}
						
			//Set Last Event Info
			productData.setLastEventComment( eventInfo.getEventComment() );
			productData.setLastEventName(eventInfo.getEventName());
			productData.setLastEventTime(eventInfo.getEventTime());
			productData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			productData.setLastEventUser(eventInfo.getEventUser());
			
			// Field have Key. Key is Don't Count , 
			// Field Count - 1 ( Key ) + 1 (Where ProductName) 
			// batchValues Param Count = Field Count
			Object[] batchValues = new Object[fields.length];
			int i = 0 ;
			for( Field productfield : fields )
			{
				if ( productfield.getName().equals("key") )
				{
					continue;
				}
				
				if ( changeColumns.containsKey(productfield.getName()))
				{
					productfield.setAccessible(true);
					ObjectUtil.copyFieldValue( productfield, productData, changeColumns.get( productfield.getName() ) );
				}
				productfield.setAccessible(true);
				Object value = productfield.get(productData);
				
				// Set History Value
				if ( histColName.containsKey(productfield.getName().toUpperCase()))
				{
					String colName = histColName.get(productfield.getName().toUpperCase());
					Field histField = productHistory.getClass().getDeclaredField(colName) ;
					histField.setAccessible( true );
					ObjectUtil.copyFieldValue( histField, productHistory, value);
				}
				
				batchValues[i] = value;
				i++;
			}
			// Where LotName Param
			batchValues[i] = productData.getKey().getProductName();
			batchArgs.add(batchValues);
			
			// Set Event info
			
			productHistory.setEventComment( eventInfo.getEventComment() );
			productHistory.setEventName( eventInfo.getEventName() );
			productHistory.setEventTime( eventInfo.getEventTime());
			productHistory.setEventUser( eventInfo.getEventUser());
			productHistory.getKey().setTimeKey( TimeStampUtil.getCurrentEventTimeKey() );

			productHistoryList.add( productHistory );
		}
		
		greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().batchUpdate( sql, batchArgs );
		ProductServiceProxy.getProductHistoryService().insert( productHistoryList );
		
		log.info( "Product Batch SetEvent Completed = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey() + ", Product Count = " + String.valueOf(productDataList.size()) );
	}
}