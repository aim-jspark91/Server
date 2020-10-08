package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FlowSampleLot extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name = "LOTNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String LOTNAME;
	
	@CTORMTemplate(seq = "2", name = "FACTORYNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String FACTORYNAME;
	
	@CTORMTemplate(seq = "3", name = "PRODUCTSPECNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String PRODUCTSPECNAME;
	
	@CTORMTemplate(seq = "4", name = "PROCESSFLOWNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String PROCESSFLOWNAME;
	
	@CTORMTemplate(seq = "5", name = "PROCESSOPERATIONNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String PROCESSOPERATIONNAME;
	
	@CTORMTemplate(seq = "6", name = "MACHINENAME", type = "Key", dataType = "String", initial = "", history = "")
	private String MACHINENAME;
	
	@CTORMTemplate(seq = "7", name = "TOPROCESSFLOWNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String TOPROCESSFLOWNAME;
	
	@CTORMTemplate(seq = "8", name = "TOPROCESSOPERATIONNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String TOPROCESSOPERATIONNAME;
	
	@CTORMTemplate(seq = "9", name = "LOTSAMPLEFLAG", type = "Column", dataType = "String", initial = "", history = "")
	private String LOTSAMPLEFLAG;
	
	@CTORMTemplate(seq = "10", name = "LOTSAMPLECOUNT", type = "Column", dataType = "String", initial = "", history = "")
	private String LOTSAMPLECOUNT;
	
	@CTORMTemplate(seq = "11", name = "CURRENTLOTCOUNT", type = "Column", dataType = "String", initial = "", history = "")
	private String CURRENTLOTCOUNT;
	
	@CTORMTemplate(seq = "12", name = "TOTALLOTCOUNT", type = "Column", dataType = "String", initial = "", history = "")
	private String TOTALLOTCOUNT;
	
	@CTORMTemplate(seq = "13", name = "PRODUCTSAMPLECOUNT", type = "Column", dataType = "String", initial = "", history = "")
	private String PRODUCTSAMPLECOUNT;
	
	@CTORMTemplate(seq = "14", name = "PRODUCTSAMPLEPOSITION", type = "Column", dataType = "String", initial = "", history = "")
	private String PRODUCTSAMPLEPOSITION;
	
	@CTORMTemplate(seq = "15", name = "ACTUALPRODUCTCOUNT", type = "Column", dataType = "String", initial = "", history = "")
	private String ACTUALPRODUCTCOUNT;
	
	@CTORMTemplate(seq = "16", name = "ACTUALSAMPLEPOSITION", type = "Column", dataType = "String", initial = "", history = "")
	private String ACTUALSAMPLEPOSITION;

	@CTORMTemplate(seq = "17", name = "MANUALSAMPLEFLAG", type = "Column", dataType = "String", initial = "", history = "")
	private String MANUALSAMPLEFLAG;
	
	@CTORMTemplate(seq = "18", name = "LASTEVENTUSER", type = "Column", dataType = "String", initial = "", history = "N")
	private String LASTEVENTUSER;
	
	@CTORMTemplate(seq = "19", name = "LASTEVENTCOMMENT", type = "Column", dataType = "String", initial = "", history = "N")
	private String LASTEVENTCOMMENT;
	
	@CTORMTemplate(seq = "20", name = "REASONCODE", type = "Column", dataType = "String", initial = "", history = "")
	private String REASONCODE;
	
	@CTORMTemplate(seq = "21", name = "REASONCODETYPE", type = "Column", dataType = "String", initial = "", history = "")
	private String REASONCODETYPE;
	
	@CTORMTemplate(seq = "22", name = "LASTEVENTTIME", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp LASTEVENTTIME;
	
	@CTORMTemplate(seq = "23", name = "LASTEVENTNAME", type = "Column", dataType = "String", initial = "", history = "N")
	private String LASTEVENTNAME;
	
	public FlowSampleLot()
	{
		
	}
	
	public FlowSampleLot(String lotName, String factoryName, String productSpecName,
			String processFlowName, String processOperationName, String machineName,
			String toProcessFlowName, String toProcessOperationName)
	{
		setLOTNAME(lotName);
		setFACTORYNAME(factoryName);
		setPRODUCTSPECNAME(productSpecName);
		setPROCESSFLOWNAME(processFlowName);
		setPROCESSOPERATIONNAME(processOperationName);
		setMACHINENAME(machineName);
		setTOPROCESSFLOWNAME(toProcessFlowName);
		setTOPROCESSOPERATIONNAME(toProcessOperationName);
	}
	
	public String getLOTNAME() {
		return LOTNAME;
	}

	public void setLOTNAME(String lOTNAME) {
		LOTNAME = lOTNAME;
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

	public String getLOTSAMPLEFLAG() {
		return LOTSAMPLEFLAG;
	}

	public void setLOTSAMPLEFLAG(String lOTSAMPLEFLAG) {
		LOTSAMPLEFLAG = lOTSAMPLEFLAG;
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

	public String getPRODUCTSAMPLECOUNT() {
		return PRODUCTSAMPLECOUNT;
	}

	public void setPRODUCTSAMPLECOUNT(String pRODUCTSAMPLECOUNT) {
		PRODUCTSAMPLECOUNT = pRODUCTSAMPLECOUNT;
	}

	public String getPRODUCTSAMPLEPOSITION() {
		return PRODUCTSAMPLEPOSITION;
	}

	public void setPRODUCTSAMPLEPOSITION(String pRODUCTSAMPLEPOSITION) {
		PRODUCTSAMPLEPOSITION = pRODUCTSAMPLEPOSITION;
	}

	public String getACTUALPRODUCTCOUNT() {
		return ACTUALPRODUCTCOUNT;
	}

	public void setACTUALPRODUCTCOUNT(String aCTUALPRODUCTCOUNT) {
		ACTUALPRODUCTCOUNT = aCTUALPRODUCTCOUNT;
	}

	public String getACTUALSAMPLEPOSITION() {
		return ACTUALSAMPLEPOSITION;
	}

	public void setACTUALSAMPLEPOSITION(String aCTUALSAMPLEPOSITION) {
		ACTUALSAMPLEPOSITION = aCTUALSAMPLEPOSITION;
	}

	public String getMANUALSAMPLEFLAG() {
		return MANUALSAMPLEFLAG;
	}

	public void setMANUALSAMPLEFLAG(String mANUALSAMPLEFLAG) {
		MANUALSAMPLEFLAG = mANUALSAMPLEFLAG;
	}

	public String getLASTEVENTUSER() {
		return LASTEVENTUSER;
	}

	public void setLASTEVENTUSER(String lASTEVENTUSER) {
		LASTEVENTUSER = lASTEVENTUSER;
	}

	public String getLASTEVENTCOMMENT() {
		return LASTEVENTCOMMENT;
	}

	public void setLASTEVENTCOMMENT(String lASTEVENTCOMMENT) {
		LASTEVENTCOMMENT = lASTEVENTCOMMENT;
	}
	
	public String getREASONCODE() {
		return REASONCODE;
	}

	public void setREASONCODE(String rEASONCODE) {
		REASONCODE = rEASONCODE;
	}
	
	public String getREASONCODETYPE() {
		return REASONCODETYPE;
	}

	public void setREASONCODETYPE(String rEASONCODETYPE) {
		REASONCODETYPE = rEASONCODETYPE;
	}
	
	public Timestamp getLASTEVENTTIME() {
		return LASTEVENTTIME;
	}

	public void setLASTEVENTTIME(Timestamp lASTEVENTTIME) {
		LASTEVENTTIME = lASTEVENTTIME;
	}

	public String getLASTEVENTNAME() {
		return LASTEVENTNAME;
	}

	public void setLASTEVENTNAME(String lASTEVENTNAME) {
		LASTEVENTNAME = lASTEVENTNAME;
	}

}
