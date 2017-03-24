package nl.harmjanwestra.utilities.vcf.filter.variantfilters;

import nl.harmjanwestra.utilities.vcf.VCFVariant;

/**
 * Created by hwestra on 3/21/17.
 */
public class VCFVariantMAFFilter implements VCFVariantFilter {


	private double threshold = 0.01;

	public enum MODE {
		CASES,
		CONTROLS,
		DEFAULT
	}

	private MODE m;

	public VCFVariantMAFFilter(double thresholdm) {
		this(thresholdm, MODE.DEFAULT);
	}

	public VCFVariantMAFFilter(double threshold, MODE m) {
		this.m = m;
		this.threshold = threshold;
	}

	@Override
	public String toString() {
		return "VCFVariantMAFFilter{" +
				"threshold=" + threshold +
				", m=" + m +
				'}';
	}

	@Override
	public boolean passesThreshold(VCFVariant variant) {

		if (m.equals(MODE.CASES)) {
			return variant.getMAFCases() > threshold;
		} else if (m.equals(MODE.CONTROLS)) {
			return variant.getMAFControls() > threshold;
		} else {
			return variant.getMAF() > threshold;
		}

	}


}