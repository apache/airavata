package org.apache.ariavata.simple.workflow.engine.dag.nodes;

/**
 * Created by shameera on 1/29/15.
 */
public enum NodeState {
    WAITING, // waiting on inputs
    READY, // all inputs are available and ready to execute
    EXECUTING, // task has been submitted , not yet finish
    EXECUTED, // task executed
    COMPLETE // all works done
}
