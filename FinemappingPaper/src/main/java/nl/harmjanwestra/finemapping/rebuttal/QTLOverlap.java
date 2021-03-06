package nl.harmjanwestra.finemapping.rebuttal;

import nl.harmjanwestra.finemapping.annotation.EQTL;
import nl.harmjanwestra.utilities.annotation.ensembl.EnsemblStructures;
import nl.harmjanwestra.utilities.enums.Chromosome;
import nl.harmjanwestra.utilities.features.Feature;
import nl.harmjanwestra.utilities.features.FeatureComparator;
import nl.harmjanwestra.utilities.features.Gene;
import nl.harmjanwestra.utilities.features.SNPFeature;
import nl.harmjanwestra.utilities.legacy.genetica.containers.Pair;
import nl.harmjanwestra.utilities.legacy.genetica.io.text.TextFile;
import nl.harmjanwestra.utilities.legacy.genetica.text.Strings;
import nl.harmjanwestra.utilities.math.DetermineLD;
import nl.harmjanwestra.utilities.vcf.VCFTabix;
import nl.harmjanwestra.utilities.vcf.VCFVariant;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QTLOverlap {
	
	public static void main(String[] args) {
		
		String disk = "d:";
		
		String[] eqtlfiles = new String[]{
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\ImmVar\\Raj\\tableS12_meta_cd4T_cis_fdr05-upd.tab",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\Milani\\CD4-cis-eQTLs-ProbeLevelFDR0.5.txt.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\Milani\\CD8-cis-eQTLs-ProbeLevelFDR0.5.txt.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BiosEQTLs\\eQTLsFDR0.05-ProbeLevel.txt.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\mono_gene_nor_combat_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\mono_K27AC_log2rpm_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\mono_K4ME1_log2rpm_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\mono_meth_M_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\mono_psi_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\neut_gene_nor_combat_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\neut_K27AC_log2rpm_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\neut_K4ME1_log2rpm_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\neut_meth_M_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\neut_psi_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\tcel_gene_nor_combat_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\tcel_K27AC_log2rpm_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\tcel_K4ME1_log2rpm_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\tcel_meth_M_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\BluePrint\\tcel_psi_peer_10_all_summary-fdr005.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\Sun-pQTL\\table1.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Adipose_Subcutaneous_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Adipose_Visceral_Omentum_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Adrenal_Gland_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Artery_Aorta_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Artery_Coronary_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Artery_Tibial_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Brain_Anterior_cingulate_cortex_BA24_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Brain_Caudate_basal_ganglia_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Brain_Cerebellar_Hemisphere_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Brain_Cerebellum_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Brain_Cortex_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Brain_Frontal_Cortex_BA9_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Brain_Hippocampus_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Brain_Hypothalamus_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Brain_Nucleus_accumbens_basal_ganglia_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Brain_Putamen_basal_ganglia_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Breast_Mammary_Tissue_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Cells_EBV-transformed_lymphocytes_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Cells_Transformed_fibroblasts_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Colon_Sigmoid_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Colon_Transverse_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Esophagus_Gastroesophageal_Junction_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Esophagus_Mucosa_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Esophagus_Muscularis_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Heart_Atrial_Appendage_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Heart_Left_Ventricle_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Liver_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Lung_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Muscle_Skeletal_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Nerve_Tibial_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Ovary_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Pancreas_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Pituitary_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Prostate_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Skin_Not_Sun_Exposed_Suprapubic_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Skin_Sun_Exposed_Lower_leg_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Small_Intestine_Terminal_Ileum_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Spleen_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Stomach_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Testis_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Thyroid_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Uterus_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Vagina_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz",
				disk + "\\Sync\\SyncThing\\Data\\eQTLs\\GTEx\\GTEx_Analysis_v6p_eQTL/Whole_Blood_Analysis.v6p.signif_snpgene_pairs.txt.gz.tab.gz"
		};
		
		String[] eqtlfilenames = new String[]{
				"Raj-Tcell",
				"Milani-CD4",
				"Milani-CD8",
				"Bios-WholeBlood",
				"Blueprint-Monocyte-eQTL",
				"Blueprint-Monocyte-hQTL-K27AC",
				"Blueprint-Monocyte-hQTL-K4ME1",
				"Blueprint-Monocyte-mQTL",
				"Blueprint-Monocyte-sQTL",
				"Blueprint-Neutrophil-eQTL",
				"Blueprint-Neutrophil-hQTL-K27AC",
				"Blueprint-Neutrophil-hQTL-K4ME1",
				"Blueprint-Neutrophil-mQTL",
				"Blueprint-Neutrophil-sQTL",
				"Blueprint-TCell-eQTL",
				"Blueprint-TCell-hQTL-K27AC",
				"Blueprint-TCell-hQTL-K4ME1",
				"Blueprint-TCell-mQTL",
				"Blueprint-TCell-sQTL",
				"Sun-pQTL",
				"GTEx-Adipose_Subcutaneous",
				"GTEx-Adipose_Visceral_Omentum",
				"GTEx-Adrenal_Gland",
				"GTEx-Artery_Aorta",
				"GTEx-Artery_Coronary",
				"GTEx-Artery_Tibial",
				"GTEx-Brain_Anterior_cingulate_cortex_BA24",
				"GTEx-Brain_Caudate_basal_ganglia",
				"GTEx-Brain_Cerebellar_Hemisphere",
				"GTEx-Brain_Cerebellum",
				"GTEx-Brain_Cortex",
				"GTEx-Brain_Frontal_Cortex_BA9",
				"GTEx-Brain_Hippocampus",
				"GTEx-Brain_Hypothalamus",
				"GTEx-Brain_Nucleus_accumbens_basal_ganglia",
				"GTEx-Brain_Putamen_basal_ganglia",
				"GTEx-Breast_Mammary_Tissue",
				"GTEx-Cells_EBV-transformed_lymphocytes",
				"GTEx-Cells_Transformed_fibroblasts",
				"GTEx-Colon_Sigmoid",
				"GTEx-Colon_Transverse",
				"GTEx-Esophagus_Gastroesophageal_Junction",
				"GTEx-Esophagus_Mucosa",
				"GTEx-Esophagus_Muscularis",
				"GTEx-Heart_Atrial_Appendage",
				"GTEx-Heart_Left_Ventricle",
				"GTEx-Liver",
				"GTEx-Lung",
				"GTEx-Muscle_Skeletal",
				"GTEx-Nerve_Tibial",
				"GTEx-Ovary",
				"GTEx-Pancreas",
				"GTEx-Pituitary",
				"GTEx-Prostate",
				"GTEx-Skin_Not_Sun_Exposed_Suprapubic",
				"GTEx-Skin_Sun_Exposed_Lower_leg",
				"GTEx-Small_Intestine_Terminal_Ileum",
				"GTEx-Spleen",
				"GTEx-Stomach",
				"GTEx-Testis",
				"GTEx-Thyroid",
				"GTEx-Uterus",
				"GTEx-Vagina",
				"GTEx-Whole_Blood"
		};
		
		
		QTLOverlap o = new QTLOverlap();
//		String gene = "ENSG00000084093";
//		int[] snppos = new int[]{57764324,
//				57823476
//		};
//		String[] snpname = new String[]{
//				"rs13353552",
//				"rs17081935"
//		};
//		Feature region = new Feature(Chromosome.FOUR, 0, Integer.MAX_VALUE);
//		try {
//			o.findGene(region, eqtlfiles, eqtlfilenames, gene, snppos, snpname);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.exit(-1);
		String ensembl = "D:\\Sync\\SyncThing\\Data\\Ref\\Ensembl\\GrCH37-b86-Structures.txt.gz";
		double ldthresh = 0.8;
		int ciswindow = 1000000;
		String variantfile = disk + "\\Sync\\Dropbox\\FineMap\\2018-01-Rebuttal\\tables\\listofsnpswithposterior0.2.txt";
		String tabix = disk + "\\Sync\\SyncThing\\Data\\Ref\\1kg\\ALL.chrCHR.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz";
		String samplefile = disk + "\\Sync\\SyncThing\\Data\\Ref\\1kg-europeanpopulations.txt.gz";
		String output = disk + "\\Sync\\SyncThing\\Postdoc\\2016-03-RAT1D-Finemapping\\Data\\2017-08-16-Reimpute4Filtered\\qtloverlap\\2018-04-22-output-topassoc.txt";
		boolean onlytestagainstopeqtl = true;
		try {
			o.run(variantfile, eqtlfiles, eqtlfilenames, tabix, samplefile, ciswindow, ldthresh, ensembl, onlytestagainstopeqtl, output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void findGene(Feature region, String[] qfiles, String[] eqtlfilenames, String gene, int[] snppos, boolean onlytestagainstopeqtl, String[] snpname) throws IOException {
		ArrayList<Feature> regions = new ArrayList<>();
		regions.add(region);
		EQTL[][][] eqtls = loadEQTLs(qfiles, regions, onlytestagainstopeqtl); // [filenames][regions][eqtls]
		
		System.out.println("Tissue\ttopeQTLSNP\ttopeQTLSNPP\tQuerySNPs");
		for (int f = 0; f < eqtlfilenames.length; f++) {

//			String[] qfile = new String[]{qfiles[f]};
			
			EQTL[] matches = new EQTL[snppos.length];
			EQTL[] es = eqtls[f][0];
			double maxp = 1;
			EQTL maxe = null;
			for (EQTL e : es) {
				if (e.getGenename().contains(gene)) {
					if (e.getPval() < maxp) {
						maxp = e.getPval();
						maxe = e;
					}
					for (int s = 0; s < snppos.length; s++) {
						if (e.getSnp().getStart() == snppos[s]) {
							matches[s] = e;
						}
					}
				}
			}
			
			if (maxe == null) {
				System.out.println(eqtlfilenames[f] + "\tNotSignificant");
			} else {
				
				String ln = eqtlfilenames[f] + "\t" + maxe.getSnp() + "\t" + maxe.getPval();
				for (int m = 0; m < matches.length; m++) {
					if (matches[m] == null) {
						ln += "\t" + snpname[m] + " not found";
					} else {
						ln += "\t" + snpname[m] + ": " + matches[m].getPval();
					}
				}
				
				System.out.println(ln);
				
			}
			
		}
		
		
	}
	
	
	public void run(String variantfile,
					String[] qfiles,
					String[] qfilenames,
					String tabixprefix,
					String samplefile,
					int ciswindow,
					double ldthresh,
					String ens,
					boolean onlytestagainstopeqtl,
					String output) throws IOException {
		
		// read the snps
		TextFile t2 = new TextFile(variantfile, TextFile.R);
		String ln = t2.readLine();
		
		ArrayList<SNPFeature> snps = new ArrayList<SNPFeature>();
		
		EnsemblStructures eg = new EnsemblStructures(ens);
		Collection<Gene> genes = eg.getGenes();
		HashMap<String, Gene> strToGene = new HashMap<>();
		for (Gene g : genes) {
			strToGene.put(g.getName(), g);
		}
		
		while (ln != null) {
			SNPFeature f = SNPFeature.parseSNPFeature(ln);
			
			f.setStart(f.getStart());
			f.setStop(f.getStop());
			snps.add(f);
			
			
			ln = t2.readLine();
		}
		t2.close();
		
		Collections.sort(snps, new FeatureComparator(true));
		ArrayList<Feature> regions = new ArrayList<Feature>();
		for (Feature f : snps) {
			Feature f2 = new Feature(f.getChromosome(), f.getStart() - ciswindow, f.getStop() + ciswindow);
			regions.add(f2);
		}
		
		// read qtls
		EQTL[][][] eqtls = loadEQTLs(qfiles, regions, onlytestagainstopeqtl);
		DetermineLD ldcal = new DetermineLD();
		
		
		String[][] outputlns = new String[qfiles.length][snps.size()];
		
		// write header;
		ExecutorService ex = Executors.newFixedThreadPool(6);
		boolean[] done = new boolean[snps.size()];
		for (int s = 0; s < snps.size(); s++) {
			ex.submit(new QTLOverLapTask(done, regions, s, snps, tabixprefix, samplefile, outputlns, eqtls, qfiles, ldthresh, strToGene));
		}
		
		while (!alldone(done)) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ex.shutdown();
		
		
		// remove the snps without any qtls
		boolean[] includecol = new boolean[snps.size()];
		for (int s = 0; s < snps.size(); s++) {
			int nrna = 0;
			for (int q = 0; q < qfiles.length; q++) {
				if (outputlns[q][s].equals("-")) {
					nrna++;
				}
			}
			if (nrna < qfiles.length) {
				includecol[s] = true;
			}
		}
		
		TextFile out = new TextFile(output, TextFile.W);
		String header = "QTL File";
		for (int s = 0; s < snps.size(); s++) {
			if (includecol[s]) {
				header += "\t" + snps.get(s).toString();
			}
		}
		out.writeln(header);
		
		for (int q = 0; q < outputlns.length; q++) {
			String lnout = qfilenames[q];
			int nrnonna = 0;
			for (int s = 0; s < snps.size(); s++) {
				if (includecol[s]) {
					lnout += "\t" + outputlns[q][s];
					if (!outputlns[q][s].equals("-")) {
						nrnonna++;
					}
				}
			}
			if (nrnonna > 0) {
				out.writeln(lnout);
			}
		}
		
		out.close();
		
	}
	
	private boolean alldone(boolean[] d) {
		boolean alldone = true;
		int done = 0;
		for (boolean b : d) {
			
			if (!b) {
				alldone = false;
			} else {
				done++;
			}
		}
		System.out.println(done + " tasks done.");
		return alldone;
	}
	
	private class QTLOverLapTask implements Runnable {
		
		private final boolean[] done;
		ArrayList<Feature> regions;
		int s;
		ArrayList<SNPFeature> snps;
		String tabixprefix;
		String samplefile;
		String[][] outputlns;
		EQTL[][][] eqtls;
		String[] qfiles;
		double ldthresh;
		HashMap<String, Gene> strToGene;
		
		public QTLOverLapTask(boolean[] done, ArrayList<Feature> regions,
							  int s,
							  ArrayList<SNPFeature> snps,
							  String tabixprefix,
							  String samplefile,
							  String[][] outputlns,
							  EQTL[][][] eqtls,
							  String[] qfiles,
							  double ldthresh,
							  HashMap<String, Gene> strToGene) {
			this.done = done;
			this.regions = regions;
			this.s = s;
			this.snps = snps;
			this.tabixprefix = tabixprefix;
			this.samplefile = samplefile;
			this.outputlns = outputlns;
			this.eqtls = eqtls;
			this.qfiles = qfiles;
			this.ldthresh = ldthresh;
			this.strToGene = strToGene;
		}
		
		@Override
		public void run() {
			try {
				Feature region = regions.get(s);
				String tabixFile = tabixprefix.replaceAll("CHR", "" + snps.get(s).getChromosome().getNumber());
				VCFTabix t = new VCFTabix(tabixFile);
				boolean[] filter = null;
				if (samplefile != null) {
					filter = t.getSampleFilter(samplefile);
				}
				ArrayList<VCFVariant> allvariants = t.getAllVariants(region, filter);
				VCFVariant v1 = getVariant(snps.get(s), allvariants);
				int nrwitheqtl = 0;
				for (int q = 0; q < qfiles.length; q++) {
					EQTL[] regioneqtls = eqtls[q][s];
					
					if (regioneqtls == null || regioneqtls.length == 0) {
						outputlns[q][s] = "-";
					} else {
						if (v1 == null) {
							// don't use LD..
							System.out.println("NoPrimaryVar\tFile: " + q + "\tSNP: " + s + "\t" + regioneqtls.length);
							ArrayList<EQTL> overlap = new ArrayList<>();
							for (EQTL e : regioneqtls) {
								if (e.getSnp().toString().equals(snps.get(s).toString())) {
									overlap.add(e);
								}
							}
							ArrayList<String> estr = new ArrayList<>();
							if (!overlap.isEmpty()) {
								nrwitheqtl++;
							}
							for (EQTL e : overlap) {
								String estrln = e.getGenename() + "_" + e.getSnp().toString();
								estr.add(estrln);
								
							}
							outputlns[q][s] = Strings.concat(estr, Strings.semicolon);
						} else {
							// check whether SNP is present
							System.out.println("PrimaryVarPresent\tFile: " + q + "\tSNP: " + s + "\t" + regioneqtls.length);
							boolean snpispresent = false;
							ArrayList<EQTL> overlap = new ArrayList<>();
							for (EQTL e : regioneqtls) {
								if (e.getSnp().toString().equals(snps.get(s).toString())) {
									overlap.add(e);
									snpispresent = true;
								}
							}
							if (snpispresent) {
								ArrayList<String> estr = new ArrayList<>();
								for (EQTL e : overlap) {
									String estrln = e.getGenename() + "_" + e.getSnp().toString();
									estr.add(estrln);
								}
								outputlns[q][s] = Strings.concat(estr, Strings.semicolon);
								nrwitheqtl++;
							} else {
								// use LD
								System.out.println("PrimaryVarPresentUsingLD\tFile: " + q + "\tSNP: " + s + "\t" + regioneqtls.length);
								boolean haseqtl = false;
								HashMap<String, EQTL> gToP = new HashMap<String, EQTL>();
								DetermineLD ldcal = new DetermineLD();
								for (EQTL e : regioneqtls) {
									VCFVariant v2 = getVariant(e.getSnp(), allvariants);
									if (v2 != null) {
										Pair<Double, Double> ld = ldcal.getLD(v1, v2);
										if (ld != null && ld.getRight() > ldthresh) {
											// variants are in LD
											haseqtl = true;
											
											// store most significant effect per gene only
											String g = e.getGenename();
											double p = e.getPval();
											EQTL d = gToP.get(g);
											
											// consider it a hit if there's LD or if the top
											if (d == null || d.getPval() > p) {
												if (d == null) {
													gToP.put(g, e);
												}
												
											}
										}
									}
								}
								
								if (haseqtl) {
									ArrayList<String> estr = new ArrayList<>();
									for (String k : gToP.keySet()) {
										EQTL e = gToP.get(k);
										
										String genename = e.getGenename();
										if (genename.startsWith("ENSG")) {
											String[] splits = Strings.dot.split(genename);
											Gene g = strToGene.get(splits[0]);
											if (g != null) {
												genename = g.getGeneSymbol();
											}
											
										}
										
										String estrln = genename + "_" + e.getSnp().toString();
										estr.add(estrln);
									}
									nrwitheqtl++;
									outputlns[q][s] = Strings.concat(estr, Strings.semicolon);
								} else {
									outputlns[q][s] = "-";
								}
							}
						}
					}
				}
			} catch (IOException e) {
				done[s] = true;
				e.printStackTrace();
			}
			done[s] = true;
		}
		
	}
	
	
	public VCFVariant getVariant(Feature snpFeature, ArrayList<VCFVariant> allvariants) {
		for (VCFVariant v : allvariants) {
			if (v.asFeature().overlaps(snpFeature)) {
				return v;
			}
		}
		return null;
		
	}
	
	private EQTL[][][] loadEQTLs(String[] eqtlfilenames, ArrayList<Feature> regions, boolean loadOnlyTopAssoc) throws IOException {
		EQTL[][][] output = new EQTL[eqtlfilenames.length][regions.size()][];
		
		
		for (int d = 0; d < eqtlfilenames.length; d++) {
			System.out.println("Parsing: " + eqtlfilenames[d]);
			
			
			ArrayList<EQTL> allEQTLs = new ArrayList<>();
			HashMap<String, EQTL> topEQTLs = new HashMap<>();
			
			String filename = eqtlfilenames[d];
			if (filename.endsWith("tab") || filename.endsWith("tab.gz")) {
				
				TextFile tf = new TextFile(eqtlfilenames[d], TextFile.R);
				// Chr3	11604119	rs1000010	ATG7	3.91E-9
				String[] elems = tf.readLineElems(TextFile.tab);
				while (elems != null) {
					
					Chromosome chr = Chromosome.parseChr(elems[0]);
					Integer pos = Integer.parseInt(elems[1]);
					String snpname = elems[2];
					String gene = elems[3];
					double pval = Double.parseDouble(elems[4]);
					SNPFeature feature = new SNPFeature(chr, pos, pos);
					feature.setName(snpname);
					
					EQTL eqtl = new EQTL();
					eqtl.setSnp(feature);
					eqtl.setGenename(gene);
					eqtl.setName(gene);
					eqtl.setPval(pval);
					
					if (eqtloverlap(eqtl, regions)) {
						if (loadOnlyTopAssoc) {
							EQTL top = topEQTLs.get(gene);
							if (top == null) {
								topEQTLs.put(gene, eqtl);
							} else {
								if (eqtl.getPval() < top.getPval()) {
									topEQTLs.put(gene, eqtl);
								}
							}
						} else {
							allEQTLs.add(eqtl);
						}
					}
					
					elems = tf.readLineElems(TextFile.tab);
				}
				tf.close();
				
				
			} else {
				
				// BIOS eQTL file format
				TextFile tf = new TextFile(eqtlfilenames[d], TextFile.R);
				String[] header = tf.readLineElems(TextFile.tab);
				
				int snpcol = -1;
				int pvalcol = -1;
				int chrcol = -1;
				int poscol = -1;
				int genecol = -1;
				int fdrcol = -1;
				
				for (int q = 0; q < header.length; q++) {
					String h = header[q];
					if (h.equals("PValue")) {
						pvalcol = q;
					}
					if (h.equals("SNPName")) {
						snpcol = q;
					}
					if (h.equals("SNPChr")) {
						chrcol = q;
					}
					if (h.equals("SNPChrPos")) {
						poscol = q;
					}
					if (h.equals("HGNCName")) {
						genecol = q;
					}
					if (h.equals("FDR") && fdrcol == -1) {
						fdrcol = q;
					}
					
				}


				/*
				PValue  SNPName SNPChr  SNPChrPos       ProbeName       ProbeChr        ProbeCenterChrPos       CisTrans        SNPType AlleleAssessed  DatasetsNrSamples       OverallZScore   IncludedDatasetsCorrelationCoefficient  HGNCName        FDR

PValue  SNPName SNPChr  SNPChrPos       ProbeName       ProbeChr        ProbeCenterChrPos       CisTrans        SNPType AlleleAssessed  OverallZScore   DatasetsWhereSNPProbePairIsAvailableAndPassesQC DatasetsZScores DatasetsNrSamples
       IncludedDatasetsMeanProbeExpression     IncludedDatasetsProbeExpressionVariance HGNCName        IncludedDatasetsCorrelationCoefficient  Meta-Beta (SE)  Beta (SE)       FoldChange      FDR     FDR

				 */
				
				String[] elems = tf.readLineElems(TextFile.tab);
				int ctr = 0;
				while (elems != null) {
					
					String snp = elems[snpcol];
					Double pval = Double.parseDouble(elems[pvalcol]);
					Chromosome chr = Chromosome.parseChr(elems[chrcol]);
					Integer pos = Integer.parseInt(elems[poscol]);
					SNPFeature feature = new SNPFeature(chr, pos, pos);
					feature.setName(snp);
					
					EQTL eqtl = new EQTL();
					eqtl.setSnp(feature);
					eqtl.setGenename(elems[genecol]);
					eqtl.setName(elems[genecol]);
					eqtl.setPval(pval);
					
					boolean sig = true;
					if (fdrcol != -1) {
						Double fdr = Double.parseDouble(elems[fdrcol]);
						if (fdr > 0.05) {
							sig = false;
						}
					}
					if (sig && eqtloverlap(eqtl, regions)) {
						if (eqtloverlap(eqtl, regions)) {
							String gene = elems[genecol];
							if (loadOnlyTopAssoc) {
								EQTL top = topEQTLs.get(gene);
								if (top == null) {
									topEQTLs.put(gene, eqtl);
								} else {
									if (eqtl.getPval() < top.getPval()) {
										topEQTLs.put(gene, eqtl);
									}
								}
							} else {
								allEQTLs.add(eqtl);
							}
						}
					}
					
					ctr++;
					if (ctr % 100000 == 0) {
						System.out.println(ctr + " lines parsed. " + allEQTLs.size() + " stored");
					}
					elems = tf.readLineElems(TextFile.tab);
				}
				tf.close();
			}
			
			if (loadOnlyTopAssoc) {
				allEQTLs = new ArrayList<>();
				Set<String> keys = topEQTLs.keySet();
				for (String key : keys) {
					allEQTLs.add(topEQTLs.get(key));
				}
			}
			
			System.out.println(allEQTLs.size() + " eqtls finally loaded from: " + filename);
			for (int r = 0; r < regions.size(); r++) {
				ArrayList<EQTL> overlapping = new ArrayList<>();
				Feature eqtlregion = regions.get(r);
				for (int e = 0; e < allEQTLs.size(); e++) {
					EQTL eqtl = allEQTLs.get(e);
					
					if (eqtl.getSnp().overlaps(eqtlregion)) {
						overlapping.add(eqtl);
					}
				}
				output[d][r] = overlapping.toArray(new EQTL[0]);
			}
			System.out.println("done loading eQTLs");
		}
		
		return output;
		
	}
	
	private boolean eqtloverlap(EQTL eqtl, ArrayList<Feature> eqtlregions) {
		for (int r = 0; r < eqtlregions.size(); r++) {
			Feature eqtlregion = eqtlregions.get(r);
			if (eqtl.getSnp().overlaps(eqtlregion)) {
				return true;
			}
		}
		return false;
		
	}
	
}
