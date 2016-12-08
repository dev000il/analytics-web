package com.toucha.analytics.service;

import org.springframework.stereotype.Service;

import com.toucha.analytics.common.dao.DataSet;
import com.toucha.analytics.common.dao.ShoplogDao;
import com.toucha.analytics.model.request.ShoplogRequest;
import com.toucha.analytics.utils.reporting.FriendlyLabelDisplay;

@Service("shoplogService")
public class ShoplogService {

    private ShoplogDao shoplogDao = new ShoplogDao();

    public DataSet getRawActivities(ShoplogRequest request) {

        DataSet data = shoplogDao.findTopScanlogs(request.getRequestHeader().getCompanyId(), request.getDids(),
                request.getOids(), request.getPris(), request.getStartDate(), request.getEndDate(), request.getFields(),
                request.getCountRequested(), request.getType());

        data.replaceValuesWithNameMap(request.getNameMap());
        FriendlyLabelDisplay.executeRules(data);

        return data;

    }

}
