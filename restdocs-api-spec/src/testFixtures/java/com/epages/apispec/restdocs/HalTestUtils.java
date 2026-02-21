package com.epages.apispec.restdocs;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.HalJacksonModule;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.AnnotationLinkRelationProvider;
import org.springframework.hateoas.server.core.DelegatingLinkRelationProvider;
import org.springframework.util.Assert;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class HalTestUtils {

    private static final Collection<String> SUFFIXES = Set.of("Test", "Tests");

    /**
     * Returns a default HAL {@link JsonMapper} using a default {@link HalConfiguration}.
     *
     * @return
     */
    public static JsonMapper halMapper() {
        return halMapper(new HalConfiguration());
    }

    /**
     * Returns a default HAL {@link JsonMapper} using the given {@link HalConfiguration}.
     *
     * @param configuration must not be {@literal null}.
     * @return
     */
    public static JsonMapper halMapper(HalConfiguration configuration) {
        return halMapper(configuration, UnaryOperator.identity());
    }

    public static JsonMapper halMapper(HalConfiguration configuration,
                                       UnaryOperator<JsonMapper.Builder> customizer) {

        Assert.notNull(configuration, "HalConfiguration must not be null!");

        var provider = new DelegatingLinkRelationProvider(new AnnotationLinkRelationProvider(),
                HalTestUtils.DefaultLinkRelationProvider.INSTANCE);
        var instantiator = new HalJacksonModule.HalHandlerInstantiator(provider, CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY,
                configuration, new DefaultListableBeanFactory());

        UnaryOperator<JsonMapper.Builder> customizations = it -> it.addModule(new HalJacksonModule())
                .handlerInstantiator(instantiator);

        return defaultMapper(customizations.andThen(customizer));
    }

    public static JsonMapper.Builder halMapperBuilder(JsonMapper.Builder builder) {

        var provider = new DelegatingLinkRelationProvider(new AnnotationLinkRelationProvider(),
                HalTestUtils.DefaultLinkRelationProvider.INSTANCE);
        var instantiator = new HalJacksonModule.HalHandlerInstantiator(provider, CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY,
                new HalConfiguration(), new DefaultListableBeanFactory());

        builder.addModule(new HalJacksonModule()).handlerInstantiator(instantiator);

        return defaultMapperBuilder(builder);
    }

    public enum DefaultLinkRelationProvider implements LinkRelationProvider {

        INSTANCE;

        /*
         * (non-Javadoc)
         * @see org.springframework.hateoas.server.LinkRelationProvider#getItemResourceRelFor(java.lang.Class)
         */
        @Override
        public LinkRelation getItemResourceRelFor(Class<?> type) {
            throw new UnsupportedOperationException();
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.hateoas.server.LinkRelationProvider#getCollectionResourceRelFor(java.lang.Class)
         */
        @Override
        public LinkRelation getCollectionResourceRelFor(Class<?> type) {
            return LinkRelation.of("content");
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
         */
        @Override
        public boolean supports(LookupContext delimiter) {
            return delimiter.isCollectionRelationLookup();
        }
    }

    public static JsonMapper defaultMapper(Function<JsonMapper.Builder, JsonMapper.Builder> consumer) {

        var mapper = JsonMapper.builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build();

        return consumer.apply(mapper.rebuild()).build();
    }

    public static JsonMapper.Builder defaultMapperBuilder(JsonMapper.Builder builder) {

        builder.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        return builder;
    }

    public static ContextualMapper createMapper(Function<JsonMapper.Builder, JsonMapper.Builder> configurer) {
        return createMapper(detectCaller(), defaultMapper(configurer));
    }

    private static ContextualMapper createMapper(Class<?> context, JsonMapper mapper) {
        return new ContextualMapper(context, mapper);
    }

    private static Class<?> detectCaller() {

        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(it -> it.map(StackWalker.StackFrame::getDeclaringClass)
                        .filter(type -> SUFFIXES.stream().anyMatch(suffix -> type.getName().endsWith(suffix)))
                        .findFirst())
                .orElseThrow();
    }

    public static class ContextualMapper {

        private final Class<?> context;
        private final JsonMapper mapper;

        public ContextualMapper(Class<?> context, JsonMapper mapper) {
            this.context = context;
            this.mapper = mapper;
        }

        private JavaType createType(Class<?> type, Class<?> elementType) {

            var factory = mapper.getTypeFactory();

            return factory.constructParametricType(type, elementType);
        }

        private JavaType createType(Class<?> type, Class<?> elementType, Class<?> nested) {

            var factory = mapper.getTypeFactory();
            var genericElement = factory.constructParametricType(elementType, nested);

            return factory.constructParametricType(type, genericElement);
        }

    }
}