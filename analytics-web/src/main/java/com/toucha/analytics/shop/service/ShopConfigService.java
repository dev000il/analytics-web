package com.toucha.analytics.shop.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.shop.dao.ShopConfigDao;
import com.toucha.analytics.common.util.AppEvents;

@Service("shopConfigService")
public class ShopConfigService {

    ShopConfigDao configDao = new ShopConfigDao();

    public List<Integer> getScheduledjobIgnoreAeList() throws ServiceException {
        try {
            return configDao.getScheduledjobIgnoreAeList();
        } catch (Exception ex) {
            throw new ServiceException(AppEvents.ServerExceptionErr);
        }
    }

    public boolean updateScheduledjobIgnoreAeList(String aelist) throws ServiceException {
        try {
            return configDao.updateScheduledjobIgnoreAeList(aelist);
        } catch (SQLException e) {
            throw new ServiceException(AppEvents.ServerExceptionErr);
        }
    }

    public boolean clearScheduledjobIgnoreAeList() throws ServiceException {

        return updateScheduledjobIgnoreAeList(JSON.toJSONString(new ArrayList<Integer>()));
    }
}
