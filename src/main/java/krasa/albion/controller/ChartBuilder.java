package krasa.albion.controller;

import cern.extjfx.chart.NumericAxis;
import cern.extjfx.chart.plugins.DataPointTooltip;
import javafx.collections.FXCollections;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;
import krasa.albion.Launcher;
import krasa.albion.market.ChartItem;
import krasa.albion.service.ItemsCache;

import java.text.SimpleDateFormat;
import java.util.*;

public class ChartBuilder {
	private SimpleDateFormat simpleDateFormat;
	private ItemsCache itemsCache;
	private final MyXYChartPane.ChartData chartData;


	public ChartBuilder(MainController mainController, MyXYChartPane.ChartData chartData) {
		this.chartData = chartData;
		itemsCache = mainController.itemsCache;
		simpleDateFormat = new SimpleDateFormat("dd.MM.");
	}

	protected String formatDate(ChartItem.Data data) {
		return simpleDateFormat.format(data.getTimestamp());
	}

	protected String name(ChartItem response) {
		return response.getTier() + " - " + response.getQualityName() + " - " + response.getLocation() + " - " + itemsCache.getName(response.getItem_id());
	}

	public MyXYChartPane create() {
//		String name = name(item);

		NumericAxis yItemsSoldAxis = createYAxis();
		yItemsSoldAxis.setLowerBound(0);
//	    yRainfallAxis.setUpperBound(100);
		yItemsSoldAxis.setAutoRanging(true);
		yItemsSoldAxis.setForceZeroInRange(true);


		BarChart<String, Number> itemsSoldChart = new BarChart<>(createXAxis(), yItemsSoldAxis);
		itemsSoldChart.setTitle("Items Sold");
		itemsSoldChart.setAnimated(false);
		itemsSoldChart.getYAxis().setLabel("Items Sold");
		itemsSoldChart.getYAxis().setTickLabelFill(Color.WHITE);
		itemsSoldChart.getYAxis().lookup(".axis-label").setStyle("-fx-text-fill: white;");
		itemsSoldChart.getYAxis().setSide(Side.LEFT);

		NumericAxis yPriceAxis = createYAxis();
//		yPriceAxis.setLowerBound(0);
//        yPriceAxis.setUpperBound(response.getData().stream().mapToInt(ChartItem.Data::getAvg_price).max().getAsInt());
		yPriceAxis.setAutoRanging(true);

		LineChart<String, Number> priceChart = new LineChart<>(createXAxis(), yPriceAxis);
		priceChart.setTitle("Average Price");
		priceChart.setAnimated(false);
		priceChart.setCreateSymbols(true);
		priceChart.getYAxis().setTickLabelFill(Color.CYAN);
		priceChart.getYAxis().lookup(".axis-label").setStyle("-fx-text-fill: cyan;");
		priceChart.getYAxis().setLabel("Average Price");
		priceChart.getYAxis().setSide(Side.RIGHT);

		MyXYChartPane chartPane = new MyXYChartPane(chartData, itemsSoldChart);
//		chartPane.setTitle(name);
		chartPane.setCommonYAxis(false);
		chartPane.getOverlayCharts().add(priceChart);
		chartPane.getPlugins().add(new DataPointTooltip<>());
		chartPane.getStylesheets().add(Launcher.class.getResource("charts/overlayChart.css").toExternalForm());

		HashMap<String, Integer> strings = new LinkedHashMap<>();

		List<ChartItem> items = new ArrayList<>(chartData.getItems());
		HashSet<Object> seen = new HashSet<>();
		items.removeIf(e -> !seen.add(e.getItem_id() + e.getLocation() + e.getQuality()));

		for (ChartItem item : items) {
			List<Data<String, Number>> itemPriceData = new ArrayList<>();

			for (ChartItem.Data d : item.getData()) {
				String date = formatDate(d);
				Integer item_count = d.getItem_count();
				Integer avg_price = d.getAvg_price();


				strings.merge(date, item_count, Integer::sum);

				itemPriceData.add(new Data<>(date, avg_price));
			}

			priceChart.getData().add(new Series<>(name(item), FXCollections.observableArrayList(itemPriceData)));
		}

		List<Data<String, Number>> itemCountData = new ArrayList<>();
		for (Map.Entry<String, Integer> stringIntegerEntry : strings.entrySet()) {
			String key = stringIntegerEntry.getKey();
			Integer value = stringIntegerEntry.getValue();
			itemCountData.add(new Data<>(key, value));
		}
		itemsSoldChart.getData().add(new Series<>("Sold", FXCollections.observableArrayList(itemCountData)));

//		}
//		City city = City.forValue(item.getLocation());
//		if (city != null) {
//			chartPane.setBorder(new Border(new BorderStroke(city.getColor(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//		}
//		chartPane.setPadding(new Insets(5,5,5,5));   
		return chartPane;
	}

	public Node addSeries(MyXYChartPane c) {
		return null;
	}

	private NumericAxis createYAxis() {
		NumericAxis yAxis = new NumericAxis();
		yAxis.setAnimated(false);
		yAxis.setForceZeroInRange(false);
		yAxis.setAutoRangePadding(0.1);
		yAxis.setAutoRangeRounding(false);
		return yAxis;
	}

	private CategoryAxis createXAxis() {
		CategoryAxis xAxis = new CategoryAxis();
		xAxis.setAnimated(false);
		return xAxis;
	}


}
