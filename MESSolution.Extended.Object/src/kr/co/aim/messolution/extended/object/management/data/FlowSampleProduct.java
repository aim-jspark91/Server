package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FlowSampleProduct extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "PRODUCTNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String PRODUCTNAME;
	
	@CTORMTemplate(seq = "2", name = "LOTNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String LOTNAME;
	
	@CTORMTemplate(seq = "3", name = "FACTORYNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String FACTORYNAME;
	
	@CTORMTemplate(seq = "4", name = "PRODUCTSPECNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String PRODUCTSPECNAME;
	
	@CTORMTemplate(seq = "5", name = "PROCESSFLOWNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String PROCESSFLOWNAME;
	
	@CTORMTemplate(seq = "6", name = "PROCESSOPERATIONNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String PROCESSOPERATIONNAME;
	
	@CTORMTemplate(seq = "7", name = "MACHINENAME", type = "Key", dataType = "String", initial = "", history = "")
	private String MACHINENAME;
	
	@CTORMTemplate(seq = "8", name = "TOPROCESSFLOWNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String TOPROCESSFLOWNAME;
	
	@CTORMTemplate(seq = "9", name = "TOPROCESSOPERATIONNAME", type = "Key", dataType = "String", initial = "", history = "")
	private String TOPROCESSOPERATIONNAME;
	
	@CTORMTemplate(seq = "10", name = "PRODUCTSAMPLEFLAG", type = "Column", dataType = "String", initial = "", history = "")
	private String PRODUCTSAMPLEFLAG;
	
	@CTORMTemplate(seq = "11", name = "PRODUCTSAMPLECOUNT", type = "Column", dataType = "String", initial = "", history = "")
	private String PRODUCTSAMPLECOUNT;
	
	@CTORMTemplate(seq = "12", name = "PRODUCTSAMPLEPOSITION", type = "Column", dataType = "String", initial = "", history = "")
	private String PRODUCTSAMPLEPOSITION;
	
	@CTORMTemplate(seq = "13", name = "ACTUALSAMPLEPOSITION", type = "Column", dataType = "String", initial = "", history = "")
	private String ACTUALSAMPLEPOSITION;
	
	@CTORMTemplate(seq = "14", name = "MANUALSAMPLEFLAG", type = "Column", dataType = "String", initial = "", history = "")
	private String MANUALSAMPLEFLAG;
	
	@CTORMTemplate(seq = "15", name = "LASTEVENTUSER", type = "Column", dataType = "String", initial = "", history = "N")
	private String LASTEVENTUSER;
	
	@CTORMTemplate(seq = "16", name = "LASTEVENTCOMMENT", type = "Column", dataType = "String", initial = "", history = "N")
	private String LASTEVENTCOMMENT;
	
	@CTORMTemplate(seq = "17", name = "LASTEVENTTIME", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp LASTEVENTTIME;
	
	@CTORMTemplate(seq = "18", name = "LASTEVENTNAME", type = "Column", dataType = "String", initial = "", history = "N")
	private String LASTEVENTNAME;
	
	@CTORMTemplate(seq = "19", name = "MACHINERECIPENAME", type = "Column", dataType = "String", initial = "", history = "")
	private String MACHINERECIPENAME;
	
	public FlowSampleProduct()
	{
		
	}
	
	public FlowSampleProduct(String productName, String lotName, String factoryName, String productSpecName,
			String processFlowName, String processOperationName, String machineName,
			String toProcessFlowName, String toProcessOperationName)
	{
		setPRODUCTNAME(productName);
		setLOTNAME(lotName);
		setFACTORYNAME(factoryName);
		setPRODUCTSPECNAME(productSpecName);
		setPROCESSFLOWNAME(processFlowName);
		setPROCESSOPERATIONNAME(processOperationName);
		setMACHINENAME(machineName);
		setTOPROCESSFLOWNAME(toProcessFlowName);
		setTOPROCESSOPERATIONNAME(toProcessOperationName);
	}

	public String getPRODUCTNAME() {
		return PRODUCTNAME;
	}

	public void setPRODUCTNAME(String pRODUCTNAME) {
		PRODUCTNAME = pRODUCTNAME;
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

	public String getPRODUCTSAMPLEFLAG() {
		return PRODUCTSAMPLEFLAG;
	}

	public void setPRODUCTSAMPLEFLAG(String pRODUCTSAMPLEFLAG) {
		PRODUCTSAMPLEFLAG = pRODUCTSAMPLEFLAG;
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
	
	public String getMACHINERECIPENAME() {
		return MACHINERECIPENAME;
	}

	public void setMACHINERECIPENAME(String mACHINERECIPENAME) {
		MACHINERECIPENAME = mACHINERECIPENAME;
	}
}
