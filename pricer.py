import numpy as np
import os
import re
import quantities as pq
import sys
import unicodedata

from bs4 import BeautifulSoup
from HTMLParser import HTMLParser
import MySQLdb

def process_row(rowId, alt_quantities, alt_prices):
  print rowId, alt_quantities, alt_prices

def guess_prices():
  cursor = db.cursor()
  cursor.execute("SELECT * FROM rawhtml where alt_prices IS NOT NULL")
  for row in cursor.fetchall():
    rowId = row[0]
    alt_quantities = row[10]
    alt_prices = row[11]
    process_row(rowId, alt_quantities, alt_prices)

def main():
 guess_prices() 

if __name__ == "__main__":
  main()