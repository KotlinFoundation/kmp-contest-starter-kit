package com.kotlinfoundation.koko.domain.usecase

import com.kotlinfoundation.koko.domain.model.generation.GenerationInput
import com.kotlinfoundation.koko.domain.model.generation.GenerationInputBuilder
import com.kotlinfoundation.koko.domain.model.generation.GenerationOutput
import com.kotlinfoundation.koko.domain.model.generation.generationInput

/**
 * Abstraction over the AI backend that turns a [GenerationInput] into a [GenerationOutput].
 * Implemented per provider in `data/source/ai/` (e.g. Replicate, OpenAI) and bound in DI.
 */
interface AiGenerationProvider {
    suspend fun generate(input: GenerationInput): Result<GenerationOutput>
}

/** Convenience overload that builds the input inline with the [generationInput] DSL. */
suspend fun AiGenerationProvider.generate(block: GenerationInputBuilder.() -> Unit): Result<GenerationOutput> {
    val input = generationInput(block)
    return generate(input)
}
