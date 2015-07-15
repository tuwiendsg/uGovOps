package at.ac.tuwien.dsg.rSybl.planningEngine;

/**
 * Copyright 2013 Technische Universitat Wien (TUW), Distributed SystemsGroup
 * E184. * This work was partially supported by the European Commission in terms
 * of the CELAR FP7 project (FP7-ICT-2011-8 #317790).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * Author : Georgiana Copil - e.copil@dsg.tuwien.ac.at
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import at.ac.tuwien.dsg.csdg.DependencyGraph;
import at.ac.tuwien.dsg.csdg.Node;
import at.ac.tuwien.dsg.csdg.Node.NodeType;
import at.ac.tuwien.dsg.csdg.Relationship;
import at.ac.tuwien.dsg.csdg.Relationship.RelationshipType;
import at.ac.tuwien.dsg.csdg.elasticityInformation.ElasticityCapability;
import at.ac.tuwien.dsg.csdg.elasticityInformation.ElasticityRequirement;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.Condition;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.SYBLSpecification;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.Strategy;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.SYBLDirectiveMappingFromXML;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.EventNotification;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.IEvent;
import at.ac.tuwien.dsg.rSybl.cloudInteractionUnit.api.EnforcementAPIInterface;
import at.ac.tuwien.dsg.rSybl.dataProcessingUnit.api.MonitoringAPIInterface;
import at.ac.tuwien.dsg.rSybl.planningEngine.ContextRepresentation.Pair;
import at.ac.tuwien.dsg.rSybl.planningEngine.adviseEffects.PlanningGreedyWithADVISE;
import at.ac.tuwien.dsg.rSybl.planningEngine.utils.Configuration;
import at.ac.tuwien.dsg.rSybl.planningEngine.staticData.ActionEffect;
import at.ac.tuwien.dsg.rSybl.planningEngine.staticData.ActionEffects;
import at.ac.tuwien.dsg.rSybl.planningEngine.utils.PlanningLogger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlanningGreedyAlgorithm implements PlanningAlgorithmInterface {

    private Timer t = new Timer();
    private ContextRepresentation contextRepresentation;
    private MonitoringAPIInterface monitoringAPI;
    private EnforcementAPIInterface enforcementAPI;
    private DependencyGraph dependencyGraph;
    private ContextRepresentation lastContextRepresentation;
    private String strategiesThatNeedToBeImproved = "";
    private int REFRESH_PERIOD = 120000;
    Deque<HashMap<String, Boolean>> stack = new ArrayDeque<HashMap<String, Boolean>>();
    PlanningGreedyWithADVISE planningGreedyWithADVISE;
    private EventNotification eventNotification;
    private Timer evaluateLearningPerformance = new Timer();

    public PlanningGreedyAlgorithm(DependencyGraph cloudService,
            MonitoringAPIInterface monitoringAPI, EnforcementAPIInterface enforcementAPI) {
        this.dependencyGraph = cloudService;
        this.monitoringAPI = monitoringAPI;
        this.enforcementAPI = enforcementAPI;
        this.eventNotification = EventNotification.getEventNotification();
        REFRESH_PERIOD = Configuration.getRefreshPeriod();
        if (Configuration.getADVISEEnabled()) {
            planningGreedyWithADVISE = new PlanningGreedyWithADVISE(monitoringAPI, cloudService.getCloudService(), enforcementAPI, this);
            planningGreedyWithADVISE.startLearningProcess();

        }
    }

    public void checkWhetherLearningIsAccurateAndSwitch() {

        if (planningGreedyWithADVISE.checkWhetherPerformanceIsAcceptable()) {

            while (enforcementAPI.isEnforcingAction()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PlanningGreedyAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            stop();
            evaluateLearningPerformance.cancel();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PlanningGreedyAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            }
            planningGreedyWithADVISE.replaceDependencyGraph(dependencyGraph);
            planningGreedyWithADVISE.start();
        }
    }

    public boolean checkIfActionPossible(ActionEffect actionEffect) {
        if (actionEffect.isConditional()) {
            if (!actionEffect.evaluateConditions(dependencyGraph, monitoringAPI)) {
                return false;
            }
        }

        // System.out.println("Targeted entity id "
        // +actionEffect.getTargetedEntityID()+entity);
        boolean possible = true;
        if (actionEffect.getActionType().equalsIgnoreCase("scalein")) {
            Node entity = dependencyGraph.getNodeWithID(actionEffect.getTargetedEntityID());
            if (entity != null && entity.getNodeType() == NodeType.CLOUD_SERVICE) {
                List<String> ips = entity.getAssociatedIps();
                // PlanningLogger.logger.info("For action " + actionEffect.getActionName() + entity.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.VIRTUAL_MACHINE).size() + " hosts");
                Node artifact = null;
                Node container = null;
                Node accessToVM = entity;

                if (entity.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.ARTIFACT) != null && entity.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.ARTIFACT).size() > 0) {
                    artifact = entity.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.ARTIFACT).get(0);

                    if (artifact.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.CONTAINER) != null && artifact.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.CONTAINER).size() > 0) {

                        container = artifact.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.CONTAINER).get(0);
                    }
                }
                if (artifact != null || container != null) {

                    if (container == null) {
                        accessToVM = artifact;
                    } else {
                        accessToVM = container;
                    }
                }
                if (accessToVM.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.VIRTUAL_MACHINE).size() > 4) {
                    return true;
                }
            }
            if (entity != null && entity.getNodeType() == NodeType.SERVICE_TOPOLOGY) {

                Node master = dependencyGraph.findParentNode(entity.getId());
                List<String> ips = master.getAssociatedIps();
                int numberPrivateIps = 0;
                for (String ip : ips) {
                    if (ip.split("\\.")[0].length() == 2) {
                        numberPrivateIps++;
                    }
                }
                Node artifact = null;
                Node container = null;
                Node accessToVM = entity;

                if (entity.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.ARTIFACT) != null && entity.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.ARTIFACT).size() > 0) {
                    artifact = entity.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.ARTIFACT).get(0);

                    if (artifact.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.CONTAINER) != null && artifact.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.CONTAINER).size() > 0) {

                        container = artifact.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.CONTAINER).get(0);
                    }
                }
                if (artifact != null || container != null) {

                    if (container == null) {
                        accessToVM = artifact;
                    } else {
                        accessToVM = container;
                    }
                }
                if (accessToVM.getAllRelatedNodesOfType(RelationshipType.HOSTED_ON_RELATIONSHIP, NodeType.VIRTUAL_MACHINE).size() > 4) {
                    return true;
                }
            }
        }
        return possible;

    }

    public void findStrategies() {

        for (ElasticityRequirement elasticityRequirement : dependencyGraph.getAllElasticityRequirements()) {
            SYBLSpecification syblSpecification = SYBLDirectiveMappingFromXML.mapFromSYBLAnnotation(elasticityRequirement.getAnnotation());
            MonitoredEntity monitoredEntity = contextRepresentation.findMonitoredEntity(syblSpecification.getComponentId());
            if (monitoredEntity == null) {
                PlanningLogger.logger.info("Not finding monitored entity " + monitoredEntity + " " + syblSpecification.getComponentId());
            }
            for (Strategy strategy : syblSpecification.getStrategy()) {
                Condition condition = strategy.getCondition();

                if (contextRepresentation.evaluateCondition(condition, monitoredEntity)) {
                    if (strategy.getToEnforce().getActionName().toLowerCase().contains("maximize") || strategy.getToEnforce().getActionName().toLowerCase().contains("minimize")) {
                        if (strategy.getToEnforce().getActionName().toLowerCase().contains("maximize")) {
                            //PlanningLogger.logger.info("Current value for "+ strategy.getToEnforce().getParameter()+" is "+ monitoredEntity.getMonitoredValue(strategy.getToEnforce().getParameter())+" .Previous value was "+previousContextRepresentation.getValueForMetric(monitoredEntity,strategy.getToEnforce().getParameter()));

                            if (monitoredEntity.getMonitoredValue(strategy.getToEnforce().getParameter()) <= lastContextRepresentation.getValueForMetric(monitoredEntity, strategy.getToEnforce().getParameter())) {
                                strategiesThatNeedToBeImproved += strategy.getId() + " ";
                            }
                        }
                        if (strategy.getToEnforce().getActionName().toLowerCase().contains("minimize")) {

                            PlanningLogger.logger.info("Current value for " + strategy.getToEnforce().getParameter() + " is " + monitoredEntity.getMonitoredValue(strategy.getToEnforce().getParameter()) + " .Previous value was " + lastContextRepresentation.getValueForMetric(monitoredEntity, strategy.getToEnforce().getParameter()));

                            if (monitoredEntity.getMonitoredValue(strategy.getToEnforce().getParameter()) >= lastContextRepresentation.getValueForMetric(monitoredEntity, strategy.getToEnforce().getParameter())) {
                                strategiesThatNeedToBeImproved += strategy.getId() + " ";
                            }
                        }
                    }

                }
            }
        }

    }

    public void checkInstantiation() {
        List<Relationship> instantiationRelationships = dependencyGraph.getAllRelationshipsOfType(RelationshipType.INSTANTIATION);
        //for
    }

    public ActionEffect checkActions(String target) {
        HashMap<String, List<ActionEffect>> actionEffects = ActionEffects.getActionConditionalEffects();
        if (actionEffects.size() > 0) {
            int maxConstraints = 0;
            ActionEffect maxConstraintsAction = null;
            PlanningLogger.logger.info("~~~~~~~~~~~Evaluating complimentary actions for " + target);

            for (List<ActionEffect> actionEffect : actionEffects.values()) {
                for (ActionEffect effect : actionEffect) {
                    if (effect.getAffectedNodes().contains(target)) {
                        int beforeConstraints = contextRepresentation.countViolatedConstraints();
                        MonitoredCloudService monitoredCloudService = contextRepresentation.getMonitoredCloudService().clone();
                        ContextRepresentation beforeContext = new ContextRepresentation(monitoredCloudService, monitoringAPI);
                        contextRepresentation.doAction(effect);
                        int improvedStrategies = contextRepresentation.countFixedStrategies(beforeContext);
                        int afterConstraints = contextRepresentation.countViolatedConstraints();
                        PlanningLogger.logger.info("With " + effect.getActionType() + " on " + effect.getTargetedEntityID() + improvedStrategies + " and constraints  " + (beforeConstraints - afterConstraints) + " violated constraints " + contextRepresentation.getViolatedConstraints());
                        contextRepresentation.undoAction(effect);
                        if (beforeConstraints - afterConstraints + improvedStrategies > maxConstraints && (beforeConstraints - afterConstraints + improvedStrategies) > 0) {
                            maxConstraints = beforeConstraints - afterConstraints + improvedStrategies;
                            maxConstraintsAction = effect;
                        }
                    }
                }
            }

            if (maxConstraintsAction != null) {
                PlanningLogger.logger.info("Returning " + maxConstraintsAction.getActionType() + " on " + maxConstraintsAction.getTargetedEntityID());
            } else {
                PlanningLogger.logger.info("Returning null ");
            }
            return maxConstraintsAction;
        } else {
            HashMap<String, ActionEffect> defaultEffects = ActionEffects.getActionDefaultEffects();
            int maxConstraints = 0;
            ActionEffect maxConstraintsAction = null;
            PlanningLogger.logger.info("~~~~~~~~~~~Evaluating complimentary actions for " + target);

            for (ActionEffect effect : defaultEffects.values()) {
                boolean checkIfAvailable = false;
                for (ElasticityCapability capability : dependencyGraph.getNodeWithID(target).getElasticityCapabilities()) {
                    if (capability.getPrimitiveOperations().contains(effect.getActionName())) {
                        checkIfAvailable = true;

                    }
                }
                if (checkIfAvailable) {
                    int beforeConstraints = contextRepresentation.countViolatedConstraints();
                    MonitoredCloudService monitoredCloudService = contextRepresentation.getMonitoredCloudService().clone();
                    ContextRepresentation beforeContext = new ContextRepresentation(monitoredCloudService, monitoringAPI);
                    contextRepresentation.doAction(effect, target);
                    int improvedStrategies = contextRepresentation.countFixedStrategies(beforeContext);
                    int afterConstraints = contextRepresentation.countViolatedConstraints();
                    PlanningLogger.logger.info("With " + effect.getActionType() + " on " + effect.getTargetedEntityID() + improvedStrategies + " and constraints  " + (beforeConstraints - afterConstraints) + " violated constraints " + contextRepresentation.getViolatedConstraints());
                    contextRepresentation.undoAction(effect, target);
                    if (beforeConstraints - afterConstraints + improvedStrategies > maxConstraints && (beforeConstraints - afterConstraints + improvedStrategies) > 0) {
                        maxConstraints = beforeConstraints - afterConstraints + improvedStrategies;
                        maxConstraintsAction = effect;
                    }

                }
            }
            if (maxConstraintsAction != null) {
                PlanningLogger.logger.info("Returning " + maxConstraintsAction.getActionType() + " on " + maxConstraintsAction.getTargetedEntityID());
            } else {
                PlanningLogger.logger.info("Returning null ");
            }

            return maxConstraintsAction;
        }

    }

    public void findAndExecuteBestActionsForDefaultEffects() {
        strategiesThatNeedToBeImproved = "";

        if (lastContextRepresentation != null) {
            findStrategies();
        }
        lastContextRepresentation = new ContextRepresentation(dependencyGraph, monitoringAPI);
        lastContextRepresentation.initializeContext();
        //PlanningLogger.logger.info("Strategies that could be enforced. ... "+strategiesThatNeedToBeImproved+" Violated constraints: "+contextRepresentation.getViolatedConstraints());
        HashMap<String, ActionEffect> actionEffects = ActionEffects.getActionDefaultEffects();
        if (actionEffects.size() == 0) {
            PlanningLogger.logger.info("Not trying any type of actions, action effect is null");
            return;
        }
        int numberOfBrokenConstraints = contextRepresentation
                .countViolatedConstraints();

        PlanningLogger.logger.info("Violated constraints number: " + numberOfBrokenConstraints);
        HashMap<String, String> constraintsWhichWouldBeViolated = new HashMap<String, String>();

        int lastFixed = 1;
        ArrayList<Pair<ActionEffect, Integer>> result = new ArrayList<Pair<ActionEffect, Integer>>();
        double violationDegree = contextRepresentation.evaluateViolationDegree();
        int numberOfRemainingConstraints = numberOfBrokenConstraints;
        if (!strategiesThatNeedToBeImproved.equalsIgnoreCase("") || numberOfBrokenConstraints > 0 && lastFixed != 0) {
//		while (contextRepresentation.countViolatedConstraints() > 0
//				&& numberOfRemainingConstraints > 0 && lastFixed>0) {
            Date date = new Date();
            HashMap<Integer, List<Pair<ActionEffect, String>>> fixedDirectives = new HashMap<Integer, List<Pair<ActionEffect, String>>>();
            HashMap<Integer, List<Pair<ActionEffect, String>>> fixedStrategies = new HashMap<Integer, List<Pair<ActionEffect, String>>>();
            // PlanningLogger.logger.info("~~~~~~~~~~~Number of actions possible: "+actionEffects.values().size());
            for (ElasticityCapability elasticityCapability : dependencyGraph.getAllElasticityCapabilities()) {
                String servicePartID = elasticityCapability.getServicePartID();

                for (ActionEffect actionEffect : actionEffects.values()) {
                    if (checkIfActionPossible(actionEffect) && elasticityCapability.getName().toLowerCase().equalsIgnoreCase(actionEffect.getActionName())) {

                        List<Pair<ActionEffect, String>> foundActions = new ArrayList<Pair<ActionEffect, String>>();

                        for (Pair<ActionEffect, Integer> a : result) {
                            PlanningLogger.logger.info("Executing the already found action" + a.getFirst().getActionName());
                            contextRepresentation.doAction(a.getFirst(), ((ActionEffect) a.getFirst()).getTargetedEntityID());
                            PlanningLogger.logger.info("At " + date.getDay() + "_"
                                    + date.getMonth() + "_" + date.getHours() + "_"
                                    + date.getMinutes()
                                    + ". The violated constraints are the following: "
                                    + contextRepresentation.getViolatedConstraints());

                        }
                        String[] initiallyBrokenConstraintsString = contextRepresentation.getViolatedConstraints().split(" ");
                        int initiallyBrokenConstraints = contextRepresentation
                                .countViolatedConstraints();
                        MonitoredCloudService monitoredCloudService = contextRepresentation.getMonitoredCloudService().clone();
                        ContextRepresentation beforeActionContextRepresentation = new ContextRepresentation(monitoredCloudService, monitoringAPI);
                        // TODO: Try from 1 to 10 actions of the same type
//						for (int i = 0; i < 10; i++) {
//							for (int current = 0; current < i; current++) {
//								contextRepresentation.doAction(actionEffect);
//							}

                        contextRepresentation.doAction(actionEffect, servicePartID);
                        String[] brokenConstraintsAfterWise = contextRepresentation.getViolatedConstraints().split(" ");
                        for (String s : initiallyBrokenConstraintsString) {
                            boolean ok = true;
                            for (String s1 : brokenConstraintsAfterWise) {
                                if (s1.equalsIgnoreCase(s)) {
                                    ok = false;
                                }
                            }
                            if (ok) {
                                String constr = "";
                                if (brokenConstraintsAfterWise.length > 0) {
                                    for (String s1 : brokenConstraintsAfterWise) {
                                        if (constraintsWhichWouldBeViolated.get(s) == null || !constraintsWhichWouldBeViolated.get(s).contains(s1)) {
                                            constr += s1 + " ";
                                        }
                                    }
                                }

                                constraintsWhichWouldBeViolated.put(s, constr);
                            }
                        }
                        foundActions.add(contextRepresentation.new Pair<ActionEffect, String>(actionEffect, servicePartID));
                        int fixedStr = contextRepresentation.countFixedStrategies(beforeActionContextRepresentation, strategiesThatNeedToBeImproved);
                        PlanningLogger.logger.info("PlanningAlgorithm: Trying the action " + actionEffect.getActionName() + "constraints violated : " + contextRepresentation.getViolatedConstraints() + " Strategies improved " + contextRepresentation.getImprovedStrategies(beforeActionContextRepresentation, strategiesThatNeedToBeImproved));

                        fixedDirectives
                                .put(initiallyBrokenConstraints
                                        - contextRepresentation
                                        .countViolatedConstraints() + fixedStr, foundActions);
                        fixedStrategies
                                .put(
                                        fixedStr, foundActions);
                        /////////////////////~~~~~~~~~~~Check complimentary actions needed~~~~~~~~~~~~~~~~//

                        contextRepresentation.undoAction(actionEffect, servicePartID);
//							for (int current = 0; current < i; current++) {
//								contextRepresentation.undoAction(actionEffect);
//							}

//						}
                        // System.out.println("Action "+actionEffect.getTargetedEntityID()+" "+actionEffect.getActionType()+" fixes "+(numberOfBrokenConstraints-contextRepresentation.countViolatedConstraints())+" constraints.");
                        for (int i = result.size() - 1; i > 0; i--) {
                            //System.out.println("Undoing action "
                            //+ actionEffect.getActionName());
                            PlanningLogger.logger.info("Undo-ing the already found action" + result.get(i).getFirst().getActionName());
                            contextRepresentation.undoAction(result.get(i)
                                    .getFirst(), ((ActionEffect) result.get(i).getFirst()).getTargetedEntityID());
                        }
                    }
                }

            }
            int maxAction = -20;
            List<Pair<ActionEffect, String>> action = null;
            for (Integer val : fixedDirectives.keySet()) {
                PlanningLogger.logger.info("fixed directives  " + val);
                if (val > maxAction) {
                    maxAction = val;
                    action = fixedDirectives.get(val);
                }

            }
            int minStrat = 0;
            for (Integer v : fixedStrategies.keySet()) {
                if (fixedStrategies.get(v).equals(fixedDirectives.get(maxAction)) && minStrat < v) {
                    minStrat = v;
                    action = fixedStrategies.get(minStrat);
                }
            }

            //	PlanningLogger.logger.info("Found action "+ action);
            // Find cloudService = SYBLRMI enforce action with action type,
            if (maxAction > 0 && action != null && !result.contains(action)) {
                for (Pair<ActionEffect, String> actionEffect : action) {

                    PlanningLogger.logger.info("Found action " + actionEffect.getFirst().getActionName()
                            + " on "
                            + actionEffect.getSecond() + " Number of directives fixed: "
                            + maxAction);
                    lastFixed = maxAction;
                    Node entity = dependencyGraph.getNodeWithID(((ActionEffect) actionEffect.getFirst())
                            .getTargetedEntityID());
                    ((ActionEffect) actionEffect.getFirst()).setTargetedEntity(entity);
                    if (maxAction > 0) {
                        //  result.add(actionEffect);
                    }

                }
                List<Pair<ActionEffect, Integer>> actions = new ArrayList<Pair<ActionEffect, Integer>>();
                for (Pair<ActionEffect, String> a : action) {
                    ActionEffect newAction = a.getFirst().clone();
                    newAction.setTargetedEntityID(a.getSecond());
                    newAction.setTargetedEntity(dependencyGraph.getNodeWithID(a.getSecond()));
                    actions.add(contextRepresentation.new Pair<ActionEffect, Integer>(newAction, 1));
                }
                result.addAll(actions);
            } else {

                lastFixed = 0;
            }
            numberOfRemainingConstraints -= lastFixed;

        }
        for (int i = 0; i < result.size(); i++) {
            contextRepresentation.doAction(result.get(i).getFirst());
        }

        if (result.size() == 0 && contextRepresentation.countViolatedConstraints() > 0) {
            EventNotification eventNotification = EventNotification.getEventNotification();
            CustomEvent customEvent = new CustomEvent();
            customEvent.setCloudServiceID(this.dependencyGraph.getCloudService().getId());
            customEvent.setType(IEvent.Type.NOTIFICATION);
            customEvent.setTarget(contextRepresentation.getViolatedConstraints());
            String violatedConstraints = contextRepresentation.getViolatedConstraints();
            String conflictingConstraintsStory = "";
           for (String s : violatedConstraints.split(" ")) {
                if (constraintsWhichWouldBeViolated.get(s)==null){
                    conflictingConstraintsStory+=s+",";
                }else
                conflictingConstraintsStory += constraintsWhichWouldBeViolated.get(s) + "(" + s + "), ";
            }
            customEvent.setMessage("Requirements " + contextRepresentation.getViolatedConstraints() + " are violated. rSYBL can not solve the problem due to: " + conflictingConstraintsStory.substring(0, conflictingConstraintsStory.length() - 1) + ".");
            eventNotification.sendEvent(customEvent);
            monitoringAPI.sendMessageToAnalysisService("Requirements " + contextRepresentation.getViolatedConstraints() + " are violated, and rSYBL can not solve the problem.");
        } else {

            ActionPlanEnforcement actionPlanEnforcement = new ActionPlanEnforcement(enforcementAPI);
            actionPlanEnforcement.enforceResult(result, dependencyGraph, contextRepresentation.getFixedConstraintsAsConstraints(lastContextRepresentation), contextRepresentation.getImprovedStrategiesAsStrategies(lastContextRepresentation, strategiesThatNeedToBeImproved));
        }
    }

    public void findAndExecuteBestActions() {

        strategiesThatNeedToBeImproved = "";

        if (lastContextRepresentation != null) {
            findStrategies();
        }
        lastContextRepresentation = new ContextRepresentation(dependencyGraph, monitoringAPI);
        lastContextRepresentation.initializeContext();
        //PlanningLogger.logger.info("Strategies that could be enforced. ... "+strategiesThatNeedToBeImproved+" Violated constraints: "+contextRepresentation.getViolatedConstraints());
        HashMap<String, List<ActionEffect>> actionEffects = ActionEffects.getActionConditionalEffects();
        if (actionEffects.size() == 0) {
            findAndExecuteBestActionsForDefaultEffects();
            return;
        }
        HashMap<String, String> constraintsWhichWouldBeViolated = new HashMap<String, String>();

        int numberOfBrokenConstraints = contextRepresentation
                .countViolatedConstraints();

        PlanningLogger.logger.info("Violated constraints number: " + numberOfBrokenConstraints);

        int lastFixed = 1;
        ArrayList<Pair<ActionEffect, Integer>> result = new ArrayList<Pair<ActionEffect, Integer>>();
        double violationDegree = contextRepresentation.evaluateViolationDegree();
        int numberOfRemainingConstraints = numberOfBrokenConstraints;
        if (!strategiesThatNeedToBeImproved.equalsIgnoreCase("") || numberOfBrokenConstraints > 0 && lastFixed != 0) {
//		while (contextRepresentation.countViolatedConstraints() > 0
//				&& numberOfRemainingConstraints > 0 && lastFixed>0) {
            Date date = new Date();
            HashMap<Integer, List<Pair<ActionEffect, Integer>>> fixedDirectives = new HashMap<Integer, List<Pair<ActionEffect, Integer>>>();
            HashMap<Integer, List<Pair<ActionEffect, Integer>>> fixedStrategies = new HashMap<Integer, List<Pair<ActionEffect, Integer>>>();
            // PlanningLogger.logger.info("~~~~~~~~~~~Number of actions possible: "+actionEffects.values().size());
            for (List<ActionEffect> list : actionEffects.values()) {

                for (ActionEffect actionEffect : list) {
                    if (checkIfActionPossible(actionEffect)) {

                        List<Pair<ActionEffect, Integer>> foundActions = new ArrayList<Pair<ActionEffect, Integer>>();

                        for (Pair<ActionEffect, Integer> a : result) {
                            for (int i = 0; i < a.getSecond(); i++) {
                                PlanningLogger.logger.info("Executing the already found action" + a.getFirst().getActionName());
                                contextRepresentation.doAction(a.getFirst());
                                PlanningLogger.logger.info("At " + date.getDay() + "_"
                                        + date.getMonth() + "_" + date.getHours() + "_"
                                        + date.getMinutes()
                                        + ". The violated constraints are the following: "
                                        + contextRepresentation.getViolatedConstraints());

                            }
                        }
                        int initiallyBrokenConstraints = contextRepresentation
                                .countViolatedConstraints();
                        String[] initiallyBrokenConstraintsString = contextRepresentation.getViolatedConstraints().split(" ");
                        MonitoredCloudService monitoredCloudService = contextRepresentation.getMonitoredCloudService().clone();
                        ContextRepresentation beforeActionContextRepresentation = new ContextRepresentation(monitoredCloudService, monitoringAPI);
                        // TODO: Try from 1 to 10 actions of the same type
//						for (int i = 0; i < 10; i++) {
//							for (int current = 0; current < i; current++) {
//								contextRepresentation.doAction(actionEffect);
//							}

                        contextRepresentation.doAction(actionEffect);
                        String[] brokenConstraintsAfterWise = contextRepresentation.getViolatedConstraints().split(" ");
                        for (String s : initiallyBrokenConstraintsString) {
                            boolean ok = true;
                            for (String s1 : brokenConstraintsAfterWise) {
                                if (s1.equalsIgnoreCase(s)) {
                                    ok = false;
                                }
                            }
                            if (ok) {
                                
                                String constr = "";
                                if (constraintsWhichWouldBeViolated.containsKey(s)){
                                    constr=constraintsWhichWouldBeViolated.get(s);
                                }
                                if (brokenConstraintsAfterWise.length > 0 ) {
                                    for (String s1 : brokenConstraintsAfterWise) {
                                        if (!s1.equalsIgnoreCase("") && ( constraintsWhichWouldBeViolated.get(s) == null || !constraintsWhichWouldBeViolated.get(s).contains(s1))) {
                                            constr += s1 + " ";
                                        }
                                    }
                                }
                                if (!constr.equalsIgnoreCase("")){
                                    constraintsWhichWouldBeViolated.put(s, constr);
                                }
                            }
                        }
                        foundActions.add(contextRepresentation.new Pair<ActionEffect, Integer>(actionEffect, 1));

                        int fixedStr = contextRepresentation.countFixedStrategies(beforeActionContextRepresentation, strategiesThatNeedToBeImproved);
                        PlanningLogger.logger.info("PlanningAlgorithm: Trying the action " + actionEffect.getActionName() + "constraints violated : " + contextRepresentation.getViolatedConstraints() + " Strategies improved " + contextRepresentation.getImprovedStrategies(beforeActionContextRepresentation, strategiesThatNeedToBeImproved));

                        fixedDirectives
                                .put(initiallyBrokenConstraints
                                        - contextRepresentation
                                        .countViolatedConstraints() + fixedStr, foundActions);
                        fixedStrategies
                                .put(
                                        fixedStr, foundActions);
                        /////////////////////~~~~~~~~~~~Check complimentary actions needed~~~~~~~~~~~~~~~~//
                        List<String> targets = contextRepresentation.simulateDataImpact(beforeActionContextRepresentation, actionEffect);
                        if (targets != null) {
                            for (String target : targets) {
                                ActionEffect dataAction = checkActions(target);
                                if (dataAction != null) {
                                    MonitoredCloudService newMonitoredCloudService = contextRepresentation.getMonitoredCloudService().clone();
                                    ContextRepresentation beforeContext = new ContextRepresentation(newMonitoredCloudService, monitoringAPI);
                                    int beforeC = contextRepresentation.countViolatedConstraints();
                                    contextRepresentation.doAction(dataAction);
                                    int afterC = contextRepresentation.countViolatedConstraints();
                                    int improvedStrategies = contextRepresentation.countFixedStrategies(beforeContext);
                                    int req = initiallyBrokenConstraints - afterC + improvedStrategies;

                                    PlanningLogger.logger.info("PlanningAlgorithm: Trying the action due to DATA " + dataAction.getActionName() + "constraints violated : " + contextRepresentation.getViolatedConstraints() + " Strategies improved " + contextRepresentation.getImprovedStrategies(beforeActionContextRepresentation, strategiesThatNeedToBeImproved));

                                    foundActions.add(contextRepresentation.new Pair<ActionEffect, Integer>(dataAction, 1));
                                    fixedDirectives
                                            .put(req, foundActions);
                                    fixedStrategies
                                            .put(
                                                    improvedStrategies, foundActions);
                                    contextRepresentation.undoAction(dataAction);
                                }
                            }
                            contextRepresentation.undoDataImpactSimulation(beforeActionContextRepresentation, actionEffect);
                        }

                        targets = contextRepresentation.simulateLoadImpact(beforeActionContextRepresentation, actionEffect);
                        if (targets != null) {
                            for (String target : targets) {
                                MonitoredCloudService newMonitoredCloudService = contextRepresentation.getMonitoredCloudService().clone();
                                ContextRepresentation beforeContext = new ContextRepresentation(newMonitoredCloudService, monitoringAPI);

                                ActionEffect loadAction = checkActions(target);
                                if (loadAction != null) {
                                    int beforeC = contextRepresentation.countViolatedConstraints();
                                    contextRepresentation.doAction(loadAction);
                                    int afterC = contextRepresentation.countViolatedConstraints();
                                    int improvedStrategies = contextRepresentation.countFixedStrategies(beforeContext);
                                    int req = initiallyBrokenConstraints - afterC + improvedStrategies;

                                    PlanningLogger.logger.info("PlanningAlgorithm: Trying the action due to LOAD " + loadAction.getActionName() + "constraints violated : " + contextRepresentation.getViolatedConstraints() + " Strategies improved " + contextRepresentation.getImprovedStrategies(beforeActionContextRepresentation, strategiesThatNeedToBeImproved));

                                    foundActions.add(contextRepresentation.new Pair<ActionEffect, Integer>(loadAction, 1));
                                    fixedDirectives
                                            .put(req, foundActions);
                                    fixedStrategies
                                            .put(
                                                    improvedStrategies, foundActions);
                                    contextRepresentation.undoAction(loadAction);
                                }
                            }

                            contextRepresentation.undoLoadImpactSimulation(beforeActionContextRepresentation, actionEffect);
                        }

                        contextRepresentation.undoAction(actionEffect);
//							for (int current = 0; current < i; current++) {
//								contextRepresentation.undoAction(actionEffect);
//							}

//						}
                        // System.out.println("Action "+actionEffect.getTargetedEntityID()+" "+actionEffect.getActionType()+" fixes "+(numberOfBrokenConstraints-contextRepresentation.countViolatedConstraints())+" constraints.");
                        for (int i = result.size() - 1; i > 0; i--) {
                            //System.out.println("Undoing action "
                            //+ actionEffect.getActionName());
                            for (int j = 0; j < result.get(i).getSecond(); j++) {
                                PlanningLogger.logger.info("Undo-ing the already found action" + result.get(i).getFirst().getActionName());
                                contextRepresentation.undoAction(result.get(i)
                                        .getFirst());
                            }
                        }
                    }
                }
            }

            int maxAction = -20;
            List<Pair<ActionEffect, Integer>> action = null;

            for (Integer val : fixedDirectives.keySet()) {
                PlanningLogger.logger.info("fixed directives  " + fixedDirectives.get(val).size());
                if (val > maxAction) {
                    maxAction = val;
                    action = fixedDirectives.get(val);
                }

            }
            int minStrat = 0;
            for (Integer v : fixedStrategies.keySet()) {
                if (fixedStrategies.get(v).equals(fixedDirectives.get(maxAction)) && minStrat < v) {
                    minStrat = v;
                    action = fixedStrategies.get(minStrat);
                }
            }

            //	PlanningLogger.logger.info("Found action "+ action);
            // Find cloudService = SYBLRMI enforce action with action type,
            if (maxAction > 0 && action != null && !result.contains(action)) {
                for (Pair<ActionEffect, Integer> actionEffect : action) {

                    for (int i = 0; i < actionEffect.getSecond(); i++) {
                        PlanningLogger.logger.info("Found action " + (i + 1) + "x"
                                + ((ActionEffect) actionEffect.getFirst()).getActionType()
                                + " on "
                                + ((ActionEffect) actionEffect.getFirst())
                                .getTargetedEntityID() + " Number of directives fixed: "
                                + maxAction);
                        lastFixed = maxAction;
                        Node entity = dependencyGraph.getNodeWithID(((ActionEffect) actionEffect.getFirst())
                                .getTargetedEntityID());
                        ((ActionEffect) actionEffect.getFirst()).setTargetedEntity(entity);
                        if (maxAction > 0) {
                            //  result.add(actionEffect);
                        }
                    }

                }
                result.addAll(action);
            } else {

                lastFixed = 0;
            }
            numberOfRemainingConstraints -= lastFixed;

        }
        for (int i = 0; i < result.size(); i++) {
            contextRepresentation.doAction(result.get(i).getFirst());
        }
        if (result.size() == 0 && contextRepresentation.countViolatedConstraints() > 0) {
            EventNotification eventNotification = EventNotification.getEventNotification();
            CustomEvent customEvent = new CustomEvent();
            customEvent.setCloudServiceID(this.dependencyGraph.getCloudService().getId());
            customEvent.setType(IEvent.Type.NOTIFICATION);
            customEvent.setTarget(this.dependencyGraph.getCloudService().getId());
            String conflictingConstraintsStory = "";
            String violatedConstraints = contextRepresentation.getViolatedConstraints();

            for (String s : violatedConstraints.split(" ")) {
                if (constraintsWhichWouldBeViolated.get(s)==null){
                    conflictingConstraintsStory+=s+",";
                }else
                conflictingConstraintsStory += constraintsWhichWouldBeViolated.get(s) + "(" + s + "), ";
            }
            customEvent.setMessage("Requirements " + contextRepresentation.getViolatedConstraints() + " are violated. rSYBL can not solve the problem due to: " + conflictingConstraintsStory.substring(0, conflictingConstraintsStory.length() - 2) + ".");

//            customEvent.setMessage("Requirements " + contextRepresentation.getViolatedConstraints() + " are violated, and rSYBL can not solve the problem.");
            eventNotification.sendEvent(customEvent);
            monitoringAPI.sendMessageToAnalysisService("Requirements " + contextRepresentation.getViolatedConstraints() + " are violated, and rSYBL can not solve the problem.");
        } else {

            ActionPlanEnforcement actionPlanEnforcement = new ActionPlanEnforcement(enforcementAPI);
            actionPlanEnforcement.enforceResult(result, dependencyGraph, contextRepresentation.getFixedConstraintsAsConstraints(lastContextRepresentation), contextRepresentation.getImprovedStrategiesAsStrategies(lastContextRepresentation, strategiesThatNeedToBeImproved));
        }
    }

    @Override
    public void run() {
        t = new Timer();
        try {
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (dependencyGraph.isInControlState()) {
                        try {
                            Thread.sleep(REFRESH_PERIOD);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            PlanningLogger.logger.error(e.toString());
                        }

                        Node cloudService = monitoringAPI.getControlledService();

                        dependencyGraph.setCloudService(cloudService);

                        contextRepresentation = new ContextRepresentation(dependencyGraph,
                                monitoringAPI);

                        contextRepresentation.initializeContext();

                        findAndExecuteBestActions();
                    }
                }
            }, REFRESH_PERIOD, REFRESH_PERIOD);
        } catch (Exception exception) {
            PlanningLogger.logger.error(exception.getMessage());
        }
    }

    @Override
    public void start() {
        if (Configuration.getADVISEEnabled()) {
            evaluateLearningPerformance = new Timer();
            evaluateLearningPerformance.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    checkWhetherLearningIsAccurateAndSwitch();
                }
            }, 0, REFRESH_PERIOD);
        }

        run();
    }

    @Override
    public void stop() {
        boolean ok = false;
        while (!ok) {
            if (enforcementAPI.getPluginsExecutingActions().size() > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PlanningGreedyAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                ok = true;
            }
        }
        t.purge();
        t.cancel();
    }

    @Override
    public void setEffects(String effects) {
        // TODO Auto-generated method stub
        ActionEffects.setActionEffects(effects);
    }

    @Override
    public void replaceDependencyGraph(DependencyGraph dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    @Override
    public void takeMainRole() {
        PlanningLogger.logger.info("SWITCHING to Initial Greedy Algorithm");
        this.start();
    }
}
