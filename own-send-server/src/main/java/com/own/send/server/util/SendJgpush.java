package com.own.send.server.util;

import com.siaya.util.Util;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.PushPayload.Builder;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendJgpush {

    public String sendJpush(String title, String msgContent, String platform, String sendId, String releaseFun, String appKey, String masterSecret) {
        String status = "";
        title = Util.toStringAndTrim(title); // 通知标题
        String alert = Util.toStringAndTrim(msgContent); // 推送的内容

        platform = Util.toStringAndTrim(platform); // 推送平台
        sendId = Util.toStringAndTrim(sendId); // 推送终端ID
        releaseFun = Util.toStringAndTrim(releaseFun); // 推送功能

        JPushClient jpushClient = new JPushClient(masterSecret, appKey, 3);

        PushPayload payload = null;
        Notification.Builder notification = null;
        AndroidNotification.Builder androidNotification = null;
        IosNotification.Builder iosNotification = null;

        Builder temp = PushPayload.newBuilder();

        switch (platform) {

            case "1": // 推送android平台

                temp.setPlatform(Platform.android());

                notification = Notification.newBuilder();
                notification.setAlert(alert);

                androidNotification = AndroidNotification.newBuilder();
                androidNotification.setTitle(title);
                androidNotification.addExtra("releaseFun", releaseFun);

                notification.addPlatformNotification(androidNotification.build());

                temp.setNotification(notification.build());

                if (!Util.isNullOrEmpty(sendId)) { // 精准推送
                    temp.setAudience(Audience.alias(sendId));
                } else {
                    temp.setAudience(Audience.all());
                }

                break;

            case "2": // 推送ios平台

                temp.setPlatform(Platform.ios());

                notification = Notification.newBuilder();

                iosNotification = IosNotification.newBuilder();
                iosNotification.setAlert(alert);
                iosNotification.addExtra("releaseFun", releaseFun);

                notification.addPlatformNotification(iosNotification.build());

                temp.setNotification(notification.build());

                if (!Util.isNullOrEmpty(sendId)) { // 精准推送
                    temp.setAudience(Audience.alias(sendId));//使用alias匹配终端
                } else {
                    temp.setAudience(Audience.all());
                }

                break;

            case "3": // 默认推送android和ios

                temp.setPlatform(Platform.android_ios());

                notification = Notification.newBuilder();
                notification.setAlert(alert);

                // 创建android平台通知
                androidNotification = AndroidNotification.newBuilder();
                androidNotification.setTitle(title);
                androidNotification.addExtra("releaseFun", releaseFun);

                notification.addPlatformNotification(androidNotification.build());

                // 创建ios平台通知
                iosNotification = IosNotification.newBuilder();
                iosNotification.setAlert(alert);
                iosNotification.addExtra("releaseFun", releaseFun);

                notification.addPlatformNotification(iosNotification.build());

                temp.setNotification(notification.build());

                if (!Util.isNullOrEmpty(sendId)) { // 精准推送
                    temp.setAudience(Audience.alias(sendId));
                } else {
                    temp.setAudience(Audience.all());
                }

                break;

        }

        payload = temp.build();

        try {
            PushResult result = jpushClient.sendPush(payload);//发送极光
            log.info("Got result - " + result);

            status = "1";
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. ", e);

            status = "2";

        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. ", e);
            log.info("HTTP Status: " + e.getStatus());
            log.info("Error Code: " + e.getErrorCode());
            log.info("Error Message: " + e.getErrorMessage());
            log.info("Msg ID: " + e.getMsgId());

            status = "2";

        }
        return status;
    }
}
