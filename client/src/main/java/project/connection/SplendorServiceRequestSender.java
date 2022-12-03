package project.connection;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * TODO.
 */
public class SplendorServiceRequestSender {

  private final String gameUrl;

  public SplendorServiceRequestSender(String gameUrl) {
    this.gameUrl = gameUrl;
  }


  /**
   * Send the request and get back a list of strings encoded in one string
   *
   * @param gameId
   * @return
   */
  public String[] sendGetAllPlayerNamesList(long gameId) {
    RestTemplate rest = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    String body = "";
    String url = String.format("%s/api/games/%s/players", gameUrl, gameId);

    HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
    ResponseEntity<String> responseEntity =
        rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
    // TODO: Parse this raw string using Gson to an Array
    return new Gson().fromJson(responseEntity.getBody(), String[].class);
  }

  ///**
  // * GET Request to resource from /api/games/{gameId}
  // *
  // * @param gameId
  // * @param hashPreviousResponse put a empty string as "" to avoid stuck in the long polling loop
  // * @return
  // */
  //public ResponseEntity<String> sendGetGameInfoRequest(long gameId, String hashPreviousResponse) {
  //  RestTemplate rest = new RestTemplate();
  //  HttpHeaders headers = new HttpHeaders();
  //  String body = "";
  //  String url = gameUrl + "/api/games/" + gameId;
  //  if (!hashPreviousResponse.equals("")) {
  //    // if we are sending something in as hash, then we need to add it to the end of url
  //    url = String.format("%s/api/games/%s?hash=%s", gameUrl, gameId, hashPreviousResponse);
  //  } else {
  //    url = String.format("%s/api/games/%s", gameUrl, gameId);
  //  }
  //  HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
  //  return rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
  //}

  public HttpResponse<String> sendGetGameInfoRequest(long gameId, String hashPreviousResponse)
  throws UnirestException {
    if (hashPreviousResponse.equals("")) {
      return Unirest.get(gameUrl + "/api/games/" + gameId).asString();
    } else {
      return Unirest.get(gameUrl + "/api/games/" + gameId)
          .queryString("hash", hashPreviousResponse)
          .asString();
    }

  }
  ///**
  // *
  // * @param gameId
  // * @param hashPreviousResponse
  // * @return PlayerStates wrapped in Json String
  // */
  //public ResponseEntity<String> sendGetAllPlayerInfoRequest(long gameId, String hashPreviousResponse) {
  //  RestTemplate rest = new RestTemplate();
  //  HttpHeaders headers = new HttpHeaders();
  //  String body = "";
  //  String url;
  //  if (!hashPreviousResponse.equals("")) {
  //    // if we are sending something in as hash, then we need to add it to the end of url
  //    url =
  //        String.format("%s/api/games/%s/playerStates?hash=%s", gameUrl, gameId, hashPreviousResponse);
  //  } else {
  //    url = String.format("%s/api/games/%s/playerStates", gameUrl, gameId);
  //  }
  //  HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
  //  return rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
  //}

  public HttpResponse<String> sendGetAllPlayerInfoRequest(long gameId,
                                                            String hashPreviousResponse)
  throws UnirestException {
    if (hashPreviousResponse.equals("")) {
      return Unirest.get(String.format("%s/api/games/%s/playerStates",gameUrl, gameId)).asString();
    } else {
      return Unirest.get(String.format("%s/api/games/%s/playerStates",gameUrl, gameId))
          .queryString("hash", hashPreviousResponse)
          .asString();

    }

  }

  //public ResponseEntity<String> sendGetPlayerInventoryRequest(long gameId, String playerName,
  //                                                            String accessToken,
  //                                                            String hashPreviousResponse) {
  //  RestTemplate rest = new RestTemplate();
  //  HttpHeaders headers = new HttpHeaders();
  //  String body = "";
  //  String url;
  //  if (!hashPreviousResponse.equals("")) {
  //    // if we are sending something in as hash, then we need to add it to the end of url
  //    url = String.
  //        format("%s/api/games/%s/players/%s/inventory?hash=%s&access_token=%s",
  //            gameUrl, gameId, playerName, hashPreviousResponse, accessToken);
  //  } else {
  //    url = String.format("%s/api/games/%s/players/%s/inventory?access_token=%s"
  //        , gameUrl, gameId, playerName, accessToken);
  //  }
  //  HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
  //  return rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
  //}

  public HttpResponse<String> sendGetPlayerInventoryRequest(long gameId, String playerName,
                                                              String accessToken,
                                                              String hashPreviousResponse)
  throws  UnirestException{

    if (hashPreviousResponse.equals("")){
      return Unirest
          .get(String.format("%s/api/games/%s/players/%s/inventory",gameUrl, gameId, playerName))
          .queryString("access_token", accessToken).asString();
    } else {
      return Unirest
          .get(String.format("%s/api/games/%s/players/%s/inventory",gameUrl, gameId, playerName))
          .queryString("access_token", accessToken)
          .queryString("hash", hashPreviousResponse)
          .asString();
    }

  }

  //public ResponseEntity<String> sendGetPlayerActionsRequest(long gameId,
  //                                                          String playerName,
  //                                                          String accessToken) {
  //  RestTemplate rest = new RestTemplate();
  //  HttpHeaders headers = new HttpHeaders();
  //  String body = "";
  //  String url = String.
  //      format("%s/api/games/%s/players/%s/actions?access_token=%s",
  //          gameUrl, gameId, playerName, accessToken);
  //
  //  HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
  //  return rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
  //}

  public HttpResponse<String> sendGetPlayerActionsRequest(long gameId, String playerName,
                                                            String accessToken)
  throws UnirestException {

    return Unirest
        .get(String.format("%s/api/games/%s/players/%s/actions", gameUrl, gameId, playerName))
        .queryString("access_token", accessToken).asString();
  }


  public void sendPlayerActionChoiceRequest(long gameId, String playerName,
                                            String accessToken, String actionId)
  throws UnirestException {
    HttpResponse<String> response =
        Unirest.post(String.format("%s/api/games/%s/players/%s/actions/%s",
        gameUrl, gameId, playerName, actionId))
        .queryString("access_token",accessToken).asString();
  }



  // TODO: Delete Request (later)


  public String getGameUrl() {
    return gameUrl;
  }
}
