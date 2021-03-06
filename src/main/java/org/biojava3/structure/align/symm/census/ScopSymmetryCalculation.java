/**
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on Sep 30, 2011
 * Created by Andreas Prlic
 *
 * @since 3.0.2
 */
package org.biojava3.structure.align.symm.census;

import java.util.concurrent.Callable;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.scop.ScopDescription;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava3.changeux.IdentifyAllSymmetries;
import org.biojava3.structure.align.symm.CeSymm;
import org.biojava3.structure.utils.SymmetryTools;

/**
 * @deprecated Replaced by {@link CensusJob}
 */
@Deprecated
public class ScopSymmetryCalculation implements Callable<CensusResult>{

	ScopDomain domain;

	AtomCache cache;

	ScopDescription scopDescription;

	Integer count;

	public AtomCache getCache() {
		return cache;
	}

	public void setCache(AtomCache cache) {
		this.cache = cache;
	}

	public ScopDomain getDomain() {
		return domain;
	}
	public ScopDescription getScopDescription() {
		return scopDescription;
	}

	public void setScopDescription(ScopDescription scopDescription) {
		this.scopDescription = scopDescription;
	}

	public void setDomain(ScopDomain domain) {
		this.domain = domain;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}



	public CensusResult call() throws Exception {

		try {
			
			String name1 = domain.getScopId();
			String name2 = domain.getScopId();

			Atom[] ca1 = cache.getAtoms(name1);
			
			if ( ca1.length < 20) {
				System.out.println(" ... ignoring " + name1);
				return null;
			}
			
			Atom[] ca2 = cache.getAtoms(name2);

			boolean isSymmetric = false;

			StructureAlignment ceSymm = ScanSCOPForSymmetry.getCeSymm();
			AFPChain afpChain = ceSymm.align(ca1,ca2);

			double angle = -1;

			if ( afpChain == null) { 
				return null;
			}
			angle = SymmetryTools.getAngle(afpChain,ca1,ca2);

			if ( IdentifyAllSymmetries.isSignificant(afpChain)) {

				if ( angle > 20) {

					isSymmetric = true;
				}
			}

			int order = CeSymm.getSymmetryOrder(afpChain);


			int[]optLen = afpChain.getOptLen();
			int[][][] optAln = afpChain.getOptAln();
			int p1 = optAln[0][0][0];
			int lenBlock1 = optLen[0] - 1;
			int p2 = optAln[0][0][lenBlock1];
			
			Atom a1 = ca1[p1];
			Atom a2 = ca1[p2];
			
			String chainId  = a1.getGroup().getChain().getChainID();
			Group g1 = a1.getGroup();
			Group g2 = a2.getGroup();
			String protodomain = domain.getPdbId();
			protodomain += "." + chainId + "_";
			protodomain+= g1.getResidueNumber().toString()  + "-" + g2.getResidueNumber().toString();
			
			//System.out.println(isSymmetric + " : " + name + " " +  domain + " : "  );
			
			//StringBuffer str = printTabbedResult(afpChain, isSymmetric, superfamily,name1, count);
			//StringBuffer str = printHTMLResult(afpChain, isSymmetric, superfamily,name1, count, angle);
			
			CensusResult result = convertResult(afpChain, isSymmetric, scopDescription, name1, count, angle, order, protodomain, domain);
			
			System.out.println(result);
			return result;
		} catch (Exception e){
			e.printStackTrace();
			System.err.println("ERROR processing " + domain + " " + e.getMessage());
			
			
		}
		System.err.println("returning null");
		return null;

	}

	private CensusResult convertResult(AFPChain afpChain, boolean isSymmetric, ScopDescription superfamily, 
			String name, int count, double angle , int order, String protodomain, ScopDomain domain){

		String description  = superfamily.getDescription();
		Character scopClass = superfamily.getClassificationId().charAt(0);

		CensusResult r = new CensusResult();

		r.setRank(count);
		r.setIsSignificant(isSymmetric);
		r.setName(name);
		r.setClassificationId(superfamily.getClassificationId());
		r.setzScore(afpChain.getProbability());
		r.setRmsd(afpChain.getTotalRmsdOpt());
		r.setTmScore(afpChain.getTMScore());
		r.setAlignScore(afpChain.getAlignScore());
		r.setIdentity((float)afpChain.getIdentity());
		r.setSimilarity((float)afpChain.getSimilarity());
		r.setLength1(afpChain.getCa1Length());
		r.setAligLength(afpChain.getOptLength());
		r.setAngle((float)angle);
		r.setDescription(description);
		r.setScopClass(scopClass);
		r.setOrder(order);
		r.setProtoDomain(protodomain);
		r.setSunid(domain.getSunid());
		
		return r;
	}


}
