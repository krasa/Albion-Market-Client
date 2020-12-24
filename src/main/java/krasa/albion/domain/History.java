package krasa.albion.domain;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import krasa.albion.controller.MainController;

import java.util.List;

public class History {

	private ObservableList<HistoryItem> items = FXCollections.observableArrayList();

	public void add(String path, MainController mainController) {
		items.removeIf(historyItem -> historyItem.getPath().equals(path));
		items.add(0, new HistoryItem(path, mainController));
	}

	public ObservableList<HistoryItem> getItems() {
		return items;
	}

	public void adAll(List<HistoryItem> history) {
		items.addAll(history);
	}
}
