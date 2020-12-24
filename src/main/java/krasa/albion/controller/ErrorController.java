package krasa.albion.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class ErrorController {
	@FXML
	public Button openAndClose;
	@FXML
	public Button copyAndClose;
	@FXML
	private TextArea errorMessage;

	public void setErrorText(String text) {
		errorMessage.setText(text);
	}

	@FXML
	private void close() {
		errorMessage.getScene().getWindow().hide();
	}


	public void copyAndClose(ActionEvent actionEvent) {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		content.putString(errorMessage.getText());
		clipboard.setContent(content);
		close();
	}

}