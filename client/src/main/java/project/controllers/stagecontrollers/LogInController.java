package project.controllers.stagecontrollers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;
import project.App;
import project.connection.LobbyRequestSender;
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
  private Button logInButton;

  @FXML
  private Button quitButton;

  /**
   * The logic of handling log in. The methods check if the user has input both username and user
   * password or not
   */

  private EventHandler<ActionEvent> createOnLogInClick() {
    return actionEvent -> {
      // extract fields from the object, in case of failing to extract "access_token",
      // update the error message
      boolean serviceLogIn = false;
      try {
        String userNameStr = userName.getText();
        String userPasswordStr = userPassword.getText();
        // retrieve the parsed JSONObject from the response
        LobbyRequestSender lobbyRequestSender = App.getLobbyServiceRequestSender();
        JSONObject logInResponseJson = lobbyRequestSender
            .sendLogInRequest(userNameStr, userPasswordStr);
        // set up the permanent refresh_token for user
        String accessToken = logInResponseJson.getString("access_token");
        String refreshToken = logInResponseJson.getString("refresh_token");
        String authority = lobbyRequestSender.sendAuthorityRequest(accessToken);
        User curUser = new User(userNameStr, accessToken, refreshToken, authority);
        // bind the user to the scope of App running lifecycle
        App.setUser(curUser);
        if (authority.equals("ROLE_SERVICE")) {
          serviceLogIn = true;
          throw new RuntimeException("");
        }
        // display the lobby page, the role of the user will be used inside to
        // decide whether to display admin zone button or not
        App.loadNewSceneToPrimaryStage("lobby_page.fxml", new LobbyController());

      } catch (Exception e) {
        if (!serviceLogIn) {
          logInPageErrorMessage.setText("Please enter both valid username and password");
        } else {
          logInPageErrorMessage.setText("Service Role can not log in LS! Try again");
        }

        userName.setText("");
        userPassword.setText("");
      }
    };
  }

  private EventHandler<ActionEvent> createOnQuitClick() {
    return actionEvent -> {
      Button quitButton = (Button) actionEvent.getSource();
      Stage curWindow = (Stage) quitButton.getScene().getWindow();
      curWindow.close();
    };
  }

  // Mainly for debug usage
  private void setDefaultLogInInfo() {
    //String name = "ruoyuplayer";
    String name = "ruoyu";
    String password = "abc123_ABC123";

    //String name = "splendorbase";
    //String password = "laaPhie*aiN0";
    userName.setText(name);
    userPassword.setText(password);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
      setDefaultLogInInfo();
      logInButton.setOnAction(createOnLogInClick());
      quitButton.setOnAction(createOnQuitClick());
  }
}
