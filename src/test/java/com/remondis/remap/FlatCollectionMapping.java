package com.remondis.remap;

import static com.remondis.remap.AssertMapping.DIFFERENT_NULL_STRATEGY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import com.remondis.remap.flatCollectionMapping.Destination;
import com.remondis.remap.flatCollectionMapping.Id;
import com.remondis.remap.flatCollectionMapping.Source;

public class FlatCollectionMapping {

  @Test
  public void shouldMapCollectionByFunction() {
    Mapper<Source, Destination> mapper = Mapping.from(Source.class)
        .to(Destination.class)
        .replaceCollection(Source::getIds, Destination::getIds)
        .with(idBuilder())
        .mapper();

    Source source = Source.builder()
        .ids(Arrays.asList(1L, 2L, 3L))
        .build();
    Destination map = mapper.map(source);
    List<Id> expected = Arrays.asList(idBuilder().apply(1L), idBuilder().apply(2L), idBuilder().apply(3L));
    List<Id> actual = map.getIds();
    assertEquals(expected, actual);

    // Assert the mapping
    AssertMapping.of(mapper)
        .expectReplaceCollection(Source::getIds, Destination::getIds)
        .andTest(idBuilder())
        .ensure();
  }

  @Test
  public void shouldDetectIllegalArguments() {
    assertThatThrownBy(() -> {
      Mapping.from(Source.class)
          .to(Destination.class)
          .replaceCollection(null, Destination::getIds);
    }).isInstanceOf(IllegalArgumentException.class)
        .hasNoCause();

    assertThatThrownBy(() -> {
      Mapping.from(Source.class)
          .to(Destination.class)
          .replaceCollection(Source::getIds, null);
    }).isInstanceOf(IllegalArgumentException.class)
        .hasNoCause();

    assertThatThrownBy(() -> {
      Mapping.from(Source.class)
          .to(Destination.class)
          .replaceCollection(Source::getIds, Destination::getIds)
          .with(null);
    }).isInstanceOf(IllegalArgumentException.class)
        .hasNoCause();
  }

  @Test
  public void shouldNotSkipNullItems() {
    Mapper<Source, Destination> mapper = Mapping.from(Source.class)
        .to(Destination.class)
        .replaceCollection(Source::getIds, Destination::getIds)
        .with(idBuilder())
        .mapper();

    Source source = Source.builder()
        .ids(Arrays.asList(1L, null, 2L, null, 3L))
        .build();
    Destination map = mapper.map(source);

    List<Id> expected = Arrays.asList(idBuilder().apply(1L), idBuilder().apply(null), idBuilder().apply(2L),
        idBuilder().apply(null), idBuilder().apply(3L));
    List<Id> actual = map.getIds();
    assertEquals(expected, actual);

    // Assert the mapping
    AssertMapping.of(mapper)
        .expectReplaceCollection(Source::getIds, Destination::getIds)
        .andTest(idBuilder())
        .ensure();
  }

  @Test
  public void shouldSkipNullItems() {
    Mapper<Source, Destination> mapper = Mapping.from(Source.class)
        .to(Destination.class)
        .replaceCollection(Source::getIds, Destination::getIds)
        .withSkipWhenNull(idBuilder())
        .mapper();

    Source source = Source.builder()
        .ids(Arrays.asList(1L, null, 2L, null, 3L))
        .build();
    Destination map = mapper.map(source);

    List<Id> expected = Arrays.asList(idBuilder().apply(1L), idBuilder().apply(2L), idBuilder().apply(3L));
    List<Id> actual = map.getIds();
    assertEquals(expected, actual);

    // Assert the mapping
    AssertMapping.of(mapper)
        .expectReplaceCollection(Source::getIds, Destination::getIds)
        .andSkipWhenNull()
        .ensure();
  }

  @Test
  public void nullCollection() {
    Mapper<Source, Destination> mapper = Mapping.from(Source.class)
        .to(Destination.class)
        .replaceCollection(Source::getIds, Destination::getIds)
        .with(idBuilder())
        .mapper();

    Source source = Source.builder()
        .ids(null)
        .build();
    Destination map = mapper.map(source);
    assertNull(map.getIds());
  }

  @Test
  public void shouldDetectDifferentNullStrategy() {
    Mapper<Source, Destination> mapper = Mapping.from(Source.class)
        .to(Destination.class)
        .replaceCollection(Source::getIds, Destination::getIds)
        .with(idBuilder())
        .mapper();

    assertThatThrownBy(() -> {
      AssertMapping.of(mapper)
          .expectReplaceCollection(Source::getIds, Destination::getIds)
          .andSkipWhenNull()
          .ensure();
    }).isInstanceOf(AssertionError.class)
        .hasMessageContaining(DIFFERENT_NULL_STRATEGY)
        .hasNoCause();
  }

  public static Function<Long, Id> idBuilder() {
    return id -> Id.builder()
        .id(id)
        .build();
  }

}
