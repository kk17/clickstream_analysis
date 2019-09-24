package net.kk17.clickstream.spark.dto;

import java.io.Serializable;
import java.util.*;
import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClickstreamData implements Serializable {
    private String sessionID;
    private String event;
    private String partnerID;
    private String partnerName;
    private double cartAmount;
    private String country;
    private String userAgent;
    private String userID;
    private String version;
    private String language;
    private long date;
    private String searchQuery;
    private String currentURL;
    private String[] category;
    private String referrer;
    private boolean initSession;
    private String pageType;

    @JsonProperty("session_id")
    public String getSessionID() { return sessionID; }
    @JsonProperty("session_id")
    public void setSessionID(String value) { this.sessionID = value; }

    @JsonProperty("event")
    public String getEvent() { return event; }
    @JsonProperty("event")
    public void setEvent(String value) { this.event = value; }

    @JsonProperty("partner_id")
    public String getPartnerID() { return partnerID; }
    @JsonProperty("partner_id")
    public void setPartnerID(String value) { this.partnerID = value; }

    @JsonProperty("partner_name")
    public String getPartnerName() { return partnerName; }
    @JsonProperty("partner_name")
    public void setPartnerName(String value) { this.partnerName = value; }

    @JsonProperty("cart_amount")
    public double getCartAmount() { return cartAmount; }
    @JsonProperty("cart_amount")
    public void setCartAmount(double value) { this.cartAmount = value; }

    @JsonProperty("country")
    public String getCountry() { return country; }
    @JsonProperty("country")
    public void setCountry(String value) { this.country = value; }

    @JsonProperty("user_agent")
    public String getUserAgent() { return userAgent; }
    @JsonProperty("user_agent")
    public void setUserAgent(String value) { this.userAgent = value; }

    @JsonProperty("user_id")
    public String getUserID() { return userID; }
    @JsonProperty("user_id")
    public void setUserID(String value) { this.userID = value; }

    @JsonProperty("version")
    public String getVersion() { return version; }
    @JsonProperty("version")
    public void setVersion(String value) { this.version = value; }

    @JsonProperty("language")
    public String getLanguage() { return language; }
    @JsonProperty("language")
    public void setLanguage(String value) { this.language = value; }

    @JsonProperty("date")
    public long getDate() { return date; }
    @JsonProperty("date")
    public void setDate(long value) { this.date = value; }

    @JsonProperty("search_query")
    public String getSearchQuery() { return searchQuery; }
    @JsonProperty("search_query")
    public void setSearchQuery(String value) { this.searchQuery = value; }

    @JsonProperty("current_url")
    public String getCurrentURL() { return currentURL; }
    @JsonProperty("current_url")
    public void setCurrentURL(String value) { this.currentURL = value; }

    @JsonProperty("category")
    public String[] getCategory() { return category; }
    @JsonProperty("category")
    public void setCategory(String[] value) { this.category = value; }

    @JsonProperty("referrer")
    public String getReferrer() { return referrer; }
    @JsonProperty("referrer")
    public void setReferrer(String value) { this.referrer = value; }

    @JsonProperty("init_session")
    public boolean getInitSession() { return initSession; }
    @JsonProperty("init_session")
    public void setInitSession(boolean value) { this.initSession = value; }

    @JsonProperty("page_type")
    public String getPageType() { return pageType; }
    @JsonProperty("page_type")
    public void setPageType(String value) { this.pageType = value; }
}
