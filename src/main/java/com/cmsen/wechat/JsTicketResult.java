/**
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.wechat;

import com.cmsen.common.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JsTicketResult extends ErrorResult {
    @JsonProperty("ticket")
    private String ticket;
    @JsonProperty("expires_in")
    private Integer expiresIn;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public JsTicketResultSign getSign(String appId, String refererUrl) {
        JsTicketResultSign jsSign = new JsTicketResultSign();
        jsSign.setAppId(appId);
        jsSign.setNonceStr(StringUtil.UUID());
        jsSign.setTimestamp(Long.toString(System.currentTimeMillis() / 1000));
        jsSign.setSignature(ticket, refererUrl);
        return jsSign;
    }
}
