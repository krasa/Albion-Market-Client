package krasa.albion.controller;

import cern.extjfx.chart.XYChartPane;
import javafx.scene.chart.XYChart;
import krasa.albion.market.ChartItem;

import java.util.ArrayList;
import java.util.List;

public class MyXYChartPane extends XYChartPane<String, Number> {
	ChartData chartData;

	public MyXYChartPane(ChartData data, XYChart<String, Number> chart) {
		super(chart);
		this.chartData = data;
	}

	public ChartData getItemsAndAdd(ChartData newItems) {
		ArrayList<ChartItem> chartItems = new ArrayList<>();
		chartItems.addAll(newItems.items);
		chartItems.addAll(chartData.items);
		return new ChartData(chartItems);
	}

	public ChartData getChartData() {
		return chartData;
	}

	public static class ChartData {
		private List<ChartItem> items = new ArrayList<>();

		public ChartData(List<ChartItem> items) {
			this.items = items;
		}

		public ChartData() {
		}

		public List<ChartItem> getItems() {
			return items;
		}
	}
}
