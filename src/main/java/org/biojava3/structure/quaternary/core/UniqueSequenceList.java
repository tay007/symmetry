package org.biojava3.structure.quaternary.core;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Group;

public class UniqueSequenceList {
	private String sequenceString = "";
	private String seqResSequence = "";
    private List<Integer> alignment1 = null;
    private List<Integer> alignment2 = null;
    private Atom[] caAtoms = null;
    private String chainId = null;
    private Integer modelNumber = null;
    private Integer structureId = null;
    
    public UniqueSequenceList(Atom[] cAlphaAtoms, String chainId, int modelNumber, int structureId, String seqResSequence) {
    	this.caAtoms = cAlphaAtoms;
    	this.chainId = chainId;
    	this.modelNumber = modelNumber;
    	this.structureId = structureId;
    	this.seqResSequence = seqResSequence;
    	this.sequenceString =  getSequenceString(cAlphaAtoms);
    	this.alignment1 = new ArrayList<Integer>(cAlphaAtoms.length);
    	this.alignment2 = new ArrayList<Integer>(cAlphaAtoms.length);
    	for (int i = 0; i < cAlphaAtoms.length; i++) {
    		this.alignment1.add(i);
    		this.alignment2.add(i);
    	}
    }
    
    /**
     * Return true is the sequence and residues numbers of the passed in array of
     * atoms matches those of this unique sequence list
     * 
     * @param caAlphaAtoms
     * @return
     */
    public boolean isMatch(Atom[] caAlphaAtoms) {
    	return sequenceString.equals(getSequenceString(caAlphaAtoms));
    }
    
    public String getChainId() {
    	return chainId;
    }
    
    public int getModelNumber() {
    	return modelNumber;
    }
     
    public int getStructureId() {
    	return structureId;
    }
    
    public Atom[] getCalphaAtoms() {
    	return caAtoms;
    }
	
	public String getSeqResSequence() {
		return seqResSequence;
	}
	
	/**
	 * @param sequenceString the sequenceString to set
	 */
	public void setSequenceString(String sequenceString) {
		this.sequenceString = sequenceString;
	}
	/**
	 * @return the alignment1
	 */
	public List<Integer> getAlignment1() {
		return alignment1;
	}
	/**
	 * @param alignment1 the alignment1 to set
	 */
	public void setAlignment1(List<Integer> alignment1) {
		this.alignment1 = alignment1;
	}
	/**
	 * @return the alignment2
	 */
	public List<Integer> getAlignment2() {
		return alignment2;
	}
	/**
	 * @param alignment2 the alignment2 to set
	 */
	public void setAlignment2(List<Integer> alignment2) {
		this.alignment2 = alignment2;
	}
	
	public static String getSequenceString(Atom[] caAlphaAtoms) {
		StringBuilder builder = new StringBuilder();

		for (Atom a:  caAlphaAtoms) {
			Group g = a.getGroup();
			// TODO is the check for UNK required? UNK should have been filtered already in ChainClusterer?
			if (! g.getPDBName().equals("UNK")) {
				builder.append(g.getResidueNumber());
				builder.append(g.getPDBName());
			}
		}
		
//		System.out.println("getSequenceString: " + builder.toString());
		return builder.toString();
	}
     
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("#: ");
		builder.append(caAtoms.length);
		builder.append(" seq: ");
		builder.append(sequenceString);
		builder.append("\n");
		builder.append(alignment1);
		builder.append("\n");
		builder.append(alignment2);
		return builder.toString();
	}
	
}
