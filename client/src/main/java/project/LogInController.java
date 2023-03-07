package project;

import com.mashape.unirest.http.exceptions.UnirestException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;
import project.connection.LobbyRequestSender;
import project.view.lobby.SessionGuiManager;
import project.view.lobby.communication.User;

/**
 * Lobby GUI controller.
 */
public class LogInController implements Initializable {

  @FXML
  private TextField userName;

  @FXML
  private PasswordField userPassword;

  @FXML
  private Label logInPageErrorMessage;

  @FXML
  private Button quitGameButton;

  /**
   * The logic of handling log in. The methods check if the user has input both username and user
   * password or not
   */
  @FXML
  protected void onLogInButtonClick() throws UnirestException {
    String userNameStr = userName.getText();
    String userPasswordStr = userPassword.getText();
    // retrieve the parsed JSONObject from the response
    LobbyRequestSender lobbyRequestSender = App.getLobbyServiceRequestSender();
    JSONObject logInResponseJson = lobbyRequestSender
        .sendLogInRequest(userNameStr, userPasswordStr);

    // extract fields from the object, in case of failing to extract "access_token",
    // update the error message
    try {
      // set up the permanent refresh_token for user
      String accessToken = logInResponseJson.getString("access_token");
      String refreshToken = logInResponseJson.getString("refresh_token");
      String authority = lobbyRequestSender.sendAuthorityRequest(accessToken);
      User curUser = new User(userNameStr, accessToken, refreshToken, authority);
      App.setUser(curUser);

      // if user is player, display admin_lobby_page
      GameBoardLayoutConfig config = App.getGuiLayouts();
      if (App.getUser().getAuthority().equals("ROLE_ADMIN") ||
          App.getUser().getAuthority().equals("ROLE_PLAYER")) {
        if(App.getLobbyController() == null) {
          App.setLobbyController(new LobbyController());
        }

        App.loadNewSceneToPrimaryStage(
            "admin_lobby_page.fxml",
            App.getLobbyController());
        //App.loadPopUpWithController("admin_lobby_page.fxml",
        //    App.getLobbyController(),
        //    config.getAppWidth(),
        //    config.getAppHeight());
        //Stage logInWindow = (Stage) quitGameButton.getScene().getWindow();
        //logInWindow.close();

      } else { // otherwise, player_lobby_page
        // App.setRoot("player_lobby_page");
        //App.setRoot("LobbyService");
      }
      //lobbyRequestSender.getRemoteSessions();

    } catch (Exception e) {
      logInPageErrorMessage.setText("Please enter both valid username and password");
      userName.setText("");
      userPassword.setText("");
    }
  }

  @FXML
  protected void onQuitGameButtonClick() {
    Stage curStage = (Stage) quitGameButton.getScene().getWindow();
    curStage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    userName.setText("ruoyu");
    userPassword.setText("abc123_ABC123");
  }
}
