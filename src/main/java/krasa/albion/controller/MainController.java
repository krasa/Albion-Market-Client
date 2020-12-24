package krasa.albion.controller;

import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import krasa.albion.application.Notifications;
import krasa.albion.application.SpringbootJavaFxApplication;
import krasa.albion.domain.PriceComparator;
import krasa.albion.domain.Tier;
import krasa.albion.service.ItemsCache;
import krasa.albion.service.NetworkService;
import krasa.albion.service.Storage;
import krasa.albion.utils.*;
import krasa.albion.web.MarketItem;
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
import java.util.HashSet;
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
	public javafx.scene.control.TableView<MarketItem> table;
	public ListView<String> quality;
	public ListView<String> tier;
	public RangeSlider ip;
	public TextField code;
	public Slider ipFrom;
	public Slider ipTo;
	public Button checkButton1;
	public Button checkButton2;
	public Button resetButton;
	public Button reloadTable;
	@Autowired
	public ItemsCache itemsCache;
	@Autowired
	private Storage storage;
	@Autowired
	private NetworkService networkService;
	boolean changing;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

		// enable copy/paste
		TableClipboardUtils.installCopyPasteHandler(table);
		checkButton1.setGraphic(new ImageView(MyUtils.getImage("rerun.png")));
		checkButton2.setGraphic(new ImageView(MyUtils.getImage("rerun.png")));
		resetButton.setGraphic(new ImageView(MyUtils.getImage("delete.png")));

		name.textProperty().addListener((observable, oldValue, newValue) -> {
			krasa.albion.service.MarketItem item = itemsCache.getItemByName(newValue);
			if (item != null) {
				code.setText(item.getCode() + " - id " + item.getId());
			} else {
				code.setText("");
			}
		});

		tier.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (!changing) {
					changing = true;
					ObservableList<String> selectedItems = tier.getSelectionModel().getSelectedItems();
					int min = Integer.MAX_VALUE;
					int max = 0;

					for (String item : selectedItems) {
						if (item.equals("---")) {
							continue;
						}
						int ip = new Tier(item).getIp();
						min = Math.min(min, ip);
						max = Math.max(max, ip);
					}
					if (max > 0) {
						ipTo.setValue(max);
					}
					if (min < Integer.MAX_VALUE) {
						ipFrom.setValue(min);
					}
					changing = false;
				}

			}
		});
		ChangeListener<Number> sliderListener = (observable, oldValue, newValue) -> {
			//https://wiki.albiononline.com/wiki/Item_Power
			if (!changing) {

				double from = ipFrom.getValue();
				double to = ipTo.getValue();

				MultipleSelectionModel<String> selectionModel = tier.getSelectionModel();
				selectionModel.clearSelection();
				for (String item : tier.getItems()) {
					if (item.equals("---")) {
						continue;
					}
					int ip = new Tier(item).getIp();
					if (ip >= from
							&& ip <= to) {
						selectionModel.select(item);
					}
				}
			}
		};

		ipFrom.valueProperty().addListener(observable -> {
			double from = ipFrom.getValue();
			double to = ipTo.getValue();

			if (from > to) {
				ipTo.setValue(from);
			}
		});

		ipTo.valueProperty().addListener(observable -> {
			double from = ipFrom.getValue();
			double to = ipTo.getValue();

			if (from > to) {
				ipFrom.setValue(to);
			}
		});
		ipFrom.valueProperty().addListener(sliderListener);
		ipTo.valueProperty().addListener(sliderListener);

		try {
			initTable();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		TextFields.bindAutoCompletion(name, itemsCache.names());
		storage.load(this);
	}

	private void initTable() {
		table.getColumns().clear();
		addColumn("name", "name", 200);
		addColumn("tier", "tier", 25);
		addColumn("Quality", "qualityName", 80);
		addColumn("city", "city", 150);

		addIpColumn(50);
		addSellPriceColumn("Sell Price", 200);
		addColumn("Elapsed", "sellElapsed", 80);
//		addDateColumn("sell_price_min_date");
//			addDateColumn("sell_price_max_date");
//			addPriceColumn("sell_price_min", 100);
//			addPriceColumn("sell_price_max", 100);
//			addPriceColumn("buy_price_min", 100);
//			addPriceColumn("buy_price_max", 100);
		addBuyPriceColumn("Buy Price", 200);
		addColumn("Elapsed", "buyElapsed", 80);
//		addDateColumn("buy_price_min_date");
//			addDateColumn("buy_price_max_date");
		addColumn("item_id", "item_id");

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
			if (i >= 4) {
				tiers.add("" + i + ".3");
				tiers.add("" + i + ".2");
				tiers.add("" + i + ".1");
				tiers.add("" + i + ".0");
			} else {
				tiers.add("" + i + ".0");
			}
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
	}


	@FXML
	public void check(ActionEvent actionEvent) {
		String text = name.getText();
		for (krasa.albion.service.MarketItem item : itemsCache.getEligibleItems(text)) {
			String path = new PriceStats(cities, quality, tier, ip, itemsCache).path(item);
			log.info(path);
			cheackPrice(path);
		}

	}

	private void cheackPrice(String path) {
		MarketItem[] responses = new RestTemplate().getForObject(path, MarketItem[].class);
		log.info(Arrays.toString(responses));
		for (MarketItem response : responses) {
			response.setRequestPath(path);
			table.getItems().add(response.init(itemsCache));
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
		storage.save(this);
	}


	protected void addColumn(String label, String propertyName) {
		addColumn(label, propertyName, 200);
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

	protected void addColumn(String label, String propertyName, int width) {
		TableColumn<MarketItem, String> column = new TableColumn(label);
		column.setPrefWidth(width);
		column.setCellValueFactory(new PropertyValueFactory(propertyName));
		table.getColumns().add(column);
//		column.setStyle("-fx-alignment: CENTER-RIGHT;");
	}

	private void addSellPriceColumn(String propertyName, int width) {
		TableColumn<MarketItem, String> column = new TableColumn<>(propertyName);
		column.setPrefWidth(width);
		column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MarketItem, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<MarketItem, String> param) {
				return new SimpleStringProperty(format(param.getValue().getSell_price_min()) + " - " + format(param.getValue().getSell_price_max()));
			}
		});
		column.setComparator(new PriceComparator());
		table.getColumns().add(column);
	}

	private void addIpColumn(int width) {
		TableColumn<MarketItem, String> column = new TableColumn<>("IP");
		column.setPrefWidth(width);
		column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MarketItem, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<MarketItem, String> param) {
				MarketItem value = param.getValue();
				return new SimpleStringProperty(itemsCache.getIp(value.getItem_id(), value.getQuality()));
			}
		});
		table.getColumns().add(column);
	}

	private void addBuyPriceColumn(String propertyName, int width) {

		TableColumn<MarketItem, String> column = new TableColumn<>(propertyName);
		column.setPrefWidth(width);
		column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MarketItem, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<MarketItem, String> param) {
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
		TableColumn<MarketItem, Integer> column = new TableColumn<>(propertyName);
		column.setPrefWidth(width);
		column.setCellValueFactory(new PropertyValueFactory(propertyName));
		column.setCellFactory(tc -> new TableCell<MarketItem, Integer>() {
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
		for (krasa.albion.service.MarketItem item : itemsCache.getEligibleItems(name.getText())) {
			String path = new PriceStats(cities, quality, tier, ip, itemsCache).path(item);

			SpringbootJavaFxApplication instance = SpringbootJavaFxApplication.getInstance();
			HostServicesDelegate hostServices = HostServicesDelegate.getInstance(instance);
			hostServices.showDocument(path.replace("/prices/", "/view/"));
		}
	}

	public void test(ActionEvent actionEvent) {
		for (krasa.albion.service.MarketItem item : itemsCache.getEligibleItems(name.getText())) {
			String path = new PriceStats(cities, quality, tier, ip, itemsCache).path(item);
			log.info(path);
		}
	}

	public void reset(ActionEvent actionEvent) {
		name.setText("");
		cities.getSelectionModel().clearSelection();
		tier.getSelectionModel().clearSelection();
		quality.getSelectionModel().clearSelection();
	}

	public void reloadTable(ActionEvent actionEvent) {
		ObservableList items = table.getItems();
		initTable();
		table.getItems().addAll(items);
		table.refresh();
	}

	public void refreshData(ActionEvent actionEvent) {
		ObservableList<MarketItem> items = table.getItems();

		HashSet<String> paths = new HashSet<>();
		for (MarketItem item : items) {
			paths.add(item.getRequestPath());
		}
		table.getItems().clear();

		for (String path : paths) {
			if (path != null) {
				Platform.runLater(() -> cheackPrice(path));
			}
		}

	}
}
