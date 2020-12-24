package krasa.albion.domain;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;

public class PriceComparator implements Comparator<String> {
	@Override
	public int compare(String o1, String o2) {
		try {
			String[] split = o1.split(" - ");
			String[] split2 = o2.split(" - ");
			int i = NumberFormat.getInstance().parse(split[0]).intValue();

			int i2 = NumberFormat.getInstance().parse(split2[0]).intValue();
			return i - i2;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
