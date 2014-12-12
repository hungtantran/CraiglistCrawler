import ast
import numpy as np
import os
import re
import quantities as pq
import sys
import unicodedata

from bs4 import BeautifulSoup
from HTMLParser import HTMLParser
import MySQLdb

db = MySQLdb.connect(host="pow-db.clfpwrv3fbfn.us-west-2.rds.amazonaws.com",
                     port=4200,user="cedro",
                     passwd="password",
                     db="powdb")
outfile = open('prices.txt', 'w')

def process_ounces(rowId, ounces, prices):
  pass

def process_grams(rowId, grams, prices, stddev, avg):
  low = stddev - avg
  hi = stddev + avg
  # print low, hi
  for quantity in grams:
    normalized_prices = [n for n in prices if n/quantity>5 and n/quantity<hi]
    if len(normalized_prices) == 0:
      outfile.write("{0},{1},,\n".format(rowId, quantity))
    else:
      for price in normalized_prices:
        outfile.write("{0},{1},{2}\n".format(rowId, quantity,price,price/quantity))
  pass

def parse_row(rowId, alt_quantities, alt_prices):
  quantity_regex = "(\[[0-9 .A-Za-z]*\])( g)?(\[[0-9 .A-Za-z+-]*\])( oz)?"
  regex = re.compile(quantity_regex)
  quantities_groups = regex.findall(alt_quantities)

  for group in quantities_groups:
    grams = [float(n) for n in filter(None,re.sub('[\[,\]]','', group[0]).split(' '))]
    ounces = [float(n) for n in filter(None,re.sub('[\[,\]]','', group[2]).split(' '))]
    prices = ast.literal_eval(alt_prices)

    cursor = db.cursor()
    cursor.execute("SELECT STDDEV(price_per_gram),AVG(price_per_gram),quantity FROM ((SELECT price_id,price/quantity AS 'price_per_gram', quantity from prices WHERE unit='gram') UNION (SELECT price_id,price/(quantity*28.3495) AS 'price_per_gram', quantity from prices WHERE unit='oz')) AS normalized;")
    row = cursor.fetchall()[0]
    stddev = row[0]
    avg = row[1]

    process_grams(rowId, grams, prices, stddev, avg)
    # process_ounces(rowId, ounces, prices)


def guess_prices():
  cursor = db.cursor()
  cursor.execute("SELECT * FROM rawhtml where alt_prices IS NOT NULL")
  for row in cursor.fetchall():
    rowId = row[0]
    alt_quantities = row[9]
    alt_prices = row[10]
    parse_row(rowId, alt_quantities, alt_prices)

def main():
 guess_prices() 

if __name__ == "__main__":
  main()