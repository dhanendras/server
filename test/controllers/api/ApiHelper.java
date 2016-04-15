package controllers.api;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import org.openqa.selenium.Cookie;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Call;
import play.test.TestBrowser;

import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ApiHelper {

    private static final String BASE_URL = "http://localhost:19001";

    @Inject
    private WSClient ws;

    public <T> T doGetWithJsonResponse(Call urlRouteLink,
                                       Class<T> expectedReturnType) {
        try {
            return doCareFree(urlRouteLink, expectedReturnType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void doPost(Call urlRouteLink, Object bodyWhichIsSentAsJson) {
        try {
            doPostCareFree(urlRouteLink, bodyWhichIsSentAsJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void doPost(Call urlRouteLink, Object bodyWhichIsSentAsJson, TestBrowser testBrowser) {
        try {
            doPostCareFree(urlRouteLink, bodyWhichIsSentAsJson, testBrowser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T doPost(Call urlRouteLink, JsonNode data, Class<T> expectedReturnType) {
        try {
            return doPostCareFree(urlRouteLink, data, expectedReturnType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private <T> T doPostCareFree(Call urlRouteLink,
                                 JsonNode jsonData,
                                 Class<T> expectedReturnType) throws ExecutionException, InterruptedException {

        CompletionStage<JsonNode> response = ws.url(BASE_URL + urlRouteLink.url())
                .setContentType("application/json")
                .setHeader("Accept", "application/json")
                .post(jsonData)
                .thenApply(this::throwExceptionIfFaultyResponse)
                .thenApply(WSResponse::asJson);

        JsonNode rawResponse = response.toCompletableFuture().get();
        return Json.fromJson(rawResponse, expectedReturnType);

    }

    private void doPostCareFree(Call urlRouteLink,
                                Object bodyWhichIsSentAsJson) throws InterruptedException, ExecutionException {
        ws.url(BASE_URL + urlRouteLink.url())
                .post(Json.toJson(bodyWhichIsSentAsJson))
                .thenApply(this::throwExceptionIfFaultyResponse)
                .toCompletableFuture().get();
    }

    private void doPostCareFree(Call urlRouteLink,
                                Object bodyWhichIsSentAsJson,
                                TestBrowser testBrowser) throws InterruptedException, ExecutionException {

        /**
         * Use the same cookie as in the browser
         */
        Set<Cookie> allCookies = testBrowser.getCookies();
        String cookieAsString = allCookies
            .stream()
            .map(Cookie::toString)
            .collect(Collectors.joining("&"));

        ws.url(BASE_URL + urlRouteLink.url())
            .setHeader("Cookie", cookieAsString)
            .post(Json.toJson(bodyWhichIsSentAsJson))
            .thenApply(this::throwExceptionIfFaultyResponse)
            .toCompletableFuture().get();
    }

    private <T> T doCareFree(Call urlRouteLink,
                             Class<T> expectedReturnType) throws InterruptedException, ExecutionException {

        CompletionStage<JsonNode> response = ws
                .url(BASE_URL + urlRouteLink.url())
                .get()
                .thenApply(this::throwExceptionIfFaultyResponse)
                .thenApply(WSResponse::asJson);

        JsonNode rawResponse = response.toCompletableFuture().get();
        return Json.fromJson(rawResponse, expectedReturnType);
    }

    /**
     * Why do we need this?
     * Because, we try to parse the message as json, that is even done,
     * if the HTTP response is not ok ( e.g. if you get a 404 -> it tries to parse
     * the message as JSON -> lucky for us - we get a complicated Unexpected
     * character exception :( -> so try to catch that and and throw
     * a proper exception.
     */
    private WSResponse throwExceptionIfFaultyResponse(WSResponse o) {
        if (o.getStatus() == 404) {
            throw new RuntimeException("Response with error: " + o.getStatusText());
        } else if (o.getStatus() == 500){
            throw new RuntimeException("Response with error: " + o.getStatusText());
        } else if (o.getStatus() == 403){
            throw new RuntimeException("Response with error: " + o.getStatusText());
        }
        return o;
    }
}
