import numpy as np
import os
import re
import quantities as pq
import sys

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
  moneyPatterns = open("moneypatterns.txt", "r").readlines()
  moneyPatterns = [x.replace('$','').strip() for x in moneyPatterns]
  moneyPattern = "|".join(moneyPatterns)
  regex = re.compile(moneyPattern)
  prices = regex.findall(text)
  prices = [float(x.split(' ')[0].strip()) for x in prices]
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
      mappedQuantities.append(quantityMapping[q])
      continue
    
    if '/' in q:
      fraction = q.split(" ")[0].split("/")
      numerator = float(fraction[0])
      denominator = float(fraction[1])
      result = numerator / denominator
      mappedQuantities.append(result)
      continue

    decimal = float(q.split(" ")[0])
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
  posting = soup.find(id="postingbody")

  text = strip_tags(str(posting))
  # print text,"\n"
  prices = extract_prices(text)
  
  extracted_quantities = extract_quantities(text)
  if len(extracted_quantities[0])==len(prices):
    print "quantities:", extracted_quantities[0]
  elif len(extracted_quantities[1])==len(prices):
    print extracted_quantities[1]
  else:
    # print text
    pass
  print "prices:", prices

def index_from_db():
  cursor = db.cursor()
  cursor.execute("SELECT * FROM RawHTML")
  for row in cursor.fetchall():
    htmltext = row[2]
    print htmltext
    index(htmltext)

  htmlFile = sys.argv[1]
  index(htmlFile)

def index_from_files():
  files = os.listdir(positiveDir)
  for fileName in files:
    filePath = os.path.join(positiveDir, fileName)
    if os.path.isfile(filePath):
      f = open(filePath, 'r')
      fileText = f.read()
      index(fileText)


def main():
  index_from_files()


if __name__ == "__main__":
  main()