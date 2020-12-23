package krasa.albion.controller;

import krasa.albion.service.MarketItem;
import org.apache.commons.lang3.StringUtils;

public class Tier {
	private final String tier;

	public Tier(String tier) {
		this.tier = tier;
	}

	public String generateCode(MarketItem item) {
		String code = item.getCode();
		code = code.substring(2);

		if (this.tier.equals("---")) {
			return code;
		}
		if (tier.contains(".")) {
			String[] split = tier.split("\\.");

			if (item.isMap()) {
				code = StringUtils.substringBeforeLast(code, "_") + "_" + split[1];
				code = code + "_+" + split[1];
			}


			if (split[1].equals("0")) {
				return "T" + split[0] + code;
			} else {
				return "T" + split[0] + code + "@" + split[1];
			}
		} else {
			return "T" + tier + code;
		}
	}
}
