package org.bbop.paint.model;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Set;


class AminoAcidStats {
	private HashMap<String, Double> aaFrequency;
	
	protected static Logger log = Logger.getLogger(AminoAcidStats.class);

	public AminoAcidStats () {
		aaFrequency = null;
	}

	public double getAAFrequency(char aa) {
		String aa_str = String.valueOf(aa);
		if (aaFrequency != null && aaFrequency.get(aa_str) != null)
			return aaFrequency.get(aa_str).doubleValue();
		else
			return 0;
	}

	public void setAAFrequency(char aa, double frequency) {
		String aa_str = String.valueOf(aa);
		if (this.aaFrequency == null)
			this.aaFrequency = new HashMap<String, Double> ();
		this.aaFrequency.put(aa_str, Double.valueOf(frequency));
	}

	public Set<String> getAAs() {
		return aaFrequency.keySet();
	}
}
