package nl.harmjanwestra.finemapping;

import nl.harmjanwestra.utilities.bedfile.BedFileReader;
import nl.harmjanwestra.utilities.features.Chromosome;
import nl.harmjanwestra.utilities.features.Feature;
import nl.harmjanwestra.utilities.features.SNPFeature;
import umcg.genetica.containers.Pair;
import umcg.genetica.io.text.TextFile;
import umcg.genetica.text.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Harm-Jan on 06/16/16.
 */
public class DeterminePositionWithinExhaustiveResults {

	public static void main(String[] args) {
		String regionfile = "";
		String assocfileprefix = "";
		String modelfileprefix = "";
		String outputfile = "";

		DeterminePositionWithinExhaustiveResults d = new DeterminePositionWithinExhaustiveResults();
		try {

//			regionfile = "D:\\tmp\\2016-06-19\\RA-lociwithindependentfx.bed";
//			assocfileprefix = "D:\\tmp\\2016-06-19\\exhaustive\\RA-assoc0.3-COSMO-chr";
//			modelfileprefix = "D:\\tmp\\2016-06-19\\exhaustive\\RA-assoc0.3-COSMO-chr";
//			outputfile = "D:\\tmp\\2016-06-19\\RA-exhaustiveout-PositionOfTopConditionalEffects.txt";
//			d.run(regionfile, assocfileprefix, modelfileprefix, outputfile);

//			regionfile = "D:\\tmp\\2016-06-19\\T1D-lociwithindependentfx.bed";
//			assocfileprefix = "D:\\tmp\\2016-06-19\\exhaustive\\T1D-assoc0.3-COSMO-chr";
//			modelfileprefix = "D:\\tmp\\2016-06-19\\exhaustive\\T1D-assoc0.3-COSMO-chr";
//			outputfile = "D:\\tmp\\2016-06-19\\T1D-exhaustive-PositionOfTopConditionalEffects.txt";
//			d.run(regionfile, assocfileprefix, modelfileprefix, outputfile);

			regionfile = "/Data/tmp/2016-06-20/TNFAIP3.bed";
			assocfileprefix = "/Data/tmp/2016-06-20/RA-assoc0.3-COSMO-TNFAIP3-chr";
			modelfileprefix = "/Data/tmp/2016-06-20/RA-assoc0.3-COSMO-chr";
			outputfile = "/Data/tmp/2016-06-20/RA-TNFAIP3";
			d.run(regionfile, assocfileprefix, modelfileprefix, outputfile);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void run(String regionfile,
	                String assocfileprefix,
	                String modelfileprefix,
	                String outputfile) throws IOException {


		BedFileReader reader = new BedFileReader();
		ArrayList<Feature> regions = reader.readAsList(regionfile);


		TextFile outf = new TextFile(outputfile, TextFile.W);
		outf.writeln("Region\tConditionalVariant1\tConditionalVariant2\tP\tPresentInExhaustiveOutput\tNrLowerPvals\tNrTotalPvals\tExhaustiveTopVariant1\tExhaustiveTopVariant2\tTopP");
		for (int r = 0; r < regions.size(); r++) {
			Feature region = regions.get(r);
			String modelfile = modelfileprefix + region.getChromosome().getNumber() + "-gwas-conditional-models.txt";
			String assocfile = assocfileprefix + region.getChromosome().getNumber() + "-pairwise.txt.gz";
			String[] variants = loadConditionalVariants(region, modelfile);

			if (variants == null) {
				System.err.println("Model file is broken for some reason: " + modelfile);
				System.exit(-1);
			}

			// get all pvalues in region
			TextFile tf2 = new TextFile(assocfile, TextFile.R);
			String headerln = tf2.readLine();
			String[] elems = tf2.readLineElems(TextFile.tab);

			Double pval = 0d;
			ArrayList<Double> allPvals = new ArrayList<>();

			String regionStr = region.toString();

			String[] topEffect = null;
			Double maxP = 0d;
			System.out.println("Looking for variants: " + variants[0] + "\t" + variants[1]);
			boolean foundit = false;


			ArrayList<Pair<Double, String>> outputBuffer = null;
			double highestPInBuffer = 0;
			int numberOfTopFx = 25000;
			ArrayList<Pair<Double, String>> workBuffer = new ArrayList<Pair<Double, String>>(numberOfTopFx);

			while (elems != null) {

				Feature s1 = new Feature();
				Chromosome chr = Chromosome.parseChr(elems[0]);
				Integer pos1 = Integer.parseInt(elems[1]);
				String snp1Id = elems[2];
				Integer pos2 = Integer.parseInt(elems[5]);
				String snp2Id = elems[6];

				SNPFeature snp1 = new SNPFeature();
				snp1.setChromosome(chr);
				snp1.setStart(pos1);
				snp1.setStop(pos1);
				snp1.setName(snp1Id);

				SNPFeature snp2 = new SNPFeature();
				snp2.setChromosome(chr);
				snp2.setStart(pos2);
				snp2.setStop(pos2);
				snp2.setName(snp2Id);


				if (region.overlaps(snp1) && region.overlaps(snp2)) {

					String snp1str = snp1.getChromosome().getNumber() + "_" + snp1.getStart() + "_" + snp1.getName();
					String snp2str = snp2.getChromosome().getNumber() + "_" + snp2.getStart() + "_" + snp2.getName();
//					System.out.println(snp1str);
//					System.exit(0);
					Double p = Double.parseDouble(elems[elems.length - 1]);
					if ((snp1str.equals(variants[0]) && snp2str.equals(variants[1]))
							|| (snp1str.equals(variants[1]) && snp2str.equals(variants[0]))) {
						System.out.println("found it.");
						pval = p;
						foundit = true;
					}
					if (p > maxP) {
						topEffect = elems;
						maxP = p;
					}
					allPvals.add(p);

					if (p > highestPInBuffer) {
						Pair<Double, String> pair = new Pair<Double, String>(p, Strings.concat(elems, Strings.tab), Pair.SORTBY.LEFT);
						workBuffer.add(pair);
						if (workBuffer.size() == numberOfTopFx) {
							if (outputBuffer == null) {
								outputBuffer = workBuffer;
								Collections.sort(outputBuffer);
								System.out.println("Set outputbuffer: "+outputBuffer.size());
							} else {
								System.out.println("Update outputbuffer");
								outputBuffer.addAll(workBuffer);
								Collections.sort(outputBuffer);
								ArrayList<Pair<Double, String>> tmp = new ArrayList<>(numberOfTopFx);
								tmp.addAll(outputBuffer.subList(0, numberOfTopFx));
								outputBuffer = tmp;
							}
							workBuffer = new ArrayList<>(numberOfTopFx);
						}
					}

				}
				elems = tf2.readLineElems(TextFile.tab);
			}
			tf2.close();

			if (outputBuffer == null) {
				outputBuffer = workBuffer;
				Collections.sort(outputBuffer);
			} else {
				outputBuffer.addAll(workBuffer);
				Collections.sort(outputBuffer);
				ArrayList<Pair<Double, String>> tmp = new ArrayList<>(numberOfTopFx);
				tmp.addAll(outputBuffer.subList(0, numberOfTopFx));
				outputBuffer = tmp;
			}

			TextFile outtop = new TextFile(outputfile + "_" + region.toString() + "-top" + numberOfTopFx + ".txt.gz", TextFile.W);
			outtop.writeln(headerln);
			for (int d = 0; d < outputBuffer.size(); d++) {
				outtop.writeln(outputBuffer.get(d).getRight());
			}
			outtop.close();

			int nrLowerPvals = 0;
			for (Double d : allPvals) {
				if (d > pval) {
					nrLowerPvals++;
				}
			}


			Chromosome chr = Chromosome.parseChr(topEffect[0]);
			Integer pos1 = Integer.parseInt(topEffect[1]);
			String snp1Id = topEffect[2];
			Integer pos2 = Integer.parseInt(topEffect[5]);
			String snp2Id = topEffect[6];
			SNPFeature snp1 = new SNPFeature();
			snp1.setChromosome(chr);
			snp1.setStart(pos1);
			snp1.setStop(pos1);
			snp1.setName(snp1Id);

			SNPFeature snp2 = new SNPFeature();
			snp2.setChromosome(chr);
			snp2.setStart(pos2);
			snp2.setStop(pos2);
			snp2.setName(snp2Id);

			String output = regionStr + "\t" +
					variants[0] + "\t" +
					variants[1] + "\t" +
					pval + "\t" +
					foundit + "\t" +
					nrLowerPvals + "\t" +
					allPvals.size() + "\t" +
					snp1.toString() + "\t" +
					snp2.toString() + "\t" +
					maxP;

			System.out.println(region.toString() + "\nConditional effect: " + (nrLowerPvals) + " lower pvals out of " + allPvals.size());

			outf.writeln(output);
		}

		outf.close();

	}

	private String[] loadConditionalVariants(Feature region, String modelfile) throws IOException {

		TextFile tf = new TextFile(modelfile, TextFile.R);
		String ln = tf.readLine();
		String[] elems = tf.readLineElems(TextFile.tab);
		while (elems != null) {
			String modelregion = elems[0];
			if (region.toString().equals(modelregion)) {
				String iter = elems[1];
				if (iter.equals("2")) {
					String snps = elems[2];
					return snps.split(";");
				}

			}
			elems = tf.readLineElems(TextFile.tab);
		}

		tf.close();

		return null;
	}

}
