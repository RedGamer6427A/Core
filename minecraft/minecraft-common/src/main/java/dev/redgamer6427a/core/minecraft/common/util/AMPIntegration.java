package dev.redgamer6427a.core.minecraft.common.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

public abstract class AMPIntegration {

    protected final String key;
    @Getter
    protected AMPStatus status;

    protected AMPIntegration(String key) {
        this.key = key;
        status = fetchStatus(key);
    }

    protected AMPStatus fetchAMPStatus(String key, int tries) throws IOException {
        URL url = makeURL(decodeB64("aHR0cHM6Ly9hbXAucmVkZ2FtZXI2NDI3YS53b3JrZXJzLmRldiA=")); // Base64 of your URL
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(decodeB64("UE9TVA==")); // "POST"
        conn.setRequestProperty(decodeB64("Q29udGVudC1UeXBl"), decodeB64("YXBwbGljYXRpb24vanNvbg==")); // "Content-Type: application/json"
        conn.setDoOutput(true);

        String jsonBody = "{ \"" + decodeB64("a2V5") + "\": \"" + key + "\" }"; // {"key": "value"}

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 405){
            if(tries < 3){
                return fetchAMPStatus(key, tries + 1);
            } else {
                return new AMPStatus(decodeB64("NDA1LCBTb21ldGhpbmcgd2VudCB3cm9uZw=="), false);
            }
        }

        if (responseCode == 400){
            if(tries < 3){
                return fetchAMPStatus(key, tries + 1);
            } else {
                return new AMPStatus(decodeB64("NDAwLCBTb21ldGhpbmcgd2VudCB3cm9uZy4="), false);
            }
        }

        if (responseCode == 500){
            if(tries < 3){
                return fetchAMPStatus(key, tries + 1);
            } else {
                return new AMPStatus(decodeB64("NTAwLCBTb21ldGhpbmcgd2VudCB3cm9uZw=="), false);
            }
        }

        String jsonString;
        try (Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A")) {
            jsonString = s.hasNext() ? s.next() : "";
        }

        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        String value = jsonObject.get(decodeB64("dmFsdWU=")).getAsString(); // "value"

        AMPStatus licenseStatus = new AMPStatus(value, value.equals(decodeB64("RU5BQkxFRA=="))); // "ENABLED"

        if (responseCode == 404){
            if(tries < 3){
                return fetchAMPStatus(key, tries + 1);
            } else {
                return new AMPStatus(decodeB64("SW52YWxpZCBMaWNlbnNlOiA=") + value, false);
            }
        } else {
            return licenseStatus;
        }
    }

    public AMPStatus fetchStatus(String key) {
        try {
            return fetchAMPStatus(key, 0);
        } catch (IOException e) {
            e.printStackTrace();
            return new AMPStatus(decodeB64("VW5hYmxlIHRvIGNvbm5lY3Qu"), false);
        }
    }

    public void shutdownOnInvalid() {
        if (status.allowed) return;
        shutdown();
    }

    protected abstract void shutdown();

    private String decodeB64(String b64) {
        return new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
    }

    public URL makeURL(String url) throws IOException {
        return new URL(url);
    }

    public record AMPStatus(String status, boolean allowed){}
}
