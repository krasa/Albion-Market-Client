package krasa.albion.controller;

import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import krasa.albion.application.Notifications;
import krasa.albion.application.SpringbootJavaFxApplication;
import krasa.albion.service.ItemsCache;
import krasa.albion.service.MarketItem;
import krasa.albion.service.NetworkService;
import krasa.albion.service.Storage;
import krasa.albion.utils.CustomListViewSkin;
import krasa.albion.utils.ThreadDump;
import krasa.albion.utils.ThreadDumper;
import krasa.albion.web.MarketResponse;
import krasa.albion.web.PriceStats;
import net.rgielen.fxweaver.core.FxmlView;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;

@Component
@FxmlView
public class MainController implements Initializable, DisposableBean {
	private static final Logger log = LoggerFactory.getLogger(MainController.class);


	@FXML
	public TextArea status;
	@FXML
	public Button checkButton;
	@FXML
	public Button historyButton;
	@FXML
	public TextField name;
	@FXML
	public javafx.scene.control.ListView cities;
	@FXML
	public javafx.scene.control.TableView table;
	public ListView quality;
	public ListView tier;
	public RangeSlider ip;
	public TextField code;
	@Autowired
	private ItemsCache itemsCache;
	@Autowired
	private Storage storage;
	@Autowired
	private NetworkService networkService;


	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		name.textProperty().addListener((observable, oldValue, newValue) -> {
			MarketItem item = itemsCache.getItemByName(newValue);
			if (item != null) {
				code.setText(item.getCode() + " - id " + item.getId());
			} else {
				code.setText("");
			}
		});


		try {
			addColumn("name", 200);
			addColumn("tier", 25);
			addColumn("qualityName", 80);
			addColumn("city", 150);

			addIpColumn(50);
			addSellPriceColumn("Sell Price", 200);
			addDateColumn("sell_price_min_date");
			addDateColumn("sell_price_max_date");
//			addPriceColumn("sell_price_min", 100);
//			addPriceColumn("sell_price_max", 100);
//			addPriceColumn("buy_price_min", 100);
//			addPriceColumn("buy_price_max", 100);
			addBuyPriceColumn("Buy Price", 200);
			addDateColumn("buy_price_min_date");
			addDateColumn("buy_price_max_date");
			addColumn("item_id");

			table.setItems(FXCollections.observableArrayList());
			cities.setItems(FXCollections.observableArrayList(
					"---",
					"Lymhurst",
					"Fort Sterling",
					"Caerleon",
					"Martlock",
					"Thetford",
//					"Merlyn",
					"Bridgewatch"));

			ObservableList<String> tiers = FXCollections.observableArrayList();
			tier.setItems(tiers);
			tiers.add("---");
			for (int i = 8; i > 0; i--) {
				tiers.add("" + i + ".3");
				tiers.add("" + i + ".2");
				tiers.add("" + i + ".1");
				tiers.add("" + i + ".0");
			}
			quality.setItems(FXCollections.observableArrayList(
					"---",
					"Normal",
					"Good",
					"Outstanding",
					"Excellent",
					"Masterpiece"

			));

			cities.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			quality.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			tier.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			tier.setSkin(new CustomListViewSkin<>(tier));
			quality.setSkin(new CustomListViewSkin<>(quality));
			cities.setSkin(new CustomListViewSkin<>(cities));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		TextFields.bindAutoCompletion(name, itemsCache.names());
		storage.load(name, cities, tier, quality);
	}


	@FXML
	public void check(ActionEvent actionEvent) {

		String text = name.getText();
		for (MarketItem item : itemsCache.getEligibleItems(text)) {
			String path = new PriceStats(cities, quality, tier, ip, itemsCache).path(item);
			log.info(path);
			MarketResponse[] forObject = new RestTemplate().getForObject(path, MarketResponse[].class);
			log.info(Arrays.toString(forObject));
			for (MarketResponse response : forObject) {
				table.getItems().add(response.init(itemsCache));
				table.refresh();
			}
		}

	}

	@FXML
	public void remoteDesktop(ActionEvent actionEvent) throws IOException {

	}


	@FXML
	public void settings(ActionEvent actionEvent) {
	}

	@FXML
	public void diagnostics(ActionEvent actionEvent) {
		ThreadDump threadDumpInfo = ThreadDumper.getThreadDumpInfo(ManagementFactory.getThreadMXBean());
		String rawDump = threadDumpInfo.getRawDump();
		status.setText(rawDump);
	}


	public void error(String ip, Throwable e) {
		Platform.runLater(() -> displayException(ip, e));
	}

	public void appendLater(String line) {
		Platform.runLater(() -> status.appendText(line + "\n"));

	}

	public void appendNow(String line) {
		Platform.runLater(() -> status.appendText(line + "\n"));

	}

	private void displayException(String ip, Throwable e) {
		log.error(ip, e);
		status.appendText(Notifications.stacktraceToString(e) + "\n");
	}

	@Override
	public void destroy() throws Exception {
		storage.save(name, cities, tier, quality);
	}


	protected void addColumn(String propertyName) {
		addColumn(propertyName, 200);
	}

	private void addDateColumn(String propertyName) {
		TableColumn<MarketItem, String> column = new TableColumn(propertyName);
		column.setPrefWidth(140);
		column.setCellValueFactory(new PropertyValueFactory(propertyName));
		column.setCellFactory(c -> {
			TableCell<MarketItem, String> cell = new TableCell<MarketItem, String>() {
				private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				private SimpleDateFormat to = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || "0001-01-01T00:00:00".equals(item)) {
						setText(null);
					} else {
						try {
							setText(to.format(format.parse(item)));
						} catch (ParseException e) {
							throw new RuntimeException(e);
						}
					}
				}
			};

			return cell;
		});
		table.getColumns().add(column);
//		column.setStyle("-fx-alignment: CENTER-RIGHT;");
	}

	protected void addColumn(String propertyName, int width) {
		TableColumn<MarketItem, String> column = new TableColumn(propertyName);
		column.setPrefWidth(width);
		column.setCellValueFactory(new PropertyValueFactory(propertyName));
		table.getColumns().add(column);
//		column.setStyle("-fx-alignment: CENTER-RIGHT;");
	}

	private void addSellPriceColumn(String propertyName, int width) {
		TableColumn<MarketResponse, String> column = new TableColumn<>(propertyName);
		column.setPrefWidth(width);
		column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MarketResponse, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<MarketResponse, String> param) {
				return new SimpleStringProperty(format(param.getValue().getSell_price_min()) + " - " + format(param.getValue().getSell_price_max()));
			}
		});
		column.setComparator(new PriceComparator());
		table.getColumns().add(column);
	}

	private void addIpColumn(int width) {
		TableColumn<MarketResponse, String> column = new TableColumn<>("IP");
		column.setPrefWidth(width);
		column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MarketResponse, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<MarketResponse, String> param) {
				MarketResponse value = param.getValue();
				return new SimpleStringProperty(itemsCache.getIp(value.getItem_id(), value.getQuality()));
			}
		});
		table.getColumns().add(column);
	}

	private void addBuyPriceColumn(String propertyName, int width) {

		TableColumn<MarketResponse, String> column = new TableColumn<>(propertyName);
		column.setPrefWidth(width);
		column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MarketResponse, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<MarketResponse, String> param) {
				return new SimpleStringProperty(format(param.getValue().getBuy_price_max()) + " - " + format(param.getValue().getBuy_price_min()));
			}
		});
		column.setComparator(new PriceComparator());
		table.getColumns().add(column);
	}

	private String format(Integer value) {
		if (value == null) {
			return "x";
		} else {
			return (NumberFormat.getInstance().format(value));
		}
	}

	private void addPriceColumn(String propertyName, int width) {
		TableColumn<MarketResponse, Integer> column = new TableColumn<>(propertyName);
		column.setPrefWidth(width);
		column.setCellValueFactory(new PropertyValueFactory(propertyName));
		column.setCellFactory(tc -> new TableCell<MarketResponse, Integer>() {
			@Override
			protected void updateItem(Integer value, boolean empty) {
				super.updateItem(value, empty);
				if (empty) {
					setText(null);
				} else {
					setText(NumberFormat.getInstance().format(value));
				}
			}
		});

		table.getColumns().add(column);

	}

	public void clear(ActionEvent actionEvent) {
		table.getItems().clear();

	}

	public void web(ActionEvent actionEvent) {
		for (MarketItem item : itemsCache.getEligibleItems(name.getText())) {
			String path = new PriceStats(cities, quality, tier, ip, itemsCache).path(item);

			SpringbootJavaFxApplication instance = SpringbootJavaFxApplication.getInstance();
			HostServicesDelegate hostServices = HostServicesDelegate.getInstance(instance);
			hostServices.showDocument(path.replace("/prices/", "/view/"));
		}
	}

	public void test(ActionEvent actionEvent) {
		for (MarketItem item : itemsCache.getEligibleItems(name.getText())) {
			String path = new PriceStats(cities, quality, tier, ip, itemsCache).path(item);
			log.info(path);
		}
	}

	private static class PriceComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			try {
				String[] split = o1.split(" - ");
				String[] split2 = o2.split(" - ");
				int i = NumberFormat.getInstance().parse(split[0]).intValue();

				int i2 = NumberFormat.getInstance().parse(split2[0]).intValue();
				return i - i2;
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
