package com.ulensapp.ulensapp.RegEx;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExactBill {
 private Map<String, String> _result;
 private String _currentData;
 private Pattern _pPayment, _pDate, _pRatio, _pPrice, _pName, _pFis, _pNo;


 public ExactBill() {
  // init all regex
  _pPayment = Pattern.compile("NAK");
  _pDate = Pattern.compile("(\\d{2})[^\\d\\sa-za-z]+(\\d{2})[^\\d\\sa-za-z]+(\\d{4})");
  _pRatio = Pattern.compile("%([0-1]?8)");
  _pPrice = Pattern.compile("[*xx](\\d{1,3})\\s*[.,]\\s*(\\d{2})");
  _pName = Pattern.compile("\\w+");
  _pFis = Pattern.compile("(?i)fi");
  _pNo = Pattern.compile("[^\\d/-](\\d{4})");



  _result = new HashMap<String, String>();
 }

 public Map<String, String> exactAll(String content) {
  _result.clear();

  if(content==null)
    Log.e("ERROR","content null");
  else
    Log.e("ERROR", "content null değil");

  if (content.length() < 10)
   return _result;
  _currentData = content;

  exactName();
  exactDate();
  exactPrice();
  exactPayment();
  exactNo();
  exactRatio();
  return _result;
}

 private void exactNo() {
  Matcher m = _pFis.matcher(_currentData);

  int pos;
  if (m.find())
   pos = m.start();
  else
   pos = _currentData.length() / 4;

  m = _pNo.matcher(_currentData);

  int gap = 214124243;
  String no = new String("");
  while(m.find()) {
   int dif = Math.abs(m.start() - pos);
   if ( dif < gap) {
    gap = dif;
    no = m.group(1);
   }
  }

  if (no.equals(""))
   no = "0000";
  _result.put("InvoiceNo", no);
 }


 private void exactPayment() {
  Matcher m = _pPayment.matcher(_currentData);

  if (m.find())
   _result.put("Payment", "NAKIT");
  else
   _result.put("Payment", "KREDI KARTI");
 }

 private void exactDate() {
  Matcher m = _pDate.matcher(_currentData);
  
  if (m.find()) {
   String date = m.group(1) + "-" + m.group(2) + "-" + m.group(3);
   _result.put("Date", date);
  }
 }

 private void exactRatio() {
  Matcher m = _pRatio.matcher(_currentData);
   
  if (m.find()) {
    String ratio = "%" + Integer.valueOf(m.group(1)).toString();
    _result.put("Ratio", ratio);
  }
 }

 private List<String> findPrice() {
  Matcher m = _pPrice.matcher(_currentData);
  List<String> result = new LinkedList<String>();
  while(m.find()) {
   String re = m.group(1) + "." + m.group(2);
   int first = 1;
   for(String price:result) {
	if (re.equals(price)) {
	 first = 0;
	 break;
	}
   }
   if (first == 1)
	result.add(re);
  }
  return result;
 }
 
 private void exactPrice() {
  List<String> prices = findPrice();

  String price;
  for(int i = 0; i < prices.size(); i++) {
   price = prices.get(i);
   if(i == 0)
	_result.put("Tax", price);
   else if(i == 1)
	_result.put("Price", price);
  }
 }

 private void exactName() {
  Matcher m = _pName.matcher(_currentData);
  String result = new String("");

  int i = 0;
  while(m.find()) {
   result += m.group();

   if (i == 2)
	break;
   result += " ";
   i += 1;
  }

  if (i != 0)
   _result.put("Name", result);
 }
 
}
