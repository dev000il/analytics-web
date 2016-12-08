package com.toucha.analytics.websocket;

import java.util.Map;

import com.toucha.analytics.common.common.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.toucha.analytics.service.RealTimeUpgradeService;
import com.toucha.analytics.utils.WebSocketSessionUtils;

/**
 * This task execute websockt broker send message
 *
 * @author senhui.li
 */
@Component
public class SubscribeSendTask {

    private TaskScheduler taskScheduler = new ConcurrentTaskScheduler();

    @Autowired
    private RealTimeUpgradeService realtimeUpgradeService;

    public void send(final SimpMessagingTemplate template) {

        this.taskScheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                Map<String, WebSocketSession> sessions = WebSocketSessionUtils.getClients();
                if (!sessions.isEmpty()) {
                    template.convertAndSend("/topic/scantag/refresh", realtimeUpgradeService.getRealTimeScantagCount(-1));
                    template.convertAndSend("/topic/enterlottery/refresh", realtimeUpgradeService.getRealTimeEnterLotteryCount(-1));
                    template.convertAndSend("/topic/members/refresh", realtimeUpgradeService.getRealTimeMembersCnt(-1));
                    template.convertAndSend("/topic/rewardamt/refresh", realtimeUpgradeService.getRealTimeRewardAmount(-1));
                    template.convertAndSend("/topic/createtags/refresh", realtimeUpgradeService.getRealTimeCreateTagCnt());
                    template.convertAndSend("/topic/servertime/refresh", System.currentTimeMillis());
                }
            }

        }, ApplicationConfig.WEBSOCKET_HEARTBEAT_TIME);
    }
}
