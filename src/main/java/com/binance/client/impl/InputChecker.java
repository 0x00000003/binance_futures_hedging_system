package com.binance.client.impl;

import com.binance.client.exception.BinanceApiException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class InputChecker {

  private static final String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\t";

  private static final InputChecker checkerInst;

  static {
    checkerInst = new InputChecker();
  }

  static InputChecker checker() {
    return checkerInst;
  }

  private boolean isSpecialChar(String str) {

    Pattern p = Pattern.compile(regEx);
    Matcher m = p.matcher(str);
    return m.find();
  }

  <T> InputChecker shouldNotNull(T value, String name) {
    if (value == null) {
      throw new BinanceApiException(BinanceApiException.INPUT_ERROR,BinanceApiException.INPUT_ERROR,
          "[Input] " + name + " should not be null");
    }
    return checkerInst;
  }
}
