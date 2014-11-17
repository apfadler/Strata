/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.collect;

import static com.opengamma.collect.TestHelper.assertUtilityClass;
import static org.testng.Assert.assertEquals;

import java.util.Objects;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test Messages.
 */
@Test
public class MessagesTest {

  @DataProvider(name = "formatMessage")
  Object[][] data_formatMessage() {
    return new Object[][] {
        {null, null, "", ""},
        {null, new Object[] {}, "", ""},
        {"", new Object[] {}, "", ""},
        {"{}", null, "{}", ""},
        {"{}", new Object[] {}, "{}", ""},
        {"{}", new Object[] {67}, "67", ""},
        {"{}", new Object[] {67, 78}, "67", " - [78]"},
        {"{}", new Object[] {67, 78, 89}, "67", " - [78, 89]"},
        {"{}", new Object[] {67, 78, 89, 90}, "67", " - [78, 89, 90]"},
        {"{}{}", null, "{}{}", ""},
        {"{}{}", new Object[] {}, "{}{}", ""},
        {"{}{}", new Object[] {67}, "67{}", ""},
        {"{}{}", new Object[] {67, 78}, "6778", ""},
        {"{}{}", new Object[] {67, 78, 89}, "6778", " - [89]"},
        {"{}{}", new Object[] {67, 78, 89, 90}, "6778", " - [89, 90]"},
        {"{} and {}", null, "{} and {}", ""},
        {"{} and {}", new Object[] {}, "{} and {}", ""},
        {"{} and {}", new Object[] {67}, "67 and {}", ""},
        {"{} and {}", new Object[] {67, 78}, "67 and 78", ""},
        {"{} and {}", new Object[] {67, 78, 89}, "67 and 78", " - [89]"},
        {"{} and {}", new Object[] {67, 78, 89, 90}, "67 and 78", " - [89, 90]"},
        {"{}, {} and {}", new Object[] {}, "{}, {} and {}", ""},
        {"{}, {} and {}", new Object[] {67}, "67, {} and {}", ""},
        {"{}, {} and {}", new Object[] {67, 78}, "67, 78 and {}", ""},
        {"{}, {} and {}", new Object[] {67, 78, 89}, "67, 78 and 89", ""},
        {"{}, {} and {}", new Object[] {67, 78, 89, 90}, "67, 78 and 89", " - [90]"},
        {"ABC", new Object[] {}, "ABC", ""},
        {"Message {}, {}, {}", new Object[] {"A", 2, 3.}, "Message A, 2, 3.0", ""},
        {"Message {}, {} blah", new Object[] {"A", 2, 3.}, "Message A, 2 blah", " - [3.0]"},
        {"Message {}, {}", new Object[] {"A", 2, 3., true}, "Message A, 2", " - [3.0, true]"},
        {"Message {}, {}, {}, {} blah", new Object[] {"A", 2, 3.}, "Message A, 2, 3.0, {} blah", ""},
    };
  }

  @Test(dataProvider = "formatMessage")
  public void test_formatMessage(String template, Object[] args, String expMain, String expExcess) {
    assertEquals(Messages.format(template, args), expMain + expExcess);
  }

  @Test(dataProvider = "formatMessage")
  public void test_formatMessage_prefix(String template, Object[] args, String expMain, String expExcess) {
    assertEquals(Messages.format("::" + Objects.toString(template, ""), args), "::" + expMain + expExcess);
  }

  @Test(dataProvider = "formatMessage")
  public void test_formatMessage_suffix(String template, Object[] args, String expMain, String expExcess) {
    assertEquals(Messages.format(Objects.toString(template, "") + "@@", args), expMain + "@@" + expExcess);
  }

  @Test(dataProvider = "formatMessage")
  public void test_formatMessage_prefixSuffix(String template, Object[] args, String expMain, String expExcess) {
    assertEquals(Messages.format("::" + Objects.toString(template, "") + "@@", args), "::" + expMain + "@@" + expExcess);
  }

  //-------------------------------------------------------------------------
  public void test_validUtilityClass() {
    assertUtilityClass(Messages.class);
  }

}
