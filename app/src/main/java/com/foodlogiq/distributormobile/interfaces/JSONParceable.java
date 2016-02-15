package com.foodlogiq.distributormobile.interfaces;

import org.json.JSONObject;


public interface JSONParceable {
    /**
     * @return A JSON representation of the implementing class.
     */
    JSONObject createJSON();

    /**
     * Populates implementing class with data from JSONObject param.
     *
     * @param jsonObject JSONObject containing data for implementing class.
     */
    void parseJSON(JSONObject jsonObject);
}
