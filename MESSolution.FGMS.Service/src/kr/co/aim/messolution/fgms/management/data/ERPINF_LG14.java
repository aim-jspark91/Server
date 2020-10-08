package kr.co.aim.messolution.fgms.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ERPINF_LG14 extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="ERDAT", type="Key", dataType="String", initial="", history="")
	private String ERDAT;

	@CTORMTemplate(seq = "2", name="ERUZE", type="Key", dataType="String", initial="", history="")
	private String ERUZE;
	
	@CTORMTemplate(seq = "3", name="FLAG", type="Key", dataType="String", initial="", history="")
	private String FLAG;
	
	@CTORMTemplate(seq = "4", name="PALLET", type="Key", dataType="String", initial="", history="")
	private String PALLET;
	
	@CTORMTemplate(seq = "5", name="POSNR", type="Key", dataType="String", initial="", history="")
	private String POSNR;
	
	@CTORMTemplate(seq = "6", name="VBELN", type="Key", dataType="String", initial="", history="")
	private String VBELN;//KEY PART
	
	@CTORMTemplate(seq = "7", name="ANZPK", type="Column", dataType="String", initial="", history="")
	private String ANZPK;
	
	@CTORMTemplate(seq = "8", name="BRGEW", type="Column", dataType="String", initial="", history="")
	private String BRGEW;
	
	@CTORMTemplate(seq = "9", name="CHARG", type="Column", dataType="String", initial="", history="")
	private String CHARG;
	
	@CTORMTemplate(seq = "10", name="CREATETIME", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp CREATETIME;
	
	@CTORMTemplate(seq = "11", name="GEWEI", type="Column", dataType="String", initial="", history="")
	private String GEWEI;

	@CTORMTemplate(seq = "12", name="INF_RESULTCODE", type="Column", dataType="String", initial="", history="")
	private String INF_RESULTCODE;
	
	@CTORMTemplate(seq = "13", name="INF_RESULTMESSAGE", type="Column", dataType="String", initial="", history="")
	private String INF_RESULTMESSAGE;
	
	@CTORMTemplate(seq = "14", name="INTERFACETIME", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp INTERFACETIME;
	
	@CTORMTemplate(seq = "15", name="LFIMG", type="Column", dataType="Number", initial="", history="")
	private Number LFIMG;
	
	@CTORMTemplate(seq = "16", name="MATNR", type="Column", dataType="String", initial="", history="")
	private String MATNR;
	
	@CTORMTemplate(seq = "17", name="MEINS", type="Column", dataType="String", initial="", history="")
	private String MEINS;
	
	@CTORMTemplate(seq = "18", name="NTGEW", type="Column", dataType="String", initial="", history="")
	private String NTGEW;
	
	@CTORMTemplate(seq = "19", name="RESULTCODE", type="Column", dataType="String", initial="", history="")
	private String RESULTCODE;
	
	@CTORMTemplate(seq = "20", name="RESULTMESSAGE", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp RESULTMESSAGE;
	
	@CTORMTemplate(seq = "21", name="SEQ", type="Column", dataType="Number", initial="", history="")
	private Number SEQ;
	
	@CTORMTemplate(seq = "22", name="VOLEH", type="Column", dataType="String", initial="", history="")
	private String VOLEH;
	
	@CTORMTemplate(seq = "23", name="VOLUM", type="Column", dataType="String", initial="", history="")
	private String VOLUM;
	
	@CTORMTemplate(seq = "24", name="WADAT", type="Column", dataType="String", initial="", history="")
	private String WADAT;
	
	@CTORMTemplate(seq = "25", name="XABLN", type="Column", dataType="String", initial="", history="")
	private String XABLN;
	
	//instantiation
	public ERPINF_LG14()
	{
		
	}
	
	public ERPINF_LG14(String ERDAT, String ERUZE, String FLAG, String PALLET, String POSNR, String VBELN)
	{
		setERDAT(ERDAT);
		setERUZE(ERUZE);
		setFLAG(FLAG);
		setPALLET(PALLET);
		setPOSNR(POSNR);
		setVBELN(VBELN);
	}

	public String getERDAT() {
		return ERDAT;
	}

	public void setERDAT(String eRDAT) {
		ERDAT = eRDAT;
	}

	public String getERUZE() {
		return ERUZE;
	}

	public void setERUZE(String eRUZE) {
		ERUZE = eRUZE;
	}

	public String getFLAG() {
		return FLAG;
	}

	public void setFLAG(String fLAG) {
		FLAG = fLAG;
	}

	public String getPALLET() {
		return PALLET;
	}

	public void setPALLET(String pALLET) {
		PALLET = pALLET;
	}

	public String getPOSNR() {
		return POSNR;
	}

	public void setPOSNR(String pOSNR) {
		POSNR = pOSNR;
	}

	public String getVBELN() {
		return VBELN;
	}

	public void setVBELN(String vBELN) {
		VBELN = vBELN;
	}

	public String getANZPK() {
		return ANZPK;
	}

	public void setANZPK(String aNZPK) {
		ANZPK = aNZPK;
	}

	public String getBRGEW() {
		return BRGEW;
	}

	public void setBRGEW(String bRGEW) {
		BRGEW = bRGEW;
	}

	public String getCHARG() {
		return CHARG;
	}

	public void setCHARG(String cHARG) {
		CHARG = cHARG;
	}

	public Timestamp getCREATETIME() {
		return CREATETIME;
	}

	public void setCREATETIME(Timestamp cREATETIME) {
		CREATETIME = cREATETIME;
	}

	public String getGEWEI() {
		return GEWEI;
	}

	public void setGEWEI(String gEWEI) {
		GEWEI = gEWEI;
	}

	public String getINF_RESULTCODE() {
		return INF_RESULTCODE;
	}

	public void setINF_RESULTCODE(String iNF_RESULTCODE) {
		INF_RESULTCODE = iNF_RESULTCODE;
	}

	public String getINF_RESULTMESSAGE() {
		return INF_RESULTMESSAGE;
	}

	public void setINF_RESULTMESSAGE(String iNF_RESULTMESSAGE) {
		INF_RESULTMESSAGE = iNF_RESULTMESSAGE;
	}

	public Timestamp getINTERFACETIME() {
		return INTERFACETIME;
	}

	public void setINTERFACETIME(Timestamp iNTERFACETIME) {
		INTERFACETIME = iNTERFACETIME;
	}

	public Number getLFIMG() {
		return LFIMG;
	}

	public void setLFIMG(Number lFIMG) {
		LFIMG = lFIMG;
	}

	public String getMATNR() {
		return MATNR;
	}

	public void setMATNR(String mATNR) {
		MATNR = mATNR;
	}

	public String getMEINS() {
		return MEINS;
	}

	public void setMEINS(String mEINS) {
		MEINS = mEINS;
	}

	public String getNTGEW() {
		return NTGEW;
	}

	public void setNTGEW(String nTGEW) {
		NTGEW = nTGEW;
	}

	public String getRESULTCODE() {
		return RESULTCODE;
	}

	public void setRESULTCODE(String rESULTCODE) {
		RESULTCODE = rESULTCODE;
	}

	public Timestamp getRESULTMESSAGE() {
		return RESULTMESSAGE;
	}

	public void setRESULTMESSAGE(Timestamp rESULTMESSAGE) {
		RESULTMESSAGE = rESULTMESSAGE;
	}

	public Number getSEQ() {
		return SEQ;
	}

	public void setSEQ(Number sEQ) {
		SEQ = sEQ;
	}

	public String getVOLEH() {
		return VOLEH;
	}

	public void setVOLEH(String vOLEH) {
		VOLEH = vOLEH;
	}

	public String getVOLUM() {
		return VOLUM;
	}

	public void setVOLUM(String vOLUM) {
		VOLUM = vOLUM;
	}

	public String getWADAT() {
		return WADAT;
	}

	public void setWADAT(String wADAT) {
		WADAT = wADAT;
	}

	public String getXABLN() {
		return XABLN;
	}

	public void setXABLN(String xABLN) {
		XABLN = xABLN;
	}
}
