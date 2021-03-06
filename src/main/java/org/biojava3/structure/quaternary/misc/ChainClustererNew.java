package org.biojava3.structure.quaternary.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.vecmath.Point3d;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Structure;
import org.biojava3.structure.quaternary.core.ProteinChainExtractor;
import org.biojava3.structure.quaternary.core.QuatSymmetryParameters;
import org.biojava3.structure.quaternary.core.SequenceAlignmentCluster;
import org.biojava3.structure.quaternary.core.UniqueSequenceList;

public class ChainClustererNew  {
	private List<Structure> structures = new ArrayList<Structure>();
	private QuatSymmetryParameters parameters = null;
	
	private List<Atom[]> caUnaligned = new ArrayList<Atom[]>();
	private List<String> chainIds = new ArrayList<String>();
	private List<Integer> modelNumbers = new ArrayList<Integer>();
	private List<Integer> structureIds = new ArrayList<Integer>();
	private List<String> sequences = new ArrayList<String>();
	private List<Atom[]> caAligned = new ArrayList<Atom[]>();
	private List<Point3d[]> caCoords = new ArrayList<Point3d[]>();

	List<SequenceAlignmentCluster> seqClusters = new ArrayList<SequenceAlignmentCluster>();
	
	private boolean modified = true;
	private boolean pseudoSymmetric = false;

	public ChainClustererNew(Structure structure, QuatSymmetryParameters parameters) {
		this.structures.add(structure);
		this.parameters = parameters;
		modified = true;
	}
	
	public void addStructure(Structure structure) {
		this.structures.add(structure);
		modified = true;
	}
	
	public List<Point3d[]> getCalphaCoordinates() {
        run();
		return caCoords;
	}
	
	public List<Atom[]> getCalphaTraces() {
		run();
		return caAligned;
	}
	
	public boolean isHomomeric() {
		run();
		return seqClusters.size() == 1;
	}
	
	public boolean isPseudoSymmetric() {
		run();
		return pseudoSymmetric;
	}

	public int getMultiplicity() {
		run();
		return seqClusters.get(seqClusters.size()-1).getSequenceCount();
	}
	
	public List<String> getChainIdsInClusterOrder() {
		run();
		List<String> chainIdList = new ArrayList<String>();

		for (int i = 0; i < seqClusters.size(); i++) {
	        SequenceAlignmentCluster cluster = seqClusters.get(i);
	        for (String chainId: cluster.getChainIds()) {
	        	chainIdList.add(chainId);
	        }
		}
		return chainIdList;
	}
	
	
	public List<Integer> getModelNumbersInClusterOrder() {
		run();
		List<Integer> modNumbers = new ArrayList<Integer>();

		for (int i = 0; i < seqClusters.size(); i++) {
	        SequenceAlignmentCluster cluster = seqClusters.get(i);
	        for (Integer number: cluster.getModelNumbers()) {
	        	modNumbers.add(number);
	        }
		}
		return modNumbers;
	}
	
	public List<Integer> getStructureIdsInClusterOrder() {
		run();
		List<Integer> structIds = new ArrayList<Integer>();

		for (int i = 0; i < seqClusters.size(); i++) {
	        SequenceAlignmentCluster cluster = seqClusters.get(i);
	        for (Integer number: cluster.getStructureIds()) {
	        	structIds.add(number);
	        }
		}
		return structIds;
	}
	
	public List<String> getChainIds() {
		return chainIds;
	}
	
	public List<Integer> getModelNumbers() {
		return modelNumbers;
	}
	
	public List<Integer> getStructureIds() {
		return structureIds;
	}
	
	public String getCompositionFormula() {
		run();
		StringBuilder formula = new StringBuilder();
		String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

		for (int i = 0; i < seqClusters.size(); i++) {
			String c = "?";
			if (i < alpha.length()) {
				c = alpha.substring(i, i+1);
			}
			formula.append(c);
			int multiplier = seqClusters.get(i).getSequenceCount();
			if (multiplier > 1) {
				formula.append(multiplier);
			}
		}
		return formula.toString();
	}

	public List<Integer> getFolds() {
		run();
		List<Integer> denominators = new ArrayList<Integer>();
        Set<Integer> nominators = new TreeSet<Integer>();
		int nChains = caCoords.size();
		
		for (int id = 0; id < seqClusters.size(); id++) {
			int seqCount = seqClusters.get(id).getSequenceCount();
			nominators.add(seqCount);
		}
		
		// find common denominators
		for (int d = 1; d <= nChains; d++) {
			int count = 0;
			for (Iterator<Integer> iter = nominators.iterator(); iter.hasNext();) {
				if (iter.next() % d == 0) {
					count++;
				}
			}
			if (count == nominators.size()) {
				denominators.add(d);
			}
		}
		
		return denominators;
	}
	
	public List<Integer> getSequenceClusterIds() {
		run();
		List<Integer> list = new ArrayList<Integer>();
		
		for (int id = 0; id < seqClusters.size(); id++) {
			int seqCount = seqClusters.get(id).getSequenceCount();
			for (int i = 0; i < seqCount; i++) {
				list.add(id);
			}
		}
		return list;
	}
	
	public List<SequenceAlignmentCluster> getSequenceClusters() {
		return seqClusters;
	}
	
	private void run() {
		if (modified) {
			extractProteinChains();
			if (caUnaligned.size() == 0) {
				modified = false;
				return;
			}
			calcSequenceClusters();
			calcAlignedSequences();
			createCalphaTraces();
			modified = false;
		}
	}
	
	private void extractProteinChains() {
		for (int i = 0; i < structures.size(); i++) {
			Structure structure = structures.get(i);
			ProteinChainExtractor extractor = new ProteinChainExtractor(structure,  parameters);
			caUnaligned.addAll(extractor.getCalphaTraces());
			chainIds.addAll(extractor.getChainIds());
			sequences.addAll(extractor.getSequences());
			modelNumbers.addAll(extractor.getModelNumbers());
			for (int j = 0; j < extractor.getChainIds().size(); j++) {
				structureIds.add(i);
			}
		}
	}
	
	private void calcSequenceClusters() {
		boolean[] processed = new boolean[caUnaligned.size()];
		Arrays.fill(processed, false);
	
		for (int i = 0; i < caUnaligned.size(); i++) {
			if (processed[i]) {
				continue;
			}
			processed[i] = true;
			// create new sequence cluster
            UniqueSequenceList seqList = new UniqueSequenceList(caUnaligned.get(i), chainIds.get(i), modelNumbers.get(i), structureIds.get(i), sequences.get(i));
            SequenceAlignmentCluster seqCluster = new SequenceAlignmentCluster(parameters);
            seqCluster.addUniqueSequenceList(seqList);	
            seqClusters.add(seqCluster);
			
            for (int j = i + 1; j < caUnaligned.size(); j++) {
            	if (processed[j]) {
            		continue;
            	}
            	for (SequenceAlignmentCluster c: seqClusters) {
            		// add to existing sequence cluster if there is a match
//            		if (c.isSeqResSequenceMatch(sequences.get(j)) || parameters.isStructuralAlignmentOnly()) {
//            			if (c.addChain(caUnaligned.get(j), chainIds.get(j), modelNumbers.get(j), structureIds.get(j), sequences.get(j))) {
//            				processed[j] = true;
//            				break;
//            			}
//            		}
            	} 
            }

		}
//		sortSequenceClustersBySize(seqClusters);
	}
	
	private void calcAlignedSequences() {
		caAligned = new ArrayList<Atom[]>();
		for (SequenceAlignmentCluster cluster: seqClusters) {
			caAligned.addAll(cluster.getAlignedCalphaAtoms());	
		}
	}
	
	private void createCalphaTraces() {
		for (Atom[] atoms: caAligned) {
			Point3d[] trace = new Point3d[atoms.length];
			for (int j = 0; j < atoms.length; j++) {
				trace[j] = new Point3d(atoms[j].getCoords());
			}
			caCoords.add(trace);
		}
	}

	public String toString() {
		run();
		StringBuilder builder = new StringBuilder();
		builder.append("Sequence alignment clusters: " + seqClusters.size());
		builder.append("\n");
		for (SequenceAlignmentCluster s: seqClusters) {
			builder.append("# seq: ");
			builder.append(s.getSequenceCount());
			builder.append(" alignment length: ");
			builder.append(s.getSequenceAlignmentLength());
			builder.append("\n");
		}
		return builder.toString();
	}
	
	public void sortSequenceClustersBySize(List<SequenceAlignmentCluster> clusters) {
		Collections.sort(clusters, new Comparator<SequenceAlignmentCluster>() {
			public int compare(SequenceAlignmentCluster c1, SequenceAlignmentCluster c2) {
				int sign = Math.round(Math.signum(c2.getSequenceCount() - c1.getSequenceCount()));
				if (sign != 0) {
					return sign;
				}
				return Math.round(Math.signum(c2.getSequenceAlignmentLength() - c1.getSequenceAlignmentLength()));
			}
		});
	}
}
