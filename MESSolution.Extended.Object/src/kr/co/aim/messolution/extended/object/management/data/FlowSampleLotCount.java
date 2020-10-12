package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FlowSampleLotCount extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "FACTORYNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String FACTORYNAME;
	
	@CTORMTemplate(seq = "2", name = "PRODUCTSPECNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String PRODUCTSPECNAME;
	
	@CTORMTemplate(seq = "3", name = "PROCESSFLOWNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String PROCESSFLOWNAME;
	
	@CTORMTemplate(seq = "4", name = "PROCESSOPERATIONNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String PROCESSOPERATIONNAME;
	
	@CTORMTemplate(seq = "5", name = "MACHINENAME", type = "Key", dataType = "String", initial = "", history = "")
	private String MACHINENAME;
	
	@CTORMTemplate(seq = "6", name = "TOPROCESSFLOWNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String TOPROCESSFLOWNAME;
	
	@CTORMTemplate(seq = "7", name = "TOPROCESSOPERATIONNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String TOPROCESSOPERATIONNAME;
	
	@CTORMTemplate(seq = "8", name = "LOTSAMPLECOUNT", type = "Column", dataType = "String", initial = "", history = "")
	private String LOTSAMPLECOUNT;
	
	@CTORMTemplate(seq = "9", name = "CURRENTLOTCOUNT", type = "Column", dataType = "String", initial = "", history = "")
	private String CURRENTLOTCOUNT;
	
	@CTORMTemplate(seq = "10", name = "TOTALLOTCOUNT", type = "Column", dataType = "String", initial = "", history = "")
	private String TOTALLOTCOUNT;
	
	public FlowSampleLotCount()
	{
		
	}
	
	public FlowSampleLotCount(String factoryName, String productSpecName, String processFlowName,
			String processOperationName, String machineName, String toProcessFlowName, String toProcessOperationName)
	{
		setFACTORYNAME(factoryName);
		setPRODUCTSPECNAME(productSpecName);
		setPROCESSFLOWNAME(processFlowName);
		setPROCESSOPERATIONNAME(processOperationName);
		setMACHINENAME(machineName);
		setTOPROCESSFLOWNAME(toProcessFlowName);
		setTOPROCESSOPERATIONNAME(toProcessOperationName);
	}

	public String getFACTORYNAME() {
		return FACTORYNAME;
	}

	public void setFACTORYNAME(String fACTORYNAME) {
		FACTORYNAME = fACTORYNAME;
	}

	public String getPRODUCTSPECNAME() {
		return PRODUCTSPECNAME;
	}

	public void setPRODUCTSPECNAME(String pRODUCTSPECNAME) {
		PRODUCTSPECNAME = pRODUCTSPECNAME;
	}

	public String getPROCESSFLOWNAME() {
		return PROCESSFLOWNAME;
	}

	public void setPROCESSFLOWNAME(String pROCESSFLOWNAME) {
		PROCESSFLOWNAME = pROCESSFLOWNAME;
	}

	public String getPROCESSOPERATIONNAME() {
		return PROCESSOPERATIONNAME;
	}

	public void setPROCESSOPERATIONNAME(String pROCESSOPERATIONNAME) {
		PROCESSOPERATIONNAME = pROCESSOPERATIONNAME;
	}

	public String getMACHINENAME() {
		return MACHINENAME;
	}

	public void setMACHINENAME(String mACHINENAME) {
		MACHINENAME = mACHINENAME;
	}

	public String getTOPROCESSFLOWNAME() {
		return TOPROCESSFLOWNAME;
	}

	public void setTOPROCESSFLOWNAME(String tOPROCESSFLOWNAME) {
		TOPROCESSFLOWNAME = tOPROCESSFLOWNAME;
	}

	public String getTOPROCESSOPERATIONNAME() {
		return TOPROCESSOPERATIONNAME;
	}

	public void setTOPROCESSOPERATIONNAME(String tOPROCESSOPERATIONNAME) {
		TOPROCESSOPERATIONNAME = tOPROCESSOPERATIONNAME;
	}

	public String getLOTSAMPLECOUNT() {
		return LOTSAMPLECOUNT;
	}

	public void setLOTSAMPLECOUNT(String lOTSAMPLECOUNT) {
		LOTSAMPLECOUNT = lOTSAMPLECOUNT;
	}

	public String getCURRENTLOTCOUNT() {
		return CURRENTLOTCOUNT;
	}

	public void setCURRENTLOTCOUNT(String cURRENTLOTCOUNT) {
		CURRENTLOTCOUNT = cURRENTLOTCOUNT;
	}

	public String getTOTALLOTCOUNT() {
		return TOTALLOTCOUNT;
	}

	public void setTOTALLOTCOUNT(String tOTALLOTCOUNT) {
		TOTALLOTCOUNT = tOTALLOTCOUNT;
	}
	
}
