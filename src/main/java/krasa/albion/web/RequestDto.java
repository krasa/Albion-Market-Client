package krasa.albion.web;

import javafx.scene.control.ListView;
import krasa.albion.controller.Tier;
import krasa.albion.service.ItemsCache;
import krasa.albion.service.MarketItem;
import org.controlsfx.control.RangeSlider;

import java.util.List;

public class RequestDto {
	private final List<String> cities;
	private final List<String> tiers;
	private final ItemsCache itemsCache;
	private final List<String> qualities;

	public RequestDto(ListView cities, ListView qualities, ListView tiers, RangeSlider ip, ItemsCache itemsCache) {
		this.cities = cities.getSelectionModel().getSelectedItems();
		this.qualities = qualities.getSelectionModel().getSelectedItems();
		this.tiers = tiers.getSelectionModel().getSelectedItems();
//		double min = ip.getMin();
//		double max = ip.getMax();


		this.itemsCache = itemsCache;
	}

	public String path(MarketItem item) {
		String code = item.getCode();

		StringBuilder sb = new StringBuilder();

		if (tiers.isEmpty() || tiers.contains("---")) {
			sb.append(code);
		} else {
			for (int i = 0; i < tiers.size(); i++) {
				String tier = tiers.get(i);
				String adjustedCode = new Tier(tier).generateCode(item);
				if (itemsCache.containsCode(adjustedCode)) {
					sb.append(adjustedCode);
					sb.append(",");
				} else {
					throw new RuntimeException("invalid code=" + adjustedCode);
				}
			}
		}

		sb.append("?");
		if (!cities.isEmpty() && !cities.contains("---")) {
			sb.append("locations=");
			for (String city : cities) {
				sb.append(city);
				sb.append(",");
			}
		}
		if (!item.isMap() && !qualities.isEmpty() && !qualities.contains("---")) {
			sb.append("&qualities=");
			for (String s : qualities) {
				sb.append(Quality.asCode(s));
				sb.append(",");
			}
		}
		return "https://www.albion-online-data.com/api/v2/stats/prices/" + sb.toString();
	}

}
