
package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.service.LotInfoUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotFutureAction;
import kr.co.aim.greentrack.lot.management.data.LotFutureActionKey;
import kr.co.aim.greentrack.lot.management.data.LotFutureCondition;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.MakeOnHoldInfo;
import org.jdom.Document;
import org.jdom.Element;

public class AbnormalSheetAttachFile extends SyncHandler {
	/**
	 * 151106 by xzquan : Create CreateLot
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AbnormalSheetAttachFile", getEventUser(), getEventComment(), "", "");
		String abnormalSheetName = SMessageUtil.getBodyItemValue( doc, "ABNORMALSHEETNAME", true );
		String fileName = SMessageUtil.getBodyItemValue(doc, "FILENAME", true);
		String fileType = SMessageUtil.getBodyItemValue(doc, "FILETYPE", false);
		String fileSize = SMessageUtil.getBodyItemValue(doc, "FILESIZE", false);
		String filePath = SMessageUtil.getBodyItemValue(doc, "FILEPATH", false);
		String attachComment = SMessageUtil.getBodyItemValue(doc, "ATTACHCOMMENT", false);
		String aType = SMessageUtil.getBodyItemValue(doc, "ACTIONTYPE", false);
		
		if(aType.equals("Insert"))
		{
			eventInfo.setEventName("Insert Attach File.");
			eventInfo.setEventComment("Insert Attach File.");
			
			InsertAttachFile(abnormalSheetName, fileName, fileType, fileSize, filePath, attachComment, eventInfo);
		}
		else if (aType.equals("Delete"))
		{
			eventInfo.setEventName("Delete Attach File.");
			eventInfo.setEventComment("Delete Attach File.");

			DeleteAttachFile(abnormalSheetName, fileName);
		}

		return doc;

	}
	
	private void InsertAttachFile(String abnormalSheetName,  String fileName, String fileType, String fileSize, String filePath, String attachComment,
			EventInfo eventInfo) throws CustomException
	{	
		try
		{
			String sql = "INSERT INTO CT_ABNORMALSHEETATTACHFILE(ABNORMALSHEETNAME, FILENAME, FILETYPE, FILESIZE, FILEPATH, ATTACHCOMMENT, LASTEVENTUSER, LASTEVENTTIME) VALUES" +
			 " (:ABNORMALSHEETNAME, :FILENAME, :FILETYPE, :FILESIZE, :FILEPATH, :ATTACHCOMMENT, :LASTEVENTUSER, :LASTEVENTTIME)";
			
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("ABNORMALSHEETNAME", abnormalSheetName);
			bindMap.put("FILENAME", fileName);
			bindMap.put("FILETYPE", fileType);
			bindMap.put("FILESIZE", fileSize);
			bindMap.put("FILEPATH", filePath);
			bindMap.put("ATTACHCOMMENT", attachComment);
			bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
			
			int result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001","for insert " + fileName + " into CT_ABNORMALSHEETATTACHFILE   Error : " + e.toString());
		}
	}


	private void DeleteAttachFile(String abnormalSheetName,  String fileName) throws CustomException
	{	
		try
		{
			String sql = "DELETE FROM CT_ABNORMALSHEETATTACHFILE "
					   + " WHERE ABNORMALSHEETNAME = :ABNORMALSHEETNAME AND FILENAME = :FILENAME";
			
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("ABNORMALSHEETNAME", abnormalSheetName);
			bindMap.put("FILENAME", fileName);
			
			int result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001","for delete " + fileName + " from the CT_ABNORMALSHEETATTACHFILE   Error : " + e.toString());
		}
	}
}
