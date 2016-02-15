package com.foodlogiq.distributormobile.entityClasses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * This class holds all the first level data for a product.
 */
public class Product implements Serializable {
    public static final String FOODLOGIQ_ID_KEY = "_id";
    public static final String DESCRIPTION_KEY = "description";
    public static final String NAME_KEY = "name";
    public static final String GLOBAL_TRADE_ITEM_NUMBER_KEY = "globalTradeItemNumber";
    public static final String TRADE_ITEM_COUNTRY_OF_ORIGIN_KEY = "tradeItemCountryOfOrigin";
    public static final String BRAND_KEY = "brand";
    public static final String COMMODITY_KEY = "commodity";
    public static final String TYPE_KEY = "type";
    public static final String COMMODITY_NAME_KEY = "brick";
    public static final String BUSINESS_KEY = "business";
    public static final String ID_KEY = "_id";
    public static final String SHARES_KEY = "shares";
    private static final String LOT_KEY = "lot";
    private static final String COMMUNITY_KEY = "community";
    private static final String OWNER_ID_KEY = "ownerId";

    private String foodlogiqId = "";
    private String businessId = "";
    private String name = "";
    private String brand = "";
    private String description = "";
    private String commodityName = "";
    private String globalTradeItemNumber = "";
    private String tradeItemCountryOfOrigin = "";
    private String type = "";
    private String lot = "";

    //Store this from the share to handle the membership Id
    private String membershipId = "";

    /**
     * @param productJSONObject JSON Object containing product data.
     * @param communityId       Id of community product is associated with.
     */
    public Product(JSONObject productJSONObject, String communityId) {
        try {
            this.foodlogiqId = productJSONObject.has(FOODLOGIQ_ID_KEY) ? productJSONObject
                    .getString(FOODLOGIQ_ID_KEY) : "";
            JSONObject businessJSONObject = productJSONObject.has(BUSINESS_KEY) ?
                    productJSONObject.getJSONObject(BUSINESS_KEY) : null;
            if (businessJSONObject != null) {
                this.businessId = businessJSONObject.has(ID_KEY) ? businessJSONObject.getString
                        (ID_KEY) : "";
            }
            this.name = productJSONObject.has(NAME_KEY) ? productJSONObject.getString(NAME_KEY) :
                    "";
            this.description = productJSONObject.has(DESCRIPTION_KEY) ? productJSONObject
                    .getString(DESCRIPTION_KEY) : "";
            this.globalTradeItemNumber = productJSONObject.has(GLOBAL_TRADE_ITEM_NUMBER_KEY) ?
                    productJSONObject.getString(GLOBAL_TRADE_ITEM_NUMBER_KEY) : "";
            this.lot = productJSONObject.has(LOT_KEY) ? productJSONObject.getString(LOT_KEY) : "";
            this.tradeItemCountryOfOrigin = productJSONObject.has
                    (TRADE_ITEM_COUNTRY_OF_ORIGIN_KEY) ? productJSONObject.getString
                    (TRADE_ITEM_COUNTRY_OF_ORIGIN_KEY) : "";
            this.brand = productJSONObject.has(BRAND_KEY) ? productJSONObject.getString
                    (BRAND_KEY) : "";
            this.type = productJSONObject.has(TYPE_KEY) ? productJSONObject.getString(TYPE_KEY) :
                    "";
            if (productJSONObject.has(SHARES_KEY)) {
                JSONArray productShares = productJSONObject.getJSONArray(SHARES_KEY);
                for (int i = 0; i < productShares.length(); i++) {
                    JSONObject share = productShares.getJSONObject(i);
                    if (share.has(COMMUNITY_KEY) && !communityId.isEmpty()) {
                        JSONObject community = share.getJSONObject(COMMUNITY_KEY);
                        if (community.getString(ID_KEY).equals(communityId)) {
                            this.membershipId = share.getString(OWNER_ID_KEY);
                        }
                        break;
                    }
                }
            }

            JSONObject commodityJSONObject = productJSONObject.has(COMMODITY_KEY) ?
                    productJSONObject.getJSONObject(COMMODITY_KEY) : null;
            if (commodityJSONObject != null) {
                this.commodityName = commodityJSONObject.has(COMMODITY_NAME_KEY) ?
                        commodityJSONObject.getString(COMMODITY_NAME_KEY) : "";
            }
        } catch (JSONException ignored) {
        }
    }

    /**
     * @param globalTradeItemNumber Global Trade Item Number for the product.
     */
    public Product(String globalTradeItemNumber) {
        this.globalTradeItemNumber = globalTradeItemNumber;
    }

    public Product() {
    }

    public String getGlobalTradeItemNumber() {
        return globalTradeItemNumber;
    }

    public String getFoodlogiqId() {
        return foodlogiqId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getDescription() {
        return description;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public String getTradeItemCountryOfOrigin() {
        return tradeItemCountryOfOrigin;
    }

    public String getType() {
        return type;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String getMembershipId() {
        return membershipId;
    }

    /**
     * @return json object of product data
     */
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("_id", getFoodlogiqId());
            json.put("globalTradeItemNumber", getGlobalTradeItemNumber());
            json.put("lot", getLot());
            json.put("name", getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
