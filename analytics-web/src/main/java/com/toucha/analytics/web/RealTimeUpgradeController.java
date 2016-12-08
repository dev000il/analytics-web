package com.toucha.analytics.web;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.service.RealTimeUpgradeService;
import com.toucha.analytics.utils.AuthAccessTokenVerifyUtil;
import com.toucha.analytics.websocket.SubscribeSendTask;

@Controller
public class RealTimeUpgradeController {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeUpgradeController.class);

    private static final String VALID_SUCCESS = "success";

    private SimpMessagingTemplate simpMsgTemp;
    private SubscribeSendTask subSendTask;
    private RealTimeUpgradeService realTimeUpgradeService;

    @Autowired
    public RealTimeUpgradeController(SimpMessagingTemplate simpMsgTemp, SubscribeSendTask subSendTask,
            RealTimeUpgradeService realTimeUpgradeService) {
        this.simpMsgTemp = simpMsgTemp;
        this.subSendTask = subSendTask;
        this.realTimeUpgradeService = realTimeUpgradeService;
    }

    @PostConstruct
    public void init() {
        subSendTask.send(simpMsgTemp);
    }

    @MessageMapping("/select/{name}/{action}")
    @SendTo("/topic/totaldata/{name}")
    public Object selectStaticsData(@Headers MessageHeaders messageHeaders, @DestinationVariable("action") String action ) {

        Object data = new String("Connect Forbid.");
        if (null == messageHeaders || action == null || action.isEmpty()) {
            return data;
        }

        JSONObject headers = JSON.parseObject(JSON.toJSONString(messageHeaders.get("nativeHeaders")));
        if (headers.containsKey("Bearer")) {
            String bearerToken = headers.getJSONArray("Bearer").getString(0);
            String result = validToken(bearerToken);

            if (!result.equals(VALID_SUCCESS)) {
                data = result;
            } else {
                switch (action) {
                case "scantag":
                    try {
                        data = realTimeUpgradeService.getRealTimeScanStatics();
                    } catch (ServiceException e) {
                        logger.error("Get scan static in service error.", e);
                    }
                    break;
                case "enterlottery":
                    try {
                        data = realTimeUpgradeService.getRealTimeEnterLotStatics();
                    } catch (ServiceException e) {
                        logger.error("Get enter lottery static in service error.", e);
                    }
                    break;
                case "members":
                    try {
                        data = realTimeUpgradeService.getRealTimeMembersStatics();
                    } catch (ServiceException e) {
                        logger.error("Get members static in service error.", e);
                    }
                    break;
                case "rewardamt":
                    try {
                        data = realTimeUpgradeService.getRealTimeRewardsStatics();
                    } catch (ServiceException e) {
                        logger.error("Get reward amount static in service error.", e);
                    }
                default:
                    break;
                }
            }
        }

        return data;
    }

    protected String validToken(String bearerToken) {
        String validResult = VALID_SUCCESS;
        if (bearerToken != null) {
            String[] uniqueId = new String[] { "" };
            int result = AuthAccessTokenVerifyUtil.validateToken(bearerToken, uniqueId);

            if (result == 0 && !uniqueId[0].equals("")) {
                // succeed
                return validResult;
            } else if (result == 2) {
                // expired token
                return "expired";
            } else {
                // invalid token Forbidden
                return "invalid";
            }
        } else {
           return "need access token";
        }
    }
}
