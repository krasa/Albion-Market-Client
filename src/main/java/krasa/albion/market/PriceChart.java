package krasa.albion.market;

import javafx.scene.control.ListView;
import krasa.albion.controller.MainController;
import krasa.albion.domain.City;
import krasa.albion.service.ItemsCache;
import krasa.albion.service.MarketItem;

public class PriceChart extends CurrentPrice {
	public PriceChart(MainController mainController) {
		super(mainController);
	}

	public PriceChart(ListView<City> cities, ListView<String> qualities, ListView<String> tiers, ItemsCache itemsCache) {
		super(cities, qualities, tiers, itemsCache);
	}

	public PriceChart(krasa.albion.market.MarketItem p, ItemsCache itemsCache) {
		super(p.getCity(), p.getQualityName(), p.getTier(), itemsCache);
	}

	@Override
	protected String subpath() {
		return "history";
	}

	@Override
	protected String pathSuffix(MarketItem item) {
		return super.pathSuffix(item) + "&time-scale=24";
	}
}
