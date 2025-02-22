/*
 * Copyright 2023-2023 the original author or authors.
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

package org.springframework.ai.openai;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.AbstractEmbeddingClient;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.EmbeddingList;
import org.springframework.ai.openai.api.OpenAiApi.OpenAiApiException;
import org.springframework.ai.openai.api.OpenAiApi.Usage;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

/**
 * Open AI Embedding Client implementation.
 *
 * @author Christian Tzolov
 */
public class OpenAiEmbeddingClient extends AbstractEmbeddingClient {

	private static final Logger logger = LoggerFactory.getLogger(OpenAiEmbeddingClient.class);

	public static final String DEFAULT_OPENAI_EMBEDDING_MODEL = "text-embedding-ada-002";

	public final RetryTemplate retryTemplate = RetryTemplate.builder()
		.maxAttempts(10)
		.retryOn(OpenAiApiException.class)
		.exponentialBackoff(Duration.ofMillis(2000), 5, Duration.ofMillis(3 * 60000))
		.build();

	private final OpenAiApi openAiApi;

	private final String embeddingModelName;

	private final MetadataMode metadataMode;

	public OpenAiEmbeddingClient(OpenAiApi openAiApi) {
		this(openAiApi, DEFAULT_OPENAI_EMBEDDING_MODEL);
	}

	public OpenAiEmbeddingClient(OpenAiApi openAiApi, String embeddingModel) {
		this(openAiApi, embeddingModel, MetadataMode.EMBED);
	}

	public OpenAiEmbeddingClient(OpenAiApi openAiApi, String model, MetadataMode metadataMode) {
		Assert.notNull(openAiApi, "OpenAiService must not be null");
		Assert.notNull(model, "Model must not be null");
		Assert.notNull(metadataMode, "metadataMode must not be null");
		this.openAiApi = openAiApi;
		this.embeddingModelName = model;
		this.metadataMode = metadataMode;
	}

	@Override
	public List<Double> embed(Document document) {
		Assert.notNull(document, "Document must not be null");
		return this.embed(document.getFormattedContent(this.metadataMode));
	}

	@Override
	public EmbeddingResponse call(EmbeddingRequest request) {

		return this.retryTemplate.execute(ctx -> {
			org.springframework.ai.openai.api.OpenAiApi.EmbeddingRequest<List<String>> apiRequest = new org.springframework.ai.openai.api.OpenAiApi.EmbeddingRequest<>(
					request.getInstructions(), this.embeddingModelName);

			EmbeddingList<OpenAiApi.Embedding> apiEmbeddingResponse = this.openAiApi.embeddings(apiRequest).getBody();

			if (apiEmbeddingResponse == null) {
				logger.warn("No embeddings returned for request: {}", request);
				return new EmbeddingResponse(List.of());
			}

			var metadata = generateMetadata(apiEmbeddingResponse.model(), apiEmbeddingResponse.usage());

			List<Embedding> embeddings = apiEmbeddingResponse.data()
				.stream()
				.map(e -> new Embedding(e.embedding(), e.index()))
				.toList();

			return new EmbeddingResponse(embeddings, metadata);

		});
	}

	private EmbeddingResponseMetadata generateMetadata(String model, Usage usage) {
		EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata();
		metadata.put("model", model);
		metadata.put("prompt-tokens", usage.promptTokens());
		metadata.put("completion-tokens", usage.completionTokens());
		metadata.put("total-tokens", usage.totalTokens());
		return metadata;
	}

}
