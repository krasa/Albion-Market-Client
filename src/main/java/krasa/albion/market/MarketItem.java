package krasa.albion.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import krasa.albion.domain.Quality;
import krasa.albion.service.ItemsCache;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.beans.Transient;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketItem {
	private String requestPath;


	private String item_id;
	private String city;
	private Integer quality;
	private Integer sell_price_min;
	private String sell_price_min_date;
	private Integer sell_price_max;
	private String sell_price_max_date;
	private Integer buy_price_min;
	private String buy_price_min_date;
	private Integer buy_price_max;
	private String buy_price_max_date;

	private transient ItemsCache items;

	public MarketItem init(ItemsCache items) {
		this.items = items;
		return this;
	}

	public String getRequestPath() {
		return requestPath;
	}

	public void setRequestPath(String requestPath) {
		this.requestPath = requestPath;
	}

	@Transient
	public String getQualityName() {
		return Quality.asName(quality);
	}


	@Transient
	public String getName() {
		return items.getName(item_id);
	}


	@Transient
	public String getTier() {
		String substring = item_id.substring(1, 2);
		if (item_id.contains("@")) {
			return substring + "." + item_id.substring(item_id.length() - 1);
		}
		return substring;
	}

	@Transient
	public String getSellAge() throws ParseException {
		String sell_price_min_date = this.sell_price_min_date;
		if ("0001-01-01T00:00:00".equals(sell_price_min_date)) {
			return null;
		}
		return age(sell_price_min_date);
	}

	@Transient
	public String getBuyAge() throws ParseException {
		String buy_price_min_date = this.buy_price_min_date;
		if ("0001-01-01T00:00:00".equals(buy_price_min_date)) {
			return null;
		}
		return age(buy_price_min_date);
	}

	private String age(String date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneOffset.UTC);
		OffsetDateTime offsetDateTime = Instant.from(dateTimeFormatter.parse(date)).atOffset(ZoneOffset.UTC);
		Duration between = Duration.between(offsetDateTime, OffsetDateTime.now(ZoneOffset.UTC));
		if (between.toHours() > 23) {
			return between.toHours() + "h";
		} else {
			return between.toHours() + "h " + between.toMinutesPart() + "min";
		}
	}

	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Integer getQuality() {
		return quality;
	}

	public void setQuality(Integer quality) {
		this.quality = quality;
	}

	public Integer getSell_price_min() {
		return sell_price_min;
	}

	public void setSell_price_min(Integer sell_price_min) {
		this.sell_price_min = sell_price_min;
	}

	public String getSell_price_min_date() {
		return sell_price_min_date;
	}

	public void setSell_price_min_date(String sell_price_min_date) {
		this.sell_price_min_date = sell_price_min_date;
	}

	public Integer getSell_price_max() {
		return sell_price_max;
	}

	public void setSell_price_max(Integer sell_price_max) {
		this.sell_price_max = sell_price_max;
	}

	public String getSell_price_max_date() {
		return sell_price_max_date;
	}

	public void setSell_price_max_date(String sell_price_max_date) {
		this.sell_price_max_date = sell_price_max_date;
	}

	public Integer getBuy_price_min() {
		return buy_price_min;
	}

	public void setBuy_price_min(Integer buy_price_min) {
		this.buy_price_min = buy_price_min;
	}

	public String getBuy_price_min_date() {
		return buy_price_min_date;
	}

	public void setBuy_price_min_date(String buy_price_min_date) {
		this.buy_price_min_date = buy_price_min_date;
	}

	public Integer getBuy_price_max() {
		return buy_price_max;
	}

	public void setBuy_price_max(Integer buy_price_max) {
		this.buy_price_max = buy_price_max;
	}

	public String getBuy_price_max_date() {
		return buy_price_max_date;
	}

	public void setBuy_price_max_date(String buy_price_max_date) {
		this.buy_price_max_date = buy_price_max_date;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}


	public krasa.albion.service.MarketItem toDomainItem() {
		return new krasa.albion.service.MarketItem("-1", item_id, getName());
	}
}
