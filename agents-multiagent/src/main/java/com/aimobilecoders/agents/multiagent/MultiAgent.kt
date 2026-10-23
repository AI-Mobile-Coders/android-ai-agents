package com.aimobilecoders.agents.multiagent

import com.aimobilecoders.agents.core.Agent

/**
 * Top-level builder DSL for constructing multi-agent systems cleanly.
 */
class MultiAgentBuilder {
    var name: String = "multiAgent"
    private var rootAgent: Agent? = null

    /** Create and set a sequential [AgentPipeline]. */
    fun pipeline(name: String = "pipeline", configure: AgentPipelineBuilder.() -> Unit): AgentPipeline {
        val pipeline = agentPipeline {
            this.name = name
            configure()
        }
        rootAgent = pipeline
        return pipeline
    }

    /** Create and set a parallel [ParallelAgentGroup]. */
    fun parallel(name: String = "parallel", configure: ParallelAgentGroupBuilder.() -> Unit): ParallelAgentGroup {
        val group = parallelAgentGroup {
            this.name = name
            configure()
        }
        rootAgent = group
        return group
    }

    /** Create and set a [RouterAgent]. */
    fun router(name: String = "router", configure: RouterAgentBuilder.() -> Unit): RouterAgent {
        val router = routerAgent {
            this.name = name
            configure()
        }
        rootAgent = router
        return router
    }

    /** Set a custom root multi-agent. */
    fun agent(agent: Agent) {
        this.rootAgent = agent
    }

    fun build(): Agent {
        return rootAgent ?: throw IllegalStateException("MultiAgent must define at least one workflow architecture (pipeline, parallel, router, or agent).")
    }
}

/**
 * Constructs a multi-agent system wrapper using DSL syntax:
 * ```
 * val myMultiAgent = multiAgent {
 *     pipeline("research-and-write") {
 *         +researchAgent
 *         +writerAgent
 *     }
 * }
 * ```
 */
fun multiAgent(configure: MultiAgentBuilder.() -> Unit): Agent {
    return MultiAgentBuilder().apply(configure).build()
}
