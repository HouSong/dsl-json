package com.dslplatform.json;

import com.dslplatform.json.models.*;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ValidationTest extends AbstractAnnotationProcessorTest {

	protected Collection<Processor> getProcessors() {
		return Collections.<Processor>singletonList(new CompiledJsonProcessor());
	}

	@Test
	public void testEmptyValidClass() {
		assertCompilationSuccessful(compileTestCase(ValidCtor.class));
	}

	@Test
	public void testMissingEmptyCtor() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 5, compileTestCase(MissingEmptyCtor.class));
	}

	@Test
	public void testValidPropertyType() {
		assertCompilationSuccessful(compileTestCase(ValidType.class));
	}

	@Test
	public void testUnsupportedPropertyType() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 9, compileTestCase(InvalidType.class));
	}

	@Test
	public void testReferencePropertyType() {
		assertCompilationSuccessful(compileTestCase(ReferenceType.class));
	}

	@Test
	public void testReferenceListPropertyType() {
		assertCompilationSuccessful(compileTestCase(ReferenceListType.class));
	}

	@Test
	public void testEnum() {
		assertCompilationSuccessful(compileTestCase(SimpleEnum.class));
	}

	@Test
	public void canIgnoreUnsupportedProperty() {
		assertCompilationSuccessful(compileTestCase(IgnoredProperty.class));
	}

	@Test
	public void coverAllTypes() {
		assertCompilationSuccessful(compileTestCase(AllTypes.class));
	}

	@Test
	public void checkIgnore() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(IgnoredProperty.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		Assert.assertEquals("Note: module json {\n  struct struct0 {\n"
						+ "    external name Java 'com.dslplatform.json.models.IgnoredProperty';\n  }\n}",
				note.getMessage(Locale.ENGLISH));
	}

	@Test
	public void checkAlias() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(PropertyAlias.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("int num { serialization name 'y'; }"));
		Assert.assertTrue(dsl.contains("string? prop { serialization name 'x'; }"));
		Assert.assertTrue(dsl.contains("external name Java 'com.dslplatform.json.models.PropertyAlias';"));
	}

	@Test
	public void checkNonNull() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(NonNullableReferenceProperty.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("string prop;"));
		Assert.assertTrue(dsl.contains("Set<uuid?> uuid;"));
		Assert.assertTrue(dsl.contains("json.struct0?[] ref;") || dsl.contains("json.struct1?[] ref;") || dsl.contains("json.struct2?[] ref;"));
		Assert.assertTrue(dsl.contains("json.struct0 enum;") || dsl.contains("json.struct1 enum;") || dsl.contains("json.struct2 enum;"));
	}
}
