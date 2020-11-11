package kr.co.aim.messolution.lot.service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import kr.co.aim.messolution.alarm.MESAlarmServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AutoMQCSetting;
import kr.co.aim.messolution.extended.object.management.data.CorresSampleLot;
import kr.co.aim.messolution.extended.object.management.data.FileJudgeSetting;
import kr.co.aim.messolution.extended.object.management.data.FlowSampleLot;
import kr.co.aim.messolution.extended.object.management.data.FlowSampleProduct;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.extended.object.management.data.LotQueueTime;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
import kr.co.aim.messolution.extended.object.management.data.MQCJobOper;
import kr.co.aim.messolution.extended.object.management.data.MQCJobPosition;
import kr.co.aim.messolution.extended.object.management.data.OperAction;
import kr.co.aim.messolution.extended.object.management.data.PermanentHoldInfo;
import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
import kr.co.aim.messolution.extended.object.management.data.RecipeIdleTime;
import kr.co.aim.messolution.extended.object.management.data.RecipeIdleTimeLot;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.SampleLotCount;
import kr.co.aim.messolution.extended.object.management.data.SampleLotState;
import kr.co.aim.messolution.extended.object.management.data.SampleProduct;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.event.CNX.ShipLot;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.product.service.ProductServiceUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.productrequest.service.ProductRequestServiceUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.orm.ObjectAttributeDef;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.IncrementTimeUsedInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.data.LotMultiHold;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.CreateWithParentLotInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.RecreateAndCreateAllProductsInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.product.management.info.RecreateInfo;
import kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.product.management.info.ext.ProductNPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductRU;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.IncrementScrappedQuantityByInfo;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

/**
 * @author Administrator
 *
 */
public class LotServiceUtil implements ApplicationContextAware {
	private static  Log log = LogFactory.getLog(LotServiceUtil.class);
	/**
	 * @uml.property name="applicationContext"
	 * @uml.associationEnd
	 */
	private ApplicationContext applicationContext;

	/**
	 *
	 */

	public LotServiceUtil() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.context.ApplicationContextAware#setApplicationContext
	 * (org.springframework.context.ApplicationContext)
	 */

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property name="applicationContext"
	 */
	@Override
    public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}


	/*
	 * Name : ConvertProductP Desc : This function is ConvertProductP Author :
	 * AIM Systems, Inc Date : 2011.01.14
	 */
	public List<ProductP> convertProductP(List<ProductPGS> productPGSSequence)
			throws Exception {
		List<ProductP> productPList = new ArrayList<ProductP>();

		for (ProductPGS productPGS : productPGSSequence) {
			ProductP productp = new ProductP();
			productp.setProductName(productPGS.getProductName());
			productp.setPosition(productPGS.getPosition());

			productPList.add(productp);
		}

		return productPList;
	}

	/*
	 * Name : ConvertProductU Desc : This function is ConvertProductU Author :
	 * AIM Systems, Inc Date : 2011.01.14
	 */
	public List<ProductU> convertProductU(List<ProductP> productPSequence)
			throws Exception {
		List<ProductU> productUList = new ArrayList<ProductU>();

		for (ProductP productp : productPSequence) {
			ProductU productU = new ProductU();
			productU.setProductName(productp.getProductName());

			productUList.add(productU);
		}

		return productUList;
	}

	/*
	 * Name : ConvertProductRU Desc : This function is ConvertProductRU Author :
	 * AIM Systems, Inc Date : 2011.01.17
	 */
	public List<ProductRU> convertProductRU(List<ProductP> productPSequence)
			throws Exception {
		List<ProductRU> productRUList = new ArrayList<ProductRU>();

		for (ProductP productp : productPSequence) {
			ProductRU productRU = new ProductRU();
			productRU.setProductName(productp.getProductName());
			productRU.setReworkFlag("Y");

			productRUList.add(productRU);
		}

		return productRUList;
	}

	/*
	 * Name : copyUdfs Desc : This function is copyUdfs Author : AIM Systems,
	 * Inc Date : 2011.02.21
	 */
	private Map<String, String> copyUdfs(Map<String, String> fromUdfs,
			Map<String, String> toUdfs) {

		for (String key : fromUdfs.keySet()) {
			toUdfs.put(key, fromUdfs.get(key));
		}

		return toUdfs;
	}

	/*
	 * Name : setProductNameSequance Desc : This function is
	 * setProductNameSequance Author : AIM Systems, Inc Date : 2011.02.21
	 */
	public List<String> setProductNameSequance(String lotName)
			throws FrameworkErrorSignal, NotFoundSignal {

		List<String> list = new ArrayList<String>();

		List<Product> products = null;
		try {
			products = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
		} catch (Exception e) {
			log.error(e);
			products = ProductServiceProxy.getProductService().allProductsByLot(lotName);
		}

		for (Product product : products) {
			list.add(product.getKey().getProductName());
		}

		return list;
	}

	/*
	 * Name : getProductionType Desc : This function is getProductionType Author
	 * : AIM Systems, Inc Date : 2011.03.06
	 */
	public String getProductionType(String lotName)
			throws FrameworkErrorSignal, NotFoundSignal {

		String result = "";

		LotKey lotKey = new LotKey(lotName);

		Lot lotData = null;

		lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

		result = lotData.getProductionType();

		return result;
	}

	/*
	 * Name : validateLotState Desc : This function is validateLotState Author :
	 * AIM Systems, Inc Date : 2011.03.06
	 */
	public void validateLotState(String lotName, String... args)
			throws FrameworkErrorSignal, NotFoundSignal {

		LotKey lotKey = new LotKey();
		lotKey.setLotName(lotName);

		Lot lot = null;
		lot = LotServiceProxy.getLotService().selectByKey(lotKey);

		for (int i = 0; i < args.length; i++) {
			if(StringUtil.equals(args[i], lot.getLotState()))
			{
				return;
			}
		}
	}

	/*
	 * Name : makeLotAttribute Desc : This function is makeLotAttribute Author :
	 * AIM Systems, Inc Date : 2011.03.22
	 */
	public void makeLotAttribute(String lotName, String attributeName,String attributeValue) {

		LotKey lotKey = new LotKey();
		lotKey.setLotName(lotName);

		String value = "";

		try {
			value = LotServiceProxy.getLotService().getAttribute(lotKey,attributeName);
		} 
		catch (NotFoundSignal ne) {
		}
		if (value == null || StringUtil.isEmpty(value)) {
			LotServiceProxy.getLotService().addAttribute(lotKey, attributeName,attributeValue);
		} else {
			LotServiceProxy.getLotService().setAttribute(lotKey, attributeName,attributeValue);
		}
	}

	/*
	 * Name : getLotData Desc : This function is getLotData Author : AIM
	 * Systems, Inc Date : 2011.03.23
	 */
	public static Lot getLotData(String lotName) throws FrameworkErrorSignal,
	NotFoundSignal, CustomException {
		try {
			LotKey lotKey = new LotKey();
			lotKey.setLotName(lotName);
			Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

			return lotData;

		} catch (Exception e) {
			throw new CustomException("LOT-9000", lotName);
		}
	}

	/*
	 * Name : getLotData Desc : This function is getLotData from cst Author : AIM
	 * Systems, Inc Date : 2018.03.30
	 */
	public List<Lot> getLotDataFromCarrier(String carrierName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try
		{
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			List<Lot> lotList = LotServiceProxy.getLotService().select("WHERE CARRIERNAME = ?", new Object[] { carrierName });
			List<Lot> lotList = LotServiceProxy.getLotService().select("WHERE CARRIERNAME = ? FOR UPDATE", new Object[] { carrierName });

			return lotList;
		}
		catch (Exception e)
		{
			throw new CustomException("LOT-0048", "");
		}
	}

	/*
	* Name : getProductListByLotName
	* Desc : This function is getProductListByLotName
	* Author : AIM Systems, Inc
	* Date : 2011.01.13
	*/
	public List<Product> getProductListByLotName(String lotName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if(log.isInfoEnabled()){
			log.info("lotName = " + lotName);
		}

		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		return productList;
	}

	public static List<Lot> getLotListByLotElement(List<Element> lotElement) throws CustomException
	{
		log.info("getLotListByLotElement");
		List<Lot> lotList = new ArrayList<Lot>();
		String stringLot = "";

		try
		{

			if (lotElement.size() > 1000)
			{
				int productCount = 0;
				int num = 0;
				for(Element ePruduct : lotElement)
				{
					if (productCount / 1000 != num)
					{
						if (StringUtil.isNotEmpty(stringLot))
							stringLot = stringLot.substring(1);

						num = productCount / 1000;
						try
						{
							lotList.addAll(LotServiceProxy.getLotService().select(" WHERE LOTNAME IN (" + stringLot + ")", null));
						}
						catch (NotFoundSignal e)
						{
							throw new CustomException("LOT-0048", "");
						}
						stringLot = "";
					}
					String lotName = SMessageUtil.getChildText(ePruduct, "LOTNAME", true);
					if (StringUtil.isEmpty(lotName))
					{
						throw new CustomException("LOT-0048", "");
					}
					stringLot += ",'" + lotName + "'";

					productCount++;
				}
				if (StringUtil.isNotEmpty(stringLot))
					stringLot = stringLot.substring(1);
				lotList.addAll(LotServiceProxy.getLotService().select(" WHERE LOTNAME IN (" + stringLot + ")", null));
			}
			else
			{
				for(Element ePruduct : lotElement)
				{
					String lotName = SMessageUtil.getChildText(ePruduct, "LOTNAME", true);
					if (StringUtil.isEmpty(lotName))
					{
						throw new CustomException("LOT-0048", "");
					}
					stringLot += ",'" + lotName + "'";
				}
				if (StringUtil.isNotEmpty(stringLot))
					stringLot = stringLot.substring(1);

				try
				{
					lotList.addAll(LotServiceProxy.getLotService().select(" WHERE LOTNAME IN (" + stringLot + ")", null));
				}
				catch (NotFoundSignal e)
				{
					throw new CustomException("LOT-0048", "");
				}
			}

		}
		catch (NotFoundSignal e)
		{
		}
		return lotList;
	}

	/*
     * Name : getLotData Desc : This function is getLotData from cst Author : AIM
     * Systems, Inc Date : 2018.03.30
     */
    public String getLotNamefromProductElements(List<Element> productElement) throws FrameworkErrorSignal, NotFoundSignal, CustomException
    {
        String lotName = "";
        String lotNaming = "";
        try
        {
            for(Element ePruduct : productElement)
            {

                lotNaming = SMessageUtil.getChildText(ePruduct, "LOTNAME", true);

                if(StringUtil.isEmpty(lotName))
                {
                    lotName = lotNaming;
                }
                else
                {
                    if(!StringUtil.equals(lotName, lotNaming))
                    {
                        throw new CustomException("LOT-9042", lotName, lotNaming);
                    }
                }
            }

            return lotName;
        }
        catch (Exception e)
        {
            throw new CustomException("LOT-9042", lotName, lotNaming);
        }
    }

	/*
	 * Name : getLotData Desc : This function is getLotData from cst Author : AIM
	 * Systems, Inc Date : 2018.03.30
	 */
	public String getFirstLotNamefromProductElements(List<Element> productElement, String machineName, String reserveState) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try
		{
		    String lotName = "";
		    /* 20180531, In case the Lot is mixed in when Unpacker becomes LotProcessEnd, bring the lotName of the first started plan. ==>> */
			String crateName = SMessageUtil.getChildText(productElement.get(0), "CRATENAME", true);
			//1. Get Crate Data
	        ConsumableKey cKey = new ConsumableKey(crateName);
	        Consumable cData = null;
	        try
	        {
	        	cData = ConsumableServiceProxy.getConsumableService().selectByKey(cKey);
	        }
	        catch(Exception ce)
	        {	        	
	        }
	        
	        //[CRATE-0011]
	        if(cData == null)
	        	throw new CustomException("CRATE-0011",crateName );


	        //2. Get Plan Data / Get Product Request Data
	        //[PRODUCTREQUEST-0022],[PRODUCTREQUEST-0053]
	        ProductRequestPlan pPlanData = CommonUtil.getFirstPlanByCrateSpecName(cData.getConsumableSpecName().toString(), reserveState);
	        
	        if(pPlanData == null)
	        {
	            throw new CustomException("PRODUCTREQUEST-0002", cData.getConsumableSpecName());
	        }
	        
	        String productRequestName = pPlanData.getKey().getProductRequestName();

	        //[LOT-0207]
	        String reservedLotName = getFirstStartLotByProductRequestName(machineName, productRequestName);
	        if(StringUtil.isEmpty(reservedLotName))
	        {
	            throw new CustomException("PRODUCTREQUEST-0021", machineName);
	        }
	        /* <<== 20180531, In case the Lot is mixed in when Unpacker becomes LotProcessEnd, bring the lotName of the first started plan. */

	        for(Element ePruduct : productElement)
			{
			    String lotNaming = SMessageUtil.getChildText(ePruduct, "LOTNAME", true);
			    if(!StringUtil.equals(reservedLotName, lotNaming))
                {
                    log.warn("LotName is different." + "[MES: " + reservedLotName + "EAP: " + lotNaming);
                }

				if(StringUtil.isEmpty(lotName))
				{
					lotName = lotNaming;
				}
				else
				{
					if(!StringUtil.equals(lotName, lotNaming))
					{
						//throw new CustomException("LOT-0048", "");
					    log.warn("LotName is mixed." + "[MES: " + reservedLotName + "EAP: " + lotName + "," + lotNaming);
					}
				}
			}

			return reservedLotName;
		}
		catch (Exception ce)
		{		
			//modified by wghuang
			//throw new CustomException("LOT-0048", "");
			throw ce;
		}
	}

	/*
	 * Name : getLotData Desc : This function is getLotData from cst Author : AIM
	 * Systems, Inc Date : 2018.03.30
	 */
	public String getCrateNameAndMRecipefromProductElements(List<Element> productElement) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try
		{
			String CrateAndPrecipe = "";

			for(Element ePruduct : productElement)
			{
				String crateName = SMessageUtil.getChildText(ePruduct, "CRATENAME", true);
				String productRecipeName = SMessageUtil.getChildText(ePruduct, "PRODUCTRECIPE", true);

				if(StringUtil.isNotEmpty(crateName) && StringUtil.isNotEmpty(productRecipeName))
				{
					CrateAndPrecipe = crateName + "," +productRecipeName;
					break;
				}
				else
				{
					throw new CustomException("CRATE-0010", "");
				}
			}

			return CrateAndPrecipe;
		}
		catch (Exception e)
		{
			//throw new CustomException("CRATE-0010", "");
			throw e;
		}
	}

	/*
	 * Name : getDistinctLotNameByProductName
	 * Desc : This function is getDistinctLotNameByProductName
	 * Author : AIM Systems, Inc
	 * Date : 2011.03.11
	 */
	public static String getDistinctLotNameByProductName(String productName) {
		String lotName = "";

		String sql = "SELECT LOTNAME FROM PRODUCT WHERE PRODUCTNAME = :productName";

		Map bindMap = new HashMap<String, Object>();
		bindMap.put("productName", productName);

		//		List<Map<String, Object>> sqlResult =
		//			kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql,bindMap);

		List<Map<String, Object>> sqlResult =GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		List<String> lotList = new ArrayList<String>();

		if (sqlResult.size() > 0)
		{
			if (!(StringUtils.isEmpty((String) sqlResult.get(0).get("LOTNAME"))))
			{
				ListOrderedMap lotMap = (ListOrderedMap) sqlResult.get(0);
				lotName = lotMap.get("LOTNAME").toString();
			}
		}

		return lotName;
	}

	/**
	 *
	 * @author smkang
	 * @since 2013-05-20
	 * @param bodyElement
	 * @return classfiedProductListElements
	 */
	public List<Element> getClassifiedProductListElement(Element bodyElement) {
		List<Element> classfiedProductListElements = new ArrayList<Element>();
		List<Element> productElementList = bodyElement.getChild("PRODUCTLIST").getChildren("PRODUCT");

		if (productElementList.size() > 0) {
			Element classifiedProductListElement = new Element("PRODUCTLIST");
			Map<String, Element> classfiedProductListMap = new Hashtable<String, Element>();

			for (int index = 0; index < productElementList.size(); index++) {
				Element productElement = productElementList.get(index);
				Element cloneProductElement = (Element) productElement.clone();
				String lotName = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productElement.getChildText("PRODUCTNAME"))).getLotName();

				if (cloneProductElement.getChild("LOTNAME") == null)
					cloneProductElement.addContent((new Element("LOTNAME")).setText(lotName));
				else
					cloneProductElement.getChild("LOTNAME").setText(lotName);

				if (classfiedProductListMap.containsKey(lotName)) {
					classifiedProductListElement = (classfiedProductListMap.get(lotName));
					classifiedProductListElement.addContent(cloneProductElement);
				} else {
					classifiedProductListElement = new Element("PRODUCTLIST");
					classifiedProductListElement.addContent(cloneProductElement);
					classfiedProductListMap.put(lotName,classifiedProductListElement);
				}
			}

			Set<String> keySet = classfiedProductListMap.keySet();

			for (String key : keySet) {
				classfiedProductListElements.add(classfiedProductListMap
						.get(key));
			}
		}

		return classfiedProductListElements;
	}
	
	/*
	 * Name : getNotAssigenCarrierProductPSequence_Loader Desc : This function
	 * is getNotAssigenCarrierProductPSequence_Loader Author : AIM Systems, Inc
	 * Date : 2019.05.16
	 */
	public static List<ProductP> getNotAssigenCarrierProductPSequence_Loader(Element element, String carrierName) throws CustomException 
	{
		List<String> productList = CommonUtil.makeList(element, "PRODUCTLIST", "PRODUCTNAME");
		
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT PRODUCTNAME FROM PRODUCT ");
		sql.append("  WHERE CARRIERNAME = :carrierName ");
		sql.append("    AND PRODUCTSTATE <> :scrapped ");
		sql.append("    AND PRODUCTSTATE <> :consumed ");
		sql.append(" INTERSECT ");
		sql.append(" SELECT PRODUCTNAME ");
		sql.append("   FROM PRODUCT ");
		sql.append("  WHERE PRODUCTNAME IN (:productList) "); 
		
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("carrierName", carrierName);
		bindMap.put("productList", productList);
		bindMap.put("scrapped", GenericServiceProxy.getConstantMap().Prod_Scrapped);
		bindMap.put("consumed", GenericServiceProxy.getConstantMap().Prod_Consumed);

		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

		
		List<ProductP> productPSequence = new ArrayList<ProductP>();

		
		String ApplyElapsedTimeFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
		if(ApplyElapsedTimeFlag != null && ApplyElapsedTimeFlag.equals(GenericServiceProxy.getConstantMap().FLAG_Y))
		{
			if(sqlResult != null && sqlResult.size() > 0)
			{
				List<String> searchProductList = new ArrayList<String>();
				for(Map<String, Object> productData : sqlResult)
				{
					String productName = ConvertUtil.getMapValueByName(productData, "PRODUCTNAME");
					if(!productName.isEmpty() && !searchProductList.contains(productName))
					{
						searchProductList.add(productName);
					}
				}
				
				StringBuilder sqlProduct = new StringBuilder();
				sqlProduct.append(" SELECT PRODUCTNAME, POSITION FROM PRODUCT WHERE PRODUCTNAME IN (:productList) ");
				
				Map<String, Object> bindMapProduct = new HashMap<String, Object>();
				bindMapProduct.put("productList", searchProductList);
	
				List<Map<String, Object>> productResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sqlProduct.toString(), bindMapProduct);
	
				if(productResult != null && productResult.size() > 0)
				{
					for(Map<String, Object> productData : productResult)
					{
						String productName = ConvertUtil.getMapValueByName(productData, "PRODUCTNAME");
						String position = ConvertUtil.getMapValueByName(productData, "POSITION");
			
						ProductP productP = new ProductP();
						productP.setProductName(productName);
						productP.setPosition(Long.parseLong(position));
			
						productPSequence.add(productP);
					}
				}
			}
		}
		else
		{
			for (int i = 0; i < sqlResult.size(); i++) 
			{
				ListOrderedMap productMap = (ListOrderedMap) sqlResult.get(i);
				String productName = productMap.get("PRODUCTNAME").toString();
	
				Product productData = CommonUtil.getProductByProductName(productName);
	
				ProductP productP = new ProductP();
				productP.setProductName(productName);
				productP.setPosition(productData.getPosition());
				//productP.setUserColumns(productData.getUserColumns());
	
				productPSequence.add(productP);
			}
		}

		return productPSequence;
	}
	
	/*
	 * Name : setProductPSimpleSequence Desc : This function is
	 * setProductPSimpleSequence Author : AIM Systems, Inc Date : 2019.05.16
	 */
	public static List<ProductP> setProductPSimpleSequence(org.jdom.Document xml)
			throws FrameworkErrorSignal, NotFoundSignal {
		if (xml == null) {
			log.error("xml is null");
		}

		List<ProductP> productPList = new ArrayList<ProductP>();
		ProductServiceUtil productServiceUtil = (ProductServiceUtil) BundleUtil
				.getBundleServiceClass(ProductServiceUtil.class);

		Element root = xml.getDocument().getRootElement();

		Element element = root.getChild("BODY").getChild("PRODUCTLIST");
		if (element != null) {
			for (Iterator iterator = element.getChildren().iterator(); iterator
					.hasNext();) {
				Element productE = (Element) iterator.next();
				String productName = productE.getChild("PRODUCTNAME").getText();

				String position = productE.getChild("POSITION").getText();

				ProductP productP = new ProductP();

				productP.setProductName(productName);
				productP.setPosition(Long.valueOf(position));

				productP.setUdfs(productServiceUtil.setNamedValueSimpleSequence(productName, productE));

				productPList.add(productP);
			}
		}

		return productPList;
	}

	/*
	 * Name : setProductPGSSequence
	 * Desc : This function is setProductPGSSequence
	 * Author : AIM Systems, Inc
	 * Date : 2011.01.11
	 */
	public List<ProductPGS> setProductPGSSequence(org.jdom.Document doc)
			throws FrameworkErrorSignal, NotFoundSignal, CustomException {
		if (doc == null) {
			log.error("doc is null");
		}

		List<ProductPGS> productPGSList = new ArrayList<ProductPGS>();
		ProductServiceUtil productServiceUtil = (ProductServiceUtil) BundleUtil.getBundleServiceClass(ProductServiceUtil.class);

		Element root = doc.getDocument().getRootElement();

		List<Product> productDatas = new ArrayList<Product>();

		String lotName = root.getChild("Body").getChildText("LOTNAME");

		LotKey lotKey = new LotKey();
		lotKey.setLotName(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

		ProductSpecKey productSpecKey = new ProductSpecKey();
		productSpecKey.setFactoryName(lotData.getFactoryName());
		productSpecKey.setProductSpecName(lotData.getProductSpecName());
		productSpecKey.setProductSpecVersion(lotData.getProductSpecVersion());

		ProductSpec productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);

		//if (lotName == null || lotName.equals("") == true)
		if(StringUtil.isEmpty(lotName) || StringUtil.equals("", lotName))
		{
			String carrierName = root.getChild("Body").getChildText("CARRIERNAME");
			if (carrierName != null && carrierName != "") {

				String condition = "WHERE carrierName = ?"
						+ "AND productState != ?" + "AND productState != ?"
						+ "ORDER BY position ";

				Object[] bindSet = new Object[] { carrierName,
						GenericServiceProxy.getConstantMap().Prod_Scrapped,
						GenericServiceProxy.getConstantMap().Prod_Consumed };

				try {
					ProductServiceProxy.getProductService().select(condition,bindSet);
				} catch (NotFoundSignal e) {
				}
			}
		} else {
			try {
				productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
			} catch (NotFoundSignal e) {
			}
		}

		Element element = root.getChild("Body").getChild("PRODUCTLIST");
		if (element != null) {
			for (Iterator iterator = element.getChildren().iterator(); iterator.hasNext();) {
				Element productE = (Element) iterator.next();

				String productName = productE.getChild("PRODUCTNAME").getText();

				ProductKey productkey = new ProductKey();
				productkey.setProductName(productName);

				Product productData = ProductServiceProxy.getProductService().selectByKey(productkey);

				if(StringUtil.equals(lotName, productData.getLotName()))
				{
					Boolean searchFlag = false;

					String subProductQty = ""; // PEX

					for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
					{
						Product product = iteratorProduct.next();
						if (productName.equals(product.getKey().getProductName()))
						{
							subProductQty = String.valueOf(product.getSubProductQuantity()); // PEX
							searchFlag = true;
							break;
						}
					}

					if (productDatas.size() == 0 || searchFlag == true)
					{
						String subProductQuantity1 = "";
						String subProductQuantity2 = "";

						String position = productE.getChild("POSITION").getText();

						String productGrade = "";
						if (productE.getChild("PRODUCTGRADE") != null) {
							productGrade = productE.getChild("PRODUCTGRADE").getText();
						}

						String subProductGrades1 = "";
						if (productE.getChild("SUBPRODUCTGRADES1") != null) {
							subProductGrades1 = productE.getChild("SUBPRODUCTGRADES1").getText();
						}
						else
						{
							subProductGrades1 = productData.getSubProductGrades1();
						}

						String subProductGrades2 = "";
						if (productE.getChild("SUBPRODUCTGRADES2") != null) {
							subProductGrades2 = productE.getChild("SUBPRODUCTGRADES2").getText();
						}
						else
						{
							subProductGrades1 = productData.getSubProductGrades1();
						}

						if (productE.getChild("SUBPRODUCTQUANTITY1") != null)
							subProductQuantity1 = productE.getChild("SUBPRODUCTQUANTITY1").getText();
						else
							subProductQuantity1 = subProductQty;

						if (productE.getChild("SUBPRODUCTQUANTITY2") != null)
							subProductQuantity2 = productE.getChild("SUBPRODUCTQUANTITY2").getText();
						else
							subProductQuantity2 = "";

						ProductPGS productPGS = new ProductPGS();

						productPGS.setProductName(productName);
						
						//if (position.equals("") != true)
						if(!StringUtil.equals(position, ""))
							productPGS.setPosition(Long.valueOf(position));
						productPGS.setProductGrade(productGrade);
						productPGS.setSubProductGrades1(subProductGrades1);
						productPGS.setSubProductGrades2(subProductGrades2);
						
						//if (subProductQuantity1.equals("") != true)
						if(!StringUtil.isEmpty(subProductQuantity1))
							productPGS.setSubProductQuantity1(Double.valueOf(subProductQuantity1));
						//if (subProductQuantity2.equals("") != true)
						if(!StringUtil.isEmpty(subProductQuantity2))
							productPGS.setSubProductQuantity2(Double.valueOf(subProductQuantity2));

						productPGS.setUdfs(productServiceUtil.setNamedValueSequence(productName, productE));

						productPGSList.add(productPGS);
					}
				}
			}
		}
		return productPGSList;
	}
	
	/*
	 * Name : ScrapLotByTrackOut
	 * Desc : This function is ScrapLotByTrackOut
	 * Author : AIM Systems, Inc
	 * Date : 2015.01.07
	 */
	public static void ScrapLotByTrackOut(EventInfo eventInfo, String lotName) throws CustomException
	{
		// 1. Get Lot Data
		if (lotName.length() > 0)
		{
			Lot lotData = CommonUtil.getLotInfoByLotName(lotName);
			// 2. If LotState is Released & ProductQuantity is 0
			if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && lotData.getProductQuantity() == 0)
			{
				List<ProductU> productUSequence = new ArrayList<ProductU>();
				// 3. ChnageSpec to Set LotState = 'Emptied', HoldState = '',
				// LotProcessState = ''.

				LotKey lotKey = new LotKey();
				lotKey.setLotName(lotName);

				ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
				changeSpecInfo.setLotState(GenericServiceProxy.getConstantMap().Lot_Emptied);
				changeSpecInfo.setLotHoldState("");
				changeSpecInfo.setLotProcessState("");
				changeSpecInfo.setFactoryName(lotData.getFactoryName());
				changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
				changeSpecInfo.setPriority(lotData.getPriority());
				changeSpecInfo.setProductionType(lotData.getProductionType());
				changeSpecInfo.setDueDate(lotData.getDueDate());
				changeSpecInfo.setProductUSequence(productUSequence);
				LotServiceProxy.getLotService().changeSpec(lotKey, eventInfo, changeSpecInfo);

				if (lotData.getCarrierName().length() > 0)
				{
					Durable durableData = CommonUtil.getDurableInfo(lotData.getCarrierName());
					DeassignCarrierInfo deassignCarrierInfo = LotInfoUtil.deassignCarrierInfo(lotData, durableData, productUSequence);

					eventInfo.setEventName("DeassignCarrier");
					LotServiceProxy.getLotService().deassignCarrier(lotData.getKey(), eventInfo, deassignCarrierInfo);
				}
			}
		} else {
			log.info("Lot ID is null");
		}
	}

	/*
	 * Name : getReworkNodeInfo
	 * Desc : This function is getReworkNodeInfo
	 * Author : jhyeom
	 * Date : 2014.04.29
	 */
	public List<Map<String, Object>> getReworkNodeInfo(String lotName, String returnOperName, String returnFlowName) throws CustomException
	{
		String nodeSql = "";
		String nodeType = "";
		String nodeStack = "";

		// Get NodeStack of ReworkDivergence
		//2016.02.17 modify by jhyeom.
		//Because Comeback Operation
		//String nodeId = isPossibleStartRework(lotName);
		String sql = "SELECT NODETYPE,NODEID,NODEATTRIBUTE1 FROM NODE WHERE NODEATTRIBUTE1 = :nodeAttribute1 AND PROCESSFLOWNAME = :processFlowName ";
		Map bindMap = new HashMap<String, Object>();
		bindMap.put("nodeAttribute1", returnOperName);
		bindMap.put("processFlowName", returnFlowName);

		List<Map<String, Object>> nodeInfo = new ArrayList<Map<String, Object>>();
		//		List<Map<String, Object>> sqlResult =
		//			kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		nodeInfo = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		//List<Map<String, Object>> sqlResult =
		//		GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		/*
		if(sqlResult.size() > 0)
		{
			nodeStack = (String)sqlResult.get(0).get("NODEID");
			do{

				// FROMNODEID °¡Á®¿À´Â °ÍÀ» TONODEID ·Î ¼öÁ¤. ´ÙÀ½°øÁ¤À» °¡Á®¿Í¼­ ³ëµå¿¡ ¹Ú¾ÆÁÖ±â ¶§¹®.
				nodeSql = "SELECT N.NODETYPE,N.NODEID,N.NODEATTRIBUTE1 FROM ARC A, NODE N" +
						  " WHERE A.TONODEID = :nodeStack AND A.TONODEID = N.NODEID ";
				bindMap.clear();
				bindMap.put("nodeStack", nodeStack);

				//nodeInfo = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(nodeSql, bindMap);
				nodeInfo = GenericServiceProxy.getSqlMesTemplate().queryForList(nodeSql, bindMap);
				nodeType = (String)nodeInfo.get(0).get("NODETYPE");
				nodeStack = (String)nodeInfo.get(0).get("NODEID");

			}while(!StringUtils.equals(nodeType, GenericServiceProxy.getConstantMap().Node_ProcessOperation));
		}*/

		if(!(nodeInfo.size() > 0))
			throw new CustomException("Node-0003");

		return nodeInfo;
	}

	/*
	 * Name : isPossibleStartRework
	 * Desc : This function is Return ReworkDivergence NodeStack to Check Possible Rework Operation
	 * Author : jhyeom
	 * Date : 2014.04.29
	 */
	public String isPossibleStartRework(String lotName) throws CustomException
	{
		String sql 							= "";
		String nodeId 						= "";
		String nodeType 					= "";
		String processOperationName 		= "";
		Map bindMap = new HashMap<String, Object>();
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		Lot lotData 						= MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		processOperationName = lotData.getProcessOperationName();
		nodeId = lotData.getNodeStack();
		do{
			sql = "SELECT NODEID, NODETYPE FROM NODE" +
					" WHERE NODEID IN ( SELECT A.TONODEID FROM ARC A, NODE N WHERE N.NODEID = :nodeId AND N.NODEID = A.TONODEID )" +
					"   AND NODETYPE IN (:nodeType1, :nodeType2, :nodeType3) ";
			bindMap.clear();
			bindMap.put("nodeId", nodeId);
			bindMap.put("nodeType1", GenericServiceProxy.getConstantMap().Node_ProcessOperation);
			bindMap.put("nodeType2", GenericServiceProxy.getConstantMap().Node_ReworkConvergence);
			bindMap.put("nodeType3", GenericServiceProxy.getConstantMap().Node_ReworkDivergence);
			
			//			sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if(sqlResult.size() > 0)
			{
				nodeType = (String)sqlResult.get(0).get("NODETYPE");
				nodeId = (String)sqlResult.get(0).get("NODEID");

				if(StringUtils.equals(nodeType, GenericServiceProxy.getConstantMap().Node_ProcessOperation)) {
					//2014.05.08 jhyeom .
					//throw new CustomException("LOT-9025", lotName, processOperationName);
				}else if(StringUtils.equals(nodeType, GenericServiceProxy.getConstantMap().Node_ReworkDivergence)) {
					break;
				}
			}
		}while(StringUtils.equals(nodeType, GenericServiceProxy.getConstantMap().Node_ReworkConvergence));

		return nodeId;
	}

	/*
	 * Name : isPossibleStartRework
	 * Desc : This function is Return ReworkDivergence NodeStack to Check Possible Rework Operation
	 * Author : jhyeom
	 * Date : 2014.04.29
	 */
	public String getBeforeOperationName (String processFlowName , String processOperationName ) throws CustomException
	{
		String nodeId = "";
		String sql    = "";

		Map bindMap = new HashMap<String, Object>();
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> sqlRst = new ArrayList<Map<String, Object>>();

		sql = " SELECT NODEID FROM NODE WHERE NODEATTRIBUTE1 = :processOperationName AND PROCESSFLOWNAME = :processFlowName ";

		bindMap.clear();
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("processFlowName"     , processFlowName);
		//sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if(sqlResult.size() > 0)
		{
			String nodeName = (String)sqlResult.get(0).get("NODEID");

			sql = " SELECT N.NODEATTRIBUTE1 "+
					" FROM NODE N "+
					"	WHERE 1=1 "+
					" AND N.NODEID = ( SELECT FROMNODEID FROM ARC WHERE TONODEID = :nodeId AND PROCESSFLOWNAME = :processFlowName ) ";

			bindMap.clear();
			bindMap.put("nodeId", nodeName);
			bindMap.put("processFlowName", processFlowName);
			//sqlRst = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			sqlRst = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if ( sqlRst.size() > 0)
			{
				nodeId = (String)sqlRst.get(0).get("NODEATTRIBUTE1");
			}
		}

		return nodeId ;
	}

	/*
	 * Name : setNamedValueSequence
	 * Desc : This function is setNamedValueSequence
	 * Author : jhyeom
	 * Date : 2014.04.29
	 */
	public Map<String, String> setNamedValueSequence(String lotName,
			Element element) throws FrameworkErrorSignal, NotFoundSignal {

		//AlarmServiceImpl alarmServiceImpl = (AlarmServiceImpl) BundleUtil
		//		.getBundleServiceClass(AlarmServiceImpl.class);
		/*
		if (log.isInfoEnabled()) {
			log.info("lotName = " + lotName);

			if (alarmServiceImpl.isStringEmpty(lotName)) {
				alarmServiceImpl.AlarmEventByMES("Null Argument", "", lotName);
			}
		}
		 */

		Map namedValueMap = new HashMap<String, String>();

		LotKey lotKey = new LotKey(lotName);

		Lot lotData = null;

		lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

		List<ObjectAttributeDef> objectAttributeDefs = greenFrameServiceProxy
				.getObjectAttributeMap().getAttributeNames("Lot", "ExtendedC");

		log.info("UDF SIZE=" + lotData.getUdfs().size());

		namedValueMap = lotData.getUdfs();

		List<Element> bodyElementList = null;
		String tempElementUdfName = "";
		String tempElementUdfValue = "";

		for (int i = 0; i < objectAttributeDefs.size(); i++) {
			if (element != null) {
				tempElementUdfValue = element.getChildText(objectAttributeDefs
						.get(i).getAttributeName());
				if (tempElementUdfValue != null) {
					namedValueMap.put(objectAttributeDefs.get(i)
							.getAttributeName(), tempElementUdfValue);
				}
			}

		}
		log.info("UDF SIZE=" + namedValueMap.size());
		return namedValueMap;
	}

	/*
	 * Name : setProductUSequence
	 * Desc : This function is setProductUSequence
	 * Author : JHYEOM
	 * Date : 2014.05.07
	 */
	public List<ProductU> setProductUSequence(org.jdom.Document doc)
			throws FrameworkErrorSignal, NotFoundSignal {
		if (doc == null) {
			log.error("xml is null");
		}

		List<ProductU> productUList = new ArrayList<ProductU>();
		ProductServiceUtil productServiceUtil = (ProductServiceUtil) BundleUtil
				.getBundleServiceClass(ProductServiceUtil.class);

		Element root = doc.getDocument().getRootElement();

		List<Product> productDatas = new ArrayList<Product>();

		String lotName = root.getChild("Body").getChildText("LOTNAME");
		if (lotName == null || lotName == "") {
			String carrierName = root.getChild("Body").getChildText(
					"CARRIERNAME");
			if (carrierName != null && carrierName != "") {

				String condition = "WHERE carrierName = ?"
						+ "AND productState != ?" + "AND productState != ?"
						+ "ORDER BY position ";

				Object[] bindSet = new Object[] { carrierName,
						GenericServiceProxy.getConstantMap().Prod_Scrapped,
						GenericServiceProxy.getConstantMap().Prod_Consumed };
				try {
					productDatas = ProductServiceProxy.getProductService()
							.select(condition, bindSet);
				} catch (Exception e) {

				}
			}
		} else {
			try {
				productDatas = ProductServiceProxy.getProductService()
						.allUnScrappedProductsByLot(lotName);
			} catch (Exception e) {
				log.error(e);
				productDatas = ProductServiceProxy.getProductService()
						.allProductsByLot(lotName);
			}

		}

		Element element = root.getChild("Body").getChild("PRODUCTLIST");

		if (element != null) {
			for (Iterator iterator = element.getChildren().iterator(); iterator
					.hasNext();) {
				Element productE = (Element) iterator.next();
				String productName = productE.getChild("PRODUCTNAME").getText();

				ProductU productU = new ProductU();

				productU.setProductName(productName);
				productU.setUdfs(productServiceUtil.setNamedValueSequence(
						productName, productE));

				productUList.add(productU);
			}
		} else {

			for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct
					.hasNext();) {
				Product product = iteratorProduct.next();

				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.setUdfs(product.getUdfs());

				productUList.add(productU);
			}
		}

		return productUList;
	}

	/*
	 * Name : setProductUSequence
	 * Desc : This function is setProductUSequence
	 * Author : JHYEOM
	 * Date : 2014.05.07
	 */
	public List<ProductU> setProductUSequence(String lotName) throws CustomException
	{
		List<ProductU> productUList = new ArrayList<ProductU>();

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		List<Product> productDatas = new ArrayList<Product>();

		try {
			productDatas = ProductServiceProxy.getProductService()
					.allUnScrappedProductsByLot(lotName);
		} catch (Exception e) {
			log.error(e);
			productDatas = ProductServiceProxy.getProductService()
					.allProductsByLot(lotName);
		}

		for (Product product : productDatas)
		{
			ProductU productU = new ProductU();
			productU.setProductName(product.getKey().getProductName());
			productU.setUdfs(product.getUdfs());

			productUList.add(productU);
		}

		return productUList;
	}
	
	/*
	 * Name : setProductConsumedMaterialSequence
	 * Desc : This function is setProductConsumedMaterialSequence
	 * Author : jhyeom
	 * Date : 2014.07.31
	 */
	public List<ConsumedMaterial> setProductConsumedMaterialSequence(
			Product productData, String materialName) throws CustomException
			{
		List<ConsumedMaterial> consumedMaterialList = new ArrayList<ConsumedMaterial>();

		ConsumedMaterial consumedMaterial = new ConsumedMaterial();

		consumedMaterial.setMaterialName(materialName);
		consumedMaterial.setMaterialType("Product");
		consumedMaterial.setQuantity(1);

		Map<String, String> productUdfs = productData.getUdfs();
		consumedMaterial.setUdfs(productUdfs);

		consumedMaterialList.add(consumedMaterial);

		return consumedMaterialList;
			}

	/*
	 * Name : setProductPGSSequence
	 * Desc : This function is setProductPGSSequence
	 * Author : jhyeom
	 * Date : 2014.10.20
	 */
	private List<ProductPGS> setProductPGSSequence(String crateName, List<String> productList, long createSubProductQuantity)
	{
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

		for (int i = 0; i < productList.size(); i++)
		{
			try
			{
				String sProductName = productList.get(i).toString();
				//String sPosition = SMessageUtil.getChildText(eProduct, "POSITION", true);
				//String sCrateName = SMessageUtil.getChildText(eProduct, "CRATENAME", true);

				ProductPGS productInfo = new ProductPGS();
				productInfo.setPosition(i+1);
				productInfo.setProductGrade(GradeDefUtil.getGrade(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,
						GenericServiceProxy.getConstantMap().GradeType_Product, true).getGrade());
				productInfo.setProductName(sProductName);
				//productInfo.setSubProductGrades1(GradeDefUtil.generateGradeSequence(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,
				//																	GenericServiceProxy.getConstantMap().GradeType_SubProduct, true, createSubProductQuantity));
				productInfo.setSubProductGrades1("");
				productInfo.setSubProductQuantity1(createSubProductQuantity);

				productInfo.getUdfs().put("CRATENAME", crateName);

				productPGSSequence.add(productInfo);
			}
			catch (Exception ex)
			{
			}
		}

		return productPGSSequence;
	}

	/**
	 * determine product to proceed
	 * @author swcho
	 * @since 2015.02.27
	 * @param isCancel
	 * @param productData
	 * @return
	 * @throws CustomException
	 */
	public String getSelectionFlag(boolean isCancel, Product productData)
			throws CustomException
	{
		String samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;

		if (isCancel)
		{
			if (CommonUtil.getValue(productData.getUdfs(), "PROCESSINGINFO").equals("B"))
				samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
		}
		else	//normal case
		{
			samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
		}

		return samplingFlag;
	}

	/**
	 * create Lot cloning package
	 * @author swcho
	 * @since 2015-11-19
	 * @param eventInfo
	 * @param parentlotData
	 * @param newCarrierName
	 * @param deassignFlag
	 * @param assignCarrierUdfs
	 * @param udfs
	 * @return
	 * @throws CustomException
	 */
	public Lot createWithParentLot(EventInfo eventInfo, Lot parentlotData, String newCarrierName, boolean deassignFlag, Map<String, String> assignCarrierUdfs, Map<String, String> udfs) throws CustomException
	{
		String newLotName = "";
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(
				parentlotData.getFactoryName(), parentlotData.getProductSpecName(),GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("FACTORYNAME", productSpecData.getKey().getFactoryName());
		nameRuleAttrMap.put("PRODUCTSPECNAME", productSpecData.getKey().getProductSpecName());

		try
		{
			//20171221, kyjung
			String lotName = StringUtil.substring(parentlotData.getKey().getLotName(), 0, 8);
			List<String> argSeq = new ArrayList<String>();
			argSeq.add(lotName);
			List<String> lstName = NameServiceProxy.getNameGeneratorRuleDefService().generateName("GlassSplitLotNaming", argSeq, 1);

			newLotName = lstName.get(0);
		}
		catch(Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}

		List<ProductP> productPSequence = new ArrayList<ProductP>();
		CreateWithParentLotInfo createWithParentLotInfo = MESLotServiceProxy.getLotInfoUtil().createWithParentLotInfo(
				parentlotData.getAreaName(),
				//"Y" , assignCarrierUdfs, newCarrierName,
				deassignFlag?"N":"Y", assignCarrierUdfs, deassignFlag?"":newCarrierName,
						parentlotData.getDueDate(), parentlotData.getFactoryName(), parentlotData.getLastLoggedInTime(), parentlotData.getLastLoggedInUser(),
						parentlotData.getLastLoggedOutTime(), parentlotData.getLastLoggedOutUser(), parentlotData.getLotGrade(),
						parentlotData.getLotHoldState(), newLotName, parentlotData.getLotProcessState(), parentlotData.getLotState(),
						parentlotData.getMachineName(), parentlotData.getMachineRecipeName(), parentlotData.getNodeStack(),
						parentlotData.getOriginalLotName(), parentlotData.getPriority(),
						parentlotData.getProcessFlowName(), parentlotData.getProcessFlowVersion(), parentlotData.getProcessGroupName(),
						parentlotData.getProcessOperationName(), parentlotData.getProcessOperationVersion(), parentlotData.getProductionType(),
						new ArrayList<ProductP>(), 0, parentlotData.getProductRequestName(),
						parentlotData.getProductSpec2Name(), parentlotData.getProductSpec2Version(),
						parentlotData.getProductSpecName(), parentlotData.getProductSpecVersion(), parentlotData.getProductType(),
						// 2019.05.13_hsryu_Add Logic. ReworkFlag "" -> StringUtils.equals(parentlotData.getReworkState(), GenericServiceProxy.getConstantMap().Lot_InRework)?"Y":""
						parentlotData.getReworkCount(), StringUtils.equals(parentlotData.getReworkState(), GenericServiceProxy.getConstantMap().Lot_InRework)?"Y":"", parentlotData.getReworkNodeId(), parentlotData.getRootLotName(),
						parentlotData.getKey().getLotName(), parentlotData.getSubProductType(),
						parentlotData.getSubProductUnitQuantity1(), parentlotData.getSubProductUnitQuantity2(),
						udfs, parentlotData);

		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().createWithParentLot(eventInfo, newLotName, createWithParentLotInfo);

		return newLotData;
	}

	/**
	 * create Lot cloning package with specified ID
	 * @author swcho
	 * @since 2016-03-10
	 * @param eventInfo
	 * @param newLotName
	 * @param parentlotData
	 * @param newCarrierName
	 * @param deassignFlag
	 * @param assignCarrierUdfs
	 * @param udfs
	 * @return
	 * @throws CustomException
	 */
	public Lot createWithParentLot(EventInfo eventInfo, String newLotName, Lot parentlotData, String newCarrierName, boolean deassignFlag,
			Map<String, String> assignCarrierUdfs, Map<String, String> udfs)
					throws CustomException
	{
		List<ProductP> productPSequence = new ArrayList<ProductP>();
		CreateWithParentLotInfo createWithParentLotInfo = MESLotServiceProxy.getLotInfoUtil().createWithParentLotInfo(
				parentlotData.getAreaName(),
				//"Y" , assignCarrierUdfs, newCarrierName,
				deassignFlag?"N":"Y", assignCarrierUdfs, deassignFlag?"":newCarrierName,
						parentlotData.getDueDate(), parentlotData.getFactoryName(), parentlotData.getLastLoggedInTime(), parentlotData.getLastLoggedInUser(),
						parentlotData.getLastLoggedOutTime(), parentlotData.getLastLoggedOutUser(), parentlotData.getLotGrade(),
						parentlotData.getLotHoldState(), newLotName, parentlotData.getLotProcessState(), parentlotData.getLotState(),
						parentlotData.getMachineName(), parentlotData.getMachineRecipeName(), parentlotData.getNodeStack(),
						parentlotData.getOriginalLotName(), parentlotData.getPriority(),
						parentlotData.getProcessFlowName(), parentlotData.getProcessFlowVersion(), parentlotData.getProcessGroupName(),
						parentlotData.getProcessOperationName(), parentlotData.getProcessOperationVersion(), parentlotData.getProductionType(),
						new ArrayList<ProductP>(), 0, parentlotData.getProductRequestName(),
						parentlotData.getProductSpec2Name(), parentlotData.getProductSpec2Version(),
						parentlotData.getProductSpecName(), parentlotData.getProductSpecVersion(), parentlotData.getProductType(),
						parentlotData.getReworkCount(), "", parentlotData.getReworkNodeId(), parentlotData.getRootLotName(),
						parentlotData.getKey().getLotName(), parentlotData.getSubProductType(),
						parentlotData.getSubProductUnitQuantity1(), parentlotData.getSubProductUnitQuantity2(),
						udfs, parentlotData);

		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().createWithParentLot(eventInfo, newLotName, createWithParentLotInfo);

		return newLotData;
	}
	/**
     * TK OUT logic
     * 150205 by swcho : modified and done refactoring
     * @param eventInfo
     * @param trackOutLot
     * @param portData
     * @param carrierName
     * @param lotJudge
     * @param machineName
     * @param reworkFlag
     * @param productPGSRCSequence
     * @param assignCarrierUdfs
     * @param deassignCarrierUdfs
     * @return
     * @throws CustomException
     */
    public Lot trackOutLot(EventInfo eventInfo, Lot trackOutLot, Port portData,
            String carrierName, String lotJudge, String machineName, String reworkFlag,
            List<ProductPGSRC> productPGSRCSequence,
            Map<String, String> assignCarrierUdfs, Map<String, String> deassignCarrierUdfs, String decideSampleNodeStack, boolean aHoldFlag,String note)
                    throws CustomException
    {
        Map<String, String> trackOutLotUdfs = trackOutLot.getUdfs();
        boolean isReworkFlow = false;
        boolean isBranchFlow = false;
        boolean isReturn = false;
        boolean removeReturnNodeStack = false;
        
        String beforeProcessFlowName = trackOutLot.getProcessFlowName();
        String beforeProcessOperationName = trackOutLot.getProcessOperationName();
        String beforeReworkState = trackOutLot.getReworkState();
        String decideSampleMainFlow = "";
        String decideSampleMainOper = "";

        if(portData != null)
        {
        	trackOutLotUdfs.put("PORTNAME", portData.getKey().getPortName());
        }
        
        trackOutLotUdfs.put("BEFOREOPERATIONNAME", trackOutLot.getProcessOperationName());
        trackOutLotUdfs.put("BEFOREFLOWNAME", trackOutLot.getProcessFlowName());
        trackOutLotUdfs.put("LASTLOGGEDOUTMACHINE", trackOutLot.getMachineName());
        
        //2018.08.22 - YJYU NOTE°¡ ¸®¼ÂµÇÁö ¾Ê¾Æ HISTORY¿¡ °è¼Ó NOTE °ªÀÌ º¹Á¦ µÇ¾î ÃÊ±âÈ­
        if(note==null || StringUtils.isEmpty(note)){
        	trackOutLotUdfs.put("NOTE", "");
        }else{
			if(note.length()>3500){
				note = note.substring(0, 3499);
			}
			// 2019.06.03_hsryu_Delete Logic. Not Memory TrackOut Event. Memory New Event(UpdateProductFlag).
        	//trackOutLotUdfs.put("NOTE", note);
        }
        if(this.isDummyOperation(trackOutLot.getFactoryName(), trackOutLot.getProcessOperationName())){
        	trackOutLotUdfs.put("FILEJUDGE", "");
        }

        String nodeStack = "";

        String tempNodeStack = trackOutLot.getNodeStack();
        String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
        int count = arrNodeStack.length;
        
        Map<String, String> flowMap = getProcessFlowInfo(arrNodeStack[count-1]);

        String flowName = flowMap.get("PROCESSFLOWNAME");
        String flowVersion = flowMap.get("PROCESSFLOWVERSION");
        
        boolean checkEndFlag = checkEndOperation(flowName, flowVersion, arrNodeStack[count-1]);
        int nodeNum = 0;
        String nextNodeID = "";
        
        if(checkEndFlag)
        {
        	if((StringUtil.equals(returnFlowType(trackOutLot.getFactoryName(), flowName, flowVersion).toUpperCase(), "SAMPLING")||
        			StringUtil.equals(returnFlowType(trackOutLot.getFactoryName(), flowName, flowVersion).toUpperCase(), "REWORK"))&&count>1)
        	{
        		isReturn =true;
        		for ( int i = count-2; i >=0; i-- )
        		{
        			Map<String, String> flowMap2 = getProcessFlowInfo(arrNodeStack[i]);

        			String flowName2 = flowMap2.get("PROCESSFLOWNAME");
        			String flowVersion2 = flowMap2.get("PROCESSFLOWVERSION");

        			boolean EndFlagForBeforeFlow = checkEndOperation(flowName2, flowVersion2, arrNodeStack[i]);

        			//if not end Operation
        			if(!EndFlagForBeforeFlow)
        			{
        				nextNodeID = GetReturnAfterNodeStackForSampling(trackOutLot.getFactoryName(), arrNodeStack[i]);
        				nodeNum = i;
        				break;
        			}
        			// add by GJJ 20200512 mantis:6088
        			else {
        				if(StringUtil.equals(returnFlowType(trackOutLot.getFactoryName(), flowName2, flowVersion2).toUpperCase(), "BRANCH"))
        				{
        					nodeStack = this.getReturnNodeForBranch(trackOutLot.getKey().getLotName(),trackOutLot.getUdfs().get("ECCODE") , trackOutLot.getProductSpecName(),arrNodeStack[i]);
        	        		isBranchFlow = true;
        				}
					}        			        			
        		}
        		//complete rework and return to ReturnNodeStack in main flow.
        		if(!trackOutLot.getUdfs().get("RETURNNODESTACK").isEmpty()){
        			if(count == 2){
        				List<Map<String, Object>> beforeflowType = MESLotServiceProxy.getLotServiceUtil().getFlowTypebyNodeID(arrNodeStack[0]);
            			if(beforeflowType.get(0).get("PROCESSFLOWTYPE").equals("MAIN")){
	        			nodeStack = trackOutLot.getUdfs().get("RETURNNODESTACK");
	        			removeReturnNodeStack = true;
            			}
        			}
        		}else{
	        		if (!isBranchFlow) {// add by GJJ 20200512 mantis:6088
	        			if(nodeNum == 0)
	            		{
	            			nodeStack = nextNodeID;
	            		}
	            		else
	            		{
	            			for(int i=0; i<=nodeNum-1; i++)
	            			{
	            				nodeStack += arrNodeStack[i] + ".";
	            			}
	            			nodeStack += nextNodeID;
	            		}
					}
        		}
        		
        		this.DeleteSamplingInfo(trackOutLot,arrNodeStack[count-1],arrNodeStack[count-2],eventInfo);
        	}
        	else if(StringUtil.equals(returnFlowType(trackOutLot.getFactoryName(), flowName, flowVersion).toUpperCase(), "BRANCH")&&count>1)
        	{
        		nodeStack = CommonUtil.getReturnNodeForBranch(trackOutLot);
        		isBranchFlow = true;
        	}
        }
        
        String trackOutLotProcessFlowName ="";
        String trackOutLotProcessOperationName ="";
        String reworkNodeStack = "";
        String returnProcessFlowName = "";
        String returnProcessOperationName = "";
        String returnNodeStack ="";
        String conditionName = "";
        boolean startRework = false;

        if(lotJudge.isEmpty()){
        	lotJudge = trackOutLot.getLotGrade();
        }
        
        //ADD BY JHYING ON20200501 MANTIS:6078 start

	    String	sql = "select * from Arc WHERE FROMNODEID = REGEXP_SUBSTR(:FROMNODEID,'[^.]+',1, REGEXP_COUNT(:FROMNODEID, '[^.]+'))"
	    		+ " AND arcType = 'Normal' ";
	
	    Map<String, Object> ArcBindMap = new HashMap<String, Object>();
	    ArcBindMap.put("FROMNODEID", trackOutLot.getNodeStack());
	    List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, ArcBindMap);
		if(sqlResult.size() == 0){
			log.error("FROMNODEID:"+ trackOutLot.getNodeStack() +"IN ARC Table Is NULL!" );
		 throw new CustomException("LOT-9200", trackOutLot.getNodeStack(),trackOutLot.getProcessOperationName(),trackOutLot.getProcessFlowName());
		}

		//ADD BY JHYING ON20200501 MANTIS:6078 end
		
        //rework grade should go to rework
        MakeLoggedOutInfo makeLoggedOutInfo =
              MESLotServiceProxy.getLotInfoUtil().makeLoggedOutInfo(trackOutLot,
                      trackOutLot.getAreaName(),
                      assignCarrierUdfs,
                                                        carrierName,
                                                        //consumedMaterialSequence,
                                                        "",
                                                        deassignCarrierUdfs,
                                                        StringUtil.isEmpty(lotJudge)?trackOutLot.getLotGrade():lotJudge,
                                                        machineName,
                                                        "",//machineRecipe
                                                        nodeStack,//trackOutLot.getNodeStack(),
                                                        trackOutLotProcessFlowName,//trackOutLot.getProcessFlowName(),
                                                        "",//trackOutLot.getProcessFlowVersion(),
                                                        trackOutLotProcessOperationName,//trackOutLot.getProcessOperationName(),
                                                        "",//trackOutLot.getProcessOperationVersion(),
                                                        productPGSRCSequence,
                                                        reworkFlag,
                                                        StringUtil.equals(GenericServiceProxy.getConstantMap().Flag_Y, reworkFlag) ? reworkNodeStack : "", // reworkFlag.equals(GenericServiceProxy.getConstantMap().Flag_Y)?nodeStack:"",
                                                        trackOutLot.getUdfs());
        
        eventInfo.setEventName("TrackOut");
        Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceImpl().trackOutLot(eventInfo, trackOutLot, makeLoggedOutInfo);
        // 2019.05.29_hsryu_Move to Logic.  
//      afterTrackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(afterTrackOutLot.getKey().getLotName());
//      afterTrackOutLot.getUdfs().put("NOTE", "");
//      LotServiceProxy.getLotService().update(afterTrackOutLot);
//      afterTrackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(afterTrackOutLot.getKey().getLotName());
  
        log.info("LeadTime, WaitTime, ProcTime - EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
        
        //ADD BY BRLEE FOR CHECK AUTO REWORK
    	Map<String, Object> alterPolicy  = LotServiceImpl.commonAlterPolicy(trackOutLot, lotJudge, "Auto");
        if(!isBranchFlow && alterPolicy!=null)
		{
        	String toProcessOperation =ConvertUtil.getMapValueByName(alterPolicy,"TOPROCESSOPERATIONNAME");
        	conditionName =ConvertUtil.getMapValueByName(alterPolicy,"CONDITIONNAME");
        	
        	if(toProcessOperation.isEmpty()){
        		ProcessOperationSpec firstOperation = CommonUtil.getFirstOperation(alterPolicy.get( "FACTORYNAME" ).toString(), alterPolicy.get( "TOPROCESSFLOWNAME" ).toString());
        		reworkNodeStack = CommonUtil.getNodeStack(alterPolicy.get( "FACTORYNAME" ).toString(), alterPolicy.get( "TOPROCESSFLOWNAME" ).toString(),firstOperation.getKey().getProcessOperationName() );
        		trackOutLotProcessOperationName = firstOperation.getKey().getProcessOperationName();
        	}else{
        		reworkNodeStack = CommonUtil.getNodeStack(alterPolicy.get( "FACTORYNAME" ).toString(), alterPolicy.get( "TOPROCESSFLOWNAME" ).toString(), alterPolicy.get( "TOPROCESSOPERATIONNAME" ).toString());
        		trackOutLotProcessOperationName = alterPolicy.get( "TOPROCESSOPERATIONNAME" ).toString();
        	}
        	
        	trackOutLotProcessFlowName = alterPolicy.get( "TOPROCESSFLOWNAME" ).toString();
        	
         	returnProcessFlowName = ConvertUtil.getMapValueByName(alterPolicy,"RETURNPROCESSFLOWNAME"); 
        	returnProcessOperationName =ConvertUtil.getMapValueByName(alterPolicy,"RETURNPROCESSOPERATIONNAME");  
        	if(!returnProcessFlowName.isEmpty() && !returnProcessOperationName.isEmpty()){
        		returnNodeStack = CommonUtil.getNodeStack(alterPolicy.get( "FACTORYNAME" ).toString(), returnProcessFlowName, returnProcessOperationName);
        	}else{
        		returnNodeStack	= arrNodeStack[0];
        	}
        /*	nodeStack = "";
        	String tempNodeStack1="";
        	
			if(StringUtil.isNotEmpty(reworkNodeStack))
			{
				String nextNodeStack = getNextNodeStack(trackOutLot.getProcessFlowName(), trackOutLot.getProcessFlowVersion(), arrNodeStack[count-1]);
	
				for ( int j = 0; j < arrNodeStack.length-1; j++ )
				{
					tempNodeStack1 = tempNodeStack1 + arrNodeStack[j] + ".";
				}
				tempNodeStack1 = tempNodeStack1+nextNodeStack + ".";
				nodeStack = tempNodeStack1 + reworkNodeStack;
			}
			*/
        	if(conditionName.equals("Rework")){
        		reworkFlag="Y";
        		startRework = true;
        	}
		}
        
//		String LeadTime = "";// modfiy by GJJ 20200501 mantis£º5487
//		String WaitTime = "";// modfiy by GJJ 20200501 mantis£º5487
		String ProcTime = "";
		
//		// TK-OUT - IF(BEFORE TK-OUT NULL THEN Release TIME ELSE BEFORE TK-OUT END)  
//		String strSql_LeadTime = 
//				"    WITH LOT_LIST AS (" +
//					"                   SELECT ROWNUM NO, LAG(LASTLOGGEDOUTTIME) OVER(ORDER BY TIMEKEY ) BEFORT , LASTLOGGEDOUTTIME ,LOTNAME  " +					
//				" 			         FROM LOTHISTORY  " +
//				" 			        WHERE LOTNAME = :LOTNAME " +
//				" 			         ORDER BY TIMEKEY )			" +
//				" 			SELECT TO_CHAR(ROUND((LASTLOGGEDOUTTIME - BEFORT) * 24 *60 *60,2))  AS LEADTIME, BEFORT, LASTLOGGEDOUTTIME " +
//				" 			FROM LOT_LIST " +
//				" 			WHERE NO IN ( SELECT MAX(NO) NO FROM LOT_LIST ) " ;		
//									 
//		Map<String, Object> bindMap_LeadTime = new HashMap<String, Object>();
//		bindMap_LeadTime.put("LOTNAME", afterTrackOutLot.getKey().getLotName());
		
		//2019.10.11 dmlee (Request By CIM)
//		List<Map<String, Object>> List_LeadTime = null;
//		//List<Map<String, Object>> List_LeadTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_LeadTime, bindMap_LeadTime);
//
//		//BEFORE TK-OUT - TK-IN
//		String strSql_WaitTime = 
//				" WITH LOT_TKIN AS (      "+    
//				"                   SELECT LASTLOGGEDINTIME EVENTTIME ,LOTNAME  " +					
//				" 			         FROM LOT  " +
//				"      				WHERE  LOTNAME = :LOTNAME ) " +
//				"       ,LOT_TKOUT AS (  "+
//				"                   SELECT ROWNUM NO, LAG(LASTLOGGEDOUTTIME) OVER(ORDER BY TIMEKEY ) EVENTTIME ,LOTNAME  " +					
//				" 			         FROM LOTHISTORY A " +
//				" 			        WHERE LOTNAME = :LOTNAME " +
//				" 			         ORDER BY TIMEKEY ) " +
//				"      SELECT TO_CHAR(ROUND((A.EVENTTIME - B.EVENTTIME ) * 24 *60 *60,2)) WAITTIME  "+
//				"      FROM  LOT_TKIN A "+
//				"        INNER JOIN LOT_TKOUT B ON A.LOTNAME = B.LOTNAME "+
//				"      WHERE 1=1  "+
//				"         AND B.NO IN (SELECT MAX(NO) NO FROM LOT_TKOUT) " ;	 ;
// 
//		Map<String, Object> bindMap_WaitTime = new HashMap<String, Object>();
//		bindMap_WaitTime.put("LOTNAME", afterTrackOutLot.getKey().getLotName());
		
		//2019.10.11 dmlee (Request By CIM)
//		List<Map<String, Object>> List_WaitTime = null;
//		//List<Map<String, Object>> List_WaitTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_WaitTime, bindMap_WaitTime);
//						
//		//TK-OUT - TK-IN
//		String strSql_ProcTime = 	 
//				"      SELECT  TO_CHAR(ROUND((LASTLOGGEDOUTTIME - LASTLOGGEDINTIME ) * 24 *60 *60,2)) PROCTIME  "+
//				"       FROM LOT  "+
//				"      WHERE  LOTNAME = :LOTNAME  ";
//
//		Map<String, Object> bindMap_ProcTime = new HashMap<String, Object>();
//		bindMap_ProcTime.put("LOTNAME", afterTrackOutLot.getKey().getLotName());
//		
//		//2019.10.11 dmlee (Request By CIM)
//		List<Map<String, Object>> List_ProcTime = null;
//		//List<Map<String, Object>> List_ProcTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_ProcTime, bindMap_ProcTime);
		
		
		//2019.10.11 dmlee (Request By CIM)
		/*
		if(List_LeadTime != null && List_LeadTime.size() > 0)
		{	
			LeadTime = List_LeadTime.get(0).get("LEADTIME").toString();
		}
		
		if(List_WaitTime != null && List_WaitTime.size() > 0)
		{
			WaitTime = List_WaitTime.get(0).get("WAITTIME").toString();
		}
		
		if(List_ProcTime != null && List_ProcTime.size() > 0)
		{
			ProcTime = List_ProcTime.get(0).get("PROCTIME").toString();
		}
		*/
		
		log.info("Update LotData - EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        afterTrackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(afterTrackOutLot.getKey().getLotName());				
        afterTrackOutLot = LotServiceProxy.getLotService().selectByKeyForUpdate(afterTrackOutLot.getKey());
        
     // modfiy by GJJ 20200501  start  mantis£º5487
        ProcessFlow beProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(
        		new ProcessFlowKey(trackOutLot.getFactoryName(), trackOutLot.getProcessFlowName(), "00001")
        		);
        
        if (!beProcessFlowData.getProcessFlowType().equalsIgnoreCase(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SORT)
        		&& !beProcessFlowData.getProcessFlowType().equalsIgnoreCase(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING ) 
        		&& !trackOutLot.getProcessOperationName().equalsIgnoreCase("DUMMYFINSH") )
        {
			
        	ProcTime = afterTrackOutLot.getLastEventTimeKey();
		}
        
     // modfiy by GJJ 20200501  end  mantis£º5487
        
        
        /************* 2019.05.29_hsryu_Add Logic. Move to Logic And Sum Update Logic ****************/
        if( StringUtil.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed)){
        	// 2019.08.27 Park Jeong Su Mantis 4302
        	if(StringUtil.equals(returnFlowType(afterTrackOutLot.getFactoryName(), beforeProcessFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION).toUpperCase(), "MQC")){
        		afterTrackOutLot.setNodeStack("111111111111111");
        	}
        	// 2019.08.27 Park Jeong Su Mantis 4302
        	afterTrackOutLot.setProcessOperationName("-");
        	afterTrackOutLot.setProcessOperationVersion("");
        }
        // 2019.05.29_hsryu_together MachineName, Note, PortName, LeadTime, WaitTime, ProcTime Update. 
        afterTrackOutLot.setMachineName("");
        afterTrackOutLot.getUdfs().put("NOTE", "");
        afterTrackOutLot.getUdfs().put("PORTNAME", "");
//        afterTrackOutLot.getUdfs().put("LEADTIME", LeadTime);modfiy by GJJ 20200501 mantis:
//        afterTrackOutLot.getUdfs().put("WAITTIME", WaitTime);
        if(!ProcTime.isEmpty())
        	afterTrackOutLot.getUdfs().put("PROCTIME", ProcTime);// modfiy by GJJ 20200501  end  mantis£º5487
        
        if(removeReturnNodeStack && !startRework){
        	 afterTrackOutLot.getUdfs().put("RETURNNODESTACK", "");
        }
        
        LotServiceProxy.getLotService().update(afterTrackOutLot);

        LotHistoryKey LotHistoryKey = new LotHistoryKey();
        LotHistoryKey.setLotName(afterTrackOutLot.getKey().getLotName());
        LotHistoryKey.setTimeKey(afterTrackOutLot.getLastEventTimeKey());
        
        // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
        LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(LotHistoryKey);
        
        if( StringUtil.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed)){
        	// 2019.08.27 Park Jeong Su Mantis 4302
        	if(StringUtil.equals(returnFlowType(afterTrackOutLot.getFactoryName(), beforeProcessFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION).toUpperCase(), "MQC")){
        		LotHistory.setNodeStack("111111111111111");
        	}
        	// 2019.08.27 Park Jeong Su Mantis 4302
		    LotHistory.setProcessOperationName("-");
		    LotHistory.setProcessOperationVersion("");
        }
        
	    // 2019.05.29_hsryu_together LeadTime,WaitTime,ProcTime Update. 
//	    LotHistory.getUdfs().put("LEADTIME", LeadTime);// modfiy by GJJ 20200501 mantis£º5487
//	    LotHistory.getUdfs().put("WAITTIME", WaitTime);// modfiy by GJJ 20200501 mantis£º5487
        if(!ProcTime.isEmpty())
        	LotHistory.getUdfs().put("PROCTIME", ProcTime);// modfiy by GJJ 20200501 mantis£º5487
        
        if(removeReturnNodeStack && !startRework){
        	LotHistory.getUdfs().put("RETURNNODESTACK", "");
       }
		LotServiceProxy.getLotHistoryService().update(LotHistory);
		
        //add brlee for auto rework
        if(startRework){
        	List<ProductRU> productRUdfs = new ArrayList<ProductRU>();
        	List<Map<String, Object>> flowType = MESLotServiceProxy.getLotServiceUtil().getFlowTypebyNodeID(arrNodeStack[count-1]);
        	if(flowType.get(0).get("PROCESSFLOWTYPE").equals("MAIN")){
        		afterTrackOutLot.getUdfs().put("RETURNNODESTACK", returnNodeStack);
			}
        	
        	MakeInReworkInfo makeInReworkInfo =  MESLotServiceProxy.getLotInfoUtil().makeInReworkInfo(afterTrackOutLot, 
						eventInfo, 
						afterTrackOutLot.getKey().getLotName(), 
						trackOutLotProcessFlowName, 
						trackOutLotProcessOperationName, 
						returnProcessFlowName, 
						returnProcessOperationName,
						afterTrackOutLot.getUdfs(), 
						productRUdfs,
						afterTrackOutLot.getNodeStack());

        		MESLotServiceProxy.getLotServiceImpl().startRework(eventInfo, afterTrackOutLot, makeInReworkInfo);
			
        	 /*	 SetEventInfo setEventInfo = new SetEventInfo();
        	 eventInfo.setEventName("StartRework");
        		List<Map<String, Object>> flowType = MESLotServiceProxy.getLotServiceUtil().getFlowTypebyNodeID(arrNodeStack[count-1]);
    			if(flowType.get(0).get("PROCESSFLOWTYPE").equals("MAIN")){
    				 setEventInfo.getUdfs().put("RETURNNODESTACK", returnNodeStack);
    			}
        	 setEventInfo.setProductUSequence(productU);
        	 setEventInfo.setProductQuantity(trackOutLot.getProductQuantity());
             LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo, setEventInfo);   */
             log.info("Update lotData - EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
        }  
        eventInfo.setEventName("TrackOut");
        /************************************************************************************************/
		
		afterTrackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(afterTrackOutLot.getKey().getLotName());
		
		/************* 2019.05.29_hsryu_Add Logic. Move to Logic And Sum Update Logic ****************/
        for(int i=0; i<productPGSRCSequence.size(); i++)
        {
        	eventInfo.setEventName("TrackOut");
    		log.info("Update ProductData - EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
    		
    		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        	Product product = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productPGSRCSequence.get(i).getProductName());
        	Product product = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productPGSRCSequence.get(i).getProductName()));
        	
	        if( StringUtil.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed)){
	        	// 2019.08.27 Park Jeong Su Mantis 4302
	        	if(StringUtil.equals(returnFlowType(afterTrackOutLot.getFactoryName(), beforeProcessFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION).toUpperCase(), "MQC")){
	        		product.setNodeStack("111111111111111");
	        	}
	        	// 2019.08.27 Park Jeong Su Mantis 4302
	        	product.setProcessOperationName("-");
	        	product.setProcessOperationVersion("");
	        }
	        
	        product.setMachineName("");
			product.getUdfs().put("PORTNAME", "");
			product.getUdfs().put("NOTE", "");
//			product.getUdfs().put("LEADTIME", LeadTime);// modfiy by GJJ 20200501 mantis£º5487
//			product.getUdfs().put("WAITTIME", WaitTime);// modfiy by GJJ 20200501 mantis£º5487
			if(!ProcTime.isEmpty())
				product.getUdfs().put("PROCTIME", ProcTime);// modfiy by GJJ 20200501 mantis£º5487
			
		    if(removeReturnNodeStack && !startRework){
		    	product.getUdfs().put("RETURNNODESTACK", "");
	       }
			
	        ProductServiceProxy.getProductService().update(product);
	        
			ProductHistoryKey productHistoryKey = new ProductHistoryKey();
            productHistoryKey.setProductName(product.getKey().getProductName());
            productHistoryKey.setTimeKey(product.getLastEventTimeKey());

            // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
            
            if( StringUtil.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed)){
	        	// 2019.08.27 Park Jeong Su Mantis 4302
	        	if(StringUtil.equals(returnFlowType(afterTrackOutLot.getFactoryName(), beforeProcessFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION).toUpperCase(), "MQC")){
	        		productHistory.setNodeStack("111111111111111");
	        	}
	        	// 2019.08.27 Park Jeong Su Mantis 4302
            	productHistory.setProcessOperationName("-");
            	productHistory.setProcessOperationVersion("");
            }
            
//			productHistory.getUdfs().put("LEADTIME", LeadTime);// modfiy by GJJ 20200501 mantis£º5487
//			productHistory.getUdfs().put("WAITTIME", WaitTime);// modfiy by GJJ 20200501 mantis£º5487
            if(!ProcTime.isEmpty())
            	productHistory.getUdfs().put("PROCTIME", ProcTime);
            
            if(removeReturnNodeStack && !startRework){
            	productHistory.getUdfs().put("RETURNNODESTACK", "");
	       }
			
	        ProductServiceProxy.getProductHistoryService().update(productHistory);
/*	        //ADD auto rework by brlee
	        if(startRework){
	        	kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo_P = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
	        	 eventInfo.setEventName("StartRework");
	        	 setEventInfo_P.getUdfs().put("RETURNNODESTACK", returnNodeStack);
	        	 ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setEventInfo_P);
	        	 log.info("Update ProductData - EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
		     }
	        eventInfo.setEventName("TrackOut");*/
        }
        /************************************************************************************************/

		FileJudgeSetting fileJudgeSetting=null;
        try {
        	Object[] obj = new Object[]{trackOutLot.getFactoryName(),trackOutLot.getProcessFlowName(),trackOutLot.getProcessFlowVersion(),trackOutLot.getProcessOperationName(),trackOutLot.getProcessOperationVersion()};
            fileJudgeSetting = ExtendedObjectProxy.getFileJudgeSettingService().selectByKey(false, obj);	
		} catch (Exception e) {
			fileJudgeSetting=null;
		}

        if(fileJudgeSetting!=null && StringUtil.equals("Y", fileJudgeSetting.getGradeDataFlag()))
        {
            /* Check Next Operation */
            ProcessOperationSpec afterTrackOutProcessOperationSpec = CommonUtil.getProcessOperationSpec(afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProcessOperationName());
            if(StringUtil.equals(afterTrackOutProcessOperationSpec.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_DUMMY))
            {
                eventInfo.setEventName("Hold");
                eventInfo.setEventComment(String.format("ReviewStation Wait Hold,Please Call INT!", afterTrackOutLot.getKey().getLotName()));
                /* 20190426, hhlee, modify, add variable(setFutureHold) */
                //this.doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","GHLD","", true, "", "SYS");

                this.doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","GHLD","", true, false, "", "SYS");

                /* <<== 20190424, hhlee, modify, changed function */

                afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().getLotData(afterTrackOutLot.getKey().getLotName());
                aHoldFlag = true;
            }            
        }
        
       

        // Not AHold, Not BranchFlow, Not Completed, Check Sampling.
        if(!aHoldFlag& !startRework)
        {
        	nodeStack = "";
            String sampleFlowName = "";
            String sampleNodeStack = decideSampleNodeStack;
        	if(!isBranchFlow)
        	{
        		if(!StringUtils.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
        		{
        			try
        			{
        				if(!StringUtil.isEmpty(decideSampleNodeStack) || !StringUtil.equals("", decideSampleNodeStack) )
        				{
        					nodeStack = trackOutLot.getNodeStack() + "." + decideSampleNodeStack;
        					decideSampleMainFlow = beforeProcessFlowName;
        					decideSampleMainOper = beforeProcessOperationName;
        				}
        				else
        				{
        					if (StringUtil.indexOf(tempNodeStack, ".") > -1)
        					{
        						for ( int i = 1; i <= count; i++ )
        						{
        							Map<String, String> flowMap2 = getProcessFlowInfo(arrNodeStack[count-i]);

        							String flowName2 = flowMap2.get("PROCESSFLOWNAME");
        							String flowVersion2 = flowMap2.get("PROCESSFLOWVERSION");

        							boolean endFlag = checkEndOperation(flowName2, flowVersion2, arrNodeStack[count-i]);

        							if ( endFlag == true )
        							{
        								if(StringUtil.equals(returnFlowType(trackOutLot.getFactoryName(), flowName2, flowVersion2).toUpperCase(), "REWORK"))
        								{
        									isReworkFlow = true;
        								}

        								if((count-i-1)>=0)
        								{
        									Map<String, String> flowMap3 = getProcessFlowInfo(arrNodeStack[count-i-1]);

        									String flowName3 = flowMap3.get("PROCESSFLOWNAME");
        									String flowVersion3 = flowMap3.get("PROCESSFLOWVERSION");
        									String operationName3 = flowMap3.get("PROCESSOPERATIONNAME");

        									sampleFlowName = checkReserveInfo(trackOutLot.getKey().getLotName(), trackOutLot.getProductSpecName(), trackOutLot.getUdfs().get("ECCODE"), flowName3, flowVersion3, operationName3, arrNodeStack[count-i-1], eventInfo);

        									if ( StringUtil.isEmpty(sampleFlowName))
        									{
        										if( i != count )
        										{
        											continue;
        										}
        									}
        									else
        									{
        										sampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getOperFirstNodeStack(sampleFlowName);
        										String tempNodeStack1 = "";

        										if(!StringUtils.isNotEmpty(decideSampleMainFlow)&&!StringUtils.isNotEmpty(decideSampleMainOper))
        										{
        											decideSampleMainFlow = flowName3;
        											decideSampleMainOper = operationName3;
        										}

        										for ( int j = 0; j < count-i; j++ )
        										{
        											tempNodeStack1 = tempNodeStack1 + arrNodeStack[j] + ".";
        										}
        										nodeStack = tempNodeStack1 + sampleNodeStack;
        										break;
        									}
        								}
        								else
        								{
        									nodeStack = "";
        								}
        							}
        							else
        							{
        								if ( i == count )
        								{
        									nodeStack = getNextNodeStack(flowName2, flowVersion2, arrNodeStack[count-i]);
        								} 
        								else
        								{
        									nodeStack = "";
        								}
        								break;
        							}
        						}
        					}
        					else
        					{
        						nodeStack = "";
        					}
        				}

        				if(StringUtil.isNotEmpty(sampleNodeStack))
        				{
        					String originalEventUser = eventInfo.getEventUser();

        					eventInfo.setEventName("Sampling");

        					String[] NodeStackArr = StringUtil.split(nodeStack, ".");
        					String beforeNode = "";
        					int nodeCount = NodeStackArr.length;

        					Map<String, String> sampleFlowMap = getProcessFlowInfo(NodeStackArr[nodeCount-1]);

        					String sampleFlow = sampleFlowMap.get("PROCESSFLOWNAME");
        					String sampleFlowVersion = sampleFlowMap.get("PROCESSFLOWVERSION");
        					String sampleOperationName = sampleFlowMap.get("PROCESSOPERATIONNAME");

        					Map<String, String> beforeFlowMap = getProcessFlowInfo(NodeStackArr[nodeCount-2]);

        					for(int i=0; i<nodeCount-1; i++)
        					{
        						beforeNode += NodeStackArr[i];

        						if(i!=nodeCount-2)
        						{
        							beforeNode += ".";
        						}
        					}

        					boolean checkEventInfo = false;

        					for ( int i = count-1; i >=0; i-- )
        					{
        						Map<String, String> flow = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[i]);

        						String decideFlowName = flow.get("PROCESSFLOWNAME");
        						String decideFlowVersion = flow.get("PROCESSFLOWVERSION");
        						String decideOperationName = flow.get("PROCESSOPERATIONNAME");

        						checkEventInfo = MESLotServiceProxy.getLotServiceUtil().setEventInfoForSampling(afterTrackOutLot,decideFlowName,decideFlowVersion,decideOperationName,sampleFlow,sampleFlowVersion,eventInfo);

        						if(checkEventInfo)
        						{
        							break;
        						}
        					}
        				

        					List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(afterTrackOutLot.getKey().getLotName());

        					//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
        					ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfoForSampling(afterTrackOutLot.getKey().getLotName(),
        							afterTrackOutLot.getProductionType(), afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProductSpec2Name(), afterTrackOutLot.getProductSpec2Version(),
        							"", afterTrackOutLot.getSubProductUnitQuantity1(), afterTrackOutLot.getSubProductQuantity2(), afterTrackOutLot.getDueDate(), afterTrackOutLot.getPriority(),
        							afterTrackOutLot.getFactoryName(), afterTrackOutLot.getAreaName(), afterTrackOutLot.getLotState(), afterTrackOutLot.getLotProcessState(), afterTrackOutLot.getLotHoldState(),
        							afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(), afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion(),
        							sampleFlow, sampleOperationName, "", "", beforeNode,
        							afterTrackOutLot.getUdfs(), productUdfs,
        							true, true);

        					afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, afterTrackOutLot, changeSpecInfo);

        					eventInfo.setEventUser(originalEventUser);

        					// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
        					Map<String, String> updateUdfs = new HashMap<String, String>();
        					updateUdfs.put("NOTE", "");
        					MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(afterTrackOutLot, updateUdfs);
        					
        					//2019.07.05 dmlee : Record CT_LOTACTION Same timeKey LotHistory 'Sampling' Event
        					try
        					{
        						
        						List<LotAction> lotActionDataList = ExtendedObjectProxy.getLotActionService().select("WHERE LOTNAME = ? AND ACTIONSTATE = 'Created' AND ACTIONNAME = 'Sampling' AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? ORDER BY POSITION DESC ", new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(), beforeProcessFlowName, "00001", beforeProcessOperationName, "00001"});
        						
        						LotAction lotActionData = lotActionDataList.get(0);
        						
        						ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotActionData);
        						
        					}
        					catch(Exception ex)
        					{
        						log.info("CT_LOTACTION Data History Record Fail !");
        					}
        					
        					
        					
        					//2019.07.05 dmlee : Record CT_SAMPLELOT Same timekey LotHistory 'Sampling' Event
        					try
        					{
            					SampleLot sampleLotData = ExtendedObjectProxy.getSampleLotService().selectByKey(false, new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getUdfs().get("ECCODE"), beforeProcessFlowName, "00001",
            							beforeProcessOperationName, "00001", machineName, sampleFlow, "00001", beforeProcessOperationName, "00001"});
            					
            					ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotData);	
        					}
        					catch(Exception ex)
        					{
        						log.info("CT_SAMPLELOT Data History Record Fail !");
        					}
        				}
        			}
        			catch ( Throwable e)
        			{
        				log.error("decideSampleNodeStack Fail!");
        			}
        		}
        	}
        	else {//add by GJJ 20200512 mantis:6088
        		if(!StringUtils.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
        		{
        			try
        			{
        				if(!StringUtil.isEmpty(decideSampleNodeStack) || !StringUtil.equals("", decideSampleNodeStack) )
        				{
        					nodeStack = trackOutLot.getNodeStack() + "." + decideSampleNodeStack;
        					decideSampleMainFlow = beforeProcessFlowName;
        					decideSampleMainOper = beforeProcessOperationName;
        				}
        				else
        				{
        					if (StringUtil.indexOf(tempNodeStack, ".") > -1)
        					{
        						for ( int i = 1; i < count; i++ )
        						{
        							Map<String, String> flowMap2 = getProcessFlowInfo(arrNodeStack[count-i]);

        							String flowName2 = flowMap2.get("PROCESSFLOWNAME");
        							String flowVersion2 = flowMap2.get("PROCESSFLOWVERSION");

        							boolean endFlag = checkEndOperation(flowName2, flowVersion2, arrNodeStack[count-i]);

        							if ( endFlag == true )
        							{
        								if(StringUtil.equals(returnFlowType(trackOutLot.getFactoryName(), flowName2, flowVersion2).toUpperCase(), "REWORK"))
        								{
        									isReworkFlow = true;
        								}

        								if((count-i-1)>=0)
        								{
        									Map<String, String> flowMap3 = getProcessFlowInfo(arrNodeStack[count-i-1]);

        									String flowName3 = flowMap3.get("PROCESSFLOWNAME");
        									String flowVersion3 = flowMap3.get("PROCESSFLOWVERSION");
        									String operationName3 = flowMap3.get("PROCESSOPERATIONNAME");

        									sampleFlowName = checkReserveInfo(trackOutLot.getKey().getLotName(), trackOutLot.getProductSpecName(), trackOutLot.getUdfs().get("ECCODE"), flowName3, flowVersion3, operationName3, arrNodeStack[count-i-1], eventInfo);

        									if ( StringUtil.isEmpty(sampleFlowName))
        									{
        										if( i != count )
        										{
        											continue;
        										}
        									}
        									else
        									{
        										sampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getOperFirstNodeStack(sampleFlowName);
        										String tempNodeStack1 = "";

        										if(!StringUtils.isNotEmpty(decideSampleMainFlow)&&!StringUtils.isNotEmpty(decideSampleMainOper))
        										{
        											decideSampleMainFlow = flowName3;
        											decideSampleMainOper = operationName3;
        										}

        										for ( int j = 0; j < count-i; j++ )
        										{
        											tempNodeStack1 = tempNodeStack1 + arrNodeStack[j] + ".";
        										}
        										nodeStack = tempNodeStack1 + sampleNodeStack;
        										break;
        									}
        								}
        								else
        								{
        									nodeStack = "";
        								}
        							}
        							else
        							{
        								if ( i == count )
        								{
        									nodeStack = getNextNodeStack(flowName2, flowVersion2, arrNodeStack[count-i]);
        								} 
        								else
        								{
        									nodeStack = "";
        								}
        								break;
        							}
        						}
        					}
        					else
        					{
        						nodeStack = "";
        					}
        				}

        				if(StringUtil.isNotEmpty(sampleNodeStack))
        				{
        					String originalEventUser = eventInfo.getEventUser();

        					eventInfo.setEventName("Sampling");

        					String[] NodeStackArr = StringUtil.split(nodeStack, ".");
        					String beforeNode = "";
        					int nodeCount = NodeStackArr.length;

        					Map<String, String> sampleFlowMap = getProcessFlowInfo(NodeStackArr[nodeCount-1]);

        					String sampleFlow = sampleFlowMap.get("PROCESSFLOWNAME");
        					String sampleFlowVersion = sampleFlowMap.get("PROCESSFLOWVERSION");
        					String sampleOperationName = sampleFlowMap.get("PROCESSOPERATIONNAME");

        					Map<String, String> beforeFlowMap = getProcessFlowInfo(NodeStackArr[nodeCount-2]);

        					for(int i=0; i<nodeCount-1; i++)
        					{
        						beforeNode += NodeStackArr[i];

        						if(i!=nodeCount-2)
        						{
        							beforeNode += ".";
        						}
        					}

        					boolean checkEventInfo = false;

        					for ( int i = count-1; i >=0; i-- )
        					{
        						Map<String, String> flow = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[i]);

        						String decideFlowName = flow.get("PROCESSFLOWNAME");
        						String decideFlowVersion = flow.get("PROCESSFLOWVERSION");
        						String decideOperationName = flow.get("PROCESSOPERATIONNAME");

        						checkEventInfo = MESLotServiceProxy.getLotServiceUtil().setEventInfoForSampling(afterTrackOutLot,decideFlowName,decideFlowVersion,decideOperationName,sampleFlow,sampleFlowVersion,eventInfo);

        						if(checkEventInfo)
        						{
        							break;
        						}
        					}
        				

        					List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(afterTrackOutLot.getKey().getLotName());

        					//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
        					ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfoForSampling(afterTrackOutLot.getKey().getLotName(),
        							afterTrackOutLot.getProductionType(), afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProductSpec2Name(), afterTrackOutLot.getProductSpec2Version(),
        							"", afterTrackOutLot.getSubProductUnitQuantity1(), afterTrackOutLot.getSubProductQuantity2(), afterTrackOutLot.getDueDate(), afterTrackOutLot.getPriority(),
        							afterTrackOutLot.getFactoryName(), afterTrackOutLot.getAreaName(), afterTrackOutLot.getLotState(), afterTrackOutLot.getLotProcessState(), afterTrackOutLot.getLotHoldState(),
        							afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(), afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion(),
        							sampleFlow, sampleOperationName, "", "", beforeNode,
        							afterTrackOutLot.getUdfs(), productUdfs,
        							true, true);

        					afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, afterTrackOutLot, changeSpecInfo);

        					eventInfo.setEventUser(originalEventUser);

        					// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
        					Map<String, String> updateUdfs = new HashMap<String, String>();
        					updateUdfs.put("NOTE", "");
        					MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(afterTrackOutLot, updateUdfs);
        					
        					//2019.07.05 dmlee : Record CT_LOTACTION Same timeKey LotHistory 'Sampling' Event
        					try
        					{
        						
        						List<LotAction> lotActionDataList = ExtendedObjectProxy.getLotActionService().select("WHERE LOTNAME = ? AND ACTIONSTATE = 'Created' AND ACTIONNAME = 'Sampling' AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? ORDER BY POSITION DESC ", new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(), beforeProcessFlowName, "00001", beforeProcessOperationName, "00001"});
        						
        						LotAction lotActionData = lotActionDataList.get(0);
        						
        						ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotActionData);
        						
        					}
        					catch(Exception ex)
        					{
        						log.info("CT_LOTACTION Data History Record Fail !");
        					}
        					
        					
        					
        					//2019.07.05 dmlee : Record CT_SAMPLELOT Same timekey LotHistory 'Sampling' Event
        					try
        					{
            					SampleLot sampleLotData = ExtendedObjectProxy.getSampleLotService().selectByKey(false, new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getUdfs().get("ECCODE"), beforeProcessFlowName, "00001",
            							beforeProcessOperationName, "00001", machineName, sampleFlow, "00001", beforeProcessOperationName, "00001"});
            					
            					ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotData);	
        					}
        					catch(Exception ex)
        					{
        						log.info("CT_SAMPLELOT Data History Record Fail !");
        					}
        				}
        			}
        			catch ( Throwable e)
        			{
        				log.error("decideSampleNodeStack Fail!");
        			}
        		}
			}
        }
        
        // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    	afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().getLotData(afterTrackOutLot.getKey().getLotName());
    	afterTrackOutLot = LotServiceProxy.getLotService().selectByKeyForUpdate(afterTrackOutLot.getKey());
    	
       if(isReturn && !StringUtils.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
        {
        	if(StringUtils.equals(beforeReworkState, GenericServiceProxy.getConstantMap().Lot_InRework))
        		afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().endReworkState(eventInfo, afterTrackOutLot);
        }

        // 2019.05.29_hsryu_if LotState is Completed, Only AHold is allowed. Mantis 0004057.
//    	if(!StringUtils.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
//    	{
        	if(aHoldFlag)
        	{
        		afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionBySystemHold(afterTrackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName, eventInfo);
        		afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionHold(afterTrackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName, eventInfo, GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
        	}
//        	else
//        	{
        		// 2019.05.30_hsryu_Move to Logic. OutSide TrackOutLot Function. 
//        		// 2019.05.29_hsryu_Add Condition. if LotState is Completed, Sampling & BHold is not allowed.
//        		if(!StringUtils.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed)){
//        			// 2019.05.30_hsryu_Delete Logic. if go to SampleFlow, check BHold about SamplingFlow.
////            		if(StringUtil.isEmpty(decideSampleNodeStack)&&StringUtil.isEmpty(sampleNodeStack))
////            		{
//            			if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(afterTrackOutLot.getKey().getLotName(), eventInfo))
//            			{
//            				//Reserve Change
//            				afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(afterTrackOutLot.getKey().getLotName(), eventInfo);
//            			}
////            		}
//        		}
//        	}
//    	}
        
        return afterTrackOutLot;
    }
    
	public boolean checkReworkState(Lot lotData)
	{
		boolean checkRework = false;
		
		String tempNodeStack = lotData.getNodeStack();
		String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
		int count = arrNodeStack.length;
		
		for(int i=0; i<arrNodeStack.length;i++)
		{
			Map<String, String> flowMap = getProcessFlowInfo(arrNodeStack[i]);

			String flowName = flowMap.get("PROCESSFLOWNAME");
			String flowVersion = flowMap.get("PROCESSFLOWVERSION");
			
			if(StringUtil.equals(returnFlowType(lotData.getFactoryName(), flowName, flowVersion).toUpperCase(), "REWORK"))
			{
				checkRework = true;
				return checkRework;
			}
		}
		
		return checkRework;
	}
	
	public Lot executeSampleLot(Lot lotData)
	{
    	EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeSampleState", "MESSystem", "Execute SampleLot", "", "");
		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		SampleLot sampleLot = new SampleLot();

		List<CorresSampleLot> corresSampleLotList = new ArrayList<CorresSampleLot>();
		CorresSampleLot corresSampleLot = new CorresSampleLot();
 
		String tempNodeStack = lotData.getNodeStack();
		String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
		int count = arrNodeStack.length;

		Map<String, String> flowMap = getProcessFlowInfo(arrNodeStack[count-1]);

		String flowName = flowMap.get("PROCESSFLOWNAME");
		String flowVersion = flowMap.get("PROCESSFLOWVERSION");

		if(StringUtil.equals(returnFlowType(lotData.getFactoryName(), flowName, flowVersion), "Sampling"))
		{
			if(count>1)
			{
				Map<String, String> beforeFlowMap = getProcessFlowInfo(arrNodeStack[count-2]);

				String beforeFlowName = beforeFlowMap.get("PROCESSFLOWNAME");
				String beforeFlowVersion = beforeFlowMap.get("PROCESSFLOWVERSION");
				String beforeOperationName = beforeFlowMap.get("PROCESSOPERATIONNAME");

				//SystemSample Completing..
				try
				{
					try {
						sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? "
								+ "and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? "
								+ "and fromProcessOperationName = ? and (fromProcessOperationVersion = ? or fromProcessOperationVersion = ?) "
								+ "and sampleProcessFlowName = ? and (sampleProcessFlowVersion = ? or sampleProcessFlowVersion = ?) "
								+ "and sampleState = ? ", 
								new Object[] {lotData.getKey().getLotName(),
								lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), beforeFlowName, beforeFlowVersion,
								beforeOperationName, "00001", "*", flowName, flowVersion, "*", "Decided"});
					} catch (Exception e) {
						log.info(e);
					}

					if ( sampleLotList != null && sampleLotList.size() > 0 )
					{
						for( int i=0; i < sampleLotList.size(); i++) {
							sampleLot = sampleLotList.get(i);
							
							if(StringUtil.equals(sampleLot.getSampleState(), "Decided"))
							{
								sampleLot.setSampleState("Executing");
								sampleLot.setLastEventName(eventInfo.getEventName());
								//2019.02.27_hsryu_Mantis 0002723. if SampleOutHold, remain EventUser . 
								//sampleLot.setLastEventUser(eventInfo.getEventUser());
								sampleLot.setLastEventComment(eventInfo.getEventComment());
								sampleLot.setLastEventTime(eventInfo.getEventTime());
								sampleLot.setLastEventTimekey(eventInfo.getEventTimeKey());
								sampleLot = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLot);
							}
						}
					}
					else{
						log.info("not systemSample currently..");
					}
				}
				catch (Throwable e)
				{
					log.error("Update SampleLot Failed");
				}

				//corresSample Executing..
				try
				{
					corresSampleLotList = ExtendedObjectProxy.getCorresSampleLotService().select(" lotName = ? and factoryName = ? "
							+ "and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? "
							+ "and fromProcessOperationName = ? and fromProcessOperationVersion = ? and sampleProcessFlowName = ? "
							+ "and sampleProcessFlowVersion = ? and sampleState = ? ",
							new Object[] {lotData.getKey().getLotName(),
							lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), beforeFlowName, beforeFlowVersion,
							beforeOperationName, "00001", flowName, flowVersion, "Decided"});
					
					if ( corresSampleLotList.size() > 0 ) {
						for( int i=0; i < corresSampleLotList.size(); i++) {
							corresSampleLot = corresSampleLotList.get(i);
							
							if(StringUtil.equals(corresSampleLot.getSampleState(), "Decided"))
							{
								corresSampleLot.setSampleState("Executing");
								corresSampleLot.setLastEventName(eventInfo.getEventName());
								////2019.02.27_hsryu_Mantis 0002723. if SampleOutHold, remain EventUser . 
								//corresSampleLot.setLastEventUser(eventInfo.getEventUser());
								corresSampleLot.setLastEventComment(eventInfo.getEventComment());
								corresSampleLot.setLastEventTime(eventInfo.getEventTime());
								corresSampleLot.setLastEventTimekey(eventInfo.getEventTimeKey());
								corresSampleLot = ExtendedObjectProxy.getCorresSampleLotService().modify(eventInfo, corresSampleLot);
							}
							
							String actualSlot = corresSampleLot.getActualSamplePosition();
						}
					}
				}
				catch(Throwable e)
				{
					log.info("not corresSample currently..");
				}

				//ReserveSample Completing..
				try
				{
					List<LotAction> sampleActionList = new ArrayList<LotAction>();

					String condition = " WHERE 1=1 AND lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND processOperationName = ?"
							+ " AND processOperationVersion = ? AND sampleProcessFlowName = ? AND sampleProcessFlowVersion = ? AND actionName = ? AND actionState = ? ";
					Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), beforeFlowName, beforeFlowVersion,
							beforeOperationName, "00001", flowName, flowVersion, "Sampling", "Created" };

					sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);

					if(sampleActionList.size() == 1)
					{
						LotAction lotAction = new LotAction();
						lotAction = sampleActionList.get(0);

						lotAction.setActionState("Executed");
						lotAction.setLastEventTime(eventInfo.getEventTime());
						lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
						//2019.02.27_hsryu_Mantis 0002723. if SampleOutHold, remain EventUser . 
						//lotAction.setLastEventUser(eventInfo.getEventUser());
						lotAction.setLastEventComment(eventInfo.getEventComment());
						ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotAction);

					}
				}
				catch(Throwable e)
				{
					log.info("not ReserveSampling currently..");
				}
			}
		}
		
		return lotData;
	}
	
	public String chkCompareSampleSlot(Lot lotData, String slotNum)
	{
		ArrayList<String> realSlot = new ArrayList<String>();
		String[] arrSampleSlot = StringUtil.split(slotNum, ",");
		ArrayList<String> notExistSlot = new ArrayList<String>();
		ArrayList<String> realSampleSlot = new ArrayList<String>();
		boolean existFlag = false;
		String strSumPosition;
		String nonSlotPosition;
		
		List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		
		if(pProductList.size()!=0)
		{
			//set PositionList..
			for(int i=0; i<pProductList.size(); i++)
			{
				realSlot.add(String.valueOf(pProductList.get(i).getPosition()));
			}
			
			//Compare realPosition and SamplePosition..
			for(int i=0; i<arrSampleSlot.length;i++)
			{
				existFlag = false;
				
				for(int j=0;j<realSlot.size(); j++)
				{
					if(StringUtil.equals(realSlot.get(j), arrSampleSlot[i]))
					{
						existFlag = true;
						realSampleSlot.add(realSlot.get(j));
					}
				}
				
				if(!existFlag)
				{
					notExistSlot.add(arrSampleSlot[i]);
				}
			}
			
			//if not exist samplePosition..
			if(notExistSlot.size()>0)
			{
				if(realSampleSlot.size()>0)
				{
					for(int i=0; i<realSampleSlot.size(); i++)
					{
						for(int j=0; j<realSlot.size(); j++)
						{
							if(StringUtil.equals(realSampleSlot.get(i), realSlot.get(j)))
							{
								realSlot.remove(j);
							}
						}
					}
				}
				
				//if not exist sampleSlot in RealSlot, get next Slot and Add..
				if(notExistSlot.size()>0&&realSlot.size()>0)
				{
					for(int i=0; i<notExistSlot.size();i++)
					{
						if(realSlot.size()<i+1)
						{
							break;
						}
						
						realSampleSlot.add(realSlot.get(i));
					}
				}
				
				//sort..
				for (int i = 0; i < realSampleSlot.size(); i++)
				{
					for (int j = i + 1; j < realSampleSlot.size(); j++)
					{
						if (Integer.parseInt(realSampleSlot.get(i)) > Integer.parseInt(realSampleSlot.get(j)))
						{
							int temp = 0;
							temp = Integer.parseInt(realSampleSlot.get(i));
							realSampleSlot.set(i, realSampleSlot.get(j));
							realSampleSlot.set(j, Integer.toString(temp));
						}
					}
				}
				
				strSumPosition = MESLotServiceProxy.getLotServiceUtil().ConvertArrayPositionToString(realSampleSlot);
				nonSlotPosition = MESLotServiceProxy.getLotServiceUtil().ConvertArrayPositionToString(notExistSlot);
				
				return strSumPosition+"&"+nonSlotPosition;
			}
			else
			{
				return "";
			}
		}
		
		return "";
	}


	public String getNextNodeStack(String processFlowName, String processFlowVersion, String curNodeStack)
	{
		String nodeStack = "";

		String checkSql = " SELECT A.TONODEID "
				  + " FROM NODE N, ARC A "
				  + " WHERE 1 = 1 "
				  + "   AND N.NODEID = A.FROMNODEID "
				  + "   AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME "
				  + "   AND N.PROCESSFLOWVERSION = A.PROCESSFLOWVERSION "
				  + "   AND N.NODEID = :NODEID "
				  + "   AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				  + "   AND N.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ";

		Map<String, Object> bindSet = new HashMap<String, Object>();
		bindSet.put("NODEID", curNodeStack);
		bindSet.put("PROCESSFLOWNAME", processFlowName);
		bindSet.put("PROCESSFLOWVERSION", processFlowVersion);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, bindSet);
		if ( sqlResult.size() > 0 )
		{
			nodeStack = (String) sqlResult.get(0).get("TONODEID");
		}

		return nodeStack;
	}
	
	public String checkReserveInfo(String lotName, String productSpecName, String ecCode, String processFlowName, String processFlowVersion, String processOperationName, String nodeStack,EventInfo eventInfo) throws CustomException
	{
		String sampleFlowName = "";

		String flowSql = "SELECT N.NODEID, N.NODEATTRIBUTE1, N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION "
				+ " FROM NODE N "
				+ " WHERE N.NODEID = :NODEID ";

		Map<String, Object> flowBindSet = new HashMap<String, Object>();
		flowBindSet.put("NODEID", nodeStack);

		List<Map<String, Object>> flowSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(flowSql, flowBindSet);

		if ( flowSqlResult.size() > 0 )
		{
			processFlowName = (String) flowSqlResult.get(0).get("PROCESSFLOWNAME");
			processFlowVersion = (String) flowSqlResult.get(0).get("PROCESSFLOWVERSION");
			processOperationName = (String) flowSqlResult.get(0).get("NODEATTRIBUTE1");
			
			String sampleLotSql = "SELECT SAMPLEPROCESSFLOWNAME, POSITION, TYPE "
					+ " FROM (SELECT LA.SAMPLEPROCESSFLOWNAME, LA.POSITION, :TYPE1 TYPE "
					+ "         FROM CT_LOTACTION LA "
					+ "        WHERE 1 = 1 "
					+ "          AND LA.LOTNAME = :LOTNAME "
					+ "          AND LA.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "          AND LA.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
					+ "          AND LA.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
					+ "          AND LA.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
					+ "          AND LA.ACTIONSTATE = :ACTIONSTATE "
					+ "          AND LA.ACTIONNAME = :ACTIONNAME "
					+ "       UNION "
					+ "       SELECT SL.SAMPLEPROCESSFLOWNAME, SLC.SAMPLEPRIORITY, :TYPE2 TYPE "
					+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_SAMPLELOT SL "
					+ "        WHERE 1 = 1 "
					+ "          AND SLC.FACTORYNAME = SL.FACTORYNAME "
					+ "          AND SLC.PRODUCTSPECNAME = SL.PRODUCTSPECNAME "
					+ "          AND SLC.ECCODE = SL.ECCODE "
					+ "          AND SLC.PROCESSFLOWNAME = SL.PROCESSFLOWNAME "
					+ "          AND SLC.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION "
					+ "          AND SLC.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME "
					+ "          AND SLC.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION "
					+ "          AND SLC.FROMPROCESSOPERATIONNAME = SL.FROMPROCESSOPERATIONNAME "
					+ "          AND SL.LOTNAME = :LOTNAME "
					+ "          AND SL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
					+ "          AND SL.ECCODE = :ECCODE "
					+ "          AND SL.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "          AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
					+ "          AND SL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION "
					+ "          AND SL.SAMPLEFLAG = :SAMPLEFLAG "
					+ "          AND SL.SAMPLESTATE = :SAMPLESTATE "
					+ "       UNION "
					+ "       SELECT CSL.SAMPLEPROCESSFLOWNAME, SLC.CORRESSAMPLEPRIORITY SAMPLEPRIORITY, :TYPE3 TYPE "
					+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_CORRESSAMPLELOT CSL "
					+ "        WHERE 1 = 1 "
					+ "          AND SLC.FACTORYNAME = CSL.FACTORYNAME "
					+ "          AND SLC.PRODUCTSPECNAME = CSL.PRODUCTSPECNAME "
					+ "          AND SLC.ECCODE = CSL.ECCODE "
					+ "          AND SLC.PROCESSFLOWNAME = CSL.PROCESSFLOWNAME "
					+ "          AND SLC.PROCESSFLOWVERSION = CSL.PROCESSFLOWVERSION "
					+ "          AND SLC.CORRESSAMPLEPROCESSFLOWNAME = CSL.SAMPLEPROCESSFLOWNAME "
					+ "          AND SLC.CORRESSAMPLEPROCESSFLOWVERSION = CSL.SAMPLEPROCESSFLOWVERSION "
					+ "          AND SLC.CORRESPROCESSOPERATIONNAME = CSL.FROMPROCESSOPERATIONNAME "
					+ "          AND SLC.CORRESPROCESSOPERATIONVERSION = CSL.FROMPROCESSOPERATIONVERSION "
					+ "          AND CSL.LOTNAME = :LOTNAME "
					+ "          AND CSL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
					+ "          AND CSL.ECCODE = :ECCODE "
					+ "          AND CSL.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "          AND CSL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
					+ "          AND CSL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION "
					+ "          AND CSL.SAMPLEFLAG = :SAMPLEFLAG "
					+ "          AND CSL.SAMPLESTATE = :SAMPLESTATE) "
					+ "  ORDER BY DECODE(TYPE, :TYPE1, 0, :TYPE2, 1,2), POSITION ASC ";

			Map<String, Object> smapleLotBindSet = new HashMap<String, Object>();
			smapleLotBindSet.put("LOTNAME", lotName);
			smapleLotBindSet.put("PRODUCTSPECNAME", productSpecName);
			smapleLotBindSet.put("ECCODE", ecCode);
			smapleLotBindSet.put("PROCESSFLOWNAME", processFlowName);
			smapleLotBindSet.put("PROCESSFLOWVERSION", processFlowVersion);
			smapleLotBindSet.put("PROCESSOPERATIONNAME", processOperationName);
			smapleLotBindSet.put("PROCESSOPERATIONVERSION", "00001");
			smapleLotBindSet.put("ACTIONSTATE", "Created");
			smapleLotBindSet.put("ACTIONNAME", "Sampling");
			smapleLotBindSet.put("FROMPROCESSOPERATIONNAME", processOperationName);
			smapleLotBindSet.put("FROMPROCESSOPERATIONVERSION", "00001");
			smapleLotBindSet.put("SAMPLEFLAG", "Y");
			smapleLotBindSet.put("SAMPLESTATE", "Decided");
			smapleLotBindSet.put("TYPE1", "RESERVE");
			smapleLotBindSet.put("TYPE2", "AUTO");
			smapleLotBindSet.put("TYPE3", "CORRES");

			List<Map<String, Object>> sampleLotSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sampleLotSql, smapleLotBindSet);

			if ( sampleLotSqlResult.size() > 0 )
			{
				List<LotAction> sampleActionList = new ArrayList<LotAction>();
				sampleFlowName = sampleLotSqlResult.get(0).get("SAMPLEPROCESSFLOWNAME").toString();
				String position = sampleLotSqlResult.get(0).get("POSITION").toString();
				String type = sampleLotSqlResult.get(0).get("TYPE").toString();

				//Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

				/*if(StringUtil.equals(type, "RESERVE"))
				{
					String condition = " WHERE 1=1 AND lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND processOperationName = ?"
							+ " AND processOperationVersion = ? AND position = ? AND sampleProcessFlowName = ? AND sampleProcessFlowVersion = ? AND actionName = ? AND actionState = ? ";
					Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), processFlowName, processFlowVersion,
							processOperationName, "00001", Integer.parseInt(position), sampleFlowName, "00001", "Sampling", "Created" };

					try
					{
						eventInfo.setEventName("Execute SampleLot");
						sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);

						LotAction lotAction = new LotAction();
						lotAction = sampleActionList.get(0);

						lotAction.setActionState("Executed");
						lotAction.setLastEventTime(eventInfo.getEventTime());
						lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
						lotAction.setLastEventUser(eventInfo.getEventUser());
						lotAction.setLastEventComment(eventInfo.getEventComment());
						ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotAction);
					}
					catch(Exception ex)
					{
						throw new CustomException("SAMPLE-0003",lotName, processFlowName, processOperationName, sampleFlowName);
					}
				}
*/
				return sampleFlowName;
			}
		}

		return "";
	}

	public Map<String, Object> checkReserveSampleInfo(String lotName, String productSpecName, String ecCode, String processFlowName, String processFlowVersion, String processOperationName) throws CustomException
	{
		String sampleLotSql = "SELECT SAMPLEPROCESSFLOWNAME, SAMPLEPROCESSFLOWVERSION, POSITION, TYPE "
				+ " FROM (SELECT LA.SAMPLEPROCESSFLOWNAME, LA.SAMPLEPROCESSFLOWVERSION, LA.POSITION, :TYPE1 TYPE "
				+ "         FROM CT_LOTACTION LA "
				+ "        WHERE 1 = 1 "
				+ "          AND LA.LOTNAME = :LOTNAME "
				+ "          AND LA.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ "          AND LA.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ "          AND LA.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ "          AND LA.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				+ "          AND LA.ACTIONSTATE = :ACTIONSTATE "
				+ "          AND LA.ACTIONNAME = :ACTIONNAME "
				+ "       UNION "
				+ "       SELECT SL.SAMPLEPROCESSFLOWNAME, SL.SAMPLEPROCESSFLOWVERSION, SLC.SAMPLEPRIORITY, :TYPE2 TYPE "
				+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_SAMPLELOT SL "
				+ "        WHERE 1 = 1 "
				+ "          AND SLC.FACTORYNAME = SL.FACTORYNAME "
				+ "          AND SLC.PRODUCTSPECNAME = SL.PRODUCTSPECNAME "
				+ "          AND SLC.ECCODE = SL.ECCODE "
				+ "          AND SLC.PROCESSFLOWNAME = SL.PROCESSFLOWNAME "
				+ "          AND SLC.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION "
				+ "          AND SLC.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME "
				+ "          AND SLC.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION "
				+ "          AND SLC.FROMPROCESSOPERATIONNAME = SL.FROMPROCESSOPERATIONNAME "
				+ "          AND SL.LOTNAME = :LOTNAME "
				+ "          AND SL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ "          AND SL.ECCODE = :ECCODE "
				+ "          AND SL.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ "          AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
				+ "          AND (SL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION OR SL.FROMPROCESSOPERATIONVERSION = :STAR) "
				+ "          AND SL.SAMPLEFLAG = :SAMPLEFLAG "
				+ "          AND SL.SAMPLESTATE = :SAMPLESTATE "
				+ "       UNION "
				+ "       SELECT CSL.SAMPLEPROCESSFLOWNAME, CSL.SAMPLEPROCESSFLOWVERSION, SLC.CORRESSAMPLEPRIORITY SAMPLEPRIORITY, :TYPE3 TYPE "
				+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_CORRESSAMPLELOT CSL "
				+ "        WHERE 1 = 1 "
				+ "          AND SLC.FACTORYNAME = CSL.FACTORYNAME "
				+ "          AND SLC.PRODUCTSPECNAME = CSL.PRODUCTSPECNAME "
				+ "          AND SLC.ECCODE = CSL.ECCODE "
				+ "          AND SLC.PROCESSFLOWNAME = CSL.PROCESSFLOWNAME "
				+ "          AND SLC.PROCESSFLOWVERSION = CSL.PROCESSFLOWVERSION "
				+ "          AND SLC.CORRESSAMPLEPROCESSFLOWNAME = CSL.SAMPLEPROCESSFLOWNAME "
				+ "          AND SLC.CORRESSAMPLEPROCESSFLOWVERSION = CSL.SAMPLEPROCESSFLOWVERSION "
				+ "          AND SLC.CORRESPROCESSOPERATIONNAME = CSL.FROMPROCESSOPERATIONNAME "
				+ "          AND SLC.CORRESPROCESSOPERATIONVERSION = CSL.FROMPROCESSOPERATIONVERSION "
				+ "          AND CSL.LOTNAME = :LOTNAME "
				+ "          AND CSL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ "          AND CSL.ECCODE = :ECCODE "
				+ "          AND CSL.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ "          AND CSL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
				+ "          AND (CSL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION OR CSL.FROMPROCESSOPERATIONVERSION = :STAR) "
				+ "          AND CSL.SAMPLEFLAG = :SAMPLEFLAG "
				+ "          AND CSL.SAMPLESTATE = :SAMPLESTATE) "
				+ "  ORDER BY DECODE(TYPE, :TYPE1, 0, :TYPE2, 1,2), POSITION ASC ";

		Map<String, Object> smapleLotBindSet = new HashMap<String, Object>();
		smapleLotBindSet.put("LOTNAME", lotName);
		smapleLotBindSet.put("PRODUCTSPECNAME", productSpecName);
		smapleLotBindSet.put("ECCODE", ecCode);
		smapleLotBindSet.put("PROCESSFLOWNAME", processFlowName);
		smapleLotBindSet.put("PROCESSFLOWVERSION", processFlowVersion);
		smapleLotBindSet.put("PROCESSOPERATIONNAME", processOperationName);
		smapleLotBindSet.put("PROCESSOPERATIONVERSION", "00001");
		smapleLotBindSet.put("ACTIONSTATE", "Created");
		smapleLotBindSet.put("ACTIONNAME", "Sampling");
		smapleLotBindSet.put("FROMPROCESSOPERATIONNAME", processOperationName);
		smapleLotBindSet.put("FROMPROCESSOPERATIONVERSION", "00001");
		smapleLotBindSet.put("SAMPLEFLAG", "Y");
		smapleLotBindSet.put("SAMPLESTATE", "Decided");
		smapleLotBindSet.put("TYPE1", "RESERVE");
		smapleLotBindSet.put("TYPE2", "AUTO");
		smapleLotBindSet.put("TYPE3", "CORRES");
		smapleLotBindSet.put("STAR", "*");

		List<Map<String, Object>> sampleLotSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sampleLotSql, smapleLotBindSet);

		if ( sampleLotSqlResult.size() > 0 )
		{
			return sampleLotSqlResult.get(0);
		}
		
		return null;
	}

	public boolean checkEndOperation(String processFlowName, String processFlowVersion, String nodeStack)
	{
		boolean endOperFlag = false;

		String checkSql = "SELECT NODEID, UPPER(NODETYPE) NODETYPE FROM NODE WHERE NODEID = ( "
				+ " SELECT A.TONODEID FROM NODE N, ARC A "
				+ " WHERE N.NODEID = A.FROMNODEID "
				+ " AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME "
				+ " AND N.PROCESSFLOWVERSION = A.PROCESSFLOWVERSION "
				+ " AND N.NODEID = :NODEID "
				+ " AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND N.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) ";

		Map<String, Object> bindSet = new HashMap<String, Object>();
		bindSet.put("NODEID", nodeStack);
		bindSet.put("PROCESSFLOWNAME", processFlowName);
		bindSet.put("PROCESSFLOWVERSION", processFlowVersion);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, bindSet);
		
		if ( sqlResult.size() > 0 )
		{
			String nodeType = (String)sqlResult.get(0).get("NODETYPE");
			//if ( nodeType.toUpperCase().equals("END") )
			if(StringUtil.equals("END", StringUtil.upperCase(nodeType)))
			{
				endOperFlag = true;
			}
		}

		return endOperFlag;
	}

	public Map<String, String> getProcessFlowInfo(String nodeStack)
	{
		Map<String, String> flowMap = new HashMap<String, String>();

		String checkSql = " SELECT N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION, N.NODEATTRIBUTE1, N.NODEATTRIBUTE2, N.NODEID FROM NODE N "
				+ " WHERE N.NODEID = :NODEID ";

		Map<String, Object> bindSet = new HashMap<String, Object>(); 
		bindSet.put("NODEID", nodeStack);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, bindSet);
		if ( sqlResult.size() > 0 )
		{
			String flowName = (String) sqlResult.get(0).get("PROCESSFLOWNAME");
			String flowVersion = (String) sqlResult.get(0).get("PROCESSFLOWVERSION");
			String operationName = (String) sqlResult.get(0).get("NODEATTRIBUTE1");
			String operationVersion = (String) sqlResult.get(0).get("NODEATTRIBUTE2");

			flowMap.put("PROCESSFLOWNAME", flowName);
			flowMap.put("PROCESSFLOWVERSION", flowVersion);
			flowMap.put("PROCESSOPERATIONNAME", operationName);
			flowMap.put("PROCESSOPERATIONVERSION", operationVersion);
		}

		return flowMap;
	}
	
	/**
	 * TK-In cancel procedure
	 * @author swcho
	 * @since 2015-10-13
	 * @param eventInfo
	 * @param lotData
	 * @param portData
	 * @param machineName
	 * @param carrierName
	 * @param lotJudge
	 * @param productPGSRCSequence
	 * @param assignCarrierUdfs
	 * @param deassignCarrierUdfs
	 * @return
	 * @throws CustomException
	 */
	public Lot cancelTrackIn(EventInfo eventInfo, Lot lotData, Port portData,
			String machineName, String carrierName, String lotJudge,
			List<ProductPGSRC> productPGSRCSequence,
			Map<String, String> assignCarrierUdfs, Map<String, String> deassignCarrierUdfs)
					throws CustomException
	{
		Map<String, String> cancelTrackInLotUdfs = lotData.getUdfs();
		cancelTrackInLotUdfs.put("PORTNAME", portData.getKey().getPortName());
		//cancelTrackInLotUdfs.put("PORTTYPE", CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"));
		//cancelTrackInLotUdfs.put("PORTUSETYPE", CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE"));
		//cancelTrackInLotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
		//cancelTrackInLotUdfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());

		MakeLoggedOutInfo makeLoggedOutInfo =
				MESLotServiceProxy.getLotInfoUtil().makeLoggedOutInfo(lotData,
						lotData.getAreaName(),
						assignCarrierUdfs,
						StringUtil.isEmpty(carrierName)?lotData.getCarrierName():carrierName,
								//consumedMaterialSequence,
								"",//completeFlag
								deassignCarrierUdfs,
								StringUtil.isEmpty(lotJudge)?lotData.getLotGrade():lotJudge,
										"", //machineName
										"",//machineRecipe
										lotData.getNodeStack(),
										"",//trackOutLot.getProcessFlowName(),
										"",//trackOutLot.getProcessFlowVersion(),
										"",//trackOutLot.getProcessOperationName(),
										"",//trackOutLot.getProcessOperationVersion(),
										productPGSRCSequence,
										"",
										lotData.getReworkNodeId(),
										lotData.getUdfs());

		eventInfo.setEventName("CancelTrackIn");

		Lot afterCanceledLot = MESLotServiceProxy.getLotServiceImpl().trackOutLot(eventInfo, lotData, makeLoggedOutInfo);

		return afterCanceledLot;
	}

	/**
	 * check and act Q-time interlock
	 * maybe require to document
	 * action is very dynamic
	 * @author swcho
	 * @since 2014.06.24
	 * @param lotName
	 * @return
	 * @throws CustomException
	 */
	public String doQTimeAction(Document doc, String lotName)
			throws CustomException
	{

		LotKey lotKey = new LotKey();
		lotKey.setLotName(lotName);
		Lot lot = LotServiceProxy.getLotService().selectByKey(lotKey);

		String targetSubject = "";

		String reworkFlag = GenericServiceProxy.getConstantMap().Flag_N;

		List<LotQueueTime> QTimeDataList = ExtendedObjectProxy.getQTimeService().findQTimeByLot(lotName);

		String resultType = "";

		for (LotQueueTime QTimeData : QTimeDataList)
		{
			resultType = ExtendedObjectProxy.getQTimeService().doActionQTime(QTimeData,lot.getProductSpecName());

			//Q-time interval is only one by structure
			if (!StringUtil.isEmpty(resultType))
				break;
		}

		if(!resultType.isEmpty())
		{
			String alarmCode = resultType.toString();
			try
			{
				targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");

				//generate Alarm request message
				Element eleBody = new Element(SMessageUtil.Body_Tag);
				{
					Element eleAlarmCode = new Element("ALARMCODE");
					eleAlarmCode.setText(alarmCode);
					eleBody.addContent(eleAlarmCode);

					Element eleAlarmType = new Element("ALARMTYPE");
					eleAlarmType.setText("MES");
					eleBody.addContent(eleAlarmType);

					Element eleLotName = new Element("LOTNAME");
					eleLotName.setText(lotName);
					eleBody.addContent(eleLotName);
				}

				Document requestDoc = SMessageUtil.createXmlDocument(eleBody, "CreateAlarm",
						"",//SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false),
						targetSubject,
						SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false),
						"Queue Time Alarm request");

				GenericServiceProxy.getESBServive().sendBySender(targetSubject, requestDoc, "GenericSender");
			}
			catch (Exception ex)
			{
				log.error(String.format("Lot[%s] is failed to Alarm", lotName));
			}
		}

		return reworkFlag;
	}

	/**
	 * makeInRework before TK OUT
	 * @author swcho
	 * @since 2014.06.25
	 * @param lotData
	 * @param productPGSRCSequence
	 * @throws CustomException
	 */
	public void makeInRework4QTime(String interlockFlag, Lot lotData, List<ProductPGSRC> productPGSRCSequence)
			throws CustomException
	{
		//Q-time rework verification
		//if (interlockFlag.equals(GenericServiceProxy.getConstantMap().Flag_Y))
		if(StringUtil.equals(GenericServiceProxy.getConstantMap().Flag_Y, interlockFlag))
		{
			//reworkFlag = GenericServiceProxy.getConstantMap().Flag_Y;
			//lotGrade = GenericServiceProxy.getConstantMap().LotGrade_R;
			lotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_R);

			if (productPGSRCSequence != null)
			{
				for (ProductPGSRC productData : productPGSRCSequence)
				{
					productData.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_R);
				}
			}
		}
	}

	/**
	 * start auto-rework
	 * 160310 by swcho : modified
	 * @since 2014.02.04
	 * @author swcho
	 * @param eventInfo
	 * @param lotData
	 * @param priorFlag
	 * @throws CustomException
	 */
	public Lot startRework(EventInfo eventInfo, Lot lotData, String reworkFlag, boolean priorFlag)
			throws CustomException
	{
		//if (StringUtil.equals(lotData.getReworkState(), "NotInRework") && reworkFlag.equals(GenericServiceProxy.getConstantMap().Flag_Y))
		if(StringUtil.equals("NotInRework", lotData.getReworkState()) && StringUtil.equals(GenericServiceProxy.getConstantMap().Flag_Y, reworkFlag)  )
		{
			String destinedNodeStack;

			//rework path with front or current
			if (priorFlag)
			{
				destinedNodeStack = PolicyUtil.getWhereNext(lotData, lotData.getFactoryName(), CommonUtil.getValue(lotData.getUdfs(), "BEFOREFLOWNAME"), CommonUtil.getValue(lotData.getUdfs(), "BEFOREOPERATIONNAME"), "Rework", "Auto", "Y");
			}
			else
			{
				destinedNodeStack = PolicyUtil.getWhereNext(lotData, lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), "Rework", "Auto", "Y");
			}

			//no rework route, then no support to rework
			if (StringUtil.isEmpty(destinedNodeStack)) return lotData;

			List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());

			List<ProductRU> productRUdfs = new ArrayList<ProductRU>();

			for (Product product : productList)
			{
				ProductRU productRU = new ProductRU();
				productRU.setProductName(product.getKey().getProductName());
				productRU.setUdfs(product.getUdfs());
				productRU.setReworkFlag("Y");

				productRUdfs.add(productRU);
			}

			MakeInReworkInfo makeInReworkInfo =
					MESLotServiceProxy.getLotInfoUtil().makeInReworkInfo(lotData.getKey().getLotName(),
							lotData.getAreaName(),
							"",
							"",
							CommonUtil.getValue(lotData.getUdfs(), "RETURNFLOWNAME"),
							CommonUtil.getValue(lotData.getUdfs(), "RETURNOPERATIONNAME"),
							destinedNodeStack,
							lotData.getUdfs(),
							productRUdfs);

			eventInfo.setEventName("Rework");

			Lot inReworkLot = MESLotServiceProxy.getLotServiceImpl().startRework(eventInfo, lotData, makeInReworkInfo);

			return inReworkLot;
		}
		else
		{
			return lotData;
		}
	}

	/**
	 * deleteSamplingData
	 * @author hykim
	 * @since 2014.07.31
	 * @param EventInfo
	 * @param Lot
	 * @return
	 * @throws CustomException
	 */
	public void deleteSamplingData(Lot afterTrackOutLot, List<Element> productList, boolean isManual)
			throws CustomException
	{

		List<SampleLot> sampleLotList=null;
		String condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? AND toprocessoperationname = ?";
		Object[] bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
				afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProcessFlowName(),
				afterTrackOutLot.getProcessOperationName()};
		try{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}catch(Exception ex)
		{

		}


		if(sampleLotList == null)
			return;

		if(sampleLotList != null)
		{
			List<SampleProduct> sampleProductList = null;

			if(isManual)
			{
				try{
					condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? AND toprocessoperationname = ? ";
					bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
							afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProcessFlowName(),
							afterTrackOutLot.getProcessOperationName()};
					sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);

					for(SampleProduct sampleProduct : sampleProductList)
					{
						ExtendedObjectProxy.getSampleProductService().delete(sampleProduct);
					}
				}
				catch(Exception ex)
				{

				}
			}
			else
			{
				condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? AND toprocessoperationname = ? ";
				bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProcessFlowName(),
						afterTrackOutLot.getProcessOperationName()};
				sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);

				for(SampleProduct sampleProductM : sampleProductList)
				{
					for(Element productE : productList)
					{
						try
						{
							//if(productE.getChildText("PRODUCTNAME").equals(sampleProductM.getProductName()) &&!productE.getChildText("PROCESSINGINFO").equals("B"))
							if(StringUtil.equals(sampleProductM.getProductName(), productE.getChildText("PRODUCTNAME")) &&
									StringUtil.equals("B", productE.getChildText("PROCESSINGINFO")))
							{
								ExtendedObjectProxy.getSampleProductService().delete(sampleProductM);
							}
						}catch(Exception e){}
					}
				}
			}

			try{
				sampleProductList = null;
				condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? AND toProcessOperationName = ? ";
				bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessOperationName()};

				sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
			}catch(Exception ex)
			{
				log.debug("Not Found SampleProduct");
			}

			if(sampleProductList == null)
			{
				try{
					/* 2018.02.22 dmlee : arrange For EDO
					condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? AND toProcessOperationName = ? ";
					bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
							afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProcessFlowName(), sampleLotList.get(0).getTOPROCESSOPERATIONNAME()};
					List<SampleLot> sampleInfolist = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);

					SampleLot sampleInfo = sampleInfolist.get(0);
					ExtendedObjectProxy.getSampleLotService().delete(sampleInfo);
					 */
				}
				catch(Exception ex)
				{
					log.info("Delete samplelot");
				}
			}
		}
	}

	/**
>>>>>>> .r3897
	 * package for Change Process Operation
	 * @author swcho
	 * @since 2015.03.05
	 * @param eventInfo
	 * @param lotData
	 * @param changeSpecInfo
	 * @return
	 * @throws CustomException
	 */
	public Lot changeProcessOperation(EventInfo eventInfo, Lot lotData, ChangeSpecInfo changeSpecInfo)
			throws CustomException
	{
		Lot preLotData = (Lot) ObjectUtil.copyTo(lotData);

		Lot movingLot = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		//keep locked
		{//execute post action
			//Skip
			//movingLot = this.executePostAction(eventInfo, preLotData, movingLot);
		}

		return movingLot;
	}

	/**
	 * skip means pass to next operation one by one
	 * @author swcho
	 * @since 2015.03.15
	 * @param eventInfo
	 * @param lotData
	 * @param skipInfo
	 * @return
	 * @throws CustomException
	 */
	public Lot skip(EventInfo eventInfo, Lot lotData, ChangeSpecInfo skipInfo)
			throws CustomException
	{
		lotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, skipInfo);

		return lotData;
	}

	/**
	 * auto Lot judge engine
	 * @author swcho
	 * @since 2015.05.05
	 * @param lotData
	 * @param lotGrade
	 * @param productPGSRCSequence
	 * @return
	 * @throws CustomException
	 */
	public String decideLotJudge(Lot lotData, String lotGrade, List<ProductPGSRC> productPGSRCSequence) throws CustomException
	{
		log.info("auto Lot judge begin...");

		//		if (StringUtil.isNotEmpty(lotGrade))
		//		{
		//			log.info(String.format("Lot judge is already given by [%s]", lotGrade));
		//			return lotGrade;
		//		}

		try
		{
			lotGrade = "G";

			if(productPGSRCSequence != null && productPGSRCSequence.size() > 0)
			{
				for (ProductPGSRC productPGSRC : productPGSRCSequence)
				{
					if(StringUtil.equals(productPGSRC.getProductGrade(), "S"))
					{
						lotGrade = "S";
						break;
					}
					else if(StringUtil.equals(productPGSRC.getProductGrade(), "W"))
					{
						lotGrade = "W";
						break;
					}
					else if(StringUtil.equals(productPGSRC.getProductGrade(), "N"))
					{
						lotGrade = "N";
					}
					else if(StringUtil.equals(productPGSRC.getProductGrade(), "R"))
					{
						if(!StringUtil.equals(lotGrade, "W") && !StringUtil.equals(lotGrade, "N")  && !StringUtil.equals(lotGrade, "S"))
						{
							lotGrade = "R";
						}
					}
					else if(StringUtil.equals(productPGSRC.getProductGrade(), "P"))
					{
						if(!StringUtil.equals(lotGrade, "W") && !StringUtil.equals(lotGrade, "N") && !StringUtil.equals(lotGrade, "R")
								&& !StringUtil.equals(lotGrade, "S"))
						{
							lotGrade = "P";
						}
					}
					else if(StringUtil.equals(productPGSRC.getProductGrade(), "G"))
					{
						if(!StringUtil.equals(lotGrade, "W") && !StringUtil.equals(lotGrade, "N") && !StringUtil.equals(lotGrade, "R")
								&& !StringUtil.equals(lotGrade, "S") && !StringUtil.equals(lotGrade, "P"))
						{
							lotGrade = "G";
						}
					}
				}
			}

			log.info(String.format("Lot[%s] is judged by LotGrade[%s]", lotData.getKey().getLotName(), lotGrade));
		}
		catch (Exception ex)
		{
			log.error(String.format("Lot[%s] auto-judge failed, so keep previous judge", lotData.getKey().getLotName()));
			log.error(ex);

			lotGrade = lotData.getLotGrade();
		}

		return lotGrade;
	}

	/**
	 * recover Lot & Product judge
	 * @author swcho
	 * @since 2015.05.06
	 * @param eventInfo
	 * @param lotData
	 * @param fromGrade
	 * @param toGrade
	 * @return
	 * @throws CustomException
	 */
	public Lot recoverLotGrade(EventInfo eventInfo, Lot lotData, String fromGrade, String toGrade)
			throws CustomException
	{
		List<Product> productList = ProductServiceProxy.getProductService().select("lotName = ? AND productState = ? AND productGrade = ?",
				new Object[] {lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().Prod_InProduction, fromGrade});

		List<ProductPGS> productPGSSequence = MESLotServiceProxy.getLotInfoUtil().getProductPGSSequence(lotData.getKey().getLotName(), toGrade, productList, null);

		ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, toGrade, productPGSSequence);

		lotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);

		return lotData;
	}

	/**
	 * execute process hold
	 * @author hwlee
	 * @since 2015.10.20
	 * @param eventInfo
	 * @param lotData
	 * @throws CustomException
	 */
	public void executePostHold(EventInfo eventInfo, Lot lotData)
			throws CustomException
	{
		/* 2018.02.07 hsryu - remove
		eventInfo.setReasonCode(reasonCode);
		eventInfo.setReasonCodeType(reasonCodeType);
		 */
		eventInfo.setEventName("Hold");

		Map<String, String> udfs = lotData.getUdfs();

		List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
		MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, udfs);
//		String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//		makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
		
		/* 20190212, hhlee, add, lot holdtime, holdreleasetime ==>> */
		//2019.02.19_hsryu_Delete HoldTime.
        //makeOnHoldInfo.getUdfs().put("HOLDTIME",eventInfo.getEventTime().toString());
        makeOnHoldInfo.getUdfs().put("HOLDRELEASETIME",StringUtil.EMPTY);
        /* <<== 20190212, hhlee, add, lot holdtime, holdreleasetime */
        
		try
		{
			LotServiceProxy.getLotService().makeOnHold(lotData.getKey(),eventInfo, makeOnHoldInfo);
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
	}

	/**
	 * instant hold Lot
	 * @author swcho
	 * @since 2016.09.09
	 * @param eventInfo
	 * @param lotData
	 * @param reasonCodeType
	 * @param reasonCode
	 * @return
	 * @throws CustomException
	 */
	public Lot executeHold(EventInfo eventInfo, Lot lotData, String reasonCodeType, String reasonCode)
			throws CustomException
	{
		eventInfo.setReasonCode(reasonCode);
		eventInfo.setReasonCodeType(reasonCodeType);
		//eventInfo.setEventName("Hold");

		Map<String, String> udfs = lotData.getUdfs();

		List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
		MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, udfs);
//		String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//		makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
		
		/* 20190212, hhlee, add, lot holdtime, holdreleasetime ==>> */
		
		//2019.02.19_hsryu_Delete HoldTime.
		//makeOnHoldInfo.getUdfs().put("HOLDTIME",eventInfo.getEventTime().toString());
		makeOnHoldInfo.getUdfs().put("HOLDRELEASETIME",StringUtil.EMPTY);
        /* <<== 20190212, hhlee, add, lot holdtime, holdreleasetime */
		
		try
		{
			lotData = LotServiceProxy.getLotService().makeOnHold(lotData.getKey(),eventInfo, makeOnHoldInfo);

			return lotData;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
	}

	/**
	 * instant release Lot on hold
	 * @author swcho
	 * @since 2016.09.10
	 * @param eventInfo
	 * @param lotData
	 * @param reasonCodeType
	 * @param reasonCode
	 * @return
	 * @throws CustomException
	 */
	public Lot releaseHold(EventInfo eventInfo, Lot lotData, String reasonCodeType, String reasonCode)
			throws CustomException
	{
		String lotName = lotData.getKey().getLotName();

		//remove Lot multi-hold
		try
		{
			Object[] array = new Object[] {lotName, reasonCode};

			LotServiceProxy.getLotMultiHoldService().delete("lotName = ? And reasoncode = ?", array);
		}
		catch (NotFoundSignal ne)
		{
			log.warn("No hold on Lot");
		}

		//remove Product multi-hold
		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		for(Product productData : productList)
		{
			try
			{
				String productName = productData.getKey().getProductName();

				Object[] bindList = new Object[] {productName, reasonCode};

				ProductServiceProxy.getProductMultiHoldService().delete("productName = ? And reasoncode = ?", bindList);
			}
			catch(NotFoundSignal ne)
			{
				log.warn("No hold on Product");
			}
		}

		List<LotMultiHold> lotHoldlist;
		try
		{
			lotHoldlist = LotServiceProxy.getLotMultiHoldService().select("lotname = ? ", new Object[] {lotName});
		}
		catch(NotFoundSignal ne)
		{
			lotHoldlist = new ArrayList<LotMultiHold>();
		}

		if(lotHoldlist.size() < 1)
		{
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

			MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, lotData.getUdfs());

			try
			{
				lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(),	eventInfo, makeNotOnHoldInfo);
			}
			catch (InvalidStateTransitionSignal ie)
			{
				throw new CustomException("LOT-9003", lotData.getKey().getLotName());
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("LOT-9999", fe.getMessage());
			}
			catch (DuplicateNameSignal de)
			{
				throw new CustomException("LOT-9002", lotData.getKey().getLotName());
			}
			catch (NotFoundSignal ne)
			{
				throw new CustomException("LOT-9001", lotData.getKey().getLotName());
			}
		}

		return lotData;
	}

	/**
	 * instant release Lot on hold
	 * @author yudan
	 * @since 2017.03.14
	 * @param eventInfo
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public Lot releaseHoldV2(EventInfo eventInfo, Lot lotData)
			throws CustomException
	{
		String lotName = lotData.getKey().getLotName();

		//remove Lot multi-hold
		try
		{
			Object[] array = new Object[] {lotName};

			LotServiceProxy.getLotMultiHoldService().delete("lotName = ? ", array);
		}
		catch (NotFoundSignal ne)
		{
			log.warn("No hold on Lot");
		}

		//remove Product multi-hold
		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		for(Product productData : productList)
		{
			try
			{
				String productName = productData.getKey().getProductName();

				Object[] bindList = new Object[] {productName};

				ProductServiceProxy.getProductMultiHoldService().delete("productName = ? ", bindList);
			}
			catch(NotFoundSignal ne)
			{
				log.warn("No hold on Product");
			}
		}

		List<LotMultiHold> lotHoldlist;
		try
		{
			lotHoldlist = LotServiceProxy.getLotMultiHoldService().select("lotname = ? ", new Object[] {lotName});
		}
		catch(NotFoundSignal ne)
		{
			lotHoldlist = new ArrayList<LotMultiHold>();
		}

		if(lotHoldlist.size() < 1)
		{
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

			MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, lotData.getUdfs());

			try
			{
				lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(),	eventInfo, makeNotOnHoldInfo);
			}
			catch (InvalidStateTransitionSignal ie)
			{
				throw new CustomException("LOT-9003", lotData.getKey().getLotName());
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("LOT-9999", fe.getMessage());
			}
			catch (DuplicateNameSignal de)
			{
				throw new CustomException("LOT-9002", lotData.getKey().getLotName());
			}
			catch (NotFoundSignal ne)
			{
				throw new CustomException("LOT-9001", lotData.getKey().getLotName());
			}
		}

		return lotData;
	}

	/**
	 * comparance between actual and virtual Products
	 * @author swcho
	 * @since 2015-11-19
	 * @param productElementList
	 * @param carrierName
	 * @return
	 * @throws CustomException
	 */
	public Map<String, ListOrderedMap> generateUnloadList(List<Element> productElementList, String carrierName) throws CustomException
	{
		log.debug(String.format("[ELAP]generateUnloadList start at [%s]", System.currentTimeMillis()));

		List<String> lstProductName = new ArrayList<String>();

		StringBuffer bindSentence = new StringBuffer();

		for (Element productElement : productElementList)
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);

			lstProductName.add(productName);

			if (!bindSentence.toString().isEmpty()) bindSentence.append(",");

			bindSentence.append("?");
		}

		//151223 by swcho : zero size of unload Glass considered, avoid sql error
		if (bindSentence.length() < 1)
		{
			bindSentence.append("'").append("'");
		}

		StringBuffer qryBuffer = new StringBuffer("--TKOUT Product query \n")
		.append("WITH UProduct AS                                                                       \n")
		.append("(--unload Glass list                                                                   \n")
		.append(" SELECT P.productName, P.position, P.lotName, P.carrierName,                           \n")
		.append("        P.productState, P.productHoldState, P.productProcessState,                     \n")
		.append("        P.productGrade, P.processingInfo, P.subProductQuantity                         \n")
		.append("    FROM Product P                                                                     \n")
		.append(" WHERE 1=1                                                                             \n")
		.append("    AND productName IN (").append(bindSentence).append(")	\n")
		.append(")                                                                                      \n")
		.append("SELECT G.PFLAG,                                                                        \n")
		.append("        G.productName, G.position, G.lotName GLOTNAME, G.carrierName GCARRIERNAME,     \n")
		.append("        G.productState, G.productHoldState, G.productProcessState,                     \n")
		.append("        G.productGrade, G.processingInfo, G.subProductQuantity,                        \n")
		.append("        L.lotName, L.carrierName,                                                      \n")
		.append("        L.factoryName, L.productSpecName, L.processFlowName, L.processOperationName,   \n")
		.append("        L.lotState, L.lotProcessState, L.lotHoldState,                                 \n")
		.append("        L.productQuantity, L.subProductQuantity LSUBPRODUCTQUANTITY                    \n")
		.append("   FROM Lot L,                                                                         \n")
		.append("       (--unload Glass List including data existing                                    \n")
		.append("        SELECT U.productName, U.position, U.lotName, U.carrierName,                    \n")
		.append("                U.productState, U.productHoldState, U.productProcessState,             \n")
		.append("                U.productGrade, U.processingInfo, U.subProductQuantity,                \n")
		.append("                CASE                                                                   \n")
		.append("                    WHEN L.productName IS NULL THEN 'M'                                \n")
		.append("                    WHEN (U.productName = L.productName) THEN 'N'                      \n")
		.append("                    ELSE 'N'                                                           \n")
		.append("                END PFLAG                                                              \n")
		.append("            FROM UProduct U,                                                           \n")
		.append("                (--existing Product in unload CST                                      \n")
		.append("                      SELECT P.productName, P.position, P.lotName, P.carrierName,      \n")
		.append("                             P.productState, P.productHoldState, P.productProcessState,\n")
		.append("                             P.productGrade, P.processingInfo, P.subProductQuantity    \n")
		.append("                        FROM Product P                                                 \n")
		.append("                      WHERE P.carrierName = ?                                          \n")
		.append("                ) L                                                                    \n")
		.append("        WHERE U.productName = L.productName(+)                                         \n")
		.append("        UNION                                                                          \n")
		.append("        SELECT L.productName, L.position, L.lotName, L.carrierName,                    \n")
		.append("                L.productState, L.productHoldState, L.productProcessState,             \n")
		.append("                L.productGrade, L.processingInfo, L.subProductQuantity,                \n")
		.append("                CASE                                                                   \n")
		.append("                    WHEN U.productName IS NULL THEN 'S'                                \n")
		.append("                    ELSE 'N'                                                           \n")
		.append("                END PFLAG                                                              \n")
		.append("            FROM (--existing Product in unload CST                                     \n")
		.append("                    SELECT P.productName, P.position, P.lotName, P.carrierName,        \n")
		.append("                             P.productState, P.productHoldState, P.productProcessState,\n")
		.append("                             P.productGrade, P.processingInfo, P.subProductQuantity    \n")
		.append("                        FROM Product P                                                 \n")
		.append("                      WHERE P.carrierName = ?                                          \n")
		.append("                  ) L, UProduct U                                                      \n")
		.append("        WHERE L.productName = U.productName(+)                                         \n")
		.append("            AND U.productName IS NULL                                                  \n")
		.append("        ) G                                                                            \n")
		.append("WHERE G.lotName = L.lotName                                                            \n")
		.append("ORDER BY L.productQuantity, G.lotName");

		List<ListOrderedMap> lstResult;
		try
		{
			lstProductName.add(carrierName);
			lstProductName.add(carrierName);
			String[] bindArray = lstProductName.toArray(new String[lstProductName.size()]);

			lstResult = GenericServiceProxy.getSqlMesTemplate().queryForList(qryBuffer.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PRODUCT-9999", fe.getMessage());
		}

		if (lstResult.size() < 1)
		{
			throw new CustomException("PRODUCT-XXXX", carrierName);
		}

		HashMap<String, ListOrderedMap> rm = new HashMap<String, ListOrderedMap>();
		//Lot, flag, productList
		ListOrderedMap lm = new ListOrderedMap();
		List<ProductP> pm = new ArrayList<ProductP>();
		List<ProductP> gpm = new ArrayList<ProductP>();

		for (int idx=0;idx < lstResult.size();idx++)
		{
			ListOrderedMap result = lstResult.get(idx);

			String lotName = CommonUtil.getValue(result, "LOTNAME");
			String productName = CommonUtil.getValue(result, "PRODUCTNAME");
			String sPosition = CommonUtil.getValue(result, "POSITION");

			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));

			//160524 by swhco : position override
			for (Element eleP : productElementList)
			{
				String elePName = SMessageUtil.getChildText(eleP, "PRODUCTNAME", true);

				if(StringUtil.equals(elePName, productName))
				{
					String elePosition = SMessageUtil.getChildText(eleP, "POSITION", false);
					
					if(!StringUtil.isEmpty(elePosition))
						sPosition = elePosition;

					break;
				}
			}

			HashMap<String, String> productUdf = new HashMap<String, String>();

			ProductP productP = new ProductP();
			productP.setProductName(productName);
			productP.setPosition(StringUtil.equals("0", sPosition) ?0: Long.parseLong(sPosition));
			productP.setUdfs(productUdf);

			//151211 by swcho : Product validation
			String productState = CommonUtil.getValue(result, "PRODUCTSTATE");
			String pFlag = CommonUtil.getValue(result, "PFLAG");

			log.debug(String.format("Product[%s] in Lot[%s] would go to [%s]", productName, lotName, pFlag));

			if(!StringUtil.equals(GenericServiceProxy.getConstantMap().Prod_InProduction, productState))
			{
				//regard as not existing
				//gpm.add(productP);
				log.warn(String.format("Product[%s] in Lot[%s] is invalid cause of [%s]", productName, lotName, productState));
			}
			else if(StringUtil.equals("S", pFlag))
			{
				gpm.add(productP);
			}
			else
			{
				pm.add(productP);
			}

			String flag;

			//last index check & post action execution
			if (idx == (lstResult.size() - 1))
			{
				//last index in list
				//flag determination
				if(StringUtil.equals(CommonUtil.getValue(result, "CARRIERNAME"), carrierName))
				{
					flag = "D";
					log.info(String.format("final unload Lot is [%s]", lotName));
				}
				else
				{
					flag = "S";
					log.info(String.format("[%d]th source Lot is [%s]", idx, lotName));
				}

				setUnloadLotInfo(lotName, flag, rm, lm, pm, gpm);

				//clear reposit
				lm = new ListOrderedMap();
				pm = new ArrayList<ProductP>();
			}
			else if(!StringUtil.equals(CommonUtil.getValue((lstResult.get(idx+1)), "LOTNAME"), lotName))
			{
				//last index by lotName

				//flag determination
				if(StringUtil.equals(CommonUtil.getValue(result, "CARRIERNAME"), carrierName))
				{
					//20170822 by yudan
					if(!StringUtil.equals("OLED", lotData.getFactoryName()) &&StringUtil.equals("PL", lotData.getUdfs().get("PORTTYPE")) && StringUtil.equals("RUN", lotData.getLotProcessState()) )
					{
						flag = "S";
						log.info(String.format("[%d]th source Lot is [%s]", idx, lotName));
					}
					else
					{
						flag = "D";
						log.info(String.format("final unload Lot is [%s]", lotName));
					}
				}
				else
				{
					flag = "S";
					log.info(String.format("[%d]th source Lot is [%s]", idx, lotName));
				}

				setUnloadLotInfo(lotName, flag, rm, lm, pm, gpm);

				//clear reposit
				lm = new ListOrderedMap();
				pm = new ArrayList<ProductP>();
			}
		}

		log.debug(String.format("[ELAP]generateUnloadList end at [%s]", System.currentTimeMillis()));

		return rm;
	}

	/**
	 * custom composition info
	 * @author swcho
	 * @since 2015-11-19
	 * @param lotName
	 * @param flag
	 * @param rm
	 * @param lm
	 * @param pm
	 * @param gpm
	 * @throws CustomException
	 */
	public void setUnloadLotInfo(String lotName, String flag,
			HashMap<String, ListOrderedMap> rm, ListOrderedMap lm, List<ProductP> pm, List<ProductP> gpm)
					throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		//Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));

		//store it
		lm.put("LOT", lotData);
		lm.put("MPRODUCTLIST", pm);
		lm.put("SPRODUCTLIST", gpm);
		lm.put("FLAG", flag);
		rm.put(lotName, lm);
	}

	/**
	 * multiple Lot composition module
	 * @param eventInfo
	 * @param carrierName
	 * @param productElementList
	 * @return
	 * @throws CustomException
	 */
	public Lot composeLot(EventInfo eventInfo, String carrierName, List<Element> productElementList)
			throws CustomException
	{
		Map<String, ListOrderedMap> lstLotInfo = generateUnloadList(productElementList, carrierName);

		//one guy only returns
		Lot desLot = null;
		//List<ProductP> productPSequence = new ArrayList<ProductP>();
		Map<String, List<ProductP>> productPMap = new HashMap<String, List<ProductP>>();
		List<Lot> lstSrcLot = new ArrayList<Lot>();

		//first loop for preparation
		log.debug(String.format("[ELAP]composing preparation start at [%s]", System.currentTimeMillis()));
		for (String mLotName : lstLotInfo.keySet())
		{
			ListOrderedMap lotProps = lstLotInfo.get(mLotName);
			String flag = CommonUtil.getValue(lotProps, "FLAG");

			Lot lotData = (Lot) lotProps.get("LOT");

			List<ProductP> lstProductSplit = (List<ProductP>) lotProps.get("SPRODUCTLIST");
			List<ProductP> lstProductMerge = (List<ProductP>) lotProps.get("MPRODUCTLIST");
			//if (flag.equals("D"))
			if(StringUtil.equals("D", flag))
			{//DLOT preparation to receive Products
				//Lot lotData = (Lot) lotProps.get("LOTDATA");

				//List<ProductP> lstProductSplit = (List<ProductP>) lotProps.get("SPRODUCTLIST");
				//List<ProductP> lstProductMerge = (List<ProductP>) lotProps.get("MPRODUCTLIST");

				if (lstProductSplit.size() > 0)
				{
					//151224 by swcho : this means not touched Products originated from a Lot
					if (lstProductSplit.size() == lotData.getProductQuantity())
					{
						//just do deassign if still assigned
						if (!lotData.getCarrierName().isEmpty())
						{
							Durable srcCarrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

							eventInfo.setEventName("Deassign");

							List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(productElementList);

							DeassignCarrierInfo deassignInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, srcCarrierData, productUSequence);

							lotData = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignInfo, eventInfo);

							//nothing to delivery
							lotData = null;
						}
					}
					else
					{
						//Split garbage to garbage
						log.info(String.format("Lot[%s] is doing split with [%d] of [%s]", mLotName, lstProductSplit.size(), lotData.getProductQuantity()));

						eventInfo.setEventName("Create");
						Lot garbageLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());

						//do split
						eventInfo.setEventName("Split");
						TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(garbageLot.getKey().getLotName(),
								lstProductSplit.size(), lstProductSplit, lotData.getUdfs(), new HashMap<String, String>());
						lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);

						garbageLot = MESLotServiceProxy.getLotInfoUtil().getLotData(garbageLot.getKey().getLotName());

						log.info(String.format("Lot[%s] has completed to split into Lot[%s]", lotData.getKey().getLotName(), garbageLot.getKey().getLotName()));
					}
				}

				//get destination Lot
				//151223 by swcho : consumed Lot is not neccessary
				if (lotData != null && lotData.getProductQuantity() > 0)
				{
					desLot = lotData;
				}
				else
				{
					desLot = null;
				}

				//productPSequence.addAll(lstProductMerge);
				productPMap.put(mLotName, lstProductMerge);
			}
			//else if (flag.equals("S"))
			else if(StringUtil.equals("S", flag))
			{
				if (lstProductSplit.size() == lotData.getProductQuantity())
				{
					//just do deassign if still assigned
					if (!lotData.getCarrierName().isEmpty())
					{
						Durable srcCarrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

						eventInfo.setEventName("Deassign");

						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(productElementList);

						DeassignCarrierInfo deassignInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, srcCarrierData, productUSequence);

						lotData = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignInfo, eventInfo);

						//nothing to delivery
						lotData = null;

						desLot = null;

						productPMap.put(mLotName, lstProductMerge);
					}
				}
				else
				{
					lstSrcLot.add(lotData);
					//productPSequence.addAll(lstProductMerge);
					productPMap.put(mLotName, lstProductMerge);
				}
			}
		}
		
		log.debug(String.format("[ELAP]composing preparation end at [%s]", System.currentTimeMillis()));
		log.debug(String.format("[ELAP]DLOT preparation start at [%s]", System.currentTimeMillis()));
		
		//if no DLOT, create with main source Lot
		log.info("Destination Lot preparation");

		if (desLot == null)
		{
			if (lstSrcLot.size() < 1)
			{//count as PL case, do deassign
				//throw new CustomException("LOT-XXXX", "No Lot for unload");
				return null;
			}
			//PL single Lot unload & preserved Lot wih entire products unload case
			else if (lstSrcLot.size() == 1 && productPMap.get(lstSrcLot.get(0).getKey().getLotName()).size() == lstSrcLot.get(0).getProductQuantity())
			{
				desLot = lstSrcLot.get(0);
				//purge working Lot because DLOT was determined
				lstSrcLot = new ArrayList<Lot>();
			}
			//standard unload case
			else
			{
				eventInfo.setEventName("Create");
				desLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lstSrcLot.get(0), "", false, new HashMap<String, String>(), lstSrcLot.get(0).getUdfs());
			}
		}

		log.info(String.format("Destination Lot is Lot[%s] of ProductQuantity[%f] in Carrier[%s]", desLot.getKey().getLotName(), desLot.getProductQuantity(), desLot.getCarrierName()));
		log.debug(String.format("[ELAP]DLOT preparation end at [%s]", System.currentTimeMillis()));

		//merge to DLOT
		log.debug(String.format("[ELAP]pre-TKOUT Lot composition start at [%s]", System.currentTimeMillis()));
		log.info(String.format("Lot[%s] is doing merge with LotQuantity[%d]", desLot.getKey().getLotName(), lstSrcLot.size()));
		
		for (Lot srcLotData : lstSrcLot)
		{
			List<ProductP> productPSequence = productPMap.get(srcLotData.getKey().getLotName());

			//2016.02.22
			//PU¿¡¼­  TrackOut ½Ã ½ÇCST°¡ ¿Ã ¼öµµ ÀÖ±â ¶§¹®¿¡ Merge·Î µÇ¾î ÀÖ´ø°É SplitÀ¸·Î ¹Ù²Þ.
			eventInfo.setEventName("Split");
			TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(desLot.getKey().getLotName(), productPSequence.size(), productPSequence,
					new HashMap<String, String>(), new HashMap<String, String>());

			srcLotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, srcLotData, transitionInfo);

			//update destination Lot status
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			desLot = MESLotServiceProxy.getLotInfoUtil().getLotData(desLot.getKey().getLotName());
			desLot = LotServiceProxy.getLotService().selectByKeyForUpdate(desLot.getKey());
		}
		
		log.info(String.format("pre-TKOUT Lot is Lot[%s] of ProductQuantity[%f] in Carrier[%s]", desLot.getKey().getLotName(), desLot.getProductQuantity(), desLot.getCarrierName()));
		log.debug(String.format("[ELAP]pre-TKOUT Lot composition end at [%s]", System.currentTimeMillis()));

		return desLot;
	}

	/**
	 * N to one merge package
	 * @author swcho
	 * @since 2016.09.12
	 * @param eventInfo
	 * @param lstSrcLot
	 * @param desLot
	 * @return
	 * @throws CustomException
	 */
	public Lot mergeLot(EventInfo eventInfo, List<Lot> lstSrcLot, Lot desLot)
			throws CustomException
	{
		//merge to DLOT
		log.debug(String.format("[ELAP]Lot composition start at [%s]", System.currentTimeMillis()));
		log.info(String.format("Lot[%s] is doing merge with LotQuantity[%d]", desLot.getKey().getLotName(), lstSrcLot.size()));

		for (Lot srcLotData : lstSrcLot)
		{
			List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(srcLotData.getKey().getLotName());

			TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(desLot.getKey().getLotName(), productPSequence.size(), productPSequence,
					new HashMap<String, String>(), new HashMap<String, String>());
			srcLotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, srcLotData, transitionInfo);
			//update destination Lot status
			desLot =  MESLotServiceProxy.getLotInfoUtil().getLotData(desLot.getKey().getLotName());
		}

		log.info(String.format("Lot[%s] of ProductQuantity[%f] in Carrier[%s]", desLot.getKey().getLotName(), desLot.getProductQuantity(), desLot.getCarrierName()));
		log.debug(String.format("[ELAP]Lot composition end at [%s]", System.currentTimeMillis()));

		return desLot;
	}

	/**
	 * one to one split package
	 * @since 2016.09.27
	 * @author swcho
	 * @param eventInfo
	 * @param productList
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public Lot splitLot(EventInfo eventInfo, List<Element> productList, Lot lotData)
			throws CustomException
	{
		log.debug(String.format("[ELAP]Lot composition start at [%s]", System.currentTimeMillis()));
		log.info(String.format("Lot[%s] is doing split with ProductQuantity[%d]", lotData.getKey().getLotName(), productList.size()));

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("FACTORYNAME", lotData.getFactoryName());
		nameRuleAttrMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());

		List<String> lotNameList = CommonUtil.generateNameByNamingRule("GlassLotNaming", nameRuleAttrMap, 1);

		String lotName = lotNameList.get(0);

		eventInfo.setEventName("Create");
		Lot childLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotName, lotData, lotData.getCarrierName(), false,
				new HashMap<String, String>(), lotData.getUdfs());

		List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList);

		TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(childLot.getKey().getLotName(), productList.size(), productPSequence, lotData.getUdfs(),
				new HashMap<String, String>());

		//do split
		//161028 by swcho : missing event
		eventInfo.setEventName("Split");
		lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);

		childLot = MESLotServiceProxy.getLotInfoUtil().getLotData(childLot.getKey().getLotName());

		log.info(String.format("Lot[%s] of ProductQuantity[%f] in Carrier[%s]", childLot.getKey().getLotName(), childLot.getProductQuantity(), childLot.getCarrierName()));
		log.debug(String.format("[ELAP]Lot composition end at [%s]", System.currentTimeMillis()));

		return childLot;
	}

	/**
	 * arrange CST for child Lot
	 * @author swcho
	 * @since 2015-11-27
	 * @param eventInfo
	 * @param lotData
	 * @param durableData
	 * @throws CustomException
	 */
	public Lot arrangeCarrier(EventInfo eventInfo, Lot lotData, Durable durableData, List<Element> productElementList)
			throws CustomException
	{
		log.debug(String.format("[ELAP]CST arrangement start at [%s]", System.currentTimeMillis()));

		String srcCarrierName = lotData.getCarrierName();
		String desCarrierName = durableData.getKey().getDurableName();

		
		//if (!StringUtil.isEmpty(srcCarrierName) && !srcCarrierName.equals(desCarrierName))
		if(!StringUtil.isEmpty(srcCarrierName) && !StringUtil.equals(desCarrierName, srcCarrierName))
		{
			//deassign Lot
			Durable srcCarrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(srcCarrierName);

			eventInfo.setEventName("DeassignCarrier");

			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(productElementList);

			DeassignCarrierInfo deassignInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, srcCarrierData, productUSequence);

			lotData = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignInfo, eventInfo);
		}

		if (!StringUtil.isEmpty(desCarrierName) && lotData.getCarrierName().isEmpty())
		{
			//assign Lot
			eventInfo.setEventName("AssignCarrier");

			List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productElementList);

			AssignCarrierInfo assignInfo =  MESLotServiceProxy.getLotInfoUtil().assignCarrierInfo(lotData, durableData, productPSequence);

			lotData = MESLotServiceProxy.getLotServiceImpl().assignCarrier(lotData, assignInfo, eventInfo);
		}

		log.debug(String.format("[ELAP]CST arrangement end at [%s]", System.currentTimeMillis()));

		return lotData;
	}

	public Lot assignCarrier(EventInfo eventInfo, Lot lotData, Durable durableData, List<Element> productElementList)
			throws CustomException
	{
		//assign Lot
		eventInfo.setEventName("AssignCarrier");

		List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productElementList);

		AssignCarrierInfo assignInfo =  MESLotServiceProxy.getLotInfoUtil().assignCarrierInfo(lotData, durableData, productPSequence);

		lotData = MESLotServiceProxy.getLotServiceImpl().assignCarrier(lotData, assignInfo, eventInfo);

		return lotData;
	}

	/**
	 * validate assembly
	 * @since 2016.03.15
	 * @author swcho
	 * @param lotData
	 * @throws CustomException
	 */
	public void validateAssembly(Lot lotData, List<Element> eleProductList) throws CustomException
	{
		ProcessOperationSpec processSpec = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());

		if(StringUtil.equals(processSpec.getDetailProcessOperationType().toString() ,"ASSY"))
		{
			ProductSpec prdSpec = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());

			if(!StringUtil.equals(prdSpec.getMultiProductSpecType().toString(), "Production"))
			{
				throw new CustomException("LOT-0078", lotData.getKey().getLotName());
			}

			//List<Product> checkProductList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());

			for (Element eleProduct : eleProductList)
			{
				String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);

				Product product = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

				if(StringUtil.equals(product.getUdfs().get("PAIRPRODUCTNAME").toString().trim(), ""))
				{
					throw new CustomException("PRODUCT-0022", product.getKey().toString());
				}
			}
		}
	}

	/**
	 * getProductName
	 * @since 2016.03.15
	 * @author xzquan
	 * @param
	 * @throws CustomException
	 */
	public String getProductName(String lotName, int i) throws CustomException {
		// Product Naming Rule : lotName + Seq(1,2,3...8,9,A,B,C ... / Exclude
		// O, I) | AAAAA4, AAAAZ
		String productName = "";
		String productSeq = "";

		if (i < 10) {
			productSeq = String.valueOf(i);
		} else if (i >= 10 && i <= 17) {
			productSeq = "A" + (i - 10);
		} else if (i > 17 && i <= 22) {
			productSeq = "A" + (i - 10 + 1);
		} else if (i > 22 && i <= 33) {
			productSeq = "A" + (i - 10 + 2);
		} else {
			new CustomException("", "");
		}

		productName = lotName + productSeq;
		return productName;
	}

	/**
	 * getProductName
	 * @since 2016.05.31
	 * @author Aim System
	 * @param
	 * @throws CustomException
	 */
	public void workOrderPlanPositionRefresh(String machineName, String position) throws CustomException
	{
		try
		{
			List <ProductRequestPlan> sqlResult = new ArrayList <ProductRequestPlan>();

			sqlResult = ProductRequestPlanServiceProxy.getProductRequestPlanService().
					select("ASSIGNEDMACHINENAME = ? AND POSITION > ? AND PRODUCTREQUESTSTATE != ? ", new Object[] {machineName, position, GenericServiceProxy.getConstantMap().Prq_Completed});

			if(sqlResult != null && sqlResult.size() > 0)
			{
				String sql = "UPDATE PRODUCTREQUESTPLAN "
						+ "SET POSITION = POSITION - 1 "
						+ "WHERE ASSIGNEDMACHINENAME = :machineName "
						+ "AND POSITION > :position "
						+ "AND PRODUCTREQUESTSTATE != :state ";

				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("machineName", machineName);
				bindMap.put("position", position);
				bindMap.put("state", GenericServiceProxy.getConstantMap().Prq_Completed);

				GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			}
		}
		catch (greenFrameDBErrorSignal ne)
		{
			log.debug(String.format("Work Order Plan Position Refresh Failed", System.currentTimeMillis()));
		}
	}
	
	/**
	 * to post input plan reservation
	 * 160525 by dmlee : service object changed
	 * @author dmlee
	 * @since 2016.05.25
	 * @param eventInfo
	 * @param ProductRequestData
	 * @throws CustomException
	 */
	public void incrementWorkOrderScrapQty(EventInfo eventInfo, ProductRequestKey pKey, int scrapedQuantity) throws CustomException
	{
		try
		{
			//Increment Product Request
			IncrementScrappedQuantityByInfo incrementScrappedInfo = new IncrementScrappedQuantityByInfo();
			incrementScrappedInfo.setQuantity(scrapedQuantity);
			if(scrapedQuantity > 0)
			{
				eventInfo.setEventName("Scrap");
			}
			else
			{
				eventInfo.setEventName("UnScrap");
			}

			ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().incrementScrappedQuantityBy(pKey, eventInfo, incrementScrappedInfo);

			//  Add History
			MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);

			//Update CT_MESProductRequestScrap
			if(ProductRequestServiceUtil.CheckERPWorkOrder(pKey.getProductRequestName()))
			{
				ProductRequestServiceUtil.WriteMESProductRequestScrap(pKey.getProductRequestName(),scrapedQuantity);
			}

			//Get New Data
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
			ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(pKey);

			//Set Product Request State
			if(pData.getFinishedQuantity() >= pData.getPlanQuantity()-pData.getScrappedQuantity())
			{
				if(!StringUtil.equals(pData.getProductRequestState(),GenericServiceProxy.getConstantMap().Prq_Finished))
				{
					eventInfo.setEventName("Complete");

					ProductRequest pUpdateData = pData;
					pUpdateData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Finished);
					ProductRequestServiceProxy.getProductRequestService().update(pUpdateData);

					//Add History
					MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(pUpdateData, eventInfo);

					//Insert MESProductRequest WO Data
					if(ProductRequestServiceUtil.CheckERPWorkOrder(resultData.getKey().getProductRequestName()))
					{
						ProductRequestServiceUtil.WriteMESProductRequest(resultData.getKey().getProductRequestName());
					}
				}
			}
			else if (pData.getReleasedQuantity() >= pData.getPlanQuantity())
			{
				if(!StringUtil.equals(pData.getProductRequestState(),GenericServiceProxy.getConstantMap().Prq_Completed))
				{
					eventInfo.setEventName("Excute");

					ProductRequest pUpdateData = pData;
					pUpdateData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Completed);
					ProductRequestServiceProxy.getProductRequestService().update(pUpdateData);

					//  Add History
					MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(pUpdateData, eventInfo);
				}
			}
			else
			{
				if(!StringUtil.equals(pData.getProductRequestState(),GenericServiceProxy.getConstantMap().Prq_Released))
				{
					eventInfo.setEventName("Release");

					ProductRequest pUpdateData = pData;
					pUpdateData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Released);
					ProductRequestServiceProxy.getProductRequestService().update(pUpdateData);
					//  Add History
					MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(pUpdateData, eventInfo);
				}
			}
		}
		catch (greenFrameDBErrorSignal ne)
		{
			throw new CustomException("", ne.getMessage());
		}
	}

	/**
	 * validate LotGrade
	 * @since 2016.03.18
	 * @author hwlee89
	 * @param lotData
	 * @throws CustomException
	 */
	public void validationLotGrade(Lot lotData) throws CustomException
	{

		//if(lotData.getReworkState().equals(GenericServiceProxy.getConstantMap().Lot_NotInRework) &&!StringUtils.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_G))
		if(StringUtil.equals(GenericServiceProxy.getConstantMap().Lot_NotInRework, lotData.getReworkState())
				&& !StringUtils.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_G))
		{
			throw new CustomException("LOT-0075", lotData.getKey().getLotName());
		}
		
		//if(lotData.getReworkState().equals(GenericServiceProxy.getConstantMap().Lot_InRework))
		if(StringUtil.equals(GenericServiceProxy.getConstantMap().Lot_InRework, lotData.getReworkState()))
		{
			if(StringUtils.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_R))
			{
				return;
			}
			else if(StringUtils.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_P))
			{
				return;
			}
			else
			{
				throw new CustomException("LOT-0080", lotData.getKey().getLotName());
			}
		}

	}

	/**
	 * must be in here at last point of event
	 * @author hwlee89
	 * @since 2015-11-27
	 * @param doc
	 * @param productList
	 */
	public void setLotGrade(String lotName, EventInfo eventInfo)
	{
		try
		{
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);
			List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotName);

			HashMap<String, Integer> index = new HashMap<String, Integer>();
			index.put("W", 1);
			index.put("S", 2);
			index.put("N", 3);
			index.put("R", 4);
			index.put("P", 5);
			index.put("G", 6);

			TreeMap<Integer, String> gradeMap = new TreeMap<Integer, String>();
			for(Product product : productList)
			{
				if(index.get(product.getProductGrade()) != null)
				{
					gradeMap.put(index.get(product.getProductGrade()), product.getProductGrade());
				}
			}

			String lotGrade = gradeMap.get(gradeMap.firstKey());
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

			//validation.
			CommonValidation.checkLotShippedState(lotData);

			eventInfo = EventInfoUtil.makeEventInfo("ChangeGrade", eventInfo.getEventUser(), eventInfo.getEventComment(), "", "");

			List<ProductPGS> productPGS = MESLotServiceProxy.getLotInfoUtil().getAllProductPGSSequence(lotData);

			ChangeGradeInfo changeGradeInfo =
					MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGS);

			MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
		}
		catch(CustomException ex)
		{
			//Not Found productList
			log.info("Not Found productList");
		}
	}

	/**
	 * AdjustProductQuantity
	 * @since 2016.04.27
	 * @author jhyeom
	 * @param eventInfo
	 * @param eleLotList
	 * @throws CustomException
	 */
	public String AdjustProductQuantity(EventInfo eventInfo, Element eleLotList) throws CustomException
	{
		String productCount = "";
		log.info("Change ProductQuantity By MODCUT begin...");
		try
		{
			if (eleLotList != null) {

				productCount = SMessageUtil.getChildText(eleLotList, "PRODUCTCOUNT", true);

				for (@SuppressWarnings("rawtypes")
				Iterator iterator = eleLotList.getChildren().iterator(); iterator.hasNext();)
				{
					Element lot = (Element) iterator.next();

					String eLotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
					String eSelectedQty = SMessageUtil.getChildText(lot, "COUNT", true); //lot.getChild("COUNT").getText();

					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Lot lotData = CommonUtil.getLotInfoByLotName(eLotName);
					Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(eLotName));

					double currentQty = lotData.getProductQuantity();
					double totalQty = currentQty - Integer.valueOf(eSelectedQty);

					lotData.setProductQuantity(totalQty);

					LotServiceProxy.getLotService().update(lotData);
					eventInfo.setEventName("AdjustQty");
					SetEventInfo setEventInfo = new SetEventInfo();

					LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
				}
			}
		}
		catch (Exception ex)
		{
			log.error(String.format("Failed the changeLotProductQuantityByMODCUT"));
			log.error(ex);
		}

		return productCount;
	}

	/**
	 *
	 * 160503 by AIM System :
	 * @author AIM System
	 * @since 2016.05.03
	 * @param eventInfo
	 * @param sProductRequestName
	 * @param releaseQuantity
	 * @throws CustomException
	 */
	public int reserveLotPosition(ProductRequestPlan pPlan)
			throws CustomException
	{
		int position = 1;

		try
		{
			ReserveLot rLot = new ReserveLot();

			String condition = "productRequestName = ? and machineName = ? and planReleasedtime = ? "
					+ "and position = (select max(position) from ct_reserveLot Where productRequestName = ? and machineName = ? and planReleasedtime = ? )";
			Object bindSet[] = new Object[]{pPlan.getKey().getProductRequestName(), pPlan.getKey().getAssignedMachineName(),
					pPlan.getKey().getPlanReleasedTime(), pPlan.getKey().getProductRequestName(),
					pPlan.getKey().getAssignedMachineName(), pPlan.getKey().getPlanReleasedTime()};
			List<ReserveLot> reserveLotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			if(reserveLotList.size() > 0 && reserveLotList != null)
			{
				rLot = reserveLotList.get(0);

				position = (int)rLot.getPosition() + 1;
			}

			return position;
		}
		catch(Exception e)
		{
			log.error(e);
		}

		return position;
	}

	/**
	 * 160630 by swcho : refactoring
	 * @since 2016.06.14
	 * @author xzquan
	 * @param lotData
	 * @param doc
	 * @return
	 */
	public Lot shipLot(Lot lotData, Document doc)
	{
		try
		{
			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());

			
			//if(!operationData.getProcessOperationType().equals("Ship"))
			if(!StringUtil.equals("Ship", operationData.getProcessOperationType()))
			{
				log.debug("Process Operation is not Ship. Skip Auto Ship.");
				return lotData;
			}

			if(!StringUtils.isEmpty(lotData.getProcessGroupName()))
			{
				log.debug("Process Group is Empty. Skip Auto Ship.");
				return lotData;
			}


			ProductRequestKey woKey = new ProductRequestKey(lotData.getProductRequestName());
			ProductRequest woData = ProductRequestServiceProxy.getProductRequestService().selectByKey(woKey);
			
			//if(woData.getUdfs().get("autoShipFlag").equals("Y"))
			if(StringUtil.equals("Y", woData.getUdfs().get("autoShipFlag")))
			{
				String destFactory = getAutoShippingFactory(lotData);

				Document shipDoc = writeShipLotRequest(doc, lotData.getFactoryName(), lotData.getKey().getLotName(), lotData.getProductSpecName(), lotData.getProductionType(), destFactory);

				InvokeUtils.invokeMethod(InvokeUtils.newInstance(ShipLot.class.getName(), null, null), "execute", new Object[] {shipDoc});
				log.info("Auto Ship Success.");

				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
			}
		}
		catch (Exception ex)
		{
			log.warn("Ship Lot Fail.");
		}

		return lotData;
	}

	/**
	 * @author aim
	 * @since 2015.04.15
	 * @return
	 * @throws CustomException
	 */
	public Document writeShipLotRequest(Document doc, String factoryName, String lotName, String productSpecName, String productionType, String destinationFactoryName)
			throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "ShipLot");

		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
		Element eleLotTemp = new Element("LOT");
		Element eleLotListTemp = new Element("LOTLIST");

		Element element1 = new Element("FACTORYNAME");
		element1.setText(factoryName);
		eleLotTemp.addContent(element1);

		Element element2 = new Element("LOTNAME");
		element2.setText(lotName);
		eleLotTemp.addContent(element2);

		Element element3 = new Element("PRODUCTSPECNAME");
		element3.setText(productSpecName);
		eleLotTemp.addContent(element3);

		Element element4 = new Element("PRODUCTIONTYPE");
		element4.setText(productionType);
		eleLotTemp.addContent(element4);

		Element element5 = new Element("DESTINATIONFACTORYNAME");
		element5.setText(destinationFactoryName);
		eleLotTemp.addContent(element5);

		eleLotListTemp.addContent(eleLotTemp);
		eleBodyTemp.addContent(eleLotListTemp);

		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);

		return doc;
	}

	public static void checkDataLot(List<Lot> lotList, String lotProcessState ) throws CustomException
	{
		if(StringUtil.equals(lotList.get(0).getLotProcessState(), lotProcessState))
		{
			throw new CustomException("LOT-9003", lotList.get(0).getKey().getLotName() +". Current State is " + lotList.get(0).getLotProcessState());
		}

		if(lotList.size() > 0)
		{
			String lotNames = "";

			if ( lotList.size() > 1000 )
			{
				int iFromIndex = 0;
				int toIndex = 1000;

				List<Lot> tempList = new ArrayList<Lot>();
				for ( iFromIndex = 0; iFromIndex < lotList.size(); )
				{
					lotNames = "";
					tempList = new ArrayList<Lot>(lotList.subList(iFromIndex, toIndex));


					for ( Lot lot : tempList )
					{
						String lotName = lot.getKey().getLotName();

						if ( StringUtils.isNotEmpty(lotName) ) lotNames += "'" + lotName + "'" + ",";
					}

					if ( StringUtils.isNotEmpty(lotNames) )
					{
						lotNames = lotNames.substring(0, lotNames.length() - 1);

						@SuppressWarnings ( "unchecked" )
						List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(" SELECT DISTINCT LOTPROCESSSTATE FROM LOT WHERE LOTNAME IN (" + lotNames + ")",
								new Object[] {});

						if ( result != null && result.size() > 0 )
						{
							if ( result.size() > 1 )
							{
								throw new CustomException("LOT-0108", "");
							}
							else
							{
								if (lotProcessState != null)
								{
									//if ( !result.get(0).get("LOTPROCESSSTATE").toString().equals(lotProcessState) )
									if(!StringUtil.equals(lotProcessState, result.get(0).get("LOTPROCESSSTATE").toString()))
									{
										throw new CustomException("LOT-0108", "");
									}
								}
							}
						}
					}

					iFromIndex = toIndex;

					if ( iFromIndex + 1000 < lotList.size() )
					{
						toIndex = toIndex + 1000;
					}
					else
					{
						toIndex = lotList.size();
					}
				}
			}
			else
			{
				for ( Lot lot : lotList )
				{
					String lotName = lot.getKey().getLotName();

					if ( StringUtils.isNotEmpty(lotName) ) lotNames += "'" + lotName + "'" + ",";
				}

				if ( StringUtils.isNotEmpty(lotNames) )
				{
					lotNames = lotNames.substring(0, lotNames.length() - 1);

					@SuppressWarnings ( "unchecked" )
					List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(" SELECT DISTINCT LOTPROCESSSTATE FROM LOT WHERE LOTNAME IN (" + lotNames + ")",
							new Object[] {});

					if ( result != null && result.size() > 0 )
					{
						if ( result.size() > 1 )
						{
							throw new CustomException("LOT-0108", "");
						}
					}
				}
			}
		}
	}

	/**
	 * @author aim
	 * @since 2015.04.15
	 * @return
	 * @throws CustomException
	 */
	public String getAutoShippingFactory(Lot lotData)
			throws CustomException
	{
		String destFactory = "";

		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT P.TOFACTORYNAME ");
		sql.append("   FROM LOT L, TPPOLICY T, POSFACTORYRELATION P ");
		sql.append("  WHERE     1 = 1 ");
		sql.append("        AND T.CONDITIONID = P.CONDITIONID ");
		sql.append("        AND T.FACTORYNAME = L.FACTORYNAME ");
		sql.append("        AND T.PRODUCTSPECNAME = L.PRODUCTSPECNAME ");
		sql.append("        AND L.LOTNAME = :LOTNAME ");
		sql.append("        AND P.JOBTYPE = :JOBTYPE ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotData.getKey().getLotName());
		bindMap.put("JOBTYPE", "Auto");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if(sqlResult.size() > 1 || sqlResult.size() == 0){
			throw new CustomException("LOT-0090", lotData.getKey().getLotName());
		}

		destFactory = sqlResult.get(0).get("TOFACTORYNAME").toString();

		return destFactory;
	}
	
	/**
	 *
	 * @author swcho
	 * @since 2016.09.09
	 * @param eventInfo
	 * @param carrierName
	 * @param lotName
	 * @param productList
	 * @return
	 * @throws CustomException
	 */
	public Lot getTrackOutLot(EventInfo eventInfo, String carrierName, String lotName, List<Element> productList)
			throws CustomException
	{
		Lot trackOutLot;
		String trackOutLotName = "";
		boolean mergeFlag = true;

		if (!mergeFlag)
		{
			//target one
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLotName);
			trackOutLot = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(trackOutLotName));

			//format raw Product list
			for (int idx = 0;idx < productList.size();idx++)
			{
				String currentProductName = SMessageUtil.getChildText(productList.get(idx), "PRODUCTNAME", true);
				//String currentLotName = SMessageUtil.getChildText(productList.get(idx), "LOTNAME", true);
				String currentLotName = MESProductServiceProxy.getProductServiceUtil().getProductData(currentProductName).getLotName();

				//if (!currentLotName.equals(trackOutLot.getKey().getLotName()))
				if(!StringUtil.equals(trackOutLot.getKey().getLotName(), currentLotName))
				{
					productList.remove(idx);
					idx--;
				}
			}

			//compose with remain
			if (productList.size() < 1)
			{//nothing to process then deassign nothing to delivery
				if (!trackOutLot.getCarrierName().isEmpty())
				{
					Durable srcCarrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trackOutLot.getCarrierName());

					List<ProductU> productUSequence = MESLotServiceProxy.getLotServiceUtil().setProductUSequence(trackOutLot.getKey().getLotName());
					DeassignCarrierInfo deassignInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(trackOutLot, srcCarrierData, productUSequence);

					eventInfo.setEventName("DeassignCarrier");
					trackOutLot = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(trackOutLot, deassignInfo, eventInfo);
				}

				trackOutLot = null;
			}
			else if (productList.size() < trackOutLot.getProductQuantity())
			{//split case
				//Split garbage to garbage
				log.info(String.format("Lot[%s] is doing split with [%d] of [%s]", trackOutLot.getKey().getLotName(), productList.size(), trackOutLot.getProductQuantity()));

				trackOutLot = MESLotServiceProxy.getLotServiceUtil().splitLot(eventInfo, productList, trackOutLot);

				log.info(String.format("Lot[%s] has completed to split into Lot[%s]", trackOutLot.getKey().getLotName(), trackOutLot.getKey().getLotName()));

				//parent Lot deassign if still assigned
				Lot parentLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(trackOutLot.getParentLotName());
				if (!parentLotData.getCarrierName().isEmpty())
				{
					Durable srcCarrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(parentLotData.getCarrierName());

					List<ProductU> productUSequence = MESLotServiceProxy.getLotServiceUtil().setProductUSequence(parentLotData.getKey().getLotName());
					DeassignCarrierInfo deassignInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(parentLotData, srcCarrierData, productUSequence);

					eventInfo.setEventName("DeassignCarrier");
					trackOutLot = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(parentLotData, deassignInfo, eventInfo);
				}
			}
		}
		else
		{//normal case
			trackOutLot = MESLotServiceProxy.getLotServiceUtil().composeLot(eventInfo, carrierName, productList);

			if(trackOutLot != null && trackOutLot.getCarrierName().isEmpty())
			{
				trackOutLot.setCarrierName(carrierName);
			}
		}

		return trackOutLot;
	}

	/**
	 * Lot judge package
	 * @author swcho
	 * @since 2016.11.16
	 * @param eventInfo
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public Lot changeGrade(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(lotData.getKey().getLotName());

		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

		String lotGrade = MESLotServiceProxy.getLotServiceUtil().decideLotJudge(lotData, "", productPGSRCSequence);

		if(!StringUtil.equals(lotData.getLotGrade(), lotGrade))
		{
			for (ProductPGSRC productPGSRC : productPGSRCSequence)
			{
				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productPGSRC.getProductName());

				ProductPGS productPGS = new ProductPGS();
				productPGS.setProductName(productPGSRC.getProductName());
				productPGS.setProductGrade(productPGSRC.getProductGrade());
				productPGS.setPosition(productPGSRC.getPosition());
				productPGS.setSubProductQuantity1(productData.getSubProductQuantity1());
				productPGS.setSubProductQuantity2(productData.getSubProductQuantity2());
				productPGS.setUdfs(productData.getUdfs());

				productPGSSequence.add(productPGS);
			}

			kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGSSequence);
			//eventInfo.setEventName("ChangeGrade");
			lotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
		}

		return lotData;
	}

	/**
	 * deleteFlowSamplingData
	 * @author hwlee89
	 * @since 2016.12.13
	 * @param afterTrackOutLot
	 * @param productList
	 * @return
	 * @throws CustomException
	 */
	public void deleteFlowSamplingData(Lot afterTrackOutLot, List<Element> productList)
			throws CustomException
	{
		List<FlowSampleLot> flowSampleLotList = null;
		String condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND toProcessflowname = ? AND toprocessoperationname = ?";
		Object[] bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
				afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProcessFlowName(),
				afterTrackOutLot.getProcessOperationName()};
		try{
			flowSampleLotList = ExtendedObjectProxy.getFlowSampleLotService().select(condition, bindSet);
		}catch(Exception ex)
		{

		}


		if(flowSampleLotList == null)
			return;

		if(flowSampleLotList != null)
		{
			List<FlowSampleProduct> flowSampleProductList = null;

			try{
				condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND toProcessflowname = ? AND toprocessoperationname = ? ";
				bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProcessFlowName(),
						afterTrackOutLot.getProcessOperationName()};
				flowSampleProductList = ExtendedObjectProxy.getFlowSampleProductService().select(condition, bindSet);

				for(FlowSampleProduct flowSampleProduct : flowSampleProductList)
				{
					ExtendedObjectProxy.getFlowSampleProductService().delete(flowSampleProduct);
				}
			}
			catch(Exception ex)
			{

			}

			try
			{
				flowSampleProductList = null;
				condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND toProcessflowname = ? AND toProcessOperationName = ? ";
				bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessOperationName()};

				flowSampleProductList = ExtendedObjectProxy.getFlowSampleProductService().select(condition, bindSet);
			}
			catch(Exception ex)
			{
				log.debug("Not Found SampleProduct");
			}

			if(flowSampleProductList == null)
			{
				try{
					condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND toProcessflowname = ? AND toProcessOperationName = ? ";
					bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
							afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessOperationName()};
					List<FlowSampleLot> flowSampleInfolist = ExtendedObjectProxy.getFlowSampleLotService().select(condition, bindSet);

					FlowSampleLot flowSampleInfo = flowSampleInfolist.get(0);
					ExtendedObjectProxy.getFlowSampleLotService().delete(flowSampleInfo);
				}
				catch(Exception ex)
				{
					log.info("Delete samplelot");
				}
			}
		}
	}

	/* 20190424, hhlee, delete, Not used ==>> */
	///**
	// * doAfterHoldbyCarrier
	// * @author aim System
	// * @param eventInfo
	// * @param carrierName
	// * @param lotName
	// * @param flag
	// * @param doc
	// * @return
	// */
	//public void doAfterHoldbyCarrier(EventInfo eventInfo, String carrierName,Lot lotdata, String flag, String errorComment /*, String lotName, List<Lot> lotList, Document doc*/)
	//{
	//	//Start a new Transaction
	//	GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
	//	
	//	String holdCode = StringUtil.EMPTY;
	//	String holdComment = StringUtil.EMPTY ;
	//	List<Lot>lotList ;
	//	String ErrorComment = StringUtil.isNotEmpty(errorComment)? ("ErrorComment: " + errorComment) : "";
	//	
	//	try
	//	{
	//		lotList = CommonUtil.getLotListByCarrier(carrierName, false);
	//	}
	//	catch (CustomException e)
	//	{
	//		lotList = new ArrayList<Lot>();
	//	}
	//	
	//	if(lotdata != null && !this.checkLotExistence(lotList, lotdata))
	//		lotList.add(lotdata);
	//	
	//	for (Lot lotData : lotList)
	//	{
	//		try
	//		{
	//			if(flag.equals("SM"))
	//				holdCode = "SMHL";
	//			else if(flag.equals("LA"))
	//				holdCode = "LAHL";
	//			else if(flag.equals("LS"))
	//				holdCode = "LSHL";
	//			else if(flag.equals("LE"))
	//				holdCode = "LEHL";
	//			else if(flag.equals("VM"))
	//				holdCode = "VM-001";
	//			else if(flag.equals("SM01")) /* <<== 20180601, Modify , hhlee */
	//			    holdCode = "BCHL";       /* <<== 20180601, Modify , hhlee */
	//			else if(flag.equals("CD")) /* <<== 20180619, Modify , hhlee */
    //                holdCode = "ABND";       /* <<== 20180619, Modify , hhlee */
	//			else if(flag.equals("EL")) /* <<== 20180619, Modify , hhlee */
    //                holdCode = "ELHL";       /* <<== 20180619, Modify , hhlee */
	//			
	//			if(flag.equals("SM"))
	//				holdComment = "SlotMap mismatch Hold. " +  ErrorComment;
	//			else if(flag.equals("LA"))
	//				holdComment = "Lot Process Abort Hold. " + ErrorComment;
	//			else if(flag.equals("LS"))
	//				holdComment = "Lot Process Start error Hold. " + ErrorComment;
	//			else if(flag.equals("LE"))
	//				holdComment = "Lot Process End error Hold. " + ErrorComment;
	//			else if(flag.equals("VM"))
	//				holdComment = "Current Operation not exit this Machine. " + ErrorComment;
	//			else if(flag.equals("SM01"))                    /* <<== 20180601, Modify , hhlee */
	//			    holdComment = eventInfo.getEventComment();  /* <<== 20180601, Modify , hhlee */
	//			else if(flag.equals("CD"))                    /* <<== 20180619, Modify , hhlee */
    //                holdComment = eventInfo.getEventComment(); // "CSTInfoDownLoadSend error Hold. " + ErrorCommont;  /* <<== 20180619, Modify , hhlee */
    //            else if(flag.equals("EL"))                    /* <<== 20180619, Modify , hhlee */
    //                    holdComment = "ELA Q-time Hold. " + ErrorComment;  /* <<== 20180619, Modify , hhlee */
    //
	//			eventInfo.setEventName("Hold");
	//			eventInfo.setEventComment(holdComment);
    //
	//			MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, lotData, "HoldLot", holdCode);
	//			MESLotServiceProxy.getLotServiceUtil().executeMultiHold(eventInfo, lotData, holdCode,""); /* add, 20180608 */
	//			
	//			//Commit new Transaction
	//			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
	//		}
	//		catch (CustomException ce)
	//		{
	//			log.error(String.format("[%s]%s", "HoldLotFail",ce.errorDef.getLoc_errorMessage()));
	//			//RollbackTransaction
	//			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
	//		}
	//	}
	//}
	/* <<== 20190424, hhlee, delete, Not used */
	
	/* 20190424, hhlee, delete, Not used ==>> */
	//public void doAfterHoldbyCarrier(EventInfo eventInfo, String carrierName,Lot lotdata, 
	//                                 String reasonCodeType, String reasonCode, String errorComment)
    //{
	//    List<Lot>lotList = null;
    //   //String ErrorComment = StringUtil.isNotEmpty(errorComment)? ("ErrorComment: " + errorComment) : "";
    //   //String holdComment = eventInfo.getEventComment() + " " + ErrorComment ;
    //    String holdComment = StringUtil.isNotEmpty(errorComment)? eventInfo.getEventComment() + " " + ("ErrorComment: " + errorComment) : eventInfo.getEventComment();
    //    
    //    eventInfo.setCheckTimekeyValidation(false);
    //    eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
    //    /* 20181128, hhlee, EventTime Sync */
    //    //eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
    //    
    //    try
    //    {
    //        lotList = CommonUtil.getLotListByCarrier(carrierName, false);
    //    }
    //    catch (CustomException e)
    //    {
    //        lotList = new ArrayList<Lot>();
    //    }
    //    
    //    if(lotdata != null && !this.checkLotExistence(lotList, lotdata))
    //        lotList.add(lotdata);
    //    
    //    for (Lot lotData : lotList)
    //    {
    //        try
    //        {
    //            //eventInfo.setEventName("Hold");
    //            eventInfo.setEventComment(holdComment);
    //            
    //            /* 20180921, hhlee, add, Lot Note ==>> */
    //            Map<String,String> lotUdfs = lotData.getUdfs();
    //            lotUdfs.put("NOTE", holdComment);
    //            lotData.setUdfs(lotUdfs);
    //            //LotServiceProxy.getLotService().update(lotData);
    //            /* <<== 20180921, hhlee, add, Lot Note */
    //            
    //            MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, lotData, reasonCodeType, reasonCode);
    //            MESLotServiceProxy.getLotServiceUtil().executeMultiHold(eventInfo, lotData, reasonCode," ");
    //            
    //            /* 20181212, hhlee, ==>> */
    //            log.info("[EXECUTEMULTIHOLD COMPLETE] =====================================================");    
    //            
    //            lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
    //            
	//    		// For Clear Note, Add By Park Jeong Su
    //            lotData.getUdfs().put("NOTE", "");
	//            LotServiceProxy.getLotService().update(lotData);
    //            
    //        }
    //        catch (CustomException ce)
    //        {
    //            /* 20181212, hhlee, ==>> */
    //            log.error("[HoldLotFail] " + ce.errorDef.getLoc_errorMessage());
    //            //RollbackTransaction
    //            GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
    //        }
    //        /* 20181211, hhlee, ==>> */
    //        catch (Exception ex)
    //        {
    //            /* 20181212, hhlee, ==>> */
    //            log.error("[HoldLotFail] " + ex.getMessage() + " - " + ex.getStackTrace());
    //            //RollbackTransaction
    //            GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
    //        }
    //    }
    //}
	/* <<== 20190424, hhlee, delete, Not used */
	
	/* 20190424, hhlee, delete, Not used ==>> */
	//public void doAfterHoldbyCarrier(EventInfo eventInfo, String carrierName,Lot lotdata, 
    //        String reasonCodeType, String reasonCode, String errorComment, String department)
    //{
    //    List<Lot>lotList = null;
    //    //String ErrorComment = StringUtil.isNotEmpty(errorComment)? ("ErrorComment: " + errorComment) : "";
    //    //String holdComment = eventInfo.getEventComment() + " " + ErrorComment ;
    //    String holdComment = StringUtil.isNotEmpty(errorComment)? eventInfo.getEventComment() + " " + ("ErrorComment: " + errorComment) : eventInfo.getEventComment();
    //    
    //    eventInfo.setCheckTimekeyValidation(false);
    //    eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
    //    eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
    //    
    //    try
    //    {
    //        lotList = CommonUtil.getLotListByCarrier(carrierName, false);
    //    }
    //    catch (CustomException e)
    //    {
    //        lotList = new ArrayList<Lot>();
    //    }
    //    
    //    if(lotdata != null && !this.checkLotExistence(lotList, lotdata))
    //    lotList.add(lotdata);
    //    
    //    for (Lot lotData : lotList)
    //    {
    //        try
    //        {
    //            //eventInfo.setEventName("Hold");
    //            eventInfo.setEventComment(holdComment);
    //            
    //            /* 20180921, hhlee, add, Lot Note ==>> */
    //            Map<String,String> lotUdfs = lotData.getUdfs();
    //            lotUdfs.put("NOTE", holdComment);
    //            lotData.setUdfs(lotUdfs);
    //            //LotServiceProxy.getLotService().update(lotData);
    //            /* <<== 20180921, hhlee, add, Lot Note */
    //            
    //            MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, lotData, reasonCodeType, reasonCode);
    //            MESLotServiceProxy.getLotServiceUtil().executeMultiHold(eventInfo, lotData, reasonCode, department); 
    //            
    //            /* 20181212, hhlee, ==>> */
    //            log.info("[EXECUTEMULTIHOLD COMPLETE] =====================================================");                
    //            lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
    //
	//    		// For Clear Note, Add By Park Jeong Su
    //            lotData.getUdfs().put("NOTE", "");
	//            LotServiceProxy.getLotService().update(lotData);
    //        
    //        }
    //        catch (CustomException ce)
    //        {
    //            /* 20181212, hhlee, ==>> */
    //            log.error("[HoldLotFail] " + ce.errorDef.getLoc_errorMessage());
    //            //RollbackTransaction
    //            GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
    //        }
    //        /* 20181211, hhlee, ==>> */
    //        catch (Exception ex)
    //        {
    //            /* 20181212, hhlee, ==>> */
    //            log.error("[HoldLotFail] " + ex.getMessage() + " - " + ex.getStackTrace());
    //            //RollbackTransaction
    //            GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
    //        }
    //    }
    //}
	/* <<== 20190424, hhlee, delete, Not used */
	
	/* 20190426, hhlee, modify, add parameter(isFutureHoldLot) */
	public void doAfterHoldbyCarrier(EventInfo eventInfo, String carrierName,Lot lotdata, String reasonCodeType, 
            String reasonCode, String errorComment, boolean isAppliedEventComment, boolean isFutureHoldLot, String messageEventName, String department) throws CustomException
    {
        List<Lot>lotList = null;
        //String ErrorComment = StringUtil.isNotEmpty(errorComment)? ("ErrorComment: " + errorComment) : "";
        //String holdComment = eventInfo.getEventComment() + " " + ErrorComment ;
        String holdComment = StringUtil.isNotEmpty(errorComment)? eventInfo.getEventComment() + " " + ("ErrorComment: " + errorComment) : eventInfo.getEventComment();
        
        eventInfo.setCheckTimekeyValidation(false);
        eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
        eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
        
        try
        {
        	//lotList = CommonUtil.getLotListByCarrier(carrierName, false);
        	lotList = CommonUtil.getLotListByCarrierLotStateReleasedAndCompleted(carrierName, false);
        }
        catch (CustomException e)
        {
         lotList = new ArrayList<Lot>();
        }
        
        if(lotdata != null && !this.checkLotExistence(lotList, lotdata))
        lotList.add(lotdata);
        
        for (Lot lotData : lotList)
        {
            try
            {
                /* 20190424, hhlee, modify, check lotProcessState ==>> */
                /* 20190426, hhlee, modify, add parameter(isFutureHoldLot) */
                //if(StringUtil.equals(lotData.getLotProcessState(),
                //        GenericServiceProxy.getConstantMap().Lot_Run))
                if(isFutureHoldLot && StringUtil.equals(lotData.getLotProcessState(),
                            GenericServiceProxy.getConstantMap().Lot_Run))
                {
                    /* 20190425, hhlee, Modify, change reasoncode */
                    //this.futureHoldLot(eventInfo, lotData, eventInfo.getEventUser(), "AHOLD", "VRHL");
                    this.futureHoldLot(eventInfo, lotData, eventInfo.getEventUser(), "AHOLD", reasonCode);
                }
                /* <<== 20190424, hhlee, modify, check lotProcessState */
                else
                {
                    if(isAppliedEventComment)
                    {
                        //eventInfo.setEventName("Hold");
                        eventInfo.setEventComment(holdComment);
                    }
                    else
                    {
                        //eventInfo.setEventName("Hold");
                        eventInfo.setEventComment(messageEventName);
                    }
                    
                    /* 20180921, hhlee, add, Lot Note ==>> */
                    Map<String,String> lotUdfs = lotData.getUdfs();
                    lotUdfs.put("NOTE", holdComment);
                    lotData.setUdfs(lotUdfs);
                    //LotServiceProxy.getLotService().update(lotData);
                    /* <<== 20180921, hhlee, add, Lot Note */
                    
                    MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, lotData, reasonCodeType, reasonCode);
                    MESLotServiceProxy.getLotServiceUtil().executeMultiHold(eventInfo, lotData, reasonCode, department); 
                    
                    /* 20181212, hhlee, ==>> */
                    log.info("[EXECUTEMULTIHOLD COMPLETE] =====================================================");    
    
                    // Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//                    lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//    
//    	    		// For Clear Note, Add By Park Jeong Su
//                    lotData.getUdfs().put("NOTE", "");
//    	            LotServiceProxy.getLotService().update(lotData);
                    Map<String, String> updateUdfs = new HashMap<String, String>();
        			updateUdfs.put("NOTE", "");
        			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
                }
                log.info(lotData.getKey().getLotName() +  " doAfterHoldbyCarrier Sucess");
            }
            catch (CustomException ce)
            {
                /* 20181212, hhlee, ==>> */
                log.error("[HoldLotFail] " + ce.errorDef.getLoc_errorMessage());
                log.info("CustomException doAfterHoldbyCarrier");
                // RollbackTransaction Á¦°Å LotServiceUtil ¿¡¼­ TransAction °ü¸® ¾ÈÇÏ°í »óÀ§ ÀÌº¥Æ®¿¡¼­ °ü¸®
                //RollbackTransaction
                //GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
                throw ce;
            }
            /* 20181211, hhlee, ==>> */
            catch (Exception ex)
            {
                /* 20181212, hhlee, ==>> */
                log.error("[HoldLotFail] " + ex.getMessage() + " - " + ex.getStackTrace());
                log.info("Exception doAfterHoldbyCarrier");
                // RollbackTransaction Á¦°Å LotServiceUtil ¿¡¼­ TransAction °ü¸® ¾ÈÇÏ°í »óÀ§ ÀÌº¥Æ®¿¡¼­ °ü¸®
                //RollbackTransaction
                //GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
                CustomException ce = new CustomException(ex);
                
                log.error(ce.errorDef);
                log.error(ce.getStackTrace());
                log.error(ce.getMessage());

                throw ce;
            }
        }
    }
	
	/* 20190424, hhlee, delete, Not used ==>> */
	///**
	// * @Name     doAfterHoldbyCarrierLot
	// * @since    2018. 9. 7.
	// * @author   hhlee
	// * @contents 
	// *           
	// * @param eventInfo
	// * @param lotdata
	// * @param reasonCodeType
	// * @param reasonCode
	// * @param errorComment
	// */
	//public void doAfterHoldbyCarrier(EventInfo eventInfo ,Lot lotdata, 
    //        String reasonCodeType, String reasonCode, String errorComment)
    //{
    //    String ErrorComment = StringUtil.isNotEmpty(errorComment)? ("ErrorComment: " + errorComment) : "";
    //    String holdComment = eventInfo.getEventComment() + " " + ErrorComment ;
    //    
    //    eventInfo.setCheckTimekeyValidation(false);
    //    eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
    //    eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
    //    
    //    try
    //    {
    //        eventInfo.setEventName("Hold");
    //        eventInfo.setEventComment(holdComment);
    //        
    //        MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, lotdata, reasonCodeType, reasonCode);
    //        MESLotServiceProxy.getLotServiceUtil().executeMultiHold(eventInfo, lotdata, reasonCode,""); 
    //    }
    //    catch (CustomException ce)
    //    {
    //        log.error(String.format("[%s]%s", "HoldLotFail",ce.errorDef.getLoc_errorMessage()));
    //        //RollbackTransaction
    //        GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
    //    }        
    //}	
	/* <<== 20190424, hhlee, delete, Not used */
	
	/* 20190424, hhlee, delete, Not used ==>> */
	///**
    // * doAfterHoldbyCarrier
    // * @author aim System
    // * @param eventInfo
    // * @param carrierName
    // * @param lotName
    // * @param flag
    // * @param doc
    // * @return
    // */
    //public void doAfterHoldbyCarrierNotBeginTraction(EventInfo eventInfo, String carrierName,Lot lotdata, String flag, String errorComment /*, String lotName, List<Lot> lotList, Document doc*/)
    //{
    //    String holdCode = StringUtil.EMPTY;
    //    String holdComment = StringUtil.EMPTY ;
    //    List<Lot>lotList ;
    //    String ErrorComment = StringUtil.isNotEmpty(errorComment)? ("ErrorComment: " + errorComment) : "";
    //    
    //    try
    //    {
    //        lotList = CommonUtil.getLotListByCarrier(carrierName, false);
    //    }
    //    catch (CustomException e)
    //    {
    //        lotList = new ArrayList<Lot>();
    //    }
    //    
    //    if(lotdata != null && !this.checkLotExistence(lotList, lotdata))
    //        lotList.add(lotdata);
    //    
    //    for (Lot lotData : lotList)
    //    {
    //        try
    //        {
    //            if(flag.equals("SM"))
    //                holdCode = "SMHL";
    //            else if(flag.equals("LA"))
    //                holdCode = "LAHL";
    //            else if(flag.equals("LS"))
    //                holdCode = "LSHL";
    //            else if(flag.equals("LE"))
    //                holdCode = "LEHL";
    //            else if(flag.equals("VM"))
    //                holdCode = "VM-001";
    //            else if(flag.equals("SM01")) /* <<== 20180601, Modify , hhlee */
    //                holdCode = "BCHL";       /* <<== 20180601, Modify , hhlee */
    //            else if(flag.equals("CD")) /* <<== 20180619, Modify , hhlee */
    //                holdCode = "ABND";       /* <<== 20180619, Modify , hhlee */
    //            else if(flag.equals("EL")) /* <<== 20180619, Modify , hhlee */
    //                holdCode = "ELHL";       /* <<== 20180619, Modify , hhlee */
    //            
    //            if(flag.equals("SM"))
    //                holdComment = "SlotMap mismatch Hold. " +  ErrorComment;
    //            else if(flag.equals("LA"))
    //                holdComment = "Lot Process Abort Hold. " + ErrorComment;
    //            else if(flag.equals("LS"))
    //                holdComment = "Lot Process Start error Hold. " + ErrorComment;
    //            else if(flag.equals("LE"))
    //                holdComment = "Lot Process End error Hold. " + ErrorComment;
    //            else if(flag.equals("VM"))
    //                holdComment = "Current Operation not exit this Machine. " + ErrorComment;
    //            else if(flag.equals("SM01"))                    /* <<== 20180601, Modify , hhlee */
    //                holdComment = eventInfo.getEventComment();  /* <<== 20180601, Modify , hhlee */
    //            else if(flag.equals("CD"))                    /* <<== 20180619, Modify , hhlee */
    //                holdComment = eventInfo.getEventComment(); // "CSTInfoDownLoadSend error Hold. " + ErrorCommont;  /* <<== 20180619, Modify , hhlee */
    //            else if(flag.equals("EL"))                    /* <<== 20180619, Modify , hhlee */
    //                    holdComment = "ELA Q-time Hold. " + ErrorComment;  /* <<== 20180619, Modify , hhlee */
    //
    //            eventInfo.setEventName("Hold");
    //            eventInfo.setEventComment(holdComment);
    //
    //            MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, lotData, "HoldLot", holdCode);
    //            MESLotServiceProxy.getLotServiceUtil().executeMultiHold(eventInfo, lotData, holdCode,""); /* add, 20180608 */                
    //        }
    //        catch (CustomException ce)
    //        {
    //            log.error(String.format("[%s]%s", "HoldLotFail",ce.errorDef.getLoc_errorMessage()));                
    //        }
    //    }
    //}
    /* <<== 20190424, hhlee, delete, Not used */
    
	public boolean checkLotExistence(List<Lot>lotlist, Lot lotData)
	{
		log.info("checkLotExistence Started.");
		
		//not exist than false
		boolean existence = false;
		
		List<String> lotNameList = new ArrayList<String>();
		
		for(Lot lotdata : lotlist)
		{
			lotNameList.add(lotdata.getKey().getLotName());
		}
		
		for(String lotName : lotNameList)
		{
			if(StringUtil.equals(lotName, lotData.getKey().getLotName()))
			{
				existence = true;
				break;
			}		
		}
		
		log.info("checkLotExistence Ended.");
		
		return existence;
	}
	
	/**
	 * doAfterHold
	 * @author aim System
	 * @param eventInfo
	 * @param carrierName
	 * @param lotName
	 * @param flag
	 * @param doc
	 * @return
	 */
	public void doAfterHold(EventInfo eventInfo, String flag, Lot lotData)
	{
		String holdCode = StringUtil.EMPTY;
		String holdComment = StringUtil.EMPTY;

		try
		{
			if (holdCode.isEmpty() || holdCode.equals("0"))
			{
				if(flag.equals("SM"))
					holdCode = "SMHL";
				else if(flag.equals("LA"))
					holdCode = "LAHL";
				else if(flag.equals("LS"))
					holdCode = "LSHL";
				else if(flag.equals("NG"))
					holdCode = "NGHL";
				else if(flag.equals("PF"))
					holdCode = "PFHL";
				else if(flag.equals("VM"))
					holdCode = "VM-001";


				if(flag.equals("SM"))
					holdComment = "SlotMap mismatch Hold";
				else if(flag.equals("LA"))
					holdComment = "Lot Process Abort Hold";
				else if(flag.equals("LS"))
					holdComment = "Lot Process Start error Hold";
				else if(flag.equals("NG"))
					holdComment = "Lot Process End, PL/NG port Hold";
				else if(flag.equals("PF"))
					holdComment = "Lot Process End, ProcessingInfo F Hold";
				else if(flag.equals("VM"))
					holdComment = "Current Operation not exit this Machine";
			}

			eventInfo.setEventName("Hold");
			eventInfo.setEventComment(holdComment);

			MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, lotData, "HoldLot", holdCode);
		}
		catch (CustomException ce)
		{
			log.error(ce.errorDef.getLoc_errorMessage());
		}

	}

	/**
	 * doAfterHold For Abnormal End
	 * @author zhongsl
	 * @since 2017.04.10
	 * @param eventInfo
	 * @param carrierName
	 * @param lotName
	 * @param flag
	 * @param doc
	 * @return
	 */
	public void doAfterHoldForAbnormalEnd(EventInfo eventInfo, String carrierName, String lotName, String flag, List<Lot> lotList, Document doc)
	{
		String holdCode;
		String holdComment;
		Lot lotDataAbnormalEnd = new Lot();

		try
		{
			if(ExtendedObjectProxy.getFirstGlassLotService().getActiveLotNameByCarrier(carrierName).isEmpty())
			{
				lotDataAbnormalEnd = CommonUtil.getLotInfoBydurableName(carrierName);
			}
			else
			{
				String firstGlassLot = ExtendedObjectProxy.getFirstGlassLotService().getActiveLotNameByCarrier(carrierName);
				lotDataAbnormalEnd = CommonUtil.getLotInfoByLotName(firstGlassLot);
			}

			holdCode = "Abnormal End";
			holdComment = "MES Hold for that EAP/EAS don't report productList";

			eventInfo.setEventName("Hold");
			eventInfo.setEventComment(holdComment);

			MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, lotDataAbnormalEnd, "HoldLot", holdCode);
		}
		catch (CustomException ce)
		{
			log.error(ce.errorDef.getLoc_errorMessage());
		}
	}

	/**
	 * notBankLotCount
	 * @author aim System
	 * @since 2016.12.20
	 * @param lotList
	 * @return int
	 */
	public int notBankLotCount(List<Lot> lotList)
	{
		int cnt = 0;

		for (Lot subData : lotList)
		{
			ProcessOperationSpecKey poKey = new ProcessOperationSpecKey(subData.getFactoryName(), subData.getProcessOperationName(),subData.getProcessOperationVersion());
			ProcessOperationSpec poSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(poKey);

			if(!poSpec.getDetailProcessOperationType().equals("BANK"))
			{
				cnt++;
			}
		}

		return cnt;
	}

	/**
	 * sampling alteration
	 * @author swcho
	 * @since 2016.12.26
	 * @param eventInfo
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public Lot startInspection(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		//departure search
		String depOperationName;
		try
		{   //µ±Ç°ÖÆ³ÌµÄÉÏÒ»¸öÖÆ³ÌnodeID
			String depNodeId = NodeStack.getPreviousNode(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), lotData.getNodeStack());

			if (!depNodeId.isEmpty())
			{
				Node depNode = ProcessFlowServiceProxy.getNodeService().getNode(depNodeId);
				depOperationName = depNode.getNodeAttribute1();
			}
			else
			{
				depOperationName = lotData.getProcessOperationName();
			}
		}
		catch (NotFoundSignal ne)
		{
			depOperationName = lotData.getProcessOperationName();
		}
		catch (FrameworkErrorSignal fe)
		{
			log.error("Flow sampling alteration search failed then cancel");
			return lotData;
		}

		/** 20180213 NJPARK
		//destination sampling flow
		List<ListOrderedMap> nextSequence;
		try
		{	//2017-03-07 16:50 wuzhiming Ìí¼ÓÁË°´ÕÕÉè¶¨µÄ³é¼ìflowµÄÓÅÏÈ¼¶£¬ÓÅÏÈ¼¶×î´óµÄflow×îÏÈ×ö
			StringBuffer queryBuffer = new StringBuffer()
				.append("SELECT DISTINCT ").append("\n")
				.append("    SN.factoryName, SN.processFlowName, SN.nodeAttribute1 processOperationName, SN.nodeId, L.INSPECTPRIORITY").append("\n")
				.append("    FROM CT_FlowSampleLot S, Node N, Arc A, Node SN, ProcessFlow L").append("\n")
				.append("WHERE 1=1 ").append("\n")
				.append("    AND S.lotName = ? ").append("\n")
				.append("    AND S.factoryName = ? ").append("\n")
				.append("    AND S.productSpecName = ? ").append("\n")
				.append("    AND S.processFlowName = ? ").append("\n")
				.append("    AND S.toProcessFlowName = L.PROCESSFLOWNAME ").append("\n")
				.append("    AND S.processOperationName = ? ").append("\n")
				.append("    AND S.lotSampleFlag = 'Y' ").append("\n")
				.append("    AND N.factoryName = S.factoryName ").append("\n")
				.append("    AND N.processFlowName = S.toProcessFlowName ").append("\n")
				.append("    AND N.processFlowVersion = '00001' ").append("\n")
				.append("    AND N.nodeType = 'Start' ").append("\n")
				.append("    AND A.factoryName = N.factoryName ").append("\n")
				.append("    AND A.processFlowName = N.processFlowName ").append("\n")
				.append("    AND A.processFlowVersion = N.processFlowVersion ").append("\n")
				.append("    AND A.fromNodeId = N.nodeId ").append("\n")
				.append("    AND A.arcType = 'Normal' ").append("\n")
				.append("    AND SN.nodeId = A.toNodeId ").append("\n")
				.append("    ORDER BY L.INSPECTPRIORITY ASC");

			//zhongsl 2017.9.14
			String firstGlassActiveLot = ExtendedObjectProxy.getFirstGlassLotService().getActiveLotNameByCarrier(lotData.getCarrierName());
			if(firstGlassActiveLot.equals(lotData.getKey().getLotName()))
			{
				ProcessFlow flowData = GenericServiceProxy.getSpecUtil().getProcessFlow(lotData.getFactoryName(), lotData.getProcessFlowName(), "");
				ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
				if (flowData.getProcessFlowType().equals("Main") && StringUtil.equals(processOperationSpecData.getProcessOperationType(), "Inspection"))
				{
					if(!processOperationSpecData.getUdfs().get("MAINPROCESSOPERATIONNAME").isEmpty())
					{
						depOperationName = processOperationSpecData.getUdfs().get("MAINPROCESSOPERATIONNAME");
					}
				}
				else if(flowData.getProcessFlowType().equals("Main") && StringUtil.equals(processOperationSpecData.getProcessOperationType(), "Production"))
				{
					depOperationName = lotData.getUdfs().get("BEFOREOPERATIONNAME");
				}
			}

			nextSequence = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(),
							 new Object[] {lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), depOperationName});
		}
		catch (FrameworkErrorSignal fe)
		{
			nextSequence = new ArrayList<ListOrderedMap>();
		}

		log.info(String.format("Sampling flow alter remains by [%d]", nextSequence.size()));

		for (ListOrderedMap destination : nextSequence)
		{
			String destinedNodeStack = CommonUtil.getValue(destination, "NODEID");
			String toFlowName = CommonUtil.getValue(destination, "PROCESSFLOWNAME");
			String toOperationName = CommonUtil.getValue(destination, "PROCESSOPERATIONNAME");
			//return must be current location
			String returnFlowName = lotData.getProcessFlowName();
			String returnOperationName = lotData.getProcessOperationName();

			try
			{
				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());

				eventInfo.setEventName("Inspect");

				ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
													lotData.getProductionType(), lotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
													lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
													lotData.getProductRequestName(),
													lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
													lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
													lotData.getProcessFlowName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
													lotData.getProcessOperationName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
													toFlowName, toOperationName,
													returnFlowName, returnOperationName, "",
													lotData.getUdfs(), productUdfs,
													true);

				lotData = this.changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			}
			catch (Exception ex)
			{
				log.error(ex);
				log.error("Process Flow Alteration failed");
			}

			log.info(String.format("Lot[%s] settled at Flow[%s] Operation[%s] for sampling", lotData.getKey().getLotName(), toFlowName, toOperationName) );

			//once sampling move then exit
			break;
		}
		 */

		return lotData;
	}

	/**
	 * bankCheck
	 * @author aim System
	 * @since 2016.12.20
	 * @param lotList
	 * @return boolean
	 */
	public boolean bankCheck(List<Lot> lotList)
	{
		if(lotList != null && lotList.size() > 1)
		{
			for (Lot lotData : lotList)
			{
				ProcessOperationSpecKey poKey = new ProcessOperationSpecKey(lotData.getFactoryName(), lotData.getProcessOperationName(),lotData.getProcessOperationVersion());
				ProcessOperationSpec poSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(poKey);

				if(poSpec.getDetailProcessOperationType().equals("BANK"))
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * findNotBankLot
	 * @author aim System
	 * @since 2016.12.20
	 * @param lotList
	 * @return Lot
	 */
	public Lot findNotBankLot(List<Lot> lotList)
	{
		Lot lotData = new Lot();

		for (Lot subData : lotList)
		{
			ProcessOperationSpecKey poKey = new ProcessOperationSpecKey(subData.getFactoryName(), subData.getProcessOperationName(),subData.getProcessOperationVersion());
			ProcessOperationSpec poSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(poKey);

			if(!poSpec.getDetailProcessOperationType().equals("BANK"))
			{
				lotData = subData;
				break;
			}
		}

		return lotData;
	}

	/**
	 * RecreateAndCreateAllProducts
	 * @author 170502 add by lszhen
	 * @param lotName
	 * @param specData
	 * @param eventInfo
	 * @return
	 * @throws CustomException
	 */
	public Lot recreateAndCreateAllProducts(String lotName, ProductSpec specData, ProductSpec mainSpecData, EventInfo eventInfo) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		eventInfo.setEventName("Recreate");

		String newLotName = "";
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("FACTORYNAME", specData.getKey().getFactoryName());
		nameRuleAttrMap.put("PRODUCTSPECNAME1", specData.getKey().getProductSpecName());
		nameRuleAttrMap.put("PRODUCTSPECNAME2", mainSpecData.getKey().getProductSpecName());

		try
		{
			List<String> lstName = CommonUtil.generateNameByNamingRule("ENLotNaming", nameRuleAttrMap, 1);
			newLotName = lstName.get(0);
		}
		catch(Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}

		List<ProductNPGS> productNPGSSequence = new ArrayList<ProductNPGS>();
		String[] prdname = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
				"B", "C", "D", "E", "F", "G", "H", "J", "K", "L"};


		List<Product> products = LotServiceProxy.getLotService().allUnScrappedProducts(lotName);

		int prdcnt = 20 - products.size();
		for (Product product : products)
		{
			String nProductName = newLotName + prdname[prdcnt++];

			ProductNPGS productNPGS = MESLotServiceProxy.getLotInfoUtil().setProductNPGS(product, nProductName);

			productNPGSSequence.add(productNPGS);
		}

		RecreateAndCreateAllProductsInfo recreateAndCreateAllProductsInfo =
				MESLotServiceProxy.getLotInfoUtil().recreateAndCreateAllProductsInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), newLotName,
						lotData.getUdfs(), lotData.getNodeStack(), lotData.getPriority(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getProductionType(), productNPGSSequence,
						lotData.getProductRequestName(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProductType(), lotData.getSubProductType(), lotData.getSubProductQuantity1(), lotData.getSubProductQuantity2());

		MESLotServiceProxy.getLotServiceImpl().recreateAndCreateAllProducts(eventInfo, lotData, recreateAndCreateAllProductsInfo);

		Lot newLot = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);

		return newLot;
	}

	//20170526 by zhanghao getENUMDEFVALUE PortUseType TAG for check ProductType
	public static List<ListOrderedMap> getENUMPortUseTypeTAG(String portUseType)throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("   SELECT  D.TAG             "+ "\n");
		sqlBuffer.append("   FROM ENUMDEFVALUE D       "+ "\n");
		sqlBuffer.append("   WHERE 1 = 1               "+ "\n");
		sqlBuffer.append("	AND D.ENUMVALUE = ?        "+ "\n");
		sqlBuffer.append("	and D.ENUMNAME = ?        "+ "\n");
		String qryString = sqlBuffer.toString();
		Object[] bindSet = new String[] {portUseType, "PortUseType"};
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString, bindSet);
			if(!sqlResult.isEmpty())
				return sqlResult;
			else return null;
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "ENUMDEFVALUE (PortUseType TAG)", de.getMessage());
		}
	}

	//20170526 by zhanghao getENUMDEFVALUE PortUseType SEQ for check CarrierSpecType
	public static List<ListOrderedMap> getENUMPortUseTypeSEQ(String portUseType)throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("   SELECT DISTINCT REGEXP_SUBSTR (( SELECT D.SEQ FROM ENUMDEFVALUE D WHERE D.ENUMNAME=? and D.ENUMVALUE=?),'[^,]+',1,LEVEL)            "+ "\n");
		sqlBuffer.append("   FROM   DUAL      "+ "\n");
		sqlBuffer.append("   CONNECT BY REGEXP_SUBSTR (( SELECT D.SEQ FROM ENUMDEFVALUE D WHERE D.ENUMNAME=? and D.ENUMVALUE=?),'[^,]+',1,LEVEL) IS NOT NULL             "+ "\n");
		sqlBuffer.append("	order by 1     "+ "\n");
		String qryString = sqlBuffer.toString();
		Object[] bindSet = new String[] {"PortUseType", portUseType, "PortUseType", portUseType};
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString, bindSet);
			if(!sqlResult.isEmpty())
				return sqlResult;
			else return null;
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "ENUMDEFVALUE (PortUseType SEQ)", de.getMessage());
		}
	}

	//20170703 by zhanghao  get Machine Table AssignWorkOrder
	public static List<ListOrderedMap> getMachineAssignWorkOrder(String MachineName)throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("   SELECT P.PRODUCTREQUESTNAME         "+ "\n");
		sqlBuffer.append("    FROM MACHINE M, PRODUCTREQUEST P  WHERE     M.MACHINENAME = ?  "+ "\n");
		sqlBuffer.append("	 AND M.ASSIGNPRODUCTREQUEST = P.PRODUCTREQUESTNAME   "+ "\n");
		String qryString = sqlBuffer.toString();
		Object[] bindSet = new String[] {MachineName };
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString, bindSet);
			if(!sqlResult.isEmpty())
				return sqlResult;
			else return null;
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "ENUMDEFVALUE (PortUseType SEQ)", de.getMessage());
		}
	}

	/**
	 * check current Operation mapping TargetOperation
	 * @author zhongsl
	 * @since 2017.08.22
	 * @param lotData
	 * @return true?false
	 * @throws CustomException
	 */
	public boolean checkFirstGlassTargetOper(Lot lotData) throws CustomException
	{
		String currentOper  = lotData.getProcessOperationName();
		ProcessFlow currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		String FlowType = currentFlowData.getProcessFlowType();
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT J.jobName, J.factoryName, J.machineName, ").append("\n")
		.append("	   J.jobState, J.jobProcessState, J.TARGETOPERATIONNAME,").append("\n")
		.append("	   FL.lotName, FL.parentLotName, FL.activeState, ").append("\n")
		.append("      OL.carrierName, OL.productQuantity, ").append("\n")
		.append("      OL.lotState, OL.lotHoldState, OL.lotProcessState, OL.reworkState ").append("\n")
		.append("  FROM CT_FirstGlassJob J, CT_FirstGlassLot FL, Lot OL ").append("\n")
		.append("WHERE J.jobName = FL.jobName ").append("\n")
		//.append("  AND J.jobState = :JOBSTATE ").append("\n")
		//.append("  AND FL.activeState = :ACTIVESTATE ").append("\n")
		.append("  AND FL.lotName = OL.lotName ").append("\n")
		.append("  AND FL.carrierName = OL.carrierName ").append("\n")
		.append("  AND OL.lotState = :LOTSTATE ").append("\n")
		.append("  AND OL.lotName = :LOTNAME ");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		//bindMap.put("JOBSTATE", "Released");
		//bindMap.put("ACTIVESTATE", "Active");
		bindMap.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);
		bindMap.put("LOTNAME",lotData.getKey().getLotName());

		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindMap);

			if (result.size() < 1)
			{
				return false;
			}
			else
			{
				String jobProcessState = CommonUtil.getValue(result.get(0), "JOBPROCESSSTATE");
				String firstGlasstargetOper = CommonUtil.getValue(result.get(0), "TARGETOPERATIONNAME");

				if(!jobProcessState.isEmpty() && !firstGlasstargetOper.isEmpty())
				{
					if(!jobProcessState.equalsIgnoreCase("Completed") && firstGlasstargetOper.equalsIgnoreCase(currentOper) && FlowType.equalsIgnoreCase("Main"))
					{
						return true;
					}
				}
				return false;
			}
		}
		catch (FrameworkErrorSignal fe)
		{
			return false;
			//throw new CustomException("SYS-9999", "FirstGlassLot", fe.getMessage());
		}
	}

	//20180214 by hsryu getBeforeNodeStack for RW or Sampling ...
	public String getBeforeNodeStack(Lot lotData) throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT REGEXP_COUNT(NODESTACK, :REGPATTERN) AS NODESTACK \n")
		.append("  FROM LOT \n")
		.append(" WHERE LOTNAME = :LOTNAME");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME",lotData.getKey().getLotName());
		bindMap.put("REGPATTERN", "[^.]+");

		List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

		if (result.size() > 0)
		{

			String strNodeStackCount = result.get(0).get("NODESTACK").toString();

			if(!strNodeStackCount.equals(null))
			{
				if(Integer.parseInt(strNodeStackCount)>1)
				{
					StringBuffer queryBuffer2 = new StringBuffer()
					.append("SELECT REGEXP_SUBSTR(NODESTACK, :REGPATTERN, 1, REGEXP_COUNT(NODESTACK, :REGPATTERN) - 1) AS NODESTACK \n")
					.append("  FROM LOT \n")
					.append(" WHERE LOTNAME = :LOTNAME");

					HashMap<String, Object> bindMap2 = new HashMap<String, Object>();
					bindMap2.put("LOTNAME",lotData.getKey().getLotName());
					bindMap2.put("REGPATTERN", "[^.]+");

					List<ListOrderedMap> result2 = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer2.toString(), bindMap2);

					if(result2.size()>0)
					{
						return result2.get(0).get("NODESTACK").toString();
					}
				}
				else
				{
					log.info("NodeStack is null");
					return null;
				}
			}

			return null;
		}
		else
		{
			throw new CustomException("LOT-9000", lotData.getKey().getLotName());
		}
	}


	/*
	 * Name : getNodeStackByLot
	 * Desc : This function is getNodeStackByLot
	 * Author : hsryu
	 * Date : 2018.02.21
	 */
	public List<Map<String, Object>> getNodeStackByLot(String lotName) throws CustomException
	{
		String nodeSql = "";
		String nodeType = "";
		String nodeStack = "";

		String sql = "SELECT NODESTACK FROM LOT WHERE LOTNAME = :lotname ";
		Map bindMap = new HashMap<String, Object>();
		bindMap.put("lotname", lotName);

		List<Map<String, Object>> nodeInfo = new ArrayList<Map<String, Object>>();

		nodeInfo = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if(!(nodeInfo.size() > 0))
			throw new CustomException("Node-0003");

		return nodeInfo;
	}

	//20180222 by hsryu getFlowTypebyNodeID for RW
	public List<Map<String, Object>> getFlowTypebyNodeID(String nodeId) throws CustomException
	{
		String nodeSql = "";
		String nodeType = "";
		String nodeStack = "";

		StringBuffer queryBuffer2 = new StringBuffer()
		.append("SELECT F.PROCESSFLOWTYPE \n")
		.append("  FROM NODE N, PROCESSFLOW F \n")
		.append(" WHERE 1 = 1 \n")
		.append("       AND N.PROCESSFLOWNAME = F.PROCESSFLOWNAME \n")
		.append("       AND N.PROCESSFLOWVERSION = F.PROCESSFLOWVERSION \n")
		.append("       AND N.NODEID = :NODEID \n");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("NODEID",nodeId);

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer2.toString(), bindMap);

		//if(!(result.size() > 0))
		//throw new CustomException("Node-0003");

		return result;

	}

	//2018.02.22 by hsryu getCurrentReworkProduct for RW
	public String getOriginalProductGradeByReworkProduct(Product productData, String lotName, String factoryName, String beforeFlow, String beforeOper,String toFlow, String toOper ) throws CustomException
	{
		Map<String, String> reworkProductMap = new HashMap<String, String>();

		String productName = productData.getKey().getProductName();

		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT RP.ORIGINALPRODUCTGRADE \n")
		.append("  FROM CT_REWORKPRODUCT RP \n")
		.append(" WHERE 1 = 1 AND RP.PRODUCTNAME = :PRODUCTNAME \n")
		.append("       AND RP.LOTNAME = :LOTNAME \n")
		.append("       AND RP.FACTORYNAME = :FACTORYNAME \n")
		.append("       AND (RP.PROCESSFLOWNAME = :PROCESSFLOWNAME OR RP.PROCESSFLOWNAME = :STAR)  \n")
		.append("       AND (RP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION OR RP.PROCESSFLOWVERSION = :STAR) \n")
		.append("       AND (RP.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME OR RP.PROCESSOPERATIONNAME = :STAR) \n")
		.append("       AND (RP.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION OR RP.PROCESSOPERATIONVERSION = :STAR) \n")			
		.append("       AND RP.REWORKPROCESSFLOWNAME = :REWORKPROCESSFLOWNAME \n")
		.append("       AND (RP.REWORKPROCESSFLOWVERSION = :REWORKPROCSESFLOWVERSION OR RP.REWORKPROCESSFLOWVERSION = :STAR) \n")
		.append("       AND (RP.REWORKPROCESSOPERATIONNAME = :REWORKPROCESSOPERATIONNAME OR RP.REWORKPROCESSOPERATIONNAME = :STAR) \n")
		.append("       AND (RP.REWORKPROCESSOPERATIONVERSION = :REWORKPROCESSOPERATIONVERSION OR RP.REWORKPROCESSOPERATIONVERSION = :STAR) \n");			

		HashMap<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("PRODUCTNAME", productName);
		bindMap.put("LOTNAME", lotName);
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", beforeFlow);
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("PROCESSOPERATIONNAME", beforeOper);
		bindMap.put("PROCESSOPERATIONVERSION", "00001");
		bindMap.put("REWORKPROCESSFLOWNAME", toFlow);
		bindMap.put("REWORKPROCSESFLOWVERSION", "00001");
		bindMap.put("REWORKPROCESSOPERATIONNAME", toOper);
		bindMap.put("REWORKPROCESSOPERATIONVERSION", "00001");
		bindMap.put("STAR", "*");

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

		if ( sqlResult.size() > 0 )
		{
			String originalProductGrade = (String) sqlResult.get(0).get("ORIGINALPRODUCTGRADE");

			return originalProductGrade;
		}
		
		return "";
	}
	
	
	//2018.02.22 by hsryu getCurrentReworkProduct for RW
		public String getOriginalLotGradeByReworkLot(Lot lotData, String factoryName, String beforeFlow, String beforeOper,String toFlow, String toOper ) throws CustomException
		{
			StringBuffer queryBuffer = new StringBuffer()
			.append("SELECT RL.ORIGINALLOTGRADE \n")
			.append("  FROM CT_REWORKLOT RL \n")
			.append(" WHERE 1 = 1 \n")
			.append("       AND RL.LOTNAME = :LOTNAME \n")
			.append("       AND RL.FACTORYNAME = :FACTORYNAME \n")
			.append("       AND (RL.PROCESSFLOWNAME = :PROCESSFLOWNAME OR RL.PROCESSFLOWNAME = :STAR) \n")
			.append("       AND (RL.PROCESSFLOWVERSION = :PROCESSFLOWVERSION OR RL.PROCESSFLOWVERSION = :STAR) \n")
			.append("       AND (RL.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME OR RL.PROCESSOPERATIONNAME = :STAR) \n")
			.append("       AND (RL.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION OR RL.PROCESSOPERATIONVERSION = :STAR) \n")			
			.append("       AND RL.REWORKPROCESSFLOWNAME = :REWORKPROCESSFLOWNAME \n")
			.append("       AND (RL.REWORKPROCESSFLOWVERSION = :REWORKPROCSESFLOWVERSION OR RL.REWORKPROCESSFLOWVERSION = :STAR) \n")
			.append("       AND RL.REWORKPROCESSOPERATIONNAME = :REWORKPROCESSOPERATIONNAME \n")
			.append("       AND (RL.REWORKPROCESSOPERATIONVERSION = :REWORKPROCESSOPERATIONVERSION OR RL.REWORKPROCESSOPERATIONVERSION = :STAR) \n");			

			HashMap<String, Object> bindMap = new HashMap<String, Object>();

			bindMap.put("LOTNAME", lotData.getKey().getLotName());
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PROCESSFLOWNAME", beforeFlow);
			bindMap.put("PROCESSFLOWVERSION", "00001");
			bindMap.put("PROCESSOPERATIONNAME", beforeOper);
			bindMap.put("PROCESSOPERATIONVERSION", "00001");
			bindMap.put("REWORKPROCESSFLOWNAME", toFlow);
			bindMap.put("REWORKPROCSESFLOWVERSION", "00001");
			bindMap.put("REWORKPROCESSOPERATIONNAME", toOper);
			bindMap.put("REWORKPROCESSOPERATIONVERSION", "00001");
			bindMap.put("STAR", "*");

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

			if ( sqlResult.size() > 0 )
			{
				String originalLotGrade = (String) sqlResult.get(0).get("ORIGINALLOTGRADE");

				return originalLotGrade;
			}
			
			return "";
		}



	//2018.02.22 by hsryu getCurrentReworkProduct for RW
	public List<String> getCurrentReworkProduct(List<Product> productList, String lotName, String factoryName, String beforeFlow, String beforeOper,String toFlow, String toOper ) throws CustomException
	{
		ArrayList<String> arrPrdName = new ArrayList<String>();

		for(Product product : productList)
		{	

			String productName = product.getKey().getProductName();

			StringBuffer queryBuffer = new StringBuffer()
			.append("SELECT RP.PRODUCTNAME \n")
			.append("  FROM CT_REWORKPRODUCT RP \n")
			.append(" WHERE 1 = 1 AND RP.PRODUCTNAME = :PRODUCTNAME \n")
				.append("       AND RP.LOTNAME = :LOTNAME \n")
				.append("       AND RP.FACTORYNAME = :FACTORYNAME \n")
				.append("       AND RP.PROCESSFLOWNAME = :PROCESSFLOWNAME \n")
				.append("       AND RP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION \n")
				.append("       AND RP.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME \n")
				.append("       AND RP.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION \n")			
				.append("       AND RP.REWORKPROCESSFLOWNAME = :REWORKPROCESSFLOWNAME \n")
				.append("       AND RP.REWORKPROCESSFLOWVERSION = :REWORKPROCSESFLOWVERSION \n")
				.append("       AND RP.REWORKPROCESSOPERATIONNAME = :REWORKPROCESSOPERATIONNAME \n")
				.append("       AND RP.REWORKPROCESSOPERATIONVERSION = :REWORKPROCESSOPERATIONVERSION \n");			

				HashMap<String, Object> bindMap = new HashMap<String, Object>();

				bindMap.put("PRODUCTNAME", productName);
				bindMap.put("LOTNAME", lotName);
				bindMap.put("FACTORYNAME", factoryName);
				bindMap.put("PROCESSFLOWNAME", beforeFlow);
				bindMap.put("PROCESSFLOWVERSION", "00001");
				bindMap.put("PROCESSOPERATIONNAME", beforeOper);
				bindMap.put("PROCESSOPERATIONVERSION", "00001");
				bindMap.put("REWORKPROCESSFLOWNAME", toFlow);
				bindMap.put("REWORKPROCSESFLOWVERSION", "00001");
				bindMap.put("REWORKPROCESSOPERATIONNAME", toOper);
				bindMap.put("REWORKPROCESSOPERATIONVERSION", "00001");

				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

				if(sqlResult.size() > 0)
				{
					arrPrdName.add(productName);
				}
			}
			return arrPrdName;
		}

		/**
		 * 20180323 : Decide Sample
		 */
		public void getDecideSample(Document doc, EventInfo eventInfo, Lot lotData)
		{
			try
			{
				String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

				// 1. Calculate Sample Count
				calculateSamplingCount(eventInfo, lotData, machineName);
			}
			catch (Exception ex)
			{
				log.warn("Decide Sample Failed!");
			}
		}
		public void getDecideSample(String machineName, EventInfo eventInfo, Lot lotData)
		{
			try
			{

				// 1. Calculate Sample Count
				calculateSamplingCount(eventInfo, lotData, machineName);
			}
			catch (Exception ex)
			{
				log.warn("Decide Sample Failed!");
			}
		}

		/**
		 * 20180323 : Calculate New Sampling Count
		 */
		public void calculateSamplingCount(EventInfo eventInfo, Lot lotData, String machineName) throws CustomException
		{
			log.info("Calculate Samling Count");
			
			String decideSampleNodeStack = "";

			String lotName = lotData.getKey().getLotName();
			String factoryName = lotData.getFactoryName();
			String productSpecName = lotData.getProductSpecName();
			String ecCode = lotData.getUdfs().get("ECCODE");
			String processFlowName = lotData.getProcessFlowName();
			String processFlowVersion = lotData.getProcessFlowVersion();
			String processOperationName = lotData.getProcessOperationName();
			String processOperationVersion = lotData.getProcessOperationVersion();
			long currentCount;
			long totalCount;

			/* 20180808, Add, samplePosition as samplePriority ==>> */
			//long tempSamplePriority = 9999;
			//String tempSamplePosition = StringUtil.EMPTY;
			/* <<== 20180808, Add, samplePosition as samplePriority */

			// Find existing sampling count
			try
			{
				String sampleSql =

						"SELECT SAMPLEPROCESSFLOWNAME, SAMPLEPROCESSFLOWVERSION, FROMPROCESSOPERATIONNAME, FROMPROCESSOPERATIONVERSION, "
								+ " SAMPLECOUNT, NVL(SAMPLEPRIORITY,9999) SAMPLEPRIORITY, SAMPLEOUTHOLDFLAG, CORRESPROCESSOPERATIONNAME, CORRESPROCESSOPERATIONVERSION, "
								+ " CORRESSAMPLEPROCESSFLOWNAME, CORRESSAMPLEPROCESSFLOWVERSION, NVL(CORRESSAMPLELOTCOUNT,0) CORRESSAMPLELOTCOUNT, CORRESSAMPLEPOSITION,   "
								+ " SAMPLEPOSITION, CORRESSAMPLEOUTHOLDFLAG, NVL(CORRESSAMPLEPRIORITY,9999) CORRESSAMPLEPRIORITY "
								+ " FROM  T, POSSAMPLE P "
								+ " WHERE T.CONDITIONID = P.CONDITIONID "
								+ " AND ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = :STAR)) "
								+ " AND ((T.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (T.PRODUCTSPECNAME = :STAR)) "
								+ " AND ((T.ECCODE = :ECCODE ) OR (T.ECCODE = :STAR)) "
								+ " AND ((T.PROCESSFLOWNAME = :PROCESSFLOWNAME ) OR (T.PROCESSFLOWNAME = :STAR)) "
								+ " AND ((T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (T.PROCESSFLOWVERSION = :STAR)) "
								+ " AND ((T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ) OR (T.PROCESSOPERATIONNAME = :STAR)) "
								+ " AND ((T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ) OR (T.PROCESSOPERATIONVERSION = :STAR)) "
								+ " AND ((T.MACHINENAME = :MACHINENAME ) OR (T.MACHINENAME = :STAR)) "
								+ " AND P.VALIDFLAG = :VALIDFLAG "
								// 2019.03.13_hsryu_add CorresSamplePriority & ConditionID.
								+ " ORDER BY P.SAMPLEPRIORITY, P.CORRESSAMPLEPRIORITY, "	
								+ 	" DECODE(T.MACHINENAME, :STAR, 9999, 0), "
								+ 	" DECODE(T.PRODUCTSPECNAME, :STAR, 9999, 0), "
								+ 	" DECODE(T.PRODUCTSPECVERSION, :STAR, 9999, 0), "
								+ 	" DECODE(T.PROCESSFLOWNAME, :STAR, 9999, 0),  "
								+ 	" DECODE(T.PROCESSFLOWVERSION, :STAR, 9999, 0), "
								+ 	" DECODE(T.ECCODE, :STAR, 9999, 0), "
								+ 	" DECODE(T.PROCESSOPERATIONNAME, :STAR, 9999, 0), "
								+ 	" DECODE(T.PROCESSOPERATIONVERSION, :STAR, 9999, 0) ,"
                                // modify by jhiying on 20190918 mantis 4829 
				                + 	" T.CONDITIONID ";
				
				Map<String, Object> smapleBindSet = new HashMap<String, Object>();
				smapleBindSet.put("FACTORYNAME", factoryName);
				smapleBindSet.put("PRODUCTSPECNAME", productSpecName);
				smapleBindSet.put("ECCODE", ecCode);
				smapleBindSet.put("PROCESSFLOWNAME", processFlowName);
				smapleBindSet.put("PROCESSFLOWVERSION", processFlowVersion);
				smapleBindSet.put("PROCESSOPERATIONNAME", processOperationName);
				smapleBindSet.put("PROCESSOPERATIONVERSION", processOperationVersion);
				smapleBindSet.put("MACHINENAME", machineName);
				smapleBindSet.put("VALIDFLAG", "Y");
				smapleBindSet.put("STAR", "*");

				List<Map<String, Object>> sampleSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sampleSql, smapleBindSet);

				if( sampleSqlResult.size() > 0 )
				{
					ArrayList<String> sameCheck = new ArrayList<String>();
					ArrayList<String> systemSampleSameCheck = new ArrayList<String>();
					ArrayList<String> corresSampleSameCheck = new ArrayList<String>();
					boolean sampleLotSaveFlag = false;
					boolean corresSampleLotSaveFlag = false;

					for ( int i = 0; i < sampleSqlResult.size(); i++ )
					{
						String corresSampleLotCount = "";
						long corresSamplePriority = 0;
						String sampleProcessFlowName = (String)sampleSqlResult.get(i).get("SAMPLEPROCESSFLOWNAME");
						String sampleProcessFlowVersion = (String)sampleSqlResult.get(i).get("SAMPLEPROCESSFLOWVERSION");
						String fromProcessOperationName = (String)sampleSqlResult.get(i).get("FROMPROCESSOPERATIONNAME");
						String fromProcessOperationVersion = (String)sampleSqlResult.get(i).get("FROMPROCESSOPERATIONVERSION");
						long sampleCount = Long.parseLong(sampleSqlResult.get(i).get("SAMPLECOUNT").toString());
						long samplePriority = Long.parseLong(sampleSqlResult.get(i).get("SAMPLEPRIORITY").toString());
						String corresProcessOperationName = (String)sampleSqlResult.get(i).get("CORRESPROCESSOPERATIONNAME");
						String corresProcessOperationVersion = (String)sampleSqlResult.get(i).get("CORRESPROCESSOPERATIONVERSION");
						String corresSampleProcessFlowName = (String)sampleSqlResult.get(i).get("CORRESSAMPLEPROCESSFLOWNAME");
						String corresSampleProcessFlowVersion = (String)sampleSqlResult.get(i).get("CORRESSAMPLEPROCESSFLOWVERSION");
						String samplePosition = (String)sampleSqlResult.get(i).get("SAMPLEPOSITION");
						String sampleOutHoldFlag = (String)sampleSqlResult.get(i).get("SAMPLEOUTHOLDFLAG");

						eventInfo.setEventName("DecideSample");
						eventInfo.setEventComment("DecideSample by TrackOut");
						// 2019.03.13_hsryu_Add Logic. For History Timekey Duplicated.
						eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));
						
						// 2019.03.13_hsryu_For me...
						log.info("SystemProcessOper :" + fromProcessOperationName + " & SystemSampleProcessFlow :" + sampleProcessFlowName);
						log.info("CorresProcessOper :" + corresProcessOperationName + " & CorreSampleProcessFlow :" + corresSampleProcessFlowName);

						//18.10.16
						if(sameCheck.contains(fromProcessOperationName + sampleProcessFlowName + corresProcessOperationName + corresSampleProcessFlowName)){
							log.info("same fromProcessOperation & SampleProcessFlow & CorresProcessOperation & CorresSampleFlow! Skip Counting.");
							continue;
						}
						else{
							sameCheck.add(fromProcessOperationName + sampleProcessFlowName + corresProcessOperationName + corresSampleProcessFlowName);
						}

						if(sampleSqlResult.get(i).get("CORRESSAMPLELOTCOUNT")!=null){
							corresSampleLotCount = String.valueOf(Long.parseLong(sampleSqlResult.get(i).get("CORRESSAMPLELOTCOUNT").toString()));
							corresSamplePriority = Long.parseLong(sampleSqlResult.get(i).get("CORRESSAMPLEPRIORITY").toString());
						}
						String corresSamplePosition = (String)sampleSqlResult.get(i).get("CORRESSAMPLEPOSITION");
						String corresSampleOutHoldFlag = (String)sampleSqlResult.get(i).get("CORRESSAMPLEOUTHOLDFLAG");

						List<SampleLotCount> sampleLotCntList = new ArrayList<SampleLotCount>();

						String condition = " WHERE factoryName = ? AND productSpecName = ? AND ecCode = ? AND processFlowName = ? AND processFlowVersion = ? AND processOperationName = ?"
								+ " AND processOperationVersion = ? AND machineName = ? AND sampleProcessFlowName = ? AND (sampleProcessFlowVersion = ? or sampleProcessFlowVersion = ?) "
								+ " AND fromProcessOperationName = ? AND (fromProcessOperationVersion = ? or fromProcessOperationVersion = ?) AND CorresProcessOperationName = ? "
								+ " AND (CorresProcessOperationVersion = ? or CorresProcessOperationVersion = ?) AND CorresSampleProcessFlowName = ? AND"
								+ " (CorresSampleProcessFlowVersion = ? or CorresSampleProcessFlowVersion = ?) ";
						
						Object[] bindSet = new Object[]{ factoryName, productSpecName, ecCode, processFlowName,processFlowVersion,
								processOperationName, processOperationVersion, machineName, sampleProcessFlowName, sampleProcessFlowVersion, "*", fromProcessOperationName, fromProcessOperationVersion, "*",
								corresProcessOperationName, corresProcessOperationVersion, "*", corresSampleProcessFlowName, corresSampleProcessFlowVersion, "*"};

						try
						{
							sampleLotCntList = ExtendedObjectProxy.getSampleLotCountService().select(condition, bindSet);
						}
						catch(Exception ex)
						{
							log.error("Not found sampling count");
						}

						if ( sampleLotCntList.size() == 0 )
						{
							currentCount = (long) lotData.getProductQuantity();

							// Create 1-raw
							SampleLotCount countInfo = new SampleLotCount(factoryName, productSpecName, ecCode, processFlowName, processFlowVersion,
									processOperationName, processOperationVersion, machineName, sampleProcessFlowName, sampleProcessFlowVersion,
									fromProcessOperationName, fromProcessOperationVersion, corresProcessOperationName, corresProcessOperationVersion,
									corresSampleProcessFlowName, corresSampleProcessFlowVersion);

							countInfo.setSampleCount(sampleCount);
							countInfo.setCurrentCount(currentCount);
							countInfo.setTotalCount(currentCount);
							countInfo.setSamplePriority(samplePriority);
							if(StringUtil.isNotEmpty(corresSampleLotCount)){
								countInfo.setCorresSampleCount(Long.parseLong(corresSampleLotCount));
								countInfo.setCorresCurrentCount(0);
								countInfo.setCorresTotalCount(0);
								countInfo.setCorresSamplePosition(corresSamplePosition);
								countInfo.setCorresSamplePriority(corresSamplePriority);
							}
							countInfo.setLastEventName(eventInfo.getEventName());
							countInfo.setLastEventTime(eventInfo.getEventTime());
							countInfo.setLastEventTimekey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
							countInfo.setLastEventUser(eventInfo.getEventUser());
							countInfo.setLastEventComment(eventInfo.getEventComment());
							countInfo = ExtendedObjectProxy.getSampleLotCountService().create(eventInfo, countInfo);
							
							// 2019.03.12_hsryu_if same SampleFlow&FromOper, ignore.
							if(systemSampleSameCheck.contains(fromProcessOperationName+sampleProcessFlowName)){
								log.info("SampleFlow & FromOper is Duplicated! can't execute SystemSample.");
								sampleLotSaveFlag = false;
							}
							else{
								systemSampleSameCheck.add(fromProcessOperationName+sampleProcessFlowName);
								sampleLotSaveFlag = true;
							}

							if ( currentCount >= sampleCount ){
								if(sampleLotSaveFlag){
									eventInfo.setEventName("DecideSample");
									eventInfo.setEventComment("Sampling Target Lot by DecideSample.");
									/* 20180808, Add, samplePosition as samplePriority ==>> */
									setSampleLotData("Y", eventInfo, lotData, countInfo, samplePosition,"",sampleOutHoldFlag,true,false);
									//setSampleLotData("Y", eventInfo, lotData, countInfo, tempSamplePosition,"",sampleOutHoldFlag,true,false);
									/* <<== 20180808, Add, samplePosition as samplePriority */

									LotAction lotActionSamplePosition = MESLotServiceProxy.getLotServiceUtil().getSamplePositionOfLotAction(lotData,sampleProcessFlowName,fromProcessOperationName,eventInfo);

									if(lotActionSamplePosition!=null){
										//Reserved Sample of Lotaction.
										if(StringUtil.isNotEmpty(lotActionSamplePosition.getSamplePosition())){
											// SampleReserve Data with different SamplePosition
											if ( !samplePosition.equals(lotActionSamplePosition.getSamplePosition()) ){
												/* 20180808, Add, samplePosition as samplePriority ==>> */
												//String sumPosition = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(samplePosition, lotActionSamplePosition);
												setSampleLotData("Y", eventInfo, lotData, countInfo, samplePosition ,lotActionSamplePosition.getSamplePosition(),sampleOutHoldFlag,false,true);
												//setSampleLotData("Y", eventInfo, lotData, countInfo, tempSamplePosition ,lotActionSamplePosition,sampleOutHoldFlag,false,true);
												/* <<== 20180808, Add, samplePosition as samplePriority */
											}
										}

										if(StringUtil.equals(lotActionSamplePosition.getSampleOutHoldFlag(), "Y")){
											if(!StringUtil.equals(lotActionSamplePosition.getSampleOutHoldFlag(), sampleOutHoldFlag))
												this.setSampleOutHoldFlag(eventInfo, lotData, countInfo);
										}
									}
								}

								// Reset CurrentCount
								eventInfo.setEventName("ResetSample");
								eventInfo.setEventComment("ResetSample by NormalSampling");
								eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));
								countInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
								countInfo.setCurrentCount(0);

								countInfo = ExtendedObjectProxy.getSampleLotCountService().modify(eventInfo, countInfo);

								if(checkExistCorresInfo(corresProcessOperationName,corresProcessOperationVersion,corresSampleProcessFlowName,corresSampleProcessFlowVersion,
										corresSampleLotCount,corresSamplePosition))
								{
									// Reset CurrentCount
									eventInfo.setEventName("IncrementCorresCount");
									eventInfo.setEventComment("CorresSampling Target Lot by DecideSample.");
									eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));
									countInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
									countInfo.setCorresCurrentCount(countInfo.getCurrentCount()+1);
									countInfo.setCorresTotalCount(countInfo.getCorresTotalCount()+1);

									countInfo = ExtendedObjectProxy.getSampleLotCountService().modify(eventInfo, countInfo);

									// 2019.03.12_hsryu_if same CorresSampleFlow&CorresOper, ignore.
									if(corresSampleSameCheck.contains(corresProcessOperationName+corresSampleProcessFlowName)){
										log.info("CorresSampleFlow & CorresOper is Duplicated! can't execute CorresSample.");
										corresSampleLotSaveFlag = false;
									}
									else{
										corresSampleSameCheck.add(corresProcessOperationName+corresSampleProcessFlowName);
										corresSampleLotSaveFlag = true;
									}
									
									if ( Long.parseLong(corresSampleLotCount) == 1 ){
										if(corresSampleLotSaveFlag){
											// 2019.03.13_hsryu_when samplePriority = 1, same fromProcessOperation & SampleProcessFlowName, CorresInfo is '-'
											// next CorresSample is not execute. 
											if(!sameCheck.contains(fromProcessOperationName+sampleProcessFlowName + "--")){
												eventInfo.setEventName("DecideCorresSample");
												eventInfo.setEventComment("CorresSampling Target Lot by DecideSample.");
												setCorresSampleLotData("Y", eventInfo, lotData, countInfo, corresSamplePosition,"",corresSampleOutHoldFlag,false);

												LotAction getLotActionSamplePosition = MESLotServiceProxy.getLotServiceUtil().getSamplePositionOfLotAction(lotData,corresSampleProcessFlowName,corresProcessOperationName,eventInfo);

												if(getLotActionSamplePosition!=null)
												{
													//Reserved Sample of Lotaction.
													if(StringUtil.isNotEmpty(getLotActionSamplePosition.getSamplePosition()))
													{
														// SampleReserve Data with different SamplePosition
														if ( !samplePosition.equals(getLotActionSamplePosition.getSamplePosition()) )
														{
															//String sumPosition = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(samplePosition, lotActionSamplePosition);
															setCorresSampleLotData("Y", eventInfo, lotData, countInfo, corresSamplePosition ,getLotActionSamplePosition.getSamplePosition(),corresSampleOutHoldFlag,true);
														}
													}

													if(StringUtil.equals(getLotActionSamplePosition.getSampleOutHoldFlag(), "Y"))
													{
														if(!StringUtil.equals(getLotActionSamplePosition.getSampleOutHoldFlag(), sampleOutHoldFlag))
														{
															this.setCorresSampleOutHoldFlag(eventInfo, lotData, countInfo);
														}
													}
												}
											}
										}

										// Reset CurrentCount
										eventInfo.setEventName("ResetSample");
										eventInfo.setEventComment("ResetSample by CorresSampling");
										eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));
										countInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
										countInfo.setCorresCurrentCount(0);

										countInfo = ExtendedObjectProxy.getSampleLotCountService().modify(eventInfo, countInfo);
									}
									else if( Long.parseLong(corresSampleLotCount) != 0 && Long.parseLong(corresSampleLotCount) > 1 )
									{
										if(countInfo.getCorresCurrentCount()>=Long.parseLong(corresSampleLotCount))
										{
											if(corresSampleLotSaveFlag){
												// 2019.03.13_hsryu_when samplePriority = 1, same fromProcessOperation & SampleProcessFlowName, CorresInfo is '-'
												// next CorresSample is not execute. 
												if(!sameCheck.contains(fromProcessOperationName+sampleProcessFlowName + "--")){
													eventInfo.setEventName("DecideCorresSample");
													eventInfo.setEventComment("Sampling Target Lot by DecideSample.");
													setCorresSampleLotData("Y", eventInfo, lotData, countInfo, corresSamplePosition,"",corresSampleOutHoldFlag,false);

													LotAction getLotActionSamplePosition = MESLotServiceProxy.getLotServiceUtil().getSamplePositionOfLotAction(lotData,corresSampleProcessFlowName,corresProcessOperationName,eventInfo);

													if(getLotActionSamplePosition!=null)
													{
														//Reserved Sample of Lotaction.
														if(StringUtil.isNotEmpty(getLotActionSamplePosition.getSamplePosition()))
														{
															// SampleReserve Data with different SamplePosition
															if ( !samplePosition.equals(getLotActionSamplePosition.getSamplePosition()) )
															{
																//String sumPosition = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(samplePosition, lotActionSamplePosition);
																setCorresSampleLotData("Y", eventInfo, lotData, countInfo, corresSamplePosition ,getLotActionSamplePosition.getSamplePosition(),corresSampleOutHoldFlag,true);
															}
														}

														if(StringUtil.equals(getLotActionSamplePosition.getSampleOutHoldFlag(), "Y"))
														{
															if(!StringUtil.equals(getLotActionSamplePosition.getSampleOutHoldFlag(), sampleOutHoldFlag))
															{
																this.setCorresSampleOutHoldFlag(eventInfo, lotData, countInfo);
															}
														}
													}
												}
											}
											
											// Reset CurrentCount
											eventInfo.setEventName("ResetSample");
											eventInfo.setEventComment("ResetSample by CorresSampling");
											eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));
											countInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
											countInfo.setCorresCurrentCount(0);
											
											countInfo = ExtendedObjectProxy.getSampleLotCountService().modify(eventInfo, countInfo);
										}
									}
								}
							}
						}
						else if ( sampleLotCntList.size() == 1 )
						{
							SampleLotCount sampleLotCnt = new SampleLotCount();
							sampleLotCnt = sampleLotCntList.get(0);

							currentCount = sampleLotCnt.getCurrentCount();
							totalCount = sampleLotCnt.getTotalCount();
							long productQuantity = (long) lotData.getProductQuantity();

							currentCount = currentCount + productQuantity;
							totalCount = totalCount + productQuantity;

							sampleLotCnt.setCurrentCount(currentCount);
							sampleLotCnt.setTotalCount(totalCount);
							sampleLotCnt.setLastEventName(eventInfo.getEventName());
							sampleLotCnt.setLastEventTime(eventInfo.getEventTime());
							eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));
							sampleLotCnt.setLastEventTimekey(eventInfo.getEventTimeKey());
							sampleLotCnt.setLastEventUser(eventInfo.getEventUser());
							sampleLotCnt.setLastEventComment(eventInfo.getEventComment());

							//2018.10.15 dmlee : If Modeling Sample Count Chagned, Update SampleLotCount
							if(sampleCount != sampleLotCnt.getSampleCount()){	
								sampleLotCnt.setSampleCount(sampleCount);
								sampleLotCnt.setCurrentCount(productQuantity);
								sampleLotCnt.setLastEventComment("Sampling Count Changed, Reset Current Count");
							}
							//2018.10.15 dmlee : -------------------------------------------------------

							//2018.11.28_hsryu_If Modeling SampleCount Changed, Update SampleLotCount.
							/****************************************************************************/
							if(samplePriority!=sampleLotCnt.getSamplePriority())
								sampleLotCnt.setSamplePriority(samplePriority);
							
							// 2019.03.12_hsryu_Modify Condition. For Multi CorresSampling.
							if(!StringUtils.equals(corresProcessOperationName, "-") && !StringUtils.equals(corresSampleProcessFlowName, "-")){

								if(Long.parseLong(corresSampleLotCount)!=sampleLotCnt.getCorresSampleCount())
									sampleLotCnt.setCorresSampleCount(Long.parseLong(corresSampleLotCount));

								if(!StringUtils.equals(corresSamplePosition, sampleLotCnt.getCorresSamplePosition()))
									sampleLotCnt.setCorresSamplePosition(corresSamplePosition);

								if(corresSamplePriority!=sampleLotCnt.getCorresSamplePriority())
									sampleLotCnt.setCorresSamplePriority(corresSamplePriority);
							}
							/****************************************************************************/

							ExtendedObjectProxy.getSampleLotCountService().modify(eventInfo, sampleLotCnt);
							
							// 2019.03.12_hsryu_if same SampleFlow&FromOper, ignore.
							if(systemSampleSameCheck.contains(fromProcessOperationName+sampleProcessFlowName)){
								log.info("SampleFlow & FromOper is Duplicated! can't execute SystemSample.");
								sampleLotSaveFlag = false;
							}
							else{
								systemSampleSameCheck.add(fromProcessOperationName+sampleProcessFlowName);
								sampleLotSaveFlag = true;
							}

							if ( currentCount >= sampleCount ){
								if( sampleLotSaveFlag ){
									eventInfo.setEventName("DecideSample");
									eventInfo.setEventComment("Sampling Target Lot by DecideSample.");
									/* 20180808, Add, samplePosition as samplePriority ==>> */
									setSampleLotData("Y", eventInfo, lotData, sampleLotCnt, samplePosition,"",sampleOutHoldFlag,true,false);
									//setSampleLotData("Y", eventInfo, lotData, sampleLotCnt, tempSamplePosition,"",sampleOutHoldFlag,true,false);
									/* <<== 20180808, Add, samplePosition as samplePriority */

									LotAction lotActionSamplePosition = MESLotServiceProxy.getLotServiceUtil().getSamplePositionOfLotAction(lotData,sampleProcessFlowName,fromProcessOperationName,eventInfo);

									if(lotActionSamplePosition!=null){
										//Reserved Sample of Lotaction.
										if(StringUtil.isNotEmpty(lotActionSamplePosition.getSamplePosition())){
											// SampleReserve Data with different SamplePosition
											if ( !samplePosition.equals(lotActionSamplePosition.getSamplePosition()))
											{
												/* 20180808, Add, samplePosition as samplePriority ==>> */
												//String sumPosition = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(samplePosition, lotActionSamplePosition);
												setSampleLotData("Y", eventInfo, lotData, sampleLotCnt, samplePosition ,lotActionSamplePosition.getSamplePosition(),sampleOutHoldFlag,false,true);
												//setSampleLotData("Y", eventInfo, lotData, sampleLotCnt, tempSamplePosition ,lotActionSamplePosition,sampleOutHoldFlag,false,true);
												/* <<== 20180808, Add, samplePosition as samplePriority */
											}
										}

										if(StringUtil.equals(lotActionSamplePosition.getSampleOutHoldFlag(), "Y")){
											if(!StringUtil.equals(lotActionSamplePosition.getSampleOutHoldFlag(), sampleOutHoldFlag))
												this.setSampleOutHoldFlag(eventInfo, lotData, sampleLotCnt);
										}
									}
								}

								// Reset CurrentCount
								eventInfo.setEventName("ResetSample");
								eventInfo.setEventComment("ResetSample by NormalSampling");
								eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));
								sampleLotCnt.setLastEventTimekey(eventInfo.getEventTimeKey());
								sampleLotCnt.setCurrentCount(0);

								sampleLotCnt = ExtendedObjectProxy.getSampleLotCountService().modify(eventInfo, sampleLotCnt);

								if(checkExistCorresInfo(corresProcessOperationName,corresProcessOperationVersion,corresSampleProcessFlowName,corresSampleProcessFlowVersion,
										corresSampleLotCount,corresSamplePosition))
								{
									eventInfo.setEventName("DecideCorresSample");
									eventInfo.setEventComment("DecideSample by TrackOut");
									eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));
									sampleLotCnt.setLastEventTimekey(eventInfo.getEventTimeKey());
									sampleLotCnt.setCorresCurrentCount(sampleLotCnt.getCorresCurrentCount()+1);
									sampleLotCnt.setCorresTotalCount(sampleLotCnt.getCorresTotalCount()+1);

									sampleLotCnt = ExtendedObjectProxy.getSampleLotCountService().modify(eventInfo, sampleLotCnt);
									
									// 2019.03.12_hsryu_if same CorresSampleFlow&CorresOper, ignore.
									if(corresSampleSameCheck.contains(corresProcessOperationName+corresSampleProcessFlowName)){
										corresSampleLotSaveFlag = false;
										log.info("CorresSampleFlow & CorresOper is Duplicated! can't execute CorresSample.");
									}
									else{
										corresSampleSameCheck.add(corresProcessOperationName+corresSampleProcessFlowName);
										corresSampleLotSaveFlag = true;
									}

									if (Long.parseLong(corresSampleLotCount)==1){
										if(corresSampleLotSaveFlag){
											// 2019.03.13_hsryu_when samplePriority = 1, same fromProcessOperation & SampleProcessFlowName, CorresInfo is '-'
											// next CorresSample is not execute. 
											if(!sameCheck.contains(fromProcessOperationName + sampleProcessFlowName + "--")){
												eventInfo.setEventName("DecideCorresSample");
												eventInfo.setEventComment("CorresSampling Target Lot by DecideSample.");
												setCorresSampleLotData("Y", eventInfo, lotData, sampleLotCnt, corresSamplePosition,"",corresSampleOutHoldFlag,false);

												LotAction getLotActionSamplePosition = MESLotServiceProxy.getLotServiceUtil().getSamplePositionOfLotAction(lotData,corresSampleProcessFlowName,corresProcessOperationName,eventInfo);

												if(getLotActionSamplePosition!=null){
													//Reserved Sample of Lotaction.
													if(StringUtil.isNotEmpty(getLotActionSamplePosition.getSamplePosition())){
														// SampleReserve Data with different SamplePosition
														if ( !samplePosition.equals(getLotActionSamplePosition.getSamplePosition()) )
														{
															//String sumPosition = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(samplePosition, lotActionSamplePosition);
															setCorresSampleLotData("Y", eventInfo, lotData, sampleLotCnt, corresSamplePosition ,getLotActionSamplePosition.getSamplePosition(),corresSampleOutHoldFlag,true);
														}
													}

													if(StringUtil.equals(getLotActionSamplePosition.getSampleOutHoldFlag(), "Y")){
														if(!StringUtil.equals(getLotActionSamplePosition.getSampleOutHoldFlag(), sampleOutHoldFlag))
															this.setCorresSampleOutHoldFlag(eventInfo, lotData, sampleLotCnt);
													}
												}
											}
										}
										
										// Reset CurrentCount
										eventInfo.setEventName("ResetSample");
										eventInfo.setEventComment("ResetSample by CorresSampling");
										eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));
										sampleLotCnt.setLastEventTimekey(eventInfo.getEventTimeKey());
										sampleLotCnt.setCorresCurrentCount(0);

										sampleLotCnt = ExtendedObjectProxy.getSampleLotCountService().modify(eventInfo, sampleLotCnt);
									}
									else if ( Long.parseLong(corresSampleLotCount)!=0 && Long.parseLong(corresSampleLotCount)>1 ){
										if( sampleLotCnt.getCorresCurrentCount() >= Long.parseLong(corresSampleLotCount) )
										{
											if(corresSampleLotSaveFlag){
												// 2019.03.13_hsryu_when samplePriority = 1, same fromProcessOperation & SampleProcessFlowName, CorresInfo is '-'
												// next CorresSample is not execute. 
												if(!sameCheck.contains(fromProcessOperationName+sampleProcessFlowName + "--")){
													eventInfo.setEventName("DecideCorresSample");
													eventInfo.setEventComment("CorresSampling Target Lot by DecideSample.");
													setCorresSampleLotData("Y", eventInfo, lotData, sampleLotCnt, corresSamplePosition,"",corresSampleOutHoldFlag,false);

													LotAction getLotActionSamplePosition = MESLotServiceProxy.getLotServiceUtil().getSamplePositionOfLotAction(lotData,corresSampleProcessFlowName,corresProcessOperationName,eventInfo);

													if(getLotActionSamplePosition!=null){
														//Reserved Sample of Lotaction.
														if(StringUtil.isNotEmpty(getLotActionSamplePosition.getSamplePosition())){
															// SampleReserve Data with different SamplePosition
															if ( !samplePosition.equals(getLotActionSamplePosition.getSamplePosition()) )
															{
																//String sumPosition = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(samplePosition, lotActionSamplePosition);
																setCorresSampleLotData("Y", eventInfo, lotData, sampleLotCnt, corresSamplePosition ,getLotActionSamplePosition.getSamplePosition(),corresSampleOutHoldFlag,true);
															}
														}

														if(StringUtil.equals(getLotActionSamplePosition.getSampleOutHoldFlag(), "Y")){
															if(!StringUtil.equals(getLotActionSamplePosition.getSampleOutHoldFlag(), sampleOutHoldFlag))
																this.setCorresSampleOutHoldFlag(eventInfo, lotData, sampleLotCnt);
														}
													}
												}
											}

											// Reset CurrentCount
											eventInfo.setEventName("ResetSample");
											eventInfo.setEventComment("ResetSample by CorresSampling");
											eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));
											sampleLotCnt.setLastEventTimekey(eventInfo.getEventTimeKey());
											sampleLotCnt.setCorresCurrentCount(0);

											sampleLotCnt = ExtendedObjectProxy.getSampleLotCountService().modify(eventInfo, sampleLotCnt);
										}
									}
								}
							}
						}
						//2019.03.12_hsryu_Delete Logic. For Multi CorresSampling
//						else
//						{
//							// less than 0-raw or more than 2-raw : Nothing
//							log.error("Wrong Data!");
//
//							/**
//							 * ToDo : Add Exception Number
//							 */
//							throw new CustomException("LOT-9000", lotName);
//						}

						/* 20180808, Add, Timekey Dupulicate ==>> */
						eventInfo.setCheckTimekeyValidation(false);
						/* 20181128, hhlee, EventTime Sync */
						//eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
						// 2019.03.13_hsryu_Delete Logic.
	                    //eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
	                    /* <<== 20180808, Add, Timekey Dupulicate */
					}
					
					log.info("Calculate Sampling End");
				}
				else
				{
					log.info("No Samplling!");
				}
			}
			catch (Exception ex)
			{
				log.error("Not found sampling count");
				throw new CustomException("Node-0003");
			}
		}
		
	public SampleLot setSampleLotDataforSampleAction(EventInfo eventInfo, Lot lotData, LotAction lotAction)
	{
		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		SampleLot sampleLot = new SampleLot();

		sampleLot.setLotName(lotData.getKey().getLotName());
		sampleLot.setFactoryName(lotData.getFactoryName());
		sampleLot.setProductSpecName(lotData.getProductSpecName());
		sampleLot.setEcCode(lotData.getUdfs().get("ECCODE"));
		sampleLot.setProcessFlowName(lotData.getProcessFlowName());
		sampleLot.setProcessFlowVersion(lotData.getProcessFlowVersion());
		sampleLot.setProcessOperationName(lotData.getProcessOperationName());
		sampleLot.setProcessOperationVersion(lotData.getProcessOperationVersion());
		sampleLot.setMachineName(lotData.getMachineName());
		sampleLot.setSampleProcessFlowName(lotAction.getSampleProcessFlowName());
		sampleLot.setSampleProcessFlowVersion(lotAction.getSampleProcessFlowVersion());
		sampleLot.setFromProcessOperationName(lotAction.getProcessOperationName());
		sampleLot.setFromProcessOperationVersion(lotAction.getProcessOperationVersion());
		sampleLot.setSampleFlag("Y");
		sampleLot.setSampleCount(0);
		sampleLot.setCurrentCount(0);
		sampleLot.setTotalCount(0);
		sampleLot.setSystemSamplePosition("");
		sampleLot.setManualSamplePosition(lotAction.getSamplePosition());
		sampleLot.setActualSamplePosition("");
			// ToDo : If exist same sampling, ActualSamplePosition is mix
		sampleLot.setActualSamplePosition("");
			// ToDo : Check ManualSampleFlag?
		sampleLot.setManualSampleFlag("Y");
		sampleLot.setSampleState("Created");
		sampleLot.setLastEventUser(eventInfo.getEventUser());
		sampleLot.setLastEventComment(eventInfo.getEventComment());
		sampleLot.setReasonCode(eventInfo.getReasonCode());
		sampleLot.setReasonCodeType(eventInfo.getReasonCodeType());
		sampleLot.setLastEventTime(eventInfo.getEventTime());
		sampleLot.setLastEventTimekey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		sampleLot.setLastEventName(eventInfo.getEventName());

		try
		{
			sampleLot = ExtendedObjectProxy.getSampleLotService().create(eventInfo, sampleLot);
		}
		catch(Throwable e)
		{
			log.info("Fail Create SampleLot.");
		}

		return sampleLot;
	}

	/**
	 * 20180323 : setSampleLotData by Auto
	 */
	public SampleLot setSampleLotData(String sampleFlag, EventInfo eventInfo, Lot lotData, SampleLotCount countInfo, String systemPosition, String manualPosition, String holdFlag, boolean sameFlag, boolean sumFlag) throws CustomException
	{
		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		SampleLot sampleLot = new SampleLot();
		String sumPosition= "";

		if(sumFlag)
		{
			sumPosition = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(systemPosition, manualPosition);
		}

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? and processOperationName = ? "
					+ "and processOperationVersion = ? and machineName = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? and fromProcessOperationName = ? and (fromProcessOperationVersion = ? or fromProcessOperationVersion = ?) "
					, new Object[] {lotData.getKey().getLotName(),
					lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(),lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), countInfo.getMachineName(),
					countInfo.getSampleProcessFlowName(), countInfo.getSampleProcessFlowVersion(), countInfo.getFromProcessOperationName(), countInfo.getFromProcessOperationVersion(), "*"});

			if(sampleLotList.size()==1)
			{
				log.info("already SampleLot Info. Start Modify!");
				
				//2019.04.02_hsryu_Insert Logic. Prevent ORA-00001 Error. 
				eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));

				sampleLot = sampleLotList.get(0);

				sampleLot.setSampleFlag(sampleFlag);
				sampleLot.setSampleCount(countInfo.getSampleCount());
				sampleLot.setCurrentCount(countInfo.getCurrentCount());
				sampleLot.setTotalCount(countInfo.getTotalCount());
				sampleLot.setSystemSamplePosition(systemPosition);
				sampleLot.setManualSamplePosition(manualPosition);
				if(sumFlag)
				{
					sampleLot.setActualSamplePosition(sumPosition);
					sampleLot.setManualSampleFlag("ReserveSampling");
				}
				else
				{
					sampleLot.setActualSamplePosition(systemPosition);
					sampleLot.setManualSampleFlag("SystemSampling");
				}
				sampleLot.setSampleState("Decided");
				sampleLot.setSampleOutHoldFlag(holdFlag);

				//}
				sampleLot = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLot);
			}

			return sampleLot;
		}
		catch ( Throwable e )
		{
			if ( sampleLotList.size() == 0 )
			{
				sampleLot.setLotName(lotData.getKey().getLotName());
				sampleLot.setFactoryName(lotData.getFactoryName());
				sampleLot.setProductSpecName(lotData.getProductSpecName());
				sampleLot.setEcCode(lotData.getUdfs().get("ECCODE"));
				sampleLot.setProcessFlowName(lotData.getProcessFlowName());
				sampleLot.setProcessFlowVersion(lotData.getProcessFlowVersion());
				sampleLot.setProcessOperationName(lotData.getProcessOperationName());
				sampleLot.setProcessOperationVersion(lotData.getProcessOperationVersion());
				sampleLot.setMachineName(countInfo.getMachineName());
				sampleLot.setSampleProcessFlowName(countInfo.getSampleProcessFlowName());
				sampleLot.setSampleProcessFlowVersion(countInfo.getSampleProcessFlowVersion());
				sampleLot.setFromProcessOperationName(countInfo.getFromProcessOperationName());
				sampleLot.setFromProcessOperationVersion(countInfo.getFromProcessOperationVersion());
				sampleLot.setSampleFlag(sampleFlag);
				sampleLot.setSampleCount(countInfo.getSampleCount());
				sampleLot.setCurrentCount(countInfo.getCurrentCount());
				sampleLot.setTotalCount(countInfo.getTotalCount());
				sampleLot.setSystemSamplePosition(systemPosition);
				sampleLot.setManualSamplePosition(manualPosition);
				// ToDo : If exist same sampling, ActualSamplePosition is mix
				if(sumFlag)
				{
					sampleLot.setActualSamplePosition(sumPosition);
					sampleLot.setManualSampleFlag("ReserveSampling");
				}
				else
				{
					sampleLot.setActualSamplePosition(systemPosition);
					sampleLot.setManualSampleFlag("SystemSampling");
				}				
				sampleLot.setSampleState("Decided");
				sampleLot.setSampleOutHoldFlag(holdFlag);
				//}
				//2019.02.27_hsryu_Mantis 0002723. eventInfo.getEventUser() -> "System"
				sampleLot.setLastEventUser("System");
				sampleLot.setLastEventComment(eventInfo.getEventComment());
				sampleLot.setReasonCode(eventInfo.getReasonCode());
				sampleLot.setReasonCodeType(eventInfo.getReasonCodeType());
				sampleLot.setLastEventTime(eventInfo.getEventTime());
				sampleLot.setLastEventTimekey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				sampleLot.setLastEventName(eventInfo.getEventName());

				sampleLot = ExtendedObjectProxy.getSampleLotService().create(eventInfo, sampleLot);
			}

			return sampleLot;
		}
	}

	/**
	 * 20180411 : setSampleLotData by OPI (ForceSampling)
	 */
	public SampleLot setSampleLotDataByOPI(String sampleFlag, EventInfo eventInfo, Lot lotData, String machineName, String processFlowName, String processOperationName, String sampleProcessFlowName, String sampleProcessFlowVersion, String samplePosition, String sampleOutHoldFlag,String sampleDepartmentName,String ReasonCodeType,String ReasonCode) throws CustomException
	{
		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		SampleLot sampleLot = new SampleLot();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? and processOperationName = ? and processOperationVersion = ? and machineName = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? ", new Object[] {lotData.getKey().getLotName(),
					lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), processFlowName,
					lotData.getProcessFlowVersion(),processOperationName, lotData.getProcessOperationVersion(), machineName,
					sampleProcessFlowName, sampleProcessFlowVersion});

			if(sampleLotList.size()==1)
			{
				sampleLot = sampleLotList.get(0);

				sampleLot.setSampleFlag(sampleFlag);
				sampleLot.setCurrentCount(0);
				sampleLot.setSystemSamplePosition("");
				sampleLot.setManualSamplePosition(samplePosition);
				sampleLot.setActualSamplePosition(samplePosition);
				sampleLot.setSampleState("Decided");
				
				//sampleLot.setSampleOutHoldFlag(sampleOutHoldFlag);//modfiy by GJJ 20200320 mantis:5851	
				if (StringUtils.isEmpty(sampleLot.getSampleOutHoldFlag())) {
					sampleLot.setSampleOutHoldFlag(sampleOutHoldFlag);
				}
				else {
					sampleLot.setSampleOutHoldFlag(sampleLot.getSampleOutHoldFlag().equals(sampleOutHoldFlag)?sampleOutHoldFlag:"Y");//add by GJJ 20200320 mantis:5851
				}
				
            // modify by JHIYING ON 2019.07.12 0004289 START
				sampleLot.setSampleDepartmentName(sampleDepartmentName);
				sampleLot.setReasonCodeType(ReasonCodeType);
				sampleLot.setReasonCode(ReasonCode);
		   // modify by JHIYING ON 2019.07.12 0004289 END		
				
				sampleLot = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLot);
			}

			return sampleLot;
		}
		catch ( Throwable e )
		{
			if ( sampleLotList.size() == 0 )
			{
				sampleLot.setLotName(lotData.getKey().getLotName());
				sampleLot.setFactoryName(lotData.getFactoryName());
				sampleLot.setProductSpecName(lotData.getProductSpecName());
				sampleLot.setEcCode(lotData.getUdfs().get("ECCODE"));
				sampleLot.setProcessFlowName(processFlowName);
				sampleLot.setProcessFlowVersion(lotData.getProcessFlowVersion());
				sampleLot.setProcessOperationName(processOperationName);
				sampleLot.setProcessOperationVersion(lotData.getProcessOperationVersion());
				sampleLot.setMachineName(machineName);
				sampleLot.setSampleProcessFlowName(sampleProcessFlowName);
				sampleLot.setSampleProcessFlowVersion(sampleProcessFlowVersion);
				sampleLot.setFromProcessOperationName(processOperationName);
				sampleLot.setFromProcessOperationVersion(lotData.getProcessOperationVersion());
				sampleLot.setSampleFlag(sampleFlag);
				sampleLot.setSampleCount(0);
				sampleLot.setCurrentCount(0);
				sampleLot.setTotalCount(0);
				sampleLot.setSystemSamplePosition("");
				sampleLot.setManualSamplePosition(samplePosition);
				sampleLot.setActualSamplePosition(samplePosition);
				// ToDo : Check ManualSampleFlag?
				sampleLot.setManualSampleFlag("ForceSampling");
				sampleLot.setSampleState("Decided");
				sampleLot.setSampleOutHoldFlag(sampleOutHoldFlag);
				sampleLot.setLastEventUser(eventInfo.getEventUser());
				sampleLot.setLastEventComment(eventInfo.getEventComment());
				sampleLot.setReasonCode(eventInfo.getReasonCode());
				sampleLot.setReasonCodeType(eventInfo.getReasonCodeType());
				sampleLot.setLastEventTime(eventInfo.getEventTime());
				sampleLot.setLastEventTimekey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				sampleLot.setLastEventName(eventInfo.getEventName());
				
				// modify by JHIYING ON 2019.07.12 0004289 START
				sampleLot.setSampleDepartmentName(sampleDepartmentName);
				sampleLot.setReasonCodeType(ReasonCodeType);
				sampleLot.setReasonCode(ReasonCode);
				// modify by JHIYING ON 2019.07.12 0004289 END	
				
				
				
				sampleLot = ExtendedObjectProxy.getSampleLotService().create(eventInfo, sampleLot);
			}

			return sampleLot;
		}
	}
	
	/**
	 * 20180411 : setSampleLotData by OPI (ForceSampling)
	 */
	public SampleLot setSampleLotDataByOPI(String sampleFlag, EventInfo eventInfo, Lot lotData, String machineName, String processFlowName, String processOperationName, String sampleProcessFlowName, String sampleProcessFlowVersion, String samplePosition, String sampleOutHoldFlag,String sampleDepartmentName,String ReasonCodeType,String ReasonCode , String holdDepartmentName) throws CustomException
	{
		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		SampleLot sampleLot = new SampleLot();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? and processOperationName = ? and processOperationVersion = ? and machineName = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? ", new Object[] {lotData.getKey().getLotName(),
					lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), processFlowName,
					lotData.getProcessFlowVersion(),processOperationName, lotData.getProcessOperationVersion(), machineName,
					sampleProcessFlowName, sampleProcessFlowVersion});

			if(sampleLotList.size()==1)
			{
				sampleLot = sampleLotList.get(0);

				sampleLot.setSampleFlag(sampleFlag);
				sampleLot.setCurrentCount(0);
				sampleLot.setSystemSamplePosition("");
				sampleLot.setManualSamplePosition(samplePosition);
				sampleLot.setActualSamplePosition(samplePosition);
				sampleLot.setSampleState("Decided");
								
				//sampleLot.setSampleOutHoldFlag(sampleOutHoldFlag);
				if (StringUtils.isEmpty(sampleLot.getSampleOutHoldFlag())) {
					sampleLot.setSampleOutHoldFlag(sampleOutHoldFlag);//add by GJJ 20200320 mantis:5851
				}
				else {
					sampleLot.setSampleOutHoldFlag(sampleLot.getSampleOutHoldFlag().equals(sampleOutHoldFlag)?sampleOutHoldFlag:"Y");//add by GJJ 20200320 mantis:5851
				}
				
				
				sampleLot.setHoldDepartmentName(StringUtils.isNotEmpty(holdDepartmentName)?holdDepartmentName:sampleLot.getHoldDepartmentName());//add by GJJ 20200320 mantis:5851
				
				
				
				
            // modify by JHIYING ON 2019.07.12 0004289 START
				sampleLot.setSampleDepartmentName(sampleDepartmentName);
				sampleLot.setReasonCodeType(ReasonCodeType);
				sampleLot.setReasonCode(ReasonCode);
		   // modify by JHIYING ON 2019.07.12 0004289 END		
				
				sampleLot = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLot);
			}

			return sampleLot;
		}
		catch ( Throwable e )
		{
			if ( sampleLotList.size() == 0 )
			{
				sampleLot.setLotName(lotData.getKey().getLotName());
				sampleLot.setFactoryName(lotData.getFactoryName());
				sampleLot.setProductSpecName(lotData.getProductSpecName());
				sampleLot.setEcCode(lotData.getUdfs().get("ECCODE"));
				sampleLot.setProcessFlowName(processFlowName);
				sampleLot.setProcessFlowVersion(lotData.getProcessFlowVersion());
				sampleLot.setProcessOperationName(processOperationName);
				sampleLot.setProcessOperationVersion(lotData.getProcessOperationVersion());
				sampleLot.setMachineName(machineName);
				sampleLot.setSampleProcessFlowName(sampleProcessFlowName);
				sampleLot.setSampleProcessFlowVersion(sampleProcessFlowVersion);
				sampleLot.setFromProcessOperationName(processOperationName);
				sampleLot.setFromProcessOperationVersion(lotData.getProcessOperationVersion());
				sampleLot.setSampleFlag(sampleFlag);
				sampleLot.setSampleCount(0);
				sampleLot.setCurrentCount(0);
				sampleLot.setTotalCount(0);
				sampleLot.setSystemSamplePosition("");
				sampleLot.setManualSamplePosition(samplePosition);
				sampleLot.setActualSamplePosition(samplePosition);
				// ToDo : Check ManualSampleFlag?
				sampleLot.setManualSampleFlag("ForceSampling");
				sampleLot.setSampleState("Decided");
				sampleLot.setSampleOutHoldFlag(sampleOutHoldFlag);
				sampleLot.setLastEventUser(eventInfo.getEventUser());
				sampleLot.setLastEventComment(eventInfo.getEventComment());
				sampleLot.setReasonCode(eventInfo.getReasonCode());
				sampleLot.setReasonCodeType(eventInfo.getReasonCodeType());
				sampleLot.setLastEventTime(eventInfo.getEventTime());
				sampleLot.setLastEventTimekey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				sampleLot.setLastEventName(eventInfo.getEventName());
				
				// modify by JHIYING ON 2019.07.12 0004289 START
				sampleLot.setSampleDepartmentName(sampleDepartmentName);
				sampleLot.setReasonCodeType(ReasonCodeType);
				sampleLot.setReasonCode(ReasonCode);
				// modify by JHIYING ON 2019.07.12 0004289 END	
				
				// ¹ÚÁ¤¼ö ¼öÁ¤ 2019.07.25 hold department °ü·Ã
				sampleLot.setHoldDepartmentName(holdDepartmentName);
				// ¹ÚÁ¤¼ö ¼öÁ¤ 2019.07.25
				
				
				sampleLot = ExtendedObjectProxy.getSampleLotService().create(eventInfo, sampleLot);
			}

			return sampleLot;
		}
	}

	/**
	 * 20180323 : setSampleProductData
	 */
	public void setSampleProductData(EventInfo eventInfo, Lot lotData, SampleLotCount countInfo, String samplePosition) throws CustomException
	{
		String[] arrsamplePosition = samplePosition.split(",");

		String inCondition = "";
		if ( arrsamplePosition.length > 0 )
		{
			if ( arrsamplePosition.length == 1 )
			{
				inCondition = "'" + arrsamplePosition[0] + "'";
			}
			else
			{
				for ( int i = 0; i < arrsamplePosition.length; i++ )
				{
					inCondition = inCondition + "'" + arrsamplePosition[i] + "'";

					if ( i == arrsamplePosition.length - 1 )
					{
						break;
					}
					else
					{
						inCondition = inCondition + ", ";
					}
				}
			}
		}

		String productListSql = "SELECT PRODUCTNAME FROM PRODUCT "
				+ " WHERE LOTNAME = :LOTNAME "
				+ " AND POSITION IN (" + inCondition + ") "
				+ " ORDER BY POSITION ";

		Map<String, Object> productListbindSet = new HashMap<String, Object>();
		productListbindSet.put("LOTNAME", lotData.getKey().getLotName());

		List<Map<String, Object>> productListSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(productListSql, productListbindSet);
		if ( productListSqlResult.size() > 0 )
		{
			for ( int j = 0; j < productListSqlResult.size(); j++ )
			{
				String productName = (String)productListSqlResult.get(j).get("PRODUCTNAME");
				Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

				List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();
				// Check Duplicate
				try
				{
					sampleProductList = ExtendedObjectProxy.getSampleProductService().select(" productName = ? and lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? and processOperationName = ? and processOperationVersion = ? and machineName = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? " ,
							new Object[]{productName, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
							lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), countInfo.getMachineName(),
							countInfo.getSampleProcessFlowName(), countInfo.getSampleProcessFlowVersion()});
				}
				catch(Exception ex)
				{
					if ( sampleProductList.size() == 0 )
					{
						SampleProduct sampleProduct = new SampleProduct();

						sampleProduct.setProductName(productName);
						sampleProduct.setLotName(lotData.getKey().getLotName());
						sampleProduct.setFactoryName(lotData.getFactoryName());
						sampleProduct.setProductSpecName(lotData.getProductSpecName());
						sampleProduct.setEcCode(lotData.getUdfs().get("ECCODE"));
						sampleProduct.setProcessFlowName(lotData.getProcessFlowName());
						sampleProduct.setProcessFlowVersion(lotData.getProcessFlowVersion());
						sampleProduct.setProcessOperationName(lotData.getProcessOperationName());
						sampleProduct.setProcessOperationVersion(lotData.getProcessOperationVersion());
						sampleProduct.setMachineName(countInfo.getMachineName());
						sampleProduct.setSampleProcessFlowName(countInfo.getSampleProcessFlowName());
						sampleProduct.setSampleProcessFlowVersion(countInfo.getSampleProcessFlowVersion());
						sampleProduct.setPosition(aProduct.getPosition());
						sampleProduct.setLastEventUser(eventInfo.getEventUser());
						sampleProduct.setLastEventComment(eventInfo.getEventComment());
						sampleProduct.setLastEventTime(eventInfo.getEventTime());
						sampleProduct.setLastEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
						sampleProduct.setLastEventName(eventInfo.getEventName());
						
					
						
						
						
						sampleProduct = ExtendedObjectProxy.getSampleProductService().create(eventInfo, sampleProduct);
					}
					else
					{
						// ToDo : Error or Update
						// If duplicate, error? update?
					}
				}
			}
		}
		else
		{
			// ToDo : Error
		}
	}

	/**
	 * 20180412 : setSampleProductData by OPI
	 */
	public void setSampleProductDataByOPI(EventInfo eventInfo, Lot lotData, String machineName, String sampleProcessFlowName, String sampleProcessFlowVersion, String samplePosition) throws CustomException
	{
		String[] arrsamplePosition = samplePosition.split(",");

		String inCondition = "";
		if ( arrsamplePosition.length > 0 )
		{
			if ( arrsamplePosition.length == 1 )
			{
				inCondition = "'" + arrsamplePosition[0] + "'";
			}
			else
			{
				for ( int i = 0; i < arrsamplePosition.length; i++ )
				{
					inCondition = inCondition + "'" + arrsamplePosition[i] + "'";

					if ( i == arrsamplePosition.length - 1 )
					{
						break;
					}
					else
					{
						inCondition = inCondition + ", ";
					}
				}
			}
		}

		String productListSql = "SELECT PRODUCTNAME FROM PRODUCT "
				+ " WHERE LOTNAME = :LOTNAME "
				+ " AND POSITION IN (" + inCondition + ") "
				+ " ORDER BY POSITION ";

		Map<String, Object> productListbindSet = new HashMap<String, Object>();
		productListbindSet.put("LOTNAME", lotData.getKey().getLotName());

		List<Map<String, Object>> productListSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(productListSql, productListbindSet);
		if ( productListSqlResult.size() > 0 )
		{
			for ( int j = 0; j < productListSqlResult.size(); j++ )
			{
				String productName = (String)productListSqlResult.get(j).get("PRODUCTNAME");
				Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

				List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();
				// Check Duplicate
				try
				{
					sampleProductList = ExtendedObjectProxy.getSampleProductService().select(" productName = ? and lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? and processOperationName = ? and processOperationVersion = ? and machineName = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? " ,
							new Object[]{productName, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
							lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName,
							sampleProcessFlowName, sampleProcessFlowVersion});
				}
				catch(Exception ex)
				{
					if ( sampleProductList.size() == 0 )
					{
						SampleProduct sampleProduct = new SampleProduct();

						sampleProduct.setProductName(productName);
						sampleProduct.setLotName(lotData.getKey().getLotName());
						sampleProduct.setFactoryName(lotData.getFactoryName());
						sampleProduct.setProductSpecName(lotData.getProductSpecName());
						sampleProduct.setEcCode(lotData.getUdfs().get("ECCODE"));
						sampleProduct.setProcessFlowName(lotData.getProcessFlowName());
						sampleProduct.setProcessFlowVersion(lotData.getProcessFlowVersion());
						sampleProduct.setProcessOperationName(lotData.getProcessOperationName());
						sampleProduct.setProcessOperationVersion(lotData.getProcessOperationVersion());
						sampleProduct.setMachineName(machineName);
						sampleProduct.setSampleProcessFlowName(sampleProcessFlowName);
						sampleProduct.setSampleProcessFlowVersion(sampleProcessFlowVersion);
						sampleProduct.setPosition(aProduct.getPosition());
						sampleProduct.setLastEventUser(eventInfo.getEventUser());
						sampleProduct.setLastEventComment(eventInfo.getEventComment());
						sampleProduct.setLastEventTime(eventInfo.getEventTime());
						sampleProduct.setLastEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
						sampleProduct.setLastEventName(eventInfo.getEventName());

						sampleProduct = ExtendedObjectProxy.getSampleProductService().create(eventInfo, sampleProduct);
					}
					else
					{
						// ToDo : Error or Update
						// If duplicate, error? update?
					}
				}
			}
		}
		else
		{
			// ToDo : Error
		}
	}

	public String getOperFirstNodeStack(String sampleFlowName) throws CustomException
	{
		String sampleNodeStack = "";

		String sql = " SELECT TONODEID FROM ARC A "
				+ " WHERE A.FROMNODEID = ( "
				+ "       SELECT N.NODEID FROM NODE N "
				+ "        WHERE N.NODETYPE = :NODETYPE "
				+ "          AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME) ";

		Map<String, Object> bindSet = new HashMap<String, Object>();
		bindSet.put("PROCESSFLOWNAME", sampleFlowName);
		bindSet.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		if ( sqlResult.size() > 0 )
		{
			sampleNodeStack = (String)sqlResult.get(0).get("TONODEID");
		}

		return sampleNodeStack;
	}

	public void setSampleLotStateByOPI(String sampleProcessFlowName, String sampleProcessFlowVersion, String machineName, EventInfo eventInfo, Lot lotData) throws CustomException
	{
		if ( sampleProcessFlowName.equals("") || sampleProcessFlowName.isEmpty() )
		{
			List<SampleLotState> sampleLotStateList = new ArrayList<SampleLotState>();
			try
			{
				sampleLotStateList = ExtendedObjectProxy.getSampleLotStateService().select(" lotName = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? " ,
						new Object[]{lotData.getKey().getLotName(), sampleProcessFlowName, sampleProcessFlowVersion});
			}
			catch(Exception ex)
			{
				if ( sampleLotStateList.size() == 0 )
				{
					SampleLotState sampleLotState = new SampleLotState();

					sampleLotState.setLotName(lotData.getKey().getLotName());
					sampleLotState.setSampleProcessFlowName(sampleProcessFlowName);
					sampleLotState.setSampleProcessFlowVersion(sampleProcessFlowVersion);
					sampleLotState.setFactoryName(lotData.getFactoryName());
					sampleLotState.setProductSpecName(lotData.getProductSpecName());
					sampleLotState.setEcCode(lotData.getUdfs().get("ECCODE"));
					sampleLotState.setProcessFlowName(lotData.getProcessFlowName());
					sampleLotState.setProcessFlowVersion(lotData.getProcessFlowVersion());
					sampleLotState.setProcessOperationName(lotData.getProcessOperationName());
					sampleLotState.setProcessOperationVersion(lotData.getProcessOperationVersion());
					sampleLotState.setMachineName(machineName);
					sampleLotState.setSamplePriority(0);
					sampleLotState.setSampleState("Executing");

					sampleLotState = ExtendedObjectProxy.getSampleLotStateService().create(eventInfo, sampleLotState);
				}
				else
				{
					// Nothing
				}
			}
		}
	}

	public void setSampleLotState(SampleLotCount countInfo, String machineName, EventInfo eventInfo, Lot lotData) throws CustomException
	{
		if ( countInfo != null )
		{
			List<SampleLotState> sampleLotStateList = new ArrayList<SampleLotState>();
			try
			{
				sampleLotStateList = ExtendedObjectProxy.getSampleLotStateService().select(" lotName = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? " ,
						new Object[]{lotData.getKey().getLotName(), countInfo.getSampleProcessFlowName(), countInfo.getSampleProcessFlowVersion()});
			}
			catch(Exception ex)
			{
				if ( sampleLotStateList.size() == 0 )
				{
					SampleLotState sampleLotState = new SampleLotState();

					sampleLotState.setLotName(lotData.getKey().getLotName());
					sampleLotState.setSampleProcessFlowName(countInfo.getSampleProcessFlowName());
					sampleLotState.setSampleProcessFlowVersion(countInfo.getSampleProcessFlowVersion());
					sampleLotState.setFactoryName(countInfo.getFactoryName());
					sampleLotState.setProductSpecName(countInfo.getProductSpecName());
					sampleLotState.setEcCode(countInfo.getECCode());
					sampleLotState.setProcessFlowName(countInfo.getProcessFlowName());
					sampleLotState.setProcessFlowVersion(countInfo.getProcessFlowVersion());
					sampleLotState.setProcessOperationName(countInfo.getProcessOperationName());
					sampleLotState.setProcessOperationVersion(countInfo.getProcessOperationVersion());
					sampleLotState.setMachineName(countInfo.getMachineName());
					sampleLotState.setSamplePriority(countInfo.getSamplePriority());
					sampleLotState.setSampleState("Decided");

					sampleLotState = ExtendedObjectProxy.getSampleLotStateService().create(eventInfo, sampleLotState);
				}
				else
				{
					// Nothing
				}
			}
		}
	}
    
	/**
	 * 
	 * @Name     executeLotFutureActionHoldCheck
	 * @since    2018. 8. 8.
	 * @author   hhlee
	 * @contents Check Lot Future Action
	 *           
	 * @param lotName
	 * @param processFlowName
	 * @param processoperationName
	 * @param eventInfo
	 * @param holdType
	 * @return
	 * @throws CustomException
	 */
	public boolean executeLotFutureActionHoldCheck(String lotName, String processFlowName, String processoperationName, EventInfo eventInfo, String holdType) throws CustomException
    {
        boolean islotAction = false;
        
        Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

        String condition = "lotName = ? and factoryName = ? and processFlowName = ? and processOperationName = ? and actionName = ? and holdtype = ?";
        Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), processFlowName, processoperationName, GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD, holdType};

        List<LotAction> lotActionList;
        try
        {
            lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);            
            islotAction = true;
        }
        catch(Exception ex)
        {
            log.info("Lot : [ " + lotName + " ] NonExist FutureAction Data.");           
        }        
     
        return islotAction;
    }
	//2018.04.03 dmlee : Check and Execute Lot Future Action
	public Lot executeLotFutureActionHold(String lotName, String processFlowName, String processoperationName, EventInfo eventInfo, String holdType) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		String condition = "lotName = ? and factoryName = ? and processFlowName = ? and processOperationName = ? and actionName = ? and actionState = ? ";
		Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), processFlowName, processoperationName, GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD, GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED};

		List<LotAction> lotActionList;
		
		try
		{
			lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			log.info("Lot : [ " + lotName + " ] NonExist FutureAction Data.");
			return lotData;
		}

		for(LotAction lotAction : lotActionList)
		{
			if(lotAction.getHoldType().equals(holdType)) //Future Hold
			{
				//BHold
				if(lotAction.getHoldType().equals(GenericServiceProxy.getConstantMap().HOLDTYPE_BHOLD)
						&& lotAction.getProcessFlowName().equals(processFlowName) && lotAction.getProcessOperationName().equals(processoperationName))
				{
					/* 20181212, hhlee, modify, Change EventUser ==>> */
				    //EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("FutureBHold", eventInfo.getEventUser(), lotAction.getLastEventComment(), null, null);
				    EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("FutureBHold", lotAction.getLastEventUser(), lotAction.getLastEventComment(), null, null);
					/* <<== 20181212, hhlee, modify, Change EventUser */
				    
					/* 20181128, hhlee, EventTime Sync */
					//holdEventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
					holdEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					holdEventInfo.setReasonCode(lotAction.getHoldCode());
					holdEventInfo.setReasonCodeType(GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT);

					if(!lotData.getLotHoldState().equals("Y"))
					{
						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
						MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
//						String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//						makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
						
						/* 20190212, hhlee, add, lot holdtime, holdreleasetime ==>> */
						
						//2019.02.19_hsryu_Delete HoldTime.
				        // makeOnHoldInfo.getUdfs().put("HOLDTIME",eventInfo.getEventTime().toString());
				        makeOnHoldInfo.getUdfs().put("HOLDRELEASETIME",StringUtil.EMPTY);
				        /* <<== 20190212, hhlee, add, lot holdtime, holdreleasetime */
				        // 2019.05.29_hsryu_Change Update -> SetEvent.
				        makeOnHoldInfo.getUdfs().put("NOTE", lotAction.getLastEventComment());
				        
						LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), holdEventInfo, makeOnHoldInfo);
						
						// 2019.05.29_hsryu_Add Logic.
						Map<String, String> updateUdfs = new HashMap<String, String>();
						updateUdfs.put("NOTE", "");
						MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);

						try {
							lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotAction.getLotName(), lotAction.getHoldCode(), lotAction.getDepartment(), lotAction.getHoldType() , holdEventInfo);
						} catch (Exception e) {
							log.warn(e);
						}
						
						// 2019.05.29_hsryu_Delete Logic. Not Update Logic.
						//this.setNote(lotName, lotAction.getLastEventComment(), holdEventInfo);

						// -------------------------------------------------------------------------------------------------------------------------------------------
					}
					else
					{
						SetEventInfo setEventInfo = new SetEventInfo();
//						String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//						setEventInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
						
						/* 20190212, hhlee, add, lot holdtime, holdreleasetime ==>> */
						
						//2019.02.19_hsryu_Delete HoldTime.
						//setEventInfo.getUdfs().put("HOLDTIME",eventInfo.getEventTime().toString());
						setEventInfo.getUdfs().put("HOLDRELEASETIME",StringUtil.EMPTY);
				        /* <<== 20190212, hhlee, add, lot holdtime, holdreleasetime */
						// 2019.05.29_hsryu_Change Update -> SetEvent.
						setEventInfo.getUdfs().put("NOTE", lotAction.getLastEventComment());
				        
						LotServiceProxy.getLotService().setEvent(lotData.getKey(), holdEventInfo, setEventInfo);
						
						// 2019.05.29_hsryu_Add Logic.
						Map<String, String> updateUdfs = new HashMap<String, String>();
						updateUdfs.put("NOTE", "");
						MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
						
						try {
							lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotAction.getLotName(), lotAction.getHoldCode(), lotAction.getDepartment(),lotAction.getHoldType(), holdEventInfo);
						} catch (Exception e) {
							log.warn(e);
						}
						
						// 2019.05.29_hsryu_Delete Logic. Not Update Logic. 
						//this.setNote(lotName, lotAction.getLastEventComment() , holdEventInfo);

						// -------------------------------------------------------------------------------------------------------------------------------------------
					}
					
					if(lotAction.getHoldPermanentFlag().equals("N"))
					{
						lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);
						lotAction.setHoldCount(lotAction.getHoldCount()+1);
						lotAction.setLastEventTime(holdEventInfo.getEventTime());
						lotAction.setLastEventTimeKey(holdEventInfo.getEventTimeKey());

						ExtendedObjectProxy.getLotActionService().remove(holdEventInfo, lotAction);
					}
					else
					{
						lotAction.setHoldCount(lotAction.getHoldCount()+1);
						lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);
						//2019.02.27_hsryu_remain.
						//lotAction.setLastEventName(holdEventInfo.getEventName());
						lotAction.setLastEventTime(holdEventInfo.getEventTime());
						lotAction.setLastEventTimeKey(holdEventInfo.getEventTimeKey());
						//2019.02.27_hsryu_remain.
						//lotAction.setLastEventUser(holdEventInfo.getEventUser());
						//2019.02.27_hsryu_remain.
						//lotAction.setLastEventComment(holdEventInfo.getEventComment());
						ExtendedObjectProxy.getLotActionService().modify(holdEventInfo, lotAction);
						
						ExtendedObjectProxy.getPermanentHoldInfoService().createPermanentHoldInfo(lotData, "*","*", "*", lotAction.getProcessFlowName(),
								lotAction.getProcessFlowVersion(), lotAction.getProcessOperationName(), lotAction.getProcessOperationVersion()
								, "BHOLD", lotAction.getHoldCode(), lotAction.getDepartment(), holdEventInfo);
					}
				}
				//AHold
				else if(lotAction.getHoldType().equals(GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD)
						&& lotAction.getProcessFlowName().equals(processFlowName) && lotAction.getProcessOperationName().equals(processoperationName))
				{
					
				    /* 20181212, hhlee, modify, Change EventUser ==>> */
                    //EventInfo holdEventInfo = EventInfoUtil.makeEventInfo(lotAction.getLastEventName(), eventInfo.getEventUser(), lotAction.getLastEventComment(), null, null);
				    EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("FutureAHold", lotAction.getLastEventUser(), lotAction.getLastEventComment(), null, null);
                    /* <<== 20181212, hhlee, modify, Change EventUser */
					
					/* 20181128, hhlee, EventTime Sync */
					holdEventInfo.setEventTime(eventInfo.getEventTime());
					//holdEventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
					holdEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					holdEventInfo.setReasonCode(lotAction.getHoldCode());
					holdEventInfo.setReasonCodeType(GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT);
					holdEventInfo.setEventUser(lotAction.getLastEventUser());
					
					if(!lotData.getLotHoldState().equals("Y"))
					{
						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
						MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
//						String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//						makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
						
						/* 20190212, hhlee, add, lot holdtime, holdreleasetime ==>> */
						//2019.02.19_hsryu_Delete HoldTime.
                        //makeOnHoldInfo.getUdfs().put("HOLDTIME",eventInfo.getEventTime().toString());
                        makeOnHoldInfo.getUdfs().put("HOLDRELEASETIME",StringUtil.EMPTY);
                        /* <<== 20190212, hhlee, add, lot holdtime, holdreleasetime */
                        // 2019.05.29_hsryu_Change Update -> SetEvent.
                        makeOnHoldInfo.getUdfs().put("NOTE", lotAction.getLastEventComment());
                        
						LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), holdEventInfo, makeOnHoldInfo);
						
						// 2019.05.29_hsryu_Add Logic.
						Map<String, String> updateUdfs = new HashMap<String, String>();
						updateUdfs.put("NOTE", "");
						MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
						
						// 2019.05.29_hsryu_Delete Logic. Not Update Logic. 
						//this.setNote(lotName, lotAction.getLastEventComment(), holdEventInfo);

						try {
							lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotAction.getLotName(), lotAction.getHoldCode(), lotAction.getDepartment(), lotAction.getHoldType(), holdEventInfo);
						} catch (Exception e) {
							log.warn(e);
						}
						// -------------------------------------------------------------------------------------------------------------------------------------------
					}
					else
					{
						SetEventInfo setEventInfo = new SetEventInfo();
//						String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//						setEventInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
						
						/* 20190212, hhlee, add, lot holdtime, holdreleasetime ==>> */
						
						//2019.02.19_hsryu_Delete HoldTime.
						//setEventInfo.getUdfs().put("HOLDTIME",eventInfo.getEventTime().toString());
						setEventInfo.getUdfs().put("HOLDRELEASETIME",StringUtil.EMPTY);
                        /* <<== 20190212, hhlee, add, lot holdtime, holdreleasetime */
						// 2019.05.29_hsryu_Delete Logic. Not Update Logic. 
						setEventInfo.getUdfs().put("NOTE",lotAction.getLastEventComment());
						
						LotServiceProxy.getLotService().setEvent(lotData.getKey(), holdEventInfo, setEventInfo);
						
						// 2019.05.29_hsryu_Add Logic.
						Map<String, String> updateUdfs = new HashMap<String, String>();
						updateUdfs.put("NOTE", "");
						MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
						
						try {
							lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotAction.getLotName(), lotAction.getHoldCode(), lotAction.getDepartment(),lotAction.getHoldType(), holdEventInfo);
						} catch (Exception e) {
							log.warn(e);
						}
						
						// 2019.05.29_hsryu_Delete Logic. Not Update Logic. 
						//this.setNote(lotName, lotAction.getLastEventComment(), holdEventInfo);
						
						// -------------------------------------------------------------------------------------------------------------------------------------------
					}

					if(lotAction.getHoldPermanentFlag().equals("N"))
					{
						lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);
						lotAction.setHoldCount(lotAction.getHoldCount()+1);
						lotAction.setLastEventTime(holdEventInfo.getEventTime());
						lotAction.setLastEventTimeKey(holdEventInfo.getEventTimeKey());

						ExtendedObjectProxy.getLotActionService().remove(holdEventInfo, lotAction);
					}
					else
					{
						long lastHoldCnt = lotAction.getHoldCount();
						lotAction.setHoldCount(lotAction.getHoldCount()+1);
						//lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);
						lotAction.setLastEventName(holdEventInfo.getEventName());
						lotAction.setLastEventTime(holdEventInfo.getEventTime());
						lotAction.setLastEventTimeKey(holdEventInfo.getEventTimeKey());
						lotAction.setLastEventUser(holdEventInfo.getEventUser());
						lotAction.setLastEventComment(holdEventInfo.getEventComment());
						ExtendedObjectProxy.getLotActionService().modify(holdEventInfo, lotAction);
					}
				}
			}
		}

		Lot resultLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		return resultLotData;
	}
	
	//2018.08.13 jspark : Check and Execute Lot Future Action by Operation
	public Lot executeLotFutureActionHoldByOperation(String lotName, String processFlowName, String processoperationName, EventInfo eventInfo, String holdType) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		String condition = "FACTORYNAME = ? and PRODUCTSPECNAME = ? and PRODUCTSPECVERSION = ? and ECCODE = ? and " + " PROCESSFLOWNAME = ? and PROCESSFLOWVERSION = ? and PROCESSOPERATIONNAME = ? and PROCESSOPERATIONVERSION = ? and ACTIONNAME = ? ";
		//Object[] bindSet = new Object[]{lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(),lotData.getProcessOperationName() ,holdType};
		Object[] bindSet = new Object[]{ lotData.getFactoryName(), lotData.getProductSpecName(), "00001", lotData.getUdfs().get("ECCODE"), processFlowName, "00001", processoperationName, "00001", GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD};

		List<OperAction> operActionList;
		try
		{
			operActionList = ExtendedObjectProxy.getOperActionService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			log.info("Lot : [ " + lotName + " ] NonExist FutureAction Data.");
			return lotData;
		}

		for(OperAction operAction : operActionList)
		{
			if(operAction.getHoldType().equals(holdType)) //Future Hold
			{
				//BHold
				if(operAction.getHoldType().equals(GenericServiceProxy.getConstantMap().HOLDTYPE_BHOLD)
						&& operAction.getProcessFlowName().equals(processFlowName) && operAction.getProcessOperationName().equals(processoperationName))
				{
				    EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("FutureBHoldByOperation", operAction.getLastEventUser(), operAction.getLastEventComment(), null, null);
					/* <<== 20181212, hhlee, modify, Change EventUser */
				    
					/* 20181128, hhlee, EventTime Sync */
					//holdEventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
					holdEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					holdEventInfo.setReasonCode(operAction.getHoldCode());
					holdEventInfo.setReasonCodeType(GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT);

					if(!lotData.getLotHoldState().equals("Y"))
					{
						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
						MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
//						String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//						makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
						LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), holdEventInfo, makeOnHoldInfo);

						try {
							lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), operAction.getHoldCode(), lotData.getUdfs().get("DEPARTMENT"), operAction.getHoldType(), holdEventInfo);
						} catch (Exception e) {
							log.warn(e);
						}
						// -------------------------------------------------------------------------------------------------------------------------------------------
					}
					else
					{
						SetEventInfo setEventInfo = new SetEventInfo();
//						String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//						setEventInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
						LotServiceProxy.getLotService().setEvent(lotData.getKey(), holdEventInfo, setEventInfo);
						
						try {
							lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), operAction.getHoldCode(),lotData.getUdfs().get("DEPARTMENT"),operAction.getHoldType(), holdEventInfo);
						} catch (Exception e) {
							log.warn(e);
						}
						// -------------------------------------------------------------------------------------------------------------------------------------------

						// 2019.03.11_hsryu_Delete ProductHistory Hold Event. 
//						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
//
//						for(ProductU product : productUSequence)
//						{
//							Product aProduct =  MESProductServiceProxy.getProductInfoUtil().getProductByProductName(product.getProductName());
//							kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfoP = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
//							ProductServiceProxy.getProductService().setEvent(aProduct.getKey(), holdEventInfo, setEventInfoP);
//							
//							try {
//								MESProductServiceProxy.getProductServiceImpl().addMultiHoldProduct(product.getProductName(), operAction.getHoldCode(), lotData.getUdfs().get("DEPARTMENT"),operAction.getHoldType(),  holdEventInfo);
//							} catch (Exception e) {
//								log.warn(e);
//							}
//							// -------------------------------------------------------------------------------------------------------------------------------------------
//						}
					}
					
					ExtendedObjectProxy.getPermanentHoldInfoService().createPermanentHoldInfo(lotData, operAction.getProductSpecName(), operAction.getProductSpecVersion(), operAction.getEcCode(), operAction.getProcessFlowName(),
							operAction.getProcessFlowVersion(), operAction.getProcessOperationName(), operAction.getProcessOperationVersion()
							, "OPERHOLD", operAction.getHoldCode(), operAction.getDepartmentName(), eventInfo);
				}
				//AHold
				else if(operAction.getHoldType().equals(GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD)
						&& operAction.getProcessFlowName().equals(processFlowName) && operAction.getProcessOperationName().equals(processoperationName))
				{
					eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					eventInfo.setReasonCode(operAction.getHoldCode());
					eventInfo.setReasonCodeType("HoldLot");
					eventInfo.setEventName("FutureAHoldByOperation");
					eventInfo.setEventComment(operAction.getLastEventComment());

					if(!lotData.getLotHoldState().equals("Y"))
					{
						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
						MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
//						String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//						makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
						LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo, makeOnHoldInfo);

						// -------------------------------------------------------------------------------------------------------------------------------------------
						// Modified by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
//							Map<String,String> multiHoldudfs = new HashMap<String, String>();
//							//2018.05.09 dmlee : To Be Modify EventUserDep
//							multiHoldudfs.put("eventuserdep", "");
//
//							LotMultiHoldKey multiholdkey = new LotMultiHoldKey();
//							multiholdkey.setLotName(lotAction.getLotName());
//							multiholdkey.setReasonCode(lotAction.getHoldCode());
//
//							LotMultiHold multihold = LotServiceProxy.getLotMultiHoldService().selectByKey(multiholdkey);
//							multihold.setUdfs(multiHoldudfs);
//
//							LotServiceProxy.getLotMultiHoldService().update(multihold);
						try {
							lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot( lotData.getKey().getLotName() , operAction.getHoldCode(), lotData.getUdfs().get("DEPARTMENT"), operAction.getHoldType(), eventInfo);
						} catch (Exception e) {
							log.warn(e);
						}
						// -------------------------------------------------------------------------------------------------------------------------------------------
					}
					else
					{
						SetEventInfo setEventInfo = new SetEventInfo();
//						String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//						setEventInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
						LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
						
						// -------------------------------------------------------------------------------------------------------------------------------------------
						// Modified by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
//							LotMultiHoldKey multiholdkey = new LotMultiHoldKey();
//							multiholdkey.setLotName(lotAction.getLotName());
//							multiholdkey.setReasonCode(lotAction.getHoldCode());
//
//							LotMultiHold multihold = new LotMultiHold();
//
//							Map<String,String> multiHoldudfs = new HashMap<String, String>();
//							//2018.05.09 dmlee : To Be Modify EventUserDep
//							multiHoldudfs.put("eventuserdep", "");
//
//							multihold.setKey(multiholdkey);
//							multihold.setUdfs(multiHoldudfs);
//							multihold.setEventTime(eventInfo.getEventTime());
//							multihold.setEventName(eventInfo.getEventName());
//							multihold.setEventUser(eventInfo.getEventUser());
//							LotServiceProxy.getLotMultiHoldService().insert(multihold);
						try {
							lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), operAction.getHoldCode(),lotData.getUdfs().get("DEPARTMENT"), operAction.getHoldType(), eventInfo);
						} catch (Exception e) {
							log.warn(e);
						}
						// -------------------------------------------------------------------------------------------------------------------------------------------

//						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
//
//						for(ProductU product : productUSequence)
//						{
//							Product aProduct =  MESProductServiceProxy.getProductInfoUtil().getProductByProductName(product.getProductName());
//							kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfoP = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
//							ProductServiceProxy.getProductService().setEvent(aProduct.getKey(), eventInfo, setEventInfoP);
//							
//							// -------------------------------------------------------------------------------------------------------------------------------------------
//							// Modified by smkang on 2018.08.13 - According to user's requirement, ProductName/ReasonCode/Department/EventComment are necessary to be keys.
////								ProductMultiHoldKey prdMultiHoldKey = new ProductMultiHoldKey();
////								prdMultiHoldKey.setProductName(product.getProductName());
////								prdMultiHoldKey.setReasonCode(lotAction.getHoldCode());
////
////								ProductMultiHold prdMultiHold = new ProductMultiHold();
////
////								prdMultiHold.setKey(prdMultiHoldKey);
////								prdMultiHold.setEventTime(eventInfo.getEventTime());
////								prdMultiHold.setEventName(eventInfo.getEventName());
////								prdMultiHold.setEventUser(eventInfo.getEventUser());
////
////								prdMultiHold.setUdfs(multiHoldudfs);
////
////								ProductServiceProxy.getProductMultiHoldService().insert(prdMultiHold);
//							try {
//								MESProductServiceProxy.getProductServiceImpl().addMultiHoldProduct(product.getProductName(), operAction.getHoldCode(), lotData.getUdfs().get("DEPARTMENT"), operAction.getHoldType(), eventInfo);
//							} catch (Exception e) {
//								log.warn(e);
//							}
//							// -------------------------------------------------------------------------------------------------------------------------------------------
//						}
					}

				}
			}
		}

		Lot resultLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);


		return resultLotData;
	}
	

	//2018.04.03 dmlee : Check and Execute Lot Future Action
	public Lot executeLotFutureActionChange(String lotName, EventInfo eventInfo) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		String workorder = lotData.getProductRequestName();
		
		String condition = "lotName = ? and factoryName = ? and processFlowName = ? and processOperationName = ? and actionName = ? and actionState = ? ";
		Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), GenericServiceProxy.getConstantMap().ACTIONNAME_CHANGE, GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED};

		List<LotAction> lotActionList;
		try
		{
			lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			log.info("Lot : [ " + lotName + " ] NonExist FutureAction Data.");
			return lotData;
		}

		for(LotAction lotAction : lotActionList)
		{
			if(lotAction.getActionName().equals(GenericServiceProxy.getConstantMap().ACTIONNAME_CHANGE)
					&& lotAction.getProcessFlowName().equals(lotData.getProcessFlowName()) && lotAction.getProcessOperationName().equals(lotData.getProcessOperationName())) //Reserve Change
			{
				List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
				
				// 2019.05.06_hsryu_Move Logic location. 
				if (!StringUtil.equals(workorder, lotAction.getChangeProductRequestName()))
				{
					// 2019.04.25_Delete Logic. if WO Name of Lot is 'MIXED', Error......
//					// Decrease Old WorkOrder
//					ProductRequestKey oldKey = new ProductRequestKey();
//					oldKey.setProductRequestName(workorder);
//					ProductRequest oldWorkorder = ProductRequestServiceProxy.getProductRequestService().selectByKey(oldKey);
//					long glassCount = (long) lotData.getProductQuantity();
//					
//					// New WorkOrder
//					ProductRequestKey newKey = new ProductRequestKey();
//					newKey.setProductRequestName(lotAction.getChangeProductRequestName());
//					ProductRequest newWorkorder = ProductRequestServiceProxy.getProductRequestService().selectByKey(newKey);
//
//					validationWorkOrder(oldWorkorder, newWorkorder, glassCount, true);
//					
//					kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo oldChangespecInfo = new kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo(); 
//					oldChangespecInfo.setFactoryName(oldWorkorder.getFactoryName());
//					oldChangespecInfo.setProductSpecName(oldWorkorder.getProductSpecName());
//					oldChangespecInfo.setProductRequestType(oldWorkorder.getProductRequestType());
//					oldChangespecInfo.setPlanQuantity(Long.valueOf(oldWorkorder.getPlanQuantity()));
//					oldChangespecInfo.setProductRequestState(oldWorkorder.getProductRequestState());
//					oldChangespecInfo.setPlanReleasedTime(oldWorkorder.getPlanReleasedTime());
//					oldChangespecInfo.setPlanFinishedTime(oldWorkorder.getPlanFinishedTime());
//					oldChangespecInfo.setReleasedQuantity(oldWorkorder.getReleasedQuantity()-glassCount);
//					oldChangespecInfo.setUdfs(oldWorkorder.getUdfs());
//					
//					String oldEventComment = String.format("ChangeWorkOrder after T/O : Old[%s]->New[%s]", workorder, lotAction.getChangeProductRequestName());
//					
//					eventInfo.setEventName("DecrementReleasedQuantity");
//					eventInfo.setEventComment(oldEventComment);
//					
//					ProductRequest oldData = ProductRequestServiceProxy.getProductRequestService().changeSpec(oldKey, eventInfo, oldChangespecInfo);
//					
//					// Add Old WorkOrder History
//					MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(oldData, eventInfo);
//					
//					// Increase new workorder
//					kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo newChangespecInfo = new kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo(); 
//					newChangespecInfo.setFactoryName(newWorkorder.getFactoryName());
//					newChangespecInfo.setProductSpecName(newWorkorder.getProductSpecName());
//					newChangespecInfo.setProductRequestType(newWorkorder.getProductRequestType());
//					newChangespecInfo.setPlanQuantity(Long.valueOf(newWorkorder.getPlanQuantity()));
//					newChangespecInfo.setProductRequestState(newWorkorder.getProductRequestState());
//					newChangespecInfo.setPlanReleasedTime(newWorkorder.getPlanReleasedTime());
//					newChangespecInfo.setPlanFinishedTime(newWorkorder.getPlanFinishedTime());
//					
//					newChangespecInfo.setReleasedQuantity(newWorkorder.getReleasedQuantity()+glassCount);
//					
//					newChangespecInfo.setUdfs(newWorkorder.getUdfs());
//					
//					String newEventComment = String.format("ChangeWorkOrder after T/O : Old[%s]->New[%s]", workorder, lotAction.getChangeProductRequestName());
//					
//					eventInfo.setEventName("IncrementReleasedQuantity");
//					eventInfo.setEventComment(newEventComment);
//					
//					ProductRequest newData = ProductRequestServiceProxy.getProductRequestService().changeSpec(newKey, eventInfo, newChangespecInfo);
//					
//					// Add Old WorkOrder History
//					MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(newData, eventInfo);
					
					MESWorkOrderServiceProxy.getProductRequestServiceUtil().adjustWorkOrder(lotData, lotAction.getChangeProductRequestName(), eventInfo);
				}

				/* 20181128, hhlee, EventTime Sync */
				//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventName("ChangeSpec");
				eventInfo.setEventComment("Change Spec By Future Action");
				eventInfo.setBehaviorName("ARRAY");
				/* 20181212, hhlee, add, Change EventUser ==>> */
				eventInfo.setEventUser(lotAction.getLastEventUser());
                /* <<== 20181212, hhlee, add, Change EventUser */
				
				ProductSpecKey productSpecKey = new ProductSpecKey();
				productSpecKey.setFactoryName(lotAction.getFactoryName());
				productSpecKey.setProductSpecName(lotAction.getChangeProductSpecName());
				productSpecKey.setProductSpecVersion("00001");

				ProductSpec productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);

				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(lotAction.getChangeProductRequestName());

				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

				Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(lotAction.getFactoryName(), lotAction.getChangeProcessFlowName(), "00001", "ProcessOperation", lotAction.getChangeProcessOperationName(), "00001");

				// 2019.05.06_hsryu_Modify Logic. WorkOrderType -> ProductionType. 
				ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(lotData.getProductionType(), lotAction.getChangeProductSpecName(),
						"00001", lotData.getProductSpec2Name(), lotData.getProductSpec2Version(), lotAction.getChangeProductRequestName(),
						productSpecData.getSubProductUnitQuantity1(), productSpecData.getSubProductUnitQuantity2(), lotData.getDueDate(), lotData.getPriority(), lotAction.getFactoryName(),
						lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(), lotAction.getChangeProcessFlowName(), "00001",
						lotAction.getChangeProcessOperationName(), "00001", nodeData.getKey().getNodeId(), productUSequence);

				LotServiceProxy.getLotService().changeSpec(lotData.getKey(), eventInfo, changeSpecInfo);

				if(StringUtil.equals(lotAction.getHoldPermanentFlag(), "N") || StringUtil.isEmpty(lotAction.getHoldPermanentFlag()))
				{
					lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);
					// 2019.05.29_hsryu_Not Modify. Delete LotAction.
					//ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotAction);
					ExtendedObjectProxy.getLotActionService().remove(eventInfo, lotAction);
				}
			}
		}

		Lot resultLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);


		return resultLotData;
	}
	
	public void validationWorkOrder(ProductRequest oldWorkOrder, ProductRequest newWorkOrder, long glassQty, boolean changeSpecFlag) throws CustomException
	{
		long oldPlanQty = oldWorkOrder.getPlanQuantity();
		long oldReleasedQty = oldWorkOrder.getReleasedQuantity();
		long oldFinishedQty = oldWorkOrder.getFinishedQuantity();
		long oldScrappedQty = oldWorkOrder.getScrappedQuantity();
		String oldHoldFlag = oldWorkOrder.getProductRequestHoldState();
						
		long newPlanQty = newWorkOrder.getPlanQuantity();
		long newReleasedQty = newWorkOrder.getReleasedQuantity();
		long newFinishedQty = newWorkOrder.getFinishedQuantity();
		long newScrappedQty = newWorkOrder.getScrappedQuantity();
		String newHoldFlag = newWorkOrder.getProductRequestHoldState();
		
		if ( oldReleasedQty - glassQty < oldFinishedQty + oldScrappedQty )
		{
			throw new CustomException("PRODUCTREQUEST-0052", oldReleasedQty, glassQty, oldFinishedQty, oldScrappedQty);
		}
		
		if ( newReleasedQty + glassQty < newFinishedQty + newScrappedQty )
		{
			throw new CustomException("PRODUCTREQUEST-0052", newReleasedQty, glassQty, newFinishedQty, newScrappedQty);
		}

		if(!changeSpecFlag)
		{
			if ( StringUtil.equals(oldHoldFlag, "Y") )
			{
				throw new CustomException("PRODUCTREQUEST-0030", oldWorkOrder.getKey().getProductRequestName(), oldHoldFlag);
			}
			
			if ( StringUtil.equals(newHoldFlag, "Y") )
			{
				throw new CustomException("PRODUCTREQUEST-0030", newWorkOrder.getKey().getProductRequestName(), newHoldFlag);
			}		
		}
	}

	//2018.04.03 dmlee : Check and Execute Lot Future Action By System Hold
	public Lot executeLotFutureActionBySystemHold(String lotName, String beforeFlowName, String beforeOperationName, EventInfo eventInfo) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//		String workorder = lotData.getProductRequestName();
		
		String condition = "lotName = ? and factoryName = ? and processFlowName = ? and processOperationName = ? and actionName = ? ";
		Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), beforeFlowName, beforeOperationName, GenericServiceProxy.getConstantMap().ACTIONNAME_SYSTEMHOLD};

		List<LotAction> lotActionList;
		try
		{
			lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			log.info("Lot : [ " + lotName + " ] NonExist FutureAction Data.");
			return lotData;
		}

		for(LotAction lotAction : lotActionList)
		{
			//AHold
			if(lotAction.getHoldType().equals(GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD)
					&& lotAction.getProcessFlowName().equals(beforeFlowName) && lotAction.getProcessOperationName().equals(beforeOperationName))
			{
				
			    EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("FutureAHold", lotAction.getLastEventUser(), lotAction.getLastEventComment(), null, null);

				holdEventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				holdEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				holdEventInfo.setReasonCode(lotAction.getHoldCode());
				holdEventInfo.setReasonCodeType(GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT);
				holdEventInfo.setEventComment(lotAction.getLastEventComment());
				
				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
				MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
//				String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//				makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
				
				/* 20190212, hhlee, add, lot holdtime, holdreleasetime ==>> */
				
				//2019.02.19_hsryu_Delete HoldTime.
		        //makeOnHoldInfo.getUdfs().put("HOLDTIME",eventInfo.getEventTime().toString());
		        makeOnHoldInfo.getUdfs().put("HOLDRELEASETIME",StringUtil.EMPTY);
		        //2019.05.13 dmlee : Set Note
		        makeOnHoldInfo.getUdfs().put("NOTE", lotAction.getLastEventComment());
		        /* <<== 20190212, hhlee, add, lot holdtime, holdreleasetime */
				
				LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), holdEventInfo, makeOnHoldInfo);
				
				// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//				
//				//2019.05.13 dmlee : Clear Note
//				Map<String, String> udfs = new HashMap<String, String>();
//				udfs.put("NOTE", "");
//				lotData.setUdfs(udfs);
//				LotServiceProxy.getLotService().update(lotData);
				Map<String, String> updateUdfs = new HashMap<String, String>();
				updateUdfs.put("NOTE", "");
				MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
				
				//2019.04.28 dmlee : SPC SystemHold need Department
				String systemHoldDepartment = " ";
				if(!StringUtils.isEmpty(lotAction.getDepartment()))
				{
					systemHoldDepartment = lotAction.getDepartment();
				}

				try {
					lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotAction.getLotName(), lotAction.getHoldCode(), systemHoldDepartment, lotAction.getHoldType(), holdEventInfo);
				} catch (Exception e) {
					log.warn(e);
				}
				// -------------------------------------------------------------------------------------------------------------------------------------------

				//2019.02.25_hsryu_Delete Logic.
//				String woName = lotAction.getChangeProductRequestName();
				
				if(lotAction.getHoldPermanentFlag().equals("N"))
				{
					lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);
					lotAction.setHoldCount(lotAction.getHoldCount()+1);
					ExtendedObjectProxy.getLotActionService().remove(eventInfo, lotAction);
				}
				else
				{
					lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);
					lotAction.setHoldCount(lotAction.getHoldCount()+1);
					// 2019.05.29_hsryu_Remain. 
					//lotAction.setLastEventName(eventInfo.getEventName());
					lotAction.setLastEventTime(eventInfo.getEventTime());
					lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
					// 2019.05.29_hsryu_Remain.
					//lotAction.setLastEventUser(eventInfo.getEventUser());
					//lotAction.setLastEventComment(eventInfo.getEventComment());
					ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotAction);
				}
			}
		}

		Lot resultLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);


		return resultLotData;
	}

	// sum Position. originalPosition + sumPosition!
	public ArrayList<String> sumPosition(String originalPosition, ArrayList<String> sumPosition)
	{
		if (originalPosition.contains(","))
		{
			String[] posPositionArr = null;
			posPositionArr = originalPosition.split(",");

			for (int i = 0; i < posPositionArr.length; i++)
			{
				sumPosition.add(posPositionArr[i]);
			}
		}
		else
		{
			sumPosition.add(originalPosition);
		}

		return sumPosition;
	}

	//sumPosition and Sort
	public String sumPositionAndSort(String fromPosition, String toPosition)
	{
		String[] fromPositionArr = null;
		String[] toPositionArr = null;
		String strSumPosition;

		ArrayList<String> sumPosition = new ArrayList<String>();

		//sum Position
		if(fromPosition.contains(","))
		{
			fromPositionArr = fromPosition.split(",");

			for(int i=0;i<fromPositionArr.length;i++)
			{
				sumPosition.add(fromPositionArr[i]);
			}

		}
		else
		{
			sumPosition.add(fromPosition);
		}

		if(toPosition.contains(","))
		{
			toPositionArr = toPosition.split(",");

			for(int i=0; i<toPositionArr.length;i++)
			{
				sumPosition.add(toPositionArr[i]);
			}
		}
		else
		{
			sumPosition.add(toPosition);
		}

		//remove overlap
		for (int i = 0; i < sumPosition.size(); i++)
		{
			if (i == sumPosition.size() - 1)
			{
				break;
			}

			for (int j = i + 1; j < sumPosition.size(); j++)
			{
				if (sumPosition.get(i).equals(sumPosition.get(j)))
				{
					sumPosition.remove(j);
					break;
				}
			}
		}

		//sort
		for (int i = 0; i < sumPosition.size(); i++)
		{
			for (int j = i + 1; j < sumPosition.size(); j++)
			{
				if (Integer.parseInt(sumPosition.get(i)) > Integer.parseInt(sumPosition.get(j)))
				{
					int temp = 0;
					temp = Integer.parseInt(sumPosition.get(i));
					sumPosition.set(i, sumPosition.get(j));
					sumPosition.set(j, Integer.toString(temp));
				}
			}
		}

		strSumPosition = MESLotServiceProxy.getLotServiceUtil().ConvertArrayPositionToString(sumPosition);

		return strSumPosition;

	}

	// sort Position
	public ArrayList<String> sort(ArrayList<String> sumPosition)
	{
		for (int i = 0; i < sumPosition.size(); i++)
		{
			for (int j = i + 1; j < sumPosition.size(); j++)
			{
				if (Integer.parseInt(sumPosition.get(i)) > Integer.parseInt(sumPosition.get(j)))
				{
					int temp = 0;
					temp = Integer.parseInt(sumPosition.get(i));
					sumPosition.set(i, sumPosition.get(j));
					sumPosition.set(j, Integer.toString(temp));
				}
			}
		}

		return sumPosition;
	}

	// sum Position
	public ArrayList<String> removeOverlapPosition(ArrayList<String> sumPosition)
	{
		for (int i = 0; i < sumPosition.size(); i++)
		{
			if (i == sumPosition.size() - 1)
			{
				break;
			}

			for (int j = i + 1; j < sumPosition.size(); j++)
			{
				if (sumPosition.get(i).equals(sumPosition.get(j)))
				{
					sumPosition.remove(j);
					break;
				}
			}
		}

		return sumPosition;
	}

	// Check Compare Position and recievePosition
	public boolean CheckComparePosition(String posPosition, ArrayList<String> sendPosition)
	{
		String position = ConvertArrayPositionToString(sendPosition);
		if (posPosition.equals(position))
		{
			return false;
		}
		return true;
	}

	// Convert ArrayListPosition -> ToStringPosition
	public String ConvertArrayPositionToString(ArrayList<String> arrPosition)
	{
		String stringPosition = "";
		for (int i = 0; i < arrPosition.size(); i++)
		{
			stringPosition += arrPosition.get(i);
			if (i != arrPosition.size() - 1)
			{
				stringPosition += ",";
			}
		}
		return stringPosition;
	}


	// Get lotAction lastPosition By LotData.
	public String getLastPositionOfLotAction(Lot lotData, String flowName, String OperationName)
	{
		String getPositionSql = "SELECT POSITION "
				+ " FROM CT_LOTACTION "
				+ " WHERE LOTNAME = :LOTNAME "
				+ " AND FACTORYNAME = :FACTORYNAME "
				+ " AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ " AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				+ " ORDER BY POSITION DESC";

		Map<String, Object> getPositionBind = new HashMap<String, Object>();
		getPositionBind.put("LOTNAME", lotData.getKey().getLotName());
		getPositionBind.put("FACTORYNAME", lotData.getFactoryName());
		getPositionBind.put("PROCESSFLOWNAME", flowName);
		getPositionBind.put("PROCESSFLOWVERSION", "00001");
		getPositionBind.put("PROCESSOPERATIONNAME", OperationName);
		getPositionBind.put("PROCESSOPERATIONVERSION", "00001");

		List<Map<String, Object>> positionSqlBindSet = GenericServiceProxy.getSqlMesTemplate().queryForList(getPositionSql, getPositionBind);

		if(positionSqlBindSet.size() == 0)
		{
			return "0";
		}

		return positionSqlBindSet.get(0).get("POSITION").toString();
	}
	
	public String getLastSeqOfLotAction(String lotName, String reasonCode, String deparment, String eventComment)
	{
	    /* 20181208, hhlee, modify because nullValue ==>> */
	    String sqlResult = "0";
	    /* <<== 20181208, hhlee, modify because nullValue */
	    
		String getSeqSql = "SELECT SEQ "
				+ " FROM CT_LOTMULTIHOLD "
				+ " WHERE LOTNAME = :LOTNAME "
				+ " AND REASONCODE = :REASONCODE "
				+ " AND DEPARTMENT = :DEPARTMENT "
				+ " AND EVENTCOMMENT = :EVENTCOMMENT "
				+ " ORDER BY SEQ DESC";

		Map<String, Object> getSeqBind = new HashMap<String, Object>();
		getSeqBind.put("LOTNAME", lotName);
		getSeqBind.put("REASONCODE", reasonCode);
		getSeqBind.put("DEPARTMENT", deparment);
		getSeqBind.put("EVENTCOMMENT", eventComment);

		List<Map<String, Object>> seqSqlBindSet = GenericServiceProxy.getSqlMesTemplate().queryForList(getSeqSql, getSeqBind);

		/* 20181208, hhlee, modify because nullValue ==>> */
		//if(seqSqlBindSet.size() == 0)
		//{
		//	return "0";
		//}
		//return seqSqlBindSet.get(0).get("SEQ").toString();
		
		if(seqSqlBindSet != null && seqSqlBindSet.size() > 0)
        {
		    sqlResult = seqSqlBindSet.get(0).get("SEQ").toString();
        }
		
		return sqlResult;
		/* <<== 20181208, hhlee, modify because nullValue */
	}

	public List<Map<String, Object>> getSamplePositionOfLotAction(Lot lotData)
	{
		String lotActionSql = "SELECT L.SAMPLEPOSITION FROM CT_LOTACTION L "
				+ " WHERE L.LOTNAME = :LOTNAME "
				+ " AND L.FACTORYNAME = :FACTORYNAME "
				+ " AND L.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND L.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ " AND L.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ " AND L.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				//+ " AND L.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
				//+ " AND L.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION "
				+ " AND L.ACTIONNAME = :ACTIONNAME "
				+ " AND L.ACTIONSTATE = :ACTIONSTATE "
				+ " ORDER BY POSITION ";

		Map<String, Object> lotActiontBindSet = new HashMap<String, Object>();
		lotActiontBindSet.put("LOTNAME", lotData.getKey().getLotName());
		lotActiontBindSet.put("FACTORYNAME", lotData.getFactoryName());
		lotActiontBindSet.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		lotActiontBindSet.put("PROCESSFLOWVERSION", "00001");
		lotActiontBindSet.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		lotActiontBindSet.put("PROCESSOPERATIONVERSION", "00001");
		lotActiontBindSet.put("ACTIONNAME", "Sampling");
		lotActiontBindSet.put("ACTIONSTATE", "Created");
		//lotActiontBindSet.put("SAMPLEPROCESSFLOWNAME", sampleProcessFlowName);
		//lotActiontBindSet.put("SAMPLEPROCESSFLOWVERSION", "00001");

		List<Map<String, Object>> lotActionSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(lotActionSql, lotActiontBindSet);

		if(lotActionSqlResult.size()==0)
		{
			return null;
		}

		return lotActionSqlResult;
	}


	public LotAction getSamplePositionOfLotAction(Lot lotData, String sampleProcessFlowName, String moveOperationName, EventInfo eventInfo)
	{
		try
		{
			List<LotAction> sampleActionList = new ArrayList<LotAction>();

			String condition = " WHERE lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND processOperationName = ?"
					+ " AND processOperationVersion = ? AND sampleProcessFlowName = ? AND sampleProcessFlowVersion = ? AND actionName = ? AND actionState = ? ";
			Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
					moveOperationName, lotData.getProcessOperationVersion(), sampleProcessFlowName, "00001", "Sampling", "Created" };

			sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);

			if(sampleActionList.size()>0)
			{
				LotAction lotAction = new LotAction();
				lotAction = sampleActionList.get(0);

/*				lotAction.setActionState("Executed");
				lotAction.setLastEventName("Executed");
				lotAction.setLastEventTime(eventInfo.getEventTime());
				lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
				lotAction.setLastEventUser(eventInfo.getEventUser());
				lotAction.setLastEventComment(eventInfo.getEventComment());
				ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotAction);*/

				return lotAction;
			}
		}
		catch(Exception ex)
		{
			return null;
		}

		return null;
	}



	public List<Map<String, Object>> getSamplePositionOfLotActionExceptSystemSample(Lot lotData)
	{
		String lotActionSql = "SELECT SAMPLEFLOWNAME,SAMPLEFLOWVERSION FROM CT_LOTACTION L "
				+ " WHERE L.LOTNAME = :LOTNAME "
				+ " AND L.FACTORYNAME = :FACTORYNAME "
				+ " AND L.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND L.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ " AND L.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ " AND L.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				+ " AND L.ACTIONNAME = :ACTIONNAME "
				+ " AND L.ACTIONSTATE = :ACTIONSTATE "
				+ " ORDER BY POSITION ";

		Map<String, Object> lotActiontBindSet = new HashMap<String, Object>();
		lotActiontBindSet.put("LOTNAME", lotData.getKey().getLotName());
		lotActiontBindSet.put("FACTORYNAME", lotData.getFactoryName());
		lotActiontBindSet.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		lotActiontBindSet.put("PROCESSFLOWVERSION", "00001");
		lotActiontBindSet.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		lotActiontBindSet.put("PROCESSOPERATIONVERSION", "00001");
		lotActiontBindSet.put("ACTIONNAME", "Sampling");
		lotActiontBindSet.put("ACTIONSTATE", "Created");

		List<Map<String, Object>> lotActionSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(lotActionSql, lotActiontBindSet);

		if(lotActionSqlResult.size()==0)
		{
			return null;
		}

		return lotActionSqlResult;
	}

	public LotAction getSamplingInfoOfLotAction(Lot lotData)
	{
		List<LotAction> lotActionList;
		LotAction lotAction = new LotAction();

		String condition = "factoryName = ? and lotName = ? and processFlowName = ? and processFlowVersion = ? "
				+ " and processOperationName = ? and processOperationVersion = ? and actionName = ? "
				+ " and actionState = ? order by position ";
		Object[] bindSet = new Object[]{lotData.getFactoryName(), lotData.getKey().getLotName(), lotData.getProcessFlowName(), "00001",
				lotData.getProcessOperationName(), "00001", "Sampling", "Created" };
		try
		{
			lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
			lotAction = lotActionList.get(0);

			return lotAction;
		}
		catch(Throwable e)
		{
			log.info("SampleAction is not exist.");
			return null;
		}

		/*String lotActionSql = "SELECT FACTORYNAME, LOTNAME, PROCESSFLOWNAME, "
				+ " PROCESSFLOWVERSION, PROCESSOPERATIONNAME,PROCESSOPERATIONVERSION, POSITION, "
				+ " SAMPLEPROCESSFLOWNAME, SAMPLEPROCESSFLOWVERSION, SAMPLEPOSITION, SAMPLEOUTHOLDFLAG "
				+ " FROM CT_LOTACTION "
				+ " WHERE FACTORYNAME = :FACTORYNAME "
				+ " AND LOTNAME = :LOTNAME "
				+ " AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ " AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				+ " AND L.ACTIONNAME = :ACTIONNAME "
				+ " AND L.ACTIONSTATE = :ACTIONSTATE "
				+ " ORDER BY POSITION ";

		Map<String, Object> lotActiontBindSet = new HashMap<String, Object>();
		lotActiontBindSet.put("FACTORYNAME", lotData.getFactoryName());
		lotActiontBindSet.put("LOTNAME", lotData.getKey().getLotName());
		lotActiontBindSet.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		lotActiontBindSet.put("PROCESSFLOWVERSION", "00001");
		lotActiontBindSet.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName() );
		lotActiontBindSet.put("PROCESSOPERATIONVERSION", "00001");
		lotActiontBindSet.put("ACTIONNAME", "Sampling");
		lotActiontBindSet.put("ACTIONSTATE", "Created");

		List<Map<String, Object>> lotActionSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(lotActionSql, lotActiontBindSet);

		if(lotActionSqlResult.size()==0)
		{
			return null;
		}

		return lotActionSqlResult;*/
	}

	public String returnFlowType(String factoryName, String processFlowName, String processFlowVersion)
	{
		String flowTypeSql = "SELECT PROCESSFLOWTYPE "
				+ " FROM PROCESSFLOW "
				+ " WHERE FACTORYNAME = :FACTORYNAME "
				+ " AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ";

		Map<String, Object> flowTypeBindSet = new HashMap<String, Object>();
		flowTypeBindSet.put("FACTORYNAME", factoryName);
		flowTypeBindSet.put("PROCESSFLOWNAME", processFlowName);
		flowTypeBindSet.put("PROCESSFLOWVERSION", processFlowVersion);

		List<Map<String, Object>> flowTypeSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(flowTypeSql, flowTypeBindSet);

		if(flowTypeSqlResult.size()>0)
		{
			String flowType = flowTypeSqlResult.get(0).get("PROCESSFLOWTYPE").toString();
			return flowType;
		}

		return "";
	}

	public Lot endReworkState(EventInfo eventInfo, Lot LotData) throws CustomException
	{
		/*ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(preLotData);

		if(StringUtil.equals(processFlowData.getProcessFlowType(), "Rework"))
		{
			if (CommonUtil.isLotInEndOperation(preLotData, preLotData.getFactoryName(), preLotData.getProcessFlowName()))
			{
				//case#1 : rework
				if(StringUtil.equals(preLotData.getReworkState(), GenericServiceProxy.getConstantMap().Lot_InRework))
				{
					postLotData = NewCompleteRework(eventInfo, preLotData,postLotData);
				}
			}
		}
			return postLotData;*/

		if(StringUtil.equals(LotData.getReworkState(), GenericServiceProxy.getConstantMap().Lot_InRework))
		{
			LotData = NewCompleteRework(eventInfo, LotData);
		}

		return LotData;
	}


	/**
	 * 150205 by swcho : done refactoring
	 * complete rework
	 * @param eventInfo
	 * @param beforeTrackOutLot
	 * @param afterTrackOutLot
	 * @return
	 * @throws CustomException
	 */
	public Lot NewCompleteRework(EventInfo eventInfo, Lot LotData) throws CustomException
	{
		if(StringUtil.equals(LotData.getReworkState(), "InRework"))
		{
			if(!StringUtils.equals(LotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_S)){
				LotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_G);
				LotServiceProxy.getLotService().update(LotData);
			}
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			List<Product> ProductList = LotServiceProxy.getLotService().allUnScrappedProducts(LotData.getKey().getLotName());
			List<Product> ProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(LotData.getKey().getLotName());
			
			for (Product product : ProductList)
			{
				if(!StringUtils.equals(product.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_S)){
					product.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
					ProductServiceProxy.getProductService().update(product);
				}
			}

			eventInfo.setEventName("CompleteRework");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			List<ProductU> productU = MESLotServiceProxy.getLotServiceUtil().setProductUSequence(LotData.getKey().getLotName());
			
			//check reworkstate
			boolean inReworkState = false;
			inReworkState =checkReworkState(LotData);
			
			if(!inReworkState){
				MakeNotInReworkInfo makeNotInReworkInfo = MESLotServiceProxy.getLotInfoUtil().makeNotInReworkInfoForTO(
						LotData, eventInfo, LotData.getKey().getLotName(),
						LotData.getProcessFlowName(), LotData.getProcessOperationName(),
						LotData.getUdfs(), productU);
				 return MESLotServiceProxy.getLotServiceImpl().completeRework(eventInfo, LotData, makeNotInReworkInfo);
			}else{
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.setProductQuantity(LotData.getProductQuantity());
				setEventInfo.setProductUSequence(productU);
				return LotServiceProxy.getLotService().setEvent(LotData.getKey(), eventInfo, setEventInfo);
			}
		}
		else
		{
			return LotData;
		}
	}

	// Modified by smkang on 2018.07.02 - According to EDO's request, ReservedSampling and POSSampling are necessary to be distinguished.
	//									  checkReservedSamplingInfo and checkNormalSamplingInfo are added instead of checkSampleReserveInfo.
//	public String checkSampleReserveInfo(String lotName, String processFlowName, String processFlowVersion, String processOperationName, EventInfo eventInfo) throws CustomException
//	{
//		String sampleFlowName = "";
//
//		String sampleLotSql = "SELECT SAMPLEPROCESSFLOWNAME, POSITION, TYPE "
//				+ " FROM (SELECT LA.SAMPLEPROCESSFLOWNAME, LA.POSITION, 'RESERVE' TYPE "
//				+ "         FROM CT_LOTACTION LA "
//				+ "        WHERE 1 = 1 "
//				+ "          AND LA.LOTNAME = :LOTNAME "
//				+ "          AND LA.PROCESSFLOWNAME = :PROCESSFLOWNAME "
//				+ "          AND LA.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
//				+ "          AND LA.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
//				+ "          AND LA.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
//				+ "          AND LA.ACTIONSTATE = :ACTIONSTATE "
//				+ "          AND LA.ACTIONNAME = :ACTIONNAME "
//				+ "       UNION "
//				+ "       SELECT SL.SAMPLEPROCESSFLOWNAME, SLC.SAMPLEPRIORITY, 'AUTO' TYPE "
//				+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_SAMPLELOT SL "
//				+ "        WHERE 1 = 1 "
//				+ "          AND SLC.FACTORYNAME = SL.FACTORYNAME "
//				+ "          AND SLC.PRODUCTSPECNAME = SL.PRODUCTSPECNAME "
//				+ "          AND SLC.ECCODE = SL.ECCODE "
//				+ "          AND SLC.PROCESSFLOWNAME = SL.PROCESSFLOWNAME "
//				+ "          AND SLC.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION "
//				+ "          AND SLC.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME "
//				+ "          AND SLC.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION "
//				+ "          AND SLC.FROMPROCESSOPERATIONNAME = SL.FROMPROCESSOPERATIONNAME "
//				+ "          AND SL.LOTNAME = :LOTNAME "
//				+ "          AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
//				+ "          AND SL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION "
//				+ "          AND SL.SAMPLEFLAG = :SAMPLEFLAG "
//				+ "          AND SL.SAMPLESTATE = :SAMPLESTATE "
//				+ "       UNION "
//				+ "       SELECT CSL.SAMPLEPROCESSFLOWNAME, SLC.SAMPLEPRIORITY, 'CORRES' TYPE "
//				+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_CORRESSAMPLELOT CSL "
//				+ "        WHERE 1 = 1 "
//				+ "          AND SLC.FACTORYNAME = CSL.FACTORYNAME "
//				+ "          AND SLC.PRODUCTSPECNAME = CSL.PRODUCTSPECNAME "
//				+ "          AND SLC.ECCODE = CSL.ECCODE "
//				+ "          AND SLC.PROCESSFLOWNAME = CSL.PROCESSFLOWNAME "
//				+ "          AND SLC.PROCESSFLOWVERSION = CSL.PROCESSFLOWVERSION "
//				+ "          AND SLC.CORRESSAMPLEPROCESSFLOWNAME = CSL.SAMPLEPROCESSFLOWNAME "
//				+ "          AND SLC.CORRESSAMPLEPROCESSFLOWVERSION = CSL.SAMPLEPROCESSFLOWVERSION "
//				+ "          AND SLC.CORRESPROCESSOPERATIONNAME = CSL.FROMPROCESSOPERATIONNAME "
//				+ "          AND SLC.CORRESPROCESSOPERATIONVERSION = CSL.FROMPROCESSOPERATIONVERSION "
//				+ "          AND CSL.LOTNAME = :LOTNAME "
//				+ "          AND CSL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
//				+ "          AND CSL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION "
//				+ "          AND CSL.SAMPLEFLAG = :SAMPLEFLAG "
//				+ "          AND CSL.SAMPLESTATE = :SAMPLESTATE) "
//				+ "  ORDER BY DECODE(TYPE, 'RESERVE', 0, 'AUTO', 1,2), POSITION ASC ";
//
//		Map<String, Object> smapleLotBindSet = new HashMap<String, Object>();
//		smapleLotBindSet.put("LOTNAME", lotName);
//		smapleLotBindSet.put("PROCESSFLOWNAME", processFlowName);
//		smapleLotBindSet.put("PROCESSFLOWVERSION", processFlowVersion);
//		smapleLotBindSet.put("PROCESSOPERATIONNAME", processOperationName);
//		smapleLotBindSet.put("PROCESSOPERATIONVERSION", "00001");
//		smapleLotBindSet.put("ACTIONSTATE", "Created");
//		smapleLotBindSet.put("ACTIONNAME", "Sampling");
//		smapleLotBindSet.put("FROMPROCESSOPERATIONNAME", processOperationName);
//		smapleLotBindSet.put("FROMPROCESSOPERATIONVERSION", "00001");
//		smapleLotBindSet.put("SAMPLEFLAG", "Y");
//		smapleLotBindSet.put("SAMPLESTATE", "Decided");
//
//		List<Map<String, Object>> sampleLotSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sampleLotSql, smapleLotBindSet);
//
//		if ( sampleLotSqlResult.size() > 0 )
//		{
//			List<LotAction> sampleActionList = new ArrayList<LotAction>();
//			sampleFlowName = sampleLotSqlResult.get(0).get("SAMPLEPROCESSFLOWNAME").toString();
//			String position = sampleLotSqlResult.get(0).get("POSITION").toString();
//			String type = sampleLotSqlResult.get(0).get("TYPE").toString();
//
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//
//			/*if(StringUtil.equals(type, "RESERVE"))
//			{
//				String condition = " WHERE 1=1 AND lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND processOperationName = ?"
//						+ " AND processOperationVersion = ? AND position = ? AND sampleProcessFlowName = ? AND sampleProcessFlowVersion = ? AND actionName = ? AND actionState = ? ";
//				Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), processFlowName, processFlowVersion,
//						processOperationName, "00001", Integer.parseInt(position), sampleFlowName, "00001", "Sampling", "Created" };
//
//				try
//				{
//					eventInfo.setEventName("Execute SampleLot");
//					sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
//
//					LotAction lotAction = new LotAction();
//					lotAction = sampleActionList.get(0);
//
//					lotAction.setActionState("Executed");
//					lotAction.setLastEventTime(eventInfo.getEventTime());
//					lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
//					lotAction.setLastEventUser(eventInfo.getEventUser());
//					lotAction.setLastEventComment(eventInfo.getEventComment());
//					ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotAction);
//				}
//				catch(Exception ex)
//				{
//					throw new CustomException("SAMPLE-0003",lotName, processFlowName, processOperationName, sampleFlowName);
//				}
//			}
//			else if(StringUtil.equals(type, "AUTO"))
//			{
//				List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
//				SampleLot sampleLot = new SampleLot();
//
//				try
//				{
//					sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? and processOperationName = ? and processOperationVersion = ? "
//							+ "and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? and sampleState = ? and sampleFlag = ? ", new Object[] {lotData.getKey().getLotName(),
//									lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), processFlowName,
//									processFlowVersion ,processOperationName, "00001",
//									sampleFlowName, "00001", "Decided" , "Y" });
//
//					if(sampleLotList.size()>1)
//					{
//						sampleLot = sampleLotList.get(0);
//
//						sampleLot.setSampleState("Executing");
//						sampleLot.setLastEventTime(eventInfo.getEventTime());
//						sampleLot.setLastEventTimekey(eventInfo.getEventTimeKey());
//						sampleLot.setLastEventUser(eventInfo.getEventUser());
//						sampleLot.setLastEventComment(eventInfo.getEventComment());
//						sampleLot = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLot);
//					}
//
//				}
//				catch ( Throwable e )
//				{
//					throw new CustomException("SAMPLE-0003",lotName, processFlowName, processOperationName, sampleFlowName);
//				}
//			}*/
//
//			return sampleFlowName;
//		}
//
//		return "";
//	}	
	public String checkReservedSamplingInfo(String lotName, String processFlowName, String processFlowVersion, String processOperationName, EventInfo eventInfo) throws CustomException
	{
		log.info("Check Reserved Sampling Info");
		
		String sampleLotSql = "SELECT LA.SAMPLEPROCESSFLOWNAME, LA.POSITION, 'RESERVE' TYPE "
							+ "  FROM CT_LOTACTION LA "
							+ " WHERE 1 = 1 "
							+ "   AND LA.LOTNAME = :LOTNAME "
							+ "   AND LA.PROCESSFLOWNAME = :PROCESSFLOWNAME "
							+ "   AND LA.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
							+ "   AND LA.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
							+ "   AND LA.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
							+ "   AND LA.ACTIONSTATE = :ACTIONSTATE "
							+ "   AND LA.ACTIONNAME = :ACTIONNAME "
							+ " ORDER BY DECODE(TYPE, :TYPE1, 0, :TYPE2, 1, 2), POSITION ASC ";

		Map<String, Object> smapleLotBindSet = new HashMap<String, Object>();
		smapleLotBindSet.put("LOTNAME", lotName);
		smapleLotBindSet.put("PROCESSFLOWNAME", processFlowName);
		smapleLotBindSet.put("PROCESSFLOWVERSION", processFlowVersion);
		smapleLotBindSet.put("PROCESSOPERATIONNAME", processOperationName);
		smapleLotBindSet.put("PROCESSOPERATIONVERSION", "00001");
		smapleLotBindSet.put("ACTIONSTATE", "Created");
		smapleLotBindSet.put("ACTIONNAME", "Sampling");
		smapleLotBindSet.put("TYPE1", "RESERVE");
		smapleLotBindSet.put("TYPE2", "AUTO");

		List<Map<String, Object>> sampleLotSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sampleLotSql, smapleLotBindSet);

		if ( sampleLotSqlResult.size() > 0 )
			return sampleLotSqlResult.get(0).get("SAMPLEPROCESSFLOWNAME").toString();
		else
			return "";
	}
	
	public String checkNormalSamplingInfo(String lotName, String processOperationName, EventInfo eventInfo) throws CustomException
	{
		log.info("Check Normal Sampling Info");
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		String sampleLotSql = "SELECT SAMPLEPROCESSFLOWNAME, SAMPLEPRIORITY, TYPE "
				+ " FROM (SELECT SL.SAMPLEPROCESSFLOWNAME, SLC.SAMPLEPRIORITY, :TYPE1 TYPE "
				+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_SAMPLELOT SL "
				+ "        WHERE 1 = 1 "
				+ "          AND SLC.FACTORYNAME = SL.FACTORYNAME "
				+ "          AND SLC.PRODUCTSPECNAME = SL.PRODUCTSPECNAME "
				+ "          AND SLC.ECCODE = SL.ECCODE "
				+ "          AND SLC.PROCESSFLOWNAME = SL.PROCESSFLOWNAME "
				+ "          AND SLC.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION "
				+ "          AND SLC.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME "
				+ "          AND SLC.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION "
				+ "          AND SLC.FROMPROCESSOPERATIONNAME = SL.FROMPROCESSOPERATIONNAME "
				+ "          AND SL.LOTNAME = :LOTNAME "
				//***** 2019.05.16_hsryu_Insert Logic. Missed Logic.*******/
				+ "          AND SL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ "          AND SL.ECCODE = :ECCODE "
				+ "          AND SL.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				//********************************************************//
				+ "          AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
				+ "          AND (SL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION OR SL.FROMPROCESSOPERATIONVERSION = :STAR) "
				+ "          AND SL.SAMPLEFLAG = :SAMPLEFLAG "
				+ "          AND SL.SAMPLESTATE = :SAMPLESTATE "
				+ "       UNION "
				+ "       SELECT CSL.SAMPLEPROCESSFLOWNAME, SLC.CORRESSAMPLEPRIORITY SAMPLEPRIORITY , :TYPE2 TYPE "
				+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_CORRESSAMPLELOT CSL "
				+ "        WHERE 1 = 1 "
				+ "          AND SLC.FACTORYNAME = CSL.FACTORYNAME "
				+ "          AND SLC.PRODUCTSPECNAME = CSL.PRODUCTSPECNAME "
				+ "          AND SLC.ECCODE = CSL.ECCODE "
				+ "          AND SLC.PROCESSFLOWNAME = CSL.PROCESSFLOWNAME "
				+ "          AND SLC.PROCESSFLOWVERSION = CSL.PROCESSFLOWVERSION "
				+ "          AND SLC.CORRESSAMPLEPROCESSFLOWNAME = CSL.SAMPLEPROCESSFLOWNAME "
				+ "          AND SLC.CORRESSAMPLEPROCESSFLOWVERSION = CSL.SAMPLEPROCESSFLOWVERSION "
				+ "          AND SLC.CORRESPROCESSOPERATIONNAME = CSL.FROMPROCESSOPERATIONNAME "
				+ "          AND SLC.CORRESPROCESSOPERATIONVERSION = CSL.FROMPROCESSOPERATIONVERSION "
				+ "          AND CSL.LOTNAME = :LOTNAME "
				//***** 2019.05.16_hsryu_Insert Logic. Missed Logic.*******/
				+ "          AND CSL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ "          AND CSL.ECCODE = :ECCODE "
				+ "          AND CSL.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				//********************************************************//
				+ "          AND CSL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
				+ "          AND (CSL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION OR CSL.FROMPROCESSOPERATIONVERSION = :STAR) "
				+ "          AND CSL.SAMPLEFLAG = :SAMPLEFLAG "
				+ "          AND CSL.SAMPLESTATE = :SAMPLESTATE) "
				+ "  ORDER BY DECODE(TYPE, :TYPE1, 0, :TYPE2, 1, 2), SAMPLEPRIORITY ASC ";

		Map<String, Object> smapleLotBindSet = new HashMap<String, Object>();
		smapleLotBindSet.put("LOTNAME", lotName);
		smapleLotBindSet.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		smapleLotBindSet.put("ECCODE", lotData.getUdfs().get("ECCODE"));
		smapleLotBindSet.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		smapleLotBindSet.put("FROMPROCESSOPERATIONNAME", processOperationName);
		smapleLotBindSet.put("FROMPROCESSOPERATIONVERSION", "00001");
		smapleLotBindSet.put("SAMPLEFLAG", "Y");
		smapleLotBindSet.put("SAMPLESTATE", "Decided");
		smapleLotBindSet.put("STAR", "*");
		smapleLotBindSet.put("TYPE1", "AUTO");
		smapleLotBindSet.put("TYPE2", "CORRES");

		List<Map<String, Object>> sampleLotSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sampleLotSql, smapleLotBindSet);

		if ( sampleLotSqlResult.size() > 0 )
			return sampleLotSqlResult.get(0).get("SAMPLEPROCESSFLOWNAME").toString();
		else
			return "";
	}

    public void anotherLotDeassignCarrier(EventInfo eventInfo, String carrierName, String lotName) throws CustomException
    {
        List<String> lotList = MESLotServiceProxy.getLotServiceUtil().getLotNamesByCarrierName(carrierName);
        if (lotList.size() > 1)
        {
            for (int i = 0; i < lotList.size(); i++)
            {
                if (lotList.get(i).equals(lotName))
                {
                    Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
                    Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
                    eventInfo.setEventName("DeassginCarrier");
                    
                    // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                    List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);
                    List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotName);

                    List<ProductU> productUSequence = MESProductServiceProxy.getProductServiceUtil().makeProductUSequence(productList);

                    DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, productUSequence);

                    MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
                }
            }
        }
    }

    public List<String> getLotNamesByCarrierName(String carrierName) throws FrameworkErrorSignal, NotFoundSignal
    {
        if (log.isInfoEnabled())
        {
            log.info("CARRIERNAME = " + carrierName);
        }

        List<String> lotNames = new ArrayList<String>();

        if (carrierName != null && !carrierName.equals(""))
        {
            String condition = "WHERE carrierName = ? ";

            Object[] bindSet = new Object[] { carrierName };

            try
            {
                List<Lot> arrayList = LotServiceProxy.getLotService().select(condition, bindSet);
                for (int i = 0; i < arrayList.size(); i++)
                {
                    lotNames.add(arrayList.get(i).getKey().getLotName());
                }
            }
            catch (Exception e)
            {
                log.error(e);
                lotNames = null;
            }
        }
        else
        {
        }

        return lotNames;
    }

    //add by wghuang 20180527
	public List<Lot> getLotDataByCarrierName(String carrierName,boolean flag) throws CustomException
	{
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		String condition = "WHERE carrierName = ?";
		String condition = "WHERE CARRIERNAME = ? FOR UPDATE";

		Object[] bindSet = new Object[] {carrierName};

		List<Lot> lotList;

		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch(NotFoundSignal ne)
		{
			if(flag == true)
				throw new CustomException("LOT-0048", "");
			//empty CST
			lotList = new ArrayList<Lot>();

			return lotList;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "Lot", "Lot binding CST incorrect");
		}

		return lotList;
	}

	public List<Lot> getLotListByProductList(List<Element> productList) throws CustomException
    {
        List<String> lotNameList = new ArrayList<String>();

        for (Element productElement : productList)
        {
            String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
            // String lotName = SMessageUtil.getChildText(productElement, "LOTNAME", true);
            Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

            if (!lotNameList.contains(productData.getLotName()))
            {
                lotNameList.add(productData.getLotName());
            }
        }

        List<Lot> lotList = new ArrayList<Lot>();

        for (String lotName : lotNameList)
        {
            Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

            lotList.add(lotData);
        }

        return lotList;
    }

	//add by wghuang 20180527
	public List<String> getLotNameByProductList(List<Element> productList) throws CustomException
    {
        List<String> lotNameList = new ArrayList<String>();

        for (Element productElement : productList)
        {
            String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
            // String lotName = SMessageUtil.getChildText(productElement, "LOTNAME", true);
            Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

            if (!lotNameList.contains(productData.getLotName()))
            {
                lotNameList.add(productData.getLotName());
            }
        }

        return lotNameList;
    }

	//add by wghuang 20180527
	public List<String> getLotNameByProductList(List<String> lotnameList,String lotname) throws CustomException
    {
		List<String>lotnameListNew = new ArrayList<String>();

        for (String eleLot : lotnameList)
        {
        	if(eleLot != lotname)
        		lotnameListNew.add(eleLot);
        }

        return lotnameListNew;
    }
	
	public boolean checkForceSampling(Lot lotData, String currentNodeStack, String beforeNodeStack, EventInfo eventInfo) throws CustomException
	{
		boolean checkForceSampling = false;
		
		Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(currentNodeStack);

		String flowName = flowMap.get("PROCESSFLOWNAME");
		String flowVersion = flowMap.get("PROCESSFLOWVERSION");
		String operationName = flowMap.get("PROCESSOPERATIONNAME");

		Map<String, String> beforeFlowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(beforeNodeStack);

		String beforeFlowName = beforeFlowMap.get("PROCESSFLOWNAME");
		String beforeFlowVersion = beforeFlowMap.get("PROCESSFLOWVERSION");
		String beforeOperationName = beforeFlowMap.get("PROCESSOPERATIONNAME");
		
		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		SampleLot sampleLot = new SampleLot();
		
		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and (processFlowVersion = ? or processFlowVersion = ?) "
					+ "and fromProcessOperationName = ? and (fromProcessOperationVersion = ? or fromProcessOperationVersion = ?) and sampleProcessFlowName = ? and (sampleProcessFlowVersion = ? or sampleProcessFlowVersion = ?) ",
					new Object[] {lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), beforeFlowName, beforeFlowVersion, "*",
					beforeOperationName, "00001", "*", flowName, flowVersion, "*"});

			
			if(sampleLotList.size()>0)
			{
				sampleLot = sampleLotList.get(0);

				if(StringUtil.isNotEmpty(sampleLot.getManualSampleFlag())&&StringUtil.equals(sampleLot.getManualSampleFlag().toUpperCase(), "FORCESAMPLING"))
				{
					return true;
				}
				
				return false;
			}
		}
		catch(Throwable e)
		{
			log.info("not ForceSampling..");
		}
		
		return false;

	}

	public String checkHoldOutFlag(Lot lotData, String currentNodeStack, String beforeNodeStack, EventInfo eventInfo) throws CustomException
	{
		log.info("Check SampleOutHoldFlag!");
		String sampleOutHoldFlag = "";
		String currentFlowSql = "SELECT N.NODEID, N.NODEATTRIBUTE1, N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION "
				+ " FROM NODE N "
				+ " WHERE N.NODEID = :NODEID ";

		Map<String, Object> currentFlowBindSet = new HashMap<String, Object>();
		currentFlowBindSet.put("NODEID", currentNodeStack);

		List<Map<String, Object>> currentFlowSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(currentFlowSql, currentFlowBindSet);

		String beforeFlowSql = "SELECT N.NODEID, N.NODEATTRIBUTE1, N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION "
				+ " FROM NODE N "
				+ " WHERE N.NODEID = :NODEID ";

		Map<String, Object> beforeFlowBindSet = new HashMap<String, Object>();
		beforeFlowBindSet.put("NODEID", beforeNodeStack);

		List<Map<String, Object>> beforeFlowSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(beforeFlowSql, beforeFlowBindSet);

		if ( currentFlowSqlResult.size() > 0 && beforeFlowSqlResult.size() > 0)
		{
			String currentProcessFlowName = (String) currentFlowSqlResult.get(0).get("PROCESSFLOWNAME");
			String currentProcessFlowVersion = (String) currentFlowSqlResult.get(0).get("PROCESSFLOWVERSION");
			String currentProcessOperationName = (String) currentFlowSqlResult.get(0).get("NODEATTRIBUTE1");

			String beforeProcessFlowName = (String) beforeFlowSqlResult.get(0).get("PROCESSFLOWNAME");
			String beforeProcessFlowVersion = (String) beforeFlowSqlResult.get(0).get("PROCESSFLOWVERSION");
			String beforeProcessOperationName = (String) beforeFlowSqlResult.get(0).get("NODEATTRIBUTE1");

			String sampleLotSql = "SELECT SAMPLEOUTHOLDFLAG, POSITION, LASTEVENTUSER, DEPARTMENTNAME, TYPE "
					+ " FROM (SELECT LA.SAMPLEOUTHOLDFLAG, LA.POSITION, LA.LASTEVENTUSER, LA.DEPARTMENT DEPARTMENTNAME, 'RESERVE' TYPE "
					+ "         FROM CT_LOTACTION LA "
					+ "        WHERE 1 = 1 "
					+ "          AND LA.LOTNAME = :LOTNAME "
					+ "          AND LA.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "          AND LA.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
					+ "          AND LA.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
					+ "          AND LA.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
					+ "          AND LA.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
					+ "          AND LA.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION "
					+ "          AND LA.ACTIONSTATE = :ACTIONSTATE "
					+ "          AND LA.ACTIONNAME = :ACTIONNAME "
					+ "       UNION "
					+ "       SELECT SL.SAMPLEOUTHOLDFLAG, SLC.SAMPLEPRIORITY AS POSITION, SL.LASTEVENTUSER, HOLDDEPARTMENTNAME DEPARTMENTNAME, 'AUTO' TYPE "
					+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_SAMPLELOT SL "
					+ "        WHERE 1 = 1 "
					+ "          AND SLC.FACTORYNAME = SL.FACTORYNAME "
					+ "          AND SLC.PRODUCTSPECNAME = SL.PRODUCTSPECNAME "
					+ "          AND SLC.ECCODE = SL.ECCODE "
					+ "          AND SLC.PROCESSFLOWNAME = SL.PROCESSFLOWNAME "
					+ "          AND SLC.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION "
					+ "          AND SLC.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME "
					+ "          AND SLC.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION "
					+ "          AND SLC.PROCESSOPERATIONNAME = SL.PROCESSOPERATIONNAME "
					+ "          AND SLC.PROCESSOPERATIONVERSION = SL.PROCESSOPERATIONVERSION "
					+ "          AND SL.LOTNAME = :LOTNAME "
					/**************** 2019.03.13_hsryu_Insert Logic. ****************/
					+ "          AND SL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
					+ "          AND SL.ECCODE = :ECCODE "
					/****************************************************************/
					+ "          AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
					+ "          AND SL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION "
					+ "          AND SL.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
					+ "          AND SL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION "
					+ "          AND SL.SAMPLEFLAG = :SAMPLEFLAG "
					+ "          AND SL.SAMPLESTATE = :SAMPLESTATE "
					//------------------------------------------------------------------------------------------------
					// Added by smkang on 2018.10.20 - Force sampling situation is missed.
					+ "       UNION "
					+ "       SELECT SL.SAMPLEOUTHOLDFLAG, 1 AS POSITION, SL.LASTEVENTUSER, HOLDDEPARTMENTNAME DEPARTMENTNAME, 'MANUAL' TYPE "
					+ "         FROM CT_SAMPLELOT SL "
					+ "        WHERE 1 = 1 "
					+ "          AND SL.LOTNAME = :LOTNAME "
					/**************** 2019.03.13_hsryu_Insert Logic. ****************/
					+ "          AND SL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
					+ "          AND SL.ECCODE = :ECCODE "
					/****************************************************************/
					+ "          AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
					+ "          AND SL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION "
					+ "          AND SL.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
					+ "          AND SL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION "
					+ "          AND SL.SAMPLEFLAG = :SAMPLEFLAG "
					+ "          AND SL.SAMPLESTATE = :SAMPLESTATE "
					+ "          AND SL.MANUALSAMPLEFLAG = :MANUALSAMPLEFLAG "
					//------------------------------------------------------------------------------------------------
					+ "       UNION "
					+ "       SELECT CSL.SAMPLEOUTHOLDFLAG, SLC.CORRESSAMPLEPRIORITY AS POSITION, CSL.LASTEVENTUSER, '' DEPARTMENTNAME, 'CORRES' TYPE "
					+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_CORRESSAMPLELOT CSL "
					+ "        WHERE 1 = 1 "
					+ "          AND SLC.FACTORYNAME = CSL.FACTORYNAME "
					+ "          AND SLC.PRODUCTSPECNAME = CSL.PRODUCTSPECNAME "
					+ "          AND SLC.ECCODE = CSL.ECCODE "
					+ "          AND SLC.PROCESSFLOWNAME = CSL.PROCESSFLOWNAME "
					+ "          AND SLC.PROCESSFLOWVERSION = CSL.PROCESSFLOWVERSION "
					+ "          AND SLC.PROCESSOPERATIONNAME = CSL.PROCESSOPERATIONNAME "
					+ "          AND SLC.PROCESSOPERATIONVERSION = CSL.PROCESSOPERATIONVERSION "
					+ "          AND SLC.CORRESSAMPLEPROCESSFLOWNAME = CSL.SAMPLEPROCESSFLOWNAME "
					+ "          AND SLC.CORRESSAMPLEPROCESSFLOWVERSION = CSL.SAMPLEPROCESSFLOWVERSION "
					+ "          AND SLC.CORRESPROCESSOPERATIONNAME = CSL.FROMPROCESSOPERATIONNAME "
					+ "          AND SLC.CORRESPROCESSOPERATIONVERSION = CSL.FROMPROCESSOPERATIONVERSION "
					+ "          AND CSL.LOTNAME = :LOTNAME "
					/**************** 2019.03.13_hsryu_Insert Logic. ****************/
					+ "          AND CSL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
					+ "          AND CSL.ECCODE = :ECCODE "
					/****************************************************************/
					+ "          AND CSL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
					+ "          AND CSL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION "
					+ "          AND CSL.SAMPLEFLAG = :SAMPLEFLAG "
					+ "          AND CSL.SAMPLESTATE = :SAMPLESTATE) "
					+ "  ORDER BY DECODE(TYPE, 'RESERVE', 0, 'AUTO', 1,2), POSITION ASC ";

			Map<String, Object> smapleLotBindSet = new HashMap<String, Object>();
			smapleLotBindSet.put("LOTNAME", lotData.getKey().getLotName());
			smapleLotBindSet.put("PRODUCTSPECNAME", lotData.getProductSpecName());
			smapleLotBindSet.put("ECCODE", lotData.getUdfs().get("ECCODE"));
			smapleLotBindSet.put("PROCESSFLOWNAME", beforeProcessFlowName);
			smapleLotBindSet.put("PROCESSFLOWVERSION", beforeProcessFlowVersion);
			smapleLotBindSet.put("PROCESSOPERATIONNAME", beforeProcessOperationName);
			smapleLotBindSet.put("PROCESSOPERATIONVERSION", "00001");
			smapleLotBindSet.put("SAMPLEPROCESSFLOWNAME", currentProcessFlowName);
			smapleLotBindSet.put("SAMPLEPROCESSFLOWVERSION", currentProcessFlowVersion);
			smapleLotBindSet.put("ACTIONSTATE", "Executed");
			smapleLotBindSet.put("ACTIONNAME", "Sampling");
			smapleLotBindSet.put("FROMPROCESSOPERATIONNAME", beforeProcessOperationName);
			smapleLotBindSet.put("FROMPROCESSOPERATIONVERSION", "00001");
			smapleLotBindSet.put("SAMPLEFLAG", "Y");
			smapleLotBindSet.put("SAMPLESTATE", "Executing");
			
			// Added by smkang on 2018.10.20 - Force sampling situation is missed.
			smapleLotBindSet.put("MANUALSAMPLEFLAG", "ForceSampling");

			List<Map<String, Object>> sampleLotSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sampleLotSql, smapleLotBindSet);
/*
			if ( sampleLotSqlResult.size() > 0 )
			{
				sampleOutHoldFlag = (String)sampleLotSqlResult.get(0).get("SAMPLEOUTHOLDFLAG");

				if(StringUtil.equals(sampleOutHoldFlag, "Y"))
				{
					EventInfo eventInfoForSampleOutHoldFlag = EventInfoUtil.makeEventInfo(
							"Hold",
							//2019.02.27_hsryu_Insert Logic. 
							StringUtils.isNotEmpty((String)sampleLotSqlResult.get(0).get("LASTEVENTUSER"))?(String)sampleLotSqlResult.get(0).get("LASTEVENTUSER"):eventInfo.getEventUser()
							,
							"Hold by SampleOutHoldFlag", 
							"HoldLot", 
							"HD-0001");
					
					eventInfoForSampleOutHoldFlag.setEventTime(eventInfo.getEventTime());
					eventInfoForSampleOutHoldFlag.setEventTimeKey(eventInfo.getEventTimeKey());
					
					//2019.03.05_hsryu_Mantis 0002944.
					String departmentName = " ";
					if(StringUtils.equals((String)sampleLotSqlResult.get(0).get("TYPE"), "RESERVE")){
						departmentName = StringUtils.isNotEmpty((String)sampleLotSqlResult.get(0).get("DEPARTMENTNAME"))?(String)sampleLotSqlResult.get(0).get("DEPARTMENTNAME"):" ";
					}
					else if(StringUtils.equals("MANUAL", (String)sampleLotSqlResult.get(0).get("TYPE"))){
						departmentName = StringUtils.isNotEmpty((String)sampleLotSqlResult.get(0).get("DEPARTMENTNAME"))?(String)sampleLotSqlResult.get(0).get("DEPARTMENTNAME"):" ";
					}
					else if(StringUtils.equals("AUTO", (String)sampleLotSqlResult.get(0).get("TYPE"))){
						departmentName = StringUtils.isNotEmpty((String)sampleLotSqlResult.get(0).get("DEPARTMENTNAME"))?(String)sampleLotSqlResult.get(0).get("DEPARTMENTNAME"):" ";
					}

					//2019.02.27_hsryu_Remove Logic. change located Logic. For EventUser.
					//EventInfo sampleOutHoldEventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), "Hold by SampleOutHoldFlag", "HoldLot", "HD-0001");
					MESLotServiceProxy.getLotServiceUtil().reserveAHold(lotData, departmentName, eventInfoForSampleOutHoldFlag);
				}					
				return "";
			}
			*/
			// add by GJJ 20200408 mantis£º5851 sampleOutHoldÐèÒªÈ¡²¢ start
			
			try {
				for (Iterator iterator = sampleLotSqlResult.iterator(); iterator
						.hasNext();) {
					Map<String, Object> sampleLotMap = (Map<String, Object>) iterator.next();
					
					sampleOutHoldFlag = (String)sampleLotMap.get("SAMPLEOUTHOLDFLAG");

					if(StringUtil.equals(sampleOutHoldFlag, "Y"))
					{
						EventInfo eventInfoForSampleOutHoldFlag = EventInfoUtil.makeEventInfo(
								"Hold",
								//2019.02.27_hsryu_Insert Logic. 
								StringUtils.isNotEmpty((String)sampleLotMap.get("LASTEVENTUSER"))?(String)sampleLotMap.get("LASTEVENTUSER"):eventInfo.getEventUser()
								,
								"Hold by SampleOutHoldFlag", 
								"HoldLot", 
								"HD-0001");
						
						eventInfoForSampleOutHoldFlag.setEventTime(eventInfo.getEventTime());
						eventInfoForSampleOutHoldFlag.setEventTimeKey(eventInfo.getEventTimeKey());
						
						//2019.03.05_hsryu_Mantis 0002944.
						String departmentName = " ";
						if(StringUtils.equals((String)sampleLotMap.get("TYPE"), "RESERVE")){
							departmentName = StringUtils.isNotEmpty((String)sampleLotMap.get("DEPARTMENTNAME"))?(String)sampleLotMap.get("DEPARTMENTNAME"):" ";
						}
						else if(StringUtils.equals("MANUAL", (String)sampleLotMap.get("TYPE"))){
							departmentName = StringUtils.isNotEmpty((String)sampleLotMap.get("DEPARTMENTNAME"))?(String)sampleLotMap.get("DEPARTMENTNAME"):" ";
						}
						else if(StringUtils.equals("AUTO", (String)sampleLotMap.get("TYPE"))){
							departmentName = StringUtils.isNotEmpty((String)sampleLotMap.get("DEPARTMENTNAME"))?(String)sampleLotMap.get("DEPARTMENTNAME"):" ";
						}

						//2019.02.27_hsryu_Remove Logic. change located Logic. For EventUser.
						//EventInfo sampleOutHoldEventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), "Hold by SampleOutHoldFlag", "HoldLot", "HD-0001");
						MESLotServiceProxy.getLotServiceUtil().reserveAHold(lotData, departmentName, eventInfoForSampleOutHoldFlag);
					}					
				}				
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			return "";	
			// add by GJJ 20200408 mantis£º5851 sampleOutHoldÐèÒªÈ¡²¢ end 
		}
		return "";
	}

	public String getFirstReservLotByMachine(String machinename) throws CustomException
    {
        try
        {
            String strSql = StringUtil.EMPTY;
            strSql = strSql + " SELECT C.LOTNAME                                           \n";
            strSql = strSql + "   FROM CT_RESERVELOT C, LOT L                              \n";
            strSql = strSql + "  WHERE C.LOTNAME = L.LOTNAME                               \n";
            strSql = strSql + "    AND C.RESERVESTATE = :RESERVESTATE                      \n";
            strSql = strSql + "    AND C.PRODUCTSPECNAME = L.PRODUCTSPECNAME               \n";
            strSql = strSql + "    AND C.PRODUCTREQUESTNAME = L.PRODUCTREQUESTNAME         \n";
            strSql = strSql + "    AND C.MACHINENAME = :MACHINENAME                        \n";
            strSql = strSql + "  ORDER BY L.PRIORITY, C.PLANRELEASEDTIME, C.RESERVETIMEKEY \n";

            Map<String, Object> bindMap = new HashMap<String, Object>();

            bindMap.put("RESERVESTATE", GenericServiceProxy.getConstantMap().RESV_LOT_STATE_RESV);
            bindMap.put("PRODUCTREQUESTNAME", machinename);

            List<Map<String, Object>> reservelotList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

            if(reservelotList.size() < 1)
            {
                throw new CustomException("LOT-0218", machinename);
            }

            return reservelotList.get(0).get("LOTNAME").toString();

        }
        catch (Exception ex)
        {
            throw new CustomException("LOT-0218", machinename);
        }
    }

	public String getFirstReservLotByProductRequestName(String machinename, String productrequestname, Timestamp planReleaseTime) throws CustomException
    {
        try
        {
            String strSql = StringUtil.EMPTY;
            strSql = strSql + " SELECT C.LOTNAME                                           \n";
            strSql = strSql + "   FROM CT_RESERVELOT C, LOT L                              \n";
            strSql = strSql + "  WHERE C.LOTNAME = L.LOTNAME                               \n";
            strSql = strSql + "    AND C.RESERVESTATE = :RESERVESTATE                      \n";
            strSql = strSql + "    AND C.PRODUCTSPECNAME = L.PRODUCTSPECNAME               \n";
            strSql = strSql + "    AND C.PRODUCTREQUESTNAME = L.PRODUCTREQUESTNAME         \n";
            strSql = strSql + "    AND C.PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME          \n";
            /* 20181107, hhlee, modify, add condition because of change RESERVESTATE at OPI(Requested by Guishi) */
            strSql = strSql + "    AND L.LOTSTATE = :LOTSTATE                              \n";
            /* 2019110117, hhlee, modify, add condition PLANRELEASEDTIME */
            strSql = strSql + "    AND C.PLANRELEASEDTIME = :PLANRELEASEDTIME              \n";
            strSql = strSql + "  ORDER BY L.PRIORITY , C.PLANRELEASEDTIME, C.RESERVETIMEKEY, C.LOTNAME \n";

            Map<String, Object> bindMap = new HashMap<String, Object>();

            bindMap.put("RESERVESTATE", GenericServiceProxy.getConstantMap().RESV_LOT_STATE_RESV);
            bindMap.put("PRODUCTREQUESTNAME", productrequestname);
            /* 2019110117, hhlee, modify, add condition PLANRELEASEDTIME */
            bindMap.put("PLANRELEASEDTIME", planReleaseTime);
            bindMap.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Created);

            List<Map<String, Object>> reservelotList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

            /* 2019110117, hhlee, modify, add validate reservelotList == 'NULL' */
            if(reservelotList == null || reservelotList.size() <= 0)
            {
                /* 2019110117, hhlee, modify, add validate reservelotList == 'NULL' */
                //throw new CustomException("LOT-0218", machinename, productrequestname);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                throw new CustomException("LOT-9045", machinename, productrequestname,  dateFormat.format(planReleaseTime));
            }

            return reservelotList.get(0).get("LOTNAME").toString();

        }
        catch (Exception ex)
        {
            /* 2019110117, hhlee, modify, change errorcode */
            //throw new CustomException("LOT-0218", machinename, productrequestname);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            throw new CustomException("LOT-9045", machinename, productrequestname,  dateFormat.format(planReleaseTime));            
        }
    }

	public String getFirstStartLotByMachine(String machinename) throws CustomException
    {
        try
        {
            String strSql = StringUtil.EMPTY;
            strSql = strSql + " SELECT C.LOTNAME                                           \n";
            strSql = strSql + "   FROM CT_RESERVELOT C, LOT L                              \n";
            strSql = strSql + "  WHERE C.LOTNAME = L.LOTNAME                               \n";
            strSql = strSql + "    AND C.RESERVESTATE = :RESERVESTATE                      \n";
            strSql = strSql + "    AND C.PRODUCTSPECNAME = L.PRODUCTSPECNAME               \n";
            strSql = strSql + "    AND C.PRODUCTREQUESTNAME = L.PRODUCTREQUESTNAME         \n";
            strSql = strSql + "    AND C.MACHINENAME = :MACHINENAME                        \n";
            strSql = strSql + "  ORDER BY L.PRIORITY, C.PLANRELEASEDTIME, C.RESERVETIMEKEY \n";

            Map<String, Object> bindMap = new HashMap<String, Object>();

            bindMap.put("RESERVESTATE", GenericServiceProxy.getConstantMap().RESV_LOT_STATE_START);
            bindMap.put("PRODUCTREQUESTNAME", machinename);

            List<Map<String, Object>> reservelotList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

            if(reservelotList.size() < 1)
            {
                throw new CustomException("LOT-0218", machinename);
            }

            return reservelotList.get(0).get("LOTNAME").toString();

        }
        catch (Exception ex)
        {
            throw new CustomException("LOT-0218", machinename);
        }
    }

	public String getFirstStartLotByProductRequestName(String machinename, String productrequestname) throws CustomException
    {
        try
        {
            String strSql = StringUtil.EMPTY;
            strSql = strSql + " SELECT C.LOTNAME                                           \n";
            strSql = strSql + "   FROM CT_RESERVELOT C, LOT L                              \n";
            strSql = strSql + "  WHERE C.LOTNAME = L.LOTNAME                               \n";
            strSql = strSql + "    AND C.RESERVESTATE = :RESERVESTATE                      \n";
            strSql = strSql + "    AND C.PRODUCTSPECNAME = L.PRODUCTSPECNAME               \n";
            strSql = strSql + "    AND C.PRODUCTREQUESTNAME = L.PRODUCTREQUESTNAME         \n";
            strSql = strSql + "    AND C.PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME          \n";
            strSql = strSql + "  ORDER BY L.PRIORITY, C.PLANRELEASEDTIME, C.RESERVETIMEKEY, L.PRIORITY \n";

            Map<String, Object> bindMap = new HashMap<String, Object>();

            bindMap.put("RESERVESTATE", GenericServiceProxy.getConstantMap().RESV_LOT_STATE_START);
            bindMap.put("PRODUCTREQUESTNAME", productrequestname);

            List<Map<String, Object>> reservelotList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

            if(reservelotList.size() < 1)
            {
                throw new CustomException("LOT-0218", machinename, productrequestname);
            }

            return reservelotList.get(0).get("LOTNAME").toString();

        }
        catch (Exception ex)
        {
            throw new CustomException("LOT-0218", machinename, productrequestname);
        }
    }

	/**
	 * 20180528 : setCorresSampleLotData by Auto
	 */
	public CorresSampleLot setCorresSampleLotData(String sampleFlag, EventInfo eventInfo, Lot lotData, SampleLotCount countInfo, String systemPosition, String manualPosition, String holdFlag, boolean sumFlag) throws CustomException
	{
		List<CorresSampleLot> corresSampleLotList = new ArrayList<CorresSampleLot>();
		CorresSampleLot corresSampleLot = new CorresSampleLot();
		String sumPosition= "";

		if(sumFlag)
		{
			sumPosition = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(systemPosition, manualPosition);
		}

		try
		{
			corresSampleLotList = ExtendedObjectProxy.getCorresSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? and processOperationName = ? and processOperationVersion = ? and machineName = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? ", new Object[] {lotData.getKey().getLotName(),
					lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(),lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), countInfo.getMachineName(),
					countInfo.getCorresSampleProcessFlowName(), countInfo.getCorresSampleProcessFlowVersion()});

			if(corresSampleLotList.size()==1)
			{
				corresSampleLot = corresSampleLotList.get(0);
				
				//2019.04.08_hsryu_Insert Logic. Prevent ORA-00001 Error. 
				eventInfo.setEventTimeKey(String.valueOf(new BigDecimal(eventInfo.getEventTimeKey()).add(new BigDecimal(1))));

				corresSampleLot.setSampleFlag(sampleFlag);
				corresSampleLot.setCurrentCount(countInfo.getCorresCurrentCount());
				corresSampleLot.setTotalCount(countInfo.getCorresTotalCount());
				corresSampleLot.setSystemSamplePosition(systemPosition);
				corresSampleLot.setManualSamplePosition(manualPosition);
				if(sumFlag)
				{
					corresSampleLot.setActualSamplePosition(sumPosition);
				}
				else
				{
					corresSampleLot.setActualSamplePosition(systemPosition);
				}
				corresSampleLot.setSampleState("Decided");
				corresSampleLot.setSampleOutHoldFlag(holdFlag);
				corresSampleLot = ExtendedObjectProxy.getCorresSampleLotService().modify(eventInfo, corresSampleLot);
			}

			return corresSampleLot;
		}
		catch ( Throwable e )
		{
			if ( corresSampleLotList.size() == 0 )
			{
				corresSampleLot.setLotName(lotData.getKey().getLotName());
				corresSampleLot.setFactoryName(lotData.getFactoryName());
				corresSampleLot.setProductSpecName(lotData.getProductSpecName());
				corresSampleLot.setEcCode(lotData.getUdfs().get("ECCODE"));
				corresSampleLot.setProcessFlowName(lotData.getProcessFlowName());
				corresSampleLot.setProcessFlowVersion(lotData.getProcessFlowVersion());
				corresSampleLot.setProcessOperationName(lotData.getProcessOperationName());
				corresSampleLot.setProcessOperationVersion(lotData.getProcessOperationVersion());
				corresSampleLot.setMachineName(countInfo.getMachineName());
				corresSampleLot.setSampleProcessFlowName(countInfo.getCorresSampleProcessFlowName());
				corresSampleLot.setSampleProcessFlowVersion(countInfo.getCorresSampleProcessFlowVersion());
				corresSampleLot.setFromProcessOperationName(countInfo.getCorresProcessOperationName());
				corresSampleLot.setFromProcessOperationVersion(countInfo.getCorresProcessOperationVersion());
				corresSampleLot.setSampleFlag(sampleFlag);
				corresSampleLot.setSampleCount(countInfo.getCorresSampleCount());
				corresSampleLot.setCurrentCount(countInfo.getCorresCurrentCount());
				corresSampleLot.setTotalCount(countInfo.getCorresTotalCount());
				corresSampleLot.setSystemSamplePosition(systemPosition);
				corresSampleLot.setManualSamplePosition(manualPosition);
				// ToDo : If exist same sampling, ActualSamplePosition is mix
				if(sumFlag)
				{
					corresSampleLot.setActualSamplePosition(sumPosition);
					corresSampleLot.setManualSampleFlag("Y");
				}
				else
				{
					corresSampleLot.setActualSamplePosition(systemPosition);
					corresSampleLot.setManualSampleFlag("N");
				}
				corresSampleLot.setSampleState("Decided");
				corresSampleLot.setSampleOutHoldFlag(holdFlag);
				//2019.02.27_hsryu_Mantis 0002723. eventInfo.getLastEventUser -> "System"
				corresSampleLot.setLastEventUser("System");
				corresSampleLot.setLastEventComment(eventInfo.getEventComment());
				corresSampleLot.setReasonCode(eventInfo.getReasonCode());
				corresSampleLot.setReasonCodeType(eventInfo.getReasonCodeType());
				corresSampleLot.setLastEventTime(eventInfo.getEventTime());
				corresSampleLot.setLastEventTimekey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				corresSampleLot.setLastEventName(eventInfo.getEventName());

				corresSampleLot = ExtendedObjectProxy.getCorresSampleLotService().create(eventInfo, corresSampleLot);
			}

			return corresSampleLot;
		}
	}

	public boolean checkExistCorresInfo(String coOperation, String coOperationVersion, String coSampleFlowName, String coSampleFlowVersion, String coSampleLotCount, String coSamplePosition)
	{
		/************** 2019.03.12_hsryu_Modify Logic. For Multi CorresSample. *************/
		log.info("CorresInfo : " + coOperation + "&" + coOperationVersion + "&" + coSampleFlowName + "&" + coSampleFlowVersion + "&" + coSampleLotCount + "&" + coSamplePosition);

		if(StringUtils.equals(coOperation, "-"))
			return false;
		if(StringUtils.equals(coSampleFlowName, "-"))
			return false;
		if(StringUtils.isEmpty(coSampleLotCount))
			return false;
		if(StringUtils.isEmpty(coSamplePosition))
			return false;
		/***********************************************************************************/
		
		return true;
	}

	//add by wghuang 20180529
	public String getToMachineName_old(String operationMode, String factoryName, String productSpecName, String ecCode, String processFlowName, String processOperationName ) throws CustomException
	{
		log.info("getToMachineName Started.");

		String toMachineName = "";

		if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
		{
			log.info("OperationMode : INDP");

			String strSql = "SELECT DISTINCT MACHINENAME " +
							"  FROM TPEFOMPOLICY T " +
							"  INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID  " +
							" WHERE     1 = 1 " +
							"       AND FACTORYNAME = :FACTORYNAME " +
							"       AND PRODUCTSPECNAME = :PRODUCTSPECNAME " +
							"       AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
							"       AND ECCODE = :ECCODE " +
							"       AND PROCESSFLOWNAME = :PROCESSFLOWNAME " +
							"       AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
							"       AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
							"       AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " ;

					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("FACTORYNAME", factoryName);
					bindMap.put("PRODUCTSPECNAME", productSpecName);
					bindMap.put("PRODUCTSPECVERSION", "00001");
					bindMap.put("ECCODE", ecCode);
					bindMap.put("PROCESSFLOWNAME", processFlowName);
					bindMap.put("PROCESSFLOWVERSION", "00001");
					bindMap.put("PROCESSOPERATIONVERSION", "00001");
					bindMap.put("PROCESSOPERATIONNAME", processOperationName);


	         List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

	         if ( tpefomPolicyData.size() <= 0 )
	     		throw new CustomException("POLICY-0017",factoryName,productSpecName,ecCode,processFlowName,processOperationName);

	         if(tpefomPolicyData.size() == 1)
	        	 toMachineName = tpefomPolicyData.get(0).toString();
	         else if(tpefomPolicyData.size() > 1)
	         {
	        	 if(tpefomPolicyData.size() > 2)
	        		 throw new CustomException("POLICY-0018","");

	        	 for(Map<String,Object> policyData : tpefomPolicyData)
	        	 {
	        		 String machineName = policyData.toString();

	        		 Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

	        		 if(StringUtil.equals(machineData.getMachineStateName(), GenericServiceProxy.getConstantMap().MACHINE_STATE_IDLE))
	        		 {
	        			 toMachineName = machineName;
	        			 break;
	        		 }
	        	 }

	        	 //should add logic when 2 units are in RUN mode
	         }
		}

		log.info("getToMachineName Ended.");

		return toMachineName;
	}

	public String getToMachineName(String operationName, String operationMode) throws CustomException
	{
		log.info("getToMachineName Started.");

		String toMachineName = "";

		//only check in INDP Mode
		if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
		{
			String Factoryname = GenericServiceProxy.getConstantMap().DEFAULT_FACTORY;
			String ProcessOperationName = "^[*]|" + operationName;
			String ProcessOperationVersion = "^[*]|" + GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
			String OperationModeName = operationMode;


			log.info("OperationMode : INDP");

			String strSql = "SELECT P.MACHINENAME " +
							"  FROM TOPOLICY T, POSOPERATIONMODE P " +
							" WHERE     1 = 1 " +
							"       AND T.CONDITIONID = P.CONDITIONID " +
							"       AND T.FACTORYNAME = :FACTORYNAME " +
							"       AND REGEXP_LIKE (T.PROCESSOPERATIONNAME, :PROCESSOPERATIONNAME) " +
							"       AND REGEXP_LIKE (T.PROCESSOPERATIONVERSION, :PROCESSOPERATIONVERSION) " +
							"       AND P.OPERATIONMODE = :OPERATIONMODE "  ;

	        Map<String, Object> bindMap = new HashMap<String, Object>();
	        bindMap.put("FACTORYNAME", Factoryname);
	        bindMap.put("PROCESSOPERATIONNAME", ProcessOperationName);
	        bindMap.put("PROCESSOPERATIONVERSION", ProcessOperationVersion);
	        bindMap.put("OPERATIONMODE", OperationModeName);

	        List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

	        if ( tpefomPolicyData.size() <= 0 )
	     		throw new CustomException("POLICY-0019",GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,operationName,operationMode);

	         if(tpefomPolicyData.size() == 1)
	        	 toMachineName = tpefomPolicyData.get(0).toString();
	         else if(tpefomPolicyData.size() > 1)
	         {
	        	 if(tpefomPolicyData.size() > 2)
	        		 throw new CustomException("POLICY-0018","");

	        	 for(Map<String,Object> policyData : tpefomPolicyData)
	        	 {
	        		 String machineName = policyData.toString();

	        		 Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

	        		 if(StringUtil.equals(machineData.getMachineStateName(), GenericServiceProxy.getConstantMap().MACHINE_STATE_IDLE))
	        		 {
	        			 toMachineName = machineName;
	        			 break;
	        		 }
	        	 }

	        	 //should add logic when 2 units are in RUN mode
	        	 toMachineName = tpefomPolicyData.get(0).toString();
	         }
		}

		log.info("getToMachineName Ended.");

		return toMachineName;
	}

	//add by wghuang 20180607
    public List<Map<String, Object>> getToMachineNameAndOperationNameByUnitName(List<Lot>lotList, String unitName, String operationMode) throws CustomException
    {
        List<Map<String, Object>> toMachineAndOperationName = new ArrayList<Map<String,Object>>();
        
        //only check in INDP Mode
        if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
        {
            String Factoryname = GenericServiceProxy.getConstantMap().DEFAULT_FACTORY;
            String ProcessOperationVersion = "^[*]|" + GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
            String OperationModeName = operationMode;        
                    
            String Sql = " SELECT TPEFOM.PROCESSOPERATIONNAME, TPEFOM.MACHINENAME                       \n" +
                         "   FROM TPEFOMPOLICY TPEFOM                                                   \n" +
                         "   INNER JOIN POSRECIPE P ON TPEFOM.CONDITIONID = P.CONDITIONID              \n" +
                         "  WHERE 1=1                                                                   \n" +
                         "    AND TPEFOM.FACTORYNAME = :FACTORYNAME                                     \n" +
                         "    AND TPEFOM.MACHINENAME = :MACHINENAME                                     \n" +
                         "    AND REGEXP_LIKE(TPEFOM.PROCESSOPERATIONVERSION, :PROCESSOPERATIONVERSION) \n" +
                         "  GROUP BY TPEFOM.PROCESSOPERATIONNAME, TPEFOM.MACHINENAME                    \n";
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("FACTORYNAME", Factoryname);
            bindMap.put("MACHINENAME", unitName);
            bindMap.put("PROCESSOPERATIONVERSION", ProcessOperationVersion);
                        
            List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(Sql, bindMap);
            
            if ( tpefomPolicyData == null || tpefomPolicyData.size() <= 0 )
            {
                throw new CustomException("POLICY-0022",GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, unitName,operationMode);
            }
            
            Map<String, Object> toMachineName = null;
            boolean isToMachineCheck = false;
            for(Map<String, Object> result : tpefomPolicyData)
            {
                String processOperationName = CommonUtil.getValue(result, "PROCESSOPERATIONNAME");
               
               for(Lot lot : lotList)
               {
                   if(StringUtil.equals(lot.getProcessOperationName(), processOperationName))
                   {
                       toMachineName = result;
                       isToMachineCheck = true;
                       break;
                   }
               }
               if(isToMachineCheck)
               {
                   break;
               }
            }
            if(toMachineName != null)
            {
                toMachineAndOperationName.add(toMachineName);
            }                  
        }       
        
        return toMachineAndOperationName;
        
    }
    
    /**
     * 
     * @Name     getMachineNameAndOperationNameByTOPolicy
     * @since    2018. 10. 24.
     * @author   hhlee
     * @contents Validate TOPolicy
     *           
     * @param lotList
     * @param machineName
     * @param unitName
     * @param operationMode
     * @return
     * @throws CustomException
     */
    public List<Map<String, Object>> getMachineNameAndOperationNameByTOPolicy(List<Lot>lotList, String machineName, String unitName, String operationMode) throws CustomException
    {
        List<Map<String, Object>> toPolicyMachineAndOperationName = new ArrayList<Map<String,Object>>();
        
        //String Factoryname = GenericServiceProxy.getConstantMap().DEFAULT_FACTORY;
        String Factoryname = lotList.get(0).getFactoryName();
        String ProcessOperationVersion = "^[*]|" + GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
        unitName = "^[*]|" + unitName;
                
        String Sql =  " SELECT MGROUP.MACHINENAME,                                                                          \n"
                    + "        MGROUP.PROCESSOPERATIONNAME                                                                  \n"
                    + "   FROM (                                                                                            \n"
                    + "         SELECT TP.FACTORYNAME                                                                       \n"
                    + "               ,TP.PROCESSOPERATIONNAME                                                              \n"
                    + "               ,TP.PROCESSOPERATIONVERSION                                                           \n"
                    + "               ,POM.MACHINENAME                                                                      \n"
                    + "               ,POM.OPERATIONMODE                                                                    \n"
                    + "               ,POM.UNITNAME                                                                         \n"
                    + "           FROM TOPOLICY TP                                                                          \n"
                    + "               ,POSOPERATIONMODE POM                                                                 \n"
                    + "          WHERE 1=1                                                                                  \n"
                    + "            AND TP.CONDITIONID = POM.CONDITIONID                                                     \n"
                    + "            AND TP.FACTORYNAME = :FACTORYNAME                                                        \n"
                    + "            AND POM.MACHINENAME = :MACHINENAME                                                       \n"
                    + "            AND POM.OPERATIONMODE = :OPERATIONMODE                                                   \n"
                    + "            AND REGEXP_LIKE(TP.PROCESSOPERATIONVERSION, :PROCESSOPERATIONVERSION)                    \n"
                    + "            AND REGEXP_LIKE(POM.UNITNAME , :UNITNAME)                                                \n"
                    + "        ) POLICY,                                                                                    \n"
                    + "        (                                                                                            \n"
                    + "         SELECT MGM.MACHINENAME                                                                      \n"
                    + "               ,MGM.MACHINEGROUPNAME                                                                 \n"
                    + "               ,OS.PROCESSOPERATIONNAME                                                              \n"
                    + "               ,OS.PROCESSOPERATIONVERSION                                                           \n"
                    + "           FROM CT_MACHINEGROUPMACHINE MGM                                                           \n"
                    + "               ,PROCESSOPERATIONSPEC OS                                                              \n"
                    + "          WHERE 1=1                                                                                  \n"
                    + "            AND MGM.MACHINENAME = :MACHINENAME                                                       \n"
                    + "            AND MGM.MACHINEGROUPNAME = OS.MACHINEGROUPNAME                                           \n"
                    + "          GROUP BY MGM.MACHINENAME,                                                                  \n"
                    + "                   OS.PROCESSOPERATIONNAME,                                                          \n"
                    + "                   OS.PROCESSOPERATIONVERSION,                                                       \n"
                    + "                   MGM.MACHINEGROUPNAME                                                              \n"
                    + "        ) MGROUP                                                                                     \n"
                    + " WHERE 1=1                                                                                           \n"
                    + "   AND (CASE WHEN POLICY.PROCESSOPERATIONNAME = '*' THEN '1' ELSE POLICY.PROCESSOPERATIONNAME END =  \n"
                    + "          CASE WHEN POLICY.PROCESSOPERATIONNAME = '*' THEN '1' ELSE MGROUP.PROCESSOPERATIONNAME END) \n"
                    + "  GROUP BY MGROUP.MACHINENAME, MGROUP.PROCESSOPERATIONNAME                                             ";
//        
//        String Sql =  " SELECT CASE WHEN POLICY.UNITNAME <> '*' THEN POLICY.UNITNAME ELSE                                   \n"
//                    + "                  MGROUP.MACHINENAME END AS MACHINENAME,                                             \n"
//                    + "        MGROUP.PROCESSOPERATIONNAME                                                                  \n"
//                    + "   FROM (                                                                                            \n"
//                    + "         SELECT TP.FACTORYNAME                                                                       \n"
//                    + "               ,TP.PROCESSOPERATIONNAME                                                              \n"
//                    + "               ,TP.PROCESSOPERATIONVERSION                                                           \n"
//                    + "               ,POM.MACHINENAME                                                                      \n"
//                    + "               ,POM.OPERATIONMODE                                                                    \n"
//                    + "               ,POM.UNITNAME                                                                         \n"
//                    + "           FROM TOPOLICY TP                                                                          \n"
//                    + "               ,POSOPERATIONMODE POM                                                                 \n"
//                    + "          WHERE 1=1                                                                                  \n"
//                    + "            AND TP.CONDITIONID = POM.CONDITIONID                                                     \n"
//                    + "            AND TP.FACTORYNAME = :FACTORYNAME                                                        \n"
//                    + "            AND POM.MACHINENAME = :MACHINENAME                                                       \n"
//                    + "            AND POM.OPERATIONMODE = :OPERATIONMODE                                                   \n"
//                    + "            AND REGEXP_LIKE(TP.PROCESSOPERATIONVERSION, :PROCESSOPERATIONVERSION)                    \n"
//                    + "            AND REGEXP_LIKE(POM.UNITNAME , :UNITNAME)                                                \n"
//                    + "        ) POLICY,                                                                                    \n"
//                    + "        (                                                                                            \n"
//                    + "         SELECT MGM.MACHINENAME                                                                      \n"
//                    + "               ,MGM.MACHINEGROUPNAME                                                                 \n"
//                    + "               ,OS.PROCESSOPERATIONNAME                                                              \n"
//                    + "               ,OS.PROCESSOPERATIONVERSION                                                           \n"
//                    + "           FROM CT_MACHINEGROUPMACHINE MGM                                                           \n"
//                    + "               ,PROCESSOPERATIONSPEC OS                                                              \n"
//                    + "          WHERE 1=1                                                                                  \n"
//                    + "            AND MGM.MACHINENAME = :MACHINENAME                                                       \n"
//                    + "            AND MGM.MACHINEGROUPNAME = OS.MACHINEGROUPNAME                                           \n"
//                    + "          GROUP BY MGM.MACHINENAME,                                                                  \n"
//                    + "                   OS.PROCESSOPERATIONNAME,                                                          \n"
//                    + "                   OS.PROCESSOPERATIONVERSION,                                                       \n"
//                    + "                   MGM.MACHINEGROUPNAME                                                              \n"
//                    + "        ) MGROUP                                                                                     \n"
//                    + " WHERE 1=1                                                                                           \n"
//                    + "   AND (CASE WHEN POLICY.PROCESSOPERATIONNAME = '*' THEN '1' ELSE POLICY.PROCESSOPERATIONNAME END =  \n"
//                    + "          CASE WHEN POLICY.PROCESSOPERATIONNAME = '*' THEN '1' ELSE MGROUP.PROCESSOPERATIONNAME END) \n"
//                    + "  GROUP BY MGROUP.MACHINENAME, POLICY.UNITNAME, MGROUP.PROCESSOPERATIONNAME                            ";
        
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("FACTORYNAME", Factoryname);
        bindMap.put("MACHINENAME", machineName);
        bindMap.put("OPERATIONMODE", operationMode);
        bindMap.put("PROCESSOPERATIONVERSION", ProcessOperationVersion);
        bindMap.put("UNITNAME", unitName);
                    
        List<Map<String, Object>> toPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(Sql, bindMap);
        
        if (toPolicyData == null || toPolicyData.size() <= 0 )
        {
            throw new CustomException("POLICY-0024",machineName, unitName, operationMode);
        }
        
        Map<String, Object> workPolicyData = null;
        boolean isExistCheck = false;
        for(Map<String, Object> result : toPolicyData)
        {
            String processOperationName = CommonUtil.getValue(result, "PROCESSOPERATIONNAME");
           
            for(Lot lot : lotList)
            {
                if(StringUtil.equals(lot.getProcessOperationName(), processOperationName))
                {
                    workPolicyData = result;
                    isExistCheck = true;
                    break;
                }
            }
            if(isExistCheck)
            {
                break;
            }
        }
        if(workPolicyData != null)
        {
            toPolicyMachineAndOperationName.add(workPolicyData);
        }
        else
        {
            throw new CustomException("POLICY-0025",machineName, unitName, operationMode);
        }
        
        return toPolicyMachineAndOperationName;
    }
    
	//add by wghuang 20180607
    public List<Map<String, Object>> getToMachineNameAndOperationName(List<Lot>lotList,String MachineName, String operationMode) throws CustomException
    {
        List<Map<String, Object>> toMachineAndOperationName = new ArrayList<Map<String,Object>>();
        
        //only check in INDP Mode
        if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
        {
            String strSql = "SELECT MACHINENAME " +
                    "  FROM MACHINESPEC " +
                    " WHERE SUPERMACHINENAME = ? " +
                    "   AND MACHINETYPE = ? " ;
        
            Object[] bindSet = new String[]{MachineName, GenericServiceProxy.getConstantMap().Mac_ProductionMachine};
        
            List<ListOrderedMap> unitList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindSet);
            
            if(unitList == null || unitList.size() <= 0)
                throw new CustomException("MACHINE-9004", MachineName);
            
            String unitlistName = "";
            
            for(ListOrderedMap unit : unitList)
            {
                if(StringUtil.isEmpty(unitlistName))
                    unitlistName = "'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
                else
                unitlistName += ",'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
            }
            
            //-----------------------------------------------------
            
            String Factoryname = GenericServiceProxy.getConstantMap().DEFAULT_FACTORY;
            String ProcessOperationVersion = "^[*]|" + GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
            String OperationModeName = operationMode;        
        
            log.info("OperationMode : INDP");
            
            String Sql = StringUtil.EMPTY;
            Sql = Sql + " SELECT TPEFOM.PROCESSOPERATIONNAME, TPEFOM.MACHINENAME                       \n" +
                        "   FROM TPEFOMPOLICY TPEFOM                                                   \n" +
                        "   INNER JOIN POSRECIPE P ON TPEFOM.CONDITIONID  = P.CONDITIONID              \n" +
                        "  WHERE 1=1                                                                   \n" +
                        "    AND TPEFOM.FACTORYNAME = :FACTORYNAME                                     \n" +
                        "    AND TPEFOM.MACHINENAME IN ( " + unitlistName + ")                         \n" +
                        "    AND REGEXP_LIKE(TPEFOM.PROCESSOPERATIONVERSION, :PROCESSOPERATIONVERSION) \n" +
                        "  GROUP BY TPEFOM.PROCESSOPERATIONNAME, TPEFOM.MACHINENAME                    \n";
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("FACTORYNAME", Factoryname);
            bindMap.put("PROCESSOPERATIONVERSION", ProcessOperationVersion);
            bindMap.put("OPERATIONMODE", OperationModeName);
            
            List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(Sql, bindMap);
            
            if ( tpefomPolicyData == null || tpefomPolicyData.size() <= 0 )
            {
                throw new CustomException("POLICY-0022",GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,unitlistName,operationMode);
            }
            
            //String toMachineName = StringUtil.EMPTY;
            //String processOperationName = StringUtil.EMPTY;
            Map<String, Object> toMachineName = null;
            /* 1. Á¶È¸ÇÑ UNITÀÌ 2°³ ÀÌ»óÀÎÁö È®ÀÎÇØ º»´Ù. */
            /* 1. I check whether it is bigger than 2 or the inquired UNIT is the same. */
            if(tpefomPolicyData.size() >= 2)
            {
                /* 2. Á¶È¸ÇÑ UNITÀÌ 2°³ ÀÌ»óÀÌ¸é */
                /* 2. If it is bigger than 2 or the inquired UNIT is the same. */
                /* 2-1. Á¶È¸ÇÑ UNITÀÇ °øÁ¤ÀÌ ¸ðµÎ °°ÀºÁö È®ÀÎÇÑ´Ù. */
                /* 2-1. I check whether the process of work of the inquired UNIT is altogether the same. */
                boolean isSameOperationCheck = true;
                for(Map<String,Object> outTpefomData : tpefomPolicyData)
                {
                    for(Map<String,Object> inTpefomData : tpefomPolicyData)
                    {
                       if(!StringUtil.equals( CommonUtil.getValue(outTpefomData, "PROCESSOPERATIONNAME"), 
                               CommonUtil.getValue(inTpefomData, "PROCESSOPERATIONNAME")))
                       {
                           isSameOperationCheck = false;
                           break;
                       }
                    }
                    if(!isSameOperationCheck)
                    {
                        break;
                    }
                }
                               
                if(isSameOperationCheck)
                {
                    boolean isIdleUnit = false;
                    /* 2-1-1. ¸ðµÎ °°À¸¸é ÇöÀç IDLEÀÎ ¼³ºñ·Î toMachineÀ» ÁöÁ¤ÇÑ´Ù. */
                    /* 2-1-1. If the process of work of the inquired UNIT is altogether the same, I designate the UNIT, that is IDLE */
                    for(Map<String,Object> tpefomData : tpefomPolicyData)
                    {
                         String machineName = CommonUtil.getValue(tpefomData, "MACHINENAME");
                         
                         Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
                         
                         if(StringUtil.equals(machineData.getMachineStateName(), GenericServiceProxy.getConstantMap().MACHINE_STATE_IDLE))
                         {
                             toMachineName = tpefomData;
                             isIdleUnit = true;
                             break;
                         }
                    }                    
                    /* 2-1-2. IDLEÀÎ ¼³ºñ°¡ Á¸ÀçÇÏÁö ¾Ê°í ¸ðµÎ RUNÀÌ¸é ·¥´ýÀ¸·Î ÁöÁ¤ÇÑ´Ù. */
                    /* 2-1-2. If the equipment, that is IDLE, dosen't exit, I designate an UNIT for the random*/
                    if(!isIdleUnit)
                    {
                        toMachineName = tpefomPolicyData.get((int)(Math.random()*tpefomPolicyData.size()));
                    }
                }
                /* 2-2. Á¶È¸ÇÑ UNITÀÇ °øÁ¤ÀÌ °°Áö ¾ÊÀ¸¸é */
                /* 2-2. If the process of work of the inquired euipment is not the same */
                else 
                {
                    /* 2-2-1. ÇöÀç CST¿¡ ´ã±ä Glass¿¡ ´ëÇØ¼­  °øÁ¤ÀÌ °°Àº Glass¸¦ Ã£´Â´Ù. */  
                    /* 2-2-1. The process finds the same PRODUCT about the PRODUCT of the Cassette. */
                } 
            }
            else
            {
                /* 3. 1°³ ÀÌÇÏ¸é ÇöÀç CST¿¡ ´ã±ä Glass¿¡ ´ëÇØ¼­  °øÁ¤ÀÌ °°Àº Glass¸¦ Ã£´Â´Ù. */
                /* 3. If it is smaller than 1 or the inquired UNIT is the same, The process finds the same PRODUCT about the PRODUCT of the Cassette. */                 
            }
                        
            if(toMachineName != null)
            {
                toMachineAndOperationName.add(toMachineName);
            }
            else
            {
                /* 2-2-1. ÇöÀç CST¿¡ ´ã±ä Glass¿¡ ´ëÇØ¼­  °øÁ¤ÀÌ °°Àº Glass¸¦ Ã£´Â´Ù. */
                /* 2-2-1. The process finds the same PRODUCT about the PRODUCT of the Cassette */
                /* 3. 1°³ ÀÌÇÏ¸é ÇöÀç CST¿¡ ´ã±ä Glass¿¡ ´ëÇØ¼­  °øÁ¤ÀÌ °°Àº Glass¸¦ Ã£´Â´Ù. */
                /* 3. If it is smaller than 1 or the inquired UNIT is the same, The process finds the same PRODUCT about the PRODUCT of the Cassette. */
                boolean isToMachineCheck = false;
                for(Map<String, Object> result : tpefomPolicyData)
                {
                    String processOperationName = CommonUtil.getValue(result, "PROCESSOPERATIONNAME");
                   
                   for(Lot lot : lotList)
                   {
                       if(StringUtil.equals(lot.getProcessOperationName(), processOperationName))
                       {
                           toMachineName = result;
                           isToMachineCheck = true;
                           break;
                       }
                   }
                   if(isToMachineCheck)
                   {
                       break;
                   }
                }
                if(toMachineName != null)
                {
                    toMachineAndOperationName.add(toMachineName);
                }                
            }                    
        }       
        
        return toMachineAndOperationName;
        
        //log.info("=======================================getToMachineName Started.========================================");
        //        
        //List<Map<String, Object>> toMachineAndOperationName = new ArrayList<Map<String,Object>>();
        //
        ////only check in INDP Mode
        //if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
        //{
        //    String strSql = "SELECT MACHINENAME " +
        //            "  FROM MACHINESPEC " +
        //            " WHERE SUPERMACHINENAME = ? " +
        //            "   AND MACHINETYPE = ? " ;
        //
        //    Object[] bindSet = new String[]{MachineName,"ProductionMachine"};
        //
        //    List<ListOrderedMap> unitList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindSet);
        //    
        //    if(unitList == null || unitList.size() <= 0)
        //        throw new CustomException("MACHINE-9004", MachineName);
        //    
        //    String unitlistName = "";
        //    
        //    for(ListOrderedMap unit : unitList)
        //    {
        //        if(StringUtil.isEmpty(unitlistName))
        //            unitlistName = "'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
        //        else
        //        unitlistName += ",'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
        //    }
        //    
        //    //-----------------------------------------------------
        //    
        //    String Factoryname = GenericServiceProxy.getConstantMap().DEFAULT_FACTORY;
        //    String ProcessOperationVersion = "^[*]|" + GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
        //    String OperationModeName = operationMode;        
        //
        //    log.info("OperationMode : INDP");
        //    
        //    /* 20181010, hhlee, Delete ==>> */
        //    //String Sql = "SELECT T.PROCESSOPERATIONNAME, P.MACHINENAME " +
        //    //        "  FROM TOPOLICY T, POSOPERATIONMODE P  " +
        //    //        " WHERE     1 = 1  " +
        //    //        "       AND T.CONDITIONID = P.CONDITIONID  " +
        //    //        "       AND T.FACTORYNAME = :FACTORYNAME  " +
        //    //        "       AND P.MACHINENAME IN ( " + unitlistName + ") " +
        //    //        "       AND REGEXP_LIKE (T.PROCESSOPERATIONVERSION, :PROCESSOPERATIONVERSION)  " +
        //    //        "       AND P.OPERATIONMODE = :OPERATIONMODE  " ;                   
        //    
        //    //Map<String, Object> bindMap = new HashMap<String, Object>();
        //    //bindMap.put("FACTORYNAME", Factoryname);
        //    //bindMap.put("PROCESSOPERATIONVERSION", ProcessOperationVersion);
        //    //bindMap.put("OPERATIONMODE", OperationModeName);
        //    
        //    //List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(Sql, bindMap);
        //    /* <<== 20181010, hhlee, Delete */
        //    
        //    String Sql = StringUtil.EMPTY;
        //    Sql = Sql + " SELECT TPEFOM.PROCESSOPERATIONNAME, TPEFOM.MACHINENAME                       \n" +
        //                "   FROM TPEFOMPOLICY TPEFOM                                                   \n" +
        //                "  WHERE 1=1                                                                   \n" +
        //                "    AND TPEFOM.FACTORYNAME = :FACTORYNAME                                     \n" +
        //                "    AND TPEFOM.MACHINENAME IN ( " + unitlistName + ")                         \n" +
        //                "    AND REGEXP_LIKE(TPEFOM.PROCESSOPERATIONVERSION, :PROCESSOPERATIONVERSION) \n" +
        //                "  GROUP BY TPEFOM.PROCESSOPERATIONNAME, TPEFOM.MACHINENAME                    \n";
        //    
        //    Map<String, Object> bindMap = new HashMap<String, Object>();
        //    bindMap.put("FACTORYNAME", Factoryname);
        //    bindMap.put("PROCESSOPERATIONVERSION", ProcessOperationVersion);
        //    bindMap.put("OPERATIONMODE", OperationModeName);
        //    
        //    List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(Sql, bindMap);
        //    
        //    if ( tpefomPolicyData == null || tpefomPolicyData.size() <= 0 )
        //    {
        //        throw new CustomException("POLICY-0022",GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,unitlistName,operationMode);
        //    }
        //    
        //    boolean isToMachineCheck = false;
        //    for(Map<String, Object> result : tpefomPolicyData)
        //    {
        //       String OperationName = CommonUtil.getValue(result, "PROCESSOPERATIONNAME");
        //       
        //       for(Lot lot : lotList)
        //       {
        //           if(StringUtil.equals(lot.getProcessOperationName(), OperationName))
        //           {
        //               toMachineAndOperationName.add(result);
        //               isToMachineCheck = true;
        //               break;
        //           }
        //       }
        //       if(isToMachineCheck)
        //       {
        //           break;
        //       }
        //    }
        //    
        //    //toMachineAndOperationName = tpefomPolicyData;
        //    
        //    //if(tpefomPolicyData.size() == 1)
        //    //     return tpefomPolicyData;
        //    //else if(tpefomPolicyData.size() > 1)
        //    //{
        //    //    if(tpefomPolicyData.size() > 2)
        //    //         throw new CustomException("POLICY-0018","");
        //    //
        //    //OUT:
        //    //for(Map<String, Object> result : tpefomPolicyData)
        //    //{
        //    //   String OperationName = CommonUtil.getValue(result, "PROCESSOPERATIONNAME");
        //    //    
        //    //   for(Lot lot : lotList)
        //    //   {
        //    //       if(StringUtil.equals(lot.getProcessOperationName(), OperationName))
        //    //       {
        //    //           toMachineAndOperationName.add(result);
        //    //           break OUT;
        //    //       }
        //    //   }
        //    //}
        //         
        //    log.info("=======================================getToMachineName Ended.========================================");
        //         
        //        
        //         
        //       /*for(Map<String,Object> policyData : tpefomPolicyData)
        //         {
        //             String machineName = CommonUtil.getValue(policyData, "MACHINENAME");
        //             
        //             Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        //             
        //             if(StringUtil.equals(machineData.getMachineStateName(), GenericServiceProxy.getConstantMap().MACHINE_STATE_IDLE))
        //             {
        //                 toMachineName = machineName;
        //                 break;
        //             }
        //         }
        //         
        //         //should add logic when 2 units are in RUN mode
        //         toMachineName = CommonUtil.getValue(tpefomPolicyData.get(0), "MACHINENAME");*/
        //            
        //}       
        //
        //return toMachineAndOperationName;
    }
    
	public void checkOperationModeByCT_OperationMode(String machineName, String operationMode)throws CustomException
	{
		log.info("checkOperationModeByCT_OperationMode Started.");

		String strSql = "SELECT CT.MACHINENAME, CT.OPERATIONMODE " +
						"  FROM CT_OPERATIONMODE CT " +
						" WHERE     1 = 1 " +
						"       AND CT.MACHINENAME = :MACHINENAME " +
						"       AND CT.OPERATIONMODE = :OPERATIONMODE " ;

        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("MACHINENAME", machineName);
        bindMap.put("OPERATIONMODE", operationMode);

        List<Map<String, Object>> tpefomPolicyData = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

        if ( tpefomPolicyData == null || tpefomPolicyData.size() <= 0 )
     		throw new CustomException("POLICY-0020",machineName,operationMode);

		log.info("checkOperationModeByCT_OperationMode Ended.");
	}

	public String getBendingValue(List<Element> productList) throws CustomException
	{
		log.info("getBendingValue Started.");

		String BendingValue = "";
		String crateName = "";

		for(Element ele : productList)
		{
		    crateName =  SMessageUtil.getChildText(ele, "CRATENAME", true);
			break;
		}

		ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);

		BendingValue = CommonUtil.getValue(consumableSpecData.getUdfs(), "BENDING");

		log.info("getBendingValue Ended.");

		return BendingValue;
	}
	
	public String GetReturnAfterNodeStackForSampling(String factoryName, String nodeStack)
	{
		log.info("ReturnAfterNodeStackForSampling Function");

		String strSql = "SELECT N.NODEID " +
				"  FROM (SELECT DISTINCT " +
				"  TONODEID " +
				"  FROM ARC A, NODE N  " +
				"  WHERE 1=1  " +
				"  AND A.FROMNODEID = :NODEID " +
				"  ) NA, NODE N " +
				"  WHERE NA.TONODEID = N.NODEID ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("NODEID", nodeStack);
		bindMap.put("FACTORYNAME", factoryName);

		List<Map<String, Object>> returnAfterNodeStack = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

		if ( returnAfterNodeStack.size() > 0 )
			return returnAfterNodeStack.get(0).get("NODEID").toString();

		return "";
	}
	
	/**
	 * instant hold Lot
	 * @author swcho
	 * @since 2016.09.09
	 * @param eventInfo
	 * @param lotData
	 * @param reasonCodeType
	 * @param reasonCode
	 * @return
	 * @throws CustomException
	 */ 
	public Lot executeMultiHold(EventInfo eventInfo, Lot lotData, String reasonCode, String departMent)
			throws CustomException
	{
		// -------------------------------------------------------------------------------------------------------------------------------------------
		// Modified by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
//		Map<String,String> multiHoldudfs = new HashMap<String, String>();
//		multiHoldudfs.put("eventuserdep", departMent);
//
//		LotMultiHoldKey multiholdkey = new LotMultiHoldKey();
//		multiholdkey.setLotName(lotData.getKey().getLotName());
//		multiholdkey.setReasonCode(reasonCode);
//		
//		eventInfo.setEventName("Hold");
//
//		LotMultiHold multihold = LotServiceProxy.getLotMultiHoldService().selectByKey(multiholdkey);
//		multihold.setUdfs(multiHoldudfs);
//
//		LotServiceProxy.getLotMultiHoldService().update(multihold);
		try {
			lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), reasonCode, departMent, "AHOLD", eventInfo);
		} catch (Exception e) {
			log.warn(e);
		}
		// -------------------------------------------------------------------------------------------------------------------------------------------

		try
		{
			return lotData;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
	}
	/**
	 * instant hold Lot
	 * @author Park Jeong Su
	 * @since 2019.04.08
	 * @param eventInfo
	 * @param lotData
	 * @param reasonCodeType
	 * @param reasonCode
	 * @param resetFlag
	 * @return
	 * @throws CustomException
	 */ 
	public Lot executeMultiHold(EventInfo eventInfo, Lot lotData, String reasonCode, String departMent,String resetFlag)
			throws CustomException
	{
		// -------------------------------------------------------------------------------------------------------------------------------------------
		// Modified by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
//		Map<String,String> multiHoldudfs = new HashMap<String, String>();
//		multiHoldudfs.put("eventuserdep", departMent);
//
//		LotMultiHoldKey multiholdkey = new LotMultiHoldKey();
//		multiholdkey.setLotName(lotData.getKey().getLotName());
//		multiholdkey.setReasonCode(reasonCode);
//		
//		eventInfo.setEventName("Hold");
//
//		LotMultiHold multihold = LotServiceProxy.getLotMultiHoldService().selectByKey(multiholdkey);
//		multihold.setUdfs(multiHoldudfs);
//
//		LotServiceProxy.getLotMultiHoldService().update(multihold);
		try {
			lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), reasonCode, departMent, "AHOLD", eventInfo,resetFlag);
		} catch (Exception e) {
			log.warn(e);
		}
		// -------------------------------------------------------------------------------------------------------------------------------------------

		try
		{
			return lotData;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
	}
	
	
	
	public boolean setEventInfoForSampling(Lot lotData, String processFlowName, String processFlowVersion, String processOperationName, String sampleProcessFlowName, String sampleProcessFlowVersion,
			EventInfo eventInfo) throws CustomException
	{
		String sampleLotSql = "SELECT LASTEVENTUSER, SAMPLEPOSITION, TYPE, MANUALSAMPLEFLAG "
				+ " FROM (SELECT LA.LASTEVENTUSER, LA.SAMPLEPOSITION, :TYPE1 TYPE, :MANUALSAMPLEFLAG MANUALSAMPLEFLAG "
				+ "         FROM CT_LOTACTION LA "
				+ "        WHERE 1 = 1 "
				+ "          AND LA.LOTNAME = :LOTNAME "
				+ "          AND LA.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ "          AND LA.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ "          AND LA.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ "          AND LA.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				+ "          AND LA.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
				+ "          AND LA.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION "
				+ "          AND LA.ACTIONSTATE = :ACTIONSTATE "
				+ "          AND LA.ACTIONNAME = :ACTIONNAME "
				+ "       UNION "
				+ "       SELECT SL.LASTEVENTUSER, SL.ACTUALSAMPLEPOSITION AS SAMPLEPOSITION, :TYPE2 TYPE, SL.MANUALSAMPLEPOSITION "
				+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_SAMPLELOT SL "
				+ "        WHERE 1 = 1 "
				+ "          AND SLC.FACTORYNAME = SL.FACTORYNAME "
				+ "          AND SLC.PRODUCTSPECNAME = SL.PRODUCTSPECNAME "
				+ "          AND SLC.ECCODE = SL.ECCODE "
				+ "          AND SLC.PROCESSFLOWNAME = SL.PROCESSFLOWNAME "
				+ "          AND SLC.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION "
				+ "          AND SLC.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME "
				+ "          AND SLC.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION "
				+ "          AND SLC.FROMPROCESSOPERATIONNAME = SL.FROMPROCESSOPERATIONNAME "
				+ "          AND SL.LOTNAME = :LOTNAME "
				+ "          AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME"
				+ "          AND SL.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
				+ "          AND (SL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION OR SL.SAMPLEPROCESSFLOWVERSION = :STAR) "
				+ "          AND SL.SAMPLEFLAG = :SAMPLEFLAG "
				+ "          AND SL.SAMPLESTATE = :SAMPLESTATE "
				+ "       UNION "
				+ "       SELECT CSL.LASTEVENTUSER, CSL.ACTUALSAMPLEPOSITION AS SAMPLEPOSITION, :TYPE3 TYPE, CSL.MANUALSAMPLEPOSITION "
				+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_CORRESSAMPLELOT CSL "
				+ "        WHERE 1 = 1 "
				+ "          AND SLC.FACTORYNAME = CSL.FACTORYNAME "
				+ "          AND SLC.PRODUCTSPECNAME = CSL.PRODUCTSPECNAME "
				+ "          AND SLC.ECCODE = CSL.ECCODE "
				+ "          AND SLC.PROCESSFLOWNAME = CSL.PROCESSFLOWNAME "
				+ "          AND SLC.PROCESSFLOWVERSION = CSL.PROCESSFLOWVERSION "
				+ "          AND SLC.CORRESSAMPLEPROCESSFLOWNAME = CSL.SAMPLEPROCESSFLOWNAME "
				+ "          AND SLC.CORRESSAMPLEPROCESSFLOWVERSION = CSL.SAMPLEPROCESSFLOWVERSION "
				+ "          AND SLC.CORRESPROCESSOPERATIONNAME = CSL.FROMPROCESSOPERATIONNAME "
				+ "          AND SLC.CORRESPROCESSOPERATIONVERSION = CSL.FROMPROCESSOPERATIONVERSION "
				+ "          AND CSL.LOTNAME = :LOTNAME "
				+ "          AND CSL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
				+ "          AND (CSL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION OR CSL.FROMPROCESSOPERATIONVERSION = :STAR) "
				+ "          AND CSL.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
				+ "          AND (CSL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION OR CSL.SAMPLEPROCESSFLOWVERSION = :STAR) "
				+ "          AND CSL.SAMPLEFLAG = :SAMPLEFLAG "
				+ "          AND CSL.SAMPLESTATE = :SAMPLESTATE) "
				+ "  ORDER BY DECODE(TYPE, :TYPE1, 0, :TYPE2, 1,2) ";

		Map<String, Object> smapleLotBindSet = new HashMap<String, Object>();
		smapleLotBindSet.put("LOTNAME", lotData.getKey().getLotName());
		smapleLotBindSet.put("PROCESSFLOWNAME", processFlowName);
		smapleLotBindSet.put("PROCESSFLOWVERSION", processFlowVersion);
		smapleLotBindSet.put("PROCESSOPERATIONNAME", processOperationName);
		smapleLotBindSet.put("PROCESSOPERATIONVERSION", "00001");
		smapleLotBindSet.put("ACTIONSTATE", "Created");
		smapleLotBindSet.put("ACTIONNAME", "Sampling");
		smapleLotBindSet.put("FROMPROCESSOPERATIONNAME", processOperationName);
		smapleLotBindSet.put("FROMPROCESSOPERATIONVERSION", "00001");
		smapleLotBindSet.put("SAMPLEPROCESSFLOWNAME", sampleProcessFlowName);
		smapleLotBindSet.put("SAMPLEPROCESSFLOWVERSION", sampleProcessFlowVersion);
		smapleLotBindSet.put("SAMPLEFLAG", "Y");
		smapleLotBindSet.put("SAMPLESTATE", "Decided");
		smapleLotBindSet.put("TYPE1", "RESERVE");
		smapleLotBindSet.put("TYPE2", "AUTO");
		smapleLotBindSet.put("TYPE3", "CORRES");
		smapleLotBindSet.put("MANUALSAMPLEFLAG", "Y");
		smapleLotBindSet.put("STAR", "*");			

		List<Map<String, Object>> sampleLotSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sampleLotSql, smapleLotBindSet);

		if ( sampleLotSqlResult.size() > 0 )
		{
//			EventInfo eventInfoForSample = EventInfoUtil.makeEventInfo("Sampling", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
//			eventInfoForSample.setEventTime(eventInfo.getEventTime());
//			eventInfoForSample.setEventTimeKey(eventInfo.getEventTimeKey());
//			
			String eventUser = sampleLotSqlResult.get(0).get("LASTEVENTUSER").toString();
			String actualSlotNum = sampleLotSqlResult.get(0).get("SAMPLEPOSITION").toString();
			String type = sampleLotSqlResult.get(0).get("TYPE").toString();
			String manualSamplePosition = (String)sampleLotSqlResult.get(0).get("MANUALSAMPLEPOSITION");

 			if(sampleLotSqlResult.size() == 1)
			{
				//Only ReserveSampling by OPI
 				if(StringUtil.equals(type, "RESERVE"))
				{
 					eventInfo.setEventUser(eventUser);
 					eventInfo.setEventComment(actualSlotNum + " (By ReserveSampling)");
				}
				else
				{
					//Only SystemSampling (include CorresSampling...)
					if(!StringUtil.isNotEmpty(manualSamplePosition))
					{
						eventInfo.setEventUser("System");
						eventInfo.setEventComment(actualSlotNum + " (By SystemSampling)");
					}
				}
			}
			else
			{
				String eventUser2 = sampleLotSqlResult.get(1).get("LASTEVENTUSER").toString();
				String actualSlotNum2 = sampleLotSqlResult.get(1).get("SAMPLEPOSITION").toString();
				String type2 = sampleLotSqlResult.get(1).get("TYPE").toString();

				//ReserveSampling + SystemSampling
				if(StringUtil.equals(type, "RESERVE"))
				{
					eventInfo.setEventUser("System."+eventUser);
					eventInfo.setEventComment(actualSlotNum2 + " (by ReserveSampling and SystemSampling)");
				}
				else	
				{
					eventInfo.setEventUser("System");
					eventInfo.setEventComment(actualSlotNum2);
				}
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * @Name     getValidationRmsRecipeCheck
	 * @since    2018. 6. 19.
	 * @author   hhlee
	 * @contents RMS Recipe Validation
	 * @param doc
	 * @param eventInfo
	 * @param machineSpecData
	 * @param machineName
	 * @param machineRecipeName
	 * @param carrierName
	 * @return
	 * @throws CustomException
	 */
	public boolean getValidationRmsRecipeCheck(Document doc, EventInfo eventInfo, MachineSpec machineSpecData, String machineName, 
	        String machineRecipeName, String carrierName) throws CustomException
    {
	    boolean isValidationCheck = true;
	    /*
         usage check 
        String flag = CommonUtil.getValue(machineSpecData.getUdfs(), "RMSFLAG");                
        if (flag.equals(GenericServiceProxy.getConstantMap().Flag_Y))
        {
             RMS Check            
            if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("TRACK"))
            {
                List<Map<String, Object>> unitDatabyHierarchy = 
                        MESMachineServiceProxy.getMachineServiceUtil().getMachineDataByHierarchy(machineName, "2", 
                                StringUtil.EMPTY, StringUtil.EMPTY, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);
                
                int machineIndex = 999;
                int machineRecipeIndex = 999;
                String constructType = StringUtil.EMPTY;
                 [Regular expression]Cut by 4 characters to create an array 
                 where the 4 dots after the G indicates every nth position to split. In this case, the 4 dots indicate every 4 positions 
                 * "(?<=\\G....)" 
                 * "(?<=\\G.4)" 
                 * String[] splitStr = str.split("(?<=\\G.{" + 4 + "})");
                 * Between elements of an array | add delimiters and join them in a single string
                 * str = StringUtils.join(splitStr, "|"); 
                String[] machinerecipeSeperate = machineRecipeName.split("(?<=\\G.{" + 4 + "})");
                
                for(int i = 0; i < machinerecipeSeperate.length; i++)
                { 
                    machineIndex = 999;
                    machineRecipeIndex = 999;
                    constructType = StringUtil.EMPTY;
                    
                    for(int j = 0; j < unitDatabyHierarchy.size(); j++)
                    {
                        constructType = StringUtil.EMPTY;
                        
                        machineIndex = j;
                        machineRecipeIndex = i;
                        
                        machineName = unitDatabyHierarchy.get(machineIndex).get("UNITNAME").toString();
                        machineRecipeName = machinerecipeSeperate[machineRecipeIndex].toString();
                        
                        if(unitDatabyHierarchy.get(j).get("CONSTRUCTTYPE") != null)
                        {   
                            constructType = unitDatabyHierarchy.get(j).get("CONSTRUCTTYPE").toString();
                        }
                                
                        if(i == 0 && StringUtil.equals(constructType, GenericServiceProxy.getConstantMap().ConstructType_TRACK_SCNN))
                        {                            
                            break;
                        }
                        else if(i == 1 && StringUtil.equals(constructType, GenericServiceProxy.getConstantMap().ConstructType_TRACK_EGEY))
                        {
                            break;
                        }
                        else if(i == 2 && StringUtil.equals(constructType, GenericServiceProxy.getConstantMap().ConstructType_TRACK_TRKD))
                        {
                            break;
                        }
                        else
                        {
                            machineIndex = 999;
                            machineRecipeIndex = 999;
                            break;
                        }
                        
                    }
                    
                    if(machineIndex != 999 && machineRecipeIndex != 999)
                    {
                        if(!MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeInfo(machineName, machineRecipeName, "UNIT", "Approved"))
                        {
                            isValidationCheck = false;
                            break;
                        }
                    } 
                    else
                    {
                        isValidationCheck = false;  
                        break;
                    }
                }                
            }
            else
            {
                if(!MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeInfo(machineName, machineRecipeName, "MAIN", "Approved"))
                {
                    isValidationCheck = false;
                }                       
            }
            if(!isValidationCheck)
            {
                eventInfo.setEventComment(String.format("This Recipe[%s] has not been approved for this machine[%s].", machineRecipeName, machineName));
                MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "RMS-0006");
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("This Recipe[%s] has not been approved for this machine[%s].", machineRecipeName, machineName));                    
                
            }
        }*/
        
        return isValidationCheck;
    }    
	
	/** 
	* @author hsryu
	* @since 2018-06-21
	* @return assembleLotData 
	*/ 
	public Map<String, String> getBeforeOperName(String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion) throws CustomException
	{ 
		try
		{
			String sql = " SELECT N.FACTORYNAME, N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION,  " +
					 "   N.NODEATTRIBUTE1 AS PROCESSOPERATIONNAME, N.NODEATTRIBUTE2 AS PROCESSOPERATIONVERSION,N.NODEID " +
					 "   FROM (SELECT DISTINCT " +
					 "   FROMNODEID " +
					 "   FROM ARC A, NODE N, ( " +
					 "   SELECT N.NODEID " +
					 "   FROM NODE N " +
					 "   WHERE 1=1 " +
					 "   AND N.FACTORYNAME =:FACTORYNAME  " +
					 "   AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME  " +
					 "   AND N.PROCESSFLOWVERSION = :PROCESSFLOWVERSION  " +
					 "   AND N.NODEATTRIBUTE1 = :PROCESSOPERATIONNAME " +
					 "   AND N.NODEATTRIBUTE2 = :PROCESSOPERATIONVERSION " +
					 "   ) NL " +
					 "   WHERE 1=1 " +
					 "   AND A.TONODEID = NL.NODEID " +
					 "   ) NA, NODE N " +
					 "   WHERE NA.FROMNODEID = N.NODEID ";
			 
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("FACTORYNAME" , factoryName);
			bindMap.put("PROCESSFLOWNAME" , processFlowName);
			bindMap.put("PROCESSFLOWVERSION" , processFlowVersion);
			bindMap.put("PROCESSOPERATIONNAME" , processOperationName);
			bindMap.put("PROCESSOPERATIONVERSION" , processOperationVersion);
			
			List<Map<String, String>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if(result!=null && result.size()>0)
			{
				return result.get(0);
			}
			else{
				throw new Exception("query result = 0");
			}
		}
		catch(Exception e)
		{	
			throw new CustomException("COMMON-0001", e.getMessage());
		}
		
	}
	
	
	public ArrayList<Integer> getProductPosition(Lot lotData) throws CustomException
	{
		String sampleNodeStack = "";
		ArrayList<Integer> arrPosition = new ArrayList<Integer>();

		String sql = " SELECT POSITION "
				+ " FROM PRODUCT "
				+ "  WHERE LOTNAME = :LOTNAME "
				+ "  AND PRODUCTSTATE IN (:PRODUCTSTATE1, :PRODUCTSTATE2) "
				+ "   ORDER BY POSITION ";
		
		Map<String, Object> bindSet = new HashMap<String, Object>();
		bindSet.put("LOTNAME", lotData.getKey().getLotName());
		bindSet.put("PRODUCTSTATE1", GenericServiceProxy.getConstantMap().Prod_InProduction);
		bindSet.put("PRODUCTSTATE2", "Received");

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		
		if ( sqlResult.size() > 0 )
		{
			for(int i=0; i<sqlResult.size(); i++)
			{
				arrPosition.add(Integer.parseInt(sqlResult.get(i).get("POSITION").toString()));
			}
		}

		return arrPosition;
	}
	
	/**
	 * @Name     futureHoldLot
	 * @since    2018. 7. 4.
	 * @author   hhlee
	 * @contents Abnormal Event occurred on the Unpacker.
	 *           Lothold is processed when abnormal conditions occur.           
	 * @param lotData
	 * @param machineName
	 * @param eventInfo
	 * @param reasonCodeType
	 * @param reasonCode
	 * @throws CustomException
	 */
	public void futureHoldLot(EventInfo eventInfo, Lot lotData, String machineName, String reasonCodeType, String reasonCode) throws CustomException
    {
        eventInfo.setEventName("FutureAHold");
        
        String lotName = lotData.getKey().getLotName();
        String factoryName = lotData.getFactoryName();
        String processFlowName = lotData.getProcessFlowName();
        String processFlowVersion = lotData.getProcessFlowVersion();
        String processOperationName = lotData.getProcessOperationName();
        String processOperationVersion = lotData.getProcessOperationVersion();
        
        //Get Last Position 
        long lastPosition = Integer.parseInt(MESAlarmServiceProxy.getAlarmServiceImpl().getLastPosition(
                lotName, factoryName, processFlowName, processOperationName));
            
        //Create CT_LOTACTION
        LotAction lotActionData = new LotAction(lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, lastPosition + 1);
        
        /*lotActionData.setActionName(GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
        lotActionData.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED);
        lotActionData.setFactoryName(factoryName);
        lotActionData.setHoldCode(reasonCode);
        lotActionData.setHoldPermanentFlag(GenericServiceProxy.getConstantMap().Flag_N);
        lotActionData.setHoldType(reasonCodeType);
        lotActionData.setChangeProductRequestName(StringUtil.EMPTY);
        lotActionData.setChangeProductSpecName(StringUtil.EMPTY);
        lotActionData.setChangeECCode(StringUtil.EMPTY);
        lotActionData.setChangeProcessFlowName(StringUtil.EMPTY);
        lotActionData.setChangeProcessOperationName(StringUtil.EMPTY);
        lotActionData.setLastEventName(eventInfo.getEventName());
        lotActionData.setLastEventTime(eventInfo.getEventTime());
        lotActionData.setLastEventTimeKey(eventInfo.getEventTimeKey());
        lotActionData.setLastEventUser(eventInfo.getEventUser());
        lotActionData.setLastEventComment(eventInfo.getEventComment());
        lotActionData.setDepartment(" ");*/
        
        lotActionData.setActionName(GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD);
        lotActionData.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED);
        lotActionData.setFactoryName(factoryName);
        lotActionData.setHoldCode(reasonCode);
        lotActionData.setHoldPermanentFlag(GenericServiceProxy.getConstantMap().Flag_N);
        lotActionData.setHoldType(GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
        lotActionData.setChangeProductRequestName(StringUtil.EMPTY);
        lotActionData.setChangeProductSpecName(StringUtil.EMPTY);
        lotActionData.setChangeECCode(StringUtil.EMPTY);
        lotActionData.setChangeProcessFlowName(StringUtil.EMPTY);
        lotActionData.setChangeProcessOperationName(StringUtil.EMPTY);
        lotActionData.setLastEventName(eventInfo.getEventName());
        lotActionData.setLastEventTime(eventInfo.getEventTime());
        lotActionData.setLastEventTimeKey(eventInfo.getEventTimeKey());
        lotActionData.setLastEventUser(eventInfo.getEventUser());
        lotActionData.setLastEventComment(eventInfo.getEventComment());
        lotActionData.setDepartment(" ");

        ExtendedObjectProxy.getLotActionService().create(eventInfo, lotActionData);
        
        log.info("Lot Reserve Future Hold");
        
        //FlowSampleLot sampleLotInfo = new FlowSampleLot();
        //sampleLotInfo.setLOTNAME(lotData.getKey().getLotName());
        //sampleLotInfo.setFACTORYNAME(lotData.getFactoryName());
        //sampleLotInfo.setPRODUCTSPECNAME(lotData.getProductSpecName());
        //sampleLotInfo.setPROCESSFLOWNAME(lotData.getProcessFlowName());
        //sampleLotInfo.setPROCESSOPERATIONNAME(lotData.getProcessOperationName());
        //sampleLotInfo.setMACHINENAME(machineName);
        //sampleLotInfo.setTOPROCESSOPERATIONNAME(lotData.getProcessOperationName());
        //sampleLotInfo.setLOTSAMPLEFLAG("AHOLD");
        //sampleLotInfo.setREASONCODETYPE(reasonCodeType);
        //sampleLotInfo.setREASONCODE(reasonCode);
        ////sampleLotInfo.setLASTEVENTCOMMENT("Lot Create Product Quantity and Product Quantity is different.");
        
        //sampleLotInfo.setLASTEVENTNAME(eventInfo.getEventName());
        //sampleLotInfo.setLASTEVENTTIME(eventInfo.getEventTime());
        //sampleLotInfo.setLASTEVENTUSER(eventInfo.getEventUser());
        //sampleLotInfo.setLASTEVENTCOMMENT(eventInfo.getEventComment());
        
        //ExtendedObjectProxy.getFlowSampleLotService().create(eventInfo, sampleLotInfo);
       
        ////LotKey lotKey = new LotKey(lotData.getKey().getLotName());
        ////SetEventInfo setEventInfo = new SetEventInfo();
        ////setEventInfo.setUdfs(lotData.getUdfs());
        ////LotServiceProxy.getLotService().setEvent(lotKey, eventInfo, setEventInfo);        
        
        //log.info("Lot Future Hold");
    }	
	
	/**
     * @Name     futureHoldLot
     * @since    2018. 7. 4.
     * @author   hhlee
     * @contents Abnormal Event occurred on the Unpacker.
     *           Lothold is processed when abnormal conditions occur.           
     * @param lotData
     * @param machineName
     * @param eventInfo
     * @param reasonCodeType
     * @param reasonCode
     * @throws CustomException
     */
    public void futureHoldLot(EventInfo eventInfo, Lot lotData, String machineName, String reasonCodeType, String reasonCode, String machineDepartment) throws CustomException
    {
        eventInfo.setEventName("FutureAHold");
        
        String lotName = lotData.getKey().getLotName();
        String factoryName = lotData.getFactoryName();
        String processFlowName = lotData.getProcessFlowName();
        String processFlowVersion = lotData.getProcessFlowVersion();
        String processOperationName = lotData.getProcessOperationName();
        String processOperationVersion = lotData.getProcessOperationVersion();
        
        //Get Last Position 
        long lastPosition = Integer.parseInt(MESAlarmServiceProxy.getAlarmServiceImpl().getLastPosition(
                lotName, factoryName, processFlowName, processOperationName));
            
        //Create CT_LOTACTION
        LotAction lotActionData = new LotAction(lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, lastPosition + 1);
        
        /*lotActionData.setActionName(GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
        lotActionData.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED);
        lotActionData.setFactoryName(factoryName);
        lotActionData.setHoldCode(reasonCode);
        lotActionData.setHoldPermanentFlag(GenericServiceProxy.getConstantMap().Flag_N);
        lotActionData.setHoldType(reasonCodeType);
        lotActionData.setChangeProductRequestName(StringUtil.EMPTY);
        lotActionData.setChangeProductSpecName(StringUtil.EMPTY);
        lotActionData.setChangeECCode(StringUtil.EMPTY);
        lotActionData.setChangeProcessFlowName(StringUtil.EMPTY);
        lotActionData.setChangeProcessOperationName(StringUtil.EMPTY);
        lotActionData.setLastEventName(eventInfo.getEventName());
        lotActionData.setLastEventTime(eventInfo.getEventTime());
        lotActionData.setLastEventTimeKey(eventInfo.getEventTimeKey());
        lotActionData.setLastEventUser(eventInfo.getEventUser());
        lotActionData.setLastEventComment(eventInfo.getEventComment());
        lotActionData.setDepartment(" ");*/
        
        lotActionData.setActionName(GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD);
        lotActionData.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED);
        lotActionData.setFactoryName(factoryName);
        lotActionData.setHoldCode(reasonCode);
        lotActionData.setHoldPermanentFlag(GenericServiceProxy.getConstantMap().Flag_N);
        lotActionData.setHoldType(GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
        lotActionData.setChangeProductRequestName(StringUtil.EMPTY);
        lotActionData.setChangeProductSpecName(StringUtil.EMPTY);
        lotActionData.setChangeECCode(StringUtil.EMPTY);
        lotActionData.setChangeProcessFlowName(StringUtil.EMPTY);
        lotActionData.setChangeProcessOperationName(StringUtil.EMPTY);
        lotActionData.setLastEventName(eventInfo.getEventName());
        lotActionData.setLastEventTime(eventInfo.getEventTime());
        lotActionData.setLastEventTimeKey(eventInfo.getEventTimeKey());
        lotActionData.setLastEventUser(eventInfo.getEventUser());
        lotActionData.setLastEventComment(eventInfo.getEventComment());
        lotActionData.setDepartment(StringUtil.isEmpty(machineDepartment.trim()) ? " " : machineDepartment);

        ExtendedObjectProxy.getLotActionService().create(eventInfo, lotActionData);
        
        log.info("Lot Reserve Future Hold");
        
        //FlowSampleLot sampleLotInfo = new FlowSampleLot();
        //sampleLotInfo.setLOTNAME(lotData.getKey().getLotName());
        //sampleLotInfo.setFACTORYNAME(lotData.getFactoryName());
        //sampleLotInfo.setPRODUCTSPECNAME(lotData.getProductSpecName());
        //sampleLotInfo.setPROCESSFLOWNAME(lotData.getProcessFlowName());
        //sampleLotInfo.setPROCESSOPERATIONNAME(lotData.getProcessOperationName());
        //sampleLotInfo.setMACHINENAME(machineName);
        //sampleLotInfo.setTOPROCESSOPERATIONNAME(lotData.getProcessOperationName());
        //sampleLotInfo.setLOTSAMPLEFLAG("AHOLD");
        //sampleLotInfo.setREASONCODETYPE(reasonCodeType);
        //sampleLotInfo.setREASONCODE(reasonCode);
        ////sampleLotInfo.setLASTEVENTCOMMENT("Lot Create Product Quantity and Product Quantity is different.");
        
        //sampleLotInfo.setLASTEVENTNAME(eventInfo.getEventName());
        //sampleLotInfo.setLASTEVENTTIME(eventInfo.getEventTime());
        //sampleLotInfo.setLASTEVENTUSER(eventInfo.getEventUser());
        //sampleLotInfo.setLASTEVENTCOMMENT(eventInfo.getEventComment());
        
        //ExtendedObjectProxy.getFlowSampleLotService().create(eventInfo, sampleLotInfo);
       
        ////LotKey lotKey = new LotKey(lotData.getKey().getLotName());
        ////SetEventInfo setEventInfo = new SetEventInfo();
        ////setEventInfo.setUdfs(lotData.getUdfs());
        ////LotServiceProxy.getLotService().setEvent(lotKey, eventInfo, setEventInfo);        
        
        //log.info("Lot Future Hold");
    }   
	
	public Port searchLoaderPort(String machineName) throws CustomException
	{
		try
		{
			List<Port> result = PortServiceProxy.getPortService().select("machineName = ? AND portType IN (?, ?, ?)", new Object[] {machineName, "PB", "PL", "PS"});
			
			return result.get(0);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, "");
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
	}
	
	public Port searchUnloaderPort(Port portData) throws CustomException
	{
		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB"))
		{
			return portData;
		}
		else
		{
			try
			{
				List<Port> result = PortServiceProxy.getPortService().select("machineName = ? AND portType IN (?, ?)", new Object[] {portData.getKey().getMachineName(), "PU", "PS"});
				
				return result.get(0);
			}
			catch (NotFoundSignal ne)
			{
				throw new CustomException("PORT-9001", portData.getKey().getMachineName(), "");
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("PORT-9999", fe.getMessage());
			}
		}
	}

	public void deassignCarrier(EventInfo eventInfo, Lot lotData)
			throws CustomException {
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil()
				.getDurableData(lotData.getCarrierName());

		if (StringUtils.equals(carrierData.getDurableState(), "InUse")) {
			eventInfo.setEventName("DeassignCarrier");

			List<ProductU> productUSequence = MESLotServiceProxy
					.getLotInfoUtil().getAllProductUSequence(lotData);

			DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy
					.getLotInfoUtil().deassignCarrierInfo(lotData, carrierData,
							productUSequence);

			LotServiceProxy.getLotService().deassignCarrier(lotData.getKey(),
					eventInfo, deassignCarrierInfo);
		}
	}
	
	public Document writeTrackInRequest(Document doc, String lotName, String machineName, String portName, String recipeName)
			throws CustomException
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackInLot");
			//SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
			
			//Element eleBody = SMessageUtil.getBodyElement(doc);

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
			
			Element element4 = new Element("RECIPENAME");
			element4.setText(recipeName);
			eleBodyTemp.addContent(element4);
			
			Element element5 = new Element("AUTOFLAG");
			element5.setText("Y");
			eleBodyTemp.addContent(element5);
			
			//overwrite
			doc.getRootElement().addContent(eleBodyTemp);
			
			return doc;
		}
	
	/**
	 * @author yudan
	 * @since 2017.04.14
	 * @param doc
	 * @throws CustomException 
	 */
	public List<Element> makeNewProductList(String machineName, List<Element> productList, EventInfo eventInfo) throws CustomException
	{
		/* 20181128, hhlee, EventTime Sync */
	    EventInfo makeNewProducteventInfo = EventInfoUtil.makeEventInfo("", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
	    makeNewProducteventInfo.setEventTime(eventInfo.getEventTime());
		
		List<Element> newProductList = new ArrayList<Element>();
		
	    for (Element product : productList)
	    {   												
			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			String vcrProductName = SMessageUtil.getChildText(product, "VCRPRODUCTNAME", false);
			
			String query = "MACHINENAME = :machineName AND PRODUCTNAME = :productName";
			Object[] bindList = new Object[] {machineName, productName};
			
			List<Product> result = new ArrayList();
			try {
				 result = ProductServiceProxy.getProductService().select(query, bindList);
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			ProductKey productkey = new ProductKey();
			productkey.setProductName(productName);
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Product sProductData = ProductServiceProxy.getProductService().selectByKey(productkey);
			Product sProductData = ProductServiceProxy.getProductService().selectByKeyForUpdate(productkey);
			
			if(result.size() > 0 &&
			   !(vcrProductName.equals("") || vcrProductName == null) &&
			   !(vcrProductName.substring(0, 5).equals("ERROR") || vcrProductName.substring(0, 4).equals("SKIP")) &&
			   !productName.equals(vcrProductName))
			{					
				//validate
				this.checkExistProduct(vcrProductName);
				
				sProductData.setDestinationProductName(vcrProductName);
				ProductServiceProxy.getProductService().update(sProductData);

				RecreateInfo RecreateInfo = new RecreateInfo();
				Map<String, String> prdUdfs = sProductData.getUdfs();	
				prdUdfs.put("VCRPRODUCTNAME", vcrProductName);
					
				RecreateInfo.setNewProductName(vcrProductName);
				RecreateInfo.setProductionType(sProductData.getProductionType());
				RecreateInfo.setProductSpecName(sProductData.getProductSpecName());
				RecreateInfo.setProductSpecVersion("00001");
				RecreateInfo.setProductRequestName(sProductData.getProductRequestName());
				RecreateInfo.setLotName(sProductData.getLotName());
				RecreateInfo.setPosition(Long.valueOf(SMessageUtil.getChildText(product, "POSITION", true)));
				RecreateInfo.setProductType(sProductData.getProductType());
				RecreateInfo.setSubProductType(sProductData.getSubProductType());
				RecreateInfo.setProductGrade(sProductData.getProductGrade());
				RecreateInfo.setAreaName(sProductData.getAreaName());
				RecreateInfo.setProcessFlowName(sProductData.getProcessFlowName());
				RecreateInfo.setProcessFlowVersion(sProductData.getProcessFlowVersion());
				RecreateInfo.setProcessOperationName(sProductData.getProcessOperationName());
				RecreateInfo.setProcessOperationVersion(sProductData.getProcessOperationVersion());
				RecreateInfo.setNodeStack(sProductData.getNodeStack());
				RecreateInfo.setCarrierName(sProductData.getCarrierName());	
				RecreateInfo.setDueDate(sProductData.getDueDate());					
				RecreateInfo.setPriority(sProductData.getPriority());
				RecreateInfo.setSubProductQuantity1(sProductData.getSubProductQuantity1());
				RecreateInfo.setNewUdfs(prdUdfs);
				
				/* 20181128, hhlee, EventTime Sync */
				makeNewProducteventInfo.setEventName("Recreate");
				ProductServiceProxy.getProductService().recreate(sProductData.getKey(), makeNewProducteventInfo, RecreateInfo);

				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Product dProductData = MESProductServiceProxy.getProductServiceUtil().getProductData(vcrProductName);
				Product dProductData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(vcrProductName));
				
				/* 20181128, hhlee, EventTime Sync */
				setPrdInfo(sProductData,dProductData,makeNewProducteventInfo);
				
				//Make productList
				Element productElement = new Element("PRODUCT");
				
				Element productNameElement = new Element("PRODUCTNAME");
				productNameElement.setText(vcrProductName);
				productElement.addContent(productNameElement);
				
				Element positionElement = new Element("POSITION");
				positionElement.setText(SMessageUtil.getChildText(product, "POSITION", true));
				productElement.addContent(positionElement);				
				
				newProductList.add(productElement);		
			}
			else 
			{
				//Make productList
				Element productElement = new Element("PRODUCT");
				
				Element productNameElement = new Element("PRODUCTNAME");
				productNameElement.setText(productName);
				productElement.addContent(productNameElement);
				
				Element positionElement = new Element("POSITION");
				positionElement.setText(SMessageUtil.getChildText(product, "POSITION", true));
				productElement.addContent(positionElement);				
				
				newProductList.add(productElement);	
			}					
	    }
	    
	    productList = newProductList;
	    
	    return productList;
	}
	
	/**
	 * Name : checkExistProduct
	 * Desc : check existence
	 * Author : yudan
	 * Date : 2017.04.14
	 */
	public void checkExistProduct(String productName) throws CustomException
	{
		String condition = "PRODUCTNAME = ?";
					
		Object[] bindSet = new Object[] {productName};
					
		try
		{
		    List <Product> sqlResult = ProductServiceProxy.getProductService().select(condition, bindSet);
						
		    if(sqlResult.size() > 0)
		    {
			     throw new CustomException("PRODUCT-9002", productName);
		    }
		}
		catch (NotFoundSignal ex)
		{
			return;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-0001", fe.getMessage());
		}
	}	
	
	/**
	* Name : setPrdInfo
	* Desc : setPrdInfo
	* Author : yudan
	* Date : 2017.04.18
	*/
	public void setPrdInfo(Product prdData,Product newPrdData, EventInfo eventInfo)
	{			
		newPrdData.setSubProductQuantity(prdData.getSubProductQuantity());
		newPrdData.setSubProductQuantity1(prdData.getSubProductQuantity1());
		newPrdData.setSubProductUnitQuantity1(prdData.getSubProductUnitQuantity1());
		newPrdData.setCreateSubProductQuantity(prdData.getCreateSubProductQuantity());
		newPrdData.setCreateSubProductQuantity1(prdData.getCreateSubProductQuantity1());			
		
		ProductServiceProxy.getProductService().update(newPrdData);			
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		String condition = "where productName = ?" + "and timekey = ? ";
//		Object[] bindSet = new Object[] {newPrdData.getKey().getProductName(),eventInfo.getEventTimeKey()};
//		ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().select(condition, bindSet).get(0);
		ProductHistoryKey productHistoryKey = new ProductHistoryKey();
		productHistoryKey.setProductName(newPrdData.getKey().getProductName());
		productHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
		ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
		
		productHistory.setSubProductQuantity(prdData.getSubProductQuantity());
		productHistory.setSubProductQuantity1(prdData.getSubProductQuantity1());
		productHistory.setSubProductUnitQuantity1(prdData.getSubProductUnitQuantity1());
				
		ProductServiceProxy.getProductHistoryService().update(productHistory);
	}
	
	public void TrackInLotForUPK(Document doc, EventInfo eventInfo) throws CustomException
	{		
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME",true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME",false);
		String autoFlag = SMessageUtil.getBodyItemValue(doc, "AUTOFLAG", false);
		String maskBarcodeIDList = SMessageUtil.getBodyItemValue(doc,"MASKBARCODEIDLIST", false);
		String maskSpec = SMessageUtil.getBodyItemValue(doc, "MASKSPEC", false);

		eventInfo = EventInfoUtil.makeEventInfo("", eventInfo.getEventUser(),eventInfo.getEventComment(), null, null);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Machine eqpData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

		String machineGroupName = null;

		// add mask info
		// 2017.10.08 by yuhonghao
		if (machineSpecData.getUdfs().get("CONSTRUCTTYPE").equals("EXP")) {
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			List<Product> sProductList = LotServiceProxy.getLotService().allProducts(lotData.getKey().getLotName());
			List<Product> sProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());

			for (Product sProductInfo : sProductList) {
				Map<String, String> productUdfs = sProductInfo.getUdfs();
				productUdfs.put("MASKBARCODEIDLIST", maskBarcodeIDList);
				productUdfs.put("MASKSPEC", maskSpec);
				kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				setEventInfo.setUdfs(productUdfs);
				ProductServiceProxy.getProductService().update(sProductInfo);
				EventInfo producteventInfo = EventInfoUtil.makeEventInfo(
						"CreateMaskInfo", eventInfo.getEventUser(), eventInfo.getEventComment(),
						null, null);
				ProductServiceProxy.getProductService().setEvent(sProductInfo.getKey(), producteventInfo, setEventInfo);
			}
		}

		// Validation LotGrade
		// 2016.03.18 by hwlee89
		MESLotServiceProxy.getLotServiceUtil().validationLotGrade(lotData);

		if (!autoFlag.equals("Y")) {
			if (!lotData.getCarrierName().isEmpty()) {
				// Update 2016.03.22 by hwlee89
				// CommonValidation.checkDurableDirtyState(lotData.gefortCarrierName());
				CommonValidation.CheckDurableHoldState(lotData.getCarrierName());
			}
		}

		Map<String, String> lotUdfs = lotData.getUdfs();

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(
				machineName, portName);

		// Recipe
		if(StringUtil.isEmpty(recipeName))
		{
			recipeName = MESRecipeServiceProxy.getRecipeServiceUtil()
					.getMachineRecipe(lotData.getFactoryName(),
							lotData.getProductSpecName(),
							lotData.getProcessFlowName(),
							lotData.getProcessOperationName(), machineName,
							lotData.getUdfs().get("ECCODE"));
		}	

		// Recipe Validation (check if POSMachine recipe matches any recipes of
		// Mask in TRK machine)
		if (!MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData)
				.getProcessFlowType().equals("MQC")) {
			MESDurableServiceProxy.getDurableServiceUtil()
			.validateRecipeMapping(machineName, recipeName,
					lotName);
		}

		//20180525, kyjung, MQC
		MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotName);

		// 20180504, kyjung, QTime
		MESProductServiceProxy.getProductServiceImpl().checkQTime(lotName);
		
		try
		{
			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
			machineGroupName = CommonUtil.getValue(operationData.getUdfs(), "MACHINEGROUPNAME");
		}
		catch (Exception ex)
		{
		}

		List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		// 20180504, kyjung, QTime
		for (Product productData : productDataList) 
		{
			MESProductServiceProxy.getProductServiceImpl().ExitedQTime(eventInfo, productData, "TrackIn");
		}

		// TrackIn
		List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotName);
		
		// Added by smkang on 2018.09.03 - According to EDO's request, PortName should be updated.
		for (ProductC productC : productCSequence) 
		{
			productC.getUdfs().put("PORTNAME", portName);
		}

		lotUdfs.put("PORTNAME", portData.getKey().getPortName());
		lotUdfs.put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
		lotUdfs.put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));

		MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, recipeName, productCSequence,lotUdfs);

		String carrierName = lotData.getCarrierName();
		
		eventInfo.setEventName("TrackIn");
		Lot trackInLot = MESLotServiceProxy.getLotServiceImpl().trackInLot(eventInfo, lotData, makeLoggedInInfo);

		// 20180612, kyjung, Recipe Idle Time
		if(!StringUtil.isEmpty(carrierName))
		{
			MESProductServiceProxy.getProductServiceImpl().firstFlagRecipeIdleTimeLot
			       (trackInLot.getKey().getLotName(), 
					carrierName, 
					machineName, 
					recipeName, 
					trackInLot.getProductSpecName(), 
					trackInLot.getProcessOperationName(), 
					eventInfo);
		}

		trackInLot = MESLotServiceProxy.getLotServiceUtil().executeSampleLot(trackInLot);

		if (!lotData.getCarrierName().isEmpty()) {
			// IncrementTimeUsed For Carrier by hwlee89
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trackInLot.getCarrierName());
			Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(trackInLot.getCarrierName()));

			IncrementTimeUsedInfo incrementTimeUsedInfo = MESDurableServiceProxy.getDurableInfoUtil().incrementTimeUsedInfo(durableData, 1);
			incrementTimeUsedInfo.getUdfs().put("MACHINENAME", machineName);
			incrementTimeUsedInfo.getUdfs().put("PORTNAME", portName);

			eventInfo.setEventName("Use");
			durableData = MESDurableServiceProxy.getDurableServiceImpl().incrementTimeUsed(durableData, incrementTimeUsedInfo,eventInfo);
		}

		if (StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PL")&& StringUtils.isNotEmpty(trackInLot.getCarrierName())) 
		{
			try 
			{
				deassignCarrier(eventInfo, trackInLot);
			} 
			catch (CustomException ce) 
			{
				log.error("Deassign failed");
			}
		}

		// 150117 by swcho : success then report to FMC
		try 
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
		} 
		catch (Exception ex) 
		{
			log.warn("FMC Report Failed!");
		}
	}
	
	public Document writeTrackOutRequest(Document doc, String lotName, String machineName, String portName, String carrierName)
			throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackOutLot");
		
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
		
		Element element4 = new Element("CARRIERNAME");
		element4.setText(carrierName);
		eleBodyTemp.addContent(element4);
		
		Element elementPL = new Element("PRODUCTLIST");
		try
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
			
			for (Product productData : productList)
			{
				Element elementP = new Element("PRODUCT");
				{
					Element elementS1 = new Element("PRODUCTNAME");
					elementS1.setText(productData.getKey().getProductName());
					elementP.addContent(elementS1);
					
					Element elementS2 = new Element("POSITION");
					elementS2.setText(String.valueOf(productData.getPosition()));
					elementP.addContent(elementS2);
					
					Element elementS3 = new Element("PRODUCTJUDGE");
					elementS3.setText(productData.getProductGrade());
					elementP.addContent(elementS3);
					
					//2018.12.25_hsryu_Insert ProcessingInfo.
					Element elementS4 = new Element("PROCESSINGINFO");
					elementS4.setText("P");
					elementP.addContent(elementS4);

				}
				elementPL.addContent(elementP);
			}
		}
		catch (NotFoundSignal e) 
	    {
	    	throw new CustomException("PRODUCT-9001", "");	
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());	
	    }
		eleBodyTemp.addContent(elementPL);
		
		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);
		
		return doc;
	}
	
	public Document writeTrackOutRequest(Document doc, String lotName, String machineName, String portName, String carrierName, String productRecipeName)
            throws CustomException
    {
        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackOutLot");
        //SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
        
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
        
        Element element4 = new Element("CARRIERNAME");
        element4.setText(carrierName);
        eleBodyTemp.addContent(element4);
        
        Element element5 = new Element("PRODUCTRECIPENAME");
        element5.setText(productRecipeName);
        eleBodyTemp.addContent(element5);
             
        /* 20190123, hhlee, modify, add ProcessedOperation update complete flag ==>> */
        Element element6 = new Element("PROCESSEDOPERATIONCOMPLETEFLAG");
        element6.setText(GenericServiceProxy.getConstantMap().Flag_Y);
        eleBodyTemp.addContent(element6);
        /* <<== 20190123, hhlee, modify, add ProcessedOperation update complete flag */
        
        Element elementPL = new Element("PRODUCTLIST");
        try
        {
            List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
            
            for (Product productData : productList)
            {
                Element elementP = new Element("PRODUCT");
                {
                    Element elementS1 = new Element("PRODUCTNAME");
                    elementS1.setText(productData.getKey().getProductName());
                    elementP.addContent(elementS1);
                    
                    Element elementS2 = new Element("POSITION");
                    elementS2.setText(String.valueOf(productData.getPosition()));
                    elementP.addContent(elementS2);
                    
                    Element elementS3 = new Element("PRODUCTJUDGE");
                    elementS3.setText(productData.getProductGrade());
                    elementP.addContent(elementS3);
                    
                }
                elementPL.addContent(elementP);
            }
        }
        catch (NotFoundSignal e) 
        {
            throw new CustomException("PRODUCT-9001", "");  
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("PRODUCT-9999", fe.getMessage()); 
        }
        eleBodyTemp.addContent(elementPL);
        
        //overwrite
        doc.getRootElement().addContent(eleBodyTemp);
        
        return doc;
    }
	
	public void TrackOutLotForUPK(Document doc, EventInfo eventInfo) throws CustomException
	{
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String lotJudge = SMessageUtil.getBodyItemValue(doc, "LOTJUDGE", false);		

		/* 20181001, hhlee, add, machine Recipe Name ==>> */
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "PRODUCTRECIPENAME", false);
		/* <<== 20181001, hhlee, add, machine Recipe Name */
		
		/* 20190123, hhlee, modify, add ProcessedOperation update complete flag ==>> */
		String processedOperationCompleteFlag = SMessageUtil.getBodyItemValue(doc, "PROCESSEDOPERATIONCOMPLETEFLAG", false);
        /* <<== 20190123, hhlee, modify, add ProcessedOperation update complete flag */
				
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		/* 20180920, hhlee, add, Modify EventName, EventComment ==>> */
		EventInfo trackOutLotEventInfo = EventInfoUtil.makeEventInfo("", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);;
        /* <<== 20180920, hhlee, add, Modify EventName, EventComment */
		
		List<Product> allProduct = null;
		ProductKey productkey = new ProductKey();
		SetMaterialLocationInfo MaterialLocationInfo=new SetMaterialLocationInfo();
		EventInfo setMaterialLocationeventInfo = EventInfoUtil.makeEventInfo("SetMaterialLocation", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		
		/* 20181128, hhlee, EventTime Sync */
		setMaterialLocationeventInfo.setEventTime(trackOutLotEventInfo.getEventTime());
		
		for (Element product : productList )
		{   
			productkey.setProductName(SMessageUtil.getChildText(product, "PRODUCTNAME", true));
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Product productData = ProductServiceProxy.getProductService().selectByKey(productkey);
			Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(productkey);
			
			if(!productData.getMaterialLocationName().equals(""))
			{
				MaterialLocationInfo.setMaterialLocationName("");
				ProductServiceProxy.getProductService().update(productData);
				MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(setMaterialLocationeventInfo, productData, MaterialLocationInfo);
			}
			
			/* 20180911, hhlee, Modify, ==>> */
            //MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(product, machineName);
			/* 20190123, hhlee, modify, add ProcessedOperation update complete flag ==>> */
			//MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, product, machineName);
	        if(!StringUtil.equals(processedOperationCompleteFlag, GenericServiceProxy.getConstantMap().Flag_Y))
			{
			    MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, product, machineName);
			}
	        /* <<== 20190123, hhlee, modify, add ProcessedOperation update complete flag */
            /* <<== 20180911, hhlee, Modify, */
		}
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		allProduct = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);
		allProduct = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotName);
		
		//for common
		eventInfo = EventInfoUtil.makeEventInfo("", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		
		/* 20181128, hhlee, EventTime Sync */
		eventInfo.setEventTime(trackOutLotEventInfo.getEventTime());
		
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		if(CommonUtil.getValue(machineSpecData.getUdfs(), "VCRCHECKFLAG").equals("Y"))
		{
			productList = MESLotServiceProxy.getLotServiceUtil().makeNewProductList(machineName,productList, eventInfo);
        }
		
		// Validation CST Hold
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(carrierName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
		CommonValidation.CheckDurableHoldState(durableData);
		
        //String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
		
		String logicalSlotMap = "";
        
        for(int i=0; i<durableData.getCapacity();i++)
        {
        	logicalSlotMap += "O";
        }		
        
		// -----------------------------------------------------------------------------------------------------------------------------------------------------------
		// Added by hsryu on 2018.09.12 - Hold check

		// check yield. if yield is lack, reserve AHold in FutureAction.
		MESLotServiceProxy.getLotServiceImpl().checkYield(lotData.getKey().getLotName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), eventInfo);
	
		// if currently in the sampleFlow & lastOperation, check SampleOutHoldFlag and reserve AHold in FutureAction. 
		MESLotServiceProxy.getLotServiceImpl().checkSampleOutHoldFlag(lotData, eventInfo);

		// check releaseLot Hold(after UPK, hold or notHold)
		MESLotServiceProxy.getLotServiceUtil().checkReserveLotHold(machineName, lotData, eventInfo);

		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		
		Lot trackOutLot = MESLotServiceProxy.getLotServiceUtil().getTrackOutLot(eventInfo, carrierName, lotName, productList);
		if (trackOutLot == null)
		{
			throw new CustomException("LOT-XXXX", carrierName);
		}
		
		// -----------------------------------------------------------------------------------------------------------------------------------------------------------
		// Added by smkang on 2018.08.11 - Update MachineIdleTime or MQCCondition.
		// Deleted by smkang on 2018.11.15 - According to Honewei's request, LastRunTime will be updated at GlassOutIndexer and GlassInIndexer time.
//		try {
//			String condition = "SUPERMACHINENAME = ? AND DETAILMACHINETYPE = ?";
//			Object[] bindSet = new Object[] {machineName, "UNIT"};
//			List<MachineSpec> unitSpecList = MachineServiceProxy.getMachineSpecService().select(condition, bindSet);
//			
//			for (MachineSpec unitSpec : unitSpecList) {
//				MESMachineServiceProxy.getMachineServiceImpl().updateMachineIdleTimeRunInfo(machineName, unitSpec.getKey().getMachineName(), trackOutLot, eventInfo);
//			}
//		} catch (Exception e) {
//			log.warn(e);
//		}
		// -----------------------------------------------------------------------------------------------------------------------------------------------------------
						
		MESLotServiceProxy.getLotServiceUtil().validateAssembly(trackOutLot, productList);
		
		//refined Lot logged in
		// Deleted by smkang on 2018.05.23 - Unnecessary to keep Lot data.
//		Lot beforeTrackOutLot = (Lot) ObjectUtil.copyTo(trackOutLot);
		
		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(productList, machineName);
		//productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequenceProcessingInfo(productPGSRCSequence, "");

		// Auto-judge lotGrade
		// 2018.11.01, hsryu, Delete DecideLotJudge
		//lotJudge = MESLotServiceProxy.getLotServiceUtil().decideLotJudge(trackOutLot, lotJudge, productPGSRCSequence);
		
		/**
		 * ToDo : Check Q-Time 
		// Auto-Rework Case#1 : Q-time
		ExtendedObjectProxy.getQTimeService().monitorQTime(eventInfo, trackOutLot.getKey().getLotName());
		MESLotServiceProxy.getLotServiceUtil().doQTimeAction(doc, trackOutLot.getKey().getLotName());
		ExtendedObjectProxy.getQTimeService().moveInQTime(eventInfo, trackOutLot.getKey().getLotName(), trackOutLot.getFactoryName(), trackOutLot.getProductSpecName(),
				trackOutLot.getProcessFlowName(), trackOutLot.getProcessOperationName());
		*/
		
		String beforeProcessFlowName = trackOutLot.getProcessFlowName();
		String beforeProcessFlowVersion = trackOutLot.getProcessFlowVersion();
		String beforeProcessOperationName = trackOutLot.getProcessOperationName();
		String beforeProcessOperationVersion = trackOutLot.getProcessOperationVersion();
		
		// Decide Sampling
   		String decideSampleNodeStack = "";
		
   		// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		// Except Sample : SuperLot
   		// Modified by smkang on 2018.07.02 - According to EDO's request, although SuperLotFlag is Y, reserved sampling should be executed.
//		if ( StringUtil.equals(trackOutLot.getUdfs().get("SUPERLOTFLAG"), "N") ||
//			 StringUtil.isEmpty(trackOutLot.getUdfs().get("SUPERLOTFLAG")) )
//		{
//			// Decide Sampling 
//			decideSampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getDecideSample(doc, eventInfo, trackOutLot);
//			//eventLog.info("Decided Sample Flag : " + decidedsampleFlag);
//		}
//		
//		String sampleFlowName = MESLotServiceProxy.getLotServiceUtil().checkSampleReserveInfo(lotName, beforeProcessFlowName, beforeProcessFlowVersion, beforeProcessOperationName, eventInfo);
//		
//		if ( (StringUtil.equals(trackOutLot.getUdfs().get("SUPERLOTFLAG"), "N") || StringUtil.isEmpty(trackOutLot.getUdfs().get("SUPERLOTFLAG")) ) 
//				&& StringUtil.isNotEmpty(sampleFlowName))
//		{
//			decideSampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getSampleNodeStack(sampleFlowName);
//		}
   		
   		boolean aHoldFlag = false;
   		
   		aHoldFlag = MESLotServiceProxy.getLotServiceUtil().isExistAhold(lotName, beforeProcessFlowName, beforeProcessOperationName);

   		String superLotFlag = lotData.getUdfs().get("SUPERLOTFLAG");
   		if (StringUtils.isEmpty(superLotFlag) || !superLotFlag.equals("Y"))
   			MESLotServiceProxy.getLotServiceUtil().getDecideSample(doc, eventInfo, trackOutLot);
   		
   		if(!aHoldFlag)
   		{
   	   		String sampleFlowName = MESLotServiceProxy.getLotServiceUtil().checkReservedSamplingInfo(lotName, beforeProcessFlowName, beforeProcessFlowVersion, beforeProcessOperationName, eventInfo);
   			
   	   		if (StringUtils.isEmpty(sampleFlowName) && (StringUtils.isEmpty(superLotFlag) || superLotFlag.equals("N")))
   	   			sampleFlowName = MESLotServiceProxy.getLotServiceUtil().checkNormalSamplingInfo(lotName, beforeProcessOperationName, eventInfo);
   			
   			if (StringUtil.isNotEmpty(sampleFlowName))
   				decideSampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getOperFirstNodeStack(sampleFlowName);
   		}
   		// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
   		
		/**
		 * ToDo : MQC 
		// MQC
		MESLotServiceProxy.getMQCLotServiceUtil().processMQC(eventInfo, trackOutLot, machineName, productList);
		MESLotServiceProxy.getMQCotServiceUtil().MQCforPU(eventInfo, trackOutLot, productList, machineName, portData, carrierName);
		*/
   		
		//20180504, kyjung, QTime
		eventInfo = EventInfoUtil.makeEventInfo("", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		List<Map<String, Object>> queuePolicyData = null;
		
		/* 20181128, hhlee, EventTime Sync */
        eventInfo.setEventTime(trackOutLotEventInfo.getEventTime());
		
		Map<String, Object> qTimeTPEFOPolicyData = MESProductServiceProxy.getProductServiceImpl().checkPriorityPolicy(trackOutLot);

		if(qTimeTPEFOPolicyData != null)
		{
			queuePolicyData = MESProductServiceProxy.getProductServiceImpl().checkQTimePolicy(qTimeTPEFOPolicyData);
			
			if(queuePolicyData != null)
			{
				for (Element product : productList )
				{   
					String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
					
					MESProductServiceProxy.getProductServiceImpl().EnteredQTime(eventInfo, eventInfo1, productName, queuePolicyData);
				}
			}
		}
		
		/* 20180920, hhlee, add, Modify EventName, EventComment ==>> */
        eventInfo.setEventName(trackOutLotEventInfo.getEventName());
        eventInfo.setEventComment(trackOutLotEventInfo.getEventComment());
        /* <<== 20180920, hhlee, add, Modify EventName, EventComment */
		
        Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, trackOutLot, portData, 
                                                carrierName, lotJudge, machineName, "",
                                                productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, aHoldFlag, null);
        
        /* Array 20180807, Add [Process Flag Update] ==>> */            
        MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, afterTrackOutLot, logicalSlotMap, true);
        /* <<== Array 20180807, Add [Process Flag Update] */
        
		// 2019.05.31_hsryu_Add Logic. Check BHold ! 
        afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().checkBHoldAndOperHold(afterTrackOutLot.getKey().getLotName(), eventInfo);
 		
/* 		if(firstHoldFlag)
		{
 			eventInfo.setEventName("FirstLotHold");
			eventInfo.setCheckTimekeyValidation(false);
			eventInfo.setReasonCode("FLHD");
			eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
			eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
			
			Map<String, String> udfs = new HashMap<String, String>();
			udfs = afterTrackOutLot.getUdfs();
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(afterTrackOutLot);
			
			MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, udfs);

			afterTrackOutLot = LotServiceProxy.getLotService().makeOnHold(afterTrackOutLot.getKey(), eventInfo, makeOnHoldInfo);
			
			// -------------------------------------------------------------------------------------------------------------------------------------------
			// Modified by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
//			LotMultiHoldKey multiholdkey = new LotMultiHoldKey();
//			multiholdkey.setLotName(lotName);
//			multiholdkey.setReasonCode("FLHD");
//			
//			LotMultiHold multihold = LotServiceProxy.getLotMultiHoldService().selectByKey(multiholdkey);
//			multihold.getUdfs().put("eventuserdep", afterTrackOutLot.getUdfs().get("DEPARTMENTNAME").toString());
//					
//			LotServiceProxy.getLotMultiHoldService().update(multihold);
			try {
				MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotName, "FLHD", afterTrackOutLot.getUdfs().get("DEPARTMENTNAME"), "AHOLD", eventInfo);
			} catch (Exception e) {
				log.warn(e);
			}
			// -------------------------------------------------------------------------------------------------------------------------------------------
		}
*/ 		
 		afterTrackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(afterTrackOutLot.getKey().getLotName());
		
		//20180604, kyjung, MQC
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(afterTrackOutLot.getFactoryName());
		processFlowKey.setProcessFlowName(afterTrackOutLot.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(afterTrackOutLot.getProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		
		if(processFlowData != null)
		{
			if(StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
			{
				eventInfo.setEventName("UpdateMQCCount");
				eventInfo.setCheckTimekeyValidation(false);
				/* 20181128, hhlee, EventTime Sync */
				//eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
				eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
				MESProductServiceProxy.getProductServiceImpl().updateMQCCountToProduct(afterTrackOutLot, eventInfo, processFlowData, beforeProcessOperationName);
				
				if(StringUtil.equals(afterTrackOutLot.getLotState(), "Completed"))
				{
					eventInfo.setEventName("FinishMQCJob");
					eventInfo.setCheckTimekeyValidation(false);
					/* 20181128, hhlee, EventTime Sync */
					//eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
					eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
					MESProductServiceProxy.getProductServiceImpl().checkFinishMQCJob(afterTrackOutLot, eventInfo, processFlowData);
				}
			}
		}
		
		/*if(!StringUtil.isNotEmpty(decideSampleNodeStack))
		{
			afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().endReworkState(eventInfo, beforeTrackOutLot, afterTrackOutLot);
		}*/
		
/*		if(!StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
		{
			chkAfterCompleteLot(afterTrackOutLot,eventInfo);
		}
*/		
		
		// 2019.05.29_hsryu_Delete Logic. Move to Logic. located TrackOutLot Function.
//		if( StringUtil.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
//		{
//			afterTrackOutLot.setProcessOperationName("-");
//			afterTrackOutLot.setProcessOperationVersion("");
//			LotServiceProxy.getLotService().update(afterTrackOutLot);
//			
//			// Change By Park Jeong Su Because not Exist lothistory
//			//String condition = "where lotname=?" + " and timekey= ? " ;
//			//Object[] bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(),eventInfo.getEventTimeKey()};
////			String condition = "where lotname = ? and timekey = (select max(timekey) from lothistory where lotname = ?)" ;
////			Object[] bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(),afterTrackOutLot.getKey().getLotName()};
////			List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
////			LotHistory lotHistory = arrayList.get(0);
//			LotHistoryKey LotHistoryKey = new LotHistoryKey();
//		    LotHistoryKey.setLotName(afterTrackOutLot.getKey().getLotName());
//		    LotHistoryKey.setTimeKey(afterTrackOutLot.getLastEventTimeKey());
//		    LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
//			lotHistory.setProcessOperationName("-");
//			lotHistory.setProcessOperationVersion("");
//			LotServiceProxy.getLotHistoryService().update(lotHistory);
//			
//			List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//		}
		
		MESLotServiceProxy.getLotServiceImpl().chkAfterCompleteLot(afterTrackOutLot,eventInfo);
		
		boolean reserveSampleFlag = false;
		// Check Reserve Sampling
		if ( reserveSampleFlag == true )
		{
			log.info("Reserve Sampling!");
		}
		
		/** 2018.12.21_hsryu_Delete Logic. already existed Logic. **/
//		try
//		{
//			if(CommonUtil.isInitialInput(machineName))
//			{
//				//Reserve Lot Data
//				ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] {machineName, lotName});
//				String holdFlag = reserveLot.getHoldFlag();
//				String department = reserveLot.getDepartmentName();
//				
//				if (StringUtil.equals(holdFlag, "Y"))
//				{
//					EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), "Unpk TrackOut auto hold to owner£¡", 
//								GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT, "HLUK");
//					
//					/* 20181128, hhlee, EventTime Sync */
//					holdEventInfo.setEventTime(eventInfo.getEventTime());
//					
//					Lot lot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//					Map<String, String> udfs = new HashMap<String, String>();
//					udfs = lot.getUdfs();
//					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lot);
//					
//					MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, udfs);
//
//					LotServiceProxy.getLotService().makeOnHold(lot.getKey(), holdEventInfo, makeOnHoldInfo);
//					
//					// -------------------------------------------------------------------------------------------------------------------------------------------
//					// Modified by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
////					LotMultiHoldKey multiholdkey = new LotMultiHoldKey();
////					multiholdkey.setLotName(lotName);
////					multiholdkey.setReasonCode("HLUK");
////					
////					LotMultiHold multihold = LotServiceProxy.getLotMultiHoldService().selectByKey(multiholdkey);
////					multihold.getUdfs().put("eventuserdep", department);
////							
////					LotServiceProxy.getLotMultiHoldService().update(multihold);
//					try {
//						MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotName, "HLUK", department, "AHOLD", holdEventInfo);
//					} catch (Exception e) {
//						log.warn(e);
//					}
//					// -------------------------------------------------------------------------------------------------------------------------------------------
//				}
//			}
//		}
//		catch (Throwable e)
//		{
//			log.warn("LotHold Fail after Unpacker TrackOut!");
//		}
		
		setNextInfo(doc, afterTrackOutLot);
		
		// Success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		
		catch(Exception ex)
		{
			log.warn("FMC Report Failed!");
		}		
	}
	
	public String insertEndBank(Lot trackOutLot, String productRequestType, String ProductionType)
	{
		
		//ProductRequest Key & Data
		//2019.02.25_hsryu_Delete Logic. Mantis 0002757.
//		ProductRequestKey pKey = new ProductRequestKey();
//		pKey.setProductRequestName(productRequestName);
//		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		
		if(StringUtils.isNotEmpty(productRequestType)){
			String sql = "";
			Map<String, Object> bindMap = new HashMap<String, Object>();
			if(StringUtil.equals(ProductionType, "DMQC"))
			{		
				sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE "
					+ "WHERE ENUMNAME = :ENUMNAME AND TAG = :TAG";	
				bindMap.put("ENUMNAME", productRequestType);
				bindMap.put("TAG", ProductionType);
			}
			else {
				sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE "
						+ "WHERE ENUMNAME = :ENUMNAME ";	
				bindMap.put("ENUMNAME", productRequestType);
			}
			
			List<Map<String, Object>> sqlResult = 
					GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			return sqlResult.get(0).get("ENUMVALUE").toString();
		}
		return "";
	}
	
	/**
	 * must be in here at last point of event
	 * @since 2015.08.21
	 * @author swcho
	 * @param doc
	 * @param lotData
	 */
	public void setNextInfo(Document doc, Lot lotData)
	{
		try
		{
			StringBuilder strComment = new StringBuilder();
			strComment.append("LotName").append("[").append(lotData.getKey().getLotName()).append("]").append("\n")
						.append("LotGrade").append("[").append(lotData.getLotGrade()).append("]").append("\n")
						.append("NextFlow").append("[").append(lotData.getProcessFlowName()).append("]").append("\n")
						.append("NextOperation").append("[").append(lotData.getProcessOperationName()).append("]").append("\n");
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			log.warn("Note after TK OUT is nothing");
		}
	}
	
	/**
	 * must be in here at last point of event
	 * @since 2015.08.21
	 * @author swcho
	 * @param doc
	 * @param lotData
	 */
	public void setNextInfoForUPK(Document doc, List<Lot> lotDataList)
	{
		try
		{
			StringBuilder strComment = new StringBuilder();
			
			for(Lot lotData : lotDataList)
			{
				strComment.append("LotName").append("[").append(lotData.getKey().getLotName()).append("]").append("\n");
			}
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			log.warn("Note after TK OUT is nothing");
		}
	}
	
	// Added by smkang on 2018.08.21 - Before a lot is started to process, MachineRecipeName of the Lot is empty.
	public void cancelOtherPortCarrier(String machineName, String portName, String recipeName, Document doc) throws InvalidStateTransitionSignal,FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		try {
			if (StringUtil.isNotEmpty(machineName) && StringUtil.isNotEmpty(recipeName)) {
				StringBuilder sqlStatement = new StringBuilder();
				sqlStatement.append("SELECT D.DURABLENAME, D.PORTNAME, L.FACTORYNAME, L.PRODUCTSPECNAME, L.PROCESSFLOWNAME, L.PROCESSOPERATIONNAME, L.ECCODE\n")
							.append("  FROM DURABLE D, LOT L\n")
							.append(" WHERE D.DURABLENAME = L.CARRIERNAME\n")
							.append("   AND D.MACHINENAME = ?\n")
							.append("   AND D.PORTNAME IN (SELECT P.PORTNAME\n")
							.append("                        FROM PORT P\n")
							.append("                       WHERE P.MACHINENAME = ?\n")
							.append("                         AND P.PORTNAME <> ?\n")
							.append("                         AND P.PORTTYPE IN (?, ?, ?))");
				
				List<ListOrderedMap> queryResultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStatement.toString(), new Object[] {machineName, machineName, portName, "PB", "PL", "PS"});
				
				if (queryResultList != null && queryResultList.size() > 0) {
					Machine machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
					String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
					
					for (ListOrderedMap queryResult : queryResultList) {
						String factoryName = queryResult.get("FACTORYNAME").toString();
						String productSpecName = queryResult.get("PRODUCTSPECNAME").toString();
						String processFlowName = queryResult.get("PROCESSFLOWNAME").toString();
						String processOperationName = queryResult.get("PROCESSOPERATIONNAME").toString();
						String ecCode = queryResult.get("ECCODE").toString();
						
						try {
							String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(factoryName, productSpecName, processFlowName, 
																													processOperationName, machineName, ecCode);
							
							if (StringUtils.isNotEmpty(machineRecipeName) && machineRecipeName.equals(recipeName)) {
								// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
								// Added by hsryu - Cancel LotProcess.
								String cancelCarrierName = queryResult.get("DURABLENAME").toString();
								Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(cancelCarrierName);
								
								String cancelPortName = queryResult.get("PORTNAME").toString();
								Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, cancelPortName);
								
								/*** 2019.03.06_hsryu_Add Validation. Request CIM ***/
								Lot lotData = null;
								boolean cancelFlag = true;
								
								try{
									lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierData.getKey().getDurableName());
								}
								catch(Throwable e){
									log.info("LotData is not exist. CarrierName :" + carrierData.getKey().getDurableName());
								}
								
								if( lotData != null ){
									if(StringUtils.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run)){
										cancelFlag = false;
									}
								}
								/****************************************************/
								
								// 2019.03.06_hsryu_add Validation. if lotProcessState is Run, not executing CancelCarrier. Requested by CIM.
								if(cancelFlag){
									doc = this.generateCSTForceQuitCommand(doc, machineName, carrierData, portData);
									GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
								}
								// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------					
							}
						} catch (Exception e) {
							if (e instanceof CustomException)
								log.warn(((CustomException) e).errorDef.getEng_errorMessage());
							else
								log.warn(e);
						}
					}
				}
				
				// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
				// Added by smkang on 2018.08.22 - Cancel TransportJob.
				StringBuilder sqlStatement2 = new StringBuilder();
				sqlStatement2.append("SELECT T.TRANSPORTJOBNAME, T.CARRIERNAME, L.FACTORYNAME, L.PRODUCTSPECNAME, L.PROCESSFLOWNAME, L.PROCESSOPERATIONNAME, L.ECCODE\n")
							 .append("  FROM CT_TRANSPORTJOBCOMMAND T, LOT L\n")
							 .append(" WHERE T.CARRIERNAME = L.CARRIERNAME\n")
							 .append("   AND T.DESTINATIONMACHINENAME = ?\n")
							 .append("   AND T.DESTINATIONPOSITIONNAME IN (SELECT P.PORTNAME\n")
							 .append("                                       FROM PORT P\n")
							 .append("                                      WHERE P.MACHINENAME = ?\n")
							 .append("                                        AND P.PORTNAME <> ?\n")
							 .append("                                        AND P.PORTTYPE IN (?, ?, ?))");
	
				List<ListOrderedMap> queryResultList2 = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStatement2.toString(), new Object[] {machineName, machineName, portName, "PB", "PL", "PS"});
	
				if (queryResultList2 != null && queryResultList2.size() > 0) {
					for (ListOrderedMap queryResult2 : queryResultList2) {
						String factoryName = queryResult2.get("FACTORYNAME").toString();
						String productSpecName = queryResult2.get("PRODUCTSPECNAME").toString();
						String processFlowName = queryResult2.get("PROCESSFLOWNAME").toString();
						String processOperationName = queryResult2.get("PROCESSOPERATIONNAME").toString();
						String ecCode = queryResult2.get("ECCODE").toString();
						
						try {
							String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(factoryName, productSpecName, processFlowName, 
																													processOperationName, machineName, ecCode);
							
							if (StringUtils.isNotEmpty(machineRecipeName) && machineRecipeName.equals(recipeName)) {
								String cancelTransportJobName = queryResult2.get("TRANSPORTJOBNAME").toString();
								String cancelCarrierName = queryResult2.get("CARRIERNAME").toString();
								
								doc = this.generateCancelTransportJobRequest(doc, cancelTransportJobName, cancelCarrierName);
								GenericServiceProxy.getESBServive().sendBySender(doc, "TEXSender");
							}
						} catch (Exception e) {
							if (e instanceof CustomException)
								log.warn(((CustomException) e).errorDef.getEng_errorMessage());
							else
								log.warn(e);
						}
					}
				}
				// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			}
		} catch (Exception e) {
			if (e instanceof CustomException)
				log.warn(((CustomException) e).errorDef.getEng_errorMessage());
			else
				log.warn(e);
		}
	}
	
	// Added by smkang on 2018.08.21 - Move this method to LotServiceUtil because cancelOtherPortCarrier method is moved to LotServiceUtil for using RecipeServiceUtil.
	private Document generateCSTForceQuitCommand(Document doc, String machineName ,Durable DurableData, Port portData) throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CSTForceQuitCommand");
		SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", "");
		SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "CHECK RECIPE IDLE TIME");
				
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
		
		Element element1 = new Element("MACHINENAME");
		element1.setText(machineName);
		eleBodyTemp.addContent(element1);
		
		Element element2 = new Element("UNITNAME");
		element2.setText("");
		eleBodyTemp.addContent(element2);
		
		Element element3 = new Element("CARRIERNAME");
		element3.setText(DurableData.getKey().getDurableName());
		eleBodyTemp.addContent(element3);

		Element element4 = new Element("CARRIERSTATE");
		element4.setText(DurableData.getDurableState());
		eleBodyTemp.addContent(element4);
		
		Element element5 = new Element("CARRIERTYPE");
		element5.setText(DurableData.getDurableType());
		eleBodyTemp.addContent(element5);
		
		Element element6 = new Element("PORTNAME");
		element6.setText(portData.getKey().getPortName());
		eleBodyTemp.addContent(element6);
		
		Element element7 = new Element("PORTTYPE");
		element7.setText(portData.getUdfs().get("PORTTYPE"));
		eleBodyTemp.addContent(element7);
		
		Element element8 = new Element("PORTUSETYPE");
		element8.setText(portData.getUdfs().get("PORTUSETYPE"));
		eleBodyTemp.addContent(element8);
		
		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);
		
		Content returnElement = doc.getRootElement().getChild(SMessageUtil.Return_Tag).detach();
		doc.getRootElement().addContent(returnElement);
		
		return doc;
	}
	
	// Added by smkang on 2018.08.21 - Make CancelTransportJobRequest Message.
	private Document generateCancelTransportJobRequest(Document doc, String transportJobName, String carrierName) {
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CANCELTRANSPORTJOBREQUEST);
		SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", "");
		SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "CHECK RECIPE IDLE TIME");
				
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
		
		Element element1 = new Element("TRANSPORTJOBNAME");
		element1.setText(transportJobName);
		eleBodyTemp.addContent(element1);
		
		Element element2 = new Element("CARRIERNAME");
		element2.setText(carrierName);
		eleBodyTemp.addContent(element2);
		
		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);
		
		Content returnElement = doc.getRootElement().getChild(SMessageUtil.Return_Tag).detach();
		doc.getRootElement().addContent(returnElement);
		
		return doc;
	}
	
	public boolean isExistAhold(String lotName, String processFlowName, String processoperationName) throws CustomException
	{
		boolean existFlag = false;
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		String condition = "lotName = ? and factoryName = ? and processFlowName = ? and processOperationName = ? and actionName = ? and holdType = ? and actionState = ? ";
		Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), processFlowName, processoperationName, GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD,
				GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD, GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED};

		List<LotAction> lotActionList = new ArrayList<LotAction>();
		
		try
		{
			lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			log.info("Lot : [ " + lotName + " ] NonExist FutureAction Data.");
		}
		
		if(lotActionList != null && lotActionList.size()>0)
		{
			existFlag = true;
		}
		
		String systemHoldCondition = "lotName = ? and factoryName = ? and processFlowName = ? and processOperationName = ? and actionName = ? and actionState = ? ";
		Object[] systemHoldBindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), processFlowName, processoperationName, GenericServiceProxy.getConstantMap().ACTIONNAME_SYSTEMHOLD,
				GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED};

		List<LotAction> lotActionList2 = new ArrayList<LotAction>();
		
		try
		{
			lotActionList2 = ExtendedObjectProxy.getLotActionService().select(systemHoldCondition, systemHoldBindSet);
		}
		catch(Exception ex)
		{
			log.info("Lot : [ " + lotName + " ] NonExist FutureAction Data.");
		}
		
		if(lotActionList2 != null && lotActionList2.size()>0)
		{
			existFlag = true;
		}
		
		return existFlag;
	}
	
	
	public boolean isExistBholdorOperHold(String lotName,EventInfo eventInfo) throws CustomException
	{
		boolean existFlag = false;
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		String strSql = StringUtil.EMPTY;
		
		strSql = strSql + " SELECT PRODUCTSPECNAME,PRODUCTSPECVERSION, ECCODE, PROCESSFLOWNAME, PROCESSFLOWVERSION,PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, POSITION, HOLDCODE, DEPARTMENTNAME ,LASTEVENTNAME, LASTEVENTCOMMENT, TYPE, LASTEVENTUSER \n";
		strSql = strSql + " FROM \n";
		strSql = strSql + " ( \n";
		strSql = strSql + " SELECT A2.PRODUCTSPECNAME, A2.PRODUCTSPECVERSION, A2.ECCODE, A2.PROCESSFLOWNAME, A2.PROCESSFLOWVERSION,A2.PROCESSOPERATIONNAME,A2.PROCESSOPERATIONVERSION, A2.HOLDCODE, A2.DEPARTMENTNAME, A2.POSITION, A2.LASTEVENTNAME, A2.LASTEVENTCOMMENT, :OPERHOLD AS TYPE, A2.LASTEVENTUSER \n";
		strSql = strSql + " FROM ( \n";
		strSql = strSql + " SELECT ROWNUM NUM, A1.* \n";
		strSql = strSql + " FROM \n";
		strSql = strSql + " ( \n";
		strSql = strSql + " SELECT C.* FROM CT_OPERACTION C \n";
		strSql = strSql + " WHERE 1=1 \n";
		strSql = strSql + " AND FACTORYNAME = :FACTORYNAME \n";
		strSql = strSql + " AND (PRODUCTSPECNAME = :PRODUCTSPECNAME OR PRODUCTSPECNAME = :STAR) \n";
		strSql = strSql + " AND (PRODUCTSPECVERSION = :PRODUCTSPECVERSION OR PRODUCTSPECVERSION = :STAR) \n";
		strSql = strSql + " AND (ECCODE = :ECCODE OR ECCODE = :STAR)  \n";
		strSql = strSql + " AND PROCESSFLOWNAME = :PROCESSFLOWNAME \n";
		strSql = strSql + " AND (PROCESSFLOWVERSION = :PROCESSFLOWVERSION OR PROCESSFLOWVERSION = :STAR) \n";
		strSql = strSql + " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME \n";
		strSql = strSql + " AND (PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION OR PROCESSOPERATIONVERSION = :STAR) \n";
		strSql = strSql + " ORDER BY DECODE (FACTORYNAME, :STAR, 9999, 0),                        \n";
		strSql = strSql + "                    DECODE (PRODUCTSPECNAME, :STAR, 9999, 0),                    \n";
		strSql = strSql + "                    DECODE (PRODUCTSPECVERSION, :STAR, 9999, 0),                 \n";
		strSql = strSql + "                    DECODE (ECCODE, :STAR, 9999, 0),                             \n";
		strSql = strSql + "                    DECODE (PROCESSFLOWNAME, :STAR, 9999, 0),                    \n";
		strSql = strSql + "                    DECODE (PROCESSFLOWVERSION, :STAR, 9999, 0),                 \n";
		strSql = strSql + "                    DECODE (PROCESSOPERATIONNAME, :STAR, 9999, 0),               \n";
		strSql = strSql + "                    DECODE (PROCESSOPERATIONVERSION, :STAR, 9999, 0) \n";
		strSql = strSql + "                    ) A1 \n";
		strSql = strSql + "                    ) A2 \n";
		strSql = strSql + " LEFT JOIN CT_PERMANENTHOLDINFO B \n";
		strSql = strSql + " ON 1=1 \n";
		strSql = strSql + " AND A2.FACTORYNAME = B.FACTORYNAME \n";
		strSql = strSql + " AND A2.PRODUCTSPECNAME = B.PRODUCTSPECNAME \n";
		strSql = strSql + " AND A2.PRODUCTSPECVERSION = B.PRODUCTSPECVERSION \n";
		strSql = strSql + " AND A2.ECCODE = B.ECCODE \n";
		strSql = strSql + " AND A2.PROCESSFLOWNAME = B.PROCESSFLOWNAME \n";
		strSql = strSql + " AND A2.PROCESSFLOWVERSION = B.PROCESSFLOWVERSION \n";
		strSql = strSql + " AND A2.PROCESSOPERATIONNAME = B.PROCESSOPERATIONNAME \n";
		strSql = strSql + " AND A2.PROCESSOPERATIONVERSION = B.PROCESSOPERATIONVERSION \n";
		strSql = strSql + " AND A2.HOLDCODE = B.REASONCODE \n";
		strSql = strSql + " AND NVL(A2.DEPARTMENTNAME,:DEPARTMENT) = NVL(B.DEPARTMENTNAME,:DEPARTMENT) \n";
		strSql = strSql + " AND A2.HOLDTYPE = :HOLDTYPE \n";
		strSql = strSql + " AND B.DESCRIPTION = :OPERHOLD \n";
		strSql = strSql + " AND B.LOTNAME = :LOTNAME \n";
		strSql = strSql + " WHERE 1=1 \n";
		strSql = strSql + " AND B.LOTNAME IS NULL AND B.FACTORYNAME IS NULL AND B.PRODUCTSPECNAME IS NULL AND B.PRODUCTSPECVERSION IS NULL \n";
		strSql = strSql + " AND B.PROCESSFLOWNAME IS NULL AND B.PROCESSFLOWVERSION IS NULL AND B.PROCESSOPERATIONNAME IS NULL AND B.POSITION IS NULL \n";
		strSql = strSql + " AND B.PROCESSOPERATIONVERSION IS NULL AND B.POSITION IS NULL \n";
		strSql = strSql + " )  \n";
		strSql = strSql + " UNION  \n";
		strSql = strSql + " (SELECT :STAR PRODUCTSPECNAME, :STAR PRODUCTSPECVERSION, :STAR ECCODE ,A.PROCESSFLOWNAME, A.PROCESSFLOWVERSION,A.PROCESSOPERATIONNAME, A.PROCESSOPERATIONVERSION, A.POSITION, A.HOLDCODE, A.DEPARTMENT AS DEPARTMENTNAME, A.LASTEVENTNAME, A.LASTEVENTCOMMENT, :HOLDTYPE AS TYPE, A.LASTEVENTUSER \n";
		strSql = strSql + " FROM CT_LOTACTION A \n";
		strSql = strSql + " LEFT JOIN CT_PERMANENTHOLDINFO B \n";
		strSql = strSql + " ON A.LOTNAME = B.LOTNAME \n";
		strSql = strSql + " AND A.FACTORYNAME = B.FACTORYNAME \n";
		strSql = strSql + " AND B.PRODUCTSPECNAME = :STAR \n";
		strSql = strSql + " AND B.PRODUCTSPECVERSION = :STAR \n";
		strSql = strSql + " AND B.ECCODE = :STAR \n";
		strSql = strSql + " AND A.PROCESSFLOWNAME = B.PROCESSFLOWNAME \n";
		strSql = strSql + " AND A.PROCESSFLOWVERSION = B.PROCESSFLOWVERSION \n";
		strSql = strSql + " AND A.PROCESSOPERATIONNAME = B.PROCESSOPERATIONNAME \n";
		strSql = strSql + " AND A.PROCESSOPERATIONVERSION = B.PROCESSOPERATIONVERSION \n";
		strSql = strSql + " AND A.HOLDCODE = B.REASONCODE \n";
		strSql = strSql + " AND A.HOLDTYPE = :HOLDTYPE \n";
		strSql = strSql + " AND B.DESCRIPTION = :HOLDTYPE \n";
		strSql = strSql + " AND NVL(A.DEPARTMENT,:DEPARTMENT) = NVL(B.DEPARTMENTNAME,:DEPARTMENT) \n";
		strSql = strSql + " WHERE 1=1 \n";
		strSql = strSql + " AND A.LOTNAME = :LOTNAME \n";
		strSql = strSql + " AND A.FACTORYNAME = :FACTORYNAME \n";
		strSql = strSql + " AND A.PROCESSFLOWNAME = :PROCESSFLOWNAME \n";
		strSql = strSql + " AND A.PROCESSFLOWVERSION = :PROCESSFLOWVERSION \n";
		strSql = strSql + " AND A.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME \n";
		strSql = strSql + " AND A.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION \n";
		strSql = strSql + " AND A.ACTIONNAME = :ACTIONNAME \n";
		strSql = strSql + " AND A.HOLDTYPE = :HOLDTYPE \n";
		strSql = strSql + " AND B.LOTNAME IS NULL AND B.FACTORYNAME IS NULL AND B.PRODUCTSPECNAME IS NULL AND B.PRODUCTSPECVERSION IS NULL \n";
		strSql = strSql + " AND B.PROCESSFLOWNAME IS NULL AND B.PROCESSFLOWVERSION IS NULL AND B.PROCESSOPERATIONNAME IS NULL AND B.POSITION IS NULL \n";
		strSql = strSql + " AND B.PROCESSOPERATIONVERSION IS NULL AND B.POSITION IS NULL) \n";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("LOTNAME", lotData.getKey().getLotName());
		bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		bindMap.put("PRODUCTSPECVERSION", "00001");
		bindMap.put("ECCODE", lotData.getUdfs().get("ECCODE"));
		bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		bindMap.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		bindMap.put("OPERHOLD", "OPERHOLD");
		bindMap.put("DEPARTMENT", " ");
		bindMap.put("HOLDTYPE", "BHOLD");
		bindMap.put("ACTIONNAME", "Hold");
		bindMap.put("STAR", "*");			

		List<Map<String, Object>> holdList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

		if(holdList.size()>0)
		{
			existFlag = true;
			
			for(int i=0; i<holdList.size(); i++)
			{
				//reselect.. because of makeOnHold function. (check HoldState)
				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				
				String productSpecName = holdList.get(i).get("PRODUCTSPECNAME").toString();
				String productSpecVer = holdList.get(i).get("PRODUCTSPECVERSION").toString();
				String ecCode = holdList.get(i).get("ECCODE").toString();
				String processFlowName = holdList.get(i).get("PROCESSFLOWNAME").toString();
				String processFlowVer = holdList.get(i).get("PROCESSFLOWVERSION").toString();
				String processOperationName = holdList.get(i).get("PROCESSOPERATIONNAME").toString();
				String processOperationVer = holdList.get(i).get("PROCESSOPERATIONVERSION").toString();
				String holdCode = holdList.get(i).get("HOLDCODE").toString();
				String departmentName = (String)holdList.get(i).get("DEPARTMENTNAME");
				String lastEventName = holdList.get(i).get("LASTEVENTNAME").toString();
				String lastEventComment = holdList.get(i).get("LASTEVENTCOMMENT").toString();
				String type = holdList.get(i).get("TYPE").toString();
				String position = holdList.get(i).get("POSITION").toString();
				String lastEventUser = holdList.get(i).get("LASTEVENTUSER").toString();
				
				EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("FutureBHold", eventInfo.getEventUser(), "", "", "");
				
				holdEventInfo.setEventTime(eventInfo.getEventTime());
				holdEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				holdEventInfo.setReasonCode(holdCode);
				holdEventInfo.setReasonCodeType(GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT);
				holdEventInfo.setEventUser(lastEventUser);
				
				if(StringUtil.equals(type, "OPERHOLD"))
				{
					// 2019.05.31_hsryu_Missed Logic. Mantis 719. 
					holdEventInfo.setEventName("FutureOperationHold");
					holdEventInfo.setEventComment("FutureActionByOperation");
				}
				else
				{
					holdEventInfo.setEventComment("FutureActionByReserve");
				}

				if(!lotData.getLotHoldState().equals("Y"))
				{
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
					MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
//					String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//					makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
			        // 2019.05.31_hsryu_Change Update -> SetEvent.
			        makeOnHoldInfo.getUdfs().put("NOTE", lastEventComment);

					LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), holdEventInfo, makeOnHoldInfo);
					
					// 2019.05.31_hsryu_Delete Logic. Not Update Logic. 
					//this.setNote(lotName, lastEventComment, holdEventInfo);
					
					holdEventInfo.setEventComment(lastEventComment);

					try {
						lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), holdCode, StringUtil.isNotEmpty(departmentName)?departmentName:" ", GenericServiceProxy.getConstantMap().HOLDTYPE_BHOLD , holdEventInfo);
					} catch (Exception e) {
						log.warn(e);
					}
					// -------------------------------------------------------------------------------------------------------------------------------------------
				}
				else
				{
					SetEventInfo setEventInfo = new SetEventInfo();
//					String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(lotData.getKey().getLotName());
//					setEventInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
					// 2019.05.31_hsryu_Change Update -> SetEvent.
					setEventInfo.getUdfs().put("NOTE",lastEventComment);
					LotServiceProxy.getLotService().setEvent(lotData.getKey(), holdEventInfo, setEventInfo);
					
					// 2019.05.31_hsryu_Delete Logic. Not Update Logic. 
					//this.setNote(lotName, lastEventComment, holdEventInfo);

					holdEventInfo.setEventComment(lastEventComment);
					
					try {
						lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), holdCode, StringUtil.isNotEmpty(departmentName)?departmentName:" ", GenericServiceProxy.getConstantMap().HOLDTYPE_BHOLD , holdEventInfo);
					} catch (Exception e) {
						log.warn(e);
					}
					// -------------------------------------------------------------------------------------------------------------------------------------------

					/*List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

					for(ProductU product : productUSequence)
					{
						Product aProduct =  MESProductServiceProxy.getProductInfoUtil().getProductByProductName(product.getProductName());
						kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfoP = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
						ProductServiceProxy.getProductService().setEvent(aProduct.getKey(), holdEventInfo, setEventInfoP);
						
						try {
							MESProductServiceProxy.getProductServiceImpl().addMultiHoldProduct(product.getProductName(), holdCode, StringUtil.isNotEmpty(departmentName)?departmentName:" ",GenericServiceProxy.getConstantMap().HOLDTYPE_BHOLD, holdEventInfo);
						} catch (Exception e) {
							log.warn(e);
						}
						// -------------------------------------------------------------------------------------------------------------------------------------------
					}*/
				}
				
				Map<String, String> updateUdfs = new HashMap<String, String>();
				updateUdfs.put("NOTE", "");
				MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
				
				holdEventInfo.setEventUser(eventInfo.getEventUser());
				
				if(StringUtil.equals(type, "BHOLD"))
				{
					this.increaseHoldCountForLotAction(lotName, lotData.getFactoryName(), processFlowName,processFlowVer,processOperationName,processOperationVer,position, holdEventInfo);
				}
				
				ExtendedObjectProxy.getPermanentHoldInfoService().createPermanentHoldInfo(lotData, productSpecName, productSpecVer,ecCode, processFlowName,
						processFlowVer, processOperationName, processOperationVer
						, type, holdCode, StringUtil.isNotEmpty(departmentName)?departmentName:" ", holdEventInfo);
			}
		}

		return existFlag;
	}
	
	public void deletePermanentHoldInfo(String lotName, String factoryName, String processFlowName, String processOperationName, EventInfo eventInfo) throws CustomException
	{
		
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		
		List<PermanentHoldInfo> permanentHoldList = new ArrayList<PermanentHoldInfo>();
		
		try
		{
			String condition = "lotName = ? and factoryName = ? and (processFlowName = ? or processFlowName = ?) and processOperationName = ? ";
			Object[] bindSet = new Object[]{lotData.getKey().getLotName(), factoryName, processFlowName, "*", processOperationName};

			permanentHoldList = ExtendedObjectProxy.getPermanentHoldInfoService().select(condition, bindSet);
		}
		catch(Throwable e)
		{
			log.info("Not exist permanentHoldInfo in beforeInfo");
		}
		
		if(permanentHoldList.size()>0)
		{
			for(PermanentHoldInfo permanentHold : permanentHoldList)
			{
				eventInfo.setEventComment("removePermanentHoldInfo");
				ExtendedObjectProxy.getPermanentHoldInfoService().remove(eventInfo, permanentHold);
			}
		}
	}

	public void increaseHoldCountForLotAction(String lotName, String factoryName, String processFlowName, String processFlowVer, String processOperationName,
			String processOperationVersion, String position, EventInfo eventInfo) throws CustomException
	{
		
		LotAction lotAction = null;
		
		try
		{
			lotAction = ExtendedObjectProxy.getLotActionService().selectByKey(false, new Object[] {lotName, factoryName,processFlowName,
					processFlowVer,processOperationName,processOperationVersion,position});
		}
		catch(Throwable e)
		{
			//not exist lotaction....
		}
		
		if(lotAction!=null)
		{
			if(StringUtil.equals(lotAction.getHoldType(), "BHOLD"))
			{
				if(StringUtil.equals(lotAction.getHoldPermanentFlag(), "N"))
				{
					lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);
					lotAction.setHoldCount(lotAction.getHoldCount()+1);
					lotAction.setLastEventName(eventInfo.getEventName());
					lotAction.setLastEventTime(eventInfo.getEventTime());
					lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
					//2019.01.04_hsryu_should not be changed.
					//lotAction.setLastEventUser(eventInfo.getEventUser());
					lotAction.setLastEventComment(eventInfo.getEventComment());
					ExtendedObjectProxy.getLotActionService().remove(eventInfo, lotAction);
				}
				else
				{
					lotAction.setHoldCount(lotAction.getHoldCount()+1);
					//lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);
					//2019.02.27_hsryu_should not be changed.
					//lotAction.setLastEventName(eventInfo.getEventName());
					lotAction.setLastEventTime(eventInfo.getEventTime());
					lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
					//2019.01.04_hsryu_should not be changed.
					//lotAction.setLastEventUser(eventInfo.getEventUser());
					//2019.02.27_hsryu_should not be changed.
					//lotAction.setLastEventComment(eventInfo.getEventComment());
					ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotAction);
				}

			}
			else if(StringUtil.equals(lotAction.getHoldType(), "AHOLD"))
			{
				lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);
				lotAction.setHoldCount(lotAction.getHoldCount()+1);
				lotAction.setLastEventName(eventInfo.getEventName());
				lotAction.setLastEventTime(eventInfo.getEventTime());
				lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
				//2019.01.04_hsryu_should not be changed.
				//lotAction.setLastEventUser(eventInfo.getEventUser());
				lotAction.setLastEventComment(eventInfo.getEventComment());
				ExtendedObjectProxy.getLotActionService().remove(eventInfo, lotAction);
			}
		}
	}


	public boolean checkBhold(String lotName, String processFlowName, String processoperationName) throws CustomException
	{
		boolean existFlag = false;

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		String condition = "lotName = ? and factoryName = ? and processFlowName = ? and processOperationName = ? and actionName = ? and holdType = ? and actionState = ? ";
		Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), processFlowName, processoperationName, GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD, GenericServiceProxy.getConstantMap().HOLDTYPE_BHOLD, GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED };

		List<LotAction> lotActionList = new ArrayList<LotAction>();
		
		try
		{
			lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			log.info("Lot : [ " + lotName + " ] NonExist FutureAction Data.");
		}
		
		if(lotActionList != null && lotActionList.size()>0)
		{
			existFlag = true;
		}
		
		String operCondition = "factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processOperationName = ? and actionName = ? and holdType = ? ";
		Object[] operBindSet = new Object[]{lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"),
				processFlowName, processoperationName, GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD, GenericServiceProxy.getConstantMap().HOLDTYPE_BHOLD};
		
		List<OperAction> operActionList = new ArrayList<OperAction>();
		
		try
		{
			operActionList = ExtendedObjectProxy.getOperActionService().select(operCondition, operBindSet);
		}
		catch(Exception ex)
		{
			log.info("Oper : [ " + processoperationName + " ] NonExist FutureAction Data.");
		}
		
		if(operActionList != null && operActionList.size()>0)
		{
			existFlag = true;
		}
		
		return existFlag;
	}


	
	public void reserveAHold(Lot lotData, String department, EventInfo eventInfo) throws CustomException
	{
		//Get Last Position	
		long lastPosition = Integer.parseInt(this.getLastPosition(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName()));

		//Create CT_LOTACTION
		LotAction lotActionData = new LotAction(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName() ,"00001", lotData.getProcessOperationName(), "00001", lastPosition + 1);
		
		lotActionData.setActionName(GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD);
		lotActionData.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED);
		lotActionData.setFactoryName(lotData.getFactoryName());
		lotActionData.setHoldCode(eventInfo.getReasonCode());
		lotActionData.setHoldPermanentFlag("N");
		lotActionData.setHoldType(GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
		lotActionData.setChangeProductRequestName("");
		lotActionData.setChangeProductSpecName("");
		lotActionData.setChangeECCode("");
		lotActionData.setChangeProcessFlowName("");
		lotActionData.setChangeProcessOperationName("");
		lotActionData.setLastEventName(eventInfo.getEventName());
		lotActionData.setLastEventTime(eventInfo.getEventTime());
		lotActionData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		lotActionData.setLastEventUser(eventInfo.getEventUser());
		//lotActionData.setLastEventComment(eventInfo.getEventComment());
		lotActionData.setLastEventComment(eventInfo.getEventComment());
		lotActionData.setDepartment(department);

		ExtendedObjectProxy.getLotActionService().create(eventInfo, lotActionData);
	}
	
	// 2019.03.19_hsryu_Insert Logic. 
	public void reserveAHoldByOtherOperation(Lot lotData, String processFlowName, String processOperationName, String department, EventInfo eventInfo) throws CustomException
	{
		//Get Last Position	
		long lastPosition = Integer.parseInt(this.getLastPosition(lotData.getKey().getLotName(), lotData.getFactoryName(), processFlowName, processOperationName));

		//Create CT_LOTACTION
		LotAction lotActionData = new LotAction(lotData.getKey().getLotName(), lotData.getFactoryName(), processFlowName ,"00001", processOperationName, "00001", lastPosition + 1);
		
		lotActionData.setActionName(GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD);
		lotActionData.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED);
		lotActionData.setFactoryName(lotData.getFactoryName());
		lotActionData.setHoldCode(eventInfo.getReasonCode());
		lotActionData.setHoldPermanentFlag("N");
		lotActionData.setHoldType(GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
		lotActionData.setChangeProductRequestName("");
		lotActionData.setChangeProductSpecName("");
		lotActionData.setChangeECCode("");
		lotActionData.setChangeProcessFlowName("");
		lotActionData.setChangeProcessOperationName("");
		lotActionData.setLastEventName(eventInfo.getEventName());
		lotActionData.setLastEventTime(eventInfo.getEventTime());
		lotActionData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		lotActionData.setLastEventUser(eventInfo.getEventUser());
		//lotActionData.setLastEventComment(eventInfo.getEventComment());
		lotActionData.setLastEventComment(eventInfo.getEventComment());
		lotActionData.setDepartment(department);

		ExtendedObjectProxy.getLotActionService().create(eventInfo, lotActionData);
	}
	
	
	public String getLastPosition(String lotName, String factoryName, String flowName, String operationName)
	{
		String getPositionSql = "SELECT POSITION "
				+ " FROM CT_LOTACTION "
				+ " WHERE LOTNAME = :LOTNAME "
				+ " AND FACTORYNAME = :FACTORYNAME "
				+ " AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ " AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				+ " ORDER BY POSITION DESC";
		
		Map<String, Object> getPositionBind = new HashMap<String, Object>();
		getPositionBind.put("LOTNAME", lotName);
		getPositionBind.put("FACTORYNAME", factoryName);
		getPositionBind.put("PROCESSFLOWNAME", flowName);
		getPositionBind.put("PROCESSFLOWVERSION", "00001");
		getPositionBind.put("PROCESSOPERATIONNAME", operationName);
		getPositionBind.put("PROCESSOPERATIONVERSION", "00001");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> positionSqlBindSet = GenericServiceProxy.getSqlMesTemplate().queryForList(getPositionSql, getPositionBind); 
		
		if(positionSqlBindSet.size() == 0)
		{
			return "0";
		}
		
		return positionSqlBindSet.get(0).get("POSITION").toString();
	}
	
	public void checkReserveLotHold(String machineName, Lot lotData, EventInfo eventInfo)
	{
		try
		{
			if(CommonUtil.isInitialInput(machineName))
			{
				//Reserve Lot Data
				ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] {machineName, lotData.getKey().getLotName()});
				String holdFlag = reserveLot.getHoldFlag();
				String department = reserveLot.getDepartmentName();
				
				if (StringUtil.equals(holdFlag, "Y"))
				{
					//2019.01.17_hsryu_Change eventName : "UPKHold" -> "Hold". Mantis 2322. 
					EventInfo reserveHoldEventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), "Unpk TrackOut auto hold to owner£¡", 
							GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT, "HLUK");
					MESLotServiceProxy.getLotServiceUtil().reserveAHold(lotData, department, reserveHoldEventInfo);
				}
			}
		}
		catch (Throwable e)
		{
			log.warn("LotHold Fail after Unpacker TrackOut!");
		}
	}
	
	public boolean isExistAholdForReleaseHold(String lotName, String processFlowName, String processoperationName, EventInfo eventInfo) throws CustomException
	{
		boolean existFlag = false;
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		String condition = "lotName = ? and factoryName = ? and processFlowName = ? and processOperationName = ? and actionName = ? and holdType = ? ";
		Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), processFlowName, processoperationName, GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD, GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD};

		List<LotAction> lotActionList = new ArrayList<LotAction>();
		
		try
		{
			lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			log.info("Lot : [ " + lotName + " ] NonExist FutureAction Data.");
		}
		
		if(lotActionList != null && lotActionList.size()>0)
		{
			existFlag = true;
		}
		
		return existFlag;
	}
	
	
	public boolean checkSampling(String lotName, EventInfo eventInfo) throws CustomException
	{
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(lotData.getCarrierName());
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);

		String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
		
		String tempNodeStack = lotData.getNodeStack();
		String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
				
		Map<String, String> returnBeforeInfo = MESLotServiceProxy.getLotServiceUtil().getBeforeOperName(lotData.getFactoryName(), lotData.getProcessFlowName(), "00001", lotData.getProcessOperationName(), "00001");
		
		String beforeOper = returnBeforeInfo.get("PROCESSOPERATIONNAME");
		
		if (StringUtil.isNotEmpty(beforeOper)) {
			String beforeFlow = returnBeforeInfo.get("PROCESSFLOWNAME");
			String beforeFlowVersion = returnBeforeInfo.get("PROCESSFLOWVERSION");
			String beforeNode = CommonUtil.getNodeStack(lotData.getFactoryName(),beforeFlow, beforeOper);		
			
			Map<String, Object> sampleFlowInfo = MESLotServiceProxy.getLotServiceUtil().checkReserveSampleInfo(lotData.getKey().getLotName(),lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), beforeFlow, beforeFlowVersion, beforeOper);
			
			if(sampleFlowInfo!=null)
			{
				String originalEventUser = eventInfo.getEventUser();
				
				eventInfo.setEventName("Sampling");
				eventInfo.setEventComment("Sampling");
				String sampleFlow = sampleFlowInfo.get("SAMPLEPROCESSFLOWNAME").toString();
				String sampleFlowVersion = sampleFlowInfo.get("SAMPLEPROCESSFLOWVERSION").toString();
				String sampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getOperFirstNodeStack(sampleFlow);
				
				Map<String, String> sampleFlowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(sampleNodeStack);
				String sampleOperationName = sampleFlowMap.get("PROCESSOPERATIONNAME");
				
				String nodeStack = "";

				if(StringUtil.isNotEmpty(sampleNodeStack))
				{
					
					for(int i=0; i<arrNodeStack.length; i++)
					{
						if(i==arrNodeStack.length-1)
						{
							nodeStack += beforeNode+".";
						}
						else
						{
							nodeStack += arrNodeStack[i]+".";
						}
					}

					nodeStack += sampleNodeStack;
				}

				MESLotServiceProxy.getLotServiceUtil().
						setEventInfoForSampling(lotData,beforeFlow,beforeFlowVersion,beforeOper,sampleFlow,sampleFlowVersion,eventInfo);

//				if(eventInfoForSample!=null)
//				{
//					Map<String, String> udfs = lotData.getUdfs();
//					udfs.put("NOTE", eventInfoForSample.getEventComment());
//
//					LotServiceProxy.getLotService().update(lotData);
//				}
				
				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());

				//Operation Changed, Update Product ProcessingInfo to N
				productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");

				//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
				ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfoForSampling(lotData.getKey().getLotName(),
						lotData.getProductionType(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
						"", lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
						lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
						lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						sampleFlow, sampleOperationName, "", "", beforeNode,
						lotData.getUdfs(), productUdfs,
						true, true);
				
				changeSpecInfo.setNodeStack(nodeStack);

				lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
				
				eventInfo.setEventUser(originalEventUser);

				/* Array 20180807, Add [Process Flag Update] ==>> */            
		        MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, lotData, logicalSlotMap, true);
		        /* <<== Array 20180807, Add [Process Flag Update] */
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 20180323 : setSampleLotData by Auto
	 */
	public SampleLot setSampleOutHoldFlag(EventInfo eventInfo, Lot lotData, SampleLotCount countInfo) throws CustomException
	{
		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		SampleLot sampleLot = new SampleLot();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? and processOperationName = ? and processOperationVersion = ? "
					+ "and machineName = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? and fromProcessOperationName = ? and (fromProcessOperationVersion = ? or fromProcessOperationVersion = ?)", new Object[] {lotData.getKey().getLotName(),
					lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(),lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), countInfo.getMachineName(),
					countInfo.getSampleProcessFlowName(), countInfo.getSampleProcessFlowVersion(), countInfo.getFromProcessOperationName(), countInfo.getFromProcessOperationVersion(), "*"});

			if(sampleLotList.size()==1)
			{
				eventInfo.setEventName("update SampleOutHoldFlag");
				
                eventInfo.setCheckTimekeyValidation(false);
                /* 20181128, hhlee, EventTime Sync */
                //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());

				sampleLot = sampleLotList.get(0);
				
				sampleLot.setSampleOutHoldFlag("Y");
				sampleLot.setLastEventName(eventInfo.getEventName());
				//2019.02.27_hsryu_Mantis 0002723. if SampleOutHold, remain EventUser . 
				//sampleLot.setLastEventUser(eventInfo.getEventUser());
				sampleLot.setLastEventComment(eventInfo.getEventComment());
				sampleLot.setLastEventTime(eventInfo.getEventTime());
				sampleLot.setLastEventTimekey(eventInfo.getEventTimeKey());

				sampleLot = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLot);
			}
		}
		catch ( Throwable e )
		{
			//not exist.
		}
		
		return sampleLot;
	}
	
	public CorresSampleLot setCorresSampleOutHoldFlag(EventInfo eventInfo, Lot lotData, SampleLotCount countInfo) throws CustomException
	{
		List<CorresSampleLot> corresSampleLotList = new ArrayList<CorresSampleLot>();
		CorresSampleLot corresSampleLot = new CorresSampleLot();

		try
		{
			corresSampleLotList = ExtendedObjectProxy.getCorresSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? and processOperationName = ? and processOperationVersion = ? and machineName = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? ", new Object[] {lotData.getKey().getLotName(),
					lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(),lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), countInfo.getMachineName(),
					countInfo.getSampleProcessFlowName(), countInfo.getSampleProcessFlowVersion()});

			if(corresSampleLotList.size()==1)
			{
				eventInfo.setEventName("update SampleOutHoldFlag by ReserveSampling");
				corresSampleLot = corresSampleLotList.get(0);
				
				corresSampleLot.setSampleOutHoldFlag("Y");
				corresSampleLot.setLastEventName(eventInfo.getEventName());
				//2019.02.27_hsryu_Mantis 0002723. if SampleOutHold, remain EventUser . 
				//corresSampleLot.setLastEventUser(eventInfo.getEventUser());
				corresSampleLot.setLastEventComment(eventInfo.getEventComment());
				corresSampleLot.setLastEventTime(eventInfo.getEventTime());
				corresSampleLot.setLastEventTimekey(eventInfo.getEventTimeKey());
				
				corresSampleLot = ExtendedObjectProxy.getCorresSampleLotService().modify(eventInfo, corresSampleLot);
			}
		}
		catch ( Throwable e )
		{
			//not exist.
		}
		
		return corresSampleLot;
	}

	private void TrackOutReport(EventInfo eventInfo ,String lotName) throws CustomException
	{
		try
		{
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			String LeadTime = "";
			String WaitTime = "";
			String ProcTime = "";
			
			// TK-OUT - IF(BEFORE TK-OUT NULL THEN Release TIME ELSE BEFORE TK-OUT END)  
			String strSql_LeadTime = 
					"    WITH LOT_LIST AS (" +
 					"                   SELECT ROWNUM NO, LAG(LASTLOGGEDOUTTIME) OVER(ORDER BY TIMEKEY ) BEFORT , LASTLOGGEDOUTTIME ,LOTNAME  " +					
					" 			         FROM LOTHISTORY  " +
					" 			        WHERE LOTNAME = :LOTNAME " +
					" 			         ORDER BY TIMEKEY )			" +
					" 			SELECT TO_CHAR(ROUND((LASTLOGGEDOUTTIME - BEFORT) * 24 *60 *60,2))  AS LEADTIME, BEFORT, LASTLOGGEDOUTTIME " +
					" 			FROM LOT_LIST " +
					" 			WHERE NO IN ( SELECT MAX(NO) NO FROM LOT_LIST ) " ;		
										 
			Map<String, Object> bindMap_LeadTime = new HashMap<String, Object>();
			bindMap_LeadTime.put("LOTNAME", lotName);
	
			List<Map<String, Object>> List_LeadTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_LeadTime, bindMap_LeadTime);

			//BEFORE TK-OUT - TK-IN
			String strSql_WaitTime = 
					" WITH LOT_TKIN AS (      "+    
					"                   SELECT LASTLOGGEDINTIME EVENTTIME ,LOTNAME  " +					
					" 			         FROM LOT  " +
					"      				WHERE  LOTNAME = :LOTNAME ) " +
					"       ,LOT_TKOUT AS (  "+
					"                   SELECT ROWNUM NO, LAG(LASTLOGGEDOUTTIME) OVER(ORDER BY TIMEKEY ) EVENTTIME ,LOTNAME  " +					
					" 			         FROM LOTHISTORY A " +
					" 			        WHERE LOTNAME = :LOTNAME " +
					" 			         ORDER BY TIMEKEY ) " +
					"      SELECT TO_CHAR(ROUND((A.EVENTTIME - B.EVENTTIME ) * 24 *60 *60,2)) WAITTIME  "+
					"      FROM  LOT_TKIN A "+
					"        INNER JOIN LOT_TKOUT B ON A.LOTNAME = B.LOTNAME "+
					"      WHERE 1=1  "+
					"         AND B.NO IN (SELECT MAX(NO) NO FROM LOT_TKOUT) " ;	 ;
	 
				Map<String, Object> bindMap_WaitTime = new HashMap<String, Object>();
				bindMap_WaitTime.put("LOTNAME", lotName);
		
				List<Map<String, Object>> List_WaitTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_WaitTime, bindMap_WaitTime);
								
				//TK-OUT - TK-IN
				String strSql_ProcTime = 	 
						"      SELECT  TO_CHAR(ROUND((LASTLOGGEDOUTTIME - LASTLOGGEDINTIME ) * 24 *60 *60,2)) PROCTIME  "+
						"       FROM LOT  "+
						"      WHERE  LOTNAME = :LOTNAME  ";
 
					Map<String, Object> bindMap_ProcTime = new HashMap<String, Object>();
					bindMap_ProcTime.put("LOTNAME", lotName);
					List<Map<String, Object>> List_ProcTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_ProcTime, bindMap_ProcTime);
					
	
				if(List_LeadTime != null && List_LeadTime.size() > 0)
				{	
					LeadTime = List_LeadTime.get(0).get("LEADTIME").toString();
				}
				
				if(List_WaitTime != null && List_WaitTime.size() > 0)
				{
					WaitTime = List_WaitTime.get(0).get("WAITTIME").toString();
				}
				
				if(List_ProcTime != null && List_ProcTime.size() > 0)
				{
					ProcTime = List_ProcTime.get(0).get("PROCTIME").toString();
				}
				
				LotHistoryKey lotHistoryKey = new LotHistoryKey();
				lotHistoryKey.setLotName(lotName);
				lotHistoryKey.setTimeKey(lotData.getLastEventTimeKey());

				//Lot History Update
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				LotHistory lotHistoryInfo = LotServiceProxy.getLotHistoryService().selectByKey(lotHistoryKey);
				LotHistory lotHistoryInfo = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
				
				lotHistoryInfo.getUdfs().put("LEADTIME", LeadTime);
				lotHistoryInfo.getUdfs().put("WAITTIME", WaitTime);
				lotHistoryInfo.getUdfs().put("PROCTIME", ProcTime);
				LotServiceProxy.getLotHistoryService().update(lotHistoryInfo);
								
				//Product Set 
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				List<Product> prdList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);
				List<Product> prdList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotName);
				
				for (Product prdData : prdList)
				{
					ProductHistoryKey productHistoryKey = new ProductHistoryKey();
		            productHistoryKey.setProductName(prdData.getKey().getProductName());
		            productHistoryKey.setTimeKey(prdData.getLastEventTimeKey());

		            // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
		            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
		            
					productHistory.getUdfs().put("LEADTIME", LeadTime);
					productHistory.getUdfs().put("WAITTIME", WaitTime);
					productHistory.getUdfs().put("PROCTIME", ProcTime);
					
			        ProductServiceProxy.getProductHistoryService().update(productHistory);
				}
		}
		catch (Exception e)
		{
			log.error("Report Update Fail!");
		}
	}
	
	public void checkTrackFlag(List<Element> productElementList, String machineName, boolean opiFlag, EventInfo eventInfo) throws CustomException 
	{
		for (Element productElement : productElementList)
		{
			String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			
			ProductFlag productFlag = new ProductFlag();
			
			try
			{
				productFlag = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {productData.getKey().getProductName()});
			}
			catch(Throwable e)
			{
				log.error(productData.getKey().getProductName() + " ProductFlag Data is not exist.");
			}
			
			if(productFlag!=null)
			{
				String originalTrackFlag = productFlag.getTrackFlag();

				// PHOTO TRCKFLAG MANAGEMENT
				try
				{
					Machine machineData = MESMachineServiceProxy.getMachineServiceUtil().getMachineData(machineName);
					MachineSpec machineSpec = CommonUtil.getMachineSpecByMachineName(machineName);

					if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_PHOTO))
					{
						//add condition 'StringUtil.isEmpty(processingInfo)' -> For OPI.
						if (StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_TRCK) && 
								(StringUtil.equals(processingInfo , GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P)))
						{
							//productUdfs.put("TRACKFLAG", "Y");
							productFlag.setTrackFlag("Y");
							productFlag.setLastEventUser(eventInfo.getEventUser());
							productFlag.setLastEventComment(eventInfo.getEventComment());
							productFlag.setLastEventTime(eventInfo.getEventTime());
							productFlag.setLastEventTimekey(eventInfo.getEventComment());
							ExtendedObjectProxy.getProductFlagService().modify(eventInfo, productFlag);
							log.info(productData.getKey().getProductName() + " TrackFlag is 'Y'");
						}
					}

					if(!opiFlag)
					{
						if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_PHOTO) ||
								StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_ETCH))
						{
							if ( StringUtil.equals(machineSpec.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP) && 
									(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P)))
							{
								//productUdfs.put("TRACKFLAG", "");
								productFlag.setTrackFlag("");
								productFlag.setLastEventUser(eventInfo.getEventUser());
								productFlag.setLastEventComment(eventInfo.getEventComment());
								productFlag.setLastEventTime(eventInfo.getEventTime());
								productFlag.setLastEventTimekey(eventInfo.getEventComment());
								ExtendedObjectProxy.getProductFlagService().modify(eventInfo, productFlag);
								log.info(productData.getKey().getProductName() + " TrackFlag is NULL");
							}
						}
					}
				}
				catch (Throwable e)
				{
					log.error("TRACKFLAG Fail!");
				}
			}
		}
	}

	public List<ProductPGSRC> setProductPGSRCForTrackOut(List<Element> productElementList) throws CustomException 
	{
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

		for (Element productElement : productElementList) 
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			ProductPGSRC productPGSRC = new ProductPGSRC();
			productPGSRC.setProductName(productName);

			String position = SMessageUtil.getChildText(productElement, "POSITION", true);
			productPGSRC.setPosition(Long.valueOf(position));

			String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);

			// 20170623 Modify by yudan
			if (StringUtil.isEmpty(productGrade))
			{
				productPGSRC.setProductGrade(productData.getProductGrade());
			}
			else 
			{
				/* 20190803, hhlee, add, Check DFS File Judge Update ==>> */
			    //if (productData.getReworkState().equals("InRework")) 
				//{
				//	productPGSRC.setProductGrade("R");
				//}
				//else if (productData.getReworkState().equals("NotInRework")) 
				//{
				//	productPGSRC.setProductGrade(productGrade);
				//}
			    productPGSRC.setProductGrade(productGrade);
			    /* <<== 20190803, hhlee, add, Check DFS File Judge Update */
			}

			productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
			productPGSRC.setReworkFlag("N");

			// Consumable ignored
			//productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

			Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(productElement, "Product");

			String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
			
			productUdfs.put("PROCESSINGINFO", processingInfo);
			productUdfs.put("REWORKGRADE",CommonUtil.getValue(productData.getUdfs(), "REWORKGRADE"));

			/* 20181113, hhlee, delete, ==>> */
			/* 20181023, hhlee, add, productjudge update ==>> */
			//productUdfs.put("PRODUCTJUDGE", productGrade);
			/* <<== 20181023, hhlee, add, productjudge update */
			/* <<== 20181113, hhlee, delete, */
			
			// 160521 by swcho : for ELA
			String lastELAProcessingTime = SMessageUtil.getChildText(productElement, "LASTPROCESSENDTIME", false);
			if (!lastELAProcessingTime.isEmpty())
			{
				productUdfs.put("LASTPROCESSENDTIME", lastELAProcessingTime);
			}
			
			String ELARecipeName = SMessageUtil.getChildText(productElement, "ELARECIPENAME", false);
			if (!ELARecipeName.isEmpty())
			{
				productUdfs.put("ELARECIPENAME", ELARecipeName);
			}
			
			String ELAEnergy = SMessageUtil.getChildText(productElement, "ELAENERGYUSAGE", false);
			if (!ELAEnergy.isEmpty())
			{
				productUdfs.put("ELAENERGYUSAGE", ELAEnergy);
			}

			productPGSRC.setUdfs(productUdfs);
			productPGSRCSequence.add(productPGSRC);
		}

		return productPGSRCSequence;
	}
	
	/**
	 * 
	 * @Name     setProductPGSRCForTrackOut
	 * @since    2019. 3. 8.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param productElementList
	 * @param detailOperationType
	 * @return
	 * @throws CustomException
	 */
	public List<ProductPGSRC> setProductPGSRCForTrackOut(List<Element> productElementList, String processOperationType) throws CustomException 
    {
        List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

        for (Element productElement : productElementList) 
        {
            String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
            Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

            ProductPGSRC productPGSRC = new ProductPGSRC();
            productPGSRC.setProductName(productName);

            String position = SMessageUtil.getChildText(productElement, "POSITION", true);
            productPGSRC.setPosition(Long.valueOf(position));

            String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);

            // 20170623 Modify by yudan
            if (StringUtil.isEmpty(productGrade))
            {
                productPGSRC.setProductGrade(productData.getProductGrade());
            }
            else 
            {
                /* 20190803, hhlee, add, Check DFS File Judge Update ==>> */
                //if (productData.getReworkState().equals("InRework")) 
                //{
                //  productPGSRC.setProductGrade("R");
                //}
                //else if (productData.getReworkState().equals("NotInRework")) 
                //{
                //  productPGSRC.setProductGrade(productGrade);
                //}
                
                productPGSRC.setProductGrade(productGrade);
                if(StringUtil.equals(processOperationType, GenericServiceProxy.getConstantMap().Pos_Inspection))
                {
                    if(this.checkLotGradeUpdateByDFSFileJudge(productName))
                    {
                        productPGSRC.setProductGrade(productData.getProductGrade());
                    }
                }
                /* <<== 20190803, hhlee, add, Check DFS File Judge Update */
            }

            /*  */
            
            productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
            productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
            productPGSRC.setReworkFlag("N");

            // Consumable ignored
            //productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

            Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(productElement, "Product");

            String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
            
            productUdfs.put("PROCESSINGINFO", processingInfo);
            productUdfs.put("REWORKGRADE",CommonUtil.getValue(productData.getUdfs(), "REWORKGRADE"));

            /* 20181113, hhlee, delete, ==>> */
            /* 20181023, hhlee, add, productjudge update ==>> */
            //productUdfs.put("PRODUCTJUDGE", productGrade);
            /* <<== 20181023, hhlee, add, productjudge update */
            /* <<== 20181113, hhlee, delete, */
            
            // 160521 by swcho : for ELA
            String lastELAProcessingTime = SMessageUtil.getChildText(productElement, "LASTPROCESSENDTIME", false);
            if (!lastELAProcessingTime.isEmpty())
            {
                productUdfs.put("LASTPROCESSENDTIME", lastELAProcessingTime);
            }
            
            String ELARecipeName = SMessageUtil.getChildText(productElement, "ELARECIPENAME", false);
            if (!ELARecipeName.isEmpty())
            {
                productUdfs.put("ELARECIPENAME", ELARecipeName);
            }
            
            String ELAEnergy = SMessageUtil.getChildText(productElement, "ELAENERGYUSAGE", false);
            if (!ELAEnergy.isEmpty())
            {
                productUdfs.put("ELAENERGYUSAGE", ELAEnergy);
            }

            productPGSRC.setUdfs(productUdfs);
            productPGSRCSequence.add(productPGSRC);
        }

        return productPGSRCSequence;
    }
	
	/**
	 * 
	 * @Name     setProductPGSRCForTrackOut
	 * @since    2019. 3. 20.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param productElementList
	 * @param processOperationType
	 * @return
	 * @throws CustomException
	 */
	public List<ProductPGSRC> setProductPGSRCForTrackOut(List<Element> productElementList, 
	        String processOperationType, String detailProcessOperationType) throws CustomException 
    {
        List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

        for (Element productElement : productElementList) 
        {
            String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
            Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

            ProductPGSRC productPGSRC = new ProductPGSRC();
            productPGSRC.setProductName(productName);

            String position = SMessageUtil.getChildText(productElement, "POSITION", true);
            productPGSRC.setPosition(Long.valueOf(position));

            String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);

            /* 20190321, hhlee, modify, ==>> */
            //// 20170623 Modify by yudan
            //if (StringUtil.isEmpty(productGrade))
            //{
            //    productPGSRC.setProductGrade(productData.getProductGrade());
            //}
            //else 
            //{
            //    /* 20190803, hhlee, add, Check DFS File Judge Update ==>> */
            //    //if (productData.getReworkState().equals("InRework")) 
            //    //{
            //    //  productPGSRC.setProductGrade("R");
            //    //}
            //    //else if (productData.getReworkState().equals("NotInRework")) 
            //    //{
            //    //  productPGSRC.setProductGrade(productGrade);
            //    //}
            //    
            //    productPGSRC.setProductGrade(productGrade);
            //    if(StringUtil.equals(processOperationType, GenericServiceProxy.getConstantMap().Pos_Inspection))
            //    {
            //        /* 20190320, hhlee, modify, DetatilOperationType = 'REP', ProductGrade 'P' -> 'G' Update ==>> */
            //        if(StringUtil.equals(detailProcessOperationType, 
            //                GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
            //        {
            //            if(StringUtil.equals(productData.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_P))
            //            {
            //                productPGSRC.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
            //            }
            //        }
            //        else
            //        {
            //            if(this.checkLotGradeUpdateByDFSFileJudge(productName))
            //            {
            //                productPGSRC.setProductGrade(productData.getProductGrade());
            //            }
            //        }
            //        /* <<== 20190320, hhlee, modify, DetatilOperationType = 'REP', ProductGrade 'P' -> 'G' Update */
            //    }                
            //    /* <<== 20190803, hhlee, add, Check DFS File Judge Update */
            //}
            
            if(StringUtil.equals(processOperationType, GenericServiceProxy.getConstantMap().Pos_Inspection))
            {
                if(this.checkLotGradeUpdateByDFSFileJudge(productName))
                {
                    productPGSRC.setProductGrade(productData.getProductGrade());
                }
            }
            else
            {
                if (StringUtil.isEmpty(productGrade))
                {
                    productPGSRC.setProductGrade(productData.getProductGrade());
                }
                else 
                {
                    productPGSRC.setProductGrade(productGrade);
                }
            }
                        
            /* 20190320, hhlee, modify, DetatilOperationType = 'REP', ProductGrade 'P' -> 'G' Update ==>> */
            if(StringUtil.equals(processOperationType, GenericServiceProxy.getConstantMap().Pos_Inspection) &&
                    StringUtil.equals(detailProcessOperationType, GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
            {
                if(StringUtil.equals(productData.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_P))
                {
                    productPGSRC.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
                }
            }
            /* <<== 20190320, hhlee, modify, DetatilOperationType = 'REP', ProductGrade 'P' -> 'G' Update */
            /* <<== 20190321, hhlee, modify, */
            
            productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
            productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
            productPGSRC.setReworkFlag("N");

            // Consumable ignored
            //productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

            Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(productElement, "Product");

            String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
            
            productUdfs.put("PROCESSINGINFO", processingInfo);
            productUdfs.put("REWORKGRADE",CommonUtil.getValue(productData.getUdfs(), "REWORKGRADE"));

            /* 20181113, hhlee, delete, ==>> */
            /* 20181023, hhlee, add, productjudge update ==>> */
            //productUdfs.put("PRODUCTJUDGE", productGrade);
            /* <<== 20181023, hhlee, add, productjudge update */
            /* <<== 20181113, hhlee, delete, */
            
            // 160521 by swcho : for ELA
            String lastELAProcessingTime = SMessageUtil.getChildText(productElement, "LASTPROCESSENDTIME", false);
            if (!lastELAProcessingTime.isEmpty())
            {
                productUdfs.put("LASTPROCESSENDTIME", lastELAProcessingTime);
            }
            
            String ELARecipeName = SMessageUtil.getChildText(productElement, "ELARECIPENAME", false);
            if (!ELARecipeName.isEmpty())
            {
                productUdfs.put("ELARECIPENAME", ELARecipeName);
            }
            
            String ELAEnergy = SMessageUtil.getChildText(productElement, "ELAENERGYUSAGE", false);
            if (!ELAEnergy.isEmpty())
            {
                productUdfs.put("ELAENERGYUSAGE", ELAEnergy);
            }

            productPGSRC.setUdfs(productUdfs);
            productPGSRCSequence.add(productPGSRC);
        }

        return productPGSRCSequence;
    }
	
	public List<ProductPGSRC> setProductPGSRCForTrackOutForOPI(Lot lotData, List<Element> productElementList) throws CustomException 
	{
		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
        
		ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());

		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

		for (Element productElement : productElementList) 
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			ProductPGSRC productPGSRC = new ProductPGSRC();
			productPGSRC.setProductName(productName);

			String position = SMessageUtil.getChildText(productElement, "POSITION", true);
			productPGSRC.setPosition(Long.valueOf(position));

			// 2019.05.30_hsryu_Insert Logic. Same PEX Logic. 
            if(StringUtil.equals(operationData.getProcessOperationType(), GenericServiceProxy.getConstantMap().Pos_Inspection) &&
                    StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
            {
                if(StringUtil.equals(productData.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_P))
                {
                    productPGSRC.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
                }
            }

			productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
			productPGSRC.setReworkFlag("N");

			// Consumable ignored
			//productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

			Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(productElement, "Product");

			String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
			
			productUdfs.put("PROCESSINGINFO", processingInfo);
			productUdfs.put("REWORKGRADE",CommonUtil.getValue(productData.getUdfs(), "REWORKGRADE"));
			
			if(this.isDummyOperation(productData.getFactoryName(), productData.getProcessOperationName())){
				productUdfs.put("FILEJUDGE", "");	
			}
			productPGSRC.setUdfs(productUdfs);
			productPGSRCSequence.add(productPGSRC);
		}

		return productPGSRCSequence;
	}
	
	public String getLastSeqOfLotMultiHold(Lot lotData, String flowName, String OperationName)
	{
		String getPositionSql = "SELECT POSITION "
				+ " FROM CT_LOTACTION "
				+ " WHERE LOTNAME = :LOTNAME "
				+ " AND FACTORYNAME = :FACTORYNAME "
				+ " AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ " AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				+ " ORDER BY POSITION DESC";

		Map<String, Object> getPositionBind = new HashMap<String, Object>();
		getPositionBind.put("LOTNAME", lotData.getKey().getLotName());
		getPositionBind.put("FACTORYNAME", lotData.getFactoryName());
		getPositionBind.put("PROCESSFLOWNAME", flowName);
		getPositionBind.put("PROCESSFLOWVERSION", "00001");
		getPositionBind.put("PROCESSOPERATIONNAME", OperationName);
		getPositionBind.put("PROCESSOPERATIONVERSION", "00001");

		List<Map<String, Object>> positionSqlBindSet = GenericServiceProxy.getSqlMesTemplate().queryForList(getPositionSql, getPositionBind);

		if(positionSqlBindSet.size() == 0)
		{
			return "0";
		}

		return positionSqlBindSet.get(0).get("POSITION").toString();
	}
	
	public void DeleteSamplingInfo(Lot lotData, String currentNodeStack, String beforeNodeStack, EventInfo eventInfo) throws CustomException
	{
		log.info("Start DeleteSamplingInfo");
		
		String sampleOutHoldFlag = "";
		String currentFlowSql = "SELECT N.NODEID, N.NODEATTRIBUTE1, N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION "
				+ " FROM NODE N "
				+ " WHERE N.NODEID = :NODEID ";

		Map<String, Object> currentFlowBindSet = new HashMap<String, Object>();
		currentFlowBindSet.put("NODEID", currentNodeStack);

		List<Map<String, Object>> currentFlowSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(currentFlowSql, currentFlowBindSet);

		String beforeFlowSql = "SELECT N.NODEID, N.NODEATTRIBUTE1, N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION "
				+ " FROM NODE N "
				+ " WHERE N.NODEID = :NODEID ";

		Map<String, Object> beforeFlowBindSet = new HashMap<String, Object>();
		beforeFlowBindSet.put("NODEID", beforeNodeStack);

		List<Map<String, Object>> beforeFlowSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(beforeFlowSql, beforeFlowBindSet);

		if ( currentFlowSqlResult.size() > 0 && beforeFlowSqlResult.size() > 0)
		{
			String currentProcessFlowName = (String) currentFlowSqlResult.get(0).get("PROCESSFLOWNAME");
			String currentProcessFlowVersion = (String) currentFlowSqlResult.get(0).get("PROCESSFLOWVERSION");
			String currentProcessOperationName = (String) currentFlowSqlResult.get(0).get("NODEATTRIBUTE1");

			String beforeProcessFlowName = (String) beforeFlowSqlResult.get(0).get("PROCESSFLOWNAME");
			String beforeProcessFlowVersion = (String) beforeFlowSqlResult.get(0).get("PROCESSFLOWVERSION");
			String beforeProcessOperationName = (String) beforeFlowSqlResult.get(0).get("NODEATTRIBUTE1");

			String sampleLotSql = "SELECT SAMPLEOUTHOLDFLAG, POSITION, TYPE "
					+ " FROM (SELECT LA.SAMPLEOUTHOLDFLAG, LA.POSITION, 'RESERVE' TYPE "
					+ "         FROM CT_LOTACTION LA "
					+ "        WHERE 1 = 1 "
					+ "          AND LA.LOTNAME = :LOTNAME "
					+ "          AND LA.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "          AND LA.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
					+ "          AND LA.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
					+ "          AND LA.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
					+ "          AND LA.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
					+ "          AND LA.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION "
					+ "          AND (LA.ACTIONSTATE = :ACTIONSTATE OR LA.ACTIONSTATE = :ACTIONSTATE2) "
					+ "          AND LA.ACTIONNAME = :ACTIONNAME "
					+ "       UNION "
					+ "       SELECT SL.SAMPLEOUTHOLDFLAG, SLC.SAMPLEPRIORITY AS POSITION, 'AUTO' TYPE "
					+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_SAMPLELOT SL "
					+ "        WHERE 1 = 1 "
					+ "          AND SLC.FACTORYNAME = SL.FACTORYNAME "
					+ "          AND SLC.PRODUCTSPECNAME = SL.PRODUCTSPECNAME "
					+ "          AND SLC.ECCODE = SL.ECCODE "
					+ "          AND SLC.PROCESSFLOWNAME = SL.PROCESSFLOWNAME "
					+ "          AND SLC.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION "
					+ "          AND SLC.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME "
					+ "          AND SLC.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION "
					+ "          AND SLC.PROCESSOPERATIONNAME = SL.PROCESSOPERATIONNAME "
					+ "          AND SLC.PROCESSOPERATIONVERSION = SL.PROCESSOPERATIONVERSION "
					+ "          AND SL.LOTNAME = :LOTNAME "
					+ "          AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
					+ "          AND SL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION "
					+ "          AND SL.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
					+ "          AND SL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION "
					+ "          AND SL.SAMPLEFLAG = :SAMPLEFLAG "
					+ "          AND SL.SAMPLESTATE = :SAMPLESTATE "
					//------------------------------------------------------------------------------------------------
					// Added by smkang on 2018.10.20 - Force sampling situation is missed.
					+ "       UNION "
					+ "       SELECT SL.SAMPLEOUTHOLDFLAG, 1 AS POSITION, 'MANUAL' TYPE "
					+ "         FROM CT_SAMPLELOT SL "
					+ "        WHERE 1 = 1 "
					+ "          AND SL.LOTNAME = :LOTNAME "
					+ "          AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
					+ "          AND SL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION "
					+ "          AND SL.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
					+ "          AND SL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION "
					+ "          AND SL.SAMPLEFLAG = :SAMPLEFLAG "
					+ "          AND SL.SAMPLESTATE = :SAMPLESTATE "
					+ "          AND SL.MANUALSAMPLEFLAG = :MANUALSAMPLEFLAG "
					//------------------------------------------------------------------------------------------------
					+ "       UNION "
					+ "       SELECT CSL.SAMPLEOUTHOLDFLAG, SLC.CORRESSAMPLEPRIORITY AS POSITION, 'CORRES' TYPE "
					+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_CORRESSAMPLELOT CSL "
					+ "        WHERE 1 = 1 "
					+ "          AND SLC.FACTORYNAME = CSL.FACTORYNAME "
					+ "          AND SLC.PRODUCTSPECNAME = CSL.PRODUCTSPECNAME "
					+ "          AND SLC.ECCODE = CSL.ECCODE "
					+ "          AND SLC.PROCESSFLOWNAME = CSL.PROCESSFLOWNAME "
					+ "          AND SLC.PROCESSFLOWVERSION = CSL.PROCESSFLOWVERSION "
					+ "          AND SLC.PROCESSOPERATIONNAME = CSL.PROCESSOPERATIONNAME "
					+ "          AND SLC.PROCESSOPERATIONVERSION = CSL.PROCESSOPERATIONVERSION "
					+ "          AND SLC.CORRESSAMPLEPROCESSFLOWNAME = CSL.SAMPLEPROCESSFLOWNAME "
					+ "          AND SLC.CORRESSAMPLEPROCESSFLOWVERSION = CSL.SAMPLEPROCESSFLOWVERSION "
					+ "          AND SLC.CORRESPROCESSOPERATIONNAME = CSL.FROMPROCESSOPERATIONNAME "
					+ "          AND SLC.CORRESPROCESSOPERATIONVERSION = CSL.FROMPROCESSOPERATIONVERSION "
					+ "          AND CSL.LOTNAME = :LOTNAME "
					+ "          AND CSL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
					+ "          AND CSL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION "
					+ "          AND CSL.SAMPLEFLAG = :SAMPLEFLAG "
					+ "          AND CSL.SAMPLESTATE = :SAMPLESTATE) "
					+ "  ORDER BY DECODE(TYPE, 'RESERVE', 0, 'AUTO', 1,2), POSITION ASC ";

			Map<String, Object> smapleLotBindSet = new HashMap<String, Object>();
			smapleLotBindSet.put("LOTNAME", lotData.getKey().getLotName());
			smapleLotBindSet.put("PROCESSFLOWNAME", beforeProcessFlowName);
			smapleLotBindSet.put("PROCESSFLOWVERSION", beforeProcessFlowVersion);
			smapleLotBindSet.put("PROCESSOPERATIONNAME", beforeProcessOperationName);
			smapleLotBindSet.put("PROCESSOPERATIONVERSION", "00001");
			smapleLotBindSet.put("SAMPLEPROCESSFLOWNAME", currentProcessFlowName);
			smapleLotBindSet.put("SAMPLEPROCESSFLOWVERSION", currentProcessFlowVersion);
			smapleLotBindSet.put("ACTIONSTATE", "Executed");
			smapleLotBindSet.put("ACTIONNAME", "Sampling");
			smapleLotBindSet.put("FROMPROCESSOPERATIONNAME", beforeProcessOperationName);
			smapleLotBindSet.put("FROMPROCESSOPERATIONVERSION", "00001");
			smapleLotBindSet.put("SAMPLEFLAG", "Y");
			smapleLotBindSet.put("SAMPLESTATE", "Executing");
			smapleLotBindSet.put("ACTIONSTATE2", "Merged");
			
			// Added by smkang on 2018.10.20 - Force sampling situation is missed.
			smapleLotBindSet.put("MANUALSAMPLEFLAG", "ForceSampling");

			List<Map<String, Object>> sampleLotSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sampleLotSql, smapleLotBindSet);

			if ( sampleLotSqlResult.size() > 0 )
			{
				for(int i=0; i<sampleLotSqlResult.size(); i++)
				{
					String type = sampleLotSqlResult.get(i).get("TYPE").toString();

					// Modified by smkang on 2018.10.20 - Force sampling situation is missed.
//					if(StringUtil.equals(type, "AUTO"))
					if(StringUtil.equals(type, "AUTO") || StringUtil.equals(type, "MANUAL"))
					{
						List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

						String condition = " WHERE lotName = ? AND factoryName = ? AND productSpecName = ? AND ecCode = ? AND processFlowName = ? AND processFlowVersion = ? "
								+ " AND fromProcessOperationName = ? AND fromProcessOperationVersion = ? AND sampleProcessFlowName = ? AND sampleProcessFlowVersion = ? AND sampleFlag = ? AND sampleState = ? ";
						Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"),
								beforeProcessFlowName, beforeProcessFlowVersion, beforeProcessOperationName, "00001", currentProcessFlowName, "00001", "Y", "Executing" };

						try
						{
							eventInfo.setEventName("CompleteSampleLot");
							sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);

							SampleLot sampleLot = new SampleLot();
							sampleLot = sampleLotList.get(0);

							sampleLot.setSampleState("Completed");
							sampleLot.setLastEventName(eventInfo.getEventName());
							//2019.02.27_hsryu_Mantis 0002723. if SampleOutHold, remain EventUse
							//sampleLot.setLastEventUser(eventInfo.getEventUser());
							sampleLot.setLastEventComment(eventInfo.getEventComment());
							sampleLot.setLastEventTime(eventInfo.getEventTime());
							sampleLot.setLastEventTimekey(eventInfo.getEventTimeKey());
							sampleLot = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLot);
							
							ExtendedObjectProxy.getSampleLotService().delete(sampleLot);
							
							log.info("Delete SamplingInfo [AUTO OR MANUAL]");
						}
						catch(Exception ex)
						{
							log.info("Not Find Sampling in CT_SAMPLELOT Table.");
						}
					}
					else if(StringUtil.equals(type, "RESERVE"))
					{
						List<LotAction> sampleActionList = new ArrayList<LotAction>();
						String position = sampleLotSqlResult.get(i).get("POSITION").toString();

						String condition = " WHERE 1=1 AND lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND processOperationName = ?"
								+ " AND processOperationVersion = ? AND position = ? AND sampleProcessFlowName = ? AND sampleProcessFlowVersion = ? AND actionName = ? AND (actionState = ? OR actionState = ?) ";
						Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), beforeProcessFlowName, beforeProcessFlowVersion,
								beforeProcessOperationName, "00001", Integer.parseInt(position), currentProcessFlowName, "00001", "Sampling", "Executed", "Merged" };
						try
						{
							eventInfo.setEventName("CompleteSampleLot");
							sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);

							LotAction lotAction = new LotAction();
							lotAction = sampleActionList.get(0);

							lotAction.setActionState("Completed");
							lotAction.setLastEventTime(eventInfo.getEventTime());
							lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
							//2019.02.27_hsryu_Mantis 0002723. if SampleOutHold, remain EventUse
							//lotAction.setLastEventUser(eventInfo.getEventUser());
							lotAction.setLastEventComment(eventInfo.getEventComment());
							ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotAction);
							
							ExtendedObjectProxy.getLotActionService().delete(lotAction);
							
							log.info("Delete SamplingInfo [RESERVE]");
						}
						catch(Exception ex)
						{
							log.info("Not Find Sampling in CT_LOTACTION Table.");
						}
					}
					else if(StringUtil.equals(type, "CORRES"))
					{
						List<CorresSampleLot> corresSampleLotList = new ArrayList<CorresSampleLot>();

						String condition = " WHERE lotName = ? AND factoryName = ? AND productSpecName = ? AND ecCode = ? AND processFlowName = ? AND processFlowVersion = ? "
								+ " AND fromProcessOperationName = ? AND fromProcessOperationVersion = ? AND sampleProcessFlowName = ? AND sampleProcessFlowVersion = ? AND sampleFlag = ? AND sampleState = ? ";
						Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"),
								beforeProcessFlowName, beforeProcessFlowVersion, beforeProcessOperationName, "00001", currentProcessFlowName, "00001", "Y", "Executing" };

						try
						{
							eventInfo.setEventName("CompleteSampleLot");
							corresSampleLotList = ExtendedObjectProxy.getCorresSampleLotService().select(condition, bindSet);

							CorresSampleLot corresSampleLot = new CorresSampleLot();
							corresSampleLot = corresSampleLotList.get(0);

							corresSampleLot.setSampleState("Completed");
							corresSampleLot.setLastEventName(eventInfo.getEventName());
							//2019.02.27_hsryu_Mantis 0002723. if SampleOutHold, remain EventUse
							//corresSampleLot.setLastEventUser(eventInfo.getEventUser());
							corresSampleLot.setLastEventComment(eventInfo.getEventComment());
							corresSampleLot.setLastEventTime(eventInfo.getEventTime());
							corresSampleLot.setLastEventTimekey(eventInfo.getEventTimeKey());
							corresSampleLot = ExtendedObjectProxy.getCorresSampleLotService().modify(eventInfo, corresSampleLot);
							
							ExtendedObjectProxy.getCorresSampleLotService().delete(corresSampleLot);
							
							log.info("Delete SamplingInfo [CORRES]");
						}
						catch(Exception ex)
						{
							log.info("Not Find Sampling in CT_SAMPLELOT Table.");
						}
					}
				}
			}
		}
	}
	

	public void checkFirstLotFlagByRecipeIdleTime(String machineName, String recipeName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
    {	
    	if(!StringUtils.isEmpty(machineName) && !StringUtils.isEmpty(recipeName))
    	{
    		RecipeIdleTime recipeIdleTime = null;

    		try
    		{
    			recipeIdleTime = ExtendedObjectProxy.getRecipeIdleTimeService().selectByKey(false, new Object[] {machineName, recipeName});
    		}
    		catch (Exception ex)
    		{
    			recipeIdleTime = null;
    		}

    		if(recipeIdleTime == null)
    		{
    			try
    			{
    				recipeIdleTime = ExtendedObjectProxy.getRecipeIdleTimeService().selectByKey(false, new Object[] {machineName, "*"});
    			}
    			catch (Exception ex)
    			{
    				recipeIdleTime = null;
    			}
    		}

    		if(recipeIdleTime != null)
    		{
    			if(StringUtils.equals(recipeIdleTime.getvalidFlag(), "Y"))
    			{
    				RecipeIdleTimeLot recipeIdleTimeLotData = null;

    				try
    				{
    					recipeIdleTimeLotData = ExtendedObjectProxy.getRecipeIdleTimeLotService().selectByKey(false, new Object[] {machineName, recipeName});
    				}
    				catch (Exception ex)
    				{
    					recipeIdleTimeLotData = null;
    				}

    				if(recipeIdleTimeLotData != null)
    				{
    					if(StringUtils.equals(recipeIdleTimeLotData.getfirstLotFlag(), "Y"))
    					{
    						throw new CustomException("RECIPE-0012", machineName, recipeName);
    					}
    				}
    			}
    		}
    	}
    }

	/**
	 * @author smkang
	 * @since 2018.11.26
	 * @param machineName
	 * @return availableLotListToRun
	 */
	public List<String> getAvailableLotToRun(String machineName) {
//		-- SEARCHING AVAILABLE LOT WITH MACHINE
//		SELECT L.LOTNAME, M.MACHINENAME
//		  FROM TOPOLICY TOP, POSOPERATIONMODE OM, MACHINE M, LOT L
//		 WHERE TOP.CONDITIONID = OM.CONDITIONID
//		   AND OM.MACHINENAME = M.MACHINENAME
//		   AND OM.OPERATIONMODE = M.OPERATIONMODE
//		   AND TOP.FACTORYNAME = L.FACTORYNAME
//		   AND TOP.PROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME
//		   AND TOP.PROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION
//		   AND M.MACHINENAME = :MACHINENAME
//		 UNION 
//		SELECT L.LOTNAME, MG.MACHINENAME
//		  FROM PROCESSOPERATIONSPEC POS, CT_MACHINEGROUPMACHINE MG, LOT L
//		 WHERE POS.MACHINEGROUPNAME = MG.MACHINEGROUPNAME
//		   AND POS.FACTORYNAME = L.FACTORYNAME
//		   AND POS.PROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME
//		   AND POS.PROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION
//		   AND MG.MACHINENAME = :MACHINENAME;
		StringBuilder sqlStatement = new StringBuilder();
		sqlStatement.append("SELECT L.LOTNAME, M.MACHINENAME");
		sqlStatement.append("  FROM TOPOLICY TOP, POSOPERATIONMODE OM, MACHINE M, LOT L");
		sqlStatement.append(" WHERE TOP.CONDITIONID = OM.CONDITIONID");
		sqlStatement.append("   AND OM.MACHINENAME = M.MACHINENAME");
		sqlStatement.append("   AND OM.OPERATIONMODE = M.OPERATIONMODE");
		sqlStatement.append("   AND TOP.FACTORYNAME = L.FACTORYNAME");
		sqlStatement.append("   AND TOP.PROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME");
		sqlStatement.append("   AND TOP.PROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION");
		sqlStatement.append("   AND M.MACHINENAME = :MACHINENAME");
		sqlStatement.append(" UNION ");
		sqlStatement.append("SELECT L.LOTNAME, MG.MACHINENAME");
		sqlStatement.append("  FROM PROCESSOPERATIONSPEC POS, CT_MACHINEGROUPMACHINE MG, LOT L");
		sqlStatement.append(" WHERE POS.MACHINEGROUPNAME = MG.MACHINEGROUPNAME");
		sqlStatement.append("   AND POS.FACTORYNAME = L.FACTORYNAME");
		sqlStatement.append("   AND POS.PROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME");
		sqlStatement.append("   AND POS.PROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION");
		sqlStatement.append("   AND MG.MACHINENAME = :MACHINENAME");
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("MACHINENAME", machineName);
		
		List<Map<String, Object>> queryResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStatement.toString(), bindMap);
		
		List<String> availableLotListToRun = new ArrayList<String>();
		if (queryResult != null && queryResult.size() > 0) {
			for (Map<String, Object> resultRow : queryResult) {
				if (resultRow.get("LOTNAME") != null)
					availableLotListToRun.add((String) resultRow.get("LOTNAME"));
			}
		}
		
		return availableLotListToRun;
	}
	
	/**
	 * @author smkang
	 * @since 2018.11.26
	 * @param lotName
	 * @return availableMachineListToRun
	 */
	public List<String> getAvailableMachineToRun(String lotName) {
//		-- SEARCHING AVAILABLE MACHINE WITH LOT
//		SELECT L.LOTNAME, M.MACHINENAME
//		  FROM TOPOLICY TOP, POSOPERATIONMODE OM, MACHINE M, LOT L
//		 WHERE TOP.CONDITIONID = OM.CONDITIONID
//		   AND OM.MACHINENAME = M.MACHINENAME
//		   AND OM.OPERATIONMODE = M.OPERATIONMODE
//		   AND TOP.FACTORYNAME = L.FACTORYNAME
//		   AND TOP.PROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME
//		   AND TOP.PROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION
//		   AND L.LOTNAME = :LOTNAME
//		 UNION 
//		SELECT L.LOTNAME, MG.MACHINENAME
//		  FROM PROCESSOPERATIONSPEC POS, CT_MACHINEGROUPMACHINE MG, LOT L
//		 WHERE POS.MACHINEGROUPNAME = MG.MACHINEGROUPNAME
//		   AND POS.FACTORYNAME = L.FACTORYNAME
//		   AND POS.PROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME
//		   AND POS.PROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION
//		   AND L.LOTNAME = :LOTNAME;
		StringBuilder sqlStatement = new StringBuilder();
		sqlStatement.append("SELECT L.LOTNAME, M.MACHINENAME");
		sqlStatement.append("  FROM TOPOLICY TOP, POSOPERATIONMODE OM, MACHINE M, LOT L");
		sqlStatement.append(" WHERE TOP.CONDITIONID = OM.CONDITIONID");
		sqlStatement.append("   AND OM.MACHINENAME = M.MACHINENAME");
		sqlStatement.append("   AND OM.OPERATIONMODE = M.OPERATIONMODE");
		sqlStatement.append("   AND TOP.FACTORYNAME = L.FACTORYNAME");
		sqlStatement.append("   AND TOP.PROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME");
		sqlStatement.append("   AND TOP.PROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION");
		sqlStatement.append("   AND L.LOTNAME = :LOTNAME");
		sqlStatement.append(" UNION ");
		sqlStatement.append("SELECT L.LOTNAME, MG.MACHINENAME");
		sqlStatement.append("  FROM PROCESSOPERATIONSPEC POS, CT_MACHINEGROUPMACHINE MG, LOT L");
		sqlStatement.append(" WHERE POS.MACHINEGROUPNAME = MG.MACHINEGROUPNAME");
		sqlStatement.append("   AND POS.FACTORYNAME = L.FACTORYNAME");
		sqlStatement.append("   AND POS.PROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME");
		sqlStatement.append("   AND POS.PROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION");
		sqlStatement.append("   AND L.LOTNAME = :LOTNAME");
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("LOTNAME", lotName);
		
		List<Map<String, Object>> queryResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStatement.toString(), bindMap);
		
		List<String> availableMachineListToRun = new ArrayList<String>();
		if (queryResult != null && queryResult.size() > 0) {
			for (Map<String, Object> resultRow : queryResult) {
				if (resultRow.get("MACHINENAME") != null)
					availableMachineListToRun.add((String) resultRow.get("MACHINENAME"));
			}
		}
		
		return availableMachineListToRun;
	}
	
	/**
	 * @author smkang
	 * @since 2018.11.26
	 * @param lotName
	 * @param machineName
	 * @return boolean
	 */
	public boolean possibleToOPIRun(String lotName, String machineName) {
		StringBuilder sqlStatement = new StringBuilder();
		sqlStatement.append("SELECT L.LOTNAME, M.MACHINENAME");
		sqlStatement.append("  FROM TOPOLICY TOP, POSOPERATIONMODE OM, MACHINE M, LOT L");
		sqlStatement.append(" WHERE TOP.CONDITIONID = OM.CONDITIONID");
		sqlStatement.append("   AND OM.MACHINENAME = M.MACHINENAME");
		sqlStatement.append("   AND TOP.FACTORYNAME = L.FACTORYNAME");
		sqlStatement.append("   AND TOP.PROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME");
		sqlStatement.append("   AND TOP.PROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION");
		sqlStatement.append("   AND L.LOTNAME = :LOTNAME");
		sqlStatement.append("   AND M.MACHINENAME = :MACHINENAME");
		sqlStatement.append(" UNION ");
		sqlStatement.append("SELECT L.LOTNAME, MG.MACHINENAME");
		sqlStatement.append("  FROM PROCESSOPERATIONSPEC POS, CT_MACHINEGROUPMACHINE MG, LOT L");
		sqlStatement.append(" WHERE POS.MACHINEGROUPNAME = MG.MACHINEGROUPNAME");
		sqlStatement.append("   AND POS.FACTORYNAME = L.FACTORYNAME");
		sqlStatement.append("   AND POS.PROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME");
		sqlStatement.append("   AND POS.PROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION");
		sqlStatement.append("   AND L.LOTNAME = :LOTNAME");
		sqlStatement.append("   AND MG.MACHINENAME = :MACHINENAME");
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("MACHINENAME", machineName);
		
		List<Map<String, Object>> queryResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStatement.toString(), bindMap);
		
		return (queryResult != null && queryResult.size() > 0);
	}
	
	
	/**
	 * 
	 * @Name     validateReserveLot
	 * @since    2018. 12. 3.
	 * @author   hhlee
	 * @contents Validate Lot
	 *           Lot, CT_ReserveLot
	 * @param machineName
	 * @param lotName
	 * @param productRequestName
	 * @return
	 * @throws CustomException
	 */
	public boolean validateReserveLot(String machineName, String lotName, String productRequestName) throws CustomException
    {
        try
        {
            String strSql = StringUtil.EMPTY;
            strSql = strSql + " SELECT RL.MACHINENAME, L.LOTNAME, RL.PRODUCTREQUESTNAME \n"
                            + "   FROM CT_RESERVELOT RL, LOT L                          \n"
                            + "  WHERE 1=1                                              \n"
                            + "    AND RL.MACHINENAME = :MACHINENAME                    \n"
                            + "    AND RL.LOTNAME = :LOTNAME                            \n"
                            + "    AND RL.RESERVESTATE = :RESERVESTATE                  \n"
                            + "    AND RL.PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME      \n"
                            + "    AND RL.LOTNAME = L.LOTNAME                           \n"
                            + "    AND RL.PRODUCTREQUESTNAME = L.PRODUCTREQUESTNAME     \n"
                            + "    AND L.LOTSTATE = :LOTSTATE                           \n";
                        
            Map<String, Object> bindMap = new HashMap<String, Object>();

            bindMap.put("MACHINENAME", machineName);
            bindMap.put("LOTNAME", lotName);
            bindMap.put("RESERVESTATE", GenericServiceProxy.getConstantMap().RESV_LOT_STATE_START);
            bindMap.put("PRODUCTREQUESTNAME", productRequestName);
            bindMap.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Created);

            List<Map<String, Object>> reservelotList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

            if(reservelotList != null && reservelotList.size() <= 0)
            {
                throw new CustomException("LOT-0218", machineName, productRequestName);
            }

            return true;

        }
        catch (Exception ex)
        {
            throw new CustomException("LOT-0218", machineName, productRequestName);
        }
    }
	
	/**
	 * 
	 * @Name     clearCarrerNameOnAutoMQCSetting
	 * @since    2018. 12. 4.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param eventInfo
	 * @param carrerName
	 * @param machineName
	 */
	public void clearCarrerNameOnAutoMQCSetting(EventInfo eventInfo, String carrerName,String machineName )
    {
        //EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
	    eventInfo.setEventName("TrackOut");
	    
	    String condition = " WHERE MACHINENAME = ? AND CARRIERNAME = ? ";
        Object[] bindSet = new Object[] { machineName ,  carrerName};
        
        try {
            List<AutoMQCSetting> autoMQCSettingList = ExtendedObjectProxy.getAutoMQCSettingService().select(condition, bindSet);
            for(AutoMQCSetting autoMQCSetting : autoMQCSettingList)
            {
                autoMQCSetting.setCarrierName("");
                autoMQCSetting.setLastRunTime(eventInfo.getEventTime());
                
                ExtendedObjectProxy.getAutoMQCSettingService().modify(eventInfo, autoMQCSetting);
            }
        } 
        /* 20181204, hhlee, Modify, add exception code ==>> */
        catch (NotFoundSignal ne)
        {
            // TODO Auto-generated catch block
            ne.printStackTrace();           
        } 
        catch (FrameworkErrorSignal fe)
        {
            // TODO Auto-generated catch block
            fe.printStackTrace();  
        }
        catch (Exception e) 
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /* <<== 20181204, hhlee, Modify, add exception code */
    }
	
    //19.01.31 ParkJeongSu : Send Alarm
    public static void sendByCreateAlarm(EventInfo eventInfo, String alarmCode, Map<String, String> bindMap) throws CustomException
    {
    	try
    	{
    		Document doc = new Document();
    		doc =  SMessageUtil.createXmlDocument("CreateAlarm", "originalSourceSubjectName", "targetSubjectName", eventInfo);

    		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
			
			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
			
			Element element1 = new Element("ALARMCODE");
			element1.setText(alarmCode);
			eleBodyTemp.addContent(element1);
			
			if(!bindMap.isEmpty()){
				for(String key :  bindMap.keySet()){
					Element element = new Element(key.toUpperCase());
					element.setText(bindMap.get(key));
					eleBodyTemp.addContent(element);
				}
			}
				
			//overwrite
			doc.getRootElement().addContent(eleBodyTemp);
			
			//log.debug(JdomUtils.toString(doc));
			
			//Send ALM Server : Create Alarm
			GenericServiceProxy.getESBServive().sendBySender(doc, "ALMSender");
    	}
    	catch(Exception ex)
    	{
    		log.error(ex);
    		log.error(String.format("E-Mail Send Fail !"));
    		throw new CustomException("COMMON-0001","E-Mail Send Fail !");
    	}
    }
    
	public void UpdateSortCSTInfo(String CSTName, EventInfo eventInfo) throws CustomException
	{
		String Query = "SELECT A.JOBNAME FROM CT_SORTJOB A JOIN CT_SORTJOBCARRIER B ON A.JOBNAME = B.JOBNAME WHERE A.JOBSTATE IN (:JOBSTATE1, :JOBSTATE2, :JOBSTATE3) AND B.CARRIERNAME = :CARRIERNAME";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("CARRIERNAME", CSTName);
		bindMap.put("JOBSTATE1", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
		bindMap.put("JOBSTATE2", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED);
		bindMap.put("JOBSTATE3", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
		if(sqlResult != null && sqlResult.size() > 0)
		{
            SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {sqlResult.get(0).get("JOBNAME").toString(), CSTName});
            sortJobCarrier.settrackflag(StringUtil.EMPTY);
            ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
		}
	}

	public void CancelTrackInSort(String CSTName, EventInfo eventInfo) throws CustomException
	{
		String Query = "SELECT JOBNAME, CARRIERNAME, LOTNAME, TRACKFLAG, MACHINENAME, PORTNAME FROM CT_SORTJOBCARRIER WHERE JOBNAME = (SELECT A.JOBNAME FROM CT_SORTJOB A JOIN CT_SORTJOBCARRIER B ON A.JOBNAME = B.JOBNAME WHERE A.JOBSTATE IN (:JOBSTATE1, :JOBSTATE2, :JOBSTATE3) AND B.CARRIERNAME = :CARRIERNAME)AND CARRIERNAME != :CARRIERNAME";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("CARRIERNAME", CSTName);
		bindMap.put("JOBSTATE1", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
		bindMap.put("JOBSTATE2", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED);
		bindMap.put("JOBSTATE3", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
		if(sqlResult != null && sqlResult.size() > 0)
		{
			String JObName = sqlResult.get(0).get("JOBNAME").toString();
			
			for(int i=0; i<sqlResult.size(); i++)
			{
				if(sqlResult.get(i).get("LOTNAME") != null && sqlResult.get(i).get("TRACKFLAG") != null)
				{
					if(StringUtil.equals(sqlResult.get(i).get("TRACKFLAG").toString(), "IN"))
					{
						String carrierName = sqlResult.get(i).get("CARRIERNAME").toString();
						String lotName = sqlResult.get(i).get("LOTNAME").toString();
						String machineName = sqlResult.get(i).get("MACHINENAME").toString();
						String portName = sqlResult.get(i).get("PORTNAME").toString();
						
						Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
						MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
						
						List<Element> productList = new ArrayList<Element>();
						
						Query = "SELECT P.POSITION, P.PRODUCTNAME, P.PRODUCTGRADE FROM PRODUCT P, LOT L  WHERE P.LOTNAME = L.LOTNAME AND L.CARRIERNAME = :CARRIERNAME AND PRODUCTSTATE IN (:PRODUCTSTATE)";
						bindMap.clear();
						bindMap.put("CARRIERNAME", sqlResult.get(i).get("CARRIERNAME").toString());
						bindMap.put("PRODUCTSTATE", GenericServiceProxy.getConstantMap().Prod_InProduction);
						
						@SuppressWarnings("unchecked")
						List<Map<String, Object>> sqlResult2 = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
						if(sqlResult2 != null && sqlResult2.size() > 0)
						{
							for(int J=0; J<sqlResult.size(); J++)
							{
								Element eleBodyTemp = new Element("PRODUCT");
								
				    			Element element1 = new Element("PRODUCTNAME");
				    			element1.setText(sqlResult2.get(J).get("PRODUCTNAME").toString());
				    			eleBodyTemp.addContent(element1);
				    			
				    			Element element2 = new Element("POSITION");
				    			element2.setText(sqlResult2.get(J).get("POSITION").toString());
				    			eleBodyTemp.addContent(element2);
				    			
				    			Element element3 = new Element("PRODUCTJUDGE");
				    			element3.setText(sqlResult2.get(J).get("PRODUCTGRADE").toString());
				    			eleBodyTemp.addContent(element3);
				    			
				    			productList.add(eleBodyTemp);
							}
						}

						Lot cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().getTrackOutLot(eventInfo, carrierName, lotName, productList);
						
						if (cancelTrackInLot == null)
						{
							throw new CustomException("LOT-XXXX", carrierName);
						}
						
						for (Element productEle : productList) 
						{
							String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
							Product productData = MESProductServiceProxy .getProductServiceUtil().getProductData(productName);
							
							// Modified by smkang on 2019.05.28 - ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//							Map<String, String> productUdfs = productData.getUdfs();
//							productUdfs.put("PORTNAME", "");
//							
//							kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
//							setEventInfo.setUdfs(productUdfs);
//							ProductServiceProxy.getProductService().update(productData);
							Map<String, String> updateUdfs = new HashMap<String, String>();
							updateUdfs.put("PORTNAME", "");
							MESProductServiceProxy.getProductServiceImpl().updateProductWithoutHistory(productData, updateUdfs);
						}

						for (Element productEle : productList )
						{   
							String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
							Product productData = MESProductServiceProxy .getProductServiceUtil().getProductData(productName);						
							MESProductServiceProxy.getProductServiceImpl().ExitedCancelQTime(eventInfo, productData, "TrackIn");
						}	
								
						List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(productList, machineName);

						Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

						String recipeName = lotData.getMachineRecipeName();
						
						if(StringUtil.isNotEmpty(recipeName))
						{
							MESProductServiceProxy.getProductServiceImpl().cancelTIRecipeIdleTimeLot(machineName, recipeName, eventInfo);
						}

						eventInfo.setEventName("CancelTrackIn");
						cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().cancelTrackIn(eventInfo, cancelTrackInLot, portData, "", carrierName, "", productPGSRCSequence, new HashMap<String, String>(), new HashMap<String,String>());
						
						// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//						Lot lotData_Port = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//						Map<String, String> udfs_note = lotData_Port.getUdfs();
//						udfs_note.put("PORTNAME", "");
//						LotServiceProxy.getLotService().update(lotData_Port);
						Map<String, String> updateUdfs = new HashMap<String, String>();
						updateUdfs.put("PORTNAME", "");
						MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(cancelTrackInLot, updateUdfs);
					}
				}						
			}		
			
			String TempQuery = "SELECT CARRIERNAME FROM CT_SORTJOBCARRIER WHERE JOBNAME = :JOBNAME";
			Map<String, String> bindMap2 = new HashMap<String, String>();
			bindMap2.put("JOBNAME", JObName);
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult3 = GenericServiceProxy.getSqlMesTemplate().queryForList(TempQuery, bindMap2);
			
			if(sqlResult3 != null && sqlResult3.size() > 0)
			{
				for(int k=0; k<sqlResult3.size(); k++)
				{
					UpdateSortCSTInfo(sqlResult3.get(k).get("CARRIERNAME").toString(), eventInfo);
				}
			}		
		}
	}
	
	public boolean checkSortCarrier(String CarrierName)
	{
		String Query = "SELECT JOBNAME, TRACKFLAG FROM CT_SORTJOBCARRIER WHERE JOBNAME = (SELECT A.JOBNAME FROM CT_SORTJOB A JOIN CT_SORTJOBCARRIER B ON A.JOBNAME = B.JOBNAME WHERE A.JOBSTATE IN (:JOBSTATE1, :JOBSTATE2, :JOBSTATE3) AND B.CARRIERNAME = :CARRIERNAME)AND CARRIERNAME != :CARRIERNAME";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("CARRIERNAME", CarrierName);
		bindMap.put("JOBSTATE1", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
		bindMap.put("JOBSTATE2", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED);
		bindMap.put("JOBSTATE3", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
		
		if(sqlResult != null && sqlResult.size() > 0)
		{
			for(int i=0; i<sqlResult.size(); i++)
			{
				if(sqlResult.get(i).get("TRACKFLAG") != null)
				{
					if(!StringUtil.equals(sqlResult.get(i).get("TRACKFLAG").toString(), "IN"))
					{
						return false;
					}		
				}			
			}
		}
		
		return true;
	}
	
	public void checkPanelJudgeInDummyOperation(Lot lotData) throws CustomException
	{
		if(StringUtil.equals(returnFlowType(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion()).toUpperCase(), "MAIN")
				&& MESLotServiceProxy.getLotServiceUtil().checkEndOperation(lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getNodeStack()))
		{
			StringBuilder sql = new StringBuilder();
			
			sql.append(" SELECT PJ.PANELNAME, PJ.GLASSNAME, PJ.PANELJUDGE ");
			sql.append("   FROM CT_PANELJUDGE PJ ");
			sql.append("  WHERE     1 = 1 ");
			sql.append("        AND PJ.GLASSNAME IN ");
			sql.append("        ( ");
			sql.append("        SELECT PRODUCTNAME GLASSNAME  ");
			sql.append("        FROM PRODUCT ");
			sql.append("        WHERE 1=1 ");
			sql.append("        AND FACTORYNAME = :FACTORYNAME ");
			sql.append("        AND LOTNAME = :LOTNAME ");
			sql.append("        AND PRODUCTSTATE <> :PRODUCTSTATE ");
			sql.append("        ) ");
			sql.append("        AND PJ.PANELJUDGE NOT IN ");
			sql.append("        ( ");
			sql.append("        SELECT GRADE ");
			sql.append("        FROM GRADEDEFINITION G ");
			sql.append("        WHERE G.FACTORYNAME = :FACTORYNAME ");
			sql.append("        AND G.GRADETYPE = :GRADETYPE ");
			sql.append("        ) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", lotData.getFactoryName());
			bindMap.put("LOTNAME", lotData.getKey().getLotName());
			bindMap.put("PRODUCTSTATE", GenericServiceProxy.getConstantMap().Lot_Scrapped);
			bindMap.put("GRADETYPE", GenericServiceProxy.getConstantMap().GradeType_SubProduct);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

			if(sqlResult.size() > 0)
			{
				String panelJudge = sqlResult.get(0).get("PANELJUDGE")!=null?(String)sqlResult.get(0).get("PANELJUDGE"):"";
				String panelName = (String)sqlResult.get(0).get("PANELNAME");
				String glassName = (String)sqlResult.get(0).get("GLASSNAME");
				
				throw new CustomException("GRADE-0001" , panelJudge, panelName, glassName);
			}
		}
	}
	
	public void checkMQCFlowAfter(Lot lotData, String CarrierName, EventInfo eventInfo) throws CustomException
	{
		// Mentis 3579
		// Modify Logic ( 2019.04.17 )
		// Change MQC state >> Delete MQC job 
		// Mentis 3726 Modify Logic ( 2019.04.29 )
		// Add 'MoveToEndBank'
		String ProductionType = lotData.getProductionType();
		if(StringUtil.equals(ProductionType,"MQCA"))
		{			
			eventInfo.setEventName("RemoveMqcJob");
			
			String mqcJobName = StringUtil.EMPTY;
			// 2019.05.14_hsryu_Delete JobState Condition.
			//String condition = "WHERE CARRIERNAME=? AND MQCSTATE=?";
			String condition = "WHERE CARRIERNAME=?";
//			Object[] bindSet = new Object[] {lotData.getKey().getLotName(),"Executing"}; // Because of child Lot
			// 2019.05.14_hsryu_Delete JobState Condition.
			//Object[] bindSet = new Object[] {CarrierName,"Executing"};
			Object[] bindSet = new Object[] {CarrierName};
			List<MQCJob> MQCJobList = null;
			MQCJobList = ExtendedObjectProxy.getMQCJobService().select(condition,bindSet);
			
			if(MQCJobList != null);
			{
				for (MQCJob mqcJob : MQCJobList)
				{
					int Compareint = 0;
					mqcJobName = mqcJob.getmqcJobName();
					Boolean checkValidation = false;
					String ValidationQuery = " SELECT DISTINCT A.MQCJOBNAME, C.PRODUCTNAME, A.LOTNAME ";
					ValidationQuery += " FROM CT_MQCJOB A JOIN CT_MQCJOBOPER B ON A.MQCJOBNAME = B.MQCJOBNAME JOIN CT_MQCJOBPOSITION C ON A.MQCJOBNAME = C.MQCJOBNAME JOIN PRODUCT D ON C.PRODUCTNAME = D.PRODUCTNAME ";
					ValidationQuery += " WHERE A.MQCJOBNAME = :MQCJOBNAME AND C.MQCCOUNTUP > 0 AND D.PRODUCTSTATE != :PRODUCTSTATE ORDER BY C.PRODUCTNAME ASC "; 
					Map<String, Object> bindMapvalildation = new HashMap<String, Object>();
					bindMapvalildation.put("MQCJOBNAME", mqcJobName);
					bindMapvalildation.put("PRODUCTSTATE", "Scrapped");
					List<Map<String, Object>> mqcJobValidation = GenericServiceProxy.getSqlMesTemplate().queryForList(ValidationQuery, bindMapvalildation);
					if(mqcJobValidation.size() == 0)
					{
						String CSTcondition = "where carrierName=?"; // All CST
						Object[] DeletebindSet = new Object[] {CarrierName};
						List<MQCJob> MQCJob_List = null;
						MQCJob_List = ExtendedObjectProxy.getMQCJobService().select(CSTcondition,DeletebindSet);
						for (MQCJob mqc_Job : MQCJob_List)
						{
							mqcJobName = mqc_Job.getmqcJobName();
							//Delete MQCJob
							ExtendedObjectProxy.getMQCJobService().remove(eventInfo, mqc_Job);
							
							//Delete MQCJobOper
							String strSql = "SELECT MQCJOBNAME, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION " +
									"  FROM CT_MQCJOBOPER " +
									" WHERE MQCJOBNAME = :MQCJOBNAME ";

							Map<String, Object> bindMap = new HashMap<String, Object>();
							bindMap.put("MQCJOBNAME", mqcJobName);

							List<Map<String, Object>> mqcJobOperList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
							
							for( int i = 0; i < mqcJobOperList.size(); i++)
							{
								MQCJobOper mqcJobOper = ExtendedObjectProxy.getMQCJobOperService().selectByKey(false, new Object[] {mqcJobName, 
																															(String)mqcJobOperList.get(i).get("PROCESSOPERATIONNAME"), 
																															(String)mqcJobOperList.get(i).get("PROCESSOPERATIONVERSION")});
								
								if(mqcJobOper != null)
								{
									ExtendedObjectProxy.getMQCJobOperService().remove(eventInfo, mqcJobOper);
								}
							}
							
							//Delete MQCJobPosition
							String strPositionSql = "SELECT MQCJOBNAME, " +
									"       PROCESSOPERATIONNAME, " +
									"       PROCESSOPERATIONVERSION, " +
									"       POSITION " +
									"  FROM CT_MQCJOBPOSITION " +
									" WHERE MQCJOBNAME = :MQCJOBNAME ";

							Map<String, Object> bindMapPosition = new HashMap<String, Object>();
							bindMapPosition.put("MQCJOBNAME", mqcJobName);

							List<Map<String, Object>> mqcJobPositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strPositionSql, bindMapPosition);
							
							for( int i = 0; i < mqcJobPositionList.size(); i++)
							{
								MQCJobPosition mqcJobPosition = ExtendedObjectProxy.getMQCJobPositionService().selectByKey(false, new Object[] {mqcJobName, 
																															(String)mqcJobPositionList.get(i).get("PROCESSOPERATIONNAME"), 
																															(String)mqcJobPositionList.get(i).get("PROCESSOPERATIONVERSION"),
																															mqcJobPositionList.get(i).get("POSITION").toString()});
								
								if(mqcJobPosition != null)
								{
									ExtendedObjectProxy.getMQCJobPositionService().remove(eventInfo, mqcJobPosition);
								}
							}
						}
						MESLotServiceProxy.getLotServiceImpl().MoveMQCLot(lotData,eventInfo);
					}
				}
			}				
		}
	}
	
	public void checkMQCFlowbyScrap(Lot lotData, String CarrierName, EventInfo eventInfo) throws CustomException
	{
		String ProductionType = lotData.getProductionType();
		if(StringUtil.equals(ProductionType,"MQCA"))
		{			
			eventInfo.setEventName("RemoveMqcJob");
			
			String mqcJobName = StringUtil.EMPTY;
			//2019.05.10 dmlee : remove MQCSTATE Condition
			String condition = "WHERE CARRIERNAME=?";
//			Object[] bindSet = new Object[] {lotData.getKey().getLotName(),"Executing"}; // Because of child Lot
			Object[] bindSet = new Object[] {CarrierName};
			List<MQCJob> MQCJobList = null;
			MQCJobList = ExtendedObjectProxy.getMQCJobService().select(condition,bindSet);
			
			if(MQCJobList != null);
			{
				for (MQCJob mqcJob : MQCJobList)
				{
					int Compareint = 0;
					mqcJobName = mqcJob.getmqcJobName();
					Boolean checkValidation = false;
					String ValidationQuery = " SELECT DISTINCT A.MQCJOBNAME, C.PRODUCTNAME, A.LOTNAME ";
					ValidationQuery += " FROM CT_MQCJOB A JOIN CT_MQCJOBOPER B ON A.MQCJOBNAME = B.MQCJOBNAME JOIN CT_MQCJOBPOSITION C ON A.MQCJOBNAME = C.MQCJOBNAME JOIN PRODUCT D ON C.PRODUCTNAME = D.PRODUCTNAME ";
					ValidationQuery += " WHERE A.MQCJOBNAME = :MQCJOBNAME AND C.MQCCOUNTUP > 0 AND D.PRODUCTSTATE != :PRODUCTSTATE ORDER BY C.PRODUCTNAME ASC "; 
					Map<String, Object> bindMapvalildation = new HashMap<String, Object>();
					bindMapvalildation.put("MQCJOBNAME", mqcJobName);
					bindMapvalildation.put("PRODUCTSTATE", "Scrapped");
					List<Map<String, Object>> mqcJobValidation = GenericServiceProxy.getSqlMesTemplate().queryForList(ValidationQuery, bindMapvalildation);
					if(mqcJobValidation.size() == 0)
					{
						String CSTcondition = "where carrierName=?"; // All CST
						Object[] DeletebindSet = new Object[] {CarrierName};
						List<MQCJob> MQCJob_List = null;
						MQCJob_List = ExtendedObjectProxy.getMQCJobService().select(CSTcondition,DeletebindSet);
						for (MQCJob mqc_Job : MQCJob_List)
						{
							mqcJobName = mqc_Job.getmqcJobName();
							//Delete MQCJob
							ExtendedObjectProxy.getMQCJobService().remove(eventInfo, mqc_Job);
							
							//Delete MQCJobOper
							String strSql = "SELECT MQCJOBNAME, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION " +
									"  FROM CT_MQCJOBOPER " +
									" WHERE MQCJOBNAME = :MQCJOBNAME ";

							Map<String, Object> bindMap = new HashMap<String, Object>();
							bindMap.put("MQCJOBNAME", mqcJobName);

							List<Map<String, Object>> mqcJobOperList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
							
							for( int i = 0; i < mqcJobOperList.size(); i++)
							{
								MQCJobOper mqcJobOper = ExtendedObjectProxy.getMQCJobOperService().selectByKey(false, new Object[] {mqcJobName, 
																															(String)mqcJobOperList.get(i).get("PROCESSOPERATIONNAME"), 
																															(String)mqcJobOperList.get(i).get("PROCESSOPERATIONVERSION")});
								
								if(mqcJobOper != null)
								{
									ExtendedObjectProxy.getMQCJobOperService().remove(eventInfo, mqcJobOper);
								}
							}
							
							//Delete MQCJobPosition
							String strPositionSql = "SELECT MQCJOBNAME, " +
									"       PROCESSOPERATIONNAME, " +
									"       PROCESSOPERATIONVERSION, " +
									"       POSITION " +
									"  FROM CT_MQCJOBPOSITION " +
									" WHERE MQCJOBNAME = :MQCJOBNAME ";

							Map<String, Object> bindMapPosition = new HashMap<String, Object>();
							bindMapPosition.put("MQCJOBNAME", mqcJobName);

							List<Map<String, Object>> mqcJobPositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strPositionSql, bindMapPosition);
							
							for( int i = 0; i < mqcJobPositionList.size(); i++)
							{
								MQCJobPosition mqcJobPosition = ExtendedObjectProxy.getMQCJobPositionService().selectByKey(false, new Object[] {mqcJobName, 
																															(String)mqcJobPositionList.get(i).get("PROCESSOPERATIONNAME"), 
																															(String)mqcJobPositionList.get(i).get("PROCESSOPERATIONVERSION"),
																															mqcJobPositionList.get(i).get("POSITION").toString()});
								
								if(mqcJobPosition != null)
								{
									ExtendedObjectProxy.getMQCJobPositionService().remove(eventInfo, mqcJobPosition);
								}
							}
						}
						//MESLotServiceProxy.getLotServiceImpl().MoveMQCLot(lotData,eventInfo);
					}
				}
			}				
		}
	}
	
	public void checkMQCFlow(Lot lotData, String CarrierName, EventInfo eventInfo, DeassignCarrierInfo deassignCarrierInfo) throws CustomException
	{
		// Mentis 3579
		// Modify Logic ( 2019.04.17 )
		// Change MQC state >> Delete MQC job 
		// Mentis 3726 Modify Logic ( 2019.04.29 )
		// Add 'MoveToEndBank'
		String NewLotName = "";
		String ProductionType = lotData.getProductionType();
		if(StringUtil.equals(ProductionType,"MQCA"))
		{			
			eventInfo.setEventName("RemoveMqcJob");
			
			List<ProductU> ProductUSequence = deassignCarrierInfo.getProductUSequence();
			
			String mqcJobName = StringUtil.EMPTY;
			String condition = "WHERE CARRIERNAME=?";
//			Object[] bindSet = new Object[] {lotData.getKey().getLotName(),"Executing"}; // Because of child Lot
			Object[] bindSet = new Object[] {CarrierName};
			List<MQCJob> MQCJobList = null;
			MQCJobList = ExtendedObjectProxy.getMQCJobService().select(condition,bindSet);
			
			if(MQCJobList != null);
			{
				for (MQCJob mqcJob : MQCJobList)
				{
					int Compareint = 0;
					mqcJobName = mqcJob.getmqcJobName();
					Boolean checkValidation = false;
					String ValidationQuery = " SELECT DISTINCT A.MQCJOBNAME, C.PRODUCTNAME, A.LOTNAME ";
					ValidationQuery += " FROM CT_MQCJOB A JOIN CT_MQCJOBOPER B ON A.MQCJOBNAME = B.MQCJOBNAME JOIN CT_MQCJOBPOSITION C ON A.MQCJOBNAME = C.MQCJOBNAME JOIN PRODUCT D ON C.PRODUCTNAME = D.PRODUCTNAME ";
					ValidationQuery += " WHERE A.MQCJOBNAME = :MQCJOBNAME AND C.MQCCOUNTUP > 0 AND D.PRODUCTSTATE != :PRODUCTSTATE ORDER BY C.PRODUCTNAME ASC "; 
					Map<String, Object> bindMapvalildation = new HashMap<String, Object>();
					bindMapvalildation.put("MQCJOBNAME", mqcJobName);
					bindMapvalildation.put("PRODUCTSTATE", "Scrapped");
					//List<Map<String, Object>> mqcJobValidation = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(ValidationQuery, bindMapvalildation);
					List<Map<String, Object>> mqcJobValidation = GenericServiceProxy.getSqlMesTemplate().queryForList(ValidationQuery, bindMapvalildation);
					for(int i = 0; i < mqcJobValidation.size(); i++)
					{
						if(i==0)
						{
							NewLotName = (String)mqcJobValidation.get(0).get("LOTNAME");
						}
						
						for (ProductU productUData : ProductUSequence)
						{
							if(StringUtil.equals((String)mqcJobValidation.get(i).get("PRODUCTNAME"),productUData.getProductName()))
							{
								Compareint++;
								break;
							}
						}
					}
					
					if(Compareint == ProductUSequence.size())
					{
						checkValidation = true;
					}
					
					if(checkValidation)
					{						
						String CSTcondition = "where carrierName=?"; // All CST
						Object[] DeletebindSet = new Object[] {CarrierName};
						List<MQCJob> MQCJob_List = null;
						MQCJob_List = ExtendedObjectProxy.getMQCJobService().select(CSTcondition,DeletebindSet);
						for (MQCJob mqc_Job : MQCJob_List)
						{
							mqcJobName = mqc_Job.getmqcJobName();
							//Delete MQCJob
							ExtendedObjectProxy.getMQCJobService().remove(eventInfo, mqc_Job);
							
							//Delete MQCJobOper
							String strSql = "SELECT MQCJOBNAME, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION " +
									"  FROM CT_MQCJOBOPER " +
									" WHERE MQCJOBNAME = :MQCJOBNAME ";

							Map<String, Object> bindMap = new HashMap<String, Object>();
							bindMap.put("MQCJOBNAME", mqcJobName);

							List<Map<String, Object>> mqcJobOperList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
							
							for( int i = 0; i < mqcJobOperList.size(); i++)
							{
								MQCJobOper mqcJobOper = ExtendedObjectProxy.getMQCJobOperService().selectByKey(false, new Object[] {mqcJobName, 
																															(String)mqcJobOperList.get(i).get("PROCESSOPERATIONNAME"), 
																															(String)mqcJobOperList.get(i).get("PROCESSOPERATIONVERSION")});
								
								if(mqcJobOper != null)
								{
									ExtendedObjectProxy.getMQCJobOperService().remove(eventInfo, mqcJobOper);
								}
							}
							
							//Delete MQCJobPosition
							String strPositionSql = "SELECT MQCJOBNAME, " +
									"       PROCESSOPERATIONNAME, " +
									"       PROCESSOPERATIONVERSION, " +
									"       POSITION " +
									"  FROM CT_MQCJOBPOSITION " +
									" WHERE MQCJOBNAME = :MQCJOBNAME ";

							Map<String, Object> bindMapPosition = new HashMap<String, Object>();
							bindMapPosition.put("MQCJOBNAME", mqcJobName);

							List<Map<String, Object>> mqcJobPositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strPositionSql, bindMapPosition);
							
							for( int i = 0; i < mqcJobPositionList.size(); i++)
							{
								MQCJobPosition mqcJobPosition = ExtendedObjectProxy.getMQCJobPositionService().selectByKey(false, new Object[] {mqcJobName, 
																															(String)mqcJobPositionList.get(i).get("PROCESSOPERATIONNAME"), 
																															(String)mqcJobPositionList.get(i).get("PROCESSOPERATIONVERSION"),
																															mqcJobPositionList.get(i).get("POSITION").toString()});
								
								if(mqcJobPosition != null)
								{
									ExtendedObjectProxy.getMQCJobPositionService().remove(eventInfo, mqcJobPosition);
								}
							}
						}			
					}			
				}
			}				
		}
	}

	public boolean CheckMQCState(String LotName)
	{
		String condition = "where lotname=?" + " and mqcstate = ?";
		Object[] bindSet = new Object[] {LotName, "Executing" };
		
		try
		{
			List<MQCJob> MQCJobList = null;
			MQCJobList = ExtendedObjectProxy.getMQCJobService().select(condition,bindSet);
			if( MQCJobList != null && MQCJobList.size() > 0)
			{
				return true;	
			}
		}
		catch (Exception e)
		{
			return true;	
		}

		return false;
	}
	
	/**
	 * 
	 * @Name     checkLotGradeUpdateByDFSFileJudge
	 * @since    2019. 3. 8.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param productName
	 * @return
	 */
	public boolean checkLotGradeUpdateByDFSFileJudge(String productName) throws CustomException
    {	    
        boolean isDfsUpdate = false;    
	    
	    String strSql = " SELECT PJ.GLASSNAME,               \n"
                      + "        PJ.PROCESSOPERATIONNAME,    \n"
                      + "        PJ.MACHINENAME              \n"
                      + "   FROM CT_PANELJUDGE PJ            \n"
                      + "  WHERE 1=1                         \n"
                      + "    AND PJ.GLASSNAME = :GLASSNAME   \n"
                      + "  GROUP BY PJ.GLASSNAME,            \n"
                      + "           PJ.PROCESSOPERATIONNAME, \n"
                      + "           PJ.MACHINENAME             ";
                        
            Map<String, Object> bindMap = new HashMap<String, Object>();

            bindMap.put("GLASSNAME", productName);
            
            List<Map<String, Object>> glassDfsFileJudge = null;
            try
            {
                glassDfsFileJudge = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
            }
            catch (Exception ex)
            {                
            }
                        
            if(glassDfsFileJudge != null && glassDfsFileJudge.size() > 0)
            {
                String processOperationName = glassDfsFileJudge.get(0).get("PROCESSOPERATIONNAME") != null ? glassDfsFileJudge.get(0).get("PROCESSOPERATIONNAME").toString() : StringUtil.EMPTY;
                String machineName = glassDfsFileJudge.get(0).get("MACHINENAME") != null ? glassDfsFileJudge.get(0).get("MACHINENAME").toString() : StringUtil.EMPTY;
            
                if(StringUtil.isNotEmpty(processOperationName) && StringUtil.isNotEmpty(machineName)) 
                {
                    isDfsUpdate = true;
                }
            }
            else
            {
                throw new CustomException("PANEL-0003", productName);
            }

            return isDfsUpdate;
    }
	
    public List<Product> getReceivedFileJudgeProduct(String lotName) throws CustomException {
    	
    	List<Product> productList = new ArrayList<Product>();
    	
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT DISTINCT P.PRODUCTNAME PRODUCTNAME  ");
		sql.append("   FROM PRODUCT P, CT_PANELJUDGE PJ ");
		sql.append("  WHERE     1 = 1 ");
		sql.append("        AND P.PRODUCTNAME = PJ.GLASSNAME ");
		sql.append("        AND P.LOTNAME = :LOTNAME ");
		sql.append("        AND P.PRODUCTSTATE <> :PRODUCTSTATE ");
		sql.append("        AND PJ.PROCESSOPERATIONNAME IS NOT NULL ");
		sql.append("        AND PJ.MACHINENAME IS NOT NULL ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("PRODUCTSTATE", "Scrapped");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if(sqlResult.size() > 0){
			for(int i=0; i < sqlResult.size(); i++){
				if(StringUtils.isNotEmpty(sqlResult.get(i).get("PRODUCTNAME").toString())) {
					String productName = sqlResult.get(i).get("PRODUCTNAME").toString();
					
					try {
						Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
						productList.add(productData);
					}
					catch(Throwable e){
						log.info("Product[" + productName + "] Data is null!");
					}
				}
			}
			
			return productList;
		}
		
		return productList;
    }
	public void checkThresHoldRatio(Lot lotData) throws CustomException 
	{
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
		List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
		
		for(Product product : productList) {
			Double cut1ThresHoldRatio = 0.0;
			Double cut2ThresHoldRatio = 0.0;
			Double cut3ThresHoldRatio = 0.0;
			Double cut4ThresHoldRatio = 0.0;
			
			// 2019.03.19_hsryu_For Note.
			Double currentCut1ThresHoldRatio = 0.0;
			Double currentCut2ThresHoldRatio = 0.0;
			
			// 2019.04.12_Insert Logic. hsryu_Error or Pass.
			boolean errorFlag = true;

			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), "00001");
			
			int cutType = StringUtil.equals(productSpecData.getUdfs().get("CUTTYPE").toUpperCase(), "HALF") ? 2 : 4 ;
			
			if(Double.valueOf(productSpecData.getUdfs().get("CUT1THRESHOLDRATIO")) != null)
				cut1ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT1THRESHOLDRATIO"));
			
			if(Double.valueOf(productSpecData.getUdfs().get("CUT2THRESHOLDRATIO")) != null)
				cut2ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT2THRESHOLDRATIO"));

			if(StringUtil.equals(String.valueOf(cutType), "4")) {
				if(Double.valueOf(productSpecData.getUdfs().get("CUT3THRESHOLDRATIO")) != null)
					cut3ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT3THRESHOLDRATIO"));
				
				if(Double.valueOf(productSpecData.getUdfs().get("CUT4THRESHOLDRATIO")) != null)
					cut4ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT4THRESHOLDRATIO"));
			}

			for(int i = 1; i < cutType + 1; i++) {
				StringBuilder sql = new StringBuilder();

				sql.append(" select  PJ.PANELNAME, SUBSTR(PJ.PANELNAME,-5,1) CUTPOSITION, PJ.PANELJUDGE ");
				sql.append("   from CT_PANELJUDGE PJ ");
				sql.append("  WHERE     PJ.GLASSNAME = :GLASSNAME ");
				sql.append("        AND SUBSTR(PJ.PANELNAME,-5,1) = :CUTNUM ");

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("GLASSNAME", product.getKey().getProductName());
				bindMap.put("CUTNUM", i);

				List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();

				try {
					sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

					if(sqlResult.size() > 0) {
						double oNum = 0;
						double xNum = 0;
						double gNum = 0;
						double otherGradeNum = 0;

						for(int j = 0; j < sqlResult.size(); j++) {
							String panelJudge = sqlResult.get(j).get("PANELJUDGE").toString();

							if(StringUtil.equals(panelJudge, "O"))
								oNum++;
							else if(StringUtil.equals(panelJudge, "X"))
								xNum++;
							else if(StringUtil.equals(panelJudge, "G"))
								gNum++;
							else if((!StringUtil.equals(panelJudge, "G"))&&(!StringUtil.equals(panelJudge, "O")&&(!StringUtil.equals(panelJudge, "X"))))
								otherGradeNum ++;
						}

						Double cutXThresHoldRatio = 0.0;
						
						if(i==1) cutXThresHoldRatio = cut1ThresHoldRatio;
						else if(i==2) cutXThresHoldRatio = cut2ThresHoldRatio;
						else if(i==3) cutXThresHoldRatio = cut3ThresHoldRatio;
						else if(i==4) cutXThresHoldRatio = cut4ThresHoldRatio;

						if( cutXThresHoldRatio != 0.0 ) {	
							if(oNum != 0) 
							{
								Double ThresRatio = oNum/(oNum+xNum+otherGradeNum);
								
								if(ThresRatio < cutXThresHoldRatio) {
									
									//2019.03.19_hsryu_Insert Logic. For HoldNote. Mantis 0003148.
									if(i==1) currentCut1ThresHoldRatio = ThresRatio;
									if(i==2) currentCut2ThresHoldRatio = ThresRatio;
									log.info("CUT" + i + ", "+ "CutThresHold : " + cutXThresHoldRatio + ", CurrentThresHold : " + ThresRatio);
									// 2019.04.12_hsryu_Insert Logic. when all Cut is 'NG', Error. 
									//throw new CustomException("LOT-0226" , lotData.getKey().getLotName(), product.getKey().getProductName());
								}
								else
								{
									log.info("Cut Judge is Good.");
									// 2019.04.12_hsryu_Insert Logic. when all Cut is 'NG', Error. 
									errorFlag = false;
								}
							}
							else if(gNum != 0)  
							{
								Double ThresRatio = gNum/(gNum+xNum+otherGradeNum);
								
								if(oNum==0 && (gNum+xNum+otherGradeNum)!=0){
									log.info("PanelJudge 'O' Number is zero. ");
									
									if(ThresRatio<cutXThresHoldRatio) {
										
										//2019.03.19_hsryu_Insert Logic. For HoldNote. Mantis 0003148.
										if(i==1) currentCut1ThresHoldRatio = ThresRatio;
										if(i==2) currentCut2ThresHoldRatio = ThresRatio;
										log.info("CUT" + i + ", "+ "CutThresHold : " + cutXThresHoldRatio + ", CurrentThresHold : " + ThresRatio);
										// 2019.04.12_hsryu_Insert Logic. when all Cut is 'NG', Error. 
										//throw new CustomException("LOT-0226" , lotData.getKey().getLotName(), product.getKey().getProductName());
									}
									else
									{
										log.info("Cut Judge is Good.");
										// 2019.04.12_hsryu_Insert Logic. when all Cut is 'NG', Error. 
										errorFlag = false;
									}
								}
							}
    						else
    						{
    							log.info("'O' or 'G' Judge is zero.");
    							//throw new CustomException("LOT-0226" , lotData.getKey().getLotName(), product.getKey().getProductName());
    						}
						}
						// 2019.04.16_hsryu_Insert Logic. if set ThresRatioHold is 0, Not Check! 
						else
						{
							errorFlag = false;
						}
					}
				} catch (Exception ex) {
					throw new CustomException("LOT-0226" , lotData.getKey().getLotName(), product.getKey().getProductName());
				}
			}
			
			// 2019.04.12_hsryu_Insert Logic. when all Cut is 'NG', Error. 
			if(errorFlag){
				throw new CustomException("LOT-0226" ,lotData.getKey().getLotName(), product.getKey().getProductName(), cut1ThresHoldRatio, currentCut1ThresHoldRatio, cut2ThresHoldRatio, currentCut2ThresHoldRatio);
			}
		}
	}
	public boolean isDummyOperation(String factoryName,String OperationName){
    	try {
			ProcessOperationSpec processOperation = CommonUtil.getProcessOperationSpec(factoryName, OperationName);
			
			if(StringUtils.equals("DUMMY", processOperation.getDetailProcessOperationType())){
				log.info("["+OperationName+"] is Dummy Operation");
				return true;
			}else{
				log.info("["+OperationName+"] is not Dummy Operation");
				return false;
			}
		} catch (CustomException e) {
			log.info("["+OperationName+"] is not Dummy Operation");
			return false;
		}
		
	}
	
	//2019.04.23_hsryu_Insert Logic. check Mixed Glass WorkOrder! Mantis 0002757.
	public String isMixedWorkOrder(Lot lotData, List<ProductP> productPSequence) throws CustomException {
		
		String workOrder = "";
		List<Product> pProductList = null;

		try {
			if(lotData != null){
				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
				pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			}
			
			for(ProductP ProductPData : productPSequence)
			{
				try {
					Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(ProductPData.getProductName());
					pProductList.add(productData);
				}
				catch(Exception e){
					log.warn("ProductData is not exist. ProductName : " + ProductPData.getProductName());
				}
			}
			
			for(Product productData : pProductList){
				if(StringUtils.isEmpty(workOrder))
					workOrder = productData.getProductRequestName();
				
				if(!StringUtils.equals(workOrder, productData.getProductRequestName()))
				{
					return "MIXED";
				}
			}
		}
		catch(Throwable e){
			log.warn("Fail check Mixed WO.");
		}
		
		return workOrder;
	}
	
	//2019.04.23_hsryu_Insert Logic. check Mixed Glass WorkOrder! Mantis 0002757.
	public String isMixedWorkOrder(Lot lotData) throws CustomException {
		
		String workOrder = "";
		
		try {
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
			List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			
			for(Product productData : pProductList){
				if(StringUtils.isEmpty(workOrder))
					workOrder = productData.getProductRequestName();
				
				if(!StringUtils.equals(workOrder, productData.getProductRequestName()))
				{
					return "MIXED";
				}
			}
		}
		catch(Throwable e) {
			log.warn("Fail check Mixed WO.");
		}
		return workOrder;
	}
	
	// 2019.05.31_Add Logic. if LotHoldState 'N' = Not Exist AHold. check BHold ! 
	public Lot checkBHoldAndOperHold(String lotName, EventInfo eventInfo) throws CustomException {
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
 		
		// 2019.05.30_hsryu_if Not exist AHold, check BHold! Requested by CIM.
 		/***** The Logic must be below the TrackOut Hold Logic *****/
 		if(!StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed)) {
 			if(StringUtils.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().FLAG_N)) {
        		if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotData.getKey().getLotName(), eventInfo) && !CommonValidation.checkFirstOperation(lotData,"SAMPLING")){
        			lotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(lotData.getKey().getLotName(), eventInfo);
        		}
 			}
 		}
 		
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
 		
 		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
 		
 		return lotData;
	}
	
	// 2019.06.04_hsryu_after TrackOut, Memory UpdateProductFlag Event. Mantis 0003934.
	public Lot MomeryNoteUpdateProductFlag(Lot lotData, String note, EventInfo eventInfo) throws CustomException {
		if(StringUtils.isNotEmpty(note)){
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
			
			EventInfo noteEventInfo = EventInfoUtil.makeEventInfo("UpdateProductFlag", eventInfo.getEventUser(), "UpdateProductFlag", "", "");
			noteEventInfo.setEventTime(eventInfo.getEventTime());
			noteEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			if(note.length()>3500){
				note = note.substring(0, 3499);
			}
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("NOTE", note);
			lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), noteEventInfo, setEventInfo);
			
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
			
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
		}
		
		return lotData;
	}
	
	/**
	 * @author smkang
	 * @since 2019.06.20
	 * @param lotName
	 * @return
	 * @throws FrameworkErrorSignal
	 * @throws NotFoundSignal
	 * @see According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
	 */
	public List<Product> allUnScrappedProductsByLotForUpdate(String lotName) throws FrameworkErrorSignal, NotFoundSignal
	{
		String condition = "WHERE LOTNAME = ? AND PRODUCTSTATE NOT IN (?, ?) ORDER BY POSITION FOR UPDATE";

		Object bindSet[] = new Object[3];
		bindSet[0] = lotName;
		bindSet[1] = GenericServiceProxy.getConstantMap().Prod_Scrapped;
		bindSet[2] = GenericServiceProxy.getConstantMap().Prod_Consumed;

		return ProductServiceProxy.getProductService().select(condition, bindSet);
	}
	
	//add by jhying on20200310 mantis:5435
	 public String getLocalRunExceptionflag(String enumName, String enumValue) 
	 {
		 String sql = "SELECT EV.DEFAULTFLAG" +
			   "  FROM ENUMDEFVALUE EV" +
			   " WHERE EV.ENUMNAME = :ENUMNAME" +
			   "   AND EV.ENUMVALUE = :ENUMVALUE";
		 
		 Map<String, String> bindMap = new HashMap<String, String>();
		 bindMap.clear();
		 bindMap.put("ENUMNAME", enumName);
		 bindMap.put("ENUMVALUE", enumValue);
							 
		 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		 String defauFlag = "";
		 if (sqlResult != null && sqlResult.size() > 0) 
		 {
			 defauFlag = sqlResult.get(0).get("DEFAULTFLAG").toString();
		 }
		 return defauFlag;
	 }
	 
 

	/**
	 * @param lotName
	 * @param ecCode
	 * @param productSpecName
	 * @param barchNodeStack
	 * @return
	 * add by GJJ 20200512 mantis:6088
	 */
	public static String getReturnNodeForBranch( String lotName,String ecCode,String productSpecName,String barchNodeStack )
	{
	 String nodeStack = "";
     String returnProcessFlowName = "";
     String returnProcessOperationName = "";
     
     String sql = "	SELECT PA.RETURNPROCESSFLOWNAME,  ";
     sql += "		PA.RETURNPROCESSOPERATIONNAME  ";
     sql += "	FROM TPEFOPOLICY TPEFO,  ";
     sql += "		POSALTERPROCESSOPERATION PA,  ";
     sql += "		(  ";
     sql += "			SELECT N.FACTORYNAME,  ";
     sql += "				N.PROCESSFLOWNAME,  ";
     sql += "				N.PROCESSFLOWVERSION,  ";
     sql += "				N.NODEATTRIBUTE1 AS PROCESSOPERATIONNAME,  ";
     sql += "				N.NODEATTRIBUTE2 AS PROCESSOPERATIONVERSION  ";
     sql += "			FROM NODE N,  ";
     sql += "				LOT L  ";
     sql += "			WHERE N.NODEID = REGEXP_SUBSTR(L.NODESTACK, '[^.]+')  ";
     sql += "				AND L.LOTNAME = :LOTNAME  ";
     sql += "			) BEFOREOPER,  ";
     sql += "		(  ";
     sql += "			SELECT N.FACTORYNAME,  ";
     sql += "				N.PROCESSFLOWNAME,  ";
     sql += "				N.PROCESSFLOWVERSION,  ";
     sql += "				N.NODEATTRIBUTE1 AS PROCESSOPERATIONNAME,  ";
     sql += "				N.NODEATTRIBUTE2 AS PROCESSOPERATIONVERSION  ";
     sql += "			FROM NODE N  ";
     sql += "			WHERE N.NODEID = :NODESTACK  ";
     sql += "			) BarchFlow  ";
     sql += "	WHERE 1 = 1  ";
     sql += "		AND (  ";
     sql += "			(TPEFO.FACTORYNAME = BEFOREOPER.FACTORYNAME)  ";
     sql += "			OR (TPEFO.FACTORYNAME = '*')  ";
     sql += "			)  ";
     sql += "		AND (  ";
     sql += "			(TPEFO.PRODUCTSPECNAME = :PRODUCTSPECNAME)  ";
     sql += "			OR (TPEFO.PRODUCTSPECNAME = '*')  ";
     sql += "			)  ";
     sql += "		AND (  ";
     sql += "			(TPEFO.PRODUCTSPECVERSION = :PRODUCTSPECVERSION)  ";
     sql += "			OR (TPEFO.PRODUCTSPECVERSION = '*')  ";
     sql += "			)  ";
     sql += "		AND (  ";
     sql += "			(TPEFO.ECCODE = :ECCODE)  ";
     sql += "			OR (TPEFO.ECCODE = '*')  ";
     sql += "			)  ";
     sql += "		AND (  ";
     sql += "			(TPEFO.PROCESSFLOWNAME = BEFOREOPER.PROCESSFLOWNAME)  ";
     sql += "			OR (TPEFO.PROCESSFLOWNAME = '*')  ";
     sql += "			)  ";
     sql += "		AND (  ";
     sql += "			(TPEFO.PROCESSFLOWVERSION = BEFOREOPER.PROCESSFLOWVERSION)  ";
     sql += "			OR (TPEFO.PROCESSFLOWVERSION = '*')  ";
     sql += "			)  ";
     sql += "		AND (  ";
     sql += "			(TPEFO.PROCESSOPERATIONNAME = BEFOREOPER.PROCESSOPERATIONNAME)  ";
     sql += "			OR (TPEFO.PROCESSOPERATIONNAME = '*')  ";
     sql += "			)  ";
     sql += "		AND (  ";
     sql += "			(TPEFO.PROCESSOPERATIONVERSION = BEFOREOPER.PROCESSOPERATIONVERSION)  ";
     sql += "			OR (TPEFO.PROCESSOPERATIONVERSION = '*')  ";
     sql += "			)  ";
     sql += "		AND PA.TOPROCESSFLOWNAME = BarchFlow.PROCESSFLOWNAME  ";
     sql += "		AND TPEFO.CONDITIONID = PA.CONDITIONID  ";
     sql += "		AND UPPER(PA.CONDITIONNAME) = 'BRANCH'  ";
     sql += "	ORDER BY DECODE(TPEFO.FACTORYNAME, '*', 9999, 0),  ";
     sql += "		DECODE(TPEFO.PROCESSFLOWNAME, '*', 9999, 0),  ";
     sql += "		DECODE(TPEFO.PROCESSFLOWVERSION, '*', 9999, 0),  ";
     sql += "		DECODE(TPEFO.PROCESSOPERATIONNAME, '*', 9999, 0),  ";
     sql += "		DECODE(TPEFO.PROCESSOPERATIONVERSION, '*', 9999, 0)  ";

     Map<String, String> bindMap = new HashMap<String, String>();
     bindMap.put("LOTNAME", lotName);
     bindMap.put("PRODUCTSPECNAME", productSpecName);
     bindMap.put("PRODUCTSPECVERSION", GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
     bindMap.put("ECCODE", ecCode);
     bindMap.put("NODESTACK", barchNodeStack);

     List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

     if( sqlResult.size() == 1 ){
            
            returnProcessFlowName = sqlResult.get(0).get("RETURNPROCESSFLOWNAME").toString();
            returnProcessOperationName = sqlResult.get(0).get("RETURNPROCESSOPERATIONNAME").toString();
            
            nodeStack = CommonUtil.getNodeStack(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,returnProcessFlowName, returnProcessOperationName);
            
            return nodeStack;

     } else {
            nodeStack = "";
     }
     return nodeStack;
  }	 
	
	public static Map<String, Object> checkAlterProcessOperation(Lot lotData, String lotJudge, String conditionValue )
	{
		ListOrderedMap result = null;
		Map<String, Object> bindMap = new HashMap<String, Object>();
	    ProductSpec productSpec = CommonUtil.getProductSpecByLotName( lotData.getKey().getLotName() );
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT POL.FACTORYNAME, POS.TOPROCESSFLOWNAME AS TOPROCESSFLOWNAME, POS.TOPROCESSOPERATIONNAME, POS.RETURNPROCESSFLOWNAME AS RETURNPROCESSFLOWNAME,"
				+ " POS.RETURNPROCESSOPERATIONNAME AS RETURNPROCESSOPERATIONNAME, CONDITIONNAME, CONDITIONVALUE,'' AS REWORKFLAG,'OPERATION' AS ALTERPOLICY FROM TPEFOPOLICY POL, POSALTERPROCESSOPERATION POS ");
		sql.append("WHERE POL.CONDITIONID = POS.CONDITIONID ");
		sql.append("    AND POL.FACTORYNAME = :FACTORYNAME ");
		sql.append("    AND (POL.PRODUCTSPECNAME = :PRODUCTSPECNAME OR POL.PRODUCTSPECNAME='*') ");
		sql.append("    AND (POL.PRODUCTSPECVERSION= :PRODUCTSPECVERSION OR POL.PRODUCTSPECVERSION='*') ");
		sql.append("    AND (POL.ECCODE = :ECCODE OR POL.ECCODE='*') ");
		sql.append("    AND (POL.PROCESSFLOWNAME = :PROCESSFLOWNAME OR POL.PROCESSFLOWNAME='*') ");
		sql.append("    AND (POL.PROCESSFLOWVERSION = :PROCESSFLOWVERSION OR POL.PROCESSFLOWVERSION='*') ");
		sql.append("    AND (POL.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME OR POL.PROCESSOPERATIONNAME='*') ");
		sql.append("    AND (POL.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION OR POL.PROCESSOPERATIONVERSION='*') ");
		sql.append("    AND (instr(POS.REWORKCONDITIONVALUE,:REWORKCONDITIONVALUE)>0 OR  POS.REWORKCONDITIONVALUE='*')");
		sql.append("    AND (POS.REWORKCONDITIONTYPE = :REWORKCONDITIONTYPE ) ");
		sql.append("    AND (POS.CONDITIONVALUE = :CONDITIONVALUE ) ");
		
		bindMap.put( "FACTORYNAME", lotData.getFactoryName());
		bindMap.put( "PRODUCTSPECNAME", lotData.getProductSpecName() );
		bindMap.put( "PRODUCTSPECVERSION", lotData.getProductSpecVersion());
		bindMap.put( "ECCODE", productSpec.getUdfs().get("ECCODE"));
		bindMap.put( "PROCESSFLOWNAME", lotData.getProcessFlowName() );
		bindMap.put( "PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		bindMap.put( "PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		bindMap.put( "PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		bindMap.put( "REWORKCONDITIONTYPE","LotJudge");
		bindMap.put( "REWORKCONDITIONVALUE",lotJudge);
		bindMap.put( "CONDITIONVALUE",conditionValue );
		
		List<ListOrderedMap> sqlResult = greenFrameServiceProxy.getSqlTemplate().queryForList(sql.toString(), bindMap);
		if(sqlResult.size()>0)
		{
			result = sqlResult.get( 0 );
		}
		return result;
	}
	
	public static void LotBatchSetEvent ( List<Lot> lotDataList, EventInfo eventInfo, Map<String, Object> changeColumns ) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
	{
		
		log.info( "Lot Batch SetEvent Start = " + eventInfo.getEventName() + ", Lot Count = " + String.valueOf(lotDataList.size()));
		
		String sql = "Update Lot Set ";
		String columns = "";
		
		Field[] fields = Lot.class.getDeclaredFields();
		
		Map<String, String> lotColName = new HashMap<String, String>();
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
			lotColName.put(field.getName().toUpperCase(), field.getName());
		}
		sql += columns;
		sql += " Where lotname = ? ";
		
		
		List<Object[]> batchArgs = new LinkedList<Object[]>();
		List<LotHistory> lotHistoryList = new LinkedList<LotHistory>();
		Field[] histfields = LotHistory.class.getDeclaredFields();
		
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
		
		for( Lot lotData : lotDataList)
		{
			LotHistory lotHistory = new LotHistory();
			LotServiceProxy.getLotHistoryDataAdaptor().setHV( new Lot(), lotData, lotHistory );
			
			// Set Old Data.
			Set<String> oldArr = oldColName.keySet();
			for (String key : oldArr)
			{
				String columName = key.replace("OLD","");
				String oldName = oldColName.get(key);
				if ( lotColName.containsKey(columName))
				{
					String lotfieldName = lotColName.get(columName);
					Field lotField = lotData.getClass().getDeclaredField(lotfieldName);
					Field histField = lotHistory.getClass().getDeclaredField(oldName) ;
					
					lotField.setAccessible(true);
					histField.setAccessible( true );
					Object value = lotField.get(lotData);
					ObjectUtil.copyFieldValue( histField, lotHistory, value);
				}
			}
						
			//Set Last Event Info
			lotData.setLastEventComment( eventInfo.getEventComment() );
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotData.setLastEventUser(eventInfo.getEventUser());
			
			// Field have Key. Key is Don't Count , 
			// Field Count - 1 ( Key ) + 1 (Where LotName) 
			// batchValues Param Count = Field Count
			Object[] batchValues = new Object[fields.length];
			int i = 0 ;
			for( Field lotfield : fields )
			{
				if ( lotfield.getName().equals("key") )
				{
					continue;
				}
				
				if ( changeColumns.containsKey(lotfield.getName()))
				{
					lotfield.setAccessible(true);
					ObjectUtil.copyFieldValue( lotfield, lotData, changeColumns.get( lotfield.getName() ) );
				}
				lotfield.setAccessible(true);
				Object value = lotfield.get(lotData);
				
				// Set History Value
				if ( histColName.containsKey(lotfield.getName().toUpperCase()))
				{
					String colName = histColName.get(lotfield.getName().toUpperCase());
					Field histField = lotHistory.getClass().getDeclaredField(colName) ;
					histField.setAccessible( true );
					ObjectUtil.copyFieldValue( histField, lotHistory, value);
				}
				
				batchValues[i] = value;
				i++;
			}
			// Where LotName Param
			batchValues[i] = lotData.getKey().getLotName();
			batchArgs.add(batchValues);
			
			// Set Event info
			
			lotHistory.setEventComment( eventInfo.getEventComment() );
			lotHistory.setEventName( eventInfo.getEventName() );
			lotHistory.setEventTime( eventInfo.getEventTime());
			lotHistory.setEventUser( eventInfo.getEventUser());
			lotHistory.getKey().setTimeKey( TimeStampUtil.getCurrentEventTimeKey() );

			lotHistoryList.add( lotHistory );
		}
		
		greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().batchUpdate( sql, batchArgs );
		LotServiceProxy.getLotHistoryService().insert( lotHistoryList );
		
		log.info( "Lot Batch SetEvent Completed = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey() + ", Lot Count = " + String.valueOf(lotDataList.size()) );
	}

	/*
	 * Name : getDistinctLotNameByProductList Desc : This function is
	 * getDistinctLotNameByProductList Author : AIM Systems, Inc Date :
	 * 2019.05.16
	 */
	public static List<String> getDistinctLotNameByProductList(Element element, String carrierName) 
	{
		List<String> productList = CommonUtil.makeList(element, "PRODUCTLIST", "PRODUCTNAME");

		List<String> lotList = new ArrayList<String>();
		
		if(productList != null && productList.size() > 0)
		{
			String sql = "SELECT DISTINCT L.LOTNAME,L.LOTSTATE FROM PRODUCT P, LOT L"
			+ " WHERE L.LOTNAME = P.LOTNAME"
			+ "   AND P.PRODUCTNAME IN (:productList)"
			+ "   AND P.PRODUCTSTATE = 'InProduction'"
			+ "   AND L.LOTSTATE = 'Released' ORDER BY L.LOTNAME ";
			Map<String, Object> bind = new HashMap<String, Object>();
			bind.put("productList", productList);
	
			List sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bind);
	
			for (int i = 0; i < sqlResult.size(); i++) {
				ListOrderedMap lotMap = (ListOrderedMap) sqlResult.get(i);
				String lotName = lotMap.get("LOTNAME").toString();
				log.info("Debug : " + lotName);
				lotList.add(lotName);
			}
		}

		return lotList;
	}

	/*
	 * Name : checkDurableHoldState Desc : This function is
	 * checkDurableHoldState Author : AIM Systems, Inc Date : 2013.04.30
	 */
	public static void checkDurableHoldState(Durable durableData)
			throws CustomException {
		String durableHoldState = durableData.getUdfs().get("DURABLEHOLDSTATE").toString();
		if (durableHoldState
				.equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y)) {
			throw new CustomException("DURABLE-9003", durableData.getKey().getDurableName());
		}
	}

	/*
	 * Name : getDistinctLotNameByProductListExceptCarrier
	 * Desc : This function is getDistinctLotNameByProductListExceptCarrier
	 * Author : AIM Systems, Inc
	 * Date : 2019.05.16
	 */
	public static List<String> getDistinctLotNameByProductListExceptCarrier(Element element, String carrierName)
	{
		String lotName = "";
		List<String> lotList = new ArrayList<String>();
		List<String> productList = CommonUtil.makeList(element, "PRODUCTLIST", "PRODUCTNAME");

		if(productList.size() > 0){

			String sql = "SELECT DISTINCT LOTNAME FROM PRODUCT WHERE PRODUCTNAME IN (:productList) AND LOTNAME NOT IN "
					   + " (SELECT DISTINCT LOTNAME FROM PRODUCT WHERE CARRIERNAME = :carrierName) ORDER BY LOTNAME ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("productList", productList);
			bindMap.put("carrierName", carrierName);
			List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);

			for (int i = 0; i < sqlResult.size(); i++) {
				lotName = sqlResult.get(i).get("LOTNAME").toString();
				lotList.add(lotName);
			}
		}

		return lotList;
	}

	/*
	 * Name : setProductPSequence_CellTest
	 * Desc : This function is setProductPSequence_CellTest
	 * Author : AIM Systems, Inc
	 * Date : 2019.07.09
	 */
	public static List<ProductP> setProductPSequence_Module(Element element, String lotName) throws CustomException
	{
		Element ListElement = element.getChild("PRODUCTLIST");
		String machineName = element.getChild("MACHINENAME").getText();

		List<Product> productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		List<ProductP> productPSequence = new ArrayList<ProductP>();

		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();

			for (Iterator iterator = ListElement.getChildren().iterator(); iterator.hasNext();)
			{
				Element productE = (Element) iterator.next();
				String productName = productE.getChild("PRODUCTNAME").getText();
				try
				{
					if (product.getKey().getProductName().equals(productName))
					{
						String position = productE.getChild("POSITION").getText();
						String productGrade = productE.getChildText("PRODUCTGRADE");

						ProductP productP = new ProductP();
						productP.setProductName(productName);
						productP.setPosition(Long.valueOf(position).longValue());

						Map<String, String> productUserColumns = new HashMap<String, String>();
						productUserColumns.put("PRODUCTGRADE", productGrade);
						
						productP.setUdfs(productUserColumns);

						productPSequence.add(productP);
					}
				} catch (Exception e) {
					throw new CustomException("PRODUCT-9010", productName);
				}
			}
		}
		return productPSequence;
	}
	
	/*
	 * Name : isCOAProduct
	 * Desc : This function is isCOAProduct
	 * Author : AIM Systems, Inc
	 * Date : 2019.06.10
	 */
	public static boolean isShipForCOA(ProductSpec productSpecData, Lot lotData) throws CustomException
	{
		if(StringUtil.equals(productSpecData.getUdfs().get("PRODUCTIONINPUTTYPE"),"COA"))
		{
				if(StringUtil.equals(lotData.getFactoryName(), "ARRAY")
						&& StringUtil.equals(lotData.getDestinationFactoryName(), "CF"))
				{
					return true;
				}
				else if(StringUtil.equals(lotData.getFactoryName(), "CF")
						&& StringUtil.equals(lotData.getDestinationFactoryName(), "ARRAY"))
				{
					return true;
				}
				else
				{
					return false;
				}
		}
		else
		{
			return false;
		}
	}

}