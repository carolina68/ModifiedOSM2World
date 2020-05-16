package org.osm2world.console;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * immutable latitude/longitude/elevation coordinate triple
 */
public class LatLonEle {

	public final double lat;
	public final double lon;
	public final double ele;

	// typographical minus '−' works around the CLI parser's special handling of '-'
	private static final String DOUBLE_P = "[+-−]?\\d+(?:\\.\\d+)?";

	/**
	 * pattern for parseable arguments
	 */
	public static final String PATTERN = "("+DOUBLE_P+"),("+DOUBLE_P+")";
	public static final String PATTERN_WITH_ELE = PATTERN + ",(" + DOUBLE_P + ")";

	/**
	 * regular constructor
	 */
	public LatLonEle(double lat, double lon, double ele) {
		this.lat = lat;
		this.lon = lon;
		this.ele = ele;
		validateValues();
	}

	/**
	 * regular constructor (with default elevation of 0)
	 */
	public LatLonEle(double lat, double lon) {
		this(lat, lon, 0);
	}

	/**
	 * parsing constructor
	 * @param arg  command line argument to be parsed;
	 *             formats see {@link #PATTERN} and {@link #PATTERN_WITH_ELE}
	 */
	public LatLonEle(String arg) {

		arg = arg.replace('−', '-');

		Matcher mEle = Pattern.compile(PATTERN_WITH_ELE).matcher(arg);
		Matcher m = Pattern.compile(PATTERN).matcher(arg);
		if (mEle.matches()) {
			lat = Double.parseDouble(mEle.group(1));
			lon = Double.parseDouble(mEle.group(2));
			ele = Double.parseDouble(mEle.group(3));
			validateValues();
		} else if (m.matches()) {
			lat = Double.parseDouble(m.group(1));
			lon = Double.parseDouble(m.group(2));
			ele = 0;
			validateValues();
		} else {
			throw new IllegalArgumentException("argument doesn't match: " + arg);
		}
	}

	/**
	 * @throws IllegalArgumentException  for incorrect field values
	 */
	private void validateValues() {
		if (lat > 90 || lat < -90 || lon > 180 || lon < -180) {
			throw new IllegalArgumentException("not valid: " + lat + ", " + lon);
		}
	}

	@Override
	public String toString() {
		return lat + "," + lon;
	}

}
