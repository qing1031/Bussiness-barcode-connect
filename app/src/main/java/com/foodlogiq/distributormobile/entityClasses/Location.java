package com.foodlogiq.distributormobile.entityClasses;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.databases.commonTasks.FindLocation;
import com.foodlogiq.distributormobile.databases.contentProviders.LocationContentProvider;
import com.foodlogiq.distributormobile.databases.contentProviders.SupplierContentProvider;
import com.foodlogiq.distributormobile.databases.contentProviders.SupplyChainLocationContentProvider;
import com.foodlogiq.distributormobile.databases.tables.LocationTable;
import com.foodlogiq.distributormobile.databases.tables.SupplierTable;
import com.foodlogiq.distributormobile.databases.tables.SupplyChainLocationTable;
import com.foodlogiq.distributormobile.interfaces.JSONParceable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;


/**
 * This class holds all the first level data for a location.
 */
public class Location implements JSONParceable, Comparable {

    public static final String FOODLOGIQ_ID_KEY = "_id";
    public static final String DESCRIPTION_KEY = "description";
    public static final String NAME_KEY = "name";
    public static final String ADDRESS_KEY = "address";
    public static final String STREET_ADDRESS_KEY = "addressLineOne";
    public static final String CITY_KEY = "city";
    public static final String REGION_KEY = "region";
    public static final String POSTAL_CODE_KEY = "postalCode";
    public static final String COUNTRY_KEY = "country";
    public static final String PHONE_KEY = "phone";
    public static final String GLOBAL_LOCATION_NUMBER_KEY = "globalLocationNumber";
    public static final String GLOBAL_LOCATION_NUMBER_EXTENSION_KEY =
            "globalLocationNumberExtension";
    public static final String COMMUNITY_KEY = "community";
    public static final String BUSINESS_KEY = "business";
    public static final String ID_KEY = "_id";
    public static final String INTERNAL_ID_KEY = "internalId";
    private static final String SUPPLIES_KEY = "supplies";
    private static final String SUPPLY_CHAIN_KEY = "supplyChain";
    private static final String LOCATIONS_KEY = "locations";
    private static final String SUPPLY_CHAIN_SUPPLIER_KEY = "supplier";
    private ArrayList<Supplier> suppliers = new ArrayList<>();
    private ArrayList<SupplyChainLocation> supplyChainLocations = new ArrayList<>();
    private String foodlogiqId;
    private String businessId;
    private String internalId;
    private String globalLocationNumber;
    private String globalLocationNumberExtension;
    private String name;
    private String streetAddress;
    private String city;
    private String region;
    private String postalCode;
    private String country;
    private String phone;
    private String description;
    private String id;
    private String communityId;
    private ArrayList<Location> suppliesLocations = new ArrayList<>();

    public Location() {
    }

    /**
     * @param foodlogiqId                   Foodlogiq Id of connect location.
     * @param businessId                    Business id of the owner of the location
     * @param internalId                    Internal Id of location.
     * @param globalLocationNumber          Global Location Number of location.
     * @param globalLocationNumberExtension Global Location Number Extension, if it exists, of
     *                                      location.
     * @param name                          Name of location.
     * @param streetAddress                 Street address of location.
     * @param city                          City of location.
     * @param region                        Region of location.
     * @param postalCode                    Postal/Zip code of location.
     * @param country                       Country of location.
     * @param phone                         Phone number of location contact.
     * @param description                   Description of location.
     * @param communityId                   Id of community that location is associated with.
     * @param supplyChainLocations          Locations that distribute product to this location.
     * @param supplyChainSuppliers          Businesses that supply this location.
     * @param id                            Sqlite id of location.
     */
    public Location(String foodlogiqId,
                    String businessId,
                    String internalId,
                    String globalLocationNumber,
                    String globalLocationNumberExtension,
                    String name,
                    String streetAddress,
                    String city,
                    String region,
                    String postalCode,
                    String country,
                    String phone,
                    String description,
                    String communityId,
                    ArrayList<SupplyChainLocation> supplyChainLocations,
                    ArrayList<Supplier> supplyChainSuppliers,
                    ArrayList<Location> suppliesLocations,
                    String id) {
        this.foodlogiqId = foodlogiqId;
        this.businessId = businessId;
        this.internalId = internalId;
        this.globalLocationNumber = globalLocationNumber;
        this.globalLocationNumberExtension = globalLocationNumberExtension;
        this.name = name;
        this.streetAddress = streetAddress;
        this.city = city;
        this.region = region;
        this.postalCode = postalCode;
        this.country = country;
        this.phone = phone;
        this.description = description;
        this.communityId = communityId;
        this.supplyChainLocations = supplyChainLocations == null ? new
                ArrayList<SupplyChainLocation>() : supplyChainLocations;
        this.suppliers = supplyChainSuppliers == null ? new ArrayList<Supplier>() :
                supplyChainSuppliers;
        this.suppliesLocations = suppliesLocations == null ? new ArrayList<Location>() :
                suppliesLocations;
        this.id = id;
    }

    /**
     * @param foodlogiqId                   Foodlogiq Id of connect location.
     * @param businessId                    Business id of the owner of the location
     * @param internalId                    Internal Id of location.
     * @param globalLocationNumber          Global Location Number of location.
     * @param globalLocationNumberExtension Global Location Number Extension, if it exists, of
     *                                      location.
     * @param name                          Name of location.
     * @param streetAddress                 Street address of location.
     * @param city                          City of location.
     * @param region                        Region of location.
     * @param postalCode                    Postal/Zip code of location.
     * @param country                       Country of location.
     * @param phone                         Phone number of location contact.
     * @param description                   Description of location.
     * @param communityId                   Id of community that location is associated with.
     */
    public Location(String foodlogiqId,
                    String businessId,
                    String internalId,
                    String globalLocationNumber,
                    String globalLocationNumberExtension,
                    String name,
                    String streetAddress,
                    String city,
                    String region,
                    String postalCode,
                    String country,
                    String phone,
                    String description,
                    String communityId) {
        this(
                foodlogiqId,
                businessId,
                internalId,
                globalLocationNumber,
                globalLocationNumberExtension,
                name,
                streetAddress,
                city,
                region,
                postalCode,
                country,
                phone,
                description,
                communityId,
                null,
                null,
                null
        );
    }

    /**
     * @param foodlogiqId                   Foodlogiq Id of connect location.
     * @param businessId                    Business id of the owner of the location
     * @param internalId                    Internal Id of location.
     * @param globalLocationNumber          Global Location Number of location.
     * @param globalLocationNumberExtension Global Location Number Extension, if it exists, of
     *                                      location.
     * @param name                          Name of location.
     * @param streetAddress                 Street address of location.
     * @param city                          City of location.
     * @param region                        Region of location.
     * @param postalCode                    Postal/Zip code of location.
     * @param country                       Country of location.
     * @param phone                         Phone number of location contact.
     * @param description                   Description of location.
     * @param communityId                   Id of community that location is associated with.
     * @param supplyChainLocations          Locations that distribute product to this location.
     * @param supplyChainSuppliers          Businesses that supply this location.
     */
    public Location(String foodlogiqId,
                    String businessId,
                    String internalId,
                    String globalLocationNumber,
                    String globalLocationNumberExtension,
                    String name,
                    String streetAddress,
                    String city,
                    String region,
                    String postalCode,
                    String country,
                    String phone,
                    String description,
                    String communityId,
                    ArrayList<SupplyChainLocation> supplyChainLocations,
                    ArrayList<Supplier> supplyChainSuppliers,
                    ArrayList<Location> suppliesLocations) {
        this(
                foodlogiqId,
                businessId,
                internalId,
                globalLocationNumber,
                globalLocationNumberExtension,
                name,
                streetAddress,
                city,
                region,
                postalCode,
                country,
                phone,
                description,
                communityId,
                supplyChainLocations,
                supplyChainSuppliers,
                suppliesLocations,
                null
        );
    }


    /**
     * Queries and returns the current location selected by the user.
     *
     * @param activity Used to get the content resolve for sqlite db interaction.
     * @return Currently selected location.
     */
    public static Location getCurrent(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(activity
                .getPackageName(), Context.MODE_PRIVATE);
        String currentLocationSqlId = sharedPreferences.getString(activity.getString(R.string
                .current_location_id), "");
        if (currentLocationSqlId.isEmpty()) return null;
        FindLocation foundLocation = new FindLocation(activity.getContentResolver(),
                currentLocationSqlId);
        return foundLocation.getLocation();
    }

    /**
     * Removes location as current location
     */
    public static void removeCurrent(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(activity
                .getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(activity.getString(R.string.current_location_foodlogiq_id));
        editor.remove(activity.getString(R.string.current_location_id));
        editor.remove(activity.getString(R.string.current_location_internal_id));
        editor.remove(activity.getString(R.string.current_location_name));
        editor.remove(activity.getString(R.string.current_location_gln));
        editor.remove(activity.getString(R.string.current_community_foodlogiq_id));
        editor.apply();
    }

    /**
     * Sets location as default so user doesn't have to reselect it every time they reopen the
     * application.
     */
    public static void setDefault(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(activity
                .getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(activity.getString(R.string.default_location), true);
        editor.apply();
    }

    /**
     * Removes default for location so user is able to select a new location on application reopen.
     */
    public static void removeDefault(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(activity
                .getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(activity.getString(R.string.default_location));
        editor.apply();
    }

    private static ArrayList<String> batchCreate(Activity activity, ArrayList<Location>
            suppliesLocations) {
        ArrayList<String> newIds = new ArrayList<>();
        for (Location d : suppliesLocations) {
            String newId = d.saveToDB(activity);
            newIds.add(newId);
        }
        return newIds;
    }

    /**
     * Sets location as current location
     */
    public void setAsCurrent(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(activity
                .getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(activity.getString(R.string.current_location_foodlogiq_id),
                getFoodlogiqId());
        editor.putString(activity.getString(R.string.current_location_id), getId());
        editor.putString(activity.getString(R.string.current_location_internal_id), getInternalId
                ());
        editor.putString(activity.getString(R.string.current_location_name), getName());
        editor.putString(activity.getString(R.string.current_location_gln),
                getGlobalLocationNumber());
        editor.putString(activity.getString(R.string.current_community_foodlogiq_id),
                getCommunityId());
        editor.apply();
    }

    /**
     * Saves the location to the Sqlite database
     * This involves inserting the related {@link SupplyChainLocation} and {@link Supplier} entities
     * as well and saving their sqlite id's in pipe-separated strings on the location.
     *
     * @param activity Used to get the content resolve for sqlite db interaction.
     */
    public String saveToDB(Activity activity) {
        ContentValues values = new ContentValues();
        values.put(LocationTable.COLUMN_FOODLOGIQ_ID, getFoodlogiqId());
        values.put(LocationTable.COLUMN_BUSINESS_ID, getBusinessId());
        values.put(LocationTable.COLUMN_INTERNAL_ID, getInternalId());
        values.put(LocationTable.COLUMN_DESCRIPTION, getDescription());
        values.put(LocationTable.COLUMN_NAME, getName());
        values.put(LocationTable.COLUMN_STREET_ADDRESS, getStreetAddress());
        values.put(LocationTable.COLUMN_CITY, getCity());
        values.put(LocationTable.COLUMN_REGION, getRegion());
        values.put(LocationTable.COLUMN_POSTAL_CODE, getPostalCode());
        values.put(LocationTable.COLUMN_COUNTRY, getCountry());
        values.put(LocationTable.COLUMN_PHONE, getPhone());
        values.put(LocationTable.COLUMN_GLOBAL_LOCATION_NUMBER, getGlobalLocationNumber());
        values.put(LocationTable.COLUMN_GLOBAL_LOCATION__EXTENSION,
                getGlobalLocationNumberExtension());
        values.put(LocationTable.COLUMN_COMMUNITY_ID, getCommunityId());

        //Handle supplyChainLocations first, and store pipe-delimited string of their id's.
        ArrayList<String> supplyChainLocationIds = SupplyChainLocation.batchCreate(activity,
                getSupplyChainLocations());
        StringBuffer supplyChainLocationsPaddingBuffer = new StringBuffer();
        for (int i = 0; i < supplyChainLocationIds.size(); i++) {
            if (i > 0) supplyChainLocationsPaddingBuffer.append("|");
            supplyChainLocationsPaddingBuffer.append(supplyChainLocationIds.get(i));
        }
        values.put(LocationTable.COLUMN_SUPPLY_CHAIN_LOCATION_IDS,
                supplyChainLocationsPaddingBuffer.toString());

        //Handle suppliesLocations first, and store pipe-delimited string of their id's.
        ArrayList<String> suppliesLocationIds = Location.batchCreate(activity,
                getSuppliesLocations());
        StringBuffer suppliesLocationsPaddingBuffer = new StringBuffer();
        for (int i = 0; i < suppliesLocationIds.size(); i++) {
            if (i > 0) suppliesLocationsPaddingBuffer.append("|");
            suppliesLocationsPaddingBuffer.append(suppliesLocationIds.get(i));
        }
        values.put(LocationTable.COLUMN_SUPPLIES_IDS,
                suppliesLocationsPaddingBuffer.toString());

        //Handle supplyChainSuppliers first, and store pipe-delimited string of their id's.
        ArrayList<String> supplyChainSupplierIds = Supplier.batchCreate(activity, getSuppliers());
        StringBuffer suppliersPaddingBuffer = new StringBuffer();
        for (int i = 0; i < supplyChainSupplierIds.size(); i++) {
            if (i > 0) suppliersPaddingBuffer.append("|");
            suppliersPaddingBuffer.append(supplyChainSupplierIds.get(i));
        }
        values.put(LocationTable.COLUMN_SUPPLY_CHAIN_SUPPLIER_IDS, suppliersPaddingBuffer
                .toString());

        Uri insertedLocation = activity.getContentResolver().insert(LocationContentProvider
                .CONTENT_URI, values);
        setId(String.valueOf(ContentUris.parseId(insertedLocation)));
        return String.valueOf(ContentUris.parseId(insertedLocation));
    }

    public String getFoodlogiqId() {
        return foodlogiqId;
    }

    public void setFoodlogiqId(String foodlogiqId) {
        this.foodlogiqId = foodlogiqId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getInternalId() {
        return internalId;
    }

    public String getGlobalLocationNumber() {
        return globalLocationNumber;
    }

    public void setGlobalLocationNumber(String globalLocationNumber) {
        this.globalLocationNumber = globalLocationNumber;
    }

    public String getGlobalLocationNumberExtension() {
        return globalLocationNumberExtension;
    }

    public void setGlobalLocationNumberExtension(String globalLocationNumberExtension) {
        this.globalLocationNumberExtension = globalLocationNumberExtension;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public String getPhone() {
        return phone;
    }

    public String getCommunityId() {
        return communityId;
    }

    public ArrayList<SupplyChainLocation> getSupplyChainLocations() {
        return supplyChainLocations;
    }

    public ArrayList<Supplier> getSuppliers() {
        return suppliers;
    }


    @Override
    public String toString() {
        return name;
    }

    /**
     * @see JSONParceable#createJSON()
     */
    @Override
    public JSONObject createJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("_id", getFoodlogiqId());
            json.put("city", getCity());
            json.put("country", getCountry());
            json.put("globalLocationNumber", getGlobalLocationNumber());
            json.put("internalId", getInternalId());
            json.put("name", getName());
            json.put("region", getRegion());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public ArrayList<Location> getSuppliesLocations() {
        Collections.sort(suppliesLocations);
        return suppliesLocations;
    }

    /**
     * Compares this object to the specified object to determine their relative
     * order.
     *
     * @param another the object to compare to this instance.
     * @return a negative integer if this instance is less than {@code another};
     * a positive integer if this instance is greater than
     * {@code another}; 0 if this instance has the same order as
     * {@code another}.
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public int compareTo(Object another) {
        return this.getName().compareToIgnoreCase(((Location) another).getName());
    }

    /**
     * Locations that are simpler than a discrete {@link Location}. Used only for selecting a
     * distributor
     * location.
     */
    public static class SupplyChainLocation extends Location {
        private static final String SUPPLIER_ID_KEY = "supplierId";
        private String supplierId;

        /**
         * @param supplyChainLocation Json object with relevant supplier location data.
         */
        public SupplyChainLocation(JSONObject supplyChainLocation) throws JSONException {
            this.setFoodlogiqId(supplyChainLocation.has(FOODLOGIQ_ID_KEY) && !supplyChainLocation
                    .isNull(FOODLOGIQ_ID_KEY)
                    ? supplyChainLocation.getString(FOODLOGIQ_ID_KEY) : "");
            if (supplyChainLocation.has(NAME_KEY) && !supplyChainLocation.isNull(NAME_KEY)) {
                this.setName(supplyChainLocation.getString(NAME_KEY));
            }
            if (supplyChainLocation.has(GLOBAL_LOCATION_NUMBER_KEY) && !supplyChainLocation
                    .isNull(GLOBAL_LOCATION_NUMBER_KEY)) {
                this.setGlobalLocationNumber(supplyChainLocation.getString
                        (GLOBAL_LOCATION_NUMBER_KEY));
            }
        }

        /**
         * @param foodlogiqId          Foodlogiq Id of supply chain location on connect server.
         * @param globalLocationNumber Global Location Number of Location.
         * @param name                 Name of Supply Chain Location.
         * @param supplierId           Business if of owner of location.
         * @param id                   Sqlite Id
         */
        public SupplyChainLocation(
                String foodlogiqId,
                String globalLocationNumber,
                String name,
                String supplierId,
                String id
        ) {
            this.setFoodlogiqId(foodlogiqId);
            this.setGlobalLocationNumber(globalLocationNumber);
            this.setName(name);
            this.setSupplierId(supplierId);
            this.setId(id);
        }

        /**
         * Inserts all the supplyChainLocations into the sqlite db. BulkInsert is not used, in order
         * to record the inserted ids.
         *
         * @param activity             Used to get the content resolve for sqlite db interaction.
         * @param supplyChainLocations ArrayList of supply chain locations.
         * @return Array List of inserted supply chain location ids.
         */
        public static ArrayList<String> batchCreate(Activity activity,
                                                    ArrayList<SupplyChainLocation>
                                                            supplyChainLocations) {
            ArrayList<String> newIds = new ArrayList<>();
            for (SupplyChainLocation d : supplyChainLocations) {
                String newId = d.saveToDB(activity);
                newIds.add(newId);
            }
            return newIds;
        }

        /**
         * Saves the location to the Sqlite database
         *
         * @param activity Used to get the content resolve for sqlite db interaction.
         */
        public String saveToDB(Activity activity) {
            ContentValues values = new ContentValues();
            values.put(SupplyChainLocationTable.COLUMN_FOODLOGIQ_ID, getFoodlogiqId());
            values.put(SupplyChainLocationTable.COLUMN_NAME, getName());
            values.put(SupplyChainLocationTable.COLUMN_SUPPLIER_ID, getSupplierId());
            values.put(SupplyChainLocationTable.COLUMN_GLOBAL_LOCATION_NUMBER,
                    getGlobalLocationNumber());

            Uri insertedSupplyChainLocation = activity.getContentResolver().insert
                    (SupplyChainLocationContentProvider.CONTENT_URI, values);
            return String.valueOf(ContentUris.parseId(insertedSupplyChainLocation));
        }

        public String getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(String supplierId) {
            this.supplierId = supplierId;
        }
    }

    public static class Supplier implements JSONParceable {
        private String id = "";
        private String foodlogiqId = "";
        private Business business;

        public Supplier(
                String foodlogiqId,
                Business business,
                String id
        ) {
            this.foodlogiqId = foodlogiqId;
            this.business = business;
            this.id = id;
        }

        public Supplier() {
        }

        /**
         * Inserts all the suppliers into the sqlite db. BulkInsert is not used, in order
         * to record the inserted ids.
         *
         * @param activity             Used to get the content resolve for sqlite db interaction.
         * @param supplyChainLocations ArrayList of suppliers.
         * @return Array List of inserted supplier ids.
         */
        public static ArrayList<String> batchCreate(Activity activity, ArrayList<Supplier>
                supplyChainLocations) {
            ArrayList<String> newIds = new ArrayList<>();
            for (Supplier d : supplyChainLocations) {
                String newId = d.saveToDB(activity);
                newIds.add(newId);
            }
            return newIds;
        }

        /**
         * Saves the supplier business to the Sqlite database
         *
         * @param activity Used to get the content resolve for sqlite db interaction.
         */
        public String saveToDB(Activity activity) {
            ContentValues values = new ContentValues();
            values.put(SupplierTable.COLUMN_FOODLOGIQ_ID, getFoodlogiqId());
            values.put(SupplierTable.COLUMN_BUSINESS_JSON, getBusiness().createJSON().toString());

            Uri insertedSupplier = activity.getContentResolver().insert(SupplierContentProvider
                    .CONTENT_URI, values);
            return String.valueOf(ContentUris.parseId(insertedSupplier));
        }

        public String getFoodlogiqId() {
            return foodlogiqId;
        }

        public Business getBusiness() {
            return business;
        }

        @Override
        public String toString() {
            if (getBusiness() != null)
                return getBusiness().getName();
            return "";
        }


        /**
         * @see JSONParceable#createJSON()
         */
        @Override
        public JSONObject createJSON() {
            JSONObject supplier = new JSONObject();
            try {
                supplier.put(FOODLOGIQ_ID_KEY, getFoodlogiqId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                supplier.put(BUSINESS_KEY, getBusiness().createJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return supplier;
        }

        /**
         * @see JSONParceable#parseJSON(JSONObject) ()
         */
        @Override
        public void parseJSON(JSONObject jsonObject) {
            try {
                this.foodlogiqId = jsonObject.has(FOODLOGIQ_ID_KEY) ? jsonObject.getString
                        (FOODLOGIQ_ID_KEY) : "";
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                this.business = new Business();
                if (jsonObject.has(BUSINESS_KEY)) {
                    this.business.parseJSON(jsonObject.getJSONObject(BUSINESS_KEY));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @see JSONParceable#parseJSON(JSONObject)
     */
    @Override
    public void parseJSON(JSONObject locationJSONObject) {
        try {
            this.foodlogiqId = locationJSONObject.has(FOODLOGIQ_ID_KEY) && !locationJSONObject
                    .isNull(FOODLOGIQ_ID_KEY)
                    ? locationJSONObject.getString(FOODLOGIQ_ID_KEY) : "";
            JSONObject businessJSONObject = locationJSONObject.has(BUSINESS_KEY) &&
                    !locationJSONObject.isNull(BUSINESS_KEY)
                    ? locationJSONObject.getJSONObject(BUSINESS_KEY) : null;
            if (businessJSONObject != null) {
                this.businessId = businessJSONObject.has(ID_KEY) && !businessJSONObject.isNull
                        (ID_KEY)
                        ? businessJSONObject.getString(ID_KEY) : "";
            }
            JSONObject communityJSONObject = locationJSONObject.has(COMMUNITY_KEY) &&
                    !locationJSONObject.isNull(COMMUNITY_KEY)
                    ? locationJSONObject.getJSONObject(COMMUNITY_KEY) : null;
            if (communityJSONObject != null) {
                this.communityId = communityJSONObject.has(ID_KEY) && !communityJSONObject.isNull
                        (ID_KEY)
                        ? communityJSONObject.getString(ID_KEY) : "";
            }
            JSONArray supplyChainJSONArray = locationJSONObject.has(SUPPLY_CHAIN_KEY) &&
                    !locationJSONObject.isNull(SUPPLY_CHAIN_KEY)
                    ? locationJSONObject.getJSONArray(SUPPLY_CHAIN_KEY) : null;
            if (supplyChainJSONArray != null) {
                for (int i = 0; i < supplyChainJSONArray.length(); i++) {
                    JSONObject supplyChainObj = supplyChainJSONArray.getJSONObject(i);
                    Supplier supplier = null;
                    if (!supplyChainObj.isNull(SUPPLY_CHAIN_SUPPLIER_KEY)) {
                        JSONObject supplierJson = supplyChainObj.getJSONObject
                                (SUPPLY_CHAIN_SUPPLIER_KEY);
                        if (!supplierJson.isNull(FOODLOGIQ_ID_KEY) && !supplierJson.getString
                                (FOODLOGIQ_ID_KEY).isEmpty()) {
                            supplier = new Supplier();
                            supplier.parseJSON(supplyChainObj.getJSONObject
                                    (SUPPLY_CHAIN_SUPPLIER_KEY));
                            this.suppliers.add(
                                    supplier
                            );
                        }
                    }
                    if (!supplyChainObj.isNull(LOCATIONS_KEY)) {
                        JSONArray supplyChainLocations = supplyChainObj.getJSONArray(LOCATIONS_KEY);
                        for (int j = 0; j < supplyChainLocations.length(); j++) {
                            SupplyChainLocation supplyChainLocation = new SupplyChainLocation(
                                    supplyChainLocations.getJSONObject(j)
                            );
                            if (supplier != null)
                                supplyChainLocation.setSupplierId(supplier.getFoodlogiqId());
                            this.supplyChainLocations.add(supplyChainLocation);
                        }
                    }
                }
            }

            JSONArray suppliesJSONArray = locationJSONObject.has(SUPPLIES_KEY) &&
                    !locationJSONObject.isNull(SUPPLIES_KEY)
                    ? locationJSONObject.getJSONArray(SUPPLIES_KEY) : null;

            if (suppliesJSONArray != null) {
                for (int i = 0; i < suppliesJSONArray.length(); i++) {
                    JSONObject supplierLocation = suppliesJSONArray.getJSONObject(i);
                    if (!supplierLocation.isNull(FOODLOGIQ_ID_KEY) && !supplierLocation.getString
                            (FOODLOGIQ_ID_KEY).isEmpty()) {
                        Location location = new Location();
                        location.parseJSON(supplierLocation);
                        this.suppliesLocations.add(location);
                    }
                }
            }


            JSONObject addressJSONObject = locationJSONObject.has(ADDRESS_KEY) &&
                    !locationJSONObject.isNull(ADDRESS_KEY)
                    ? locationJSONObject.getJSONObject(ADDRESS_KEY) : null;
            if (addressJSONObject != null) {
                this.streetAddress = addressJSONObject.has(STREET_ADDRESS_KEY) &&
                        !addressJSONObject.isNull(STREET_ADDRESS_KEY)
                        ? addressJSONObject.getString(STREET_ADDRESS_KEY) : "";
                this.city = addressJSONObject.has(CITY_KEY) && !addressJSONObject.isNull(CITY_KEY)
                        ? addressJSONObject.getString(CITY_KEY) : "";
                this.region = addressJSONObject.has(REGION_KEY) && !addressJSONObject.isNull
                        (REGION_KEY)
                        ? addressJSONObject.getString(REGION_KEY) : "";
                this.postalCode = addressJSONObject.has(POSTAL_CODE_KEY) && !addressJSONObject
                        .isNull(POSTAL_CODE_KEY)
                        ? addressJSONObject.getString(POSTAL_CODE_KEY) : "";
                this.country = addressJSONObject.has(COUNTRY_KEY) && !addressJSONObject.isNull
                        (COUNTRY_KEY)
                        ? addressJSONObject.getString(COUNTRY_KEY) : "";

            }

            this.internalId = locationJSONObject.has(INTERNAL_ID_KEY) && !locationJSONObject
                    .isNull(INTERNAL_ID_KEY)
                    ? locationJSONObject.getString(INTERNAL_ID_KEY) : "";
            this.phone = locationJSONObject.has(PHONE_KEY) && !locationJSONObject.isNull(PHONE_KEY)
                    ? locationJSONObject.getString(PHONE_KEY) : "";
            this.globalLocationNumber = locationJSONObject.has(GLOBAL_LOCATION_NUMBER_KEY) &&
                    !locationJSONObject.isNull(GLOBAL_LOCATION_NUMBER_KEY)
                    ? locationJSONObject.getString(GLOBAL_LOCATION_NUMBER_KEY) : "";
            this.globalLocationNumberExtension = locationJSONObject.has
                    (GLOBAL_LOCATION_NUMBER_EXTENSION_KEY) && !locationJSONObject.isNull
                    (GLOBAL_LOCATION_NUMBER_EXTENSION_KEY)
                    ? locationJSONObject.getString(GLOBAL_LOCATION_NUMBER_EXTENSION_KEY) : "";
            this.name = locationJSONObject.has(NAME_KEY) && !locationJSONObject.isNull(NAME_KEY)
                    ? locationJSONObject.getString(NAME_KEY) : "";
            this.description = locationJSONObject.has(DESCRIPTION_KEY) && !locationJSONObject
                    .isNull(DESCRIPTION_KEY)
                    ? locationJSONObject.getString(DESCRIPTION_KEY) : "";
            this.internalId = locationJSONObject.has(INTERNAL_ID_KEY) && !locationJSONObject
                    .isNull(INTERNAL_ID_KEY)
                    ? locationJSONObject.getString(INTERNAL_ID_KEY) : "";
        } catch (
                JSONException e
                )

        {
            e.printStackTrace();
        }
    }


}
