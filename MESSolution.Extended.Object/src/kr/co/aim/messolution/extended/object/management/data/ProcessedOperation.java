package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProcessedOperation extends UdfAccessor {
    
    @CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
    private String productName;
    
    @CTORMTemplate(seq = "2", name="attribute1", type="Column", dataType="String", initial="", history="")
    private String attribute1;
    
    @CTORMTemplate(seq = "3", name="attribute2", type="Column", dataType="String", initial="", history="")
    private String attribute2;
    
    @CTORMTemplate(seq = "4", name="attribute3", type="Column", dataType="String", initial="", history="")
    private String attribute3; 
    
    @CTORMTemplate(seq = "5", name="attribute4", type="Column", dataType="String", initial="", history="")
    private String attribute4;  
    
    @CTORMTemplate(seq = "6", name="attribute5", type="Column", dataType="String", initial="", history="")
    private String attribute5;
    
    @CTORMTemplate(seq = "7", name="unitNameList", type="Column", dataType="String", initial="", history="")
    private String unitNameList;
    
    @CTORMTemplate(seq = "8", name="unitRecipeNameList", type="Column", dataType="String", initial="", history="")
    private String unitRecipeNameList;

    //instantiation
    public ProcessedOperation()
    {
        
    }
    
    public ProcessedOperation(String ProductName)
    {
        setProductName(ProductName);
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAttribute1() {
        return attribute1;
    }

    public void setAttribute1(String attribute1) {
        this.attribute1 = attribute1;
    }

    public String getAttribute2() {
        return attribute2;
    }

    public void setAttribute2(String attribute2) {
        this.attribute2 = attribute2;
    }
    
    public String getAttribute3() {
        return attribute3;
    }

    public void setAttribute3(String attribute3) {
        this.attribute3 = attribute3;
    }
    
    public String getAttribute4() {
        return attribute4;
    }

    public void setAttribute4(String attribute4) {
        this.attribute4 = attribute4;
    }
    
    public String getAttribute5() {
        return attribute5;
    }

    public void setAttribute5(String attribute5) {
        this.attribute5 = attribute5;
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
}