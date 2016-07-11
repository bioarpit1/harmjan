package nl.harmjanwestra.finemapping;

import nl.harmjanwestra.finemapping.plots.PlotterAccuracy;
import nl.harmjanwestra.utilities.enums.Chromosome;
import nl.harmjanwestra.utilities.features.Feature;
import nl.harmjanwestra.utilities.vcf.VCFVariant;
import umcg.genetica.io.text.TextFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by hwestra on 5/28/16.
 */
public class VariantCounter {

	public static void main(String[] args) {
		try {
			VariantCounter c = new VariantCounter();
			c.countAccuracy();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void countAccuracy() throws IOException {

		String variantsOnIC = "/Data/tmp/2016-05-20/T1D-recode-stats.vcf.gz";

		String seqpanelvcf = "/Data/tmp/2016-05-28/seqpanelfiltered-maf0005-cr0950-rd10-gq30-runNamesFixed-RASampleNamesFixed-badSamplesRemoved-mixupsFixed.vcf.gz";

		String[] files = new String[]{
				"/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-06-21-ImputationQuality/2016-07-10-Accuracy/T1D-EUR.txt",
				"/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-06-21-ImputationQuality/2016-07-10-Accuracy/T1D-COSMO.txt",
				"/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-06-21-ImputationQuality/2016-07-10-Accuracy/T1D-COSMO-EAGLE.txt",
				"/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-06-21-ImputationQuality/2016-07-10-Accuracy/T1D-COSMO-SHAPEIT.txt",
				"/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-06-21-ImputationQuality/2016-07-10-Accuracy/T1D-HRC-EAGLE.txt",
				"/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-06-21-ImputationQuality/2016-07-10-Accuracy/T1D-HRC-SHAPEIT.txt",
				"/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-06-21-ImputationQuality/2016-07-10-Accuracy/T1D-HRC-EAGLE-Michigan.txt"
		};
		String[] labels = new String[]{
				"EUR",
				"COSMO",
				"HRC / COSMO / EAGLE",
				"HRC / COSMO / SHAPEIT",
				"HRC / HRC / EAGLE",
				"HRC / HRC / SHAPEIT",
				"HRC / HRC / EAGLE / MICHIGAN"
		};

		files = new String[]{
				"/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-06-21-ImputationQuality/2016-07-10-Accuracy/RA-EUR.txt",
				"/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-06-21-ImputationQuality/2016-07-10-Accuracy/RA-COSMO.txt",
				"/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-06-21-ImputationQuality/2016-07-10-Accuracy/RA-HRC-w100kb.txt",
		};
		labels = new String[]{
				"EUR",
				"COSMO",
				"HRC / COSMO / EAGLE"
		};

		// get a list of maf > 0.005 variants that on the sequencingpanel
		TextFile tf = new TextFile(seqpanelvcf, TextFile.R);
		ArrayList<VCFVariant> seqpanel = new ArrayList<>();
		String ln = tf.readLine();

		double mafthreshold = 0.01;
		double upperthreshold = 1;
		double infothreshold = 0.5;
		boolean includeICVariants = true;

		boolean includeId = true;
		boolean includeIndels = false;

		HashSet<String> variantsOnICHash = loadVariantHash(variantsOnIC, includeId);
		System.out.println(variantsOnICHash.size() + " total on IC");
		ArrayList<VCFVariant> variantsNotOnImmunoChip = new ArrayList<>();
		ArrayList<VCFVariant> variantsOnImmunoChip = new ArrayList<>();
		while (ln != null) {
			if (!ln.startsWith("#")) {
				String[] elems = ln.split("\t");
				Chromosome chr = Chromosome.parseChr(elems[0]);
				if (chr.isAutosome()) {
					VCFVariant variant = new VCFVariant(ln, VCFVariant.PARSE.ALL);
					if (variant.getMAF() > mafthreshold && variant.getMAF() < upperthreshold) {
						seqpanel.add(variant);
						boolean varOnIc = isVariantOnIC(elems, variantsOnICHash, includeId);
						boolean indel = isIndel(elems);

						if (includeIndels || (!includeIndels && !indel)) {
							if (!varOnIc) {
								variantsNotOnImmunoChip.add(variant);
							} else {
								variantsOnImmunoChip.add(variant);
							}
						}
					}
				}
			}
			ln = tf.readLine();
		}
		tf.close();

		System.out.println(seqpanel.size() + " variants in VCF");
		System.out.println(variantsNotOnImmunoChip.size() + " not on IC");
		System.out.println(variantsOnImmunoChip.size() + " on IC");


		// get a list of imputed variants for each of the sequencing panels


		System.out.println("MAF> " + mafthreshold);
		System.out.println("INFO> " + infothreshold);


		PlotterAccuracy p = new PlotterAccuracy();

		HashSet<String> sequencedVariantsHash = null;
		if (includeICVariants) {
			ArrayList<VCFVariant> allvars = new ArrayList<>();
			allvars.addAll(variantsNotOnImmunoChip);
			allvars.addAll(variantsOnImmunoChip);
			sequencedVariantsHash = hashit(allvars, includeId);
		} else {
			sequencedVariantsHash = hashit(variantsNotOnImmunoChip, includeId);
		}

		System.out.println(sequencedVariantsHash.size() + " after hashing");
		System.out.println(files.length + " files");
		for (int f = 0; f < files.length; f++) {
			// get the imputation accuracies for these variants
			TextFile tf2 = new TextFile(files[f], TextFile.R);

			String[] elems = tf2.readLineElems(TextFile.tab);
			int nrSequenced = 0;
			int nrSequencedPassingRSQ = 0;
			int nrSequencedPassingMaf = 0;
			int nrSequencdPassingMafAndRSQ = 0;
			while (elems != null) {


				if (!elems[p.rsqlcol].equals("null")) {
					double val = Double.parseDouble(elems[p.rsqlcol]);
					double maf = Double.parseDouble(elems[p.maf2col]);

					String[] varElems = elems[0].split("_");

					boolean sequenced = isVariantOnIC(varElems, sequencedVariantsHash, includeId);

					if (sequenced) {
						nrSequenced++;
						if (maf > mafthreshold) {
							nrSequencedPassingMaf++;
							if (val > infothreshold) {
								nrSequencdPassingMafAndRSQ++;
							}
						}
						if (val > infothreshold) {
							nrSequencedPassingRSQ++;
						}
					}
				}

				elems = tf2.readLineElems(TextFile.tab);
			}
			tf2.close();

			System.out.println("----");
			System.out.println(labels[f]);

			System.out.println("nrSequenced\t" + nrSequenced + "\t" + ((double) nrSequenced / sequencedVariantsHash.size()));
			System.out.println("nrSequencedPassingRSQ\t" + nrSequencedPassingRSQ + "\t" + ((double) nrSequencedPassingRSQ / sequencedVariantsHash.size()) + "\t" + ((double) nrSequencedPassingRSQ / nrSequenced));
			System.out.println("nrSequencedPassingMaf\t" + nrSequencedPassingMaf + "\t" + ((double) nrSequencedPassingMaf / sequencedVariantsHash.size()));
			System.out.println("nrSequencdPassingMafAndRSQ\t" + nrSequencdPassingMafAndRSQ + "\t" + ((double) nrSequencdPassingMafAndRSQ / sequencedVariantsHash.size()));
			System.out.println();
		}


	}

	public boolean isIndel(String[] elems) {

		String alleles = elems[3] + "," + elems[4];
		String[] alleleElems = alleles.split(",");

		for (String s : alleleElems) {
			if (s.length() > 1) {
				return true;
			}
		}
		return false;
	}

	public HashSet<String> hashit(ArrayList<VCFVariant> variantsNotOnImmunoChip, boolean includeId) {
		HashSet<String> variantHash = new HashSet<String>();
		for (VCFVariant var : variantsNotOnImmunoChip) {
			String variant = "";
			if (includeId) {
				variant = var.getChrObj().toString() + "_" + var.getPos() + "_" + var.getId();
			} else {
				variant = var.getChrObj().toString() + "_" + var.getPos();
			}
			variantHash.add(variant);
		}


		return variantHash;
	}

	public HashSet<String> loadVariantHash(String variantsOnIC, boolean includeId) throws IOException {
		TextFile tf = new TextFile(variantsOnIC, TextFile.R);
		HashSet<String> variantIds = new HashSet<String>();
		String[] elems = tf.readLineElems(TextFile.tab);
		while (elems != null) {
			if (!elems[0].startsWith("#")) {
				if (includeId) {
					String id = Chromosome.parseChr(elems[0]).toString() + "_" + elems[1] + "_" + elems[2];
					variantIds.add(id);
				} else {
					String id = Chromosome.parseChr(elems[0]).toString() + "_" + elems[1];
					variantIds.add(id);
				}

			}
			elems = tf.readLineElems(TextFile.tab);
		}
		tf.close();
		return variantIds;
	}

	private double getMaf(String[] elems) {
		String[] infoElems = elems[7].split(";");
		double maf = 1;
		for (String info : infoElems) {
			if (info.startsWith("AF")) {
				String[] elems2 = info.split("=");
				String[] elems3 = elems2[1].split(",");
				for (String afStr : elems3) {
					double af = Double.parseDouble(afStr);
					if (af > 0.5) {
						af = 1 - af;
					}
					if (af < maf) {
						maf = af;
					}

				}
			}
		}
		return maf;
	}

	private double getInfo(String[] elems) {
		String[] infoElems = elems[7].split(";");
		double infoscore = 0;
		for (String info : infoElems) {
			if (info.startsWith("INFO")) {
				String[] elems2 = info.split("=");
				infoscore = Double.parseDouble(elems2[1]);
			}
		}
		return infoscore;
	}


	public boolean mafbelowfthreshold(String[] elems, double t) {
		double maf = getMaf(elems);
		if (maf < t) {
			return true;
		}
		return false;
	}

	public boolean isVariantOnIC(String[] elems, HashSet<String> variantHash, boolean includeId) {
		if (includeId) {
			String variant = Chromosome.parseChr(elems[0]).toString() + "_" + elems[1] + "_" + elems[2];
			return variantHash.contains(variant);
		} else {
			String variant = Chromosome.parseChr(elems[0]).toString() + "_" + elems[1];
			return variantHash.contains(variant);
		}

	}

	private boolean isWithinRegion(ArrayList<Feature> list, String[] elems) {
		Feature varfeat = new Feature();

		int pos = Integer.parseInt(elems[1]);
		varfeat.setChromosome(Chromosome.parseChr(elems[0]));
		varfeat.setStart(pos);
		varfeat.setStop(pos);
		for (Feature f : list) {
			if (f.overlaps(varfeat)) {
				return true;
			}
		}
		return false;
	}


}
