package krasa.albion.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.ListView;
import krasa.albion.controller.MainController;
import krasa.albion.domain.HistoryItem;
import krasa.albion.web.MarketItem;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class Storage {
	public static final Path STORAGE = Paths.get("settings.json");
	public static final Path STORAGE_OLD = Paths.get("settings.json.old");
	public static final Path STORAGE_TMP = Paths.get("settings.json.tmp");


	private ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		return objectMapper;
	}

	public void save(MainController mainController) {
		StorageData storageData = new StorageData();
		storageData.cities.addAll(((ListView<String>) mainController.cities).getSelectionModel().getSelectedItems());
		storageData.tier.addAll(mainController.tier.getSelectionModel().getSelectedItems());
		storageData.quality.addAll(mainController.quality.getSelectionModel().getSelectedItems());
		storageData.categories.addAll(mainController.categories.getSelectionModel().getSelectedItems());
		storageData.name = mainController.name.getText();
		storageData.ipFrom = mainController.ipFrom.getValue();
		storageData.ipTo = mainController.ipTo.getValue();
		storageData.tableItems = mainController.table.getItems();
		storageData.history.addAll(mainController.history.getItems());

		try {
			String s = getObjectMapper().writeValueAsString(storageData);


			if (Files.exists(STORAGE)) {
				if (s.equals(Files.readString(STORAGE))) {
					return;
				}
			}

			Files.deleteIfExists(STORAGE_TMP);
			Files.writeString(STORAGE_TMP, s.toString());
			if (Files.exists(STORAGE)) {
				Files.move(STORAGE, STORAGE_OLD, StandardCopyOption.REPLACE_EXISTING);
			}
			Files.move(STORAGE_TMP, STORAGE);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void load(MainController mainController) {
		try {
			StorageData storageData;
			if (Files.exists(STORAGE)) {

				String s1 = Files.readString(STORAGE);
				storageData = getObjectMapper().readValue(s1, StorageData.class);
			} else {
				storageData = new StorageData();
			}


			for (String s : storageData.cities) {
				mainController.cities.getSelectionModel().select(s);
			}
			for (String s : storageData.quality) {
				mainController.quality.getSelectionModel().select(s);
			}
			for (String s : storageData.tier) {
				mainController.tier.getSelectionModel().select(s);
			}
			for (String s : storageData.categories) {
				mainController.categories.getSelectionModel().select(s);
			}
			mainController.name.setText(storageData.name);
			mainController.ipFrom.setValue(storageData.ipFrom);
			mainController.ipTo.setValue(storageData.ipTo);
			mainController.history.adAll(storageData.history);

			for (MarketItem tableItem : storageData.tableItems) {
				tableItem.init(mainController.itemsCache);
			}
			mainController.table.getItems().addAll(storageData.tableItems);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class StorageData {
		public double ipFrom = 1000;
		public double ipTo = 1400;
		public List<MarketItem> tableItems = new ArrayList<>();
		public List<String> categories = new ArrayList<>();
		private List<String> tier = new ArrayList<>();
		private List<String> cities = new ArrayList<>();
		private List<String> quality = new ArrayList<>();
		private List<HistoryItem> history = new ArrayList<>();
		private String name = "";

	}

}
