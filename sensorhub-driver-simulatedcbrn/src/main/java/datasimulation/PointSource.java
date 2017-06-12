package datasimulation;

/**
 * Created by Ian Patterson on 6/11/2017.
 */

// TODO: add method to make the source mobile
public class PointSource
{
	private double lat;
	private double lon;
	private double alt;
	private double intensity;
	private ChemAgent agent;

	public PointSource(double lat, double lon, double alt, double intensity) {
		this.lat = lat;
		this.lon = lon;
		this.alt = alt;
		this.intensity = intensity;
	}

	public double getLat() {

		return lat;
	}

	public double getLon() {
		return lon;
	}

	public double getAlt() {
		return alt;
	}

	public double getIntensity() {
		return intensity;
	}

	public void setLat(double lat) {

		this.lat = lat;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public void setAlt(double alt) {
		this.alt = alt;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}
}
