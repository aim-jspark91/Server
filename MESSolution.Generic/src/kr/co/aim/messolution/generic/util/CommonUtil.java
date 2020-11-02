
package kr.co.aim.messolution.generic.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.orm.ObjectAttributeDef;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleAttrDef;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleDef;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleDefKey;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.port.management.data.PortSpecKey;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Arc;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.object.ErrorDef;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;



public class CommonUtil implements ApplicationContextAware
{
	private static Log log = LogFactory.getLog(CommonUtil.class);
	private ApplicationContext	applicationContext;


	@Override
    public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}

	/*
	* Name : getRecipeByProduct
	* Desc : This function is getRecipeByProduct
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static String getRecipeByProduct( List<Map<String, Object>> reserveRecipeByProduct, String productName, String lotRecipeName ){
		String recipename = "";

		for( int i = 0; i < reserveRecipeByProduct.size(); i++ ){
			String inputProductName = reserveRecipeByProduct.get(i).get("PRODUCTNAME").toString();
			if( StringUtils.equals(inputProductName, productName)){
				recipename = reserveRecipeByProduct.get(i).get("RECIPENAME").toString();
				break;
			}
		}
		if(StringUtils.isEmpty(recipename))
			recipename = lotRecipeName;

		return recipename;
	}

	/*
	* Name : getValue
	* Desc : This function is getValue
	* Author : AIM Systems, Inc
	* Date : 2011.05.20
	*/
	public static String getValue(Document doc, String itemName)
	{
		String value = "";
		Element root = doc.getDocument().getRootElement();
		try
		{
			value  = root.getChild("Body").getChild(itemName).getText();
		}
		catch(Exception e1)
		{
			log.info("Cannot Find Item Name: " + itemName);
		}
		return value;
	}

	/*
	* Name : getIp
	* Desc : This function is getIp
	* Author : AIM Systems, Inc
	* Date : 2011.05.20
	*/
	public static String getIp()
	{
		String ip = "";
		try
		{
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {}
		return ip;
	}

	/*
	* Name : getEnumDefValueStringByEnumName
	* Desc : This function is getEnumDefValueStringByEnumName
	* Author : AIM Systems, Inc
	* Date : 2011.05.20
	*/
	public static String getEnumDefValueStringByEnumName( String enumName ){
		String enumValue = "";
		String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME = :enumName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("enumName", enumName);

//		List<Map<String, Object>> sqlResult =
//			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);

		List<Map<String, Object>> sqlResult =
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if(sqlResult.size() > 0){
			enumValue = sqlResult.get(0).get("ENUMVALUE").toString();
		}

		return enumValue;
	}
	/*
	* Name : getEnumDefValueByEnumName
	* Desc : This function is getEnumDefValueByEnumName
	* Author : AIM Systems, Inc
	* Date : 2011.05.20
	*/
	public static List<Map<String, Object>> getEnumDefValueByEnumName( String enumName ){
		String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE "
					+ "WHERE ENUMNAME = :enumName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("enumName", enumName);

//		List<Map<String, Object>> sqlResult =
//			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);

		List<Map<String, Object>> sqlResult =
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		return sqlResult;
	}
	/*
	* Name : getEnumDefByEnumName
	* Desc : This function is getEnumDefByEnumName
	* Author : AIM Systems, Inc
	* Date : 2011.05.20
	*/
	public static List<Map<String, Object>> getEnumDefByEnumName( String enumName ){
		String sql = "SELECT ENUMNAME, CONSTANTFLAG FROM ENUMDEF "
					+ "WHERE ENUMNAME = :enumName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("enumName", enumName);

//		List<Map<String, Object>> sqlResult =
//			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);

		List<Map<String, Object>> sqlResult =
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		return sqlResult;
	}

	public static List<Product> getProductList (String carrierName) throws CustomException
	{
		String condition = "WHERE lotName in ( select lotname from lot where carrierName = :carrierName ) "
				 + "AND PRODUCTSTATE = :productState " + "ORDER BY POSITION ";

		Object[] bindSet = new Object[] { carrierName, GenericServiceProxy.getConstantMap().Prod_InProduction };

		List<Product> productList = ProductServiceProxy.getProductService().select(condition, bindSet);

		return productList;
	}

	/*
	* Name : getMachineInfo
	* Desc : This function is getMachineInfo
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static Machine getMachineInfo( String machineName ) throws CustomException{
		MachineKey machineKey = new MachineKey();
		machineKey.setMachineName(machineName);

		Machine machineData = null;

		try {
			machineData = MachineServiceProxy.getMachineService().selectByKey(machineKey);
		}catch (Exception e){
			throw new CustomException("MACHINE-9000", machineName);
		}

		return machineData;
	}

	/*
	* Name : getCrateInfo
	* Desc : This function is getCrateInfo
	* Author : AIM Systems, Inc
	* Date : 2011.03.11
	*/
	public static Consumable getCrateInfo ( String crateName ) throws CustomException{
		try
		{
			ConsumableKey consumableKey = new ConsumableKey();
			consumableKey.setConsumableName(crateName);

			Consumable consumableData = null;
			consumableData = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);

			return consumableData;
		}
		catch (Exception e)
		{
			throw new CustomException("CRATE-9000", crateName);
		}
	}

	/*
	* Name : getLotInfoByLotName
	* Desc : This function is getLotInfoByLotName
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static Lot getLotInfoByLotName ( String lotName ) throws CustomException{
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
	* Name : getPortInfo
	* Desc : This function is getPortInfo
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static Port getPortInfo ( String machineName, String portName ) throws CustomException{
		try
		{
		PortKey portKey = new PortKey();
		portKey.setMachineName(machineName);
		portKey.setPortName(portName);


		Port portData = null;

		portData = PortServiceProxy.getPortService().selectByKey(portKey);

		return portData;
		}
		catch (Exception e) {
			throw new CustomException("PORT-9000",portName);
		}
	}


	/**
	 * get Machine spec
	 * @author swcho
	 * @since 2016.11.02
	 * @param machineName
	 * @return
	 */
	public static MachineSpec getMachineSpecByMachineName(String machineName) throws CustomException
	{
		try
		{
			MachineSpecKey machineSpecKey = new MachineSpecKey();
			machineSpecKey.setMachineName(machineName);

			MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(machineSpecKey);

			return machineSpecData;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9001", "MachineSpec");
		}
	}

	/**
	 * get product request by ID
	 * @author swcho
	 * @since 2016.07.12
	 * @param productRequestName
	 * @return
	 * @throws CustomException
	 */
	public static ProductRequest getProductRequestData(String productRequestName)
		throws CustomException
	{
		try
		{
			ProductRequestKey prKey = new ProductRequestKey();
			prKey.setProductRequestName(productRequestName);

			ProductRequest prData = ProductRequestServiceProxy.getProductRequestService().selectByKey(prKey);

			return prData;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9001", "ProductRequest");
		}
	}

	/**
	 * Lot in any carrier, null means empty carrier
	 * @author swcho
	 * @since 2015.03.09
	 * @param carrierName
	 * @return
	 * @throws CustomException
	 */
	public static Lot getLotInfoBydurableName(String carrierName)
		throws CustomException
	{
		String condition = "WHERE carrierName = ? AND lotState = ?";

		Object[] bindSet = new Object[] {carrierName, GenericServiceProxy.getConstantMap().Lot_Released};

		List<Lot> lotList;

		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch(NotFoundSignal ne)
		{
			//throw new CustomException("CARRIER-9002",carrierName);
			lotList = new ArrayList<Lot>();

			return null;
		}
		catch (Exception ex)
		{
			throw new CustomException("", "");
		}

		//one Lot to a carrier
		return lotList.get(0);
	}

	/**
	 * get multi-Lot in any carrier
	 * @since 2016.04.26
	 * @author swcho
	 * @param carrierName
	 * @param isMandatory
	 * @return
	 * @throws CustomException
	 */
	public static List<Lot> getLotListByCarrier(String carrierName, boolean isMandatory) throws CustomException
	{
		String condition = "WHERE carrierName = ? AND lotState = ?";

		Object[] bindSet = new Object[] {carrierName, GenericServiceProxy.getConstantMap().Lot_Released};

		List<Lot> lotList;

		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch (Exception ex)
		{
			if (isMandatory)
				throw new CustomException("SYS-9999", "Product", "Nothing Lot");
			else
				return lotList = new ArrayList<Lot>();
		}

		return lotList;
	}
	
	/**
	 * get multi-Lot in any carrier
	 * @since 2019.07.18
	 * @author Park Jeong Su
	 * @param carrierName
	 * @param isMandatory
	 * @return
	 * @throws CustomException
	 */
	public static List<Lot> getLotListByCarrierLotStateReleasedAndCompleted(String carrierName, boolean isMandatory) throws CustomException
	{
		String condition = "WHERE carrierName = ? AND (lotState = ? OR lotState = ?)";

		Object[] bindSet = new Object[] {carrierName, GenericServiceProxy.getConstantMap().Lot_Released,GenericServiceProxy.getConstantMap().Lot_Completed};

		List<Lot> lotList;

		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch (Exception ex)
		{
			if (isMandatory)
				throw new CustomException("SYS-9999", "Product", "Nothing Lot");
			else
				return lotList = new ArrayList<Lot>();
		}

		return lotList;
	}
	
	/**
	 * @author smkang
	 * @since 2019.06.20
	 * @param carrierName
	 * @param isMandatory
	 * @return List<Lot>
	 * @throws CustomException
	 * @see According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
	 */
	public static List<Lot> getLotListByCarrierForUpdate(String carrierName, boolean isMandatory) throws CustomException
	{
		String condition = "WHERE CARRIERNAME = ? AND LOTSTATE = ? FOR UPDATE";

		Object[] bindSet = new Object[] {carrierName, GenericServiceProxy.getConstantMap().Lot_Released};

		try
		{
			return LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch (Exception ex)
		{
			if (isMandatory)
				throw new CustomException("SYS-9999", "Product", "Nothing Lot");
			else
				return new ArrayList<Lot>();
		}
	}

	/*
	* Name : getProductSpecByLotName
	* Desc : This function is getProductSpecByLotName
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static ProductSpec getProductSpecByLotName ( String lotName){
		LotKey lotKey = new LotKey();
		lotKey.setLotName(lotName);

		Lot lotData = null;

		lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

		ProductSpecKey productSpecKey = new ProductSpecKey();
		productSpecKey.setFactoryName(lotData.getFactoryName());
		productSpecKey.setProductSpecName(lotData.getProductSpecName());
		productSpecKey.setProductSpecVersion(lotData.getProductSpecVersion());

		ProductSpec productSpecData = null;
		productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);

		return productSpecData;
	}
	
	/*
	 * Name : getProductSpecByProductSpecName 
	 * Desc : This function is getProductSpecByProductSpecName 
	 * Author : AIM Systems, Inc 
	 * Date : 2020.08.25
	 */
	public static ProductSpec getProductSpecByProductSpecName(
			String factoryName, String productSpecName,
			String productSpecVersion) throws CustomException {
		try {
			ProductSpecKey productSpecKey = new ProductSpecKey();
			productSpecKey.setFactoryName(factoryName);
			productSpecKey.setProductSpecName(productSpecName);
			productSpecKey.setProductSpecVersion(productSpecVersion);

			ProductSpec productSpecData = null;
			productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);

			return productSpecData;
		} catch (Exception e) {
			throw new CustomException("PRODUCTSPEC-9001", productSpecName);
		}
	}

	/*
	* Name : getProductSpecByLotName
	* Desc : This function is getProductSpecByLotName
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static ConsumableSpec getConsumableSpecByProductName ( String productName) throws CustomException{

		ProductKey productKey = new ProductKey();
		productKey.setProductName(productName);

		Product productData = ProductServiceProxy.getProductService().selectByKey(productKey);

		ConsumableKey consumableKey = new ConsumableKey();
		consumableKey.setConsumableName(productData.getUdfs().get("CRATENAME").toString());

		Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);

		ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
		consumableSpecKey.setFactoryName(consumableData.getFactoryName());
		consumableSpecKey.setConsumableSpecName(consumableData.getConsumableSpecName());
		consumableSpecKey.setConsumableSpecVersion(consumableData.getConsumableSpecVersion());

		ConsumableSpec consumableSpecData = null;
		consumableSpecData = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);

		return consumableSpecData;
	}

	/*
	* Name : getProductSpecByLotName
	* Desc : This function is getProductSpecByLotName
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static ConsumableSpec getConsumableSpec ( String crateName) throws CustomException{

		ConsumableKey consumableKey = new ConsumableKey();
		consumableKey.setConsumableName(crateName);

		Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);

		ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
		consumableSpecKey.setFactoryName(consumableData.getFactoryName());
		consumableSpecKey.setConsumableSpecName(consumableData.getConsumableSpecName());
		consumableSpecKey.setConsumableSpecVersion(consumableData.getConsumableSpecVersion());

		ConsumableSpec consumableSpecData = null;
		consumableSpecData = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);

		return consumableSpecData;
	}

	/*
	* Name : getProcessOperationSpec
	* Desc : This function is getProcessOperationSpec
	* Author : AIM Systems, Inc
	* Date : 2011.03.17
	*/
	public static ProcessOperationSpec getProcessOperationSpec( String factoryName, String processOperationName ) throws CustomException{

		ProcessOperationSpec processOperationData = new ProcessOperationSpec();
		try{
			ProcessOperationSpecKey processOperationKey = new ProcessOperationSpecKey();

			processOperationKey.setFactoryName(factoryName);
			processOperationKey.setProcessOperationName(processOperationName);
			processOperationKey.setProcessOperationVersion("00001");

			processOperationData
				= ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(processOperationKey);

		} catch( Exception e ){
			throw new CustomException("PROCESSOPERATION-9001", processOperationName);
		}

		return processOperationData;
	}
	
	/*
	* Name : getDurableSpecByDurableName
	* Desc : This function is getDurableSpecByDurableName
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static DurableSpec getDurableSpecByDurableName( String durableName ){
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);

		Durable durableData = null;

		durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);

		DurableSpecKey durableSpecKey = new DurableSpecKey();
		durableSpecKey.setFactoryName(durableData.getFactoryName());
		durableSpecKey.setDurableSpecName(durableData.getDurableSpecName());
		durableSpecKey.setDurableSpecVersion(durableData.getDurableSpecVersion());

		DurableSpec durableSpecData = null;
		durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);

		return durableSpecData;
	}

	/*
	* Name : getDurableInfo
	* Desc : This function is getDurableInfo
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static Durable getDurableInfo( String durableName ) throws CustomException{
		try{
			DurableKey durableKey = new DurableKey();
			durableKey.setDurableName(durableName);

			Durable durableData = null;

			durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);

			return durableData;
		}
		catch (Exception e)
		{
			throw new CustomException("DURABLE-9000", durableName);
		}
	}

	/*
	* Name : getPortSpecInfo
	* Desc : This function is getPortSpecInfo
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static PortSpec getPortSpecInfo ( String machineName, String portName ) throws CustomException{

		try
		{
		PortSpecKey portSpecKey = new PortSpecKey();
		portSpecKey.setMachineName(machineName);
		portSpecKey.setPortName(portName);

		PortSpec portSpecData = null;

		portSpecData = PortServiceProxy.getPortSpecService().selectByKey(portSpecKey);

		return portSpecData;
		}
		catch (Exception e) {
			throw new CustomException("PORT-9000",portName);
		}
	}

	public static String getAreaName(String factoryName, String processOperationName, String processOperationVersion)
			throws FrameworkErrorSignal, NotFoundSignal
	{
		ProcessOperationSpecKey key = new ProcessOperationSpecKey();
		key.setFactoryName(factoryName);
		key.setProcessOperationName(processOperationName);
		key.setProcessOperationVersion(processOperationVersion);

		ProcessOperationSpec spec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(key);
		return spec.getDefaultAreaName();
	}

	/*
	* Name : hasText
	* Desc : This function is find Text
	* Author : AIM Systems, Inc
	* Date : 2011.08.10
	*/
	public static boolean hasText(String targetText, String searchText)
	{
		boolean hasText = false;
		if (targetText.length() > 0) {
			if (targetText.indexOf(searchText) > 0)
				hasText = true;
		}
		return hasText;
	}

	/*
	* Name : getPrefix
	* Desc : This function is getPrefix
	* Author : AIM Systems, Inc
	* Date : 2011.08.10
	*/
	public static String getPrefix(String targetText, String delimeter)
	{
		if (targetText.length() > 0)
			targetText = targetText.substring(0, targetText.indexOf(delimeter));
		return targetText;
	}

	public static int getIsSameCharactorCount( String context, String compareCharator, int compareCLength ){
		int iSameCount = 0;
		try{
			for( int i = 0; i < context.length(); i++ ){
				String tempContext = context.substring(i, compareCLength + i);

				if( StringUtils.equals(tempContext, compareCharator)){
					iSameCount++;
				}
			}
		}
		catch( Exception e ){

		}
		return iSameCount;
	}

	/*
	* Name : makeListForQueryAndCount
	* Desc : This function is makeListForQueryAndCount
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static List<String> makeListForQueryAndCount(Element element, String listName, String Name) throws CustomException
	{
		int i = 0;
		String list= "'";
		List<String> argSeq = new ArrayList();

		Element ListElement = element.getChild(listName);

		if( ListElement.getChildren().size() > 0 ){
			for( Iterator iterator = ListElement.getChildren().iterator(); iterator.hasNext(); ){
				Element product = (Element) iterator.next();
				String productName = product.getChild(Name).getText();
				list =  list + productName + "', '";
				i = i + 1;
			}
		}
		list = list + "'";
		try{
			if( list.length() > 4){
				list = list.substring(0, list.length() - 4);
			}
			argSeq.add(list);
			argSeq.add(Integer.toString(i));
		}
		catch(Exception e){
			//throw new Exception();
		}
		return argSeq;
	}

	/*
	* Name : strConvent
	* Desc : This function is spiltGlassGrade String Convent
	* Author : AIM Systems, Inc
	* Date : 2011.08.08
	*/
	public static int strConvent(String id)
	{
		int num = 0;
		for(int i = 65; i < 91; i++)
		{
			if(id.charAt(0) >= 86)
			{
				if(id.charAt(0) == i)
				{
					num = i - 57;
					break;
				}
			}
			else
			{
				if(id.charAt(0) == i)
				{
					num = i - 55;
					break;
				}
			}
		}

		return num;
	}

	/**
	 * 150311 by swcho : modified with correction
	 * to get generic naming rule
	 * @author swcho
	 * @since 2013.09.23
	 * @param ruleName
	 * @param nameRuleAttrMap
	 * @param quantity
	 * @param originalSourceSubjectName
	 * @return
	 * @throws CustomException
	 */
	public static List<String> getNameByNamingRule(String ruleName, Map<String, Object> nameRuleAttrMap, int quantity,
														String sendSubjectName, String workOrderType)
		throws CustomException
	{
		List<String> lstResult = new ArrayList<String>();

		try
		{
			//set param for query
			HashMap<String, Object> paraMap = new HashMap<String, Object>();
			paraMap.put("RULENAME", ruleName);
			paraMap.put("QUANTITY", String.valueOf(quantity));
			paraMap.put("WORKORDERTYPE", workOrderType);

			Document doc = SMessageUtil.generateQueryMessage("GetNameList",
																paraMap,
																(HashMap<String, Object>) nameRuleAttrMap,
																"", "MES", "");

			//doc = ESBService.sendBySenderReturnMessage(replySubjectName, doc,
			//											doc.getRootElement().getChild(SMessageUtil.Body_Tag),
			//											"QRYSender", "");

			doc = GenericServiceProxy.getESBServive().sendRequestBySender(sendSubjectName, doc, "QRYSender");

			if (doc != null)
			{
				Element returnList = XmlUtil.getNode(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Return_Tag);

				String errorCode = SMessageUtil.getChildText(returnList, SMessageUtil.Result_ReturnCode, true);

				if (!errorCode.equals("0"))
				{
					String errorString = SMessageUtil.getChildText(returnList, SMessageUtil.Result_ErrorMessage, false);

					throw new Exception(errorString);
				}

				//parsing result
				Element resultList = XmlUtil.getNode(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/" + "DATALIST");

				for (Iterator iterator = resultList.getChildren().iterator(); iterator.hasNext();) {
					Element resultData = (Element) iterator.next();

					String name = resultData.getChildText("NAMEVALUE");

					lstResult.add(name);
				}
			}
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0000", ex.getMessage());
		}

		return lstResult;
	}

	/**
	 * modified to tune
	 * @author swcho
	 * @since 2013.09.26
	 * @param ruleName
	 * @param nameRuleAttrMap
	 * @param quantity
	 * @return generatedNameList
	 */
	public static List<String> generateNameByNamingRule(String ruleName, Map<String, Object> nameRuleAttrMap, int quantity, String workOrderType) throws CustomException
	{
		if(log.isInfoEnabled()){
			log.debug("ruleName = " + ruleName);
			log.debug("demand = " + quantity);
		}

		//get single Lot ID
		//${location}.${factory}.${cim}.${mode}.${svr}
		StringBuffer nameServerSubjectName = new StringBuffer(GenericServiceProxy.getESBServive().getSendSubject("QRYsvr"));
		/*StringBuffer nameServerSubjectName = new StringBuffer(System.getProperty("location")).append(".")
													.append(System.getProperty("factory")).append(".")
													.append(System.getProperty("cim")).append(".")
													.append(System.getProperty("mode")).append(".")
													.append(System.getProperty("shop")).append(".")
													.append("PEXsvr");*/

		List<String> lstLotName;

		try {
			lstLotName = CommonUtil.getNameByNamingRule(ruleName, nameRuleAttrMap, quantity, nameServerSubjectName.toString(), workOrderType);
		} catch (CustomException e) {
			lstLotName = new ArrayList<String>();
			//throw new CustomException("SYS-0000", e.getMessage());
			throw e;
		}

		return lstLotName;
	}

	/**
	 * name generator
	 * @author swcho
	 * @since 2014.04.16
	 * @param ruleName
	 * @param nameRuleAttrMap
	 * @param quantity
	 * @return
	 * @throws CustomException
	 */
	public static List<String> generateNameByNamingRule(String ruleName, Map<String, Object> nameRuleAttrMap, long quantity)
		throws CustomException
	{
		List<String> argSeq = new ArrayList();

		try
		{
			NameGeneratorRuleDef ruleDef;

			try
			{
				ruleDef = NameServiceProxy.getNameGeneratorRuleDefService().selectByKey(
												new NameGeneratorRuleDefKey(ruleName));
			}
			catch (NotFoundSignal ne)
			{
				throw new CustomException("SYS-9001", NameGeneratorRuleDef.class.getSimpleName());
			}

			String namingRuleSql = CommonUtil.getValue(ruleDef.getUdfs(), "SQL");

//			List<Map<String, Object>> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.
//													getSqlMesTemplate().queryForList(namingRuleSql, nameRuleAttrMap);
			List<Map<String, Object>> sqlResult = null;
			if (StringUtil.isNotEmpty(namingRuleSql))
			{
				sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(namingRuleSql, nameRuleAttrMap);
			}

			List<NameGeneratorRuleAttrDef> nameGeneratorRuleAttrDefList;

			try
			{
				nameGeneratorRuleAttrDefList = NameServiceProxy.getNameGeneratorRuleAttrDefService()
													.getAllRuleAttrDefs(ruleName);
			}
			catch (NotFoundSignal ne)
			{
				throw new CustomException("SYS-9001", NameGeneratorRuleAttrDef.class.getSimpleName());
			}

			for (NameGeneratorRuleAttrDef nameGeneratorRuleAttrDef : nameGeneratorRuleAttrDefList)
			{
				if(nameGeneratorRuleAttrDef.getSectionType().equals("Argument"))
				{
					int position = (int) nameGeneratorRuleAttrDef.getKey().getPosition();

					try
					{
						argSeq.add(sqlResult.get(position).get("SECTIONVALUE").toString());
					}
					catch (Exception ex)
					{
						//150825 by swcho : get by name
						String argName = nameGeneratorRuleAttrDef.getSectionName();

						for (Map<String, Object> row : sqlResult)
						{
							if (row.get("SECTIONNAME").toString().equals(argName))
							{
								argSeq.add(row.get("SECTIONVALUE").toString());
							}
						}
					}
				}
			}

			List<String> names = NameServiceProxy.getNameGeneratorRuleDefService().generateName(ruleName, argSeq, quantity);

			String[] receiveNames = new String[names.size()];

			List<String> convertedNames = new ArrayList<String>();

			for (int i = 0; i < names.size(); i++)
			{
				String convertedName = convertReceivedNaming(ruleName, names.get(i).toString());

				convertedNames.add(convertedName);
				//receiveNames[i] = names.get(i).toString();
				receiveNames[i] = convertedName;

				log.info("ID LIST = " + i + " : " + receiveNames[i]);
			}

			return convertedNames;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "NameGeneratorRuleDefService", fe.getMessage());
		}
	}

	/*
	* Name : getMachineRecipeByTPFO
	* Desc : This function is getMachineRecipeByTPFOM
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static List<Map<String, Object>> getMachineRecipeByTPFO(String lotName, String machineName){
		//log.info("START getMachineRecipeByTPFOM");
		String sql = "SELECT MACHINERECIPENAME " +
				"FROM POSMACHINE PM, TPFOPOLICY TP " +
				"WHERE 1=1 " +
				"AND PM.MACHINENAME = :machineName " +
				"AND PM.CONDITIONID = TP.CONDITIONID " +
				"AND TP.FACTORYNAME = :factoryName " +
				"AND TP.PRODUCTSPECNAME = :productSpecName " +
				"AND TP.PROCESSFLOWNAME = :processFlowName " +
				"AND TP.PROCESSOPERATIONNAME = :procesOperationName ";

		Map<String, String> bindMap = new HashMap<String, String>();

		LotKey lotKey = new LotKey();
		lotKey.setLotName(lotName);

		Lot lotData = null;
		lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

		bindMap.put("machineName", machineName);
		bindMap.put("factoryName", lotData.getFactoryName());
		bindMap.put("productSpecName", lotData.getProductSpecName());
		bindMap.put("processFlowName", lotData.getProcessFlowName());
		bindMap.put("procesOperationName", lotData.getProcessOperationName());

//		List<Map<String, Object>> sqlResult
//		  = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);

		List<Map<String, Object>> sqlResult
		  = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		String machineRecipe = "";
		if(sqlResult.size() > 0)
			machineRecipe = sqlResult.get(0).get("MACHINERECIPENAME").toString();

		log.info("MachineRecipe = " +  machineRecipe);
		log.info("END getMachineRecipeByTPFO");
		return sqlResult;
	}

	/**
	 * convert MES name according to rule type
	 * @author swcho
	 * @since 2014.04.17
	 * @param ruleName
	 * @param name
	 * @return
	 */
	public static String convertReceivedNaming(String ruleName, String name)
	{
		String convertedName = name;

		try
		{
			if (ruleName.equals("LotNaming"))
			{
				//handling only by query
				/*int length = 2;

				//parse only serial partition
				String serial = StringUtil.substring(name, name.length() - length);

				if (!StringUtil.isEmpty(serial) && serial.length() == length)
				{
					//must be numeric with single digit
					String target = StringUtil.substring(serial, 0, 1);
					String suffix = StringUtil.substring(serial, 1, length);
					String alpha = "";

					switch (Integer.parseInt(target))
					{
						case 0: alpha = "A";break;
						case 1: alpha = "B";break;
						case 2: alpha = "C";break;
						case 3: alpha = "D";break;
						case 4: alpha = "E";break;
						case 5: alpha = "F";break;
						case 6: alpha = "G";break;
						case 7: alpha = "H";break;
						case 8: alpha = "J";break;
						case 9: alpha = "K";break;
						default: alpha = target;
					}

					convertedName = new StringBuilder()
										.append(StringUtil.removeEnd(name, serial))
										.append(alpha).append(suffix).toString();
				}*/
			}
		}
		catch (Exception ex)
		{
			//ignore
		}

		return convertedName;
	}

	/**
	 * get value from dictionary by key
	 * @author swcho
	 * @since 2013.11.06
	 * @param map
	 * @param keyName
	 * @return
	 */
	public static String getValue(Map map, String keyName)
	{
		try
		{
			Object value = map.get(keyName);

			if (value != null && value instanceof String)
				return value.toString();
			else if (value != null && value instanceof BigDecimal)
				return value.toString();
		}
		catch(Exception ex)
		{
			log.debug(ex.getMessage());
		}

		return "";
	}

	/**
	 * @author smkang
	 * @since 2014-02-20
	 * @param element
	 * @return namedValueSequence
	 * @throws FrameworkErrorSignal
	 * @throws NotFoundSignal
	 */
	public static Map<String, String> setNamedValueSequence(Element element, String typeName)
			throws FrameworkErrorSignal, NotFoundSignal
	{
		Map<String, String> namedValuemap = new HashMap<String, String>();

		List<ObjectAttributeDef> objectAttributeDefs = greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(typeName, "ExtendedC");

		//log.info("UDF SIZE=" + objectAttributeDefs.size());

		if (objectAttributeDefs != null)
		{
			for (int i = 0; i < objectAttributeDefs.size(); i++)
			{
				String name = "";
				String value = "";

				if (element != null)
				{
					for (int j = 0; j < element.getContentSize(); j++)
					{
						if (element.getChildText(objectAttributeDefs.get(i).getAttributeName()) != null)
						{
							name = objectAttributeDefs.get(i).getAttributeName();
							value = element.getChildText(objectAttributeDefs.get(i).getAttributeName());

							break;
						}
						else
						{
							name = objectAttributeDefs.get(i).getAttributeName();
						}
					}
				}

				//140821 by swcho : empty value could not be modified in UDF
				if (name.equals("") != true && StringUtil.isNotEmpty(value))
					namedValuemap.put(name, value);
			}
		}

		log.info("UDF SIZE=" + namedValuemap.size());
		return namedValuemap;
	}

	/*
	* Name : getProductAttributeByLength
	* Desc : This function is getProductAttributeByLength
	* Author : AIM Systems, Inc
	* Date : 2011.05.20
	*/
	public static String getProductAttributeByLength(String machineName, String productName, String fieldName, String itemValue) throws CustomException
	{
		String sql = "SELECT LENGTH(" + fieldName + ") AS LEN, " + fieldName + " FROM PRODUCT WHERE PRODUCTNAME = :productName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("productName", productName);

		try{

//			List<Map<String,Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);

			List<Map<String,Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if(sqlResult.size() > 0)
			{
				if(!(StringUtils.isEmpty((String)sqlResult.get(0).get(fieldName))))
				{
					if(Integer.parseInt(sqlResult.get(0).get("LEN").toString()) != itemValue.length() )
					{
						itemValue = (String)sqlResult.get(0).get(fieldName);
						log.debug("Product Info Different In Field: " + fieldName + ". Reported Value: " + itemValue + ". Machine ID: " + machineName);
					}
				}
				else
				{
					sql = "UPDATE PRODUCT SET " + fieldName +  " = :itemValue WHERE PRODUCTNAME = :productName";
					bindMap.put("itemValue", itemValue);

//					greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
					GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
				}
			}
		}
		catch(Exception e)
		{
			throw new CustomException("COM-9001", fieldName);
		}
		return itemValue;
	}

	/**
	 * convert collection array to String
	 * @author swcho
	 * @since 2014.04.29
	 * @param obj
	 * @return
	 */
	public static String toStringFromCollection(Object obj)
	{
		StringBuffer temp = new StringBuffer("");

		if (obj instanceof Object[])
		{
			for (Object element : (Object[]) obj)
			{
				if (!temp.toString().isEmpty())
					temp.append(",");

				try
				{
					temp.append(element.toString());
				}
				catch (Exception ex)
				{
					if (log.isDebugEnabled())
						log.debug(ex.getMessage());
				}
			}
		}
		else if (obj instanceof List)
		{
			for (Object element : (List<Object>) obj)
			{
				if (!temp.toString().isEmpty())
					temp.append(",");

				try
				{
					temp.append(element.toString());
				}
				catch (Exception ex)
				{
					if (log.isDebugEnabled())
						log.debug(ex.getMessage());
				}
			}
		}

		return temp.toString();
	}

	/*
	* Name : getProductdata
	* Desc : This function is getMachineInfo
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static Product getProductData( String productName ) throws CustomException{
		ProductKey productKey = new ProductKey();
		productKey.setProductName(productName);

		Product productData = null;

		try {
			productData = ProductServiceProxy.getProductService().selectByKey(productKey);
		}catch (Exception e){
			throw new CustomException("PRODUCT-0001", productName);
		}

		return productData;
	}

	/**
	 * Added by hykim
	 * @param factoryName
	 * @param processFlowName
	 * @param processFlowVersion
	 * @return List<String>
	 * @throws
	 */
	public static List<String> getOperList(String factoryName, String processFlowName, String processFlowVersion) throws CustomException
	{
		String sql = "SELECT N.NODEATTRIBUTE1, PC.PROCESSOPERATIONTYPE FROM NODE N, ARC A, PROCESSOPERATIONSPEC PC" +
					 " WHERE N.FACTORYNAME = :factoryName " +
					 " AND N.FACTORYNAME = PC.FACTORYNAME " +
					 " AND A.FROMNODEID = N.NODEID " +
					 " AND N.NODEATTRIBUTE1 = PC.PROCESSOPERATIONNAME " +
					 " AND N.PROCESSFLOWNAME = :processFlowName " +
					 " AND N.PROCESSFLOWVERSION = :processFlowVersion " +
					 " AND N.NODETYPE = :nodeType ";
					 //" ORDER BY XCOORDINATE, YCOORDINATE ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("factoryName", factoryName);
		bindMap.put("processFlowName", processFlowName);
		bindMap.put("processFlowVersion", processFlowVersion);
		bindMap.put("nodeType", GenericServiceProxy.getConstantMap().Node_ProcessOperation);

//		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		List<String> operList = new ArrayList<String>();

		if( sqlResult.size() > 0)
		{
			for(int i=0; i<sqlResult.size(); i++)
			{
				String operationType = sqlResult.get(i).get("PROCESSOPERATIONTYPE").toString();

				if( operationType.equals("Inspection"))
				{
					operList.add(sqlResult.get(i).get("NODEATTRIBUTE1").toString());
				}
			}
		}
		return operList;
	}

	/**
	 * Added by hykim
	 * @param factoryName
	 * @param processFlowName
	 * @param processFlowVersion
	 * @return List<String>
	 * @throws
	 */
	public static List<String> getProductionOperList(String factoryName, String processFlowName, String processFlowVersion) throws CustomException
	{
		String sql = "SELECT N.NODEATTRIBUTE1, PC.PROCESSOPERATIONTYPE FROM NODE N, ARC A, PROCESSOPERATIONSPEC PC" +
					 " WHERE N.FACTORYNAME = :factoryName " +
					 " AND N.FACTORYNAME = PC.FACTORYNAME " +
					 " AND A.FROMNODEID = N.NODEID " +
					 " AND N.NODEATTRIBUTE1 = PC.PROCESSOPERATIONNAME " +
					 " AND N.PROCESSFLOWNAME = :processFlowName " +
					 " AND N.PROCESSFLOWVERSION = :processFlowVersion " +
					 " AND N.NODETYPE = :nodeType ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("factoryName", factoryName);
		bindMap.put("processFlowName", processFlowName);
		bindMap.put("processFlowVersion", processFlowVersion);
		bindMap.put("nodeType", GenericServiceProxy.getConstantMap().Node_ProcessOperation);

//		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		List<String> operList = new ArrayList<String>();

		if( sqlResult.size() > 0)
		{
			for(int i=0; i<sqlResult.size(); i++)
			{
				String operationType = sqlResult.get(i).get("PROCESSOPERATIONTYPE").toString();

				if( operationType.equals("Production"))
				{
					operList.add(sqlResult.get(i).get("NODEATTRIBUTE1").toString());
				}
			}
		}
		return operList;
	}

	/**
	 * Added by hykim
	 * @param lotName
	 * @param String
	 * @return List<String>
	 * @throws
	 */
	public static List<String> splitString(String regex, String str) throws CustomException
	{
		List<String> strList = new ArrayList<String>();
		String[] strArray = str.split(regex);

		for(String strTemp : strArray)
		{
			strList.add(strTemp);
		}

		return strList;
	}

	/**
	 * Added by hykim
	 * @param lotName
	 * @param double
	 * @return String
	 * @throws
	 */
	public static String makeProductSamplingPositionList(int productSamplingCount) throws CustomException
	{
		String productSamplePositionList = "";

		for(int i=1; i<productSamplingCount+1; i++)
		{
			productSamplePositionList = productSamplePositionList + String.valueOf(i);

			if(i != productSamplingCount)
				productSamplePositionList = productSamplePositionList + ",";
		}

		return productSamplePositionList;
	}

	/**
	 * Added by hykim
	 * @param lotName
	 * @param double
	 * @return String
	 * @throws
	 */
	public static String StringValueOf(double d) throws CustomException
	{
		String str = "";

		if(d == 0)
			str = "0";
		else
		{
			str = String.valueOf(d);

			if(str.indexOf(".") > 0)
			{
				str = str.substring(0, str.indexOf("."));
				//List<String> strList = CommonUtil.splitString(".", str);

				//str = strList.get(0);
			}
		}

		return str;
	}

	/** copyToObjectList
	 * Added by hykim
	 * @param List<Object>
	 * @return List<Object>
	 * @throws
	 */
	public static List<String> copyToStringList(List<String> strList) throws CustomException
	{
		List<String> returnStrList = new ArrayList<String>();

		for(String str : strList)
		{
			returnStrList.add(str);
		}

		return returnStrList;
	}

	/** toStringWithoutBrackets
	 * Added by hykim
	 * @param List<Object>
	 * @return List<Object>
	 * @throws
	 */
	public static String toStringWithoutBrackets(List<String> strList) throws CustomException
	{
		String str = strList.toString();

		str = str.replace("[", "");
		str = str.replace("]", "");

		return str;
	}

	/*
	* Name : getMaskInfoBydurableName
	* Desc : This function is getLotInfoBydurableName
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static List<Durable> getMaskInfoBydurableName(String carrierName,String durabletype) throws CustomException{
		//log.info("START getLotInfoBydurableName");
		String condition = "WHERE durabletype = : durabletype and MASKCARRIERNAME = :carrierName ";

		Object[] bindSet = new Object[] {durabletype,carrierName,};
		List<Durable> maskList = new ArrayList<Durable>();
		try
		{
			maskList = DurableServiceProxy.getDurableService().select(condition, bindSet);
		}
		catch(Exception e)
		{
			maskList = null;
			throw new CustomException("MASK-0009",carrierName);
		}
		//log.info("END getLotInfoBydurableName");
		return maskList;
	}

	/**
	 * whether Lot has reached end of any flow?
	 * @author swcho
	 * @since 2015.03.01
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public static boolean isLotInEndOperation(Lot lotData, String factoryName, String processFlowName)
		throws CustomException
	{
		try
		{
			Node endNode = ProcessFlowServiceProxy.getProcessFlowService().getEndNode(new ProcessFlowKey(factoryName, processFlowName, "00001"));

			List<Arc> connectArc = ProcessFlowServiceProxy.getArcService().select("toNodeId = ?", new Object[] {endNode.getKey().getNodeId()});

			Node lastOperationNode = ProcessFlowServiceProxy.getNodeService().getNode(connectArc.get(0).getKey().getFromNodeId());

			String targetFactoryName = lastOperationNode.getFactoryName();
			String targetFlowName = lastOperationNode.getProcessFlowName();
			String targetOperationName = lastOperationNode.getNodeAttribute1();

			if (lotData.getFactoryName().equals(targetFactoryName)
					&& lotData.getProcessFlowName().equals(targetFlowName)
					&& lotData.getProcessOperationName().equals(targetOperationName))
			{
				return true;
			}
		}
		catch (Exception ex)
		{
			//all components are mandatory
			log.warn("this Lot might not be located anywhere");
		}

		return false;
	}

	/*
	* Name : isInitialInput
	* Desc : This function is isInitialInput
	* Author : xzquan
	* Date : 2016.02.20
	*/
	public static boolean isInitialInput(String machineName)
			throws CustomException
	{
		boolean isInitialInput = false;

		try
		{
			MachineSpecKey mSpecKey = new MachineSpecKey(machineName);
			MachineSpec mSpec = MachineServiceProxy.getMachineSpecService().selectByKey(mSpecKey);
			String constructType = mSpec.getUdfs().get("CONSTRUCTTYPE").toString();

			if(constructType.equals("UNPK"))
			{
				isInitialInput = true;
			}
		}
		catch (Exception ex)
		{
			log.error(ex.getMessage());
		}

		return isInitialInput;
	}
	

	/*
	 * Name : getProcessFlowData Desc : This function is getProcessFlowData
	 * Author : AIM Systems, Inc Date : 2014.12.20
	 */
	public static ProcessFlow getProcessFlowData(Lot lotData)
			throws CustomException {

		ProcessFlow processFlowData = new ProcessFlow();

		try {
			ProcessFlowKey processFlowKey = new ProcessFlowKey();
			processFlowKey.setFactoryName(lotData.getFactoryName());
			processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
			processFlowKey.setProcessFlowVersion(lotData
					.getProcessFlowVersion());

			processFlowData = ProcessFlowServiceProxy.getProcessFlowService()
					.selectByKey(processFlowKey);
		} catch (Exception e) {
			throw new CustomException("PROCESSOPERATION-9000",
					lotData.getProcessOperationName());
		}

		return processFlowData;
	}


	/*
	 * Name : getProductByProductName Desc : This function is
	 * getProductByProductName Author : AIM Systems, Inc Date : 2013.03.07
	 */
	public static Product getProductByProductName(String productName)
			throws CustomException {
		try {
			ProductKey productKey = new ProductKey();
			productKey.setProductName(productName);

			Product productData = null;
			productData = ProductServiceProxy.getProductService().selectByKey(
					productKey);

			return productData;
		} catch (Exception e) {
			throw new CustomException("PRODUCT-9000", productName);
		}
	}

	/*
	 * Name : makeList Desc : This function is makeList Author : AIM Systems,
	 * Date : 2019.05.16
	 */
	public static List<String> makeList(Element element, String listName,
			String name) {
		// Caution.
		// This list size should not exceed 1000 when it uses for SQL BindSet
		// without SQL Exception.
		String childText = "";
		List<String> list = new ArrayList<String>();
		Element childElement = null;
		Element listElement = element.getChild(listName);

		if (listElement != null) {
			for (Iterator<?> iterator = listElement.getChildren().iterator(); iterator
					.hasNext();) {
				childElement = (Element) iterator.next();
				childText = childElement.getChildText(name);
				if (StringUtil.isNotEmpty(childText)) {
					list.add(childText);
				}
			}
		} else {
			listElement = element;
			if (listElement != null) {
				for (Iterator<?> iterator = listElement.getChildren()
						.iterator(); iterator.hasNext();) {
					childElement = (Element) iterator.next();
					childText = childElement.getChildText(name);
					if (StringUtil.isNotEmpty(childText)) {
						list.add(childText);
					}
				}
			}
		}
		log.info(list);

		return list;
	}	


	/*
	* Name : isBpkType
	* Desc : This function is isBpkType
	* Author : Aim System
	* Date : 2016.06.22
	*/
	public static boolean isBpkType(String machineName)
			throws CustomException
	{
		boolean isInitialInput = false;

		try
		{
			MachineSpecKey mSpecKey = new MachineSpecKey(machineName);
			MachineSpec mSpec = MachineServiceProxy.getMachineSpecService().selectByKey(mSpecKey);
			String constructType = mSpec.getUdfs().get("CONSTRUCTTYPE").toString();

			if(constructType.equals("BPK"))
			{
				isInitialInput = true;
			}
		}
		catch (Exception ex)
		{
			log.error(ex.getMessage());
		}

		return isInitialInput;
	}

	/**
	 * is inline EQP
	 * @author swcho
	 * @since 2016-04-27
	 * @param machineName
	 * @return
	 * @throws CustomException
	 */
	public static boolean isInineType(String machineName)
		throws CustomException
	{
		boolean isIt = false;

		try
		{
			MachineSpecKey mSpecKey = new MachineSpecKey(machineName);
			MachineSpec mSpec = MachineServiceProxy.getMachineSpecService().selectByKey(mSpecKey);

			String constructType = mSpec.getUdfs().get("CONSTRUCTTYPE").toString();

			if(constructType.equals("EVA"))
			{
				isIt = true;
			}
		}
		catch (Exception ex)
		{
			log.error(ex.getMessage());
		}

		return isIt;
	}

	//-- COMMENT
		//-- 2016.02.17 LEE HYEON WOO
		public static String getProcessFlowNameByFGCode(Lot lotData, String productSpecName){
			String processFlowName = "";

			ProductSpecKey productSpecKey = new ProductSpecKey();
			productSpecKey.setFactoryName(lotData.getFactoryName());
			productSpecKey.setProductSpecName(productSpecName);
			productSpecKey.setProductSpecVersion("00001");

			ProductSpec productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);

			processFlowName = productSpecData.getProcessFlowName();

			return processFlowName;
		}
		public static ProcessFlow getProcessFlow(Lot lotData){
			ProcessFlowKey processFlowKey = new ProcessFlowKey();
			processFlowKey.setFactoryName(lotData.getFactoryName());
			processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
			processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());
			ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
			
			return processFlowData;
		}

		//-- COMMENT
		//-- 2011.02.16 JUNG SUN KYU // 2016.02.22 LEE HYEON WOO
		public static String getProcessFlowNameByFGCode(String productSpecName){
			String processFlowName = "";

			ProductSpecKey productSpecKey = new ProductSpecKey();
			productSpecKey.setFactoryName("MODULE");
			productSpecKey.setProductSpecName(productSpecName);
			productSpecKey.setProductSpecVersion("00001");

			ProductSpec productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);

			processFlowName = productSpecData.getProcessFlowName();

			return processFlowName;
		}

		//-- COMMENT
		//-- 2016.02.17 LEE HYEON WOO
		public static String getNodeStack( String factoryName, String processFlowName, String processOperationName){
			String nodeStack = "";
			String sql = "SELECT NODEID FROM NODE WHERE " ;
			sql += " FACTORYNAME = :factoryName ";
			sql += " AND PROCESSFLOWNAME = :processFlowName ";
			sql += " AND PROCESSFLOWVERSION = :processFlowVersion ";
			sql += " AND NODEATTRIBUTE1 = :processOperationName ";
			sql += " AND NODEATTRIBUTE2 = :processOperationVersion ";

			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("factoryName", factoryName);
			bindMap.put("processFlowName", processFlowName);
			bindMap.put("processFlowVersion", "00001");
			bindMap.put("processOperationName", processOperationName);
			bindMap.put("processOperationVersion", "00001");

			List<Map<String, Object>> sqlResult
			= greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);

			if( sqlResult.size() == 1 ){
				nodeStack = sqlResult.get(0).get("NODEID").toString();
			} else {
				nodeStack = "";
			}
			return nodeStack;
		}

		//-- COMMENT
		//-- 2016.02.17 LEE HYEON WOO
		public static ProcessOperationSpec getProcessOperationSpec( Lot lotData, String stepID ) throws CustomException{

			ProcessOperationSpec processOperationData = new ProcessOperationSpec();
			try{
				ProcessOperationSpecKey processOperationKey = new ProcessOperationSpecKey();

				processOperationKey.setFactoryName(lotData.getFactoryName());
				processOperationKey.setProcessOperationName(stepID);
				processOperationKey.setProcessOperationVersion("00001");

				processOperationData
					= ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(processOperationKey);

			} catch( Exception e ){
				throw new CustomException("PROCESSOPERATION-9000", stepID);
			}

			return processOperationData;
		}

		//-- COMMENT
		//-- 2011.01.28 // 2016.02.23 LEE HYEON WOO
		public static List<Map<String, Object>> trackOutInfoResult ( Lot lotData, String serviceName ){
			// Set Sql
			String sql = "SELECT * FROM CT_TRACKOUTINFO WHERE SERVICENAME = :serviceName AND PROCESSOPERATIONNAME = :processOpreationName";

			// Set bindMap and performs the query.
			Map<String, String> bindMap = new HashMap<String, String>();

			bindMap.put("serviceName", serviceName);
			bindMap.put("processOpreationName", lotData.getProcessOperationName().toString());

			List<Map<String, Object>> sqlResult
			= greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);

			if( sqlResult.size() == 0 ){
				// Throw
			}
			return sqlResult;
		}

		/**
		 * getFirstPlanByMachine
		 * 151105 by xzquan : service object changed
		 * @author xzquan
		 * @since 2015.11.08
		 * @param eventInfo
		 * @param machineName
		 * @throws CustomException
		 */
		public static ProductRequestPlan getFirstPlanByMachine(String machineName, boolean flag)
			throws CustomException
		{
			try
			{
				ProductRequestPlan pPlan = new ProductRequestPlan();

				if(flag)
				{
					//String condition = "assignedMachineName = ? and productRequestState IN (?, ?, ?) "
					//		+ "and position = (select min(position) from productRequestPlan Where assignedMachineName = ? and productRequestState IN (?, ?, ?))";
					//Object bindSet[] = new Object[]{machineName, "Completing", "Started", "Aborted", machineName, "Completing", "Started", "Aborted"};
					String condition =   " assignedMachineName = ?                                                            "
									   + " AND productRequestState IN (?, ?, ?)                                               "
									   + " AND planreleasedtime = (SELECT MIN(PLANRELEASEDTIME)                               "
									   + " 						     FROM productRequestPlan                                  "
									   + " 						    WHERE 1=1                                                 "
									   + " 						      AND assignedMachineName = ?                             "
									   + " 						      AND productRequestState IN (?, ?, ?)                    "
									   + " 						      AND position = (SELECT MIN(position)                    "
									   + "                     						    FROM productRequestPlan               "
									   + "                     						   WHERE assignedMachineName = ?          "
									   + "                     					   	     AND productRequestState IN (?, ?, ?) "
									   + " 										     )                                        "
									   + " 						   )                                                          " ;
					Object bindSet[] = new Object[]{machineName, "Completed", "Started", "Aborted",
							                        machineName, "Completed", "Started", "Aborted",
							                        machineName, "Completed", "Started", "Aborted"};
					List<ProductRequestPlan> pPlanList = ProductRequestPlanServiceProxy.getProductRequestPlanService().select(condition, bindSet);

					if(pPlanList.size() > 1)
					{
						throw new CustomException("PRODUCTREQUEST-0022", machineName);
					}

					pPlan = pPlanList.get(0);
				}
				else
				{
					//String condition = "assignedMachineName = ? and productRequestState IN (?, ?) "
					//		+ "and position = (select min(position) from productRequestPlan Where assignedMachineName = ? and productRequestState IN (?, ?))";
					//Object bindSet[] = new Object[]{machineName, "Released", "Started", machineName, "Released", "Started"};
					String condition =   " assignedMachineName = ?                                                            "
							           + " AND productRequestState IN (?, ?)                                                  "
							           + " AND planreleasedtime = (SELECT MIN(PLANRELEASEDTIME)                               "
							           + " 						     FROM productRequestPlan                                  "
						         	   + " 						    WHERE 1=1                                                 "
					        		   + " 						      AND assignedMachineName = ?                             "
					         		   + " 						      AND productRequestState IN (?, ?)                       "
							           + " 						      AND position = (SELECT MIN(position)                    "
							           + "                     						    FROM productRequestPlan               "
							           + "                     						   WHERE assignedMachineName = ?          "
							           + "                     					   	     AND productRequestState IN (?, ?)    "
							           + " 										     )                                        "
							           + " 						   )                                                          " ;
			        Object bindSet[] = new Object[]{machineName, "Released", "Started",
					                                machineName, "Released", "Started",
					                                machineName, "Released", "Started"};
					List<ProductRequestPlan> pPlanList = ProductRequestPlanServiceProxy.getProductRequestPlanService().select(condition, bindSet);

					if(pPlanList.size() > 1)
					{
						throw new CustomException("PRODUCTREQUEST-0022", machineName);
					}

					pPlan = pPlanList.get(0);
				}

				return pPlan;
			}
			//160603 by swcho : error enhanced
			catch (NotFoundSignal ne)
			{
				throw new CustomException("PRODUCTREQUEST-0021", machineName);
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("PRODUCTREQUEST-0021", machineName);
			}
		}

		/**
         * getFirstPlanByMachine
         * 151105 by xzquan : service object changed
         * @author xzquan
         * @since 2015.11.08
         * @param eventInfo
         * @param machineName
         * @throws CustomException
         */
        public static ProductRequestPlan getFirstPlanByCrateSpecName(String cratespecname, String reserveState)
            throws CustomException
        {
            try
            {
                ProductRequestPlan pPlan = new ProductRequestPlan();

                String condition =    "    PLANRELEASEDTIME = (                                                                   \n"
                                    + "                            SELECT MIN(PRP.PLANRELEASEDTIME)                               \n"
                                    + "                            FROM PRODUCTREQUEST PR,                                        \n"
                                    + "                                 PRODUCTREQUESTPLAN PRP,                                   \n"
                                    + "                                 CT_RESERVELOT C                                           \n"
                                    + "                        WHERE 1=1                                                          \n"
                                    + "                            AND PR.CRATESPECNAME = ?                                       \n"
                                    + "                            AND PR.PRODUCTREQUESTNAME = PRP.PRODUCTREQUESTNAME             \n"
                                    + "                            AND PR.PRODUCTREQUESTSTATE NOT IN (?,?)                        \n"
                                    + "                            AND PR.PRODUCTREQUESTTYPE NOT LIKE ?                           \n"
                                    + "                            AND PR.PRODUCTREQUESTHOLDSTATE <> ?                            \n"
                                    + "                            AND PR.RELEASEDQUANTITY < PR.PLANQUANTITY                      \n"
                                    //2019.01.07_hsryu_Modify Logic. Mantis 2263 
									+ "                            AND (TO_CHAR(PRP.PLANRELEASEDTIME, ?)||?) <= TO_CHAR(SYSDATE, ?)                             \n"
                                    //+ "                            AND PR.PLANRELEASEDTIME <= SYSDATE                             \n"
									//20190117, hhlee, modify, add PRODUCTREQUESTSTATE condition 
                                    + "                            AND PRP.PRODUCTREQUESTSTATE <> ?                               \n"
                                    + "                            AND PR.PRODUCTREQUESTNAME = C.PRODUCTREQUESTNAME               \n"
                                    //20190118, hhlee, modify, add PLANRELEASEDTIME condition 
                                    + "                            AND PRP.PLANRELEASEDTIME = C.PLANRELEASEDTIME                  \n"
                                    + "                            AND C.RESERVESTATE = ?                                         \n"
                                    + "                            )                                                              \n";
                Object bindSet[] = new Object[]{cratespecname, "Completed", "Finished", "R%", "Y", "YYYYMMDD", "070000", "YYYYMMDDHH24MISS", "Completed", reserveState};
                List<ProductRequestPlan> pPlanList = ProductRequestPlanServiceProxy.getProductRequestPlanService().select(condition, bindSet);

                if(pPlanList.size() > 1)
                {
                    throw new CustomException("PRODUCTREQUEST-0022", cratespecname);
                }

                pPlan = pPlanList.get(0);

                return pPlan;
            }
            //160603 by swcho : error enhanced
            catch (NotFoundSignal ne)
            {
                throw new CustomException("PRODUCTREQUEST-0053", cratespecname);
            }
            catch (FrameworkErrorSignal fe)
            {
                throw new CustomException("PRODUCTREQUEST-0053", cratespecname);
            }
        }

        /**
         * getFirstPlanByMachine
         * 151105 by xzquan : service object changed
         * @author xzquan
         * @since 2015.11.08
         * @param eventInfo
         * @param machineName
         * @throws CustomException
         */
        public static ProductRequestPlan getFirstPlanStartByCrateSpecName(String cratespecname)
            throws CustomException
        {
            try
            {
                ProductRequestPlan pPlan = new ProductRequestPlan();

                String condition =    "    PLANRELEASEDTIME = (                                                                   \n"
                        + "                            SELECT MIN(PRP.PLANRELEASEDTIME)                               \n"
                        + "                            FROM PRODUCTREQUEST PR,                                        \n"
                        + "                                 PRODUCTREQUESTPLAN PRP,                                   \n"
                        + "                                 CT_RESERVELOT C                                           \n"
                        + "                        WHERE 1=1                                                          \n"
                        + "                            AND PR.CRATESPECNAME = ?                                       \n"
                        + "                            AND PR.PRODUCTREQUESTNAME = PRP.PRODUCTREQUESTNAME             \n"
                        + "                            AND PR.PRODUCTREQUESTSTATE NOT IN (?,?)                        \n"
                        + "                            AND PR.PRODUCTREQUESTTYPE NOT LIKE ?                           \n"
                        + "                            AND PR.PRODUCTREQUESTHOLDSTATE <> ?                            \n"
                        + "                            AND PR.RELEASEDQUANTITY < PR.PLANQUANTITY                      \n"
                        + "                            AND PR.PLANRELEASEDTIME <= SYSDATE                             \n"
                        + "                            AND PR.PRODUCTREQUESTNAME = C.PRODUCTREQUESTNAME               \n"
                        + "                            AND C.RESERVESTATE = ?                                         \n"
                        + "                            )                                                              \n";
                Object bindSet[] = new Object[]{cratespecname, "Completed", "Finished", "R%", "Y", "Started"};
                List<ProductRequestPlan> pPlanList = ProductRequestPlanServiceProxy.getProductRequestPlanService().select(condition, bindSet);

                if(pPlanList.size() > 1)
                {
                    throw new CustomException("PRODUCTREQUEST-0022", cratespecname);
                }

                pPlan = pPlanList.get(0);

                return pPlan;
            }
            //160603 by swcho : error enhanced
            catch (NotFoundSignal ne)
            {
                throw new CustomException("PRODUCTREQUEST-0053", cratespecname);
            }
            catch (FrameworkErrorSignal fe)
            {
                throw new CustomException("PRODUCTREQUEST-0053", cratespecname);
            }
        }
        
		/**
		 * getStartPlanByMachine
		 * 160503 by Aim System : service object changed
		 * @author Aim System
		 * @since 2016.05.03
		 * @param eventInfo
		 * @param machineName
		 * @throws CustomException
		 */
		public static ProductRequestPlan getStartPlanByMachine(String machineName)
			throws CustomException
		{
			try
			{
				ProductRequestPlan pPlan = new ProductRequestPlan();

				String condition = "assignedMachineName = ? and productRequestState = ? ";
				Object bindSet[] = new Object[]{machineName, "Started"};
				List<ProductRequestPlan> pPlanList = ProductRequestPlanServiceProxy.getProductRequestPlanService().select(condition, bindSet);

				if(pPlanList.size() > 1)
				{
					throw new CustomException("PRODUCTREQUEST-0022", machineName);
				}

				pPlan = pPlanList.get(0);

				return pPlan;
			}
			catch (greenFrameDBErrorSignal ne)
			{
				throw new CustomException("PRODUCTREQUEST-0021", machineName);
			}
		}

		/**
		 * getFactoryNameByMachine
		 * 151105 by xzquan : service object changed
		 * @author xzquan
		 * @since 2015.11.08
		 * @param eventInfo
		 * @param machineName
		 * @throws CustomException
		 */
		public static String getFactoryNameByMachine(String machineName)
			throws CustomException
		{
			try
			{
				MachineSpecKey mSpecKey = new MachineSpecKey(machineName);
				MachineSpec mSpec = MachineServiceProxy.getMachineSpecService().selectByKey(mSpecKey);

				String factoryName = mSpec.getFactoryName();

				return factoryName;
			}
			catch (greenFrameDBErrorSignal ne)
			{
				throw new CustomException("PRODUCTREQUEST-0021", machineName);
			}
		}

		/**
		 * get first operation for any flow
		 * @author swcho
		 * @since 2016.04.06
		 * @param factoryName
		 * @param processFlowName
		 * @return
		 * @throws CustomException
		 */
		public static ProcessOperationSpec getFirstOperation(String factoryName, String processFlowName)
			throws CustomException
		{
			try
			{
				ProcessFlowKey pfKey = new ProcessFlowKey(factoryName, processFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

				String startNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(pfKey).getKey().getNodeId();
				String nodeId = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(startNodeStack, "Normal", "").getKey().getNodeId();

				Node currentNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeId);

				String processOperationName = currentNode.getNodeAttribute1();

				ProcessOperationSpec processOperation = getProcessOperationSpec(factoryName, processOperationName);

				return processOperation;
			}
			catch (CustomException ce)
			{
				throw ce;
			}
			catch (Exception ex)
			{
				throw new CustomException("SYS-9999", "ProcessOperation", ex.getMessage());
			}
		}

		/**
		 * get next operation for any flow
		 * @author swcho
		 * @since 2017.02.09
		 * @param factoryName
		 * @param processFlowName
		 * @param processOperationName
		 * @return
		 * @throws CustomException
		 */
		public static ProcessOperationSpec getNextOperation(String factoryName, String processFlowName, String processOperationName)
			throws CustomException
		{
			try
			{
				String startNodeStack = NodeStack.getNodeID(factoryName, processFlowName, processOperationName);
				String nodeId = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(startNodeStack, "Normal", "").getKey().getNodeId();

				Node currentNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeId);

				String nextOperationName = currentNode.getNodeAttribute1();

				ProcessOperationSpec processOperation = getProcessOperationSpec(factoryName, nextOperationName);

				return processOperation;
			}
			catch (CustomException ce)
			{
				throw ce;
			}
			catch (Exception ex)
			{
				throw new CustomException("SYS-9999", "ProcessOperation", ex.getMessage());
			}
		}
		// start add by jhying on20200327 mantis:5819
		public static ProcessOperationSpec getNextOperationForNotNullNextOperation(String factoryName, String processFlowName, String processOperationName)
				throws CustomException
			{
				try
				{
					String startNodeStack = NodeStack.getNodeID(factoryName, processFlowName, processOperationName);
					String nodeId = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(startNodeStack, "Normal", "").getKey().getNodeId();
					ProcessOperationSpec processOperation = new ProcessOperationSpec();	
					Node currentNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeId);

					String nextOperationName = currentNode.getNodeAttribute1();
					
                    if(!StringUtil.isEmpty(nextOperationName)){
					        processOperation = getProcessOperationSpec(factoryName, nextOperationName);					    
                    }
                    else{
                    	log.warn("nextOperationName is NULL ");
                    }
					 return processOperation;
                    
				}
				catch (CustomException ce)
				{
					throw ce;
				}
				catch (Exception ex)
				{
					throw new CustomException("SYS-9999", "ProcessOperation", ex.getMessage());
				}
			}
		
		// end add by jhying on20200327 mantis:5819

		/**
		 * getUnScrappedProducts
		 * 160425 AIM System
		 * @since 2016.04.25
		 * @param carrierName
		 * @throws CustomException
		 */
		public static List<Product> getUnScrappedProducts(String carrierName)
			throws CustomException
		{
			String condition = "carrierName = ? AND productState != ?" + " AND productState != ? ";

			Object[] bindSet = new Object[] {carrierName, GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed};
			List<Product> productList = new ArrayList<Product>();
			try
			{
				productList = ProductServiceProxy.getProductService().select(condition, bindSet);
			}
			catch(Exception e)
			{
				productList = null;
				throw new CustomException("CST-0011");
			}

			return productList;
		}

		/*
		* Name : Get ZPL Code
		* Author : AIM Systems, Inc
		* Date : 2016.06.18
		*/
		public static String getZplCode(String labelID, String version)
		{
			boolean flag = false;

			String sql = "SELECT L.LABELCODE " +
					"FROM CT_LABEL L " +
					"WHERE 1=1 " +
					"AND L.LABELID = :labelID " +
					"AND L.VERSION = :version ";

			Map<String, String> bindMap = new HashMap<String, String>();

			bindMap.put("labelID", labelID);
			bindMap.put("version", version);

			List<Map<String, Object>> sqlResult
			  = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);


			if(sqlResult.size() < 1)
			{
				log.info("Not Exist LabelCode");
				flag = true;
			}

			return sqlResult.get(0).get("LABELCODE").toString();
		}

		/**
		 * getProductRequestPlanList，按照position的顺序 1 2 3 依次排列
		 * @author 170419 add by lszhen
		 * @param machineName
		 * @return List<ProductRequestPlan>
		 * @throws CustomException
		 */
		public static List<ListOrderedMap> getProductRequestPlanList(String machineName) throws CustomException
		{
			try {
				StringBuffer sqlBuffer =  new StringBuffer().append(" SELECT *                           ").append("\n")
						.append("   FROM (  SELECT A.PLANRELEASEDTIME,C.MULTIPRODUCTSPECTYPE,            ").append("\n")
						.append("                  B.PRODUCTSPECNAME,                                    ").append("\n")
						.append("                  A.POSITION,                                           ").append("\n")
						.append("                  A.PRODUCTREQUESTNAME,                                 ").append("\n")
						.append("                  A.PRODUCTREQUESTHOLDSTATE,                            ").append("\n")
						.append("                  A.PLANQUANTITY,                                       ").append("\n")
						.append("                  A.RELEASEDQUANTITY                                    ").append("\n")
						.append("             FROM PRODUCTREQUESTPLAN A, PRODUCTREQUEST B, PRODUCTSPEC C ").append("\n")
						.append("            WHERE     A.ASSIGNEDMACHINENAME = ?                         ").append("\n")
						.append("                  AND A.PRODUCTREQUESTSTATE IN (?, ?)                   ").append("\n")
						.append("                  AND A.PRODUCTREQUESTNAME = B.PRODUCTREQUESTNAME       ").append("\n")
						.append("                  AND B.PRODUCTSPECNAME = C.PRODUCTSPECNAME             ").append("\n")
						.append("                  AND C.MULTIPRODUCTSPECTYPE = ?                        ").append("\n")
						.append("         ORDER BY A.POSITION)                                           ").append("\n")
						.append("  WHERE ROWNUM = 1                                                      ").append("\n")
						.append(" UNION ALL                                                              ").append("\n")
						.append(" SELECT *                                                               ").append("\n")
						.append("   FROM (  SELECT A.PLANRELEASEDTIME,C.MULTIPRODUCTSPECTYPE,            ").append("\n")
						.append("                  B.PRODUCTSPECNAME,                                    ").append("\n")
						.append("                  A.POSITION,                                           ").append("\n")
						.append("                  A.PRODUCTREQUESTNAME,                                 ").append("\n")
						.append("                  A.PRODUCTREQUESTHOLDSTATE,                            ").append("\n")
						.append("                  A.PLANQUANTITY,                                       ").append("\n")
						.append("                  A.RELEASEDQUANTITY                                    ").append("\n")
						.append("             FROM PRODUCTREQUESTPLAN A, PRODUCTREQUEST B, PRODUCTSPEC C ").append("\n")
						.append("            WHERE     A.ASSIGNEDMACHINENAME = ?                         ").append("\n")
						.append("                  AND A.PRODUCTREQUESTSTATE IN (?, ?)                   ").append("\n")
						.append("                  AND A.PRODUCTREQUESTNAME = B.PRODUCTREQUESTNAME       ").append("\n")
						.append("                  AND B.PRODUCTSPECNAME = C.PRODUCTSPECNAME             ").append("\n")
						.append("                  AND C.MULTIPRODUCTSPECTYPE = ?                        ").append("\n")
						.append("         ORDER BY A.POSITION)                                           ").append("\n")
						.append("  WHERE ROWNUM = 1                                                      ").append("\n");
				List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(),
				new Object[] {machineName, "Released", "Started", "Material", machineName, "Released", "Started", "Production"});
			return result;
			} catch (Exception e) {
				// TODO: 没找到，说明当前无woPlan
			}
			return null;
		}

		/**
		 * CustomExceptionLog
		 * @author 20180409 add by wghuang
		 * @param errorCode
		 * @param args
		 * @throws CustomException
		 */
		public static void CustomExceptionLog(String errorCode, Object... args)throws CustomException
		{
			ErrorDef  errorDef;
			ErrorDef tempErrorDef = GenericServiceProxy.getErrorDefMap().getErrorDef(errorCode);

			if ( tempErrorDef == null )
			{
				//tempErrorDef = GenericServiceProxy.getErrorDefMap().getErrorDef("UndefinedCode");

				tempErrorDef = new ErrorDef();
				tempErrorDef.setErrorCode("UndefinedCode");
				tempErrorDef.setCha_errorMessage("");
				tempErrorDef.setEng_errorMessage("");
				tempErrorDef.setKor_errorMessage("");
				tempErrorDef.setLoc_errorMessage("");
			}

			errorDef = new ErrorDef();
			{
				//initialize object
				errorDef.setErrorCode(tempErrorDef.getErrorCode());
				errorDef.setCha_errorMessage("");
				errorDef.setEng_errorMessage("");
				errorDef.setKor_errorMessage("");
				errorDef.setLoc_errorMessage("");
			}

			String korTempMsg = tempErrorDef.getKor_errorMessage();
			String engTempMsg = tempErrorDef.getEng_errorMessage();
			String chaTempMsg = tempErrorDef.getCha_errorMessage();
			String locTempMsg = tempErrorDef.getLoc_errorMessage();

			errorDef.setKor_errorMessage(
					MessageFormat.format(korTempMsg, args));
			errorDef.setEng_errorMessage(
					MessageFormat.format(engTempMsg, args));
			errorDef.setCha_errorMessage(
					MessageFormat.format(chaTempMsg, args));
			errorDef.setLoc_errorMessage(
					MessageFormat.format(locTempMsg, args));

			if(log.isWarnEnabled())
			{
				log.warn(String.format("[%s]%s", errorDef.getErrorCode(), errorDef.getLoc_errorMessage()));
			}
		}


		/*
		* Name : getEnumDefValueByEnumNameAndDescription
		* Desc : This function is getEnumDefValueByEnumNameAndDescription
		* Author : hsryu
		* Date : 2018.04.08
		*/
		public static List<Map<String, Object>> getEnumDefValueByEnumNameAndDescription( String enumName, String description )
		{
			String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE "
						+ "WHERE ENUMNAME = :enumName "
						+ "AND DESCRIPTION = :description ";

			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("enumName", enumName);
			bindMap.put("description", description);

//			List<Map<String, Object>> sqlResult =
//				greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);

			List<Map<String, Object>> sqlResult =
					GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			return sqlResult;
		}

		/*
		* Name : checkEnumDefValueByEnumValue
		* Desc : This function is checkEnumDefValueByEnumValue
		* Author : hsryu
		* Date : 2018.04.11
		*/
		public static boolean checkEnumDefValueByEnumValue( String enumName , String value ){
			String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE "
						+ "WHERE ENUMNAME = :enumName AND ENUMVALUE = :enumValue ";

			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("enumName", enumName);
			bindMap.put("enumValue", value);

			List<Map<String, Object>> sqlResult =
					GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if(sqlResult.size() > 0)
			{
				return false;
			}

			return true;
		}
		
		// 2018.07.19 add hsryu
		public static String getLastOperation(String factoryName, String processFlowName)
				throws CustomException
		{

			try
			{
				Node endNode = ProcessFlowServiceProxy.getProcessFlowService().getEndNode(new ProcessFlowKey(factoryName, processFlowName, "00001"));

				List<Arc> connectArc = ProcessFlowServiceProxy.getArcService().select("toNodeId = ?", new Object[] {endNode.getKey().getNodeId()});

				Node lastOperationNode = ProcessFlowServiceProxy.getNodeService().getNode(connectArc.get(0).getKey().getFromNodeId());

				String targetFactoryName = lastOperationNode.getFactoryName();
				String targetFlowName = lastOperationNode.getProcessFlowName();
				String targetOperationName = lastOperationNode.getNodeAttribute1();

				return targetOperationName;

			}
			catch (Exception ex)
			{
				throw new CustomException("SYS-9999", "ProcessOperation", ex.getMessage());
			}
		}
		
		//2018.07.19 add hsryu
		public static String getReturnNodeForBranch( Lot lotData )
		{
			String nodeStack = "";
            String returnProcessFlowName = "";
            String returnProcessOperationName = "";
            
            String sql = "SELECT PA.RETURNPROCESSFLOWNAME, PA.RETURNPROCESSOPERATIONNAME " ;
            sql += " FROM TPEFOPOLICY TPEFO,POSALTERPROCESSOPERATION PA, ";
            sql += " (SELECT N.FACTORYNAME, N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION,  ";
            sql += " N.NODEATTRIBUTE1 AS PROCESSOPERATIONNAME, N.NODEATTRIBUTE2 AS PROCESSOPERATIONVERSION ";
            sql += " FROM (SELECT N.NODEID FROMNODEID ";
            sql += " FROM NODE N, LOT L ";
            sql += " WHERE 1 = 1 ";
            sql += " AND L.LOTNAME = :LOTNAME ";
            sql += " AND REGEXP_SUBSTR(L.NODESTACK,:REGPATTERN,1,REGEXP_COUNT(L.NODESTACK, :REGPATTERN)-1) = N.NODEID  ";
            sql += " ) NA, NODE N ";
            sql += " WHERE NA.FROMNODEID = N.NODEID ";
            sql += " ) BEFOREOPER ";
            sql += " WHERE 1=1 ";
            sql += " AND ((TPEFO.FACTORYNAME = BEFOREOPER.FACTORYNAME) OR (TPEFO.FACTORYNAME = :STAR)) ";
            sql += " AND ((TPEFO.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (TPEFO.PRODUCTSPECNAME = :STAR)) ";
            sql += " AND ((TPEFO.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (TPEFO.PRODUCTSPECVERSION = :STAR)) ";
            sql += " AND ((TPEFO.ECCODE = :ECCODE) OR (TPEFO.ECCODE = :STAR)) ";
            sql += " AND ((TPEFO.PROCESSFLOWNAME = BEFOREOPER.PROCESSFLOWNAME) OR (TPEFO.PROCESSFLOWNAME = :STAR)) ";
            sql += " AND ((TPEFO.PROCESSFLOWVERSION = BEFOREOPER.PROCESSFLOWVERSION) OR (TPEFO.PROCESSFLOWVERSION = :STAR)) ";
           sql += " AND ((TPEFO.PROCESSOPERATIONNAME = BEFOREOPER.PROCESSOPERATIONNAME) OR (TPEFO.PROCESSOPERATIONNAME = :STAR)) ";
            sql += " AND ((TPEFO.PROCESSOPERATIONVERSION = BEFOREOPER.PROCESSOPERATIONVERSION) OR (TPEFO.PROCESSOPERATIONVERSION = :STAR)) ";
            sql += " AND PA.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ";
            sql += " AND TPEFO.CONDITIONID = PA.CONDITIONID ";
            sql += " AND UPPER(PA.CONDITIONNAME) = :BRANCH ";
            sql += " ORDER BY DECODE (TPEFO.FACTORYNAME, :STAR, 9999, 0), ";
            sql += " DECODE (TPEFO.PROCESSFLOWNAME, :STAR, 9999, 0), ";
            sql += " DECODE (TPEFO.PROCESSFLOWVERSION, :STAR, 9999, 0), ";
            sql += " DECODE (TPEFO.PROCESSOPERATIONNAME, :STAR, 9999, 0), ";
            sql += " DECODE (TPEFO.PROCESSOPERATIONVERSION, :STAR, 9999, 0) ";

            Map<String, String> bindMap = new HashMap<String, String>();
            bindMap.put("LOTNAME", lotData.getKey().getLotName());
            bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
            bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
            bindMap.put("ECCODE", lotData.getUdfs().get("ECCODE"));
            bindMap.put("TOPROCESSFLOWNAME", lotData.getProcessFlowName());
            bindMap.put("REGPATTERN", "[^.]+");
            bindMap.put("STAR", "*");
            bindMap.put("BRANCH", "BRANCH");

            List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

            if( sqlResult.size() == 1 ){
                   
                   returnProcessFlowName = sqlResult.get(0).get("RETURNPROCESSFLOWNAME").toString();
                   returnProcessOperationName = sqlResult.get(0).get("RETURNPROCESSOPERATIONNAME").toString();
                   
                   nodeStack = CommonUtil.getNodeStack(lotData.getFactoryName(),returnProcessFlowName, returnProcessOperationName);
                   
                   return nodeStack;

            } else {
                   nodeStack = "";
            }
            return nodeStack;
		}
		
		/**
		 * 
		 * @Name     setMaskPositionUpdate
		 * @since    2018. 8. 13.
		 * @author   Admin
		 * @contents 
		 *           
		 * @param durableName
		 * @throws CustomException
		 */
		public static void setMaskPositionUpdate(String durableName) throws CustomException 
	    {
	        try
	        {
	            StringBuilder sql = new StringBuilder();
	            sql.setLength(0);
	            sql.append("UPDATE DURABLE A                                                    ");
	            sql.append("     SET  A.MASKPOSITION = NULL WHERE A.DURABLENAME = :DURABLENAME  ");
	            Map<String, Object> bindMap = new HashMap<String, Object>();
	            bindMap.put("DURABLENAME", durableName);
	            int rows = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().update(sql.toString(), bindMap);
	        }
	        catch(Exception ex)
	        {
	            
	        }
	    }
		

		/*
		 * Name : getWorkOrderType
		 * Desc : This function is getWorkOrderType By ProductData.
		 * Author : hsryu
		 * Date : 2019.02.25
		 */
		public static String getWorkOrderType(Lot lotData) throws CustomException
		{
			String woType = "";

			try{
				List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());

				for(Product productData : pProductList){
					if(StringUtils.isNotEmpty(productData.getProductRequestName())){
						ProductRequest pData = CommonUtil.getProductRequestData(productData.getProductRequestName());    
						woType = pData.getProductRequestType();
						break;
					}
				}
			}
			catch(Throwable e){
				log.warn("getWorkOrderType is Error!");
			}
			return woType;
		}
		
		/*
		 * Name : getWorkOrderType
		 * Desc : This function is getWorkOrderType By ProductData.
		 * Author : hsryu
		 * Date : 2019.02.25
		 */
		public static String getWorkOrderTypeByProductData(Product productData) throws CustomException
		{
			String woType = "";

			try{
				if(StringUtils.isNotEmpty(productData.getProductRequestName())){
					ProductRequest pData = CommonUtil.getProductRequestData(productData.getProductRequestName());    
					woType = pData.getProductRequestType();
				}
			}
			catch(Throwable e){
				log.warn("getWorkOrderType is Error!");
			}
			return woType;
		}
		
		public static List<Map<String, Object>> getNextOperationList(String factoryName,String processFlowName,String processOperationName){
			String sql = "SELECT LEVEL LV,FACTORYNAME,PROCESSOPERATIONNAME,PROCESSFLOWNAME,PROCESSFLOWVERSION,NODEID,NODETYPE,FROMNODEID,TONODEID " +
					" FROM (SELECT N.FACTORYNAME,N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,N.PROCESSFLOWNAME,N.PROCESSFLOWVERSION,N.NODEID,N.NODETYPE,A.FROMNODEID,A.TONODEID " +
					" FROM ARC A, " +
					" NODE N, " +
					" PROCESSFLOW PF " +
					" WHERE 1 = 1 " +
					" AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
					" AND N.FACTORYNAME = :FACTORYNAME " +
					" AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME " +
					" AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION " +
					" AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME " +
					" AND N.FACTORYNAME = PF.FACTORYNAME " +
					" AND A.FROMNODEID = N.NODEID) " +
					" START WITH PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
					" CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID " ;
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PROCESSFLOWNAME", processFlowName);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			return sqlResult;

		}
		
		// 2019.06.03_hsryu_Add Logic. getLastNodeID.
		public static String getLastNode(String factoryName, String processFlowName)
				throws CustomException
		{

			try
			{
				Node endNode = ProcessFlowServiceProxy.getProcessFlowService().getEndNode(new ProcessFlowKey(factoryName, processFlowName, "00001"));
				List<Arc> connectArc = ProcessFlowServiceProxy.getArcService().select("toNodeId = ?", new Object[] {endNode.getKey().getNodeId()});
				Node lastOperationNode = ProcessFlowServiceProxy.getNodeService().getNode(connectArc.get(0).getKey().getFromNodeId());

				return lastOperationNode.getKey().getNodeId();

			}
			catch (Exception ex)
			{
				throw new CustomException("SYS-9999", "ProcessOperation", ex.getMessage());
			}
		}
		
		public static Map<String, String> getNodeInfo(String nodeId)
		{
			Map<String, String> flowMap = new HashMap<String, String>();
			try {
				
				String checkSql = " SELECT N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION, N.NODEATTRIBUTE1, N.NODEATTRIBUTE2, N.NODEID FROM NODE N  WHERE N.NODEID = :NODEID ";

				Map<String, Object> bindSet = new HashMap<String, Object>(); 
				bindSet.put("NODEID", nodeId);

				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, bindSet);
				if ( sqlResult!=null && sqlResult.size() > 0 )
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
			} catch (Exception e) {
				log.info("queryForList Error");
			}
			return flowMap;

		}
		public static String generateTransportJobId(String carrierName, String sender)
		{
	 		String transportDate = TimeStampUtil.getCurrentEventTimeKey();
			return (transportDate + "-" + carrierName + "-" + sender);
		}
		public static boolean isNullOrEmpty(List<?> list) {
			if (list == null || list.size() <= 0)
				return true;

			return false;
		}
		public static Map ConverObjectToMap(Object obj)
		{ 
			try 
			{ //Field[] fields = obj.getClass().getFields(); //private field绰 唱坷瘤 臼澜. 
				Field[] fields = obj.getClass().getDeclaredFields(); 
				Map resultMap = new HashMap(); 
				for(int i=0; i<=fields.length-1;i++)
				{ 
					fields[i].setAccessible(true);
					resultMap.put(fields[i].getName(), fields[i].get(obj)); 
				} 
				return resultMap; 
			} 
			catch (IllegalArgumentException e) 
			{ e.printStackTrace(); }
			catch (IllegalAccessException e) 
			{ e.printStackTrace(); }
			return null; 
	
		}
		
		public static String getGlassSize(String productSpecName)
		{
			String size = StringUtils.EMPTY;
			
			if(!StringUtils.isEmpty(productSpecName) && productSpecName.length() > 5){
				size = productSpecName.substring(1,4);
			}
			
			return size;
		}	
		
		public static ProcessOperationSpec getProcessOperationSpec(Lot lotData) throws CustomException{
			
			ProcessOperationSpec processOperationData = new ProcessOperationSpec();
			
			try{
				ProcessOperationSpecKey processOperationKey = new ProcessOperationSpecKey();

				processOperationKey.setFactoryName(lotData.getFactoryName());
				processOperationKey.setProcessOperationName(lotData.getProcessOperationName());
				processOperationKey.setProcessOperationVersion("00001");

				processOperationData = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(processOperationKey);

			} catch( Exception e ){
				throw new CustomException("PROCESSOPERATION-9001", lotData.getProcessOperationName());
			}

			return processOperationData;
		}
		
		/*
		 * Name : getBoxListByPalletID 
		 * Desc : This function is getBoxListByPalletID 
		 * Author : AIM Systems, Inc 
		 * Date : 2019.08.21
		 */
		public static List<Lot> getLotListByPalletID(String palletID) 
		{
			List<Lot> LotDataList = null;
			
			try
			{
				LotDataList = LotServiceProxy.getLotService().allLotsByProcessGroup( palletID );
			}
			catch (NotFoundSignal nfs)
			{
				log.warn(nfs);
				
				LotDataList = new ArrayList<Lot>();
			}

			return LotDataList;
		}
		
		/*
		 * Name : getNodeStackInfo Desc : This function is getProcessOperationNameByDetailProcessOperationType 
		 * Author :
		 * AIM Systems, Inc Date : 2019.06.19
		 */
		public static String getProcessOperationNameByDetailProcessOperationType(String factoryName, String processFlowName, String detailProcessOperationType)
		{
			try{
				StringBuffer sbSql = new StringBuffer();
				sbSql.append(" SELECT PFS.PROCESSOPERATIONNAME " );
				sbSql.append("   FROM PROCESSOPERATIONSPEC POS " );
				sbSql.append("        JOIN V_PROCESSFLOWSEQ PFS " );
				sbSql.append("           ON (POS.PROCESSOPERATIONNAME = PFS.PROCESSOPERATIONNAME) " );
				sbSql.append("  WHERE POS.FACTORYNAME = :FACTORYNAME " );
				sbSql.append("    AND PFS.PROCESSFLOWNAME = :PROCESSFLOWNAME " );
				sbSql.append("    AND POS.DETAILPROCESSOPERATIONTYPE = :DETAILPROCESSOPERATIONTYPE " );
				
				Map<String,String> mBind = new HashMap<String,String>();
				mBind.put("FACTORYNAME",factoryName);
				mBind.put("PROCESSFLOWNAME",processFlowName);
				mBind.put("DETAILPROCESSOPERATIONTYPE",detailProcessOperationType);
				
				List<Map<String,Object>> result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sbSql.toString(), mBind);
				
				return  result.get(0).get("PROCESSOPERATIONNAME").toString();
				
			}catch(Exception e){
				return null;
			}
		}
		
		/*
		 * Name : getProcessGroupByProcesessGroupName Desc : This function is
		 * getProcessGroupByProcesessGroupName Author : AIM Systems, Inc Date :
		 * 2013.07.27
		 */
		public static ProcessGroup getProcessGroupByProcesessGroupName(
				String processGroupName) throws CustomException {
			if (StringUtils.isEmpty(processGroupName)) {
				throw new CustomException("PROCESSGROUP-1001", processGroupName);
			}

			try {
				ProcessGroupKey processGroupKey = new ProcessGroupKey();
				processGroupKey.setProcessGroupName(processGroupName);

				ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(processGroupKey);

				return processGroupData;

			} catch (Exception e) {
				throw new CustomException("PROCESSGROUP-1001", processGroupName);
			}
		}
		
		/*
		 * Name : getBeforeProcessOperation 
		 * Desc : This function is getBeforeProcessOperation 
		 * Author : hsryu 
		 * IncDate : 2020.10.30
		 */
		public static String getBeforeProcessOperation(Lot lotData) throws CustomException 
		{
			StringBuffer queryBuffer = new StringBuffer();
			queryBuffer.append("	SELECT N2.NODEATTRIBUTE1 BEFOREOPERATIONNAME"); 
			queryBuffer.append("	FROM NODE N,"); 
			queryBuffer.append("	  ARC a,"); 
			queryBuffer.append("	  NODE N2"); 
			queryBuffer.append("	WHERE a.TONODEID= N.NODEID"); 
			queryBuffer.append("	AND N.NODEID    ="); 
			queryBuffer.append("	  (SELECT REGEXP_SUBSTR(L.NODESTACK,'[^.]+',1,REGEXP_COUNT(L.NODESTACK, '[^.]+'))"); 
			queryBuffer.append("	  FROM LOT L"); 
			queryBuffer.append("	  WHERE LOTNAME=:LOTNAME"); 
			queryBuffer.append("	  )"); 
			queryBuffer.append("	AND N2.NODEID = a.FROMNODEID"); 
			queryBuffer.append("	AND N.PROCESSFLOWNAME = N2.PROCESSFLOWNAME");     

			HashMap<String, Object> bindMap = new HashMap<String, Object>();

			bindMap.put("LOTNAME", lotData.getKey().getLotName());

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

			if(sqlResult != null && sqlResult.size() > 0)
			{
				
				if(!StringUtil.isEmpty((String)sqlResult.get(0).get("BEFOREOPERATIONNAME")))
				{
					return (String)sqlResult.get(0).get("BEFOREOPERATIONNAME");
				}
				else 
				{
					throw new CustomException("PROCESSOPERATION-0005", lotData.getProcessOperationName(), "ForceSampling");
				}
			}
			
			throw new CustomException("PROCESSOPERATION-0005", lotData.getProcessOperationName(), "ForceSampling");
		}

}