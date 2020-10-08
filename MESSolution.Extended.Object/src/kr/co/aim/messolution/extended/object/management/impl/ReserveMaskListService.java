package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReserveMaskListService extends CTORMService<ReserveMaskList> {
	
	public static Log logger = LogFactory.getLog(ReserveMaskList.class);
	
	private final String historyEntity = "ReserveMaskListHist";
	
	public List<ReserveMaskList> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal, CustomException
	{
		List<ReserveMaskList> result;
		try
		{
			result = super.select(condition, bindSet, ReserveMaskList.class);
		}
		catch (greenFrameDBErrorSignal de )
		{
			result = null;
			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new greenFrameDBErrorSignal("MASK-0009",de.getSql()); 
			}else 
			{
				throw new CustomException("SYS-8001",de.getSql()); 
			}
		}
		
		return result;
		
	}
	
	public ReserveMaskList selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReserveMaskList.class, isLock, keySet);
	}
	
	public ReserveMaskList create(EventInfo eventInfo, ReserveMaskList dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ReserveMaskList dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ReserveMaskList modify(EventInfo eventInfo, ReserveMaskList dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	/*
	* Name : getReserveMaskInfoByCarreierName
	* Desc : This function is getReserveMaskInfoByCarreierName
	* Author : Lhkim
	* Date : 2015.03.27
	*/
	public static List<ReserveMaskList> getReserveMaskInfoByCarreierName(String machineName, String portName,String carrierName) throws CustomException
	{ 
		String condition = "WHERE machinename =? and portName =? and  carrierName =? order by positionName";
						
		Object[] bindSet = new Object[] {machineName,portName,carrierName,};
		List<ReserveMaskList> maskList = new ArrayList<ReserveMaskList>();
		try
		{
			maskList = ExtendedObjectProxy.getReserveMaskService().select(condition, bindSet);
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
		return maskList;
	}
	
	/*
	* Name : getMaskInfoByCarreierName
	* Desc : This function is getMaskInfoByCarreierName
	* Author : jhlee
	* Date : 2016.05.05
	*/
	public static List<ReserveMaskList> getMaskInfoByCarreierName(String machineName, String portName,String carrierName) throws CustomException
	{ 
		String condition = "WHERE machinename =? and portName =? and  carrierName =? order by positionName";
						
		Object[] bindSet = new Object[] {machineName,portName,carrierName,};
		List<ReserveMaskList> resvMaskList = new ArrayList<ReserveMaskList>();
		try
		{
			resvMaskList = ExtendedObjectProxy.getReserveMaskService().select(condition, bindSet);
		}
		catch(greenFrameDBErrorSignal de)
		{
			resvMaskList = null;
			
			return resvMaskList;
		}
		return resvMaskList;
	}
	
	/*
	* Name : getMaskInfoByCarreierName
	* Desc : This function is getMaskInfoByCarreierName
	* Author : zhongsl
	* Date : 2017.06.08
	*/
	public static List<ReserveMaskList> getMaskInfoByCarreierName(String machineName, String carrierName) throws CustomException
	{ 
		String condition = "WHERE machinename =?  and  carrierName =? order by positionName";
						
		Object[] bindSet = new Object[] {machineName,carrierName,};
		List<ReserveMaskList> resvMaskList = new ArrayList<ReserveMaskList>();
		try
		{
			resvMaskList = ExtendedObjectProxy.getReserveMaskService().select(condition, bindSet);
		}
		catch(greenFrameDBErrorSignal de)
		{
			resvMaskList = null;
			
			return resvMaskList;
		}
		return resvMaskList;
	}
	
	/*
	* Name : getReserveMaskInfoByPB
	* Desc : This function is getReserveMaskInfoByPB
	* Author : aim System
	* Date : 2016.08.12
	*/
	public static ReserveMaskList getReserveMaskInfoByPB(String maskName, String unitName) throws CustomException
	{ 
		String condition = "WHERE maskName =? and unitName =? ";
						
		Object[] bindSet = new Object[] {maskName, unitName};
		List<ReserveMaskList> resvMaskList = new ArrayList<ReserveMaskList>();
		ReserveMaskList resvMask = new ReserveMaskList();
		try
		{
			resvMaskList = ExtendedObjectProxy.getReserveMaskService().select(condition, bindSet);
			
			resvMask = resvMaskList.get(0);
		}
		catch(greenFrameDBErrorSignal de)
		{
			//resvMaskList = null;
			
			return null;
		}
		
		return resvMask;
	}
	
	/*
	* Name : getReserveMaskInfoByPL
	* Desc : This function is getReserveMaskInfoByPL
	* Author : aim System
	* Date : 2016.08.12
	*/
	public static ReserveMaskList getReserveMaskInfoByPL(String maskName) throws CustomException
	{ 
		String condition = "WHERE maskName =? ";
						
		Object[] bindSet = new Object[] {maskName};
		List<ReserveMaskList> resvMaskList = new ArrayList<ReserveMaskList>();
		ReserveMaskList resvMask = new ReserveMaskList();
		try
		{
			resvMaskList = ExtendedObjectProxy.getReserveMaskService().select(condition, bindSet);
			if(!resvMaskList.isEmpty())
			{
				resvMask = resvMaskList.get(0);
			}			
		}
		catch(greenFrameDBErrorSignal de)
		{
			//resvMaskList = null;
			
			return null;
		}
		return resvMask;
	}
}
