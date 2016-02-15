package com.foodlogiq.distributormobile.entityClasses;

import com.foodlogiq.distributormobile.interfaces.JSONParceable;
import com.foodlogiq.distributormobile.miscellaneousHelpers.DateFormatters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class SparseProduct implements JSONParceable {
    public static final String LOT = "lot";
    public static final String QUANTITY_UNITS = "quantityUnits";
    public static final String QUANTITY_AMOUNT = "quantityAmount";
    public static final String GLOBAL_TRADE_ITEM_NUMBER = "globalTradeItemNumber";
    public static final String PRODUCT_DESCRIPTION = "description";
    public static final String USE_BY_DATE = "useByDate";
    public static final String PACK_DATE = "packDate";
    private static final String SERIAL_NUMBER = "serialNumber";
    private static final String NAME = "name";

    private String globalLocationTradeItemNumber = "";
    private String description = "";
    private String name = "";
    private String lot = "";
    private Date useByDate = null;
    private Date packDate = null;
    private String serialNumber = "";
    private int quantityAmount = 1; //Shouldn't exist unless at least one exists.
    private String quantityUnits = "cases";

    /**
     * @param name                  Name of Product.
     * @param globalTradeItemNumber Global Trade Item Number of Product.
     * @param lot                   Lot of case of Product.
     * @param packDateString        Pack date of case of Product.
     * @param useByDateString       Use By Date of case of Product.
     * @param serialNumber          Serial number of case of Product.
     * @param quantityAmount        Amount of cases of Product.
     */
    public SparseProduct(String name, String globalTradeItemNumber, String lot, String
            packDateString, String useByDateString, String serialNumber, int quantityAmount) {
        this.name = name;
        this.globalLocationTradeItemNumber = globalTradeItemNumber;
        this.lot = lot;
        try {
            this.packDate = DateFormatters.gs1Format.parse(packDateString);
        } catch (ParseException ignored) {
            try {
                this.packDate = DateFormatters.simpleFormat.parse(packDateString);
            } catch (ParseException ignored2) {
            }
        }
        try {
            this.useByDate = DateFormatters.gs1Format.parse(useByDateString);
        } catch (ParseException ignored) {
            try {
                this.useByDate = DateFormatters.simpleFormat.parse(useByDateString);
            } catch (ParseException ignored2) {
            }
        }
        this.serialNumber = serialNumber;
        this.quantityAmount = quantityAmount;
    }

    public SparseProduct() {
    }

    /**
     * @param productsJsonArray Array of JSON objects with Product data.
     * @return Array list of Products.
     */
    public static ArrayList<SparseProduct> fromJSONArray(JSONArray productsJsonArray) {
        ArrayList<SparseProduct> products = new ArrayList<>();

        int i = 0;
        while (i < productsJsonArray.length()) {
            try {
                SparseProduct sp = new SparseProduct();
                sp.parseJSON(productsJsonArray.getJSONObject(i));
                products.add(sp);
            } catch (JSONException ignored) {
            }
            i++;
        }

        return products;
    }

    /**
     * @param products Array list of Products
     * @return JSONArray of JSONObjects representing products
     */
    public static JSONArray toJSONArray(ArrayList<SparseProduct> products) throws JSONException {
        JSONArray sps = new JSONArray();

        for (SparseProduct p : products) {
            sps.put(p.createJSON());
        }

        return sps;
    }

    public void incrementQuantity() {
        this.quantityAmount++;
    }

    public void incrementQuantity(int q) {
        this.quantityAmount = this.quantityAmount + q;
    }

    public String getGlobalLocationTradeItemNumber() {
        return globalLocationTradeItemNumber;
    }

    public void setGlobalLocationTradeItemNumber(String globalLocationTradeItemNumber) {
        this.globalLocationTradeItemNumber = globalLocationTradeItemNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public int getQuantityAmount() {
        return quantityAmount;
    }

    public void setQuantityAmount(int quantityAmount) {
        this.quantityAmount = quantityAmount;
    }

    public String getQuantityUnits() {
        return quantityUnits;
    }

    public void setQuantityUnits(String quantityUnits) {
        this.quantityUnits = quantityUnits;
    }

    public Date getUseByDate() {
        return useByDate;
    }

    public void setUseByDate(Date useByDate) {
        this.useByDate = useByDate;
    }

    public String getUseByDateAsISOString() {
        if (getUseByDate() == null) return "";
        return DateFormatters.isoDateFormatter.format(getUseByDate());
    }

    public String getUseByDateAsSimpleString() {
        if (getUseByDate() == null) return "";
        return DateFormatters.simpleFormat.format(getUseByDate());
    }

    public String getUseByDateAsGs1String() {
        if (getUseByDate() == null) return "";
        return DateFormatters.gs1Format.format(getUseByDate());
    }

    public Date getPackDate() {
        return packDate;
    }

    public void setPackDate(Date packDate) {
        this.packDate = packDate;
    }

    public String getPackDateAsISOString() {
        if (getPackDate() == null) return "";
        return DateFormatters.isoDateFormatter.format(getPackDate());
    }

    public String getPackDateAsSimpleString() {
        if (getPackDate() == null) return "";
        return DateFormatters.simpleFormat.format(getPackDate());
    }

    public String getPackDateAsGs1String() {
        if (getPackDate() == null) return "";
        return DateFormatters.gs1Format.format(getPackDate());
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * @return JSON representation of the Product Data.
     */
    public JSONObject createJSON() {
        JSONObject sp = new JSONObject();
        try {
            sp.put(NAME, getName());
            sp.put(GLOBAL_TRADE_ITEM_NUMBER, getGlobalLocationTradeItemNumber());
            sp.put(PRODUCT_DESCRIPTION, getDescription());
            sp.put(LOT, getLot());
            sp.put(USE_BY_DATE, getUseByDateAsISOString());
            sp.put(PACK_DATE, getPackDateAsISOString());
            sp.put(QUANTITY_AMOUNT, getQuantityAmount());
            sp.put(QUANTITY_UNITS, getQuantityUnits());
            sp.put(SERIAL_NUMBER, getSerialNumber());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sp;
    }

    /**
     * @param jsonProduct JSON data containing product data.
     */
    public void parseJSON(JSONObject jsonProduct) {
        try {
            this.name = jsonProduct.has(NAME)
                    ? jsonProduct.getString(NAME) : "";
        } catch (JSONException ignored) {
        }
        try {
            this.globalLocationTradeItemNumber = jsonProduct.has(GLOBAL_TRADE_ITEM_NUMBER)
                    ? jsonProduct.getString(GLOBAL_TRADE_ITEM_NUMBER) : "";
        } catch (JSONException ignored) {
        }
        try {
            this.description = jsonProduct.has(PRODUCT_DESCRIPTION)
                    ? jsonProduct.getString(PRODUCT_DESCRIPTION) : "";
        } catch (JSONException ignored) {
        }
        try {
            this.lot = jsonProduct.has(LOT)
                    ? jsonProduct.getString(LOT) : "";
        } catch (JSONException ignored) {
        }
        try {
            this.useByDate = jsonProduct.has(USE_BY_DATE)
                    ? DateFormatters.isoDateFormatter.parse(jsonProduct.getString(USE_BY_DATE)) :
                    null;
        } catch (JSONException | ParseException ignored) {
        }
        try {
            this.packDate = jsonProduct.has(PACK_DATE)
                    ? DateFormatters.isoDateFormatter.parse(jsonProduct.getString(PACK_DATE)) :
                    null;
        } catch (JSONException | ParseException ignored) {
        }
        try {
            this.quantityAmount = jsonProduct.has(QUANTITY_AMOUNT)
                    ? jsonProduct.getInt(QUANTITY_AMOUNT) : 1;
        } catch (JSONException ignored) {
        }
        try {
            this.quantityUnits = jsonProduct.has(QUANTITY_UNITS)
                    ? jsonProduct.getString(QUANTITY_UNITS) : "";
        } catch (JSONException ignored) {
        }
        try {
            this.serialNumber = jsonProduct.has(SERIAL_NUMBER)
                    ? jsonProduct.getString(SERIAL_NUMBER) : "";
        } catch (JSONException ignored) {
        }
    }
}
