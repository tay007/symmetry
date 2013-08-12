/**
 * 
 */
package org.biojava3.structure.quaternary.jmolScript;

import org.biojava3.structure.quaternary.core.RotationAxisAligner;
import org.biojava3.structure.quaternary.geometry.Octahedron;


/**
 * @author Peter
 *
 */
public class JmolSymmetryScriptGeneratorO extends JmolSymmetryScriptGeneratorPointGroup {

	public JmolSymmetryScriptGeneratorO(RotationAxisAligner axisTransformation, String name) {
		super(axisTransformation, name);
		Octahedron o = new Octahedron();
		double radius = Math.max(axisTransformation.getDimension().z, axisTransformation.getXYRadius());
		o.setMidRadius(radius);
		setPolyhedron(o);
	}
	
	public int getZoom() {
		// find maximum extension of structure
		double maxExtension = getMaxExtension();
		// find maximum extension of polyhedron
		double polyhedronExtension = getPolyhedron().getCirumscribedRadius();
		
		int zoom = Math.round((float)(maxExtension/polyhedronExtension * 110));
		if (zoom > 100) {
			zoom = 100;
		}
		return zoom;
	}
	
}
