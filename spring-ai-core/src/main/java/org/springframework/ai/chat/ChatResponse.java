/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ai.chat;

import java.util.List;

import org.springframework.ai.metadata.GenerationMetadata;
import org.springframework.ai.metadata.PromptMetadata;
import org.springframework.ai.model.ModelRawResponse;
import org.springframework.lang.Nullable;

/**
 * The chat completion (e.g. generation) response returned by an AI provider.
 */
public class ChatResponse {

	/**
	 * List of generated messages returned by the AI provider.
	 */
	private final List<Generation> generations;

	/**
	 * Metadata returned by the AI provider when processing the prompt.
	 */
	private PromptMetadata promptMetadata;

	/**
	 * Raw response content. Depends on the AI provider this could be null. If not null
	 * you would have to cast it to the provider's specific response type.
	 */
	private ModelRawResponse rawResponse;

	/**
	 * Common AI provider metadata returned in an AI response.
	 */
	private final GenerationMetadata metadata;

	/**
	 * Construct a new {@link ChatResponse} instance without metadata.
	 * @param generations the {@link List} of {@link Generation} returned by the AI
	 * provider.
	 */
	public ChatResponse(List<Generation> generations) {
		this(generations, GenerationMetadata.NULL);
	}

	/**
	 * Construct a new {@link ChatResponse} instance.
	 * @param generations the {@link List} of {@link Generation} returned by the AI
	 * provider.
	 * @param metadata {@link GenerationMetadata} containing information about the use of
	 * the AI provider's API.
	 */
	public ChatResponse(List<Generation> generations, GenerationMetadata metadata) {
		this.metadata = metadata;
		this.generations = List.copyOf(generations);
	}

	/**
	 * The {@link List} of {@link Generation generated outputs}.
	 * <p>
	 * It is a {@link List} of {@link List lists} because the Prompt could request
	 * multiple output {@link Generation generations}.
	 * @return the {@link List} of {@link Generation generated outputs}.
	 */
	public List<Generation> getGenerations() {
		return this.generations;
	}

	/**
	 * @return Returns the first {@link Generation} in the generations list.
	 */
	public Generation getGeneration() {
		return this.generations.get(0);
	}

	/**
	 * @return Returns {@link GenerationMetadata} containing information about the use of
	 * the AI provider's API.
	 */
	public GenerationMetadata getGenerationMetadata() {
		return this.metadata;
	}

	/**
	 * @return {@link PromptMetadata} containing information on prompt processing by the
	 * AI.
	 */
	public PromptMetadata getPromptMetadata() {
		PromptMetadata promptMetadata = this.promptMetadata;
		return promptMetadata != null ? promptMetadata : PromptMetadata.empty();
	}

	/**
	 * Builder method used to include {@link PromptMetadata} returned in the AI response
	 * when processing the prompt.
	 * @param promptMetadata {@link PromptMetadata} returned by the AI in the response
	 * when processing the prompt.
	 * @return this {@link ChatResponse}.
	 * @see #getPromptMetadata()
	 */
	public ChatResponse withPromptMetadata(@Nullable PromptMetadata promptMetadata) {
		this.promptMetadata = promptMetadata;
		return this;
	}

	public ChatResponse withRawResponse(ModelRawResponse rawResponse) {
		this.rawResponse = rawResponse;
		return this;
	}

	public ModelRawResponse getRawResponse() {
		return rawResponse;
	}

}
