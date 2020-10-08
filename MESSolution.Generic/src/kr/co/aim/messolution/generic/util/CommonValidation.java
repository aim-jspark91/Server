package kr.co.aim.messolution.generic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

//import com.sun.xml.internal.ws.wsdl.writer.document.Port;

public class CommonValidation implements ApplicationContextAware
{
	/**
	 * @uml.property name="applicationContext"
	 * @uml.associationEnd
	 */
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(CommonValidation.class);



	@Override
    public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	//from StateCheckUtil.java
	
	/*
	* Name : differentCheck
	* Desc : This function is differentCheck
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	public static <T> int differentCheck(List<T> list){
		
		T oldValue = list.get(0);
		int differentPoint = 0;
		
		for(T value : list){
			
			if(!oldValue.equals(null) && !oldValue.equals(value)){
				return differentPoint;
			}
			oldValue = value;
			differentPoint++;
		}
		
		return 0;
	}
	
	/*
	* Name : differentCheck
	* Desc : This function is differentCheck
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	public static <T> boolean differentCheck(T oldvalue, T newvalue){
		
		if(!oldvalue.equals(null) && !oldvalue.equals(newvalue)){
			return false;
		}else{
			return true;
		}
		
	}

	/*
	* Name : AccordCheck
	* Desc : This function is AccordCheck
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	public static <T> int AccordCheck(List<T> list, T arcordValue){
		
		int differentPoint = 0;
		
		for(T value : list){
			
			if(!arcordValue.equals(value)){
				return differentPoint;
			}
			differentPoint++;
		}
		
		return -1;
	}
	
	/*
	* Name : checkNotNull
	* Desc : This function is checkNotNull
	* Author : AIM Systems, Inc
	* Date : 2011.03.08
	*/
	public static void checkNotNull( String aItem, String aValue ) throws CustomException{
		if( StringUtils.isEmpty(aValue)){
			throw new CustomException("COM-9000", aItem, aValue);
		}
	}
	
	/*
	* Name : checkStateModelEvent
	* Desc : This function is checkStateModelEvent
	* Author : AIM Systems, Inc 
	* Date : 2011.09.21
	*/
	public static void checkStateModelEvent(String type, String machineName, String portName, String oldState, String newState) throws CustomException
	{
		String stateModel = "";
		String sql = "";
		String eventName = "";
		
		boolean isDifferent = false;

		Map<String, String> bindMap = new HashMap<String, String>();
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		
		if(StringUtils.equals(type, "MACHINE")){
			MachineSpec machineSpec = CommonUtil.getMachineSpecByMachineName(machineName);
			stateModel = machineSpec.getMachineStateModelName();
			
			if(oldState.equals(newState))
			{
				isDifferent = true;
			}
		}else if(StringUtils.equals(type, "PORT")){
			PortSpec portSpec = CommonUtil.getPortSpecInfo(machineName, portName);
			stateModel = portSpec.getPortStateModelName();
		}
		
		if(!isDifferent){
			sql = "SELECT EVENTNAME FROM STATEMODELEVENT WHERE STATEMODELNAME = :stateModel AND EVENTNAME = :eventName";
			eventName = oldState + "-" + newState;
			
			bindMap.put("stateModel", stateModel);
			bindMap.put("eventName" , eventName );
			
//			sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql,bindMap);
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if(!(sqlResult.size() > 0)){
				throw new CustomException("STATE-0002", newState, oldState, newState);
	/*			if(StringUtils.equals(type, "MACHINE")){
					log.info("checkStateModelEvent : no exist MachineStateChange Rule ["+ eventName+"]" );
				}else if(StringUtils.equals(type, "PORT")){
					log.info("checkStateModelEvent : no exist PortState Change Rule ["+ eventName+"]" );
				}*/
			}
		}
	}
	/*
	* Name : checkNamingRuleArgumentsCount
	* Desc : This function is checkNamingRuleArgumentsCount
	* Author : AIM Systems, Inc
	* Date : 2011.08.29
	*/
	public static void checkNamingRuleArgumentsCount(List<Map<String, Object>> sqlResult, String ruleName) throws CustomException
	{
		int enumValueCount = 0;
		for (int i = 0; i < sqlResult.size(); i++) {
			if(!StringUtils.isEmpty((String)sqlResult.get(i).get("ENUMVALUE"))) enumValueCount = enumValueCount +1;
		}
		if(sqlResult.size() != enumValueCount)
			throw new CustomException("NAMING-9000", enumValueCount, ruleName, sqlResult.size());
	}
	
	/*
	* Name : checkLotState
	* Desc : This function is checkLotState
	* Author : AIM Systems, Inc
	* Date : 2011.03.08
	*/
	public static void checkLotState( Lot lotData ) throws CustomException{
		if ( !(lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Released)) )
		{
			throw new CustomException("LOT-0016", lotData.getKey().getLotName(), lotData.getLotState()); 
		}
	} 
	
	/*
	* Name : checkStateName
	* Desc : This function is check Exist StateName
	* Author : AIM Systems, Inc
	* Date : 2012.01.04
	*/
	public static void checkStateName( String stateModelName, String stateName ) throws CustomException{
		
		String sql = "SELECT STATENAME FROM STATEMODELSTATE WHERE STATEMODELNAME = :stateModelName AND STATENAME = :stateName";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("stateModelName", stateModelName);
		bindMap.put("stateName", stateName);
		
//		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if(sqlResult.size() == 0){
			throw new CustomException("STATE-0001", stateName);
		}
	}
	
	/*
	* Name : checkEmptyCst
	* Desc : This function is checkEmptyCst
	* Author : AIM Systems, Inc
	* Date : 2011.01.28
	*/
	public static void checkEmptyCst( String carrierName ) throws CustomException
	{
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(carrierName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			  
		if(durableData.getDurableState().equals("InUse"))
			throw new CustomException("CST-0006", carrierName);
		
	}
	
	/*
	* Name : checkEmptyCst
	* Desc : This function is checkEmptyCst
	* Author : AIM Systems, Inc
	* Date : 2011.01.28
	*/
	public static void checkEmptyBox( String boxName ) throws CustomException
	{
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(boxName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			  
		if(durableData.getDurableState().equals("InUse"))
			throw new CustomException("CST-0024", boxName);
		
	}
	
	/*
	* Name : checkMachineRecipe
	* Desc : This function is checkMachineRecipe
	* Author : AIM Systems, Inc
	* Date : 2011.03.08
	*/
	public static String checkMachineRecipe( Lot lotData, Machine machineData ) throws CustomException{
		
		List<Map<String, Object>> sqlResult = CommonUtil.getMachineRecipeByTPFO(lotData.getKey().getLotName(), machineData.getKey().getMachineName());
		
		if ( sqlResult.size() > 0 ) 
		{
			return sqlResult.get(0).get("MACHINERECIPENAME").toString();
		}
				
		throw new CustomException("MACHINE-9002"); 
	}
	
	/*
	* Name : checkLotProcessState
	* Desc : This function is checkLotProcessState
	* Author : AIM Systems, Inc
	* Date : 2011.03.08
	*/
	public static void checkLotProcessState( Lot lotData ) throws CustomException{
	    /* 20181015, hhlee, modify LotprocessState : 'WAIT','RUN' ==>> */
		//if ( !(lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_LoggedOut)) )
	    if ( !(lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_Wait)) )
		{
			//throw new CustomException("LOT-9003", lotData.getKey().getLotName() +". Current State is " + lotData.getLotProcessState()); 
	        throw new CustomException("LOT-9046", lotData.getKey().getLotName() , lotData.getLotProcessState()); 
		}
	    /* <<== 20181015, hhlee, modify LotprocessState : 'WAIT','RUN' */
	}
	
	/*
	* Name : checkLotProcessState
	* Desc : This function is checkLotProcessState
	* Author : zhongsl
	* Date : 2017.05.16
	*/
	public static boolean checkLotProcessState( String durableName,String machineName) throws CustomException{
		boolean checkFlag = false;
		Lot lotData = CommonUtil.getLotInfoBydurableName(durableName);
		if ( lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_LoggedOut))
		{
			checkFlag = true;
		}
		return checkFlag;
	}
		
	/*
	* Name : checkCancelAvail
	* Desc : This function is check for undo condition by event
	* Author : swcho
	* Date : 2011.06.11
	*/
	public static void checkCancelAvailEvent(String lastEventName, String availEventName)
		throws CustomException
	{
		if (!lastEventName.equals(availEventName))
		{
			throw new CustomException("SYS-0000", "Invalid condition to do cancel");				
		}
	}
	
	/*
	* Name : CheckDurableHoldState
	* Desc : This function is CheckDurableHoldState
	* Author : AIM Systems, Inc
	* Date : 2011.04.30
	*/
	public static void CheckDurableHoldState(Durable durableData) throws CustomException {
		String durableHoldState = durableData.getUdfs().get("DURABLEHOLDSTATE").toString();
		if (durableHoldState.equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y)) {
			throw new CustomException("CST-0005", durableData.getKey().getDurableName().toString());
		}
	}
	
	/*
	* Name : CheckDurScrapped
	* Desc : This function checks 'Scrapped' durableState  
	* Author : jhlee
	* Date : 2016.05.02
	*/
	public static void CheckDurScrapped(Durable durableData) throws CustomException 
	{	
		String durableState = durableData.getDurableState();
		
		if (durableState.equals(GenericServiceProxy.getConstantMap().Dur_Scrapped)) 
		{
			throw new CustomException("CST-0007", durableData.getKey().getDurableName().toString());
		}
	}
	
	/*
	* Name : checkEmptyCST
	* Desc : This function is checkEmptyCST
	* Author : AIM Systems, Inc
	* Date : 2011.07.12
	*/
	public static void checkEmptyCST(String carrierName,
									 String portType) throws CustomException
	{
		// Get DurableData & PortData
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(carrierName);

		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
				
		// Get valuable
		String durableState = durableData.getDurableState();
		
		//Check DurableState
		if(StringUtils.equals(portType, GenericServiceProxy.getConstantMap().PORT_TYPE_PL) || 
		   StringUtils.equals(portType, GenericServiceProxy.getConstantMap().PORT_TYPE_PB)) 
		{
			if(StringUtils.equals(durableState, GenericServiceProxy.getConstantMap().Dur_Available))
			{
				throw new CustomException("DURABLE-9002", carrierName);
			}
		}
	}
	 
	
	/*
	* Name : checkAlreadyLotProcessStateTrackIn
	* Desc : This function is checkAlreadyLotProcessStateTrackIn
	* Author : JHYEOM
	* Date : 2014.04.29
	*/
	public static void checkAlreadyLotProcessStateTrackIn(Lot lotData) throws CustomException{
		if(lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_LoggedIn))
		{
			throw new CustomException("LOT-9003",lotData.getKey().getLotName() +". Current State is " + lotData.getLotProcessState());
		}	
	}
	 
	/*
	* Name : checkExistCarrier
	* Desc : This function is checkExistCarrier
	* Author : AIM Systems, Inc
	* Date : 2011.02.18
	*/
	public static Durable checkExistCarrier( String carrierName ) throws CustomException{

		try{
			DurableKey durableKey = new DurableKey();
			durableKey.setDurableName(carrierName);
			Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			
			return durableData;
		} catch ( Exception e ){
			throw new CustomException("CARRIER-9000", carrierName);
		}
	}	
	
	/*
	* Name : checkExistMachine
	* Desc : This function is checkExistMachine
	* Author : AIM Systems, Inc
	* Date : 2011.01.28
	*/
	public static void checkExistMachine( String machineName ) throws CustomException{

		try{
			MachineKey machineKey = new MachineKey();
			machineKey.setMachineName(machineName);
			Machine machineData = MachineServiceProxy.getMachineService().selectByKey(machineKey);
		} catch ( Exception e ){
			throw new CustomException("MACHINE-9000", machineName);
		}
	}
	
	/*
	* Name : checkExistMachine
	* Desc : This function is checkExistMachine
	* Author : AIM Systems, Inc
	* Date : 2011.01.28
	*/
	public static void checkConsumedProduct( Product productData, String productName ) throws CustomException{

		if(productData.getProductState().equals(GenericServiceProxy.getConstantMap().Prod_Consumed)) {
			throw new CustomException("PRODUCT-9006", productName, productData.getProductState());
		}
	}
	
	/*
	* Name : checkExistPort
	* Desc : This function is checkExistPort
	* Author : AIM Systems, Inc
	* Date : 2011.02.18
	*/
	public static void checkExistPort( String machineName, String portName ) throws CustomException{

		try{
			PortKey portKey = new PortKey();
			portKey.setMachineName(machineName);
			portKey.setPortName(portName);
			kr.co.aim.greentrack.port.management.data.Port PortData = PortServiceProxy.getPortService().selectByKey(portKey);
		} catch ( Exception e ){
			throw new CustomException("PORT-9000", portName);
		}
	} 
	
	/*
	* Name : checkLotShippedState
	* Desc : This function is checkLotShippedState
	* Author : AIM Systems, Inc
	* Date : 2011.09.21
	*/
	public static void checkLotShippedState( Lot lotData ) throws CustomException{
		if ( lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Shipped) )
		{
			throw new CustomException("LOT-9020", lotData.getKey().getLotName(), lotData.getLotState()); 
		}
	}
	
	public static void checkLotCompletedState( Lot lotData ) throws CustomException{
		if ( !lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Completed) )
		{
			throw new CustomException("LOT-0228", lotData.getKey().getLotName()); 
		}
	}
	public static void checkProcessFlowTypeIsSort(Lot lotData) throws CustomException{
		ProcessFlow processFlow=null;
		try {
			ProcessFlowKey keyInfo = new ProcessFlowKey(lotData.getFactoryName(),lotData.getProcessFlowName(),lotData.getProcessFlowVersion());
			processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(keyInfo);
		} catch (Exception e) {
			return;
		}

		if(StringUtils.equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SORT, processFlow.getProcessFlowType())){
			throw new CustomException("FLOW-0002", lotData.getProcessFlowName()); 
		}
	}
	
	/*
	* Name : checkTFTLotCFLot
	* Desc : This function is check TFTLot CFLot
	* Author : AIM Systems, Inc
	* Date : 2012.05.08
	*/
	public static void checkTFTLotCFLot( String TFTLot, String CFLot ) throws CustomException{

		try{ 
			if(TFTLot.equals(CFLot))
			{
				throw new CustomException("Lot-0054", TFTLot,CFLot);
			}
		} catch ( Exception e ){
			throw new CustomException("Exception-0001", e);
		}
	}
	
	/*
	* Name : checkLotHoldState
	* Desc : This function is checkLotHoldState
	* Author : AIM Systems, Inc
	* Date : 2011.03.08
	*/ 
	public static void checkLotHoldState( Lot lotData ) throws CustomException{
		if ( lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().FLAG_Y) )
		{
			throw new CustomException("LOT-9015", lotData.getKey().getLotName(), lotData.getLotHoldState()); 
		}
	}
	
	/*
	 * NAME : checkExistLotName
	 * Desc : This function is checkExistLotName
	 * Author : jhlee.AIM System
	 * Date : 2016.02.17
	 */
	public static Lot checkExistLotName(String lotName) throws CustomException{
		
		try{
			LotKey lotKey = new LotKey();
			lotKey.setLotName(lotName);
			
			Lot lot = LotServiceProxy.getLotService().selectByKey(lotKey);
			
			return lot;
		}
		catch(Exception e)
		{
			throw new CustomException("LOT-0071", lotName);
		}
	}
	
	/*
	 * NAME : checkLotIdLength
	 * Desc : This function checks legnth of Lot Name
	 * Author : jhlee.AIM System
	 * Date : 2016.02.17
	 */
	public static void checkLotIdLength(String lotName) throws CustomException{
		if(lotName.length() != 14 )
			throw new CustomException("LOT-0000", lotName);
	}

	/*
	* Name : checkLotReworkState
	* Desc : This function is checkLotReworkState
	* Author : jhlee.AIM System
	* Date : 2016.02.18
	*/
	public static void checkLotReworkState( Lot lotData ) throws CustomException{
		if( !StringUtils.equals(lotData.getReworkState(), GenericServiceProxy.getConstantMap().Lot_InRework)){
			throw new CustomException("LOT-0000", lotData.getReworkState());
		}
	}
	
	/*
	* Name : checkProcessFlowName
	* Desc : This function is checkProcessFlowName
	* Author : jhlee.AIM System 
	* Date : 2016.02.22
	*/
	public static void checkProcessFlowName( Lot lotData, String processFlowName ) throws CustomException{
		if(StringUtils.equals(lotData.getProcessFlowName(), processFlowName))
		{
			throw new CustomException("LOT-9014", lotData.getKey().getLotName());
		}
	}
	
	/*
	* Name : checkProcessFlow
	* Desc : This function is checkProcessFlow
	* Author : AIM.jhlee
	* Date : 2016.02.23
	*/
	public static void checkProcessFlow( Lot lotData, String processFlowName ) throws CustomException{
		if( !StringUtils.equals(lotData.getProcessFlowName(), processFlowName )){
			String processFlowDescription = "";
			try{
				
				ProcessFlowKey processFlowKey = new ProcessFlowKey();
				
				processFlowKey.setFactoryName(lotData.getFactoryName());
				processFlowKey.setProcessFlowName(processFlowName);
				processFlowKey.setProcessFlowVersion("00001");
				
				ProcessFlow processFlowData 
					= ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

				processFlowDescription = processFlowData.getDescription();
			} catch( Exception e ){
				throw new CustomException("PROCESSFLOW-9000", processFlowName);
			}
			
			throw new CustomException("LOT-9008", processFlowDescription);
		}
	}
	
	/*
	 * NAME : checkExistPanelName
	 * Desc : This function is checkExistPanelName
	 * Author : AIM Systems, Inc of muncher 
	 * Date : 2013.06.25
	 */
	public static void checkExistPanelName(String productName) throws CustomException{
		
		ProductKey keyInfo = new ProductKey();
		keyInfo.setProductName(productName);
		
		Product product = new Product();
		try{
			product = ProductServiceProxy.getProductService().selectByKey(keyInfo);
		}catch(Exception e){
			throw new CustomException("PANEL-9001", product.getKey().getProductName());
		}
	}
	 
	/*
	* Name : checkExistProductName
	* Desc : This function is checkExistProductName
	* Author : AIM Systems, Inc
	* Date : 2011.01.28
	*/
	public static Product checkExistProductName(String productName) throws CustomException
	{
		try {
			ProductKey productKey = new ProductKey();
			productKey.setProductName(productName);
			
			Product productData = null;
			productData = ProductServiceProxy.getProductService().selectByKey(productKey);
			
			return productData;
		} catch (Exception e) {		
			throw new CustomException("PRODUCT-9000", productName);	
		}
	}
	
	/*
	 * Name : checkPalletExist
	 * Desc : This function is checkPalletExist
	 * Author : AIM Systems, Inc
	 * Date : 2011.01.28
	 */
	public static ProcessGroup checkExistPallet( String aPalletId ) throws CustomException{
		try{
			ProcessGroupKey processGroupKey = new ProcessGroupKey();
			processGroupKey.setProcessGroupName( aPalletId );

			ProcessGroup boxData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(processGroupKey);

			return boxData;
		}
		catch (Exception e){
			throw new CustomException("PALLET-9000", aPalletId);
		}
	}
	
	/*
	 * NAME : checkExistPanelName
	 * Desc : This function is checkExistPanelName
	 * Author : AIM Systems, Inc of muncher 
	 * Date : 2013.06.25
	 */ 
	public static void checkExistSheetName(String productName) throws CustomException{
		
		ProductKey keyInfo = new ProductKey();
		keyInfo.setProductName(productName);
		
		Product product = new Product();
		try{
			product = ProductServiceProxy.getProductService().selectByKey(keyInfo);
		}catch(Exception e){
			throw new CustomException("PANEL-9001", product.getKey().getProductName());
		}
	}
	 
	public static void checkProductListAndCount(String productList, int listCount, int productQuantity) throws CustomException
	{
		if(listCount == 0) {
			throw new CustomException("PRODUCT-9002", "");
		}else if(listCount !=  productQuantity) {
			throw new CustomException("PRODUCT-9002", "Reported: " + Integer.toString(listCount) + ", ExistOnMES: " + Integer.toString(productQuantity) + ". List: (" + productList + ")");
		}else {
			String sql = "SELECT PRODUCTNAME FROM PRODUCT WHERE PRODUCTNAME IN ( " + productList + " ) ";
			Map<String, String> bindMap = new HashMap<String, String>();
			
//			List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			if(sqlResult.size() > 0) {
				throw new CustomException("PRODUCT-9012", productList);
			}
		}
	}
	
	//-- COMMENT
	//-- 2016.02.16
	//-- checkExistProductSpec
	public static ProductSpec checkExistProductSpec( String productSpecName ) throws CustomException{

		try{
			ProductSpecKey productSpecKey = new ProductSpecKey();
			productSpecKey.setFactoryName("MODULE");
			productSpecKey.setProductSpecName(productSpecName);
			productSpecKey.setProductSpecVersion("00001");
			
			ProductSpec productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);
			
			return productSpecData;
		} catch ( Exception e ){
			throw new CustomException("PRODUCTSPEC-9000", productSpecName);
		}
	}
	
	
	/*
	* Name : checkExistProductSpec
	* Desc : This function is checkExistProductName
	* Author : AIM - jhlee
	* Date : 2016.05.28
	*/
	public static ProductSpec checkExistProductSpec( String productSpecName, String factoryName ) throws CustomException{

		try{
			ProductSpecKey productSpecKey = new ProductSpecKey();
			productSpecKey.setFactoryName(factoryName);
			productSpecKey.setProductSpecName(productSpecName);
			productSpecKey.setProductSpecVersion("00001");
			
			ProductSpec productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);
			
			return productSpecData;
		} catch ( Exception e ){
			throw new CustomException("PRODUCTSPEC-9000", productSpecName);
		}
	}
	
	//-- COMMENT
	//-- 2011.01.28 // 2016.02.18 LEE HYEON WOO
	//-- checkPanelIdLength
	public static void checkPanelIdLength( String aPanelId ) throws CustomException{
		if( aPanelId.length() != 14 ){
			throw new CustomException("COM-9001", aPanelId);
		}
	}
	
	//-- COMMENT
	//-- 2011.01.28 // 2016.02.20 LEE HYEON WOO
	//-- checkBoxIdLength
	public static void checkBoxIdLength( String aBoxId ) throws CustomException{
		if( aBoxId.length() != 13){
			throw new CustomException("BOX-9003");
		}
	}
	
	//-- COMMENT
	//-- 2011.01.28 // 2016.02.20 LEE HYEON WOO
	//-- checkBoxExist
	public static ProcessGroup checkBoxExist( String aBoxId ) throws CustomException{
		try{
			ProcessGroupKey processGroupKey = new ProcessGroupKey();		
			processGroupKey.setProcessGroupName( aBoxId );
			
			ProcessGroup boxData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(processGroupKey);
			
			return boxData;
		} catch (Exception e){
			throw new CustomException("BOX-9000", aBoxId);	
		}
	}
	
	//-- COMMENT
	//-- 2011.01.28 // 2016.02.22 LEE HYEON WOO
	//-- checkBoxNotExist
	public static void checkBoxNotExist( String aBoxId ) throws CustomException{
		try{
			ProcessGroupKey processGroupKey = new ProcessGroupKey();		
			processGroupKey.setProcessGroupName( aBoxId );
			
			ProcessGroup boxData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(processGroupKey);
			
			throw new CustomException("BOX-9001", aBoxId);	
		} catch (Exception e){
			
		}
	}
	
	//-- COMMENT
	//-- 2011.01.28 // 2016.02.20 LEE HYEON WOO
	//-- checkNotAssignedToPallet
	public static void checkNotAssignedToPallet( ProcessGroup boxData ) throws CustomException{
		if( !StringUtils.isEmpty(boxData.getSuperProcessGroupName())){
			throw new CustomException("BOX-9002", boxData.getSuperProcessGroupName());
		}
	}
	
	//-- COMMENT
	//-- 2011.01.28 // 2016.02.22 LEE HYEON WOO
	//-- checkBoxIdLength
	public static void checkEmptyBoxIdLength( String aBoxId ) throws CustomException{
		if( aBoxId.length() != 14){
			throw new CustomException("BOX-9003");
		}
	}
	
	//-- COMMENT
	//-- 2011.02.21 JUNG SUN KYU // 2016.02.23 LEE HYEON WOO
	public static void checkShipToFGMS(ProcessGroup processGroupData) 
			throws CustomException
	{
		if(StringUtils.equals(processGroupData.getUdfs().get("SHIPTOFGMS"), "Y"))
			throw new CustomException("LOT-9017", processGroupData.getKey().getProcessGroupName().toString());
	}
	
	//-- COMMENT
	//-- 2011.02.23 JUNG SUN KYU // 2016.02.23 LEE HYEON WOO
	public static void checkFGMSInterfaceFlag(ProcessGroup processGroupData)
			throws CustomException
	{
		String sql = "SELECT INTERFACEFLAG FROM CT_FGMS_SHIP_PALLET WHERE PALLETNAME = :palletName";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		
		bindMap.put("palletName", processGroupData.getKey().getProcessGroupName());
		
		List<Map<String, Object>> sqlResult 
		  = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);

		String interfaceFlag = "";
		
        if( sqlResult.size() == 0 ) {
        	throw new CustomException("PALLET-9008", processGroupData.getKey().getProcessGroupName()); 
        }
        else
        	interfaceFlag = sqlResult.get(0).get("INTERFACEFLAG").toString();
       
        if(!StringUtils.equals(interfaceFlag, "N"))
        	throw new CustomException("PALLET-9009", processGroupData.getKey().getProcessGroupName());
	}
	
	//-- COMMENT
	//-- 2011.01.28 // 2016.02.23 LEE HYEON WOO
	//-- checkPanelExist
	public static Lot checkPanelExist( String aPanelId ) throws CustomException{
		try {
			Lot panelData = LotServiceProxy.getLotService().selectByKey(new LotKey(aPanelId));
			return panelData;
		} catch (Exception e) {
			throw new CustomException("LOT-9000", aPanelId);	
		}
	}
	
	//-- COMMENT
	//-- 2011.02.21 JUNG SUN KYU // 2016.02.23 LEE HYEON WOO
	public static void checkLineName(Lot lotData, 
									 String machineName) throws CustomException
	{
		String sql = "SELECT LINENAME FROM MACHINESPEC WHERE MACHINENAME = :machineName";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		
		bindMap.put("machineName", machineName);
		
		List<Map<String, Object>> sqlResult 
		  = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		String lineName = "";
		
        if( sqlResult.size()> 0 ) {
        	lineName = sqlResult.get(0).get("LINENAME").toString();
        }
        else
        	throw new CustomException("MACHINE-9001", machineName); 
        
        //LineName match check
        if(!StringUtils.equals(lineName, lotData.getUdfs().get("LINENAME").toString()))
        	throw new CustomException("MACHINE-9002", machineName, lotData.getUdfs().get("LINENAME"));
	}
	
	//-- COMMENT
	//-- 2011.02.21 JUNG SUN KYU // 2016.02.23 LEE HYEON WOO
	public static void checkEtcState(Lot lotData) 
			throws CustomException
	{		
		if(StringUtils.equals(lotData.getUdfs().get("ETCSTATE").toString(), "OUT"))
			throw new CustomException("LOT-9017", lotData.getKey().getLotName().toString());
	}
	
	//-- COMMENT
	//-- 2011.01.28 // 2016.02.23 LEE HYEON WOO
	//-- checkPanelState
	public static void checkPanelState( Lot lotData, String lotState, String lotHoldState, 
										String lotProcessState, String reworkState ) throws CustomException{
		if( !StringUtils.equals(lotData.getLotState(), lotState)) {
			throw new CustomException("LOT-9003", lotData.getLotState());
		}
		
		if( !StringUtils.equals(lotData.getLotHoldState(), lotHoldState)){
			throw new CustomException("LOT-9004", lotData.getLotHoldState());
		}
		
		if( !StringUtils.equals(lotData.getLotProcessState(), lotProcessState)){
			throw new CustomException("LOT-9005", lotData.getLotProcessState());
		}
		
		if( !StringUtils.equals(lotData.getReworkState(), reworkState)){
			throw new CustomException("LOT-9006", lotData.getReworkState());
		}
	}
	
//	//-- COMMENT
//	//-- 2011.01.28
//	//-- checkNotAssignedToBox // 2016.02.23 LEE HYEON WOO
//	public static void checkNotAssignedToBox( Lot lotData ) throws CustomException{
//		if( !StringUtils.isEmpty(lotData.getProcessGroupName())){
//			throw new CustomException("LOT-9008", (lotData.getKey().getLotName()));
//		}
//	}
//	
//	//-- COMMENT
//	//-- 2011.01.28
//	//-- checkPanelStep // 2016.02.23 LEE HYEON WOO
//	public static void checkPanelStep( Lot lotData, String stepID ) throws CustomException{
//
//		if( !StringUtils.equals(lotData.getProcessOperationName(), stepID )){
//			String stepName = "";
//			
//			try{
//				ProcessOperationSpecKey processOperationKey = new ProcessOperationSpecKey();
//				
//				processOperationKey.setFactoryName(lotData.getFactoryName());
//				processOperationKey.setProcessOperationName(stepID);
//				processOperationKey.setProcessOperationVersion("00001");
//				
//				ProcessOperationSpec processOperationData 
//					= ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(processOperationKey);
//				
//				stepName = processOperationData.getDescription();
//			} catch( Exception e ){
//				throw new CustomException("PROCESSOPERATION-9000", stepID);
//			}
//			
//			throw new CustomException("LOT-9007", stepName);
//		}
//	}
	
	//-- COMMENT
	//-- 2011.01.28 // LEE HYEON WOO
	//-- checkConsumable
	public static void checkConsumableInfo( Lot lotData, String checkConsumable, List<ConsumedMaterial> cms) throws CustomException{
		if( cms.size() == 0 ){
			throw new CustomException("CONSUMABLE-9000", lotData.getKey().getLotName(), checkConsumable);
		}
	}
	
	//-- COMMENT
	//-- 2011.01.28 // 2016.02.26 LEE HYEON WOO
	//-- checkPalletIdLength
	public static void checkPalletIdLength( String aPallet ) throws CustomException{
		if( aPallet.length() != 12){
			throw new CustomException("PALLET-9002");
		}
	}
	
	//-- COMMENT
	//-- 2016.03.18
	//-- checkProductRequestHoldState
	public static void checkProductRequestHoldState( String productRequestName ) throws CustomException{
		ProductRequestKey pKey = new ProductRequestKey(productRequestName);
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey); 
		
		if(pData.getProductRequestHoldState().equals(GenericServiceProxy.getConstantMap().Prq_OnHold))
		{
			throw new CustomException("PROCESSGROUP-0004", productRequestName);
		}
	}
	
	//-- COMMENT
	//-- 2016.03.22 by hwlee89
	//-- checkDurableDirtyState
	public static void checkDurableDirtyState( String durableName ) throws CustomException{
		DurableKey durableKey = new DurableKey(durableName) ;
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
		
		if(StringUtils.equals(durableData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Dirty ) )
		{
			throw new CustomException("CST-0013", durableName);
		}
	}
	
	//-- COMMENT
	//-- 2016.03.22 by hwlee89
	//-- checkDurableDirtyState
	public static void checkDurableDirtyState( Durable durableData ) throws CustomException{
		if(StringUtils.equals(durableData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Dirty ) )
		{
			throw new CustomException("CST-0013", durableData.getKey().getDurableName());
		}
	}
	
	//-- COMMENT
	//-- 2016.03.22 by hwlee89
	//-- CheckDurableHoldState
	public static void CheckDurableHoldState(String durableName) throws CustomException {
		
		DurableKey durableKey = new DurableKey(durableName) ;
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			
		if (StringUtils.equals(durableData.getUdfs().get("DURABLEHOLDSTATE").toString(),
				GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y) ) {
			throw new CustomException("CST-0005", durableName);
		}
		
	}
	
	//-- COMMENT
	//-- 2016.04.20 by xzquan
	//-- CheckCrateSpec
	public static void CheckCrateSpec(String factoryName, String productSpecName, String crateSpecName, String productionType) throws CustomException
	{
//		if(productionType.equals("D")) return;
		
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT P.MATERIALSPECNAME AS CONSUMABLESPECNAME ");
		sql.append("   FROM TPPOLICY T, POSBOM P ");
		sql.append("  WHERE     T.CONDITIONID = P.CONDITIONID ");
		sql.append("        AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("        AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);

		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		try
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			
			Map<String, Object> obj = new HashMap<String, Object>();
			obj.put("CONSUMABLESPECNAME", crateSpecName);
			
			if (!sqlResult.contains(obj)) {
				throw new CustomException("CRATE-0004", "","");
			}
		}
		catch (Exception ex)
		{
			throw new CustomException("CRATE-0003", factoryName, productSpecName);
		}
	}
	
	public static void checkMachineState(Machine machineData) throws CustomException
	{
		if(StringUtils.equals(machineData.getCommunicationState(),"OnLineLocal") ||
				StringUtils.equals(machineData.getCommunicationState(),"OnLineRemote"))
		{
			if(StringUtils.equals(machineData.getMachineStateName(), "DOWN") ||
					StringUtils.equals(machineData.getMachineStateName(), "PM") ||
					StringUtils.equals(machineData.getMachineStateName(), "NONSCHEDULEDTIME")) //modify by jhying on20200309 ENGINER-->NONSCHEDULEDTIME
			{
				throw new CustomException("MACHINE-0008", machineData.getCommunicationState(), machineData.getMachineStateName());
			}
		}
	}
	
	//add by wghuang 20180529
	public static void checkMachineOperationModeExistence(Machine machineData) throws CustomException
	{
		if(StringUtil.isEmpty(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE")))
			throw new CustomException("MACHINE-0011", machineData.getKey().getMachineName());	
	}
	
	/*
	* Name : checkProductGradeN
	* Desc : This function is checkLotHoldState
	* Author : Park Jeong Su
	* Date : 2019.03.20
	*/ 
	public static void checkProductGradeN( Lot lotData ) throws CustomException{
		boolean productGradeNFlag=false;
		StringBuilder str = new StringBuilder();
		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
		for(Product product : productList){
			if(StringUtils.equals("N", product.getProductGrade())){
				str.append(product.getKey().getProductName()+" ");
				productGradeNFlag=true;
			}
		}
		
		if(productGradeNFlag){
			throw new CustomException("LOT-0225", str.toString());
		}
	}
	
	public static void checkWORemainQty(String productRequestName, long releaseQty) throws CustomException 
	{
		//Get Product Request Data
		ProductRequestKey pKey = new ProductRequestKey(productRequestName);
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		
		long remainQty = pData.getPlanQuantity();
		String planQty = "";
	    String notCompletedQty = "";
	    String otherQty = "";
		
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append(" /* GetProductRequestList [00030] */ ");
			sql.append("   SELECT DISTINCT ");
			sql.append("          'False' CHECKBOX, ");
			sql.append("          P.PRODUCTREQUESTNAME, ");
			sql.append("          P.PRODUCTSPECNAME, ");
			sql.append("          P.PRODUCTREQUESTTYPE, ");
			sql.append("          P.PLANRELEASEDTIME, ");
			sql.append("          P.PLANFINISHEDTIME, ");
			sql.append("          P.PLANQUANTITY, ");
			sql.append("          P.RELEASEDQUANTITY, ");
			sql.append("            P.PLANQUANTITY ");
			sql.append("          - NVL (NRQ.QUANTITY, 0) ");
			sql.append("          - P.RELEASEDQUANTITY ");
			sql.append("             AS REMAINQUANTITY, ");
			sql.append("          P.PRODUCTREQUESTSTATE, ");
			sql.append("          P.PRODUCTREQUESTHOLDSTATE, ");
			sql.append("          TO_CHAR (P.CREATETIME, 'YYYY-MM-DD HH24:MI:SS') CREATETIME, ");
			sql.append("          P.CREATEUSER, ");
			sql.append("          TO_CHAR (P.RELEASETIME, 'YYYY-MM-DD HH24:MI:SS') RELEASETIME, ");
			sql.append("          P.RELEASEUSER, ");
			sql.append("          TO_CHAR (P.COMPLETETIME, 'YYYY-MM-DD HH24:MI:SS') COMPLETETIME, ");
			sql.append("          P.COMPLETEUSER ");
			sql.append("     FROM PRODUCTREQUEST P, ");
			sql.append("          /* All Plan Qty */ ");
			sql.append("          (  SELECT PL.PRODUCTREQUESTNAME, ");
			sql.append("                    SUM (PL.PLANQUANTITY - PL.RELEASEDQUANTITY) AS QUANTITY ");
			sql.append("               FROM PRODUCTREQUESTPLAN PL ");
			sql.append("              WHERE 1 = 1 ");
			sql.append("           GROUP BY PRODUCTREQUESTNAME) NRQ ");
			sql.append("    WHERE     P.PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME ");
			sql.append("          AND P.PRODUCTREQUESTNAME = NRQ.PRODUCTREQUESTNAME(+) ");
			sql.append("          AND P.PRODUCTREQUESTSTATE IN ('Created', 'Released') ");
			sql.append(" ORDER BY P.PRODUCTREQUESTNAME ");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTREQUESTNAME", productRequestName);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> remainList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			
			remainQty = Long.valueOf(remainList.get(0).get("REMAINQUANTITY").toString());
			
			log.debug("WO[" + productRequestName + "] WO Qty[" + planQty + 
					"] / Remain Qty[" + remainQty + "]" );
		}
		catch (Exception ex)
		{
			log.debug("Search Remain Qty Error");
			throw new CustomException("PRODUCTREQUEST-0034", productRequestName);
		}

		if(remainQty < releaseQty) {
			throw new CustomException("PRODUCTREQUEST-0036", productRequestName, releaseQty, remainQty);
		}
	}
	
	/**
	 * Product mix validation
	 * @author swcho
	 * @since 2016.08.09
	 * @param productList
	 * @param allowProductSpec
	 * @param allowProductRequest
	 * @param allowFlow
	 * @param allowOperation
	 * @throws CustomException
	 */
	public static void validateProductMix(List<Element> productList,
											boolean allowProductSpec,
											boolean allowProductRequest,
											boolean allowFlow,
											boolean allowOperation)
		throws CustomException
	{
		String productSpecName = "";
		//String productRequestName = "";
		String productRequestType = "";
		String processFlowName = "";
		String processOperationName = "";
		
		for (Element productElement : productList)
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			
			Product productData = CommonUtil.getProductData(productName);
			
			//get mandatory standard
			if (productSpecName.isEmpty())
				productSpecName = productData.getProductSpecName();
			//2019.02.25_hsryu_Delete Logic. Mantis 0002757.
//			if (productRequestName.isEmpty())
//				productRequestName = productData.getProductRequestName();
			//2019.02.25_hsryu_Mantis 0002757.
			if(productRequestType.isEmpty())
				productRequestType = CommonUtil.getWorkOrderTypeByProductData(productData);
			if (processFlowName.isEmpty())
				processFlowName = productData.getProcessFlowName();
			if (processOperationName.isEmpty())
				processOperationName = productData.getProcessOperationName();
			
			//compare
			if (!productSpecName.equals(productData.getProductSpecName()) && !allowProductSpec)
				throw new  CustomException("PRODUCT-0030", productName, "ProductSpec");
			//2019.02.25_hsryu_Delete Logic. Mantis 0002757.
//			else if (!productRequestName.equals(productData.getProductRequestName()) && !allowProductRequest)
//				throw new  CustomException("PRODUCT-0030", productName, "WorkOrder");
			//2019.02.25_hsryu_Mantis 0002757.
			else if (!productRequestType.equals(CommonUtil.getWorkOrderTypeByProductData(productData)))
				throw new  CustomException("PRODUCT-0030", productName, "productRequestType");
			else if (!processFlowName.equals(productData.getProcessFlowName()) && !allowFlow)
				throw new  CustomException("PRODUCT-0030", productName, "ProcessFlow");
			else if (!processOperationName.equals(productData.getProcessOperationName()) && !allowOperation)
				throw new  CustomException("PRODUCT-0030", productName, "ProcessOperation");
		}
	}
	
	/*
	* Name : checkCstSlot
	* Desc : This function is checkEmptyCst
	* Author : AIM Systems, Inc
	* Date : 2011.09.22
	*/
	public static void checkCstSlot( String slotMap ) throws CustomException
	{
		if(slotMap.contains(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
		{
			//throw new CustomException("CST-0026");
		    throw new CustomException("CST-0009");
		}		
	}
	
	/**
	 *    
	 * @Name     checkCstSlot
	 * @since    2018. 12. 14.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param carrier
	 * @param slotMap
	 * @throws CustomException
	 */
	public static void checkCstSlot(String carrier, String slotMap ) throws CustomException
	{
	    if(slotMap.contains(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
	    {
	        throw new CustomException("CST-0042", carrier, StringUtil.replace(slotMap, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT, GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT), slotMap);
	    }       
	}	    
	
	/**
	 * validateCurrentOperation
	 * @author zhongsl
	 * @since 2017.01.13
	 * @param lotData
	 * @param machineName
	 * @throws CustomException
	 */
	public static void validateCurrentOperation(Lot lotData,String machineName) throws CustomException
	{		
		Boolean existFlag = false;
		try
		{
			String sql = " SELECT P.MACHINENAME " +
					     " FROM TPFOPOLICY T, POSMACHINE P " +
					     " WHERE 1 = 1 AND T.CONDITIONID = P.CONDITIONID " + 
					     " AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME " +
					     " AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
					     " AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
					     " AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
					     " AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
					     " AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
			bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
			bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
			bindMap.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
			bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
			bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
			
			List<ListOrderedMap> machineTPFOList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
						
			if (machineTPFOList.size() > 0)
			{	
				
				for(int i=0;i<machineTPFOList.size();i++)
				{
					if (machineName.equals(machineTPFOList.get(i).getValue(0).toString()))
					{
						existFlag = true;
						break;
					}
				}
			}			
		}
		catch (Exception ex)
		{
			throw new CustomException("MACHINE-0105",lotData.getKey().getLotName(),machineName);
		}	
				
		if(existFlag){
			return;					
		}
		else{
			throw new CustomException("MACHINE-0105",lotData.getKey().toString(),machineName);					
		}
	}
	
	/*
	* Name : checkCarrierLocation
	* Desc : This function is checkCarrierLocation
	* Author : zhongsl  
	* Date : 2017.02.28
	*/
	public static void checkCarrierLocation( Lot lotData) throws CustomException
	{
		//String carrierName = lotData.getCarrierName();
		//Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		//String machineName = CommonUtil.getValue(carrierData.getUdfs(), "MACHINENAME");
		//Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		//MachineSpec machineSpec = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
		
		//modify dmlee 17.03.02
		String carrierName = lotData.getCarrierName();
		
		if(carrierName.isEmpty() && !StringUtil.equalsIgnoreCase("OLED", lotData.getFactoryName()))
		{
			throw new CustomException("LOT-0221", lotData.getKey().toString());
		}
		
		DurableKey durableKey = new DurableKey(carrierName);		
		
		Durable carrierData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
		
		String machineName = CommonUtil.getValue(carrierData.getUdfs(), "MACHINENAME");
		
		if(machineName.isEmpty())
		{
			return;
			//throw new CustomException("CST-0023", carrierName);
		}
	}
	
	/**
	 * validateCurrentOperation
	 * @author zhongsl
	 * @since 2017.07.13
	 * @param lotData
	 * @param machineName
	 * @throws CustomException
	 */
	public static boolean  validateCurrentOperationMapMachine(Lot lotData,String machineName) throws CustomException
	{		
		Boolean existFlag = false;
		
		try
		{
			String sql = " SELECT P.MACHINENAME " +
					     " FROM TPFOPOLICY T, POSMACHINE P " +
					     " WHERE 1 = 1 AND T.CONDITIONID = P.CONDITIONID " + 
					     " AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME " +
					     " AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
					     " AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
					     " AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
					     " AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
					     " AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
			bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
			bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
			bindMap.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
			bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
			bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
			
			List<ListOrderedMap> machineTPFOList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
						
			if (machineTPFOList.size() > 0)
			{	
				
				for(int i=0;i<machineTPFOList.size();i++)
				{
					if (machineName.equals(machineTPFOList.get(i).getValue(0).toString()))
					{
						existFlag = true;
						break;
					}
				}
			}	
			else
			{
				existFlag = false;
			}
		}
		catch (Exception ex)
		{
			existFlag = false;
			throw new CustomException("MACHINE-0105",lotData.getKey().getLotName(),machineName);
		}	
				
		return existFlag;
	}
	
	/*
	* Name : checkLotHoldState_Y
	* Desc : This function is checkLotHoldState_Y
	* Author : hsryu
	* Date : 2018.02.08
	*/ 
	public static void checkLotHoldState_Y( Lot lotData ) throws CustomException{
		if ( lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().FLAG_N) )
		{
			throw new CustomException("LOT-9016", lotData.getKey().getLotName(), lotData.getLotHoldState()); 
		}
	}
	
	/*
	* Name : checkOverReworkLimitCount
	* Desc : This function is checkOverReworkLimitCount
	* Author : hsryu
	* Date : 2018.02.12
	*/ 
	public static void checkOverReworkLimitCount(List<Product> productNameList,String lotname,String factoryName,String beforeFlow, String beforeFlowVersion, String beforeOper,
            String beforeOperVersion, String toFlow, String toOper) throws CustomException
	{
		// Modified by smkang on 2018.09.11 - According to EDO's request, ReworkSectionCount should be checked.
	     ArrayList<String> arrPrdName = new ArrayList<String>();
	     
	     for(Product product : productNameList)
	     {
	    	 StringBuffer queryBuffer = new StringBuffer()
	            .append("SELECT RP.REWORKCOUNT, RP.CURRENTCOUNT \n")
	            .append("  FROM CT_REWORKPRODUCT RP \n")
	            .append(" WHERE 1 = 1 AND RP.PRODUCTNAME = :PRODUCTNAME \n")
	            .append("       AND RP.LOTNAME = :LOTNAME \n")
	            .append("       AND (RP.FACTORYNAME = :FACTORYNAME OR RP.FACTORYNAME = :STAR) \n")
	            .append("       AND (RP.PROCESSFLOWNAME = :PROCESSFLOWNAME OR RP.PROCESSFLOWNAME = :STAR)   \n")
	            .append("       AND (RP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION OR RP.PROCESSFLOWVERSION = :STAR) \n")
	            .append("       AND (RP.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME OR RP.PROCESSOPERATIONNAME = :STAR) \n")
	            .append("       AND (RP.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION OR RP.PROCESSOPERATIONVERSION = :STAR) \n")               
	            .append("       AND RP.REWORKPROCESSFLOWNAME = :REWORKPROCESSFLOWNAME \n")
	            .append("       AND RP.REWORKPROCESSFLOWVERSION = :REWORKPROCESSFLOWVERSION \n")
	            .append("       AND RP.REWORKPROCESSOPERATIONNAME = :REWORKPROCESSOPERATIONNAME \n")
	            .append("       AND RP.REWORKPROCESSOPERATIONVERSION = :REWORKPROCESSOPERATIONVERSION \n")
	            .append("       ORDER BY DECODE (RP.FACTORYNAME, :STAR, 9999, 0), \n")
	            .append("       DECODE (RP.PROCESSFLOWNAME, :STAR, 9999, 0), \n")
	            .append("       DECODE (RP.PROCESSFLOWVERSION, :STAR, 9999, 0), \n")
	            .append("       DECODE (RP.PROCESSOPERATIONNAME, :STAR, 9999, 0), \n")
	            .append("       DECODE (RP.PROCESSOPERATIONVERSION, :STAR, 9999, 0) \n");         
	            
	            HashMap<String, Object> bindMap = new HashMap<String, Object>();

	            bindMap.put("PRODUCTNAME", product.getKey().getProductName());
	            bindMap.put("LOTNAME", lotname);
	            bindMap.put("FACTORYNAME", factoryName);
	            bindMap.put("PROCESSFLOWNAME", beforeFlow);
	            bindMap.put("PROCESSFLOWVERSION", beforeFlowVersion);
	            bindMap.put("PROCESSOPERATIONNAME", beforeOper);
	            bindMap.put("PROCESSOPERATIONVERSION", beforeOperVersion);
	            bindMap.put("REWORKPROCESSFLOWNAME", toFlow);
	            bindMap.put("REWORKPROCESSFLOWVERSION", "00001");
	            bindMap.put("REWORKPROCESSOPERATIONNAME", toOper);
	            bindMap.put("REWORKPROCESSOPERATIONVERSION", "00001");
	            bindMap.put("STAR", "*");
                         
            List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
                   
            if(sqlResult.size() > 0)
            {
               String exceedCount = Integer.parseInt(sqlResult.get(0).get("CURRENTCOUNT").toString())>=Integer.parseInt(sqlResult.get(0).get("REWORKCOUNT").toString())?"Y":"N";
                                         
               if(exceedCount.equals("Y"))
               {
            	   arrPrdName.add(product.getKey().getProductName());
               }
            }
	     }
     
	     if(arrPrdName.size()>0)
	     {
            String overCountProductList = "";
            
            for(int i=0;i<arrPrdName.size();i++)
            {
            	overCountProductList += "[" + arrPrdName.get(i).toString() + "]";
            }
            
            throw new CustomException("PRD-0002", overCountProductList); 
	     }
	     
		ArrayList<String> arrsSectionPrdName = new ArrayList<String>();
		
 		for(Product product : productNameList) {
			StringBuffer queryBuffer = new StringBuffer()
//			.append("WITH REWORKSECTIONCOUNT AS (SELECT TFO.FACTORYNAME, TFO.PROCESSFLOWNAME, TFO.PROCESSOPERATIONNAME, TFO.PROCESSOPERATIONVERSION, RSC.TOPROCESSOPERATIONNAME, RSC.TOPROCESSOPERATIONVERSION, RSC.REWORKCOUNT \n")
			.append("WITH REWORKSECTIONCOUNT AS (SELECT TFO.FACTORYNAME, TFO.PROCESSFLOWNAME, TFO.PROCESSOPERATIONNAME, RSC.TOPROCESSOPERATIONNAME, RSC.REWORKCOUNT \n")
     		.append("                              FROM TFOPOLICY TFO, POSREWORKSECTIONCOUNT RSC \n")
    		.append("                             WHERE TFO.CONDITIONID = RSC.CONDITIONID \n")
    		.append("                               AND (TFO.FACTORYNAME = :FACTORYNAME OR TFO.FACTORYNAME = '*') \n")
    		.append("                               AND (TFO.PROCESSFLOWNAME = :PROCESSFLOWNAME OR TFO.PROCESSFLOWNAME = '*') \n")
//    		.append("                               AND (TFO.PROCESSFLOWVERSION = :PROCESSFLOWVERSION OR TFO.PROCESSFLOWVERSION = '*') \n")
    		.append("                               AND RSC.VALIDFLAG = 'Y'), \n")
//    		.append("     REWORKSECTION AS (SELECT FACTORYNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, 0 AS REWORKCOUNT \n")
    		.append("     REWORKSECTION AS (SELECT FACTORYNAME, PROCESSFLOWNAME, PROCESSOPERATIONNAME, (SELECT REWORKCOUNT FROM REWORKSECTIONCOUNT) AS REWORKCOUNT \n")
    		.append("                         FROM (SELECT N.FACTORYNAME, N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION, N.NODEATTRIBUTE1 PROCESSOPERATIONNAME, N.NODEATTRIBUTE2 PROCESSOPERATIONVERSION, A.FROMNODEID, A.TONODEID \n")
    		.append("                                 FROM ARC A, NODE N, PROCESSFLOW PF \n")
    		.append("                                WHERE PF.PROCESSFLOWNAME = :PROCESSFLOWNAME \n")
    		.append("                                  AND N.FACTORYNAME = :FACTORYNAME \n")
    		.append("                                  AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME \n")
//    		.append("                                  AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION \n")
    		.append("                                  AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME \n")
    		.append("                                  AND N.FACTORYNAME = PF.FACTORYNAME \n")
    		.append("                                  AND A.FROMNODEID = N.NODEID) \n")
    		.append("                                START WITH PROCESSOPERATIONNAME = (SELECT PROCESSOPERATIONNAME FROM REWORKSECTIONCOUNT) \n")
    		.append("                              CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID \n")
    		.append("                                  AND PROCESSOPERATIONNAME <> (SELECT TOPROCESSOPERATIONNAME FROM REWORKSECTIONCOUNT)            \n")
    		.append("                        UNION \n")
//    		.append("                       SELECT :FACTORYNAME, :PROCESSFLOWNAME, :PROCESSFLOWVERSION, TOPROCESSOPERATIONNAME, TOPROCESSOPERATIONVERSION, REWORKCOUNT \n")
    		.append("                       SELECT :FACTORYNAME, :PROCESSFLOWNAME, TOPROCESSOPERATIONNAME, REWORKCOUNT \n")
    		.append("                         FROM REWORKSECTIONCOUNT) \n")
    		.append("SELECT RP.REWORKCOUNT, RP.CURRENTCOUNT, 'REWORKPRODUCT' TYPE \n")
	    	.append("  FROM CT_REWORKPRODUCT RP \n")
	    	.append(" WHERE RP.PRODUCTNAME = :PRODUCTNAME \n")
	    	.append("   AND (RP.FACTORYNAME = :FACTORYNAME OR RP.FACTORYNAME = '*') \n")
	    	.append("   AND (RP.PROCESSFLOWNAME = :PROCESSFLOWNAME OR RP.PROCESSFLOWNAME = '*')   \n")
//	    	.append("   AND (RP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION OR RP.PROCESSFLOWVERSION = '*') \n")
	    	.append("   AND (RP.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME OR RP.PROCESSOPERATIONNAME = '*') \n")
//	    	.append("   AND (RP.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION OR RP.PROCESSOPERATIONVERSION = '*') \n")
	    	.append("   AND RP.REWORKPROCESSFLOWNAME = :REWORKPROCESSFLOWNAME \n")
//	    	.append("   AND RP.REWORKPROCESSFLOWVERSION = :REWORKPROCESSFLOWVERSION \n")
	    	.append("   AND RP.REWORKPROCESSOPERATIONNAME = :REWORKPROCESSOPERATIONNAME \n")
//	    	.append("   AND RP.REWORKPROCESSOPERATIONVERSION = :REWORKPROCESSOPERATIONVERSION")
	    	.append(" UNION \n")
    		.append("SELECT MAX(RS.REWORKCOUNT) REWORKCOUNT, SUM(RP.CURRENTCOUNT) CURRENTCOUNT, 'REWORKSECTION' TYPE \n")
    		.append("  FROM CT_REWORKPRODUCT RP, REWORKSECTION RS \n")
    		.append(" WHERE RP.PRODUCTNAME = :PRODUCTNAME \n")
    		.append("   AND (RP.FACTORYNAME = RS.FACTORYNAME OR RP.FACTORYNAME = '*') \n")
    		.append("   AND (RP.PROCESSFLOWNAME = RS.PROCESSFLOWNAME OR RP.PROCESSFLOWNAME = '*') \n")
//    		.append("   AND (RP.PROCESSFLOWVERSION = RS.PROCESSFLOWVERSION OR RP.PROCESSFLOWVERSION = '*') \n")
    		.append("   AND (RP.PROCESSOPERATIONNAME = RS.PROCESSOPERATIONNAME OR RP.PROCESSOPERATIONNAME = '*') \n")
//    		.append("   AND (RP.PROCESSOPERATIONVERSION = RS.PROCESSOPERATIONVERSION OR RP.PROCESSOPERATIONVERSION = '*') \n")
    		.append(" GROUP BY RP.PRODUCTNAME");
	    	
	    	HashMap<String, Object> bindMap = new HashMap<String, Object>();
	    	bindMap.put("PRODUCTNAME", product.getKey().getProductName());
	    	bindMap.put("FACTORYNAME", factoryName);
	    	bindMap.put("PROCESSFLOWNAME", beforeFlow);
//	    	bindMap.put("PROCESSFLOWVERSION", beforeFlowVersion);
	    	bindMap.put("PROCESSOPERATIONNAME", beforeOper);
//	    	bindMap.put("PROCESSOPERATIONVERSION", beforeOperVersion);
	    	bindMap.put("REWORKPROCESSFLOWNAME", toFlow);
//	    	bindMap.put("REWORKPROCESSFLOWVERSION", "00001");
	    	bindMap.put("REWORKPROCESSOPERATIONNAME", toOper);
//	    	bindMap.put("REWORKPROCESSOPERATIONVERSION", "00001");
	    	
	    	List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
	    		    	 
	    	if(sqlResult.size() > 0) {
	    		for (Map<String, Object> resultRow : sqlResult) {
	    			if (Integer.parseInt(resultRow.get("CURRENTCOUNT").toString()) >= Integer.parseInt(resultRow.get("REWORKCOUNT").toString())) {
		    			 arrPrdName.add(product.getKey().getProductName());
		    			 break;
	    			}
				}
	    	}
		}
		
		if(arrPrdName.size() > 0) {
			String overCountProductList = "[";
			
			for (int i = 0; i < arrPrdName.size(); i++) {
				overCountProductList += arrPrdName.get(i).toString() + ", ";
			}
			
			throw new CustomException("PRD-0002", StringUtils.removeEnd(overCountProductList, ", ").concat("]"));
		}
	}
	
	public static void checkOverReworkLimitCount2(List<Product> productNameList,String lotname,String factoryName,String beforeFlow, String beforeFlowVersion, String beforeOper,
            String beforeOperVersion, String toFlow, String toOper) throws CustomException
	{
		// Modified by smkang on 2018.09.11 - According to EDO's request, ReworkSectionCount should be checked.
	     ArrayList<String> arrOverPrdName = new ArrayList<String>();
	     String arrAllPrdName = "";

	     ArrayList<String> arrPrdName = new ArrayList<String>();
	     
	     for(Product product : productNameList)
	     {
	    	 StringBuffer queryBuffer = new StringBuffer()
	            .append("SELECT RP.REWORKCOUNT, RP.CURRENTCOUNT \n")
	            .append("  FROM CT_REWORKPRODUCT RP \n")
	            .append(" WHERE 1 = 1 AND RP.PRODUCTNAME = :PRODUCTNAME \n")
	            .append("       AND RP.LOTNAME = :LOTNAME \n")
	            .append("       AND (RP.FACTORYNAME = :FACTORYNAME OR RP.FACTORYNAME = :STAR) \n")
	            .append("       AND (RP.PROCESSFLOWNAME = :PROCESSFLOWNAME OR RP.PROCESSFLOWNAME = :STAR)   \n")
	            .append("       AND (RP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION OR RP.PROCESSFLOWVERSION = :STAR) \n")
	            .append("       AND (RP.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME OR RP.PROCESSOPERATIONNAME = :STAR) \n")
	            .append("       AND (RP.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION OR RP.PROCESSOPERATIONVERSION = :STAR) \n")               
	            .append("       AND RP.REWORKPROCESSFLOWNAME = :REWORKPROCESSFLOWNAME \n")
	            .append("       AND RP.REWORKPROCESSFLOWVERSION = :REWORKPROCESSFLOWVERSION \n")
	            .append("       AND RP.REWORKPROCESSOPERATIONNAME = :REWORKPROCESSOPERATIONNAME \n")
	            .append("       AND RP.REWORKPROCESSOPERATIONVERSION = :REWORKPROCESSOPERATIONVERSION \n")
	            .append("       ORDER BY DECODE (RP.FACTORYNAME, :STAR, 9999, 0), \n")
	            .append("       DECODE (RP.PROCESSFLOWNAME, :STAR, 9999, 0), \n")
	            .append("       DECODE (RP.PROCESSFLOWVERSION, :STAR, 9999, 0), \n")
	            .append("       DECODE (RP.PROCESSOPERATIONNAME, :STAR, 9999, 0), \n")
	            .append("       DECODE (RP.PROCESSOPERATIONVERSION, :STAR, 9999, 0) \n");         

	            HashMap<String, Object> bindMap = new HashMap<String, Object>();

	            bindMap.put("PRODUCTNAME", product.getKey().getProductName());
	            bindMap.put("LOTNAME", lotname);
	            bindMap.put("FACTORYNAME", factoryName);
	            bindMap.put("PROCESSFLOWNAME", beforeFlow);
	            bindMap.put("PROCESSFLOWVERSION", beforeFlowVersion);
	            bindMap.put("PROCESSOPERATIONNAME", beforeOper);
	            bindMap.put("PROCESSOPERATIONVERSION", beforeOperVersion);
	            bindMap.put("REWORKPROCESSFLOWNAME", toFlow);
	            bindMap.put("REWORKPROCESSFLOWVERSION", "00001");
	            bindMap.put("REWORKPROCESSOPERATIONNAME", toOper);
	            bindMap.put("REWORKPROCESSOPERATIONVERSION", "00001");
	            bindMap.put("STAR", "*");

            List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);

            if(sqlResult.size() > 0)
            {
            	if(Integer.parseInt(sqlResult.get(0).get("REWORKCOUNT").toString())!=0)
            	{
                	String exceedCount = Integer.parseInt(sqlResult.get(0).get("CURRENTCOUNT").toString())> Integer.parseInt(sqlResult.get(0).get("REWORKCOUNT").toString())?"Y":"N";

                	if(exceedCount.equals("Y"))
                	{
                		arrPrdName.add(product.getKey().getProductName());
                	}

            	}
            }
	     }

	     if(arrPrdName.size()>0)
	     {
	    	 String overCountProductList = "";

	    	 for(int i=0;i<arrPrdName.size();i++)
	    	 {
	    		 overCountProductList += "[" + arrPrdName.get(i).toString() + "]";
	    	 }

            throw new CustomException("PRD-0002", overCountProductList); 
	     }
	     
		ArrayList<String> arrOverSectionPrdName = new ArrayList<String>();
		
		StringBuffer queryBufferForSection = new StringBuffer()
		.append(" SELECT TFO.FACTORYNAME, TFO.PROCESSFLOWNAME, TFO.PROCESSOPERATIONNAME, RSC.TOPROCESSOPERATIONNAME, RSC.REWORKCOUNT  \n")
		.append("                                            FROM TFOPOLICY TFO, POSREWORKSECTIONCOUNT RSC  \n")
		.append("                                          WHERE TFO.CONDITIONID = RSC.CONDITIONID  \n")
		.append("                                            AND (TFO.FACTORYNAME = :FACTORYNAME  OR TFO.FACTORYNAME = '*')  \n")
		.append("                                            AND (TFO.PROCESSFLOWNAME = :PROCESSFLOWNAME  OR TFO.PROCESSFLOWNAME = '*')  \n")
		.append("                                            AND RSC.VALIDFLAG = 'Y' \n");
		
       HashMap<String, Object> bindMapForSection = new HashMap<String, Object>();

       bindMapForSection.put("FACTORYNAME", factoryName);
       bindMapForSection.put("PROCESSFLOWNAME", beforeFlow);

    	List<Map<String, Object>> sqlResultForSection = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBufferForSection.toString(), bindMapForSection);
    	
    	if(sqlResultForSection.size() > 0) {
    		for (Map<String, Object> resultRow : sqlResultForSection) {
    			
                String sectionProcessFlowName = resultRow.get("PROCESSFLOWNAME").toString();
                String sectionFromProcessOperationName = resultRow.get("PROCESSOPERATIONNAME").toString();
                String sectionToProcessOperationName = resultRow.get("TOPROCESSOPERATIONNAME").toString();
                String reworkCount = resultRow.get("REWORKCOUNT").toString();
                
                for(Product product : productNameList) {
                	StringBuffer queryBufferForSection2 = new StringBuffer()
                	.append(" WITH REWORKSECTION AS (SELECT FACTORYNAME, PROCESSFLOWNAME, PROCESSOPERATIONNAME  \n")
                	.append("                                      FROM (SELECT N.FACTORYNAME, N.PROCESSFLOWNAME, N.PROCESSFLOWVERSION, N.NODEATTRIBUTE1 PROCESSOPERATIONNAME, N.NODEATTRIBUTE2 PROCESSOPERATIONVERSION, A.FROMNODEID, A.TONODEID  \n")
                	.append("                                              FROM ARC A, NODE N, PROCESSFLOW PF  \n")
                	.append("                                             WHERE PF.PROCESSFLOWNAME = :PROCESSFLOWNAME \n")
                	.append("                                               AND N.FACTORYNAME = :FACTORYNAME   \n")
                	.append("                                               AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME  \n")
                	.append("                                               AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME  \n")
                	.append("                                               AND N.FACTORYNAME = PF.FACTORYNAME  \n")
                	.append("                                               AND A.FROMNODEID = N.NODEID)  \n")
                	.append("                                             START WITH PROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME \n")
                	.append("                                           CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID  \n")
                	.append("                                               AND PROCESSOPERATIONNAME <> :TOPROCESSOPERATIONNAME           \n")
                	.append("                                     UNION  \n")
                	.append("                                    SELECT :FACTORYNAME , :PROCESSFLOWNAME, :TOPROCESSOPERATIONNAME \n")
                	.append("                                      FROM DUAL)  \n")
                	.append("             SELECT RP.PRODUCTNAME, :REWORKCOUNT REWORKCOUNT, SUM(CURRENTCOUNT) CURRENTCOUNT, 'REWORKSECTION' TYPE  \n")
                	.append("               FROM CT_REWORKPRODUCT RP, REWORKSECTION RS  \n")
                	.append("              WHERE RP.PRODUCTNAME = :PRODUCTNAME \n")
                	.append("                AND (RP.FACTORYNAME = RS.FACTORYNAME OR RP.FACTORYNAME = '*')  \n")
                	.append("                AND (RP.PROCESSFLOWNAME = RS.PROCESSFLOWNAME OR RP.PROCESSFLOWNAME = '*')  \n")
                	.append("                AND (RP.PROCESSOPERATIONNAME = RS.PROCESSOPERATIONNAME OR RP.PROCESSOPERATIONNAME = '*')  \n")
                	.append("              GROUP BY RP.PRODUCTNAME \n");
                	
                    HashMap<String, Object> bindMapForSection2 = new HashMap<String, Object>();

                    bindMapForSection2.put("FACTORYNAME", factoryName);
                    bindMapForSection2.put("PROCESSFLOWNAME", beforeFlow);
                    bindMapForSection2.put("FROMPROCESSOPERATIONNAME", sectionFromProcessOperationName);
                    bindMapForSection2.put("TOPROCESSOPERATIONNAME", sectionToProcessOperationName);
                    bindMapForSection2.put("REWORKCOUNT", Integer.parseInt(reworkCount));
                    bindMapForSection2.put("PRODUCTNAME", product.getKey().getProductName());

                 	List<Map<String, Object>> sqlResultForSection2 = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBufferForSection2.toString(), bindMapForSection2);

                 	if(sqlResultForSection2.size()>0)
                 	{
                		for (Map<String, Object> resultSectionRow : sqlResultForSection2) {
        	    			if (Integer.parseInt(resultSectionRow.get("CURRENTCOUNT").toString()) > Integer.parseInt(resultSectionRow.get("REWORKCOUNT").toString())) {
        	    				arrOverSectionPrdName.add(product.getKey().getProductName());
       	    			}
                		}
                		
                 	}
                }
			}
    	}
    	
	     if(arrOverSectionPrdName.size()>0)
	     {
          String overCountProductList = "";
          
          for(int i=0;i<arrOverSectionPrdName.size();i++)
          {
          	overCountProductList += "[" + arrOverSectionPrdName.get(i).toString() + "]";
          }
          
          throw new CustomException("PRD-0004", overCountProductList); 
	     }
	}

	
	// 2018.07.27
	public static void CheckSortOperaion(Lot lotData, String lotName) throws CustomException
	{
		String sql = "SELECT PROCESSFLOWNAME FROM PROCESSFLOW WHERE PROCESSFLOWTYPE = :PROCESSFLOWTYPE AND ROWNUM = 1";
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		Map bindMap = new HashMap<String, Object>();
		bindMap.put("PROCESSFLOWTYPE", GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SORT);
		
		sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		if(sqlResult.size() > 0) 
		{
			String SortProcessFlowName = sqlResult.get(0).get("PROCESSFLOWNAME").toString();
			
			sql = "SELECT LEVEL LV,FACTORYNAME,PROCESSOPERATIONNAME,PROCESSFLOWNAME,PROCESSFLOWVERSION,NODEID,NODETYPE";
			sql += " FROM (SELECT N.FACTORYNAME,N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,N.PROCESSFLOWNAME,N.PROCESSFLOWVERSION,N.NODEID,N.NODETYPE,A.FROMNODEID,A.TONODEID";
			sql += " FROM ARC A,";
			sql += " NODE N,";
			sql += " PROCESSFLOW PF";
			sql += " WHERE 1 = 1";
			sql += " AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME";
			sql += " AND N.FACTORYNAME = :FACTORYNAME";
			sql += " AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME";
			sql += " AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION";
			sql += " AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME";
			sql += " AND N.FACTORYNAME = PF.FACTORYNAME";
			sql += " AND A.FROMNODEID = N.NODEID)";
			sql += " START WITH NODETYPE = :NODETYPE";
			sql += " CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID";
			
			bindMap.put("PROCESSFLOWNAME", SortProcessFlowName);
			bindMap.put("FACTORYNAME", lotData.getFactoryName());
			bindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> Result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			String SortOperationName = Result.get(1).get("PROCESSOPERATIONNAME").toString();
			
			if(StringUtil.equals(lotData.getProcessOperationName(),SortOperationName))
			{
				sql = "SELECT C.JOBNAME FROM CT_SORTJOB C JOIN CT_SORTJOBCARRIER CC ON C.JOBNAME = CC.JOBNAME WHERE C.JOBSTATE IN (:WAIT, :CONFIRMED, :STARTED) AND LOTNAME = :LOTNAME";
				bindMap.clear();
				bindMap.put("LOTNAME", lotName);
				bindMap.put("WAIT", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
				bindMap.put("CONFIRMED", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED);
				bindMap.put("STARTED", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED);
				
				List<Map<String, Object>> C_Result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				if(C_Result.size() < 1)
				{
					throw new CustomException("SORT-0003");
				}
			}
		}
	}
	
	public static boolean checkFirstOperation(Lot lotData) throws CustomException
	{
		String nodeId = "";
		String sql    = "";
		
		Map bindMap = new HashMap<String, Object>();
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> sqlRst = new ArrayList<Map<String, Object>>();

		sql = " SELECT N.NODEID "
			+ " FROM NODE N, PROCESSFLOW F "
			+ " WHERE 1=1 AND N.NODEATTRIBUTE1 = :PROCESSOPERATIONNAME "
			+ " AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME "
			+ " AND N.PROCESSFLOWVERSION =:PROCESSFLOWVERSION "
			+ " AND N.PROCESSFLOWNAME = F.PROCESSFLOWNAME ";
		
		bindMap.clear();
		bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		bindMap.put("PROCESSFLOWNAME"     , lotData.getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION"  , "00001");

		//sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if(sqlResult.size() > 0) 
		{
			String nodeName = (String)sqlResult.get(0).get("NODEID");	

			sql = " SELECT N.NODETYPE "+
				  " FROM NODE N "+
				  "	WHERE 1=1 "+
				  " AND N.NODEID = ( SELECT FROMNODEID FROM ARC WHERE TONODEID = :NODEID AND PROCESSFLOWNAME = :PROCESSFLOWNAME ) ";
			
			bindMap.clear();
			bindMap.put("NODEID", nodeName);
			bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
			//sqlRst = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			sqlRst = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if ( sqlRst.size() > 0)
			{
				String nodeType = (String)sqlRst.get(0).get("NODETYPE");	
				if(nodeType.equals("Start"))
				{
					return true; 
				}
				else
				{
					return false;
				}
			}
		}
		
		return false;
	
	}
	
	public static boolean checkFirstOperation(Lot lotData, String type) throws CustomException
	{
		String nodeId = "";
		String sql    = "";
		
		Map bindMap = new HashMap<String, Object>();
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> sqlRst = new ArrayList<Map<String, Object>>();

		sql = " SELECT N.NODEID "
			+ " FROM NODE N, PROCESSFLOW F "
			+ " WHERE 1=1 AND N.NODEATTRIBUTE1 = :processOperationName "
			+ " AND N.PROCESSFLOWNAME = :processFlowName "
			+ " AND N.PROCESSFLOWVERSION =:processFlowVersion "
			+ " AND N.PROCESSFLOWNAME = F.PROCESSFLOWNAME "
			+ " AND UPPER(F.PROCESSFLOWTYPE) = :TYPE ";
		
		bindMap.clear();
		bindMap.put("processOperationName", lotData.getProcessOperationName());
		bindMap.put("processFlowName"     , lotData.getProcessFlowName());
		bindMap.put("processFlowVersion"  , "00001");
		bindMap.put("TYPE"  , type);

		//sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if(sqlResult.size() > 0) 
		{
			String nodeName = (String)sqlResult.get(0).get("NODEID");	

			sql = " SELECT N.NODETYPE "+
				  " FROM NODE N "+
				  "	WHERE 1=1 "+
				  " AND N.NODEID = ( SELECT FROMNODEID FROM ARC WHERE TONODEID = :nodeId AND PROCESSFLOWNAME = :processFlowName ) ";
			
			bindMap.clear();
			bindMap.put("nodeId", nodeName);
			bindMap.put("processFlowName", lotData.getProcessFlowName());
			//sqlRst = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			sqlRst = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if ( sqlRst.size() > 0)
			{
				String nodeType = (String)sqlRst.get(0).get("NODETYPE");	
				if(!nodeType.equals("Start"))
				{
					return true; 
					//throw new CustomException("LOT-9012", lotData.getProcessOperationName()); 
				}
				else
				{
					return false;
				}
			}
		}
		
		return false;
	}
	
	public static void checkMaskUseState( String maskName ) throws CustomException
	{
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(maskName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			  
		if(durableData.getDurableState().equals("InUse"))
			throw new CustomException("MASK-0085", maskName);
		
	}	
	
	public static void checkExistMountMask(Durable maskData, String position, String machineName) throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT DURABLENAME \n")
		.append("  FROM DURABLE \n")
		.append(" WHERE 1 = 1 AND DURABLENAME = :DURABLENAME \n")
		.append("       AND FACTORYNAME = :FACTORYNAME \n")
		.append("       AND MACHINENAME = :MACHINENAME \n")
		.append("       AND MASKPOSITION = :MASKPOSITION \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("FACTORYNAME", maskData.getFactoryName());
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("DURABLENAME", maskData.getKey().getDurableName());
		bindMap.put("MASKPOSITION", Integer.parseInt(position));
				
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
		
		if ( sqlResult.size() == 0)
		{
			throw new CustomException("MASK-0090", maskData.getKey().getDurableName(), position,machineName);
		}
	}
	
	public static void checkSameOperAction(String productSpecName, String ecCode, String processFlowName, String processFlowVer, String processOperationName, String departmentName) throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT * FROM CT_OPERACTION \n")
		.append("  WHERE 1=1 \n")
		.append(" AND PRODUCTSPECNAME = :PRODUCTSPECNAME \n")
		 // 2019.05.06_hsryu_Insert ECCode. it was missing. 
		.append(" AND ECCODE = :ECCODE \n")
		.append(" AND PROCESSFLOWNAME = :PROCESSFLOWNAME \n")
		.append(" AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION \n")
		.append(" AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME \n")
		// 2019.05.17_hsryu_Insert "DEPARTMENT" Condition. Requested by CIM. 
		.append(" AND DEPARTMENTNAME = :DEPARTMENTNAME \n");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("PRODUCTSPECNAME", productSpecName);
		// 2019.05.06_hsryu_Insert ECCode. it was missing. 
		bindMap.put("ECCODE", ecCode);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVer);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		// 2019.05.17_hsryu_Insert Logic. Requested by CIM. 
		bindMap.put("DEPARTMENTNAME", departmentName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
		
		if ( sqlResult.size() > 0)
		{
			throw new CustomException("OPERACTION-0001", productSpecName, ecCode, processFlowName,processOperationName, departmentName );
		}
	}
	
	public static void checkExistSortJob(String factoryName, String CarrierName) throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT SJC.CARRIERNAME, SJ.JOBNAME FROM CT_SORTJOB SJ, CT_SORTJOBCARRIER SJC, DURABLE D \n")
		.append("  WHERE SJ.JOBNAME = SJC.JOBNAME \n")
		.append(" AND SJC.CARRIERNAME = D.DURABLENAME \n")
		.append(" AND D.FACTORYNAME = :FACTORYNAME \n")
		.append(" AND D.DURABLENAME = :DURABLENAME \n")
		.append(" AND UPPER(SJ.JOBSTATE) IN (:CONFIRMED, :WAIT, :STARTED) \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("DURABLENAME", CarrierName);
		bindMap.put("CONFIRMED", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED);
		bindMap.put("WAIT", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
		bindMap.put("STARTED", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
		
		if ( sqlResult.size() > 0)
		{
			String sortJobName = sqlResult.get(0).get("JOBNAME").toString();
			throw new CustomException("SORT-0005",CarrierName,sortJobName);
		}
	}
	
	public static void checkExistOtherSortJob(String factoryName, String CarrierName) throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT SJC.CARRIERNAME, SJ.JOBNAME, SJ.CREATETIME FROM CT_SORTJOB SJ, CT_SORTJOBCARRIER SJC, DURABLE D \n")
		.append("  WHERE SJ.JOBNAME = SJC.JOBNAME \n")
		.append(" AND SJC.CARRIERNAME = D.DURABLENAME \n")
		.append(" AND D.FACTORYNAME = :FACTORYNAME \n")
		.append(" AND D.DURABLENAME = :DURABLENAME \n")
		.append(" AND UPPER(SJ.JOBSTATE) IN (:CONFIRMED, :WAIT, :STARTED) \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("DURABLENAME", CarrierName);
		bindMap.put("CONFIRMED", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED);
		bindMap.put("WAIT", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
		bindMap.put("STARTED", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
		
		if ( sqlResult.size() > 0)
		{
			String sortJobName = sqlResult.get(0).get("JOBNAME").toString();
			String jobState = sqlResult.get(0).get("JOBSTATE").toString();
			String createTime = sqlResult.get(0).get("CREATETIME").toString();
			
			throw new CustomException("SORT-0006",CarrierName,sortJobName,jobState,createTime);
		}
	}

	
	public static void checkExistHoldAction(String lotName, String factoryName, String processFlowName, String processFlowVer,
			String processOperationName, String processOperationVersion, String holdCode, String holdType, String holdPermanentFlag, String department) throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT * FROM CT_LOTACTION \n")
		.append("  WHERE 1=1 \n")
		.append(" AND LOTNAME = :LOTNAME \n")
		.append(" AND FACTORYNAME = :FACTORYNAME \n")
		.append(" AND PROCESSFLOWNAME = :PROCESSFLOWNAME \n")
		.append(" AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION \n")
		.append(" AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME \n")
		.append(" AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION \n")
		.append(" AND HOLDCODE = :HOLDCODE \n")
		.append(" AND HOLDTYPE = :HOLDTYPE \n")
		.append(" AND HOLDPERMANENTFLAG = :HOLDPERMANENTFLAG \n")
		.append(" AND DEPARTMENT = :DEPARTMENT \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("LOTNAME", lotName);
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVer);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
		bindMap.put("HOLDCODE", holdCode);
		bindMap.put("HOLDTYPE", holdType);
		bindMap.put("HOLDPERMANENTFLAG", holdPermanentFlag);
		bindMap.put("DEPARTMENT", department);
				
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
		
		if ( sqlResult.size() > 0)
		{
			throw new CustomException("ACTION-0002", lotName,processFlowName,processOperationName, holdCode, holdPermanentFlag, department);
		}
	}
	
	// 2019.05.14_hsryu_Add Validation. if MQCValidationFlag is 'N', Not Proceed!
	public static void checkMQCValidationFlag(Lot lotData) throws CustomException {
		
		ProductSpec productSpecData = null;
		
		if(StringUtils.equals(lotData.getProductionType(), GenericServiceProxy.getConstantMap().PRODUCTION_TYPE_MQCA)){

			try{				
				productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), 
						lotData.getProductSpecName(), lotData.getProductSpecVersion());
			}
			catch(Throwable e){
				log.error("ProductSpecData is null.. Check Please.");
			}
			
			if(productSpecData != null){
				if(StringUtils.equals(productSpecData.getUdfs().get("MQCVALIDATIONFLAG"), "N")){
					throw new CustomException("MQC-0059", lotData.getKey().getLotName(), productSpecData.getKey().getProductSpecName());
				}
			}
		}
	}
	
	public static void checkProductRequestStateClosed(String productName) throws CustomException{
		ProductKey keyInfo = new ProductKey(productName); 
		Product productData = ProductServiceProxy.getProductService().selectByKey(keyInfo);
		// 2019.05.23_hsryu_Add Logic. productData is not 'MQCA'.. 
		if(!StringUtils.equals(productData.getProductionType(), GenericServiceProxy.getConstantMap().PRODUCTION_TYPE_MQCA)){
			ProductRequestKey pKey = new ProductRequestKey();
			pKey.setProductRequestName(productData.getProductRequestName());
			ProductRequest productRequest = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
			if(StringUtils.equals("Closed", productRequest.getProductRequestState())){
				throw new CustomException("PRODUCTREQUEST-0056", productRequest.getKey().getProductRequestName());
			}
		}
	}
//	public static void checkFirstCheckResultIsY(String lotName) throws CustomException{
//		Lot lotData =  LotServiceProxy.getLotService().selectByKey(new LotKey(lotName));
//		if(StringUtils.equals("Y", lotData.getUdfs().get("FIRSTCHECKRESULT"))){
//			throw new CustomException("LOT-0229", lotData.getKey().getLotName());
//		}
//	}

	/*
	 * Name : checkLotStateAndLotHoldStateByLotList Desc : This function is
	 * checkLotStateAndLotHoldStateByLotList Author : AIM Systems, Inc Date :
	 * 2019.06.19
	 */
	public static List<Lot> checkLotStateAndLotHoldStateByLotList( List<String> lotList ) throws CustomException
	{
		List<Lot> returnLotList = new ArrayList<Lot>();

		for ( String lotName : lotList )
		{
			Lot lotData = CommonUtil.getLotInfoByLotName( lotName );

			checkLotState( lotData );
			checkLotHoldState( lotData );			
			returnLotList.add( lotData );
		}

		return returnLotList;
	}

	/*
	 * Name : checkProductHoldState Desc : This function is
	 * checkProductHoldState Author : AIM Systems, Inc Date : 2014.02.10
	 */
	public static void checkProductHoldState( List<String> productList ) throws CustomException
	{
		if ( productList.size() > 0 )
		{
			// Check Hold Product where Not (Scrapped, Consumed) Product
			StringBuilder sql = new StringBuilder();
			sql.append( "SELECT PRODUCTNAME " );
			sql.append( "  FROM PRODUCT " );
			sql.append( " WHERE PRODUCTNAME IN (:PRODUCTLIST) " );
			sql.append( "   AND PRODUCTSTATE != :SCRAPPED " );
			sql.append( "   AND PRODUCTSTATE != :CONSUMED " );
			sql.append( "   AND PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE " );

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put( "PRODUCTLIST", productList );
			bindMap.put( "SCRAPPED", GenericServiceProxy.getConstantMap().Prod_Scrapped );
			bindMap.put( "CONSUMED", GenericServiceProxy.getConstantMap().Prod_Consumed );
			bindMap.put( "PRODUCTHOLDSTATE", GenericServiceProxy.getConstantMap().Prod_OnHold );

			List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( sql.toString(), bindMap );

			if ( sqlResult.size() > 0 )
			{
				productList.clear();

				for ( int i = 0; sqlResult.size() > i; i++ )
				{
					productList.add( ConvertUtil.getMapValueByName( sqlResult.get( i ), "PRODUCTNAME" ) );
				}

				throw new CustomException( "PRODUCT-9009", productList, GenericServiceProxy.getConstantMap().Prod_OnHold );
			}
		}
	}

	/*
	 * Name : checkDistinctProductionType Desc : This function is
	 * checkDistinctProductionType Author : AIM Systems, Inc Date : 2019.05.16
	 */
	public static void checkDistinctProductionType( List<String> lotList ) throws CustomException
	{
		if ( lotList.size() > 0 )
		{

			String sql = "SELECT DISTINCT PRODUCTIONTYPE FROM LOT WHERE LOTNAME IN (:lotList) ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put( "lotList", lotList );

			List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( sql, bindMap );

			if ( sqlResult.size() > 1 ) { throw new CustomException( "LOT-9019" ); }
		}
	}

	/*
	 * Name : checkSameProductionType Desc : This function is
	 * checkSameProductionType Author : AIM Systems, Inc Date : 20120.01.03
	 */
	public static void checkSameProductionType( List<String> productList ) throws CustomException
	{
		if ( productList.size() > 0 )
		{
			StringBuilder sql = new StringBuilder();
			sql.append( "SELECT DISTINCT PRODUCTIONTYPE FROM PRODUCT WHERE PRODUCTNAME IN (:PRODUCTLIST) " );

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put( "PRODUCTLIST", productList );

			List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList( sql.toString(), bindMap );

			if ( sqlResult.size() != 1 )
			{

				throw new CustomException( "LOT-9019", productList );
				
			}
		}
	}
}