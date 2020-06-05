package com.vanderhighway.trbac.verifier;

import com.google.common.base.Objects;
import com.vanderhighway.trbac.model.trbac.model.Demarcation;
import com.vanderhighway.trbac.model.trbac.model.Permission;
import com.vanderhighway.trbac.model.trbac.model.TRBACPackage;
import com.vanderhighway.trbac.model.trbac.model.Role;
import com.vanderhighway.trbac.model.trbac.model.User;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.collections.impl.lazy.parallel.Batch;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.IModelManipulations;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.SimpleModelManipulations;
import org.eclipse.viatra.transformation.runtime.emf.rules.batch.BatchTransformationRule;
import org.eclipse.viatra.transformation.runtime.emf.rules.batch.BatchTransformationRuleFactory;
import org.eclipse.viatra.transformation.runtime.emf.transformation.batch.BatchTransformation;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Extension;

@SuppressWarnings("all")
public class PolicyModifier {

	/**
	 * Transformation-related extensions
	 */

	@Extension
	public IModelManipulations manipulation;

	@Extension
	private BatchTransformationRuleFactory batchFactory = new BatchTransformationRuleFactory();

	@Extension
	private BatchTransformation transformation;

	@Extension
	private TRBACPackage ePackage = TRBACPackage.eINSTANCE;

	private AdvancedViatraQueryEngine engine;

	public PolicyModifier(final AdvancedViatraQueryEngine engine) {
		this.engine = engine;
		this.manipulation = new SimpleModelManipulations(this.engine);
		this.transformation = BatchTransformation.forEngine(this.engine).build();
	}
	
	

	// ---------- Add / Remove Authorization Model Entities ----------

	public BatchTransformationRule addRole(String name) {
		return createNamedEntity(ePackage.getRole(), ePackage.getPolicy_Roles(), ePackage.getRole_Name(), name);
	}

	public BatchTransformationRule addScheduleRange(EObject day, String name, int startTime, int endTime){
		final Consumer<Policy.Match> function = (Policy.Match it) -> {
			EObject daySchedule = null;
			try {
				daySchedule = this.manipulation.createChild(day, ePackage.getDaySchedule_Scheduleranges(), ePackage.getScheduleRange());
				this.manipulation.set(daySchedule, ePackage.getScheduleRange_Name(), name);
				this.manipulation.set(daySchedule, ePackage.getScheduleRange_Starttime(), startTime);
				this.manipulation.set(daySchedule, ePackage.getScheduleRange_Endtime(), endTime);
			} catch (ModelManipulationException e) {
				e.printStackTrace();
			}
		};
		final BatchTransformationRule<Policy.Match, Policy.Matcher> exampleRule =
				this.batchFactory.createRule(Policy.instance()).action(function)
						.name("add-schedulerange-" + day.toString() + '-' + name + "-" + startTime + "-" + endTime).build();
		return exampleRule;
	}

	// -----------------------------------------------


	
	// ---------- Add / Remove Authorization Model Relations ----------


	// ----------------------------------------------------------------


	
	// -------------------- Utility --------------------
	
	public static List findAllByName(com.vanderhighway.trbac.model.trbac.model.Policy policy, EClass clazz, String name) {
		switch (clazz.getName()) {
			case "Session":
				return policy.getSessions().stream().filter(entity -> entity.getName().equals(name)).collect(Collectors.toList());
			case "User":
				return policy.getUsers().stream().filter(entity -> entity.getName().equals(name)).collect(Collectors.toList());
			case "Role":
				return policy.getRoles().stream().filter(entity -> entity.getName().equals(name)).collect(Collectors.toList());
			case "Demarcation":
				return policy.getDemarcations().stream().filter(entity -> entity.getName().equals(name)).collect(Collectors.toList());
			case "Permission":
				return policy.getPermissions().stream().filter(entity -> entity.getName().equals(name)).collect(Collectors.toList());
			default:
				return new LinkedList();
		}
	}

	public BatchTransformationRule createNamedEntity(EClass clazz, EReference containmentReference,
													 EStructuralFeature nameFeature, String name) {
		final Consumer<Policy.Match> function = (Policy.Match it) -> {
			try {
				if (findAllByName(it.getPolicy(), clazz, name).size() != 0) {
					throw new IllegalArgumentException();
				}
				EObject entity = this.manipulation.createChild(it.getPolicy(), containmentReference, clazz);
				this.manipulation.set(entity, nameFeature, name);

			} catch (ModelManipulationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		final BatchTransformationRule<Policy.Match, Policy.Matcher> exampleRule =
				this.batchFactory.createRule(Policy.instance()).action(function)
				.name("create-named-entity-" + clazz.getName() + '-' + name).build();
		return exampleRule;
	}

	// --------------------------------------------------

	
	public void execute(BatchTransformationRule rule) {
		this.transformation.getTransformationStatements().fireOne(rule);
	}

	public void dispose() {
		if (!Objects.equal(this.transformation, null)) {
			this.transformation.dispose();
		}
		this.transformation = null;
		return;
	}
}
