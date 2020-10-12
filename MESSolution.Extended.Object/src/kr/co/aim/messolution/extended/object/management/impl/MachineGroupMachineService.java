package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MachineGroupMachine;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/* hhlee, 2018.03.31, Add */
public class MachineGroupMachineService extends CTORMService<MachineGroupMachine> {
	
	public static Log logger = LogFactory.getLog(MachineGroupMachineService.class);
	
	private final String historyEntity = "";
	
	public List<MachineGroupMachine> select(String condition, Object[] bindSet)
		throws CustomException
	{
		try
		{
			List<MachineGroupMachine> result = super.select(condition, bindSet, MachineGroupMachine.class);
			
			return result;
		}
		catch (greenFrameDBErrorSignal de)
		{
			throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9199", fe.getMessage());
		}
	}
	
	public MachineGroupMachine selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		try
		{
			return super.selectByKey(MachineGroupMachine.class, isLock, keySet);
		}
		catch (greenFrameDBErrorSignal de)
		{
			throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9199", fe.getMessage());
		}
	}
	
	public MachineGroupMachine create(EventInfo eventInfo, MachineGroupMachine dataInfo)
		throws CustomException
	{
		try
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch (greenFrameDBErrorSignal de)
		{
			throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9199", fe.getMessage());
		}
	}
	
	public void remove(EventInfo eventInfo, MachineGroupMachine dataInfo)
		throws CustomException
	{
		try
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		catch (greenFrameDBErrorSignal de)
		{
			throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9199", fe.getMessage());
		}
	}
	
	public MachineGroupMachine modify(EventInfo eventInfo, MachineGroupMachine dataInfo)
		throws CustomException
	{
		try
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch (greenFrameDBErrorSignal de)
		{
			throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9199", fe.getMessage());
		}
	}
}
