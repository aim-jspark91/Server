package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ExposureFeedBack;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExposureFeedBackService extends CTORMService<ExposureFeedBack> {

    public static Log logger = LogFactory.getLog(ExposureFeedBackService.class);

    private final String historyEntity = "ExposureFeedBackHistory";

    public List<ExposureFeedBack> select(String condition, Object[] bindSet)
            throws CustomException {

        List<ExposureFeedBack> result = super.select(condition, bindSet,
                ExposureFeedBack.class);

        return result;
    }

    public ExposureFeedBack selectByKey(boolean isLock, Object[] keySet)
            throws CustomException, greenFrameDBErrorSignal {
        try
        {
            return super.selectByKey(ExposureFeedBack.class, isLock, keySet);
        }
        catch(greenFrameDBErrorSignal ns)
        {
            throw ns;
        }

    }

    public ExposureFeedBack create(EventInfo eventInfo, ExposureFeedBack dataInfo)
            throws CustomException {

        super.insert(dataInfo);

        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

        return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());

    }

    public void remove(EventInfo eventInfo, ExposureFeedBack dataInfo)
            throws CustomException {

        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

        super.delete(dataInfo);

    }

    public ExposureFeedBack modify(EventInfo eventInfo, ExposureFeedBack dataInfo)
            throws CustomException {
        super.update(dataInfo);

        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

        return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());

    }

    /**
     * @Name     getLastExposureFeedBackDataOfPhoto
     * @since    2018. 5. 28.
     * @author   hhlee
     * @contents Get Last Exposure FeedBack Data Of Photo
     * @param productName
     * @param lotName
     * @param productSpecName
     * @return
     * @throws CustomException
     */
    public ExposureFeedBack getLastExposureFeedBackDataOfPhoto(String productName, String lotName, String productSpecName) throws CustomException
    {
        ExposureFeedBack pExposureFeedBack = new ExposureFeedBack();
        try
        {
            String condition = StringUtil.EMPTY;
            condition = condition + "    PRODUCTNAME = ?                                       \n";
            condition = condition + "    AND LOTNAME = ?                                       \n";
            condition = condition + "    AND PRODUCTSPECNAME = ?                               \n";
            condition = condition + "    AND LASTEVENTTIME = (SELECT MAX(CEP.LASTEVENTTIME)    \n";
            condition = condition + "                           FROM CT_EXPOSUREFEEDBACK CEP   \n";
            condition = condition + "                          WHERE CEP.PRODUCTNAME = ?       \n";
            condition = condition + "                            AND CEP.LOTNAME = ?           \n";
            condition = condition + "                            AND CEP.PRODUCTSPECNAME = ?)";

            Object bindSet[] = new Object[]{productName, lotName, productSpecName, productName, lotName, productSpecName};
            List<ExposureFeedBack> pExposureFeedBackList = ExtendedObjectProxy.getExposureFeedBackService().select(condition, bindSet);

            if(pExposureFeedBackList.size() < 1)
            {
                pExposureFeedBack = null;
            }
            else
            {
                pExposureFeedBack = pExposureFeedBackList.get(0);
            }
        }
        catch (Exception ex)
        {
            logger.warn(String.format("Get LastExposureFeedBackData failed.[%s - %s - %s] ", productName, lotName, productSpecName));
            pExposureFeedBack = null;
        }

        return pExposureFeedBack;
    }
}

