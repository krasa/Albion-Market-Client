package krasa.albion.commons;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.util.function.BiFunction;

public class ActionButtonTableCell<S> extends TableCell<S, Button> {

	protected final Button actionButton;

	public ActionButtonTableCell(String label, BiFunction<MouseEvent, S, S> function) {
		this.getStyleClass().add("action-button-table-cell");

		this.actionButton = new Button(label);
		actionButton.setOnMouseClicked(event -> function.apply(event, getCurrentItem()));

		this.actionButton.setMaxWidth(Double.MAX_VALUE);
	}

	public void tooltip(Tooltip tooltip) {
		setTooltip(tooltip);
		actionButton.setTooltip(tooltip);
	}

	public ActionButtonTableCell(String label, Image image, BiFunction<MouseEvent, S, S> function) {
		this(label, function);
		actionButton.setGraphic(new ImageView(image));
	}

	public S getCurrentItem() {
		return (S) getTableView().getItems().get(getIndex());
	}

	public static <S> Callback<TableColumn<S, Button>, TableCell<S, Button>> forTableColumn(String label, BiFunction<MouseEvent, S, S> function) {
		return param -> new ActionButtonTableCell<>(label, function);
	}

	public static <S> Callback<TableColumn<S, Button>, TableCell<S, Button>> forTableColumn(String label, Image icon, BiFunction<MouseEvent, S, S> function) {
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