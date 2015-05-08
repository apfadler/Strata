/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.impl.rate;

import static com.opengamma.strata.basics.index.PriceIndices.GB_RPIX;
import static com.opengamma.strata.collect.TestHelper.date;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.time.LocalDate;
import java.time.YearMonth;

import org.testng.annotations.Test;

import com.opengamma.strata.finance.rate.InflationInterpolatedRateObservation;
import com.opengamma.strata.pricer.rate.RatesProvider;
import com.opengamma.strata.pricer.sensitivity.InflationRateSensitivity;
import com.opengamma.strata.pricer.sensitivity.PointSensitivityBuilder;

/**
 * Test.
 */
@Test
public class ForwardInflationInterpolatedRateObservationFnTest {
  private static final LocalDate DUMMY_ACCRUAL_START_DATE = date(2015, 1, 4); // Accrual dates irrelevant for the rate
  private static final LocalDate DUMMY_ACCRUAL_END_DATE = date(2016, 1, 5); // Accrual dates irrelevant for the rate
  private static final YearMonth REFERENCE_START_MONTH = YearMonth.of(2014, 10);
  private static final YearMonth REFERENCE_END_MONTH = YearMonth.of(2015, 10);
  private static final double RATE_START = 317.0;
  private static final double RATE_START_INTERP = 325.0;
  private static final double RATE_END = 344.0;
  private static final double RATE_END_INTERP = 349.0;
  private static final double WEIGHT = 0.5;

  private static final double EPS = 1.0e-12;
  private static final double EPS_FD = 1.0e-4;
  //TODO add test curve parameter sensitivity with RatesFiniteDifferenceSensitivityCalculator

  public void test_rate() {
    RatesProvider mockProv = mock(RatesProvider.class);
    when(mockProv.inflationIndexRate(GB_RPIX, REFERENCE_START_MONTH)).thenReturn(RATE_START);
    when(mockProv.inflationIndexRate(GB_RPIX, REFERENCE_END_MONTH)).thenReturn(RATE_END);
    when(mockProv.inflationIndexRate(GB_RPIX, REFERENCE_START_MONTH.plusMonths(1))).thenReturn(RATE_START_INTERP);
    when(mockProv.inflationIndexRate(GB_RPIX, REFERENCE_END_MONTH.plusMonths(1))).thenReturn(RATE_END_INTERP);
    InflationInterpolatedRateObservation ro =
        InflationInterpolatedRateObservation.of(GB_RPIX, REFERENCE_START_MONTH, REFERENCE_END_MONTH, WEIGHT);
    ForwardInflationInterpolatedRateObservationFn obsFn = ForwardInflationInterpolatedRateObservationFn.DEFAULT;
    double rateExpected = (WEIGHT * RATE_END + (1.0 - WEIGHT) * RATE_END_INTERP) /
        (WEIGHT * RATE_START + (1.0 - WEIGHT) * RATE_START_INTERP) - 1.0;
    assertEquals(obsFn.rate(ro, DUMMY_ACCRUAL_START_DATE, DUMMY_ACCRUAL_END_DATE, mockProv), rateExpected, EPS);
  }

  public void test_rateSensitivity() {
    InflationInterpolatedRateObservation ro = InflationInterpolatedRateObservation.of(
        GB_RPIX, REFERENCE_START_MONTH, REFERENCE_END_MONTH, WEIGHT);
    ForwardInflationInterpolatedRateObservationFn obsFn = ForwardInflationInterpolatedRateObservationFn.DEFAULT;

    YearMonth refStartMonthInterp = REFERENCE_START_MONTH.plusMonths(1);
    YearMonth refEndMonthInterp = REFERENCE_END_MONTH.plusMonths(1);
    RatesProvider[] mockProvs = new RatesProvider[8];
    setRateProvider(mockProvs);
    when(mockProvs[0].inflationIndexRate(GB_RPIX, REFERENCE_START_MONTH)).thenReturn(RATE_START + EPS_FD);
    when(mockProvs[1].inflationIndexRate(GB_RPIX, REFERENCE_START_MONTH)).thenReturn(RATE_START - EPS_FD);
    when(mockProvs[2].inflationIndexRate(GB_RPIX, refStartMonthInterp)).thenReturn(RATE_START_INTERP + EPS_FD);
    when(mockProvs[3].inflationIndexRate(GB_RPIX, refStartMonthInterp)).thenReturn(RATE_START_INTERP - EPS_FD);
    when(mockProvs[4].inflationIndexRate(GB_RPIX, REFERENCE_END_MONTH)).thenReturn(RATE_END + EPS_FD);
    when(mockProvs[5].inflationIndexRate(GB_RPIX, REFERENCE_END_MONTH)).thenReturn(RATE_END - EPS_FD);
    when(mockProvs[6].inflationIndexRate(GB_RPIX, refEndMonthInterp)).thenReturn(RATE_END_INTERP + EPS_FD);
    when(mockProvs[7].inflationIndexRate(GB_RPIX, refEndMonthInterp)).thenReturn(RATE_END_INTERP - EPS_FD);
    YearMonth[] months = new YearMonth[] {
      REFERENCE_START_MONTH, refStartMonthInterp, REFERENCE_END_MONTH, refEndMonthInterp };
    PointSensitivityBuilder sensiExpected = PointSensitivityBuilder.none();
    for (int i = 0; i < 4; ++i) {
      double rateUp = obsFn.rate(ro, DUMMY_ACCRUAL_START_DATE, DUMMY_ACCRUAL_END_DATE, mockProvs[2 * i]);
      double rateDw = obsFn.rate(ro, DUMMY_ACCRUAL_START_DATE, DUMMY_ACCRUAL_END_DATE, mockProvs[2 * i + 1]);
      double sensiVal = 0.5 * (rateUp - rateDw) / EPS_FD;
      PointSensitivityBuilder sensi = InflationRateSensitivity.of(GB_RPIX, months[i], sensiVal);
      sensiExpected = sensiExpected.combinedWith(sensi);
    }

    RatesProvider[] mockProv = new RatesProvider[1];
    setRateProvider(mockProv);
    when(mockProv[0].inflationIndexRateSensitivity(GB_RPIX, REFERENCE_START_MONTH))
        .thenReturn(InflationRateSensitivity.of(GB_RPIX, REFERENCE_START_MONTH, 1.0d));
    when(mockProv[0].inflationIndexRateSensitivity(GB_RPIX, refStartMonthInterp))
        .thenReturn(InflationRateSensitivity.of(GB_RPIX, refStartMonthInterp, 1.0d));
    when(mockProv[0].inflationIndexRateSensitivity(GB_RPIX, REFERENCE_END_MONTH))
        .thenReturn(InflationRateSensitivity.of(GB_RPIX, REFERENCE_END_MONTH, 1.0d));
    when(mockProv[0].inflationIndexRateSensitivity(GB_RPIX, refEndMonthInterp))
        .thenReturn(InflationRateSensitivity.of(GB_RPIX, refEndMonthInterp, 1.0d));
    PointSensitivityBuilder sensiComputed =
        obsFn.rateSensitivity(ro, DUMMY_ACCRUAL_START_DATE, DUMMY_ACCRUAL_END_DATE, mockProv[0]);

    assertTrue(sensiComputed.build().normalized().equalWithTolerance(sensiExpected.build().normalized(), EPS_FD));
  }

  private void setRateProvider(RatesProvider[] provs) {
    int n = provs.length;
    for (int i = 0; i < n; ++i) {
      provs[i] = mock(RatesProvider.class);
      when(provs[i].inflationIndexRate(GB_RPIX, REFERENCE_START_MONTH)).thenReturn(RATE_START);
      when(provs[i].inflationIndexRate(GB_RPIX, REFERENCE_END_MONTH)).thenReturn(RATE_END);
      when(provs[i].inflationIndexRate(GB_RPIX, REFERENCE_START_MONTH.plusMonths(1))).thenReturn(RATE_START_INTERP);
      when(provs[i].inflationIndexRate(GB_RPIX, REFERENCE_END_MONTH.plusMonths(1))).thenReturn(RATE_END_INTERP);
    }
  }
}
