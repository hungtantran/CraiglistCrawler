import json
import numpy as np
import os
import re
import quantities as pq
import sys
import time
import unicodedata
import urllib2

from bs4 import BeautifulSoup
from HTMLParser import HTMLParser
import MySQLdb

positiveDir = "./crawled_files/positive"
db = MySQLdb.connect(host="powdb.clfpwrv3fbfn.us-west-2.rds.amazonaws.com",
                     port=4200,user="cedro",
                     passwd="password",
                     db="powdb")
quanitityPatternsEnglishFile = "/home/ec2-user/pow/pow/quantitypatterns_english.txt"
quanitityPatternsMetricFile = "/home/ec2-user/pow/pow/quantitypatterns_english.txt"
moneyPatternsFile = "/home/ec2-user/pow/pow/moneypatterns.txt"

class MLStripper(HTMLParser):
  def __init__(self):
      self.reset()
      self.fed = []
  def handle_data(self, d):
      self.fed.append(d)
  def get_data(self):
      return ''.join(self.fed)

def strip_tags(html):
  s = MLStripper()
  s.feed(html)
  return s.get_data()

def get_quantity_mapping():
  quantityMapping = {}

  quantityPatterns_english = open(quanitityPatternsEnglishFile, "r").readlines()
  for line in quantityPatterns_english:
    key = line.split(',')[0]
    value = float(line.split(',')[1])
    quantityMapping[key] = value

  quantityPatterns_metric = open(quanitityPatternsMetricFile, "r").readlines()
  for line in quantityPatterns_metric:
    key = line.split(',')[0]
    value = float(line.split(',')[1])
    quantityMapping[key] = value

  return quantityMapping

def extract_locations(text):
  latitudePattern = re.compile("data-latitude=\"(-?\d+?[0-9]*\.?[0-9]+)\"")
  longitudePattern = re.compile("data-longitude=\"(-?\d+?[0-9]*\.?[0-9]+)\"")

  latMatches = latitudePattern.findall(text)
  lonMatches = longitudePattern.findall(text)
  if (len(latMatches) != len(lonMatches) or len(latMatches) != 1):
    return [None, None]

  return [latMatches[0], lonMatches[0]]

def extract_prices(text):
  # remove percentages, these aren't legit prices
  compiledQuantityPattern = re.compile("\d+?[0-9]*\.?[0-9]+%")
  text = compiledQuantityPattern.sub('', text)

  moneyPatterns = open(moneyPatternsFile, "r").readlines()
  moneyPatterns = [x.strip() for x in moneyPatterns]
  moneyPattern = "|".join(moneyPatterns)
  regex = re.compile(moneyPattern)
  prices = regex.findall(text)

  cleanPrices = []
  for price in prices:
    if "/" not in price:
      cleanPrices.append(price)
  prices = cleanPrices

  prices = [float(x.split(' ')[0].strip().replace('$','')) for x in prices]
  return prices

def extract_quantities_with_pattern(text, pattern):
  regex = re.compile(pattern)
  quantities = regex.findall(text)
  return quantities

def map_quantityPattern_to_quantity(quantityMapping, quantities):
  mappedQuantities = []
  for q in quantities:
    if q in quantityMapping:
      mappedQuantities.append(quantityMapping[q])
      continue
    
    if '/' in q:
      non_decimal = re.compile(r'[^\d.]+')
      fraction = non_decimal.sub(' ', q).split()

      numerator = float(fraction[0])
      denominator = float(fraction[1])
      if denominator==0: continue
      result = numerator / denominator
      mappedQuantities.append(result)
      continue

    non_decimal = re.compile(r'[^\d.]+')
    decimal = float(non_decimal.sub('', q))
    # decimal = float(q.split(" ")[0])
    mappedQuantities.append(decimal)

  return mappedQuantities

def extract_quantities(text):
  quantityMapping = get_quantity_mapping() 

  quantityPatterns = open(quanitityPatternsEnglishFile, "r").readlines()
  quantityPattern = "|".join([x.split(',')[0].strip() for x in quantityPatterns])
  quantities_english = extract_quantities_with_pattern(text, quantityPattern)
  mapped_quantities_english = map_quantityPattern_to_quantity(quantityMapping, quantities_english)
  if len(mapped_quantities_english) > 0:
    quantities_english = mapped_quantities_english * pq.oz

  quantityPatterns = open(quanitityPatternsMetricFile, "r").readlines()
  quantityPattern = "|".join([x.split(',')[0].strip() for x in quantityPatterns])
  quantities_metric = extract_quantities_with_pattern(text, quantityPattern)
  mapped_quantities_metric = map_quantityPattern_to_quantity(quantityMapping, quantities_metric)
  if len(mapped_quantities_metric) > 0:
    quantities_metric = pq.Quantity(mapped_quantities_metric, "gram")

  return quantities_metric, quantities_english


def write_to_db(rowId, metricQuantities, englishQuantities, prices, locations):
  query = "UPDATE rawhtml SET alt_quantities=\"%s\", alt_prices=\"%s\" WHERE id=%s;" % (str(metricQuantities)+str(englishQuantities), str(prices), str(rowId))
  # print query
  cursor = db.cursor()
  cursor.execute(query)
  db.commit()

  if (locations[0] != None):
    query = "UPDATE posting_location SET latitude=\"%s\", longitude=\"%s\" WHERE location_fk=%s" % (str(locations[0]), str(locations[1]), rowId);

    # print query
    cursor = db.cursor()
    cursor.execute(query)

    db.commit()

def index(rowId, htmlFile):
  html_doc = htmlFile

  try:
   soup = BeautifulSoup(html_doc)
   title = soup.title.string
   title = str(unicodedata.normalize('NFKD', title).encode('ascii', 'ignore'))
   h2 = soup.h2.text
   h2 = str(unicodedata.normalize('NFKD', h2).encode('ascii', 'ignore'))
   posting = soup.find(id="postingbody")
   postingStr = ""
  except:
   print "posting " + str(rowId) + " has invalid HTML"
   return
  try:
   postingStr = str(posting)
  except (RuntimeError):
   print "posting " + str(rowId) + " was too large"
   return

  text = strip_tags(title + h2 + postingStr)
  # text = htmlFile
  prices = extract_prices(text)
  prices = [price for price in prices if price!=420 and price!=215 and price!=502] # and price>9
  
  extracted_quantities = extract_quantities(text)

  extracted_locations = extract_locations(htmlFile)

  # print "metric quantities:", extracted_quantities[0]
  # print "english quantities:", extracted_quantities[1]
  # print "location:", extracted_locations
  # print "prices:", prices, '\n'
  
  if (len(extracted_quantities[0])==0 and len(extracted_quantities[1])==0) or len(prices)==0:
    print "could not find prices or quantities for rowid:" + str(rowId)
    return

  write_to_db(rowId, extracted_quantities[0], extracted_quantities[1], prices, extracted_locations)

def index_from_db():
  cursor = db.cursor()
  cursor.execute("SELECT id,html FROM rawhtml WHERE (alt_prices IS NULL OR alt_quantities IS NULL) AND dateCrawled >= DATE_ADD(NOW(), INTERVAL -1 DAY)  ")
  print "here"
  for row in cursor.fetchall():
    rowId = row[0]
    htmltext = row[1].replace("scr\"+\"ipt", "script")
    index(rowId, htmltext)


def index_from_file(fileName):
  filePath = os.path.join(positiveDir, fileName)
  if os.path.isfile(filePath):
    f = open(filePath, 'r')
    fileText = f.read()
    print filePath
    index(0, fileText)
  else:
    print "not valid file"

def index_from_files():
  files = os.listdir(positiveDir)
  for fileName in files:
    index_from_file(fileName)

def main():
  if len(sys.argv) > 1:
    index_from_file(sys.argv[1])
  else:
    index_from_db()
    # index_from_files()  


if __name__ == "__main__":
  main()
