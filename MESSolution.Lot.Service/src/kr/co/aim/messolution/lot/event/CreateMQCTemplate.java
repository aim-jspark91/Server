package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplate;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.exception.ExceptionKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleAttrDef;
import kr.co.aim.greentrack.name.management.util.GenerateUtil;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CreateMQCTemplate extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String productQuantity = SMessageUtil.getBodyItemValue(doc, "PRODUCTQUANTITY", true);
		String mqcCountLimit = SMessageUtil.getBodyItemValue(doc, "MQCCOUNTLIMIT", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		
		String productspecname = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPEC", false);
		String eccode = SMessageUtil.getBodyItemValue(doc, "ECCODE", false);
		
		String MQCTemplateNameByMenual = SMessageUtil.getBodyItemValue(doc, "MQCTEMPLATENAME", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(factoryName);
		processFlowKey.setProcessFlowName(processFlowName);
		processFlowKey.setProcessFlowVersion(processFlowVersion);
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		
		Boolean checkname;
		
		if(processFlowData != null)
		{
			if(!StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
			{
				throw new CustomException("MQC-0005", processFlowName);
			}
			
			MQCTemplate mqcTemplateData = null;
			List<String> mqcTemplateName = null;
			String NewTemplateName = "";
			// 2018.10.15 Mentis 682;
			if(StringUtil.isEmpty(MQCTemplateNameByMenual))
			{
				mqcTemplateData = null;
				List<String> argSeq = new ArrayList<String>();
				mqcTemplateName = NameServiceProxy.getNameGeneratorRuleDefService().generateName("MQCTemplateNaming", argSeq, 1);
				NewTemplateName = mqcTemplateName.get(0);			
				checkname = this.checkExistMQCTemplate(NewTemplateName);
				if(checkname){
					do 
					{
						List<NameGeneratorRuleAttrDef> allRuleAttrDefs = NameServiceProxy.getNameGeneratorRuleAttrDefService().getAllRuleAttrDefs("MQCTemplateNaming");
						this.checkArgumentSize(allRuleAttrDefs, argSeq.size());
						this.updateSectionValue(allRuleAttrDefs, argSeq);
						StringBuilder prefixBuilder = new StringBuilder();
						for (NameGeneratorRuleAttrDef ruleAttrDef : allRuleAttrDefs)
						{
							if (ruleAttrDef.isPrefixCategory())
							{
								prefixBuilder.append(ruleAttrDef.getSectionValue());
							}
						}
						String prefix = prefixBuilder.toString();
						
						char[] cs = NewTemplateName.substring(prefix.length()).toString().toCharArray();
						for (int i = cs.length - 1; i >= 0; i--)
						{
							if (cs[i] == '9')
							{
								cs[i] = '0';
							}
							else
							{
								cs[i] += 1;
								break;
							}
						}
						String NewSerial = new String(cs);
						NewTemplateName = prefix + NewSerial;

						checkname = this.checkExistMQCTemplate(NewTemplateName);
						if(!checkname)
						{
							String Updatesql = "UPDATE NameGeneratorSerial SET LASTSERIALNO = :LASTSERIALNO WHERE RULENAME = :RULENAME";
							Map<String, String> UbindMap = new HashMap<String, String>();
							UbindMap.put("LASTSERIALNO" , NewSerial);
							UbindMap.put("RULENAME"  , "MQCTemplateNaming");
							GenericServiceProxy.getSqlMesTemplate().update(Updatesql, UbindMap);
						}
					}
					while(checkname);
				}
				
			}
			else 
			{
				checkname = this.checkExistMQCTemplate(MQCTemplateNameByMenual);
				if(checkname)
				{
					throw new CustomException("MQC-0008", MQCTemplateNameByMenual);
				}
			}
			
			if(StringUtil.isEmpty(MQCTemplateNameByMenual))
			{
				mqcTemplateData = new MQCTemplate(NewTemplateName);			
			}
			else 
			{
				mqcTemplateData = new MQCTemplate(MQCTemplateNameByMenual);			
			}
			
			
			mqcTemplateData.setproductspecname(productspecname);
			mqcTemplateData.seteccode(eccode);		
			mqcTemplateData.setdescription(description);
			mqcTemplateData.setfactoryName(factoryName);
			mqcTemplateData.setprocessFlowName(processFlowName);
			mqcTemplateData.setprocessFlowVersion(processFlowVersion);
			mqcTemplateData.setproductQuantity(Long.valueOf(productQuantity));
			mqcTemplateData.setmqcCountLimit(Long.valueOf(mqcCountLimit));
			mqcTemplateData.setlastEventUser(eventInfo.getEventUser());
			mqcTemplateData.setlastEventComment(eventInfo.getEventComment());
			mqcTemplateData.setlastEventTime(eventInfo.getEventTime());
			mqcTemplateData.setlastEventTimekey(eventInfo.getEventTimeKey());
			mqcTemplateData.setlastEventName(eventInfo.getEventName());
			
			ExtendedObjectProxy.getMQCTemplateService().create(eventInfo, mqcTemplateData);
		}
		else
		{
			throw new CustomException("MQC-0004", processFlowName);
		}
		return doc;
	}
	
	private void updateSectionValue(List<NameGeneratorRuleAttrDef> allRuleAttrDefs, List<String> argSeq)
			throws FrameworkErrorSignal
	{

		int argCnt = 0;
		for (NameGeneratorRuleAttrDef ruleAttrDef : allRuleAttrDefs)
		{
			if (ruleAttrDef.isArgumentType())
			{
				String arg = argSeq.get(argCnt++);
				updateArgumentSectionValue(ruleAttrDef, arg);
			}
			else if (ruleAttrDef.isConstantType())
			{
				updateConstantSectionValue(ruleAttrDef);
			}
			else if (ruleAttrDef.isYearType())
			{
				updateYearSectionValue(ruleAttrDef);
			}
			else if (ruleAttrDef.isMonthType())
			{
				updateMonthSectionValue(ruleAttrDef);
			}
			else if (ruleAttrDef.isDayType())
			{
				updateDaySectionValue(ruleAttrDef);
			}
		}
	}
	
	private void updateDaySectionValue(NameGeneratorRuleAttrDef ruleAttrDef) throws FrameworkErrorSignal
	{
		if (ruleAttrDef.getSectionLength() > 2)
		{
			throw new FrameworkErrorSignal(ExceptionKey.InvalidArguments_Exception, "Day Section Length : "
				+ ruleAttrDef.getSectionLength(), "Day Section Length <= 2");
		}

		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		if (ruleAttrDef.getSectionLength() == 1)
		{
			if (day < 10)
			{
				ruleAttrDef.setSectionValue("" + day);
			}
			else
			{
				char c = 'A';
				c += (day - 10);
				c = GenerateUtil.checkException(c, ruleAttrDef.getExceptionalCharacter());
				ruleAttrDef.setSectionValue(new String(new char[] { c }));
			}
		}
		else
		{
			if (day < 10)
			{
				ruleAttrDef.setSectionValue("0" + day);
			}
			else
			{
				ruleAttrDef.setSectionValue("" + day);
			}
		}
	}
	private void updateMonthSectionValue(NameGeneratorRuleAttrDef ruleAttrDef) throws FrameworkErrorSignal
	{
		if (ruleAttrDef.getSectionLength() > 2)
		{
			throw new FrameworkErrorSignal(ExceptionKey.InvalidArguments_Exception, "Month Section Length : "
				+ ruleAttrDef.getSectionLength(), "Month Section Length <= 2");
		}

		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		if (ruleAttrDef.getSectionLength() == 1)
		{
			if (month < 10)
			{
				ruleAttrDef.setSectionValue("" + month);
			}
			else
			{
				char c = ruleAttrDef.getSectionValue().charAt(month - 10);
				c = GenerateUtil.checkException(c, ruleAttrDef.getExceptionalCharacter());
				ruleAttrDef.setSectionValue(new String(new char[] { c }));
			}
		}
		else
		{
			if (month < 10)
			{
				ruleAttrDef.setSectionValue("0" + month);
			}
			else
			{
				ruleAttrDef.setSectionValue("" + month);
			}
		}
	}
	private void updateYearSectionValue(NameGeneratorRuleAttrDef ruleAttrDef) throws FrameworkErrorSignal
	{
		if (ruleAttrDef.getSectionLength() > 4)
		{
			throw new FrameworkErrorSignal(ExceptionKey.InvalidArguments_Exception, "Year Section Length : "
				+ ruleAttrDef.getSectionLength(), "Year Section Length <= 4");
		}

		String year = "" + Calendar.getInstance().get(Calendar.YEAR);
		int secLength = (int) ruleAttrDef.getSectionLength();
		ruleAttrDef.setSectionValue(year.substring(4 - secLength, 4 - secLength + secLength));
	}
	
	private void updateConstantSectionValue(NameGeneratorRuleAttrDef ruleAttrDef) throws FrameworkErrorSignal
	{
		if (ruleAttrDef.getSectionValue().length() < ruleAttrDef.getSectionLength())
		{
			throw new FrameworkErrorSignal(ExceptionKey.InvalidArguments_Exception, "Constant Section Value : "
				+ ruleAttrDef.getSectionValue()
				+ "(Length:"
				+ ruleAttrDef.getSectionValue().length()
				+ ")", ObjectUtil.getString(ruleAttrDef));
		}

		ruleAttrDef.setSectionValue(ruleAttrDef.getSectionValue().substring(0, (int) ruleAttrDef.getSectionLength()));
	}
	private void updateArgumentSectionValue(NameGeneratorRuleAttrDef ruleAttrDef, String arg)
			throws FrameworkErrorSignal
	{

		if (StringUtils.isEmpty(ruleAttrDef.getSectionValue()))
		{

			if (arg.length() < ruleAttrDef.getSectionLength())
			{
				throw new FrameworkErrorSignal(ExceptionKey.InvalidArguments_Exception, "Argument Value : "
					+ arg
					+ " (Length="
					+ arg.length()
					+ ")", "Length=" + ruleAttrDef.getSectionLength());
			}

			ruleAttrDef.setSectionValue(arg.substring(0, (int) ruleAttrDef.getSectionLength()));

		}
		else if (ruleAttrDef.getSectionValue().toLowerCase().startsWith("substr"))
		{

			int begin = Integer.parseInt(ruleAttrDef.getSectionValue().substring(6));

			if (arg.length() < begin + ruleAttrDef.getSectionLength())
			{
				throw new FrameworkErrorSignal(ExceptionKey.InvalidArguments_Exception, "Argument Value : "
					+ arg
					+ " (Length="
					+ arg.length()
					+ ")", "Length=" + (begin + ruleAttrDef.getSectionLength()));
			}

			ruleAttrDef.setSectionValue(arg.substring(begin, begin + (int) ruleAttrDef.getSectionLength()));
		}
	}
	private void checkArgumentSize(List<NameGeneratorRuleAttrDef> allRuleAttrDefs, int inputArgSeqCount)
			throws FrameworkErrorSignal
	{
		// Calculate Argument Count
		int argumentCount = 0;
		for (NameGeneratorRuleAttrDef ruleAttrDef : allRuleAttrDefs)
		{
			if (ruleAttrDef.isArgumentType())
			{
				argumentCount++;
			}
		}

		// Check Argument Quantity
		if (argumentCount != inputArgSeqCount)
		{
			throw new FrameworkErrorSignal(ExceptionKey.InvalidArguments_Exception, "Argument Count : "
				+ inputArgSeqCount, "Argument Rule Count : " + argumentCount);
		}
	}
	
	public boolean checkExistMQCTemplate(String MQCTemplateName) throws CustomException{
		String condition = "MQCTEMPLATENAME = ?";			
		Object[] bindSet = new Object[] {MQCTemplateName};				
		List <MQCTemplate> sqlResult = null;
		sqlResult = ExtendedObjectProxy.getMQCTemplateService().select(condition, bindSet);		
		if(sqlResult.size() > 0)
		{
			return true;
		}
		return false;
	}	
}
