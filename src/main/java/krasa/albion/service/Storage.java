package krasa.albion.service;

import ch.qos.logback.core.util.EnvUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import krasa.albion.controller.MainController;
import krasa.albion.domain.Categories;
import krasa.albion.domain.City;
import krasa.albion.domain.HistoryItem;
import krasa.albion.web.ChartItem;
import krasa.albion.web.MarketItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class Storage {
	final Path STORAGE = path("settings.json");
	final Path STORAGE_OLD = path("settings.json.old");
	final Path STORAGE_TMP = path("settings.json.tmp");

	private Path path(String s) {
		String appdata = System.getenv("APPDATA");
		if (StringUtils.isEmpty(appdata) || isDev() || !EnvUtil.isWindows()) {
			return Paths.get(s);
		}
		return Paths.get(appdata + "/Albion Market Client/" + s);
	}

	private boolean isDev() {
		return new File("build.gradle").exists();
	}

	private ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		return objectMapper;
	}

	public void save(MainController mainController) {
		StorageData storageData = new StorageData();
		storageData.cities.addAll(mainController.cities.getSelectionModel().getSelectedItems());
		storageData.tier.addAll(mainController.tier.getSelectionModel().getSelectedItems());
		storageData.quality.addAll(mainController.quality.getSelectionModel().getSelectedItems());
		storageData.categories.addAll(mainController.categories.getSelectionModel().getSelectedItems());
		storageData.name = mainController.name.getText();
		storageData.ipFrom = mainController.ipFrom.getValue();
		storageData.ipTo = mainController.ipTo.getValue();
		storageData.tableItems = mainController.table.getItems();
		storageData.history.addAll(mainController.history.getItems());
		storageData.chartData = mainController.chartData;
		storageData.splitPaneDivider = mainController.splitPane.getDividerPositions();

		try {
			String s = getObjectMapper().writeValueAsString(storageData);

			if (Files.exists(STORAGE)) {
				if (s.equals(Files.readString(STORAGE))) {
					return;
				}
			}

			Path parent = STORAGE.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}

			Files.deleteIfExists(STORAGE_TMP);
			Files.writeString(STORAGE_TMP, s);
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


			mainController.history.adAll(storageData.history);
			mainController.name.setText(storageData.name);
			mainController.ipFrom.setValue(storageData.ipFrom);
			mainController.ipTo.setValue(storageData.ipTo);

			mainController.cities.getSelectionModel().clearSelection();
			for (City s : storageData.cities) {
				mainController.cities.getSelectionModel().select(s);
			}
			mainController.quality.getSelectionModel().clearSelection();
			for (String s : storageData.quality) {
				mainController.quality.getSelectionModel().select(s);
			}
			mainController.tier.getSelectionModel().clearSelection();
			for (String s : storageData.tier) {
				mainController.tier.getSelectionModel().select(s);
			}
			mainController.categories.getSelectionModel().clearSelection();
			for (Categories s : storageData.categories) {
				mainController.categories.getSelectionModel().select(s);
			}
			for (MarketItem tableItem : storageData.tableItems) {
				tableItem.init(mainController.itemsCache);
			}
			mainController.table.getItems().addAll(storageData.tableItems);

			mainController.chartData = storageData.chartData;

			if (storageData.splitPaneDivider != null) {
				mainController.splitPane.setDividerPositions(storageData.splitPaneDivider);
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class StorageData {
		public double ipFrom = 1000;
		public double ipTo = 1400;
		public List<MarketItem> tableItems = new ArrayList<>();
		public List<Categories> categories = new ArrayList<>();
		public ChartItem[] chartData;
		public double[] splitPaneDivider;
		private List<String> tier = new ArrayList<>();
		private List<City> cities = new ArrayList<>();
		private List<String> quality = new ArrayList<>();
		private List<HistoryItem> history = new ArrayList<>();
		private String name = "";

	}

}
