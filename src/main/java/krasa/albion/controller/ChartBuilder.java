package krasa.albion.controller;

import cern.extjfx.chart.NumericAxis;
import cern.extjfx.chart.XYChartPane;
import cern.extjfx.chart.plugins.DataPointTooltip;
import javafx.collections.FXCollections;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import krasa.albion.Launcher;
import krasa.albion.domain.City;
import krasa.albion.market.ChartItem;
import krasa.albion.service.ItemsCache;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChartBuilder {
	private final ChartItem item;
	private SimpleDateFormat simpleDateFormat;
	private ItemsCache itemsCache;

	public ChartBuilder(MainController mainController, ChartItem item) {
		this.item = item;
		itemsCache = mainController.itemsCache;
		simpleDateFormat = new SimpleDateFormat("dd.MM.");
	}

	protected String formatDate(ChartItem.Data data) {
		return simpleDateFormat.format(data.getTimestamp());
	}

	protected String name(ChartItem response) {
		return itemsCache.getName(response.getItem_id()) + " - " + response.getTier() + " - " + response.getQualityName() + " - " + response.getLocation();
	}

	public XYChartPane<String, Number> create() {
		String name = name(item);

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

		XYChartPane<String, Number> chartPane = new XYChartPane<>(itemsSoldChart);
		chartPane.setTitle(name);
		chartPane.setCommonYAxis(false);
		chartPane.getOverlayCharts().add(priceChart);
		chartPane.getPlugins().add(new DataPointTooltip<>());
		chartPane.getStylesheets().add(Launcher.class.getResource("charts/overlayChart.css").toExternalForm());

		List<Data<String, Number>> itemCountData = new ArrayList<>();
		List<Data<String, Number>> itemPriceData = new ArrayList<>();

		for (ChartItem.Data d : item.getData()) {
			String date = formatDate(d);
			Integer item_count = d.getItem_count();
			Integer avg_price = d.getAvg_price();

			itemCountData.add(new Data<>(date, item_count));
			itemPriceData.add(new Data<>(date, avg_price));
		}

		itemsSoldChart.getData().add(new Series<>("Sold", FXCollections.observableArrayList(itemCountData)));
		priceChart.getData().add(new Series<>("Price", FXCollections.observableArrayList(itemPriceData)));


		chartPane.setBorder(new Border(new BorderStroke(City.forValue(item.getLocation()).getColor(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//		chartPane.setPadding(new Insets(5,5,5,5));   
		return chartPane;
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
