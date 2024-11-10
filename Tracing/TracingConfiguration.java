package com.example.demo.Tracing;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import io.opentelemetry.context.Context;

@Configuration
public class TracingConfiguration {

    @Value("${spring.application.name:spring-boot-backend}")
    private String serviceName;

    @Value("${zipkin.url:http://localhost:9411/api/v2/spans}")
    private String zipkinUrl;

    private static final TextMapGetter<HttpServletRequest> GETTER =
            new TextMapGetter<>() {
                @Override
                public Iterable<String> keys(HttpServletRequest carrier) {
                    return Collections.list(carrier.getHeaderNames());
                }

                @Override
                public String get(HttpServletRequest carrier, String key) {
                    if (carrier == null) return null;
                    return carrier.getHeader(key);
                }
            };

    @Bean
    public OpenTelemetry openTelemetry() {
        Resource serviceResource = Resource.getDefault()
                .merge(Resource.create(Attributes.builder()
                        .put(ResourceAttributes.SERVICE_NAME, serviceName)
                        .put(ResourceAttributes.SERVICE_VERSION, "1.0.0")
                        .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, "production")
                        .build()));

        ZipkinSpanExporter zipkinExporter = ZipkinSpanExporter.builder()
                .setEndpoint(zipkinUrl)
                .build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(zipkinExporter).build())
                .setResource(serviceResource)
                .setSampler(Sampler.alwaysOn())
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName);
    }

    @Bean
    public OncePerRequestFilter traceContextFilter(OpenTelemetry openTelemetry) {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                TextMapPropagator propagator = openTelemetry.getPropagators().getTextMapPropagator();
                Context parentContext = propagator.extract(Context.current(), request, GETTER);

                try (var scope = parentContext.makeCurrent()) {
                    // Log pour debug
                    System.out.println("Trace ID: " + parentContext.toString());
                    System.out.println("TraceParent header: " + request.getHeader("traceparent"));

                    filterChain.doFilter(request, response);
                }
            }
        };
    }
}
