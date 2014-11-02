import numpy as np
import os
import re
import quantities as pq
import sys
import unicodedata

from bs4 import BeautifulSoup
from HTMLParser import HTMLParser
import MySQLdb

positiveDir = "./crawled_files/positive"
db = MySQLdb.connect(host="pow-db.clfpwrv3fbfn.us-west-2.rds.amazonaws.com",
                     port=4200,user="cedro",
                     passwd="password",
                     db="powdb")

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

  quantityPatterns_english = open("quantitypatterns_english.txt", "r").readlines()
  for line in quantityPatterns_english:
    key = line.split(',')[0]
    value = float(line.split(',')[1])
    quantityMapping[key] = value

  quantityPatterns_metric = open("quantitypatterns_metric.txt", "r").readlines()
  for line in quantityPatterns_metric:
    key = line.split(',')[0]
    value = float(line.split(',')[1])
    quantityMapping[key] = value

  return quantityMapping

def extract_prices(text):
  # remove all quantity matches from text here
  englishQuantityPatterns = open("quantitypatterns_english.txt", "r").readlines()
  metricQuantityPatterns = open("quantitypatterns_metric.txt", "r").readlines()
  
  quantityPatterns = englishQuantityPatterns + metricQuantityPatterns
  quantityPattern = "|".join([x.split(',')[0].strip() for x in quantityPatterns])
  compiledQuantityPattern = re.compile(quantityPattern)
  text = compiledQuantityPattern.sub('', text)

  moneyPatterns = open("moneypatterns.txt", "r").readlines()
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
  # quantityPatterns = open(quantitypatterns, "r").readlines()
  # quantityPatterns = [x.split(',')[0].strip() for x in quantityPatterns]
  # quantityPattern = "|".join(quantityPatterns)
  regex = re.compile(pattern)
  quantities = regex.findall(text)
  return quantities

def map_quantityPattern_to_quantity(quantityMapping, quantities):
  mappedQuantities = []
  for q in quantities:
    if q in quantityMapping:
      print q, quantityMapping[q]
      mappedQuantities.append(quantityMapping[q])
      continue
    
    if '/' in q:
      non_decimal = re.compile(r'[^\d.]+')
      fraction = non_decimal.sub(' ', q).split()

      numerator = float(fraction[0])
      denominator = float(fraction[1])
      result = numerator / denominator
      print q, result
      mappedQuantities.append(result)
      continue

    non_decimal = re.compile(r'[^\d.]+')
    decimal = float(non_decimal.sub('', q))
    # decimal = float(q.split(" ")[0])
    mappedQuantities.append(decimal)

  return mappedQuantities

def extract_quantities(text):
  quantityMapping = get_quantity_mapping() 

  quantityPatterns = open("quantitypatterns_english.txt", "r").readlines()
  quantityPattern = "|".join([x.split(',')[0].strip() for x in quantityPatterns])
  quantities_english = extract_quantities_with_pattern(text, quantityPattern)
  mapped_quantities_english = map_quantityPattern_to_quantity(quantityMapping, quantities_english)
  if len(mapped_quantities_english) > 0:
    quantities_english = mapped_quantities_english * pq.oz

  quantityPatterns = open("quantitypatterns_metric.txt", "r").readlines()
  quantityPattern = "|".join([x.split(',')[0].strip() for x in quantityPatterns])
  quantities_metric = extract_quantities_with_pattern(text, quantityPattern)
  mapped_quantities_metric = map_quantityPattern_to_quantity(quantityMapping, quantities_metric)
  if len(mapped_quantities_metric) > 0:
    quantities_metric = pq.Quantity(mapped_quantities_metric, "gram")

  return quantities_metric, quantities_english


def write_to_db(prices, quantities):
  pass

def index(htmlFile):
  # html_doc = open(htmlFile, 'r')
  # soup = BeautifulSoup(html_doc.read())
  html_doc = htmlFile
  soup = BeautifulSoup(html_doc)
  
  title = soup.title.string
  title = str(unicodedata.normalize('NFKD', title).encode('ascii', 'ignore'))
  print title
  h2 = soup.h2.text
  h2 = str(unicodedata.normalize('NFKD', h2).encode('ascii', 'ignore'))
  print h2
  posting = soup.find(id="postingbody")

  text = strip_tags(title + h2 + str(posting))
  # print text,"\n"
  prices = extract_prices(text)
  
  extracted_quantities = extract_quantities(text)

  if len(extracted_quantities[0]) > 0:
    print "metric quantities:", extracted_quantities[0]
  if len(extracted_quantities[1]) > 0:
    print "english quantities:", extracted_quantities[1]
  print "prices:", prices

  if len(extracted_quantities[0])==0 and len(extracted_quantities[1])==0 or len(prices)==0:
    print text
    sys.exit(1)

def index_from_db():
  cursor = db.cursor()
  cursor.execute("SELECT * FROM RawHTML")
  for row in cursor.fetchall():
    htmltext = row[2]
    print htmltext
    index(htmltext)

  htmlFile = sys.argv[1]
  index(htmlFile)

def index_from_file(fileName):
  filePath = os.path.join(positiveDir, fileName)
  if os.path.isfile(filePath):
    f = open(filePath, 'r')
    fileText = f.read()
    print filePath
    index(fileText)
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
    index_from_files()  


if __name__ == "__main__":
  main()