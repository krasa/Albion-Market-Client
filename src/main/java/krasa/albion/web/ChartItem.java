package krasa.albion.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import krasa.albion.domain.Quality;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.beans.Transient;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartItem {
	private String requestPath;


	private String item_id;
	private String location;
	private Integer quality;
	private List<Data> data;

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
	public String getTier() {
		String substring = item_id.substring(1, 2);
		if (item_id.contains("@")) {
			return substring + "." + item_id.substring(item_id.length() - 1);
		}
		return substring + ".0";
	}

	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getQuality() {
		return quality;
	}

	public void setQuality(Integer quality) {
		this.quality = quality;
	}

	public List<Data> getData() {
		return data;
	}

	public void setData(List<Data> data) {
		this.data = data;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Data {
		Integer item_count;
		Integer avg_price;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		Date timestamp;

		public Integer getItem_count() {
			return item_count;
		}

		public void setItem_count(Integer item_count) {
			this.item_count = item_count;
		}

		public Integer getAvg_price() {
			return avg_price;
		}

		public void setAvg_price(Integer avg_price) {
			this.avg_price = avg_price;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}

		public String toString() {
			return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}
}
