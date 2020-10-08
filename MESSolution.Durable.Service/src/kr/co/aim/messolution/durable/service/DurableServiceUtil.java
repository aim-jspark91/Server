/**
 *
 */
package kr.co.aim.messolution.durable.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.event.TerminalMessageSend;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author sjlee
 *
 */
public class DurableServiceUtil implements ApplicationContextAware
{

	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd
	 */
	private ApplicationContext		applicationContext;
	private static Log				log = LogFactory.getLog("DurableServiceUtil");

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	@Override
    public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}

	/*
	* Name : generateCarrierName
	* Desc : This function is Create NameList NanaTrack API Call generateName
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public List<String>  generateCarrierName(String ruleName,
			String durableSpecName,
			String quantity)
	{
		List<String> argSeq = new ArrayList<String>();

		String sql = "SELECT SQL_TEXT " +
					 "FROM NAMEGENERATORRULEDEF " +
				     "WHERE RULENAME = :ruleName ";

		Map<String, String> bindMap = new HashMap<String, String>();

		bindMap.put("ruleName", ruleName);

//		List<Map<String, Object>> sqlResult
//		  = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		String sSqlText = "";

	    if( sqlResult.size()> 0 ) {
	    	sSqlText = sqlResult.get(0).get("SQL_TEXT").toString();
	    } else {

	    }

		bindMap.clear();
		sqlResult.clear();
		bindMap = new HashMap<String, String>();
		bindMap.put("durableSpecName", durableSpecName);

		//sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sSqlText, bindMap);

		sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);



		for( int i = 0; i < sqlResult.size(); i++ ) {

			argSeq.add(sqlResult.get(i).get("ENUMVALUE").toString());
		}

		return argSeq;
	}

	/*
	* Name : getDurableData
	* Desc : This function is Durable Data Return
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public Durable getDurableData(String durableName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try{
			DurableKey durableKey = new DurableKey();
			durableKey.setDurableName(durableName);
			Durable durableData = null;
			durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);

			return durableData;
		} catch ( Exception e ){
			throw new CustomException("CST-0017", durableName);
		}
	}

	/*
	* Name : getBoxData
	* Desc : This function is Durable Data Return
	* Author : AIM Systems, Inc
	* Date : 2016.08.30
	*/
	public Durable getBoxData(String durableName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try{
			DurableKey durableKey = new DurableKey();
			durableKey.setDurableName(durableName);
			Durable durableData = null;
			durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);

			return durableData;
		} catch ( Exception e ){
			throw new CustomException("CST-0025", durableName);
		}
	}

	public void updateCarrierLocation(EventInfo eventInfo, String currentMachineName, String currentPositionName, String transferState, String carrierName)
	{
		try{
			if(StringUtils.isNotEmpty(currentMachineName)&&null!=currentMachineName&&currentMachineName.contains("FFBC05")&&null!=carrierName&&StringUtils.isNotEmpty(carrierName))
			{
				DurableKey durableKey = new DurableKey();

				durableKey.setDurableName(carrierName);

				Durable durableData = DurableServiceProxy.getDurableService()
						.selectByKey(durableKey);

				Map<String,String> durableUdfs = durableData.getUdfs();

				durableUdfs.put("MACHINENAME", currentMachineName);
				durableUdfs.put("POSITIONNAME", currentPositionName);
				durableUdfs.put("TRANSPORTSTATE", transferState);

				SetEventInfo setEventInfo = new SetEventInfo();

				setEventInfo.setUdfs(durableUdfs);

				DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo,
						setEventInfo);
				log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
						+ eventInfo.getEventTimeKey());

			}
		}
		catch(Exception e){

		}
	}



	/**
	* Name : getMaskListByCST
	* Desc : This function is getMaskListByCST
	* Author : jhlee
	* Date : 2016.05.05
	*/
	public List<Durable> getMaskListByCST(String machineName, String portName,String carrierName)
			throws CustomException
	{
		String condition = "WHERE maskcarrierName =? order by maskPosition";

		Object[] bindSet = new Object[] {carrierName,};
		List<Durable> maskList = new ArrayList<Durable>();

		try
		{
			maskList = DurableServiceProxy.getDurableService().select(condition, bindSet);
		}
		catch(NotFoundSignal nf)
		{
			//maskList = null;

			return maskList;
		}
		catch(greenFrameDBErrorSignal de)
		{
			//maskList = null;

			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new NotFoundSignal(de.getDataKey(),de.getSql());
			}else
			{
				throw new CustomException("SYS-8001",de.getSql());
			}
		}
		return maskList;
	}


	/**
	* Name : validateCSTInPort
	* Desc : This function validate CST in port
	* Author : jhlee
	* Date : 2016.05.10
	*/
	public void validateCSTInPort(String machineName, String portName,String carrierName)
			throws CustomException
	{
		if (!portName.isEmpty())
		{
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);

			//validate CST in port
			//PL: CST w/ maskQTY = 0
			if (portData.getUdfs().get("PORTTYPE").equals("PL"))
				throw new CustomException("PORT-0001", portName, carrierName);

			//PU: CST w/ maskQTY > 0
			if (portData.getUdfs().get("PORTTYPE").equals("PU"))
				throw new CustomException("PORT-0002", portName, carrierName);
		}
	}

	/**
	 * getChamberID
	 * @author LHKIM
	 * @since 2015.01.22
	 * @param clusterNo
	 * @return String
	 */
	public String getChamberID(int clusterNo)
	{
		String tmpChamberID =null;

		switch (clusterNo)
        {
            case 1 ://C1 is Chamber1

            	tmpChamberID = "1EVA01-EVA-MS1";

                break;
            case 2://C2 is Chamber2

            	tmpChamberID = "1EVA01-EVA-MS2";

                break;
            case 3 ://C3 is Chamber3

            	tmpChamberID = "1EVA01-EVA-MS3";

                break;
            case 4 ://C4 is Chamber4

            	tmpChamberID = "1EVA01-EVA-MS4";

                break;
            case 5 ://C5 is Chamber5

            	tmpChamberID = "1EVA01-EVA-MS5";

                break;
            case 6 ://C6 is Chamber6

            	tmpChamberID = "1EVA01-EVA-MS6";

                break;
            case 7 ://C7 is Chamber7

            	tmpChamberID = "1EVA01-EVA-MS7";

                break;
            default:
                break;
        }
		return tmpChamberID;
	}

	/**
	 * checkExistMaskList
	 * @author Lhkim
	 * @since 2015.03.03
	 * @param maskCarrierName
	 * @return boolean
	 * @throws CustomException
	 */
	public List<Durable> checkExistMaskList(String maskCarrierName,String durabletype) throws CustomException
	{
		List<Durable> maskList;

		String condition = "WHERE maskCarrierName = ?" + "AND durableType = ?";

		Object[] bindSet = new Object[] { maskCarrierName,durabletype};

	 try
	 {
		  maskList =  DurableServiceProxy.getDurableService().select(condition, bindSet);

	 }
	 catch (Exception ex)
	 {
		 maskList =null;
	 }
	 log.info("maskList.Size is  "+maskList.size());
	 return maskList;

	 }

	/**
	* Name : getMaskSlotMapInfo
	* Desc : This function is getSlotMapInfo
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static String getMaskSlotMapInfo(Durable durableData, List<Durable> maskList ){
		String normalSlotInfo = "";

		StringBuffer normalSlotInfoBuffer = new StringBuffer();

		// Get Durable's Capacity
		long iCapacity = durableData.getCapacity();

		// Get Product's Slot , These are not Scrapped Product.
		// Make Durable Normal SlotMapInfo
		for( int i = 0; i < iCapacity; i++ ){
			normalSlotInfoBuffer.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
		}
		log.debug("Normal Slot Map : " + normalSlotInfoBuffer );

		if (maskList != null)
		{
			for( int i = 0; i < maskList.size(); i++ )
			{
				if(StringUtils.isNotEmpty(maskList.get(i).getUdfs().get("MASKPOSITION")))
				{
					int index = Integer.parseInt(maskList.get(i).getUdfs().get("MASKPOSITION"))- 1;
					normalSlotInfoBuffer.replace(index, index+1, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
				}
				else
				{
					log.error("MaskPostion is Null ! MaskName : "+ maskList.get(i).getKey().getDurableName());
				}
			}
		}

		log.debug("Completed Slot Map : " + normalSlotInfoBuffer );

		normalSlotInfo = normalSlotInfoBuffer.toString();

		return normalSlotInfo;
	}

	/**
	* Name : getMaskInfoBydurableName
	* Desc : This function is getLotInfoBydurableName
	* Author : AIM Systems, Inc
	* Date : 2011.03.07
	*/
	public static List<Durable> getMaskInfoBydurableName(String carrierName,String durabletype) throws CustomException{
		//log.info("START getLotInfoBydurableName");
		String condition = "WHERE durabletype = ? and MASKCARRIERNAME = ? and DURABLESTATE <> ? order by durableName";

		Object[] bindSet = new Object[] {durabletype, carrierName, GenericServiceProxy.getConstantMap().Dur_Scrapped};
		List<Durable> maskList = new ArrayList<Durable>();
		try
		{
			maskList = DurableServiceProxy.getDurableService().select(condition, bindSet);
		}
		catch(greenFrameDBErrorSignal de)
		{
			maskList = null;
			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new NotFoundSignal(de.getDataKey(),de.getSql());
			}else
			{
				throw new CustomException("SYS-8001",de.getSql());
			}
		}
		//log.info("END getLotInfoBydurableName");
		return maskList;
	}

	/**
	 * 160914 by swcho : requested by CIM
	 * request by CIM
	 * @author swcho
	 * @since 2016.09.07
	 * @param factoryName
	 * @param machineName
	 * @param portName
	 * @return
	 * @throws CustomException
	 */
	public String getCarrierNameFromTransportJobV2(String factoryName, String machineName, String portName)
		throws CustomException
	{
		String carrierName = "";

		try
		{
			try
			{
				log.info("Start to sleep to find CST");
				Thread.sleep(3000);
				log.info("Stop to sleep to find CST");
			}
			catch(InterruptedException e)
			{
				log.error(e.getMessage());
			}

			String condition = "transportState = ? AND machineName = ? AND portName = ? ORDER BY lastEventTimekey DESC ";

			List<Durable> result = DurableServiceProxy.getDurableService().select(condition, new Object[] {GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP, machineName, portName});

			carrierName = result.get(0).getKey().getDurableName();
		}
		catch (NotFoundSignal ne)
		{
			//throw new CustomException("SYS-9999", "CST", "Not found CST on loading");

			log.info("Retry to find CST");

			try
			{
				log.info("Start to sleep to find CST.");
				Thread.sleep(12000);
				log.info("Stop to sleep to find CST");
			}
			catch(InterruptedException e)
			{
				log.error(e.getMessage());
			}

			try
			{
				String condition = "transportState = ? AND machineName = ? AND portName = ? ORDER BY lastEventTimekey DESC ";

				List<Durable> result = DurableServiceProxy.getDurableService().select(condition, new Object[] {GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP, machineName, portName});

				carrierName = result.get(0).getKey().getDurableName();
			}
			catch (Exception ex)
			{
				throw new CustomException("SYS-9999", "CST", "Not found CST on loading");
			}
		}

		return carrierName;
	}


	/**
	* Name : getMaskListByEQP
	* Desc : This function is returns mask list in EQP
	* Author : AIM -jhlee
	* Date : 2016.06.02
	*/
	public static List<Durable> getMaskListByEQP(String machineName) throws CustomException{

		List<Durable> maskList = new ArrayList<Durable>();

		try
		{
			maskList = DurableServiceProxy.getDurableService().select("machineName = ? order by durableName", new Object[] { machineName });
		}
		catch (NotFoundSignal nf)
		{
			//mask not found in EQP
			throw new CustomException("MASK-0074", machineName);
		}
		catch (FrameworkErrorSignal fe)
		{
			maskList = new ArrayList<Durable>();
			throw new CustomException("SYS-9999", "EVAMask", fe.getMessage());
		}

		return maskList;
	}

	/**
	 * @desc Check if POSMachine recipe matches any mask recipes in TRK EQP
	 * @author AIM - jhlee
	 * @since 2016.04.12
	 * @param machineName
	 * @param machineRecipeName
	 * @param lotName
	 * @throws CustomException
	 */
	public void validateRecipeMapping(String machineName, String POSMachineReipce, String lotName) throws CustomException
	{
		MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);

		// photo machine
		if (CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").toString().equals("EXP"))
		{
			List <Durable> maskList = new ArrayList <Durable>();

			boolean isEqualRecipe = false;

			try
			{
				maskList = DurableServiceProxy.getDurableService().select("machinename= ? AND durabletype = ?", new Object[] {machineName, "PhotoMask"});

				if (maskList.size() > 0)
				{
					isEqualRecipe = true;
					/**
					 * 20180208 PhotoMask & Recipe Mapping Check (Need to modify) : NJPARK
					for (Durable mask : maskList)
					{
						if (POSMachineReipce.equals(CommonUtil.getValue(mask.getUdfs(), "MACHINERECIPENAME")))
						{
							isEqualRecipe = true;
							break;
						}
					}

					if (!isEqualRecipe)
						throw new CustomException("MASK-0068", machineName);

					*/
				}
			}
			catch (NotFoundSignal ne)
			{
				//mask not found in EQP
				throw new CustomException("MASK-0069", machineName);
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("SYS-9999", "EVAMask", fe.getMessage());
			}
		}
	}

	/**
	 * @desc checkMaskSpec
	 * @author zhongsl
	 * @since 20170319
	 * @param machineName
	 * @param maskSpec
	 * @throws CustomException
	 */
	public void checkMaskSpec(String machineName, String maskSpec) throws CustomException
	{
		try
		{
			StringBuffer sqlBuffer = new StringBuffer("")
			.append("   SELECT D.DURABLENAME           ")
			.append("   FROM DURABLE D                 ")
			.append("   WHERE D.MACHINENAME = ?        ")
			.append("	AND D.DURABLESPECNAME = ?      ");

			String qryString = sqlBuffer.toString();
			Object[] bindSet = new String[] {machineName,maskSpec};
			List<ListOrderedMap> maskListResult = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString, bindSet);

			if(maskListResult.size()<1)
			{
				throw new CustomException("MASK-0083",maskSpec);
			}
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MASK-0083", maskSpec);
		}
	}

	/**
	 * checkExistMaskPosition
	 * @author zhongsl
	 * @since 2017.07.31
	 * @param machineName
	 * @param maskPosition
	 * @return
	 * @throws CustomException
	 */
	public void checkExistPosition(String machineName,String maskPosition, String durableType) throws CustomException
	{

		String condition = "machineName = ? AND maskPosition = ? "
	    + "AND maskPosition is not null AND DURABLETYPE = ? ORDER BY lastEventTimekey DESC ";

		Object[] bindSet = new Object[] {machineName,maskPosition,durableType};

		try
		{
			List <Durable> sqlResult = DurableServiceProxy.getDurableService().select(condition, bindSet);
			if(sqlResult.size() > 0)
			{
				throw new CustomException("MASK-0091",durableType, machineName,maskPosition);
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
	 * checkExistMaskPosition
	 * @author zhongsl
	 * @since 2017.07.31
	 * @param machineName
	 * @param maskPosition
	 * @return
	 * @throws CustomException
	 */
	public void checkExistPositionForProbe(String machineName, String unitName, String maskPosition, String durableType) throws CustomException
	{

		String condition = "machineName = ? and UnitName = ? AND maskPosition = ? "
	    + "AND maskPosition is not null AND DURABLETYPE = ? ORDER BY lastEventTimekey DESC ";

		Object[] bindSet = new Object[] {machineName, unitName, maskPosition,durableType};

		try
		{
			List <Durable> sqlResult = DurableServiceProxy.getDurableService().select(condition, bindSet);
			if(sqlResult.size() > 0)
			{
				throw new CustomException("PROBE-0005",durableType, machineName,unitName, maskPosition);
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
	 * @desc Check if POSMASK MaskBarcodes are in the  durableName list when it is a TRK EQP
	 * @author zhongsl
	 * @since 2016.11.29
	 * @param machineName
	 * @param lotData
	 * @param carrierName
	 * @return
	 * @throws CustomException
	 */
	public void validateMaskMapping(String machineName, Lot lotData,String carrierName) throws CustomException
	{
		if(!ExtendedObjectProxy.getFirstGlassLotService().getActiveLotNameByCarrier(carrierName).isEmpty())
		{
			String firstGlassLotKey = ExtendedObjectProxy.getFirstGlassLotService().getActiveLotNameByCarrier(carrierName);
			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(firstGlassLotKey);
		}

		MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
		List<ListOrderedMap> lPosMaskBarcodeIDList = PolicyUtil.getTPFOMPolicyMaskList(lotData.getProductSpecName(),lotData.getProcessFlowName(),lotData.getProcessOperationName(),machineName);
		int maskQty = 0;
		// photo machine
		if (CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").toString().equals("EXP"))
		{
			String barcodeList = "";
			List<ListOrderedMap> maskList = new ArrayList <ListOrderedMap>();
			try
			{
				StringBuffer sqlBuffer = new StringBuffer("")
				.append("SELECT D.DURABLENAME")
				.append("  FROM DURABLE D")
				.append(" WHERE 1 = 1")
				.append("   AND D.DURABLETYPE = ?")
				.append("	AND D.TRANSPORTSTATE = ?")
				.append("   AND D.MACHINENAME  = ?");

				String qryString = sqlBuffer.toString();
				Object[] bindSet = new String[] {"PhotoMask", machineName, "INLINE"};
				maskList = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString, bindSet);

				if (maskList.size() > 0)
				{
					for(int i=0;i<lPosMaskBarcodeIDList.size();i++)
					{
						for(int j=0; j< maskList.size();j++)
						{
							String posMask = lPosMaskBarcodeIDList.get(i).getValue(0).toString();
							String cim_maskHoldState = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(posMask).getUdfs().get("DURABLEHOLDSTATE");

							if(StringUtil.equalsIgnoreCase("Y", cim_maskHoldState))
							{
								throw new CustomException("MASK-0013",posMask);
							}

							if (posMask.equals(maskList.get(j).getValue(0).toString()))
							{
								maskQty+= 1;
							}
						}

						barcodeList +=lPosMaskBarcodeIDList.get(i).getValue(0).toString() + " ";
					}
				}

				if(maskQty==lPosMaskBarcodeIDList.size())
				{
					return;
				} else
					throw new CustomException("MASK-0082", machineName,lotData.getProductSpecName(),lotData.getProcessFlowName(),lotData.getProcessOperationName(),barcodeList);
			}
			catch (NotFoundSignal ne)
			{
				//mask not found in EQP
				throw new CustomException("MASK-0028",machineName);
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("SYS-9999", "PhotoMask", fe.getMessage());
			}
		}
	}


	public void validateMaskMappingExceptPhotoMask(String machineName, Lot lotData) throws CustomException
	{
		String enumName = "MaskPosition";
		String description = "MaskMachineSlotNo.";
		ArrayList<String> arrEmptyPosition = new ArrayList<String>();

		List<Map<String, Object>> sqlResult = CommonUtil.getEnumDefValueByEnumNameAndDescription(enumName, description);

		List<ListOrderedMap> maskList = new ArrayList <ListOrderedMap>();

		for(int i=0;i<sqlResult.size();i++)
		{
			StringBuffer sqlBuffer = new StringBuffer("")
			.append("   SELECT D.DURABLENAME                  ")
			.append("   FROM DURABLE D                        ")
			.append("   WHERE 1 = 1                           ")
			.append("	AND D.DURABLETYPE = :durableType      ")
			.append("	AND D.TRANSPORTSTATE = :transportState ")					// ONEQP? or INLINE?
			.append("   AND D.MACHINENAME  = :machinename     ")
			.append("   AND D.MASKPOSITION  = :positionname   ");

			String qryString = sqlBuffer.toString();

			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("durableType" , "PhotoMask");
			bindMap.put("transportState" , "ONEQP");
			bindMap.put("machinename" , machineName);
			bindMap.put("positionname", sqlResult.get(i).get("ENUMVALUE").toString());

			maskList = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString, bindMap);

			if(maskList.size()==0)
			{
				arrEmptyPosition.add(sqlResult.get(i).get("ENUMVALUE").toString());
			}
		}

		if(arrEmptyPosition.size()>0)
		{
			String emptyPositionList = "";

			for(int i=0;i<arrEmptyPosition.size();i++)
			{
				emptyPositionList += "[" + arrEmptyPosition.get(i).toString() + "]";
			}

			throw new CustomException("MASK-0086", machineName, emptyPositionList);
		}
		else
		{
			return;
		}
	}

	/**
	 * @author smkang
	 * @since 2018.05.02
	 * @param machineName
	 * @return List<Durable>
	 * @see Returns carriers at this machine except exceptCarrierNames.
	 */
	public static List<Durable> getCarrierListByEQP(String machineName, List<String> exceptCarrierNames) {

		List<Durable> carrierList = new ArrayList<Durable>();

		try {
			if (exceptCarrierNames != null && exceptCarrierNames.size() > 0) {
				Object[] bindSet = new Object[exceptCarrierNames.size() + 1];
				bindSet[0] = machineName;

				for (int index = 0; index < exceptCarrierNames.size(); index++) {
					bindSet[index + 1] = exceptCarrierNames.get(index);
				}

				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				String condition = "machineName = ? and durableName not in (" + StringUtils.removeEnd(StringUtils.repeat("?,", exceptCarrierNames.size()), ",") + ")";
				String condition = "machineName = ? and durableName not in (" + StringUtils.removeEnd(StringUtils.repeat("?,", exceptCarrierNames.size()), ",") + ") for update";

				carrierList = DurableServiceProxy.getDurableService().select(condition, bindSet);
			} else {
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				carrierList = DurableServiceProxy.getDurableService().select("machineName = ?", new Object[] {machineName});
				carrierList = DurableServiceProxy.getDurableService().select("machineName = ? for update", new Object[] {machineName});
			}
		} catch (Exception e) {
			log.info(e);
		}

		return carrierList;
	}

	public void probeCheckByDurableSpec(String probeCardType) throws CustomException
	{
		log.info("probeCheckByDurableSpec Started.");

		boolean checkResult = false ;//means no type

		String condition = "WHERE DURABLETYPE = ? " ;

		Object[] bindSet = new Object[]{GenericServiceProxy.getConstantMap().DURABLETYPE_PBOBECARD};

		List<DurableSpec> durableSpecList = DurableServiceProxy.getDurableSpecService().select(condition, bindSet);

		for(DurableSpec durableSpec : durableSpecList )
		{
			if(StringUtil.equals(probeCardType, durableSpec.getKey().getDurableSpecName()))
			{
				checkResult = true;
				break;
			}
		}

		if(checkResult == false)
			throw new CustomException("PROBECARD-0006",probeCardType);

		log.info("probeCheckByDurableSpec Ended.");
	}

	public void probeCheckByTPEFOMPolicy(String probeCardType, String machineName, String machineRecipeName) throws CustomException
	{
		log.info("probeCheckByTPEFOMPolicy Started.");

		//String strSql ="SELECT * " +
		//				"  FROM TPEFOMPOLICY T " +
		//				" WHERE     T.FACTORYNAME = :FACTORY " +
		//				"       AND T.MACHINENAME = :MACHINENAME " +
		//				"       AND T.MACHINERECIPENAME = :MACHINERECIPENAME " +
		//				"       AND T.PROBECARD = :PROBECARD " ;
		
		String strSql= StringUtil.EMPTY;
		strSql = strSql + " SELECT *                                                          \n";
		strSql = strSql + "    FROM TPEFOMPOLICY T                                            \n";
		strSql = strSql + "    	INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID      \n";
		strSql = strSql + "   WHERE 1 = 1                                                     \n";
		strSql = strSql + "     AND ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = '*')) \n";
		strSql = strSql + "     AND ((T.MACHINENAME = :MACHINENAME) OR (T.MACHINENAME = '*')) \n";
		strSql = strSql + "     AND P.MACHINERECIPENAME = :MACHINERECIPENAME                  \n";
		strSql = strSql + "     AND P.PROBECARD = :PROBECARD                                  \n";
		strSql = strSql + " ORDER BY DECODE (FACTORYNAME, '*', 9999, 0),                      \n";
		strSql = strSql + "          DECODE (PRODUCTSPECNAME, '*', 9999, 0),                  \n";
		strSql = strSql + "          DECODE (PRODUCTSPECVERSION, '*', 9999, 0),               \n";
		strSql = strSql + "          DECODE (ECCODE, '*', 9999, 0),                           \n";
		strSql = strSql + "          DECODE (PROCESSFLOWNAME, '*', 9999, 0),                  \n";
		strSql = strSql + "          DECODE (PROCESSFLOWVERSION, '*', 9999, 0),               \n";
		strSql = strSql + "          DECODE (PROCESSOPERATIONNAME, '*', 9999, 0),             \n";
		strSql = strSql + "          DECODE (PROCESSOPERATIONVERSION, '*', 9999, 0),          \n";
		strSql = strSql + "          DECODE (MACHINENAME, '*', 9999, 0)                       \n";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", GenericServiceProxy.getConstantMap().DEFAULT_FACTORY);
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("MACHINERECIPENAME", machineRecipeName);
		bindMap.put("PROBECARD", probeCardType);

       List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

		if ( tpefomPolicyData.size() < 0 )
		{
			throw new CustomException("POLICY-0014",GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,machineName,machineRecipeName,probeCardType);
		}

		log.info("probeCheckByTPEFOMPolicy Ended.");
	}
	
	/**
	 * 
	 * @Name     probeCheckByTPEFOMPolicy
	 * @since    2018. 9. 25.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param probeCardType
	 * @param machineName
	 * @param machineRecipeName
	 * @throws CustomException
	 */
	public void probeCheckByTPEFOMPolicyNotMachine(String probeCardType, String machineName, String machineRecipeName) throws CustomException
    {
        log.info("probeCheckByTPEFOMPolicyNotMachine Started.");

        String strSql= StringUtil.EMPTY;
        strSql = strSql + " SELECT *                                                          \n";
        strSql = strSql + "    FROM TPEFOMPOLICY T                                            \n";
        strSql = strSql + "    INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID       \n";
        strSql = strSql + "   WHERE 1 = 1                                                     \n";
        strSql = strSql + "     AND ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = '*')) \n";
        strSql = strSql + "     AND P.MACHINERECIPENAME = :MACHINERECIPENAME                  \n";
        strSql = strSql + "     AND P.PROBECARD = :PROBECARD                                  \n";
        strSql = strSql + " ORDER BY DECODE (FACTORYNAME, '*', 9999, 0),                      \n";
        strSql = strSql + "          DECODE (PRODUCTSPECNAME, '*', 9999, 0),                  \n";
        strSql = strSql + "          DECODE (PRODUCTSPECVERSION, '*', 9999, 0),               \n";
        strSql = strSql + "          DECODE (ECCODE, '*', 9999, 0),                           \n";
        strSql = strSql + "          DECODE (PROCESSFLOWNAME, '*', 9999, 0),                  \n";
        strSql = strSql + "          DECODE (PROCESSFLOWVERSION, '*', 9999, 0),               \n";
        strSql = strSql + "          DECODE (PROCESSOPERATIONNAME, '*', 9999, 0),             \n";
        strSql = strSql + "          DECODE (PROCESSOPERATIONVERSION, '*', 9999, 0),          \n";
        strSql = strSql + "          DECODE (MACHINENAME, '*', 9999, 0)                       \n";
        
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("FACTORYNAME", GenericServiceProxy.getConstantMap().DEFAULT_FACTORY);
        bindMap.put("MACHINERECIPENAME", machineRecipeName);
        bindMap.put("PROBECARD", probeCardType);

       List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

        if ( tpefomPolicyData.size() <= 0 )
        {
            throw new CustomException("POLICY-0014",GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,machineName,machineRecipeName,probeCardType);
        }

        log.info("probeCheckByTPEFOMPolicyNotMachine Ended.");
    }

	//add by wghuang 20180527
	public List<String> getUnitListByMahcineName(String machineName) throws CustomException
	{
		log.info("getUnitListByMahcineName Started.");

		List<String> UnitList = new ArrayList<String>();

		String strSql = "SELECT MACHINENAME " +
		        "  FROM MACHINESPEC " +
		        " WHERE SUPERMACHINENAME = ? " +
		        "   AND MACHINETYPE = ? " ;

		Object[] bindSet = new String[]{machineName, GenericServiceProxy.getConstantMap().Mac_ProductionMachine};

		List<ListOrderedMap> unitList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindSet);

		if(unitList == null || unitList.size() <= 0)
			throw new CustomException("MACHINE-9001", machineName);

		for(ListOrderedMap list : unitList)
		{
			UnitList.add(list.get("MACHINENAME").toString());
		}

		log.info("getUnitListByMahcineName Ended.");

		return UnitList;
	}

	//add by wghuang 20180527
	public void checkProbeCardCondition(String machineName, String operationMode, boolean TPEFOMPolicyCheckFlag) throws CustomException
	{
		log.info("checkProbeCardCondition Started.");

		//String strSql = "SELECT MACHINENAME " +
		//		        "  FROM MACHINESPEC " +
		//		        " WHERE SUPERMACHINENAME = ? " +
		//		        "   AND MACHINETYPE = ? " ;
		
		String strSql = "SELECT M.MACHINENAME " +
                "  FROM MACHINESPEC MS, MACHINE M " +
                " WHERE MS.SUPERMACHINENAME = ? " +
                "   AND MS.MACHINETYPE = ? "  +
		        "   AND MS.MACHINENAME = M.MACHINENAME "  +
		        "   AND M.COMMUNICATIONSTATE = ? ";

		Object[] bindSet = new String[]{machineName, GenericServiceProxy.getConstantMap().Mac_ProductionMachine, GenericServiceProxy.getConstantMap().Mac_OnLineRemote};

		List<ListOrderedMap> unitList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindSet);

		if(unitList.size() <= 0)
		{
			/* 20181112, hhlee, Modify, Add Error Definition Code ==>> */
		    //throw new CustomException("MACHINE-9001", machineName);
		    throw new CustomException("PROBECARD-0013", machineName);
		    /* <<== 20181112, hhlee, Modify, Add Error Definition Code */
		}

		//DetailCheck
		List<Durable>totalPBList = new ArrayList<Durable>() ;
		List<Durable>PBListU1 = new ArrayList<Durable>();
		List<Durable>PBListU2 = new ArrayList<Durable>() ;
		int count = 0;

		for(ListOrderedMap unit :unitList)
		{
			String unitName = CommonUtil.getValue(unit, "MACHINENAME");

			String sql = " WHERE MACHINENAME = ? AND UNITNAME = ? " ;
			Object[]bindSet2 = new Object[]{machineName, unitName};

			try
			{
    			List<Durable>pbList = DurableServiceProxy.getDurableService().select(sql,bindSet2);
    
    			if(count == 0)
    				PBListU1 = pbList;
    			else
    				PBListU2 = pbList;
    
    			if(pbList.size() > 4)
    				throw new CustomException("PROBECARD-0007", unitName,machineName);
    
    			for(Durable pblist : pbList)
    			{
    				totalPBList.add(pblist);
    			}
    			count ++;
			}
			catch (Exception ex)
			{
			    //throw new CustomException("PROBECARD-0008", machineName);
			    throw new CustomException("PROBECARD-0008", machineName, unitName);
			    //log.info("checkProbeCardCondition Started.");
			    //log.error(String.format("ERRORCODE[%s] No Probecard !! MachineName:[%s]","PROBECARD-0008",machineName));
			}
		}

		//NormalMode has no PBCard
		if(totalPBList.size() <=0)
		{
			throw new CustomException("PROBECARD-0008", machineName, StringUtil.EMPTY);
		    //log.error(String.format("ERRORCODE[%s] No Probecard !! MachineName:[%s]","PROBECARD-0008",machineName));
		}

		//TPEFOMPolicyCheck or Not
		if(TPEFOMPolicyCheckFlag == true)
		{
			boolean existResult = false;

			for(Durable pblist : totalPBList)
			{
				for(ListOrderedMap pbtype : getPBTypeByTPEFOMPolicy())
				{
					if(StringUtil.equals(pblist.getDurableSpecName(), CommonUtil.getValue(pbtype, "PROBECARD")))
					{
						existResult = true;
						break ;
					}
				}

				if(existResult == false)
					throw new CustomException("PROBECARD-0009", machineName,pblist.getDurableSpecName());
			}
		}

		//OperationMode:NORMAL
		if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
		{
			//ProbeCardType in units should be same
			for(int i = 0; i < totalPBList.size(); i++)
			{
				if(i == totalPBList.size() - 1 )
					break;

				for(int j = i+1; j < totalPBList.size(); j++)
				{
					if(!totalPBList.get(i).getDurableSpecName().equals
					(totalPBList.get(j).getDurableSpecName()))
					{
						//throw new CustomException("PROBECARD-0002", machineName, totalPBList.get(i).getDurableSpecName(), totalPBList.get(j).getDurableSpecName());
					    throw new CustomException("PROBECARD-0002", machineName + "-" + totalPBList.get(i).getUdfs().get("UNITNAME"), 
					            totalPBList.get(i).getKey().getDurableName(), totalPBList.get(i).getDurableSpecName(), 
					            totalPBList.get(j).getKey().getDurableName(), totalPBList.get(j).getDurableSpecName());
					}
				}
			}
		}
		else
		{
			//INDP
			//atlist one pbcard on this machine
/*			if(PBListU1.size() <=0 && PBListU2.size() <=0)
				throw new CustomException("PROBECARD-0005", machineName);*/

			//check unit1 probecard condition:should be same type.
			for(int i = 0; i < PBListU1.size(); i++)
			{
				if(i == PBListU1.size() - 1 )
					break;

				for(int j = i+1; j < PBListU1.size(); j++)
				{
					if(!PBListU1.get(i).getDurableSpecName().equals
					(PBListU1.get(j).getDurableSpecName()))
					{
					    //throw new CustomException("PROBECARD-0003",MachineName, PBListU1.get(i).getDurableSpecName(), PBListU1.get(j).getDurableSpecName());
						//throw new CustomException("PROBECARD-0003", machineName + "-" + PBListU1.get(i).getUdfs().get("UNITNAME"), PBListU1.get(i).getDurableSpecName(), PBListU1.get(j).getDurableSpecName());
					    throw new CustomException("PROBECARD-0003", machineName + "-" + PBListU1.get(i).getUdfs().get("UNITNAME"), 
					            PBListU1.get(i).getKey().getDurableName(), PBListU1.get(i).getDurableSpecName(), 
					            PBListU1.get(j).getKey().getDurableName(), PBListU1.get(j).getDurableSpecName());
					}
				}
			}

			//check unit2 probecard condition:should be same type.
			for(int i = 0; i < PBListU2.size(); i++)
			{
				if(i == PBListU2.size() - 1 )
					break;

				for(int j = i+1; j < PBListU2.size(); j++)
				{
					if(!PBListU2.get(i).getDurableSpecName().equals
					(PBListU2.get(j).getDurableSpecName()))
					{
						//throw new CustomException("PROBECARD-0003",machineName, PBListU2.get(i).getDurableSpecName(), PBListU2.get(j).getDurableSpecName());
					    //throw new CustomException("PROBECARD-0003",machineName + "-" + PBListU2.get(i).getUdfs().get("UNITNAME"), PBListU2.get(i).getDurableSpecName(), PBListU2.get(j).getDurableSpecName());
					    throw new CustomException("PROBECARD-0003",machineName + "-" + PBListU2.get(i).getUdfs().get("UNITNAME"), 
					            PBListU2.get(i).getKey().getDurableName(), PBListU2.get(i).getDurableSpecName(), 
					            PBListU2.get(j).getKey().getDurableName(), PBListU2.get(j).getDurableSpecName());
					}
				}
			}
		}

		log.info("checkProbeCardCondition Ended.");
	}
    
	public List<Durable> getPBListByMachine(String machineName) throws CustomException
	{
		log.info("getPBListByMachine Started.");

		String strSql = "SELECT MACHINENAME " +
		        "  FROM MACHINESPEC " +
		        " WHERE SUPERMACHINENAME = ? " +
		        "   AND MACHINETYPE = ? " ;

		Object[] bindSet = new String[]{machineName, GenericServiceProxy.getConstantMap().Mac_ProductionMachine};

		List<ListOrderedMap> unitList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindSet);

		if( unitList == null || unitList.size() <= 0)
			throw new CustomException("MACHINE-9001", machineName);

		//getPBList by UnitList
		String unitlistName = "";
		List<Durable> probeCardList;

		for(ListOrderedMap unit : unitList)
		{
			if(StringUtil.isEmpty(unitlistName))
				unitlistName = "'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
			else
			unitlistName += ",'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
		}

		try
		{
			probeCardList = DurableServiceProxy.getDurableService().select(" WHERE UNITNAME IN (" + unitlistName + ")", null);
		}
		catch(NotFoundSignal ne)
		{
			probeCardList = new ArrayList<Durable>();
		}
        		
		log.info("getPBListByMachine Ended.");

		return probeCardList;
	}
	public String getPBListTypeByMachine(String machineName) throws CustomException
    {
        log.info("getPBListByMachine Started.");

        String strSql = "SELECT MACHINENAME " +
                "  FROM MACHINESPEC " +
                " WHERE SUPERMACHINENAME = ? " +
                "   AND MACHINETYPE = ? " ;

        Object[] bindSet = new String[]{machineName,"ProductionMachine"};

        List<ListOrderedMap> unitList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindSet);

        if( unitList == null || unitList.size() <= 0)
            throw new CustomException("MACHINE-9001", machineName);

        //getPBList by UnitList
        String unitlistName = "";
        List<Durable> probeCardList;
        String probeCardType = StringUtil.EMPTY;
        
        for(ListOrderedMap unit : unitList)
        {
            if(StringUtil.isEmpty(unitlistName))
                unitlistName = "'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
            else
            unitlistName += ",'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
        }

        try
        {
            probeCardList = DurableServiceProxy.getDurableService().select(" WHERE UNITNAME IN (" + unitlistName + ")", null);
            
            probeCardType = probeCardList.get(0).getDurableSpecName();
        }
        catch(NotFoundSignal ne)
        {
            probeCardList = new ArrayList<Durable>();
        }

        log.info("getPBListByMachine Ended.");

        return probeCardType;
    }

	public boolean checkPBCardDuplicationByMachinePBList(String machineName, String PBCardName) throws CustomException
	{
		log.info("checkPBCardDuplicationByMachinePBList Started.");

		boolean DuplicationResult = false ;

		String strSql = "SELECT MACHINENAME " +
		        "  FROM MACHINESPEC " +
		        " WHERE SUPERMACHINENAME = ? " +
		        "   AND MACHINETYPE = ? " ;

		Object[] bindSet = new String[]{machineName, GenericServiceProxy.getConstantMap().Mac_ProductionMachine};

		List<ListOrderedMap> unitList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindSet);

		if(unitList.size() <= 0)
			throw new CustomException("MACHINE-9001", machineName);

		//getPBList by UnitList
		String unitlistName = "";
		List<Durable> probeCardList;

		for(ListOrderedMap unit : unitList)
		{
			if(StringUtil.isEmpty(unitlistName))
				unitlistName = "'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
			else
			unitlistName += ",'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
		}

		try
		{
			probeCardList = DurableServiceProxy.getDurableService().select(" WHERE UNITNAME IN (" + unitlistName + ")", null);
		}
		catch(NotFoundSignal ne)
		{
			//throw new CustomException("PROBECARD-0011",""); /* <<== 20180601, deleted by hhlee */
		    return false;
		}

		for(Durable durable : probeCardList)
		{
			if(StringUtil.equals(PBCardName, durable.getKey().getDurableName()))
			{
				DuplicationResult = true ;
				break;
			}
		}

		log.info("checkPBCardDuplicationByMachinePBList Ended.");

		return DuplicationResult;
	}

	public boolean checkPBCardDuplicationByUnitPBList(String unitName, String PBCardName) throws CustomException
	{
		log.info("checkPBCardDuplicationByUnitPBList Started.");

		boolean DuplicationResult = false;

		String sql = "WHERE UNITNAME = ?" ;
		Object[]bindSet = new Object[]{unitName};

		List<Durable>probeCardList = new ArrayList<Durable>();
		try
		{
			probeCardList = DurableServiceProxy.getDurableService().select(sql,bindSet);
		}
		catch(Exception e)
		{
			throw new CustomException("PROBECARD-0011","");
		}

		for(Durable durable : probeCardList)
		{
			if(StringUtil.equals(PBCardName, durable.getKey().getDurableName()))
			{
				DuplicationResult = true ;
				break;
			}
		}

		log.info("checkPBCardDuplicationByUnitPBList Ended.");

		return DuplicationResult;
	}

	public List<Durable> getPBListByUnitName(String unitName) throws CustomException
	{
		log.info("getPBListByUnitName Started.");

		String sql = "WHERE UNITNAME = ?" ;
		Object[]bindSet = new Object[]{unitName};

		List<Durable>pbList = new ArrayList<Durable>();
		try
		{
			pbList = DurableServiceProxy.getDurableService().select(sql,bindSet);
		}
		catch(Exception e)
		{
			//throw new CustomException("PROBECARD-0011",""); /* <<== 20180601, deleted by hhlee */
		}

		log.info("getPBListByUnitName Ended.");

		return pbList;
	}
	
	public String getPBListTypeByUnitName(String unitName) throws CustomException
    {
        log.info("getPBListByUnitName Started.");

        String sql = "WHERE UNITNAME = ?" ;
        Object[]bindSet = new Object[]{unitName};

        List<Durable>pbList = new ArrayList<Durable>();
        String probeCardType = StringUtil.EMPTY;
        try
        {
            pbList = DurableServiceProxy.getDurableService().select(sql,bindSet);
            probeCardType = pbList.get(0).getDurableSpecName();
        }
        catch(Exception e)
        {
            //throw new CustomException("PROBECARD-0011",""); /* <<== 20180601, deleted by hhlee */
        }

        log.info("getPBListByUnitName Ended.");

        return probeCardType;
    }

	public List<ListOrderedMap>getPBTypeByTPEFOMPolicy() throws CustomException
	{
		log.info("getPBTypeByTPEFOMPolicy Started.");

		String strSql = "SELECT DISTINCT PROBECARD " +
						"  FROM POSRECIPE " +
						" WHERE PROBECARD IS NOT NULL " ;

		Object [] bindSet = new Object[]{};

		List<ListOrderedMap> PBTypeList  = null ;

		try
		{
			PBTypeList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindSet);
		}
		catch(Exception e)
		{
			throw new CustomException("PROBECARD-0011","");
		}

		log.info("getPBTypeByTPEFOMPolicy Ended.");

		return PBTypeList ;
	}

	//add by wghuang 20180528
	public boolean checkPhotoMaskDuplicationByMaskName(String machineName, String maskName)throws CustomException
	{
		log.info("checkPhotoMaskDuplicationByMaskName Started.");

		boolean kitFlag = false;

		String sql = "WHERE MACHINENAME = ? AND DURABLESTATE IN(?,?) AND DURABLENAME = ? " ;

		Object[]bindSet = new Object[]{machineName,GenericServiceProxy.getConstantMap().Cons_Mount,GenericServiceProxy.getConstantMap().Cons_InUse,maskName};

		List<Durable>PhotoMaskList = new  ArrayList<Durable>();

		try
		{
			PhotoMaskList = DurableServiceProxy.getDurableService().select(sql,bindSet);
		}
		catch(Exception e)
		{
			log.warn(e.getMessage());
		}

		if(PhotoMaskList.size() >= 1)
			kitFlag = true;

		log.info("checkPhotoMaskDuplicationByMaskName Ended.");

		return kitFlag;
	}

	public List<Durable> getPhotoMaskNameByMachineName(String machineName)throws CustomException
	{
		log.info("getPhotoMaskNameByMachineName Started.");

		/* 20181016, hhlee, add,  */
		//String sql = "WHERE MACHINENAME = ? AND DURABLETYPE = ? AND DURABLENAME <> 'NONE'" ;
		/* 20190523, hhlee, add, ORER BY MASKPOSITION */
		String sql = "WHERE MACHINENAME = ? AND DURABLETYPE = ? ORDER BY MASKPOSITION " ;
		Object[]bindSet = new Object[]{machineName,GenericServiceProxy.getConstantMap().DURABLETYPE_PHOTOMASK};

		List<Durable>PhotoMaskList = new  ArrayList<Durable>();
		try
		{
			PhotoMaskList = DurableServiceProxy.getDurableService().select(sql,bindSet);
		}
		catch(Exception e)
		{
			log.warn(e.getMessage());
		}

		if(PhotoMaskList.size() > 6)
			throw new CustomException("MASK-0092",machineName);

		/* 20190523, hhlee, add, validate duplicate Mask Position ==>> */
		//this.validateDuplicateMaskPosition(PhotoMaskList);
		/* <<== 20190523, hhlee, add, validate duplicate Mask Position */
		
		log.info("getPhotoMaskNameByMachineName Ended.");

		return PhotoMaskList;
	}

	public boolean checkPhotoMaskStateByMachineName(String machineName)throws CustomException
	{
		log.info("checkPhotoMaskStateByMachineName Started.");
		//true : already have 1
		//false : there is no mask on machine

		boolean PhotoMaskStateFlag = false;

		/* 2190516, hhlee, modify, add DURABLETYPE */
		String sql = "WHERE MACHINENAME = ? AND DURABLESTATE = ? AND DURABLETYPE = ? " ;
		Object[]bindSet = new Object[]{machineName,GenericServiceProxy.getConstantMap().Cons_InUse, GenericServiceProxy.getConstantMap().DURABLETYPE_PHOTOMASK};

		List<Durable>PhotoMaskList = new ArrayList<Durable>();
		try
		{
			PhotoMaskList = DurableServiceProxy.getDurableService().select(sql,bindSet);
		}
		catch(Exception e)
		{
			//throw new CustomException("checkPhotoMaskStateByMachineName Failed!","");
			log.warn(e.getMessage());
		}

		if(PhotoMaskList.size() >= 1)
			PhotoMaskStateFlag = true;

		log.info("checkPhotoMaskStateByMachineName Ended.");

		return PhotoMaskStateFlag;
	}

	//add by wghuang 20180529
	public void checkExistenceByMachineNamePosition(EventInfo eventInfo, String machineName, String position,String transferState)throws CustomException
	{
		log.info("checkExistenceByMachineNamePosition Started.");

		String strSql = "";

		Object [] bindSet = new Object[]{};

		if(StringUtil.equals(transferState,  GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_PROCESSING))
		{
		    /* 2190516, hhlee, modify, add DURABLETYPE */
			 strSql = "WHERE MACHINENAME = ?  AND DURABLESTATE = ?  AND DURABLETYPE = ? ";

			bindSet = new Object[]{machineName,GenericServiceProxy.getConstantMap().Cons_InUse, GenericServiceProxy.getConstantMap().DURABLETYPE_PHOTOMASK};
		}
		else
		{
		    /* 2190516, hhlee, modify, add DURABLETYPE */
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//	        strSql = "WHERE MACHINENAME = ? AND MASKPOSITION = ? AND DURABLETYPE = ? AND TRANSPORTSTATE = ?";
	        strSql = "WHERE MACHINENAME = ? AND MASKPOSITION = ? AND DURABLETYPE = ? AND TRANSPORTSTATE = ? FOR UPDATE";
	        
			bindSet = new Object[]{machineName,position,GenericServiceProxy.getConstantMap().DURABLETYPE_PHOTOMASK, 
			        GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE};
		}


		List<Durable>PhotoMaskList = new ArrayList<Durable>();

		try
		{
			PhotoMaskList = DurableServiceProxy.getDurableService().select(strSql,bindSet);
		}
		catch(Exception e)
		{
			log.warn(e.getMessage());
		}

		if(PhotoMaskList.size() > 0)
		{
			//and change mask state to UNMOUNT/OUTSTK
			for(Durable durable : PhotoMaskList)
			{
				// Modified by smkang on 2018.09.25 - Available state is used instead of UnMount state.
//				durable.setDurableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
				durable.setDurableState(GenericServiceProxy.getConstantMap().Cons_Available);

				DurableServiceProxy.getDurableService().update(durable);

				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("MACHINENAME", "");
				udfs.put("UNITNAME", "");
				udfs.put("MASKPOSITION", "");
				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);

				// SetEvent Info create
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.setUdfs(udfs);

				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durable, setEventInfo, eventInfo);
			}
		}

		log.info("checkExistenceByMachineNamePosition Ended.");

	}

	/**
	 * 
	 * @Name     checkExistenceByMachineNamePosition
	 * @since    2018. 9. 22.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param eventInfo
	 * @param machineName
	 * @param position
	 * @param transferState
	 * @throws CustomException
	 */
    public boolean checkExistenceCountByMachineName(String machineName, String unitName)throws CustomException
    {
        boolean checkResult = false;
        
        /* 2190516, hhlee, modify, add DURABLETYPE */
        String strSql =  " WHERE MACHINENAME = ? AND UNITNAME = ? AND DURABLESTATE IN ( ?, ?) AND DURABLETYPE = ?  ";

        Object [] bindSet = new Object[]{machineName, unitName, 
                GenericServiceProxy.getConstantMap().Cons_Mount, GenericServiceProxy.getConstantMap().Cons_InUse, GenericServiceProxy.getConstantMap().DURABLETYPE_PHOTOMASK};

        List<Durable>PhotoMaskList = new ArrayList<Durable>();

        try
        {
            PhotoMaskList = DurableServiceProxy.getDurableService().select(strSql,bindSet);
        }
        catch(Exception e)
        {
            log.warn(e.getMessage());
        }

        if(PhotoMaskList.size() >= 5)
        {
            checkResult = true;
        }
        
        return checkResult;
    }
    
    public void checkExistenceByPhotoMaskPosition(EventInfo eventInfo, String machineName, String unitName, String position)
    {
        log.info("checkExistenceByPhotoMaskPosition Started.");

        // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        String strSql = "WHERE MACHINENAME = ? AND UNITNAME = ? AND MASKPOSITION = ? AND TRANSPORTSTATE = ?";
        String strSql = "WHERE MACHINENAME = ? AND UNITNAME = ? AND MASKPOSITION = ? AND TRANSPORTSTATE = ? FOR UPDATE";

        Object [] bindSet = new Object[]{machineName,unitName,position, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK};
        
        List<Durable>PhotoMaskList = new ArrayList<Durable>();

        try
        {
            PhotoMaskList = DurableServiceProxy.getDurableService().select(strSql,bindSet);
        }
        catch(Exception e)
        {
            log.warn(e.getMessage());
        }

        if(PhotoMaskList.size() > 0)
        {
            eventInfo.setEventName("PhotoMaskTakeOut");
            eventInfo.setEventComment("Material in same locatioin!!! - OUTSTK ");
            
            // 2018.05.15_hsryu_Delete Logic. Move Logic. "PhotoMaskTakeIn"
            //PhtMaskStocker phtMaskStockerData = null;
            
            //and change mask state to UNMOUNT/OUTSTK
            for(Durable durable : PhotoMaskList)
            {
                durable.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);

                DurableServiceProxy.getDurableService().update(durable);

                Map<String, String> udfs = new HashMap<String, String>();
                udfs.put("MACHINENAME", "");
                udfs.put("UNITNAME", "");
                udfs.put("MASKPOSITION", "");
                udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);

                // SetEvent Info create
                SetEventInfo setEventInfo = new SetEventInfo();
                setEventInfo.setUdfs(udfs);

                MESDurableServiceProxy.getDurableServiceImpl().setEvent(durable, setEventInfo, eventInfo);
                
                // 2018.05.15_hsryu_Delete Logic. Move Logic. "PhotoMaskTakeIn"
//                try
//                {
//                    phtMaskStockerData = ExtendedObjectProxy.getPhtMaskStockerService().selectByKey(false, new Object[] {machineName,unitName,position});
//                    
//                    phtMaskStockerData.setCurrentMaskName(StringUtil.EMPTY);
//                    phtMaskStockerData.setCurrentInTime(null);
//                    
//                    phtMaskStockerData.setLastOutMaskName(durable.getKey().getDurableName());
//                    phtMaskStockerData.setLastOutTime(eventInfo.getEventTime());
//                    
//                    phtMaskStockerData = ExtendedObjectProxy.getPhtMaskStockerService().modify(eventInfo, phtMaskStockerData);
//                }
//                catch(Exception e)
//                {
//                    log.warn(e.getMessage());
//                }
                
                eventInfo.setCheckTimekeyValidation(false);
                eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
            }
        }

        log.info("checkExistenceByPhotoMaskPosition Ended.");
    }
    
    
	/**
	 * @Name     setCarrierHold
	 * @since    2018. 5. 31.
	 * @author
	 * @contents
	 * @param durableData
	 * @param eventName
	 * @param eventUser
	 * @param eventComment
	 * @param reasonCodeType
	 * @param reasonCode
	 */
	public void setCarrierHold(Durable durableData, String eventName, String eventUser, String eventComment, String reasonCodeType, String reasonCode) throws CustomException
    {
	    log.info("Carrier Hold Started.");

	    SetEventInfo setEventInfo = new SetEventInfo();
	    setEventInfo.getUdfs().put("DURABLEHOLDSTATE", "Y");
	    setEventInfo.getUdfs().put("NOTE", eventComment);
        
        EventInfo eventInfo = EventInfoUtil.makeEventInfo(eventName, eventUser, eventComment, reasonCodeType, reasonCode);

        MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
        
     // Modified by smkang on 2019.05.28 - DurableServiceProxy.getDurableService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableData.getKey().getDurableName());
//
//		// For Clear Note, Add By Park Jeong Su
//		durableData.getUdfs().put("NOTE", "");
//		DurableServiceProxy.getDurableService().update(durableData);
        Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESDurableServiceProxy.getDurableServiceImpl().updateDurableWithoutHistory(durableData, updateUdfs);

        log.info("Carrier Hold End.");
    }
	
	
	public void updateProbeCardData(String sProbeCardName, String sTransportState, String sUnitName, String smachineName, String spCardPosition, 
	        String machineRecipeName, EventInfo eventInfo) throws CustomException
    {
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        Durable durableData = CommonUtil.getDurableInfo(sProbeCardName);
        Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sProbeCardName));
        
        //ONEQP -> Mount, OUTEQP -> Unmount
        if(StringUtil.equals(sTransportState, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
        {
            durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Mount);
            durableData.setMaterialLocationName(spCardPosition);
        }       
        else if(StringUtil.equals(sTransportState, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTEQP))
        {
            /* 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. ==>> */
            CommonUtil.setMaskPositionUpdate(durableData.getKey().getDurableName());
            /* <<== 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. */
            
            durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
            durableData.setMaterialLocationName("");
        }
                
        /************************************************************
          ProbeCard TransportState : ONEQP / OUTEQP
        *************************************************************/      
        // Put data into UDF
        Map<String, String> durableudfs = durableData.getUdfs();
        durableudfs.put("MACHINENAME", smachineName);  
        durableudfs.put("UNITNAME", sUnitName);
        if(StringUtil.isNotEmpty(spCardPosition))
        {
            durableudfs.put("MASKPOSITION", spCardPosition);
        }
        durableudfs.put("MACHINERECIPE", machineRecipeName);
        durableudfs.put("TRANSPORTSTATE", sTransportState);    
                
        DurableServiceProxy.getDurableService().update(durableData);
        
        // SetEvent Info create
        SetEventInfo setEventInfo = new SetEventInfo();
        setEventInfo.setUdfs(durableudfs);
        
        // Excute   
        MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
    }
	
	/**
	 * 
	 * @Name     checkExistenceByMachineNamePosition
	 * @since    2018. 9. 26.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param eventInfo
	 * @param durableName
	 * @param machineName
	 * @param unitName
	 * @param materialLocationName
	 * @param consumableState
	 * @throws CustomException
	 */
	public void checkExistenceByMachineNamePosition(EventInfo eventInfo, String durableName, String machineName, String unitName, 
            String materialLocationName, String transportState)throws CustomException
    {
        log.info("checkExistenceByMachineNamePosition Started.");

        // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        String strSql = "WHERE MACHINENAME = ? AND UNITNAME = ? AND MASKPOSITION = ? AND TRANSPORTSTATE = ? AND DURABLESTATE IN (?, ?)";
        String strSql = "WHERE MACHINENAME = ? AND UNITNAME = ? AND MASKPOSITION = ? AND TRANSPORTSTATE = ? AND DURABLESTATE IN (?, ?) FOR UPDATE";
        
        Object [] bindSet = new Object[]{machineName, unitName, materialLocationName, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,
                GenericServiceProxy.getConstantMap().Cons_Mount, GenericServiceProxy.getConstantMap().Cons_InUse};
        
        List<Durable>durableList = new ArrayList<Durable>();

        try
        {
            durableList = DurableServiceProxy.getDurableService().select(strSql,bindSet);
            
            if(durableList.size() > 0)
            {
                eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Unmount);
                //and change mask state to UNMOUNT/OUTSTK
                for(Durable durableData : durableList)
                {
                    if(StringUtil.equals(durableData.getKey().getDurableName(), durableName) && 
                            StringUtil.equals(durableData.getUdfs().get("MASKPOSITION"), materialLocationName) )
                    {
                    }
                    else
                    {
                        /* 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. ==>> */
                        CommonUtil.setMaskPositionUpdate(durableData.getKey().getDurableName());
                        /* <<== 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. */
                        
                        Map<String, String> durableUdfs = durableData.getUdfs();
                        durableUdfs.put("MACHINENAME", "");
                        durableUdfs.put("UNITNAME", "");
                        durableUdfs.put("MACHINERECIPE", "");
                        //durableUdfs.put("MASKPOSITION", "");
                        durableUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
                        
                        durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Available);
                        durableData.setMaterialLocationName("");
                        durableData.setUdfs(durableUdfs);
                        
                        DurableServiceProxy.getDurableService().update(durableData);
                        SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(durableData, durableUdfs);
                        MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
                        
                        eventInfo.setCheckTimekeyValidation(false);
                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                    }
                }
            }
        }
        catch(Exception e)
        {
            log.error(e.getMessage());
        }

        log.info("checkExistenceByMachineNamePosition Ended.");

    }
	
	/**
	 * 
	 * @Name     checkExistenceByDurableName
	 * @since    2018. 9. 26.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param eventInfo
	 * @param durableName
	 * @param machineName
	 * @param unitName
	 * @param materialLocationName
	 * @param transportState
	 * @throws CustomException
	 */
	public void checkExistenceByDurableName(EventInfo eventInfo, String durableName, String machineName, String unitName, String materialLocationName, String transportState)throws CustomException
    {
        log.info("checkExistenceByDurableName Started.");

        // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        String strSql = "WHERE DURABLENAME = ? AND TRANSPORTSTATE = ? AND DURABLESTATE IN (?, ?)";
        String strSql = "WHERE DURABLENAME = ? AND TRANSPORTSTATE = ? AND DURABLESTATE IN (?, ?) FOR UPDATE";
        
        Object [] bindSet = new Object[]{durableName, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,
                GenericServiceProxy.getConstantMap().Cons_Mount, GenericServiceProxy.getConstantMap().Cons_InUse};
        
        List<Durable>durableList = new ArrayList<Durable>();

        try
        {
            durableList = DurableServiceProxy.getDurableService().select(strSql,bindSet);
            
            if(durableList.size() > 0)
            {
                eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Unmount);
                //and change mask state to UNMOUNT/OUTSTK
                if(StringUtil.equals(durableList.get(0).getUdfs().get("MACHINENAME"), machineName) && 
                        StringUtil.equals(durableList.get(0).getUdfs().get("UNITNAME"), unitName) && 
                        StringUtil.equals(durableList.get(0).getUdfs().get("MASKPOSITION"), materialLocationName))
                {
                }
                else
                {
                    /* 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. ==>> */
                    CommonUtil.setMaskPositionUpdate(durableList.get(0).getKey().getDurableName());
                    /* <<== 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. */
                    
                    Map<String, String> durableUdfs = durableList.get(0).getUdfs();
                    durableUdfs.put("MACHINENAME", "");
                    durableUdfs.put("UNITNAME", "");
                    durableUdfs.put("MACHINERECIPE", "");
                    //durableUdfs.put("MASKPOSITION", "");
                    durableUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
                    
                    durableList.get(0).setDurableState(GenericServiceProxy.getConstantMap().Cons_Available);
                    durableList.get(0).setMaterialLocationName("");
                    durableList.get(0).setUdfs(durableUdfs);
                    
                    DurableServiceProxy.getDurableService().update(durableList.get(0));
                    SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(durableList.get(0), durableUdfs);
                    MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableList.get(0), setEventInfo, eventInfo);
                    
                    eventInfo.setCheckTimekeyValidation(false);
                    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());     
                }
            }
        }
        catch(Exception e)
        {
            log.error(e.getMessage());
        }

        log.info("checkExistenceByDurableName Ended.");
    }
	
	/**
	 * 
	 * @Name     probeCardUnMountByUnitOffLine
	 * @since    2018. 9. 26.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param eventInfo
	 * @param machineName
	 * @param unitName
	 * @param transportState
	 * @throws CustomException
	 */
	public void probeCardUnMountByUnitOffLine(EventInfo eventInfo, String machineName, String unitName)throws CustomException
    {
        log.info("probeCardUnMountByUnitOffLine Started.");

        // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        String strSql = "WHERE MACHINENAME = ? AND UNITNAME = ? AND TRANSPORTSTATE = ? AND DURABLESTATE IN (?, ?)";
        String strSql = "WHERE MACHINENAME = ? AND UNITNAME = ? AND TRANSPORTSTATE = ? AND DURABLESTATE IN (?, ?) FOR UPDATE";
        
        Object [] bindSet = new Object[]{ machineName, unitName, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP,
                GenericServiceProxy.getConstantMap().Cons_Mount, GenericServiceProxy.getConstantMap().Cons_InUse};
        
        List<Durable>durableList = new ArrayList<Durable>();

        try
        {
            durableList = DurableServiceProxy.getDurableService().select(strSql,bindSet);
            
            if(durableList.size() > 0)
            {
                for(Durable durableData : durableList)
                {                
                    /* 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. ==>> */
                    CommonUtil.setMaskPositionUpdate(durableData.getKey().getDurableName());
                    /* <<== 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. */
                    
                    Map<String, String> durableUdfs = durableData.getUdfs();
                    durableUdfs.put("MACHINENAME", "");
                    durableUdfs.put("UNITNAME", "");
                    durableUdfs.put("MACHINERECIPE", "");
                    //durableUdfs.put("MASKPOSITION", "");
                    durableUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
                    
                    durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Available);
                    durableData.setMaterialLocationName("");
                    durableData.setUdfs(durableUdfs);
                    
                    DurableServiceProxy.getDurableService().update(durableData);
                    SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(durableData, durableUdfs);
                    MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
                    
                    eventInfo.setCheckTimekeyValidation(false);
                    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());     
                }
            }
        }
        catch(Exception e)
        {
            log.error(e.getMessage());
        }

        log.info("probeCardUnMountByUnitOffLine Ended.");

    }
	
	/**
	 * @Name cancelCarrier
	 * @since 2018. 8. 19.
	 * @author hsryu
	 * @param machineName
	 * @param portName
	 * @param recipeName
	 * @param doc
	 * @throws InvalidStateTransitionSignal
	 * @throws FrameworkErrorSignal
	 * @throws NotFoundSignal
	 * @throws DuplicateNameSignal
	 * @throws CustomException
	 */
	// Deleted by smkang on 2018.08.21 - Move this method to LotServiceUtil for using RecipeServiceUtil.
//	public void cancelOtherPortCarrier(String machineName, String portName, String recipeName, Document doc) throws InvalidStateTransitionSignal,FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
//	{
//		if(StringUtil.isNotEmpty(machineName)&&StringUtil.isNotEmpty(recipeName))
//		{
//			List<Port> loaderPorts = MESPortServiceProxy.getPortServiceUtil().searchOtherLoaderPorts(machineName, portName);
//			
//			if(loaderPorts.size()>0)
//			{
//				for (Port port : loaderPorts) 
//				{
//					String strSql = "SELECT D.MACHINENAME, D.PORTNAME, D.DURABLENAME, L.MACHINERECIPENAME " +
//									"  FROM DURABLE D, LOT L " +
//									" WHERE 1=1 " +
//									"   AND D.DURABLENAME = L.CARRIERNAME  " +
//									"   AND D.MACHINENAME = :MACHINENAME " +
//									"   AND D.PORTNAME = :PORTNAME " +
//									"   AND L.MACHINERECIPENAME = :MACHINERECIPENAME ";
//					
//					Object[] bindSet = new Object[] { machineName,port.getKey().getPortName(),recipeName};
//
//					List<ListOrderedMap> durableList  = null ;
//
//					try
//					{
//						durableList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindSet);
//						
//						if(durableList.size()>0)
//						{
//							Machine machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
//
//							for(int i=0; i<durableList.size(); i++)
//							{
//								// Added by hsryu - Cancel LotProcess.
//								String resultDurableName = durableList.get(i).get("DURABLENAME").toString();
//								Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(resultDurableName);
//								
//								String resultPortName = durableList.get(i).get("PORTNAME").toString();
//								Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, resultPortName);
//								
//								doc = this.generateCancelCarrierBodyElement(doc, machineName, durableData, portData);
//								
//								String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
//								GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
//							}
//						}
//					}
//					catch(Exception e)
//					{
//						//Not exist.
//					}
//				}
//			}
//		}
//	}
		
	// Added by hsryu on 2018.08.19 - Make CSTForceQuitCommand Message.
	// Deleted by smkang on 2018.08.21 - Move this method to LotServiceUtil because cancelOtherPortCarrier method is moved to LotServiceUtil for using RecipeServiceUtil.
//	private Document generateCSTForceQuitCommand(Document doc, String machineName ,Durable DurableData, Port portData) throws CustomException
//	{
//		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CSTForceQuitCommand");
//		SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", "");
//				
//		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
//		
//		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
//		
//		Element element1 = new Element("MACHINENAME");
//		element1.setText(machineName);
//		eleBodyTemp.addContent(element1);
//		
//		Element element2 = new Element("UNITNAME");
//		element2.setText("");
//		eleBodyTemp.addContent(element2);
//		
//		Element element3 = new Element("CARRIERNAME");
//		element3.setText(DurableData.getKey().getDurableName());
//		eleBodyTemp.addContent(element3);
//
//		Element element4 = new Element("CARRIERSTATE");
//		element4.setText(DurableData.getDurableState());
//		eleBodyTemp.addContent(element4);
//		
//		Element element5 = new Element("CARRIERTYPE");
//		element5.setText(DurableData.getDurableType());
//		eleBodyTemp.addContent(element5);
//		
//		Element element6 = new Element("PORTNAME");
//		element6.setText(portData.getKey().getPortName());
//		eleBodyTemp.addContent(element6);
//		
//		Element element7 = new Element("PORTTYPE");
//		element7.setText(portData.getUdfs().get("PORTTYPE"));
//		eleBodyTemp.addContent(element7);
//		
//		Element element8 = new Element("PORTUSETYPE");
//		element8.setText(portData.getUdfs().get("PORTUSETYPE"));
//		eleBodyTemp.addContent(element8);
//		
//		//overwrite
//		doc.getRootElement().addContent(eleBodyTemp);
//		
//		return doc;
//	}
	
	// Added by smkang on 2018.10.01 - According to EDO's request, carrier data should be synchronized with shared factory.
	public void publishMessageToSharedShop (Document document, String carrierName) {
		if (StringUtils.isNotEmpty(carrierName)) {
			try {
				Durable durableData = getDurableData(carrierName);
				
				// Modified by smkang on 2018.10.10 - To avoid recursive publishing.
//				if (durableData != null) {
				String eventUser = SMessageUtil.getHeaderItemValue(document, "EVENTUSER", false);
				String originalSourceSubjectName = SMessageUtil.getHeaderItemValue(document, "ORIGINALSOURCESUBJECTNAME", false);

				if (durableData != null && !System.getProperty("svr").equals(eventUser)) {
					// Modified by smkang on 2018.10.17 - According to EDO's request, common carrier can be created in shared factory.
					//									  But a user doesn't want to register DurableSpec in shared factory.
//					DurableSpecKey durableSpecKey = new DurableSpecKey();
//					durableSpecKey.setFactoryName(durableData.getFactoryName());
//					durableSpecKey.setDurableSpecName(durableData.getDurableSpecName());
//					durableSpecKey.setDurableSpecVersion(durableData.getDurableSpecVersion());
//					
//					DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);
//					
//					String sharedFactoryName = durableSpecData.getUdfs().get("SHAREDFACTORY");					
					String sharedFactoryName = "";
					try {
						List<DurableSpec> durableSpecDataList = DurableServiceProxy.getDurableSpecService().select("DURABLESPECNAME = ?", new Object[] {durableData.getDurableSpecName()});
						
						if (durableSpecDataList != null && durableSpecDataList.size() > 0)
							sharedFactoryName = durableSpecDataList.get(0).getUdfs().get("SHAREDFACTORY");
						else
							sharedFactoryName = durableData.getUdfs().get("OWNER");
					} catch (Exception e) {
						// TODO: handle exception
						sharedFactoryName = durableData.getUdfs().get("OWNER");
					}
					
					// Modified by smkang on 2018.10.18 - According to EDO's request, common carrier can be created in shared factory.
					//									  But a user doesn't want to register DurableSpec in shared factory.
//					if (StringUtils.isNotEmpty(sharedFactoryName)) {
					if (StringUtils.isNotEmpty(sharedFactoryName) && !System.getProperty("shop").equals(sharedFactoryName)) {
						// Added by smkang on 2018.10.10 - To avoid recursive publishing.
						SMessageUtil.setHeaderItemValue(document, "EVENTUSER", System.getProperty("svr"));
						SMessageUtil.setHeaderItemValue(document, "ORIGINALSOURCESUBJECTNAME", "");

						if (StringUtils.equals(sharedFactoryName, "OLED"))
							GenericServiceProxy.getESBServive().sendBySender(document, "OledCNMSender");
						
						SMessageUtil.setHeaderItemValue(document, "EVENTUSER", eventUser);
						SMessageUtil.setHeaderItemValue(document, "ORIGINALSOURCESUBJECTNAME", originalSourceSubjectName);
					}
				}
			} catch (Exception e) {
				log.warn(e);
			}
		}
	}
	
	//except update Durable - MachineRecipeName
	public void updateProbeCardDataForOPI(String sProbeCardName, String sTransportState, String sUnitName, String smachineName, String spCardPosition, 
	         EventInfo eventInfo) throws CustomException
    {
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        Durable durableData = CommonUtil.getDurableInfo(sProbeCardName);
        Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sProbeCardName));
        
        //ONEQP -> Mount, OUTEQP -> Unmount
        if(StringUtil.equals(sTransportState, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
        {
            durableData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Mount);
            durableData.setMaterialLocationName(spCardPosition);
        }       
        else if(StringUtil.equals(sTransportState, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTEQP))
        {
            /* 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. ==>> */
            CommonUtil.setMaskPositionUpdate(durableData.getKey().getDurableName());
            /* <<== 20180813, Add, Change the MaskPosition value to NULL when unmounting ProbeCard. */
            
            durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
            durableData.setMaterialLocationName("");
        }
                
        /************************************************************
          ProbeCard TransportState : ONEQP / OUTEQP
        *************************************************************/      
        // Put data into UDF
        Map<String, String> durableudfs = durableData.getUdfs();
        durableudfs.put("MACHINENAME", smachineName);  
        durableudfs.put("UNITNAME", sUnitName);
        if(StringUtil.isNotEmpty(spCardPosition))
        {
            durableudfs.put("MASKPOSITION", spCardPosition);
        }
        durableudfs.put("TRANSPORTSTATE", sTransportState);    
                
        DurableServiceProxy.getDurableService().update(durableData);
        
        // SetEvent Info create
        SetEventInfo setEventInfo = new SetEventInfo();
        setEventInfo.setUdfs(durableudfs);
        
        // Excute   
        MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
    }
	
	//add by wghuang 20181122
	public void sendTerminalMessage(Document doc, String machineName, String[] terminalContent) throws CustomException
	{	
		boolean existValue = false;
		
		//check value	
		for(int count = 0; count < terminalContent.length; count ++)
		{
			String terminalMessage = terminalContent[count];
			
			if(StringUtil.isNotEmpty(terminalMessage))
			{
				existValue = true;
			}
		}
		
		if(existValue == true)
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_TerminalMessageSend");
			SMessageUtil.setHeaderItemValue(doc, "TRANSACTIONID", TimeUtils.getCurrentEventTimeKey());
			/* 20190509, hhlee, modify, change originalsourcesubjectname */
			//SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", "MES.SUBJECT");
			SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("PEMsvr"));
			SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", "MES");
			SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "PhotoMask not exist.");
				
			//remove Body Content
		    SMessageUtil.getBodyElement(doc).removeContent();
					
		    Element machineNameE = new Element("MACHINENAME");
			machineNameE.setText(machineName);
			SMessageUtil.getBodyElement(doc).addContent(machineNameE);
			
			Element terminalMessageList = new Element("TERMINALMESSAGELIST");
				
			Element terminalMessageE = null;
			
			String terminalmessage = "";
			
			for(int i = 0; i < terminalContent.length; i++)
			{
				terminalmessage = terminalContent[i];
				if(StringUtil.isNotEmpty(terminalmessage))
				{
					terminalMessageE = new Element("TERMINALMESSAGE");
					terminalMessageE.setText("photoMask:[" + terminalContent[i] + "] not exist!!!");
					terminalMessageList.addContent(terminalMessageE);
				}
				else
				{
					continue;
				}		
			}
			
			SMessageUtil.getBodyElement(doc).addContent(terminalMessageList);
					
			 try
	        {
	            Object result = InvokeUtils.invokeMethod(InvokeUtils.newInstance(TerminalMessageSend.class.getName(), null, null), "execute", new Object[] {doc});
	        }
	        catch (Exception ex)
	        {
	        	log.error("=======================================================");
	        	log.error("TerminalMessageSend Failed. Reason:" + ex.getMessage());
	        	log.error("=======================================================");
	        }	 
		}
	}
	
	/**
	 * 
	 * @Name     validateDuplicateMaskPosition
	 * @since    2019. 5. 22.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param photoMaskDataList
	 * @throws CustomException
	 */
	public void validateDuplicateMaskPosition(List<Durable> photoMaskDataList) throws CustomException
	{
	    if(photoMaskDataList != null && photoMaskDataList.size() > 0)
	    {
	        String maskPosition = StringUtil.EMPTY;
	        
	        for(Durable photoMaskData : photoMaskDataList)
	        {
	            if(!StringUtil.equals(maskPosition, CommonUtil.getValue(photoMaskData.getUdfs(), "MASKPOSITION")))
	            {
	                maskPosition = CommonUtil.getValue(photoMaskData.getUdfs(), "MASKPOSITION");
	            }
	            else
	            {
	                throw new CustomException("MASK-0098", photoMaskData.getKey().getDurableName(), maskPosition);
	            }
	        }
	    }
	}
}