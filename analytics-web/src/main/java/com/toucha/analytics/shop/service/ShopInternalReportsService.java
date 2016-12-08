package com.toucha.analytics.shop.service;

import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.shop.dao.ShopInternalReportsDao;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.InternalReportCard;
import com.toucha.analytics.common.model.InternalReportData;
import com.toucha.analytics.common.model.InternalReportDataGroup;
import com.toucha.analytics.common.model.InternalReportGroup;
import com.toucha.analytics.common.util.DateHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ShopInternalReportsService {

    private ShopInternalReportsDao internalReportDao = new ShopInternalReportsDao();
    
    @Cacheable(value = "internalCache", key = "'internal-report'+#companyId")
    public InternalReportGroup getAllReports(int companyId) throws ServiceException {
        InternalReportGroup group = new InternalReportGroup();
        group.setD(DateHelper.DateToStr(DateHelper.getGMTadd8()));
        List<InternalReportCard> cards = new ArrayList<InternalReportCard>();
        try {
            // Scan QrCode Statis
            InternalReportCard scanCard = new InternalReportCard();
            scanCard.setT(ApplicationConfig.INTERNAL_SCAN_TITLE);
            scanCard.setC(Arrays.asList(
                    getNormalScanReports(companyId), 
                    getValidScanReports(companyId), 
                    getNewUserScanReports(companyId), 
                    getOldUserScanReports(companyId)));
            cards.add(scanCard);
            
            // Members Statis
            InternalReportCard memberCard = new InternalReportCard();
            memberCard.setT(ApplicationConfig.INTERNAL_MEMBER_TITLE);
            memberCard.setC(Arrays.asList(getUserReports(companyId)));
            cards.add(memberCard);
            
            // Market Statis
            InternalReportCard marketCard = new InternalReportCard();
            marketCard.setT(ApplicationConfig.INTERNAL_MARKET_TITLE);
            marketCard.setC(Arrays.asList(
                    getAwardCountsReports(companyId), 
                    getAwardMoniesReports(companyId), 
                    getAwardPhonebillCountReports(companyId), 
                    getAwardUnionpayCountReports(companyId), 
                    getAwardWechatCountReports(companyId),
                    getAwardPhonebillMoneyReports(companyId), 
                    getAwardUnionpayMoneyReports(companyId), 
                    getAwardWechatMoneyReports(companyId),
                    getAwardPointsReports(companyId)));
            cards.add(marketCard);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
        
        group.setG(cards);
        return group;
    }
    
    private InternalReportDataGroup getNormalScanReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getNormalScanStatis(companyId);
            return getStatis(ApplicationConfig.INTERNAL_NORMAL_SCAN_TITLE, ApplicationConfig.INTERNAL_SCAN, statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getValidScanReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getValidScanStatis(companyId);
            return getStatis(ApplicationConfig.INTERNAL_VALID_SCAN_TITLE, ApplicationConfig.INTERNAL_SCAN, statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getNewUserScanReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getNewUserScanStatis(companyId);
            return getStatis(ApplicationConfig.INTERNAL_NEWUSER_SCAN_TITLE, ApplicationConfig.INTERNAL_SCAN, statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getOldUserScanReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getOldUserScanStatis(companyId);
            return getStatis(ApplicationConfig.INTERNAL_OLDUSER_SCAN_TITLE, ApplicationConfig.INTERNAL_SCAN, statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getUserReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getUserStatis(companyId);
            return getStatis(ApplicationConfig.INTERNAL_MEMBER_TITLE, ApplicationConfig.INTERNAL_MEMBER,  statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getAwardCountsReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getAwardCounts(companyId);
            return getStatis(ApplicationConfig.INTERNAL_MARKET_AWARD_COUNTS, ApplicationConfig.INTERNAL_MARKET_AWARD_COUNTS,  statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getAwardMoniesReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getAwardMonies(companyId);
            return getStatis(ApplicationConfig.INTERNAL_MARKET_AWARD_MONEY, ApplicationConfig.INTERNAL_MARKET_AWARD_MONEY,  statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    private InternalReportDataGroup getAwardPhonebillCountReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getAwardPhonebillCount(companyId);
            return getStatis(ApplicationConfig.INTERNAL_MARKET_AWARD_PHONEBILL_COUNTS, ApplicationConfig.INTERNAL_MARKET_AWARD_PHONEBILL_COUNTS,  statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getAwardUnionpayCountReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getAwardUnionPayCount(companyId);
            return getStatis(ApplicationConfig.INTERNAL_MARKET_AWARD_UNIONPAY_COUNTS, ApplicationConfig.INTERNAL_MARKET_AWARD_UNIONPAY_COUNTS,  statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getAwardWechatCountReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getAwardWeChatCount(companyId);
            return getStatis(ApplicationConfig.INTERNAL_MARKET_AWARD_WECHAT_COUNTS, ApplicationConfig.INTERNAL_MARKET_AWARD_WECHAT_COUNTS,  statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getAwardPhonebillMoneyReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getAwardPhonebillMoney(companyId);
            return getStatis(ApplicationConfig.INTERNAL_MARKET_AWARD_PHONEBILL_MONEY, ApplicationConfig.INTERNAL_MARKET_AWARD_PHONEBILL_MONEY,  statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getAwardUnionpayMoneyReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getAwardUnionPayMoney(companyId);
            return getStatis(ApplicationConfig.INTERNAL_MARKET_AWARD_UNIONPAY_MONEY, ApplicationConfig.INTERNAL_MARKET_AWARD_UNIONPAY_MONEY,  statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getAwardWechatMoneyReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getAwardWeChatMoney(companyId);
            return getStatis(ApplicationConfig.INTERNAL_MARKET_AWARD_WECHAT_MONEY, ApplicationConfig.INTERNAL_MARKET_AWARD_WECHAT_MONEY,  statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getAwardPointsReports(int companyId) throws ServiceException {
        try {
            BigDecimal[] statis = internalReportDao.getAwardPoints(companyId);
            return getStatis(ApplicationConfig.INTERNAL_MARKET_AWARD_POINTS, ApplicationConfig.INTERNAL_MARKET_AWARD_POINTS,  statis);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
    
    private InternalReportDataGroup getStatis(String title, String postfixTitle, BigDecimal[] statis) throws ServiceException {
        InternalReportDataGroup dg = new InternalReportDataGroup();
        dg.setT(title);

        if (ArrayUtils.isNotEmpty(statis)) {
            List<InternalReportData> ds = new ArrayList<InternalReportData>();
            for (int i = 0; i < statis.length; i++) {
                switch (i) {
                case 1:
                    ds.add(new InternalReportData(ApplicationConfig.INTERNAL_TODAY,
                            statis[1]));
                    break;
                case 0:
                    ds.add(new InternalReportData(ApplicationConfig.INTERNAL_YESTODAY, 
                            statis[0]));
                    break;
                case 2:
                    ds.add(new InternalReportData(ApplicationConfig.INTERNAL_TOTAL, 
                            statis[2]));
                    break;
                }
            }
            dg.setD(ds);
        }

        return dg;
    }

}
