package krasa.albion.commons;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.util.function.Function;

public class ActionButtonTableCell<S> extends TableCell<S, Button> {

	private final Button actionButton;

	public ActionButtonTableCell(String label, Function<S, S> function) {
		this.getStyleClass().add("action-button-table-cell");

		this.actionButton = new Button(label);
		this.actionButton.setOnAction((ActionEvent e) -> {
			function.apply(getCurrentItem());
		});
		this.actionButton.setMaxWidth(Double.MAX_VALUE);
	}

	public ActionButtonTableCell(String label, Image image, Function<S, S> function) {
		this(label, function);
		actionButton.setGraphic(new ImageView(image));
	}

	public S getCurrentItem() {
		return (S) getTableView().getItems().get(getIndex());
	}

	public static <S> Callback<TableColumn<S, Button>, TableCell<S, Button>> forTableColumn(String label, Function<S, S> function) {
		return param -> new ActionButtonTableCell<>(label, function);
	}

	public static <S> Callback<TableColumn<S, Button>, TableCell<S, Button>> forTableColumn(String label, Image icon, Function<S, S> function) {
		return param -> new ActionButtonTableCell<>(label, icon, function);
	}

	@Override
	public void updateItem(Button item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setGraphic(null);
		} else {
			setGraphic(actionButton);
		}
	}
}