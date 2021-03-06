/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableValidator;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.light.LightMetaBean;

import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * A set of market data which combines the data from two other {@link MarketData} instances.
 * <p>
 * When an item of data is requested the underlying sets of market data are checked in order.
 * If the item is present in the first set of data it is returned. If the item is not found
 * it is looked up in the second set of data.
 */
@BeanDefinition(style = "light", constructorScope = "package")
final class CombinedMarketData
    implements MarketData, ImmutableBean, Serializable {

  /**
   * The first set of market data.
   */
  @PropertyDefinition(validate = "notNull")
  private final MarketData underlying1;
  /**
   * The second set of market data.
   */
  @PropertyDefinition(validate = "notNull")
  private final MarketData underlying2;

  //-------------------------------------------------------------------------
  @ImmutableValidator
  private void validate() {
    if (!underlying1.getValuationDate().equals(underlying2.getValuationDate())) {
      throw new IllegalArgumentException("Unable to combine market data instances with different valuation dates");
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDate getValuationDate() {
    return underlying1.getValuationDate();
  }

  @Override
  public boolean containsValue(MarketDataId<?> id) {
    return underlying1.containsValue(id) || underlying2.containsValue(id);
  }

  @Override
  public <T> T getValue(MarketDataId<T> id) {
    Optional<T> value1 = underlying1.findValue(id);
    return value1.isPresent() ? value1.get() : underlying2.getValue(id);
  }

  @Override
  public <T> Optional<T> findValue(MarketDataId<T> id) {
    Optional<T> value1 = underlying1.findValue(id);
    return value1.isPresent() ? value1 : underlying2.findValue(id);
  }

  @Override
  public Set<MarketDataId<?>> getIds() {
    return ImmutableSet.<MarketDataId<?>>builder()
        .addAll(underlying1.getIds())
        .addAll(underlying2.getIds())
        .build();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Set<MarketDataId<T>> findIds(MarketDataName<T> name) {
    return ImmutableSet.<MarketDataId<T>>builder()
        .addAll(underlying1.findIds(name))
        .addAll(underlying2.findIds(name))
        .build();
  }

  @Override
  public LocalDateDoubleTimeSeries getTimeSeries(ObservableId id) {
    LocalDateDoubleTimeSeries timeSeries = underlying1.getTimeSeries(id);
    return !timeSeries.isEmpty() ? timeSeries : underlying2.getTimeSeries(id);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CombinedMarketData}.
   */
  private static MetaBean META_BEAN = LightMetaBean.of(CombinedMarketData.class);

  /**
   * The meta-bean for {@code CombinedMarketData}.
   * @return the meta-bean, not null
   */
  public static MetaBean meta() {
    return META_BEAN;
  }

  static {
    JodaBeanUtils.registerMetaBean(META_BEAN);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   * @param underlying1  the value of the property, not null
   * @param underlying2  the value of the property, not null
   */
  CombinedMarketData(
      MarketData underlying1,
      MarketData underlying2) {
    JodaBeanUtils.notNull(underlying1, "underlying1");
    JodaBeanUtils.notNull(underlying2, "underlying2");
    this.underlying1 = underlying1;
    this.underlying2 = underlying2;
    validate();
  }

  @Override
  public MetaBean metaBean() {
    return META_BEAN;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the first set of market data.
   * @return the value of the property, not null
   */
  public MarketData getUnderlying1() {
    return underlying1;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the second set of market data.
   * @return the value of the property, not null
   */
  public MarketData getUnderlying2() {
    return underlying2;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CombinedMarketData other = (CombinedMarketData) obj;
      return JodaBeanUtils.equal(underlying1, other.underlying1) &&
          JodaBeanUtils.equal(underlying2, other.underlying2);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(underlying1);
    hash = hash * 31 + JodaBeanUtils.hashCode(underlying2);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("CombinedMarketData{");
    buf.append("underlying1").append('=').append(underlying1).append(',').append(' ');
    buf.append("underlying2").append('=').append(JodaBeanUtils.toString(underlying2));
    buf.append('}');
    return buf.toString();
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
