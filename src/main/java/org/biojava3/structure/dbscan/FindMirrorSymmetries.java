package org.biojava3.structure.dbscan;

import java.util.SortedSet;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.StructureException;

import org.biojava.bio.structure.align.ce.CECalculator;
import org.biojava.bio.structure.align.ce.CeCPMain;
import org.biojava.bio.structure.align.ce.CeMain;
import org.biojava.bio.structure.align.ce.CeParameters;

import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.align.util.AFPChainScorer;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.jama.Matrix;
import org.biojava3.structure.utils.SimpleLog;
import org.biojava3.structure.utils.SymmetryTools;
import org.rcsb.fatcat.server.PdbChainKey;

public class FindMirrorSymmetries {
	
	
	

	
	
	public static void main(String[] args){
		SortedSet<PdbChainKey> reps = GetRepresentatives.getRepresentatives();
		AtomCache cache = new AtomCache();
		
		String filename = cache.getPath()+System.getProperty("file.seperator")
				+"findMirrorSymmetries.log";
		SimpleLog.setLogFilename(filename);
		
		int total = 0;
		int symmetric = 0;
		for ( PdbChainKey r : reps){
			try {
				String name = r.toName();
				Atom[] ca1 = cache.getAtoms(name);
				Atom[] ca2 = cache.getAtoms(name);

				Atom[] ca2M = SymmetryTools.mirrorCoordinates(ca2);
				ca2M = SymmetryTools.duplicateMirrorCA2(ca2M);
				
				AFPChain afp = FindMirrorSymmetries.align(ca1,ca2M,name, false);
				afp.setAlgorithmName(CeMain.algorithmName);

				//boolean isSignificant =  afp.isSignificantResult();
				boolean isSignificant = false;
				if ( afp.getTMScore() > 0.3 && afp.isSignificantResult()) {
					isSignificant = true;
					//StructureAlignmentDisplay.display(afp, ca1, ca2M);
				}
				total++;
				if ( isSignificant)
					symmetric++;
				String log = name + "\t" + String.format("%.2f", afp.getProbability()) + String.format(" %.2f", afp.getTMScore());
				log += " " + symmetric + "/" + total + " (" + (symmetric/(float)total*100f)+"%)";
				
								
				SimpleLog.write(log);
				
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	
	

	public static AFPChain align(Atom[] ca1, Atom[] ca2, String name, boolean showMatrix) throws StructureException {
		
		
		//Atom[] ca2clone = SymmetryTools.cloneAtoms(ca2);
		Atom[] ca2clone = CeCPMain.prepareAtomsForAlign(ca2);
		CeParameters params = new CeParameters();
		
		CECalculator calculator = new CECalculator(params);

		//Build alignment ca1 to ca2-ca2
		AFPChain afpChain = new AFPChain();
		afpChain.setName1(name);
		afpChain.setName2(name);
		afpChain = calculator.extractFragments(afpChain, ca1, ca2clone);

		Matrix origM = new Matrix( calculator.getMatMatrix());
		// symmetry hack, disable main diagonale

//		for ( int i = 0 ; i< rows; i++){
//			for ( int j = 0 ; j < cols ; j++){
//				int diff = Math.abs(i-j);
//
//				if ( diff < 15 ){
//					origM.set(i,j, 99);
//				}
//				int diff2 = Math.abs(i-(j-ca2.length/2));
//				if ( diff2 < 15 ){
//					origM.set(i,j, 99);
//				}
//			}
//		}
		
		
		if ( showMatrix)
			SymmetryTools.showMatrix(origM, "mirror matrix");
		
		//showMatrix(origM, "original CE matrix");
		Matrix clone =(Matrix) origM.clone();
		calculator.setMatMatrix(clone.getArray());
		//calculator.setMatMatrix(diffDistMax.getArray());
		calculator.traceFragmentMatrix( afpChain,ca1, ca2clone);
		calculator.nextStep( afpChain,ca1, ca2clone);

		//afpChain.setAlgorithmName("SpeedCE");
		//afpChain.setVersion("0.0000001");
		afpChain.setDistanceMatrix((Matrix)origM.clone());

		double tmScore = AFPChainScorer.getTMScore(afpChain, ca1, ca2clone);
		afpChain.setTMScore(tmScore);
		
		
		CeCPMain.postProcessAlignment(afpChain, ca1, ca2clone, calculator);
		return afpChain;


	}
}