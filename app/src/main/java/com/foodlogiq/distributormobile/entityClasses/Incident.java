package com.foodlogiq.distributormobile.entityClasses;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.foodlogiq.distributormobile.interfaces.JSONParceable;
import com.foodlogiq.distributormobile.interfaces.LogDescriptor;
import com.foodlogiq.distributormobile.miscellaneousHelpers.DateFormatters;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class holds all the first level data for a incident.
 */
public class Incident implements JSONParceable, LogDescriptor {
    private ArrayList<Photo> photos;
    private boolean distributionIssue;
    private String communityId = "";
    private Date incidentDate;
    private Location location;
    private Product product;
    private String type = "";
    private Date useByDate;
    private String foundByName = "";
    private String foundByTitle = "";
    private boolean customerComplaint;
    private Date packedDate;
    private int quantityAffected;
    private String details = "";
    private boolean creditRequest;
    private boolean packagingExists;
    private String sourceType = "";
    private String invoiceNumber = "";
    private Date invoiceDate;
    private Location.SupplyChainLocation invoiceDistributor;
    private String sourceStore = "";
    private Location.Supplier supplier;

    /**
     * @param communityId       Foodlogiq Id of community associated with incident.
     * @param location          Location object of location that is filing the incident.
     * @param product           Product object that incident is regarding
     * @param type              What type of incident is it. For instance, "Bad Label"
     * @param distributionIssue Is the incident a distribution center issue. For instance, "Late
     *                          Delivery"
     * @param foundByName       Name of employee reporting the incident
     * @param foundByTitle      Title of employee reporting the incident
     * @param useByDate         Use by date printed on case of product.
     * @param packedDate        Packed date printed on case of product.
     * @param customerComplaint Was the incident a result of a customer's complaint.
     * @param quantityAffected  How many cases of product were included in the incident.
     * @param details           Details of the incident.
     * @param creditRequest     Whether or not the location is requesting credit because of the
     *                          incident.
     * @param invoiceNumber     Used if credit is requested. Invoice number of order that
     *                          incident occured with.
     * @param packagingExists   Does the location still retain the packaging of incident cases.
     * @param photos            Photos related to the incident. Uploaded to S3.
     */
    public Incident(
            String communityId,
            Location location,
            Product product,
            String type,
            boolean distributionIssue,
            String foundByName,
            String foundByTitle,
            String useByDate,
            String packedDate,
            boolean customerComplaint,
            int quantityAffected,
            String details,
            boolean creditRequest,
            String invoiceNumber,
            String invoiceDate,
            Location.SupplyChainLocation invoiceDistributor,
            boolean packagingExists,
            String sourceType,
            ArrayList<Photo> photos) {
        this.communityId = communityId;
        this.location = location;
        this.product = product;
        this.type = type;
        this.distributionIssue = distributionIssue;
        this.foundByName = foundByName;
        this.foundByTitle = foundByTitle;
        try {
            this.useByDate = DateFormatters.isoDateFormatter.parse(useByDate);
        } catch (ParseException ignored) {
        }
        try {
            this.packedDate = DateFormatters.isoDateFormatter.parse(packedDate);
        } catch (ParseException ignored) {
        }
        this.customerComplaint = customerComplaint;
        this.quantityAffected = quantityAffected;
        this.details = details;
        this.creditRequest = creditRequest;
        this.invoiceNumber = invoiceNumber;
        try {
            this.invoiceDate = DateFormatters.isoDateFormatter.parse(invoiceDate);
        } catch (ParseException ignored) {
        }
        this.invoiceDistributor = invoiceDistributor;
        this.packagingExists = packagingExists;
        this.sourceType = sourceType;
        this.photos = photos;
        this.incidentDate = new Date();
    }

    public Incident() {
    }

    private boolean doesPackagingExist() {
        return packagingExists;
    }

    public Date getUseByDate() {
        return useByDate;
    }

    public int getQuantityAffected() {
        return quantityAffected;
    }

    public boolean isCustomerComplaint() {
        return customerComplaint;
    }

    public String getType() {
        return type;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public boolean getCreditRequest() {
        return creditRequest;
    }

    public String getAdditionalDescription() {
        return details;
    }

    public Date getPackedDate() {
        return packedDate;
    }

    public String getFoundByName() {
        return foundByName;
    }

    public Date getIncidentDate() {
        return incidentDate;
    }

    public String getCommunityId() {
        return communityId;
    }

    public Product getProduct() {
        return product;
    }

    public boolean getCustomerComplaint() {
        return customerComplaint;
    }

    public String getFoundByTitle() {
        return foundByTitle;
    }

    public boolean isDistributionIssue() {
        return distributionIssue;
    }

    public String getSourceStore() {
        return sourceStore;
    }

    public void setSourceStore(String sourceStore) {
        this.sourceStore = sourceStore;
    }

    public String getSourceType() {
        return sourceType;
    }

    public boolean isPackagingExists() {
        return packagingExists;
    }

    public Location.Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Location.Supplier supplier) {
        this.supplier = supplier;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public Location.SupplyChainLocation getInvoiceDistributor() {
        return invoiceDistributor;
    }

    /**
     * @return JSONObject of incident class.
     * @see JSONParceable#createJSON()
     */
    @Override
    public JSONObject createJSON() {
        JSONObject json = new JSONObject();
        try {
            JSONObject community = new JSONObject();
            community.put("_id", getCommunityId());
            json.put("community", community);
            json.put("product", product.getJson());
            json.put("location", location.createJSON());
            if (getQuantityAffected() != -1) {
                json.put("quantityAffected", getQuantityAffected());
                json.put("quantityUnits", "cases");
            }
            if (!getFoundByName().isEmpty()) {
                json.put("foundBy", getFoundByName());
            }
            if (!getFoundByTitle().isEmpty()) {
                json.put("foundByTitle", getFoundByTitle());
            }
            if (isCustomerComplaint()) {
                json.put("customerComplaint", isCustomerComplaint());
            }
            if (isDistributionIssue()) {
                json.put("distributionIssue", isDistributionIssue());
            }
            if (doesPackagingExist()) {
                json.put("packagingExists", doesPackagingExist());
            }
            if (getUseByDate() != null) {
                json.put("useByDate", DateFormatters.isoDateFormatter.format(getUseByDate()));
            }
            if (getPackedDate() != null) {
                json.put("packedDate", DateFormatters.isoDateFormatter.format(getPackedDate()));
            }
            if (!getType().isEmpty()) {
                json.put("type", getType());
            }
            if (getCreditRequest()) {
                json.put("creditRequest", getCreditRequest());
            }
            if (!getInvoiceNumber().isEmpty()) {
                json.put("purchaseOrder", getInvoiceNumber());
            }
            if (getIncidentDate() != null) {
                json.put("incidentDate", DateFormatters.isoDateFormatter.format(getIncidentDate()));
            }
            if (getInvoiceDistributor() != null) {
                json.put("distributor", getInvoiceDistributor().createJSON());
            }
            if (getInvoiceDate() != null) {
                json.put("invoiceDate", DateFormatters.isoDateFormatter.format(getInvoiceDate()));
            }

            if (!getAdditionalDescription().isEmpty()) {
                json.put("details", getAdditionalDescription());
            }
            if (!getSourceType().isEmpty()) {
                json.put("sourceType", getSourceType());
            }

            if (!getSourceStore().isEmpty()) {
                json.put("sourceStore", getSourceStore());
            }

            if (getSupplier() != null) {
                json.put("sourceMembership", getSupplier().createJSON());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Business class populated from json data.
     *
     * @param jsonObject jsonData containing incident data
     * @see JSONParceable#parseJSON(JSONObject)
     */
    @Override
    public void parseJSON(JSONObject jsonObject) {
        if (jsonObject.has("community")) {
            try {
                JSONObject communityJson = jsonObject.getJSONObject("community");
                this.communityId = communityJson.getString("_id");
            } catch (JSONException ignored) {
            }
        }
        if (jsonObject.has("location")) {
            try {
                JSONObject locationJson = jsonObject.getJSONObject("location");
                (this.location = new Location()).parseJSON(locationJson);
            } catch (JSONException e) {
                this.location = new Location();
            }
        }
        if (jsonObject.has("product")) {
            try {
                JSONObject productJson = jsonObject.getJSONObject("product");
                this.product = new Product(productJson, communityId);
            } catch (JSONException e) {
                this.product = new Product();
            }
        }
        try {
            this.creditRequest = jsonObject.getBoolean("creditRequest");
        } catch (JSONException e) {
            this.creditRequest = false;
        }
        try {
            this.customerComplaint = jsonObject.getBoolean("customerComplaint");
        } catch (JSONException e) {
            this.customerComplaint = false;
        }
        try {
            this.creditRequest = jsonObject.getBoolean("creditRequest");
        } catch (JSONException e) {
            this.creditRequest = false;
        }
        try {
            this.packagingExists = jsonObject.getBoolean("packagingExists");
        } catch (JSONException e) {
            this.packagingExists = false;
        }
        try {
            this.foundByName = jsonObject.getString("foundBy");
        } catch (JSONException e) {
            this.foundByName = "";
        }
        try {
            this.foundByTitle = jsonObject.getString("foundByTitle");
        } catch (JSONException e) {
            this.foundByTitle = "";
        }
        try {
            this.details = jsonObject.getString("details");
        } catch (JSONException e) {
            this.details = "";
        }
        try {
            this.incidentDate = DateFormatters.isoDateFormatter.parse(jsonObject.getString
                    ("incidentDate"));
        } catch (Exception ignored) {
        }
        try {
            this.packedDate = DateFormatters.isoDateFormatter.parse(jsonObject.getString
                    ("packedDate"));
        } catch (Exception ignored) {
        }
        try {
            this.invoiceNumber = jsonObject.getString("purchaseOrder");
        } catch (JSONException e) {
            this.invoiceNumber = "";
        }
        try {
            this.invoiceDate = DateFormatters.isoDateFormatter.parse(jsonObject.getString
                    ("invoiceDate"));
        } catch (Exception ignored) {
        }
        try {
            this.quantityAffected = jsonObject.getInt("quantityAffected");
        } catch (JSONException e) {
            this.quantityAffected = 0;
        }
        try {
            this.type = jsonObject.getString("type");
        } catch (JSONException e) {
            this.type = "";
        }
        try {
            this.sourceStore = jsonObject.getString("sourceStore");
        } catch (JSONException e) {
            this.sourceStore = "";
        }
        try {
            this.supplier = new Location.Supplier();
            this.supplier.parseJSON(jsonObject.getJSONObject("supplier"));
        } catch (JSONException e) {
            this.supplier = null;
        }
        try {
            this.distributionIssue = jsonObject.getBoolean("distributionIssue");
        } catch (JSONException e) {
            this.distributionIssue = false;
        }
        try {
            this.useByDate = DateFormatters.isoDateFormatter.parse(jsonObject.getString
                    ("useByDate"));
        } catch (Exception ignored) {
        }
        //default to empty array.
        this.photos = new ArrayList<>();
    }

    /**
     * @see LogDescriptor#getDetails(boolean)
     */
    @Override
    public Spannable getDetails(boolean success) {
        String details = "Quality Report ";
        if (getType() != null && !getType().isEmpty())
            details = details.concat("(" + getType() + ")");
        details = details.concat(success ? " succeeded" : " failed");
        Spannable detailSpan = new SpannableString(details);
        if (success) {
            detailSpan.setSpan(new StyleSpan(Typeface.BOLD), detailSpan.length() - ("succeeded"
                    .length()), detailSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            detailSpan.setSpan(new ForegroundColorSpan(Color.RED), detailSpan.length() -
                    ("failed".length()), detailSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            detailSpan.setSpan(new StyleSpan(Typeface.BOLD), detailSpan.length() - ("failed"
                    .length()), detailSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return detailSpan;
    }

    @Override
    public Spannable getAdditionalDetails() {
        //TODO: Do this when they want more details for the incident report
        return null;
    }
}