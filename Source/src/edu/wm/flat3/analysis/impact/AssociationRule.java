package edu.wm.flat3.analysis.impact;

import java.io.Serializable;

import edu.wm.flat3.analysis.FLATTTMember;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class AssociationRule implements Serializable{
	private static final long serialVersionUID = 7658471007671383291L;
	private FLATTTMember lhs;
	private FLATTTMember rhs;
	private float confidenceValue;
	private int supportValue;
	public AssociationRule(FLATTTMember lhs, FLATTTMember rhs,
			float confidenceValue, int supportValue) {
		super();
		this.lhs = lhs;
		this.rhs = rhs;
		this.confidenceValue = confidenceValue;
		this.supportValue = supportValue;
	}
	public FLATTTMember getLhs() {
		return lhs;
	}
	public FLATTTMember getRhs() {
		return rhs;
	}
	public float getConfidenceValue() {
		return confidenceValue;
	}
	public int getSupportValue() {
		return supportValue;
	}
	
	public String toString() {
		return "{"+lhs.getFullName()+"} => {"+rhs.getFullName()+"} " + confidenceValue + " " + supportValue;
	}
	
}
