package net.kk17.clickstream.spark.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author kk
 */

public class UserStatusData implements Serializable {
    private String userId;
    private String lastSessionId;
    private double cartAmount;
    private Date lastVisitTime;

    public UserStatusData() {
    }

    public UserStatusData(String userId, String lastSessionId, long cartAmount, Date lastVisitTime) {
        this.userId = userId;
        this.lastSessionId = lastSessionId;
        this.cartAmount = cartAmount;
        this.lastVisitTime = lastVisitTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastSessionId() {
        return lastSessionId;
    }

    public void setLastSessionId(String lastSessionId) {
        this.lastSessionId = lastSessionId;
    }

    public double getCartAmount() {
        return cartAmount;
    }

    public void setCartAmount(double cartAmount) {
        this.cartAmount = cartAmount;
    }

    public Date getLastVisitTime() {
        return lastVisitTime;
    }

    public void setLastVisitTime(Date lastVisitTime) {
        this.lastVisitTime = lastVisitTime;
    }
}


