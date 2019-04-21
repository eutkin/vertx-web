package io.vertx.ext.web.validation.impl;

import io.vertx.core.Vertx;
import io.vertx.ext.json.schema.*;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.BodyProcessor;
import io.vertx.ext.web.validation.BodyProcessorException;
import io.vertx.ext.web.validation.dsl.BodyProcessorFactory;
import io.vertx.ext.web.validation.testutils.TestSchemas;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
class JsonBodyProcessorImplTest {

  SchemaRouter router;
  SchemaParser parser;

  @Mock RoutingContext mockedContext;

  @BeforeEach
  public void setUp(Vertx vertx) {
    router = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(new SchemaParserOptions(), router);
  }

  @Test
  public void testContentTypeCheck() {
    BodyProcessor processor = BodyProcessorFactory.json(TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER).create(parser);
    assertThat(processor.canProcess("application/json")).isTrue();
    assertThat(processor.canProcess("application/json; charset=utf-8")).isTrue();
    assertThat(processor.canProcess("application/superapplication+json")).isTrue();
  }

  @Test
  public void testJsonObject(VertxTestContext testContext) {
    when(mockedContext.getBody()).thenReturn(TestSchemas.VALID_OBJECT.toBuffer());

    BodyProcessor processor = BodyProcessorFactory.json(TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER).create(parser);

    processor.process(mockedContext).setHandler(testContext.succeeding(rp -> {
      testContext.verify(() -> {
        assertThat(rp.isJsonObject()).isTrue();
        assertThat(rp.getJsonObject())
          .isEqualTo(
            TestSchemas.VALID_OBJECT
          );
      });
      testContext.completeNow();
    }));
  }

  @Test
  public void testInvalidJsonObject(VertxTestContext testContext) {
    when(mockedContext.getBody()).thenReturn(TestSchemas.INVALID_OBJECT.toBuffer());

    BodyProcessor processor = BodyProcessorFactory.json(TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER).create(parser);

    processor.process(mockedContext).setHandler(testContext.failing(err -> {
      testContext.verify(() -> {
        assertThat(err)
          .isInstanceOf(BodyProcessorException.class)
          .hasFieldOrPropertyWithValue("contentType", "application/json")
          .hasCauseInstanceOf(ValidationException.class);
      });
      testContext.completeNow();
    }));
  }

  @Test
  public void testJsonArray(VertxTestContext testContext) {
    when(mockedContext.getBody()).thenReturn(TestSchemas.VALID_ARRAY.toBuffer());

    BodyProcessor processor = BodyProcessorFactory.json(TestSchemas.SAMPLE_ARRAY_SCHEMA_BUILDER).create(parser);

    processor.process(mockedContext).setHandler(testContext.succeeding(rp -> {
      testContext.verify(() -> {
        assertThat(rp.isJsonArray()).isTrue();
        assertThat(rp.getJsonArray())
          .isEqualTo(
            TestSchemas.VALID_ARRAY
          );
      });
      testContext.completeNow();
    }));
  }

  @Test
  public void testInvalidJsonArray(VertxTestContext testContext) {
    when(mockedContext.getBody()).thenReturn(TestSchemas.INVALID_ARRAY.toBuffer());

    BodyProcessor processor = BodyProcessorFactory.json(TestSchemas.SAMPLE_ARRAY_SCHEMA_BUILDER).create(parser);

    processor.process(mockedContext).setHandler(testContext.failing(err -> {
      testContext.verify(() -> {
        assertThat(err)
          .isInstanceOf(BodyProcessorException.class)
          .hasFieldOrPropertyWithValue("contentType", "application/json")
          .hasCauseInstanceOf(ValidationException.class);
      });
      testContext.completeNow();
    }));
  }
}