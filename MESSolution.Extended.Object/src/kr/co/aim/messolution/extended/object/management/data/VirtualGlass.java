package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class VirtualGlass extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="virtualGlassName", type="Key", dataType="String", initial="", history="")
	private String virtualGlassName;
	
	@CTORMTemplate(seq = "2", name="grade", type="Column", dataType="String", initial="", history="")
	private String grade;
	
	@CTORMTemplate(seq = "3", name="location", type="Column", dataType="String", initial="", history="")
	private String location;
	
	@CTORMTemplate(seq = "4", name="carrier", type="Column", dataType="String", initial="", history="")
	private String carrier;	
	
	@CTORMTemplate(seq = "5", name="position", type="Column", dataType="Number", initial="", history="")
	private long position;	
	
	@CTORMTemplate(seq = "6", name="productName", type="Column", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "7", name="productRequestName", type="Column", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "8", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "9", name="crateName", type="Column", dataType="String", initial="", history="")
	private String crateName;

	@CTORMTemplate(seq = "10", name="unitNameList", type="Column", dataType="String", initial="", history="")
    private String unitNameList;
    
    @CTORMTemplate(seq = "11", name="unitRecipeNameList", type="Column", dataType="String", initial="", history="")
    private String unitRecipeNameList;
    
    @CTORMTemplate(seq = "12", name="lotName", type="Column", dataType="String", initial="", history="")
    private String lotName;

	//instantiation
	public VirtualGlass()
	{
		
	}
	
	public VirtualGlass(String virtualGlassName)
	{
		setVirtualGlassName(virtualGlassName);
	}

	public String getVirtualGlassName() {
		return virtualGlassName;
	}

	public void setVirtualGlassName(String virtualGlassName) {
		this.virtualGlassName = virtualGlassName;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}
	
	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getCrateName() {
		return crateName;
	}

	public void setCrateName(String crateName) {
		this.crateName = crateName;
	}
	
	public long getPosition() {
		return position;
	}

	public void setPosition(long positon) {
		this.position = positon;
	}
	
	public String getUnitNameList() {
        return unitNameList;
    }

    public void setUnitNameList(String unitNameList) {
        this.unitNameList = unitNameList;
    }
    
    public String getUnitRecipeNameList() {
        return unitRecipeNameList;
    }

    public void setUnitRecipeNameList(String unitRecipeNameList) {
        this.unitRecipeNameList = unitRecipeNameList;
    }
    
    public String getLotName() {
        return lotName;
    }

    public void setLotName(String lotName) {
        this.lotName = lotName;
    }
}
