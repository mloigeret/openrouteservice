package heigit.ors.routing.parameters;

public class WheelchairParameters extends ProfileParameters 
{
	private float _maxIncline = 0.0F;
	private float _maxSlopedCurb;
	private int _surfaceType;
	private int _trackType;
	private int _smoothnessType;

	public WheelchairParameters()
	{

	}

	public float getMaximumIncline() {
		return _maxIncline;
	}

	public void setMaximumIncline(float maxIncline) {
		_maxIncline = maxIncline;
	}

	public int getSurfaceType() {
		return _surfaceType;
	}

	public void setSurfaceType(int surfaceType) {
		_surfaceType = surfaceType;
	}

	public float getMaximumSlopedCurb() {
		return _maxSlopedCurb;
	}

	public void setMaximumSlopedCurb(float maxSlopedCurb) {
		_maxSlopedCurb = maxSlopedCurb;
	}

	public int getTrackType() {
		return _trackType;
	}

	public void setTrackType(int trackType) {
		_trackType = trackType;
	}

	public int getSmoothnessType() {
		return _smoothnessType;
	}

	public void setSmoothnessType(int smoothnessType) {
		_smoothnessType = smoothnessType;
	}
}
