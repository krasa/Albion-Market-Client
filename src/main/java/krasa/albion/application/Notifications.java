package krasa.albion.application;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import krasa.albion.AlbionMarketClientApplication;
import krasa.albion.controller.ErrorController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Notifications {
	private static final Logger LOG = LoggerFactory.getLogger(Notifications.class);


	public static void showError(Thread t, Throwable e) {
		try {
			LOG.error("", e);
			if (Platform.isFxApplicationThread()) {
				showErrorDialog(e);
			} else {
				Platform.runLater(() -> showErrorDialog(e));
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			LOG.error("", ex);
		}
	}

	private static void showErrorDialog(Throwable e) {
		String text = stacktraceToString(e);
		Stage dialog = new Stage();
		dialog.setTitle("Error");
		dialog.initModality(Modality.APPLICATION_MODAL);

		FXMLLoader loader = new FXMLLoader(ErrorController.class.getResource("ErrorController.fxml"));
		try {
			Parent root = loader.load();
			ErrorController controller = (ErrorController) loader.getController();
			controller.setErrorText(text);

			Scene scene = new Scene(root, 800, 400);
			String styleSheetURL = AlbionMarketClientApplication.class.getResource("dark.css").toString();

			scene.getStylesheets().add(styleSheetURL);

			dialog.setScene(scene);
			dialog.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
				if (KeyCode.ESCAPE == event.getCode()) {
					dialog.close();
				}
			});
			dialog.show();
			dialog.requestFocus();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	public static String stacktraceToString(Throwable e) {
		StringWriter errorMsg = new StringWriter();
		e.printStackTrace(new PrintWriter(errorMsg));
		String text = errorMsg.toString();
		return text;
	}
}
