package nl.harmjanwestra.finemapping;

import nl.harmjanwestra.utilities.annotation.Annotation;
import nl.harmjanwestra.utilities.annotation.ensembl.EnsemblStructures;
import nl.harmjanwestra.utilities.annotation.gtf.GTFAnnotation;
import nl.harmjanwestra.utilities.association.AssociationFile;
import nl.harmjanwestra.utilities.association.AssociationResult;
import nl.harmjanwestra.utilities.association.approximatebayesposterior.ApproximateBayesPosterior;
import nl.harmjanwestra.utilities.bedfile.BedFileReader;
import nl.harmjanwestra.utilities.features.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by hwestra on 11/14/16.
 */
public class CountCodingVariants {


	public static void main(String[] args) {
		CountCodingVariants c = new CountCodingVariants();

		try {
			String annot = "/Data/Ref/Ensembl/GrCH37-b86-Structures.txt.gz";
			String bedregions = "/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-09-06-SummaryStats/NormalHWEP1e4/T1D-significantloci-75e7.bed";
			String assocfile = "/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-09-06-SummaryStats/NormalHWEP1e4/T1D-assoc0.3-COSMO-merged-posterior-significantDS75e7.txt.gz";
			c.run(annot, bedregions, assocfile);

			System.out.println();
			bedregions = "/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-09-06-SummaryStats/NormalHWEP1e4/RA-significantloci-75e7.bed";
			assocfile = "/Sync/Dropbox/2016-03-RAT1D-Finemappng/Data/2016-09-06-SummaryStats/NormalHWEP1e4/RA-assoc0.3-COSMO-merged-posterior-significantDS75e7.txt.gz";
			c.run(annot, bedregions, assocfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run(String annot, String bedregions, String assocFile) throws IOException {

		Annotation annotation = null;
		if (annot.endsWith(".gtf.gz") || annot.endsWith(".gtf")) {
			annotation = new GTFAnnotation(annot);
		} else {
			annotation = new EnsemblStructures(annot);
		}

		Collection<Gene> allgenes = annotation.getGenes();

		BedFileReader reader = new BedFileReader();
		ArrayList<Feature> regions = reader.readAsList(bedregions);

		// region variant1 pval1 posterior1 variant2 pval2 posterior2 variant3 pval3 posterior3
		AssociationFile f = new AssociationFile();

		AssociationResult[][] data = new AssociationResult[regions.size()][];
		AssociationResult[][] crediblesets = new AssociationResult[regions.size()][];

		ApproximateBayesPosterior abp = new ApproximateBayesPosterior();
		ArrayList<Feature> regionsWithCredibleSets = new ArrayList<>();
		double maxPosteriorCredibleSet = 0.9;
		int maxNrVariantsInCredibleSet = 10;
		for (int d = 0; d < regions.size(); d++) {
			boolean hasSet = false;

			ArrayList<AssociationResult> allDatasetData = f.read(assocFile, regions.get(d));
			data[d] = allDatasetData.toArray(new AssociationResult[0]);
			ArrayList<AssociationResult> credibleSet = abp.createCredibleSet(allDatasetData, maxPosteriorCredibleSet);
			crediblesets[d] = credibleSet.toArray(new AssociationResult[0]);
			if (credibleSet.size() <= maxNrVariantsInCredibleSet) {
				hasSet = true;
			}

			if (hasSet) {
				regionsWithCredibleSets.add(regions.get(d));
			}
		}

		// now we have the variants, see if they overlap

		int nrCodingCredibleSets = 0;
		int nrTotalCredibleSets = 0;

		double sumposterior = 0;
		double sumposteriorBg = 0;

		double sumposteriorIndel = 0;
		double sumposteriorBgIndel = 0;

		for (int d = 0; d < regions.size(); d++) {
			Feature region = regions.get(d);
			int nrCodingOverall = 0;
			int nrTotal = 0;

			int nrIndelOverall = 0;


			ArrayList<Gene> genes = new ArrayList<>();
			for (Gene g : allgenes) {
				if (g.overlaps(region)) {
					genes.add(g);
				}
			}


			for (AssociationResult r : data[d]) {
				// check if variant overlaps coding region
				SNPFeature snp = r.getSnp();
				if (snp.isIndel()) {
					nrIndelOverall++;
				}

				boolean coding = getIsCoding(snp, genes);
				if (coding) {
					nrCodingOverall++;
				}
				nrTotal++;
			}

			for (AssociationResult r : crediblesets[d]) {
				SNPFeature snp = r.getSnp();

				boolean coding = getIsCoding(snp, genes);
				if (snp.isIndel()) {
					sumposteriorIndel += r.getPosterior();
				}
				if (coding) {
					sumposterior += r.getPosterior();
					nrCodingCredibleSets++;
				}
				nrTotalCredibleSets++;
			}

			sumposteriorBgIndel += ((double) nrIndelOverall) / nrTotal;

			sumposteriorBg += (double) nrCodingOverall / nrTotal;
		}

		double perc = sumposterior / regions.size();
		double percBg = sumposteriorBg / regions.size();
		double percIndel = sumposteriorIndel / regions.size();
		double percBgIndel = sumposteriorBgIndel / regions.size();

		double enrichment = perc / percBg;
		double enrichmentIndel = percIndel / percBgIndel;

//		System.out.println("overall: " + nrCodingOverall + "\t" + nrTotal + "\t" + ((double) nrCodingOverall / nrTotal));
		System.out.println("sumposterior: " + perc + "\t" + percBg + "\t" + enrichment);
		System.out.println("sumposterior indel: " + percIndel + "\t" + percBgIndel + "\t" + enrichmentIndel);
//		System.out.println("credible sets: " + nrCodingCredibleSets + "\t" + nrTotalCredibleSets + "\t" + ((double) nrCodingCredibleSets / nrTotalCredibleSets));


	}

	private boolean getIsCoding(SNPFeature snp, ArrayList<Gene> genes) {
		for (Gene g : genes) {
			if (g.overlaps(snp)) {
				ArrayList<Transcript> transcripts = g.getTranscripts();
				for (Transcript t : transcripts) {
					ArrayList<Exon> exons = t.getExons();
					ArrayList<UTR> utrs = t.getUTRs();
					boolean overlapsUTR = false;
					if (utrs != null) {
						for (UTR u : utrs) {
							if (u.overlaps(snp)) {
								overlapsUTR = true;
							}
						}
					}

					if (!overlapsUTR) {
						for (Exon e : exons) {
							if (e.overlaps(snp)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

}