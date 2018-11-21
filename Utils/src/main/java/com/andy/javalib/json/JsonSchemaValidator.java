package com.andy.javalib.json;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

public class JsonSchemaValidator {

    public static List<String> validateSchema(String jsonString) {
        List<String> validationErrors = new ArrayList<String>();
        try{
            InputStream inputStream = JsonSchemaValidator.class.getResourceAsStream("schema.json");
            if (inputStream == null) {
                validationErrors.add("Error validating DSAR request - Could not load schema");
            } else{
                Schema schema = SchemaLoader.load(new JSONObject(new JSONTokener(inputStream)));
                JSONObject json = new JSONObject(new JSONTokener(jsonString));
                schema.validate(json);
            }
        } catch (ValidationException validationException) {
            validationErrors.add(validationException.getMessage());
            validationException.getCausingExceptions().stream()
                .map(ValidationException::getMessage)
                .forEach(validationErrors::add);
        } catch (Exception e) {
            String errorMessage = "Exception validating DSAR request against schema - " + e.getClass().getCanonicalName() + ": " + e.getMessage();
            validationErrors.add(errorMessage);
        }
        return validationErrors;
    }
}