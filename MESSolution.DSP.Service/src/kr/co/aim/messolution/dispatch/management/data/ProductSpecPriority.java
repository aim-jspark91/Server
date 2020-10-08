package kr.co.aim.messolution.dispatch.management.data;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "CT", divider="_")
public class ProductSpecPriority extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "2", name="ProductSpecName", type="Key", dataType="String", initial="", history="")
	private String ProductSpecName;
	
	@CTORMTemplate(seq = "3", name="Priority", type="Column", dataType="Number", initial="", history="")
	private Number Priority;

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	
	public String getProductSpecName() {
		return ProductSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		ProductSpecName = productSpecName;
	}

	public Number getPriority() {
		return Priority;
	}

	public void setPriority(Number priority) {
		Priority = priority;
	}
	
}
