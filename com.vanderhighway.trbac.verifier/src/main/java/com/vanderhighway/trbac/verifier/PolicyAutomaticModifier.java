package com.vanderhighway.trbac.verifier;

import com.google.common.base.Objects;
import com.vanderhighway.trbac.model.trbac.model.TRBACPackage;
import org.apache.log4j.Logger;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.transformation.evm.specific.Lifecycles;
import org.eclipse.viatra.transformation.evm.specific.crud.CRUDActivationStateEnum;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.IModelManipulations;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.SimpleModelManipulations;
import org.eclipse.viatra.transformation.runtime.emf.rules.eventdriven.EventDrivenTransformationRule;
import org.eclipse.viatra.transformation.runtime.emf.rules.eventdriven.EventDrivenTransformationRuleFactory;
import org.eclipse.viatra.transformation.runtime.emf.transformation.eventdriven.EventDrivenTransformation;
import org.eclipse.xtext.xbase.lib.Extension;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class PolicyAutomaticModifier {

    @Extension
    private Logger logger = Logger.getLogger(PolicyValidator.class);

    @Extension
    private IModelManipulations manipulation;

    @Extension
    private TRBACPackage ePackage = TRBACPackage.eINSTANCE;

    @Extension //Transformation-related extensions
    private EventDrivenTransformation transformation;

    @Extension //Transformation rule-related extensions
    private EventDrivenTransformationRuleFactory _eventDrivenTransformationRuleFactory = new EventDrivenTransformationRuleFactory();

    private AdvancedViatraQueryEngine engine;
    private PolicyModifier policyModifier;

    public PolicyAutomaticModifier(AdvancedViatraQueryEngine engine, PolicyModifier policyModifier) {
        this.engine = engine;
        this.policyModifier = policyModifier;
    }

    public void initialize() {
        this.logger.info("Preparing transformation rules.");
        this.transformation = createTransformation();
        this.logger.info("Prepared transformation rules");
    }

    public void execute() {
        this.logger.debug("Executing transformations");
        this.transformation.getExecutionSchema().startUnscheduledExecution();
    }

    private EventDrivenTransformation createTransformation() {
        EventDrivenTransformation transformation = null;
        this.manipulation = new SimpleModelManipulations(this.engine);
        transformation = EventDrivenTransformation.forEngine(this.engine)
                .addRule(this.ProcessRanges())
                .build();
        return transformation;
    }

    public void dispose() {
        if (!Objects.equal(this.transformation, null)) {
            this.transformation.dispose();
        }
        this.transformation = null;
        return;
    }


    private EventDrivenTransformationRule<Range.Match, Range.Matcher> ProcessRanges() {
        final Consumer<Range.Match> function = (Range.Match it) -> {
            System.out.println("hey!");
        };
        EventDrivenTransformationRule<Range.Match, Range.Matcher> dayrangerule =
                this._eventDrivenTransformationRuleFactory.createRule(Range.instance()).action(
                        CRUDActivationStateEnum.CREATED, (Range.Match it) -> {
                            System.out.println("EventDrivenTransformationRule called, with:" + it.toString());
                            try {
                            	//engine.delayUpdatePropagation(new DoAddRole(this.policyModifier, "Role"+ it.getRange().getName() ));
                                engine.delayUpdatePropagation(new DoAddScheduleRange(this.policyModifier, it));
                                 } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }).action(
                        CRUDActivationStateEnum.UPDATED, (Range.Match it) -> {}).action(
                        CRUDActivationStateEnum.DELETED, (Range.Match it) -> {}
                            ).addLifeCycle(Lifecycles.getDefault(true, true))
                        .name("process-day-ranges").build();
        return dayrangerule;
    }

    public class DoAddScheduleRange implements Callable<Void> {
        private final PolicyModifier modifier;
        private final Range.Match it;

        public DoAddScheduleRange(PolicyModifier modifier, Range.Match it) {
            this.modifier = modifier;
            this.it = it;
        }

        public Void call() throws Exception {
            String key = String.valueOf("copy-" + it.getRange().toString());

            this.modifier.execute(this.modifier.addRole("Role-" + it.getRange().getName())); 
            this.modifier.execute(this.modifier.addScheduleRange(it.getRange().getDayschedule(),
                    key, it.getStarttime(), it.getEndtime()));
            return null;
        }
    }
    
    public class DoAddRole implements Callable<Void> {
        private final PolicyModifier modifier;
        private final String name;

        public DoAddRole(PolicyModifier modifier, String name) {
            this.modifier = modifier;
            this.name = name;
        }

        public Void call() throws Exception {
            this.modifier.execute(this.modifier.addRole(name));
            return null;
        }
    }
}
