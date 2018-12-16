package com.ulensapp.ulensapp.RegEx;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String text = ".....";
 * Price price = new Price();
 * Map<String, String> map = price.exact(text);
 * map["Tax"], map["Price"] give you what you want
 *
 * This find almost all of my examples
 * */


public class Price {

 private class Assoc {
  private String _small;
  private String _big;
  private Float _float_small;
  private Float _float_big;

  public Assoc(String a, String b) {
   _float_small = Float.parseFloat(a);
   _float_big = Float.parseFloat(b);

   int res = Float.compare(_float_small, _float_big);
   if (res > 0) {
	Float tmp = _float_small;
	_float_small = _float_big;
	_float_big = tmp;

	_small = b;
	_big = a;
	return;
   }
   _small = a;
   _big = b;
  }

  public int check() {
   Float tmp = _float_small * 6.555f;
   Float dif = Math.abs(tmp - _float_big);
   int res = Float.compare(dif, 0.2f);

   if (res <= 0)
	return 0;
   
   tmp = _float_small * 13.5f;
   dif = Math.abs(tmp - _float_big);
   res = Float.compare(dif, 0.2f);
   if (res <= 0)
	return 0;

   res = Float.compare(_float_big, tmp);
   if (res > 0)
	return 2;
   return 1;
  }

  public Map<String, String> convert() {
   Map<String, String> map = new HashMap<String, String>();
   map.put("Tax", _small);
   map.put("Price", _big);
   return map;
  }
 }

 public Map<String, String> exact(String content) {
  List<String> prices = find_prices(content);
  List<Assoc> assocs = associate(prices);
  
  if (assocs.size() == 1)
   return assocs.get(0).convert();

  for (Assoc assoc: assocs) {
   int status = assoc.check();
   if (status == 0) {
	return assoc.convert();
   }
  }
  return new Assoc("0.00", "0.00").convert();
 }

 private List<String> find_prices(String content) {
  Pattern p = Pattern.compile("(\\d{1,3})\\s*[.,]\\s*(\\d{2})");
  Matcher m = p.matcher(content);

  List<String> prices = new LinkedList<String>();
  while(m.find()) {
   String price = m.group(1) + "." + m.group(2);

   boolean first = true;
   for(String tmp:prices) {
	if (tmp.equals(price)) {
	 first = false;
	 break;
	}
   }

   if (first)
	prices.add(price);
  }
  return prices;
 }

 private List<Assoc> associate(List<String> prices) {
  List<Assoc> assocs = new LinkedList<Assoc>();

  int size = prices.size();
  for(int i = 0; i < size; i++) {
   for(int j = i + 1; j < size; j++) {
	assocs.add(new Assoc(prices.get(i), prices.get(j)));
   }
  }
  return assocs;
 }

}
