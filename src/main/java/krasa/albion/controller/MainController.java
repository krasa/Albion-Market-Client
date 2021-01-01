package krasa.albion.controller;

import cern.extjfx.chart.XYChartPane;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import krasa.albion.application.Notifications;
import krasa.albion.application.SpringbootJavaFxApplication;
import krasa.albion.commons.ActionButtonTableCell;
import krasa.albion.commons.CustomListViewSkin;
import krasa.albion.domain.*;
import krasa.albion.market.ChartItem;
import krasa.albion.market.CurrentPrice;
import krasa.albion.market.MarketItem;
import krasa.albion.market.PriceChart;
import krasa.albion.service.ItemsCache;
import krasa.albion.service.Storage;
import krasa.albion.utils.MyUtils;
import krasa.albion.utils.TableClipboardUtils;
import krasa.albion.utils.ThreadDump;
import krasa.albion.utils.ThreadDumper;
import net.rgielen.fxweaver.core.FxmlView;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@FxmlView
public class MainController implements Initializable, DisposableBean {
	private static final Logger log = LoggerFactory.getLogger(MainController.class);
	public static final String HISTORY = "history";


	public Button test;
	public Button reloadTable;


	public TextArea status;
	public TextField name;
	public javafx.scene.control.ListView<String> cities;
	public javafx.scene.control.TableView<MarketItem> table;
	public ListView<String> quality;
	public ListView<String> tier;
	public ListView<HistoryItem> historyListView;
	public TextField code;
	public Slider ipFrom;
	public Slider ipTo;
	public Button checkButton1;
	public Button checkButton2;
	public Button resetButton;
	public ListView<Categories> categories;
	public LineChart lineChart;
	public Pane chartPane;
	public VBox centerVBox;
	public AnchorPane centerAnchor;
	public SplitPane splitPane;
	public Button charts;
	transient volatile boolean changing;
	public History history = new History();
	private AutoCompletionBinding<String> stringAutoCompletionBinding;
	private SimpleBooleanProperty checking = new SimpleBooleanProperty();

	@Autowired
	private Storage storage;
	@Autowired
	public ItemsCache itemsCache;
	private TableColumn<MarketItem, String> ipColumn;
	private boolean initialized;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		if (!new File("build.gradle").exists()) {
			remove(test);
			remove(reloadTable);
		}
		checkButton2.setTooltip(new Tooltip("Hold Ctrl for adding"));
		checkButton1.setTooltip(new Tooltip("Hold Ctrl for adding"));

		checkButton2.disableProperty().bind(
				name.textProperty().isEmpty().or(checking));
		checkButton1.disableProperty().bind(
				name.textProperty().isEmpty().or(checking));

		historyListView.setOnKeyPressed(keyEvent -> {
			KeyCodeCombination copyKeyCodeCompination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
			if (keyEvent.getCode() == KeyCode.DELETE) {
				historyListView.getItems().removeAll(historyListView.getSelectionModel().getSelectedItem());
				keyEvent.consume();
			}
			if (copyKeyCodeCompination.match(keyEvent)) {
				HistoryItem selectedItem = historyListView.getSelectionModel().getSelectedItem();
				if (selectedItem != null) {
					// create clipboard content
					final ClipboardContent clipboardContent = new ClipboardContent();
					clipboardContent.putString(selectedItem.getName());

					// set clipboard content
					Clipboard.getSystemClipboard().setContent(clipboardContent);
				}
				keyEvent.consume();

			}
		});
		charts.setOnMouseClicked(event -> charts(event));

		historyListView.setItems(history.getItems());
		historyListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
					HistoryItem storageData = historyListView.getSelectionModel().getSelectedItem();
					if (storageData == null) {
						return;
					}
					log.info(storageData.getPath());
					cities.getSelectionModel().clearSelection();
					for (String city : storageData.getCities()) {
						cities.getSelectionModel().select(city);
					}
					quality.getSelectionModel().clearSelection();
					for (String city : storageData.getQuality()) {
						quality.getSelectionModel().select(city);
					}
					tier.getSelectionModel().clearSelection();
					for (String city : storageData.getTier()) {
						tier.getSelectionModel().select(city);
					}
					name.setText(storageData.getName());


					//your code here        
				} else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
					check(new ActionEvent(HISTORY, null));
				}
			}
		});


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

		tier.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> c) {
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
		ObservableList<Categories> observableList = FXCollections.observableArrayList();
		observableList.addAll(Arrays.asList(Categories.values()));
		categories.setItems(observableList);


		ChangeListener<Number> sliderListener = (observable, oldValue, newValue) -> {
			if (!changing) {
				changing = true;
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
				changing = false;
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

		categories.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<Categories>() {
			@Override
			public void onChanged(Change<? extends Categories> c) {
				if (stringAutoCompletionBinding != null) {
					stringAutoCompletionBinding.dispose();
				}

				stringAutoCompletionBinding = TextFields.bindAutoCompletion(name, itemsCache.autocompleteNames(categories.getSelectionModel().getSelectedItems()));
				stringAutoCompletionBinding.setDelay(0);
			}
		});

		stringAutoCompletionBinding = TextFields.bindAutoCompletion(name, itemsCache.autocompleteNames(categories.getSelectionModel().getSelectedItems()));
		stringAutoCompletionBinding.setDelay(0);


		Platform.runLater(() -> {
			Storage.StorageData load = storage.load(this);

			table.sort();
			createCharts(load.charts);
			initialized = true;
		});
	}

	protected void remove(Node n) {
		if (n.getParent() instanceof Pane)
			((Pane) n.getParent()).getChildren().remove(n);
	}

	private void initTable() {
		// enable copy/paste
		TableClipboardUtils.installCopyPasteHandler(table);
		table.getSelectionModel().setSelectionMode(
				SelectionMode.MULTIPLE
		);

		table.getColumns().clear();
		addColumn("Name", "name", 200);
		addColumn("Tier", "tier", 30);
		addColumn("Quality", "qualityName", 80);
		addCityColumn("City", 150);

		ipColumn = addIpColumn(50);
		addSellPriceColumn("Sell Price", 200);
		addColumn("Age", "sellAge", 80);
//		addDateColumn("sell_price_min_date");
//			addDateColumn("sell_price_max_date");
//			addPriceColumn("sell_price_min", 100);
//			addPriceColumn("sell_price_max", 100);
//			addPriceColumn("buy_price_min", 100);
//			addPriceColumn("buy_price_max", 100);
		addBuyPriceColumn("Buy Price", 200);
		addColumn("Age", "buyAge", 80);

		addChartColumn();

//		addDateColumn("buy_price_min_date");
//			addDateColumn("buy_price_max_date");
		addColumn("item_id", "item_id");

		table.setItems(FXCollections.observableArrayList());
// programmatically set a sort column:
		table.getSortOrder().add(ipColumn);

		cities.setItems(FXCollections.observableArrayList(City.getListViewItems()));

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
		categories.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tier.setSkin(new CustomListViewSkin<>(tier));
		quality.setSkin(new CustomListViewSkin<>(quality));
		cities.setSkin(new CustomListViewSkin<>(cities));
		categories.setSkin(new CustomListViewSkin<>(categories));
		EventHandler<MouseEvent> eventHandler = new EventHandler<>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton().equals(MouseButton.PRIMARY)) {
					ActionEvent button = new ActionEvent("button", null);
					if (!event.isControlDown()) {
						clear(button);
					}
					check(button);
				}
			}

		};
		checkButton1.setOnMouseClicked(eventHandler);
		checkButton2.setOnMouseClicked(eventHandler);
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
		if (initialized) {
			save();
		} else {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Save data?", ButtonType.YES, ButtonType.NO);
			alert.showAndWait();
			if (alert.getResult() == ButtonType.YES) {
				save();
			}
		}
	}

	private void save() {
		try {
			storage.save(this);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
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

	private void addChartColumn() {
		TableColumn<MarketItem, Button> column = new TableColumn<>();
		column.setCellFactory(param -> {
			ActionButtonTableCell<MarketItem> col = new ActionButtonTableCell<>("Chart", (actionEvent, p) -> {
				chart(actionEvent.isControlDown(), actionEvent.isAltDown(), actionEvent.isShiftDown(), new PriceChart(p, itemsCache).path(p.toDomainItem()));
				return p;
			});
			col.tooltip(new Tooltip("Hold Ctrl for adding"));
			return col;
		});
		table.getColumns().add(column);
	}

	protected void addCityColumn(String label, int width) {
		TableColumn<MarketItem, MarketItem> column = new TableColumn(label);
		column.setPrefWidth(width);
		column.setCellFactory(new Callback<>() {
			@Override
			public TableCell<MarketItem, MarketItem> call(TableColumn<MarketItem, MarketItem> param) {
				return new TableCell<>() {
					@Override
					protected void updateItem(MarketItem item, boolean empty) {
						super.updateItem(item, empty);

						setText(null);
						setGraphic(null);

						if (item != null && !empty) {
							setText(item.getCity());
							City city = City.from(item.getCity());
							if (city != null) {
								Paint color = city.getColor();
								if (color != null) {
									Rectangle colorRect = new Rectangle(12, 12);
									colorRect.setFill(color);
									setGraphic(colorRect);
								}

							}
						}
					}
				};


			}
		});
		column.setCellValueFactory(param -> {
			MarketItem value = param.getValue();
			return new SimpleObjectProperty<>(value);
		});
		table.getColumns().add(column);
//		column.setStyle("-fx-alignment: CENTER-RIGHT;");
	}

	private void addSellPriceColumn(String propertyName, int width) {
		TableColumn<MarketItem, String> column = new TableColumn<>(propertyName);
		column.setPrefWidth(width);
		column.setCellValueFactory(param -> new SimpleStringProperty(format(param.getValue().getSell_price_min()) + " - " + format(param.getValue().getSell_price_max())));
		column.setComparator(new PriceComparator());
		table.getColumns().add(column);
	}

	private TableColumn<MarketItem, String> addIpColumn(int width) {
		TableColumn<MarketItem, String> column = new TableColumn<>("IP");
		column.setPrefWidth(width);
		column.setCellValueFactory(param -> {
			MarketItem value = param.getValue();
			return new SimpleStringProperty(itemsCache.getIp(value.getItem_id(), value.getQuality()));
		});
		column.setComparator(column.getComparator().reversed());
		table.getColumns().add(column);
		return column;
	}

	private void addBuyPriceColumn(String propertyName, int width) {

		TableColumn<MarketItem, String> column = new TableColumn<>(propertyName);
		column.setPrefWidth(width);
		column.setCellValueFactory(param -> new SimpleStringProperty(format(param.getValue().getBuy_price_max()) + " - " + format(param.getValue().getBuy_price_min())));
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

	public void check(ActionEvent actionEvent) {
		for (krasa.albion.service.MarketItem item : itemsCache.getEligibleItems(name.getText())) {
			String path = new CurrentPrice(this).path(item);
			if (!HISTORY.equals(actionEvent.getSource())) {
				history.add(path, this);
				historyListView.getSelectionModel().clearSelection();
			}

			log.info(path);
			checkPrice(actionEvent, path);
		}

	}

	private void checkPrice(ActionEvent actionEvent, String path) {
		checking.setValue(true);
		CompletableFuture.runAsync(() -> {
			try {
				MarketItem[] responses = new RestTemplate().getForObject(path, MarketItem[].class);
				log.info(Arrays.toString(responses));

				Platform.runLater(() -> {
					for (MarketItem response : responses) {
						response.setRequestPath(path);
						table.getItems().add(response.init(itemsCache));
						table.sort();
					}
				});
			} finally {
				Platform.runLater(() -> {
					checking.setValue(false);
				});
			}
		});
	}

	public void web(ActionEvent actionEvent) throws URISyntaxException, IOException {
		for (krasa.albion.service.MarketItem item : itemsCache.getEligibleItems(name.getText())) {
			String path = new CurrentPrice(this).path(item);

			SpringbootJavaFxApplication instance = SpringbootJavaFxApplication.getInstance();
			HostServicesDelegate hostServices = HostServicesDelegate.getInstance(instance);
			hostServices.showDocument(path.replace("/prices/", "/view/"));
		}
	}

	public void test(ActionEvent actionEvent) throws InterruptedException {
		for (krasa.albion.service.MarketItem item : itemsCache.getEligibleItems(name.getText())) {
			String path = new CurrentPrice(this).path(item);
			history.add(path, this);
			log.info(path);
		}
	}

	public void clear(ActionEvent actionEvent) {
		table.getItems().clear();
		removeCharts();
	}

	public void reset(ActionEvent actionEvent) {
		name.setText("");
		ipTo.setValue(ipTo.getMax());
		cities.getSelectionModel().clearSelection();
		tier.getSelectionModel().clearSelection();
		quality.getSelectionModel().clearSelection();
		table.getItems().clear();
		categories.getSelectionModel().clearSelection();

		removeCharts();
	}

	public void reloadUi(ActionEvent actionEvent) {
		ObservableList items = table.getItems();
		initTable();
		table.getItems().addAll(items);
		table.refresh();
		List<MyXYChartPane.ChartData> chartData = getChartData();
		removeCharts();
		createCharts(chartData);
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
				checkPrice(actionEvent, path);
			}
		}

	}

	public void openAlbionOnline(ActionEvent actionEvent) {
		SpringbootJavaFxApplication instance = SpringbootJavaFxApplication.getInstance();
		HostServicesDelegate hostServices = HostServicesDelegate.getInstance(instance);
		hostServices.showDocument("https://www.albion-online-data.com/");
	}

	private void createCharts(List<MyXYChartPane.ChartData> chartData) {
		for (int i = chartData.size() - 1; i >= 0; i--) {
			MyXYChartPane.ChartData data = chartData.get(i);
			createChart(true, true, false, data);
		}
	}

	public void charts(MouseEvent event) {
		List<krasa.albion.service.MarketItem> eligibleItems = itemsCache.getEligibleItems(name.getText());
		if (eligibleItems.size() != 1) {
			throw new RuntimeException(eligibleItems.toString());
		}
		PriceChart priceChart = new PriceChart(this);

		chart(event.isControlDown(), event.isAltDown(), event.isShiftDown(), priceChart.path(eligibleItems.get(0)));
	}

	private void chart(boolean add, boolean altDown, boolean shiftDown, String path) {
		checking.setValue(true);
		CompletableFuture.runAsync(() -> {
			try {
				log.info(path);
				ChartItem[] newItems = new RestTemplate().getForObject(path, ChartItem[].class);
				Platform.runLater(() -> {
					createChart(add, altDown, shiftDown, new MyXYChartPane.ChartData(Arrays.asList(newItems)));
				});
			} finally {
				Platform.runLater(() -> {
					checking.setValue(false);
				});
			}
		});
	}

	protected void createChart(boolean ctrlDown, boolean altDown, boolean shiftDown, MyXYChartPane.ChartData data) {
		if (!data.getItems().isEmpty()) {
			if (shiftDown) {
				ObservableList<Node> children = centerVBox.getChildren();
				if (!children.isEmpty()) {
					Node node = children.get(0);
					MyXYChartPane c = (MyXYChartPane) node;
					children.remove(c);
					children.add(0, new ChartBuilder(this, c.getItemsAndAdd(data)).create());
				} else {
					children.add(0, new ChartBuilder(this, data).create());
				}
			} else if (altDown) {
				if (!ctrlDown) {
					removeCharts();
				}
				centerVBox.getChildren().add(0, new ChartBuilder(this, data).create());
			} else if (ctrlDown) {
				for (ChartItem item : data.getItems()) {
					centerVBox.getChildren().add(0, new ChartBuilder(this, new MyXYChartPane.ChartData(Collections.singletonList(item))).create());
				}
			} else {
				removeCharts();
				for (ChartItem item : data.getItems()) {
					centerVBox.getChildren().add(0, new ChartBuilder(this, new MyXYChartPane.ChartData(Collections.singletonList(item))).create());
				}
			}
		}
	}

	private boolean removeCharts() {
		return centerVBox.getChildren().removeIf(node -> node instanceof XYChartPane);
	}

	public List<MyXYChartPane.ChartData> getChartData() {
		ArrayList<MyXYChartPane.ChartData> chartItems = new ArrayList<>();
		ObservableList<Node> children = centerVBox.getChildren();
		for (Node child : children) {
			if (child instanceof MyXYChartPane) {
				chartItems.add(((MyXYChartPane) child).getChartData());
			}
		}
		return chartItems;
	}
}
