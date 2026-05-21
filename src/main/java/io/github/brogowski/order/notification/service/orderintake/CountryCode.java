package io.github.brogowski.order.notification.service.orderintake;

record CountryCode(String value) {

  private static final String COUNTRY_CODE_PATTERN = "^[A-Z]{2}$";

  CountryCode {
    if (value == null || !value.matches(COUNTRY_CODE_PATTERN)) {
      throw new IllegalArgumentException("Country code must use ISO alpha-2 uppercase format");
    }
  }
}
