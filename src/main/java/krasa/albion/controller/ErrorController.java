package krasa.albion.controller;


import com.sun.javafx.application.HostServicesDelegate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import krasa.albion.application.SpringbootJavaFxApplication;

public class ErrorController {
	@FXML
	public Button openAndClose;
	@FXML
	public Button copyAndClose;
	@FXML
	private TextArea errorMessage;
	private String link;

	public void setErrorText(String text) {
		errorMessage.setText(text);
	}

	@FXML
	private void close() {
		errorMessage.getScene().getWindow().hide();
	}

	public void setLink(String link) {
		openAndClose.setVisible(link != null);
		copyAndClose.setVisible(link != null);
		this.link = link;
	}

	public void openAndClose(ActionEvent actionEvent) {
		if (link != null) {
			SpringbootJavaFxApplication instance = SpringbootJavaFxApplication.getInstance();
			MainController bean = instance.getContext().getBean(MainController.class);

			HostServicesDelegate hostServices = HostServicesDelegate.getInstance(instance);
			hostServices.showDocument(link);
			close();
		}
	}

	public void copyAndClose(ActionEvent actionEvent) {
		if (link != null) {
			Clipboard clipboard = Clipboard.getSystemClipboard();
			final ClipboardContent content = new ClipboardContent();
			content.putString(link);
			clipboard.setContent(content);
			close();
		}
	}

}