package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectRuleSetting;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.jdom.Document;
import org.jdom.Element;

public class DefectValidationRuleRequest extends AsyncHandler { 
	
	@Override 
	public void doWorks(Document doc) throws CustomException {
		
		 SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "DefectValidationRuleReply");
		 
		try
		{
			this.generateBodyTemplate(doc);	
		}
		catch(Exception ex)
		{
			throw new CustomException("ReviewStation-0001", "");
		}
		
		
		GenericServiceProxy.getMessageLogService().getLog().debug(new StringBuffer("jeongSuTest : ").append(JdomUtils.toString(doc)).toString());
		GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "DFSSender");
	}
	
	private void generateBodyTemplate(Document doc) throws CustomException
	{
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element bodyElement = null;
		bodyElement = new Element("Body");
		
		Element defectValidationList = new Element("DEFECTVALIDATIONLIST");
		Element aoiOperationList = new Element("AOIOPERATIONLIST");
		try {
			List<DefectRuleSetting> defectRuleSettingList = ExtendedObjectProxy.getDefectRuleSettingService().select("", new Object[]{});
			
			for(DefectRuleSetting defectRuleSetting : defectRuleSettingList)
			{
				Element defectValidation = new Element("DEFECTVALIDATION");
				
				Element factoryName = new Element("FACTORYNAME");
				factoryName.setText(defectRuleSetting.getFactoryName());
				
				Element productSpecName = new Element("PRODUCTSPECNAME");
				productSpecName.setText(defectRuleSetting.getProductSpecName());
				Element processOperationName = new Element("PROCESSOPERATIONNAME");
				processOperationName.setText(defectRuleSetting.getProcessOperationName());
				Element defectCode = new Element("DEFECTCODE");
				defectCode.setText(defectRuleSetting.getDefectCode());
				Element defectSize = new Element("DEFECTSIZE");
				defectSize.setText(String.valueOf( defectRuleSetting.getDefectSize() ) );
				Element defectCount = new Element("DEFECTCOUNT");
				defectCount.setText(   String.valueOf(defectRuleSetting.getDefectCount())   );
				
				Element holdFlag = new Element("HOLDFLAG");
				holdFlag.setText(   defectRuleSetting.getHoldFlag()  );
				Element mailFlag = new Element("MAILFLAG");
				mailFlag.setText(  defectRuleSetting.getMailFlag()   );
				Element userGroupName = new Element("USERGROUPNAME");
				userGroupName.setText(  defectRuleSetting.getUserGroupName()   );
				
				
				defectValidation.addContent(factoryName);
				
				defectValidation.addContent(productSpecName);
				defectValidation.addContent(processOperationName);
				defectValidation.addContent(defectCode);
				defectValidation.addContent(defectSize);
				defectValidation.addContent(defectCount);
				
				defectValidation.addContent(holdFlag);
				defectValidation.addContent(mailFlag);
				defectValidation.addContent(userGroupName);

				defectValidationList.addContent(defectValidation);
			}
		} catch (Exception e) {
			GenericServiceProxy.getMessageLogService().getLog().debug(new StringBuffer("Data not fount!"));
		}
		
		try {
			List<ProcessOperationSpec> processOperationSpecList = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().select(" WHERE FACTORYNAME = ? AND PROCESSOPERATIONTYPE = ?", new Object[]{"ARRAY", "Inspection"});

			for(ProcessOperationSpec processOperationSpec : processOperationSpecList)
			{
				Element processOperationName = new Element("PROCESSOPERATIONNAME");
				processOperationName.setText(processOperationSpec.getKey().getProcessOperationName());
				
				aoiOperationList.addContent(processOperationName);
			}
		} catch (Exception e) {
			GenericServiceProxy.getMessageLogService().getLog().debug(new StringBuffer("Data not fount!"));
		}

		bodyElement.addContent(defectValidationList);
		bodyElement.addContent(aoiOperationList);
		doc.getRootElement().addContent(2, bodyElement);
		
		//return bodyElement;
		//return doc;
	}
}
