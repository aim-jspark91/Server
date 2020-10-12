package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FileJudgeSetting;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;

import org.jdom.Document;
import org.jdom.Element;

public class FileJudgeRuleReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "FileJudgeRuleReport");
		 
		try
		{
			this.generateBodyTemplate(doc);	
		}
		catch(Exception ex)
		{
			throw new CustomException("ReviewStation-0001", "");
		}
		
		GenericServiceProxy.getESBServive().sendBySender(doc, "DFSSender");
	}
	private Element generateBodyTemplate(Document doc) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element judgeRuleList = new Element("JUDGERULELIST");
		try {
			// Start 2019.09.06 Modify By Park Jeong Su Mantis 4755
			//List<FileJudgeSetting> fileJudgeSetting = ExtendedObjectProxy.getFileJudgeSettingService().select("", new Object[]{""});
			List<FileJudgeSetting> fileJudgeSetting = ExtendedObjectProxy.getFileJudgeSettingService().select(" WHERE GRADEDATAFLAG = ? ", new Object[]{"Y"});
			// End
			for(FileJudgeSetting fileJudge : fileJudgeSetting)
			{
				Element judgeRule = new Element("JUDGERULE");
				
				Element factoryName = new Element("FACTORYNAME");
				factoryName.setText(fileJudge.getFactoryName());
				Element processFlowName = new Element("PROCESSFLOWNAME");
				processFlowName.setText(fileJudge.getProcessFlowName());
				Element processOperationName = new Element("PROCESSOPERATIONNAME");
				processOperationName.setText(fileJudge.getProcessOperationName());
				Element gradeDataFlag = new Element("GRADEDATAFLAG");
				gradeDataFlag.setText(fileJudge.getGradeDataFlag());
				Element reJudgeFlag = new Element("REJUDGEFLAG");
				reJudgeFlag.setText(fileJudge.getReJudgeFlag());
				Element autoReviewFlag = new Element("AUTOREVIEWFLAG");
				autoReviewFlag.setText(fileJudge.getAutoReviewFlag());
				
				judgeRule.addContent(factoryName);
				judgeRule.addContent(processFlowName);
				judgeRule.addContent(processOperationName);
				judgeRule.addContent(gradeDataFlag);
				judgeRule.addContent(reJudgeFlag);
				judgeRule.addContent(autoReviewFlag);

				judgeRuleList.addContent(judgeRule);
			}
		} catch (Exception e) {
			throw new CustomException("ReviewStation-0001", "");
		}
		

		bodyElement.addContent(judgeRuleList);
		doc.getRootElement().addContent(2, bodyElement);
		
		return bodyElement;
	}
}