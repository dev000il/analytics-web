package com.toucha.analytics.shop.service;

import com.toucha.analytics.common.model.DailyStats;
import com.toucha.analytics.common.model.DailyStatsBase;
import com.toucha.analytics.common.shop.dao.ShopPromotionDao;
import com.toucha.analytics.common.shop.dao.ShopScanReportDao;
import org.springframework.stereotype.Service;

/**
 * All activities report services
 *
 * @author senhui.li
 */
@Service("shopActReportService")
public class ShopActivityReportService {

    private ShopScanReportDao scanReportDao = new ShopScanReportDao();
    private ShopPromotionDao promotionDao = new ShopPromotionDao();

    public DailyStats getTodayYesterdayActivitiesStats(int companyId) throws Exception {
        DailyStats stats = new DailyStats();

        DailyStatsBase scanDaily = scanReportDao.getYesterTodayStats(companyId);
        stats.getDailyMeasures().add(scanDaily);

        DailyStatsBase lotteryDaily = promotionDao.getYesterTodayLotteryStats(companyId);
        stats.getDailyMeasures().add(lotteryDaily);

        DailyStatsBase claimDaily = promotionDao.getYesterTodayClaimStats(companyId);
        stats.getDailyMeasures().add(claimDaily);

        return stats;
    }
}
