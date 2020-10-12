package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FirstGlassLog extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="id", type="Key", dataType="String", initial="", history="")
	private String id;
	
	@CTORMTemplate(seq = "2", name="timeKey", type="Column", dataType="String", initial="", history="")
	private String timeKey;
	
	@CTORMTemplate(seq = "3", name="eventDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp eventDate;
	
	@CTORMTemplate(seq = "4", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "5", name="recipeId", type="Column", dataType="String", initial="", history="")
	private String recipeId;
	
	@CTORMTemplate(seq = "6", name="tactTime", type="Column", dataType="String", initial="", history="")
	private String tactTime;
	
	@CTORMTemplate(seq = "7", name="barCode", type="Column", dataType="String", initial="", history="")
	private String barCode;
	
	@CTORMTemplate(seq = "8", name="hmds", type="Column", dataType="String", initial="", history="")
	private String hmds;
	
	@CTORMTemplate(seq = "9", name="glassThickness", type="Column", dataType="String", initial="", history="")
	private String glassThickness;
	
	@CTORMTemplate(seq = "10", name="aoiDefectQuantity", type="Column", dataType="String", initial="", history="")
	private String aoiDefectQuantity;
	
	@CTORMTemplate(seq = "11", name="aoiInspectionMachine", type="Column", dataType="String", initial="", history="")
	private String aoiInspectionMachine;
	
	@CTORMTemplate(seq = "12", name="lineWidthMax", type="Column", dataType="String", initial="", history="")
	private String lineWidthMax;
	
	@CTORMTemplate(seq = "13", name="lineWidthAverage", type="Column", dataType="String", initial="", history="")
	private String lineWidthAverage;
	
	@CTORMTemplate(seq = "14", name="lineWidthMin", type="Column", dataType="String", initial="", history="")
	private String lineWidthMin;
	
	@CTORMTemplate(seq = "15", name="lineWidthInspectionMachine", type="Column", dataType="String", initial="", history="")
	private String lineWidthInspectionMachine;
	
	@CTORMTemplate(seq = "16", name="overlayMax", type="Column", dataType="String", initial="", history="")
	private String overlayMax;
	
	@CTORMTemplate(seq = "17", name="overlayAverage", type="Column", dataType="String", initial="", history="")
	private String overlayAverage;
	
	@CTORMTemplate(seq = "18", name="overlayMin", type="Column", dataType="String", initial="", history="")
	private String overlayMin;
	
	@CTORMTemplate(seq = "19", name="overlayPerfect", type="Column", dataType="String", initial="", history="")
	private String overlayPerfect;
	
	@CTORMTemplate(seq = "20", name="exposureSpeed", type="Column", dataType="String", initial="", history="")
	private String exposureSpeed;
	
	@CTORMTemplate(seq = "21", name="illuminance", type="Column", dataType="String", initial="", history="")
	private String illuminance;
	
	@CTORMTemplate(seq = "22", name="esdExist", type="Column", dataType="String", initial="", history="")
	private String esdExist;
	
	@CTORMTemplate(seq = "23", name="existAroundPhoto", type="Column", dataType="String", initial="", history="")
	private String existAroundPhoto;
	
	@CTORMTemplate(seq = "24", name="perfectMark", type="Column", dataType="String", initial="", history="")
	private String perfectMark;
	
	@CTORMTemplate(seq = "25", name="exposureProgramName", type="Column", dataType="String", initial="", history="")
	private String exposureProgramName;
	
	@CTORMTemplate(seq = "26", name="judge", type="Column", dataType="String", initial="", history="")
	private String judge;
	
	@CTORMTemplate(seq = "27", name="operator", type="Column", dataType="String", initial="", history="")
	private String operator;
	
	@CTORMTemplate(seq = "28", name="exposureMachineName", type="Column", dataType="String", initial="", history="")
	private String exposureMachineName;
	
	@CTORMTemplate(seq = "29", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTimeKey() {
		return timeKey;
	}

	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}

	public Timestamp getEventDate() {
		return eventDate;
	}

	public void setEventDate(Timestamp eventDate) {
		this.eventDate = eventDate;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(String recipeId) {
		this.recipeId = recipeId;
	}

	public String getTactTime() {
		return tactTime;
	}

	public void setTactTime(String tactTime) {
		this.tactTime = tactTime;
	}

	public String getBarCode() {
		return barCode;
	}

	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}

	public String getHmds() {
		return hmds;
	}

	public void setHmds(String hmds) {
		this.hmds = hmds;
	}

	public String getGlassThickness() {
		return glassThickness;
	}

	public void setGlassThickness(String glassThickness) {
		this.glassThickness = glassThickness;
	}

	public String getAoiDefectQuantity() {
		return aoiDefectQuantity;
	}

	public void setAoiDefectQuantity(String aoiDefectQuantity) {
		this.aoiDefectQuantity = aoiDefectQuantity;
	}

	public String getAoiInspectionMachine() {
		return aoiInspectionMachine;
	}

	public void setAoiInspectionMachine(String aoiInspectionMachine) {
		this.aoiInspectionMachine = aoiInspectionMachine;
	}

	public String getLineWidthMax() {
		return lineWidthMax;
	}

	public void setLineWidthMax(String lineWidthMax) {
		this.lineWidthMax = lineWidthMax;
	}

	public String getLineWidthAverage() {
		return lineWidthAverage;
	}

	public void setLineWidthAverage(String lineWidthAverage) {
		this.lineWidthAverage = lineWidthAverage;
	}

	public String getLineWidthMin() {
		return lineWidthMin;
	}

	public void setLineWidthMin(String lineWidthMin) {
		this.lineWidthMin = lineWidthMin;
	}

	public String getLineWidthInspectionMachine() {
		return lineWidthInspectionMachine;
	}

	public void setLineWidthInspectionMachine(String lineWidthInspectionMachine) {
		this.lineWidthInspectionMachine = lineWidthInspectionMachine;
	}

	public String getOverlayMax() {
		return overlayMax;
	}

	public void setOverlayMax(String overlayMax) {
		this.overlayMax = overlayMax;
	}

	public String getOverlayAverage() {
		return overlayAverage;
	}

	public void setOverlayAverage(String overlayAverage) {
		this.overlayAverage = overlayAverage;
	}

	public String getOverlayMin() {
		return overlayMin;
	}

	public void setOverlayMin(String overlayMin) {
		this.overlayMin = overlayMin;
	}

	public String getOverlayPerfect() {
		return overlayPerfect;
	}

	public void setOverlayPerfect(String overlayPerfect) {
		this.overlayPerfect = overlayPerfect;
	}

	public String getExposureSpeed() {
		return exposureSpeed;
	}

	public void setExposureSpeed(String exposureSpeed) {
		this.exposureSpeed = exposureSpeed;
	}

	public String getIlluminance() {
		return illuminance;
	}

	public void setIlluminance(String illuminance) {
		this.illuminance = illuminance;
	}

	public String getEsdExist() {
		return esdExist;
	}

	public void setEsdExist(String esdExist) {
		this.esdExist = esdExist;
	}

	public String getExistAroundPhoto() {
		return existAroundPhoto;
	}

	public void setExistAroundPhoto(String existAroundPhoto) {
		this.existAroundPhoto = existAroundPhoto;
	}

	public String getPerfectMark() {
		return perfectMark;
	}

	public void setPerfectMark(String perfectMark) {
		this.perfectMark = perfectMark;
	}

	public String getExposureProgramName() {
		return exposureProgramName;
	}

	public void setExposureProgramName(String exposureProgramName) {
		this.exposureProgramName = exposureProgramName;
	}

	public String getJudge() {
		return judge;
	}

	public void setJudge(String judge) {
		this.judge = judge;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getExposureMachineName() {
		return exposureMachineName;
	}

	public void setExposureMachineName(String exposureMachineName) {
		this.exposureMachineName = exposureMachineName;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	
	
}
