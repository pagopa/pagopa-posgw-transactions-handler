package it.pagopa.posgw.transactions.handler.config

import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.type.classreading.CachingMetadataReaderFactory

@Configuration
@ImportRuntimeHints(PosgwCommonDocumentsRuntimeHints::class)
class PosgwCommonReflectionConfig

// Spring AOT only registers the repositories' declared domain types: concrete session events (and
// the data reachable only through them) are resolved at runtime from _class, so the native image
// needs explicit reflection hints for them
class PosgwCommonDocumentsRuntimeHints : RuntimeHintsRegistrar {

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        val resolver = PathMatchingResourcePatternResolver(classLoader)
        val metadataReaderFactory = CachingMetadataReaderFactory(resolver)
        val resources =
            resolver.getResources("classpath*:${DOCUMENTS_PACKAGE.replace('.', '/')}/**/*.class")
        // fail the build rather than ship a native image that breaks on the first read
        check(resources.isNotEmpty()) { "No classes found in $DOCUMENTS_PACKAGE" }

        resources
            .map { metadataReaderFactory.getMetadataReader(it).classMetadata }
            .filterNot { it.isInterface }
            .forEach {
                hints.reflection().registerType(TypeReference.of(it.className), *MEMBER_CATEGORIES)
            }
    }

    companion object {
        const val DOCUMENTS_PACKAGE = "it.pagopa.posgw.common.documents"

        private val MEMBER_CATEGORIES =
            arrayOf(
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.ACCESS_DECLARED_FIELDS)
    }
}
