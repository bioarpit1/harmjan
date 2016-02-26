package nl.harmjanwestra.broshifter;


import com.itextpdf.text.DocumentException;
import nl.harmjanwestra.broshifter.CLI.BroShifterOptions;
import nl.harmjanwestra.broshifter.CLI.MainOptions;
import nl.harmjanwestra.gwas.*;
import nl.harmjanwestra.gwas.CLI.*;

import java.io.IOException;

/**
 * Created by hwestra on 11/23/15.
 */
public class Main {


	public static void main(String[] args) {

		try {
			MainOptions options = new MainOptions(args);
			if (options.mode.equals(MainOptions.MODE.NA)) {
				System.out.println("Please specify a mode");
			} else if (options.mode.equals(MainOptions.MODE.BROSHIFTER)) {
				new BroShifter(new BroShifterOptions(args));
			} else if (options.mode.equals(MainOptions.MODE.BEDFILTER)) {
				new BedAssocFilter(new BedAssocFilterOptions(args));
			} else if (options.mode.equals(MainOptions.MODE.ANNOTATIONOVERLAPPLOT)) {
				new AnnotationOverlapPlot(new BroShifterOptions(args));
			} else if (options.mode.equals(MainOptions.MODE.POSTERIORPVAL)) {
				new PosteriorPvalues(new PosteriorPvalueOptions(args));
			} else if (options.mode.equals(MainOptions.MODE.PLOT)) {
				new AssociationPlotter(new AssociationPlotterOptions(args));
			} else if (options.mode.equals(MainOptions.MODE.MERGE)) {
				new AssociationResultMerger(new AssociationResultMergerOptions(args));
			} else if (options.mode.equals(MainOptions.MODE.ASSOC)) {
				new LRTest(new LRTestOptions(args));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}

	}


}
