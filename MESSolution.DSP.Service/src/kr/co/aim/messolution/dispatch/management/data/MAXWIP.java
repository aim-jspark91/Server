package kr.co.aim.messolution.dispatch.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "CT", divider="_")
public class MAXWIP extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="ProductSpecName", type="Key", dataType="String", initial="", history="")
	private String ProductSpecName;
	
	@CTORMTemplate(seq = "3", name="ProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String ProcessOperationName;
	
	@CTORMTemplate(seq = "4", name="MaxWIP", type="Column", dataType="Number", initial="", history="")
	private Number MaxWIP;

 
	
	public MAXWIP()
	{
		
	}
	
	public MAXWIP(String ProductSpecName )
	{
		setProductSpecName(ProductSpecName);
	}

 	public String getProductSpecName() {
		return ProductSpecName;
	}

	public void setProductSpecName(String ProductSpecName) {
		this.ProductSpecName = ProductSpecName;
	}

	public String getProcessOperationName() {
		return ProcessOperationName;
	}

	public void setProcessOperationName(String ProcessOperationName) {
		this.ProcessOperationName = ProcessOperationName;
	}

	public Number getMaxWIP() {
		return MaxWIP;
	}

	public void setMaxWIP(Number MaxWIP) {
		this.MaxWIP = MaxWIP;
	}
 
}
