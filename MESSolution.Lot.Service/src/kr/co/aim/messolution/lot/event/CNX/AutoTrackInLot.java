package kr.co.aim.messolution.lot.event.CNX;

import java.lang.reflect.InvocationTargetException;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;

import org.jdom.Document;
import org.jdom.Element;

public class AutoTrackInLot extends AsyncHandler {
	@Override
	public void doWorks(Document doc) throws CustomException {

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);

		Object result = null;
		boolean glassTrackInFlag = false;

		Lot lotData =  MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		// Added by smkang on 2018.11.26 - According to Liu Hongwei's request, check possible to run again.
		if (!MESLotServiceProxy.getLotServiceUtil().possibleToOPIRun(lotName, machineName))
			throw new CustomException("COMMON-0001", "Impossible to run, MachineName[" + machineName + "] and ProcessOperationName[" + lotData.getProcessOperationName() + "]");

		MachineSpecKey msKey = new MachineSpecKey(machineName);
		MachineSpec msData = MachineServiceProxy.getMachineSpecService().selectByKey(msKey);
		String constructType = msData.getUdfs().get("CONSTRUCTTYPE").toString();

		if(CommonUtil.isInineType(machineName))
		{
			glassTrackInFlag = true;
		}

		try
		{
			//String machineRecipeName = "wghuangTest" ; //Need to check !!!
			String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
										lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));

			doc = this.writeTrackInRequest(doc, lotData.getKey().getLotName(), machineName, portName, machineRecipeName, glassTrackInFlag);

			if(glassTrackInFlag)
			{
				result = InvokeUtils.invokeMethod(InvokeUtils.newInstance(TrackInGlass.class.getName(), null, null), "execute", new Object[] {doc});
			}
			else
			{
				result = InvokeUtils.invokeMethod(InvokeUtils.newInstance(TrackInLot.class.getName(), null, null), "execute", new Object[] {doc});
			}

		}
		catch (NoSuchMethodException e)
		{
			eventLog.error(e);
		}
		catch (IllegalAccessException e)
		{
			eventLog.error(e);
		}
		catch (InvocationTargetException e)
		{
			eventLog.error(e);
		}

		eventLog.debug("end");
	}

	private Document writeTrackInRequest(Document doc, String lotName, String machineName, String portName, String recipeName, boolean glassTrackInFlag)
		throws CustomException
	{
		String messageName = "";

		if(glassTrackInFlag)
		{
			messageName = "TrackInGlass";
		}
		else
		{
			messageName = "TrackInLot";
		}

		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", messageName);
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

		Element element6 = new Element("LOTNAME");
		element6.setText(lotName);

		Element element7 = new Element("LOT");
		element7.addContent(element6);

		Element element8 = new Element("LOTLIST");
		element8.addContent(element7);
		eleBodyTemp.addContent(element8);

		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);

		return doc;
	}
}
